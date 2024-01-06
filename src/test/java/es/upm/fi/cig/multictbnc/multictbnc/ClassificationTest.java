package es.upm.fi.cig.multictbnc.multictbnc;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.multictbnc.util.Util;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the classification of sequences with a multidimensional continuous-time Bayesian network classifiers.
 *
 * @author Carlos Villa Blanco
 */
class ClassificationTest {
	static MultiCTBNC<CPTNode, CIMNode> multiCTBNC;
	static Dataset dataset;

	@BeforeAll
	static void setUp() {
		// Definition of the model

		// Define class variables
		CPTNode C1 = new CPTNode("C1", List.of("C1_A", "C1_B"), true);
		CPTNode C2 = new CPTNode("C2", List.of("C2_A", "C2_B"), true);

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

		BN<CPTNode> bn = new BN<>(List.of(C1, C2));

		// Define feature variables
		CIMNode X1 = new CIMNode("X1", List.of("X1_A", "X1_B", "X1_C"), false);
		CIMNode X2 = new CIMNode("X2", List.of("X2_A", "X2_B"), false);

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

		CTBN<CIMNode> ctbn = new CTBN<>(List.of(X1, X2), bn);

		multiCTBNC = new MultiCTBNC<>(bn, ctbn);

		// Definition of the sequences to predict
		List<String> nameClassVariables = List.of("C1", "C2");

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"Time", "X1", "X2", "C1", "C2"});
		dataSequence1.add(new String[]{"0.0", "X1_A", "X2_B", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"0.3", "X1_A", "X2_A", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"0.31", "X1_B", "X2_A", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"0.6", "X1_A", "X2_A", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"0.9", "X1_B", "X2_A", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"1.2", "X1_A", "X2_A", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"1.5", "X1_B", "X2_A", "C1_A", "C2_A"});
		dataSequence1.add(new String[]{"1.8", "X1_A", "X2_A", "C1_A", "C2_A"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"Time", "X1", "X2", "C1", "C2"});
		dataSequence2.add(new String[]{"0.0", "X1_C", "X2_A", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"0.3", "X1_B", "X2_A", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"0.6", "X1_B", "X2_B", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"0.61", "X1_C", "X2_B", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"0.9", "X1_B", "X2_B", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"1.2", "X1_C", "X2_B", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"1.5", "X1_B", "X2_B", "C1_B", "C2_B"});
		dataSequence2.add(new String[]{"1.8", "X1_C", "X2_B", "C1_B", "C2_B"});

		List<String[]> dataSequence3 = new ArrayList<>();
		dataSequence3.add(new String[]{"Time", "X1", "X2", "C1", "C2"});
		dataSequence3.add(new String[]{"0.0", "X1_B", "X2_A", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"0.3", "X1_B", "X2_B", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"0.31", "X1_A", "X2_B", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"0.6", "X1_B", "X2_B", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"0.9", "X1_A", "X2_B", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"1.2", "X1_B", "X2_B", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"1.5", "X1_A", "X2_B", "C1_A", "C2_B"});
		dataSequence3.add(new String[]{"1.8", "X1_B", "X2_B", "C1_A", "C2_B"});

		List<String[]> dataSequence4 = new ArrayList<>();
		dataSequence4.add(new String[]{"Time", "X1", "X2", "C1", "C2"});
		dataSequence4.add(new String[]{"0.0", "X1_B", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"0.3", "X1_C", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"0.31", "X1_B", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"0.6", "X1_C", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"0.9", "X1_B", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"1.2", "X1_C", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"1.5", "X1_B", "X2_B", "C1_B", "C2_A"});
		dataSequence4.add(new String[]{"1.8", "X1_C", "X2_B", "C1_B", "C2_A"});

		dataset = new Dataset("Time", nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
	}

	@Test
	void markovBlaketClassVariablesUsedForClassification() {
		// Define class variables
		CPTNode C1 = new CPTNode("C1", List.of("C1_A", "C1_B"), true);
		CPTNode C2 = new CPTNode("C2", List.of("C2_A", "C2_B"), true);
		CPTNode C3 = new CPTNode("C3", List.of("C3_A", "C3_B"), true);
		CPTNode C4 = new CPTNode("C4", List.of("C4_A", "C4_B"), true);
		BN<CPTNode> bn = new BN<>(List.of(C1, C2, C3, C4));

		// Define feature variables
		CIMNode X1 = new CIMNode("X1", List.of("X1_A", "X1_B", "X1_C"), false);
		CIMNode X2 = new CIMNode("X2", List.of("X2_A", "X2_B"), false);
		CIMNode X3 = new CIMNode("X3", List.of("X3_A", "X3_B", "X3_C"), false);
		CIMNode X4 = new CIMNode("X4", List.of("X4_A", "X4_B", "X4_C"), false);
		CIMNode X5 = new CIMNode("X5", List.of("X5_A", "X5_B", "X5_C"), false);
		CIMNode X6 = new CIMNode("X6", List.of("X6_A", "X6_B", "X6_C"), false);
		CIMNode X7 = new CIMNode("X7", List.of("X7_A", "X7_B", "X7_C"), false);
		CIMNode X8 = new CIMNode("X8", List.of("X8_A", "X8_B", "X8_C"), false);
		CIMNode X9 = new CIMNode("X9", List.of("X9_A", "X9_B", "X9_C"), false);
		CIMNode X10 = new CIMNode("X10", List.of("X10_A", "X10_B", "X10_C"), false);
		CIMNode X11 = new CIMNode("X11", List.of("X11_A", "X11_B", "X11_C"), false);
		CIMNode X12 = new CIMNode("X12", List.of("X12_A", "X12_B", "X12_C"), false);
		CIMNode X13 = new CIMNode("X13", List.of("X13_A", "X13_B", "X13_C"), false);
		CIMNode X14 = new CIMNode("X14", List.of("X14_A", "X14_B", "X14_C"), false);
		CIMNode X15 = new CIMNode("X15", List.of("X15_A", "X15_B", "X15_C"), false);

		// Definition of the structure of the bridge and feature subgraphs
		C1.setChild(C2);
		C2.setChild(C4);
		C3.setChild(C4);
		C1.setChild(X2);
		C1.setChild(X13);
		C2.setChild(X12);
		C3.setChild(X3);
		C3.setChild(X5);
		C4.setChild(X6);
		X1.setChild(X13);
		X2.setChild(X3);
		X4.setChild(X1);
		X4.setChild(X7);
		X7.setChild(X10);
		X9.setChild(X6);
		X10.setChild(X9);
		X11.setChild(X15);
		X13.setChild(X14);
		X14.setChild(X15);
		X15.setChild(X5);
		X15.setChild(X14);
		CTBN<CIMNode> ctbn = new CTBN<>(List.of(X1, X2, X3, X4, X5, X6, X7, X8, X9, X10, X11, X12, X13, X14, X15), bn);

		// Define multi-CTBNC
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<CPTNode, CIMNode>(bn, ctbn);

		// Extract feature variables that are in the Markov blanket of at least one class variable
		List<CIMNode> featureNodesMarkovBlankets = multiCTBNC.getNodesCTBNInMarkovBlanketClassVariables();

		// Check that feature variables outside of the previous Markov blankets are not included in the list
		List<CIMNode> expectedNodesInMarkovBlanket = List.of(X1, X2, X3, X5, X6, X9, X12, X13, X15);
		assertTrue(Util.listsOfNodeContainSameElements(featureNodesMarkovBlankets, expectedNodesInMarkovBlanket));
	}

	@Test
	void testClassificationResults() throws FileNotFoundException, UnreadDatasetException, ErroneousValueException {
		Long seed = 5744034L;
		String pathDataset = "src/test/resources/multictbnc/datatestclassificationresults";
		DatasetReader datasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset, 0);
		String timeVariable = "t";
		List<String> classVariables = List.of("C1", "C2", "C3", "C4");
		List<String> featureVariables = List.of("X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8", "X9", "X10", "X11",
				"X12", "X13", "X14", "X15");
		datasetReader.setVariables(timeVariable, classVariables, featureVariables);
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Hold-out", datasetReader,
				null,
				0.7, 0, true, true, seed);
		MultiCTBNC multiCTBNC = ClassifierFactory.getMultiCTBNC();
		Map<String, Double> results = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.97, results.get("Accuracy C1"), 0.0001);
		assertEquals(0.79, results.get("Accuracy C2"), 0.0001);
		assertEquals(0.77, results.get("Accuracy C3"), 0.0001);
		assertEquals(0.9967, results.get("Accuracy C4"), 0.0001);
		assertEquals(0.5967, results.get("Global accuracy"), 0.0001);
		assertEquals(0.8817, results.get("Mean accuracy"), 0.0001);
		assertEquals(0.8857, results.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.9033, results.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.8938, results.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.8847, results.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.9160, results.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.9001, results.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.5373, results.get("Global Brier score"), 0.0001);
	}

	@Test
	void testProbabilitiesClassConfigurations() {
		Prediction[] predictions = multiCTBNC.predict(dataset, true);

		// Sequence 1
		// Predict class configuration
		State expectedClassConfiguration = new State(Map.of("C1", "C1_A", "C2", "C2_A"));
		State actualClassConfiguration = predictions[0].getPredictedClasses();
		assertEquals(expectedClassConfiguration, actualClassConfiguration);
		// Unnormalised log-a-posteriori probability for C1 = C1_A and C2 = C2_A
		double ulpAA = // C1, C2
				Math.log(0.6) + Math.log(0.2)
						// X1
						+ (-3.3 * (0.3)) + (-3.3 * (0.31 - 0.3) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001))) +
						(-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001))) +
						(-3.3 * (0.9 - 0.6) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001))) +
						(-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001))) +
						(-3.3 * (1.5 - 1.2) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001))) +
						(-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						// X2
						+ (-3.3 * (0.3) + Math.log(3.3)) + (-0.03 * (0.31 - 0.3)) + (-0.01 * (0.6 - 0.31)) +
						(-0.03 * (0.9 - 0.6)) + (-0.01 * (1.2 - 0.9)) + (-0.03 * (1.5 - 1.2)) + (-0.01 * (1.8 - 1.5));
		double ulpAB =
				// C1, C2
				Math.log(0.6) + Math.log(0.8)
						// X1
						+ (-3.3 * (0.3)) + (-3.3 * (0.31 - 0.3) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001))) +
						(-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001))) +
						(-3.3 * (0.9 - 0.6) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001))) +
						(-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001))) +
						(-3.3 * (1.5 - 1.2) + Math.log(1 - Math.exp(-3.3 * 0.8 * 0.00001))) +
						(-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.7 * 0.00001)))
						// X2
						+ (-0.03 * (0.3) + Math.log(0.03)) + (-3.33 * (0.31 - 0.3)) + (-3.33 * (0.6 - 0.31)) +
						(-3.33 * (0.9 - 0.6)) + (-3.33 * (1.2 - 0.9)) + (-3.33 * (1.5 - 1.2)) + (-3.33 * (1.8 - 1.5));
		double ulpBA =
				// C1, C2
				Math.log(0.4) + Math.log(0.8) +
						// X1
						+(-10.0 * (0.3)) + (-10.0 * (0.31 - 0.3) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001))) +
						(-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001))) +
						(-10.0 * (0.9 - 0.6) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001))) +
						(-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001))) +
						(-10.0 * (1.5 - 1.2) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001))) +
						(-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						// X2
						+ (-3.3 * (0.3) + Math.log(3.3)) + (-0.03 * (0.31 - 0.3)) + (-0.01 * (0.6 - 0.31)) +
						(-0.03 * (0.9 - 0.6)) + (-0.01 * (1.2 - 0.9)) + (-0.03 * (1.5 - 1.2)) + (-0.01 * (1.8 - 1.5));
		double ulpBB =
				// C1, C2
				Math.log(0.4) + Math.log(0.2)
						// X1
						+ (-10.0 * (0.3)) + (-10.0 * (0.31 - 0.3) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001))) +
						(-3.3 * (0.6 - 0.31) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001))) +
						(-10.0 * (0.9 - 0.6) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001))) +
						(-3.3 * (1.2 - 0.9) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001))) +
						(-10.0 * (1.5 - 1.2) + Math.log(1 - Math.exp(-10.0 * 0.5 * 0.00001))) +
						(-3.3 * (1.8 - 1.5) + Math.log(1 - Math.exp(-3.3 * 0.2 * 0.00001)))
						// X2
						+ (-0.03 * (0.3) + Math.log(0.03)) + (-3.33 * (0.31 - 0.3)) + (-3.33 * (0.6 - 0.31)) +
						(-3.33 * (0.9 - 0.6)) + (-3.33 * (1.2 - 0.9)) + (-3.33 * (1.5 - 1.2)) + (-3.33 * (1.8 - 1.5));
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