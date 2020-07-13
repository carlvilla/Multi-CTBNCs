package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import main.java.com.cig.mctbnc.learning.structure.BNStructureHillClimbing;
import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.Sequence;

class GenerateDataset {

	@Test
	void getStatesVariables() {
		String[] nameVariables = { "Time", "V1", "V2", "V3" };
		String[] nameClassVariables = { "V2" };

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.1", "b", "a", "a" });
		dataSequence1.add(new String[] { "0.2", "c", "a", "a" });
		dataSequence1.add(new String[] { "0.4", null, "a", "a" });
		dataSequence1.add(new String[] { "0.6", "d", "a", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "0.0", "a", "b", "a" });
		dataSequence2.add(new String[] { "0.2", "e", "b", "a" });
		dataSequence2.add(new String[] { "0.4", "f", "b", "a" });
		dataSequence2.add(new String[] { "0.5", "g", "b", "a" });
		dataSequence2.add(new String[] { "0.7", "h", "b", "a" });
		dataSequence2.add(new String[] { "1.0", "i", "b", "a" });

		Sequence sequence1 = new Sequence("Time", nameVariables, nameClassVariables, dataSequence1);
		Sequence sequence2 = new Sequence("Time", nameVariables, nameClassVariables, dataSequence2);

		Map<String, Integer> indexVariables = new HashMap<String, Integer>();
		for (String nameVariable : nameVariables) {
			indexVariables.put(nameVariable, Arrays.asList(nameVariables).indexOf(nameVariable));
		}

		List<Sequence> sequences = new ArrayList<Sequence>();
		sequences.add(sequence1);
		sequences.add(sequence2);

		Dataset trainingDataset = new Dataset(indexVariables, sequences);

		assertEquals(10, trainingDataset.getStatesVariable("V1").size());
		assertEquals(2, trainingDataset.getStatesVariable("V2").size());
		assertEquals(1, trainingDataset.getStatesVariable("V3").size());
	}

}
