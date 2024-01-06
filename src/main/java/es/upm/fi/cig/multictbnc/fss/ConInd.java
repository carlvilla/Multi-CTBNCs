package es.upm.fi.cig.multictbnc.fss;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.nodes.AbstractNode;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.NodeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements the ConInd online feature subset selection algorithm from Yu et al. 2018.
 */
public class ConInd extends StatisticalBasedFeatureSelection implements OnlineFeatureSubsetSelection {
    // Define factories to build the nodes
    private NodeFactory<CPTNode> factoryCPTNodes = NodeFactory.createFactory(CPTNode.class);
    private NodeFactory<CIMNode> factoryCIMNodes = NodeFactory.createFactory(CIMNode.class);
    private boolean lastExecutionYieldAnyChange;
    private Logger logger = LogManager.getLogger(ConInd.class);


    /**
     * Constructs a {@code ConInd} object.
     *
     * @param nameClassVariables                  list of names of class variables
     * @param cimPLA                              parameter learning algorithm for CIM nodes
     * @param maxSeparatingSizeRedundancyAnalysis maximum size of separating sets in redundancy analysis
     * @param sigTimeTransitionHyp                significance level for time transition hypothesis tests
     * @param sigStateToStateTransitionHyp        significance level for state-to-state transition hypothesis tests
     */
    public ConInd(List<String> nameClassVariables, ParameterLearningAlgorithm cimPLA,
                  int maxSeparatingSizeRedundancyAnalysis, double sigTimeTransitionHyp,
                  double sigStateToStateTransitionHyp) {
        super(nameClassVariables, cimPLA, maxSeparatingSizeRedundancyAnalysis, sigTimeTransitionHyp,
                sigStateToStateTransitionHyp);
    }

    @Override
    public SubsetSelectedFeatures execute(String newVariable, Dataset dataBatch) {
        if (dataBatch == null) {
            this.logger.error("No dataset was provided to perform online feature subset selection");
            return null;
        }
        this.logger.info("Performing online feature subset selection (ConInd) after receiving variable: {}",
                newVariable);
        // Measure time to perform the feature selection
        Instant start = Instant.now();
        setDataset(dataBatch);
        List<CIMNode> featureNodes = featureSubsetSelectionGivenClassVariable(newVariable, dataBatch);
        Instant end = Instant.now();
        double executionTime = Duration.between(start, end).toMillis() / 1000.f;
        List<String> nameSelectedFeatureNodes = featureNodes.stream().map(AbstractNode::getName).collect(
                Collectors.toList());
        this.logger.info("Subset of selected features: {}", nameSelectedFeatureNodes);
        this.logger.info("Feature subset selection performed in {}", executionTime);
        return new SubsetSelectedFeatures(nameSelectedFeatureNodes, executionTime);
    }

    /**
     * Returns whether the last execution of the feature subset selection algorithm resulted in any changes.
     *
     * @return true if the last execution yielded a change, false otherwise
     */
    public boolean getLastExecutionYieldAnyChange() {
        return this.lastExecutionYieldAnyChange;
    }

    /**
     * Sets the flag indicating whether the last execution resulted in any changes.
     *
     * @param anyChange a boolean value to set the flag
     */
    private void setLastExecutionYieldAnyChange(boolean anyChange) {
        this.lastExecutionYieldAnyChange = anyChange;
    }

    /**
     * Sets the current feature variables for the algorithm.
     *
     * @param nameFeatureVariables list of names of current feature variables
     */
    public void setCurrentFeatureVariables(List<String> nameFeatureVariables) {
        this.nameFeatureVariables = nameFeatureVariables;
    }

    /**
     * Performs feature subset selection given a new feature variable.
     *
     * @param nameNewFeatureVariable the name of the new feature variable
     * @param dataBatch              the dataset on which the selection is based
     * @return a list of {@code CIMNode} objects representing the selected feature nodes
     */
    protected List<CIMNode> featureSubsetSelectionGivenClassVariable(String nameNewFeatureVariable,
                                                                     Dataset dataBatch) {
        setLastExecutionYieldAnyChange(false);
        String nameClassVariable = dataBatch.getNameClassVariables().get(0);
        List<String> nameCurrentFeatureVariables = new ArrayList<>(dataBatch.getNameFeatureVariables());
        nameCurrentFeatureVariables.remove(nameNewFeatureVariable);
        // Generate class variable node
        CPTNode classNode = this.factoryCPTNodes.createNode(nameClassVariable, dataBatch);
        // Generate feature nodes
        List<CIMNode> featureNodes = new ArrayList<CIMNode>();
        for (String nameFeatureVariable : nameCurrentFeatureVariables)
            featureNodes.add(this.factoryCIMNodes.createNode(nameFeatureVariable, dataBatch));
        CIMNode newFeatureNode = this.factoryCIMNodes.createNode(nameNewFeatureVariable, dataBatch);
        // Relevance analysis on the new feature variable
        logger.info("---- Relevance analysis on {} ----", newFeatureNode.getName());
        boolean newFeatureIsRelevant = relevanceAnalysis(newFeatureNode, classNode);
        if (newFeatureIsRelevant) {
            logger.info("---- Redundancy analysis on {} ----", newFeatureNode.getName());
            boolean newFeatureIsRedundant = redundancyAnalysis(newFeatureNode, classNode, featureNodes, 1);
            if (!newFeatureIsRedundant) {
                // Check if older feature variables are redundant given new feature variable (Single-conditional
                // redundancy analysis)
                logger.info("---- First redundancy analysis on current feature variables (except new one) ----");
                featureNodes = redundancyAnalysis(featureNodes, classNode, newFeatureNode);
                // Check if any feature variable is redundant given any other set of feature variables
                // (Multi-conditional redundancy analysis)
                logger.info("---- Second redundancy analysis on current feature variables (except new one) ----");
                featureNodes.add(newFeatureNode);
                featureNodes = redundancyAnalysis(featureNodes, classNode);
                // Add new relevant feature variable to final selected feature subset
                setLastExecutionYieldAnyChange(true);
            }
        }
        return featureNodes;
    }

    /**
     * Conducts a relevance analysis to determine if a new feature is relevant to the class variable.
     *
     * @param newFeatureNode the new feature node to be analyzed
     * @param classNode      the class node
     * @return {@code true} if the new feature is relevant, {@code false} otherwise
     */
    private boolean relevanceAnalysis(CIMNode newFeatureNode, CPTNode classNode) {
        this.logger.info("Relevance analysis...");
        // unconditional independence test
        boolean areUncondIndep = conditionalIndependenceTest(newFeatureNode, classNode, List.of());
        if (!areUncondIndep) {
            this.logger.info("Feature variable {} was found relevant to class variable {}", newFeatureNode.getName(),
                    classNode.getName());
            return true;
        }
        this.logger.info("Feature variable {} was found irrelevant", newFeatureNode.getName());
        return false;
    }

}
