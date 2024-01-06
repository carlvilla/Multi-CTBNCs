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
 * Tests the validation method that uses a separate test dataset.
 *
 * @author Carlos Villa Blanco
 */
public class TestDatasetMethodTest {
	static String pathDataset = "src/test/resources/validationmethods/dataset";
	static String pathTestDataset = "src/test/resources/validationmethods/testdataset";
	static String timeVariable = "t";
	static List<String> classVariables = List.of("C1", "C2", "C3");
	static List<String> featureVariables = List.of("X1", "X2", "X3", "X4", "X5");
	static DatasetReader datasetReader;
	static DatasetReader testDatasetReader;
	boolean estimateProbabilities = true;
	boolean shuffleSequences = true;
	Long seed = 34940L;

	@BeforeAll
	public static void setUp() throws FileNotFoundException, UnreadDatasetException {
		datasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset, 0);
		testDatasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathTestDataset, 0);
		datasetReader.setVariables(timeVariable, classVariables, featureVariables);
		testDatasetReader.setVariables(timeVariable, classVariables, featureVariables);
	}

	@Test
	void testTestDatasetMethod() throws UnreadDatasetException, ErroneousValueException {
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNC();
		// Cross-validation
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Test dataset", datasetReader,
				testDatasetReader, 0, 0, estimateProbabilities, shuffleSequences, seed);
		Map<String, Double> resultsCV = validationMethod.evaluate(multiCTBNC);
		assertEquals(0.2950, resultsCV.get("Global accuracy"), 0.0001);
		assertEquals(0.6550, resultsCV.get("Mean accuracy"), 0.0001);
		assertEquals(0.5350, resultsCV.get("Macro-averaged precision"), 0.0001);
		assertEquals(0.4743, resultsCV.get("Macro-averaged recall"), 0.0001);
		assertEquals(0.4671, resultsCV.get("Macro-averaged F1 score"), 0.0001);
		assertEquals(0.6550, resultsCV.get("Micro-averaged precision"), 0.0001);
		assertEquals(0.6550, resultsCV.get("Micro-averaged recall"), 0.0001);
		assertEquals(0.6550, resultsCV.get("Micro-averaged F1 score"), 0.0001);
		assertEquals(0.8355, resultsCV.get("Global Brier score"), 0.0001);
	}
}
