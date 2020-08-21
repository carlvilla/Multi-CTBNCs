package metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.performance.Metrics;

/**
 * Test to evaluate the correct behavior of performance test over
 * multi-dimensional time series.
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
	 * Define some predictions and actual values that will be used to evaluate the
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
		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence2.add(new String[] { "0.0", "a", "b", "a" });
		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence3.add(new String[] { "0.0", "b", "b", "c" });
		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence4.add(new String[] { "0.0", "b", "b", "a" });
		actualDataset = new Dataset(timeVariable, classVariables);
		actualDataset.addSequence(dataSequence1);
		actualDataset.addSequence(dataSequence2);
		actualDataset.addSequence(dataSequence3);
		actualDataset.addSequence(dataSequence4);

		// Define all possible class configurations
		State ccAAA = new State();
		ccAAA.addEvent("C1", "a");
		ccAAA.addEvent("C2", "a");
		ccAAA.addEvent("C3", "a");
		State ccABA = new State();
		ccABA.addEvent("C1", "a");
		ccABA.addEvent("C2", "b");
		ccABA.addEvent("C3", "a");
		State ccBAA = new State();
		ccBAA.addEvent("C1", "b");
		ccBAA.addEvent("C2", "a");
		ccBAA.addEvent("C3", "a");
		State ccBBA = new State();
		ccBBA.addEvent("C1", "b");
		ccBBA.addEvent("C2", "b");
		ccBBA.addEvent("C3", "a");
		State ccAAB = new State();
		ccAAB.addEvent("C1", "a");
		ccAAB.addEvent("C2", "a");
		ccAAB.addEvent("C3", "b");
		State ccABB = new State();
		ccABB.addEvent("C1", "a");
		ccABB.addEvent("C2", "b");
		ccABB.addEvent("C3", "b");
		State ccBAB = new State();
		ccBAB.addEvent("C1", "b");
		ccBAB.addEvent("C2", "a");
		ccBAB.addEvent("C3", "b");
		State ccBBB = new State();
		ccBBB.addEvent("C1", "b");
		ccBBB.addEvent("C2", "b");
		ccBBB.addEvent("C3", "b");
		State ccAAC = new State();
		ccAAC.addEvent("C1", "a");
		ccAAC.addEvent("C2", "a");
		ccAAC.addEvent("C3", "c");
		State ccABC = new State();
		ccABC.addEvent("C1", "a");
		ccABC.addEvent("C2", "b");
		ccABC.addEvent("C3", "c");
		State ccBAC = new State();
		ccBAC.addEvent("C1", "b");
		ccBAC.addEvent("C2", "a");
		ccBAC.addEvent("C3", "c");
		State ccBBC = new State();
		ccBBC.addEvent("C1", "b");
		ccBBC.addEvent("C2", "b");
		ccBBC.addEvent("C3", "c");

		// --------------- Perfect classification ---------------
		perfectPredictions = new Prediction[4];
		Prediction prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccAAA);
		prediction1.setProbability(ccAAA, 1.0);
		prediction1.setProbability(ccABA, 0.0);
		prediction1.setProbability(ccBAA, 0.0);
		prediction1.setProbability(ccBBA, 0.0);
		prediction1.setProbability(ccAAB, 0.0);
		prediction1.setProbability(ccABB, 0.0);
		prediction1.setProbability(ccBAB, 0.0);
		prediction1.setProbability(ccBBB, 0.0);
		prediction1.setProbability(ccAAC, 0.0);
		prediction1.setProbability(ccABC, 0.0);
		prediction1.setProbability(ccBAC, 0.0);
		prediction1.setProbability(ccBBC, 0.0);
		perfectPredictions[0] = prediction1;
		Prediction prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccABA);
		prediction2.setProbability(ccAAA, 0.0);
		prediction2.setProbability(ccABA, 1.0);
		prediction2.setProbability(ccBAA, 0.0);
		prediction2.setProbability(ccBBA, 0.0);
		prediction2.setProbability(ccAAB, 0.0);
		prediction2.setProbability(ccABB, 0.0);
		prediction2.setProbability(ccBAB, 0.0);
		prediction2.setProbability(ccBBB, 0.0);
		prediction2.setProbability(ccAAC, 0.0);
		prediction2.setProbability(ccABC, 0.0);
		prediction2.setProbability(ccBAC, 0.0);
		prediction2.setProbability(ccBBC, 0.0);
		perfectPredictions[1] = prediction2;
		Prediction prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccBBC);
		prediction3.setProbability(ccAAA, 0.0);
		prediction3.setProbability(ccABA, 0.0);
		prediction3.setProbability(ccBAA, 0.0);
		prediction3.setProbability(ccBBA, 0.0);
		prediction3.setProbability(ccAAB, 0.0);
		prediction3.setProbability(ccABB, 0.0);
		prediction3.setProbability(ccBAB, 0.0);
		prediction3.setProbability(ccBBB, 0.0);
		prediction3.setProbability(ccAAC, 0.0);
		prediction3.setProbability(ccABC, 0.0);
		prediction3.setProbability(ccBAC, 0.0);
		prediction3.setProbability(ccBBC, 1.0);
		perfectPredictions[2] = prediction3;
		Prediction prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccBBA);
		prediction4.setProbability(ccAAA, 0.0);
		prediction4.setProbability(ccABA, 0.0);
		prediction4.setProbability(ccBAA, 0.0);
		prediction4.setProbability(ccBBA, 1.0);
		prediction4.setProbability(ccAAB, 0.0);
		prediction4.setProbability(ccABB, 0.0);
		prediction4.setProbability(ccBAB, 0.0);
		prediction4.setProbability(ccBBB, 0.0);
		prediction4.setProbability(ccAAC, 0.0);
		prediction4.setProbability(ccABC, 0.0);
		prediction4.setProbability(ccBAC, 0.0);
		prediction4.setProbability(ccBBC, 0.0);
		perfectPredictions[3] = prediction4;

		// ------------ Perfect predictions but with uncertainty ------------
		perfectPredictionWithUncertainty = new Prediction[4];
		// Define predictions
		prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccAAA);
		prediction1.setProbability(ccAAA, 0.45);
		prediction1.setProbability(ccABA, 0.05);
		prediction1.setProbability(ccBAA, 0.05);
		prediction1.setProbability(ccBBA, 0.05);
		prediction1.setProbability(ccAAB, 0.05);
		prediction1.setProbability(ccABB, 0.05);
		prediction1.setProbability(ccBAB, 0.05);
		prediction1.setProbability(ccBBB, 0.05);
		prediction1.setProbability(ccAAC, 0.05);
		prediction1.setProbability(ccABC, 0.05);
		prediction1.setProbability(ccBAC, 0.05);
		prediction1.setProbability(ccBBC, 0.05);
		perfectPredictionWithUncertainty[0] = prediction1;
		prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccABA);
		prediction2.setProbability(ccAAA, 0.05);
		prediction2.setProbability(ccABA, 0.45);
		prediction2.setProbability(ccBAA, 0.05);
		prediction2.setProbability(ccBBA, 0.05);
		prediction2.setProbability(ccAAB, 0.05);
		prediction2.setProbability(ccABB, 0.05);
		prediction2.setProbability(ccBAB, 0.05);
		prediction2.setProbability(ccBBB, 0.05);
		prediction2.setProbability(ccAAC, 0.05);
		prediction2.setProbability(ccABC, 0.05);
		prediction2.setProbability(ccBAC, 0.05);
		prediction2.setProbability(ccBBC, 0.05);
		perfectPredictionWithUncertainty[1] = prediction2;
		prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccBBC);
		prediction3.setProbability(ccAAA, 0.05);
		prediction3.setProbability(ccABA, 0.05);
		prediction3.setProbability(ccBAA, 0.05);
		prediction3.setProbability(ccBBA, 0.05);
		prediction3.setProbability(ccAAB, 0.05);
		prediction3.setProbability(ccABB, 0.05);
		prediction3.setProbability(ccBAB, 0.05);
		prediction3.setProbability(ccBBB, 0.05);
		prediction3.setProbability(ccAAC, 0.05);
		prediction3.setProbability(ccABC, 0.05);
		prediction3.setProbability(ccBAC, 0.05);
		prediction3.setProbability(ccBBC, 0.45);
		perfectPredictionWithUncertainty[2] = prediction3;
		prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccBBA);
		prediction4.setProbability(ccAAA, 0.05);
		prediction4.setProbability(ccABA, 0.05);
		prediction4.setProbability(ccBAA, 0.05);
		prediction4.setProbability(ccBBA, 0.45);
		prediction4.setProbability(ccAAB, 0.05);
		prediction4.setProbability(ccABB, 0.05);
		prediction4.setProbability(ccBAB, 0.05);
		prediction4.setProbability(ccBBB, 0.05);
		prediction4.setProbability(ccAAC, 0.05);
		prediction4.setProbability(ccABC, 0.05);
		prediction4.setProbability(ccBAC, 0.05);
		prediction4.setProbability(ccBBC, 0.05);
		perfectPredictionWithUncertainty[3] = prediction4;

		// ------------ Two predictions are partially incorrect ------------
		twoPartiallyIncorrectPrediction = new Prediction[4];
		prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccABA); // Incorrect
		prediction1.setProbability(ccAAA, 0.4);
		prediction1.setProbability(ccABA, 0.6);
		prediction1.setProbability(ccBAA, 0.0);
		prediction1.setProbability(ccBBA, 0.0);
		prediction1.setProbability(ccAAB, 0.0);
		prediction1.setProbability(ccABB, 0.0);
		prediction1.setProbability(ccBAB, 0.0);
		prediction1.setProbability(ccBBB, 0.0);
		prediction1.setProbability(ccAAC, 0.0);
		prediction1.setProbability(ccABC, 0.0);
		prediction1.setProbability(ccBAC, 0.0);
		prediction1.setProbability(ccBBC, 0.0);
		twoPartiallyIncorrectPrediction[0] = prediction1;
		prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccABC); // Incorrect
		prediction2.setProbability(ccAAA, 0.0);
		prediction2.setProbability(ccABA, 0.4);
		prediction2.setProbability(ccBAA, 0.0);
		prediction2.setProbability(ccBBA, 0.0);
		prediction2.setProbability(ccAAB, 0.0);
		prediction2.setProbability(ccABB, 0.0);
		prediction2.setProbability(ccBAB, 0.0);
		prediction2.setProbability(ccBBB, 0.0);
		prediction2.setProbability(ccAAC, 0.0);
		prediction2.setProbability(ccABC, 0.6);
		prediction2.setProbability(ccBAC, 0.0);
		prediction2.setProbability(ccBBC, 0.0);
		twoPartiallyIncorrectPrediction[1] = prediction2;
		prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccBBC);
		prediction3.setProbability(ccAAA, 0.0);
		prediction3.setProbability(ccABA, 0.0);
		prediction3.setProbability(ccBAA, 0.0);
		prediction3.setProbability(ccBBA, 0.0);
		prediction3.setProbability(ccAAB, 0.0);
		prediction3.setProbability(ccABB, 0.0);
		prediction3.setProbability(ccBAB, 0.0);
		prediction3.setProbability(ccBBB, 0.0);
		prediction3.setProbability(ccAAC, 0.0);
		prediction3.setProbability(ccABC, 0.0);
		prediction3.setProbability(ccBAC, 0.0);
		prediction3.setProbability(ccBBC, 1.0);
		twoPartiallyIncorrectPrediction[2] = prediction3;
		prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccBBA);
		prediction4.setProbability(ccAAA, 0.0);
		prediction4.setProbability(ccABA, 0.0);
		prediction4.setProbability(ccBAA, 0.0);
		prediction4.setProbability(ccBBA, 1.0);
		prediction4.setProbability(ccAAB, 0.0);
		prediction4.setProbability(ccABB, 0.0);
		prediction4.setProbability(ccBAB, 0.0);
		prediction4.setProbability(ccBBB, 0.0);
		prediction4.setProbability(ccAAC, 0.0);
		prediction4.setProbability(ccABC, 0.0);
		prediction4.setProbability(ccBAC, 0.0);
		prediction4.setProbability(ccBBC, 0.0);
		twoPartiallyIncorrectPrediction[3] = prediction4;

		// ------------ Two classes correct, one wrong ------------
		predictionsWithTwoCorrectClasses = new Prediction[4];
		// Define predictions
		prediction1 = new Prediction();
		prediction1.setPredictedClasses(ccBAA);
		prediction1.setProbability(ccAAA, 0.0);
		prediction1.setProbability(ccABA, 0.0);
		prediction1.setProbability(ccBAA, 1.0);
		prediction1.setProbability(ccBBA, 0.0);
		prediction1.setProbability(ccAAB, 0.0);
		prediction1.setProbability(ccABB, 0.0);
		prediction1.setProbability(ccBAB, 0.0);
		prediction1.setProbability(ccBBB, 0.0);
		prediction1.setProbability(ccAAC, 0.0);
		prediction1.setProbability(ccABC, 0.0);
		prediction1.setProbability(ccBAC, 0.0);
		prediction1.setProbability(ccBBC, 0.0);
		predictionsWithTwoCorrectClasses[0] = prediction1;
		prediction2 = new Prediction();
		prediction2.setPredictedClasses(ccBBA);
		prediction2.setProbability(ccAAA, 0.0);
		prediction2.setProbability(ccABA, 0.0);
		prediction2.setProbability(ccBAA, 0.0);
		prediction2.setProbability(ccBBA, 1.0);
		prediction2.setProbability(ccAAB, 0.0);
		prediction2.setProbability(ccABB, 0.0);
		prediction2.setProbability(ccBAB, 0.0);
		prediction2.setProbability(ccBBB, 0.0);
		prediction2.setProbability(ccAAC, 0.0);
		prediction2.setProbability(ccABC, 0.0);
		prediction2.setProbability(ccBAC, 0.0);
		prediction2.setProbability(ccBBC, 0.0);
		predictionsWithTwoCorrectClasses[1] = prediction2;
		prediction3 = new Prediction();
		prediction3.setPredictedClasses(ccABC);
		prediction3.setProbability(ccAAA, 0.0);
		prediction3.setProbability(ccABA, 0.0);
		prediction3.setProbability(ccBAA, 0.0);
		prediction3.setProbability(ccBBA, 0.0);
		prediction3.setProbability(ccAAB, 0.0);
		prediction3.setProbability(ccABB, 0.0);
		prediction3.setProbability(ccBAB, 0.0);
		prediction3.setProbability(ccBBB, 0.0);
		prediction3.setProbability(ccAAC, 0.0);
		prediction3.setProbability(ccABC, 1.0);
		prediction3.setProbability(ccBAC, 0.0);
		prediction3.setProbability(ccBBC, 0.0);
		predictionsWithTwoCorrectClasses[2] = prediction3;
		prediction4 = new Prediction();
		prediction4.setPredictedClasses(ccABA);
		prediction4.setProbability(ccAAA, 0.0);
		prediction4.setProbability(ccABA, 1.0);
		prediction4.setProbability(ccBAA, 0.0);
		prediction4.setProbability(ccBBA, 0.0);
		prediction4.setProbability(ccAAB, 0.0);
		prediction4.setProbability(ccABB, 0.0);
		prediction4.setProbability(ccBAB, 0.0);
		prediction4.setProbability(ccBBB, 0.0);
		prediction4.setProbability(ccAAC, 0.0);
		prediction4.setProbability(ccABC, 0.0);
		prediction4.setProbability(ccBAC, 0.0);
		prediction4.setProbability(ccBBC, 0.0);
		predictionsWithTwoCorrectClasses[3] = prediction4;
	}

	/**
	 * Test the global accuracy metric.
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
		assertEquals(0.5, globalAccuracy, 0.01);
	}

	/**
	 * Test the mean accuracy metric.
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
		assertEquals(0.83, meanAccuracy, 0.01);
	}

	/**
	 * Test the global brier score.
	 */
	@Test
	public void testGlobalBrierScore() {
		// Perfect classification
		double globalAccuracy = Metrics.globalBrierScore(perfectPredictions, actualDataset);
		assertEquals(0, globalAccuracy, 0.01);
		// Perfect predictions but with uncertainty
		globalAccuracy = Metrics.globalBrierScore(perfectPredictionWithUncertainty, actualDataset);
		assertEquals(0.33, globalAccuracy, 0.01);
		// Two class variables correct, one wrong
		globalAccuracy = Metrics.globalBrierScore(predictionsWithTwoCorrectClasses, actualDataset);
		assertEquals(2, globalAccuracy, 0.01);
	}

}
