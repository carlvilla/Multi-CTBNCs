package es.upm.fi.cig.multictbnc.models;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;

import java.util.List;

/**
 * Defines the methods of a probabilistic graphical model (PGM)
 *
 * @param <NodeType> type of the nodes of the PGM
 * @author Carlos Villa Blanco
 */
public interface PGM<NodeType extends Node> {

    /**
     * Adds the provided nodes to the PGM.
     *
     * @param nodes       nodes to add
     * @param createNodes {@code true} to create new nodes from the provided ones, {@code false} otherwise (any change
     *                    to the provided nodes will affect this model).
     */
    void addNodes(List<NodeType> nodes, boolean createNodes);

    /**
     * Returns true if all the parameters were estimated.
     *
     * @return true if all the parameters were estimated
     */
    boolean areParametersEstimated();

    /**
     * Computes the sufficient statistics for the nodes whose indexes are specified.
     *
     * @param idxNodes node indexes
     */
    void computeSufficientStatistics(List<Integer> idxNodes);

    /**
     * Displays the probabilistic graphical model.
     */
    void display();

    /**
     * Displays the probabilistic graphical model.
     *
     * @param windowTitle title of the window where the model is displayed
     */
    void display(String windowTitle);

    /**
     * Displays the probabilistic graphical model.
     *
     * @param title            title of the window where the model is displayed
     * @param nodesToHighlight indexes of the nodes to be highlighted in red
     */
    void display(String title, List<Integer> nodesToHighlight);

    /**
     * Returns the adjacency matrix.
     *
     * @return two-dimensional {@code boolean} array representing the adjacency matrix
     */
    boolean[][] getAdjacencyMatrix();

    /**
     * Returns the indexes of the nodes.
     *
     * @return node indexes
     */
    List<Integer> getIndexNodes();

    /**
     * Returns a {@code String} that identifies the model.
     *
     * @return {@code String} that identifies the model
     */
    String getModelIdentifier();

    /**
     * Return the names of the nodes whose indexes are given.
     *
     * @param idxNodes node indexes
     * @return node names
     */
    List<String> getNamesNodesByIndex(List<Integer> idxNodes);

    /**
     * Obtains the node (feature or class variable) with a certain index.
     *
     * @param idxNode node index
     * @return node with the specified index
     */
    NodeType getNodeByIndex(int idxNode);

    /**
     * Returns the index of the provided node.
     *
     * @param node node for which the index is needed
     * @return index of the node
     */
    int getIndexOfNode(Node node);

    /**
     * Returns the node whose variable name is given.
     *
     * @param nameVariable name of the variable
     * @return requested node
     */
    NodeType getNodeByName(String nameVariable);

    /**
     * Returns the node indexer used by the PGM.
     *
     * @return node indexer
     */
    NodeIndexer<NodeType> getNodeIndexer();

    /**
     * Returns all the nodes in the model.
     *
     * @return list of nodes
     */
    List<NodeType> getNodes();

    /**
     * Returns the number of nodes.
     *
     * @return number of nodes
     */
    int getNumNodes();

    /**
     * Returns the algorithm that is used to learn the parameters of the PGM.
     *
     * @return parameter learning algorithm
     */
    ParameterLearningAlgorithm getParameterLearningAlg();

    /**
     * Provides the type of PGM.
     *
     * @return string describing the type of PGM
     */
    String getType();

    /**
     * Sets the dataset that will be used to estimate the structure and parameters of the model and creates its nodes.
     *
     * @param dataset dataset used to learn the model
     */
    void initialiseModel(Dataset dataset);

    /**
     * Checks if a structure is legal for the PGM.
     *
     * @param adjacencyMatrix two-dimensional {@code boolean} array representing the adjacency matrix to analyse
     * @return true if the structure is legal, false otherwise
     */
    boolean isStructureLegal(boolean[][] adjacencyMatrix);

    /**
     * Learns the structure and parameters of the model.
     *
     * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
     */
    void learn() throws ErroneousValueException;

    /**
     * Learns the structure and parameters of the model from a given dataset.
     *
     * @param dataset dataset used to learn the model
     * @return time to learn the model
     * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
     */
    long learn(Dataset dataset) throws ErroneousValueException;

    /**
     * Learns the parameters and parent set of a model's node from a given dataset.
     *
     * @param dataset dataset used to learn the parameters and parent set of the node
     * @param idxNode node index
     * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
     */
    void learn(Dataset dataset, int idxNode) throws ErroneousValueException;

    /**
     * Learns the parameters and parent set of some nodes of the model from a given dataset.
     *
     * @param dataset  dataset used to learn the parameters and parent set of the nodes
     * @param idxNodes node indexes
     * @throws ErroneousValueException if a provided parameter is erroneous for the
     *                                 requested task
     */
    void learn(Dataset dataset, List<Integer> idxNodes) throws ErroneousValueException;

    /**
     * Learns the parameters of the PGM.
     */
    void learnParameters();

    /**
     * Learns the parameters of the PGM using the provided dataset.
     *
     * @param dataset dataset used to learn the parameters
     */
    void learnParameters(Dataset dataset);

    /**
     * Learns the parameters of a certain node of the PGM using the provided dataset.
     *
     * @param dataset dataset used to learn the parameters
     * @param idxNode node index
     */
    void learnParameters(Dataset dataset, int idxNode);

    /**
     * Learns the parameters of the nodes whose indexes are specified.
     *
     * @param idxNodes indexes of the nodes whose parameters should be learnt
     */
    void learnParameters(List<Integer> idxNodes);

    /**
     * Learns the parameters of the node whose index is specified.
     *
     * @param idxNode index of the node whose parameters should be learnt
     */
    void learnParameters(Integer idxNode);

    /**
     * Learns the parameters of the nodes whose indexes are specified using a provider parameter learning algorithm.
     *
     * @param idxNodes             indexes of the nodes whose parameters should be learnt
     * @param parameterLearningAlg a {@code ParameterLearningAlgorithm}
     */
    void learnParameters(List<Integer> idxNodes, ParameterLearningAlgorithm parameterLearningAlg);

    /**
     * Remove all edges between the nodes of the PGM.
     */
    void removeAllEdges();

    /**
     * Removes all the nodes from the PGM.
     */
    void removeAllNodes();

    /**
     * Saves the PGM graph to a file.
     *
     * @param filePath            destination path of the file
     * @param filename            filename
     * @param idxNodesToHighlight indexes of the nodes to highlight
     */
    void saveGraph(String filePath, String filename, List<Integer> idxNodesToHighlight);

    /**
     * Modifies the structure of the PGM by changing the parents of the nodes.
     *
     * @param newAdjacencyMatrix adjacency matrix with the new structure of the PGM
     */
    void setStructure(boolean[][] newAdjacencyMatrix);

    /**
     * Updates the structure of the model only for the specified node.
     *
     * @param idxNode        node index
     * @param structureFound adjacency matrix
     */
    void setStructure(int idxNode, boolean[][] structureFound);

    /**
     * Updates the structure of the model only for the specified node.
     *
     * @param idxNodes       node indexes
     * @param structureFound adjacency matrix found
     */
    void setStructure(List<Integer> idxNodes, boolean[][] structureFound);

    /**
     * Modifies the structure of the PGM by changing the parents and CPDs of those nodes which have different parents
     * between the current adjacency matrix and the new one.
     *
     * @param newAdjacencyMatrix new adjacency matrix
     */
    void setStructureModifiedNodes(boolean[][] newAdjacencyMatrix);

}