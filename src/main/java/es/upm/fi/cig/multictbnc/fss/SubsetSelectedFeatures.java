package es.upm.fi.cig.multictbnc.fss;

import java.util.List;

/**
 * Class that encapsulates the names of the feature variables selected by a FSS algorithm and the execution time of this
 * algorithm to provide that solution.
 *
 * @author Carlos Villa Blanco
 */
public class SubsetSelectedFeatures {
    List<String> features;
    double executionTime;

    /**
     * Constructs an instance of SubsetSelectedFeatureVariables.
     *
     * @param features      list of names of the features selected by the FSS algorithm
     * @param executionTime time taken by the FSS algorithm to provide the solution
     */
    public SubsetSelectedFeatures(List<String> features, double executionTime) {
        this.features = features;
        this.executionTime = executionTime;
    }

    /**
     * Checks if a specific feature is included in the selected subset.
     *
     * @param nameFeature name of the feature to check
     * @return {@code true} if the specified feature is part of the selected subset, {@code false} otherwise
     */
    public boolean containsFeature(String nameFeature) {
        return features.contains(nameFeature);
    }

    /**
     * Retrieves the execution time of the FSS algorithm.
     *
     * @return the execution time of the FSS algorithm
     */
    public double getExecutionTime() {
        return executionTime;
    }

    /**
     * Provides the list of features selected by the FSS algorithm.
     *
     * @return list of names of the selected features
     */
    public List<String> getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return features.toString();
    }
}
