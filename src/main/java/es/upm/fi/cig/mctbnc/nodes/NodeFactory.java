package es.upm.fi.cig.mctbnc.nodes;

import java.util.List;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;

/**
 * Provides static methods for the creation of nodes.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType>
 */
public class NodeFactory<NodeType extends Node> {

	final Class<NodeType> nodeClass;

	/**
	 * Constructs a {@code NodeFactory}.
	 * 
	 * @param nodeClass
	 */
	public NodeFactory(Class<NodeType> nodeClass) {
		this.nodeClass = nodeClass;
	}

	/**
	 * Creates a node given the name of its variable and the dataset where it
	 * appears.
	 * 
	 * @param nameVariable
	 * @param dataset
	 * @return node
	 */
	public NodeType createNode(String nameVariable, Dataset dataset) {
		if (this.nodeClass == CIMNode.class) {
			// Create a CIMNode
			List<String> states = dataset.getPossibleStatesVariable(nameVariable);
			boolean isClassVariable = dataset.getNameClassVariables().contains(nameVariable);
			return (NodeType) new CIMNode(nameVariable, states, isClassVariable);
		}
		// Create a CPTNode
		List<String> states = dataset.getPossibleStatesVariable(nameVariable);
		return (NodeType) new CPTNode(nameVariable, states);
	}

}
