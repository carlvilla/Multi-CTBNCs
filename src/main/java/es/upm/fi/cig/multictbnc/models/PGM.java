package es.upm.fi.cig.multictbnc.models;

import java.util.List;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;

/**
 * Defines the methods of a probabilistic graphical model (PGM)
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> type of the nodes of the PGM
 */
public interface PGM<NodeType extends Node> {

	/**
	 * Learns the structure and parameters of the model.
	 * 
	 */
	public void learn();

	/**
	 * Learns the structure and parameters of the model from a given dataset.
	 * 
	 * @param dataset dataset used to learn the model
	 */
	public void learn(Dataset dataset);

	/**
	 * Sets the dataset that will be used to estimate the structure and parameters
	 * of the model and creates its nodes.
	 * 
	 * @param dataset dataset used to learn the model
	 */
	void initializeModel(Dataset dataset);

	/**
	 * Adds the provided nodes to the PGM. It is necessary to include a NodeIndexer
	 * object in order to keep track of their index.
	 * 
	 * @param nodes nodes to add
	 */
	public void addNodes(List<NodeType> nodes);

	/**
	 * Removes all the nodes from the PGM.
	 */
	public void removeAllNodes();

	/**
	 * Modifies the structure of the PGM by changing the parents of the nodes and
	 * their CPD.
	 * 
	 * @param newAdjacencyMatrix adjacency matrix with the new structure of the PGM
	 */
	public void setStructure(boolean[][] newAdjacencyMatrix);

	/**
	 * Modifies the structure of the PGM by changing the parents and CPDs of those
	 * nodes which have different parents between the current adjacency matrix and
	 * the new one.
	 * 
	 * @param newAdjacencyMatrix new adjacency matrix
	 */
	void setStructureModifiedNodes(boolean[][] newAdjacencyMatrix);

	/**
	 * Checks if a structure is legal for the PGM.
	 * 
	 * @param adjacencyMatrix two-dimensional {@code boolean} array representing the
	 *                        adjacency matrix to Analyze
	 * @return true if the structure is legal, false otherwise
	 */
	public boolean isStructureLegal(boolean[][] adjacencyMatrix);

	/**
	 * Learns the parameters of the PGM.
	 */
	public void learnParameters();

	/**
	 * Learns the parameters of the nodes whose indexes are specified.
	 * 
	 * @param idxsNodes indexes of the nodes whose parameters should be learned
	 */
	public void learnParameters(List<Integer> idxsNodes);

	/**
	 * Displays the probabilistic graphical model.
	 */
	public void display();

	/**
	 * Returns all the nodes in the model.
	 * 
	 * @return list of nodes
	 */
	public List<NodeType> getNodes();

	/**
	 * Obtains the node (feature or class variable) with certain index.
	 * 
	 * @param index node index
	 * @return node with the specified index
	 */
	public NodeType getNodeByIndex(int index);

	/**
	 * Returns the node whose variable name is given.
	 * 
	 * @param nameVariable name of the variable
	 * @return requested node
	 */
	public NodeType getNodeByName(String nameVariable);

	/**
	 * Returns the nodes whose variable names are given.
	 * 
	 * @param nameVariables names of the variables
	 * @return requested nodes
	 */
	List<NodeType> getNodesByNames(List<String> nameVariables);

	/**
	 * Returns the list of nodes for the class variables.
	 * 
	 * @return list of nodes for the class variables
	 */
	public List<NodeType> getNodesClassVariables();

	/**
	 * Returns the list of nodes for the feature variables.
	 * 
	 * @return list of nodes for the feature variables
	 */
	public List<NodeType> getNodesFeatureVariables();

	/**
	 * Returns the number of nodes.
	 * 
	 * @return number of nodes
	 */
	public int getNumNodes();

	/**
	 * Returns the adjacency matrix.
	 * 
	 * @return two-dimensional {@code boolean} array representing the adjacency
	 *         matrix
	 */
	public boolean[][] getAdjacencyMatrix();

	/**
	 * Provides the type of PGM.
	 * 
	 * @return string describing the type of PGM
	 */
	public String getType();

	/**
	 * Returns the node indexer used by the PGM.
	 * 
	 * @return node indexer
	 */
	public NodeIndexer<NodeType> getNodeIndexer();

	/**
	 * Returns true if all the parameters were estimated.
	 * 
	 * @return true if all the parameters were estimated
	 */
	public boolean areParametersEstimated();

	/**
	 * Returns a {@code String} that identifies the model.
	 * 
	 * @return {@code String} that identifies the model
	 */
	public String getModelIdentifier();

}
