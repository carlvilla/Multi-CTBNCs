package es.upm.fi.cig.multictbnc.fss;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;

import java.util.List;

/**
 * This interface defines the structure for classes that implement online feature subset selection algorithms. Online
 * feature subset selection algorithms analyze data as it becomes available and decide whether newly arriving
 * features should be included in the model.
 */
public interface OnlineFeatureSubsetSelection {

    /**
     * Executes the feature subset selection algorithm for a newly arrived feature variable in a given data batch.
     * This method is designed to analyze the relevance and redundancy of a new variable in the context of the
     * current feature subset and update it accordingly.
     *
     * @param newVariable name of the new feature variable that is to be evaluated
     * @param dataBatch   data batch containing the new variable along with existing features and class variables
     * @return an instance of {@code SubsetSelectedFeatureVariables}, containing the subset of selected features
     */
    SubsetSelectedFeatures execute(String newVariable, Dataset dataBatch);

    /**
     * Returns a boolean indicating whether the last execution of the feature subset selection algorithm resulted in
     * any changes to the selected feature subset.
     *
     * @return {@code true} if the last execution yielded a change in the feature subset, {@code false} otherwise
     */
    boolean getLastExecutionYieldAnyChange();

    /**
     * Sets the current set of feature variables.
     *
     * @param nameFeatureVariables list of names of the current feature variables
     */
    void setCurrentFeatureVariables(List<String> nameFeatureVariables);

}
