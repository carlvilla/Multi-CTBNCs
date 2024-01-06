package es.upm.fi.cig.multictbnc.fss;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.NodeFactory;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides the basis for statistical-based feature subset selection algorithms.
 *
 * @author Carlos Villa Blanco
 */
public abstract class StatisticalBasedFeatureSelection {
    // Dataset used to perform the FSS
    static Dataset dataset;
    // Significances
    static double sigTimeTransitionHyp = 0.00001;
    static double sigStateToStateTransitionHyp = 0.00001;
    // Parameter learning algorithms
    static ParameterLearningAlgorithm cimPLA;
    // Define factories to build the nodes
    static NodeFactory<CPTNode> factoryCPTNodes = NodeFactory.createFactory(CPTNode.class);
    static NodeFactory<CIMNode> factoryCIMNodes = NodeFactory.createFactory(CIMNode.class);
    static Logger logger = LogManager.getLogger(StatisticalBasedFeatureSelection.class);
    int maxSeparatingSizeRedundancyAnalysis;
    List<String> nameClassVariables;
    List<String> nameFeatureVariables;
    Map<String, Boolean> redundancyCache;

    /**
     * Initializes the necessary structures for feature subset selection including parameter learning algorithms,
     * significance levels, and a cache for redundancy checks.
     *
     * @param nameClassVariables           names of class variables to be considered in the feature selection process
     * @param cimPLA                       parameter learning algorithm for CIM nodes
     * @param maxSeparatingSizeFSS         maximum size of the separating set to be considered in redundancy analysis
     * @param sigTimeTransitionHyp         significance level for time transition hypothesis tests
     * @param sigStateToStateTransitionHyp significance level for state-to-state transition hypothesis tests
     */
    public StatisticalBasedFeatureSelection(List<String> nameClassVariables, ParameterLearningAlgorithm cimPLA,
                                            int maxSeparatingSizeFSS, double sigTimeTransitionHyp, double sigStateToStateTransitionHyp) {
        this.nameClassVariables = nameClassVariables;
        this.nameFeatureVariables = new ArrayList<>();
        this.cimPLA = cimPLA;
        this.sigTimeTransitionHyp = sigTimeTransitionHyp;
        this.sigStateToStateTransitionHyp = sigStateToStateTransitionHyp;
        this.redundancyCache = new HashMap<>();
        this.maxSeparatingSizeRedundancyAnalysis = maxSeparatingSizeFSS;
    }

    /**
     * Tests whether a feature and a class variables are conditionally independent given a certain separating set.
     *
     * @param featureNode feature node to test
     * @param classNode   class node to test
     * @param sepSet      list of nodes forming the separating set
     * @return {@code true} if the variables are conditionally independent, {@code false} otherwise.
     */
    public static boolean conditionalIndependenceTest(CIMNode featureNode, CPTNode classNode, List<CIMNode> sepSet) {
        // Create new nodes to perform the relevance analysis in order to avoid race conditions
        CPTNode cloneClassNode = factoryCPTNodes.createEmptyNode(classNode);
        CIMNode cloneFeatureNode = factoryCIMNodes.createEmptyNode(featureNode);
        List<CIMNode> cloneSepSet = new ArrayList<CIMNode>();
        for (CIMNode node : sepSet)
            cloneSepSet.add(factoryCIMNodes.createEmptyNode(node));
        // Retrieve the names of the feature variables in the separating set
        List<String> nameFeaturesSepSet =
                cloneSepSet.stream().map(node -> node.getName()).collect(Collectors.toList());
        // Learn parameters of the feature variable given that the separating set's variables are its parents
        addCondSetAsParents(cloneFeatureNode, cloneSepSet);
        cimPLA.learn(cloneFeatureNode, dataset);
        // Retrieve parameters and sufficient statistics of the feature variable
        double[][] qx = cloneFeatureNode.getQx();
        CTBNSufficientStatistics suffStat = cloneFeatureNode.getSufficientStatistics();
        // Set the class variable as parent of the feature variable and retrieve the parameters and sufficient
        // statistics of the latter
        cloneFeatureNode.setParent(cloneClassNode);
        cimPLA.learn(cloneFeatureNode, dataset);
        double[][] qxGivenClassVariable = cloneFeatureNode.getQx();
        CTBNSufficientStatistics suffStatGivenClassVariable = cloneFeatureNode.getSufficientStatistics();
        // Test if no statistical relationship exists between the feature and class variable for the waiting times
        boolean testResult = testNullTimeToTransitionHypothesis(cloneFeatureNode, nameFeaturesSepSet, suffStat, qx,
                suffStatGivenClassVariable, qxGivenClassVariable);
        if (testResult) {
            // If they are conditional independent for the waiting times, check in terms of state transitions
            return testNullStateToStateTransitionHypothesis(cloneFeatureNode, nameFeaturesSepSet, suffStat,
                    suffStatGivenClassVariable);
        }
        return false;
    }

    /**
     * Evaluates the null state-to-state transition hypothesis between a feature node and the class node,
     * given a separating set. This method iteratively tests each state of the feature node against all possible
     * states of its parents (including those in the separating set and the class node), to determine if state
     * transitions of the feature node are independent of the class node, conditioned on the separating set.
     *
     * @param featureNode                feature node whose state transitions are being tested
     * @param nameFeatureVariablesSepSet names of the feature variables in the separating set
     * @param suffStat                   sufficient statistics for the feature node without the class variable as a
     *                                   parent
     * @param suffStatGivenClass         sufficient statistics for the feature node with the class variable as a parent
     * @return {@code true} if the null hypothesis is not rejected indicating that the state transitions of the
     * feature node are conditionally independent of the class node given the separating set; {@code false} otherwise
     */
    protected static boolean testNullStateToStateTransitionHypothesis(CIMNode featureNode,
                                                                      List<String> nameFeatureVariablesSepSet,
                                                                      CTBNSufficientStatistics suffStat,
                                                                      CTBNSufficientStatistics suffStatGivenClass) {
        // Iterate over all possible states of the node
        for (int idxState = 0; idxState < featureNode.getNumStates(); idxState++) {
            // Iterate over all possible states of the subset and 'idxParent'
            for (int idxStateSepSetAndParent = 0; idxStateSepSetAndParent < featureNode.getNumStatesParents();
                 idxStateSepSetAndParent++) {
                // Perform hypothesis test
                boolean isNullStateToStateTransitionHypAccepted = testNullStateToStateTransitionHypGivenState(
                        featureNode, nameFeatureVariablesSepSet, suffStat, suffStatGivenClass, idxState,
                        idxStateSepSetAndParent);
                if (!isNullStateToStateTransitionHypAccepted) {
                    // Reject hypothesis. Variables are not conditionally independent
                    logger.debug("Nodes {} and class variable ARE NOT conditionally independent given {} (using the " +
                            "transitions)", featureNode.getName(), nameFeatureVariablesSepSet);
                    return false;
                }
            }
        }
        logger.debug("Nodes {} and class variable ARE conditionally independent given {} (using the transitions)",
                featureNode.getName(), nameFeatureVariablesSepSet);
        return true;
    }

    /**
     * Evaluates the null time to transition hypothesis between a feature node and the class node, given a separating
     * set. This method iteratively tests each state of the feature node against all combined states of the class
     * node and the separating set. The hypothesis test checks if the transition times of the feature node are
     * independent of the class node, given the states of the separating set.
     *
     * @param featureNode                feature node whose transition times are being tested
     * @param nameFeatureVariablesSepSet names of the feature variables in the separating set
     * @param suffStat                   sufficient statistics for the feature node without the class variable as a
     *                                   parent
     * @param qx                         matrix with the intensities of the feature node without the class variable
     * @param suffStatGivenClassVariable sufficient statistics for the feature node with the class variable as a parent
     * @param qxGivenClassVariable       matrix with the intensities of the feature node with the class variable as a
     *                                   parent
     * @return {@code true} if the null hypothesis is not rejected, meaning that the feature and class variables are
     * conditionally independent for the given significance level; {@code false} otherwise
     */
    protected static boolean testNullTimeToTransitionHypothesis(CIMNode featureNode,
                                                                List<String> nameFeatureVariablesSepSet,
                                                                CTBNSufficientStatistics suffStat, double[][] qx,
                                                                CTBNSufficientStatistics suffStatGivenClassVariable,
                                                                double[][] qxGivenClassVariable) {
        // Iterate over all possible states of the node
        for (int idxState = 0; idxState < featureNode.getNumStates(); idxState++) {
            // Iterate over all possible states of the subset and 'idxParent'
            for (int idxStateSepSetAndClassVariable = 0;
                 idxStateSepSetAndClassVariable < featureNode.getNumStatesParents(); idxStateSepSetAndClassVariable++) {
                // Set state of the parents of the node
                featureNode.setStateParents(idxStateSepSetAndClassVariable);
                // Extract state index of the conditioning set
                int idxStateSepSet = featureNode.getIdxStateParents(nameFeatureVariablesSepSet);
                // Perform hypothesis test
                boolean isNullTimeToTransitionHypAccepted = testNullTimeToTransitionHypGivenState(featureNode,
                        suffStat,
                        qx, suffStatGivenClassVariable, qxGivenClassVariable, idxState, idxStateSepSet,
                        idxStateSepSetAndClassVariable);
                if (!isNullTimeToTransitionHypAccepted) {
                    // Reject null hypothesis. Variables are not conditionally independent in terms of waiting time
                    logger.debug(
                            "Nodes {} and class variable ARE NOT conditionally independent given {} (using waiting " +
                                    "times)", featureNode.getName(), nameFeatureVariablesSepSet);
                    return false;
                }
            }
        }
        // Accept null hypothesis. Variables are conditionally independent in terms of waiting time
        logger.debug("Nodes {} and class variable ARE conditionally independent given {} (using waiting times)",
                featureNode.getName(), nameFeatureVariablesSepSet);
        return true;
    }

    /**
     * Adds a set of conditioning nodes as parents to a specified feature node.
     *
     * @param featureNode feature node to which the conditioning set will be added as parents
     * @param condSet     list of conditioning nodes to be added as parents to the feature node
     */
    private static void addCondSetAsParents(CIMNode featureNode, List<CIMNode> condSet) {
        if (condSet == null || condSet.isEmpty())
            return;
        for (CIMNode condFeatureNode : condSet)
            featureNode.setParent(condFeatureNode);
    }

    /**
     * Performs an F-test to compare the variances in transition intensities of a feature node with and without
     * a class node as a parent. This statistical test is used to determine if the inclusion of a class node
     * significantly alters the intensity matrix of the feature node, which would imply a dependency between
     * the feature node and the class node.
     *
     * @param qxs  intensity of the feature node without the class node as a parent
     * @param qxys intensity of the feature node with the class node as a parent
     * @param df1  degrees of freedom associated with qxys
     * @param df2  degrees of freedom associated with qxs
     * @return {@code true} if the null hypothesis is rejected, {@code false} otherwise
     */
    static boolean performFTest(double qxs, double qxys, double df1, double df2) {
        // Estimate F value
        double fValue = qxs / qxys;
        // Retrieve critical values from F distribution
        FDistribution fDistribution = new FDistribution(df1, df2);
        // Obtain upper and lower critical values for the given significance
        double upperCriticalValue = fDistribution.inverseCumulativeProbability(1 - sigTimeTransitionHyp / 2);
        double lowerCriticalValue = fDistribution.inverseCumulativeProbability(sigTimeTransitionHyp / 2);
        return fValue <= lowerCriticalValue || fValue >= upperCriticalValue;
    }

    /**
     * Tests the null state-to-state transition hypothesis for a given state of the feature variable, class variable
     * and feature variables in the separating set. This method evaluates whether the transitions between states of a
     * feature node are independent of a class node and conditioning set for specific states.
     *
     * @param node                           feature node being tested
     * @param nameFeaturesCondSet            names of the feature variables in the separating set
     * @param suffStat                       sufficient statistics for the feature node without the class variable
     * @param suffStatGivenClassVariable     sufficient statistics for the feature node with the class variable
     * @param idxStateNode                   index of the state of the feature node
     * @param idxStateSepSetAndClassVariable index of the studied state of the class variable and features in the
     *                                       separating set (if any)
     * @return {@code true} if the null hypothesis is not rejected, {@code false} otherwise
     */
    static boolean testNullStateToStateTransitionHypGivenState(CIMNode node, List<String> nameFeaturesCondSet,
                                                               CTBNSufficientStatistics suffStat,
                                                               CTBNSufficientStatistics suffStatGivenClassVariable,
                                                               int idxStateNode, int idxStateSepSetAndClassVariable) {
        // Set state of the parent of the node
        node.setStateParents(idxStateSepSetAndClassVariable);
        // Extract state index of the conditioning set
        int idxStateCondSet = node.getIdxStateParents(nameFeaturesCondSet);
        // Estimate degrees of freedom
        double mxxs = 0;
        double mxxys = 0;
        for (int idxToStateNode = 0; idxToStateNode < node.getNumStates(); idxToStateNode++) {
            mxxys += suffStatGivenClassVariable.getMxy()[idxStateSepSetAndClassVariable][idxStateNode][idxToStateNode];
            mxxs += suffStat.getMxy()[idxStateCondSet][idxStateNode][idxToStateNode];
        }
        double K = Math.sqrt((mxxs / mxxys));
        double L = 1 / K;
        int df = node.getNumStates() - 1;
        double chiSquaredValue = 0;
        for (int idxToStateNode = 0; idxToStateNode < node.getNumStates(); idxToStateNode++) {
            mxxys = suffStatGivenClassVariable.getMxy()[idxStateSepSetAndClassVariable][idxStateNode][idxToStateNode];
            mxxs = suffStat.getMxy()[idxStateCondSet][idxStateNode][idxToStateNode];
            chiSquaredValue += Math.pow(K * mxxys - L * mxxs, 2) / (mxxys + mxxs);
        }
        // Retrieve critical values from chi-squared distribution
        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(df);
        // Obtain critical value for the given significance
        double criticalValue = chiSquaredDistribution.inverseCumulativeProbability(1 - sigStateToStateTransitionHyp);
        // True if the null hypothesis is not rejected
        return chiSquaredValue < criticalValue;
    }

    /**
     * Evaluates the null time to transition hypothesis for a given state of the feature variable, class variable and
     * feature variables in the separating set. This method checks if the transition times of the feature node are
     * independent of the class node and the separating set for particular states.
     *
     * @param featureNode                    feature node being tested
     * @param suffStat                       sufficient statistics for the feature node without the class variable
     * @param qx                             matrix with the intensities of the feature node without the class variable
     * @param suffStatGivenClassVariable     sufficient statistics for the feature node with the class variable
     * @param qxGivenClassVariable           matrix with the intensities of the feature node with the class variable
     * @param idxState                       index of the state of the feature node
     * @param idxStateSepSetAndClassVariable index of the studied state of the class variable and features in the
     *                                       separating set (if any)
     * @return {@code true} if the null hypothesis is not rejected, {@code false} otherwise
     */
    static boolean testNullTimeToTransitionHypGivenState(CIMNode featureNode, CTBNSufficientStatistics suffStat,
                                                         double[][] qx,
                                                         CTBNSufficientStatistics suffStatGivenClassVariable,
                                                         double[][] qxGivenClassVariable, int idxState,
                                                         int idxStateSepSet, int idxStateSepSetAndClassVariable) {
        // Extract intensities exponential distribution
        // Feature variable without the class variable as parent
        double qxs = qx[idxStateSepSet][idxState];
        // Feature variable with the class variable as parent
        double qxys = qxGivenClassVariable[idxStateSepSetAndClassVariable][idxState];
        // Estimate degrees of freedom
        double df1 = 0;
        double df2 = 0;
        for (int idxToStateNode = 0; idxToStateNode < featureNode.getNumStates(); idxToStateNode++) {
            df1 += suffStatGivenClassVariable.getMxy()[idxStateSepSetAndClassVariable][idxState][idxToStateNode];
            df2 += suffStat.getMxy()[0][idxState][idxToStateNode];
        }
        return !performFTest(qxs, qxys, df1, df2);
    }

    /**
     * Returns a subset of feature nodes that are non-redundant given a conditioned feature node. This method
     * evaluates each feature node in a given list against a conditioned feature node to determine if it is redundant.
     * Redundancy is assessed based on the conditional independence of each feature node from the class node,
     * considering the conditioned feature node.
     *
     * @param evaluatedFeatureNodes  list of feature nodes to be evaluated for redundancy
     * @param classNode              class node used in the redundancy analysis
     * @param conditionedFeatureNode a feature node used as a condition in the redundancy analysis
     * @return list of non-redundant feature nodes
     */
    public List<CIMNode> redundancyAnalysis(List<CIMNode> evaluatedFeatureNodes, CPTNode classNode,
                                            CIMNode conditionedFeatureNode) {
        List nonRedundantFeatureNodes = new ArrayList(evaluatedFeatureNodes);
        for (CIMNode featureNode : evaluatedFeatureNodes) {
            boolean redundantFeature = redundancyAnalysis(featureNode, classNode, conditionedFeatureNode);
            if (redundantFeature) {
                logger.info("Feature variable {} is redundant", featureNode.getName());
                nonRedundantFeatureNodes.remove(featureNode);
            }
        }
        return nonRedundantFeatureNodes;
    }


    /**
     * Returns a subset of feature nodes that are non-redundant given the class node. This method performs redundancy
     * analysis on each feature node in the list, considering all possible separating sets up to a specified maximum
     * size, to determine if any feature is redundant in the context of the class node.
     *
     * @param evaluatedFeatureNodes list of feature nodes to be evaluated for redundancy
     * @param classNode             class node used in the redundancy analysis
     * @return list of non-redundant feature nodes
     */
    public List<CIMNode> redundancyAnalysis(List<CIMNode> evaluatedFeatureNodes, CPTNode classNode) {
        List<CIMNode> nonRedundantFeatureNodes = new ArrayList<>(evaluatedFeatureNodes);
        for (CIMNode featureNode : evaluatedFeatureNodes) {
            for (int sizeCondSet = 1; sizeCondSet <= maxSeparatingSizeRedundancyAnalysis && sizeCondSet <= nonRedundantFeatureNodes.size(); sizeCondSet++) {
                // Obtain separating sets of size "sizeCondSet" not including the feature variable is being analysed
                List<List<CIMNode>> condSets = Util.getSubsets(nonRedundantFeatureNodes, sizeCondSet, featureNode);
                boolean redundantFeature = false;
                if (condSets.size() > 0) {
                    redundantFeature = condSets.parallelStream().anyMatch(condSet -> {
                        return redundancyAnalysis(featureNode, classNode, condSet);
                    });
                }
                if (redundantFeature) {
                    logger.info("Feature variable {} is redundant", featureNode.getName());
                    nonRedundantFeatureNodes.remove(featureNode);
                    break;
                }
            }
        }
        return nonRedundantFeatureNodes;
    }

    /**
     * Determines if a feature node is redundant with respect to the class node, given another feature node. This
     * method checks the conditional independence of the evaluated feature node from the class node, considering the
     * given feature node as a conditioning variable.
     *
     * @param evaluatedFeatureNode feature node being evaluated for redundancy
     * @param classNode            class node used in the redundancy analysis
     * @param featureNode          feature node used as a conditioning variable in the analysis
     * @return {@code true} if the evaluated feature node is redundant, {@code false} otherwise
     */
    public boolean redundancyAnalysis(CIMNode evaluatedFeatureNode, CPTNode classNode, CIMNode featureNode) {
        return redundancyAnalysis(evaluatedFeatureNode, classNode, List.of(featureNode));
    }

    /**
     * Determines if a feature node is redundant with respect to a class node, given a set of other feature nodes.
     * This method checks the conditional independence of the evaluated feature node from the class node, considering
     * a set of feature nodes as conditioning variables.
     *
     * @param evaluatedFeatureNode feature node being evaluated for redundancy
     * @param classNode            class node used in the redundancy analysis
     * @param featureNodes         list of feature nodes used as conditioning variables in the analysis
     * @return {@code true} if the evaluated feature node is redundant, {@code false} otherwise
     */
    public boolean redundancyAnalysis(CIMNode evaluatedFeatureNode, CPTNode classNode, List<CIMNode> featureNodes) {
        String keyCache = getKeyCache(evaluatedFeatureNode.getName(), classNode.getName(), featureNodes);
        Boolean isFeatureRedundant = this.redundancyCache.get(keyCache);
        if (isFeatureRedundant == null) {
            isFeatureRedundant = conditionalIndependenceTest(evaluatedFeatureNode, classNode, featureNodes);
            redundancyCache.put(keyCache, isFeatureRedundant);
        }
        if (isFeatureRedundant) {
            logger.info("Feature variable {} is redundant for class node {} given feature variables {}...",
                    evaluatedFeatureNode.getName(), classNode.getName(),
                    featureNodes.stream().map(node -> node.getName()).collect(Collectors.toList()));
        }
        return isFeatureRedundant;
    }

    /**
     * Determines if a feature node is redundant with respect to a class node, given sets of features nodes with a
     * determine maximum size. This method evaluates the redundancy of the feature node by examining all
     * possible separating sets up to the specified maximum size.
     *
     * @param evaluatedFeatureNode            feature node being evaluated for redundancy
     * @param classNode                       class node used in the redundancy analysis
     * @param featureNodes                    list of feature nodes used as conditioning variables
     * @param maxSizeSepSetRedundancyAnalysis maximum size of separating sets to consider in the analysis
     * @return {@code true} if the evaluated feature node is redundant, {@code false} otherwise
     */
    public boolean redundancyAnalysis(CIMNode evaluatedFeatureNode, CPTNode classNode, List<CIMNode> featureNodes,
                                      int maxSizeSepSetRedundancyAnalysis) {
        logger.info("Redundancy analysis on feature variable {} for class variable {} given feature variables {}...",
                evaluatedFeatureNode.getName(), classNode.getName(),
                featureNodes.stream().map(node -> node.getName()).collect(Collectors.toList()));
        // If there are no other feature variables, the new feature cannot be redundant
        boolean redundantFeature = featureNodes.size() > 0;
        for (int sizeCondSet = 1; sizeCondSet <= featureNodes.size() && sizeCondSet <= maxSizeSepSetRedundancyAnalysis;
             sizeCondSet++) {
            // Obtain separating sets of size "sizeCondSet" not including the feature variable is being analysed
            List<List<CIMNode>> condSets = Util.getSubsets(featureNodes, sizeCondSet, null);
            redundantFeature = condSets.parallelStream().anyMatch(condSet -> {
                return redundancyAnalysis(evaluatedFeatureNode, classNode, condSet);
            });
        }
        return redundantFeature;
    }

    /**
     * Sets the dataset to be used in the feature subset selection.
     *
     * @param newDataset dataset to be used in the feature selection
     */
    public void setDataset(Dataset newDataset) {
        dataset = newDataset;
    }

    /**
     * Generates a unique key for caching based on the names of a node and its conditioning set.
     * This key is used to cache the results of redundancy analyses to avoid recomputing them.
     *
     * @param nameNode1 name of the first node
     * @param nameNode2 name of the second node
     * @param sepNodes  list of separating nodes
     * @return a unique string key for caching purposes
     */
    private String getKeyCache(String nameNode1, String nameNode2, List<CIMNode> sepNodes) {
        // Indexes of the feature variable evolve along the feature stream. Thus, they should not be used as a key
        String key = nameNode1 + nameNode2;
        if (sepNodes != null) {
            List<String> nameSepNodes = sepNodes.stream().map(node -> node.getName()).collect(Collectors.toList());
            key += String.join("", nameSepNodes);
        }
        return key;
    }

}