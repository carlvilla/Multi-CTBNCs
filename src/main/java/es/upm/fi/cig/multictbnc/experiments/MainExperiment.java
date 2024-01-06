package es.upm.fi.cig.multictbnc.experiments;

/**
 * Main class for running experiments. It creates an experiment based on the provided command-line arguments and
 * executes it.
 *
 * @author Carlos Villa Blanco
 */
public class MainExperiment {

    /**
     * Entry point of the application.
     *
     * @param args command-line arguments used to configure the experiment.
     */
    public static void main(String[] args) {
        Experiment experiment = ExperimentFactory.getExperiment(args);
        experiment.execute();
    }
}
