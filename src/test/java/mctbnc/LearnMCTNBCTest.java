package mctbnc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.BNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbing;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.models.MCTNBC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

/**
 * Tests to evaluate the learning of multidimensional continuous time naive
 * Bayes classifiers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class LearnMCTNBCTest {
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
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = new BNMaximumLikelihoodEstimation();
		BNStructureLearningAlgorithm bnStructureLearningAlgorithm = new BNHillClimbing("Log-likelihood");
		bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Algorithms to learn CTBN (feature and bridge subgraph of MCTBNC)
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm = new CTBNMaximumLikelihoodEstimation();
		CTBNStructureLearningAlgorithm ctbnStructureLearningAlgorithm = new CTBNHillClimbing("Log-likelihood");
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

		// Show results
		// mctbnc.display();
		// System.out.println(); // (don't forget breakpoint)
	}

}
