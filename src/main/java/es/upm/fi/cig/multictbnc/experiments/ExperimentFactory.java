package es.upm.fi.cig.multictbnc.experiments;

import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.LearningStreamExperiment;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.ModelComparisonExperiment;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.StructureLearningAlgorithmsComparisonExperiment;

import java.util.Arrays;

/**
 * A factory class for creating instances of experiments based on provided arguments.
 *
 * @author Carlos Villa Blanco
 */
public class ExperimentFactory {

    /**
     * Gets an instance of an experiment based on the provided arguments.
     *
     * @param args arguments specifying the experiment to create and its configuration
     * @return an instance of the specified experiment.
     */
    public static Experiment getExperiment(String... args) {
        String nameExperimentHandler = args[0];
        String[] experimentConfig = Arrays.copyOfRange(args, 1, args.length);
        switch (nameExperimentHandler) {
            case "StructureLearningAlgorithmsComparisonExperiment":
                return new StructureLearningAlgorithmsComparisonExperiment(experimentConfig);
            case "LearningStreamExperiment":
                return new LearningStreamExperiment(experimentConfig);
            default:
                return new ModelComparisonExperiment(experimentConfig);
        }

    }
}
