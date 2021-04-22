package com.cig.mctbnc.writers.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.models.MCTBNC;

/**
 * Class to write the predictions made on a dataset in a txt file.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class TxtClassificationWriter {
	private static Logger logger = LogManager.getLogger(MCTBNC.class);

	/**
	 * Write predictions of a dataset in the specified folder.
	 * 
	 * @param predictions
	 * @param dataset
	 * @param pathFolder
	 */
	public static void writePredictions(Prediction[] predictions, Dataset dataset, String pathFolder) {
		String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
		File file = new File(pathFolder + fileName);
		file.getParentFile().mkdirs();
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			for (int i = 0; i < predictions.length; i++) {
				String fileSequence = dataset.getNameFiles().get(i);
				State predictedCC = predictions[i].getPredictedClasses();
				double probabilityCC = predictions[i].getProbabilityPrediction();
				writer.print(fileSequence + " / Predicted classes: " + predictedCC);
				if (predictions[i].getProbabilities() != null)
					writer.print(" / Probability class configuration: " + probabilityCC + "%");
				writer.println();
			}
			writer.close();
			logger.info("Classifications saved in {}", file.getAbsoluteFile());
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			logger.error("There was an error creating the file for the classifications");
		}
	}
}