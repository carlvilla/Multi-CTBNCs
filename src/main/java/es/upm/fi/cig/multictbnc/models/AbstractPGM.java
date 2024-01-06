package es.upm.fi.cig.multictbnc.models;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeFactory;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains common attributes and methods for PGM.
 *
 * @param <NodeType> type of nodes used by the model, e.g., nodes with conditional probability tables ({@code CPTNode})
 *                   or conditional intensity matrices (@code CIMNode)
 * @author Carlos Villa Blanco
 */
public abstract class AbstractPGM<NodeType extends Node> implements PGM<NodeType> {
    private static final Logger logger = LogManager.getLogger(AbstractPGM.class);
    Dataset dataset;
    List<String> nameVariables;
    List<NodeType> nodes;
    NodeIndexer<NodeType> nodeIndexer;
    NodeFactory<NodeType> nodeFactory;
    Class<NodeType> nodeClass;
    ParameterLearningAlgorithm parameterLearningAlg;
    StructureLearningAlgorithm structureLearningAlg;
    StructureConstraints structureConstraints;
    // CSS used to display the graph. A variable is used instead of a CSS file to avoid errors while loading the latter
    String cssStyle = "node {fill-color:#FFFFFF; stroke-mode:plain;size:30px;text-size:15px;} node" +
            ".highlighted{fill-color: red;}";

    /**
     * Common initialisation for PGMs.
     *
     * @param nodes nodes of the PGM
     */
    public AbstractPGM(List<NodeType> nodes) {
        addNodes(nodes, true);
        this.nameVariables = new ArrayList<>();
        for (Node node : nodes)
            this.nameVariables.add(node.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addNodes(List<NodeType> nodes, boolean createNodes) {
        if (nodes != null && nodes.size() > 0) {
            if (this.nodes == null || this.nodes.isEmpty()) {
                this.nodeClass = (Class<NodeType>) nodes.get(0).getClass();
                this.nodes = new ArrayList<>();
            }
            if (createNodes)
                this.nodes.addAll(getNodeFactory().createNodes(nodes));
            else
                this.nodes.addAll(nodes);
            this.nodeIndexer = new NodeIndexer<>(getNodes());
        }
    }

    @Override
    public boolean areParametersEstimated() {
        for (Node node : getNodes()) {
            if (!node.areParametersEstimated())
                return false;
        }
        return true;
    }

    @Override
    public void computeSufficientStatistics(List<Integer> idxNodes) {
        if (this.dataset != null) {
            for (int idx : idxNodes) {
                Node node = this.nodeIndexer.getNodeByIndex(idx);
                this.parameterLearningAlg.setSufficientStatistics(node, this.dataset);
            }
        }
    }

    @Override
    public void display() {
        display(getType());
    }

    @Override
    public void display(String title) {
        display(title, null);
    }

    @Override
    public void display(String title, List<Integer> nodesToHighlight) {
        if (getNodes() != null && !getNodes().isEmpty()) {
            Stage stage = createStage(title, nodesToHighlight);
            stage.show();
        } else {
            logger.warn("The model cannot be displayed as it does not contain nodes");
        }
    }

    /**
     * Returns the adjacency matrix of the PGM by analysing the parents of each node.
     *
     * @return two-dimensional {@code boolean} array representing the adjacency matrix
     */
    @Override
    public boolean[][] getAdjacencyMatrix() {
        int numNodes = getNumNodes();
        boolean[][] adjacencyMatrix = new boolean[numNodes][numNodes];
        for (Node node : getNodes()) {
            List<Node> parents = node.getParents();
            for (Node parentNode : parents) {
                int idxNode = this.nodeIndexer.getIndexNodeByName(node.getName());
                int indexParentNode = this.nodeIndexer.getIndexNodeByName(parentNode.getName());
                adjacencyMatrix[indexParentNode][idxNode] = true;
            }
        }
        return adjacencyMatrix;
    }

    @Override
    public List<Integer> getIndexNodes() {
        return this.nodeIndexer.getIndexNodes();
    }

    @Override
    public List<String> getNamesNodesByIndex(List<Integer> indexes) {
        return indexes.stream().map(index -> getNodeByIndex(index)).map(node -> node.getName()).collect(
                Collectors.toList());
    }

    @Override
    public NodeType getNodeByIndex(int index) {
        return this.nodeIndexer.getNodeByIndex(index);
    }

    @Override
    public int getIndexOfNode(Node node) {
        return this.nodeIndexer.getIndexNodeByName(node.getName());
    }

    @Override
    public NodeType getNodeByName(String nameVariable) {
        return getNodes().stream().filter(node -> node.getName().equals(nameVariable)).findFirst().orElse(null);
    }

    /**
     * Returns the node indexer of the model.
     *
     * @return node indexer
     */
    @Override
    public NodeIndexer<NodeType> getNodeIndexer() {
        return this.nodeIndexer;
    }

    @Override
    public List<NodeType> getNodes() {
        return this.nodes;
    }

    @Override
    public int getNumNodes() {
        return getNodes().size();
    }

    @Override
    public ParameterLearningAlgorithm getParameterLearningAlg() {
        return this.parameterLearningAlg;
    }

    @Override
    public void initialiseModel(Dataset dataset) {
        // Save the dataset used to learn the model
        this.dataset = dataset;
        // Clear the entire model
        removeAllNodes();
        // Create nodes using the dataset
        List<NodeType> nodes = new ArrayList<>();
        for (String nameVariable : this.nameVariables) {
            // Use node factory to create nodes of the specified type
            NodeType node = getNodeFactory().createNode(nameVariable, dataset);
            nodes.add(node);
        }
        addNodes(nodes, false);
    }

    /**
     * Determines if the structure is legal.
     *
     * @param adjacencyMatrix adjacency matrix
     * @return true if the structure is valid, false otherwise
     */
    @Override
    public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
        return this.structureConstraints.isStructureLegal(adjacencyMatrix, getNodeIndexer());
    }

    @Override
    public void learn() throws ErroneousValueException {
        if (this.dataset != null) {
            // Depending on the class of model to learn, there could be a unique structure
            // (naive Bayes or empty graph) or the initial one has to be optimised
            if (this.structureConstraints.uniqueStructure()) {
                // One possible structure. It is set in the PGM, and the parameters learnt
                this.structureConstraints.initialiseStructure(this);
                learnParameters();
            } else
                // Learn structure and parameters with the specified algorithms
                this.structureLearningAlg.learn(this);
        } else {
            logger.warn("Training dataset was not established");
        }
    }

    @Override
    public long learn(Dataset dataset) throws ErroneousValueException {
        Instant start = Instant.now();
        initialiseModel(dataset);
        learn();
        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    @Override
    public void learn(Dataset dataset, int idxNode) throws ErroneousValueException {
        // Save the dataset used to learn the model
        this.dataset = dataset;
        // Learn structure and parameters with the specified algorithms
        this.structureLearningAlg.learn(this, idxNode);
    }

    @Override
    public void learn(Dataset dataset, List<Integer> idxNodes) throws ErroneousValueException {
        if (idxNodes != null && !idxNodes.isEmpty()) {
            // Save the dataset used to learn the model
            this.dataset = dataset;
            // Learn structure and parameters with the specified algorithms
            this.structureLearningAlg.learn(this, idxNodes);
        }
    }

    @Override
    public void learnParameters() {
        learnParameters(this.dataset);
    }

    @Override
    public void learnParameters(Dataset dataset) {
        if (getNodes() != null && !getNodes().isEmpty() && dataset != null)
            this.parameterLearningAlg.learn(getNodes(), dataset);
    }

    @Override
    public void learnParameters(Dataset dataset, int idxNode) {
        if (getNodes() != null && !getNodes().isEmpty() && dataset != null)
            this.parameterLearningAlg.learn(getNodeByIndex(idxNode), dataset);
    }

    @Override
    public void learnParameters(List<Integer> idxNodes) {
        if (this.dataset != null) {
            List<NodeType> nodes = idxNodes.stream().map(idxNode -> this.nodeIndexer.getNodeByIndex(idxNode)).collect(
                    Collectors.toList());
            this.parameterLearningAlg.learn(nodes, this.dataset);
        }
    }

    @Override
    public void learnParameters(Integer idxNode) {
        if (this.dataset != null) {
            Node node = this.nodeIndexer.getNodeByIndex(idxNode);
            this.parameterLearningAlg.learn(node, this.dataset);
        }
    }

    @Override
    public void learnParameters(List<Integer> idxNodes, ParameterLearningAlgorithm parameterLearningAlg) {
        if (this.dataset != null) {
            List<NodeType> nodes = idxNodes.stream().map(idxNode -> this.nodeIndexer.getNodeByIndex(idxNode)).collect(
                    Collectors.toList());
            parameterLearningAlg.learn(nodes, this.dataset);
        }
    }

    @Override
    public void removeAllEdges() {
        for (Node node : this.nodes)
            node.clearParentAndChildrenSets();
    }

    @Override
    public void removeAllNodes() {
        this.nodes = new ArrayList<>();
        this.nodeIndexer = null;
    }

    @Override
    public void saveGraph(String destinationPath, String filename, List<Integer> idxNodesToHighlight) {
        if (getNodes() != null && !getNodes().isEmpty()) {
            Stage stage = createStage(filename, idxNodesToHighlight);
            // Save stage
            WritableImage snapshot = stage.getScene().snapshot(null);
            File f = new File(destinationPath);
            f.mkdirs();
            String pathImage = Paths.get(destinationPath, filename + ".png").toString();
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(pathImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.warn("The model graph cannot be saved as it does not contain nodes");
        }
    }

    @Override
    public void setStructure(boolean[][] adjacencyMatrix) {
        removeAllEdges();
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            Node node = this.nodeIndexer.getNodeByIndex(i);
            for (int j = 0; j < adjacencyMatrix.length; j++)
                if (adjacencyMatrix[i][j]) {
                    Node childNode = this.nodeIndexer.getNodeByIndex(j);
                    node.setChild(childNode);
                }
        }
    }

    @Override
    public void setStructure(int idxNode, boolean[][] adjacencyMatrix) {
        // TODO set entire structure for now
        setStructure(adjacencyMatrix);
    }

    @Override
    public void setStructure(List<Integer> idxNodes, boolean[][] adjacencyMatrix) {
        // TODO set entire structure for now
        setStructure(adjacencyMatrix);
    }

    @Override
    public void setStructureModifiedNodes(boolean[][] newAdjacencyMatrix) {
        if (areParametersEstimated()) {
            boolean[][] currentAdjacencyMatrix = getAdjacencyMatrix();
            // Get nodes whose parents changed
            List<Integer> modifiedNodes = getModifiedNodes(currentAdjacencyMatrix, newAdjacencyMatrix);
            // Modify the structure of the model
            for (int idx : modifiedNodes) {
                Node node = this.nodeIndexer.getNodeByIndex(idx);
                // Current edges of selected nodes are removed
                node.removeParents();
                // Establish parents of selected nodes
                for (int i = 0; i < newAdjacencyMatrix.length; i++) {
                    if (newAdjacencyMatrix[i][idx])
                        node.setParent(this.nodeIndexer.getNodeByIndex(i));
                }
            }
            // Learn the parameters of the modified nodes
            learnParameters(modifiedNodes);
        } else {
            // All nodes must have their parameters estimated
            setStructure(newAdjacencyMatrix);
            learnParameters();
        }
    }

    /**
     * Creates a {@code Stage} used to display a PGM.
     *
     * @return a {@code Stage}
     */
    private Stage createStage(String title, List<Integer> nodesToHighlight) {
        Graph graph = getGraphUI();
        // Use red colour to highlight the specified nodes
        if (nodesToHighlight != null) {
            for (int idxNode : nodesToHighlight)
                graph.getNode(idxNode).setAttribute("ui.class", "highlighted");
        }
        // Define viewer
        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        FxViewPanel panel = (FxViewPanel) viewer.addDefaultView(false, new FxGraphRenderer());
        // Create a stage and scene to visualise the graph
        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(panel);
        stage.setScene(scene);
        return stage;
    }

    /**
     * Returns a {@code Graph} used to display a PGM.
     *
     * @return a {@code Graph}
     */
    private Graph getGraphUI() {
        // Create GraphStream graph
        Graph graph = new SingleGraph("PGM");
        addNodes(graph, getNodes());
        addEdges(graph, getNodes());
        graph.setAttribute("ui.stylesheet", cssStyle);
        return graph;
    }

    /**
     * Adds nodes to a graphstream graph.
     *
     * @param graph graphstream graph
     * @param nodes list of nodes to add to the graph
     */
    private void addNodes(Graph graph, List<NodeType> nodes) {
        // Variables used to determine the position of the node in the graph
        int numClassVariables = 0;
        int numFeatureVariables = 0;
        for (int i = 0; i < nodes.size(); i++) {
            // Retrieve node from the model
            Node node = nodes.get(i);
            String nameNode = node.getName();
            // Create a node for the graph
            org.graphstream.graph.Node nodeGraph = graph.addNode(nameNode);
            nodeGraph.setAttribute("ui.label", nameNode);
            // Display node differently depending on if it is a class variable or not
            if (node.isClassVariable()) {
                nodeGraph.setAttribute("y", (++numClassVariables) % 2 + 1);
                nodeGraph.setAttribute("x", numClassVariables);
            } else {
                nodeGraph.setAttribute("y", -(((++numFeatureVariables) % 10) * 3 + 2));
                nodeGraph.setAttribute("x", numFeatureVariables);
            }
        }
    }

    /**
     * Adds edges to a graphstream graph.
     *
     * @param graph graphstream graph
     * @param nodes nodes whose edges are added to the graph
     */
    private void addEdges(Graph graph, List<NodeType> nodes) {
        for (Node node : nodes) {
            String nameNode = node.getName();
            for (Node parentNode : node.getParents()) {
                String nameParent = parentNode.getName();
                graph.addEdge(nameParent + nameNode, nameParent, nameNode, true);
            }
        }
    }

    /**
     * Returns a {@code NodeFactory} for the nodes of the PGM.
     *
     * @return a {@code NodeFactory}
     */
    protected NodeFactory<NodeType> getNodeFactory() {
        if (this.nodeFactory == null)
            this.nodeFactory = NodeFactory.createFactory(getNodeClass());
        return this.nodeFactory;
    }

    /**
     * Returns the type of the nodes.
     *
     * @return type of the nodes
     */
    @SuppressWarnings("unchecked")
    public Class<NodeType> getNodeClass() {
        if (this.nodeClass != null)
            return this.nodeClass;
        return (Class<NodeType>) getNodes().get(0).getClass();
    }

    /**
     * Common initialisation for PGMs. The provided dataset is used to learn the model. References to the provided
     * nodes
     * are stored.
     *
     * @param nodes   nodes of the PGM
     * @param dataset dataset used to learn the model
     */
    public AbstractPGM(List<NodeType> nodes, Dataset dataset) {
        addNodes(nodes, true);
        this.nameVariables = new ArrayList<>();
        for (Node node : nodes)
            this.nameVariables.add(node.getName());
        this.dataset = dataset;
    }

    /**
     * Default constructor.
     */
    public AbstractPGM() {
    }

    /**
     * Returns the dataset used to learn the PGM.
     *
     * @return dataset
     */
    public Dataset getDataset() {
        return this.dataset;
    }

    /**
     * Set the dataset used to learn the PGM.
     *
     * @param dataset dataset
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns the hyperparameters of the model the user sets.
     *
     * @return {@code a Map} with the hyperparameters
     */
    public Map<String, String> getHyperparameters() {
        return Map.of();
    }

    /**
     * Returns the names of the variables of the PGM.
     *
     * @return names of the variables
     */
    public List<String> getNameVariables() {
        return this.nameVariables;
    }
    
    /**
     * Set the name of the variables in the PGM.
     *
     * @param nameVariables names of the variables
     */
    public void setNameVariables(List<String> nameVariables) {
        this.nameVariables = new ArrayList<>(nameVariables);
    }

    /**
     * Returns the constraints that the PGM needs to meet.
     *
     * @return structure constraints
     */
    public StructureConstraints getStructureConstraints() {
        return this.structureConstraints;
    }

    /**
     * Establishes the constraints that the PGM needs to meet.
     *
     * @param structureConstraints structure constraints to take into account during the learning of the model
     */
    public void setStructureConstraints(StructureConstraints structureConstraints) {
        this.structureConstraints = structureConstraints;
    }

    /**
     * Returns the algorithm used to learn the structure of the PGM.
     *
     * @return structure learning algorithm
     */
    public StructureLearningAlgorithm getStructureLearningAlg() {
        return this.structureLearningAlg;
    }

    /**
     * Establishes the algorithm that will be used to learn the parameters of the PGM.
     *
     * @param parameterLearningAlg parameter learning algorithm
     */
    public void setParameterLearningAlgorithm(ParameterLearningAlgorithm parameterLearningAlg) {
        this.parameterLearningAlg = parameterLearningAlg;
    }

    /**
     * Establishes the algorithm that will be used to learn the structure of the PGM.
     *
     * @param structureLearningAlg structure learning algorithm
     */
    public void setStructureLearningAlgorithm(StructureLearningAlgorithm structureLearningAlg) {
        this.structureLearningAlg = structureLearningAlg;
    }

    /**
     * Returns those nodes with different parents in the provided adjacency matrices.
     *
     * @param adjacencyMatrix1 an adjacency matrix
     * @param adjacencyMatrix2 an adjacency matrix
     * @return nodes with different parents in the provided adjacency matrices
     */
    private List<Integer> getModifiedNodes(boolean[][] adjacencyMatrix1, boolean[][] adjacencyMatrix2) {
        List<Integer> modifiedNodes = new ArrayList<>();
        for (int j = 0; j < getNumNodes(); j++) {
            for (int i = 0; i < getNumNodes(); i++) {
                if (adjacencyMatrix1[j][i] != adjacencyMatrix2[j][i])
                    modifiedNodes.add(i);
            }
        }
        return modifiedNodes;
    }

}