package es.upm.fi.cig.multictbnc.experiments;

import es.upm.fi.cig.multictbnc.util.Util;

import java.util.Queue;

/**
 * Abstract class for defining experiments. This class provides a structure for setting up and executing
 * different types of experiments related to Multi-CTBNCs.
 *
 * @author Carlos Villa Blanco
 */
public abstract class AbstractExperiment implements Experiment {
    private final Queue<String> experimentConfig;

    /**
     * This constructor initializes an experiment with the provided configuration parameters. It converts an array of
     * configuration parameters into a queue, which allows for efficient processing of these parameters in the
     * derived experiment classes.
     *
     * @param experimentConfig array of strings representing the configuration parameters for the experiment
     */
    public AbstractExperiment(String[] experimentConfig) {
        this.experimentConfig = Util.arrayToQueue(experimentConfig);
    }

    /**
     * Retrieves the queue of experiment configuration parameters.
     *
     * @return queue of strings representing the configuration parameters for the experiment
     */
    protected Queue<String> getExperimentConfig() {
        return this.experimentConfig;
    }

}
