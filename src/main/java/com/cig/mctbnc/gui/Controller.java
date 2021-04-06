package com.cig.mctbnc.gui;

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
import com.cig.mctbnc.services.EvaluationService;
import com.cig.mctbnc.util.ControllerUtil;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
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
	private ComboBox<String> cmbTimeVariable;
	@FXML
	private CheckComboBox<String> ckcmbClassVariables;
	@FXML
	private CheckComboBox<String> ckcmbFeatures;
	@FXML
	private TextField fldSizeSequences;
	@FXML
	private TextField fldPath;
	@FXML
	private HBox hbStrategy;
	@FXML
	private HBox hbSizeSequences;

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
	private TextField fldNx;
	@FXML
	private TextField fldMxy;
	@FXML
	private TextField fldTx;
	@FXML
	private TextField fldRestarts;
	@FXML
	private HBox hbKParents;
	@FXML
	private HBox hbHyperBN;
	@FXML
	private HBox hbHyperCTBN;
	@FXML
	private HBox hbRestarts;
	@FXML
	private HBox hbPenalization;

	// Evaluation
	@FXML
	private ToggleGroup tgValidationMethod;
	@FXML
	private Slider sldTrainingSize;
	@FXML
	private TextField fldNumFolds;
	@FXML
	private HBox hbTrainingSize;
	@FXML
	private HBox hbNumFolds;
	@FXML
	private CheckBox chkShuffle;
	@FXML
	private CheckBox chkProbabilities;

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
		defineBindings();
	}

	/**
	 * Define bindings between controls. For example, the evaluation button should
	 * be disabled if no dataset was selected.
	 */
	private void defineBindings() {
		this.btnEvaluate.disableProperty().bind(this.fldPath.textProperty().isEqualTo(""));
	}

	/**
	 * Evaluate the selected model.
	 */
	public void evaluate() {
		// Get selected variables
		String nameTimeVariable = this.cmbTimeVariable.getValue();
		List<String> nameClassVariables = this.ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameSelectedFeatures = this.ckcmbFeatures.getCheckModel().getCheckedItems();
		// Set the variables that will be used
		this.datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameSelectedFeatures);
		// Define model
		MCTBNC<CPTNode, CIMNode> model = defineModel();
		try {
			// Define the validation method
			ValidationMethod validationMethod = defineValidationMethod();
			// Create service to evaluate model in another thread. This will allow to update
			// and keep using the user interface
			Service<Void> service = new EvaluationService(validationMethod, model);
			service.start();
			// The status label will be updated with the progress of the service
			this.status.textProperty().bind(service.messageProperty());

		} catch (UnreadDatasetException e) {
			// The dataset could not be read due to a problem with the provided files
			this.logger.error(e.getMessage());
			this.status.setText(e.getMessage());
		}
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
	 * @throws UnreadDatasetException
	 */
	public void setFolderDataset() throws FileNotFoundException, UnreadDatasetException {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(this.stage);
		if (selectedDirectory != null) {
			// Show the selected directory
			String pathFolder = selectedDirectory.getAbsolutePath();
			this.fldPath.setText(pathFolder);
			// Define dataset reader
			initializeDatasetReader(pathFolder);
			// Read the variables
			readVariablesDataset(pathFolder);
		}
	}

	/**
	 * Initialize the controllers of the model pane.
	 */
	private void initializeDatasetPane() {
		// Initialize options of comboBoxes
		this.cmbDataFormat.getItems().addAll(this.datasetReaders);
		this.cmbStrategy.getItems().addAll(this.datasetReaderStrategies);
		// Select first option as default in comboBoxes
		this.cmbDataFormat.getSelectionModel().selectFirst();
		this.cmbStrategy.getSelectionModel().selectFirst();
		ControllerUtil.showNode(this.hbStrategy, false);
		// Initialize text fields with default values
		this.fldSizeSequences.setText("30");
		ControllerUtil.showNode(this.hbSizeSequences, false);
		// Add listeners to checkcomboboxes
		this.ckcmbClassVariables.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				datasetModified();
			}
		});
		this.ckcmbFeatures.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				datasetModified();
			}
		});
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldSizeSequences);
	}

	/**
	 * Initialize the controllers of the model pane.
	 */
	private void initializeModelPane() {
		// Initialize options of comboBoxes
		this.cmbModel.getItems().addAll(this.models);
		this.cmbParameterBN.getItems().addAll(this.parameterLearningAlgs);
		this.cmbParameterCTBN.getItems().addAll(this.parameterLearningAlgs);
		this.cmbStructure.getItems().addAll(this.structureLearningAlgs);
		this.cmbPenalization.getItems().addAll(this.penalizations);
		this.cmbInitialStructure.getItems().addAll(this.initialStructures);
		this.cmbScoreFunction.getItems().addAll(this.scores);
		// Select first option as default in comboBoxes
		this.cmbModel.getSelectionModel().selectFirst();
		this.cmbParameterBN.getSelectionModel().selectFirst();
		this.cmbParameterCTBN.getSelectionModel().selectFirst();
		this.cmbStructure.getSelectionModel().selectFirst();
		this.cmbPenalization.getSelectionModel().selectFirst();
		this.cmbInitialStructure.getSelectionModel().selectFirst();
		this.cmbScoreFunction.getSelectionModel().selectFirst();
		// Initialize text fields with default values
		this.fldKParents.setText("2");
		this.fldNx.setText("1");
		this.fldMxy.setText("1");
		this.fldTx.setText("0.001");
		this.fldRestarts.setText("5");
		// Hide parameters
		ControllerUtil.showNode(this.hbKParents, false);
		ControllerUtil.showNode(this.hbHyperBN, false);
		ControllerUtil.showNode(this.hbHyperCTBN, false);
		ControllerUtil.showNode(this.hbRestarts, false);
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldKParents);
		ControllerUtil.onlyPositiveInteger(this.fldRestarts);
	}

	/**
	 * Initialize the controllers of the evaluation pane.
	 */
	private void initializeEvaluationPane() {
		// Hold-out validation by default
		selectHoldOutValidation();
		// Initialize text fields with default values
		this.sldTrainingSize.setValue(0.7);
		this.fldNumFolds.setText("5");
		// Text values are restricted to certain values
		ControllerUtil.onlyPositiveIntegerGreaterThan(this.fldNumFolds, 2);
		// By default the sequences are shuffled
		this.chkShuffle.setSelected(true);
		// By default the probabilities of the class configurations are estimated
		this.chkProbabilities.setSelected(true);
	}

	/**
	 * Initialize the dataset reader given the paht of the dataset folder.
	 * 
	 * @param pathFolder
	 * @throws FileNotFoundException
	 * @throws UnreadDatasetException
	 */
	private void initializeDatasetReader(String pathFolder) throws FileNotFoundException, UnreadDatasetException {
		String nameDatasetReader = this.cmbDataFormat.getValue();
		int sizeSequence = Integer.valueOf(this.fldSizeSequences.getText());
		this.datasetReader = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder, sizeSequence);
	}

	/**
	 * Obtain the variables of the selected dataset to added to the comboBoxes.
	 * 
	 * @param pathFolder
	 * @throws FileNotFoundException
	 */
	private void readVariablesDataset(String pathFolder) throws FileNotFoundException {
		// Names of the variables are retrieved
		List<String> nameVariables = this.datasetReader.getAllVariablesDataset();
		// If another dataset was used before, comboBoxes are reseted
		resetCheckComboBoxes();
		// Variables' names are added to the comboBoxes
		this.ckcmbFeatures.getItems().addAll(nameVariables);
		this.cmbTimeVariable.getItems().addAll(nameVariables);
		this.ckcmbClassVariables.getItems().addAll(nameVariables);
	}

	private MCTBNC<CPTNode, CIMNode> defineModel() {
		// Retrieve learning algorithms
		BNLearningAlgorithms bnLearningAlgs = defineAlgorithmsBN();
		CTBNLearningAlgorithms ctbnLearningAlgs = defineAlgorithmsCTBN();
		// Parameters that could be necessary for the generation of the model
		Map<String, String> parameters = new WeakHashMap<String, String>();
		parameters.put("maxK", this.fldKParents.getText());
		// Generate selected model
		String selectedModel = this.cmbModel.getValue();
		MCTBNC<CPTNode, CIMNode> model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel, bnLearningAlgs,
				ctbnLearningAlgs, parameters, CPTNode.class, CIMNode.class);
		// Define initial structure
		model.setIntialStructure(this.cmbInitialStructure.getValue());
		return model;
	}

	private BNLearningAlgorithms defineAlgorithmsBN() {
		// Get names learning algorithms
		String nameBnPLA = this.cmbParameterBN.getValue();
		String nameBnSLA = this.cmbStructure.getValue();
		// Get hyperparameters
		double nx = Double.valueOf(this.fldNx.getText());
		// Get score function
		String scoreFunction = this.cmbScoreFunction.getValue();
		// Define penalization function (if any)
		String penalizationFunction = this.cmbPenalization.getValue();
		// Get number of restarts (for random restart hill climbing)
		int restarts = Integer.valueOf(this.fldRestarts.getText());
		// Define learning algorithms for the class subgraph (Bayesian network)
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA, scoreFunction,
				penalizationFunction, restarts);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		return bnLearningAlgs;
	}

	private CTBNLearningAlgorithms defineAlgorithmsCTBN() {
		// Get names learning algorithms
		String nameCtbnPLA = this.cmbParameterCTBN.getValue();
		String nameCtbnSLA = this.cmbStructure.getValue();
		// Get hyperparameters
		double mxy = Double.valueOf(this.fldMxy.getText());
		double tx = Double.valueOf(this.fldTx.getText());
		// Get score function
		String scoreFunction = this.cmbScoreFunction.getValue();
		// Define penalization function (if any)
		String penalizationFunction = this.cmbPenalization.getValue();
		// Get number of restarts (for random restart hill climbing)
		int restarts = Integer.valueOf(this.fldRestarts.getText());
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxy,
				tx);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA,
				scoreFunction, penalizationFunction, restarts);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		return ctbnLearningAlgs;
	}

	/**
	 * Define the validation method.
	 * 
	 * @param selectedValidationMethod
	 * @return validation method
	 * @throws UnreadDatasetException
	 */
	private ValidationMethod defineValidationMethod() throws UnreadDatasetException {
		// Get selected validation method
		RadioButton rbValidationMethod = (RadioButton) this.tgValidationMethod.getSelectedToggle();
		String selectedValidationMethod = rbValidationMethod.getText();
		// Define if sequences are shuffled before applying validation method
		boolean shuffleSequences = this.chkShuffle.isSelected();
		// Define if the probabilities of the class configurations are estimated
		boolean estimateProbabilities = this.chkProbabilities.isSelected();
		// Retrieve parameters of the validation methods
		double trainingSize = this.sldTrainingSize.getValue();
		int folds = Integer.valueOf(this.fldNumFolds.getText());
		// Retrieve algorithm of the validation method
		ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod(selectedValidationMethod,
				this.datasetReader, shuffleSequences, estimateProbabilities, trainingSize, folds);
		return validationMethod;
	}

	/**
	 * Reset the comboBoxes.
	 */
	private void resetCheckComboBoxes() {
		this.ckcmbFeatures.getCheckModel().clearChecks();
		this.ckcmbClassVariables.getCheckModel().clearChecks();
		this.ckcmbFeatures.getItems().clear();
		this.cmbTimeVariable.getItems().clear();
		this.ckcmbClassVariables.getItems().clear();
	}

	// ---------- onAction methods ----------

	/**
	 * The information of the dataset to load was modified, so the DatasetReader is
	 * warned. This is useful to avoid the reloading of the same dataset.
	 */
	public void datasetModified() {
		if (this.datasetReader != null)
			this.datasetReader.setDatasetAsOutdated(true);
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
		if (this.cmbDataFormat.getValue().equals("Single CSV")) {
			ControllerUtil.showNode(this.hbStrategy, true);
			ControllerUtil.showNode(this.hbSizeSequences, true);
			if (this.cmbStrategy.getValue().equals("Fixed size"))
				ControllerUtil.showNode(this.fldSizeSequences, true);
		} else {
			ControllerUtil.showNode(this.hbStrategy, false);
			ControllerUtil.showNode(this.hbSizeSequences, false);
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
		if (this.cmbStrategy.getValue().equals("Fixed size"))
			this.fldSizeSequences.setDisable(false);
		else
			this.fldSizeSequences.setDisable(true);
	}

	/**
	 * A model was selected in the comboBox. Show its correspondent parameters.
	 */
	public void changeModel() {
		String model = this.cmbModel.getValue();
		if (model.equals("DAG-maxK MCTBNC") || model.equals("Empty-maxK MCTBNC"))
			// Enable selecting the number of maximum parents
			ControllerUtil.showNode(this.hbKParents, true);
		else
			ControllerUtil.showNode(this.hbKParents, false);

		if (model.equals("MCTNBC")) {
			// Disable of learning structure options if learning a naive Bayes classifier
			this.cmbStructure.setDisable(true);
			this.cmbInitialStructure.setDisable(true);
			this.cmbScoreFunction.setDisable(true);
			this.cmbPenalization.setDisable(true);
		} else {
			this.cmbStructure.setDisable(false);
			this.cmbInitialStructure.setDisable(false);
			this.cmbScoreFunction.setDisable(false);
			this.cmbPenalization.setDisable(false);
		}
	}

	/**
	 * A parameter learning algorithm for BNs was selected in the comboBox. Show its
	 * correspondent parameters.
	 */
	public void changeParameterLearningAlgBN() {
		if (this.cmbParameterBN.getValue().equals("Bayesian estimation"))
			ControllerUtil.showNode(this.hbHyperBN, true);
		else
			ControllerUtil.showNode(this.hbHyperBN, false);
	}

	/**
	 * A parameter learning algorithm for CTBNs was selected in the comboBox. Show
	 * its correspondent parameters.
	 */
	public void changeParameterLearningAlgCTBN() {
		if (this.cmbParameterCTBN.getValue().equals("Bayesian estimation"))
			ControllerUtil.showNode(this.hbHyperCTBN, true);
		else
			ControllerUtil.showNode(this.hbHyperCTBN, false);
	}

	/**
	 * A structure learning algorithm was selected in the comboBox. Show its
	 * correspondent parameters.
	 */
	public void changeStructureLearningAlg() {
		if (this.cmbStructure.getValue().equals("Random-restart hill climbing")) {
			ControllerUtil.showNode(this.hbRestarts, true);
		} else {
			ControllerUtil.showNode(this.hbRestarts, false);
		}
	}

	/**
	 * A score function was selected in the comboBox. Show its correspondent
	 * parameters.
	 */
	public void changeScoreFunction() {
		if (this.cmbScoreFunction.getValue().equals("Bayesian Dirichlet equivalent")) {
			ControllerUtil.showNode(this.hbPenalization, false);
		} else {
			ControllerUtil.showNode(this.hbPenalization, true);
		}
	}

	/**
	 * Cross-validation method was selected. Show its correspondent parameters.
	 */
	public void selectCrossValidation() {
		ControllerUtil.showNode(this.hbNumFolds, true);
		ControllerUtil.showNode(this.hbTrainingSize, false);
	}

	/**
	 * Hold-out-validation method was selected. Show its correspondent parameters.
	 */
	public void selectHoldOutValidation() {
		ControllerUtil.showNode(this.hbTrainingSize, true);
		ControllerUtil.showNode(this.hbNumFolds, false);
	}

}