package com.cig.mctbnc.data.writer;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;

public class MultipleCSVWriter {

	public static void write(Dataset dataset, String destinationPath) {
		List<Sequence> sequences = dataset.getSequences();
		for (int i = 0; i < sequences.size(); i++) {
			String nameFile = "Sequence" + i;

		}

	}

}
