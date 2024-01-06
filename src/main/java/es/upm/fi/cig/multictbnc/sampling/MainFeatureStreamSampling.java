package es.upm.fi.cig.multictbnc.sampling;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.data.writer.MultipleCSVWriter;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class serves as the entry point for the feature stream sampling application. It contains methods for
 * generating feature streams with relevant and irrelevant feature variables.
 *
 * @author Carlos Villa Blanco
 */
public class MainFeatureStreamSampling {

    /**
     * Entry point of the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Application.launch(MainFeatureStreamSamplingFX.class);
    }

    /**
     * This class represents the JavaFX application for feature stream sampling.
     *
     * @author Carlos Villa Blanco
     */
    public static class MainFeatureStreamSamplingFX extends Application {
        private static final int numFeatureVariables = 125; // 25,50,75,100,125
        private static final int numFeatureInitialDataset = 0;
        private static final double percentageRelevantFeatureVariablesInFeatureStream = 0.2;
        private static final int numClassVariables = 4;
        // Number of feature variables, includes those received from the feature stream
        private static double percentageNoisyStates = 0.01;
        private static double stdDeviationGaussianNoiseWaitingTime = 0.05;
        private static int numSequencesFeatureStream = 5000;
        private static double durationSequences = 5;
        // Extreme probabilities (only for binary class variables)
        private static boolean forceExtremeProb = false;
        // Minimum and maximum values of the intensities
        private static int minIntensity = 1;
        private static int maxIntensity = 3;
        // Maximum number of feature variables that can be parents of others
        private static int MAXNUMPARENTS = 5;
        private static int numExperiments = 10;
        private static Logger logger = LogManager.getLogger(MainDataStreamSampling.class);
        private static String pathData = "datasets/synthetic/chapter8/feature_stream/" + numFeatureVariables + "_features";

        /**
         * Entry point for the JavaFX application responsible for the generation of feature streams.
         *
         * @param args command-line arguments
         */
        public static void main(String[] args) {
            Application.launch(args);
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            for (int idxExperiment = 1; idxExperiment <= numExperiments; idxExperiment++)
                generateExperiment(idxExperiment);
            Platform.exit();
        }

        private static void generateExperiment(int idxExperiment) throws IOException {
            logger.info("Generating feature stream for the experiment " + idxExperiment + "...");
            String destinationPathExperiment = Paths.get(pathData + "/experiment" + idxExperiment).toString();
            // Get number of relevant and irrelevant feature variables (including those in the initial dataset)
            int numRelevantFeatureVariables = (int) (numFeatureVariables * percentageRelevantFeatureVariablesInFeatureStream) + numFeatureInitialDataset;
            // Get indexes of feature nodes
            List<Integer> idxFeatureNodes = IntStream.rangeClosed(0, (numFeatureVariables + numFeatureInitialDataset) - 1).boxed().collect(
                    Collectors.toList());
            // Randomly define initial model
            MultiCTBNC<CPTNode, CIMNode> model = generateModelFeatureStream(forceExtremeProb,
                    minIntensity, maxIntensity, MAXNUMPARENTS);
            // Get indexes of feature variables that should be relevant for the classification task
            List<Integer> idxNewRelevantFeatureNodes = retrieveIdxRelevantFeatures(
                    numRelevantFeatureVariables, idxFeatureNodes, model);
            // Define some nodes of the model as irrelevant
            List<Integer> idxIrrelevantFeatureNodes = setIrrelevantFeaturesInModel(model, idxFeatureNodes, idxNewRelevantFeatureNodes);
            // Randomly set the parameters of the model
            setParametersModel(model);
            // Display the final model highlighting the irrelevant nodes
            displayModel(model, idxIrrelevantFeatureNodes, destinationPathExperiment);
            logger.info("Model feature stream experiment:");
            logger.info(model);
            // Get some relevant feature variables for the initial dataset and remove them from the feature nodes that will arrive from the feature stream
            List<Integer> idxFeatureNodesInitialDataset = Util.getRandomElements(idxNewRelevantFeatureNodes,
                    numFeatureInitialDataset);
            idxFeatureNodes.removeAll(idxFeatureNodesInitialDataset);
            logger.info("Number of feature nodes in the initial dataset: {}", idxFeatureNodesInitialDataset.size());
            logger.info("Number of relevant features that will be received from feature stream: {}", idxNewRelevantFeatureNodes.size());
            // Save names relevant and irrelevant feature variables
            FileWriter fileNamesRelevantIrrelevantFeatures = new FileWriter(destinationPathExperiment + "/relevancy_features.txt");
            fileNamesRelevantIrrelevantFeatures.write("Relevant features: ");
            for (int idxRelevantFeature : idxNewRelevantFeatureNodes)
                fileNamesRelevantIrrelevantFeatures.write(model.getNodeByIndex(idxRelevantFeature).getName() + ", ");
            fileNamesRelevantIrrelevantFeatures.write("Irrelevant features: ");
            for (int idxIrrelevantFeature : idxIrrelevantFeatureNodes)
                fileNamesRelevantIrrelevantFeatures.write(model.getNodeByIndex(idxIrrelevantFeature).getName() + ", ");
            fileNamesRelevantIrrelevantFeatures.close();
            generateFeatureStream(numSequencesFeatureStream, durationSequences, destinationPathExperiment,
                    model, idxFeatureNodes);
            logger.info("Feature stream generated!");
        }

        private static MultiCTBNC<CPTNode, CIMNode> generateModelFeatureStream(boolean forceExtremeProb,
                                                                               int minIntensity, int maxIntensity,
                                                                               int maxNumParents) {
            // Define class variables. Specify their names and sample spaces.
            BN<CPTNode> CS = getBayesianNetworkInitialClassSubgraph();
            // Define feature variables. Specify their names and sample spaces.
            List<CIMNode> featureNodes = new ArrayList<CIMNode>();
            for (int idxFeatureVariable = 1; idxFeatureVariable <= numFeatureVariables + numFeatureInitialDataset; idxFeatureVariable++) {
                String nameFeatureNode = "X" + idxFeatureVariable;
                List<String> statesFeatureNode = List.of(nameFeatureNode + "_A", nameFeatureNode + "_B",
                        nameFeatureNode + "_C", nameFeatureNode + "_D");
                CIMNode featureNode = generateFeatureVariable(nameFeatureNode, statesFeatureNode);
                featureNodes.add(featureNode);
            }
            CTBN<CIMNode> FBS = new CTBN<CIMNode>(featureNodes, CS);
            // Randomly define the structure of the initial Multi-CTBNC
            int numClassVariables = CS.getNumNodes();
            int numFeatureVariables = FBS.getNumNodes();
            boolean[][] adjacencyMatrix = new boolean[numClassVariables + numFeatureVariables][numClassVariables +
                    numFeatureVariables];
            // Generate random class subgraph
            adjacencyMatrix = generateRandomClassSubgraph(numFeatureVariables, adjacencyMatrix);
            // Generate random bridge subgraph
            adjacencyMatrix = generateRandomBridgeSubgraph(numFeatureVariables, adjacencyMatrix);
            // Generate random feature subgraph
            adjacencyMatrix = generateRandomFeatureSubgraph(maxNumParents, numFeatureVariables, adjacencyMatrix);
            // Definition of the initial Multi-CTBNC
            MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<CPTNode, CIMNode>(CS, FBS);
            multiCTBNC.setStructure(adjacencyMatrix);
            // Definition of the CPTs and CIMs
            DataSampler.generateRandomCPTs(multiCTBNC.getBN(), forceExtremeProb);
            DataSampler.generateRandomCIMs(multiCTBNC.getCTBN(), minIntensity, maxIntensity);
            return multiCTBNC;
        }

        private static List<Integer> retrieveIdxRelevantFeatures(int numNewRelevantFeatures, List<Integer> idxFeatureNodes
                , MultiCTBNC<CPTNode, CIMNode> model) {
            logger.info("Defining relevant feature variables...");
            List<Integer> idxNewRelevantFeatureNodes = new ArrayList<Integer>();
            Random random = new Random();
            // Shuffle the feature nodes indexes before defining which ones are relevant
            List<Integer> shuffledIdxFeatureNodes = new ArrayList<>(idxFeatureNodes);
            Collections.shuffle(shuffledIdxFeatureNodes);
            for (int idxFeatureNode : shuffledIdxFeatureNodes) {
                if (idxNewRelevantFeatureNodes.size() >= numNewRelevantFeatures) {
                    logger.info("Number of relevant feature variables that were retrieved: {}",
                            idxNewRelevantFeatureNodes.size());
                    break;
                }
                if (!idxNewRelevantFeatureNodes.contains(idxFeatureNode)) {
                    logger.info("Checking: {}", idxFeatureNode);
                    // Check if feature node is in Markov blanket of a class variable
                    Node nodeFeature = model.getNodeByIndex(idxFeatureNode);
                    if (isFeatureChildOfClassVariable(nodeFeature, model)) {
                        // Node is a child of a class variable
                        logger.info("Feature variable with index {} is relevant", idxFeatureNode);
                        idxNewRelevantFeatureNodes.add(idxFeatureNode);
                        // Randomly decide to include one feature node that is parent of the previous relevant feature variable (if it was not already included)
                        boolean isSpouseIncluded = (random.nextInt(2) == 0) ? true : false;
                        List<Integer> parentFeatureNodes = nodeFeature.getParents().stream().filter(node -> !node.isClassVariable()).map(node -> model.getIndexOfNode(node)).collect(Collectors.toList());
                        if (!parentFeatureNodes.isEmpty() && !idxNewRelevantFeatureNodes.contains(parentFeatureNodes.get(0)) && isSpouseIncluded) {
                            idxNewRelevantFeatureNodes.add(parentFeatureNodes.get(0));
                        }
                    }
                }
            }
            return idxNewRelevantFeatureNodes;
        }

        private static List<Integer> setIrrelevantFeaturesInModel(MultiCTBNC<CPTNode, CIMNode> model, List<Integer> idxFeatureNodes, List<Integer> idxRelevantFeatureNodes) {
            logger.info("Defining irrelevant feature variables...");
            List<Integer> idxIrrelevantFeatureNodes = new ArrayList<>(idxFeatureNodes);
            // The feature variables defined as relevant cannot be made irrelevant
            for (Integer idxRelevantFeatureNode : idxRelevantFeatureNodes)
                idxIrrelevantFeatureNodes.remove(idxRelevantFeatureNode);
            logger.info("Number of irrelevant features in the feature stream: {}", idxIrrelevantFeatureNodes.size());
            boolean[][] adjacencyMatrix = getAdjacencyMatrixWithIrrelevantFeatures(idxIrrelevantFeatureNodes, model);
            model.setStructure(adjacencyMatrix);
            return idxIrrelevantFeatureNodes;
        }

        /**
         * Sets the parameters of the given Multi-CTBNC. Random values for the parameters are generated.
         *
         * @param model Multi-CTBNC for which parameters will be set
         */
        public static void setParametersModel(MultiCTBNC<CPTNode, CIMNode> model) {
            // Definition of the CPTs and CIMs
            DataSampler.generateRandomCPTs(model.getBN(), forceExtremeProb);
            DataSampler.generateRandomCIMs(model.getCTBN(), minIntensity, maxIntensity);
        }

        private static void displayModel(MultiCTBNC<CPTNode, CIMNode> model,
                                         List<Integer> idxIrrelevantFeatureNodes, String destinationPathExperiment) {
            String titleWindow = "Underlying model [Irrelevant nodes in red]";
            model.saveGraph(destinationPathExperiment, titleWindow, idxIrrelevantFeatureNodes);
        }

        private static void generateFeatureStream(int numSequences, double durationSequences, String pathFeatureStream,
                                                  MultiCTBNC<CPTNode, CIMNode> model,
                                                  List<Integer> idxFeatureNodesFeatureStream) {
            logger.info("Generating feature stream with {} sequences in {}", numSequences, pathFeatureStream);
            logger.info("Number of feature variables that will be received from the feature stream: {}",
                    idxFeatureNodesFeatureStream.size());
            // Shuffle indexes new feature nodes
            Collections.shuffle(idxFeatureNodesFeatureStream);
            // Create dataset with all feature nodes
            Dataset dataset = generateDatasetFeatureStream(model, numSequences, durationSequences);
            // Generate feature stream
            List<String> nameNewFeatureNodes = model.getNamesNodesByIndex(idxFeatureNodesFeatureStream);
            // For each new feature, create and save a dataset
            for (int idxNewFeatureNode = 0; idxNewFeatureNode < idxFeatureNodesFeatureStream.size(); idxNewFeatureNode++) {
                String nameNewFeature = nameNewFeatureNodes.get(idxNewFeatureNode);
                System.out.println("Writing dataset with feature variable " + nameNewFeature);
                writeDataset(dataset, List.of(nameNewFeature),
                        pathFeatureStream + "/feature_stream/dataset" + (idxNewFeatureNode + 1));
            }
            // Generate initial dataset with some relevant feature variables and the class variables
            dataset.removeFeatureVariables(nameNewFeatureNodes);
            writeDataset(dataset, pathFeatureStream + "/initial_dataset");
            // Generate test dataset with all variables
            Dataset testDataset = generateDatasetFeatureStream(model, 1000, durationSequences);
            writeDataset(testDataset, pathFeatureStream + "/test_dataset");
        }

        private static BN<CPTNode> getBayesianNetworkInitialClassSubgraph() {
            List<CPTNode> classNodes = new ArrayList<>();
            for (int idxClassVariable = 1; idxClassVariable <= numClassVariables; idxClassVariable++) {
                String nameClassNode = "C" + idxClassVariable;
                List<String> statesClassNode = List.of(nameClassNode + "_A", nameClassNode + "_B",
                        nameClassNode + "_C");
                CPTNode classNode = generateClassVariable(nameClassNode, statesClassNode);
                classNodes.add(classNode);
            }
            logger.info("Generating data stream for multi-dimensional classification problem");
            return new BN<CPTNode>(classNodes);
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

        private static boolean[][] generateRandomBridgeSubgraph(int idxFirstClassVariable,
                                                                boolean[][] adjacencyMatrix) {
            Random random = new Random();
            int idxLastClassVariable = adjacencyMatrix.length;
            for (int i = idxFirstClassVariable; i < idxLastClassVariable; i++)
                for (int j = 0; j < idxFirstClassVariable; j++)
                    adjacencyMatrix[i][j] = random.nextFloat() < 0.5;
            return adjacencyMatrix;
        }

        private static boolean[][] generateRandomFeatureSubgraph(int maxNumParents, int numFeatureVariables,
                                                                 boolean[][] adjacencyMatrix) {
            for (int idxFeature = 0; idxFeature < numFeatureVariables; idxFeature++) {
                List<Integer> idxParents = getRandomParents(idxFeature, numFeatureVariables, maxNumParents);
                for (int idxParent : idxParents)
                    adjacencyMatrix[idxParent][idxFeature] = true;
            }
            return adjacencyMatrix;
        }

        private static boolean isFeatureChildOfClassVariable(Node nodeFeature, MultiCTBNC<CPTNode, CIMNode> model) {
            for (Node nodeClassVariable : model.getNodesClassVariables()) {
                if (nodeClassVariable.getChildren().contains(nodeFeature)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean[][] getAdjacencyMatrixWithIrrelevantFeatures(
                List<Integer> idxIrrelevantFeatureNodes, MultiCTBNC<CPTNode, CIMNode> model) {
            boolean[][] adjacencyMatrix = model.getAdjacencyMatrix();
            // Irrelevant feature nodes cannot have class variables as parents
            for (int idxIrrelevantNode : idxIrrelevantFeatureNodes) {
                for (int idxClassNode = model.getNumFeatureVariables(); idxClassNode < adjacencyMatrix.length; idxClassNode++) {
                    adjacencyMatrix[idxClassNode][idxIrrelevantNode] = false;
                }
            }
            // Irrelevant feature nodes cannot be parents of other feature nodes that have class nodes as parents
            for (int idxIrrelevantNode : idxIrrelevantFeatureNodes) {
                for (int idxFeatureNode = 0; idxFeatureNode < model.getNumFeatureVariables(); idxFeatureNode++) {
                    Node nodeFeature = model.getNodeByIndex(idxFeatureNode);
                    if (isFeatureChildOfClassVariable(nodeFeature, model)) {
                        adjacencyMatrix[idxIrrelevantNode][idxFeatureNode] = false;
                    }
                }
            }
            return adjacencyMatrix;
        }

        private static Dataset generateDatasetFeatureStream(MultiCTBNC<CPTNode, CIMNode> multiCTBNC,
                                                            int numSequencesDataset, double durationSequences) {
            // Sample sequences from the Multi-CTBNC
            List<Sequence> trainSequences = new ArrayList<>();
            for (int i = 0; i < numSequencesDataset; i++)
                trainSequences.add(multiCTBNC.sample(durationSequences, percentageNoisyStates,
                        stdDeviationGaussianNoiseWaitingTime));
            // Create a dataset with the generated sequences
            Dataset trainDataset = new Dataset(trainSequences);
            return trainDataset;
        }

        private static void writeDataset(Dataset dataset, List<String> featuresToInclude, String pathDataset) {
            MultipleCSVWriter.write(dataset, featuresToInclude, pathDataset);
        }

        private static void writeDataset(Dataset dataset, String pathDataset) {
            MultipleCSVWriter.write(dataset, pathDataset);
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

    }

}