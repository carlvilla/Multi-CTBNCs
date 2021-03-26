package com.cig.mctbnc.performance.writers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;

/**
 * 
 * Allows writing the results of the experiments in an Excel file.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ExcelExperimentsWriter extends MetricsWriter {
	String path = "src/main/resources/results/";
	XSSFWorkbook workbook;
	XSSFSheet sheet;
	FileOutputStream out = null;
	int numDatasets;
	int numModels;
	int numScoreFunctions;
	int idxCurrentDataset = 0;
	int idxCurrentModel = 0;
	int idxCurrentScoreFunction = 0;
	List<String> metrics = List.of("Global accuracy", "Mean accuracy", "Macro-averaged precision",
			"Macro-averaged recall", "Macro-averaged f1 score", "Micro-averaged precision", "Micro-averaged recall",
			"Micro-averaged f1 score", "Globar Brier score");

	/**
	 * Initialize the writer.
	 * 
	 * @param scoreFunctions
	 * @param nameDatasets
	 * @param nameModels
	 * @param nameFeatureVariables
	 * @param nameClassVariables
	 * @param bnPLA
	 * @param ctbnPLA
	 * @param penalizationFunction
	 * @param initialStructure
	 * 
	 */
	public ExcelExperimentsWriter(List<String> scoreFunctions, List<String> nameDatasets, List<String> nameModels,
			List<String> nameFeatureVariables, List<String> nameClassVariables, BNParameterLearningAlgorithm bnPLA,
			CTBNParameterLearningAlgorithm ctbnPLA, String penalizationFunction, String initialStructure) {
		// Set values global variables
		this.numDatasets = nameDatasets.size();
		this.numModels = nameModels.size();
		this.numScoreFunctions = scoreFunctions.size();
		this.nameClassVariables = nameClassVariables;
		// Create a workbook, sheet and file to store the results
		this.workbook = new XSSFWorkbook();
		this.sheet = this.workbook.createSheet("Experiments");
		String fileName = new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
		try {
			this.out = new FileOutputStream(this.path + fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Conditions experiments
		writeConditionsExperiment(bnPLA, ctbnPLA, nameFeatureVariables, penalizationFunction, initialStructure);
		for (int i = 0; i < scoreFunctions.size(); i++) {
			// Get initial row in the excel for score
			int initialRow = getInitialRowScore(i);
			// Score function
			setScoreFunction(initialRow, scoreFunctions.get(i));
			// Table with results
			generateTableDatasets(initialRow, nameDatasets, nameModels);
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

	}

	@Override
	public void write(Map<String, Double> results) {
		double ga = results.get("Global accuracy");
		double ma = results.get("Mean accuracy");
		double map = results.get("Macro-averaged precision");
		double mar = results.get("Macro-averaged recall");
		double maf1 = results.get("Macro-averaged f1 score");
		double mip = results.get("Micro-averaged precision");
		double mir = results.get("Micro-averaged recall");
		double mif1 = results.get("Micro-averaged f1 score");
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
			this.workbook.write(this.out);
			this.out.flush();
			this.out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write in the excel the score function that is being used.
	 * 
	 * @param initialRow
	 * @param scoreFunction
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
	 * Write information about the conditions in which the experiments were
	 * performed.
	 * 
	 * @param bnPLA
	 * @param ctbnPLA
	 * @param nameFeatureVariables
	 * @param penalizationFunction
	 * @param initialStructure
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
	 * Generate a table where it is shown the results of each model for each dataset
	 * and evaluation metric.
	 */
	private void generateTableDatasets(int initialRow, List<String> nameDatasets, List<String> nameModels) {
		// Results for each of the datasets
		this.sheet.createRow(initialRow + 1);
		this.sheet.createRow(initialRow + 2);
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

	/**
	 * Generate a table where it is compare the results of each model.
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
	 * Generate a table where it is compare the results of each model for each class
	 * variable.
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
	 * Set formulas in certain cells.
	 * 
	 * @param initialRow
	 * @param nameModels
	 * @param nameClassVariables
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
	 * Define the conditional formating used for some cells.
	 * 
	 * @param initialRow
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
	 * @param index
	 * @return column letter
	 */
	private String columnName(int index) {
		return CellReference.convertNumToColString(index);
	}

	/**
	 * Get the initial row in the excel for the score whose index is given.
	 * 
	 * @param i
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