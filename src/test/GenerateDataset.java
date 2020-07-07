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
		String[] nameVariables = { "V1", "V2", "V3" };
		String[] nameClassVariables = { "V2" };

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "a", "a", "a" });
		dataSequence1.add(new String[] { "b", "a", "a" });
		dataSequence1.add(new String[] { "c", "a", "a" });
		dataSequence1.add(new String[] { null, "a", "a" });
		dataSequence1.add(new String[] { "d", "a", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "a", "b", "a" });
		dataSequence2.add(new String[] { "e", "b", "a" });
		dataSequence2.add(new String[] { "f", "b", "a" });
		dataSequence2.add(new String[] { "g", "b", "a" });
		dataSequence2.add(new String[] { "h", "b", "a" });
		dataSequence2.add(new String[] { "i", "b", "a" });

		Sequence sequence1 = new Sequence(nameVariables, nameClassVariables, dataSequence1);
		Sequence sequence2 = new Sequence(nameVariables, nameClassVariables, dataSequence2);

		Map<String, Integer> indexVariables = new HashMap<String, Integer>();
		for (String nameVariable : nameVariables) {
			indexVariables.put(nameVariable, Arrays.asList(nameVariables).indexOf(nameVariable));
		}

		List<Sequence> sequences = new ArrayList<Sequence>();
		sequences.add(sequence1);
		sequences.add(sequence2);

		Dataset trainingDataset = new Dataset(indexVariables, sequences);

		assertEquals(10, trainingDataset.getStatesVariable("V1").length);
		assertEquals(2, trainingDataset.getStatesVariable("V2").length);
		assertEquals(1, trainingDataset.getStatesVariable("V3").length);
	}

}
