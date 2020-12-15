package mctbnc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.util.ProbabilityUtil;
import com.cig.mctbnc.util.Util;

class ClassificationTest {
	static MCTBNC<CPTNode, CIMNode> mctbnc;
	static Dataset dataset;

	@BeforeAll
	static void setUp() {
		// Definition of the model

		// Define class variables
		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));

		// Definition of the structure of the class subgraph
		CV1.setChild(CV2);

		Map<State, Double> CPTCV1 = new HashMap<State, Double>();
		State CV1state1 = new State(Map.of("CV1", "CV1_A"));
		State CV1state2 = new State(Map.of("CV1", "CV1_B"));
		CPTCV1.put(CV1state1, 0.6);
		CPTCV1.put(CV1state2, 0.4);
		CV1.setCPT(CPTCV1);

		Map<State, Double> CPTCV2 = new HashMap<State, Double>();
		State CV2state1 = new State(Map.of("CV2", "CV2_A", "CV1", "CV1_A"));
		State CV2state2 = new State(Map.of("CV2", "CV2_B", "CV1", "CV1_A"));
		State CV2state3 = new State(Map.of("CV2", "CV2_A", "CV1", "CV1_B"));
		State CV2state4 = new State(Map.of("CV2", "CV2_B", "CV1", "CV1_B"));
		CPTCV2.put(CV2state1, 0.2);
		CPTCV2.put(CV2state2, 0.8);
		CPTCV2.put(CV2state3, 0.8);
		CPTCV2.put(CV2state4, 0.2);
		CV2.setCPT(CPTCV2);

		BN<CPTNode> bn = new BN<CPTNode>(List.of(CV1, CV2));

		// Define features
		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B"));

		// Definition of the structure of the bridge and feature subgraphs
		CV1.setChild(X1);
		CV2.setChild(X2);
		X1.setChild(X2);

		State X1state1 = new State(Map.of("X1", "X1_A", "CV1", "CV1_A"));
		State X1state2 = new State(Map.of("X1", "X1_B", "CV1", "CV1_A"));
		State X1state3 = new State(Map.of("X1", "X1_C", "CV1", "CV1_A"));
		State X1state4 = new State(Map.of("X1", "X1_A", "CV1", "CV1_B"));
		State X1state5 = new State(Map.of("X1", "X1_B", "CV1", "CV1_B"));
		State X1state6 = new State(Map.of("X1", "X1_C", "CV1", "CV1_B"));

		State X1toA = new State(Map.of("X1", "X1_A"));
		State X1toB = new State(Map.of("X1", "X1_B"));
		State X1toC = new State(Map.of("X1", "X1_C"));

		Map<State, Double> Qx = new HashMap<State, Double>();
		Qx.put(X1state1, 3.3);
		Qx.put(X1state2, 3.3);
		Qx.put(X1state3, 10.0);
		Qx.put(X1state4, 10.0);
		Qx.put(X1state5, 3.3);
		Qx.put(X1state6, 3.3);

		Map<State, Map<State, Double>> Oxy = new HashMap<State, Map<State, Double>>();
		Oxy.put(X1state1, Map.of(X1toB, 0.8, X1toC, 0.2));
		Oxy.put(X1state2, Map.of(X1toA, 0.7, X1toC, 0.3));
		Oxy.put(X1state3, Map.of(X1toA, 0.6, X1toB, 0.4));
		Oxy.put(X1state4, Map.of(X1toB, 0.5, X1toC, 0.5));
		Oxy.put(X1state5, Map.of(X1toA, 0.2, X1toC, 0.8));
		Oxy.put(X1state6, Map.of(X1toA, 0.3, X1toB, 0.7));

		X1.setParameters(Qx, Oxy);

		State X2state1 = new State(Map.of("X2", "X2_A", "X1", "X1_A", "CV2", "CV2_A"));
		State X2state2 = new State(Map.of("X2", "X2_B", "X1", "X1_A", "CV2", "CV2_A"));
		State X2state3 = new State(Map.of("X2", "X2_A", "X1", "X1_B", "CV2", "CV2_A"));
		State X2state4 = new State(Map.of("X2", "X2_B", "X1", "X1_B", "CV2", "CV2_A"));
		State X2state5 = new State(Map.of("X2", "X2_A", "X1", "X1_A", "CV2", "CV2_B"));
		State X2state6 = new State(Map.of("X2", "X2_B", "X1", "X1_A", "CV2", "CV2_B"));
		State X2state7 = new State(Map.of("X2", "X2_A", "X1", "X1_B", "CV2", "CV2_B"));
		State X2state8 = new State(Map.of("X2", "X2_B", "X1", "X1_B", "CV2", "CV2_B"));
		State X2state9 = new State(Map.of("X2", "X2_A", "X1", "X1_C", "CV2", "CV2_A"));
		State X2state10 = new State(Map.of("X2", "X2_B", "X1", "X1_C", "CV2", "CV2_A"));
		State X2state11 = new State(Map.of("X2", "X2_A", "X1", "X1_C", "CV2", "CV2_B"));
		State X2state12 = new State(Map.of("X2", "X2_B", "X1", "X1_C", "CV2", "CV2_B"));

		State X2toA = new State(Map.of("X2", "X2_A"));
		State X2toB = new State(Map.of("X2", "X2_B"));

		Map<State, Double> X2Qx = new HashMap<State, Double>();
		X2Qx.put(X2state1, 0.03);
		X2Qx.put(X2state2, 3.33);
		X2Qx.put(X2state3, 0.01);
		X2Qx.put(X2state4, 0.5);
		X2Qx.put(X2state5, 3.33);
		X2Qx.put(X2state6, 0.03);
		X2Qx.put(X2state7, 3.33);
		X2Qx.put(X2state8, 0.03);
		X2Qx.put(X2state9, 0.05);
		X2Qx.put(X2state10, 0.05);
		X2Qx.put(X2state11, 1.5);
		X2Qx.put(X2state12, 3.33);

		Map<State, Map<State, Double>> X2Oxy = new HashMap<State, Map<State, Double>>();
		X2Oxy.put(X2state1, Map.of(X2toB, 1.0));
		X2Oxy.put(X2state2, Map.of(X2toA, 1.0));
		X2Oxy.put(X2state3, Map.of(X2toB, 1.0));
		X2Oxy.put(X2state4, Map.of(X2toA, 1.0));
		X2Oxy.put(X2state5, Map.of(X2toB, 1.0));
		X2Oxy.put(X2state6, Map.of(X2toA, 1.0));
		X2Oxy.put(X2state7, Map.of(X2toB, 1.0));
		X2Oxy.put(X2state8, Map.of(X2toA, 1.0));
		X2Oxy.put(X2state9, Map.of(X2toB, 1.0));
		X2Oxy.put(X2state10, Map.of(X2toA, 1.0));
		X2Oxy.put(X2state11, Map.of(X2toB, 1.0));
		X2Oxy.put(X2state12, Map.of(X2toA, 1.0));

		X2.setParameters(X2Qx, X2Oxy);

		CTBN<CIMNode> ctbn = new CTBN<CIMNode>(List.of(X1, X2), bn);

		mctbnc = new MCTBNC<CPTNode, CIMNode>(bn, ctbn);

		// Definition of the sequences to predict
		List<String> nameClassVariables = List.of("C1", "C2");

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence1.add(new String[] { "0.0", "X1_A", "X2_B", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "0.3", "X1_A", "X2_A", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "0.31", "X1_B", "X2_A", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "0.6", "X1_A", "X2_A", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "0.9", "X1_B", "X2_A", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "1.2", "X1_A", "X2_A", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "1.5", "X1_B", "X2_A", "CV1_A", "CV2_A" });
		dataSequence1.add(new String[] { "1.8", "X1_A", "X2_A", "CV1_A", "CV2_A" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence2.add(new String[] { "0.0", "X1_C", "X2_A", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "0.3", "X1_B", "X2_A", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "0.6", "X1_B", "X2_B", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "0.61", "X1_C", "X2_B", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "0.9", "X1_B", "X2_B", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "1.2", "X1_C", "X2_B", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "1.5", "X1_B", "X2_B", "CV1_B", "CV2_B" });
		dataSequence2.add(new String[] { "1.8", "X1_C", "X2_B", "CV1_B", "CV2_B" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence3.add(new String[] { "0.0", "X1_B", "X2_A", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "0.3", "X1_B", "X2_B", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "0.31", "X1_A", "X2_B", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "0.6", "X1_B", "X2_B", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "0.9", "X1_A", "X2_B", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "1.2", "X1_B", "X2_B", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "1.5", "X1_A", "X2_B", "CV1_A", "CV2_B" });
		dataSequence3.add(new String[] { "1.8", "X1_B", "X2_B", "CV1_A", "CV2_B" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence4.add(new String[] { "0.0", "X1_B", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "0.3", "X1_C", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "0.31", "X1_B", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "0.6", "X1_C", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "0.9", "X1_B", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "1.2", "X1_C", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "1.5", "X1_B", "X2_B", "CV1_B", "CV2_A" });
		dataSequence4.add(new String[] { "1.8", "X1_C", "X2_B", "CV1_B", "CV2_A" });

		dataset = new Dataset("Time", nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
	}

	@Test
	void testClassification() {
		Prediction[] predictions = mctbnc.predict(dataset, true);

		// Sequence 1
		// Predict class configuration
		State expectedClassConfiguration = new State(Map.of("CV1", "CV1_A", "CV2", "CV2_A"));
		State actualClassConfiguration = predictions[0].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);
		// Unnormalized log-a-posteriori probability for CV1 = CV1_A and CV2 = CV2_A
		double ulpAA = // CV1, CV2
				Math.log(0.6) + Math.log(0.2)
				// X1
						+ (-3.3 * (0.3)) + (-3.3 * (0.31 - 0.3) + Math.log(3.3 * 0.8))
						+ (-3.3 * (0.6 - 0.31) + Math.log(3.3 * 0.7)) + (-3.3 * (0.9 - 0.6) + Math.log(3.3 * 0.8))
						+ (-3.3 * (1.2 - 0.9) + Math.log(3.3 * 0.7)) + (-3.3 * (1.5 - 1.2) + Math.log(3.3 * 0.8))
						+ (-3.3 * (1.8 - 1.5) + Math.log(3.3 * 0.7))
						// X2
						+ (-3.3 * (0.3) + Math.log(3.3)) + (-0.03 * (0.31 - 0.3)) + (-0.01 * (0.6 - 0.31))
						+ (-0.03 * (0.9 - 0.6)) + (-0.01 * (1.2 - 0.9)) + (-0.03 * (1.5 - 1.2)) + (-0.01 * (1.8 - 1.5));
		double ulpAB =
				// CV1, CV2
				Math.log(0.6) + Math.log(0.8)
				// X1
						+ (-3.3 * (0.3)) + (-3.3 * (0.31 - 0.3) + Math.log(3.3 * 0.8))
						+ (-3.3 * (0.6 - 0.31) + Math.log(3.3 * 0.7)) + (-3.3 * (0.9 - 0.6) + Math.log(3.3 * 0.8))
						+ (-3.3 * (1.2 - 0.9) + Math.log(3.3 * 0.7)) + (-3.3 * (1.5 - 1.2) + Math.log(3.3 * 0.8))
						+ (-3.3 * (1.8 - 1.5) + Math.log(3.3 * 0.7))

						// X2
						+ (-0.03 * (0.3) + Math.log(0.03)) + (-3.33 * (0.31 - 0.3)) + (-3.33 * (0.6 - 0.31))
						+ (-3.33 * (0.9 - 0.6)) + (-3.33 * (1.2 - 0.9)) + (-3.33 * (1.5 - 1.2)) + (-3.33 * (1.8 - 1.5));
		;
		double ulpBA =
				// CV1, CV2
				Math.log(0.4) + Math.log(0.8)
				// X1
						+ (-10.0 * (0.3)) + (-10.0 * (0.31 - 0.3) + Math.log(10.0 * 0.5))
						+ (-3.3 * (0.6 - 0.31) + Math.log(3.3 * 0.2)) + (-10.0 * (0.9 - 0.6) + Math.log(10.0 * 0.5))
						+ (-3.3 * (1.2 - 0.9) + Math.log(3.3 * 0.2)) + (-10.0 * (1.5 - 1.2) + Math.log(10.0 * 0.5))
						+ (-3.3 * (1.8 - 1.5) + Math.log(3.3 * 0.2))

						// X2
						+ (-3.3 * (0.3) + Math.log(3.3)) + (-0.03 * (0.31 - 0.3)) + (-0.01 * (0.6 - 0.31))
						+ (-0.03 * (0.9 - 0.6)) + (-0.01 * (1.2 - 0.9)) + (-0.03 * (1.5 - 1.2)) + (-0.01 * (1.8 - 1.5));
		double ulpBB =
				// CV1, CV2
				Math.log(0.4) + Math.log(0.2)
				// X1
						+ (-10.0 * (0.3)) + (-10.0 * (0.31 - 0.3) + Math.log(10.0 * 0.5))
						+ (-3.3 * (0.6 - 0.31) + Math.log(3.3 * 0.2)) + (-10.0 * (0.9 - 0.6) + Math.log(10.0 * 0.5))
						+ (-3.3 * (1.2 - 0.9) + Math.log(3.3 * 0.2)) + (-10.0 * (1.5 - 1.2) + Math.log(10.0 * 0.5))
						+ (-3.3 * (1.8 - 1.5) + Math.log(3.3 * 0.2))

						// X2
						+ (-0.03 * (0.3) + Math.log(0.03)) + (-3.33 * (0.31 - 0.3)) + (-3.33 * (0.6 - 0.31))
						+ (-3.33 * (0.9 - 0.6)) + (-3.33 * (1.2 - 0.9)) + (-3.33 * (1.5 - 1.2)) + (-3.33 * (1.8 - 1.5));
		// Marginal log-likelihood of sequence 1
		double mll = ulpAA + Math.log(
				Math.exp(ulpAA - ulpAA) + Math.exp(ulpAB - ulpAA) + Math.exp(ulpBA - ulpAA) + Math.exp(ulpBB - ulpAA));
		// Probability of each class configuration
		double expectedProbabilityAA = Math.exp(ulpAA - mll);
		double expectedProbabilityAB = Math.exp(ulpAB - mll);
		double expectedProbabilityBA = Math.exp(ulpBA - mll);
		double expectedProbabilityBB = Math.exp(ulpBB - mll);
		assertEquals(expectedProbabilityAA, predictions[0].getProbabilityPrediction(), 0.001);
		assertEquals(expectedProbabilityAB,
				predictions[0].getProbabilities().get(new State(Map.of("CV1", "CV1_A", "CV2", "CV2_B"))), 0.001);
		assertEquals(expectedProbabilityBA,
				predictions[0].getProbabilities().get(new State(Map.of("CV1", "CV1_B", "CV2", "CV2_A"))), 0.001);
		assertEquals(expectedProbabilityBB,
				predictions[0].getProbabilities().get(new State(Map.of("CV1", "CV1_B", "CV2", "CV2_B"))), 0.001);

		// Sequence 2
		// Predict class configuration
		expectedClassConfiguration = new State(Map.of("CV1", "CV1_B", "CV2", "CV2_B"));
		actualClassConfiguration = predictions[1].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);

		// Sequence 3
		// Predict class configuration
		expectedClassConfiguration = new State(Map.of("CV1", "CV1_A", "CV2", "CV2_B"));
		actualClassConfiguration = predictions[2].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);

		// Sequence 4
		// Predict class configuration
		expectedClassConfiguration = new State(Map.of("CV1", "CV1_B", "CV2", "CV2_A"));
		actualClassConfiguration = predictions[3].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);
	}

}
