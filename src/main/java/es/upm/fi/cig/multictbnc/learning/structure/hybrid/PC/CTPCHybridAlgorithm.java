package es.upm.fi.cig.multictbnc.learning.structure.hybrid.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC.CTPC;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the restriction phase (CTPC algorithm) of the hybrid structure learning algorithm.
 *
 * @author Carlos Villa Blanco
 */
public class CTPCHybridAlgorithm extends CTPC {
    private static final Logger logger = LogManager.getLogger(CTPCHybridAlgorithm.class);
    int maxSizeSepSet;

    /**
     * Initialises the algorithm by proving a significance level.
     *
     * @param maxSizeSepSet                       maximum separating set size
     * @param sigTimeTransitionHypothesis         significance level
     * @param sigStateToStateTransitionHypothesis significance level
     */
    public CTPCHybridAlgorithm(int maxSizeSepSet, double sigTimeTransitionHypothesis,
                               double sigStateToStateTransitionHypothesis) {
        super(sigTimeTransitionHypothesis, sigStateToStateTransitionHypothesis);
        this.maxSizeSepSet = maxSizeSepSet;
    }

    /**
     * Learns the initial structure of a given PGM. Indexes of feature variables whose parent sets will be learnt need
     * to be specified.
     *
     * @param pgm                 probabilistic graphical model
     * @param idxFeatureVariables indexes of feature variables
     * @return initial adjacency matrix
     */
    public boolean[][] learnInitialStructure(PGM<? extends Node> pgm, List<Integer> idxFeatureVariables) {
        boolean[][] adjacencyMatrix = buildCompleteStructure(pgm);
        idxFeatureVariables.parallelStream().forEach(idxFeatureVariable -> {
            @SuppressWarnings("unchecked") CTBN<CIMNode> ctbn = new CTBN<>((CTBN<CIMNode>) pgm,
                    ((CTBN<CIMNode>) pgm).getDataset());
            // Find parent set of each node in parallel
            try {
                learnParentSetNode(ctbn, idxFeatureVariable, adjacencyMatrix);
            } catch (ErroneousValueException erroneousValueException) {
                logger.error(erroneousValueException.getMessage());
            }
        });
        return adjacencyMatrix;
    }

    @Override
    protected void learnParentSetNode(PGM<? extends Node> pgm, int idxNode, boolean[][] adjacencyMatrix)
            throws ErroneousValueException {
        // Cache used to avoid the recomputation of parameters and sufficient statistics
        Map<String, List<Object>> cache = new HashMap<>();
        // Retrieve node
        CIMNode node = (CIMNode) pgm.getNodeByIndex(idxNode);
        logger.info("Define parents of node {}", node.getName());
        // Define all other variables as the parents of the node
        List<Integer> currentParents = getIdxParentsNode(idxNode, adjacencyMatrix);
        // Iterate over possible sizes of the separating sets
        for (int sizeSepSets = 0; sizeSepSets <= currentParents.size() - 1 && sizeSepSets <= this.maxSizeSepSet;
             sizeSepSets++) {
            logger.info("Evaluating separating sets of size = {} for {}", sizeSepSets, node.getName());
            // Iterate over the parents of the variable to decide if it should be removed
            parentNodeLoop:
            for (int idxParentNode = 0; idxParentNode < adjacencyMatrix.length; idxParentNode++) {
                // Only evaluate conditional independence test with variables that are
                // considered potential parents
                if (adjacencyMatrix[idxParentNode][idxNode]) {
                    // Retrieve parent node
                    CIMNode parentNode = (CIMNode) pgm.getNodeByIndex(idxParentNode);
                    // Retrieve possible separating sets and iterate over them
                    List<List<Integer>> possibleSepSets = Util.getSubsets(currentParents, sizeSepSets, idxParentNode);
                    for (List<Integer> idxSepSet : possibleSepSets) {
                        // Extract names of variables in the subset
                        List<String> nameNodesSepSet = pgm.getNamesNodesByIndex(idxSepSet);
                        logger.debug("Test conditional independence between {} and {} given separating set {}",
                                node.getName(), parentNode.getName(), nameNodesSepSet);
                        // Set nodes of separating set as parents of the node
                        addSepSetAsParents(pgm, node, idxSepSet);
                        // Retrieve parameters and sufficient statistics given the separating set
                        retrieveParametersAndSuffStatistics(pgm, idxNode, cache, idxSepSet);
                        double[][] qxSepSet = node.getQx();
                        CTBNSufficientStatistics suffStatSepSet = node.getSufficientStatistics();
                        // Set nodes of separating set and 'idxParentNode' as parents of the node
                        addSepSetAndNodeAsParents(pgm, node, idxSepSet, idxParentNode);
                        // Retrieve parameters and sufficient statistics given 'idxParentNode' and the
                        // separating set
                        retrieveParametersAndSuffStatistics(pgm, idxNode, cache, idxSepSet, idxParentNode);
                        double[][] qxSepSetAndParent = node.getQx();
                        CTBNSufficientStatistics suffStatSepSetAndParent = node.getSufficientStatistics();
                        // Test null time to transition hypothesis for the given separating set
                        boolean isNullTimeToTransitionHypAccepted = testNullTimeToTransitionHypForGivenSepSet(node,
                                parentNode, nameNodesSepSet, qxSepSet, suffStatSepSet, qxSepSetAndParent,
                                suffStatSepSetAndParent);
                        // If hypothesis is rejected, evaluate following separating set
                        if (!isNullTimeToTransitionHypAccepted)
                            continue;
                        // The null time to transition hypothesis was not rejected
                        if (node.getNumStates() > 2) {
                            // Test null state-to-state transition hypothesis
                            boolean isNullStateToStateTransitionHypAccepted =
                                    testNullStateToStateTransitionHypForGivenSepSet(
                                            node, parentNode, nameNodesSepSet, suffStatSepSet, suffStatSepSetAndParent);
                            // If hypothesis is rejected, evaluate following separating set
                            if (!isNullStateToStateTransitionHypAccepted)
                                continue;
                        }
                        // If hypothesis tests are not rejected, nodes are conditionally independent
                        logger.trace("Removing arc {} <- {}, given separating set {}", node.getName(),
                                parentNode.getName(), nameNodesSepSet);
                        // Conditional independence between the variables was detected
                        adjacencyMatrix[idxParentNode][idxNode] = false;
                        currentParents.remove(Integer.valueOf(idxParentNode));
                        continue parentNodeLoop;
                    }
                }
            }
        }
        logger.info("Parents of node {} were learnt", node.getName());
    }

}