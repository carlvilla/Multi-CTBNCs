package com.cig.mctbnc.classification;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.models.submodels.DAG_MaxK_MCTBNC;
import com.cig.mctbnc.models.submodels.EMPTY_MaxK_MCTBNC;
import com.cig.mctbnc.models.submodels.MCTNBC;
import com.cig.mctbnc.nodes.Node;

/**
 * Creates the specified classifiers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ClassifierFactory {
	static Logger logger = LogManager.getLogger(ClassifierFactory.class);

	/**
	 * Build the specified classifier with the provided parameters.
	 * 
	 * @param <NodeTypeBN>
	 * @param <NodeTypeCTBN>
	 * @param nameClassifier
	 * @param bnLearningAlgs
	 * @param ctbnLearningAlgs
	 * @param parameters       special parameters for each type of classifier
	 * @param bnNodeClass
	 * @param ctbnNodeClass
	 * @return classifier
	 */
	public static <NodeTypeBN extends Node, NodeTypeCTBN extends Node> MCTBNC<NodeTypeBN, NodeTypeCTBN> getMCTBNC(
			String nameClassifier, BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Map<String, String> parameters, Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		switch (nameClassifier) {
		case "MCTNBC":
			logger.info("Creating a multi-dimensional continuous naive Bayes classifier (MCTBNC)");
			return new MCTNBC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		case "DAG-kDB MCTBNC":
			int maxK = Integer.valueOf(parameters.get("maxK"));
			logger.info("Creating a DAG-{}DB multi-dimensional continuous Bayesian network classifier", maxK);
			return new DAG_MaxK_MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass,
					ctbnNodeClass);
		case "Empty-kDB MCTBNC":
			maxK = Integer.valueOf(parameters.get("maxK"));
			logger.info("Creating a Empty-{}DB multi-dimensional continuous Bayesian network classifier", maxK);
			return new EMPTY_MaxK_MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, maxK, bnNodeClass,
					ctbnNodeClass);
		default:
			// If the specified classifier is not found, a MCTBNC is created
			logger.info("Creating a multi-dimensional continuous time Bayesian network classifier (MCTBNC)");
			return new MCTBNC<NodeTypeBN, NodeTypeCTBN>(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		}

	}

	/**
	 * Return a list with the currently available classifiers.
	 * 
	 * @return list of available classifiers
	 */
	public static List<String> getAvailableModels() {
		return List.of("MCTBNC", "MCTNBC", "DAG-kDB MCTBNC", "Empty-kDB MCTBNC");
	}

}
