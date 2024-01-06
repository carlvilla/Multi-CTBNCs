package es.upm.fi.cig.multictbnc.multictbnc;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.MultipleCSVReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class LearnSubmodels {

    static Dataset dataset;
    static BNLearningAlgorithms bnLearningAlgs;
    static CTBNLearningAlgorithms ctbnLearningAlgs;
    static boolean[][] expectedAdjacencyMatrix;

    /**
     * This method reads a training dataset that will be used to generate Multi-CTBNC under different learning
     * algorithms. The dataset is formed by five discrete-state variables, four feature variables (X1, X2, X3 and X4) and
     * two class variables (C1 and C2). The data were sampled from a Multi-CTBNC with the following structure:
     * X4 <- X3 -> X2 <-> X1 <- C1 -> C2 -> X2.
     *
     * @throws FileNotFoundException  thrown if a file was not found
     * @throws UnreadDatasetException thrown if a dataset could not be read
     */
    @BeforeAll
    public static void setUp() throws FileNotFoundException, UnreadDatasetException {
        // Define dataset
        String nameTimeVariable = "t";
        List<String> nameClassVariables = List.of("C1", "C2");
        List<String> nameFeatureVariables = List.of("X1", "X2", "X3", "X4");
        String pathDataset = "src/test/resources/multictbnc/trainingdata";
        DatasetReader dr = new MultipleCSVReader(pathDataset);
        dr.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
        dataset = dr.readDataset();
        // Define learning algorithms
        // Algorithms to learn BN (class subgraph of Multi-CTBNC)
        BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
                "Maximum likelihood estimation", 0.0);
        Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "BIC");
        StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
                "Hill climbing", paramSLA);
        bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
        // Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
        CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
                CTBNParameterLearningAlgorithmFactory.getAlgorithm(
                        "Maximum likelihood estimation", 0.0, 0.0);
        StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
                "Hill climbing", paramSLA);
        ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
    }

    /**
     * Learns a DAG-maxK multidimensional continuous time Bayesian network classifier.
     */
    @Test
    public void learnDAGMaxKMultiCTBNC() throws ErroneousValueException {
        // Define model
        Map<String, String> hyperparameters = Map.of("maxK", "1");
        MultiCTBNC multiCTBNC = ClassifierFactory.getMultiCTBNC("DAG-maxK Multi-CTBNC", bnLearningAlgs,
                ctbnLearningAlgs, hyperparameters);
        // Learn model
        multiCTBNC.learn(dataset);
        // Variable X2 is expected to have only one feature variable as a parent instead of two
        boolean[][] expectedAdjacencyMatrix = new boolean[][]{{false, true, false, false, false, false},
                {true, false, false, false, false, false}, {false, false, false, true, false, false},
                {false, false, false, false, false, false}, {true, false, false, false, false, true},
                {false, true, false, false, false, false}};
        assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
    }

    /**
     * Learns a DAG-maxK multidimensional continuous time Bayesian network classifier.
     */
    @Test
    public void learnEmptyDigraphMultiCTBNC() throws ErroneousValueException {
        // Define model
        MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNC("Empty-digraph Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs);
        // Learn model
        multiCTBNC.learn(dataset);
        // Class subgraph is expected to be empty
        expectedAdjacencyMatrix = new boolean[][]{{false, true, false, false, false, false},
                {true, false, false, false, false, false}, {false, true, false, true, false, false},
                {false, false, false, false, false, false}, {true, false, false, false, false, false},
                {false, true, false, false, false, false}};
        assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
    }

    /**
     * Learns a DAG-maxK multidimensional continuous time Bayesian network classifier.
     */
    @Test
    public void learnEmptyMaxKMultiCTBNC() throws ErroneousValueException {
        // Define model
        Map<String, String> hyperparameters = Map.of("maxK", "1");
        MultiCTBNC<CPTNode, CIMNode> multiCTBNC = ClassifierFactory.getMultiCTBNC("Empty-maxK Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs, hyperparameters);
        // Learn model
        multiCTBNC.learn(dataset);
        // Variable X2 is expected to have only one feature variable as a parent and the class subgraph to be empty
        boolean[][] expectedAdjacencyMatrix = new boolean[][]{{false, true, false, false, false, false},
                {true, false, false, false, false, false}, {false, false, false, true, false, false},
                {false, false, false, false, false, false}, {true, false, false, false, false, false},
                {false, true, false, false, false, false}};
        assertArrayEquals(expectedAdjacencyMatrix, multiCTBNC.getAdjacencyMatrix());
    }

}