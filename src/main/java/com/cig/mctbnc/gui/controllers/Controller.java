package com.cig.mctbnc.gui.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.BNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbing;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {

	public Stage stage;
	
	// Dataset
	public Button btnFolder;
	public Label lblFolderPath;
	
	// Model
	public ComboBox cmbModel;
	
	
	// ----------------------- AVAILABLE ALGORITHMS -----------------------
	Map<String, BNParameterLearningAlgorithm> parameterLearningBN = new HashMap<String, BNParameterLearningAlgorithm>() {
		{
			put("MLE", new BNMaximumLikelihoodEstimation());
		}
	};

	Map<String, CTBNParameterLearningAlgorithm> parameterLearningCTBN = new HashMap<String, CTBNParameterLearningAlgorithm>() {
		{
			put("MLE", new CTBNMaximumLikelihoodEstimation()); // Maximum likelihood estimation
			put("BE", new CTBNBayesianEstimation()); // Bayesian estimation
		}
	};

	Map<String, BNStructureLearningAlgorithm> structureLearningBN = new HashMap<String, BNStructureLearningAlgorithm>() {
		{
			put("HillClimbing", new BNHillClimbing());
		}
	};

	Map<String, CTBNStructureLearningAlgorithm> structureLearningCTBN = new HashMap<String, CTBNStructureLearningAlgorithm>() {
		{
			put("HillClimbing", new CTBNHillClimbing());
		}
	};

	public void setStage(Stage stage) {
		this.stage = stage;

	}

	public void setFolderDataset() {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);
		// Show the selected path
		lblFolderPath.setText(selectedDirectory.getAbsolutePath());
	}

}
