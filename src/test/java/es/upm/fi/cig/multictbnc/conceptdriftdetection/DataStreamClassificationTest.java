package es.upm.fi.cig.multictbnc.conceptdriftdetection;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.DataStreamExperiment;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.util.Util;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataStreamClassificationTest {
    static Queue<String> experimentConfigNoUpdate;
    static Queue<String> experimentConfigLocalUpdate;
    static Queue<String> experimentConfigGlobalUpdate;

    @BeforeAll
    public static void setUp() throws FileNotFoundException, UnreadDatasetException {
        // Define batches
        String nameClassVariables = "C1;C2;C3;C4";
        String detectionThreshold = "1.1";
        String magnitudeThreshold = "1";
        String batchSize = "1000";
        String penalisationFunctionConceptDriftScore = "No";
        String pathDataStream = "src/test/resources/conceptdriftdetection/datastream";
        experimentConfigNoUpdate = Util.arrayToQueue(new String[]{pathDataStream, batchSize, "NO", detectionThreshold, magnitudeThreshold, "true", "0", nameClassVariables, penalisationFunctionConceptDriftScore, "false"});
        experimentConfigLocalUpdate = Util.arrayToQueue(new String[]{pathDataStream, batchSize, "LOCAL", detectionThreshold, magnitudeThreshold, "true", "0", nameClassVariables, penalisationFunctionConceptDriftScore, "false"});
        experimentConfigGlobalUpdate = Util.arrayToQueue(new String[]{pathDataStream, batchSize, "GLOBAL", detectionThreshold, magnitudeThreshold, "true", "0", nameClassVariables, penalisationFunctionConceptDriftScore, "false"});
    }

    @Test
    public void noUpdateTest() throws ErroneousValueException {
        DataStreamExperiment dataStreamExperiment = new DataStreamExperiment();
        dataStreamExperiment.setAreResultsSaved(false);
        dataStreamExperiment.main(experimentConfigNoUpdate);
        assertEquals(0.3355, dataStreamExperiment.getMeanGlobalAccuracy(), 0.0001f);
        assertEquals(0.7058, dataStreamExperiment.getMeanMeanAccuracy(), 0.0001f);
        assertEquals(0.5343, dataStreamExperiment.getMeanMacroAveragedF1Score(), 0.0001f);
        assertEquals(0.7058, dataStreamExperiment.getMeanMicroAveragedF1Score(), 0.0001f);
        assertEquals(0.9452, dataStreamExperiment.getMeanGlobalBrierScore(), 0.0001f);
        assertEquals(0, dataStreamExperiment.getNumTimesModelUpdated());
    }

    @Test
    public void localUpdateTest() throws ErroneousValueException {
        DataStreamExperiment dataStreamExperiment = new DataStreamExperiment();
        dataStreamExperiment.setAreResultsSaved(false);
        dataStreamExperiment.main(experimentConfigLocalUpdate);
        assertEquals(0.3734, dataStreamExperiment.getMeanGlobalAccuracy(), 0.0001f);
        assertEquals(0.7352, dataStreamExperiment.getMeanMeanAccuracy(), 0.0001f);
        assertEquals(0.5662, dataStreamExperiment.getMeanMacroAveragedF1Score(), 0.0001f);
        assertEquals(0.7352, dataStreamExperiment.getMeanMicroAveragedF1Score(), 0.0001f);
        assertEquals(0.7725, dataStreamExperiment.getMeanGlobalBrierScore(), 0.0001f);
        assertEquals(6, dataStreamExperiment.getNumTimesModelUpdated());
    }

    @Test
    public void globalUpdateTest() throws ErroneousValueException {
        DataStreamExperiment dataStreamExperiment = new DataStreamExperiment();
        dataStreamExperiment.setAreResultsSaved(false);
        dataStreamExperiment.main(experimentConfigGlobalUpdate);
        assertEquals(0.3357, dataStreamExperiment.getMeanGlobalAccuracy(), 0.0001f);
        assertEquals(0.7245, dataStreamExperiment.getMeanMeanAccuracy(), 0.0001f);
        assertEquals(0.5160, dataStreamExperiment.getMeanMacroAveragedF1Score(), 0.0001f);
        assertEquals(0.7245, dataStreamExperiment.getMeanMicroAveragedF1Score(), 0.0001f);
        assertEquals(0.7900, dataStreamExperiment.getMeanGlobalBrierScore(), 0.0001f);
        assertEquals(7, dataStreamExperiment.getNumTimesModelUpdated());
    }

}
