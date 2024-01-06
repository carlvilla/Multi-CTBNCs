package es.upm.fi.cig.multictbnc.models;

import es.upm.fi.cig.multictbnc.classification.Classifier;
import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC.Digraph;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.nodes.*;
import es.upm.fi.cig.multictbnc.util.ProbabilityUtil;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Implements the multi-dimensional continuous-time Bayesian network classifier (Multi-CTBNC).
 *
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 * @author Carlos Villa Blanco
 */
public class MultiCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends AbstractPGM<Node> implements
        Classifier {
    private static final Logger logger = LogManager.getLogger(MultiCTBNC.class);
    // The subgraph formed by class variables is a Bayesian network
    private BN<NodeTypeBN> bn;
    private CTBN<NodeTypeCTBN> ctbn;
    // Classes of the nodes
    private Class<NodeTypeBN> bnNodeClass;
    private Class<NodeTypeCTBN> ctbnNodeClass;
    // Algorithms used to learn the class subgraph (BN)
    private BNLearningAlgorithms bnLearningAlgs;
    // Algorithms used to learn the feature and bridge subgraphs (CTBN)
    private CTBNLearningAlgorithms ctbnLearningAlgs;
    // Initial structure (Empty by default)
    private String initialStructure = "Empty";

    /**
     * Receives learning algorithms for Bayesian networks and continuous-time Bayesian networks to generate a
     * Multi-CTBNC.
     *
     * @param bnLearningAlgs   parameter and structure learning algorithms for Bayesian networks
     * @param ctbnLearningAlgs parameter and structure learning algorithms for continuous-time Bayesian networks
     * @param bnNodeClass      Bayesian network node type
     * @param ctbnNodeClass    continuous-time Bayesian network node type
     */
    public MultiCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
                      Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
        // Save the class type of the nodes
        this.bnNodeClass = bnNodeClass;
        this.ctbnNodeClass = ctbnNodeClass;
        // Save algorithms used for the learning of the Multi-CTBNC
        this.bnLearningAlgs = bnLearningAlgs;
        this.ctbnLearningAlgs = ctbnLearningAlgs;
    }

    /**
     * Receives a Bayesian network and a continuous-time Bayesian network that represent the class subgraph and
     * feature/bridge subgraph of a Multi-CTBNC, respectively.
     *
     * @param bn   Bayesian network
     * @param ctbn continuous-time Bayesian network
     */
    public MultiCTBNC(BN<NodeTypeBN> bn, CTBN<NodeTypeCTBN> ctbn) {
        this.bn = new BN<>(bn, true);
        this.ctbn = new CTBN<>(ctbn, true);
        getNodes();
    }

    /**
     * Returns the list of nodes for the feature variables.
     *
     * @return list of nodes for the feature variables
     */
    public List<NodeTypeCTBN> getNodesFeatureVariables() {
        return this.ctbn.getNodes().stream().filter(node -> !node.isClassVariable()).collect(Collectors.toList());
    }

    /**
     * Returns the list of nodes for the class variables.
     *
     * @return list of nodes for the class variables
     */
    public List<NodeTypeBN> getNodesClassVariables() {
        List<NodeTypeBN> nodesBN = this.bn.getNodes();
        // Define nodes of the BN as class variables
        for (Node node : nodesBN)
            node.isClassVariable(true);
        return nodesBN;
    }

    /**
     * Return the name of the initial structure of the model. This can be, for example, an empty structure (Empty) or a
     * naive Bayes where all features are children of all class variables (Naive Bayes).
     *
     * @return name of the initial structure
     */
    public String getInitialStructure() {
        return this.initialStructure;
    }

    /**
     * Establishes the approach that will be used to define the initial structure of the Multi-CTBNC. For now, it is
     * possible to define an empty structure or a naive Bayes.
     *
     * @param initialStructure initial structure that will be used (Empty (default) or naive Bayes)
     */
    public void setInitialStructure(String initialStructure) {
        this.initialStructure = initialStructure;
    }

    /**
     * Establishes the initial structure of a CTBN depending on the global variable "initialStructure".
     *
     * @param ctbn continuous-time Bayesian network whose initial structure is defined
     */
    private void setInitialStructure(CTBN<NodeTypeCTBN> ctbn) {
        logger.info("Initial structure: {}", this.initialStructure);
        boolean[][] initialAdjMatrix = new boolean[ctbn.getNumNodes()][ctbn.getNumNodes()];
        if (this.initialStructure.equals("Naive Bayes")) {
            // Obtain names class variables
            List<String> nameCVs = this.dataset.getNameClassVariables();
            // Obtain names feature variables
            List<String> nameFVs = this.dataset.getNameFeatureVariables();
            // Get the node indexer of the CTBN
            NodeIndexer<NodeTypeCTBN> nodeIndexer = ctbn.getNodeIndexer();
            // Get the indexes of the class and feature variables in the CTBN
            int[] idxCVs = nameCVs.stream().mapToInt(nameCV -> nodeIndexer.getIndexNodeByName(nameCV)).toArray();
            int[] idxFVs = nameFVs.stream().mapToInt(nameFV -> nodeIndexer.getIndexNodeByName(nameFV)).toArray();
            // Each class variable is defined as the parent of all features
            for (int idxCV : idxCVs)
                for (int idxFV : idxFVs)
                    initialAdjMatrix[idxCV][idxFV] = true;
        } else
            // An empty structure will be used
            return;
        ctbn.setStructure(initialAdjMatrix);
    }

    /**
     * Returns learning algorithms for class subgraph (Bayesian network).
     *
     * @return learning algorithms for class subgraph (Bayesian network)
     */
    public BNLearningAlgorithms getLearningAlgsBN() {
        return this.bnLearningAlgs;
    }

    /**
     * Returns learning algorithms for bridge and features subgraphs (continuous time Bayesian network).
     *
     * @return learning algorithms for bridge and features subgraphs (continuous time Bayesian network)
     */
    public CTBNLearningAlgorithms getLearningAlgsCTBN() {
        return this.ctbnLearningAlgs;
    }

    /**
     * Returns the names of the feature variables.
     *
     * @return names of the feature variables
     */
    public List<String> getNameFeatureVariables() {
        List<NodeTypeCTBN> nodesFeatureVariables = getNodesFeatureVariables();
        return nodesFeatureVariables.stream().map(node -> node.getName()).collect(Collectors.toList());
    }

    /**
     * Returns the number of nodes for the feature variables.
     *
     * @return number of nodes for the feature variables
     */
    public int getNumFeatureVariables() {
        return getNodesFeatureVariables().size();
    }

    /**
     * NodeFactory Returns the type of the class variable nodes.
     *
     * @return type of the class variable nodes
     */
    public Class<NodeTypeBN> getTypeNodeClassVariable() {
        return this.bnNodeClass;
    }

    /**
     * Returns the type of the feature nodes.
     *
     * @return type of the feature nodes
     */
    public Class<NodeTypeCTBN> getTypeNodeFeature() {
        return this.ctbnNodeClass;
    }

    /**
     * Samples a sequence given its duration.
     *
     * @param duration duration of the sequence
     * @return sampled sequence
     */
    public Sequence sample(double duration) {
        return sample(duration, 0, 0);
    }

    /**
     * Samples a sequence given its duration with added noise.
     *
     * @param duration                             duration of the sequence
     * @param percentageNoisyStates                percentage of class variables' states and state transitions of
     *                                             feature variables which are randomly sampled.
     * @param stdDeviationGaussianNoiseWaitingTime standard deviation of the Gaussian distribution used to sample noise
     *                                             to be added to the waiting times of feature variables in a certain
     *                                             state
     * @return sampled sequence
     */
    public Sequence sample(double duration, double percentageNoisyStates,
                           double stdDeviationGaussianNoiseWaitingTime) {
        // Forward sampling Bayesian network (class subgraph). Sample the state
        // of the class variables
        State sampledCVs = sampleClassVariables(percentageNoisyStates);
        // Sample a sequence given the state of the class variables
        return sampleSequence(sampledCVs, duration, percentageNoisyStates, stdDeviationGaussianNoiseWaitingTime);
    }

    /**
     * Samples the state of the class variables.
     */
    private State sampleClassVariables(double percentageNoisyStates) {
        // Define topological order
        List<Node> classVariables = this.bn.getTopologicalOrdering();
        // Map used to save the variable in the order given by the Bayesian network
        Map<String, State> statesClassVariables = new HashMap<>();
        // Store the sampled state of the nodes
        State sampledState = new State();
        for (int i = 0; i < classVariables.size(); i++) {
            // Extract CPT node
            CPTNode cptNode = (CPTNode) classVariables.get(i);
            // Sample state of the node given the states of the currently sampled nodes
            State state = cptNode.sampleState(percentageNoisyStates);
            statesClassVariables.put(cptNode.getName(), state);
        }
        // Save sample states of each node in the order given by the Bayesian network
        for (String nameClassVariable : this.bn.getNameVariables()) {
            // Save sampled state of the node
            sampledState.addEvents(statesClassVariables.get(nameClassVariable).getEvents());
        }
        return sampledState;
    }

    /**
     * Samples the transitions of a sequence given its duration and the state of the class variables.
     *
     * @param sampledCVs sampled state of the class variables
     * @param duration   duration of the sampled sequence
     */
    @SuppressWarnings("unchecked")
    private Sequence sampleSequence(State sampledCVs, double duration, double percentageNoisyTransitions,
                                    double stdDeviationGaussianNoiseWaitingTime) {
        // Features whose transitions will be sampled
        LinkedList<CIMNode> features = new LinkedList<>((List<CIMNode>) this.ctbn.getNodes());
        // List with the time when observations occur
        List<Double> timestamps = new ArrayList<>();
        // List of states with the observations (which form transitions) of the sequence
        List<State> observations = new ArrayList<>();
        // Sample initial observation of the sequence
        State currentObservation = sampleInitialStateSequence(sampledCVs);
        // Add the initial observation and time when it occurred to the results lists
        observations.add(currentObservation);
        double currentTime = 0.0;
        timestamps.add(currentTime);
        // Keep the times when each of the nodes will change their state
        TreeMap<Double, CIMNode> transitionTimes = new TreeMap<>();
        // Generate transitions until the time of the current observation surpasses the
        // duration of the sequence
        while (currentTime < duration) {
            // Create an object that contains the previous observations
            State previousObservation = new State(currentObservation.getEvents());
            // Obtain the time when each feature changes state. It is only considered those
            // features whose next transition time is unknown
            while (!features.isEmpty()) {
                // Get the first feature of the list
                CIMNode featureToSample = features.pollFirst();
                Util.setStateNodeAndParents(featureToSample, previousObservation);
                // Get the time that the variable will stay in its current state
                double sampledTime = featureToSample.sampleTimeState(stdDeviationGaussianNoiseWaitingTime);
                // Add the times to the list ordered from lowest to highest
                transitionTimes.put(currentTime + sampledTime, featureToSample);
            }
            // Get Entry with the next node whose state will change and the time this occurs
            Entry<Double, CIMNode> nextTransition = transitionTimes.pollFirstEntry();
            // Update current time
            currentTime = nextTransition.getKey();
            if (currentTime > duration)
                // The sequence of the desired duration has already been sampled
                break;
            // Get the next node whose state will change
            CIMNode changingNode = nextTransition.getValue();
            Util.setStateNodeAndParents(changingNode, previousObservation);
            // Sample the next state of the node
            State nextState = changingNode.sampleNextState(percentageNoisyTransitions);
            // Transition times of the changing node's children must be resampled
            for (Node childNode : changingNode.getChildren()) {
                transitionTimes.values().remove(childNode);
                features.add((CIMNode) childNode);
            }
            // Get current observation using the previous one and the new state of the node
            currentObservation = new State(previousObservation.getEvents());
            currentObservation.modifyEventValue(changingNode.getName(), nextState.getValues()[0]);
            // Save the observation
            observations.add(currentObservation);
            // Save the time when the observation (transition) occurred
            timestamps.add(currentTime);
            // Add the feature to the list to sample the time its next transition will occur
            features.add(changingNode);
        }
        // Create a sequence with the sampled transitions and the state of the class
        // variables
        Sequence sequence = null;
        try {
            sequence = new Sequence(sampledCVs, observations, "t", timestamps);
        } catch (ErroneousSequenceException ese) {
            logger.warn(ese.getMessage());
        }
        return sequence;
    }

    /**
     * Samples the initial state of the sequence (first observation). Although, theoretically, it should be sampled
     * from
     * a multi-dimensional Bayesian network classifier, a uniform distribution is used for simplicity.
     *
     * @param sampledCVs state of the class variables
     * @return State object with the initial observation of a sequence
     */
    private State sampleInitialStateSequence(State sampledCVs) {
        State evidence = new State();
        for (int i = 0; i < this.ctbn.getNumNodes(); i++) {
            CIMNode node = (CIMNode) this.ctbn.getNodes().get(i);
            // Sample state
            int idxSampledState = (int) (Math.random() * node.getNumStates());
            // Set state feature node
            node.setState(idxSampledState);
            // Add state of the feature node
            evidence.addEvent(node.getName(), node.getState());
        }
        // Add the states of the class variables to the first observation
        evidence.addEvents(sampledCVs.getEvents());
        // Add the initial observation and the time when it occurred
        return new State(evidence.getEvents());
    }

    /**
     * Sets the learning algorithms used to define the class subgraph (BN).
     *
     * @param bnLearningAlgs structure learning algorithms
     */
    public void setBnLearningAlgs(BNLearningAlgorithms bnLearningAlgs) {
        this.bnLearningAlgs = bnLearningAlgs;
    }

    /**
     * Sets the learning algorithms used to define the bridge and feature subgraphs (CTBN).
     *
     * @param ctbnLearningAlgs structure learning algorithms
     */
    public void setCtbnLearningAlgs(CTBNLearningAlgorithms ctbnLearningAlgs) {
        this.ctbnLearningAlgs = ctbnLearningAlgs;
    }

    /**
     * Learns the sets of parents and children of some nodes from a provided dataset and update the model with them.
     *
     * @param nodes   list of nodes
     * @param dataset a dataset
     * @return time to perform the learning
     * @throws ErroneousValueException if an error occurs during the updating process
     */
    public long update(List<Node> nodes, Dataset dataset) throws ErroneousValueException {
        List<String> namesNodes = nodes.stream().map(node -> node.getName()).collect(Collectors.toList());
        logger.info("Updating model in nodes {} with {} training samples", namesNodes, dataset.getNumDataPoints());
        // Measure execution time
        Instant start = Instant.now();
        // Check if it is a feature or class variable node
        // Check if there was a concept drift for any class variable
        // Update the class subgraph
        List<Integer> idxClassNodes = nodes.stream().filter(Node::isClassVariable).map(
                node -> this.bn.getNodeIndexer().getIndexNodeByName(node.getName())).collect(Collectors.toList());
        this.bn.learn(dataset, idxClassNodes);
        // Check if there was a concept drift for any feature variable
        // Update the bridge and feature subgraph
        // Retrieve concept drifting features nodes to update their parent set
        List<Integer> idxFeatureNodes = nodes.stream().filter(node -> !node.isClassVariable()).map(
                node -> this.ctbn.getNodeIndexer().getIndexNodeByName(node.getName())).collect(Collectors.toList());
        this.ctbn.learn(dataset, idxFeatureNodes);
        this.nodes = Stream.concat(getNodesFeatureVariables().stream(), getNodesClassVariables().stream()).collect(
                Collectors.toList());
        Instant end = Instant.now();
        Duration updatingDuration = Duration.between(start, end);
        logger.info("Model updated in {}", updatingDuration);
        return updatingDuration.toMillis();
    }

    /**
     * Updates the bridge and feature subgraphs of the Multi-CTBNC model with new data from the provided dataset. If
     * the model is empty, it learns the model for the first time using the dataset.
     *
     * @param dataset dataset containing the new data for updating the model
     * @return time taken to update the model in milliseconds
     * @throws ErroneousValueException if an error occurs during the updating process
     */
    public long updateBridgeAndFeatureSubgraph(Dataset dataset) throws ErroneousValueException {
        // If the model is empty, use the first dataset to learn it for the first time
        if (this.ctbn == null) {
            logger.info("Learning model for the first time...");
            return learn(dataset);
        }
        Instant start = Instant.now();
        this.dataset = dataset;
        setNameVariables(dataset.getNameVariables());
        // Add new feature to the model
        this.ctbn.setNameVariables(dataset.getNameVariables());
        this.ctbn.initialiseModel(dataset);
        this.ctbn.learn();
        // Update list of nodes of the multi-ctbnc
        this.nodes = new ArrayList<>();
        setStructure();
        Instant end = Instant.now();
        Duration learningDuration = Duration.between(start, end);
        logger.info("Model relearned with new feature added in {}", learningDuration);
        return learningDuration.toMillis();
    }

    /**
     * Defines the nodes of the Multi-CTBNC by using the nodes obtained from the BN and the CTBN.
     */
    private void setStructure() {
        // Obtain nodes that will be added to the model
        List<Node> nodes = new ArrayList<>();
        // It is important to respect the original order of the variables to generate a
        // correct adjacency matrix
        for (String nameVariable : this.dataset.getNameVariables()) {
            // Each variable node is obtained from the BN (if it exists) and from the CTBN
            Node nodeInBN = this.bn.getNodeByName(nameVariable);
            Node nodeInCTBN = this.ctbn.getNodeByName(nameVariable);
            if (nodeInBN != null && nodeInCTBN != null) {
                // Arcs between nodes of the BN and CTBN are created, while those from class
                // variables nodes of the CTBN are removed. A temporary list is used
                // to avoid a concurrent modification exception
                List<Node> tempList = new ArrayList<>(nodeInCTBN.getChildren());
                for (Node child : tempList) {
                    // CTBN class variables nodes are removed from being parents. These nodes are
                    // not necessary anymore
                    child.removeParent(nodeInCTBN);
                    // BN class variables nodes are added as parents for the CTBN feature nodes
                    child.setParent(nodeInBN);
                }
                // Set node as class variable
                nodeInBN.isClassVariable(true);
                nodes.add(nodeInBN);
            } else if (nodeInCTBN != null)
                // It is a feature node
                nodes.add(nodeInCTBN);
        }
        // The nodes are added to the Multi-CTBNC
        addNodes(nodes, false);
    }

    /**
     * Returns the structure constraints for the BN.
     *
     * @return a {@code StructureConstraint}
     */
    public StructureConstraints getStructureConstraintsBN() {
        return new DAG();
    }

    /**
     * Returns the structure constraints for the CTBN.
     *
     * @return a {@code StructureConstraint}
     */
    public StructureConstraints getStructureConstraintsCTBN() {
        return new Digraph();
    }

    @Override
    public boolean areParametersEstimated() {
        return this.bn.areParametersEstimated() && this.ctbn.areParametersEstimated();
    }

    @Override
    public List<Node> getNodes() {
        if (this.nodes != null)
            return this.nodes;
        // Retrieve feature and class variable nodes
        List<Node> nodes = Stream.concat(getNodesFeatureVariables().stream(),
                getNodesClassVariables().stream()).collect(Collectors.toList());
        // Add nodes to the model
        addNodes(nodes, false);
        // Retrieve variable names
        this.nameVariables = nodes.stream().map(node -> node.getName()).collect(Collectors.toList());
        return this.nodes;
    }

    @Override
    public long learn(Dataset dataset) throws ErroneousValueException {
        Instant start = Instant.now();
        logger.info("Learning model with {} training samples", dataset.getNumDataPoints());
        // Remove the previous instantiation of the model (if any)
        removeAllNodes();
        // Save the dataset used for training
        this.dataset = dataset;
        // Extract name variables
        List<String> nameClassVariables = dataset.getNameClassVariables();
        this.nameVariables = dataset.getNameVariables();
        // Measure execution time
        // ------------------ Class subgraph ------------------
        // Learn structure and parameters of class subgraph (Bayesian network)
        logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
        this.bn = new BN<>(dataset, nameClassVariables, this.bnLearningAlgs, getStructureConstraintsBN(),
                this.bnNodeClass);
        this.bn.learn();
        logger.info("Class subgraph established!");
        // ----------- Feature and bridge subgraphs -----------
        // Learn structure and parameters of feature and bridge subgraph. These are
        // modelled by a continuous-time Bayesian network classifier where the
        // restriction that the class variable does not depend on the states of the
        // features is extended to more class variables
        logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous-time " +
                "Bayesian network)");
        this.ctbn = new CTBN<>(dataset, this.nameVariables, this.ctbnLearningAlgs, getStructureConstraintsCTBN(),
                this.bn, this.ctbnNodeClass);
        setInitialStructure(this.ctbn);
        this.ctbn.learn();
        logger.info("Feature and bridge subgraphs established!");
        // ------------------ Join subgraphs ------------------
        // Join class subgraph with feature and bridge subgraphs
        setStructure();
        Instant end = Instant.now();
        Duration learningDuration = Duration.between(start, end);
        logger.info("Model learnt in {}", learningDuration);
        return learningDuration.toMillis();
    }

    @Override
    public void learnParameters(Dataset dataset) {
        getBN().getParameterLearningAlg().learn(getNodesClassVariables(), dataset);
        getCTBN().getParameterLearningAlg().learn(getNodesCTBN(), dataset);
    }

    /**
     * Returns the Bayesian network used to model the class subgraph of the Multi-CTBNC.
     *
     * @return a Bayesian network
     */
    public BN<NodeTypeBN> getBN() {
        return this.bn;
    }

    /**
     * Returns the continuous-time Bayesian network used to model the bridge and feature subgraphs of the Multi-CTBNC.
     *
     * @return a continuous-time Bayesian network
     */
    public CTBN<NodeTypeCTBN> getCTBN() {
        return this.ctbn;
    }

    /**
     * Returns the nodes of the continuous-time Bayesian network modelling the feature and bridge subgraphs of the
     * Multi-CTBNC.
     *
     * @return node list
     */
    public List<NodeTypeCTBN> getNodesCTBN() {
        return this.ctbn.getNodes();
    }

    @Override
    public void learnParameters(Dataset dataset, int idxNode) {
        if (getNodes().get(idxNode).isClassVariable())
            this.bn.learnParameters(dataset, idxNode);
        else
            this.ctbn.learnParameters(dataset, idxNode);
    }

    @Override
    public String getModelIdentifier() {
        return "Multi-CTBNC";
    }

    @Override
    public String getType() {
        if (getNodesClassVariables().size() == 1)
            return "Continuous-time Bayesian network classifier";
        return "Multidimensional continuous-time Bayesian network classifier";
    }

    /**
     * Performs classification over the sequences of a dataset according to the maximum a posteriori probability, i.e.,
     * the classes that obtain the highest posterior probability given each sequence are predicted.
     *
     * @param dataset               dataset from which the sequences to predict are extracted
     * @param estimateProbabilities true to estimate the probabilities of the class configurations, false otherwise
     * @return array of Prediction objects (one per sequence) that contain the predicted classes and, if requested, the
     * probabilities of all possible class configurations
     */
    @Override
    public Prediction[] predict(Dataset dataset, boolean estimateProbabilities) {
        logger.info("Performing prediction over {} sequences", dataset.getNumDataPoints());
        int numSequences = dataset.getNumDataPoints();
        Prediction[] predictions = new Prediction[numSequences];
        // Obtain class configurations
        List<State> statesCVs = getClassConfigurations(this.bn.getNodes());
        // Make predictions on all the sequences (Parallel)
        Instant start = Instant.now();
        IntStream.range(0, numSequences).parallel().forEach(indexSequence -> {
            logger.trace("Performing prediction over sequence {}/{}", indexSequence, dataset.getNumDataPoints());
            Sequence evidenceSequence = dataset.getSequences().get(indexSequence);
            try {
                predictions[indexSequence] = predict(evidenceSequence, statesCVs, estimateProbabilities);
            } catch (Exception exception) {
                logger.error("State of class variables in sequence {} could not be predicted - {}",
                        dataset.getSequences().get(indexSequence).getFilePath(), exception.getMessage());
            }
        });
        Instant end = Instant.now();
        double totalPredictionTime = Duration.between(start, end).toMillis() / 1000.f;
        // Store totalPredictionTime in predictions. Divide prediction time buy the number of instances
        double timePerPrediction = totalPredictionTime / predictions.length;
        IntStream.range(0, predictions.length).parallel().forEach(indexPrediction -> {
            predictions[indexPrediction].setPredictionTime(timePerPrediction);
        });
        logger.info("Sequences predicted in {}", totalPredictionTime + "S");
        return predictions;
    }

    /**
     * Returns State objects with all class configurations.
     *
     * @param nodesCVs nodes of the class variables
     * @return states with all class configurations
     */
    private List<State> getClassConfigurations(List<NodeTypeBN> nodesCVs) {
        List<List<State>> statesEachCV = new ArrayList<List<State>>();
        for (Node nodeCV : nodesCVs) {
            List<State> statesNode = new ArrayList<>();
            for (String valueState : ((DiscreteStateNode) nodeCV).getStates())
                statesNode.add(new State(Map.of(nodeCV.getName(), valueState)));
            statesEachCV.add(statesNode);
        }
        return Util.cartesianProduct(statesEachCV);
    }

    /**
     * Performs classification over a sequence according to the maximum a posteriori probability.
     *
     * @param sequence             sequence whose class variables are predicted
     * @param statesClassVariables possible class configurations
     * @return Prediction object that contains the predicted classes and, if requested, the probabilities of all
     * possible class configurations
     */
    private Prediction predict(Sequence sequence, List<State> statesClassVariables, boolean estimateProbabilities) {
        Instant start = Instant.now();
        // Obtain nodes of the Bayesian network and continuous-time Bayesian network
        // If the prediction task is parallelised
        MultiCTBNC<NodeTypeBN, NodeTypeCTBN> tmpMultiCTBNC = new MultiCTBNC<>(this.bn, this.ctbn);
        List<NodeTypeBN> nodesBN = tmpMultiCTBNC.getNodesClassVariables();
        List<NodeTypeCTBN> nodesCTBN = tmpMultiCTBNC.getNodesCTBNInMarkovBlanketClassVariables();
        // Estimate unnormalised log-a-posteriori probabilities of class configurations
        double[] laps = new double[statesClassVariables.size()];
        for (int i = 0; i < statesClassVariables.size(); i++) {
            // Get class configuration 'i' of the class variables
            State stateClassVariables = statesClassVariables.get(i);
            // Compute the log-a-posteriori probability of current class configuration 'i'
            double lap = 0;
            // Estimate the log-prior probability of the class configuration 'i'
            lap += ProbabilityUtil.logPriorProbabilityClassVariables(nodesBN, stateClassVariables);
            // Estimate log-likelihood of the sequence given the classes 'i'
            lap += ProbabilityUtil.logLikelihoodSequence(sequence, nodesCTBN, stateClassVariables);
            // Store log-a-posteriori probability of class variables taking classes 'i'
            laps[i] = lap;
        }
        // Define a Prediction object to save the result
        Prediction prediction = new Prediction();
        // Retrieve class configuration that obtains the largest a-posterior probability
        int idxBestCC = Util.getIndexLargestValue(laps);
        // The state is cloned to avoid different predictions using the same objects
        State predictedCC = new State(statesClassVariables.get(idxBestCC));
        // Save the predicted classes
        prediction.setPredictedClasses(predictedCC);
        if (estimateProbabilities) {
            // If the class probabilities are requested, the marginal log-likelihood
            // (Normalising constant) is estimated
            double mll = ProbabilityUtil.marginalLogLikelihoodSequence(laps);
            // Save probabilities of each class configuration
            for (int k = 0; k < statesClassVariables.size(); k++) {
                // Obtain class probability (normalised a-posteriori probability)
                double ap = Math.exp(laps[k] - mll);
                prediction.setProbability(statesClassVariables.get(k), ap);
            }
            prediction.setProbabilityPrediction(prediction.getProbabilities().get(predictedCC));
        }
        Instant end = Instant.now();
        double predictionTime = Duration.between(start, end).toMillis() / 1000.f;
        prediction.setPredictionTime(predictionTime);
        return prediction;
    }

    /**
     * Returns the nodes of the continuous-time Bayesian network modelling the feature and bridge subgraphs of the
     * Multi-CTBNC, which are in the Markov blaket of at least one class variable.
     *
     * @return node list
     */
    public List<NodeTypeCTBN> getNodesCTBNInMarkovBlanketClassVariables() {
        // Retrieve feature nodes that are part of the Markov blanket of a class variable
        return this.ctbn.getNodes().stream().filter(node -> node.isInMarkovBlanketClassVariable()).collect(
                Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getNumClassVariables() > 1)
            sb.append("--Structure multi-dimensional continuous-time Bayesian network classifier--\n");
        else
            sb.append("--Structure continuous-time Bayesian network classifier--\n");
        for (Node node : getNodes()) {
            if (node.getParents().isEmpty())
                sb.append("{}");
            else
                for (Node parent : node.getParents())
                    sb.append("(" + parent.getName() + ")");
            sb.append(" => (" + node.getName() + ") \n");
        }
        return sb.toString();
    }

    /**
     * Returns the number of nodes for the class variables.
     *
     * @return number of nodes for the class variables
     */
    public int getNumClassVariables() {
        return getNodesClassVariables().size();
    }

}