package dataset;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the definition of {@code Dataset} objects.
 *
 * @author Carlos Villa Blanco
 */
class GenerateDatasetTest {

	private static Dataset dataset;

	/**
	 * Defines a dataset by creating the sequences first.
	 *
	 * @throws ErroneousSequenceException thrown if a sequence was found to be erroneous
	 */
	@BeforeAll
	public static void generateDatasetFromSequences() throws ErroneousSequenceException {
		List<String> nameVariables = List.of("Time", "V1", "V2", "V3");
		List<String> nameClassVariables = List.of("V2");

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"0.0", "a", "a", "a"});
		dataSequence1.add(new String[]{"0.1", "b", "a", "a"});
		dataSequence1.add(new String[]{"0.2", "c", "a", "a"});
		dataSequence1.add(new String[]{"0.4", "c", "a", "a"});
		dataSequence1.add(new String[]{"0.6", "e", "a", "a"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"0.0", "a", "b", "a"});
		dataSequence2.add(new String[]{"0.2", "e", "b", "a"});
		dataSequence2.add(new String[]{"0.4", "f", "b", "a"});
		dataSequence2.add(new String[]{"0.5", "g", "b", "a"});
		dataSequence2.add(new String[]{"0.7", "h", "b", "a"});
		dataSequence2.add(new String[]{"1.0", "i", "b", "a"});

		Sequence sequence1 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence1);
		Sequence sequence2 = new Sequence(nameVariables, "Time", nameClassVariables, dataSequence2);

		Map<String, Integer> indexVariables = new HashMap<>();
		for (String nameVariable : nameVariables) {
			indexVariables.put(nameVariable, nameVariables.indexOf(nameVariable));
		}

		List<Sequence> sequences = new ArrayList<>();
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

	/**
	 * Defines a dataset by adding raw data sequentially.
	 */
	@Test
	public void generateDatasetFromRawData() {
		List<String> nameVariables = List.of("Time", "V1", "V2", "V3", "V4");
		List<String> nameClassVariables = List.of("V1", "V3");
		String nameTimeVariable = "Time";
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence1.add(new String[]{"0.0", "a", "a", "b", "a"});
		dataSequence1.add(new String[]{"0.1", "a", "b", "b", "b"});
		dataSequence1.add(new String[]{"0.2", "a", null, "b", "c"});
		dataSequence1.add(new String[]{"0.4", "a", "a", "b", "d"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence2.add(new String[]{"0.0", "a", "b", "a", "a"});
		dataSequence2.add(new String[]{"0.2", "a", "b", "a", "b"});
		dataSequence2.add(new String[]{"0.4", "a", "b", "a", "c"});

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
		List<String[]> dataSequence1 = new ArrayList<>();

		// Sequence with only names of variables
		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(nameVariables.toArray(new String[nameVariables.size()]));

		// Class variables with different values
		List<String[]> dataSequence3 = new ArrayList<>();
		dataSequence3.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence3.add(new String[]{"0.0", "a", "a", "a", "b"});
		dataSequence3.add(new String[]{"0.2", "e", "b", "c", "c"});
		dataSequence3.add(new String[]{"0.4", "f", "c", "a", "a"});

		// Sequence without a time variable
		List<String[]> dataSequence4 = new ArrayList<>();
		dataSequence4.add(new String[]{"V1", "V2", "V3", "V4"});
		dataSequence4.add(new String[]{"a", "b", "a", "a"});
		dataSequence4.add(new String[]{"a", "b", "a", "b"});

		// Sequence without class variables
		List<String[]> dataSequence5 = new ArrayList<>();
		dataSequence5.add(new String[]{"Time", "V1", "V4"});
		dataSequence5.add(new String[]{"a", "b", "a"});
		dataSequence5.add(new String[]{"e", "c", "d"});

		// A correct sequence is added
		List<String[]> dataSequence6 = new ArrayList<>();
		dataSequence6.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence6.add(new String[]{"0.0", "a", "a", "c", "c", "b", "a"});
		dataSequence6.add(new String[]{"0.1", "b", "a", "c", "d", "b", "a"});

		// Sequence with fewer feature variables
		List<String[]> dataSequence7 = new ArrayList<>();
		dataSequence7.add(new String[]{"Time", "V1", "V2", "V3"});
		dataSequence7.add(new String[]{"0.0", "a", "b", "c"});
		dataSequence7.add(new String[]{"0.2", "e", "b", "c"});

		// Sequence with variables not seen before
		List<String[]> dataSequence8 = new ArrayList<>();
		dataSequence8.add(new String[]{"Time", "V1", "V2", "V3", "V4", "V5", "V6"});
		dataSequence8.add(new String[]{"0.0", "a", "a", "c", "c", "b", "a"});
		dataSequence8.add(new String[]{"0.1", "b", "a", "c", "d", "b", "a"});

		// Another correct sequence is added
		List<String[]> dataSequence9 = new ArrayList<>();
		dataSequence9.add(nameVariables.toArray(new String[nameVariables.size()]));
		dataSequence9.add(new String[]{"0.0", "a", "a", "c", "c", "b", "a"});
		dataSequence9.add(new String[]{"0.1", "b", "a", "c", "d", "b", "a"});

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

	/**
	 * Reads a dataset from CSV files. Zero variance variables should be ignored when reading an entire dataset,
	 *
	 * @throws UnreadDatasetException thrown if the dataset could not be read
	 * @throws FileNotFoundException  thrown if the dataset files were not found
	 */
	@Test
	public void generateDatasetFromCSVFiles() throws FileNotFoundException, UnreadDatasetException {
		String nameTimeVariable = "t";
		List<String> nameClassVariables = List.of("C1");
		String pathDataset = "src/test/resources/dataset/csvfiles";
		DatasetReader dr = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset, 0);
		dr.setTimeAndClassVariables(nameTimeVariable, nameClassVariables);
		Dataset dataset = dr.readDataset();
		// The dataset contains three feature variables ("X1", "X2" and "X3"), but "X2"
		// has zero variance, and it should be ignored
		List<String> nameFeatureVariables = List.of("X1", "X3");
		assertEquals(nameTimeVariable, dataset.getNameTimeVariable());
		assertEquals(nameFeatureVariables, dataset.getNameFeatureVariables());
		assertEquals(nameClassVariables, dataset.getNameClassVariables());
		assertEquals(3, dataset.getPossibleStatesVariable("X1").size());
		assertTrue(dataset.getPossibleStatesVariable("X2").isEmpty());
		assertEquals(3, dataset.getPossibleStatesVariable("X3").size());
	}

}