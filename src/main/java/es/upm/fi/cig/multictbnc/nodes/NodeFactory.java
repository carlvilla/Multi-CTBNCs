package es.upm.fi.cig.multictbnc.nodes;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static methods for the creation of nodes.
 *
 * @param <NodeType> type of nodes that will be created, e.g., nodes with conditional probability tables ({@code
 *                   CPTNode}) or conditional intensity matrices (@code CIMNode)
 * @author Carlos Villa Blanco
 */
public class NodeFactory<NodeType extends Node> {

    final Class<NodeType> nodeClass;

    /**
     * Constructs a {@code NodeFactory}.
     *
     * @param nodeClass Type of nodes that will be created, e.g., nodes with conditional probability tables ({@code
     *                  CPTNode}) or conditional intensity matrices (@code CIMNode)
     */
    private NodeFactory(Class<NodeType> nodeClass) {
        this.nodeClass = nodeClass;
    }

    /**
     * Constructs a {@code NodeFactory} for nodes whose {@code Class} is passed as a parameter.
     *
     * @param <NodeType> type of nodes to be generated
     * @param nodeClass  {@code Class} of the nodes to be generated
     * @return a {@code NodeFactory}
     */
    public static <NodeType extends Node> NodeFactory<NodeType> createFactory(Class<NodeType> nodeClass) {
        return new NodeFactory<>(nodeClass);
    }

    /**
     * Creates a node given the name of its variable and the dataset where it appears.
     *
     * @param nameVariable name of the node
     * @param dataset      dataset where the variable of the node appears. It is used to extract the states of the
     *                     variable and to define it as a class variable or not
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
            boolean isClassVariable = dataset.getNameClassVariables().contains(nameVariable);
            return (NodeType) new CPTNode(nameVariable, states, isClassVariable);
        } else {
            throw new UnsupportedOperationException("The specified node type is not currently supported");
        }
    }

    /**
     * Creates a node from a given one.
     *
     * @param node a {@code NodeType}
     * @return node a {@code NodeType}
     */
    @SuppressWarnings("unchecked")
    public NodeType createNode(Node node) {
        if (this.nodeClass == CIMNode.class) {
            // Create a CIMNode
            List<String> states = ((CIMNode) node).getStates();
            boolean isClassVariable = node.isClassVariable();
            CIMNode newNode = new CIMNode(node.getName(), states, isClassVariable);
            newNode.setSufficientStatistics(((CIMNode) node).getSufficientStatistics());
            newNode.setParameters(((CIMNode) node).getQx(), ((CIMNode) node).getOxy());
            return (NodeType) newNode;
        } else if (this.nodeClass == CPTNode.class) {
            // Create a CPTNode
            List<String> states = ((CPTNode) node).getStates();
            boolean isClassVariable = node.isClassVariable();
            CPTNode newNode = new CPTNode(node.getName(), states, isClassVariable);
            newNode.setSufficientStatistics(((CPTNode) node).getSufficientStatistics());
            newNode.setCPT(((CPTNode) node).getCPT());
            return (NodeType) newNode;
        } else {
            throw new UnsupportedOperationException("The specified node type is not currently supported");
        }
    }

    /**
     * Creates a node from a given one without storing its parameters or sufficient statistics.
     *
     * @param node a {@code Node}
     * @return new node
     */
    public NodeType createEmptyNode(Node node) {
        if (this.nodeClass == CIMNode.class) {
            // Create a CIMNode
            List<String> states = ((CIMNode) node).getStates();
            CIMNode newNode = new CIMNode(node.getName(), states, node.isClassVariable());
            //noinspection unchecked
            return (NodeType) newNode;
        } else if (this.nodeClass == CPTNode.class) {
            // Create a CPTNode
            List<String> states = ((CPTNode) node).getStates();
            CPTNode newNode = new CPTNode(node.getName(), states, node.isClassVariable());
            //noinspection unchecked
            return (NodeType) newNode;
        } else {
            throw new UnsupportedOperationException("The specified node type is not currently supported");
        }
    }

    /**
     * Creates a list of nodes with the same attributes as those provided.
     *
     * @param nodes list of {@code NodeType}
     * @return list of {@code NodeType}
     */
    public List<NodeType> createNodes(List<NodeType> nodes) {
        List<NodeType> newNodes = new ArrayList<>();
        for (Node node : nodes)
            newNodes.add(createNode(node));
        // Recover the parents of each node
        setDependenciesNodes(newNodes, nodes);
        return newNodes;
    }

    /**
     * Set the dependencies of the created nodes given the dependencies of the provided nodes. Both lists are
     * assumed to
     * contain the same nodes in the same order. If the type of a parent node is different from that created by the
     * factory, a reference is saved to that node. Otherwise, the dependencies are defined only between the newly
     * created nodes. It is important to keep the order of the parents since the state indexes could vary otherwise.
     *
     * @param newNodes      created nodes
     * @param providedNodes provided node
     */
    private void setDependenciesNodes(List<NodeType> newNodes, List<NodeType> providedNodes) {
        for (int idxNode = 0; idxNode < newNodes.size(); idxNode++) {
            for (Node parentNode : providedNodes.get(idxNode).getParents()) {
                if (parentNode.getClass().equals(this.nodeClass) && newNodes.contains(parentNode))
                    newNodes.get(idxNode).setParent(newNodes.get(newNodes.indexOf(parentNode)));
                else {
                    newNodes.get(idxNode).setParent(
                            NodeFactory.createFactory(parentNode.getClass()).createNode(parentNode));
                }
            }
        }
    }

}