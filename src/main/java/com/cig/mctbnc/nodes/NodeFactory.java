package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;

/**
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType>
 */
public class NodeFactory<NodeType extends Node> {

	final Class<NodeType> nodeClass;

	public NodeFactory(Class<NodeType> nodeClass) {
		this.nodeClass = nodeClass;
	}

	public NodeType createNode(String nameVariable, Dataset dataset) {
		if (nodeClass == CIMNode.class) {
			// Create a CIMNode
			List<String> states = dataset.getPossibleStatesVariable(nameVariable);
			boolean isClassVariable = dataset.getNameClassVariables().contains(nameVariable);
			return (NodeType) new CIMNode(nameVariable, states, isClassVariable);
		} else {
			// Create a CPTNode
			List<String> states = dataset.getPossibleStatesVariable(nameVariable);
			return (NodeType) new CPTNode(nameVariable, states);
		}
	}

}
