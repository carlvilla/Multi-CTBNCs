package mctbnc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithmFactory;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.models.submodels.MCTNBC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

/**
 * Tests to evaluate the learning of multidimensional continuous time naive
 * Bayes classifiers.
 * 
 * @author Carlos Villa Blanco
 *
 */
class LearnMCTNBCTest {
	static Dataset dataset;
	static BNLearningAlgorithms bnLearningAlgs;
	static CTBNLearningAlgorithms ctbnLearningAlgs;

	@BeforeAll
	public static void setUp() {
		// Define dataset
		String nameTimeVariable = "Time";
		List<String> nameClassVariables = List.of("C1", "C2", "C3");
		// The data of the dataset is irrelevant, only necessary to run the software
		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "F1", "F2", "F3", "F4", "C1", "C2", "C3" });
		dataSequence1.add(new String[] { "0.0", "a", "b", "c", "d", "1", "1", "1" });
		dataSequence1.add(new String[] { "0.5", "b", "c", "d", "e", "1", "1", "1" });
		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "F1", "F2", "F3", "F4", "C1", "C2", "C3" });
		dataSequence2.add(new String[] { "0.0", "a", "b", "c", "d", "2", "2", "2" });
		dataSequence2.add(new String[] { "0.5", "b", "c", "d", "e", "2", "2", "2" });
		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		// Algorithms to learn BN (class subgraph of MCTBNC)
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = BNParameterLearningAlgorithmFactory
				.getAlgorithm("Maximum likelihood estimation", 0.0);
		StructureLearningAlgorithm bnStructureLearningAlgorithm = StructureLearningAlgorithmFactory
				.getAlgorithmBN("Hill climbing", "Log-likelihood", "No", 0);
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of MCTBNC)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm = CTBNParameterLearningAlgorithmFactory
				.getAlgorithm("Maximum likelihood estimation", 0.0, 0.0);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = StructureLearningAlgorithmFactory
				.getAlgorithmCTBN("Hill climbing", "Log-likelihood", "No", 0);
		ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm);
	}

	/**
	 * Learn a multidimensional continuous time Bayesian network.
	 */
	@Test
	public void learnModel() {
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTNBC<CPTNode, CIMNode>(bnLearningAlgs, ctbnLearningAlgs, CPTNode.class,
				CIMNode.class);
		mctbnc.learn(dataset);
		boolean[][] expectedAdjacencyMatrix = new boolean[][] { { false, false, false, false, false, false, false },
				{ false, false, false, false, false, false, false },
				{ false, false, false, false, false, false, false },
				{ false, false, false, false, false, false, false }, { true, true, true, true, false, false, false },
				{ true, true, true, true, false, false, false }, { true, true, true, true, false, false, false } };
		assertArrayEquals(expectedAdjacencyMatrix, mctbnc.getAdjacencyMatrix());
	}

}
