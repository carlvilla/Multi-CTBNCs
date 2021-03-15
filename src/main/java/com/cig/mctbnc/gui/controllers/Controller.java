package com.cig.mctbnc.gui.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;

import com.cig.mctbnc.classification.ClassifierFactory;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.DatasetReaderFactory;
import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithmFactory;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.performance.ValidationMethod;
import com.cig.mctbnc.performance.ValidationMethodFactory;
import com.cig.mctbnc.util.ControllerUtil;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Controller used to initialize the elements of the GUI and allow the
 * interaction between the logic of the application and the GUI.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Controller {
	// Objects MCTBNC
	DatasetReader datasetReader;

	// Controllers javafx
	private Stage stage;

	// Dataset
	@FXML
	private ComboBox<String> cmbDataFormat;
	@FXML
	private ComboBox<String> cmbStrategy;
	@FXML
	private TextField fldSizeSequences;
	@FXML
	private TextField fldPath;
	@FXML
	private ComboBox<String> cmbTimeVariable;
	@FXML
	private CheckComboBox<String> ckcmbClassVariables;
	@FXML
	private CheckComboBox<String> ckcmbFeatures;

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
	private ComboBox<String> cmbInitialStructure;
	@FXML
	private ComboBox<String> cmbScoreFunction;
	@FXML
	private ComboBox<String> cmbPenalization;
	@FXML
	private TextField fldKParents;
	@FXML
	private TextField fldNxBN;
	@FXML
	private TextField fldMxy;
	@FXML
	private TextField fldTx;
	@FXML
	private CheckBox chkProbabilities;

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
	private Label status;

	// -------------------- AVAILABLE MODELS --------------------
	List<String> models = ClassifierFactory.getAvailableModels();
	// ----------------------- AVAILABLE ALGORITHMS -----------------------
	List<String> parameterLearningAlgs = List.of("Maximum likelihood estimation", "Bayesian estimation");
	List<String> structureLearningAlgs = StructureLearningAlgorithmFactory.getAvailableOptimizationMethods();
	List<String> initialStructures = List.of("Empty", "Naive Bayes");
	List<String> scores = List.of("Log-likelihood", "Conditional log-likelihood", "Bayesian Dirichlet equivalent");
	List<String> penalizations = List.of("No", "BIC", "AIC");
	// -------------------- AVAILABLE DATASET READERS --------------------
	List<String> datasetReaders = DatasetReaderFactory.getAvailableDatasetReaders();
	List<String> datasetReaderStrategies = DatasetReaderFactory.getAvailableStrategies();

	Logger logger = LogManager.getLogger(Controller.class);

	/**
	 * Initialize the controller.
	 */
	@FXML
	public void initialize() {
		initializeDatasetPane();
		initializeModelPane();
		initializeEvaluationPane();
	}

	/**
	 * Establish the stage used by the application to show dialogs.
	 * 
	 * @param stage
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Open a dialog to select the folder where the dataset is located.
	 * 
	 * @throws FileNotFoundException
	 */
	public void setFolderDataset() throws FileNotFoundException {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);
		if (selectedDirectory != null) {
			// Show the selected directory
			String pathFolder = selectedDirectory.getAbsolutePath();
			fldPath.setText(pathFolder);
			// Define dataset reader
			initializeDatasetReader(pathFolder);
			// Read the variables
			readVariablesDataset(pathFolder);
		}
	}

	/**
	 * Evaluate the selected model.
	 */
	public void evaluate() {
		// TODO CHECK THAT IT IS POSSIBLE TO LEARN A MODEL WITH THE GIVEN INFORMATION
		checkValidOptions();
		// Get selected variables
		String nameTimeVariable = cmbTimeVariable.getValue();
		List<String> nameClassVariables = ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameSelectedFeatures = ckcmbFeatures.getCheckModel().getCheckedItems();
		// Set the variables that will be used
		datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameSelectedFeatures);
		// Define model
		MCTBNC<CPTNode, CIMNode> model = defineModel();
		try {
			// Define the validation method
			ValidationMethod validationMethod = defineValidationMethod();
			// Evaluate the performance of the model
			status.setText("Evaluating model...");
			validationMethod.evaluate(model);
			status.setText("Idle");
		} catch (UnreadDatasetException e) {
			// The dataset could not be read due to a problem with the provided files
			logger.error(e.getMessage());
			status.setText(e.getMessage());
		}

	}

	/**
	 * Initialize the controllers of the model pane.
	 */
	private void initializeDatasetPane() {
		// Initialize options of comboBoxes
		cmbDataFormat.getItems().addAll(datasetReaders);
		cmbStrategy.getItems().addAll(datasetReaderStrategies);
		// Select first option as default in comboBoxes
		cmbDataFormat.getSelectionModel().selectFirst();
		cmbStrategy.getSelectionModel().selectFirst();
		cmbStrategy.setDisable(true);
		// Initialize text fields with default values
		fldSizeSequences.setText("30");
		fldSizeSequences.setDisable(true);
		// Add listeners to checkcomboboxes
		ckcmbClassVariables.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				datasetModified();
			}
		});
		ckcmbFeatures.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				datasetModified();
			}
		});
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(fldSizeSequences);
	}

	/**
	 * Initialize the controllers of the model pane.
	 */
	private void initializeModelPane() {
		// Initialize options of comboBoxes
		cmbModel.getItems().addAll(models);
		cmbParameterBN.getItems().addAll(parameterLearningAlgs);
		cmbParameterCTBN.getItems().addAll(parameterLearningAlgs);
		cmbStructure.getItems().addAll(structureLearningAlgs);
		cmbPenalization.getItems().addAll(penalizations);
		cmbInitialStructure.getItems().addAll(initialStructures);
		cmbScoreFunction.getItems().addAll(scores);
		// Select first option as default in comboBoxes
		cmbModel.getSelectionModel().selectFirst();
		cmbParameterBN.getSelectionModel().selectFirst();
		cmbParameterCTBN.getSelectionModel().selectFirst();
		cmbStructure.getSelectionModel().selectFirst();
		cmbPenalization.getSelectionModel().selectFirst();
		cmbInitialStructure.getSelectionModel().selectFirst();
		cmbScoreFunction.getSelectionModel().selectFirst();
		// Initialize text fields with default values
		fldKParents.setText("2");
		fldNxBN.setText("1");
		fldMxy.setText("1");
		fldTx.setText("0.001");
		fldKParents.setDisable(true);
		fldNxBN.setDisable(true);
		fldMxy.setDisable(true);
		fldTx.setDisable(true);
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(fldKParents);
	}

	/**
	 * Initialize the controllers of the evaluation pane.
	 */
	private void initializeEvaluationPane() {
		// Initialize text fields with default values
		sldTrainingSize.setValue(0.7);
		fldNumFolds.setText("5");
		// Text values are restricted to certain values
		ControllerUtil.onlyPositiveIntegerGreaterThan(fldNumFolds, 2);
		// By default the sequences are shuffled
		cbShuffle.setSelected(true);
	}

	/**
	 * Initialize the dataset reader given the paht of the dataset folder.
	 * 
	 * @param pathFolder
	 * @throws FileNotFoundException
	 */
	private void initializeDatasetReader(String pathFolder) throws FileNotFoundException {
		String nameDatasetReader = cmbDataFormat.getValue();
		int sizeSequence = Integer.valueOf(fldSizeSequences.getText());
		datasetReader = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder, sizeSequence);
	}

	/**
	 * Obtain the variables of the selected dataset to added to the comboBoxes.
	 * 
	 * @param pathFolder
	 * @throws FileNotFoundException
	 */
	private void readVariablesDataset(String pathFolder) throws FileNotFoundException {
		// Names of the variables are retrieved
		List<String> nameVariables = datasetReader.getAllVariablesDataset();
		// If another dataset was used before, comboBoxes are reseted
		resetCheckComboBoxes();
		// Variables' names are added to the comboBoxes
		ckcmbFeatures.getItems().addAll(nameVariables);
		cmbTimeVariable.getItems().addAll(nameVariables);
		ckcmbClassVariables.getItems().addAll(nameVariables);
	}

	/**
	 * Evaluate if it is possible to train a model with the selected options. For
	 * example, it could be forgotten the selection of the dataset.
	 */
	private void checkValidOptions() {
		// TODO
	}

	private MCTBNC<CPTNode, CIMNode> defineModel() {
		// Retrieve learning algorithms
		BNLearningAlgorithms bnLearningAlgs = defineAlgorithmsBN();
		CTBNLearningAlgorithms ctbnLearningAlgs = defineAlgorithmsCTBN();
		// Parameters that could be necessary for the generation of the model
		Map<String, String> parameters = new WeakHashMap<String, String>();
		parameters.put("maxK", fldKParents.getText());
		// Generate selected model
		String selectedModel = cmbModel.getValue();
		MCTBNC<CPTNode, CIMNode> model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel, bnLearningAlgs,
				ctbnLearningAlgs, parameters, CPTNode.class, CIMNode.class);
		// Define initial structure
		model.setIntialStructure(cmbInitialStructure.getValue());
		return model;
	}

	private BNLearningAlgorithms defineAlgorithmsBN() {
		// Get names learning algorithms
		String nameBnPLA = cmbParameterBN.getValue();
		String nameBnSLA = cmbStructure.getValue();
		// Get hyperparameters
		double nx = Double.valueOf(fldNxBN.getText());
		// Get score function
		String scoreFunction = cmbScoreFunction.getValue();
		// Define penalization function (if any)
		String penalizationFunction = cmbPenalization.getValue();
		// Define learning algorithms for the class subgraph (Bayesian network)
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA, scoreFunction,
				penalizationFunction);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		return bnLearningAlgs;
	}

	private CTBNLearningAlgorithms defineAlgorithmsCTBN() {
		// Get names learning algorithms
		String nameCtbnPLA = cmbParameterCTBN.getValue();
		String nameCtbnSLA = cmbStructure.getValue();
		// Get hyperparameters
		double mxy = Double.valueOf(fldMxy.getText());
		double tx = Double.valueOf(fldTx.getText());
		// Get score function
		String scoreFunction = cmbScoreFunction.getValue();
		// Define penalization function (if any)
		String penalizationFunction = cmbPenalization.getValue();
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxy,
				tx);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA,
				scoreFunction, penalizationFunction);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		return ctbnLearningAlgs;
	}

	/**
	 * Define the validation method.
	 * 
	 * @param selectedValidationMethod
	 * @return
	 * @throws UnreadDatasetException
	 */
	private ValidationMethod defineValidationMethod() throws UnreadDatasetException {
		// Get selected validation method
		RadioButton rbValidationMethod = (RadioButton) validationMethod.getSelectedToggle();
		String selectedValidationMethod = rbValidationMethod.getText();
		// Define if sequences are shuffled before applying validation method
		boolean shuffleSequences = cbShuffle.isSelected();
		// Retrieve parameters of the validation methods
		double trainingSize = sldTrainingSize.getValue();
		int folds = Integer.valueOf(fldNumFolds.getText());
		// Retrieve algorithm of the validation method
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod(selectedValidationMethod,
				datasetReader, shuffleSequences, trainingSize, folds);
		return validationMethod;
	}

	/**
	 * Reset the comboBoxes.
	 */
	private void resetCheckComboBoxes() {
		ckcmbFeatures.getCheckModel().clearChecks();
		ckcmbClassVariables.getCheckModel().clearChecks();
		ckcmbFeatures.getItems().clear();
		cmbTimeVariable.getItems().clear();
		ckcmbClassVariables.getItems().clear();
	}

	// ---------- onAction methods ----------

	/**
	 * The information of the dataset to load was modified, so the DatasetReader is
	 * warned. This is useful to avoid the reloading of the same dataset.
	 */
	public void datasetModified() {
		if (datasetReader != null)
			datasetReader.setDatasetAsOutdated(true);
	}

	// TODO Improve the strategy to show and hide options of each algorithm

	/**
	 * A dataset reader was selected in the comboBox. Show its correspondent
	 * options.
	 */
	public void changeDatasetReader() {
		// Define dataset as modified
		datasetModified();
		// Show or hide options
		if (cmbDataFormat.getValue().equals("Single CSV")) {
			cmbStrategy.setDisable(false);
			if (cmbStrategy.getValue().equals("Fixed size"))
				fldSizeSequences.setDisable(false);
		} else {
			cmbStrategy.setDisable(true);
			fldSizeSequences.setDisable(true);
		}
	}

	/**
	 * An strategy for the extraction of sequences was selected. Show its
	 * correspondent options.
	 */
	public void changeDatasetReaderStrategy() {
		// Define dataset as modified
		datasetModified();
		// Show or hide options
		if (cmbStrategy.getValue().equals("Fixed size"))
			fldSizeSequences.setDisable(false);
		else
			fldSizeSequences.setDisable(true);
	}

	/**
	 * A model was selected in the comboBox. Show its correspondent parameters.
	 */
	public void changeModel() {
		String model = cmbModel.getValue();
		if (model.equals("DAG-maxK MCTBNC") || model.equals("Empty-maxK MCTBNC"))
			// Enable selecting the number of maximum parents
			fldKParents.setDisable(false);
		else
			fldKParents.setDisable(true);
		if (model.equals("MCTNBC")) {
			// Disable of learning structure options if learning a naive Bayes classifier
			cmbStructure.setDisable(true);
			cmbInitialStructure.setDisable(true);
			cmbScoreFunction.setDisable(true);
			cmbPenalization.setDisable(true);
		} else {
			cmbStructure.setDisable(false);
			cmbInitialStructure.setDisable(false);
			cmbScoreFunction.setDisable(false);
			cmbPenalization.setDisable(false);
		}
	}

	/**
	 * A parameter learning algorithm for BNs was selected in the comboBox. Show its
	 * correspondent parameters.
	 */
	public void changeParameterLearningAlgBN() {
		if (cmbParameterBN.getValue().equals("Bayesian estimation"))
			fldNxBN.setDisable(false);
		else
			fldNxBN.setDisable(true);
	}

	/**
	 * A parameter learning algorithm for CTBNs was selected in the comboBox. Show
	 * its correspondent parameters.
	 */
	public void changeParameterLearningAlgCTBN() {
		if (cmbParameterCTBN.getValue().equals("Bayesian estimation")) {
			fldMxy.setDisable(false);
			fldTx.setDisable(false);
		} else {
			fldMxy.setDisable(true);
			fldTx.setDisable(true);
		}
	}

}
