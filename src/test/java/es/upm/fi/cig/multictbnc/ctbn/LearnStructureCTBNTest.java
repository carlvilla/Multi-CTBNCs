package es.upm.fi.cig.multictbnc.ctbn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC.Digraph;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests the learning of the structure of a CTBN.
 *
 * @author Carlos Villa Blanco
 */
class LearnStructureCTBNTest {

	/**
	 * Learns a continuous-time Bayesian network. The dataset is formed by four binary feature variables V1, V2, V3 and
	 * V4. Each of the features depends on one variable in the following way V1->V2->V3->V4->V1. Thus, if V1 changes
	 * its
	 * state, the variable V2 will transition to another state.
	 */
	@Test
	public void learnContinuousTimeBayesianNetwork() throws ErroneousValueException {
		List<String> nameFeatureVariables = List.of("V1", "V2", "V3", "V4");
		String nameTimeVariable = "Time";
		List<String> nameClassVariables = List.of();

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"Time", "V1", "V2", "V3", "V4"});
		dataSequence1.add(new String[]{"0.0", "b", "a", "a", "a"});
		dataSequence1.add(new String[]{"0.2", "b", "b", "a", "a"});
		dataSequence1.add(new String[]{"0.4", "b", "b", "b", "a"});
		dataSequence1.add(new String[]{"0.6", "b", "b", "b", "b"});
		dataSequence1.add(new String[]{"0.8", "a", "b", "b", "b"});
		dataSequence1.add(new String[]{"0.8", "a", "a", "b", "b"});
		dataSequence1.add(new String[]{"1.0", "a", "a", "a", "b"});
		dataSequence1.add(new String[]{"1.0", "a", "a", "a", "a"});
		dataSequence1.add(new String[]{"1.2", "b", "a", "a", "a"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"Time", "V1", "V2", "V3", "V4"});
		dataSequence2.add(new String[]{"0.0", "b", "b", "b", "b"});
		dataSequence2.add(new String[]{"0.2", "a", "b", "b", "b"});
		dataSequence2.add(new String[]{"0.4", "a", "a", "b", "b"});
		dataSequence2.add(new String[]{"0.6", "a", "a", "a", "b"});
		dataSequence2.add(new String[]{"0.8", "a", "a", "a", "a"});
		dataSequence2.add(new String[]{"0.8", "b", "a", "a", "a"});
		dataSequence2.add(new String[]{"1.0", "b", "b", "a", "a"});
		dataSequence2.add(new String[]{"1.0", "b", "b", "b", "a"});
		dataSequence2.add(new String[]{"1.2", "b", "b", "b", "b"});

		List<String[]> dataSequence3 = new ArrayList<>();
		dataSequence3.add(new String[]{"Time", "V1", "V2", "V3", "V4"});
		dataSequence3.add(new String[]{"0.0", "a", "a", "b", "b"});
		dataSequence3.add(new String[]{"0.2", "a", "a", "a", "b"});
		dataSequence3.add(new String[]{"0.4", "a", "a", "a", "a"});
		dataSequence3.add(new String[]{"0.6", "b", "a", "a", "a"});
		dataSequence3.add(new String[]{"0.8", "b", "b", "a", "a"});
		dataSequence3.add(new String[]{"0.8", "b", "b", "b", "a"});
		dataSequence3.add(new String[]{"1.0", "b", "b", "b", "b"});
		dataSequence3.add(new String[]{"1.0", "a", "b", "b", "b"});
		dataSequence3.add(new String[]{"1.2", "a", "a", "b", "b"});

		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);

		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
				CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0, 0.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "No");
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				"Hill climbing", paramSLA);

		CTBNLearningAlgorithms learningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm);
		StructureConstraints strucConst = new Digraph();

		CTBN<CIMNode> ctbn = new CTBN<>(dataset, nameFeatureVariables, learningAlgs, strucConst, CIMNode.class);
		ctbn.learn();

		boolean[][] expectedAdjacencyMatrix = new boolean[][]{{false, true, false, false}, {false, false, true, false},
				{false, false, false, true}, {true, false, false, false}};
		assertArrayEquals(expectedAdjacencyMatrix, ctbn.getAdjacencyMatrix());
	}

}