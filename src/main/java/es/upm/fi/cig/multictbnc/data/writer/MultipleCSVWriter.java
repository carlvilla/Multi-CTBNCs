package es.upm.fi.cig.multictbnc.data.writer;

import com.opencsv.CSVWriter;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Manages the writing of datasets into CSV files
 *
 * @author Carlos Villa Blanco
 */
public class MultipleCSVWriter {
    private static final Logger logger = LogManager.getLogger(MultipleCSVWriter.class);

    /**
     * Writes the sequences of the provided dataset to multiple CSV files in the specified directory.
     *
     * @param dataset         dataset whose sequences are written to multiple CSV files
     * @param destinationPath directory path where the CSVs are stored
     */
    public static void write(Dataset dataset, String destinationPath) {
        logger.info("Writing dataset with {} sequences to {}", dataset.getNumDataPoints(), destinationPath);
        List<Sequence> sequences = dataset.getSequences();
        IntStream.range(0, sequences.size()).parallel().forEach(indexSequence -> {
            // Get a sequence
            Sequence sequence = sequences.get(indexSequence);
            // Define the name of the CSV for the sequence
            String nameFile = "Sequence" + indexSequence;
            write(sequence, destinationPath, nameFile);
        });
    }

    /**
     * Writes a sequence to a CSV file in the specified directory.
     *
     * @param sequence        {@code Sequence} which is written to a CSV file
     * @param destinationPath directory path where the CSV is stored
     * @param nameFile        name for the CSV file
     */
    public static void write(Sequence sequence, String destinationPath, String nameFile) {
        logger.debug("Writing sequence to {}", destinationPath);
        // Create folders of the provided path if they do not exist
        File f = new File(destinationPath);
        f.mkdirs();
        // Define the path of the CSV
        String pathSequence = Paths.get(destinationPath, nameFile + ".csv").toString();
        // Get the name of the variables (their order could vary between sequences)
        List<String> header = sequence.getNameAllVariables();
        // Store the data of the sequences
        List<String[]> dataSequence = new ArrayList<>();
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(pathSequence), ',');
            // Write header
            dataSequence.add(Util.listToArray(header));
            // Write observations
            for (int idxObservation = 0; idxObservation < sequence.getNumObservations(); idxObservation++) {
                // Extract time and state of features during the observation
                String time = String.valueOf(sequence.getTimeValue(idxObservation));
                String[] valuesFeatures = sequence.getValuesFeatureVariables(idxObservation);
                // Create an array to store the extracted data
                String[] dataObservation = new String[valuesFeatures.length + 1];
                dataObservation[0] = time;
                System.arraycopy(valuesFeatures, 0, dataObservation, 1, valuesFeatures.length);
                dataSequence.add(dataObservation);
            }
            csvWriter.writeAll(dataSequence);
            csvWriter.close();
        } catch (IOException ioe) {
            logger.error("An error occurred while creating CSV to write sequence {}", nameFile);
        }
    }

    /**
     * Writes the sequences of the provided dataset to multiple CSV files in the specified directory, including only
     * the specified feature variables.
     *
     * @param dataset           dataset whose sequences are written to multiple CSV files
     * @param featuresToInclude list of feature variables to include in the CSV files
     * @param destinationPath   path where the CSVs are stored
     */
    public static void write(Dataset dataset, List<String> featuresToInclude, String destinationPath) {
        logger.info("Writing dataset with {} sequences to {}", dataset.getNumDataPoints(), destinationPath);
        List<Sequence> sequences = dataset.getSequences();
        IntStream.range(0, sequences.size()).parallel().forEach(indexSequence -> {
            // Get a sequence
            Sequence sequence = sequences.get(indexSequence);
            // Define the name of the CSV for the sequence
            String nameFile = "Sequence" + indexSequence;
            write(sequence, featuresToInclude, destinationPath, nameFile);
        });
    }

    /**
     * Writes a sequence to a CSV file in the specified directory and including only the specified feature variables.
     *
     * @param sequence          {@code Sequence} which is written to a CSV file
     * @param featuresToInclude list of feature variables to include in the CSV
     * @param destinationPath   directory path where the CSV is stored
     * @param nameFile          name for the CSV file
     */
    public static void write(Sequence sequence, List<String> featuresToInclude, String destinationPath,
                             String nameFile) {
        logger.debug("Writing sequence to {}", destinationPath);
        // Create folders of the provided path if they do not exist
        File f = new File(destinationPath);
        f.mkdirs();
        // Define the path of the CSV
        String pathSequence = Paths.get(destinationPath, nameFile + ".csv").toString();
        // Store the data of the sequences
        List<String[]> dataSequence = new ArrayList<>();
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(pathSequence), ',');
            // Write header
            List<String> header = new ArrayList<>(featuresToInclude);
            header.add(0, sequence.getNameTimeVariable());
            dataSequence.add(Util.listToArray(header));
            // Write observations
            for (int idxObservation = 0; idxObservation < sequence.getNumObservations(); idxObservation++) {
                // Extract time and state of features during the observation
                String time = String.valueOf(sequence.getTimeValue(idxObservation));
                //String[] valuesFeatures = sequence.getValuesFeatureVariables(idxObservation);
                int finalIdxObservation = idxObservation;
                String[] valuesFeatures = featuresToInclude.stream().map(
                        nameFeature -> sequence.getValueFeatureVariable(finalIdxObservation, nameFeature)).toArray(
                        String[]::new);
                // Create an array to store the extracted data
                String[] dataObservation = new String[valuesFeatures.length + 1];
                dataObservation[0] = time;
                System.arraycopy(valuesFeatures, 0, dataObservation, 1, valuesFeatures.length);
                dataSequence.add(dataObservation);
            }
            csvWriter.writeAll(dataSequence);
            csvWriter.close();
        } catch (IOException ioe) {
            logger.error("An error occurred while creating CSV to write sequence {}", nameFile);
        }
    }

}