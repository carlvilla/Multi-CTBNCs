package es.upm.fi.cig.multictbnc.data.reader;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Class responsible for reading multiple CSV files representing a feature stream.
 *
 * @author Carlos Villa Blanco
 */
public class FeatureStreamMultipleCSVReader {
    static Logger logger = LogManager.getLogger(FeatureStreamMultipleCSVReader.class);
    private final String nameTimeVariable;
    private final String pathFeatureStream;
    private final String[] pathsBatches;
    Dataset currentDataset;
    int numFeatureVariablesReceived = 0;
    String nameLastFeatureReceived;

    /**
     * Constructs a FeatureStreamMultipleCSVReader with the specified path to the feature stream, a current dataset
     * and the name of the time variable.
     *
     * @param pathFeatureStream path to the feature stream
     * @param currentDataset    current dataset to which feature variables will be added
     * @param nameTimeVariable  name of the time variable
     */
    public FeatureStreamMultipleCSVReader(String pathFeatureStream, Dataset currentDataset, String nameTimeVariable) {
        this.pathFeatureStream = pathFeatureStream;
        this.currentDataset = currentDataset;
        this.nameTimeVariable = nameTimeVariable;
        this.pathsBatches = getPathsBatches(pathFeatureStream);
    }

    /**
     * Retrieve file paths of the datasets of each feature variable.
     *
     * @param pathFeatureStream path to the feature stream
     * @return array of file paths representing the datasets of each feature variable
     */
    private String[] getPathsBatches(String pathFeatureStream) {
        File[] directories = new File(pathFeatureStream).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        String[] pathsBatches = Arrays.stream(directories).map(File::getAbsolutePath).toArray(String[]::new);
        // Sort data batches by number
        Arrays.sort(pathsBatches, new Comparator<String>() {
            @Override
            public int compare(String e1, String e2) {
                return Long.compare(Util.extractLong(e1), Util.extractLong(e2));
            }
        });
        return pathsBatches;
    }

    /**
     * Retrieves the name of the last feature variable received from the feature stream.
     *
     * @return name of the last feature variable received
     */
    public String getNameLastFeatureReceived() {
        return this.nameLastFeatureReceived;
    }

    /**
     * Reads the complete feature stream to generate a static dataset. It iterates through the feature stream,
     * reading individual datasets until no more data is arriving.
     *
     * @return generated static dataset
     * @throws UnreadDatasetException if there is an issue reading the dataset
     * @throws FileNotFoundException  if the dataset file is not found
     */
    public Dataset readCompleteDataset() throws UnreadDatasetException, FileNotFoundException {
        logger.info("Reading complete feature stream to generate a static dataset from {}", this.pathFeatureStream);
        while (isDataArriving())
            readDataset();
        return this.currentDataset;
    }

    /**
     * Checks whether there is more feature data arriving in the feature stream.
     *
     * @return {@code true} if there is more data arriving, {@code false} otherwise
     */
    public boolean isDataArriving() {
        return numFeatureVariablesReceived < pathsBatches.length;
    }

    /**
     * Reads a new feature variable from the feature stream and adds it to the current dataset.
     *
     * @return current dataset with the new feature variable added
     * @throws UnreadDatasetException if there is an issue reading the dataset
     * @throws FileNotFoundException  if the dataset file is not found
     */
    public Dataset readDataset() throws UnreadDatasetException, FileNotFoundException {
        logger.info("Reading new feature variable from feature stream in {}", this.pathFeatureStream);
        DatasetReader newFeatureVariableDatasetReader = getDatasetReader(
                this.pathsBatches[numFeatureVariablesReceived]);
        Dataset dataset = newFeatureVariableDatasetReader.readDataset();
        // Merge current dataset with new one
        this.nameLastFeatureReceived = dataset.getNameFeatureVariables().get(0);
        this.currentDataset.addFeatureVariable(this.nameLastFeatureReceived, dataset);
        this.numFeatureVariablesReceived++;
        return this.currentDataset;
    }

    private DatasetReader getDatasetReader(String pathBatch) throws FileNotFoundException {
        DatasetReader datasetNewFeatureVariableReader = DatasetReaderFactory.getDatasetReader("Multiple CSV",
                pathBatch, 0);
        datasetNewFeatureVariableReader.setTimeVariable(this.nameTimeVariable);
        return datasetNewFeatureVariableReader;
    }

}
