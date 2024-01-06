package es.upm.fi.cig.multictbnc.writers.performance;

import java.util.List;
import java.util.Map;

/**
 * Defines classes that write the results of evaluation metrics on different outputs.
 *
 * @author Carlos Villa Blanco
 */
public abstract class MetricsWriter {
    List<String> nameClassVariables;

    /**
     * Closes the writer.
     */
    public void close() {
    }
    
    /**
     * Writes the results to an output. The results are provided as a {@code Map} where keys represent evaluation
     * metric names and values represent their corresponding scores.
     *
     * @param results a {@code Map} with the results of the evaluation metrics
     */
    public void write(List<Map<String, Double>> results) {
        for (Map<String, Double> result : results)
            write(result);
    }

    /**
     * Writes the given results. The results are provided as a {@code Map} where ke evaluation metric names and
     * values represent their corresponding scores.
     *
     * @param results a {@code Map} with the results of the evaluation metrics
     */
    public abstract void write(Map<String, Double> results);
}