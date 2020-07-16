package com.cig.mctbnc.learning.structure;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;

public class CTBNCStructureHillClimbing implements StructureLearningAlgorithm {

	List<Node> classVariablesNodes;
	List<Node> featureNodes;
	static Logger logger = LogManager.getLogger(CTBNCStructureHillClimbing.class);

	@Override
	public void learn(PGM pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning) {
		logger.info("Learning continuous time Bayesian network classifier using Hill Climbing");

	}

}