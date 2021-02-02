package mctbnc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
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
import com.cig.mctbnc.nodes.DiscreteNode;

class ClassificationTest {
	static MCTBNC<CPTNode, CIMNode> mctbnc;
	static Dataset dataset;

	@BeforeAll
	static void setUp() {
		// Definition of the model

		// Define class variables
		CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
		CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));

		// Definition of the structure of the class subgraph
		C1.setChild(C2);

		double[][] CPTC1 = new double[1][2];
		CPTC1[0][0] = 0.6; // Probability C1 = C1_A
		CPTC1[0][1] = 0.4; // Probability C1 = C1_B
		C1.setCPT(CPTC1);

		double[][] CPTC2 = new double[2][2];
		CPTC2[0][0] = 0.2; // Probability C2 = C2_A given C1 = C1_A
		CPTC2[0][1] = 0.8; // Probability C2 = C2_B given C1 = C1_A
		CPTC2[1][0] = 0.8; // Probability C2 = C2_A given C1 = C1_B
		CPTC2[1][1] = 0.2; // Probability C2 = C2_B given C1 = C1_B
		C2.setCPT(CPTC2);

		BN<CPTNode> bn = new BN<CPTNode>(List.of(C1, C2));

		// Define features
		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B"));

		// Definition of the structure of the bridge and feature subgraphs
		C1.setChild(X1);
		C2.setChild(X2);
		X1.setChild(X2);

		double[][] QxX1 = new double[2][3];
		QxX1[0][0] = 3.3; // Intensity X1 = X1_A given C1 = C1_A
		QxX1[0][1] = 3.3; // Intensity X1 = X1_B given C1 = C1_A
		QxX1[0][2] = 10.0; // Intensity X1 = X1_C given C1 = C1_A
		QxX1[1][0] = 10.0; // Intensity X1 = X1_A given C1 = C1_B
		QxX1[1][1] = 3.3; // Intensity X1 = X1_B given C1 = C1_B
		QxX1[1][2] = 3.3; // Intensity X1 = X1_C given C1 = C1_B

		double[][][] OxyX1 = new double[2][3][3];
		OxyX1[0][0][1] = 0.8; // Probability X1 = X1_A to X1 = X1_B given C1 = C1_A
		OxyX1[0][0][2] = 0.2; // Probability X1 = X1_A to X1 = X1_C given C1 = C1_A
		OxyX1[0][1][0] = 0.7; // Probability X1 = X1_B to X1 = X1_A given C1 = C1_A
		OxyX1[0][1][2] = 0.3; // Probability X1 = X1_B to X1 = X1_C given C1 = C1_A
		OxyX1[0][2][0] = 0.6; // Probability X1 = X1_C to X1 = X1_A given C1 = C1_A
		OxyX1[0][2][1] = 0.4; // Probability X1 = X1_C to X1 = X1_B given C1 = C1_A
		OxyX1[1][0][1] = 0.5; // Probability X1 = X1_A to X1 = X1_B given C1 = C1_B
		OxyX1[1][0][2] = 0.5; // Probability X1 = X1_A to X1 = X1_C given C1 = C1_B
		OxyX1[1][1][0] = 0.2; // Probability X1 = X1_B to X1 = X1_A given C1 = C1_B
		OxyX1[1][1][2] = 0.8; // Probability X1 = X1_B to X1 = X1_C given C1 = C1_B
		OxyX1[1][2][0] = 0.3; // Probability X1 = X1_C to X1 = X1_A given C1 = C1_B
		OxyX1[1][2][1] = 0.7; // Probability X1 = X1_C to X1 = X1_B given C1 = C1_B

		X1.setParameters(QxX1, OxyX1);

		double[][] QxX2 = new double[6][2];
		QxX2[0][0] = 0.03; // Intensity X2 = X2_A given X1 = X1_A and C2 = C2_A
		QxX2[1][0] = 3.33; // Intensity X2 = X2_A given X1 = X1_A and C2 = C2_B
		QxX2[2][0] = 0.01; // Intensity X2 = X2_A given X1 = X1_B and C2 = C2_A
		QxX2[3][0] = 3.33; // Intensity X2 = X2_A given X1 = X1_B and C2 = C2_B
		QxX2[4][0] = 0.05; // Intensity X2 = X2_A given X1 = X1_C and C2 = C2_A
		QxX2[5][0] = 0.5; // Intensity X2 = X2_A given X1 = X1_C and C2 = C2_B
		QxX2[0][1] = 3.33; // Intensity X2 = X2_B given X1 = X1_A and C2 = C2_A
		QxX2[1][1] = 0.03; // Intensity X2 = X2_B given X1 = X1_A and C2 = C2_B
		QxX2[2][1] = 0.5; // Intensity X2 = X2_B given X1 = X1_B and C2 = C2_A
		QxX2[3][1] = 0.03; // Intensity X2 = X2_B given X1 = X1_B and C2 = C2_B
		QxX2[4][1] = 0.05; // Intensity X2 = X2_B given X1 = X1_C and C2 = C2_A
		QxX2[5][1] = 3.33; // Intensity X2 = X2_B given X1 = X1_C and C2 = C2_B

		double[][][] OxyX2 = new double[6][2][2];
		OxyX2[0][0][1] = 1; // Probability X2 = X2_A to X2 = X2_B given X1 = X1_A and C2 = C2_A
		OxyX2[0][1][0] = 1; // Probability X2 = X2_B to X2 = X2_A given X1 = X1_A and C2 = C2_A
		OxyX2[1][0][1] = 1; // Probability X2 = X2_A to X2 = X2_B given X1 = X1_B and C2 = C2_A
		OxyX2[1][1][0] = 1; // Probability X2 = X2_B to X2 = X2_A given X1 = X1_B and C2 = C2_A
		OxyX2[2][0][1] = 1; // Probability X2 = X2_A to X2 = X2_B given X1 = X1_C and C2 = C2_A
		OxyX2[2][1][0] = 1; // Probability X2 = X2_B to X2 = X2_A given X1 = X1_C and C2 = C2_A
		OxyX2[3][0][1] = 1; // Probability X2 = X2_A to X2 = X2_B given X1 = X1_A and C2 = C2_B
		OxyX2[3][1][0] = 1; // Probability X2 = X2_B to X2 = X2_A given X1 = X1_A and C2 = C2_B
		OxyX2[4][0][1] = 1; // Probability X2 = X2_A to X2 = X2_B given X1 = X1_B and C2 = C2_B
		OxyX2[4][1][0] = 1; // Probability X2 = X2_B to X2 = X2_A given X1 = X1_B and C2 = C2_B
		OxyX2[5][0][1] = 1; // Probability X2 = X2_A to X2 = X2_B given X1 = X1_C and C2 = C2_B
		OxyX2[5][1][0] = 1; // Probability X2 = X2_B to X2 = X2_A given X1 = X1_C and C2 = C2_B

		X2.setParameters(QxX2, OxyX2);

		CTBN<CIMNode> ctbn = new CTBN<CIMNode>(List.of(X1, X2), bn);

		mctbnc = new MCTBNC<CPTNode, CIMNode>(bn, ctbn);

		// Definition of the sequences to predict
		List<String> nameClassVariables = List.of("C1", "C2");

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence1.add(new String[] { "0.0", "X1_A", "X2_B", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "0.3", "X1_A", "X2_A", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "0.31", "X1_B", "X2_A", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "0.6", "X1_A", "X2_A", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "0.9", "X1_B", "X2_A", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "1.2", "X1_A", "X2_A", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "1.5", "X1_B", "X2_A", "C1_A", "C2_A" });
		dataSequence1.add(new String[] { "1.8", "X1_A", "X2_A", "C1_A", "C2_A" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence2.add(new String[] { "0.0", "X1_C", "X2_A", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "0.3", "X1_B", "X2_A", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "0.6", "X1_B", "X2_B", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "0.61", "X1_C", "X2_B", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "0.9", "X1_B", "X2_B", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "1.2", "X1_C", "X2_B", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "1.5", "X1_B", "X2_B", "C1_B", "C2_B" });
		dataSequence2.add(new String[] { "1.8", "X1_C", "X2_B", "C1_B", "C2_B" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence3.add(new String[] { "0.0", "X1_B", "X2_A", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "0.3", "X1_B", "X2_B", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "0.31", "X1_A", "X2_B", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "0.6", "X1_B", "X2_B", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "0.9", "X1_A", "X2_B", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "1.2", "X1_B", "X2_B", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "1.5", "X1_A", "X2_B", "C1_A", "C2_B" });
		dataSequence3.add(new String[] { "1.8", "X1_B", "X2_B", "C1_A", "C2_B" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "X1", "X2", "C1", "C2" });
		dataSequence4.add(new String[] { "0.0", "X1_B", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "0.3", "X1_C", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "0.31", "X1_B", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "0.6", "X1_C", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "0.9", "X1_B", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "1.2", "X1_C", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "1.5", "X1_B", "X2_B", "C1_B", "C2_A" });
		dataSequence4.add(new String[] { "1.8", "X1_C", "X2_B", "C1_B", "C2_A" });

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
		State expectedClassConfiguration = new State(Map.of("C1", "C1_A", "C2", "C2_A"));
		State actualClassConfiguration = predictions[0].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);
		// Unnormalized log-a-posteriori probability for C1 = C1_A and C2 = C2_A
		double ulpAA = // C1, C2
				Math.log(0.6) + Math.log(0.2)
				// X1
						+ (-3.3 * (0.3)) + (-3.3 * (0.31 - 0.3) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001)))
						+ (-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						+ (-3.3 * (0.9 - 0.6) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001)))
						+ (-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						+ (-3.3 * (1.5 - 1.2) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001)))
						+ (-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						// X2
						+ (-3.3 * (0.3) + Math.log(3.3)) + (-0.03 * (0.31 - 0.3)) + (-0.01 * (0.6 - 0.31))
						+ (-0.03 * (0.9 - 0.6)) + (-0.01 * (1.2 - 0.9)) + (-0.03 * (1.5 - 1.2)) + (-0.01 * (1.8 - 1.5));
		double ulpAB =
				// C1, C2
				Math.log(0.6) + Math.log(0.8)
				// X1
						+ (-3.3 * (0.3)) + (-3.3 * (0.31 - 0.3) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001)))
						+ (-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						+ (-3.3 * (0.9 - 0.6) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001)))
						+ (-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						+ (-3.3 * (1.5 - 1.2) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001)))
						+ (-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						// X2
						+ (-0.03 * (0.3) + Math.log(0.03)) + (-3.33 * (0.31 - 0.3)) + (-3.33 * (0.6 - 0.31))
						+ (-3.33 * (0.9 - 0.6)) + (-3.33 * (1.2 - 0.9)) + (-3.33 * (1.5 - 1.2)) + (-3.33 * (1.8 - 1.5));
		double ulpBA =
				// C1, C2
				Math.log(0.4) + Math.log(0.8) +
				// X1
						+(-10.0 * (0.3)) + (-10.0 * (0.31 - 0.3) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001)))
						+ (-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						+ (-10.0 * (0.9 - 0.6) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001)))
						+ (-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						+ (-10.0 * (1.5 - 1.2) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001)))
						+ (-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						// X2
						+ (-3.3 * (0.3) + Math.log(3.3)) + (-0.03 * (0.31 - 0.3)) + (-0.01 * (0.6 - 0.31))
						+ (-0.03 * (0.9 - 0.6)) + (-0.01 * (1.2 - 0.9)) + (-0.03 * (1.5 - 1.2)) + (-0.01 * (1.8 - 1.5));
		double ulpBB =
				// C1, C2
				Math.log(0.4) + Math.log(0.2)
				// X1
						+ (-10.0 * (0.3)) + (-10.0 * (0.31 - 0.3) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001)))
						+ (-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						+ (-10.0 * (0.9 - 0.6) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001)))
						+ (-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						+ (-10.0 * (1.5 - 1.2) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001)))
						+ (-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
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
				predictions[0].getProbabilities().get(new State(Map.of("C1", "C1_A", "C2", "C2_B"))), 0.001);
		assertEquals(expectedProbabilityBA,
				predictions[0].getProbabilities().get(new State(Map.of("C1", "C1_B", "C2", "C2_A"))), 0.001);
		assertEquals(expectedProbabilityBB,
				predictions[0].getProbabilities().get(new State(Map.of("C1", "C1_B", "C2", "C2_B"))), 0.001);

		// Sequence 2
		// Predict class configuration
		expectedClassConfiguration = new State(Map.of("C1", "C1_B", "C2", "C2_B"));
		actualClassConfiguration = predictions[1].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);

		// Sequence 3
		// Predict class configuration
		expectedClassConfiguration = new State(Map.of("C1", "C1_A", "C2", "C2_B"));
		actualClassConfiguration = predictions[2].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);

		// Sequence 4
		// Predict class configuration
		expectedClassConfiguration = new State(Map.of("C1", "C1_B", "C2", "C2_A"));
		actualClassConfiguration = predictions[3].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);
	}

}
