package ctbn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CIMNode;

/**
 * Test the estimation of sufficient statistics and parameters of a CTBN.
 * Maximum likelihood estimation test is executed first so the sufficient
 * statistics are computed before their tests start.
 * 
 * @author Carlos Villa Blanco
 *
 */
@TestMethodOrder(OrderAnnotation.class)
public class LearnParametersCTBNTest {
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
		for (String nameFeature : dataset.getNameVariables()) {
			nodes.add(new CIMNode(nameFeature, dataset.getPossibleStatesVariable(nameFeature)));
		}

		// V2 and V3 are parents of V1 and V3 is parent of V2
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
	}

	/**
	 * Check the computation of the sufficient statistic Nxx for a certain CTBN and
	 * dataset.
	 */
	@Test
	public void testSufficientStatisticNxxCTBN() {
		// Sufficient statistics V1
		Map<State, Map<State, Integer>> ssV1 = nodes.get(0).getSufficientStatistics().getNxy();
		// Sufficient statistics V2
		Map<State, Map<State, Integer>> ssV2 = nodes.get(1).getSufficientStatistics().getNxy();
		// Sufficient statistics V3
		Map<State, Map<State, Integer>> ssV3 = nodes.get(2).getSufficientStatistics().getNxy();

		// Sufficient statistics V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		State toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(2, (int) ssV1.get(fromState).get(toState));
		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(1, (int) ssV1.get(fromState).get(toState));

		fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0, (int) ssV1.get(fromState).get(toState));
		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(1, (int) ssV1.get(fromState).get(toState));

		// Sufficient statistics V2
		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		toState = new State();
		toState.addEvent("V2", "b");
		assertEquals(0, (int) ssV2.get(fromState).get(toState));

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, (int) ssV2.get(fromState).get(toState));

		// Sufficient statistics V3
		fromState = new State();
		fromState.addEvent("V3", "a");
		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0, (int) ssV3.get(fromState).get(toState));
		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(1, (int) ssV3.get(fromState).get(toState));

		fromState = new State();
		fromState.addEvent("V3", "b");
		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(3, (int) ssV3.get(fromState).get(toState));
		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(0, (int) ssV3.get(fromState).get(toState));
	}

	/**
	 * Check the computation of the sufficient statistic Nx for a certain CTBN and
	 * dataset.
	 */
	@Test
	public void testSufficientStatisticNxCTBN() {
		// Sufficient statistics V1
		Map<State, Integer> ssV1 = nodes.get(0).getSufficientStatistics().getNx();
		// Sufficient statistics V2
		Map<State, Integer> ssV2 = nodes.get(1).getSufficientStatistics().getNx();
		// Sufficient statistics V3
		Map<State, Integer> ssV3 = nodes.get(2).getSufficientStatistics().getNx();

		// Sufficient statistics V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		assertEquals(3, (int) ssV1.get(fromState));

		fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		assertEquals(1, (int) ssV1.get(fromState));

		// Sufficient statistics V2
		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		assertEquals(0, (int) ssV2.get(fromState));

		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "b");
		assertEquals(1, (int) ssV2.get(fromState));

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "a");
		assertEquals(0, (int) ssV2.get(fromState));

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		assertEquals(1, (int) ssV2.get(fromState));

		// Sufficient statistics V3
		fromState = new State();
		fromState.addEvent("V3", "a");
		assertEquals(1, (int) ssV3.get(fromState));

		fromState = new State();
		fromState.addEvent("V3", "b");
		assertEquals(3, (int) ssV3.get(fromState));

		fromState = new State();
		fromState.addEvent("V3", "c");
		assertEquals(0, (int) ssV3.get(fromState));
	}

	/**
	 * Check the sufficient statistic Tx, for a certain CTBN and dataset, related to
	 * the time that a variable stay in a certain state.
	 */
	@Test
	public void testSufficientStatisticTxCTBN() {
		// Test the sufficient parameters for certain combinations of states
		// Sufficient statistics V1
		Map<State, Double> ssV1 = nodes.get(0).getSufficientStatistics().getTx();
		// Sufficient statistics V2
		Map<State, Double> ssV2 = nodes.get(1).getSufficientStatistics().getTx();
		// Sufficient statistics V3
		Map<State, Double> ssV3 = nodes.get(2).getSufficientStatistics().getTx();

		// Sufficient statistics V1
		State state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.5, ssV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "c");
		state.addEvent("V2", "a");
		state.addEvent("V3", "c"); // Last observation of a sequence. It never changes, so the
									// duration is unknown.
		assertEquals(0, ssV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(1.0, ssV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "c"); // State that never occurs
		assertEquals(0, ssV1.get(state), 0.001);

		// Sufficient statistics V2
		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.5, ssV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "b");
		assertEquals(0.4, ssV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0, ssV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "a");
		assertEquals(0, ssV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(1.7, ssV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "c");
		assertEquals(0, ssV2.get(state), 0.001);

		// Sufficient statistics V3
		state = new State();
		state.addEvent("V3", "a");
		assertEquals(0.5, ssV3.get(state), 0.001);

		state = new State();
		state.addEvent("V3", "b");
		assertEquals(2.1, ssV3.get(state), 0.001);

		state = new State();
		state.addEvent("V3", "c");
		assertEquals(0, ssV3.get(state), 0.001);
	}

	/**
	 * Check the parameters of a CTBN obtained by maximum likelihood estimation.
	 */
	@Test
	@Order(1)
	public void testMLE() {
		// Learn sufficient statistics and parameters given the nodes and the dataset
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNMaximumLikelihoodEstimation();
		ctbnParameterLearning.learn(nodes, dataset);
		testMLEParameterQxCTBN();
		testMLEParameterOxxCTBN();
	}

	/**
	 * Check the parameter qx, i.e., the instantaneous probability of a variable
	 * leaving a certain state for another given the state of its parents.
	 */
	public void testMLEParameterQxCTBN() {
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
		assertEquals(2, qxV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(3, qxV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "b");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0, qxV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "c");
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0, qxV1.get(state), 0.001);

		// Parameters V2
		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "b");
		assertEquals(2.5, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "a");
		assertEquals(0, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(0.588, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "c");
		assertEquals(0, qxV2.get(state), 0.001);

		// Sufficient statistics V3
		state = new State();
		state.addEvent("V3", "a");
		assertEquals(2, qxV3.get(state), 0.001);

		state = new State();
		state.addEvent("V3", "b");
		assertEquals(1.428, qxV3.get(state), 0.001);

		state = new State();
		state.addEvent("V3", "c");
		assertEquals(0, qxV3.get(state), 0.001);
	}

	/**
	 * Check the parameter Oxx, i.e., the probability that a variable transitions
	 * from one state to another while its parents are in a certain state.
	 */
	public void testMLEParameterOxxCTBN() {
		// Parameters V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV1 = nodes.get(0).getOxx().get(fromState);

		State toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0, OxV1.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(1, OxV1.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV1 = nodes.get(0).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0.666, OxV1.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(0.333, OxV1.get(toState), 0.001);

		// Parameters V2
		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV2 = nodes.get(1).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V2", "b");
		assertEquals(0, OxV2.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV2 = nodes.get(1).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, OxV2.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "c");
		OxV2 = nodes.get(1).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(0, OxV2.get(toState), 0.001);

		// Parameters V3
		fromState = new State();
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV3 = nodes.get(2).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0, OxV3.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(1, OxV3.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V3", "b");
		OxV3 = nodes.get(2).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(1, OxV3.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(0, OxV3.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V3", "c");
		OxV3 = nodes.get(2).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(0, OxV3.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0, OxV3.get(toState), 0.001);
	}

	/**
	 * Check the parameters of a CTBN obtained by Bayesian estimation. The imaginary
	 * counts are all established to 0, so the result should be the same as with
	 * maximum likelihood estimation.
	 */
	@Test
	public void testBE_ImaginaryCountsOff() {
		// Establish imaginary counts
		double NxyPrior = 0.0;
		double NxPrior = 0.0;
		double TxPrior = 0.0;
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNBayesianEstimation(NxyPrior, NxPrior, TxPrior);
		ctbnParameterLearning.learn(nodes, dataset);
		testMLEParameterQxCTBN();
		testMLEParameterOxxCTBN();
	}

	/**
	 * Check the parameters of a CTBN obtained by Bayesian estimation. The imaginary
	 * counts are all established to 0, so the result should be the same as with
	 * maximum likelihood estimation.
	 */
	@Test
	public void testBE_ImaginaryCountsOn() {
		// Establish imaginary counts
		double NxyPrior = 1.0;
		double NxPrior = 1.0;
		double TxPrior = 2.0;
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNBayesianEstimation(NxyPrior, NxPrior, TxPrior);
		ctbnParameterLearning.learn(nodes, dataset);
		testBEParameterQxCTBN();
		testBEParameterOxxCTBN();
	}

	/**
	 * Check the parameter qx, i.e., the instantaneous probability of a variable
	 * leaving a certain state for another given the state of its parents.
	 */
	public void testBEParameterQxCTBN() {
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
		assertEquals(0.8, qxV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(1.333, qxV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "b");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.5, qxV1.get(state), 0.001);

		state = new State();
		state.addEvent("V1", "c");
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0.5, qxV1.get(state), 0.001);

		// Parameters V2
		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.4, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "b");
		assertEquals(0.833, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0.5, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "a");
		assertEquals(0.5, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(0.540, qxV2.get(state), 0.001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "c");
		assertEquals(0.5, qxV2.get(state), 0.001);

		// Sufficient statistics V3
		state = new State();
		state.addEvent("V3", "a");
		assertEquals(0.8, qxV3.get(state), 0.001);

		state = new State();
		state.addEvent("V3", "b");
		assertEquals(0.975, qxV3.get(state), 0.001);

		state = new State();
		state.addEvent("V3", "c");
		assertEquals(0.5, qxV3.get(state), 0.001);
	}

	/**
	 * Check the parameter Oxx, i.e., the probability that a variable transitions
	 * from one state to another while its parents are in a certain state.
	 */
	public void testBEParameterOxxCTBN() {
		// Parameters V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV1 = nodes.get(0).getOxx().get(fromState);

		State toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0.5, OxV1.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(1, OxV1.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV1 = nodes.get(0).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V1", "b");
		assertEquals(0.75, OxV1.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V1", "c");
		assertEquals(0.5, OxV1.get(toState), 0.001);

		// Parameters V2
		fromState = new State();
		fromState.addEvent("V2", "a");
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV2 = nodes.get(1).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V2", "b");
		assertEquals(1, OxV2.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		OxV2 = nodes.get(1).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, OxV2.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "c");
		OxV2 = nodes.get(1).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V2", "a");
		assertEquals(1, OxV2.get(toState), 0.001);

		// Parameters V3
		fromState = new State();
		fromState.addEvent("V3", "a");
		Map<State, Double> OxV3 = nodes.get(2).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(0.5, OxV3.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(1, OxV3.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V3", "b");
		OxV3 = nodes.get(2).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(1, OxV3.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V3", "c");
		assertEquals(0.25, OxV3.get(toState), 0.001);

		fromState = new State();
		fromState.addEvent("V3", "c");
		OxV3 = nodes.get(2).getOxx().get(fromState);

		toState = new State();
		toState.addEvent("V3", "a");
		assertEquals(1, OxV3.get(toState), 0.001);

		toState = new State();
		toState.addEvent("V3", "b");
		assertEquals(1, OxV3.get(toState), 0.001);
	}

}
