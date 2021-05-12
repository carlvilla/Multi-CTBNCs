package es.upm.fi.cig.mctbnc.nodes;

import java.util.List;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;

/**
 * Provides static methods for the creation of nodes.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> type of nodes that will be created, e.g., nodes with
 *                   conditional probability tables ({@code CPTNode}) or
 *                   conditional intensity matrices (@code CIMNode)
 */
public class NodeFactory<NodeType extends Node> {

	final Class<NodeType> nodeClass;

	/**
	 * Constructs a {@code NodeFactory}.
	 * 
	 * @param nodeClass Type of nodes that will be created, e.g., nodes with
	 *                  conditional probability tables ({@code CPTNode}) or
	 *                  conditional intensity matrices (@code CIMNode)
	 */
	private NodeFactory(Class<NodeType> nodeClass) {
		this.nodeClass = nodeClass;
	}

	/**
	 * Constructs a {@code NodeFactory} for nodes whose {@code Class} is passed as a
	 * parameter.
	 * 
	 * @param nodeClass {@code Class} of the nodes
	 * @return a {@code NodeFactory}
	 */

	/**
	 * Constructs a {@code NodeFactory} for nodes whose {@code Class} is passed as a
	 * parameter.
	 * 
	 * @param <NodeType> type of nodes to be generated
	 * @param nodeClass  {@code Class} of the nodes to be generated
	 * @return a {@code NodeFactory}
	 */
	public static <NodeType extends Node> NodeFactory<NodeType> createFactory(Class<NodeType> nodeClass) {
		return new NodeFactory<NodeType>(nodeClass);
	}

	/**
	 * Creates a node given the name of its variable and the dataset where it
	 * appears.
	 * 
	 * @param nameVariable name of the node
	 * @param dataset      dataset where the variable of the node appears. It is
	 *                     used to extract the states of the variable and to define
	 *                     it as a class variable or not
	 * @return node a {@code NodeType}
	 */
	@SuppressWarnings("unchecked")
	public NodeType createNode(String nameVariable, Dataset dataset) {
		if (this.nodeClass == CIMNode.class) {
			// Create a CIMNode
			List<String> states = dataset.getPossibleStatesVariable(nameVariable);
			boolean isClassVariable = dataset.getNameClassVariables().contains(nameVariable);
			return (NodeType) new CIMNode(nameVariable, states, isClassVariable);
		} else if (this.nodeClass == CPTNode.class) {
			// Create a CPTNode
			List<String> states = dataset.getPossibleStatesVariable(nameVariable);
			return (NodeType) new CPTNode(nameVariable, states);
		} else {
			throw new UnsupportedOperationException("The specified node type is not currently supported");
		}
	}

}
