package multictbnc;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.MultiCTNBC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests the learning of multidimensional continuous-time naive Bayes classifiers.
 *
 * @author Carlos Villa Blanco
 */
class LearnMultiCTNBCTest {
	static Dataset dataset;
	static BNLearningAlgorithms bnLearningAlgs;
	static CTBNLearningAlgorithms ctbnLearningAlgs;

	@BeforeAll
	public static void setUp() {
		// Define dataset
		String nameTimeVariable = "Time";
		List<String> nameClassVariables = List.of("C1", "C2", "C3");
		// The data of the dataset is irrelevant. It is only necessary to run the
		// software
		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"Time", "F1", "F2", "F3", "F4", "C1", "C2", "C3"});
		dataSequence1.add(new String[]{"0.0", "a", "b", "c", "d", "1", "1", "1"});
		dataSequence1.add(new String[]{"0.5", "b", "c", "d", "e", "1", "1", "1"});
		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"Time", "F1", "F2", "F3", "F4", "C1", "C2", "C3"});
		dataSequence2.add(new String[]{"0.0", "a", "b", "c", "d", "2", "2", "2"});
		dataSequence2.add(new String[]{"0.5", "b", "c", "d", "e", "2", "2", "2"});
		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "No");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"Hill climbing", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
				CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0, 0.0);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				"Hill climbing", paramSLA);
		ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network.
	 */
	@Test
	public void learnModel() throws ErroneousValueException {
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTNBC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		multiCTBNC.learn(dataset);
		boolean[][] expectedAdjacencyMatrix = new boolean[][]{{false, false, false, false, false, false, false},
				{false, false, false, false, false, false, false}, {false, false, false, false, false, false, false},
				{false, false, false, false, false, false, false}, {true, true, true, true, false, false, false},
				{true, true, true, true, false, false, false}, {true, true, true, true, false, false, false}};
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

}