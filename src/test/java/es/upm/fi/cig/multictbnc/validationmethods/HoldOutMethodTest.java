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

/**
 * Tests the hold-out validation method.
 *
 * @author Carlos Villa Blanco
 */
public class HoldOutMethodTest {
	static String pathDataset = "src/test/resources/validationmethods/dataset";
	static String timeVariable = "t";
	static List<String> featureVariables = List.of("X1", "X2", "X3", "X4", "X5");
	boolean estimateProbabilities = true;
	boolean shuffleSequences = true;
	Long seed = 87428L;

	@Test
	void testHoldOutValidation() throws UnreadDatasetException, ErroneousValueException, FileNotFoundException {
		// Define dataset
		List<String> classVariables = List.of("C1", "C2", "C3");
		DatasetReader datasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset, 0);
		datasetReader.setVariables(timeVariable, classVariables, featureVariables);
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNC();
		// Hold-out validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Hold-out validation",
				datasetReader, null, 0.7, 0, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.3400, resultsCV.get("Global accuracy"), 0.0001);
		assertEquals(0.6844, resultsCV.get("Mean accuracy"), 0.0001);
		assertEquals(0.5129, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.4916, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.4793, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6844, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6844, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6844, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8233, resultsCV.get("Global Brier score"), 0.0001);
	}

	@Test
	void testUnidimensionalHoldOutValidation()
			throws UnreadDatasetException, ErroneousValueException, FileNotFoundException {
		// Define dataset
		List<String> classVariables = List.of("C2");
		DatasetReader datasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset, 0);
		datasetReader.setVariables(timeVariable, classVariables, featureVariables);
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNC();
		// Hold-out validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Hold-out validation",
				datasetReader, null, 0.7, 0, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.8267, resultsCV.get("Accuracy"), 0.0001);
		assertEquals(0.7781, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.7047, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.7300, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.8267, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.8267, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.8267, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.2582, resultsCV.get("Brier score"), 0.0001);
	}

}
