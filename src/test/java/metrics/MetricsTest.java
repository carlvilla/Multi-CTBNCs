package metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.performance.Metrics;

/**
 * Tests the evaluation metrics.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MetricsTest {
	static Dataset actualDataset;
	static Prediction[] perfectPredictions;
	static Prediction[] perfectPredictionWithUncertainty;
	static Prediction[] twoPartiallyIncorrectPrediction;
	static Prediction[] predictionsWithTwoCorrectClasses;

	/**
	 * Defines some predictions and actual values that will be used to evaluate the
	 * metrics. There are three classes, two binaries and one ternary.
	 */
	@BeforeAll
	public static void setUp() {
		// Define actual dataset
		String timeVariable = "Time";
		List<String> classVariables = List.of("C1", "C2", "C3");
		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence1.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.1", "a", "a", "a" });
		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence2.add(new String[] { "0.0", "a", "b", "a" });
		dataSequence2.add(new String[] { "0.1", "a", "b", "a" });
		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence3.add(new String[] { "0.0", "b", "b", "c" });
		dataSequence3.add(new String[] { "0.1", "b", "b", "c" });
		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence4.add(new String[] { "0.0", "b", "b", "a" });
		dataSequence4.add(new String[] { "0.1", "b", "b", "a" });
		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence5.add(new String[] { "0.0", "b", "c", "c" });
		dataSequence5.add(new String[] { "0.1", "b", "c", "c" });
		actualDataset = new Dataset(timeVariable, classVariables);
		actualDataset.addSequence(dataSequence1);
		actualDataset.addSequence(dataSequence2);
		actualDataset.addSequence(dataSequence3);
		actualDataset.addSequence(dataSequence4);
		actualDataset.addSequence(dataSequence5);

		// Define all possible class configurations
		State ccAAA = new State();
		ccAAA.addEvent("C1", "a");
		ccAAA.addEvent("C2", "a");
		ccAAA.addEvent("C3", "a");
		State ccABA = new State();
		ccABA.addEvent("C1", "a");
		ccABA.addEvent("C2", "b");
		ccABA.addEvent("C3", "a");
		State ccACA = new State();
		ccACA.addEvent("C1", "a");
		ccACA.addEvent("C2", "c");
		ccACA.addEvent("C3", "a");
		State ccAAC = new State();
		ccAAC.addEvent("C1", "a");
		ccAAC.addEvent("C2", "a");
		ccAAC.addEvent("C3", "c");
		State ccABC = new State();
		ccABC.addEvent("C1", "a");
		ccABC.addEvent("C2", "b");
		ccABC.addEvent("C3", "c");
		State ccACC = new State();
		ccACC.addEvent("C1", "a");
		ccACC.addEvent("C2", "c");
		ccACC.addEvent("C3", "c");
		State ccBAA = new State();
		ccBAA.addEvent("C1", "b");
		ccBAA.addEvent("C2", "a");
		ccBAA.addEvent("C3", "a");
		State ccBBA = new State();
		ccBBA.addEvent("C1", "b");
		ccBBA.addEvent("C2", "b");
		ccBBA.addEvent("C3", "a");
		State ccBCA = new State();
		ccBCA.addEvent("C1", "b");
		ccBCA.addEvent("C2", "c");
		ccBCA.addEvent("C3", "a");
		State ccBAC = new State();
		ccBAC.addEvent("C1", "b");
		ccBAC.addEvent("C2", "a");
		ccBAC.addEvent("C3", "c");
		State ccBBC = new State();
		ccBBC.addEvent("C1", "b");
		ccBBC.addEvent("C2", "b");
		ccBBC.addEvent("C3", "c");
		State ccBCC = new State();
		ccBCC.addEvent("C1", "b");
		ccBCC.addEvent("C2", "c");
		ccBCC.addEvent("C3", "c");

		// --------------- Perfect classification ---------------
		perfectPredictions = new Prediction[5];
		Prediction prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccAAA);
		prediction1.setProbability(ccAAA, 1.0);
		prediction1.setProbability(ccABA, 0.0);
		prediction1.setProbability(ccACA, 0.0);
		prediction1.setProbability(ccAAC, 0.0);
		prediction1.setProbability(ccABC, 0.0);
		prediction1.setProbability(ccACC, 0.0);
		prediction1.setProbability(ccBAA, 0.0);
		prediction1.setProbability(ccBBA, 0.0);
		prediction1.setProbability(ccBCA, 0.0);
		prediction1.setProbability(ccBAC, 0.0);
		prediction1.setProbability(ccBBC, 0.0);
		prediction1.setProbability(ccBCC, 0.0);
		perfectPredictions[0] = prediction1;
		Prediction prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccABA);
		prediction2.setProbability(ccAAA, 0.0);
		prediction2.setProbability(ccABA, 1.0);
		prediction2.setProbability(ccACA, 0.0);
		prediction2.setProbability(ccAAC, 0.0);
		prediction2.setProbability(ccABC, 0.0);
		prediction2.setProbability(ccACC, 0.0);
		prediction2.setProbability(ccBAA, 0.0);
		prediction2.setProbability(ccBBA, 0.0);
		prediction2.setProbability(ccBCA, 0.0);
		prediction2.setProbability(ccBAC, 0.0);
		prediction2.setProbability(ccBBC, 0.0);
		prediction2.setProbability(ccBCC, 0.0);
		perfectPredictions[1] = prediction2;
		Prediction prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccBBC);
		prediction3.setProbability(ccAAA, 0.0);
		prediction3.setProbability(ccABA, 0.0);
		prediction3.setProbability(ccACA, 0.0);
		prediction3.setProbability(ccAAC, 0.0);
		prediction3.setProbability(ccABC, 0.0);
		prediction3.setProbability(ccACC, 0.0);
		prediction3.setProbability(ccBAA, 0.0);
		prediction3.setProbability(ccBBA, 0.0);
		prediction3.setProbability(ccBCA, 0.0);
		prediction3.setProbability(ccBAC, 0.0);
		prediction3.setProbability(ccBBC, 1.0);
		prediction3.setProbability(ccBCC, 0.0);
		perfectPredictions[2] = prediction3;
		Prediction prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccBBA);
		prediction4.setProbability(ccAAA, 0.0);
		prediction4.setProbability(ccABA, 0.0);
		prediction4.setProbability(ccACA, 0.0);
		prediction4.setProbability(ccAAC, 0.0);
		prediction4.setProbability(ccABC, 0.0);
		prediction4.setProbability(ccACC, 0.0);
		prediction4.setProbability(ccBAA, 0.0);
		prediction4.setProbability(ccBBA, 1.0);
		prediction4.setProbability(ccBCA, 0.0);
		prediction4.setProbability(ccBAC, 0.0);
		prediction4.setProbability(ccBBC, 0.0);
		prediction4.setProbability(ccBCC, 0.0);
		perfectPredictions[3] = prediction4;
		Prediction prediction5 = new Prediction();
		prediction5.setPredictedClasses(ccBCC);
		prediction5.setProbability(ccAAA, 0.0);
		prediction5.setProbability(ccABA, 0.0);
		prediction5.setProbability(ccACA, 0.0);
		prediction5.setProbability(ccAAC, 0.0);
		prediction5.setProbability(ccABC, 0.0);
		prediction5.setProbability(ccACC, 0.0);
		prediction5.setProbability(ccBAA, 0.0);
		prediction5.setProbability(ccBBA, 0.0);
		prediction5.setProbability(ccBCA, 0.0);
		prediction5.setProbability(ccBAC, 0.0);
		prediction5.setProbability(ccBBC, 0.0);
		prediction5.setProbability(ccBCC, 1.0);
		perfectPredictions[4] = prediction5;

		// ------------ Two predictions are partially incorrect ------------
		twoPartiallyIncorrectPrediction = new Prediction[5];
		prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccABA); // Incorrect
		prediction1.setProbability(ccAAA, 0.4);
		prediction1.setProbability(ccABA, 0.6);
		prediction1.setProbability(ccACA, 0.0);
		prediction1.setProbability(ccAAC, 0.0);
		prediction1.setProbability(ccABC, 0.0);
		prediction1.setProbability(ccACC, 0.0);
		prediction1.setProbability(ccBAA, 0.0);
		prediction1.setProbability(ccBBA, 0.0);
		prediction1.setProbability(ccBCA, 0.0);
		prediction1.setProbability(ccBAC, 0.0);
		prediction1.setProbability(ccBBC, 0.0);
		prediction1.setProbability(ccBCC, 0.0);
		twoPartiallyIncorrectPrediction[0] = prediction1;
		prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccABC); // Incorrect
		prediction2.setProbability(ccAAA, 0.0);
		prediction2.setProbability(ccABA, 0.4);
		prediction2.setProbability(ccACA, 0.0);
		prediction2.setProbability(ccAAC, 0.0);
		prediction2.setProbability(ccABC, 0.6);
		prediction2.setProbability(ccACC, 0.0);
		prediction2.setProbability(ccBAA, 0.0);
		prediction2.setProbability(ccBBA, 0.0);
		prediction2.setProbability(ccBCA, 0.0);
		prediction2.setProbability(ccBAC, 0.0);
		prediction2.setProbability(ccBBC, 0.0);
		prediction2.setProbability(ccBCC, 0.0);
		twoPartiallyIncorrectPrediction[1] = prediction2;
		twoPartiallyIncorrectPrediction[2] = prediction3;
		twoPartiallyIncorrectPrediction[3] = prediction4;
		twoPartiallyIncorrectPrediction[4] = prediction5;

		// ------------ Perfect predictions but with uncertainty ------------
		perfectPredictionWithUncertainty = new Prediction[5];
		// Define predictions
		prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccAAA);
		prediction1.setProbability(ccAAA, 0.45);
		prediction1.setProbability(ccABA, 0.05);
		prediction1.setProbability(ccACA, 0.05);
		prediction1.setProbability(ccAAC, 0.05);
		prediction1.setProbability(ccABC, 0.05);
		prediction1.setProbability(ccACC, 0.05);
		prediction1.setProbability(ccBAA, 0.05);
		prediction1.setProbability(ccBBA, 0.05);
		prediction1.setProbability(ccBCA, 0.05);
		prediction1.setProbability(ccBAC, 0.05);
		prediction1.setProbability(ccBBC, 0.05);
		prediction1.setProbability(ccBCC, 0.05);
		perfectPredictionWithUncertainty[0] = prediction1;
		prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccABA);
		prediction2.setProbability(ccAAA, 0.05);
		prediction2.setProbability(ccABA, 0.45);
		prediction2.setProbability(ccACA, 0.05);
		prediction2.setProbability(ccAAC, 0.05);
		prediction2.setProbability(ccABC, 0.05);
		prediction2.setProbability(ccACC, 0.05);
		prediction2.setProbability(ccBAA, 0.05);
		prediction2.setProbability(ccBBA, 0.05);
		prediction2.setProbability(ccBCA, 0.05);
		prediction2.setProbability(ccBAC, 0.05);
		prediction2.setProbability(ccBBC, 0.05);
		prediction2.setProbability(ccBCC, 0.05);
		perfectPredictionWithUncertainty[1] = prediction2;
		prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccBBC);
		prediction3.setProbability(ccAAA, 0.05);
		prediction3.setProbability(ccABA, 0.05);
		prediction3.setProbability(ccACA, 0.05);
		prediction3.setProbability(ccAAC, 0.05);
		prediction3.setProbability(ccABC, 0.05);
		prediction3.setProbability(ccACC, 0.05);
		prediction3.setProbability(ccBAA, 0.05);
		prediction3.setProbability(ccBBA, 0.05);
		prediction3.setProbability(ccBCA, 0.05);
		prediction3.setProbability(ccBAC, 0.05);
		prediction3.setProbability(ccBBC, 0.45);
		prediction3.setProbability(ccBCC, 0.05);
		perfectPredictionWithUncertainty[2] = prediction3;
		prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccBBA);
		prediction4.setProbability(ccAAA, 0.05);
		prediction4.setProbability(ccABA, 0.05);
		prediction4.setProbability(ccACA, 0.05);
		prediction4.setProbability(ccAAC, 0.05);
		prediction4.setProbability(ccABC, 0.05);
		prediction4.setProbability(ccACC, 0.05);
		prediction4.setProbability(ccBAA, 0.05);
		prediction4.setProbability(ccBBA, 0.45);
		prediction4.setProbability(ccBCA, 0.05);
		prediction4.setProbability(ccBAC, 0.05);
		prediction4.setProbability(ccBBC, 0.05);
		prediction4.setProbability(ccBCC, 0.05);
		perfectPredictionWithUncertainty[3] = prediction4;
		prediction5 = new Prediction();
		prediction5.setPredictedClasses(ccBCC);
		prediction5.setProbability(ccAAA, 0.05);
		prediction5.setProbability(ccABA, 0.05);
		prediction5.setProbability(ccACA, 0.05);
		prediction5.setProbability(ccAAC, 0.05);
		prediction5.setProbability(ccABC, 0.05);
		prediction5.setProbability(ccACC, 0.05);
		prediction5.setProbability(ccBAA, 0.05);
		prediction5.setProbability(ccBBA, 0.05);
		prediction5.setProbability(ccBCA, 0.05);
		prediction5.setProbability(ccBAC, 0.05);
		prediction5.setProbability(ccBBC, 0.05);
		prediction5.setProbability(ccBCC, 0.45);
		perfectPredictionWithUncertainty[4] = prediction5;

		// ------------ Two classes correct, one wrong ------------
		predictionsWithTwoCorrectClasses = new Prediction[5];
		// Define predictions
		prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccBAA);
		prediction1.setProbability(ccAAA, 0.0);
		prediction1.setProbability(ccABA, 0.0);
		prediction1.setProbability(ccACA, 0.0);
		prediction1.setProbability(ccAAC, 0.0);
		prediction1.setProbability(ccABC, 0.0);
		prediction1.setProbability(ccACC, 0.0);
		prediction1.setProbability(ccBAA, 1.0);
		prediction1.setProbability(ccBBA, 0.0);
		prediction1.setProbability(ccBCA, 0.0);
		prediction1.setProbability(ccBAC, 0.0);
		prediction1.setProbability(ccBBC, 0.0);
		prediction1.setProbability(ccBCC, 0.0);
		predictionsWithTwoCorrectClasses[0] = prediction1;
		prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccBBA);
		prediction2.setProbability(ccAAA, 0.0);
		prediction2.setProbability(ccABA, 0.0);
		prediction2.setProbability(ccACA, 0.0);
		prediction2.setProbability(ccAAC, 0.0);
		prediction2.setProbability(ccABC, 0.0);
		prediction2.setProbability(ccACC, 0.0);
		prediction2.setProbability(ccBAA, 0.0);
		prediction2.setProbability(ccBBA, 1.0);
		prediction2.setProbability(ccBCA, 0.0);
		prediction2.setProbability(ccBAC, 0.0);
		prediction2.setProbability(ccBBC, 0.0);
		prediction2.setProbability(ccBCC, 0.0);
		predictionsWithTwoCorrectClasses[1] = prediction2;
		prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccABC);
		prediction3.setProbability(ccAAA, 0.0);
		prediction3.setProbability(ccABA, 0.0);
		prediction3.setProbability(ccACA, 0.0);
		prediction3.setProbability(ccAAC, 0.0);
		prediction3.setProbability(ccABC, 1.0);
		prediction3.setProbability(ccACC, 0.0);
		prediction3.setProbability(ccBAA, 0.0);
		prediction3.setProbability(ccBBA, 0.0);
		prediction3.setProbability(ccBCA, 0.0);
		prediction3.setProbability(ccBAC, 0.0);
		prediction3.setProbability(ccBBC, 0.0);
		prediction3.setProbability(ccBCC, 0.0);
		predictionsWithTwoCorrectClasses[2] = prediction3;
		prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccABA);
		prediction4.setProbability(ccAAA, 0.0);
		prediction4.setProbability(ccABA, 1.0);
		prediction4.setProbability(ccACA, 0.0);
		prediction4.setProbability(ccAAC, 0.0);
		prediction4.setProbability(ccABC, 0.0);
		prediction4.setProbability(ccACC, 0.0);
		prediction4.setProbability(ccBAA, 0.0);
		prediction4.setProbability(ccBBA, 0.0);
		prediction4.setProbability(ccBCA, 0.0);
		prediction4.setProbability(ccBAC, 0.0);
		prediction4.setProbability(ccBBC, 0.0);
		prediction4.setProbability(ccBCC, 0.0);
		predictionsWithTwoCorrectClasses[3] = prediction4;
		prediction5 = new Prediction();
		prediction5.setPredictedClasses(ccACC);
		prediction5.setProbability(ccAAA, 0.0);
		prediction5.setProbability(ccABA, 0.0);
		prediction5.setProbability(ccACA, 0.0);
		prediction5.setProbability(ccAAC, 0.0);
		prediction5.setProbability(ccABC, 0.0);
		prediction5.setProbability(ccACC, 1.0);
		prediction5.setProbability(ccBAA, 0.0);
		prediction5.setProbability(ccBBA, 0.0);
		prediction5.setProbability(ccBCA, 0.0);
		prediction5.setProbability(ccBAC, 0.0);
		prediction5.setProbability(ccBBC, 0.0);
		prediction5.setProbability(ccBCC, 0.0);
		predictionsWithTwoCorrectClasses[4] = prediction5;
	}

	/**
	 * Tests the global accuracy metric.
	 */
	@Test
	public void testGlobalAccuracy() {
		// Perfect classification
		double globalAccuracy = Metrics.globalAccuracy(perfectPredictions, actualDataset);
		assertEquals(1, globalAccuracy, 0.01);
		// Two class variables correct, one wrong
		globalAccuracy = Metrics.globalAccuracy(predictionsWithTwoCorrectClasses, actualDataset);
		assertEquals(0, globalAccuracy, 0.01);
		// All instances are correct except two that are partially wrong
		globalAccuracy = Metrics.globalAccuracy(twoPartiallyIncorrectPrediction, actualDataset);
		assertEquals(0.6, globalAccuracy, 0.01);
	}

	/**
	 * Tests the mean accuracy metric.
	 */
	@Test
	public void testMeanAccuracy() {
		// Perfect classification
		double meanAccuracy = Metrics.meanAccuracy(perfectPredictions, actualDataset);
		assertEquals(1, meanAccuracy, 0.01);
		// Two class variables correct, one wrong
		meanAccuracy = Metrics.meanAccuracy(predictionsWithTwoCorrectClasses, actualDataset);
		assertEquals(0.66, meanAccuracy, 0.01);
		// All instances are correct except two that are partially wrong
		meanAccuracy = Metrics.meanAccuracy(twoPartiallyIncorrectPrediction, actualDataset);
		assertEquals(0.86, meanAccuracy, 0.01);
	}

	/**
	 * Tests the global brier score.
	 */
	@Test
	public void testGlobalBrierScore() {
		// Perfect classification
		double globalBrierScore = Metrics.globalBrierScore(perfectPredictions, actualDataset);
		assertEquals(0, globalBrierScore, 0.01);
		// Perfect predictions but with uncertainty
		globalBrierScore = Metrics.globalBrierScore(perfectPredictionWithUncertainty, actualDataset);
		assertEquals(0.33, globalBrierScore, 0.01);
		// Two class variables correct, one wrong
		globalBrierScore = Metrics.globalBrierScore(predictionsWithTwoCorrectClasses, actualDataset);
		assertEquals(2, globalBrierScore, 0.01);
	}

	/**
	 * Tests the macro-average precision. The class 'a' will be considered as the
	 * positive class for "C1" and "C3", since they only have two classes and it is
	 * the first class to appear.
	 */
	@Test
	public void testMacroPrecision() {
		// Perfect classification
		double macroPrecision = Metrics.macroAverage(perfectPredictions, actualDataset, Metrics::precision);
		assertEquals(1, macroPrecision, 0.01);
		// Two class variables correct, one wrong
		macroPrecision = Metrics.macroAverage(predictionsWithTwoCorrectClasses, actualDataset, Metrics::precision);
		assertEquals(0.66, macroPrecision, 0.01);
		// All instances are correct except two that are partially wrong
		macroPrecision = Metrics.macroAverage(twoPartiallyIncorrectPrediction, actualDataset, Metrics::precision);
		assertEquals(0.86, macroPrecision, 0.01);
	}

	/**
	 * Tests the macro-average recall. The class 'a' will be considered as the
	 * positive class for "C1" and "C3", since they only have two classes and it is
	 * the first class to appear.
	 */
	@Test
	public void testMacroRecall() {
		// Perfect classification
		double macroRecall = Metrics.macroAverage(perfectPredictions, actualDataset, Metrics::recall);
		assertEquals(1, macroRecall, 0.01);
		// Two class variables correct, one wrong
		macroRecall = Metrics.macroAverage(predictionsWithTwoCorrectClasses, actualDataset, Metrics::recall);
		assertEquals(0.66, macroRecall, 0.01);
		// All instances are correct except two that are partially wrong
		macroRecall = Metrics.macroAverage(twoPartiallyIncorrectPrediction, actualDataset, Metrics::recall);
		assertEquals(0.77, macroRecall, 0.01);
	}

	/**
	 * Tests the macro-average F1 score. The class 'a' will be considered as the
	 * positive class for "C1" and "C3", since they only have two classes and it is
	 * the first class to appear.
	 */
	@Test
	public void testMacroF1score() {
		// Perfect classification
		double macroF1Score = Metrics.macroAverage(perfectPredictions, actualDataset, Metrics::f1score);
		assertEquals(1, macroF1Score, 0.01);
		// Two class variables correct, one wrong
		macroF1Score = Metrics.macroAverage(predictionsWithTwoCorrectClasses, actualDataset, Metrics::f1score);
		assertEquals(0.66, macroF1Score, 0.01);
		// All instances are correct except two that are partially wrong
		macroF1Score = Metrics.macroAverage(twoPartiallyIncorrectPrediction, actualDataset, Metrics::f1score);
		assertEquals(0.80, macroF1Score, 0.01);
	}

	/**
	 * Tests the micro-average precision. The class 'a' will be considered as the
	 * positive class for "C1" and "C3", since they only have two classes and it is
	 * the first class to appear.
	 */
	@Test
	public void testMicroPrecision() {
		// Perfect classification
		double microPrecision = Metrics.microAverage(perfectPredictions, actualDataset, Metrics::precision);
		assertEquals(1, microPrecision, 0.01);
		// Two class variables correct, one wrong
		microPrecision = Metrics.microAverage(predictionsWithTwoCorrectClasses, actualDataset, Metrics::precision);
		assertEquals(0.72, microPrecision, 0.01);
		// All instances are correct except two that are partially wrong
		microPrecision = Metrics.microAverage(twoPartiallyIncorrectPrediction, actualDataset, Metrics::precision);
		assertEquals(0.88, microPrecision, 0.01);
	}

	/**
	 * Tests the micro-average recall. The class 'a' will be considered as the
	 * positive class for "C1" and "C3", since they only have two classes and it is
	 * the first class to appear.
	 */
	@Test
	public void testMicroRecall() {
		// Perfect classification
		double microRecall = Metrics.microAverage(perfectPredictions, actualDataset, Metrics::recall);
		assertEquals(1, microRecall, 0.01);
		// Two class variables correct, one wrong
		microRecall = Metrics.microAverage(predictionsWithTwoCorrectClasses, actualDataset, Metrics::recall);
		assertEquals(0.8, microRecall, 0.01);
		// All instances are correct except two that are partially wrong
		microRecall = Metrics.microAverage(twoPartiallyIncorrectPrediction, actualDataset, Metrics::recall);
		assertEquals(0.8, microRecall, 0.01);
	}

	/**
	 * Tests the micro-average F1 score. The class 'a' will be considered as the
	 * positive class for "C1" and "C3", since they only have two classes and it is
	 * the first class to appear.
	 */
	@Test
	public void testMicroF1score() {
		// Perfect classification
		double microF1Score = Metrics.microAverage(perfectPredictions, actualDataset, Metrics::f1score);
		assertEquals(1, microF1Score, 0.01);
		// Two class variables correct, one wrong
		microF1Score = Metrics.microAverage(predictionsWithTwoCorrectClasses, actualDataset, Metrics::f1score);
		assertEquals(0.76, microF1Score, 0.01);
		// All instances are correct except two that are partially wrong
		microF1Score = Metrics.microAverage(twoPartiallyIncorrectPrediction, actualDataset, Metrics::f1score);
		assertEquals(0.84, microF1Score, 0.01);
	}

}
