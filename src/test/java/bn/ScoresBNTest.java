package bn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.special.Gamma;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.bn.BNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.structure.optimization.BNScoreFunction;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNLogLikelihood;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

class ScoresBNTest {
	static Dataset dataset;
	static List<CPTNode> nodes;

	/**
	 * Defines a dataset and the structure of a BN. Instead of learning the
	 * structure of the BN, it is directly defined over CPT nodes. This is done to
	 * know which structure we are working on during the tests.
	 * 
	 */
	@BeforeAll
	public static void setUp() {
		// Define dataset
		List<String> nameClassVariables = List.of("C1", "C2", "C3");
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence1.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.1", "a", "a", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence2.add(new String[] { "0.0", "a", "a", "a" });
		dataSequence2.add(new String[] { "0.4", "a", "a", "a" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence3.add(new String[] { "0.2", "b", "a", "a" });
		dataSequence3.add(new String[] { "0.7", "b", "a", "a" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence4.add(new String[] { "0.0", "b", "a", "b" });
		dataSequence4.add(new String[] { "0.2", "b", "a", "b" });

		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence5.add(new String[] { "0.0", "b", "b", "a" });
		dataSequence5.add(new String[] { "0.2", "b", "b", "a" });

		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence6.add(new String[] { "0.0", "b", "b", "a" });
		dataSequence6.add(new String[] { "0.2", "b", "b", "a" });

		List<String[]> dataSequence7 = new ArrayList<String[]>();
		dataSequence7.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence7.add(new String[] { "0.0", "c", "b", "a" });
		dataSequence7.add(new String[] { "0.2", "c", "b", "a" });

		List<String[]> dataSequence8 = new ArrayList<String[]>();
		dataSequence8.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence8.add(new String[] { "0.0", "c", "b", "b" });
		dataSequence8.add(new String[] { "0.5", "c", "b", "b" });

		List<String[]> dataSequence9 = new ArrayList<String[]>();
		dataSequence9.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence9.add(new String[] { "0.0", "c", "b", "b" });
		dataSequence9.add(new String[] { "0.5", "c", "b", "b" });

		List<String[]> dataSequence10 = new ArrayList<String[]>();
		dataSequence10.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence10.add(new String[] { "0.0", "c", "b", "b" });
		dataSequence10.add(new String[] { "0.5", "c", "b", "b" });

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6);
		dataset.addSequence(dataSequence7);
		dataset.addSequence(dataSequence8);
		dataset.addSequence(dataSequence9);
		dataset.addSequence(dataSequence10);

		// Define nodes
		// As it is tested the learned parameters, that a node is a class variable or
		// not is irrelevant.
		nodes = new ArrayList<CPTNode>();
		for (String nameClassVariable : dataset.getNameVariables()) {
			nodes.add(new CPTNode(nameClassVariable, dataset.getPossibleStatesVariable(nameClassVariable)));
		}
	}

	@Test
	void testLogLikelihood() {
		// Test with structure C1 <- C2 <- C3 -> C1
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
		BN<CPTNode> bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		double ll = 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) + 1 * Math.log(1) + 2 * Math.log(2 / 3.0)
				+ 1 * Math.log(1 / 3.0) + 3 * Math.log(1) + 3 * Math.log(0.5) + 3 * Math.log(0.5)
				+ 1 * Math.log(1 / 4.0) + 3 * Math.log(3 / 4.0) + 6 * Math.log(6 / 10.0) + 4 * Math.log(4 / 10.0);
		// Log-likelihood
		BNScoreFunction scoreFunction = new BNLogLikelihood("No");
		assertEquals(ll, scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		assertEquals(ll - 0.5 * Math.log(10) * ((3 - 1) * 4 + (2 - 1) * 2 + (2 - 1)), scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		assertEquals(ll - ((3 - 1) * 4 + (2 - 1) * 2 + (2 - 1)), scoreFunction.compute(bn), 0.001);

		// Test with structure C1 -> C2 -> C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(1).setParent(nodes.get(0));
		nodes.get(2).setParent(nodes.get(1));
		bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		ll = 2 * Math.log(2 / 10.0) + 4 * Math.log(4 / 10.0) + 4 * Math.log(4 / 10.0) + 2 * Math.log(2 / 2.0)
				+ 2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0) + 4 * Math.log(4 / 4.0) + 3 * Math.log(3 / 4.0)
				+ 1 * Math.log(1 / 4.0) + 3 * Math.log(3 / 6.0) + 3 * Math.log(3 / 6.0);
		// Log-likelihood
		scoreFunction = new BNLogLikelihood("No");
		assertEquals(ll, scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		assertEquals(ll - 0.5 * Math.log(10) * ((3 - 1) + (2 - 1) * 3 + (2 - 1) * 2), scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		assertEquals(ll - ((3 - 1) + (2 - 1) * 3 + (2 - 1) * 2), scoreFunction.compute(bn), 0.001);

		// Test with structure C2 -> C1 <- C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));

		bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		ll = 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) + 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0)
				+ 3 * Math.log(3 / 3.0) + 4 * Math.log(4 / 10.0) + 6 * Math.log(6 / 10.0) + 6 * Math.log(6 / 10.0)
				+ 4 * Math.log(4 / 10.0);
		// Log-likelihood
		scoreFunction = new BNLogLikelihood("No");
		assertEquals(ll, scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		assertEquals(ll - 0.5 * Math.log(10) * ((3 - 1) * 4 + (2 - 1) + (2 - 1)), scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		assertEquals(ll - ((3 - 1) * 4 + (2 - 1) + (2 - 1)), scoreFunction.compute(bn), 0.001);
	}

	@Test
	void testBayesianScore() {
	}

	@AfterEach
	public void resetStructureAfterTest() {
		resetStructure(nodes);
	}

	private void resetStructure(List<CPTNode> nodes) {
		for (Node node : nodes)
			node.removeAllEdges();
	}

}
