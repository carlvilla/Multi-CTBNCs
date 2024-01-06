package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * This class provides a method to compute the average local log-likelihood of each node of a Multi-CTBNC.
 */
public class AverageLocalLogLikelihood implements ConceptDriftScore {

    /**
     * A map that associates penalisation function names with their corresponding mathematical functions.
     */
    protected Map<String, DoubleUnaryOperator> penalisationFunctionMap = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put("BIC", N -> Math.log(N) / 2);
            put("AIC", N -> 1.0);
        }
    };
    String penalisationFunction;

    /**
     * Constructs an instance of AverageLocalLogLikelihood with a specified penalisation function.
     *
     * @param penalisationFunction name of the penalisation function
     */
    public AverageLocalLogLikelihood(String penalisationFunction) {
        this.penalisationFunction = penalisationFunction;
    }

    @Override
    public double[] compute(MultiCTBNC<CPTNode, CIMNode> model, Dataset newBatch, List<String> nameVariables) {
        // The parameters of a new model are learnt from the incoming batch
        MultiCTBNC<CPTNode, CIMNode> modelBatch = new MultiCTBNC<CPTNode, CIMNode>(model.getBN(), model.getCTBN());
        modelBatch.learnParameters(newBatch);
        double numSequences = newBatch.getNumDataPoints();
        double[] avgLocalLogLikelihoods = new double[model.getNumNodes()];
        for (int idxVariable = 0; idxVariable < nameVariables.size(); idxVariable++) {
            DiscreteStateNode node = (DiscreteStateNode) modelBatch.getNodeByName(nameVariables.get(idxVariable));
            double localLogLikelihood = node.estimateLogLikelihood();
            // New. A penalisation could be added to the score
            DoubleUnaryOperator penalisationFunction = this.penalisationFunctionMap.get(this.penalisationFunction);
            if (penalisationFunction != null) {
                // Overfitting is avoid by penalising the complexity of the network
                // Number of possible transitions
                int numStates = node.getNumStates();
                int numTransitions = (numStates - 1) * numStates;
                // Number of states of the parents
                int numStatesParents = node.getNumStatesParents();
                // Complexity of the network
                int networkComplexity = numTransitions * numStatesParents;
                // Sample size (number of sequences)
                int sampleSize = newBatch.getNumDataPoints();
                // Non-negative penalisation
                double penalisation = penalisationFunction.applyAsDouble(sampleSize);
                localLogLikelihood -= networkComplexity * penalisation;
            }
            avgLocalLogLikelihoods[idxVariable] = (1 / numSequences) * localLogLikelihood;
        }
        return avgLocalLogLikelihoods;
    }

}
