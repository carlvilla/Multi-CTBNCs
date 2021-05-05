package es.upm.fi.cig.mctbnc.models;

import java.util.List;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.nodes.Node;
import es.upm.fi.cig.mctbnc.nodes.NodeIndexer;

/**
 * Defines the methods of a probabilistic graphical model
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType>
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
	 * Sets the training dataset that will be used to estimate the structure and
	 * parameters of the model.
	 * 
	 * @param dataset dataset used to learn the model
	 */
	void setTrainingDataset(Dataset dataset);

	/**
	 * Adds the nodes to list of the PGM. It is necessary to include a NodeIndexer
	 * object in order to keep track of their index.
	 * 
	 * @param nodes
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
	 * @param newAdjacencyMatrix
	 */
	public void setStructure(boolean[][] newAdjacencyMatrix);

	/**
	 * Modifies the structure of the PGM by changing the parents and CPDs of those
	 * nodes which have different parents between the current adjacency matrix and
	 * the new one.
	 * 
	 * @param newAdjacencyMatrix
	 */
	void setStructureModifiedNodes(boolean[][] newAdjacencyMatrix);

	/**
	 * Checks if a structure is legal for the PGM.
	 * 
	 * @param adjacencyMatrix bidimensional boolean array representing the adjacency
	 *                        matrix to Analyze
	 * @return boolean that determines if the structure is legal
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
	 * @return node list
	 */
	public List<NodeType> getNodes();

	/**
	 * Obtains the node (feature or class variable) with certain index.
	 * 
	 * @param index
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
	 * Returns the list of nodes for the features.
	 * 
	 * @return list of nodes for the features
	 */
	public List<NodeType> getNodesFeatures();

	/**
	 * Returns the number of nodes.
	 * 
	 * @return number of nodes
	 */
	public int getNumNodes();

	/**
	 * Returns the adjacency matrix.
	 * 
	 * @return bidimensional boolean array representing the adjacency matrix
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
