package es.upm.fi.cig.multictbnc.classification;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.DAG_maxK_MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.Empty_digraph_MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.Empty_maxK_MultiCTBNC;
import es.upm.fi.cig.multictbnc.models.submodels.MultiCTNBC;
import es.upm.fi.cig.multictbnc.nodes.Node;

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
	 * @return a {@code MultiCTBNC}
	 */
	public static <NodeTypeBN extends Node, NodeTypeCTBN extends Node> MultiCTBNC<NodeTypeBN, NodeTypeCTBN> getMultiCTBNC(
			String nameClassifier, BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Map<String, String> hyperparameters, Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		switch (nameClassifier) {
		case "Multi-CTNBC":
			logger.info("Learning a multi-dimensional continuous naive Bayes classifier (Multi-CTNBC)");
			return new MultiCTNBC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		case "DAG-maxK Multi-CTBNC":
			int maxK = extractMaxK(hyperparameters);
			logger.info("Learning a DAG-max{} multi-dimensional continuous Bayesian network classifier", maxK);
			return new DAG_maxK_MultiCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass,
					ctbnNodeClass);
		case "Empty-maxK Multi-CTBNC":
			maxK = extractMaxK(hyperparameters);
			logger.info("Learning an empty-max{} multi-dimensional continuous Bayesian network classifier", maxK);
			return new Empty_maxK_MultiCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass,
					ctbnNodeClass);
		case "Empty-digraph Multi-CTBNC":
			logger.info("Learning an empty-digraph multi-dimensional continuous Bayesian network classifier");
			return new Empty_digraph_MultiCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass,
					ctbnNodeClass);
		default:
			// If the specified classifier is not found, a Multi-CTBNC is created
			logger.info("Learning a multi-dimensional continuous time Bayesian network classifier (Multi-CTBNC)");
			return new MultiCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		}
	}

	/**
	 * Returns a list with the currently available classifiers.
	 * 
	 * @return list of available classifiers
	 */
	public static List<String> getAvailableModels() {
		return List.of("Multi-CTBNC", "Multi-CTNBC", "DAG-maxK Multi-CTBNC", "Empty-digraph Multi-CTBNC", "Empty-maxK Multi-CTBNC");
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
