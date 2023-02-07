package es.upm.fi.cig.multictbnc.sampling;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SamplingSequencesTest {

	@Test
	public void testSampleDataset() throws IOException, UnreadDatasetException {
		// Generate three datasets
		String pathOneDataset = "src/test/resources/sampling/datasetsgeneratedbytest/onlyonedataset/";
		String pathTwoDatasets = "src/test/resources/sampling/datasetsgeneratedbytest/twodatasets/";
		String[] argsOneDataset = new String[]{"1", "20", "2", "4", "4", "3", "3", "0.3", "0.3", "0.3", "3", "true",
				"false", pathOneDataset};
		String[] argsTwoDatasets = new String[]{"2", "50", "5", "3", "5", "2", "2", "0.3", "0.3", "0.3", "3", "true",
				"false", pathTwoDatasets};
		MainSampling.main(argsOneDataset);
		MainSampling.main(argsTwoDatasets);

		// Read datasets
		String pathDataset0 = pathOneDataset;
		String pathDataset1 = pathTwoDatasets + "dataset0";
		String pathDataset2 = pathTwoDatasets + "dataset1";
		DatasetReader dR0 = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset0, 0);
		DatasetReader dR1 = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset1, 0);
		DatasetReader dR2 = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathDataset2, 0);
		dR0.setTimeAndClassVariables("t", List.of("C1", "C2", "C3"));
		dR1.setTimeAndClassVariables("t", List.of("C1", "C2"));
		dR2.setTimeAndClassVariables("t", List.of("C1", "C2"));
		Dataset dataset0 = dR0.readDataset();
		Dataset dataset1 = dR1.readDataset();
		Dataset dataset2 = dR2.readDataset();

		// Expected number of sampled sequences
		assertEquals(20, dataset0.getNumDataPoints());
		assertEquals(50, dataset1.getNumDataPoints());
		assertEquals(50, dataset2.getNumDataPoints());
		// Expected names of feature variables
		assertTrue(equals(List.of("X1", "X2", "X3", "X4"), dataset0.getNameFeatureVariables()));
		assertTrue(equals(List.of("X1", "X2", "X3"), dataset1.getNameFeatureVariables()));
		assertTrue(equals(List.of("X1", "X2", "X3"), dataset2.getNameFeatureVariables()));
		// Expected duration of the sampled sequences
		Sequence sequenceDataset0 = dataset0.getSequences().get(0);
		double lastTimeSequenceDataset0 = sequenceDataset0.getTimeValue(sequenceDataset0.getNumObservations() - 1);
		assertFalse(lastTimeSequenceDataset0 > 2);
		Sequence sequenceDataset1 = dataset1.getSequences().get(0);
		double lastTimeSequenceDataset1 = sequenceDataset1.getTimeValue(sequenceDataset1.getNumObservations() - 1);
		assertFalse(lastTimeSequenceDataset1 > 5);
		assertTrue(lastTimeSequenceDataset1 > 2);
		Sequence sequenceDataset2 = dataset2.getSequences().get(0);
		double lastTimeSequenceDataset2 = sequenceDataset2.getTimeValue(sequenceDataset2.getNumObservations() - 1);
		assertFalse(lastTimeSequenceDataset2 > 5);
		assertTrue(lastTimeSequenceDataset2 > 2);

		// Remove folder
		FileUtils.deleteDirectory(new File("src/test/resources/sampling/"));
	}

	private boolean equals(List<String> list1, List<String> list2) {
		return list1.size() == list2.size() && list1.containsAll(list2) && list2.containsAll(list1);
	}

}
