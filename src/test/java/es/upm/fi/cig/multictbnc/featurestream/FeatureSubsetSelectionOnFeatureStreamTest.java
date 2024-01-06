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

import static org.junit.jupiter.api.Assertions.*;

class FeatureSubsetSelectionOnFeatureStreamTest {
    static String pathData;
    static String pathTestDataset;
    static String nameTimeVariable;
    static String nameClassVariables;
    static Queue<String> paramExperiment;
    private static double SIGTIMETRANSITIONHYPOTHESIS_FSS = 0.00001;
    private static double SIGSTATETOSTATETRANSITIONHYPOTHESIS_FSS = 0.00001;

    @BeforeAll
    public static void setUp() throws IOException, UnreadDatasetException, ErroneousValueException {
        pathData = "src/test/resources/fss/feature_stream";
        pathTestDataset = "src/test/resources/fss/feature_stream/experiment1/test_dataset";
        nameTimeVariable = "t";
        nameClassVariables = "C1;C2";
        paramExperiment = new LinkedList<>(List.of(pathData, nameClassVariables, "false"));
    }

    @Test
    void checkLabelPowersetDatasetTest() throws FileNotFoundException, UnreadDatasetException {
        String pathFeatureStream = pathData + "/experiment1/feature_stream";
        String pathInitialDataset = "src/test/resources/fss/feature_stream/experiment1/initial_dataset";
        DatasetReader initialDatasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathInitialDataset, 0);
        initialDatasetReader.setTimeAndClassVariables(nameTimeVariable, Arrays.asList(nameClassVariables.split(";")));
        FeatureStreamMultipleCSVReader featureStreamReader = new FeatureStreamMultipleCSVReader(pathFeatureStream,
                initialDatasetReader.readDataset(), nameTimeVariable);
        Dataset completeFeatureStreamDataset = featureStreamReader.readCompleteDataset();
        Dataset labelPowersetDataset = completeFeatureStreamDataset.getLabelPowerset();
        List<String> nameClassVariables = labelPowersetDataset.getNameClassVariables();
        List<String> nameFeatureVariables = labelPowersetDataset.getNameFeatureVariables();
        assertTrue(nameClassVariables.contains("ClassLP"));
        assertFalse(nameClassVariables.contains("C1"));
        assertFalse(nameClassVariables.contains("C2"));
        assertEquals(1, nameClassVariables.size());
        assertEquals(15, nameFeatureVariables.size());
        assertEquals(1000, completeFeatureStreamDataset.getNumDataPoints());
        List<String> statesClassVariables = labelPowersetDataset.getPossibleStatesVariable("ClassLP");
        assertEquals(9, statesClassVariables.size());
        assertTrue(statesClassVariables.contains("C1_AC2_A"));
    }

    @Test
    void performModelRetrainingWithOnlineFSSOnFeatureStreamTest() throws UnreadDatasetException, IOException, ErroneousValueException {
        FeatureStreamExperiment featureStreamExperiment = new FeatureStreamExperiment();
        featureStreamExperiment.setParametersExperiment(paramExperiment);
        String typeProcessing = "Retraining(FSS)";
        MultiCTBNC<CPTNode, CIMNode> model = featureStreamExperiment.executeExperiment(pathData + "/experiment1", typeProcessing, SIGTIMETRANSITIONHYPOTHESIS_FSS, SIGSTATETOSTATETRANSITIONHYPOTHESIS_FSS);
        List<String> subsetSelectedFeatures = model.getNameFeatureVariables();
        assertEquals(2, subsetSelectedFeatures.size());
        assertTrue(subsetSelectedFeatures.contains("X5"));
        assertTrue(subsetSelectedFeatures.contains("X13"));
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