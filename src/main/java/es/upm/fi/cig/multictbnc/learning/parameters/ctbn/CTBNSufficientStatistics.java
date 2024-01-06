package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.NeverSeenStateException;
import es.upm.fi.cig.multictbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Computes and stores the sufficient statistics of a discrete CTBN node. The sufficient statistics are:
 * <p>
 * (1) Mxy: number of times a variable transitions from a certain state to another one while its parents take a certain
 * instantiation.
 * <p>
 * (2) Mx: number of times a variable leaves a certain state (for any other state) while its parents take a certain
 * instantiation.
 * <p>
 * (3) Tx: time that a variable stays in a certain state while its parents take a certain instantiation.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNSufficientStatistics implements SufficientStatistics {
    private static final Logger logger = LogManager.getLogger(CTBNSufficientStatistics.class);
    // Sufficient statistics
    double[][][] Mxy;
    double[][] Mx;
    double[][] Tx;
    // Hyperparameters of the Dirichlet prior distribution (zero if MLE is used)
    double mxyHP;
    double mxHP; // defined with MxyHP and the number of states of the variable
    double txHP;
    // Equivalent sample size
    private double mHP;
    private double tHP;

    /**
     * Receives the hyperparameters of the Dirichlet prior distribution over the parameters that are necessary for
     * Bayesian estimation.
     *
     * @param mHP number of times a variable transitions from a certain state (hyperparameter)
     * @param tHP time that a variable stays in a certain state (hyperparameter)
     */
    public CTBNSufficientStatistics(double mHP, double tHP) {
        this.mHP = mHP;
        this.tHP = tHP;
    }

    /**
     * Returns the sufficient statistic with the number of times the variable leaves every state (i.e., the state
     * changes) while its parents have certain values.
     *
     * @return number of times the variable leaves every state
     */
    public double[][] getMx() {
        if (this.Mx == null)
            logger.warn("Sufficient statistic mx was not computed");
        return this.Mx;
    }

    /**
     * Returns the value of the hyperparameter with the number of 'imaginary' transitions that occurred from a certain
     * state before seeing the data.
     *
     * @return hyperparameter with the number of 'imaginary' transitions that occurred from a certain state
     */
    public double getMxHyperparameter() {
        return this.mxHP;
    }

    /**
     * Returns the sufficient statistic with the number of times the variable transition from a certain state to
     * another
     * while its parents have certain values
     *
     * @return number of occurrences of every transition
     */
    public double[][][] getMxy() {
        if (this.Mxy == null)
            logger.warn("Sufficient statistic mxy was not computed");
        return this.Mxy;
    }

    /**
     * Returns the value of the hyperparameter with the number of 'imaginary' transitions that occurred from a certain
     * state to another before seeing the data.
     *
     * @return hyperparameter with the number of 'imaginary' transitions that occurred from a certain state to another
     */
    public double getMxyHyperparameter() {
        return this.mxyHP;
    }

    /**
     * Returns the sufficient statistic with the time that the variable stays in every state, while its parents take
     * different values.
     *
     * @return time that the variable stay for every state
     */
    public double[][] getTx() {
        if (this.Tx == null)
            logger.warn("Sufficient statistic tx was not computed");
        return this.Tx;
    }

    /**
     * Returns the value of the hyperparameter with the 'imaginary' time that was spent in a certain state before
     * seeing
     * the data.
     *
     * @return hyperparameter with the 'imaginary' time that was spent in a certain state
     */
    public double getTxHyperparameter() {
        return this.txHP;
    }

    /**
     * Computes the sufficient statistics of a CTBN node.
     *
     * @param dataset dataset from which sufficient statistics are extracted
     */
    @Override
    public void computeSufficientStatistics(DiscreteStateNode node, Dataset dataset) {
        String nameVariable = node.getName();
        logger.trace("Computing sufficient statistics CTBN for node {}", nameVariable);
        // Variable used to avoid overflowing the console if states were not seen before
        boolean neverSeenBeforeState = false;
        // Initialise sufficient statistics
        initialiseSufficientStatistics(node);
        // Iterate over all sequences and observations to extract sufficient statistics
        for (Sequence sequence : dataset.getSequences()) {
            for (int idxObservation = 1; idxObservation < sequence.getNumObservations(); idxObservation++) {
                // State of the variable before the transition
                String fromState = sequence.getValueVariable(idxObservation - 1, nameVariable);
                int idxFromState = node.setState(fromState);
                // State of the parents before the transition
                for (int idxParentNode = 0; idxParentNode < node.getNumParents(); idxParentNode++) {
                    DiscreteStateNode nodeParent = (DiscreteStateNode) node.getParents().get(idxParentNode);
                    String stateParent = sequence.getValueVariable(idxObservation - 1, nodeParent.getName());
                    nodeParent.setState(stateParent);
                }
                int idxStateParents = node.getIdxStateParents();
                // State of the variable after the transition
                String toState = sequence.getValueVariable(idxObservation, nameVariable);
                int idxtoState = node.setState(toState);
                try {
                    updateSufficientStatistics(sequence, idxFromState, idxtoState, idxStateParents, idxObservation);
                } catch (NeverSeenStateException nsse) {
                    if (!neverSeenBeforeState) {
                        logger.warn(
                                "Variable: {}  - {} [Similar messages will be ignored to avoid overflowing the " +
                                        "console]",
                                nameVariable, nsse.getMessage());
                        neverSeenBeforeState = true;
                    }
                }
            }
        }

    }

    /**
     * Initialises the structures to store the sufficient statistics of the node. In the case of Bayesian estimation,
     * the imaginary counts are defined based on the number of states of the node and its parents, so the total number
     * of imaginary samples is not influenced by the cardinality of those nodes.
     *
     * @param node node
     */
    protected void initialiseSufficientStatistics(DiscreteStateNode node) {
        this.Mxy = new double[node.getNumStatesParents()][node.getNumStates()][node.getNumStates()];
        this.Mx = new double[node.getNumStatesParents()][node.getNumStates()];
        this.Tx = new double[node.getNumStatesParents()][node.getNumStates()];

        this.mxyHP = this.mHP / (node.getNumStatesParents() * Math.pow(node.getNumStates(), 2));
        this.mxHP = (this.mHP / (node.getNumStatesParents() * Math.pow(node.getNumStates(), 2))) *
                (node.getNumStates() - 1);
        this.txHP = this.tHP / (node.getNumStatesParents() * node.getNumStates());

        // Adds the imaginary counts (hyperparameters) to the sufficient statistics
        Util.fill3dArray(this.Mxy, this.mxyHP);
        Util.fill2dArray(this.Mx, this.mxHP);
        Util.fill2dArray(this.Tx, this.txHP);
    }

    /**
     * Updates the number of occurrences where the node transitions from a certain state to any other state given an
     * instantiation of its parents.
     *
     * @param stateParents   index of the state of the node's parents
     * @param fromState      index of the current state
     * @param numOccurrences number of occurrences
     * @throws NeverSeenStateException thrown if new node's states appeared after the initialisation of the sufficient
     *                                 statistics
     */
    protected void updateMx(int stateParents, int fromState, double numOccurrences) throws NeverSeenStateException {
        try {
            this.Mx[stateParents][fromState] += numOccurrences;
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            throw new NeverSeenStateException(
                    "The sufficient statistic Mx could not be updated as a never-before-seen state has been received");
        }
    }

    /**
     * Updates the number of occurrences where the node transitions from a certain state to another given an
     * instantiation of its parents.
     *
     * @param stateParents   index of the state of the node's parents
     * @param fromState      index of the state from which the transition starts
     * @param toState        index of the state to which the transition ends
     * @param numOccurrences number of times this transition occurs
     * @throws NeverSeenStateException thrown if new node's states appeared after the initialisation of the sufficient
     *                                 statistics
     */
    protected void updateMxy(int stateParents, int fromState, int toState, double numOccurrences)
            throws NeverSeenStateException {
        try {
            this.Mxy[stateParents][fromState][toState] += numOccurrences;
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            throw new NeverSeenStateException(
                    "The sufficient statistic Mxy could not be updated as a never-before-seen state has been " +
                            "received");
        }
    }

    /**
     * Update the values of the sufficient statistics.
     *
     * @param sequence        sequence analysed when updating the sufficient statistics
     * @param idxFromState    index of the state from which the transition used to update the sufficient statistics
     *                        starts
     * @param idxtoState      index of the state to which the transition used to update the sufficient statistics ends
     * @param idxStateParents index of the state the node's parents had when the transition occurred.
     * @param idxObservation  index of the observation where the transition ends
     * @throws NeverSeenStateException thrown if new node's states appeared after the initialisation of the sufficient
     *                                 statistics
     */
    protected void updateSufficientStatistics(Sequence sequence, int idxFromState, int idxtoState, int idxStateParents,
                                              int idxObservation) throws NeverSeenStateException {
        // If the node is transitioning to a different state, Mxy and Mx are updated
        if (idxFromState != idxtoState) {
            updateMxy(idxStateParents, idxFromState, idxtoState, 1);
            updateMx(idxStateParents, idxFromState, 1);
        }
        // Increase the time the node and its parents are in a certain state
        double transitionTime = sequence.getTimeValue(idxObservation) - sequence.getTimeValue(idxObservation - 1);
        updateTx(idxStateParents, idxFromState, transitionTime);
    }

    /**
     * Updates the time the node spends in a certain state given an instantiation of its parents.
     *
     * @param stateParents index of the state of the node's parents
     * @param fromState    index of the node state
     * @param time         time the node spends in the given state
     * @throws NeverSeenStateException thrown if new node's states appeared after the initialisation of the sufficient
     *                                 statistics
     */
    private void updateTx(int stateParents, int fromState, double time) throws NeverSeenStateException {
        try {
            this.Tx[stateParents][fromState] += time;
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            throw new NeverSeenStateException(
                    "The sufficient statistic Tx could not be updated as a never-before-seen state has been received");
        }

    }

}