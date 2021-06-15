package dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;

/**
 * Tests the definition of {@code Dataset} objects.
 * 
 * @author Carlos Villa Blanco
 *
 */
class GenerateDatasetTest {

	private static Dataset dataset;

	/**
	 * Defines a dataset by creating first the sequences.
	 * 
	 * @throws ErroneousSequenceException
	 */
	@BeforeAll
	public static void generateDatasetFromSequences() throws ErroneousSequenceException {
		List<String> nameVariables = List.of("Time", "V1", "V2", "V3");
		List<String> nameClassVariables = List.of("V2");

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.1", "b", "a", "a" });
		dataSequence1.add(new String[] { "0.2", "c", "a", "a" });
		dataSequence1.add(new String[] { "0.4", "c", "a", "a" });
		dataSequence1.add(new String[] { "0.6", "e", "a", "a" });

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
			indexVariables.put(nameVariable, nameVariables.indexOf(nameVariable));
		}

		List<Sequence> sequences = new ArrayList<Sequence>();
		sequences.add(sequence1);
		sequences.add(sequence2);

		dataset = new Dataset(sequences);
	}

	/**
	 * Defines a dataset with different order of variables for each sequence.
	 */
	@Test
	public void testStatesVariables() {
		assertEquals(8, dataset.getPossibleStatesVariable("V1").size());
		assertEquals(2, dataset.getPossibleStatesVariable("V2").size());
		assertEquals(1, dataset.getPossibleStatesVariable("V3").size());
	}

	@Test
	public void testTimeVariable() {
		assertEquals("Time", dataset.getNameTimeVariable());
	}

	@Test
	/**
	 * Defines a dataset by adding raw data sequentially.
	 */
	public void generateDatasetFromRawData() {
		List<String> nameVariables = List.of("Time", "V1", "V2", "V3", "V4");
		List<String> nameClassVariables = List.of("V1", "V3");
		String nameTimeVariable = "Time";
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence1.add(new String[] { "0.0", "a", "a", "b", "a" });
		dataSequence1.add(new String[] { "0.1", "a", "b", "b", "b" });
		dataSequence1.add(new String[] { "0.2", "a", null, "b", "c" });
		dataSequence1.add(new String[] { "0.4", "a", "a", "b", "d" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence2.add(new String[] { "0.0", "a", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.2", "a", "b", "a", "b" });
		dataSequence2.add(new String[] { "0.4", "a", "b", "a", "c" });

		dataset.addSequence(dataSequence1);
		assertEquals(1, dataset.getNumDataPoints());
		dataset.addSequence(dataSequence2);
		assertEquals(2, dataset.getNumDataPoints());
	}

	/**
	 * Tries to generate erroneous sequences.
	 */
	@Test
	public void generateDatasetWithErroneousSequences() {
		List<String> nameVariables = List.of("Time", "V1", "V2", "V3", "V4");
		List<String> nameClassVariables = List.of("V2", "V3");
		String nameTimeVariable = "Time";
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);

		// Empty sequence
		List<String[]> dataSequence1 = new ArrayList<String[]>();

		// Sequence with only names of variables
		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(nameVariables.toArray(new String[nameVariables.size()]));

		// Class variables with different values
		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence3.add(new String[] { "0.0", "a", "a", "a", "b" });
		dataSequence3.add(new String[] { "0.2", "e", "b", "c", "c" });
		dataSequence3.add(new String[] { "0.4", "f", "c", "a", "a" });

		// Sequence without time variable
		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "V1", "V2", "V3", "V4" });
		dataSequence4.add(new String[] { "a", "b", "a", "a" });
		dataSequence4.add(new String[] { "a", "b", "a", "b" });

		// Sequence without class variables
		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "V1", "V4" });
		dataSequence5.add(new String[] { "a", "b", "a" });
		dataSequence5.add(new String[] { "e", "c", "d" });

		// A correct sequence is added
		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence6.add(new String[] { "0.0", "a", "a", "c", "c", "b", "a" });
		dataSequence6.add(new String[] { "0.1", "b", "a", "c", "d", "b", "a" });

		// Sequence with less feature variables
		List<String[]> dataSequence7 = new ArrayList<String[]>();
		dataSequence7.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence7.add(new String[] { "0.0", "a", "b", "c" });
		dataSequence7.add(new String[] { "0.2", "e", "b", "c" });

		// Sequence with variables not seen before
		List<String[]> dataSequence8 = new ArrayList<String[]>();
		dataSequence8.add(new String[] { "Time", "V1", "V2", "V3", "V4", "V5", "V6" });
		dataSequence8.add(new String[] { "0.0", "a", "a", "c", "c", "b", "a" });
		dataSequence8.add(new String[] { "0.1", "b", "a", "c", "d", "b", "a" });

		// Another correct sequence is added
		List<String[]> dataSequence9 = new ArrayList<String[]>();
		dataSequence9.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence9.add(new String[] { "0.0", "a", "a", "c", "c", "b", "a" });
		dataSequence9.add(new String[] { "0.1", "b", "a", "c", "d", "b", "a" });

		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6); // Correct sequence
		dataset.addSequence(dataSequence7);
		dataset.addSequence(dataSequence8);
		dataset.addSequence(dataSequence9); // Correct sequence

		// Only one sequence should have been added
		assertEquals(2, dataset.getNumDataPoints());
	}

}
