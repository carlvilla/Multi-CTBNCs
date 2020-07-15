package dataset;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;

public class GenerateDataset {

	private Dataset dataset;

	/**
	 * Defines a dataset by creating first the sequences.
	 */
	@Before
	public void generateDatasetFromSequences() {
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

		Sequence sequence1 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence1);
		Sequence sequence2 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence2);

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
	/**
	 * Defines a dataset by adding raw data sequentially.
	 */
	public void generateDatasetFromRawData() {
		String[] nameVariables = { "Time", "V1", "V2", "V3", "V4" };
		String[] nameClassVariables = { "V1", "V3" };
		String nameTimeVariable = "Time";
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(nameVariables);
		dataSequence1.add(new String[] { "0.0", "a", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.1", "b", "a", "a", "b" });
		dataSequence1.add(new String[] { "0.2", "c", "a", "a", "c" });
		dataSequence1.add(new String[] { "0.4", null, "a", "a", "d" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(nameVariables);
		dataSequence2.add(new String[] { "0.0", "a", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.2", "e", "b", "a", "b" });
		dataSequence2.add(new String[] { "0.4", "f", "b", "a", "c" });

		// Sequence without time variable
		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "V1", "V2", "V3", "V4" });
		dataSequence2.add(new String[] { "0.0", "a", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.2", "e", "b", "a", "b" });

		// Sequence without class variables
		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "V2", "V4" });
		dataSequence4.add(new String[] { "a", "b", "a" });
		dataSequence4.add(new String[] { "e", "b", "a" });

		// Sequence with less features
		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence5.add(new String[] { "0.0", "a", "b" });
		dataSequence5.add(new String[] { "0.2", "e", "b" });

		// Sequence with variables not seen before
		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "V4", "V5" });
		dataSequence6.add(new String[] { "b", "a" });
		dataSequence6.add(new String[] { "b", "a" });
		
		// Empty sequence
		List<String[]> dataSequence7 = new ArrayList<String[]>();

		dataset.addSequence(dataSequence1);
		assertEquals(1, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence2);
		assertEquals(2, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence3);
		assertEquals(2, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence4);
		assertEquals(2, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence5);
		assertEquals(2, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence6);
		assertEquals(2, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence7);
		assertEquals(2, dataset.getNumDataPoints());
	}

	/**
	 * Defines a dataset with different order of variables for each sequence.
	 */

	@Test
	public void testStatesVariables() {
		assertEquals(10, dataset.getStatesVariable("V1").size());
		assertEquals(2, dataset.getStatesVariable("V2").size());
		assertEquals(1, dataset.getStatesVariable("V3").size());
	}

	@Test
	public void testTimeVariable() {
		assertEquals("Time", dataset.getNameTimeVariable());
	}

	@Test
	public void testQuery() {

	}

}
