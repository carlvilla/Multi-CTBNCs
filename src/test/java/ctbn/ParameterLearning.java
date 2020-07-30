package ctbn;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.CTBNParameterMLE;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

public class ParameterLearning {

	CTBNParameterMLE ctbnParameterLearning;

	/**
	 * Defines a dataset by creating first the sequences.
	 */
	@Before
	public void generateDatasetFromSequences() {
		// Define dataset
		String[] nameClassVariables = {};
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

		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence5.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence5.add(new String[] { "0.2", "b", "b", "b" });
		dataSequence5.add(new String[] { "0.7", "b", "b", "a" });

		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence6.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence6.add(new String[] { "0.1", "a", "a", "a" });

		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6);

		// Define nodes
		// As it is tested the learned parameters, that a node is a class variable or
		// not is irrelevant.
		List<Node> nodes = new ArrayList<Node>();
		for (String nameFeature : dataset.getNameVariables()) {
			int index = dataset.getIndexVariable(nameFeature);
			nodes.add(new DiscreteNode(index, nameFeature, false, dataset.getStatesVariable(nameFeature)));
		}

		// V2 and V3 are parents of V1 and V3 is parent of V2
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));

		// Learn sufficient parameters given the nodes and the dataset
		ctbnParameterLearning = new CTBNParameterMLE();
		ctbnParameterLearning.learn(nodes, dataset);
	}

	/**
	 * Check the computation of the sufficient statistic Nxx for a certain CTBN and
	 * dataset.
	 */
	@Test
	public void testSufficientStatisticNxxCTBN() {
		List<CIMNode> cimNodes = ctbnParameterLearning.getParameters();
		// Sufficient statistics V1
		Map<State, Map<State, Integer>> ssV1 = cimNodes.get(0).getSufficientStatistics().getNxx();
		// Sufficient statistics V2
		Map<State, Map<State, Integer>> ssV2 = cimNodes.get(1).getSufficientStatistics().getNxx();
		// Sufficient statistics V3
		Map<State, Map<State, Integer>> ssV3 = cimNodes.get(2).getSufficientStatistics().getNxx();

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
		assertEquals(0, (int) ssV1.get(fromState).get(toState));

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
		List<CIMNode> cimNodes = ctbnParameterLearning.getParameters();
		// Sufficient statistics V1
		Map<State, Integer> ssV1 = cimNodes.get(0).getSufficientStatistics().getNx();
		// Sufficient statistics V2
		Map<State, Integer> ssV2 = cimNodes.get(1).getSufficientStatistics().getNx();
		// Sufficient statistics V3
		Map<State, Integer> ssV3 = cimNodes.get(2).getSufficientStatistics().getNx();

		// Sufficient statistics V1
		State fromState = new State();
		fromState.addEvent("V1", "a");
		fromState.addEvent("V2", "b");
		fromState.addEvent("V3", "b");
		assertEquals(2, (int) ssV1.get(fromState));

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
	 * Check the sufficient statistic, for a certain CTBN and dataset, related to
	 * the time that a variable stay in a certain state.
	 */
	@Test
	public void testSufficientStatisticTCTBN() {
		List<CIMNode> cimNodes = ctbnParameterLearning.getParameters();
		// Test the sufficient parameters for certain combinations of states
		// Sufficient statistics V1
		Map<State, Double> ssV1 = cimNodes.get(0).getSufficientStatistics().getT();
		// Sufficient statistics V2
		Map<State, Double> ssV2 = cimNodes.get(1).getSufficientStatistics().getT();
		// Sufficient statistics V3
		Map<State, Double> ssV3 = cimNodes.get(2).getSufficientStatistics().getT();

		// Sufficient statistics V1
		State state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.5, (double) ssV1.get(state), 0.000001);

		state = new State();
		state.addEvent("V1", "c");
		state.addEvent("V2", "a");
		state.addEvent("V3", "c"); // Last observation of a sequence. It never changes, so the
														// duration is unknown.
		assertEquals(0, (double) ssV1.get(state), 0.000001);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(0.7, (double) ssV1.get(state), 0.000001);

		state = new State();
		state.addEvent("V1", "a");
		state.addEvent("V2", "b");
		state.addEvent("V3", "c"); // State that never occurs
		assertEquals(0, (double) ssV1.get(state), 0.000001);

		// Sufficient statistics V2
		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "a");
		assertEquals(0.5, (double) ssV2.get(state), 0.000001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "b");
		assertEquals(0.4, (double) ssV2.get(state), 0.000001);

		state = new State();
		state.addEvent("V2", "a");
		state.addEvent("V3", "c");
		assertEquals(0, (double) ssV2.get(state), 0.000001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "a");
		assertEquals(0, (double) ssV2.get(state), 0.000001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "b");
		assertEquals(1.2, (double) ssV2.get(state), 0.000001);

		state = new State();
		state.addEvent("V2", "b");
		state.addEvent("V3", "c");
		assertEquals(0, (double) ssV2.get(state), 0.000001);

		// Sufficient statistics V3
		state = new State();
		state.addEvent("V3", "a");
		assertEquals(0.5, (double) ssV3.get(state), 0.000001);

		state = new State();
		state.addEvent("V3", "b");
		assertEquals(1.6, (double) ssV3.get(state), 0.000001);

		state = new State();
		state.addEvent("V3", "c");
		assertEquals(0, (double) ssV3.get(state), 0.000001);

	}

}
