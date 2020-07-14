package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.com.cig.mctbnc.data.representation.Dataset;
import main.java.com.cig.mctbnc.data.representation.Sequence;

class GenerateDataset {

	private Dataset dataset;
	
    /**
     * Defines a dataset to use in the tests.
     */
	@BeforeEach
    public void setUp() {
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

		dataset = new Dataset(indexVariables, sequences);
    }

	
	@Test
	void testStatesVariables() {
		assertEquals(10, dataset.getStatesVariable("V1").size());
		assertEquals(2, dataset.getStatesVariable("V2").size());
		assertEquals(1, dataset.getStatesVariable("V3").size());
	}
	
	@Test
	void testTimeVariable() {
		assertEquals("Time", dataset.getNameTimeVariable());
	}
	
	@Test
	void testQuery() {
		
	}

}
