package bn;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.mctbnc.models.BN;
import es.upm.fi.cig.mctbnc.nodes.CPTNode;

/**
 * Tests the learning of the structure of a BN.
 * 
 * @author Carlos Villa Blanco
 *
 */
class LearnStructureBNTest {

	/**
	 * Learns a Bayesian network. The dataset used is formed by binary variables
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
		dataSequence1.add(new String[] { "0.1", "a", "b", "b" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence2.add(new String[] { "0.0", "b", "a", "b" });
		dataSequence2.add(new String[] { "0.1", "b", "a", "b" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence3.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence3.add(new String[] { "0.1", "a", "a", "a" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "V1", "V2", "V3" });
		dataSequence4.add(new String[] { "0.0", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.1", "b", "b", "b" });

		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);

		// Class subgraph is defined with a Bayesian network
		// Algorithm to learn parameters
		BNParameterLearningAlgorithm plAlg = BNParameterLearningAlgorithmFactory
				.getAlgorithm("Maximum likelihood estimation", 0.0);
		// Algorithm to learn structure
		StructureLearningAlgorithm slAlg = StructureLearningAlgorithmFactory.getAlgorithmBN("Hill climbing",
				"Log-likelihood", "No", 0);

		// Define object containing the learning algorithms
		BNLearningAlgorithms learningAlgs = new BNLearningAlgorithms(plAlg, slAlg);
		// Structure constraints
		StructureConstraints strucConst = new DAG();
		// Create the Bayesian network
		BN<CPTNode> bn = new BN<CPTNode>(dataset, nameClassVariables, learningAlgs, strucConst, CPTNode.class);
		// Learn the parameters and structure
		bn.learn();
		boolean[][] expectedAdjacencyMatrix = new boolean[][] { { false, false, true }, { false, false, true },
				{ false, false, false } };
		assertArrayEquals(expectedAdjacencyMatrix, bn.getAdjacencyMatrix());
	}
}
