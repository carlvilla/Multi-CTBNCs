package bn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.special.Gamma;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.bn.BNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNScoreFunction;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Tests over the score functions for BNs. Some structures are built and their
 * scores manually estimated in order to verify the correct behavior of the
 * score functions.
 * 
 * @author Carlos Villa Blanco
 *
 */
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
		// Log-likelihood
		BNScoreFunction scoreFunction = new BNLogLikelihood("No");
		double llS1Expected = 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) + 1 * Math.log(1) + 2 * Math.log(2 / 3.0)
				+ 1 * Math.log(1 / 3.0) + 3 * Math.log(1) + 3 * Math.log(0.5) + 3 * Math.log(0.5)
				+ 1 * Math.log(1 / 4.0) + 3 * Math.log(3 / 4.0) + 6 * Math.log(6 / 10.0) + 4 * Math.log(4 / 10.0);
		double llS1Actual = scoreFunction.compute(bn);
		assertEquals(llS1Expected, llS1Actual, 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		double llS1BICExpected = llS1Expected - 0.5 * Math.log(10) * ((3 - 1) * 4 + (2 - 1) * 2 + (2 - 1));
		double llS1BICActual = scoreFunction.compute(bn);
		assertEquals(llS1BICExpected, llS1BICActual, 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		double llS1AICExpected = llS1Expected - ((3 - 1) * 4 + (2 - 1) * 2 + (2 - 1));
		double llS1AICActual = scoreFunction.compute(bn);
		assertEquals(llS1AICExpected, llS1AICActual, 0.001);

		// Test with structure C1 -> C2 -> C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(1).setParent(nodes.get(0));
		nodes.get(2).setParent(nodes.get(1));
		bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		// Log-likelihood
		scoreFunction = new BNLogLikelihood("No");
		double llS2Expected = 2 * Math.log(2 / 10.0) + 4 * Math.log(4 / 10.0) + 4 * Math.log(4 / 10.0)
				+ 2 * Math.log(2 / 2.0) + 2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0) + 4 * Math.log(4 / 4.0)
				+ 3 * Math.log(3 / 4.0) + 1 * Math.log(1 / 4.0) + 3 * Math.log(3 / 6.0) + 3 * Math.log(3 / 6.0);
		double llS2Actual = scoreFunction.compute(bn);
		assertEquals(llS2Expected, llS2Actual, 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		double llS2BICExpected = llS2Expected - 0.5 * Math.log(10) * ((3 - 1) + (2 - 1) * 3 + (2 - 1) * 2);
		double llS2BICActual = scoreFunction.compute(bn);
		assertEquals(llS2BICExpected, llS2BICActual, 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		double llS2AICExpected = llS2Expected - ((3 - 1) + (2 - 1) * 3 + (2 - 1) * 2);
		double llS2AICActual = scoreFunction.compute(bn);
		assertEquals(llS2AICExpected, llS2AICActual, 0.001);

		// Test with structure C2 -> C1 <- C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));

		bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		// Log-likelihood
		scoreFunction = new BNLogLikelihood("No");
		double llS3Expected = 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) + 2 * Math.log(2 / 3.0)
				+ 1 * Math.log(1 / 3.0) + 3 * Math.log(3 / 3.0) + 4 * Math.log(4 / 10.0) + 6 * Math.log(6 / 10.0)
				+ 6 * Math.log(6 / 10.0) + 4 * Math.log(4 / 10.0);
		double llS3Actual = scoreFunction.compute(bn);
		assertEquals(llS3Expected, llS3Actual, 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		double llS3BICExpected = llS3Expected - 0.5 * Math.log(10) * ((3 - 1) * 4 + (2 - 1) + (2 - 1));
		double llS3BICActual = scoreFunction.compute(bn);
		assertEquals(llS3BICExpected, scoreFunction.compute(bn), 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		double llS3AICExpected = llS3Expected - ((3 - 1) * 4 + (2 - 1) + (2 - 1));
		double llS3AICActual = scoreFunction.compute(bn);
		assertEquals(llS3AICExpected, llS3AICActual, 0.001);

		// Compare scores between structures.
		// Although the dataset was extracted from the third structure (with some
		// noise), the more dense first structure has a higher log-likelihood. If a BIC
		// penalization is applied, the first structure would have a better score
		assertTrue(llS1Actual > llS3Actual);
		assertTrue(llS1BICActual < llS3BICActual);
		assertTrue(llS1AICActual < llS3AICActual);
	}

	@Test
	void testBayesianScore() {
		double nxHP = 2.0;

		// Test with structure C1 <- C2 <- C3 -> C1
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
		BN<CPTNode> bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNBayesianEstimation(nxHP));
		bn.learnParameters();
		BNScoreFunction scoreFunction = new BNBayesianScore();
		double bdeS1Expected = Gamma.logGamma(nxHP * 3) - Gamma.logGamma(nxHP * 3 + 2 + 1) + Gamma.logGamma(nxHP + 2)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 3)
				- Gamma.logGamma(nxHP * 3 + 1) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP)
				+ Gamma.logGamma(nxHP * 3) - Gamma.logGamma(nxHP * 3 + 2 + 1) + Gamma.logGamma(nxHP + 2)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 3)
				- Gamma.logGamma(nxHP * 3 + 3) + Gamma.logGamma(nxHP + 3) - Gamma.logGamma(nxHP)

				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(6 + nxHP * 2) + Gamma.logGamma(3 + nxHP)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(3 + nxHP) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 2)
				- Gamma.logGamma(4 + nxHP * 2) + Gamma.logGamma(1 + nxHP) - Gamma.logGamma(nxHP)
				+ Gamma.logGamma(3 + nxHP) - Gamma.logGamma(nxHP)

				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 6 + 4) + Gamma.logGamma(nxHP + 6)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(4 + nxHP) - Gamma.logGamma(nxHP);
		double bdeS1Actual = scoreFunction.compute(bn);
		assertEquals(bdeS1Expected, bdeS1Actual, 0.001);

		// Test with structure C1 -> C2 -> C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(1).setParent(nodes.get(0));
		nodes.get(2).setParent(nodes.get(1));
		bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNBayesianEstimation(nxHP));
		bn.learnParameters();
		scoreFunction = new BNBayesianScore();
		double bdeS2Expected = Gamma.logGamma(nxHP * 3) - Gamma.logGamma(nxHP * 3 + 2 + 4 + 4)
				+ Gamma.logGamma(nxHP + 2) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 4) - Gamma.logGamma(nxHP)
				+ Gamma.logGamma(nxHP + 4) - Gamma.logGamma(nxHP)

				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 2) + Gamma.logGamma(nxHP + 2)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 2 + 2)
				+ Gamma.logGamma(nxHP + 2) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 2) - Gamma.logGamma(nxHP)
				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 4) + Gamma.logGamma(nxHP + 4)
				- Gamma.logGamma(nxHP)

				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 3 + 1) + Gamma.logGamma(nxHP + 3)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 2)
				- Gamma.logGamma(nxHP * 2 + 3 + 3) + Gamma.logGamma(nxHP + 3) - Gamma.logGamma(nxHP)
				+ Gamma.logGamma(nxHP + 3) - Gamma.logGamma(nxHP);
		double bdeS2Actual = scoreFunction.compute(bn);
		assertEquals(bdeS2Expected, bdeS2Actual, 0.001);

		// Test with structure C2 -> C1 <- C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		bn = new BN<CPTNode>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNBayesianEstimation(nxHP));
		bn.learnParameters();
		scoreFunction = new BNBayesianScore();
		double bdeS3Expected = Gamma.logGamma(nxHP * 3) - Gamma.logGamma(nxHP * 3 + 2 + 1) + Gamma.logGamma(nxHP + 2)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 3)
				- Gamma.logGamma(nxHP * 3 + 1) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP)
				+ Gamma.logGamma(nxHP * 3) - Gamma.logGamma(nxHP * 3 + 2 + 1) + Gamma.logGamma(nxHP + 2)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 1) - Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP * 3)
				- Gamma.logGamma(nxHP * 3 + 3) + Gamma.logGamma(nxHP + 3) - Gamma.logGamma(nxHP)

				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 4 + 6) + Gamma.logGamma(nxHP + 4)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 6) - Gamma.logGamma(nxHP)

				+ Gamma.logGamma(nxHP * 2) - Gamma.logGamma(nxHP * 2 + 6 + 4) + Gamma.logGamma(nxHP + 6)
				- Gamma.logGamma(nxHP) + Gamma.logGamma(nxHP + 4) - Gamma.logGamma(nxHP);
		double bdeS3Actual = scoreFunction.compute(bn);
		assertEquals(bdeS3Expected, bdeS3Actual, 0.001);

		// Compare scores between structures
		assertTrue(bdeS1Actual < bdeS3Actual);
	}

	@AfterEach
	public void resetStructureAfterTest() {
		resetStructure(nodes);
	}

	private void resetStructure(List<? extends Node> nodes) {
		for (Node node : nodes)
			node.removeAllEdges();
	}

}
