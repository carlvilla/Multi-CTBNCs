package ctbn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

/**
 * Test the estimation of sufficient statistics and parameters of a CTBN.
 * 
 * @author Carlos Villa Blanco
 *
 */
@TestMethodOrder(OrderAnnotation.class)
class LearnParametersCTBNTest {
	static Dataset dataset;
	static List<CIMNode> nodes;

	/**
	 * Defines a dataset and the structure of a CTBN. Instead of learning the
	 * structure of the CTBN from the data, it is directly established over CPT
	 * nodes. This is done to know which structure we are working on during the
	 * tests. The CTBN will be formed by three variable V1, V2 and V3, where V2 and
	 * V3 are parents of V1 and V3 is parent of V2.
	 * 
	 */
	@BeforeAll
	public static void setUp() {
		// Define dataset
		List<String> nameClassVariables = new ArrayList<String>();
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence1.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence1.add(new String[] { "0.1", "a", "b", "b" });
		dataSequence1.add(new String[] { "0.3", "a", "b", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence2.add(new String[] { "0.0", "a", "a", "b" });
		dataSequence2.add(new String[] { "0.4", "a", "b", "b" });
		dataSequence2.add(new String[] { "0.5", "b", "b", "b" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence3.add(new String[] { "0.2", "a", "a", "a" });
		dataSequence3.add(new String[] { "0.7", "c", "a", "c" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence4.add(new String[] { "0.0", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.2", "a", "b", "b" });
		dataSequence4.add(new String[] { "0.5", "c", "b", "b" });

		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence5.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence5.add(new String[] { "0.2", "b", "b", "b" });
		dataSequence5.add(new String[] { "0.7", "b", "b", "a" });

		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence6.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence6.add(new String[] { "0.1", "a", "a", "a" });

		// Following sequence is added, but it will be irrelevant for the parameters
		List<String[]> dataSequence7 = new ArrayList<String[]>();
		dataSequence7.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence7.add(new String[] { "0.5", "c", "b", "b" });

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6);
		dataset.addSequence(dataSequence7);

		// Define nodes
		// As it is tested the learned parameters, that a node is a class variable or
		// not is irrelevant.
		nodes = new ArrayList<CIMNode>();
		for (String nameFeature : dataset.getNameVariables())
			nodes.add(new CIMNode(nameFeature, dataset.getPossibleStatesVariable(nameFeature)));

		// V2 and V3 are parents of V1 and V3 is parent of V2
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
	}

	/**
	 * Check the parameters of a CTBN obtained by maximum likelihood estimation.
	 */
	@Test
	public void testMLE() {
		// Learn sufficient statistics and parameters given the nodes and the dataset
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNMaximumLikelihoodEstimation();
		ctbnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testMLESufficientStatisticMxy();
		testMLESufficientStatisticMx();
		testMLESufficientStatisticTx();
		// Parameters
		testMLEParameterQx();
		testMLEParameterOxx();
	}

	/**
	 * Check the computation of the sufficient statistic Mxy for a certain CTBN and
	 * dataset when using maximum likelihood estimation.
	 */
	public void testMLESufficientStatisticMxy() {
		// Retrieve nodes
		CIMNode nodeV1 = nodes.get(0);
		CIMNode nodeV2 = nodes.get(1);
		CIMNode nodeV3 = nodes.get(2);
		// Sufficient statistics Mxy of node V1
		double[][][] mxyV1 = nodeV1.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node V2
		double[][][] mxyV2 = nodeV2.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node V3
		double[][][] mxyV3 = nodeV3.getSufficientStatistics().getMxy();

		// Check sufficient statistics V1
		Integer idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("b");
		Integer idxStateParents = nodeV1.getIdxStateParents();
		Integer idxToStateNode = nodeV1.setState("b");
		assertEquals(2, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV1.setState("c");
		assertEquals(1, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV1.getIdxStateParents();
		idxToStateNode = nodeV1.setState("b");
		assertEquals(0, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV1.setState("c");
		assertEquals(1, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);

		// Sufficient statistics V2
		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		idxToStateNode = nodeV2.setState("b");
		assertEquals(0, mxyV2[idxStateParents][idxFromStateNode][idxToStateNode]);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		idxToStateNode = nodeV2.setState("a");
		assertEquals(1, mxyV2[idxStateParents][idxFromStateNode][idxToStateNode]);

		// Sufficient statistics V3
		idxFromStateNode = nodeV3.setState("a");
		idxStateParents = nodeV3.getIdxStateParents();
		idxToStateNode = nodeV3.setState("b");
		assertEquals(0, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV3.setState("c");
		assertEquals(1, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);

		idxFromStateNode = nodeV3.setState("b");
		idxStateParents = nodeV3.getIdxStateParents();
		idxToStateNode = nodeV3.setState("a");
		assertEquals(3, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV3.setState("c");
		assertEquals(0, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);
	}

	/**
	 * Check the computation of the sufficient statistic Mx for a certain CTBN and
	 * dataset when using maximum likelihood estimation.
	 */
	public void testMLESufficientStatisticMx() {
		// Retrieve nodes
		CIMNode nodeV1 = nodes.get(0);
		CIMNode nodeV2 = nodes.get(1);
		CIMNode nodeV3 = nodes.get(2);
		// Sufficient statistics Mxy of node V1
		double[][] mxV1 = nodeV1.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node V2
		double[][] mxV2 = nodeV2.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node V3
		double[][] mxV3 = nodeV3.getSufficientStatistics().getMx();

		// Check sufficient statistics V1
		Integer idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("b");
		Integer idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(3, mxV1[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(1, mxV1[idxStateParents][idxFromStateNode]);

		// Check sufficient statistics V2
		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0, mxV2[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(1, mxV2[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0, mxV2[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(1, mxV2[idxStateParents][idxFromStateNode]);

		// Check sufficient statistics V3
		idxFromStateNode = nodeV3.setState("a");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(1, mxV3[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV3.setState("b");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(3, mxV3[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV3.setState("c");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(0, mxV3[idxStateParents][idxFromStateNode]);
	}

	/**
	 * Check the sufficient statistic Tx, for a certain CTBN and dataset, related to
	 * the time that a variable stay in a certain state, when using maximum
	 * likelihood estimation.
	 */
	public void testMLESufficientStatisticTx() {
		// Retrieve nodes
		CIMNode nodeV1 = nodes.get(0);
		CIMNode nodeV2 = nodes.get(1);
		CIMNode nodeV3 = nodes.get(2);
		// Sufficient statistics Mxy of node V1
		double[][] txV1 = nodeV1.getSufficientStatistics().getTx();
		// Sufficient statistics Mxy of node V2
		double[][] txV2 = nodeV2.getSufficientStatistics().getTx();
		// Sufficient statistics Mxy of node V3
		double[][] txV3 = nodeV3.getSufficientStatistics().getTx();

		// Check sufficient statistics V1
		Integer idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("a");
		nodeV3.setState("a");
		Integer idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(0.5, txV1[idxStateParents][idxFromStateNode], 0.01);

		// Last observation of a sequence. It never changes, so the duration is unknown.
		idxFromStateNode = nodeV1.setState("c");
		nodeV2.setState("a");
		nodeV3.setState("c");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(0, txV1[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(1.0, txV1[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("c");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(0, txV1[idxStateParents][idxFromStateNode], 0.01);

		// Check sufficient statistics V2
		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0.5, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0.4, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("c");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(1.7, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("c");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(0, txV2[idxStateParents][idxFromStateNode], 0.01);

		// Check sufficient statistics V3
		idxFromStateNode = nodeV3.setState("a");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(0.5, txV3[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV3.setState("b");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(2.1, txV3[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV3.setState("c");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(0, txV3[idxStateParents][idxFromStateNode], 0.01);
	}

	/**
	 * Check the parameter qx, i.e., the instantaneous probability of a variable
	 * leaving a certain state for another given the state of its parents.
	 */
	public void testMLEParameterQx() {
		// Parameters V1
		Map<State, Double> qxV1 = nodes.get(0).getQx();
		// Parameters V2
		Map<State, Double> qxV2 = nodes.get(1).getQx();
		// Parameters V3
		Map<State, Double> qxV3 = nodes.get(2).getQx();

		// Parameters V1
		State state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(2, qxV1.get(state), 0.01);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(3, qxV1.get(state), 0.01);

		state = new State();
		state.addEvent("V1", "b");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0, qxV1.get(state), 0.01);

		state = new State();
		state.addEvent("V1", "c");
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0, qxV1.get(state), 0.01);

		// Parameters V2
		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "b");
		assertEquals(2.5, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "a");
		assertEquals(0, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(0.58, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "c");
		assertEquals(0, qxV2.get(state), 0.01);

		// Sufficient statistics V3
		state = new State();
		state.addEvent("V3", "a");
		assertEquals(2, qxV3.get(state), 0.01);

		state = new State();
		state.addEvent("V3", "b");
		assertEquals(1.42, qxV3.get(state), 0.01);

		state = new State();
		state.addEvent("V3", "c");
		assertEquals(0, qxV3.get(state), 0.01);
	}

	/**
	 * Check the parameter Oxx, i.e., the probability that a variable transitions
	 * from one state to another while its parents are in a certain state.
	 */
	public void testMLEParameterOxx() {
		// Parameters V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV1 = nodes.get(0).getOxy().get(fromState);

		State toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0, OxV1.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(1, OxV1.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV1 = nodes.get(0).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0.66, OxV1.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(0.33, OxV1.get(toState), 0.01);

		// Parameters V2
		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV2 = nodes.get(1).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V2", "b");
		assertEquals(0, OxV2.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV2 = nodes.get(1).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, OxV2.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "c");
		OxV2 = nodes.get(1).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(0, OxV2.get(toState), 0.01);

		// Parameters V3
		fromState = new State();
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV3 = nodes.get(2).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0, OxV3.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(1, OxV3.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V3", "b");
		OxV3 = nodes.get(2).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(1, OxV3.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(0, OxV3.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V3", "c");
		OxV3 = nodes.get(2).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(0, OxV3.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0, OxV3.get(toState), 0.01);
	}

	/**
	 * Check the parameters of a CTBN obtained by Bayesian estimation. The imaginary
	 * counts are all established to 0, so the result should be the same as with
	 * maximum likelihood estimation.
	 */
	@Test
	public void testBE_ImaginaryCountsOff() {
		// Establish imaginary counts
		double MxyPrior = 0.0;
		double TxPrior = 0.0;
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNBayesianEstimation(MxyPrior, TxPrior);
		ctbnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testMLESufficientStatisticMxy();
		testMLESufficientStatisticMx();
		testMLESufficientStatisticTx();
		// Parameters
		testMLEParameterQx();
		testMLEParameterOxx();
	}

	/**
	 * Check the parameters of a CTBN obtained by Bayesian estimation. All the
	 * imaginary counts are more than 0.
	 */
	@Test
	public void testBE_ImaginaryCountsOn() {
		// Establish imaginary counts
		double MxyPrior = 1.0;
		double TxPrior = 2.0;
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNBayesianEstimation(MxyPrior, TxPrior);
		ctbnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testBESufficientStatisticMxy();
		testBESufficientStatisticMx();
		testBESufficientStatisticTx();
		// Parameters
		testBEParameterQx();
		testBEParameterOxx();
	}

	/**
	 * Check the computation of the sufficient statistic Mxy for a certain CTBN and
	 * dataset when using Bayesian estimation.
	 */
	public void testBESufficientStatisticMxy() {
		// Retrieve nodes
		CIMNode nodeV1 = nodes.get(0);
		CIMNode nodeV2 = nodes.get(1);
		CIMNode nodeV3 = nodes.get(2);
		// Sufficient statistics Mxy of node V1
		double[][][] mxyV1 = nodeV1.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node V2
		double[][][] mxyV2 = nodeV2.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node V3
		double[][][] mxyV3 = nodeV3.getSufficientStatistics().getMxy();

		// Check sufficient statistics V1
		Integer idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("b");
		Integer idxStateParents = nodeV1.getIdxStateParents();
		Integer idxToStateNode = nodeV1.setState("b");
		assertEquals(3, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV1.setState("c");
		assertEquals(2, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV1.getIdxStateParents();
		idxToStateNode = nodeV1.setState("b");
		assertEquals(1, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV1.setState("c");
		assertEquals(2, mxyV1[idxStateParents][idxFromStateNode][idxToStateNode]);

		// Sufficient statistics V2
		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		idxToStateNode = nodeV2.setState("b");
		assertEquals(1, mxyV2[idxStateParents][idxFromStateNode][idxToStateNode]);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		idxToStateNode = nodeV2.setState("a");
		assertEquals(2, mxyV2[idxStateParents][idxFromStateNode][idxToStateNode]);

		// Sufficient statistics V3
		idxFromStateNode = nodeV3.setState("a");
		idxStateParents = nodeV3.getIdxStateParents();
		idxToStateNode = nodeV3.setState("b");
		assertEquals(1, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV3.setState("c");
		assertEquals(2, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);

		idxFromStateNode = nodeV3.setState("b");
		idxStateParents = nodeV3.getIdxStateParents();
		idxToStateNode = nodeV3.setState("a");
		assertEquals(4, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);
		idxToStateNode = nodeV3.setState("c");
		assertEquals(1, mxyV3[idxStateParents][idxFromStateNode][idxToStateNode]);
	}

	/**
	 * Check the computation of the sufficient statistic Mx for a certain CTBN and
	 * dataset when using Bayesian estimation.
	 */
	public void testBESufficientStatisticMx() {
		// Retrieve nodes
		CIMNode nodeV1 = nodes.get(0);
		CIMNode nodeV2 = nodes.get(1);
		CIMNode nodeV3 = nodes.get(2);
		// Sufficient statistics Mxy of node V1
		double[][] mxV1 = nodeV1.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node V2
		double[][] mxV2 = nodeV2.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node V3
		double[][] mxV3 = nodeV3.getSufficientStatistics().getMx();

		// Check sufficient statistics V1
		Integer idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("b");
		Integer idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(5, mxV1[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(3, mxV1[idxStateParents][idxFromStateNode]);

		// Check sufficient statistics V2
		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(1, mxV2[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2, mxV2[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(1, mxV2[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2, mxV2[idxStateParents][idxFromStateNode]);

		// Check sufficient statistics V3
		idxFromStateNode = nodeV3.setState("a");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(3, mxV3[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV3.setState("b");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(5, mxV3[idxStateParents][idxFromStateNode]);

		idxFromStateNode = nodeV3.setState("c");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(2, mxV3[idxStateParents][idxFromStateNode]);
	}

	/**
	 * Check the sufficient statistic Tx, for a certain CTBN and dataset, related to
	 * the time that a variable stay in a certain state, when using Bayesian
	 * estimation.
	 */
	public void testBESufficientStatisticTx() {
		// Retrieve nodes
		CIMNode nodeV1 = nodes.get(0);
		CIMNode nodeV2 = nodes.get(1);
		CIMNode nodeV3 = nodes.get(2);
		// Sufficient statistics Mxy of node V1
		double[][] txV1 = nodeV1.getSufficientStatistics().getTx();
		// Sufficient statistics Mxy of node V2
		double[][] txV2 = nodeV2.getSufficientStatistics().getTx();
		// Sufficient statistics Mxy of node V3
		double[][] txV3 = nodeV3.getSufficientStatistics().getTx();

		// Check sufficient statistics V1
		Integer idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("a");
		nodeV3.setState("a");
		Integer idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(2.5, txV1[idxStateParents][idxFromStateNode], 0.01);

		// Last observation of a sequence. It never changes, so the duration is unknown.
		idxFromStateNode = nodeV1.setState("c");
		nodeV2.setState("a");
		nodeV3.setState("c");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(2, txV1[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(3, txV1[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV1.setState("a");
		nodeV2.setState("b");
		nodeV3.setState("c"); // State that never occurs
		idxStateParents = nodeV1.getIdxStateParents();
		assertEquals(2, txV1[idxStateParents][idxFromStateNode], 0.01);

		// Check sufficient statistics V2
		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2.5, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2.4, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("a");
		nodeV3.setState("c");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("a");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("b");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(3.7, txV2[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV2.setState("b");
		nodeV3.setState("c");
		idxStateParents = nodeV2.getIdxStateParents();
		assertEquals(2, txV2[idxStateParents][idxFromStateNode], 0.01);

		// Check sufficient statistics V3
		idxFromStateNode = nodeV3.setState("a");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(2.5, txV3[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV3.setState("b");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(4.1, txV3[idxStateParents][idxFromStateNode], 0.01);

		idxFromStateNode = nodeV3.setState("c");
		idxStateParents = nodeV3.getIdxStateParents();
		assertEquals(2, txV3[idxStateParents][idxFromStateNode], 0.01);
	}

	/**
	 * Check the parameter qx, i.e., the instantaneous probability of a variable
	 * leaving a certain state for another given the state of its parents.
	 */
	public void testBEParameterQx() {
		// Parameters V1
		Map<State, Double> qxV1 = nodes.get(0).getQx();
		// Parameters V2
		Map<State, Double> qxV2 = nodes.get(1).getQx();
		// Parameters V3
		Map<State, Double> qxV3 = nodes.get(2).getQx();

		// Parameters V1
		State state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(1.2, qxV1.get(state), 0.01);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(1.66, qxV1.get(state), 0.01);

		state = new State();
		state.addEvent("V1", "b");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(1, qxV1.get(state), 0.01);

		state = new State();
		state.addEvent("V1", "c");
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(1, qxV1.get(state), 0.01);

		// Parameters V2
		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.4, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "b");
		assertEquals(0.83, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0.5, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "a");
		assertEquals(0.5, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(0.54, qxV2.get(state), 0.01);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "c");
		assertEquals(0.5, qxV2.get(state), 0.01);

		// Sufficient statistics V3
		state = new State();
		state.addEvent("V3", "a");
		assertEquals(1.2, qxV3.get(state), 0.01);

		state = new State();
		state.addEvent("V3", "b");
		assertEquals(1.21, qxV3.get(state), 0.01);

		state = new State();
		state.addEvent("V3", "c");
		assertEquals(1, qxV3.get(state), 0.01);
	}

	/**
	 * Check the parameter Oxx, i.e., the probability that a variable transitions
	 * from one state to another while its parents are in a certain state.
	 */
	public void testBEParameterOxx() {
		// Parameters V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV1 = nodes.get(0).getOxy().get(fromState);

		State toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0.33, OxV1.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(0.66, OxV1.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV1 = nodes.get(0).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0.6, OxV1.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(0.4, OxV1.get(toState), 0.01);

		// Parameters V2
		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV2 = nodes.get(1).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V2", "b");
		assertEquals(1, OxV2.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV2 = nodes.get(1).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, OxV2.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "c");
		OxV2 = nodes.get(1).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, OxV2.get(toState), 0.01);

		// Parameters V3
		fromState = new State();
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV3 = nodes.get(2).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0.33, OxV3.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(0.66, OxV3.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V3", "b");
		OxV3 = nodes.get(2).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(0.8, OxV3.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(0.2, OxV3.get(toState), 0.01);

		fromState = new State();
		fromState.addEvent("V3", "c");
		OxV3 = nodes.get(2).getOxy().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(0.5, OxV3.get(toState), 0.01);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0.5, OxV3.get(toState), 0.01);
	}

}
