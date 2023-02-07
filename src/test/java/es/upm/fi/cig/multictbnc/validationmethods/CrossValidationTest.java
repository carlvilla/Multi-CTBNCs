package es.upm.fi.cig.multictbnc.validationmethods;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the cross-validation when solving multidimensional method both using a single Multi-CTBNC or a binary relevance
 * approach.
 *
 * @author Carlos Villa Blanco
 */
public class CrossValidationTest {
	static String pathDataset = "src/test/resources/validationmethods/dataset";
	static String timeVariable = "t";
	static List<String> classVariables = List.of("C1", "C2", "C3");
	static List<String> featureVariables = List.of("X1", "X2", "X3", "X4", "X5");
	static DatasetReader datasetReader;
	static int numFolds = 5;
	boolean estimateProbabilities = true;
	boolean shuffleSequences = true;
	Long seed = 508102L;

	@BeforeAll
	public static void setUp() throws FileNotFoundException, UnreadDatasetException {
		datasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset, 0);
		datasetReader.setVariables(timeVariable, classVariables, featureVariables);
	}

	@Test
	void testCrossValidationHillClimbing() throws UnreadDatasetException, ErroneousValueException {
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNC();
		// Cross-validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation",
				datasetReader, null, 0, numFolds, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.3390, resultsCV.get("Global accuracy"), 0.0001);
		assertEquals(0.6730, resultsCV.get("Mean accuracy"), 0.0001);
		assertEquals(0.5172, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.4795, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.4637, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6730, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6730, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6730, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8281, resultsCV.get("Global Brier score"), 0.0001);
		// Cross-validation of binary relevance approach
		ValidationMethod validationMethodBinaryRelevance = ValidationMethodFactory.getValidationMethod(
				"Binary relevance cross-validation", datasetReader, null, 0, numFolds, estimateProbabilities,
				shuffleSequences, seed);
		Map<String, Double> resultsCVBR = validationMethodBinaryRelevance.evaluate(multiCTBNC);
		assertEquals(0.2680, resultsCVBR.get("Global accuracy"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Mean accuracy"), 0.0001);
		assertEquals(0.3741, resultsCVBR.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.4499, resultsCVBR.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.3918, resultsCVBR.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8503, resultsCVBR.get("Global Brier score"), 0.0001);
		// Compare results of learning one Multi-CTBNC and several independent CTBNCs
		assertTrue(resultsCV.get("Global accuracy") > resultsCVBR.get("Global accuracy"));
		assertTrue(resultsCV.get("Mean accuracy") > resultsCVBR.get("Mean accuracy"));
		assertTrue(resultsCV.get("Macro-averaged precision") > resultsCVBR.get("Macro-averaged precision"));
		assertTrue(resultsCV.get("Macro-averaged recall") > resultsCVBR.get("Macro-averaged " + "recall"));
		assertTrue(resultsCV.get("Macro-averaged F1 score") > resultsCVBR.get("Macro-averaged F1 score"));
		assertTrue(resultsCV.get("Micro-averaged precision") > resultsCVBR.get("Micro-averaged precision"));
		assertTrue(resultsCV.get("Micro-averaged recall") > resultsCVBR.get("Micro-averaged " + "recall"));
		assertTrue(resultsCV.get("Micro-averaged F1 score") > resultsCVBR.get("Micro-averaged F1 score"));
		assertTrue(resultsCV.get("Global Brier score") < resultsCVBR.get("Global Brier score"));
	}

	@Test
	void testFoldCrossValidationCTPC() throws UnreadDatasetException, ErroneousValueException {
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNCLearnedWithCTPC();
		// Cross-validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation",
				datasetReader, null, 0, numFolds, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.3660, resultsCV.get("Global accuracy"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Mean accuracy"), 0.0001);
		assertEquals(0.6666, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.5714, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.5851, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.7811, resultsCV.get("Global Brier score"), 0.0001);
		// Cross-validation of binary relevance approach
		ValidationMethod validationMethodBinaryRelevance = ValidationMethodFactory.getValidationMethod(
				"Binary relevance cross-validation", datasetReader, null, 0, numFolds, estimateProbabilities,
				shuffleSequences, seed);
		Map<String, Double> resultsCVBR = validationMethodBinaryRelevance.evaluate(multiCTBNC);
		assertEquals(0.2870, resultsCVBR.get("Global accuracy"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Mean accuracy"), 0.0001);
		assertEquals(0.5321, resultsCVBR.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.5416, resultsCVBR.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.5133, resultsCVBR.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8037, resultsCVBR.get("Global Brier score"), 0.0001);
		// Compare results of learning one Multi-CTBNC and several independent CTBNCs
		assertTrue(resultsCV.get("Global accuracy") > resultsCVBR.get("Global accuracy"));
		assertTrue(resultsCV.get("Mean accuracy") > resultsCVBR.get("Mean accuracy"));
		assertTrue(resultsCV.get("Macro-averaged precision") > resultsCVBR.get("Macro-averaged precision"));
		assertTrue(resultsCV.get("Macro-averaged recall") > resultsCVBR.get("Macro-averaged " + "recall"));
		assertTrue(resultsCV.get("Macro-averaged F1 score") > resultsCVBR.get("Macro-averaged F1 score"));
		assertTrue(resultsCV.get("Micro-averaged precision") > resultsCVBR.get("Micro-averaged precision"));
		assertTrue(resultsCV.get("Micro-averaged recall") > resultsCVBR.get("Micro-averaged " + "recall"));
		assertTrue(resultsCV.get("Micro-averaged F1 score") > resultsCVBR.get("Micro-averaged F1 score"));
		assertTrue(resultsCV.get("Global Brier score") < resultsCVBR.get("Global Brier score"));
	}

	@Test
	void testFoldCrossValidationMBCTPC() throws UnreadDatasetException, ErroneousValueException {
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNCLearnedWithCTPC();
		// Cross-validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation",
				datasetReader, null, 0, numFolds, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.3660, resultsCV.get("Global accuracy"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Mean accuracy"), 0.0001);
		assertEquals(0.6666, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.5714, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.5851, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.7067, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.7811, resultsCV.get("Global Brier score"), 0.0001);
		// Cross-validation of binary relevance approach
		ValidationMethod validationMethodBinaryRelevance = ValidationMethodFactory.getValidationMethod(
				"Binary relevance cross-validation", datasetReader, null, 0, numFolds, estimateProbabilities,
				shuffleSequences, seed);
		Map<String, Double> resultsCVBR = validationMethodBinaryRelevance.evaluate(multiCTBNC);
		assertEquals(0.2870, resultsCVBR.get("Global accuracy"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Mean accuracy"), 0.0001);
		assertEquals(0.5321, resultsCVBR.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.5416, resultsCVBR.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.5133, resultsCVBR.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6803, resultsCVBR.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8037, resultsCVBR.get("Global Brier score"), 0.0001);
		// Compare results of learning one Multi-CTBNC and several independent CTBNCs
		assertTrue(resultsCV.get("Global accuracy") > resultsCVBR.get("Global accuracy"));
		assertTrue(resultsCV.get("Mean accuracy") > resultsCVBR.get("Mean accuracy"));
		assertTrue(resultsCV.get("Macro-averaged precision") > resultsCVBR.get("Macro-averaged precision"));
		assertTrue(resultsCV.get("Macro-averaged recall") > resultsCVBR.get("Macro-averaged " + "recall"));
		assertTrue(resultsCV.get("Macro-averaged F1 score") > resultsCVBR.get("Macro-averaged F1 score"));
		assertTrue(resultsCV.get("Micro-averaged precision") > resultsCVBR.get("Micro-averaged precision"));
		assertTrue(resultsCV.get("Micro-averaged recall") > resultsCVBR.get("Micro-averaged " + "recall"));
		assertTrue(resultsCV.get("Micro-averaged F1 score") > resultsCVBR.get("Micro-averaged F1 score"));
		assertTrue(resultsCV.get("Global Brier score") < resultsCVBR.get("Global Brier score"));
	}

	@Test
	void testFoldCrossValidationHybridAlgorithm() throws UnreadDatasetException,
			ErroneousValueException {
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNCLearnedWithHybridAlgorithm();
		// Cross-validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation",
				datasetReader, null, 0, numFolds, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.3390, resultsCV.get("Global accuracy"), 0.0001);
		assertEquals(0.6703, resultsCV.get("Mean accuracy"), 0.0001);
		assertEquals(0.5085, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.4796, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.4635, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6703, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6703, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6703, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8294, resultsCV.get("Global Brier score"), 0.0001);
		// Cross-validation of binary relevance approach
		ValidationMethod validationMethodBR = ValidationMethodFactory.getValidationMethod(
				"Binary relevance cross-validation", datasetReader, null, 0, numFolds, estimateProbabilities,
				shuffleSequences, seed);
		Map<String, Double> resultsCVBR = validationMethodBR.evaluate(multiCTBNC);
		assertEquals(0.2680, resultsCVBR.get("Global accuracy"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Mean accuracy"), 0.0001);
		assertEquals(0.3741, resultsCVBR.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.4499, resultsCVBR.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.3918, resultsCVBR.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6440, resultsCVBR.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8503, resultsCVBR.get("Global Brier score"), 0.0001);
		// Compare results of learning one Multi-CTBNC and several independent CTBNCs
		assertTrue(resultsCV.get("Global accuracy") > resultsCVBR.get("Global accuracy"));
		assertTrue(resultsCV.get("Mean accuracy") > resultsCVBR.get("Mean accuracy"));
		assertTrue(resultsCV.get("Macro-averaged precision") > resultsCVBR.get("Macro-averaged precision"));
		assertTrue(resultsCV.get("Macro-averaged recall") > resultsCVBR.get("Macro-averaged " + "recall"));
		assertTrue(resultsCV.get("Macro-averaged F1 score") > resultsCVBR.get("Macro-averaged F1 score"));
		assertTrue(resultsCV.get("Micro-averaged precision") > resultsCVBR.get("Micro-averaged precision"));
		assertTrue(resultsCV.get("Micro-averaged recall") > resultsCVBR.get("Micro-averaged " + "recall"));
		assertTrue(resultsCV.get("Micro-averaged F1 score") > resultsCVBR.get("Micro-averaged F1 score"));
		assertTrue(resultsCV.get("Global Brier score") < resultsCVBR.get("Global Brier score"));
	}

	// Compare CTPC and hill climbing when states feature variables change
	@Test
	void testCTPCAndHillClimbingIncreasingCardinalityFeatureStates()
			throws UnreadDatasetException, ErroneousValueException, FileNotFoundException {
		int numFolds = 5;
		// Compare results of learning with hill climbing and CTPC
		MultiCTBNC<CPTNode, CIMNode> multiCTBNCHillClimbing = ClassifierFactory.getMultiCTBNC();
		MultiCTBNC<CPTNode, CIMNode> multiCTBNCCTPC = ClassifierFactory.getMultiCTBNCLearnedWithCTPC();
		// Extract datasets where feature variables have either 4 or 15 possible states
		String pathDataset4States = "src/test/resources/validationmethods/dataset4states";
		String pathDataset15States = "src/test/resources/validationmethods/dataset15states";
		DatasetReader datasetReader4States = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset4States,
				0);
		DatasetReader datasetReader20States = DatasetReaderFactory.getDatasetReader("Multiple CSV",
				pathDataset15States,
				0);
		List<String> featureVariables = List.of("X1", "X2", "X3", "X4", "X5");
		datasetReader4States.setVariables(timeVariable, classVariables, featureVariables);
		datasetReader20States.setVariables(timeVariable, classVariables, featureVariables);
		// Learn the models using the dataset with feature variables of 4 states
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation",
				datasetReader4States, null, 0, numFolds, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsHillClimbing = validationMethod.evaluate(multiCTBNCHillClimbing);
		Map<String, Double> resultsCTPC = validationMethod.evaluate(multiCTBNCCTPC);
		assertTrue(resultsHillClimbing.get("Global accuracy") > resultsCTPC.get("Global accuracy"));
		assertTrue(resultsHillClimbing.get("Mean accuracy") > resultsCTPC.get("Mean accuracy"));
		assertTrue(resultsHillClimbing.get("Macro-averaged precision") > resultsCTPC.get("Macro-averaged precision"));
		assertTrue(resultsHillClimbing.get("Macro-averaged recall") > resultsCTPC.get("Macro-averaged " + "recall"));
		assertTrue(resultsHillClimbing.get("Macro-averaged F1 score") > resultsCTPC.get("Macro-averaged F1 score"));
		assertTrue(resultsHillClimbing.get("Micro-averaged precision") > resultsCTPC.get("Micro-averaged precision"));
		assertTrue(resultsHillClimbing.get("Micro-averaged recall") > resultsCTPC.get("Micro-averaged " + "recall"));
		assertTrue(resultsHillClimbing.get("Micro-averaged F1 score") > resultsCTPC.get("Micro-averaged F1 score"));
		assertTrue(resultsHillClimbing.get("Global Brier score") < resultsCTPC.get("Global Brier score"));
		// Learn the models using the dataset with feature variables of 15 states
		validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation", datasetReader20States, null,
				0, numFolds, estimateProbabilities, shuffleSequences, seed);
		resultsHillClimbing = validationMethod.evaluate(multiCTBNCHillClimbing);
		resultsCTPC = validationMethod.evaluate(multiCTBNCCTPC);
		assertTrue(resultsHillClimbing.get("Global accuracy") < resultsCTPC.get("Global accuracy"));
		assertTrue(resultsHillClimbing.get("Mean accuracy") < resultsCTPC.get("Mean accuracy"));
		assertTrue(resultsHillClimbing.get("Macro-averaged precision") < resultsCTPC.get("Macro-averaged precision"));
		assertTrue(resultsHillClimbing.get("Macro-averaged recall") < resultsCTPC.get("Macro-averaged " + "recall"));
		assertTrue(resultsHillClimbing.get("Macro-averaged F1 score") < resultsCTPC.get("Macro-averaged F1 score"));
		assertTrue(resultsHillClimbing.get("Micro-averaged precision") < resultsCTPC.get("Micro-averaged precision"));
		assertTrue(resultsHillClimbing.get("Micro-averaged recall") < resultsCTPC.get("Micro-averaged " + "recall"));
		assertTrue(resultsHillClimbing.get("Micro-averaged F1 score") < resultsCTPC.get("Micro-averaged F1 score"));
		assertTrue(resultsHillClimbing.get("Global Brier score") > resultsCTPC.get("Global Brier score"));
	}
}