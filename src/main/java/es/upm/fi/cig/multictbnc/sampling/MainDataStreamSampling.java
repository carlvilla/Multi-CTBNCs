package es.upm.fi.cig.multictbnc.sampling;

import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.data.writer.MultipleCSVWriter;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.util.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class serves as the entry point for the data stream sampling application. It contains methods for
 * generating data streams with concept drifts.
 *
 * @author Carlos Villa Blanco
 */
public class MainDataStreamSampling {

    /**
     * Entry point of the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Application.launch(MainDataStreamSamplingFX.class);
    }

    /**
     * This class represents the JavaFX application for data stream sampling.
     *
     * @author Carlos Villa Blanco
     */
    public static class MainDataStreamSamplingFX extends Application {
        // Noise added to datasets
        private static final double percentageNoisyStates = 0.01;
        private static final double stdDeviationGaussianNoiseWaitingTime = 0.05;
        private static final int numSequencesTrainDataset = 5000;
        private static final int numSequencesDataStream = 60000;
        private static final int numDataStreamsToGenerate = 10;
        private static final int numBatchesPerConcept = 10;
        private static final int numConceptDrifts = 5; // Five concept drifts imply sampling from 6 models
        private static final double durationSequences = 5;
        // Extreme probabilities (only for binary class variables)
        private static final boolean forceExtremeProb = false;
        // Minimum and maximum values of the intensities
        private static final int minIntensity = 1;
        private static final int maxIntensity = 3;
        // Maximum number of feature variables that can be parents of others subgraph of the initial model
        private static final int MAXNUMPARENTS = 5;
        private static final Logger logger = LogManager.getLogger(MainDataStreamSampling.class);
        // Destination folder
        private static final String pathDataStreams = "datasets/synthetic/chapter8/data_stream/";

        @Override
        public void start(Stage primaryStage) throws Exception {
            String[] conceptDriftRates = new String[]{"Stationary", "Gradual", "Abrupt"};
            for (String conceptDriftRate : conceptDriftRates) {
                for (int idxDataStream = 1; idxDataStream <= numDataStreamsToGenerate; idxDataStream++) {
                    Path path = Paths.get(pathDataStreams);
                    double percentageChangedNodes;
                    switch (conceptDriftRate) {
                        case ("Stationary"):
                            path = path.resolve("no_concept_drift/experiment" + idxDataStream);
                            percentageChangedNodes = 0;
                            break;
                        case ("Gradual"):
                            path = path.resolve("gradual_concept_drift/experiment" + idxDataStream);
                            percentageChangedNodes = 0.2;
                            break;
                        case ("Abrupt"):
                            path = path.resolve("abrupt_concept_drift/experiment" + idxDataStream);
                            percentageChangedNodes = 0.5;
                            break;
                        default:
                            logger.error("Concept drift rate not defined");
                            return;
                    }
                    String pathTrainDataset = path.resolve("train").toString();
                    String pathDataStream = path.resolve("data_stream").toString();
                    List<MultiCTBNC<CPTNode, CIMNode>> modelsStream = new ArrayList<>();
                    // Initial model from which data is sampled
                    MultiCTBNC<CPTNode, CIMNode> initialMultiCTBNC = generateInitialModel(forceExtremeProb,
                            minIntensity, maxIntensity, MAXNUMPARENTS, path.toString());
                    modelsStream.add(initialMultiCTBNC);
                    modelsStream = generateModelsDataStream(percentageChangedNodes, modelsStream, path.toString());
                    DataSampler.generateDataset(initialMultiCTBNC, numSequencesTrainDataset,
                            durationSequences, percentageNoisyStates,
                            stdDeviationGaussianNoiseWaitingTime, pathTrainDataset);
                    generateDataStream(numSequencesDataStream, durationSequences, pathDataStream, modelsStream);
                    logger.info("Data stream {} generated!", idxDataStream);
                }
            }
            logger.info("All data streams generated!");
            Platform.exit();
        }

        /**
         * Generate a Multi-CTBNC for the selected experiment.
         *
         * @param forceExtremeProb true to force the probabilities of the CPTs to be extreme (0 to 0.3 or 0.7 to 1)
         * @param minIntensity     minimum value of the intensities of the CIMs
         * @param maxIntensity     maximum value of the intensities of the CIMs
         * @return a {@code MultiCTBNC}
         */
        private static MultiCTBNC<CPTNode, CIMNode> generateInitialModel(boolean forceExtremeProb, int minIntensity,
                                                                         int maxIntensity, int maxNumParents, String pathDataStream) {
            // Define class variables. Specify their names and sample spaces.
            BN<CPTNode> CS = getBayesianNetworkInitialClassSubgraph();
            // Define feature variables. Specify their names and sample spaces.
            CIMNode X1 = generateFeatureVariable("X1", List.of("X1_A", "X1_B", "X1_C", "X1_D"));
            CIMNode X2 = generateFeatureVariable("X2", List.of("X2_A", "X2_B", "X2_C", "X2_D"));
            CIMNode X3 = generateFeatureVariable("X3", List.of("X3_A", "X3_B", "X3_C", "X3_D"));
            CIMNode X4 = generateFeatureVariable("X4", List.of("X4_A", "X4_B", "X4_C", "X4_D"));
            CIMNode X5 = generateFeatureVariable("X5", List.of("X5_A", "X5_B", "X5_C", "X5_D"));
            CIMNode X6 = generateFeatureVariable("X6", List.of("X6_A", "X6_B", "X6_C", "X6_D"));
            CIMNode X7 = generateFeatureVariable("X7", List.of("X7_A", "X7_B", "X7_C", "X7_D"));
            CIMNode X8 = generateFeatureVariable("X8", List.of("X8_A", "X8_B", "X8_C", "X8_D"));
            CIMNode X9 = generateFeatureVariable("X9", List.of("X9_A", "X9_B", "X9_C", "X9_D"));
            CIMNode X10 = generateFeatureVariable("X10", List.of("X10_A", "X10_B", "X10_C", "X10_D"));
            CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5, X6, X7, X8, X9, X10), CS);
            // Randomly define the structure of the initial Multi-CTBNC
            int numClassVariables = CS.getNumNodes();
            int numFeatureVariables = FBS.getNumNodes();
            boolean[][] adjacencyMatrix = new boolean[numClassVariables + numFeatureVariables][numClassVariables +
                    numFeatureVariables];
            // Generate random class subgraph
            adjacencyMatrix = generateRandomClassSubgraph(numFeatureVariables, adjacencyMatrix);
            // Generate random bridge and feature subgraphs
            adjacencyMatrix = generateRandomBridgeAndFeatureSubgraph(maxNumParents, numFeatureVariables, adjacencyMatrix);
            // Definition of the initial Multi-CTBNC
            MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(CS, FBS);
            multiCTBNC.setStructure(adjacencyMatrix);
            // Definition of the CPTs and CIMs
            DataSampler.generateRandomCPTs(multiCTBNC.getBN(), forceExtremeProb);
            DataSampler.generateRandomCIMs(multiCTBNC.getCTBN(), minIntensity, maxIntensity);
            // Display the initial model
            displayModel(multiCTBNC, 0, null, pathDataStream);
            System.out.println("Initial model:");
            System.out.println(multiCTBNC);
            return multiCTBNC;
        }

        private static List<MultiCTBNC<CPTNode, CIMNode>> generateModelsDataStream(double percentageChangedNodes,
                                                                                   List<MultiCTBNC<CPTNode, CIMNode>> modelsStream, String pathDataStream) {
            for (int i = 0; i < numConceptDrifts; i++) {
                MultiCTBNC<CPTNode, CIMNode> modifiedMultiCTBNC = generateModifiedModel(
                        modelsStream.get(modelsStream.size() - 1), i + 1, forceExtremeProb, minIntensity, maxIntensity,
                        MAXNUMPARENTS, percentageChangedNodes, pathDataStream);
                modelsStream.add(modifiedMultiCTBNC);
            }
            return modelsStream;
        }

        private static void generateDataStream(int numSequencesDataStream, double durationSequences,
                                               String pathDataStream, List<MultiCTBNC<CPTNode, CIMNode>> models) {
            logger.info("Generating data stream with {} sequences in {}", numSequencesDataStream, pathDataStream);
            List<Entry<int[], MultiCTBNC<CPTNode, CIMNode>>> evolutionDataStream = new ArrayList<>();
            int numSequencesPerConcept = (numSequencesDataStream / (numConceptDrifts + 1));
            for (int idxModel = 0; idxModel < models.size(); idxModel++) {
                int idxBeg = idxModel * numSequencesPerConcept;
                int idxEnd = idxBeg + numSequencesPerConcept - 1;
                evolutionDataStream.add(new SimpleEntry<>(new int[]{idxBeg, idxEnd}, models.get(idxModel)));
            }
            // Sample data stream sequences from the Multi-CTBNCs
            sampleSequencesDataStream(numSequencesDataStream, durationSequences, evolutionDataStream, pathDataStream);
        }

        private static BN<CPTNode> getBayesianNetworkInitialClassSubgraph() {
            CPTNode C1 = generateClassVariable("C1", List.of("C1_A", "C1_B", "C1_C"));
            CPTNode C2 = generateClassVariable("C2", List.of("C2_A", "C2_B", "C2_C"));
            CPTNode C3 = generateClassVariable("C3", List.of("C3_A", "C3_B", "C3_C"));
            CPTNode C4 = generateClassVariable("C4", List.of("C4_A", "C4_B", "C4_C"));
            logger.info("Generating data stream for multi-dimensional classification problem");
            return new BN<>(List.of(C1, C2, C3, C4));
        }

        private static CIMNode generateFeatureVariable(String name, List<String> states) {
            CIMNode featureVariableNode = new CIMNode(name, states, false);
            return featureVariableNode;
        }

        private static boolean[][] generateRandomClassSubgraph(int idxFirstClassVariable,
                                                               boolean[][] adjacencyMatrix) {
            Random random = new Random();
            DAG dag = new DAG();
            for (int i = idxFirstClassVariable; i < adjacencyMatrix.length; i++) {
                for (int j = idxFirstClassVariable; j < adjacencyMatrix.length; j++) {
                    if (i != j) {
                        boolean addEdge = random.nextBoolean();
                        adjacencyMatrix[i][j] = addEdge;
                        if (addEdge && !dag.isStructureLegal(adjacencyMatrix, null)) {
                            adjacencyMatrix[i][j] = false;
                        }
                    }
                }
            }
            return adjacencyMatrix;
        }

        private static boolean[][] generateRandomBridgeAndFeatureSubgraph(int maxNumParents, int numFeatureVariables,
                                                                          boolean[][] adjacencyMatrix) {
            for (int idxFeature = 0; idxFeature < numFeatureVariables; idxFeature++) {
                // Define both the bridge and the feature subgraph
                List<Integer> idxParents = getRandomParents(idxFeature, adjacencyMatrix.length, maxNumParents);
                for (int idxParent : idxParents)
                    adjacencyMatrix[idxParent][idxFeature] = true;
            }
            return adjacencyMatrix;
        }

        private static void displayModel(MultiCTBNC<CPTNode, CIMNode> model, int idxModel,
                                         List<Integer> idxNodesToChange, String pathDataStream) {
            String titleWindow = "Model from batch " + numBatchesPerConcept * idxModel + " to " +
                    (numBatchesPerConcept * (idxModel + 1) - 1) + "(" +
                    numSequencesDataStream / (numConceptDrifts + 1) * idxModel + "," +
                    ((numSequencesDataStream / (numConceptDrifts + 1)) * (idxModel + 1) - 1) + ")";
            model.saveGraph(pathDataStream, titleWindow, idxNodesToChange);
        }

        /**
         * Generates a modified Multi-CTBNC to simulate concept drift.
         *
         * @param previousMultiCTBNC     Multi-CTBNC to be modified
         * @param idxModel               index of the current model
         * @param forceExtremeProb       boolean indicating whether to force extreme probabilities in CPTs
         * @param minIntensity           minimum value of the intensities for CIMs
         * @param maxIntensity           maximum value of the intensities for CIMs
         * @param maxNumParents          maximum number of parent nodes for feature variables
         * @param percentageChangedNodes percentage of nodes to change in the model
         * @param pathDataStream         path to save the data stream
         * @return modified Multi-CTBNC
         */
        public static MultiCTBNC<CPTNode, CIMNode> generateModifiedModel(
                MultiCTBNC<CPTNode, CIMNode> previousMultiCTBNC, int idxModel, boolean forceExtremeProb,
                int minIntensity, int maxIntensity, int maxNumParents, double percentageChangedNodes, String pathDataStream) {
            // Define model used to simulate a concept drift
            MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(previousMultiCTBNC.getBN(),
                    previousMultiCTBNC.getCTBN());
            // Randomly define the new adjacency matrix
            boolean[][] adjacencyMatrix = multiCTBNC.getAdjacencyMatrix();
            List<Integer> idxNodesToChange = new ArrayList<>();
            if (percentageChangedNodes > 0) {
                int numNodes = multiCTBNC.getNumNodes();
                int numClassVariableNodes = multiCTBNC.getNumClassVariables();
                int numFeatureVariableNodes = multiCTBNC.getNumFeatureVariables();
                int numNodesToChange = (int) (numNodes * percentageChangedNodes);
                // Get indexes all nodes
                List<Integer> idxNodes = IntStream.rangeClosed(0, numNodes - 1).boxed().collect(Collectors.toList());
                // Choose the nodes whose dependencies are changed
                idxNodesToChange = Util.getRandomElements(idxNodes, numNodesToChange);
                // Class subgraph
                // Retrieve indexes class variable nodes
                adjacencyMatrix = modifyClassSubgraph(adjacencyMatrix, idxNodesToChange, numClassVariableNodes,
                        numFeatureVariableNodes);
                // Bridge and feature subgraph
                adjacencyMatrix = modifyBridgeAndFeatureSubgraphs(adjacencyMatrix, idxNodesToChange,
                        numClassVariableNodes, maxNumParents);
                // Set new structure
                multiCTBNC.setStructure(adjacencyMatrix);
                // Sample CPD changed nodes
                for (int idxNodeToChange : idxNodesToChange) {
                    if (multiCTBNC.getNodeByIndex(idxNodeToChange) instanceof CIMNode)
                        DataSampler.generateRandomCIM(multiCTBNC.getCTBN().getNodeByName(
                                multiCTBNC.getNodeByIndex(idxNodeToChange).getName()), minIntensity, maxIntensity);
                    else
                        DataSampler.generateRandomCPT(
                                multiCTBNC.getBN().getNodeByName(multiCTBNC.getNodeByIndex(idxNodeToChange).getName()),
                                forceExtremeProb);
                }
            }
            // Display the modified model highlighting the nodes whose dependencies changed
            displayModel(multiCTBNC, idxModel, idxNodesToChange, pathDataStream);
            System.out.println("Concept drift:");
            System.out.println(multiCTBNC);
            return multiCTBNC;
        }

        private static void sampleSequencesDataStream(int numSequencesDataStream, double durationSequences,
                                                      List<Entry<int[], MultiCTBNC<CPTNode, CIMNode>>> evolutionDataStream,
                                                      String pathDataStream) {
            // Iterate over the number of sequences that will be sampled
            for (int i = 0; i < numSequencesDataStream; i++) {
                // Define distribution from where the current sequence is sampled
                for (int idxDistributionsDataStream = 0; idxDistributionsDataStream < evolutionDataStream.size();
                     idxDistributionsDataStream++) {
                    int[] interval = evolutionDataStream.get(idxDistributionsDataStream).getKey();
                    if (i >= interval[0] && i <= interval[1]) {
                        MultiCTBNC<CPTNode, CIMNode> multiCTBNC = evolutionDataStream.get(
                                idxDistributionsDataStream).getValue();
                        Sequence sampledSequence = multiCTBNC.sample(durationSequences, percentageNoisyStates,
                                stdDeviationGaussianNoiseWaitingTime);
                        String nameFile = "Sequence" + i;
                        MultipleCSVWriter.write(sampledSequence, pathDataStream, nameFile);
                        break;
                    }
                }
            }
        }

        private static CPTNode generateClassVariable(String name, List<String> states) {
            CPTNode classVariableNode = new CPTNode(name, states, true);
            return classVariableNode;
        }

        private static List<Integer> getRandomParents(int idxNode, int numFeatureVariables, int maxNumParents) {
            Random random = new Random();
            int numParents = random.nextInt(maxNumParents);
            List<Integer> possibleParents = IntStream.rangeClosed(0, numFeatureVariables - 1).boxed().collect(
                    Collectors.toList());
            possibleParents.remove(idxNode);
            List<Integer> idxParents = Util.getRandomElements(possibleParents, numParents);
            return idxParents;
        }

        private static boolean[][] modifyClassSubgraph(boolean[][] adjacencyMatrix, List<Integer> idxNodesToChange,
                                                       int numClassVariableNodes, int numFeatureVariableNodes) {
            List<Integer> idxClassVariablesNodesToChange = idxNodesToChange.stream().filter(
                    idxNode -> idxNode >= adjacencyMatrix.length - numClassVariableNodes).collect(Collectors.toList());
            // Remove parents selected nodes
            for (int idxClassVariableNode = numFeatureVariableNodes; idxClassVariableNode < adjacencyMatrix.length;
                 idxClassVariableNode++) {
                for (int idxClassVariableNodeToChange : idxClassVariablesNodesToChange) {
                    adjacencyMatrix[idxClassVariableNode][idxClassVariableNodeToChange] = false;
                }
            }
            // Define new parents of the selected nodes (randomly add new arcs or remove existing ones)
            Random random = new Random();
            DAG dag = new DAG();
            for (int idxClassVariableNode = numFeatureVariableNodes; idxClassVariableNode < adjacencyMatrix.length;
                 idxClassVariableNode++) {
                for (int idxClassVariableNodeToChange : idxClassVariablesNodesToChange) {
                    if (idxClassVariableNode != idxClassVariableNodeToChange) {
                        boolean addEdge = random.nextBoolean();
                        adjacencyMatrix[idxClassVariableNode][idxClassVariableNodeToChange] = addEdge;
                        // Retrieve adjacency matrix of class subgraph to check that it is valid
                        boolean[][] adjacencyMatrixClassSubgraph = new boolean[numClassVariableNodes][numClassVariableNodes];
                        for (int idxClassAdj = 0, idxCompleteAdj = numFeatureVariableNodes; idxClassAdj < adjacencyMatrixClassSubgraph.length; idxClassAdj++, idxCompleteAdj++)
                            adjacencyMatrixClassSubgraph[idxClassAdj] = Arrays.copyOfRange(adjacencyMatrix[idxCompleteAdj], numFeatureVariableNodes, adjacencyMatrix.length);

                        if (addEdge && !dag.isStructureLegal(adjacencyMatrixClassSubgraph, null)) {
                            adjacencyMatrix[idxClassVariableNode][idxClassVariableNodeToChange] = false;
                        }
                    }
                }
            }
            return adjacencyMatrix;
        }
        
        private static boolean[][] modifyBridgeAndFeatureSubgraphs(boolean[][] adjacencyMatrix,
                                                                   List<Integer> idxNodesToChange,
                                                                   int numClassVariableNodes, int maxNumParents) {
            // Retrieve indexes feature variable nodes
            List<Integer> idxFeatureVariablesNodesToChange = idxNodesToChange.stream().filter(
                    idxNode -> idxNode < adjacencyMatrix.length - numClassVariableNodes).collect(Collectors.toList());
            for (int idxNodeToChange : idxFeatureVariablesNodesToChange) {
                // Get the new parents of the changed node
                List<Integer> idxParents = getRandomParents(idxNodeToChange, adjacencyMatrix.length, maxNumParents);
                for (int idxParent = 0; idxParent < adjacencyMatrix.length; idxParent++) {
                    if (idxParent != idxNodeToChange && idxParents.contains(idxParent)) {
                        // Set new parents
                        adjacencyMatrix[idxParent][idxNodeToChange] = true;
                    } else
                        // Remove previous parents
                        adjacencyMatrix[idxParent][idxNodeToChange] = false;
                }
            }
            return adjacencyMatrix;
        }
    }

}
