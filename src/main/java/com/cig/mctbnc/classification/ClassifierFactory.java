package com.cig.mctbnc.classification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.models.MCTNBC;
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
	 * @param <NodeTypeBN>
	 * @param <NodeTypeCTBN>
	 * @param nameClassifier
	 * @param dataset
	 * @param ctbnParameterLearningAlgorithm
	 * @param ctbnStructureLearningAlgorithm
	 * @param bnParameterLearningAlgorithm
	 * @param bnStructureLearningAlgorithm
	 * @param bnNodeClass
	 * @param ctbnNodeClass
	 * @return classifier
	 */
	public static <NodeTypeBN extends Node, NodeTypeCTBN extends Node> MCTBNC getMCTBNC(String nameClassifier,
			ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbnStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm, Class<NodeTypeBN> bnNodeClass,
			Class<NodeTypeCTBN> ctbnNodeClass) {
		if (nameClassifier.equals("MCTNBC")) {
			logger.info("Creating a multi-dimensional continuous naive Bayes classifier");
			return new MCTNBC<NodeTypeBN, NodeTypeCTBN>(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm,
					bnParameterLearningAlgorithm, bnStructureLearningAlgorithm, bnNodeClass, ctbnNodeClass);
		} else {
			// If the specified classifier is not found, a MCTBNC is created
			logger.info("Creating a multi-dimensional continuous time Bayesian network classifier");
			return new MCTBNC<NodeTypeBN, NodeTypeCTBN>(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm,
					bnParameterLearningAlgorithm, bnStructureLearningAlgorithm, bnNodeClass, ctbnNodeClass);
		}

	}

}
