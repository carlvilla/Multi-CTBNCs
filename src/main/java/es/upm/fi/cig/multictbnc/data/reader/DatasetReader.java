package es.upm.fi.cig.multictbnc.data.reader;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.exceptions.VariableNotFoundException;

import java.util.List;

/**
 * Interface for classes that read datasets.
 *
 * @author Carlos Villa Blanco
 */
public interface DatasetReader {

	/**
	 * Returns the name of the class variables.
	 *
	 * @return name of the class variables.
	 */
	List<String> getNameClassVariables();

	/**
	 * Returns the name of the feature variables.
	 *
	 * @return name of the feature variables.
	 */
	List<String> getNameFeatureVariables();

	/**
	 * Returns the name of the time variable.
	 *
	 * @return name of the time variable.
	 */
	String getNameTimeVariable();

	/**
	 * Returns the names of all the variables of the dataset, including those that are not used.
	 *
	 * @return names of the variables
	 */
	List<String> getNameVariables();

	/**
	 * Indicates if the dataset is out-of-date.
	 *
	 * @return <code>true</code> if dataset is out-of-date; <code>false</code>
	 * otherwise.
	 */
	boolean isDatasetOutdated();

	/**
	 * Returns a dataset.
	 *
	 * @return a {@code Dataset}
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	Dataset readDataset() throws UnreadDatasetException;

	/**
	 * Creates a dataset using only the specified number of files. This method allows reading datasets using batches.
	 *
	 * @param numFiles number of files
	 * @return a {@code Dataset}
	 * @throws UnreadDatasetException thrown if the dataset could not be read
	 */
	Dataset readDataset(int numFiles) throws UnreadDatasetException;

	/**
	 * Defines if the feature variables with no variance should be removed.
	 *
	 * @param removeZeroVarianceVariable {@code true} to remove zero variance feature variables, {@code false}
	 */
	void removeZeroVarianceVariables(boolean removeZeroVarianceVariable);

	/**
	 * Defines a previously read dataset as out-of-date, so it should be reloaded.
	 *
	 * @param outdated <code>true</code> to set dataset as out-of-date;
	 *                 <code>false</code> otherwise.
	 */
	void setDatasetAsOutdated(boolean outdated);

	/**
	 * Receives the names of the time and class variables of a dataset. All the other variables are considered feature
	 * variables.
	 *
	 * @param nameTimeVariable   name of the time variable
	 * @param nameClassVariables name of the class variables
	 */
	void setTimeAndClassVariables(String nameTimeVariable, List<String> nameClassVariables);

	/**
	 * Receives the names of the time and feature variables of a dataset. This method can be used, for example, when
	 * reading datasets to be classified.
	 *
	 * @param nameTimeVariable     name of the time variable
	 * @param nameFeatureVariables name of the feature variables
	 */
	void setTimeAndFeatureVariables(String nameTimeVariable, List<String> nameFeatureVariables);

	/**
	 * Receives the names of the time variable, feature variables and class variables of a dataset. This method can be
	 * used, for example, when read training datasets.
	 *
	 * @param nameTimeVariable     name of the time variable
	 * @param nameClassVariables   names of the class variables
	 * @param nameFeatureVariables names of the feature variables
	 */
	void setVariables(String nameTimeVariable, List<String> nameClassVariables, List<String> nameFeatureVariables);

}