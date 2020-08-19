package mctbnc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterMLE;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.structure.HillClimbingBN;
import com.cig.mctbnc.learning.structure.HillClimbingCTBN;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
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
public class LearnMCTNBC {
	static Dataset dataset;
	static ParameterLearningAlgorithm bnParameterLearningAlgorithm;
	static StructureLearningAlgorithm bnStructureLearningAlgorithm;
	static ParameterLearningAlgorithm ctbnParameterLearningAlgorithm;
	static StructureLearningAlgorithm ctbnStructureLearningAlgorithm;

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
		// Definition of learning algorithms
		bnParameterLearningAlgorithm = new BNParameterMLE();
		bnStructureLearningAlgorithm = new HillClimbingBN();
		ctbnParameterLearningAlgorithm = new CTBNMaximumLikelihoodEstimation();
		ctbnStructureLearningAlgorithm = new HillClimbingCTBN();
	}

	/**
	 * Learn a multidimensional continuous time Bayesian network.
	 */
	@Test
	public void learnModel() {
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTNBC<CPTNode, CIMNode>(ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm,
				CPTNode.class, CIMNode.class);
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
