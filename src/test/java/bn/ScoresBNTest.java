package bn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNBayesianEstimation;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNBayesianScore;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNLogLikelihood;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNScoreFunction;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.commons.math3.special.Gamma;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests over the score functions for BNs. Some structures are built, and their scores manually estimated to verify the
 * correct behaviour of the score functions.
 *
 * @author Carlos Villa Blanco
 */
class ScoresBNTest {
	static Dataset dataset;
	static List<CPTNode> nodes;

	/**
	 * Defines a dataset and the structure of a BN. Instead of learning the structure of the BN, it is directly defined
	 * over CPT nodes. This is done to know which structure we are working on during the tests.
	 */
	@BeforeAll
	public static void setUp() {
		// Define dataset
		List<String> nameClassVariables = List.of("C1", "C2", "C3");
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence1.add(new String[]{"0.0", "a", "a", "a"});
		dataSequence1.add(new String[]{"0.1", "a", "a", "a"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence2.add(new String[]{"0.0", "a", "a", "a"});
		dataSequence2.add(new String[]{"0.4", "a", "a", "a"});

		List<String[]> dataSequence3 = new ArrayList<>();
		dataSequence3.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence3.add(new String[]{"0.2", "b", "a", "a"});
		dataSequence3.add(new String[]{"0.7", "b", "a", "a"});

		List<String[]> dataSequence4 = new ArrayList<>();
		dataSequence4.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence4.add(new String[]{"0.0", "b", "a", "b"});
		dataSequence4.add(new String[]{"0.2", "b", "a", "b"});

		List<String[]> dataSequence5 = new ArrayList<>();
		dataSequence5.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence5.add(new String[]{"0.0", "b", "b", "a"});
		dataSequence5.add(new String[]{"0.2", "b", "b", "a"});

		List<String[]> dataSequence6 = new ArrayList<>();
		dataSequence6.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence6.add(new String[]{"0.0", "b", "b", "a"});
		dataSequence6.add(new String[]{"0.2", "b", "b", "a"});

		List<String[]> dataSequence7 = new ArrayList<>();
		dataSequence7.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence7.add(new String[]{"0.0", "c", "b", "a"});
		dataSequence7.add(new String[]{"0.2", "c", "b", "a"});

		List<String[]> dataSequence8 = new ArrayList<>();
		dataSequence8.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence8.add(new String[]{"0.0", "c", "b", "b"});
		dataSequence8.add(new String[]{"0.5", "c", "b", "b"});

		List<String[]> dataSequence9 = new ArrayList<>();
		dataSequence9.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence9.add(new String[]{"0.0", "c", "b", "b"});
		dataSequence9.add(new String[]{"0.5", "c", "b", "b"});

		List<String[]> dataSequence10 = new ArrayList<>();
		dataSequence10.add(new String[]{"Time", "C1", "C2", "C3"});
		dataSequence10.add(new String[]{"0.0", "c", "b", "b"});
		dataSequence10.add(new String[]{"0.5", "c", "b", "b"});

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
		// As learnt parameters are tested, whether a node is a class variable or not
		// is irrelevant.
		nodes = new ArrayList<>();
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
		BN<CPTNode> bn = new BN<>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		// Log-likelihood
		BNScoreFunction scoreFunction = new BNLogLikelihood("No");
		double llS1Expected = 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) + 1 * Math.log(1) + 2 * Math.log(2 / 3.0) +
				1 * Math.log(1 / 3.0) + 3 * Math.log(1) + 3 * Math.log(0.5) + 3 * Math.log(0.5) +
				1 * Math.log(1 / 4.0) + 3 * Math.log(3 / 4.0) + 6 * Math.log(6 / 10.0) + 4 * Math.log(4 / 10.0);
		double llS1Actual = scoreFunction.compute(bn);
		assertEquals(llS1Expected, llS1Actual, 0.001);
		// Penalised log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		double llS1BICExpected = llS1Expected - 0.5 * Math.log(10) * ((3 - 1) * 4 + (2 - 1) * 2 + (2 - 1));
		double llS1BICActual = scoreFunction.compute(bn);
		assertEquals(llS1BICExpected, llS1BICActual, 0.001);
		// Penalised log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		double llS1AICExpected = llS1Expected - ((3 - 1) * 4 + (2 - 1) * 2 + (2 - 1));
		double llS1AICActual = scoreFunction.compute(bn);
		assertEquals(llS1AICExpected, llS1AICActual, 0.001);

		// Test with structure C1 -> C2 -> C3
		// Remove arcs of the previous structure
		resetStructure(nodes);
		nodes.get(1).setParent(nodes.get(0));
		nodes.get(2).setParent(nodes.get(1));
		bn = new BN<>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		// Log-likelihood
		scoreFunction = new BNLogLikelihood("No");
		double llS2Expected =
				2 * Math.log(2 / 10.0) + 4 * Math.log(4 / 10.0) + 4 * Math.log(4 / 10.0) + 2 * Math.log(2 / 2.0) +
						2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0) + 4 * Math.log(4 / 4.0) + 3 * Math.log(3 / 4.0) +
						1 * Math.log(1 / 4.0) + 3 * Math.log(3 / 6.0) + 3 * Math.log(3 / 6.0);
		double llS2Actual = scoreFunction.compute(bn);
		assertEquals(llS2Expected, llS2Actual, 0.001);
		// Penalised log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		double llS2BICExpected = llS2Expected - 0.5 * Math.log(10) * ((3 - 1) + (2 - 1) * 3 + (2 - 1) * 2);
		double llS2BICActual = scoreFunction.compute(bn);
		assertEquals(llS2BICExpected, llS2BICActual, 0.001);
		// Penalised log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		double llS2AICExpected = llS2Expected - ((3 - 1) + (2 - 1) * 3 + (2 - 1) * 2);
		double llS2AICActual = scoreFunction.compute(bn);
		assertEquals(llS2AICExpected, llS2AICActual, 0.001);

		// Test with structure C2 -> C1 <- C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));

		bn = new BN<>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bn.learnParameters();
		// Log-likelihood
		scoreFunction = new BNLogLikelihood("No");
		double llS3Expected =
				2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) + 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0) +
						3 * Math.log(3 / 3.0) + 4 * Math.log(4 / 10.0) + 6 * Math.log(6 / 10.0) +
						6 * Math.log(6 / 10.0) + 4 * Math.log(4 / 10.0);
		double llS3Actual = scoreFunction.compute(bn);
		assertEquals(llS3Expected, llS3Actual, 0.001);
		// Penalised log-likelihood with BIC
		scoreFunction = new BNLogLikelihood("BIC");
		double llS3BICExpected = llS3Expected - 0.5 * Math.log(10) * ((3 - 1) * 4 + (2 - 1) + (2 - 1));
		double llS3BICActual = scoreFunction.compute(bn);
		assertEquals(llS3BICExpected, scoreFunction.compute(bn), 0.001);
		// Penalised log-likelihood with AIC
		scoreFunction = new BNLogLikelihood("AIC");
		double llS3AICExpected = llS3Expected - ((3 - 1) * 4 + (2 - 1) + (2 - 1));
		double llS3AICActual = scoreFunction.compute(bn);
		assertEquals(llS3AICExpected, llS3AICActual, 0.001);

		// Compare scores between structures.
		// Although the dataset was extracted from the third structure (with some
		// noise), the more dense first structure has a higher log-likelihood. If a BIC
		// Penalisation is applied, the first structure would have a better score
		assertTrue(llS1Actual > llS3Actual);
		assertTrue(llS1BICActual < llS3BICActual);
		assertTrue(llS1AICActual < llS3AICActual);
	}

	@Test
	void testBayesianScore() {
		double nHP = 2.0;

		// Test with structure C1 <- C2 <- C3 -> C1
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
		BN<CPTNode> bn = new BN<>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNBayesianEstimation(nHP));
		bn.learnParameters();
		BNScoreFunction scoreFunction = new BNBayesianScore();

		double nxHPC1 = nHP / (nodes.get(0).getNumStates() * nodes.get(0).getNumStatesParents());
		double nxHPC2 = nHP / (nodes.get(1).getNumStates() * nodes.get(1).getNumStatesParents());
		double nxHPC3 = nHP / (nodes.get(2).getNumStates() * nodes.get(2).getNumStatesParents());

		double bdeS1Expected =
				Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 2 + 1) + Gamma.logGamma(nxHPC1 + 2) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 + 1) - Gamma.logGamma(nxHPC1) +
						Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 1) + Gamma.logGamma(nxHPC1 + 1) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 2 + 1) +
						Gamma.logGamma(nxHPC1 + 2) - Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 + 1) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 3) +
						Gamma.logGamma(nxHPC1 + 3) - Gamma.logGamma(nxHPC1)

						+ Gamma.logGamma(nxHPC2 * 2) - Gamma.logGamma(6 + nxHPC2 * 2) + Gamma.logGamma(3 + nxHPC2) -
						Gamma.logGamma(nxHPC2) + Gamma.logGamma(3 + nxHPC2) - Gamma.logGamma(nxHPC2) +
						Gamma.logGamma(nxHPC2 * 2) - Gamma.logGamma(4 + nxHPC2 * 2) + Gamma.logGamma(1 + nxHPC2) -
						Gamma.logGamma(nxHPC2) + Gamma.logGamma(3 + nxHPC2) - Gamma.logGamma(nxHPC2)

						+ Gamma.logGamma(nxHPC3 * 2) - Gamma.logGamma(nxHPC3 * 2 + 6 + 4) + Gamma.logGamma(nxHPC3 + 6) -
						Gamma.logGamma(nxHPC3) + Gamma.logGamma(4 + nxHPC3) - Gamma.logGamma(nxHPC3);
		double bdeS1Actual = scoreFunction.compute(bn);
		assertEquals(bdeS1Expected, bdeS1Actual, 0.001);

		// Test with structure C1 -> C2 -> C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(1).setParent(nodes.get(0));
		nodes.get(2).setParent(nodes.get(1));
		bn = new BN<>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNBayesianEstimation(nHP));
		bn.learnParameters();
		scoreFunction = new BNBayesianScore();

		// Hyperparameters dirichlet distribution
		nxHPC1 = nHP / (nodes.get(0).getNumStates() * nodes.get(0).getNumStatesParents());
		nxHPC2 = nHP / (nodes.get(1).getNumStates() * nodes.get(1).getNumStatesParents());
		nxHPC3 = nHP / (nodes.get(2).getNumStates() * nodes.get(2).getNumStatesParents());

		double bdeS2Expected =
				Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 2 + 4 + 4) + Gamma.logGamma(nxHPC1 + 2) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 + 4) - Gamma.logGamma(nxHPC1) +
						Gamma.logGamma(nxHPC1 + 4) - Gamma.logGamma(nxHPC1)

						+ Gamma.logGamma(nxHPC2 * 2) - Gamma.logGamma(nxHPC2 * 2 + 2) + Gamma.logGamma(nxHPC2 + 2) -
						Gamma.logGamma(nxHPC2) + Gamma.logGamma(nxHPC2 * 2) - Gamma.logGamma(nxHPC2 * 2 + 2 + 2) +
						Gamma.logGamma(nxHPC2 + 2) - Gamma.logGamma(nxHPC2) + Gamma.logGamma(nxHPC2 + 2) -
						Gamma.logGamma(nxHPC2) + Gamma.logGamma(nxHPC2 * 2) - Gamma.logGamma(nxHPC2 * 2 + 4) +
						Gamma.logGamma(nxHPC2 + 4) - Gamma.logGamma(nxHPC2)

						+ Gamma.logGamma(nxHPC3 * 2) - Gamma.logGamma(nxHPC3 * 2 + 3 + 1) + Gamma.logGamma(nxHPC3 + 3) -
						Gamma.logGamma(nxHPC3) + Gamma.logGamma(nxHPC3 + 1) - Gamma.logGamma(nxHPC3) +
						Gamma.logGamma(nxHPC3 * 2) - Gamma.logGamma(nxHPC3 * 2 + 3 + 3) + Gamma.logGamma(nxHPC3 + 3) -
						Gamma.logGamma(nxHPC3) + Gamma.logGamma(nxHPC3 + 3) - Gamma.logGamma(nxHPC3);
		double bdeS2Actual = scoreFunction.compute(bn);
		assertEquals(bdeS2Expected, bdeS2Actual, 0.001);

		// Test with structure C2 -> C1 <- C3
		// Remove arcs previous structure
		resetStructure(nodes);
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		bn = new BN<>(nodes, dataset);
		bn.setParameterLearningAlgorithm(new BNBayesianEstimation(nHP));
		bn.learnParameters();
		scoreFunction = new BNBayesianScore();

		// Hyperparameters dirichlet distribution
		nxHPC1 = nHP / (nodes.get(0).getNumStates() * nodes.get(0).getNumStatesParents());
		nxHPC2 = nHP / (nodes.get(1).getNumStates() * nodes.get(1).getNumStatesParents());
		nxHPC3 = nHP / (nodes.get(2).getNumStates() * nodes.get(2).getNumStatesParents());

		double bdeS3Expected =
				Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 2 + 1) + Gamma.logGamma(nxHPC1 + 2) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 + 1) - Gamma.logGamma(nxHPC1) +
						Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 1) + Gamma.logGamma(nxHPC1 + 1) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 2 + 1) +
						Gamma.logGamma(nxHPC1 + 2) - Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 + 1) -
						Gamma.logGamma(nxHPC1) + Gamma.logGamma(nxHPC1 * 3) - Gamma.logGamma(nxHPC1 * 3 + 3) +
						Gamma.logGamma(nxHPC1 + 3) - Gamma.logGamma(nxHPC1)

						+ Gamma.logGamma(nxHPC2 * 2) - Gamma.logGamma(nxHPC2 * 2 + 4 + 6) + Gamma.logGamma(nxHPC2 + 4) -
						Gamma.logGamma(nxHPC2) + Gamma.logGamma(nxHPC2 + 6) - Gamma.logGamma(nxHPC2)

						+ Gamma.logGamma(nxHPC3 * 2) - Gamma.logGamma(nxHPC3 * 2 + 6 + 4) + Gamma.logGamma(nxHPC3 + 6) -
						Gamma.logGamma(nxHPC3) + Gamma.logGamma(nxHPC3 + 4) - Gamma.logGamma(nxHPC3);
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