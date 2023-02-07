package es.upm.fi.cig.multictbnc.gui;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.multictbnc.services.ClassificationService;
import es.upm.fi.cig.multictbnc.services.EvaluationService;
import es.upm.fi.cig.multictbnc.services.TrainingService;
import es.upm.fi.cig.multictbnc.util.ControllerUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Controller used to initialise the elements of the GUI and allow the interaction between the logic of the application
 * and the GUI.
 *
 * @author Carlos Villa Blanco
 */
public class Controller {
	private static final Logger logger = LogManager.getLogger(Controller.class);
	// Dataset readers
	DatasetReader trainingDatasetReader;
	DatasetReader testDatasetReader;
	DatasetReader ClassificationDatasetReader;

	// Trained model wrapped with ObjectProperty to allow binding with controls
	ObjectProperty<MultiCTBNC<CPTNode, CIMNode>> model = new SimpleObjectProperty<>();

	// Services. They allow to use and update the interface while performing tasks
	TrainingService trainingService = new TrainingService();
	EvaluationService evaluationService = new EvaluationService();
	ClassificationService classificationService = new ClassificationService();
	// -------------------- AVAILABLE MODELS --------------------
	List<String> models = ClassifierFactory.getAvailableModels();
	// ----------------------- AVAILABLE ALGORITHMS -----------------------
	List<String> parameterLearningAlgs = List.of("Maximum likelihood estimation", "Bayesian estimation");
	List<String> structureLearningAlgs = StructureLearningAlgorithmFactory.getAvailableLearningMethods();
	List<String> initialStructures = List.of("Empty", "Naive Bayes");
	List<String> scores = List.of("Log-likelihood", "Conditional log-likelihood", "Bayesian Dirichlet equivalent");
	List<String> penalisations = List.of("No", "BIC", "AIC");
	// -------------------- AVAILABLE DATASET READERS --------------------
	List<String> datasetReaders = DatasetReaderFactory.getAvailableDatasetReaders();
	List<String> datasetReaderStrategies = DatasetReaderFactory.getAvailableStrategies();
	// Controllers javafx
	private Stage stage;
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
	private ComboBox<String> cmbPenalisation;
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
	private TextField fldSigClassSubgraph;
	@FXML
	private TextField fldSigFeatureBridgeSubgraph;
	@FXML
	private TextField fldMaxSizeSepSet;
	@FXML
	private TextField fldSizeTabuList;
	@FXML
	private HBox hbKParents;
	@FXML
	private HBox hbHyperBN;
	@FXML
	private HBox hbHyperCTBN;
	@FXML
	private HBox hbRestarts;
	@FXML
	private HBox hbSignificanceLevel;
	@FXML
	private HBox hbMaxSizeSepSet;
	@FXML
	private HBox hbSizeTabuList;
	@FXML
	private HBox hbInitialStructure;
	@FXML
	private HBox hbScore;
	@FXML
	private HBox hbPenalisation;
	// Evaluation tab
	@FXML
	private ToggleGroup tgValidationMethod;
	@FXML
	private TextField fldPathTestDataset;
	@FXML
	private Slider sldTrainingSize;
	@FXML
	private TextField fldNumFolds;
	@FXML
	private HBox hbTrainingSize;
	@FXML
	private HBox hbNumFolds;
	@FXML
	private HBox hbTestDataset;
	@FXML
	private HBox hbShuffle;
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

	/**
	 * A dataset reader was selected in the comboBox. Shows its correspondent options.
	 */
	public void changeDatasetReader() {
		// Remove the path previously selected datasets
		this.fldPathDataset.clear();
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
	 * A dataset reader for the classification dataset was selected in the comboBox. Shows its correspondent options.
	 */
	public void changeDatasetReaderClassification() {
		// Remove the path previously selected datasets
		this.fldPathDatasetClassification.clear();
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
	 * An strategy for the extraction of sequences to classify was selected. Shows its correspondent options.
	 */
	public void changeDatasetReaderClassificationStrategy() {
		// Define dataset as modified
		datasetClassificationModified();
		// Show or hide options
		this.fldSizeSequencesClassification.setDisable(
				!this.cmbStrategyClassification.getValue().equals("Fixed " + "size"));
	}

	/**
	 * An strategy for the extraction of sequences was selected. Shows its correspondent options.
	 */
	public void changeDatasetReaderStrategy() {
		// Define dataset as modified
		datasetModified();
		// Show or hide options
		this.fldSizeSequences.setDisable(!this.cmbStrategy.getValue().equals("Fixed size"));
	}

	/**
	 * A model was selected in the comboBox. Shows its correspondent hyperparameters.
	 */
	public void changeModel() {
		String model = this.cmbModel.getValue();
		// Enable selecting the maximum number of parents
		ControllerUtil.showNode(this.hbKParents,
				model.equals("DAG-maxK Multi-CTBNC") || model.equals("Empty-maxK Multi-CTBNC"));
		if (model.equals("Multi-CTNBC")) {
			// Disable learning structure options if learning a naive Bayes classifier
			this.cmbStructure.setDisable(true);
			this.cmbInitialStructure.setDisable(true);
			this.cmbScoreFunction.setDisable(true);
			this.cmbPenalisation.setDisable(true);
		} else {
			this.cmbStructure.setDisable(false);
			this.cmbInitialStructure.setDisable(false);
			this.cmbScoreFunction.setDisable(false);
			this.cmbPenalisation.setDisable(false);
		}
	}

	/**
	 * A parameter learning algorithm for BNs was selected in the comboBox. Its corresponding parameters are shown.
	 */
	public void changeParameterLearningAlgBN() {
		ControllerUtil.showNode(this.hbHyperBN, this.cmbParameterBN.getValue().equals("Bayesian estimation"));
	}

	/**
	 * A parameter learning algorithm for CTBNs was selected in the comboBox. Shows its correspondent parameters.
	 */
	public void changeParameterLearningAlgCTBN() {
		ControllerUtil.showNode(this.hbHyperCTBN, this.cmbParameterCTBN.getValue().equals("Bayesian estimation"));
	}

	/**
	 * A score function was selected in the comboBox. Shows its correspondent parameters.
	 */
	public void changeScoreFunction() {
		ControllerUtil.showNode(this.hbPenalisation,
				!this.cmbScoreFunction.getValue().equals("Bayesian Dirichlet equivalent"));
	}

	/**
	 * A structure learning algorithm was selected in the comboBox. Shows its correspondent parameters.
	 */
	public void changeStructureLearningAlg() {
		String structureLearningAlg = this.cmbStructure.getValue();
		// Score-based algorithms
		if (structureLearningAlg.equals("Hill climbing") ||
				structureLearningAlg.equals("Random-restart hill climbing") ||
				structureLearningAlg.equals("Tabu search")) {
			showScoreBasedAlg();
			if (structureLearningAlg.equals("Hill climbing"))
				showHillClimbing();
			if (structureLearningAlg.equals("Random-restart hill climbing"))
				showRandomRestartHillClimbing();
			if (structureLearningAlg.equals("Tabu search"))
				showTabuSearch();
		}
		// Constraint-based algorithms
		if (structureLearningAlg.equals("CTPC") || structureLearningAlg.equals("MB-CTPC"))
			showConstraintBasedAlg();
		// Hybrid algorithms
		if (structureLearningAlg.equals("Hybrid algorithm"))
			showHybridAlg();
	}

	/**
	 * Performs classification over a provided dataset with a previously trained model.
	 */
	public void classify() {
		if (this.model.get() != null && this.ClassificationDatasetReader != null) {
			// Retrieve time and feature variables that are necessary for the classification
			String nameTimeVariable = this.trainingDatasetReader.getNameTimeVariable();
			List<String> nameFeatureVariables = this.trainingDatasetReader.getNameFeatureVariables();
			// Set the variables
			this.ClassificationDatasetReader.setTimeAndFeatureVariables(nameTimeVariable, nameFeatureVariables);
			// Check if probabilities of predicted class configurations should be estimated
			boolean estimateProbabilities = this.chkProbabilitiesClassification.isSelected();
			// Initialise and restart service to classify the dataset in another thread
			this.classificationService.initialiseService(this.model.get(), this.ClassificationDatasetReader,
					estimateProbabilities);
			this.classificationService.restart();
			// The status label will be updated with the progress of the service
			this.status.textProperty().bind(this.classificationService.messageProperty());
		} else if (this.model.get() == null)
			logger.warn("The classification was not performed. No model has been trained");
		else
			logger.warn("The classification was not performed. No dataset to classify has been provided");
	}

	/**
	 * The information of the dataset used for classification was modified, so its {@code DatasetReader} is warned.
	 * This
	 * is useful to avoid the reloading of the same dataset.
	 */
	public void datasetClassificationModified() {
		if (this.ClassificationDatasetReader != null)
			this.ClassificationDatasetReader.setDatasetAsOutdated(true);
	}

	/**
	 * The information of the dataset for training/evaluation was modified, so its {@code DatasetReader} is warned.
	 * This
	 * is useful to avoid the reloading of the same dataset.
	 */
	public void datasetModified() {
		if (this.trainingDatasetReader != null)
			this.trainingDatasetReader.setDatasetAsOutdated(true);
	}

	/**
	 * Evaluates the selected model.
	 */
	public void evaluate() {
		// Get selected variables
		String nameTimeVariable = this.cmbTimeVariable.getValue();
		List<String> nameClassVariables = this.ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameFeatureVariables = this.ckcmbFeatureVariables.getCheckModel().getCheckedItems();
		if (datasetIsValid(this.trainingDatasetReader, nameTimeVariable, nameClassVariables, nameFeatureVariables)) {
			try {
				// Set the variables that will be used
				this.trainingDatasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
				// Initialise and restart service to evaluate the selected model in another
				// thread
				this.evaluationService.initialiseService(getValidationMethod(), getModel());
				this.evaluationService.restart();
				// The status label will be updated with the progress of the service
				this.status.textProperty().bind(this.evaluationService.messageProperty());
			} catch (UnreadDatasetException ude) {
				// The dataset could not be read due to a problem with the provided files
				logger.error(ude.getMessage());
				this.status.setText(
						"There was an error reading the provided datasets. Check application log for details.");
			}
		} else
			logger.warn("The evaluation was not performed");
	}

	/**
	 * Initialises the controller.
	 */
	@FXML
	public void initialize() {
		initialiseDatasetPane();
		initialiseModelPane();
		initialiseEvaluationPane();
		initialiseClassificationPane();
		setBindings();
	}

	/**
	 * Cross-validation method was selected. Shows its correspondent parameters.
	 */
	public void selectCrossValidation() {
		ControllerUtil.showNode(this.hbNumFolds, true);
		ControllerUtil.showNode(this.hbShuffle, true);
		ControllerUtil.showNode(this.hbTrainingSize, false);
		ControllerUtil.showNode(this.hbTestDataset, false);
	}

	/**
	 * Hold-out-validation method was selected. Shows its correspondent parameters.
	 */
	public void selectHoldOutValidation() {
		ControllerUtil.showNode(this.hbTrainingSize, true);
		ControllerUtil.showNode(this.hbShuffle, true);
		ControllerUtil.showNode(this.hbNumFolds, false);
		ControllerUtil.showNode(this.hbTestDataset, false);
	}

	/**
	 * Test dataset method was selected. Shows its correspondent parameters.
	 */
	public void selectTestDataset() {
		ControllerUtil.showNode(this.hbTrainingSize, false);
		ControllerUtil.showNode(this.hbNumFolds, false);
		ControllerUtil.showNode(this.hbShuffle, false);
		ControllerUtil.showNode(this.hbTestDataset, true);
	}

	/**
	 * Opens a dialog to select the folder where the dataset for training and evaluation is located.
	 *
	 * @throws FileNotFoundException if the provided files were not found
	 */
	public void setFolderDataset() throws FileNotFoundException {
		File selectedFile = getDatasetPath(this.cmbDataFormat);
		if (selectedFile != null) {
			// Show the selected directory
			String pathFolder = selectedFile.getAbsolutePath();
			// Define dataset reader
			initialiseDatasetReader(pathFolder);
			if (this.trainingDatasetReader != null) {
				// Read the variables
				readVariablesDataset();
				// Show dataset folder path in a text field
				this.fldPathDataset.setText(pathFolder);
			}
		}
	}

	/**
	 * Opens a dialog to select the folder where the dataset on which classification is performed is located.
	 *
	 * @throws FileNotFoundException if the provided files were not found
	 */
	public void setFolderDatasetClassification() throws FileNotFoundException {
		File selectedFile = getDatasetPath(this.cmbDataFormatClassification);
		if (selectedFile != null) {
			// Show the selected directory
			String pathFolder = selectedFile.getAbsolutePath();
			// Define dataset reader
			initialiseDatasetReaderClassification(pathFolder);
			if (this.ClassificationDatasetReader != null) {
				// Show folder path of the dataset to classify in text field
				this.fldPathDatasetClassification.setText(pathFolder);
			}
		}
	}

	/**
	 * Opens a dialog to select the folder where the dataset for testing is located.
	 *
	 * @throws FileNotFoundException if the provided files were not found
	 */
	public void setFolderTestDataset() throws FileNotFoundException {
		// Open window to select the folder with the dataset
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(this.stage);
		if (selectedDirectory != null) {
			// Show the selected directory
			String pathFolder = selectedDirectory.getAbsolutePath();
			// Define dataset reader
			initialiseDatasetReaderTesting(pathFolder);
			if (this.testDatasetReader != null) {
				// Show dataset folder path in text field
				this.fldPathTestDataset.setText(pathFolder);
			}
		}
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
	 * Trains the selected model.
	 */
	public void trainModel() {
		// Get selected variables
		String nameTimeVariable = this.cmbTimeVariable.getValue();
		List<String> nameClassVariables = this.ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameFeatureVariables = this.ckcmbFeatureVariables.getCheckModel().getCheckedItems();
		if (datasetIsValid(this.trainingDatasetReader, nameTimeVariable, nameClassVariables, nameFeatureVariables)) {
			// Define a model to train
			this.model.set(getModel());
			// Set the variables that will be used
			this.trainingDatasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
			// Initialise and restart service to train the model in another thread
			this.trainingService.initialiseService(this.model.get(), this.trainingDatasetReader);
			this.trainingService.restart();
			// The status label will be updated with the progress of the service
			this.status.textProperty().bind(this.trainingService.messageProperty());
		} else
			logger.warn("The training was not performed");
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
			logger.warn("A dataset must be provided");
			return false;
		} else if (nameTimeVariable == null) {
			logger.warn("A time variable must be provided");
			return false;
		} else if (nameClassVariables.isEmpty()) {
			logger.warn("At least one class variable must be provided");
			return false;
		} else if (nameFeatureVariables.isEmpty()) {
			logger.warn("At least one feature variable must be provided");
			return false;
		}
		return true;
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
		// Get parameters structure learning algorithm
		// Get score function
		String scoreFunction = this.cmbScoreFunction.getValue();
		// Define penalisation function (if any)
		String penalisationFunction = this.cmbPenalisation.getValue();
		// Get the number of restarts (for random restart hill climbing)
		int numRestarts = ControllerUtil.extractInteger(this.fldRestarts.getText(), 5);
		// Get significance level (for constraint-based algorithms)
		double significance = ControllerUtil.extractDecimal(this.fldSigClassSubgraph.getText(), 0.05);
		// Get the size of the tabu list
		int tabuListSize = ControllerUtil.extractInteger(this.fldSizeTabuList.getText(), 5);
		// Save parameters of the structure learning algorithm
		Map<String, String> paramSLA = new HashMap<>();
		paramSLA.put("scoreFunction", scoreFunction);
		paramSLA.put("penalisationFunction", penalisationFunction);
		paramSLA.put("significancePC", String.valueOf(significance));
		paramSLA.put("numRestarts", String.valueOf(numRestarts));
		paramSLA.put("tabuListSize", String.valueOf(tabuListSize));
		// Define learning algorithms for the class subgraph (Bayesian network)
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA, paramSLA);
		return new BNLearningAlgorithms(bnPLA, bnSLA);
	}

	/**
	 * Return the selected learning algorithms for the continuous-time Bayesian network.
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
		// Define penalisation function (if any)
		String penalisationFunction = this.cmbPenalisation.getValue();
		// Get number of restarts (for random restart hill climbing)
		int numRestarts = ControllerUtil.extractInteger(this.fldRestarts.getText(), 5);
		// Get significance level (for constraint-based algorithms)
		double significance = ControllerUtil.extractDecimal(this.fldSigFeatureBridgeSubgraph.getText(), 0.00001);
		// Get maximum size separation set (for the hybrid algorithm)
		int maxSizeSepSet = ControllerUtil.extractInteger(this.fldMaxSizeSepSet.getText(), 1);
		// Get size of tabu list
		int tabuListSize = ControllerUtil.extractInteger(this.fldSizeTabuList.getText(), 5);
		// Save parameters of the structure learning algorithm
		Map<String, String> paramSLA = new HashMap<>();
		paramSLA.put("scoreFunction", scoreFunction);
		paramSLA.put("penalisationFunction", penalisationFunction);
		paramSLA.put("sigTimeTransitionHyp", String.valueOf(significance));
		paramSLA.put("sigStateToStateTransitionHyp", String.valueOf(significance));
		paramSLA.put("numRestarts", String.valueOf(numRestarts));
		paramSLA.put("tabuListSize", String.valueOf(tabuListSize));
		paramSLA.put("maxSizeSepSet", String.valueOf(maxSizeSepSet));
		// Define learning algorithms for the feature and class subgraph (Continuous
		// time Bayesian network)
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxyHP,
				txHP);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA, paramSLA);
		return new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
	}

	/**
	 * Returns a {@code BooleanBinding} for the classification button. It checks if the model was not trained or is
	 * being trained, if a dataset is being classified and if a training dataset was not provided. If one of these
	 * conditions is met, the button should be disabled.
	 *
	 * @return {@code BooleanBinding} for the classification button
	 */
	private BooleanBinding getBindingClassifyBtn() {
		return this.model.isNull().or(this.fldPathDatasetClassification.textProperty().isEmpty()).or(
				this.trainingService.runningProperty()).or(this.classificationService.runningProperty());
	}

	/**
	 * Returns a {@code BooleanBinding} for the evaluation button. It checks if the model is being evaluated, and if a
	 * dataset was not provided. If one of these conditions is met, the button should be disabled.
	 *
	 * @return {@code BooleanBinding} for the evaluation button
	 */
	private BooleanBinding getBindingEvaluateBtn() {
		return Bindings.or(this.fldPathDataset.textProperty().isEmpty(), this.evaluationService.runningProperty());

	}

	// ---------- onAction methods ----------

	/**
	 * Returns a {@code BooleanBinding} for the training button. It checks if the model is being trained, if a dataset
	 * is being classified, and if a training dataset was not provided. If one of these conditions is met, the button
	 * should be disabled.
	 *
	 * @return {@code BooleanBinding} for the training button
	 */
	private BooleanBinding getBindingTrainBtn() {
		return this.fldPathDataset.textProperty().isEmpty().or(this.trainingService.runningProperty()).or(
				this.classificationService.runningProperty());
	}

	/**
	 * Opens a dialog to select the dataset path.
	 *
	 * @return a {@code File} with the dataset path
	 */
	private File getDatasetPath(ComboBox comboBoxDatasetSelection) {
		File selectedFile;
		if (comboBoxDatasetSelection.getValue().equals("Single CSV")) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
			selectedFile = fileChooser.showOpenDialog(this.stage);
		} else {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			selectedFile = directoryChooser.showDialog(this.stage);
		}
		return selectedFile;
	}

	/**
	 * Return the selected model.
	 *
	 * @return a {@code MultiCTBNC<CPTNode, CIMNode>}
	 */
	private MultiCTBNC<CPTNode, CIMNode> getModel() {
		// Retrieve learning algorithms
		BNLearningAlgorithms bnLearningAlgs = getAlgorithmsBN();
		CTBNLearningAlgorithms ctbnLearningAlgs = getAlgorithmsCTBN();
		// Hyperparameters that could be necessary for the generation of the model
		Map<String, String> hyperparameters = new WeakHashMap<>();
		hyperparameters.put("maxK", this.fldKParents.getText());
		// Generate selected model
		String selectedModel = this.cmbModel.getValue();
		MultiCTBNC<CPTNode, CIMNode> model = ClassifierFactory.getMultiCTBNC(selectedModel, bnLearningAlgs,
				ctbnLearningAlgs, hyperparameters, CPTNode.class, CIMNode.class);
		// Define initial structure
		model.setIntialStructure(this.cmbInitialStructure.getValue());
		return model;
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
		// Define if sequences are shuffled before applying the validation method
		boolean shuffleSequences = this.chkShuffle.isSelected();
		// Define if the probabilities of the class configurations are estimated
		boolean estimateProbabilities = this.chkProbabilities.isSelected();
		// Retrieve parameters of the validation methods
		double trainingSize = this.sldTrainingSize.getValue();
		int folds = Integer.valueOf(this.fldNumFolds.getText());
		// Retrieve algorithm of the validation method
		return ValidationMethodFactory.getValidationMethod(selectedValidationMethod, this.trainingDatasetReader,
				this.testDatasetReader, trainingSize, folds, estimateProbabilities, shuffleSequences, null);
	}

	/**
	 * Initialises the controllers of the classification pane.
	 */
	private void initialiseClassificationPane() {
		// Initialise options of comboBoxes
		this.cmbDataFormatClassification.getItems().addAll(this.datasetReaders);
		this.cmbStrategyClassification.getItems().addAll(this.datasetReaderStrategies);
		// Select first option as default in comboBoxes
		this.cmbDataFormatClassification.getSelectionModel().selectFirst();
		this.cmbStrategyClassification.getSelectionModel().selectFirst();
		ControllerUtil.showNode(this.hbStrategyClassification, false);
		// Initialise text fields with default values
		this.fldSizeSequencesClassification.setText("100");
		ControllerUtil.showNode(this.hbSizeSequencesClassification, false);
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldSizeSequencesClassification);
	}

	/**
	 * Initialises the controllers of the model pane.
	 */
	private void initialiseDatasetPane() {
		// Initialise options of comboBoxes
		this.cmbDataFormat.getItems().addAll(this.datasetReaders);
		this.cmbStrategy.getItems().addAll(this.datasetReaderStrategies);
		// Select first option as default in comboBoxes
		this.cmbDataFormat.getSelectionModel().selectFirst();
		this.cmbStrategy.getSelectionModel().selectFirst();
		ControllerUtil.showNode(this.hbStrategy, false);
		// Initialise text fields with default values
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
		// Text fields are restricted to specific values
		ControllerUtil.onlyPositiveInteger(this.fldSizeSequences);
	}

	/**
	 * Initialises the dataset reader given the path of the dataset folder.
	 *
	 * @param pathFolder path of the dataset folder
	 * @throws FileNotFoundException if the provided files were not found
	 */
	private void initialiseDatasetReader(String pathFolder) throws FileNotFoundException {
		String nameDatasetReader = this.cmbDataFormat.getValue();
		int sizeSequence = ControllerUtil.extractInteger(this.fldSizeSequences.getText(), 0);
		this.trainingDatasetReader = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder,
				sizeSequence);
	}

	/**
	 * Initialises the classification dataset reader given the path of the dataset folder.
	 *
	 * @param pathFolder path of the dataset folder
	 * @throws FileNotFoundException if the provided files were not found
	 */
	private void initialiseDatasetReaderClassification(String pathFolder) throws FileNotFoundException {
		String nameDatasetReader = this.cmbDataFormatClassification.getValue();
		int sizeSequence = Integer.valueOf(this.fldSizeSequencesClassification.getText());
		this.ClassificationDatasetReader = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder,
				sizeSequence);
		this.ClassificationDatasetReader.removeZeroVarianceVariables(false);
	}

	/**
	 * Initialises the test dataset reader given the path of the dataset folder.
	 *
	 * @param pathFolder path of the dataset folder
	 * @throws FileNotFoundException if the provided files were not found
	 */
	private void initialiseDatasetReaderTesting(String pathFolder) throws FileNotFoundException {
		String nameDatasetReader = this.cmbDataFormat.getValue();
		this.testDatasetReader = DatasetReaderFactory.getDatasetReader(nameDatasetReader, pathFolder, 0);
		String nameTimeVariable = this.cmbTimeVariable.getValue();
		List<String> nameClassVariables = this.ckcmbClassVariables.getCheckModel().getCheckedItems();
		List<String> nameFeatureVariables = this.ckcmbFeatureVariables.getCheckModel().getCheckedItems();
		this.testDatasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
	}

	/**
	 * Initialises the controllers of the evaluation pane.
	 */
	private void initialiseEvaluationPane() {
		// Hold-out validation by default
		selectHoldOutValidation();
		// Initialise text fields with default values
		this.sldTrainingSize.setValue(0.7);
		this.fldNumFolds.setText("5");
		// Text values are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldNumFolds);
		// By default, the sequences are shuffled
		this.chkShuffle.setSelected(true);
		// By default, the probabilities of the class configurations are estimated
		this.chkProbabilities.setSelected(true);
	}

	/**
	 * Initialises the controllers of the model pane.
	 */
	private void initialiseModelPane() {
		// Initialise options of comboBoxes
		this.cmbModel.getItems().addAll(this.models);
		this.cmbParameterBN.getItems().addAll(this.parameterLearningAlgs);
		this.cmbParameterCTBN.getItems().addAll(this.parameterLearningAlgs);
		this.cmbStructure.getItems().addAll(this.structureLearningAlgs);
		this.cmbPenalisation.getItems().addAll(this.penalisations);
		this.cmbInitialStructure.getItems().addAll(this.initialStructures);
		this.cmbScoreFunction.getItems().addAll(this.scores);
		// Select first option as default in comboBoxes
		this.cmbModel.getSelectionModel().selectFirst();
		this.cmbParameterBN.getSelectionModel().selectFirst();
		this.cmbParameterCTBN.getSelectionModel().selectFirst();
		this.cmbStructure.getSelectionModel().selectFirst();
		this.cmbPenalisation.getSelectionModel().selectFirst();
		this.cmbInitialStructure.getSelectionModel().selectFirst();
		this.cmbScoreFunction.getSelectionModel().selectFirst();
		// Initialise text fields with default values
		this.fldKParents.setText("2");
		this.fldNx.setText("1");
		this.fldMxy.setText("1");
		this.fldTx.setText("0.001");
		this.fldRestarts.setText("5");
		this.fldSigClassSubgraph.setText("0.05");
		this.fldSigFeatureBridgeSubgraph.setText("0.00001");
		this.fldMaxSizeSepSet.setText("1");
		this.fldSizeTabuList.setText("5");
		// Hide parameters
		ControllerUtil.showNode(this.hbKParents, false);
		ControllerUtil.showNode(this.hbHyperBN, false);
		ControllerUtil.showNode(this.hbHyperCTBN, false);
		ControllerUtil.showNode(this.hbRestarts, false);
		ControllerUtil.showNode(this.hbSignificanceLevel, false);
		ControllerUtil.showNode(this.hbMaxSizeSepSet, false);
		ControllerUtil.showNode(this.hbSizeTabuList, false);
		// Text fields are restricted to certain values
		ControllerUtil.onlyPositiveInteger(this.fldKParents);
		ControllerUtil.onlyPositiveInteger(this.fldRestarts);
		ControllerUtil.onlyPositiveInteger(this.fldSizeTabuList);
		ControllerUtil.onlyPositiveDouble(this.fldNx);
		ControllerUtil.onlyPositiveDouble(this.fldMxy);
		ControllerUtil.onlyPositiveDouble(this.fldTx);
		ControllerUtil.onlyPositiveDouble(this.fldSigClassSubgraph);
		ControllerUtil.onlyPositiveDouble(this.fldSigFeatureBridgeSubgraph);
		ControllerUtil.onlyZeroOrGreaterInteger(this.fldMaxSizeSepSet);
	}

	/**
	 * Obtains the variables of the selected dataset to add them to the comboBoxes.
	 */
	private void readVariablesDataset() {
		if (this.trainingDatasetReader != null) {
			// Names of the variables are retrieved
			List<String> nameVariables = this.trainingDatasetReader.getNameVariables();
			// If another dataset was used before, comboBoxes are reseted
			resetCheckComboBoxes();
			// Variables' names are added to the comboBoxes
			this.ckcmbFeatureVariables.getItems().addAll(nameVariables);
			this.cmbTimeVariable.getItems().addAll(nameVariables);
			this.ckcmbClassVariables.getItems().addAll(nameVariables);
		}
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

	private void showConstraintBasedAlg() {
		// Show common options for score-based algorithms
		ControllerUtil.showNode(this.hbSignificanceLevel, true);
		// Hide options not used by score-based algorithms
		ControllerUtil.showNode(this.hbInitialStructure, false);
		ControllerUtil.showNode(this.hbScore, false);
		ControllerUtil.showNode(this.hbPenalisation, false);
		ControllerUtil.showNode(this.hbRestarts, false);
		ControllerUtil.showNode(this.hbSizeTabuList, false);
		ControllerUtil.showNode(this.hbMaxSizeSepSet, false);
		cmbModel.setValue("Multi-CTBNC");
		cmbModel.setDisable(true);
	}

	private void showHillClimbing() {
		ControllerUtil.showNode(this.hbRestarts, false);
		ControllerUtil.showNode(this.hbSizeTabuList, false);
	}

	private void showHybridAlg() {
		ControllerUtil.showNode(this.hbScore, true);
		ControllerUtil.showNode(this.hbPenalisation, true);
		ControllerUtil.showNode(this.hbSignificanceLevel, true);
		ControllerUtil.showNode(this.hbMaxSizeSepSet, true);
		ControllerUtil.showNode(this.hbInitialStructure, false);
		ControllerUtil.showNode(this.hbRestarts, false);
		ControllerUtil.showNode(this.hbSizeTabuList, false);
		cmbModel.setValue("Multi-CTBNC");
		cmbModel.setDisable(true);
	}

	private void showRandomRestartHillClimbing() {
		ControllerUtil.showNode(this.hbRestarts, true);
		ControllerUtil.showNode(this.hbSizeTabuList, false);
	}

	private void showScoreBasedAlg() {
		// Show common options for score-based algorithms
		ControllerUtil.showNode(this.hbInitialStructure, true);
		ControllerUtil.showNode(this.hbScore, true);
		ControllerUtil.showNode(this.hbPenalisation, true);
		cmbModel.setDisable(false);
		// Hide options not used by score-based algorithms
		ControllerUtil.showNode(this.hbSignificanceLevel, false);
		ControllerUtil.showNode(this.hbMaxSizeSepSet, false);
	}

	private void showTabuSearch() {
		ControllerUtil.showNode(this.hbSizeTabuList, true);
		ControllerUtil.showNode(this.hbRestarts, false);
	}

}