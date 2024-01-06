package es.upm.fi.cig.multictbnc.experiments;

/**
 * Represents an experiment that can be executed. Implementations of this interface define specific
 * experiments to be conducted.
 *
 * @author Carlos Villa Blanco
 */
public interface Experiment {

    /**
     * Executes the experiment.
     */
    void execute();

}