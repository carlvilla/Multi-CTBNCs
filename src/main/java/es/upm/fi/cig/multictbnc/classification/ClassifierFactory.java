package es.upm.fi.cig.multictbnc.classification;

import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.DAG_maxK_MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.Empty_digraph_MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.Empty_maxK_MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.MultiCTNBC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Provides static methods for the creation of classifiers.
 *
 * @author Carlos Villa Blanco
 */
public class ClassifierFactory {
    private static final Logger logger = LogManager.getLogger(ClassifierFactory.class);

    /**
     * Returns a list with the currently available classifiers.
     *
     * @return list of available classifiers
     */
    public static List<String> getAvailableModels() {
        return List.of("Multi-CTBNC", "Multi-CTNBC", "DAG-maxK Multi-CTBNC", "Empty-digraph Multi-CTBNC",
                "Empty-maxK Multi-CTBNC");
    }

    /**
     * Builds the specified classifier. The node type for the class variables is assumed to be a {@code CPTNode}, while for a feature variable a {@code CIMNode}.
     *
     * @param nameClassifier   name of the classifier to build
     * @param bnLearningAlgs   parameter and structure learning algorithms for Bayesian networks
     * @param ctbnLearningAlgs parameter and structure learning algorithms for continuous-time Bayesian networks
     * @return a {@code MultiCTBNC}
     */
    public static MultiCTBNC getMultiCTBNC(
            String nameClassifier, BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs) {
        return getMultiCTBNC(nameClassifier, bnLearningAlgs, ctbnLearningAlgs, null, CPTNode.class, CIMNode.class);
    }

    /**
     * Builds the specified classifier with the provided hyperparameters. The node type for the class variables is assumed to be a {@code CPTNode}, while for a feature variable a {@code CIMNode}.
     *
     * @param nameClassifier   name of the classifier to build
     * @param bnLearningAlgs   parameter and structure learning algorithms for Bayesian networks
     * @param ctbnLearningAlgs parameter and structure learning algorithms for continuous-time Bayesian networks
     * @param hyperparameters  specific hyperparameters for each type of classifier
     * @return a {@code MultiCTBNC}
     */
    public static MultiCTBNC getMultiCTBNC(
            String nameClassifier, BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
            Map<String, String> hyperparameters) {
        return getMultiCTBNC(nameClassifier, bnLearningAlgs, ctbnLearningAlgs, hyperparameters, CPTNode.class, CIMNode.class);
    }

    /**
     * Builds the specified classifier with the provided hyperparameters.
     *
     * @param <NodeTypeBN>     type of the nodes of the Bayesian network
     * @param <NodeTypeCTBN>   type of the nodes of the continuous-time Bayesian network
     * @param nameClassifier   name of the classifier to build
     * @param bnLearningAlgs   parameter and structure learning algorithms for Bayesian networks
     * @param ctbnLearningAlgs parameter and structure learning algorithms for continuous-time Bayesian networks
     * @param hyperparameters  specific hyperparameters for each type of classifier
     * @param bnNodeClass      Bayesian network node type
     * @param ctbnNodeClass    continuous-time Bayesian network node type
     * @return a {@code MultiCTBNC}
     */
    public static <NodeTypeBN extends Node, NodeTypeCTBN extends Node> MultiCTBNC<NodeTypeBN, NodeTypeCTBN> getMultiCTBNC(
            String nameClassifier, BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
            Map<String, String> hyperparameters, Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
        switch (nameClassifier) {
            case "Multi-CTNBC":
                logger.info("Learning a multi-dimensional continuous naive Bayes classifier (Multi-CTNBC)");
                return new MultiCTNBC<>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
            case "DAG-maxK Multi-CTBNC":
                int maxK = extractMaxK(hyperparameters);
                logger.info("Learning a DAG-max{} multi-dimensional continuous Bayesian network classifier", maxK);
                return new DAG_maxK_MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass, ctbnNodeClass);
            case "Empty-maxK Multi-CTBNC":
                maxK = extractMaxK(hyperparameters);
                logger.info("Learning an empty-max{} multi-dimensional continuous Bayesian network classifier", maxK);
                return new Empty_maxK_MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass, ctbnNodeClass);
            case "Empty-digraph Multi-CTBNC":
                logger.info("Learning an empty-digraph multi-dimensional continuous Bayesian network classifier");
                return new Empty_digraph_MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
            case "Multi-CTBNC":
                logger.info("Learning a multi-dimensional continuous-time Bayesian network classifier");
                return new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
            default:
                logger.warn("The specified classifier {} was not found. Learning a multi-dimensional continuous-time Bayesian network classifier (Multi-CTBNC)", nameClassifier);
                return new MultiCTBNC<>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
        }
    }

    /**
     * Generates a {@code Multi-CTBNC} including some default nodes and algorithms for the learning of its parameters
     * and structure. This method was thought to easily generate the classifier.
     *
     * @return a {@code Multi-CTBNC}
     */
    public static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNC() {
        // Algorithms to learn BN (class subgraph of Multi-CTBNC)
        BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
                "Bayesian estimation", 1.0);
        Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "BIC");
        StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
                "Hill climbing", paramSLA);
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm,
                bnStructureLearningAlgorithm);
        // Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
        CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
                CTBNParameterLearningAlgorithmFactory.getAlgorithm(
                        "Bayesian estimation", 1.0, 0.001);
        StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
                "Hill climbing", paramSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
                ctbnStructureLearningAlgorithm);
        return new MultiCTBNC<CPTNode, CIMNode>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class, CIMNode.class);
    }

    /**
     * Generates a {@code Multi-CTBNC} including some default nodes and algorithms for the learning of its parameters
     * and structure. This method was thought to easily generate the classifier. The algorithms could be later
     * modifier.
     *
     * @return a {@code Multi-CTBNC}
     */
    public static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithCTPC() {
        // Algorithms to learn BN (class subgraph of Multi-CTBNC)
        BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
                "Bayesian estimation", 1.0);
        Map<String, String> paramSLA = Map.of("significancePC", "0.05", "sigTimeTransitionHyp", "0.00001",
                "sigStateToStateTransitionHyp", "0.00001");
        StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
                "CTPC", paramSLA);
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm,
                bnStructureLearningAlgorithm);
        // Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
        CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
                CTBNParameterLearningAlgorithmFactory.getAlgorithm(
                        "Bayesian estimation", 1.0, 0.001);
        StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
                "CTPC", paramSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
                ctbnStructureLearningAlgorithm);
        return new MultiCTBNC<CPTNode, CIMNode>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class, CIMNode.class);
    }

    /**
     * Generates a {@code Multi-CTBNC} including some default nodes and algorithms for the learning of its parameters
     * and structure. This method was thought to easily generate the classifier. The algorithms could be later
     * modifier.
     *
     * @return a {@code Multi-CTBNC}
     */
    public static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithHybridAlgorithm() {
        // Algorithms to learn BN (class subgraph of Multi-CTBNC)
        BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
                "Bayesian estimation", 1.0);
        Map<String, String> paramSLA = Map.of("significancePC", "0.05", "sigTimeTransitionHyp", "0.00001",
                "sigStateToStateTransitionHyp", "0.00001", "maxSizeSepSet", "2", "scoreFunction", "Log-likelihood",
                "penalisationFunction", "BIC");
        StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
                "Hybrid algorithm", paramSLA);
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm,
                bnStructureLearningAlgorithm);
        // Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
        CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
                CTBNParameterLearningAlgorithmFactory.getAlgorithm(
                        "Bayesian estimation", 1.0, 0.001);
        StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
                "Hybrid algorithm", paramSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
                ctbnStructureLearningAlgorithm);
        return new MultiCTBNC<CPTNode, CIMNode>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class, CIMNode.class);
    }

    /**
     * Generates a {@code Multi-CTBNC} including some default nodes and algorithms for the learning of its parameters
     * and structure. This method was thought to easily generate the classifier. The algorithms could be later
     * modifier.
     *
     * @return a {@code Multi-CTBNC}
     */
    public static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithMBCTPC() {
        // Algorithms to learn BN (class subgraph of Multi-CTBNC)
        BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory.getAlgorithm(
                "Bayesian estimation", 1.0);
        Map<String, String> paramSLA = Map.of("significancePC", "0.05", "sigTimeTransitionHyp", "0.00001",
                "sigStateToStateTransitionHyp", "0.00001");
        StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmBN(
                "MB-CTPC", paramSLA);
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm,
                bnStructureLearningAlgorithm);
        // Algorithms to learn CTBN (feature and bridge subgraph of Multi-CTBNC)
        CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm =
                CTBNParameterLearningAlgorithmFactory.getAlgorithm(
                        "Bayesian estimation", 1.0, 0.001);
        StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
                "MB-CTPC", paramSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
                ctbnStructureLearningAlgorithm);
        return new MultiCTBNC<CPTNode, CIMNode>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class, CIMNode.class);
    }

    /**
     * Extracts the maximum number of parents of the feature variables. The value 2 is returned if an incorrect number
     * is provided.
     *
     * @param hyperparameters a {@code Map} containing a key "maxK" with the maximum number of parents
     * @return maximum number of parents of the feature variables.
     */
    private static int extractMaxK(Map<String, String> hyperparameters) {
        try {
            return Integer.valueOf(hyperparameters.getOrDefault("maxK", "2"));
        } catch (NumberFormatException nfe) {
            logger.warn("Illegal maximum number of parents. The maximum number of parents must be one or more");
            return 2;
        }
    }

}