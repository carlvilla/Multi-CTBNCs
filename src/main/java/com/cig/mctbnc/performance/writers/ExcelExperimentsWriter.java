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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;

/**
 * Excel writer for automatic experiments.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ExcelExperimentsWriter implements MetricsWriter {
	String path = "src/main/resources/results/";
	XSSFWorkbook workbook;
	XSSFSheet sheet;
	FileOutputStream out = null;

	int numDatasets;
	int numModels;
	int numScoreFunctions;

	List<String> nameClassVariables;
	List<String> nameDatasets;
	List<String> nameModels;
	List<String> metrics = List.of("Global accuracy", "Mean accuracy", "Macro-average precision",
			"Macro-average recall", "Macro-average f1 score", "Micro-average precision", "Micro-average recall",
			"Micro-average f1 score", "Globar Brier score");

	int currentDataset = 0;
	int currentModel = 0;
	int currentScoreFunction = 0;

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
	 * 
	 */
	public ExcelExperimentsWriter(List<String> scoreFunctions, List<String> nameDatasets, List<String> nameModels,
			List<String> nameFeatureVariables, List<String> nameClassVariables, BNParameterLearningAlgorithm bnPLA,
			CTBNParameterLearningAlgorithm ctbnPLA, String penalizationFunction) {

		// Set values global variables
		this.numDatasets = nameDatasets.size();
		this.numModels = nameModels.size();
		this.numScoreFunctions = scoreFunctions.size();
		this.nameClassVariables = nameClassVariables;
		this.nameDatasets = nameDatasets;
		this.nameModels = nameModels;

		// Create a workbook, sheet and file to store the results
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Experiments");
		String fileName = new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
		try {
			out = new FileOutputStream(path + fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Enable newlines in cells
		CellStyle newLineCS = workbook.createCellStyle();
		newLineCS.setWrapText(true);

		// Conditions experiments
		sheet.addMergedRegion(new CellRangeAddress(3, 7, 1, 9));
		sheet.createRow(3).createCell(1).setCellStyle(newLineCS);
		sheet.getRow(3).getCell(1)
				.setCellValue("- Feature variables: " + nameFeatureVariables + "\n - Class variables: "
						+ nameClassVariables + " \n - Penalization: " + penalizationFunction
						+ "\n - Parameter learning alg. class subgraph: " + bnPLA.getNameMethod()
						+ "\n - Parameter learning alg. bridge and feature subgraphs: " + ctbnPLA.getNameMethod());

		for (int i = 0; i < scoreFunctions.size(); i++) {
			// Score
			sheet.createRow(15).createCell(0).setCellValue(scoreFunctions.get(i));
			// Table with results
			generateTableDatasets();
			// Comparison of evaluation metrics
			generateTableComparionMetrics();
			// Comparison of evaluation metrics per class variable
			generateTableComparionPerClassVariable();
			// Set formulas
			setFormulas();
			// Conditional formatting
			setConditionalFormating();
		}
		// Merge title dataset
		sheet.autoSizeColumn(10);

	}

	/**
	 * Generate a table where it is shown the results of each model for each dataset
	 * and evaluation metric.
	 */
	public void generateTableDatasets() {
		// Results for each of the datasets
		sheet.createRow(16);
		sheet.createRow(17);
		for (int i = 0; i < numDatasets; i++) {
			sheet.getRow(16).createCell(1 + numModels * i).setCellValue(nameDatasets.get(i));
			sheet.addMergedRegion(new CellRangeAddress(16, 16, 1 + numModels * i, numModels * (i + 1)));
			for (int j = 0; j < numModels; j++) {
				sheet.getRow(17).createCell(1 + numModels * i + j).setCellValue(nameModels.get(j));
			}
		}

	}

	/**
	 * Generate a table where it is compare the results of each model.
	 */
	public void generateTableComparionMetrics() {
		sheet.createRow(30);
		sheet.createRow(31).createCell(0).setCellValue("Model");
		for (int i = 0; i < metrics.size(); i++) {
			sheet.createRow(18 + i).createCell(0).setCellValue(metrics.get(i));
			sheet.addMergedRegion(new CellRangeAddress(30, 30, i * 2 + 1, i * 2 + 2));
			sheet.getRow(30).createCell(i * 2 + 1).setCellValue(metrics.get(i));
		}
		for (int i = 0; i < metrics.size(); i++) {
			sheet.getRow(31).createCell(i * 2 + 1).setCellValue("Mean");
			sheet.getRow(31).createCell(i * 2 + 2).setCellValue("Std. deviation");
		}
		for (int i = 0; i < nameModels.size(); i++) {
			sheet.createRow(32 + i).createCell(0).setCellValue(nameModels.get(i));
			if (i < nameModels.size() - 1)
				sheet.createRow(33 + numModels + i).createCell(0)
						.setCellValue(nameModels.get(numModels - 1) + " vs. " + nameModels.get(i));
		}
	}

	/**
	 * Generate a table where it is compare the results of each model for each class
	 * variable.
	 */
	public void generateTableComparionPerClassVariable() {
		int rowClasses = 26 + 9 + 2 * numModels - 1;
		sheet.addMergedRegion(new CellRangeAddress(rowClasses, rowClasses + 1, 0, 0));
		sheet.createRow(rowClasses).createCell(0).setCellValue("Model");
		sheet.createRow(rowClasses + 1);
		for (int i = 0; i < nameClassVariables.size(); i++) {
			sheet.addMergedRegion(new CellRangeAddress(rowClasses, rowClasses, 2 * i + 1, 2 * i + 2));
			sheet.getRow(rowClasses).createCell(2 * i + 1).setCellValue(nameClassVariables.get(i));
			sheet.getRow(rowClasses + 1).createCell(2 * i + 1).setCellValue("Mean");
			sheet.getRow(rowClasses + 1).createCell(2 * i + 2).setCellValue("Std. deviation");
		}
		for (int i = 0; i < nameModels.size(); i++) {
			sheet.addMergedRegion(new CellRangeAddress(rowClasses + 2 + (numDatasets + 3) * i,
					rowClasses + 2 + (numDatasets + 3) * i + numDatasets, 0, 0));
			sheet.createRow(rowClasses + 2 + (numDatasets + 3) * i).createCell(0).setCellValue(nameModels.get(i));
			sheet.createRow(rowClasses + 2 + (numDatasets + 3) * i + numDatasets);
			// Create rows for the results of the evaluation metrics for each class
			// variable, dataset and model
			for (int j = 1; j < this.numDatasets; j++) {
				sheet.createRow(rowClasses + 2 + (numDatasets + 3) * i + j);
			}
		}
	}

	/**
	 * Set formulas in certain cells.
	 */
	public void setFormulas() {
		for (int i = 0; i < numModels; i++)
			for (int j = 0; j < metrics.size(); j++) {
				String strFormula = "";
				for (int k = 0; k < numDatasets; k++) {
					int row = 19 + j;
					String col = columnName(1 + k * numModels + i);
					strFormula += col + row + ",";
				}
				sheet.getRow(32 + i).createCell(1 + j * 2).setCellFormula("AVERAGE(" + strFormula + ")");
				sheet.getRow(32 + i).createCell((1 + j) * 2).setCellFormula("STDEV(" + strFormula + ")");
			}
		for (int i = 0; i < metrics.size(); i++) {
			String col = columnName(1 + i * 2);
			for (int j = 1; j < numModels; j++) {
				String strFormula = col + (32 + numModels) + " - " + col + (32 + j);
				sheet.getRow(32 + numModels + j).createCell(1 + i * 2).setCellFormula(strFormula);
			}
		}
		// Table class variables
		int rowClasses = 26 + 9 + 2 * numModels - 1;
		for (int i = 0; i < nameModels.size(); i++) {
			for (int j = 0; j < nameClassVariables.size(); j++) {
				sheet.getRow(rowClasses + 2 + (numDatasets + 3) * i + numDatasets).createCell(1 + j * 2)
						.setCellFormula("AVERAGE(" + columnName(1 + j * 2) + (rowClasses + 3 + (numDatasets + 3) * i)
								+ ":" + columnName(1 + j * 2) + (rowClasses + 2 + numDatasets + (numDatasets + 3) * i)
								+ ")");
				sheet.getRow(rowClasses + 2 + (numDatasets + 3) * i + numDatasets).createCell(2 + j * 2)
						.setCellFormula("STDEV(" + columnName(1 + j * 2) + (rowClasses + 3 + (numDatasets + 3) * i)
								+ ":" + columnName(1 + j * 2) + (rowClasses + 2 + numDatasets + (numDatasets + 3) * i)
								+ ")");
			}
		}
	}

	/**
	 * Define the conditional formating used for some cells.
	 */
	public void setConditionalFormating() {
		XSSFSheetConditionalFormatting condFormat = sheet.getSheetConditionalFormatting();
		XSSFConditionalFormattingRule moreRule = condFormat.createConditionalFormattingRule(ComparisonOperator.GT, "0");
		XSSFConditionalFormattingRule lessRule = condFormat.createConditionalFormattingRule(ComparisonOperator.LT, "0");
		moreRule.createPatternFormatting().setFillBackgroundColor(IndexedColors.GREEN.getIndex());
		lessRule.createPatternFormatting().setFillBackgroundColor(IndexedColors.RED.getIndex());

		for (int i = 1; i < numModels; i++) {
			CellRangeAddress[] rangeFormating = { CellRangeAddress.valueOf(
					"B" + (32 + numModels + i) + ":" + columnName(metrics.size() * 2 - 1) + (33 + numModels + i)) };
			condFormat.addConditionalFormatting(rangeFormating, moreRule);
			condFormat.addConditionalFormatting(rangeFormating, lessRule);
		}
	}

	public void write(Map<String, Double> results) {
		double ga = results.get("Global accuracy");
		double ma = results.get("Mean accuracy");
		double map = results.get("Macro-average precision");
		double mar = results.get("Macro-average recall");
		double maf1 = results.get("Macro-average f1 score");
		double mip = results.get("Micro-average precision");
		double mir = results.get("Micro-average recall");
		double mif1 = results.get("Micro-average f1 score");
		Double gb = results.getOrDefault("Global Brier score", null);

		int numColumn = this.currentModel + (this.numModels * this.currentDataset) + 1;
		sheet.getRow(18).createCell(numColumn).setCellValue(ga);
		sheet.getRow(19).createCell(numColumn).setCellValue(ma);
		sheet.getRow(20).createCell(numColumn).setCellValue(map);
		sheet.getRow(21).createCell(numColumn).setCellValue(mar);
		sheet.getRow(22).createCell(numColumn).setCellValue(maf1);
		sheet.getRow(23).createCell(numColumn).setCellValue(mip);
		sheet.getRow(24).createCell(numColumn).setCellValue(mir);
		sheet.getRow(25).createCell(numColumn).setCellValue(mif1);
		if (gb != null)
			sheet.getRow(26).createCell(numColumn).setCellValue(gb);

		// Evaluation metrics per class variables
		int rowClasses = 26 + 9 + 2 * numModels - 1;

		for (int i = 0; i < nameClassVariables.size(); i++) {
			double acc = results.get("Accuracy " + nameClassVariables.get(i));
			sheet.getRow(rowClasses + 2 + this.currentDataset + (this.numDatasets + 3) * this.currentModel)
					.createCell(2 * i + 1).setCellValue(acc);
		}

		// It is updated the reference to the currently evaluated dataset and model
		if (this.currentModel == this.numModels - 1 && this.currentDataset == this.numDatasets - 1) {
			// All the models where studied for all datasets
			this.currentModel = 0;
			this.currentDataset = 0;
			this.currentScoreFunction++;
		} else if (this.currentModel == this.numModels - 1) {
			// All the models where studied for the current dataset
			this.currentModel = 0;
			this.currentDataset++;
		} else
			this.currentModel++;

	}

	public void close() {
		try {
			workbook.write(out);
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts numeric index of a column to its corresponding letter.
	 * 
	 * @param index
	 * @return column letter
	 */
	public static String columnName(int index) {
		return CellReference.convertNumToColString(index);
	}

}
