package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.experiments.AbstractExperiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements a experiment on streaming data. It is used to conduct experiments with data streams and
 * feature streams.
 */
public class LearningStreamExperiment extends AbstractExperiment {
    private static final Logger logger = LogManager.getLogger(ModelComparisonExperiment.class);
    String pathDataset;

    /**
     * Constructs a LearningStreamExperiment with the given configuration parameters. The first parameter from the
     * configuration is expected to be the path to the dataset.
     *
     * @param args array of strings representing configuration parameters for the experiment
     */
    public LearningStreamExperiment(String... args) {
        super(args);
        this.pathDataset = getExperimentConfig().peek();
    }

    @Override
    public void execute() {
        try {
            String streamType = getExperimentConfig().poll();
            if (streamType.equals("data stream")) {
                DataStreamExperiment dataStreamExperiment = new DataStreamExperiment();
                dataStreamExperiment.main(getExperimentConfig());
            } else if (streamType.equals("feature stream")) {
                FeatureStreamExperiment featureStreamExperiment = new FeatureStreamExperiment();
                featureStreamExperiment.main(getExperimentConfig());
            } else {
                logger.error("Specified stream type not implemented");
            }
        } catch (ErroneousValueException e) {
            logger.error("Error while learning data stream");
        }
    }
}
