package es.upm.fi.cig.multictbnc.multictbnc;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.MultipleCSVReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class LearnOneDimensionalProblem {
	static Dataset dataset;
	static BNLearningAlgorithms bnLearningAlgs;
	static CTBNLearningAlgorithms ctbnLearningAlgs;
	static boolean[][] expectedAdjacencyMatrix;

	/**
	 * This method reads a training dataset that will be used to generate a CTBNC. Five discrete-state variables are
	 * used, four feature variables (X1, X2, X3 and X4) and one class variables (C1). The data were sampled from a
	 * Multi-CTBNC with the following structure: X4 <- X3 -> X2 <-> X1 <- C1 -> C2 -> X2
	 *
	 * @throws FileNotFoundException  thrown if a file was not found
	 * @throws UnreadDatasetException thrown if a dataset could not be read
	 */
	@BeforeAll
	public static void setUp() throws FileNotFoundException, UnreadDatasetException {
		// Define dataset
		String nameTimeVariable = "t";
		List<String> nameClassVariables = List.of("C1");
		List<String> nameFeatureVariables = List.of("X1", "X2", "X3", "X4");
		String pathDataset = "src/test/resources/multictbnc/trainingdata";
		DatasetReader dr = new MultipleCSVReader(pathDataset);
		dr.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
		dataset = dr.readDataset();
		// As C2 is not considered, the model will find C1 to be the parent of X2
		expectedAdjacencyMatrix = new boolean[][]{{false, true, false, false, false},
				{true, false, false, false, false}, {false, true, false, true, false},
				{false, false, false, false, false}, {true, true, false, false, false}};
	}

	/**
	 * Learns a continuous-time Bayesian network classifier using hill climbing and log-likelihood as score.
	 */
	@Test
	public void learnModel() throws ErroneousValueException {
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "BIC");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"Hill climbing", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
				CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0, 0.0);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				"Hill climbing", paramSLA);
		ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
		MultiCTBNC<CPTNode, CIMNode> CTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		CTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, CTBNC.getAdjacencyMatrix());
	}
}
