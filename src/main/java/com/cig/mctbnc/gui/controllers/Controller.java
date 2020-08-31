package com.cig.mctbnc.gui.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.controlsfx.control.CheckComboBox;

import com.cig.mctbnc.classification.ClassifierFactory;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.SeparateCSVReader;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.BNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.HillClimbing;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.performance.CrossValidation;
import com.cig.mctbnc.performance.HoldOut;
import com.cig.mctbnc.performance.ValidationMethod;
import com.cig.mctbnc.util.ControllerUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {
	// Objects MCTBNC
	DatasetReader datasetReader;

	// Controllers javafx
	private Stage stage;

	// Dataset
	@FXML
	private TextField fldPath;
	@FXML
	private CheckComboBox<String> ckcmbVariables;
	@FXML
	private CheckComboBox<String> ckcmbClassVariables;
	@FXML
	private ComboBox<String> cmbTimeVariable;

	// Model
	@FXML
	private ComboBox<String> cmbModel;
	@FXML
	private ComboBox<String> cmbParameterBN;
	@FXML
	private ComboBox<String> cmbParameterCTBN;
	@FXML
	private ComboBox<String> cmbStructure;
	@FXML
	private ComboBox<String> cmbPenalization;
	@FXML
	private TextField fldKParents;

	// Evaluation
	@FXML
	ToggleGroup validationMethod;
	@FXML
	private CheckBox cbShuffle;
	@FXML
	private Slider sldTrainingSize;
	@FXML
	private TextField fldNumFolds;

	// General controls
	@FXML
	private Button btnEvaluate;
	@FXML
	private ProgressIndicator progressIndicator;

	// ----------------------- AVAILABLE Models -----------------------
	List<String> models = List.of("MCTBNC", "MCTNBC", "KMCTBNC");
	List<String> parameterLearningAlgs = List.of("Maximum likelihood estimation", "Bayesian estimation");
	List<String> structureLearningAlgs = List.of("Hill climbing");
	List<String> penalizations = List.of("No", "BIC", "AIC");

	// ----------------------- AVAILABLE ALGORITHMS -----------------------
	Map<String, BNParameterLearningAlgorithm> parameterLearningBN = new HashMap<String, BNParameterLearningAlgorithm>() {
		{
			put("Maximum likelihood estimation", new BNMaximumLikelihoodEstimation());
		}
	};

	Map<String, CTBNParameterLearningAlgorithm> parameterLearningCTBN = new HashMap<String, CTBNParameterLearningAlgorithm>() {
		{
			put("Maximum likelihood estimation", new CTBNMaximumLikelihoodEstimation()); // Maximum likelihood
																							// estimation
			put("Bayesian estimation", new CTBNBayesianEstimation()); // Bayesian estimation
		}
	};

	Map<String, BNStructureLearningAlgorithm> structureLearningBN = new HashMap<String, BNStructureLearningAlgorithm>() {
		{
			put("Hill climbing", new BNHillClimbing());
		}
	};

	Map<String, CTBNStructureLearningAlgorithm> structureLearningCTBN = new HashMap<String, CTBNStructureLearningAlgorithm>() {
		{
			put("Hill climbing", new CTBNHillClimbing());
		}
	};

	/**
	 * Initialize the controller.
	 */
	@FXML
	public void initialize() {
		initializeDatasetPane();
		initializeModelPane();
		initializeEvaluationPane();
	}

	private void initializeDatasetPane() {

	}

	private void initializeModelPane() {
		// Initialize options of comboBoxes
		cmbModel.getItems().addAll(models);
		cmbParameterBN.getItems().addAll(parameterLearningAlgs);
		cmbParameterCTBN.getItems().addAll(parameterLearningAlgs);
		cmbStructure.getItems().addAll(structureLearningAlgs);
		cmbPenalization.getItems().addAll(penalizations);
		// Select first option as default in comboBoxes
		cmbModel.getSelectionModel().selectFirst();
		cmbParameterBN.getSelectionModel().selectFirst();
		cmbParameterCTBN.getSelectionModel().selectFirst();
		cmbStructure.getSelectionModel().selectFirst();
		cmbPenalization.getSelectionModel().selectFirst();
		// Initialize text fields with default values
		fldKParents.setText("2");
		// Text values are restricted to certain values
		ControllerUtil.onlyPositiveInteger(fldKParents);
	}

	private void initializeEvaluationPane() {
		// Initialize text fields with default values
		sldTrainingSize.setValue(0.7);
		fldNumFolds.setText("5");
		// Text values are restricted to certain values
		ControllerUtil.onlyPositiveIntegerGreaterThan(fldNumFolds, 2);
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setFolderDataset() throws FileNotFoundException {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);
		// Show the selected path
		String pathFolder = selectedDirectory.getAbsolutePath();
		System.out.println(pathFolder);
		fldPath.setText(pathFolder);
		// Read the variables
		readVariablesDataset(pathFolder);
	}

	private void readVariablesDataset(String pathFolder) throws FileNotFoundException {
		datasetReader = new SeparateCSVReader(pathFolder);
		List<String> nameVariables = datasetReader.getAllVariablesDataset();
		ckcmbVariables.getItems().addAll(nameVariables);
		cmbTimeVariable.getItems().addAll(nameVariables);
		ckcmbClassVariables.getItems().addAll(nameVariables);
	}

	public void evaluate() {
		// CHECK THAT IT IS POSSIBLE TO LEARN A MODEL WITH THE GIVEN INFORMATION

		progressIndicator.setVisible(true);
		progressIndicator.setManaged(true);
		// Get selected variables
		String nameTimeVariable = cmbTimeVariable.getValue();
		List<String> nameClassVariables = ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameSelectedVariables = ckcmbVariables.getCheckModel().getCheckedItems();
		// Obtain variables that should be ignored
		List<String> nameExcludesVariables = new ArrayList<String>(datasetReader.getAllVariablesDataset());
		nameExcludesVariables.remove(nameTimeVariable);
		nameExcludesVariables.removeAll(nameClassVariables);
		nameExcludesVariables.removeAll(nameSelectedVariables);
		// Set the variables that will be used
		datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameExcludesVariables);

		// Get selected options for validation
		RadioButton rbValidationMethod = (RadioButton) validationMethod.getSelectedToggle();
		String selectedValidationMethod = rbValidationMethod.getText();

		// Define model
		MCTBNC<CPTNode, CIMNode> model = defineModel();

		// Define validation method
		ValidationMethod validationMethod = defineValidationMethod(selectedValidationMethod);

		// Evaluate the performance of the model
		validationMethod.evaluate(model);
		progressIndicator.setVisible(false);
		progressIndicator.setManaged(false);
	}

	/**
	 * Evaluate if it is possible to train a model with the selected options. For
	 * example, it could be forgotten the selection of the dataset.
	 * 
	 * @return
	 */
	private boolean checkOptions() {
		return false;
	}

	private MCTBNC<CPTNode, CIMNode> defineModel() {

		// ------------------------ RETRIVE LEARNING ALGORITHMS -----------------------
		BNLearningAlgorithms bnLearningAlgs = defineAlgorithmsBN();
		CTBNLearningAlgorithms ctbnLearningAlgs = defineAlgorithmsCTBN();

		// Parameters that could be necessary for the generation of the model
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("maxK", fldKParents.getText());

		// Generate selected model
		String selectedModel = cmbModel.getValue();
		MCTBNC<CPTNode, CIMNode> model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel, bnLearningAlgs,
				ctbnLearningAlgs, parameters, CPTNode.class, CIMNode.class);

		// Define penalization function (if any)
		String penalizationFunction = cmbPenalization.getValue();
		model.setPenalizationFunction(penalizationFunction);

		return model;
	}

	private BNLearningAlgorithms defineAlgorithmsBN() {
		// Get names learning algorithms
		String parameterLearningAlgBN = cmbParameterBN.getValue();
		String structureLearningAlg = cmbStructure.getValue();

		// Define learning algorithms for the class subgraph (Bayesian network)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = parameterLearningBN.get(parameterLearningAlgBN);
		BNStructureLearningAlgorithm bnStructureLearningAlgorithm = structureLearningBN.get(structureLearningAlg);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm);

		return bnLearningAlgs;
	}

	private CTBNLearningAlgorithms defineAlgorithmsCTBN() {
		// Get names learning algorithms
		String parameterLearningAlgCTBN = cmbParameterCTBN.getValue();
		String structureLearningAlg = cmbStructure.getValue();

		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm = parameterLearningCTBN
				.get(parameterLearningAlgCTBN); // new
												// CTBNBayesianEstimation(1, // //
												// parameterLearningCTBN.get("MLE");
		CTBNStructureLearningAlgorithm ctbnStructureLearningAlgorithm = structureLearningCTBN.get(structureLearningAlg);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm);

		return ctbnLearningAlgs;
	}

	/**
	 * Define the validation method.
	 * 
	 * @param selectedValidationMethod
	 * @return
	 */
	private ValidationMethod defineValidationMethod(String selectedValidationMethod) {
		ValidationMethod validationMethod;
		boolean shuffleSequences = cbShuffle.isSelected();
		switch (selectedValidationMethod) {
		case "Cross-validation":
			int folds = Integer.valueOf(fldNumFolds.getText());
			validationMethod = new CrossValidation(datasetReader, folds, shuffleSequences);
		default:
			// Hold-out validation
			double trainingSize = sldTrainingSize.getValue();
			validationMethod = new HoldOut(datasetReader, trainingSize, shuffleSequences);
		}
		return validationMethod;
	}

}
