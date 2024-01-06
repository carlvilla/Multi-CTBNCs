package es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNSufficientStatistics;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the HITON-PC algorithm.
 *
 * @author Carlos Villa Blanco
 */
public class HITONPC extends PC {
    private static final Logger logger = LogManager.getLogger(PC.class);

    /**
     * Constructor that initialises the HITON-PC algorithm by proving the significance level used.
     *
     * @param significance significance level
     */
    public HITONPC(double significance) {
        super(significance);
    }

    @Override
    public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) throws ErroneousValueException {
        // We want to find the new parent set of the provided nodes. This method is used to learn the class subgraph
        // of multi-ctbncs
        logger.info("Updating parent set of nodes {} using the HITON-PC algorithm...", idxNodes);
        boolean[][] currentAdjacencyMatrix = pgm.getAdjacencyMatrix();
        // Iterate each provided variable
        for (int idxNodeX : idxNodes) {
            CPTNode nodeX = (CPTNode) pgm.getNodeByIndex(idxNodeX);
            List<Integer> currentChildren =
                    nodeX.getChildren().stream().filter(Node::isClassVariable).map(pgm::getIndexOfNode).collect(Collectors.toList());
            List<Integer> currentParent =
                    nodeX.getParents().stream().map(pgm::getIndexOfNode).collect(Collectors.toList());
            List<Integer> currentPC = new ArrayList<>(currentChildren);
            currentPC.addAll(currentParent);
            // Get nodes sorted by pairwise association with idxNodeX
            List<Integer> openSet = getOPENSet(pgm, idxNodeX);
            List<Integer> nodeXCP = new ArrayList<>();
            // Elimination strategy
            while (!openSet.isEmpty()) {
                nodeXCP.add(openSet.get(0));
                openSet.remove(0);
                List<Integer> tempTCP = new ArrayList<>(nodeXCP);
                for (Integer idxNodeY : tempTCP) {
                    CPTNode nodeY = (CPTNode) pgm.getNodeByIndex(idxNodeY);
                    List<Integer> TCPMinusY = new ArrayList<>(tempTCP);
                    TCPMinusY.remove(idxNodeY);
                    for (int l = 1; l <= TCPMinusY.size(); l++) {
                        List<List<Integer>> possibleSepSets = Util.getSubsets(TCPMinusY, l, idxNodeX);
                        for (List<Integer> idxSepSet : possibleSepSets) {
                            // Extract names of variables in the subset
                            List<String> nameNodesSepSet = pgm.getNamesNodesByIndex(idxSepSet);
                            logger.debug("Test conditional independence between {} and {} given separating set {}",
                                    nodeX.getName(), nodeY.getName(), nameNodesSepSet);
                            numCondIndTestsPerformed++;
                            // Remove previous parents of the nodes
                            nodeX.removeParents();
                            nodeY.removeParents();
                            // Retrieve sufficient statistics of nodeX given separating set
                            addParentsToNode(pgm, nodeX, idxSepSet);
                            pgm.computeSufficientStatistics(List.of(idxNodeX));
                            BNSufficientStatistics suffStatNodeXGivenSepSet = nodeX.getSufficientStatistics();
                            // Retrieve sufficient statistics of nodeY given separating set
                            addParentsToNode(pgm, nodeY, idxSepSet);
                            pgm.computeSufficientStatistics(List.of(idxNodeY));
                            BNSufficientStatistics suffStatNodeYGivenSepSet = nodeY.getSufficientStatistics();
                            // Retrieve sufficient statistics of nodeX <- nodeY given separating set
                            nodeX.setParent(nodeY);
                            pgm.computeSufficientStatistics(List.of(idxNodeX));
                            BNSufficientStatistics suffStatNodeXGivenNodeYSepSet = nodeX.getSufficientStatistics();
                            // Test if nodeX and nodeY are conditionally independent given a separating set
                            boolean areConditionalDependent = testConditionalDependence(nodeX, nodeY,
                                    suffStatNodeXGivenSepSet, suffStatNodeYGivenSepSet, suffStatNodeXGivenNodeYSepSet);
                            if (!areConditionalDependent) {
                                logger.debug("Nodes {} and {} are conditionally independent given {}", nodeX.getName(),
                                        nodeY.getName(), nameNodesSepSet);
                                nodeXCP.remove(nodeY);
                                break;
                            }
                        }
                    }
                }
            }
            // In nodeXCP we have the current children and parents of nodeX
            // Remove arcs from parents of nodeX that are not in nodeXCP
            List<Integer> idxOldAdjNodes =
                    currentPC.stream().filter(idxNode -> !nodeXCP.contains(idxNode)).collect(Collectors.toList());
            for (int idxOldAdjNode : idxOldAdjNodes)
                currentAdjacencyMatrix[idxOldAdjNode][idxNodeX] = false;
            // Add arcs from new parent in nodeXCP to nodeX
            List<Integer> idxNewAdjNodes =
                    nodeXCP.stream().filter(idxNode -> !currentPC.contains(idxNode)).collect(Collectors.toList());
            for (int idxNewAdjNode : idxNewAdjNodes) {
                currentAdjacencyMatrix[idxNewAdjNode][idxNodeX] = true;
                // An arc will only be added if no cycles are introduced
                if (!pgm.isStructureLegal(currentAdjacencyMatrix)) {
                    currentAdjacencyMatrix[idxNewAdjNode][idxNodeX] = false;
                }
            }
        }
        // Set found structure and learn parameters
        pgm.setStructure(currentAdjacencyMatrix);
        pgm.learnParameters(idxNodes);
        logger.info("Parent sets of nodes {} were updated", idxNodes);
    }

    private List<Integer> getOPENSet(PGM<? extends Node> pgm, int idxNodeX) {
        // Use the chi-squared statistic to compute the potential association between two nodes.
        Map<Integer, Double> associationOtherNodes = new HashMap<>();
        for (int idxNodeY : pgm.getIndexNodes()) {
            if (idxNodeY != idxNodeX) {
                CPTNode nodeX = (CPTNode) pgm.getNodeByIndex(idxNodeX);
                CPTNode nodeY = (CPTNode) pgm.getNodeByIndex(idxNodeY);
                // Remove previous parents of the nodes
                nodeX.removeParents();
                nodeY.removeParents();
                // Retrieve sufficient statistics of nodeX
                pgm.computeSufficientStatistics(List.of(idxNodeX));
                BNSufficientStatistics suffStatNodeX = nodeX.getSufficientStatistics();
                // Retrieve sufficient statistics of nodeY
                pgm.computeSufficientStatistics(List.of(idxNodeY));
                BNSufficientStatistics suffStatNodeY = nodeY.getSufficientStatistics();
                // Retrieve sufficient statistics of nodeX <- nodeY given separating set
                nodeX.setParent(nodeY);
                pgm.computeSufficientStatistics(List.of(idxNodeX));
                BNSufficientStatistics suffStatNodeXGivenNodeY = nodeX.getSufficientStatistics();
                double chiSquared = 0;
                int df = 0;
                int numSamples = getNumSamplesGivenSepSet(nodeX, nodeY, suffStatNodeXGivenNodeY);
                for (int idxStateNodeY = 0; idxStateNodeY < nodeY.getNumStates(); idxStateNodeY++) {
                    nodeY.setState(idxStateNodeY);
                    double probNodeY = suffStatNodeY.getNx()[0][idxStateNodeY] / numSamples;
                    for (int idxStateNodeX = 0; idxStateNodeX < nodeX.getNumStates(); idxStateNodeX++) {
                        double probNodeX = suffStatNodeX.getNx()[0][idxStateNodeX] / numSamples;
                        double observed = suffStatNodeXGivenNodeY.getNx()[nodeX.getIdxStateParents()][idxStateNodeX];
                        double expected = (probNodeY * probNodeX) * numSamples;
                        chiSquared += Math.pow(observed - expected, 2) / expected;
                        df += (nodeX.getNumStates() - 1) * (nodeX.getNumStatesParents() - 1);
                    }
                }
                // Obtain critical value for the given significance
                double criticalValue = 0;
                try {
                    // Retrieve critical values from the chi-squared distribution
                    ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(df);
                    criticalValue = chiSquaredDistribution.inverseCumulativeProbability(1 - this.significance);
                } catch (OutOfRangeException ore) {
                    logger.error(
                            "The significance must be in range [0, 1]. Value provided: " + this.significance);
                } catch (NotStrictlyPositiveException nspe) {
                    logger.error(
                            "Degree of freedom provided to the chi-squared test (PC algorithm) is zero. Check if " +
                                    "variables " +
                                    nodeX.getName() + " or " + nodeY.getName() + " have only one possible state");
                }
                // If chiSquared >= criticalValue, the null hypothesis is rejected, and
                // variables are conditionally dependent
                if (chiSquared >= criticalValue) {
                    associationOtherNodes.put(idxNodeY, chiSquared);
                }
            }
        }
        // Sort nodes by pairwise association
        return associationOtherNodes.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }


}
