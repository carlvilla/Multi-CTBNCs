package es.upm.fi.cig.mctbnc.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;

import es.upm.fi.cig.mctbnc.classification.ClassifierFactory;
import es.upm.fi.cig.mctbnc.data.reader.DatasetReader;
import es.upm.fi.cig.mctbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.mctbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.nodes.CIMNode;
import es.upm.fi.cig.mctbnc.nodes.CPTNode;
import es.upm.fi.cig.mctbnc.performance.ValidationMethod;
import es.upm.fi.cig.mctbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.mctbnc.services.ClassificationService;
import es.upm.fi.cig.mctbnc.services.EvaluationService;
import es.upm.fi.cig.mctbnc.services.TrainingService;
import es.upm.fi.cig.mctbnc.util.ControllerUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
	// Controllers javafx
	private Stage stage;

	// Dataset readers
	DatasetReader dRTraining;
	DatasetReader dRClassification;

	// Trained model wrapped with ObjectProperty to allow binding with controls
	ObjectProperty<MCTBNC<CPTNode, CIMNode>> model = new SimpleObjectProperty<>();

	// Services. They allow to use and update the interface while performing tasks
	TrainingService trainingService = new TrainingService();
	EvaluationService evaluationService = new EvaluationService();
	ClassificationService classificationService = new ClassificationService();

	// Dataset tab
	@FXML
	private ComboBox<String> cmbDataFormat;
	@FXML
	private ComboBox<String> cmbStrategy;
	@FXML
	private ComboBox<String> cmbTimeVariable;
	@FXML
	private CheckComboBox<String> ckcmbClassVariables;
	@FXML
	private CheckComboBox<String> ckcmbFeatureVariables;
	@FXML
	private TextField fldSizeSequences;
	@FXML
	private TextField fldPathDataset;
	@FXML
	private HBox hbStrategy;
	@FXML
	private HBox hbSizeSequences;

	// Model tab
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

	// Evaluation tab
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
	@FXML
	private Button btnEvaluate;

	// Classification tab
	@FXML
	private ComboBox<String> cmbDataFormatClassification;
	@FXML
	private ComboBox<String> cmbStrategyClassification;
	@FXML
	private TextField fldSizeSequencesClassification;
	@FXML
	private TextField fldPathDatasetClassification;
	@FXML
	private HBox hbStrategyClassification;
	@FXML
	private HBox hbSizeSequencesClassification;
	@FXML
	private CheckBox chkProbabilitiesClassification;
	@FXML
	private Button btnTraining;
	@FXML
	private Button btnClassify;

	// General controls
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
	 * Initializes the controller.
	 */
	@FXML
	public void initialize() {
		initializeDatasetPane();
		initializeModelPane();
		initializeEvaluationPane();
		initializeClassificationPane();
		setBindings();
	}

	/**
	 * Evaluates the selected model.
	 */
	public void evaluate() {
		// Get selected variables
		String nameTimeVariable = this.cmbTimeVariable.getValue();
		List<String> nameClassVariables = this.ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameFeatureVariables = this.ckcmbFeatureVariables.getCheckModel().getCheckedItems();
		if (datasetIsValid(this.dRTraining, nameTimeVariable, nameClassVariables, nameFeatureVariables)) {
			// Set the variables that will be used
			this.dRTraining.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
			try {
				// Initialize and restart service to evaluate the selected model in another
				// thread
				this.evaluationService.initializeService(getValidationMethod(), getModel());
				this.evaluationService.restart();
				// The status label will be updated with the progress of the service
				this.status.textProperty().bind(this.evaluationService.messageProperty());
			} catch (UnreadDatasetException e) {
				// The dataset could not be read due to a problem with the provided files
				this.logger.error(e.getMessage());
				this.status.setText(e.getMessage());
			}
		} else
			this.logger.warn("The evaluation was not performed");
	}

	/**
	 * Trains the selected model.
	 */
	public void trainModel() {
		// Get selected variables
		String nameTimeVariable = this.cmbTimeVariable.getValue();
		List<String> nameClassVariables = this.ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameFeatureVariables = this.ckcmbFeatureVariables.getCheckModel().getCheckedItems();
		if (datasetIsValid(this.dRTraining, nameTimeVariable, nameClassVariables, nameFeatureVariables)) {
			// Set the variables that will be used
			this.dRTraining.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
			// Define model to train
			this.model.set(getModel());
			// Initialize and restart service to train the model in another thread
			this.trainingService.initializeService(this.model.get(), this.dRTraining);
			this.trainingService.restart();
			// The status label will be updated with the progress of the service
			this.status.textProperty().bind(this.trainingService.messageProperty());
		} else
			this.logger.warn("The training was not performed");
	}

	/**
	 * Performs classification over a provided dataset with a previously trained
	 * model.
	 */
	public void classify() {
		if (this.model.get() != null && this.dRClassification != null) {
			// Retrieve time and feature variables that are necessary for the classification
			String nameTimeVariable = this.dRTraining.getNameTimeVariable();
			List<String> nameFeatureVariables = this.dRTraining.getNameFeatureVariables();
			// Set the variables
			this.dRClassification.setVariables(nameTimeVariable, nameFeatureVariables);
			// Check if probabilities of predicted class configurations should be estimated
			boolean estimateProbabilities = this.chkProbabilitiesClassification.isSelected();
			// Initialize and restart service to classify the dataset in another thread
			this.classificationService.initializeService(this.model.get(), this.dRClassification,
					estimateProbabilities);
			this.classificationService.restart();
			// The status label will be updated with the progress of the service
			this.status.textProperty().bind(this.classificationService.messageProperty());
		} else if (this.model.get() == null)
			this.logger.warn("The classification was not performed. No model has been trained");
		else
			this.logger.warn("The classification was not performed. No dataset to classify has been provided");
	}

	/**
	 * Establishes the stage used by the application to show dialogs.
	 * 
	 * @param stage a {@code Stage}
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Opens a dialog to select the folder where the dataset for training and
	 * evaluation is located.
	 * 
	 * @throws FileNotFoundException  if the provided files were not found
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public void setFolderDataset() throws FileNotFoundException, UnreadDatasetException {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(this.stage);
		if (selectedDirectory != null) {
			// Show the selected directory
			String pathFolder = selectedDirectory.getAbsolutePath();
			// Define dataset reader
			initializeDatasetReader(pathFolder);
			if (this.dRTraining != null) {
				// Read the variables
				readVariablesDataset();
				// Show dataset folder path in text field
				this.fldPathDataset.setText(pathFolder);
			}
		}
	}

	/**
	 * Opens a dialog to select the folder where the dataset on which classification
	 * is performed is located.
	 * 
	 * @throws FileNotFoundException  if the provided files were not found
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public void setFolderDatasetClassification() throws FileNotFoundException, UnreadDatasetException {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(this.stage);
		if (selectedDirectory != null) {
			// Show the selected directory
			String pathFolder = selectedDirectory.getAbsolutePath();
			// Define dataset reader
			initializeDatasetReaderClassification(pathFolder);
			if (this.dRClassification != null) {
				// Show folder path of the dataset to classify in text field
				this.fldPathDatasetClassification.setText(pathFolder);
			}
		}
	}

	/**
	 * Initializes the controllers of the model pane.
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
		this.fldSizeSequences.setText("100");
		ControllerUtil.showNode(this.hbSizeSequences, false);
		// Add listeners to checkcomboboxes
		this.ckcmbClassVariables.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				datasetModified();
			}
		});
		this.ckcmbFeatureVariables.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				datasetModified();
			}
		});
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldSizeSequences);
	}

	/**
	 * Initializes the controllers of the model pane.
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
		ControllerUtil.onlyPositiveDouble(this.fldNx);
		ControllerUtil.onlyPositiveDouble(this.fldMxy);
		ControllerUtil.onlyPositiveDouble(this.fldTx);
	}

	/**
	 * Initializes the controllers of the evaluation pane.
	 */
	private void initializeEvaluationPane() {
		// Hold-out validation by default
		selectHoldOutValidation();
		// Initialize text fields with default values
		this.sldTrainingSize.setValue(0.7);
		this.fldNumFolds.setText("5");
		// Text values are restricted to certain values
		ControllerUtil.onlyPositiveIntegerGTE(this.fldNumFolds, 2);
		// By default the sequences are shuffled
		this.chkShuffle.setSelected(true);
		// By default the probabilities of the class configurations are estimated
		this.chkProbabilities.setSelected(true);
	}

	/**
	 * Initializes the controllers of the classification pane.
	 */
	private void initializeClassificationPane() {
		// Initialize options of comboBoxes
		this.cmbDataFormatClassification.getItems().addAll(this.datasetReaders);
		this.cmbStrategyClassification.getItems().addAll(this.datasetReaderStrategies);
		// Select first option as default in comboBoxes
		this.cmbDataFormatClassification.getSelectionModel().selectFirst();
		this.cmbStrategyClassification.getSelectionModel().selectFirst();
		ControllerUtil.showNode(this.hbStrategyClassification, false);
		// Initialize text fields with default values
		this.fldSizeSequencesClassification.setText("100");
		ControllerUtil.showNode(this.hbSizeSequencesClassification, false);
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldSizeSequencesClassification);
	}

	/**
	 * Initializes the dataset reader given the path of the dataset folder.
	 * 
	 * @param pathFolder path of the dataset folder
	 * @throws FileNotFoundException  if the provided files were not found
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	private void initializeDatasetReader(String pathFolder) throws FileNotFoundException, UnreadDatasetException {
		String nameDatasetReader = this.cmbDataFormat.getValue();
		int sizeSequence = ControllerUtil.extractInteger(this.fldSizeSequences.getText(), 0);
		this.dRTraining = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder, sizeSequence);
	}

	/**
	 * Initializes the dataset reader given the path of the dataset folder.
	 * 
	 * @param pathFolder path of the dataset folder
	 * @throws FileNotFoundException  if the provided files were not found
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	private void initializeDatasetReaderClassification(String pathFolder)
			throws FileNotFoundException, UnreadDatasetException {
		String nameDatasetReader = this.cmbDataFormatClassification.getValue();
		int sizeSequence = Integer.valueOf(this.fldSizeSequencesClassification.getText());
		this.dRClassification = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder, sizeSequence);
	}

	/**
	 * Obtains the variables of the selected dataset to added to the comboBoxes.
	 * 
	 * @throws FileNotFoundException if the provided files were not found
	 */
	private void readVariablesDataset() throws FileNotFoundException {
		if (this.dRTraining != null) {
			// Names of the variables are retrieved
			List<String> nameVariables = this.dRTraining.getNameVariables();
			// If another dataset was used before, comboBoxes are reseted
			resetCheckComboBoxes();
			// Variables' names are added to the comboBoxes
			this.ckcmbFeatureVariables.getItems().addAll(nameVariables);
			this.cmbTimeVariable.getItems().addAll(nameVariables);
			this.ckcmbClassVariables.getItems().addAll(nameVariables);
		}
	}

	/**
	 * Set the bindings of the buttons.
	 */
	private void setBindings() {
		// Set the bindings for the evaluation button
		this.btnEvaluate.disableProperty().bind(getBindingEvaluateBtn());
		// Set the bindings for the training button
		this.btnTraining.disableProperty().bind(getBindingTrainBtn());
		// Set the bindings for the classification button
		this.btnClassify.disableProperty().bind(getBindingClassifyBtn());
	}

	/**
	 * Returns a {@code BooleanBinding} for the evaluation button. It checks if the
	 * model is being evaluated and if a dataset was not provided. If one of these
	 * conditions is met, the button should be disabled.
	 * 
	 * @return {@code BooleanBinding} for the evaluation button
	 */
	private BooleanBinding getBindingEvaluateBtn() {
		return Bindings.or(this.fldPathDataset.textProperty().isEmpty(), this.evaluationService.runningProperty());

	}

	/**
	 * Returns a {@code BooleanBinding} for the training button. It checks if the
	 * model is being trained, if a dataset is being classified and if a training
	 * dataset was not provided. If one of these conditions is met, the button
	 * should be disabled.
	 * 
	 * @return {@code BooleanBinding} for the training button
	 */
	private BooleanBinding getBindingTrainBtn() {
		return this.fldPathDataset.textProperty().isEmpty().or(this.trainingService.runningProperty())
				.or(this.classificationService.runningProperty());
	}

	/**
	 * Returns a {@code BooleanBinding} for the classification button. It checks if
	 * the model was not trained or is being trained, if a dataset is being
	 * classified and if a training dataset was not provided. If one of these
	 * conditions is met, the button should be disabled.
	 * 
	 * @return {@code BooleanBinding} for the classification button
	 */
	private BooleanBinding getBindingClassifyBtn() {
		return this.model.isNull().or(this.fldPathDatasetClassification.textProperty().isEmpty())
				.or(this.trainingService.runningProperty()).or(this.classificationService.runningProperty());
	}

	/**
	 * Return the selected model.
	 * 
	 * @return a {@code MCTBNC<CPTNode, CIMNode>}
	 */
	private MCTBNC<CPTNode, CIMNode> getModel() {
		// Retrieve learning algorithms
		BNLearningAlgorithms bnLearningAlgs = getAlgorithmsBN();
		CTBNLearningAlgorithms ctbnLearningAlgs = getAlgorithmsCTBN();
		// Hyperparameters that could be necessary for the generation of the model
		Map<String, String> hyperparameters = new WeakHashMap<String, String>();
		hyperparameters.put("maxK", this.fldKParents.getText());
		// Generate selected model
		String selectedModel = this.cmbModel.getValue();
		MCTBNC<CPTNode, CIMNode> model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel, bnLearningAlgs,
				ctbnLearningAlgs, hyperparameters, CPTNode.class, CIMNode.class);
		// Define initial structure
		model.setIntialStructure(this.cmbInitialStructure.getValue());
		return model;
	}

	/**
	 * Return the selected learning algorithms for the Bayesian network.
	 * 
	 * @return a {@code BNLearningAlgorithms}
	 */
	private BNLearningAlgorithms getAlgorithmsBN() {
		// Get names learning algorithms
		String nameBnPLA = this.cmbParameterBN.getValue();
		String nameBnSLA = this.cmbStructure.getValue();
		// Get hyperparameters
		double nx = ControllerUtil.extractDecimal(this.fldNx.getText(), 0);
		// Get score function
		String scoreFunction = this.cmbScoreFunction.getValue();
		// Define penalization function (if any)
		String penalizationFunction = this.cmbPenalization.getValue();
		// Get number of restarts (for random restart hill climbing)
		int restarts = ControllerUtil.extractInteger(this.fldRestarts.getText(), 2);
		// Define learning algorithms for the class subgraph (Bayesian network)
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA, scoreFunction,
				penalizationFunction, restarts);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		return bnLearningAlgs;
	}

	/**
	 * Return the selected learning algorithms for the continuous time Bayesian
	 * network.
	 * 
	 * @return a {@code CTBNLearningAlgorithms}
	 */
	private CTBNLearningAlgorithms getAlgorithmsCTBN() {
		// Get names learning algorithms
		String nameCtbnPLA = this.cmbParameterCTBN.getValue();
		String nameCtbnSLA = this.cmbStructure.getValue();
		// Get hyperparameters
		double mxyHP = ControllerUtil.extractDecimal(this.fldMxy.getText(), 0);
		double txHP = ControllerUtil.extractDecimal(this.fldTx.getText(), 0);
		// Get score function
		String scoreFunction = this.cmbScoreFunction.getValue();
		// Define penalization function (if any)
		String penalizationFunction = this.cmbPenalization.getValue();
		// Get number of restarts (for random restart hill climbing)
		int restarts = ControllerUtil.extractInteger(this.fldRestarts.getText(), 2);
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxyHP,
				txHP);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA,
				scoreFunction, penalizationFunction, restarts);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		return ctbnLearningAlgs;
	}

	/**
	 * Return the selected validation method.
	 * 
	 * @return validation method
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	private ValidationMethod getValidationMethod() throws UnreadDatasetException {
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
				this.dRTraining, trainingSize, folds, estimateProbabilities, shuffleSequences, null);
		return validationMethod;
	}

	/**
	 * Check if a dataset and variables were provided.
	 * 
	 * @param datasetReader        a {@code DatasetReader} to read the dataset
	 * @param nameTimeVariable     name of the time variable
	 * @param nameClassVariables   names of the class variables
	 * @param nameFeatureVariables names of the feature variables
	 * @return true if a dataset and variables were provided, false otherwise
	 */
	private boolean datasetIsValid(DatasetReader datasetReader, String nameTimeVariable,
			List<String> nameClassVariables, List<String> nameFeatureVariables) {
		if (datasetReader == null) {
			this.logger.warn("A dataset must be provided");
			return false;
		} else if (nameTimeVariable == null) {
			this.logger.warn("A time variable must be provided");
			return false;
		} else if (nameClassVariables.isEmpty()) {
			this.logger.warn("At least one class variable must be provided");
			return false;
		} else if (nameFeatureVariables.isEmpty()) {
			this.logger.warn("At least one feature variable must be provided");
			return false;
		}
		return true;
	}

	/**
	 * Resets the comboBoxes.
	 */
	private void resetCheckComboBoxes() {
		this.ckcmbFeatureVariables.getCheckModel().clearChecks();
		this.ckcmbClassVariables.getCheckModel().clearChecks();
		this.ckcmbFeatureVariables.getItems().clear();
		this.cmbTimeVariable.getItems().clear();
		this.ckcmbClassVariables.getItems().clear();
	}

	// ---------- onAction methods ----------

	/**
	 * The information of the dataset for training/evaluation was modified, so its
	 * {@code DatasetReader} is warned. This is useful to avoid the reloading of the
	 * same dataset.
	 */
	public void datasetModified() {
		if (this.dRTraining != null)
			this.dRTraining.setDatasetAsOutdated(true);
	}

	/**
	 * The information of the dataset used for classification was modified, so its
	 * {@code DatasetReader} is warned. This is useful to avoid the reloading of the
	 * same dataset.
	 */
	public void datasetClassificationModified() {
		if (this.dRClassification != null)
			this.dRClassification.setDatasetAsOutdated(true);
	}

	/**
	 * A dataset reader was selected in the comboBox. Shows its correspondent
	 * options.
	 * 
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 * @throws FileNotFoundException  if the provided files were not found
	 */
	public void changeDatasetReader() throws FileNotFoundException, UnreadDatasetException {
		if (this.dRTraining != null)
			initializeDatasetReader(this.fldPathDataset.getText());
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
	 * A dataset reader for the classification dataset was selected in the comboBox.
	 * Shows its correspondent options.
	 * 
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 * @throws FileNotFoundException  if the provided files were not found
	 */
	public void changeDatasetReaderClassification() throws FileNotFoundException, UnreadDatasetException {
		if (this.dRClassification != null)
			initializeDatasetReaderClassification(this.fldPathDatasetClassification.getText());
		// Show or hide options
		if (this.cmbDataFormatClassification.getValue().equals("Single CSV")) {
			ControllerUtil.showNode(this.hbStrategyClassification, true);
			ControllerUtil.showNode(this.hbSizeSequencesClassification, true);
			if (this.cmbStrategyClassification.getValue().equals("Fixed size"))
				ControllerUtil.showNode(this.fldSizeSequencesClassification, true);
		} else {
			ControllerUtil.showNode(this.hbStrategyClassification, false);
			ControllerUtil.showNode(this.hbSizeSequencesClassification, false);
		}
	}

	/**
	 * An strategy for the extraction of sequences was selected. Shows its
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
	 * An strategy for the extraction of sequences to classify was selected. Shows
	 * its correspondent options.
	 */
	public void changeDatasetReaderClassificationStrategy() {
		// Define dataset as modified
		datasetClassificationModified();
		// Show or hide options
		if (this.cmbStrategyClassification.getValue().equals("Fixed size"))
			this.fldSizeSequencesClassification.setDisable(false);
		else
			this.fldSizeSequencesClassification.setDisable(true);
	}

	/**
	 * A model was selected in the comboBox. Shows its correspondent
	 * hyperparameters.
	 */
	public void changeModel() {
		String model = this.cmbModel.getValue();
		if (model.equals("DAG-maxK MCTBNC") || model.equals("Empty-maxK MCTBNC"))
			// Enable selecting the maximum number of parents
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
	 * A parameter learning algorithm for BNs was selected in the comboBox. Shows
	 * its correspondent parameters.
	 */
	public void changeParameterLearningAlgBN() {
		if (this.cmbParameterBN.getValue().equals("Bayesian estimation"))
			ControllerUtil.showNode(this.hbHyperBN, true);
		else
			ControllerUtil.showNode(this.hbHyperBN, false);
	}

	/**
	 * A parameter learning algorithm for CTBNs was selected in the comboBox. Shows
	 * its correspondent parameters.
	 */
	public void changeParameterLearningAlgCTBN() {
		if (this.cmbParameterCTBN.getValue().equals("Bayesian estimation"))
			ControllerUtil.showNode(this.hbHyperCTBN, true);
		else
			ControllerUtil.showNode(this.hbHyperCTBN, false);
	}

	/**
	 * A structure learning algorithm was selected in the comboBox. Shows its
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
	 * A score function was selected in the comboBox. Shows its correspondent
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
	 * Cross-validation method was selected. Shows its correspondent parameters.
	 */
	public void selectCrossValidation() {
		ControllerUtil.showNode(this.hbNumFolds, true);
		ControllerUtil.showNode(this.hbTrainingSize, false);
	}

	/**
	 * Hold-out-validation method was selected. Shows its correspondent parameters.
	 */
	public void selectHoldOutValidation() {
		ControllerUtil.showNode(this.hbTrainingSize, true);
		ControllerUtil.showNode(this.hbNumFolds, false);
	}

}
