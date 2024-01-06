package es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class extends the MB-CTPC algorithm to an online learning context, allowing for dynamic updates to the
 * learned model based on new data.
 *
 * @author Carlos Villa Blanco
 */
public class OnlineMarkovBlanketCTPC extends MarkovBlanketCTPC {
    private static final Logger logger = LogManager.getLogger(OnlineMarkovBlanketCTPC.class);

    /**
     * Initialises the Online-MB-CTPC algorithm by providing the significances to be used.
     *
     * @param sigTimeTransitionHypothesis         significance level used for the null time to transition hypothesis
     * @param sigStateToStateTransitionHypothesis significance level used for the null state-to-state transition
     *                                            hypothesis
     */
    public OnlineMarkovBlanketCTPC(double sigTimeTransitionHypothesis, double sigStateToStateTransitionHypothesis) {
        super(sigTimeTransitionHypothesis, sigStateToStateTransitionHypothesis);
    }

    @Override
    public void learn(PGM<? extends Node> pgm, List<Integer> idxNodesToUpdate) {
        logger.info(
                "Learning {} using the online Markov blanket-based continuous-time PC algorithm (Online-MB-CTPC) and " +
                        "significances" +
                        " " + "alpha_1 = {} and alpha_2 = {}", pgm.getType(), this.sigTimeTransitionHyp,
                this.sigStateToStateTransitionHyp);
        // Algorithm used to learn the bridge and feature subgraph of a Multi-CTBNC
        List<Integer> idxFeatureVariables = getIdxFeatureVariables(pgm);
        List<Integer> idxFeatureVariablesToUpdate = getIdxFeatureVariables(pgm, idxNodesToUpdate);
        List<Integer> idxClassVariables = getIdxClassVariables(pgm);
        boolean[][] newAdjacencyMatrix = buildCompleteBridgeSubgraphAndParentSetUpdatedFeatureNodes(pgm,
                idxFeatureVariablesToUpdate);
        Map<Integer, Map<String, List<Object>>> caches = new ConcurrentHashMap<>();
        initialiseCaches(caches, idxFeatureVariables);
        defineDescendantsClassVariables((CTBN<CIMNode>) pgm, idxFeatureVariables, newAdjacencyMatrix, caches);
        removeUnlikelyDependencies(pgm, idxFeatureVariables, idxClassVariables, newAdjacencyMatrix);
        updateBridgeAndFeatureSubgraphs(pgm, idxFeatureVariablesToUpdate, newAdjacencyMatrix, caches);
        pgm.setStructure(idxFeatureVariablesToUpdate, newAdjacencyMatrix);
        pgm.learnParameters(idxFeatureVariablesToUpdate);
        logger.info("Bridge and feature subgraph learnt performing {} conditional independence tests",
                this.numCondIndTestsPerformed);
        // Reset number of performed conditional independence tests
        this.numCondIndTestsPerformed = 0;
    }

    /**
     * Retrieves the index nodes representing feature variables from a given list of index nodes.
     *
     * @param pgm      the PGM containing the nodes
     * @param idxNodes the list of index nodes
     * @return the list of index nodes representing feature variables
     */
    protected List<Integer> getIdxFeatureVariables(PGM<? extends Node> pgm, List<Integer> idxNodes) {
        List<Integer> idxFeatureVariables = new ArrayList<>();
        for (int idxNode : idxNodes)
            if (!pgm.getNodeByIndex(idxNode).isClassVariable())
                idxFeatureVariables.add(idxNode);
        return idxFeatureVariables;
    }

    /**
     * Returns the adjacency matrix of a PGM with a complete bridge subgraph and the complete parent sets of a some
     * given features nodes.
     *
     * @param pgm             the PGM containing the nodes
     * @param idxFeatureNodes indexes of the feature nodes whose parent sets are built
     * @return
     */
    private boolean[][] buildCompleteBridgeSubgraphAndParentSetUpdatedFeatureNodes(PGM<? extends Node> pgm,
                                                                                   List<Integer> idxFeatureNodes) {
        boolean[][] adjacencyMatrix = new boolean[pgm.getNumNodes()][pgm.getNumNodes()];
        for (int i = 0; i < pgm.getNumNodes(); i++) {
            for (int j = 0; j < pgm.getNumNodes(); j++) {
                if (i != j && ((pgm.getNodeByIndex(i).isClassVariable() && !pgm.getNodeByIndex(j).isClassVariable()) ||
                        (!pgm.getNodeByIndex(i).isClassVariable() && idxFeatureNodes.contains(j))))
                    adjacencyMatrix[i][j] = true;
            }
        }
        return adjacencyMatrix;
    }

    /**
     * Updates the bridge and feature subgraphs of the PGM. This method is key in the online learning process, where
     * only specific parts of the model are modified based on new data.
     *
     * @param pgm                         PGM being updated
     * @param idxFeatureVariablesToUpdate list of indices of feature variables to be updated
     * @param adjacencyMatrix             adjacency matrix representing the structure of the model
     * @param caches                      map of caches used for efficient computation during updates
     */
    private void updateBridgeAndFeatureSubgraphs(PGM<? extends Node> pgm, List<Integer> idxFeatureVariablesToUpdate,
                                                 boolean[][] adjacencyMatrix,
                                                 Map<Integer, Map<String, List<Object>>> caches) {
        // Update the bridge parent sets of the given feature nodes
        logger.info("Update parent set of feature nodes {}", pgm.getNamesNodesByIndex(idxFeatureVariablesToUpdate));
        idxFeatureVariablesToUpdate.parallelStream().forEach(idxFeatureVariable -> {
            @SuppressWarnings("unchecked") CTBN<CIMNode> ctbn = new CTBN<>((CTBN<CIMNode>) pgm,
                    ((CTBN<CIMNode>) pgm).getDataset());
            // Parents of feature variables are being defined in parallel. Class variables are ignored
            try {
                learnFinalBridgeAndFeatureSubgraphs(ctbn, idxFeatureVariable, adjacencyMatrix,
                        caches.get(idxFeatureVariable));
            } catch (ErroneousValueException erroneousValueException) {
                logger.error("[Variable {}] {}", ctbn.getNameVariables().get(idxFeatureVariable),
                        erroneousValueException.getMessage());
            }
        });
    }

}