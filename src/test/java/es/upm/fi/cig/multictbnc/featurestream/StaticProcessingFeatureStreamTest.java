package es.upm.fi.cig.multictbnc.featurestream;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.data.reader.FeatureStreamMultipleCSVReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.FeatureStreamExperiment;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.TestDatasetMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StaticProcessingFeatureStreamTest {
    static String pathData;
    static String pathTestDataset;
    static String nameTimeVariable;
    static String nameClassVariables;
    static Queue<String> paramExperiment;

    @BeforeAll
    public static void setUp() throws IOException, UnreadDatasetException, ErroneousValueException {
        pathData = "src/test/resources/fss/feature_stream";
        pathTestDataset = "src/test/resources/fss/feature_stream/experiment1/test_dataset";
        nameTimeVariable = "t";
        nameClassVariables = "C1;C2";
        paramExperiment = new LinkedList<String>(List.of(pathData, nameClassVariables, "false"));
    }

    @Test
    void checkCompleteDatasetRetrievedFromFeatureStreamTest() throws FileNotFoundException, UnreadDatasetException {
        String pathFeatureStream = pathData + "/experiment1/feature_stream";
        String pathInitialDataset = "src/test/resources/fss/feature_stream/experiment1/initial_dataset";
        DatasetReader initialDatasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathInitialDataset, 0);
        initialDatasetReader.setTimeAndClassVariables(nameTimeVariable, Arrays.asList(nameClassVariables.split(";")));
        FeatureStreamMultipleCSVReader featureStreamReader = new FeatureStreamMultipleCSVReader(pathFeatureStream,
                initialDatasetReader.readDataset(), nameTimeVariable);
        Dataset completeFeatureStreamDataset = featureStreamReader.readCompleteDataset();
        List<String> nameVariables = completeFeatureStreamDataset.getNameAllVariables();
        assertEquals(18, nameVariables.size());
        assertTrue(nameVariables.contains("t"));
        assertTrue(nameVariables.contains("C1"));
        assertTrue(nameVariables.contains("C2"));
        assertTrue(nameVariables.contains("X1"));
        assertTrue(nameVariables.contains("X2"));
        assertTrue(nameVariables.contains("X3"));
        assertTrue(nameVariables.contains("X4"));
        assertTrue(nameVariables.contains("X5"));
        assertTrue(nameVariables.contains("X6"));
        assertTrue(nameVariables.contains("X7"));
        assertTrue(nameVariables.contains("X8"));
        assertTrue(nameVariables.contains("X9"));
        assertTrue(nameVariables.contains("X10"));
        assertTrue(nameVariables.contains("X11"));
        assertTrue(nameVariables.contains("X12"));
        assertTrue(nameVariables.contains("X13"));
        assertTrue(nameVariables.contains("X14"));
        assertTrue(nameVariables.contains("X15"));
        assertEquals(1000, completeFeatureStreamDataset.getNumDataPoints());
    }

    @Test
    void performBatchTrainingOnFeatureStreamTest() throws UnreadDatasetException, IOException, ErroneousValueException {
        FeatureStreamExperiment featureStreamExperiment = new FeatureStreamExperiment();
        featureStreamExperiment.setParametersExperiment(paramExperiment);
        String typeProcessing = "Batch";
        MultiCTBNC<CPTNode, CIMNode> model = featureStreamExperiment.executeExperiment(pathData + "/experiment1", typeProcessing);
        List<String> subsetSelectedFeatures = model.getNameFeatureVariables();
        assertEquals(15, subsetSelectedFeatures.size());
        //Validate model
        DatasetReader testDatasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathTestDataset, 0);
        testDatasetReader.setTimeAndClassVariables(nameTimeVariable, Arrays.asList(nameClassVariables.split(";")));
        ValidationMethod validationMethod = new TestDatasetMethod(testDatasetReader, true);
        Map<String, Double> resultsLearningStaticDataset = validationMethod.evaluate(model);
        assertEquals(0.78, resultsLearningStaticDataset.get("Global accuracy"), 0.0001);
        assertEquals(0.86, resultsLearningStaticDataset.get("Mean accuracy"), 0.0001);
        assertEquals(0.8118, resultsLearningStaticDataset.get("Macro-averaged F1 score"), 0.0001);
        assertEquals(0.86, resultsLearningStaticDataset.get("Micro-averaged F1 score"), 0.0001);
        assertEquals(0.3315, resultsLearningStaticDataset.get("Global Brier score"), 0.0001);
    }
}
