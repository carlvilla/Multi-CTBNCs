package multictbnc;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests to evaluate the learning of multidimensional continuous-time Bayesian networks classifiers.
 *
 * @author Carlos Villa Blanco
 */
class LearnMultiCTBNCTest {
	static Dataset dataset;
	static BNLearningAlgorithms bnLearningAlgs;
	static CTBNLearningAlgorithms ctbnLearningAlgs;
	static boolean[][] expectedAdjacencyMatrix;

	/**
	 * This method reads a training dataset that will be used to generate Multi-CTBNC under different learning
	 * algorithms. The dataset is formed by five discrete-state variables, three feature variables (X1, X2 and X3) and
	 * two class variables (C1 and C2). The data were sampled from a Multi-CTBNC with the following structure: X1 <- C1
	 * -> C2 -> X2 <-> X3 <- X1.
	 *
	 * @throws FileNotFoundException  thrown if a file was not found
	 * @throws UnreadDatasetException thrown if a dataset could not be read
	 */
	@BeforeAll
	public static void setUp() throws FileNotFoundException, UnreadDatasetException {
		// Define dataset
		String nameTimeVariable = "t";
		List<String> nameClassVariables = List.of("C1", "C2");
		List<String> nameFeatureVariables = List.of("X1", "X2", "X3", "X4");
		String pathDataset = "src/test/resources/multictbnc/trainingdata";
		DatasetReader dr = new MultipleCSVReader(pathDataset);
		dr.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
		dataset = dr.readDataset();
		expectedAdjacencyMatrix = new boolean[][]{{false, true, false, false, false, false},
				{true, false, false, false, false, false}, {false, true, false, true, false, false},
				{false, false, false, false, false, false}, {true, false, false, false, false, true},
				{false, true, false, false, false, false}};
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using hill climbing and log-likelihood as score.
	 */
	@Test
	public void learnModelBIC() throws ErroneousValueException {
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "BIC");
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
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using hill climbing and the Bayesian Dirichlet
	 * equivalent score.
	 */
	@Test
	public void learnModelBDe() throws ErroneousValueException {
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Bayesian Dirichlet equivalent");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"Hill climbing", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
				CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0, 0.001);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				"Hill climbing", paramSLA);
		ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using tabu search and log-likelihood as score.
	 */
	@Test
	public void learnModelTabuSearch() throws ErroneousValueException {
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "BIC",
				"tabuListSize", "3");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"Tabu search", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
				CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0, 0.0);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				"Tabu search", paramSLA);
		ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using hill climbing with random restart and
	 * log-likelihood as score.
	 */
	@Test
	public void learnModelHillClimbingRandomRestart() throws ErroneousValueException {
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0);
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "BIC",
				"numRestarts", "3");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"Random-restart hill climbing", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
				CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0, 0.0);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				"Random-restart hill climbing", paramSLA);
		ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using the continuous-time PC algorithm.
	 */
	@Test
	public void learnModelCTPC() throws ErroneousValueException {
		// Set algorithms to learn model
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0);
		Map<String, String> paramSLA = new HashMap<>();
		paramSLA.put("significancePC", "0.05");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"CTPC", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0, 0.0001);
		paramSLA.put("sigTimeTransitionHyp", "0.00001");
		paramSLA.put("sigStateToStateTransitionHyp", "0.00001");
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN("CTPC", paramSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using the Markov blanket-based continuous-time PC
	 * algorithm.
	 */
	@Test
	public void learnModelMBCTPC() throws ErroneousValueException {
		// Set algorithms to learn model
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0);
		Map<String, String> paramSLA = new HashMap<>();
		paramSLA.put("significancePC", "0.05");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"MB-CTPC", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0, 0.0001);
		paramSLA.put("sigTimeTransitionHyp", "0.00001");
		paramSLA.put("sigStateToStateTransitionHyp", "0.00001");
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN("MB-CTPC", paramSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		// The arc from X3 to X4 is ignored
		boolean[][] expectedAdjacencyMatrixMBCTPC = new boolean[][]{{false, true, false, false, false, false},
				{true, false, false, false, false, false}, {false, true, false, false, false, false},
				{false, false, false, false, false, false}, {true, false, false, false, false, true},
				{false, true, false, false, false, false}};
		assertArrayEquals(expectedAdjacencyMatrixMBCTPC, multiCTBNC.getAdjacencyMatrix());
	}

	/**
	 * Learns a multidimensional continuous-time Bayesian network using a hybrid algorithm.
	 */
	@Test
	public void learnModelHybridAlgorithm() throws ErroneousValueException {
		// Set algorithms to learn model
		// Algorithms to learn BN (class subgraph of Multi-CTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0);
		Map<String, String> paramSLA = new HashMap<>();
		paramSLA.put("scoreFunction", "Log-likelihood");
		paramSLA.put("penalisationFunction", "BIC");
		paramSLA.put("significancePC", "0.05");
		paramSLA.put("maxSizeSepSet", "1");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
				"Hybrid algorithm", paramSLA);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(
				"Bayesian estimation", 1.0, 0.0001);
		paramSLA.put("sigTimeTransitionHyp", "0.00001");
		paramSLA.put("sigStateToStateTransitionHyp", "0.0001");
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN("Hybrid algorithm",
				paramSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		// Define model
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		// Learn model
		multiCTBNC.learn(dataset);
		assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
	}

}