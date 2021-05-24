package es.upm.fi.cig.mctbnc.classification;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.models.submodels.DAG_maxK_MCTBNC;
import es.upm.fi.cig.mctbnc.models.submodels.Empty_digraph_MCTBNC;
import es.upm.fi.cig.mctbnc.models.submodels.Empty_maxK_MCTBNC;
import es.upm.fi.cig.mctbnc.models.submodels.MCTNBC;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Provides static methods for the creation of classifiers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ClassifierFactory {
	static Logger logger = LogManager.getLogger(ClassifierFactory.class);

	/**
	 * Builds the specified classifier with the provided hyperparameters.
	 * 
	 * @param <NodeTypeBN>     type of the nodes of the Bayesian network
	 * @param <NodeTypeCTBN>   type of the nodes of the continuous time Bayesian
	 *                         network
	 * @param nameClassifier   name of the classifier to build
	 * @param bnLearningAlgs   parameter and structure learning algorithms for
	 *                         Bayesian networks
	 * @param ctbnLearningAlgs parameter and structure learning algorithms for
	 *                         continuous time Bayesian networks
	 * @param hyperparameters  specific hyperparameters for each type of classifier
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous time Bayesian network node type
	 * @return a {@code MCTBNC}
	 */
	public static <NodeTypeBN extends Node, NodeTypeCTBN extends Node> MCTBNC<NodeTypeBN, NodeTypeCTBN> getMCTBNC(
			String nameClassifier, BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Map<String, String> hyperparameters, Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		switch (nameClassifier) {
		case "MCTNBC":
			logger.info("Learning a multi-dimensional continuous naive Bayes classifier (MCTBNC)");
			return new MCTNBC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		case "DAG-maxK MCTBNC":
			int maxK = extractMaxK(hyperparameters);
			logger.info("Learning a DAG-max{} multi-dimensional continuous Bayesian network classifier", maxK);
			return new DAG_maxK_MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass,
					ctbnNodeClass);
		case "Empty-maxK MCTBNC":
			maxK = extractMaxK(hyperparameters);
			logger.info("Learning an empty-max{} multi-dimensional continuous Bayesian network classifier", maxK);
			return new Empty_maxK_MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass,
					ctbnNodeClass);
		case "Empty-digraph MCTBNC":
			logger.info("Learning an empty-digraph multi-dimensional continuous Bayesian network classifier");
			return new Empty_digraph_MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass,
					ctbnNodeClass);
		default:
			// If the specified classifier is not found, a MCTBNC is created
			logger.info("Learning a multi-dimensional continuous time Bayesian network classifier (MCTBNC)");
			return new MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		}
	}

	/**
	 * Returns a list with the currently available classifiers.
	 * 
	 * @return list of available classifiers
	 */
	public static List<String> getAvailableModels() {
		return List.of("MCTBNC", "MCTNBC", "DAG-maxK MCTBNC", "Empty-digraph MCTBNC", "Empty-maxK MCTBNC");
	}

	/**
	 * Extracts the maximum number of parents of the feature variables. The value 2
	 * is returned if an erroneous number is provided.
	 * 
	 * @param hyperparameters a {@code Map} containing a key "maxK" with the maximum
	 *                        number of parents
	 * @return maximum number of parents of the feature variables.
	 */
	private static int extractMaxK(Map<String, String> hyperparameters) {
		try {
			return Integer.valueOf(hyperparameters.getOrDefault("maxK", "2"));
		} catch (NumberFormatException e) {
			logger.warn("Illegal maximum number of parents. The maximum number of parents must be 1 or more");
			return 2;
		}
	}

}