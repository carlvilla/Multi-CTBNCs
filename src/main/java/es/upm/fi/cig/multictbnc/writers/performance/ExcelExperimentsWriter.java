package es.upm.fi.cig.multictbnc.writers.performance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;

/**
 * 
 * Allows writing the results of the experiments in an Excel file.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ExcelExperimentsWriter extends MetricsWriter {
	String path = "results/experiments/";
	XSSFWorkbook workbook;
	XSSFSheet sheet;
	FileOutputStream out = null;
	int numDatasets;
	int numModels;
	int numScoreFunctions;
	int idxCurrentDataset = 0;
	int idxCurrentModel = 0;
	int idxCurrentScoreFunction = 0;
	List<Long> seeds;
	List<String> metrics = List.of("Global accuracy", "Mean accuracy", "Macro-averaged precision",
			"Macro-averaged recall", "Macro-averaged F1 score", "Micro-averaged precision", "Micro-averaged recall",
			"Micro-averaged F1 score", "Global Brier score");
	static Logger logger = LogManager.getLogger(ExcelExperimentsWriter.class);

	/**
	 * Initializes the writer.
	 * 
	 * @param scoreFunctions       list of score functions
	 * @param nameDatasets         list of dataset names
	 * @param nameModels           list of model names
	 * @param nameFeatureVariables names of feature variables
	 * @param nameClassVariables   names of class variables
	 * @param bnPLA                parameter learning algorithm for a Bayesian
	 *                             network
	 * @param ctbnPLA              parameter learning algorithm for a continuous
	 *                             time Bayesian network
	 * @param penalizationFunction name of the penalization function
	 * @param initialStructure     name for the initial structure
	 * @param seeds                seeds that are used to shuffle the datasets. It
	 *                             is assumed that each dataset will be evaluated
	 *                             with each of those seeds
	 * 
	 */
	public ExcelExperimentsWriter(List<String> scoreFunctions, List<String> nameDatasets, List<String> nameModels,
			List<String> nameClassVariables, List<String> nameFeatureVariables, BNParameterLearningAlgorithm bnPLA,
			CTBNParameterLearningAlgorithm ctbnPLA, String penalizationFunction, String initialStructure,
			List<Long> seeds) {
		// Set values global variables
		this.numDatasets = nameDatasets.size();
		this.numModels = nameModels.size();
		this.numScoreFunctions = scoreFunctions.size();
		this.nameClassVariables = nameClassVariables;
		if (seeds.size() > 1)
			// It will be generated as many shuffled datasets as specified seeds
			this.numDatasets *= seeds.size();
		// Create a workbook, sheet and file to store the results
		this.workbook = new XSSFWorkbook();
		this.sheet = this.workbook.createSheet("Experiments");
		String fileName = new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
		try {
			File file = new File(this.path + fileName);
			file.getParentFile().mkdirs();
			this.out = new FileOutputStream(file);
			// Conditions experiments
			writeConditionsExperiment(bnPLA, ctbnPLA, nameFeatureVariables, penalizationFunction, initialStructure);
			for (int i = 0; i < scoreFunctions.size(); i++) {
				// Get initial row in the excel file for the current score function
				int initialRow = getInitialRowScore(i);
				// Score function
				setScoreFunction(initialRow, scoreFunctions.get(i));
				// Table with results
				generateTableDatasets(initialRow, nameDatasets, nameModels, seeds);
				// Comparison of evaluation metrics
				generateTableComparionMetrics(initialRow, nameModels);
				// Comparison of evaluation metrics per class variable
				generateTableComparionPerClassVariable(initialRow, nameModels, nameClassVariables);
				// Set formulas
				setFormulas(initialRow, nameModels, nameClassVariables);
				// Conditional formatting
				setConditionalFormating(initialRow);
			}
			// Merge title dataset
			this.sheet.autoSizeColumn(10);
		} catch (IOException e) {
			logger.error("An error occurred while creating an Excel file to write the experimment results: {}",
					e.getMessage());
		}
	}

	@Override
	public void write(Map<String, Double> results) {
		double ga = results.get("Global accuracy");
		double ma = results.get("Mean accuracy");
		double map = results.get("Macro-averaged precision");
		double mar = results.get("Macro-averaged recall");
		double maf1 = results.get("Macro-averaged F1 score");
		double mip = results.get("Micro-averaged precision");
		double mir = results.get("Micro-averaged recall");
		double mif1 = results.get("Micro-averaged F1 score");
		Double gb = results.getOrDefault("Global Brier score", null);
		// Get initial row in the excel for current score
		int initialRow = getInitialRowScore(this.idxCurrentScoreFunction);
		int numColumn = this.idxCurrentModel + (this.numModels * this.idxCurrentDataset) + 1;
		// Evaluation metrics per dataset and model
		this.sheet.getRow(initialRow + 3).createCell(numColumn).setCellValue(ga);
		this.sheet.getRow(initialRow + 4).createCell(numColumn).setCellValue(ma);
		this.sheet.getRow(initialRow + 5).createCell(numColumn).setCellValue(map);
		this.sheet.getRow(initialRow + 6).createCell(numColumn).setCellValue(mar);
		this.sheet.getRow(initialRow + 7).createCell(numColumn).setCellValue(maf1);
		this.sheet.getRow(initialRow + 8).createCell(numColumn).setCellValue(mip);
		this.sheet.getRow(initialRow + 9).createCell(numColumn).setCellValue(mir);
		this.sheet.getRow(initialRow + 10).createCell(numColumn).setCellValue(mif1);
		if (gb != null)
			this.sheet.getRow(initialRow + 11).createCell(numColumn).setCellValue(gb);
		// Evaluation metrics per class variables
		int initialRowClasses = initialRow + 19 + 2 * this.numModels;
		for (int i = 0; i < this.nameClassVariables.size(); i++) {
			double acc = results.get("Accuracy " + this.nameClassVariables.get(i));
			this.sheet.getRow(
					initialRowClasses + 2 + this.idxCurrentDataset + (this.numDatasets + 2) * this.idxCurrentModel)
					.createCell(2 * i + 1).setCellValue(acc);
		}
		// It is updated the reference to the currently evaluated dataset and model
		if (this.idxCurrentModel == this.numModels - 1 && this.idxCurrentDataset == this.numDatasets - 1) {
			// All the models where studied for all datasets
			this.idxCurrentModel = 0;
			this.idxCurrentDataset = 0;
			this.idxCurrentScoreFunction++;
		} else if (this.idxCurrentModel == this.numModels - 1) {
			// All the models where studied for the current dataset
			this.idxCurrentModel = 0;
			this.idxCurrentDataset++;
		} else
			this.idxCurrentModel++;
	}

	@Override
	public void close() {
		try {
			// Set size of columns to fit all the text
			this.sheet.setDefaultColumnWidth(16);
			this.sheet.autoSizeColumn(0);
			// Write results to file
			this.workbook.write(this.out);
			this.out.flush();
			this.out.close();
		} catch (IOException e) {
			logger.error("An error occurred while closing the Excel experiment writer: {}", e.getMessage());
		}
	}

	/**
	 * Writes in the excel the score function that is being used.
	 * 
	 * @param initialRow    initial row in the excel file for the current score
	 *                      function
	 * @param scoreFunction name of the score function
	 */
	private void setScoreFunction(int initialRow, String scoreFunction) {
		this.sheet.addMergedRegion(new CellRangeAddress(initialRow, initialRow, 0, 4));
		this.sheet.createRow(initialRow).createCell(0).setCellValue("Score function: " + scoreFunction);
		XSSFFont font = this.workbook.createFont();
		font.setBold(true);
		CellStyle styleCS = this.workbook.createCellStyle();
		styleCS.setFont(font);
		this.sheet.getRow(initialRow).getCell(0).setCellStyle(styleCS);
	}

	/**
	 * Writes information about the conditions in which the experiments were
	 * performed.
	 * 
	 * @param bnPLA                parameter learning algorithm for a Bayesian
	 *                             network
	 * @param ctbnPLA              parameter learning algorithm for a continuous
	 *                             time Bayesian network
	 * @param nameFeatureVariables names of feature variables
	 * @param penalizationFunction name of the penalization function
	 * @param initialStructure     name for the initial structure
	 */
	private void writeConditionsExperiment(BNParameterLearningAlgorithm bnPLA, CTBNParameterLearningAlgorithm ctbnPLA,
			List<String> nameFeatureVariables, String penalizationFunction, String initialStructure) {
		// Enable newlines in cells
		CellStyle newLineCS = this.workbook.createCellStyle();
		newLineCS.setWrapText(true);
		// Conditions experiments
		XSSFRichTextString rts = new XSSFRichTextString();
		XSSFFont fontBold = this.workbook.createFont();
		fontBold.setBold(true);
		rts.append("Experiment conditions\n", fontBold);
		rts.append("Feature variables: " + nameFeatureVariables + "\nClass variables: " + this.nameClassVariables
				+ " \nPenalization: " + penalizationFunction + "\nParameter learning alg. class subgraph: "
				+ bnPLA.getNameMethod() + "\nParameter learning alg. bridge and feature subgraphs: "
				+ ctbnPLA.getNameMethod() + "\nInitial structure: " + initialStructure);
		this.sheet.addMergedRegion(new CellRangeAddress(3, 9, 1, 10));
		this.sheet.createRow(3).createCell(1).setCellStyle(newLineCS);
		this.sheet.getRow(3).getCell(1).setCellValue(rts);
	}

	/**
	 * Generates a table where it is shown the results of each model for each
	 * dataset and evaluation metric.
	 * 
	 * @param seeds
	 */
	private void generateTableDatasets(int initialRow, List<String> nameDatasets, List<String> nameModels,
			List<Long> seeds) {
		// Results for each of the datasets
		this.sheet.createRow(initialRow + 1);
		this.sheet.createRow(initialRow + 2);
		if (seeds.size() > 1) {
			// More than one seed is specified, so more than one evaluation is performed per
			// dataset and model
			for (int i = 0; i < nameDatasets.size(); i++) {
				for (int j = 0; j < seeds.size(); j++) {
					this.sheet.getRow(initialRow + 1).createCell(1 + this.numModels * (i + j))
							.setCellValue(nameDatasets.get(i) + " (Shuffling seed: " + seeds.get(j) + ")");
					this.sheet.addMergedRegion(new CellRangeAddress(initialRow + 1, initialRow + 1,
							1 + this.numModels * (i + j), this.numModels * (i + j + 1)));
					for (int k = 0; k < this.numModels; k++) {
						this.sheet.getRow(initialRow + 2).createCell(1 + this.numModels * (i + j) + k)
								.setCellValue(nameModels.get(k));
					}
				}
			}
		} else {
			// No more than one seed is specified, so one evaluation is performed per
			// dataset and model
			for (int i = 0; i < this.numDatasets; i++) {
				this.sheet.getRow(initialRow + 1).createCell(1 + this.numModels * i).setCellValue(nameDatasets.get(i));
				this.sheet.addMergedRegion(new CellRangeAddress(initialRow + 1, initialRow + 1, 1 + this.numModels * i,
						this.numModels * (i + 1)));
				for (int j = 0; j < this.numModels; j++) {
					this.sheet.getRow(initialRow + 2).createCell(1 + this.numModels * i + j)
							.setCellValue(nameModels.get(j));
				}
			}
		}

	}

	/**
	 * Generates a table where it is compare the results of each model.
	 */
	private void generateTableComparionMetrics(int initialRow, List<String> nameModels) {
		int initialRowTable = initialRow + this.metrics.size() + 6;
		this.sheet.createRow(initialRowTable);
		this.sheet.createRow(initialRowTable + 1).createCell(0).setCellValue("Model");
		for (int i = 0; i < this.metrics.size(); i++) {
			this.sheet.createRow(initialRow + 3 + i).createCell(0).setCellValue(this.metrics.get(i));
			this.sheet.addMergedRegion(new CellRangeAddress(initialRowTable, initialRowTable, i * 2 + 1, i * 2 + 2));
			this.sheet.getRow(initialRowTable).createCell(i * 2 + 1).setCellValue(this.metrics.get(i));
		}
		for (int i = 0; i < this.metrics.size(); i++) {
			this.sheet.getRow(initialRowTable + 1).createCell(i * 2 + 1).setCellValue("Mean");
			this.sheet.getRow(initialRowTable + 1).createCell(i * 2 + 2).setCellValue("Std. deviation");
		}
		for (int i = 0; i < nameModels.size(); i++) {
			this.sheet.createRow(initialRowTable + 2 + i).createCell(0).setCellValue(nameModels.get(i));
			if (i < nameModels.size() - 1)
				this.sheet.createRow(initialRowTable + 3 + this.numModels + i).createCell(0)
						.setCellValue(nameModels.get(this.numModels - 1) + " vs. " + nameModels.get(i));
		}
	}

	/**
	 * Generates a table where it is compare the results of each model for each
	 * class variable.
	 */
	private void generateTableComparionPerClassVariable(int initialRow, List<String> nameModels,
			List<String> nameClassVariables) {
		int initialRowClasses = initialRow + 19 + 2 * this.numModels;
		this.sheet.addMergedRegion(new CellRangeAddress(initialRowClasses, initialRowClasses + 1, 0, 0));
		this.sheet.createRow(initialRowClasses).createCell(0).setCellValue("Model");
		this.sheet.createRow(initialRowClasses + 1);
		for (int i = 0; i < nameClassVariables.size(); i++) {
			this.sheet
					.addMergedRegion(new CellRangeAddress(initialRowClasses, initialRowClasses, 2 * i + 1, 2 * i + 2));
			this.sheet.getRow(initialRowClasses).createCell(2 * i + 1).setCellValue(nameClassVariables.get(i));
			this.sheet.getRow(initialRowClasses + 1).createCell(2 * i + 1).setCellValue("Mean");
			this.sheet.getRow(initialRowClasses + 1).createCell(2 * i + 2).setCellValue("Std. deviation");
		}
		initialRowClasses += 2;
		for (int i = 0; i < nameModels.size(); i++) {
			this.sheet.addMergedRegion(new CellRangeAddress(initialRowClasses + (this.numDatasets + 2) * i,
					initialRowClasses + (this.numDatasets + 2) * i + this.numDatasets, 0, 0));
			this.sheet.createRow(initialRowClasses + (this.numDatasets + 2) * i).createCell(0)
					.setCellValue(nameModels.get(i));
			this.sheet.createRow(initialRowClasses + (this.numDatasets + 2) * i + this.numDatasets);
			// Create rows for the results of the evaluation metrics for each class
			// variable, dataset and model
			for (int j = 1; j < this.numDatasets; j++) {
				this.sheet.createRow(initialRowClasses + (this.numDatasets + 2) * i + j);
			}
		}
		initialRowClasses += (this.numDatasets + 2) * this.numModels;
		for (int i = 0; i < nameModels.size(); i++) {
			if (i < nameModels.size() - 1)
				this.sheet.createRow(initialRowClasses + i).createCell(0)
						.setCellValue(nameModels.get(this.numModels - 1) + " vs. " + nameModels.get(i));
		}
	}

	/**
	 * Sets formulas in certain cells.
	 * 
	 * @param initialRow         initial row in the excel file for the current score
	 *                           function
	 * @param nameModels         list of model names
	 * @param nameClassVariables names of class variables
	 */
	private void setFormulas(int initialRow, List<String> nameModels, List<String> nameClassVariables) {
		// Evaluation metrics
		for (int i = 0; i < this.numModels; i++)
			for (int j = 0; j < this.metrics.size(); j++) {
				String strFormula = "";
				for (int k = 0; k < this.numDatasets; k++) {
					int row = initialRow + 4 + j;
					String col = columnName(1 + k * this.numModels + i);
					strFormula += col + row;
					if (k < this.numDatasets - 1)
						strFormula += ",";
				}
				this.sheet.getRow(initialRow + 17 + i).createCell(1 + j * 2)
						.setCellFormula("AVERAGE(" + strFormula + ")");
				this.sheet.getRow(initialRow + 17 + i).createCell((1 + j) * 2)
						.setCellFormula("STDEV(" + strFormula + ")");
			}
		for (int i = 0; i < this.metrics.size(); i++) {
			String col = columnName(1 + i * 2);
			for (int j = 1; j < this.numModels; j++) {
				String strFormula = col + (initialRow + 17 + this.numModels) + " - " + col + (initialRow + 17 + j);
				this.sheet.getRow(initialRow + 17 + this.numModels + j).createCell(1 + i * 2)
						.setCellFormula(strFormula);
			}
		}
		// Table class variables
		int initialRowClasses = initialRow + 21 + 2 * this.numModels;
		for (int i = 0; i < nameModels.size(); i++) {
			for (int j = 0; j < nameClassVariables.size(); j++) {
				this.sheet.getRow(initialRowClasses + (this.numDatasets + 2) * i + this.numDatasets)
						.createCell(1 + j * 2)
						.setCellFormula("AVERAGE(" + columnName(1 + j * 2)
								+ (initialRowClasses + 1 + (this.numDatasets + 2) * i) + ":" + columnName(1 + j * 2)
								+ (initialRowClasses + this.numDatasets + (this.numDatasets + 2) * i) + ")");
				this.sheet.getRow(initialRowClasses + (this.numDatasets + 2) * i + this.numDatasets)
						.createCell(2 + j * 2)
						.setCellFormula("STDEV(" + columnName(1 + j * 2)
								+ (initialRowClasses + 1 + (this.numDatasets + 2) * i) + ":" + columnName(1 + j * 2)
								+ (initialRowClasses + this.numDatasets + (this.numDatasets + 2) * i) + ")");
			}
		}
		for (int i = 0; i < nameClassVariables.size(); i++) {
			String col = columnName(1 + i * 2);
			int rowLastModel = initialRowClasses + (this.numDatasets + 1) * this.numModels + this.numModels - 1;
			for (int j = 0; j < this.numModels - 1; j++) {
				int rowOtherModel = initialRowClasses + ((this.numDatasets + 1) * (j + 1)) + j;
				String strFormula = col + rowLastModel + " - " + col + rowOtherModel;
				this.sheet.getRow(
						initialRowClasses + ((this.numDatasets + 1) * this.numModels) + (this.numModels - 1) + j + 1)
						.createCell(1 + i * 2).setCellFormula(strFormula);
			}
		}
	}

	/**
	 * Defines the conditional formating used for some cells.
	 * 
	 * @param initialRow initial row in the excel file for the current score
	 *                   function
	 */
	private void setConditionalFormating(int initialRow) {
		XSSFSheetConditionalFormatting condFormat = this.sheet.getSheetConditionalFormatting();
		XSSFConditionalFormattingRule moreRule = condFormat.createConditionalFormattingRule(ComparisonOperator.GT, "0");
		XSSFConditionalFormattingRule lessRule = condFormat.createConditionalFormattingRule(ComparisonOperator.LT, "0");
		moreRule.createPatternFormatting().setFillBackgroundColor(IndexedColors.GREEN.getIndex());
		lessRule.createPatternFormatting().setFillBackgroundColor(IndexedColors.RED.getIndex());
		for (int i = 1; i < this.numModels; i++) {
			CellRangeAddress[] rangeFormating = { CellRangeAddress.valueOf("B" + (initialRow + 17 + this.numModels + i)
					+ ":" + columnName(this.metrics.size() * 2 - 1) + (initialRow + 18 + this.numModels + i)) };
			condFormat.addConditionalFormatting(rangeFormating, moreRule);
			condFormat.addConditionalFormatting(rangeFormating, lessRule);
		}
		int initialRowClasses = initialRow + 21 + 2 * this.numModels;
		CellRangeAddress[] rangeFormating = { CellRangeAddress.valueOf(
				"B" + (initialRowClasses + ((this.numDatasets + 1) * this.numModels) + (this.numModels - 1) + 1) + ":"
						+ columnName(this.nameClassVariables.size() * 2 - 1)
						+ ((initialRowClasses + ((this.numDatasets + 1) * this.numModels) + (this.numModels - 1) + 1)
								+ this.numModels - 1)) };
		condFormat.addConditionalFormatting(rangeFormating, moreRule);
		condFormat.addConditionalFormatting(rangeFormating, lessRule);
	}

	/**
	 * Converts numeric index of a column to its corresponding letter.
	 * 
	 * @param index numeric index
	 * @return column letter
	 */
	private String columnName(int index) {
		return CellReference.convertNumToColString(index);
	}

	/**
	 * Gets the initial row in the excel for the score whose index is given.
	 * 
	 * @param i score index
	 * @return initial row for given score
	 */
	private int getInitialRowScore(int i) {
		int headers = 6;
		int blankSpaces = 13;
		int numCellsForScore = this.metrics.size() + this.numModels + this.numModels * (this.numDatasets + 1) + headers
				+ blankSpaces + (this.numModels - 1) * 2;
		int initialRow = 15 + i * numCellsForScore;
		return initialRow;
	}

}