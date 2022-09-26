package bn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests the learning of the structure of a BN.
 *
 * @author Carlos Villa Blanco
 */
class LearnStructureBNTest {

	/**
	 * Learns a Bayesian network. The dataset used is formed by binary variables V1, V2 and V3 (plus Time) such that V3
	 * has value "a" iff V1 and V2 have both value "a". Otherwise, its value is "b" (AND gate). It is expected
	 * dependencies from V1 and V2 to V3.
	 */
	@Test
	public void learnBayesianNetwork() throws ErroneousValueException {
		List<String> nameClassVariables = List.of("V1", "V2", "V3");
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"Time", "V1", "V2", "V3"});
		dataSequence1.add(new String[]{"0.0", "a", "b", "b"});
		dataSequence1.add(new String[]{"0.1", "a", "b", "b"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"Time", "V1", "V2", "V3"});
		dataSequence2.add(new String[]{"0.0", "b", "a", "b"});
		dataSequence2.add(new String[]{"0.1", "b", "a", "b"});

		List<String[]> dataSequence3 = new ArrayList<>();
		dataSequence3.add(new String[]{"Time", "V1", "V2", "V3"});
		dataSequence3.add(new String[]{"0.0", "a", "a", "a"});
		dataSequence3.add(new String[]{"0.1", "a", "a", "a"});

		List<String[]> dataSequence4 = new ArrayList<>();
		dataSequence4.add(new String[]{"Time", "V1", "V2", "V3"});
		dataSequence4.add(new String[]{"0.0", "b", "b", "b"});
		dataSequence4.add(new String[]{"0.1", "b", "b", "b"});

		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);

		// Class subgraph is defined with a Bayesian network
		// Algorithm to learn parameters
		BNParameterLearningAlgorithm plAlg = BNParameterLearningAlgorithmFactory.getAlgorithm(
				"Maximum likelihood estimation", 0.0);
		// Algorithm to learn the structure
		Map<String, String> paramSLA = Map.of("scoreFunction", "Log-likelihood", "penalisationFunction", "No");
		StructureLearningAlgorithm slAlg = StructureLearningAlgorithmFactory.getAlgorithmBN("Hill climbing", paramSLA);

		// Define the object containing the learning algorithms
		BNLearningAlgorithms learningAlgs = new BNLearningAlgorithms(plAlg, slAlg);
		// Structure constraints
		StructureConstraints strucConst = new DAG();
		// Create the Bayesian network
		BN<CPTNode> bn = new BN<>(dataset, nameClassVariables, learningAlgs, strucConst, CPTNode.class);
		// Learn the parameters and structure
		bn.learn();
		boolean[][] expectedAdjacencyMatrix = new boolean[][]{{false, false, true}, {false, false, true},
				{false, false, false}};
		assertArrayEquals(expectedAdjacencyMatrix, bn.getAdjacencyMatrix());
	}
}