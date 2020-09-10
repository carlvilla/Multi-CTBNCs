package com.cig.mctbnc.models;

import java.util.List;

import org.graphstream.graph.Graph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * Defines the methods of a probabilistic graphical model
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface PGM<NodeType extends Node> {

	/**
	 * Learn the structure and parameters of the model.
	 * 
	 * @param dataset dataset used to learn the model
	 * 
	 */
	public void learn(Dataset dataset);

	/**
	 * Add the nodes to list of the PGM. It is necessary to include a NodeIndexer
	 * object in order to keep track of their index.
	 * 
	 * @param nodes
	 */
	public void addNodes(List<NodeType> nodes);

	/**
	 * Remove all the nodes from the PGM.
	 */
	public void removeAllNodes();

	/**
	 * Modify the structure of the PGM by modifying the parents of the nodes and
	 * their CPD.
	 * 
	 * @param adjacencyMatrix
	 */
	public void setStructure(boolean[][] adjacencyMatrix);

	/**
	 * Check if a structure is legal for the PGM.
	 * 
	 * @param adjacencyMatrix bidimensional boolean array representing the adjacency
	 *                        matrix to Analyze
	 * @return boolean that determines if the structure is legal
	 */
	public boolean isStructureLegal(boolean[][] adjacencyMatrix);

	/**
	 * Learn the parameters of the PGM.
	 */
	public void learnParameters();

	/**
	 * Displays the probabilistic graphical model.
	 */
	public void display();

	/**
	 * Return all the nodes in the model.
	 * 
	 * @return node list
	 */
	public List<NodeType> getNodes();

	/**
	 * Obtain the node (feature or class variable) with certain index.
	 * 
	 * @param index
	 * @return node with the specified index
	 */
	public NodeType getNodeByIndex(int index);

	/**
	 * Return the node whose variable name is given.
	 * 
	 * @param nameVariable name of the variable
	 * @return node requested node
	 */
	public NodeType getNodeByName(String nameVariable);

	/**
	 * Return the list of nodes for the class variables.
	 * 
	 * @return list of nodes for the class variables
	 */
	public List<NodeType> getNodesClassVariables();

	/**
	 * Return the list of nodes for the features.
	 * 
	 * @return list of nodes for the features
	 */
	public List<NodeType> getNodesFeatures();

	/**
	 * Return the number of nodes.
	 * 
	 * @return number of nodes
	 */
	public int getNumNodes();

	/**
	 * Return the adjacency matrix.
	 * 
	 * @return bidimensional boolean array representing the adjacency matrix
	 */
	public boolean[][] getAdjacencyMatrix();

	/**
	 * Provide the type of PGM.
	 * 
	 * @return string describing the type of PGM
	 */
	public String getType();

	/**
	 * Return the node indexer used by the PGM.
	 * 
	 * @return node indexer
	 */
	public NodeIndexer<NodeType> getNodeIndexer();

}
