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
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

/**
 * Tests to evaluate the learning of multidimensional continuous time Bayesian
 * networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class LearnMCTBNC {

	static Dataset dataset;
	static ParameterLearningAlgorithm bnParameterLearningAlgorithm;
	static StructureLearningAlgorithm bnStructureLearningAlgorithm;
	static ParameterLearningAlgorithm ctbnParameterLearningAlgorithm;
	static StructureLearningAlgorithm ctbnStructureLearningAlgorithm;

	/**
	 * 
	 * This method creates a dataset that will be used to generate MCTBNCs. The
	 * dataset is formed by five discrete variables, three features (F1, F2 and F3)
	 * and two class variables (C1 and C2). The states and relationships of the
	 * variable are as follows:
	 * 
	 * F1: it can take states 'a', 'b' and 'c', and its transitions depend on the
	 * state of C1. If C1='1', the transitions of F1 are always 'a'->'b'->'c'->'a',
	 * while if C1='2', the transitions of F1 are always 'a'->'c'->'b'->'a'.
	 * 
	 * F2: it can take states 'a', 'b' and 'c', and its transitions depend on the
	 * state of C2 and F3. If C2='a', the transitions of F2 are 'a'->'b'->'c'->'a',
	 * if C2='b', the transitions of F2 are 'a'->'c'->'b'->'a', if C2='c', the
	 * transitions of F2 are 'a'->'c'->'a', while if C2='d', the transitions of F2
	 * are 'b'->'c'->'b'. Those transitions are always met, however, some states of
	 * F2 would be changed to 'a' if the state of F3 was previously 'a' too.
	 * Therefore, it is possible that F2 do not transition in one iteration and
	 * stays with state 'a'.
	 * 
	 * F3: it can take states 'a', 'b', 'c' and 'd', and its transitions depend on
	 * the state of F1 and F2. If both features are equal, F3 will transition to the
	 * same state of their parents. Otherwise, F3 will transition to state 'd'.
	 * 
	 * C1: it can take states '1' and '2'. It does not depend on any variable.
	 * 
	 * C2: it can take states 'a', 'b', 'c' and 'd', depending on the state of C1.
	 * 
	 */
	@BeforeAll
	public static void setUp() {
		// Define dataset
		String nameTimeVariable = "Time";
		List<String> nameClassVariables = List.of("C1", "C2");

		// It is necessary to create a relatively big dataset to learn the MCTBNC
		// Number of observations in each sequence
		int numObservations = 1000;

		// Create data of sequence 1
		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "F1", "F2", "F3", "C1", "C2" });
		// Create a value for F1 for each observation.
		String[] valuesF1 = Arrays.copyOfRange(
				String.join(",", Collections.nCopies(numObservations, "a,b,c")).split(","), 0, numObservations);
		// Create a value for F2 for each observation.
		String[] valuesF2 = Arrays.copyOfRange(
				String.join(",", Collections.nCopies(numObservations, "b,c,a")).split(","), 0, numObservations);
		// Create a value for F3 for each observation.
		String[] valuesF3 = Arrays.copyOfRange(
				String.join(",", Collections.nCopies(numObservations, "d,d,d")).split(","), 0, numObservations);
		for (int i = 0; i < numObservations; i++)
			dataSequence1
					.add(new String[] { String.valueOf(i * 0.1), valuesF1[i], valuesF2[i], valuesF3[i], "1", "a" });

		// Create data of sequence 2
		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "F1", "F2", "F3", "C1", "C2" });
		valuesF1 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "a,b,c")).split(","), 0,
				numObservations);
		valuesF2 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "c,b,a")).split(","), 0,
				numObservations);
		// Create a value for F3 for each observation.
		valuesF3 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "d,d,b")).split(","), 0,
				numObservations);
		for (int i = 0; i < numObservations; i++)
			dataSequence2
					.add(new String[] { String.valueOf(i * 0.1), valuesF1[i], valuesF2[i], valuesF3[i], "1", "b" });

		// Create data of sequence 3
		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "F1", "F2", "F3", "C1", "C2" });
		valuesF1 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "c,a,b")).split(","), 0,
				numObservations);
		valuesF2 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "c,b,a")).split(","), 0,
				numObservations);
		// Create a value for F3 for each observation.
		valuesF3 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "d,c,d")).split(","), 0,
				numObservations);
		for (int i = 0; i < numObservations; i++)
			dataSequence3
					.add(new String[] { String.valueOf(i * 0.1), valuesF1[i], valuesF2[i], valuesF3[i], "1", "b" });

		// Create data of sequence 4
		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "F1", "F2", "F3", "C1", "C2" });
		valuesF1 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "c,b,a")).split(","), 0,
				numObservations);
		valuesF2 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "a,c")).split(","), 0,
				numObservations);
		for (int i = 0; i < numObservations; i++) {
			// Definition values F3
			if (i == 0)
				valuesF3[i] = "d";
			else if (valuesF1[i].equals(valuesF2[i]) && i + 1 < numObservations)
				valuesF3[i + 1] = valuesF1[i];
			else if (i + 1 < numObservations)
				valuesF3[i + 1] = "d";
		}

		// Create data of sequence 5
		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "F1", "F2", "F3", "C1", "C2" });
		valuesF1 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "c,b,a")).split(","), 0,
				numObservations);
		valuesF2 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "b,c")).split(","), 0,
				numObservations);
		for (int i = 0; i < numObservations; i++) {
			// Definition values F3
			if (i == 0)
				valuesF3[i] = "d";
			if (valuesF1[i].equals(valuesF2[i]) && i + 1 < numObservations)
				valuesF3[i + 1] = valuesF1[i];
			else if (i + 1 < numObservations)
				valuesF3[i + 1] = "d";
			dataSequence5
					.add(new String[] { String.valueOf(i * 0.1), valuesF1[i], valuesF2[i], valuesF3[i], "2", "d" });
		}

		// Create data of sequence 6
		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "Time", "F1", "F2", "F3", "C1", "C2" });
		// Create a value for F1 for each observation.
		valuesF1 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "a,b,c")).split(","), 0,
				numObservations);
		// Create a value for F2 for each observation.
		valuesF2 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "a,b,c")).split(","), 0,
				numObservations);
		// Create a value for F3 for each observation.
		valuesF3 = Arrays.copyOfRange(String.join(",", Collections.nCopies(numObservations, "c,a,b")).split(","), 0,
				numObservations);
		for (int i = 0; i < numObservations; i++) {
			// Definition values F3
			if (valuesF1[i].equals(valuesF2[i]) && i + 1 < numObservations)
				valuesF3[i + 1] = valuesF1[i];
			else if (i + 1 < numObservations)
				valuesF3[i + 1] = "d";
			// If F3 has state 'a' in the current observation, the next state of F2 would be
			// 'a' too, but the pattern given by C2 to F2 would not be affected, i.e., F2
			// will always follow the pattern a->b->c->a, but those observations where F3 is
			// "a" will make F2 to have value "a" in the following observation.
			if (valuesF3[i].equals("a") && i + 1 < numObservations)
				valuesF2[i + 1] = "a";

			dataSequence6
					.add(new String[] { String.valueOf(i * 0.1), valuesF1[i], valuesF2[i], valuesF3[i], "1", "a" });
		}

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);

		dataset.addSequence(dataSequence6);

		bnParameterLearningAlgorithm = new BNParameterMLE();
		bnStructureLearningAlgorithm = new HillClimbingBN();
		ctbnParameterLearningAlgorithm = new CTBNMaximumLikelihoodEstimation();
		ctbnStructureLearningAlgorithm = new HillClimbingCTBN();
	}

	/**
	 * Learn a multidimensional continuous time Bayesian network.
	 */
	@Test
	public void learnMultidimensionalContinuousTimeBayesianNetwork() {
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTBNC<CPTNode, CIMNode>(dataset, ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm,
				CPTNode.class, CIMNode.class);
	
		mctbnc.setPenalizationFunction("BIC");
		
		mctbnc.learn();

		boolean[][] expectedAdjacencyMatrix = new boolean[][] { { false, false, true, false, false },
				{ false, false, true, false, false }, { false, true, false, false, false },
				{ true, false, false, false, true }, { false, true, false, false, false } };

		assertArrayEquals(expectedAdjacencyMatrix, mctbnc.getAdjacencyMatrix());

		// Show results (don't forget breakpoint)
		// mctbnc.display();
		// System.out.println();
	}
}
