package ctbn;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Event;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.CTBNParameterMLE;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

public class ParameterLearning {

	Dataset dataset;

	/**
	 * Defines a dataset by creating first the sequences.
	 */
	@Before
	public void generateDatasetFromSequences() {
		String[] nameVariables = { "Time", "V1", "V2", "V3" };
		String[] nameClassVariables = {};
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence1.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence1.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence1.add(new String[] { "0.0", "a", "b", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence2.add(new String[] { "0.0", "a", "a", "b" });
		dataSequence2.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence2.add(new String[] { "0.0", "b", "b", "b" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence3.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence3.add(new String[] { "0.0", "c", "a", "c" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence4.add(new String[] { "0.0", "b", "b", "b" });

		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence5.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence5.add(new String[] { "0.0", "b", "b", "b" });
		dataSequence5.add(new String[] { "0.0", "b", "b", "a" });

		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence6.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence6.add(new String[] { "0.0", "a", "a", "a" });

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6);
	}

	/**
	 * Check the sufficient statistic, for a certain CTBN and dataset, related to
	 * the number of transitions of a variable.
	 */
	@Test
	public void testSufficientStatisticTransitionsCTBN() {
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

		// Learn sufficient parameters
		CTBNParameterMLE ctbnParameterLearning = new CTBNParameterMLE();
		ctbnParameterLearning.learn(nodes, dataset);
		List<CIMNode> cimNodes = ctbnParameterLearning.getParameters();

		// Test the sufficient parameters for certain combinations of states
		// Sufficient statistics V1
		Map<State, Map<State, Integer>> ssV1 = cimNodes.get(0).getSufficientStatistics();
		// Sufficient statistics V2
		Map<State, Map<State, Integer>> ssV2 = cimNodes.get(1).getSufficientStatistics();
		// Sufficient statistics V3
		Map<State, Map<State, Integer>> ssV3 = cimNodes.get(2).getSufficientStatistics();

		// Sufficient statistics for V1 changing its state
		State fromState = new State();
		fromState.addEvent(new Event<String>("V1", "a"));
		fromState.addEvent(new Event<String>("V2", "b"));
		fromState.addEvent(new Event<String>("V3", "b"));
		State toState = new State();
		toState.addEvent(new Event<String>("V1", "a"));
		assertEquals(3, (int) ssV1.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V1", "b"));
		assertEquals(2, (int) ssV1.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V1", "c"));
		assertEquals(0, (int) ssV1.get(fromState).get(toState));

		fromState = new State();
		fromState.addEvent(new Event<String>("V1", "a"));
		fromState.addEvent(new Event<String>("V2", "a"));
		fromState.addEvent(new Event<String>("V3", "a"));
		toState = new State();
		toState.addEvent(new Event<String>("V1", "a"));
		assertEquals(0, (int) ssV1.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V1", "b"));
		assertEquals(0, (int) ssV1.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V1", "c"));
		assertEquals(1, (int) ssV1.get(fromState).get(toState));

		// Sufficient statistics for V2 changing its state
		fromState = new State();
		fromState.addEvent(new Event<String>("V2", "b"));
		fromState.addEvent(new Event<String>("V3", "b"));
		toState = new State();
		toState.addEvent(new Event<String>("V2", "a"));
		assertEquals(1, (int) ssV2.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V2", "b"));
		assertEquals(5, (int) ssV2.get(fromState).get(toState));

		// Sufficient statistics for V3 changing its state
		fromState = new State();
		fromState.addEvent(new Event<String>("V3", "a"));
		toState = new State();
		toState.addEvent(new Event<String>("V3", "a"));
		assertEquals(0, (int) ssV3.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V3", "b"));
		assertEquals(0, (int) ssV3.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V3", "c"));
		assertEquals(1, (int) ssV3.get(fromState).get(toState));

		fromState = new State();
		fromState.addEvent(new Event<String>("V3", "b"));
		toState = new State();
		toState.addEvent(new Event<String>("V3", "a"));
		assertEquals(3, (int) ssV3.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V3", "b"));
		assertEquals(4, (int) ssV3.get(fromState).get(toState));
		toState = new State();
		toState.addEvent(new Event<String>("V3", "c"));
		assertEquals(0, (int) ssV3.get(fromState).get(toState));
	}
	
	/**
	 * Check the sufficient statistic, for a certain CTBN and dataset, related to
	 * the time that a variable stay in a certain state.
	 */
	@Test
	public void testSufficientStatisticTimeCTBN() {
		assertEquals(false, true);
	}

}
