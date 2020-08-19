package bn;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterMLE;
import com.cig.mctbnc.learning.structure.HillClimbingBN;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.DAG;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;

public class LearnBN {

	/**
	 * Learn a Bayesian network. The dataset used is formed by binary variables
	 * V1,V2 and V3 (plus Time) such that V3 has value "a" iff V1 and V2 have both
	 * value "a". Otherwise, it is value is "b" (AND gate). It is expected
	 * dependencies from V1 and V2 to V3.
	 */
	@Test
	public void learnBayesianNetwork() {
		List<String> nameClassVariables = List.of("V1", "V2", "V3");
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence1.add(new String[] { "0.0", "a", "b", "b" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence2.add(new String[] { "0.0", "b", "a", "b" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence3.add(new String[] { "0.0", "a", "a", "a" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence4.add(new String[] { "0.0", "b", "b", "b" });

		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);

		// Class subgraph is defined with a Bayesian network
		// Algorithm to learn parameters
		ParameterLearningAlgorithm parameterLearningAlgorithm = new BNParameterMLE();
		// Algorithm to learn structure
		StructureLearningAlgorithm structureLearningAlgorithm = new HillClimbingBN();
		// Structure constraints
		StructureConstraints structureConstraintsBN = new DAG();

		BN<CPTNode> bn = new BN<CPTNode>(nameClassVariables, parameterLearningAlgorithm, structureLearningAlgorithm,
				structureConstraintsBN, CPTNode.class);
		bn.learn(dataset);
		boolean[][] expectedAdjacencyMatrix = new boolean[][] { { false, false, true }, { false, false, true },
				{ false, false, false } };
		assertArrayEquals(expectedAdjacencyMatrix, bn.getAdjacencyMatrix());
	}
}
