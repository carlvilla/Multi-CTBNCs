package bn;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.models.BN;

public class LearnBayesianNetwork {

	/**
	 * Defines a dataset to use in the tests.
	 */
	@Before
	public void setUp() {
		String[] nameVariables = { "t", "V1", "V2", "V3" };
		String[] nameClassVariables = { "V1", "V2", "V3" };

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "0.0", "a", "a", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "0.0", "a", "b", "b" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "0.0", "b", "b", "b" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "0.0", "b", "a", "b" });

		Sequence sequence1 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence1);
		Sequence sequence2 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence2);
		Sequence sequence3 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence3);
		Sequence sequence4 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence4);

		Map<String, Integer> indexVariables = new HashMap<String, Integer>();
		for (String nameVariable : nameVariables) {
			indexVariables.put(nameVariable, Arrays.asList(nameVariables).indexOf(nameVariable));
		}

		List<Sequence> sequences = new ArrayList<Sequence>();
		sequences.add(sequence1);
		sequences.add(sequence2);

		/*
		dataset = new Dataset(indexVariables, sequences);

		// Class subgraph is defined with a Bayesian network
		BN bn = new BN<DiscreteNode>(classVariables, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm,
				trainingDataset);
		*/
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
