package ctbn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.special.Gamma;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNConditionalLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNScoreFunction;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Tests over the score functions for CTBNs. Some structures are built and their
 * scores manually estimated in order to verify the correct behavior of the
 * score functions. As the CTBNs are used to define the bridge and feature
 * subgraphs of a MCTBNC, there can exist nodes for class variables.
 * 
 * @author Carlos Villa Blanco
 *
 */
class ScoresCTBNTest {
	static Dataset dataset;
	static List<CPTNode> nodesCVs;
	static List<CIMNode> nodesFs;

	/**
	 * Defines a dataset and the structure of a CTBN. Instead of learning the
	 * structure of the CTBN, it is directly defined over CIM nodes. This is done to
	 * know which structure we are working on during the tests.
	 * 
	 * 
	 * X1 = {a, b} if C1 = {a} X1 = {b, c} if C1 = {b}
	 * 
	 * X2 = ~X3
	 * 
	 * X3 = {b} if X1 + X2 + C2 -> Two or more 'b'
	 * 
	 */
	@BeforeAll
	public static void setUp() {
		// Define dataset
		List<String> nameClassVariables = List.of("C1", "C2", "C3");
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<String[]>();
		dataSequence1.add(new String[] { "Time", "X1", "X2", "X3", "C1", "C2", "C3" });
		dataSequence1.add(new String[] { "0.0", "a", "a", "a", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.1", "a", "b", "a", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.4", "b", "b", "a", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.5", "b", "b", "b", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.6", "b", "a", "b", "a", "a", "a" });
		dataSequence1.add(new String[] { "0.9", "a", "a", "b", "a", "a", "a" });
		dataSequence1.add(new String[] { "1.0", "a", "a", "a", "a", "a", "a" });
		dataSequence1.add(new String[] { "1.1", "a", "b", "a", "a", "a", "a" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "X1", "X2", "X3", "C1", "C2", "C3" });
		dataSequence2.add(new String[] { "0.0", "b", "b", "b", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.1", "b", "a", "b", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.4", "c", "a", "b", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.5", "c", "a", "a", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.6", "c", "b", "a", "b", "a", "a" });
		dataSequence2.add(new String[] { "0.9", "b", "b", "a", "b", "a", "a" });
		dataSequence2.add(new String[] { "1.0", "b", "b", "b", "b", "a", "a" });
		dataSequence2.add(new String[] { "1.1", "b", "a", "b", "b", "a", "a" });
		dataSequence2.add(new String[] { "1.2", "a", "a", "b", "b", "a", "a" }); // Noise

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "X1", "X2", "X3", "C1", "C2", "C3" });
		dataSequence3.add(new String[] { "0.0", "a", "a", "a", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.1", "a", "b", "a", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.2", "a", "b", "b", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.3", "a", "a", "b", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.4", "a", "a", "a", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.5", "a", "b", "a", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.6", "b", "b", "a", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.7", "b", "b", "b", "a", "b", "a" });
		dataSequence3.add(new String[] { "0.8", "b", "a", "b", "a", "b", "a" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "X1", "X2", "X3", "C1", "C2", "C3" });
		dataSequence4.add(new String[] { "0.0", "c", "a", "a", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.1", "c", "b", "a", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.4", "b", "b", "a", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.5", "b", "b", "b", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.6", "b", "a", "b", "b", "b", "b" });
		dataSequence4.add(new String[] { "0.9", "c", "a", "b", "b", "b", "b" });
		dataSequence4.add(new String[] { "1.0", "c", "a", "a", "b", "b", "b" });
		dataSequence4.add(new String[] { "1.1", "c", "b", "a", "b", "b", "b" });
		dataSequence4.add(new String[] { "1.4", "b", "b", "a", "b", "b", "b" });
		dataSequence4.add(new String[] { "1.5", "b", "b", "b", "b", "b", "b" });

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);

		// Define class variables' nodes and class subgraph
		nodesCVs = new ArrayList<CPTNode>();
		for (String nameClassVariable : dataset.getNameClassVariables()) {
			CPTNode nodeCV = new CPTNode(nameClassVariable, dataset.getPossibleStatesVariable(nameClassVariable));
			nodeCV.isClassVariable(true);
			nodesCVs.add(nodeCV);
		}

		// Define features' nodes
		nodesFs = new ArrayList<CIMNode>();
		for (String nameClassVariable : dataset.getNameFeatures()) {
			nodesFs.add(new CIMNode(nameClassVariable, dataset.getPossibleStatesVariable(nameClassVariable)));
		}

	}

	@Test
	void testLogLikelihood() {
		// Test with structure C1 -> X1 -> X3 <-> X2 X3 <- C2

		// Structure class subgraph
		nodesCVs.get(0).setParent(nodesCVs.get(1));
		BN<CPTNode> bnClassSubgraph = new BN<CPTNode>(nodesCVs, dataset);

		// Structure feature and bridge subgraphs
		nodesFs.get(0).setParent(nodesCVs.get(0));
		nodesFs.get(2).setParent(nodesCVs.get(1));
		nodesFs.get(1).setParent(nodesFs.get(2));
		nodesFs.get(2).setParent(nodesFs.get(0));
		nodesFs.get(2).setParent(nodesFs.get(1));
		CTBN<CIMNode> ctbn = new CTBN<CIMNode>(nodesFs, bnClassSubgraph, dataset);
		ctbn.setParameterLearningAlgorithm(new CTBNMaximumLikelihoodEstimation());
		ctbn.learnParameters();

		// Log-likelihood
		CTBNScoreFunction scoreFunction = new CTBNLogLikelihood("No");
		double llS1Expected = 2 * Math.log(2 / 1.2) - (2 / 1.2) * 1.2 + (2 * Math.log(2 / 2)) + 1 * Math.log(1 / 0.7)
				- (1 / 0.7) * 0.7 + (1 * Math.log(1 / 1)) + 3 * Math.log(3 / 1.3) - (3 / 1.3) * 1.3
				+ (3 * Math.log(2 / 2)) + (2 * Math.log(2 / 3.0)) + (1 * Math.log(1 / 3.0)) + 3 * Math.log(3 / 1.4)
				- (3 / 1.4) * 1.4 + (3 * Math.log(3 / 3))

				+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7 + 7 * Math.log(7 / 7) + 6 * Math.log(6 / 0.6)
				- (6 / 0.6) * 0.6 + 6 * Math.log(6 / 6)

				+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1 + 2 * Math.log(2 / 0.2) - (2 / 0.2) * 0.2
				+ 3 * Math.log(3 / 0.3) - (3 / 0.3) * 0.3 + 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
				+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1 + 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
				+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double llS1Actual = scoreFunction.compute(ctbn);
		assertEquals(llS1Expected, llS1Actual, 0.001);
		// Penalized log-likelihood with BIC
		scoreFunction = new CTBNLogLikelihood("BIC");
		double llS1BICExpected = llS1Expected
				- 0.5 * Math.log(4) * ((3 - 1) * 3 * 2 + (2 - 1) * 2 * 2 + (2 - 1) * 2 * 12);
		double llS1BICActual = scoreFunction.compute(ctbn);
		assertEquals(llS1BICExpected, llS1BICActual, 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new CTBNLogLikelihood("AIC");
		double llS1AICExpected = llS1Expected - ((3 - 1) * 3 * 2 + (2 - 1) * 2 * 2 + (2 - 1) * 2 * 12);
		double llS1AICActual = scoreFunction.compute(ctbn);
		assertEquals(llS1AICExpected, llS1AICActual, 0.001);

		// Test with structure X3 -> X1 <- X2 <- C1 <- C2 -> X1 (a feature has no
		// parents)
		// Remove previous structure (except class subgraph)
		resetStructure(nodesFs);
		// New structure
		nodesFs.get(0).setParent(nodesCVs.get(1));
		nodesFs.get(1).setParent(nodesCVs.get(0));
		nodesFs.get(0).setParent(nodesFs.get(1));
		nodesFs.get(0).setParent(nodesFs.get(2));
		ctbn = new CTBN<CIMNode>(nodesFs, bnClassSubgraph, dataset);

		ctbn.setParameterLearningAlgorithm(new CTBNMaximumLikelihoodEstimation());
		ctbn.learnParameters();
		// Log-likelihood
		scoreFunction = new CTBNLogLikelihood("No");
		double llS2Expected = 3 * Math.log(3 / 0.7) - (3 / 0.7) * 0.7 + 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0)
				+ 1 * Math.log(1 / 0.3) - (1 / 0.3) * 0.3 + 1 * Math.log(1 / 0.3) - (1 / 0.3) * 0.3
				+ 1 * Math.log(1 / 0.3) - (1 / 0.3) * 0.3 + 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
				+ 2 * Math.log(2 / 0.6) - (2 / 0.6) * 0.6

				+ 3 * Math.log(3 / 1.2) - (3 / 1.2) * 1.2 + 3 * Math.log(3 / 1.0) - (3 / 1.0) * 1.0
				+ 4 * Math.log(4 / 0.9) - (4 / 0.9) * 0.9 + 3 * Math.log(3 / 1.5) - (3 / 1.5) * 1.5

				+ 6 * Math.log(6 / 2.6) - (6 / 2.6) * 2.6 + 4 * Math.log(4 / 2) - (4 / 2) * 2;

		double llS2Actual = scoreFunction.compute(ctbn);
		assertEquals(llS2Expected, llS2Actual, 0.001);

		// Penalized log-likelihood with BIC
		scoreFunction = new CTBNLogLikelihood("BIC");
		double llS2BICExpected = llS2Expected - 0.5 * Math.log(4) * ((3 - 1) * 3 * 8 + (2 - 1) * 2 * 2 + (2 - 1) * 2);
		double llS2BICActual = scoreFunction.compute(ctbn);
		assertEquals(llS2BICExpected, llS2BICActual, 0.001);
		// Penalized log-likelihood with AIC
		scoreFunction = new CTBNLogLikelihood("AIC");
		double llS2AICExpected = llS2Expected - ((3 - 1) * 3 * 8 + (2 - 1) * 2 * 2 + (2 - 1) * 2);
		double llS2AICActual = scoreFunction.compute(ctbn);
		assertEquals(llS2AICExpected, llS2AICActual, 0.001);

		// Compare scores between structures.
		assertTrue(llS1Actual > llS2Actual);
		assertTrue(llS1BICActual > llS2BICActual);
		assertTrue(llS1AICActual > llS2AICActual);
	}

	@Test
	void testConditionalLogLikelihood() {
		// Test with structure X3 <- C2 -> C3 <- C1 -> X1 -> X3 <-> X2
		// Class subgraph
		nodesCVs.get(2).setParent(nodesCVs.get(0));
		nodesCVs.get(2).setParent(nodesCVs.get(1));
		BN<CPTNode> bnClassSubgraph = new BN<CPTNode>(nodesCVs, dataset);
		bnClassSubgraph.setParameterLearningAlgorithm(new BNMaximumLikelihoodEstimation());
		bnClassSubgraph.learnParameters();
		// Feature and bridge subgraphs
		nodesFs.get(0).setParent(nodesCVs.get(0));
		nodesFs.get(2).setParent(nodesCVs.get(1));
		nodesFs.get(1).setParent(nodesFs.get(2));
		nodesFs.get(2).setParent(nodesFs.get(0));
		nodesFs.get(2).setParent(nodesFs.get(1));
		CTBN<CIMNode> ctbn = new CTBN<CIMNode>(nodesFs, bnClassSubgraph, dataset);
		ctbn.setParameterLearningAlgorithm(new CTBNMaximumLikelihoodEstimation());
		ctbn.learnParameters();

		// Conditional log-likelihood
		CTBNScoreFunction scoreFunction = new CTBNConditionalLogLikelihood("No");

//		double probCVs =
//				// Evaluate C1
//				1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0)
//						+ 2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0)
//
//						// Evaluate C2
//						+ 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0)
//						+ 2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0)
//
//						// Evaluate C3
//						+ 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0) + 1 * Math.log(1 / 2.0)
//						+ 2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0);

		double probCVs =
				// Evaluate C1
				2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0)
				// Evaluate C2
						+ 2 * Math.log(2 / 4.0) + 2 * Math.log(2 / 4.0)
		// Evaluate C3 (log probability = 0)
		;

		double llSequences =
				// X1
				2 * Math.log(2 / 1.2) - (2 / 1.2) * 1.2 + Math.log(1 / 0.7) - (1 / 0.7) * 0.7 + 3 * Math.log(3 / 1.3)
						- (3 / 1.3) * 1.3 + 1 * Math.log(1 / 3.0) + 2 * Math.log(2 / 3.0) + 3 * Math.log(3 / 1.4)
						- (3 / 1.4) * 1.4

						// X2
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7 + 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6

						// X3
						+ 2 * Math.log(2 / 0.2) - (2 / 0.2) * 0.2 + 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1 + 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1 + 3 * Math.log(3 / 0.3) - (3 / 0.3) * 0.3
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1

		;
		// To compute the log-marginal-likelihood of a sequence, it is necessary to
		// extract for each possible class configuration the sum of their log prior
		// probabilities and the posterior probability of sequences given the class
		// configurations
		double C1aC2aC3a =
				// C1 = a / C2 = a / C3 = a
				0.5 + 0.5 + 1
				// X1
				// X1 = a / C1 = a
						+ 2 * Math.log(2 / 1.2) - (2 / 1.2) * 1.2
						// X1 = b / C1 = a
						+ 1 * Math.log(1 / 0.7) - (1 / 0.7) * 0.7
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b/ C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = b / X2 = b / X3 = a / C2 = a
						+ 2 * Math.log(2 / 0.2) - (2 / 0.2) * 0.2
						// X1 = c / X2 = a / X3 = b / C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1aC2aC3b =
				// C1 = a / C2 = a / C3 = b
				0.5 + 0.5
				// X1
				// X1 = a / C1 = a
						+ 2 * Math.log(2 / 1.2) - (2 / 1.2) * 1.2
						// X1 = b / C1 = a
						+ 1 * Math.log(1 / 0.7) - (1 / 0.7) * 0.7
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b/ C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = b / X2 = b / X3 = a / C2 = a
						+ 2 * Math.log(2 / 0.2) - (2 / 0.2) * 0.2
						// X1 = c / X2 = a / X3 = b / C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1aC2bC3a =
				// C1 = a / C2 = b / C3 = a
				0.5 + 0.5 + 1
				// X1
				// X1 = a / C1 = a
						+ 2 * Math.log(2 / 1.2) - (2 / 1.2) * 1.2
						// X1 = b / C1 = a
						+ 1 * Math.log(1 / 0.7) - (1 / 0.7) * 0.7
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = a / X2 = b / X3 = a / C2 = b
						+ 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
						// X1 = b / X2 = b / X3 = a / C2 = b
						+ 3 * Math.log(3 / 0.3) - (3 / 0.3) * 0.3
						// X1 = c / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1aC2bC3b =
				// C1 = a / C2 = b / C3 = b
				0.5 + 0.5
				// X1
				// X1 = a / C1 = a
						+ 2 * Math.log(2 / 1.2) - (2 / 1.2) * 1.2
						// X1 = b / C1 = a
						+ 1 * Math.log(1 / 0.7) - (1 / 0.7) * 0.7
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = a / X2 = b / X3 = a / C2 = b
						+ 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
						// X1 = b / X2 = b / X3 = a / C2 = b
						+ 3 * Math.log(3 / 0.3) - (3 / 0.3) * 0.3
						// X1 = c / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1bC2aC3a =
				// C1 = b / C2 = a / C3 = a
				0.5 + 0.5 + 1
				// X1
				// X1 = b / C1 = b
						+ 3 * Math.log(3 / 1.3) - (3 / 1.3) * 1.3 + 2 * Math.log(2 / 3.0) + Math.log(1 / 3.0)
						// X1 = c / C1 = b
						+ 3 * Math.log(3 / 1.4) - (3 / 1.4) * 1.4
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b/ C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = b / X2 = b / X3 = a / C2 = a
						+ 2 * Math.log(2 / 0.2) - (2 / 0.2) * 0.2
						// X1 = c / X2 = a / X3 = b / C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1bC2aC3b =
				// C1 = b / C2 = a / C3 = b
				0.5 + 0.5
				// X1
				// X1 = b / C1 = b
						+ 3 * Math.log(3 / 1.3) - (3 / 1.3) * 1.3 + 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0)
						// X1 = c / C1 = b
						+ 3 * Math.log(3 / 1.4) - (3 / 1.4) * 1.4
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b/ C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = b / X2 = b / X3 = a / C2 = a
						+ 2 * Math.log(2 / 0.2) - (2 / 0.2) * 0.2
						// X1 = c / X2 = a / X3 = b / C2 = a
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1bC2bC3a =
				// C1 = b / C2 = b / C3 = a
				0.5 + 0.5
				// X1
				// X1 = b / C1 = b
						+ 3 * Math.log(3 / 1.3) - (3 / 1.3) * 1.3 + 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0)
						// X1 = c / C1 = b
						+ 3 * Math.log(3 / 1.4) - (3 / 1.4) * 1.4
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = a / X2 = b / X3 = a / C2 = b
						+ 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
						// X1 = b / X2 = b / X3 = a / C2 = b
						+ 3 * Math.log(3 / 0.3) - (3 / 0.3) * 0.3
						// X1 = c / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;
		double C1bC2bC3b =
				// C1 = b / C2 = b / C3 = b
				0.5 + 0.5 + 1
				// X1
				// X1 = b / C1 = b
						+ 3 * Math.log(3 / 1.3) - (3 / 1.3) * 1.3 + 2 * Math.log(2 / 3.0) + 1 * Math.log(1 / 3.0)
						// X1 = c / C1 = b
						+ 3 * Math.log(3 / 1.4) - (3 / 1.4) * 1.4
						// X2
						// X2 = a / X3 = a
						+ 7 * Math.log(7 / 0.7) - (7 / 0.7) * 0.7
						// X2 = b / X3 = b
						+ 6 * Math.log(6 / 0.6) - (6 / 0.6) * 0.6
						// X3
						// X1 = a / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1
						// X1 = a / X2 = b / X3 = a / C2 = b
						+ 1 * Math.log(1 / 0.2) - (1 / 0.2) * 0.2
						// X1 = b / X2 = b / X3 = a / C2 = b
						+ 3 * Math.log(3 / 0.3) - (3 / 0.3) * 0.3
						// X1 = c / X2 = a / X3 = b / C2 = b
						+ 1 * Math.log(1 / 0.1) - (1 / 0.1) * 0.1;

		// The log-sum-exp trick is used to avoid overflows and underflows
		double largest = C1aC2bC3a;
		double sum = Math.exp(C1aC2aC3a - largest) + Math.exp(C1aC2aC3b - largest) + Math.exp(C1aC2bC3a - largest)
				+ Math.exp(C1aC2bC3b - largest) + Math.exp(C1bC2aC3a - largest) + Math.exp(C1bC2aC3b - largest)
				+ Math.exp(C1bC2bC3a - largest) + Math.exp(C1bC2bC3b - largest);
		// Log-marginal-likelihood
		double mll = largest + Math.log(sum);

		double cllS1Expected = probCVs + llSequences - mll;
		double cllS1Actual = scoreFunction.compute(ctbn);
		assertEquals(cllS1Expected, cllS1Actual, 0.001);

		// Penalized conditional log-likelihood with BIC
		scoreFunction = new CTBNConditionalLogLikelihood("BIC");
		double cllS1BICExpected = cllS1Expected
				- ((2 * (3 - 1) * 3 + 2 * (2 - 1) * 2 + 12 * (2 - 1) * 2) / 2) * Math.log(4);
		double cllS1BICActual = scoreFunction.compute(ctbn);
		assertEquals(cllS1BICExpected, cllS1BICActual, 0.001);

		// Penalized conditional log-likelihood with AIC
		scoreFunction = new CTBNConditionalLogLikelihood("AIC");
		double cllS1AICExpected = cllS1Expected - ((2 * (3 - 1) * 3 + 2 * (2 - 1) * 2 + 12 * (2 - 1) * 2) / 2) * 2;
		double cllS1AICActual = scoreFunction.compute(ctbn);
		assertEquals(cllS1AICExpected, cllS1AICActual, 0.001);
	}

	@Test
	void testBayesianScore() {
		double mxyHP = 3;
		double txHP = 4.5;

		// Test with structure X3 <- C2 -> C1 -> X1 -> X3 <-> X2
		BN<CPTNode> bnClassSubgraph = new BN<CPTNode>(nodesCVs, dataset);
		nodesFs.get(0).setParent(nodesCVs.get(0));
		nodesFs.get(2).setParent(nodesCVs.get(1));
		nodesFs.get(1).setParent(nodesFs.get(2));
		nodesFs.get(2).setParent(nodesFs.get(0));
		nodesFs.get(2).setParent(nodesFs.get(1));
		CTBN<CIMNode> ctbn = new CTBN<CIMNode>(nodesFs, bnClassSubgraph, dataset);
		ctbn.setParameterLearningAlgorithm(new CTBNBayesianEstimation(mxyHP, txHP));
		ctbn.learnParameters();

		// Log-likelihood
		CTBNScoreFunction scoreFunction = new CTBNBayesianScore();
		double bsS1Expected = Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 2) + Gamma.logGamma(mxyHP + 2)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 2 + 1) * Math.log(txHP + 1.2))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 1) + Gamma.logGamma(mxyHP + 1)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 1 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1 + 1) * Math.log(txHP + 0.7))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 3) + Gamma.logGamma(mxyHP + 1)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP + 2) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP * 2 + 3 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 3 + 1) * Math.log(txHP + 1.3))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 3) + Gamma.logGamma(mxyHP + 3)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 3 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 3 + 1) * Math.log(txHP + 1.4))

				+ Gamma.logGamma(mxyHP) - Gamma.logGamma(mxyHP + 7) + Gamma.logGamma(mxyHP + 7) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 7 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 7 + 1) * Math.log(txHP + 0.7)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 1.4))
				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 1.9)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 6) + Gamma.logGamma(mxyHP + 6) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 6 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 6 + 1) * Math.log(txHP + 0.6))

				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.2)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP) - Gamma.logGamma(mxyHP + 2) + Gamma.logGamma(mxyHP + 2) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 2 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 2 + 1) * Math.log(txHP + 0.2)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 1) + Gamma.logGamma(mxyHP + 1) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 1 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1 + 1) * Math.log(txHP + 0.1)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 1) + Gamma.logGamma(mxyHP + 1) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 1 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1 + 1) * Math.log(txHP + 0.1)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 1) + Gamma.logGamma(mxyHP + 1) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 1 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1 + 1) * Math.log(txHP + 0.2)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 1) + Gamma.logGamma(mxyHP + 1) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 1 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1 + 1) * Math.log(txHP + 0.1)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 3) + Gamma.logGamma(mxyHP + 3) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 3 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 3 + 1) * Math.log(txHP + 0.3)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 1) + Gamma.logGamma(mxyHP + 1) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 1 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1 + 1) * Math.log(txHP + 0.1)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.7)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.1))
				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.3)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.1)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.3)) + Gamma.logGamma(mxyHP + 1)
				+ (mxyHP + 1) * Math.log(txHP) - (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 1) * Math.log(txHP + 0.6));

		double bsS1Actual = scoreFunction.compute(ctbn);
		assertEquals(bsS1Expected, bsS1Actual, 0.001);

		// Test with structure X3 -> X1 <- X2 <- C1 <- C2 -> X1 (a feature has no
		// parents)
		// Remove previous structure (except class subgraph)
		nodesFs.get(0).removeAllEdges();
		nodesFs.get(1).removeAllEdges();
		nodesFs.get(2).removeAllEdges();
		// New structure
		nodesFs.get(0).setParent(nodesCVs.get(1));
		nodesFs.get(1).setParent(nodesCVs.get(0));
		nodesFs.get(0).setParent(nodesFs.get(1));
		nodesFs.get(0).setParent(nodesFs.get(2));
		ctbn = new CTBN<CIMNode>(nodesFs, bnClassSubgraph, dataset);
		ctbn.setParameterLearningAlgorithm(new CTBNBayesianEstimation(mxyHP, txHP));
		ctbn.learnParameters();

		// Log-likelihood
		scoreFunction = new CTBNBayesianScore();
		double bsS2Expected = Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 1) + Gamma.logGamma(mxyHP + 1)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 1 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1 + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 2) + Gamma.logGamma(mxyHP + 2)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 2 + 1) * Math.log(txHP + 0.6))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 1) + Gamma.logGamma(mxyHP + 1)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 1 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1 + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 3) + Gamma.logGamma(mxyHP + 2)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP + 1) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP * 2 + 3 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 3 + 1) * Math.log(txHP + 0.7))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 1) + Gamma.logGamma(mxyHP + 1)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 1 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1 + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP * 2) - Gamma.logGamma(mxyHP * 2 + 1) + Gamma.logGamma(mxyHP + 1)
				- Gamma.logGamma(mxyHP) + Gamma.logGamma(mxyHP * 2 + 1 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1 + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.1))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.1))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.3))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.1))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.1))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.2))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.1))
				+ Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP * 2 + 1) + (mxyHP * 2 + 1) * Math.log(txHP + 0.1))

				+ Gamma.logGamma(mxyHP) - Gamma.logGamma(mxyHP + 3) + Gamma.logGamma(mxyHP + 3) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 3 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 3 + 1) * Math.log(txHP + 1.2)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 3) + Gamma.logGamma(mxyHP + 3) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 3 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 3 + 1) * Math.log(txHP + 1)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 4) + Gamma.logGamma(mxyHP + 4) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 4 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 4 + 1) * Math.log(txHP + 0.9)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 3) + Gamma.logGamma(mxyHP + 3) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 3 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 3 + 1) * Math.log(txHP + 1.5))

				+ Gamma.logGamma(mxyHP) - Gamma.logGamma(mxyHP + 6) + Gamma.logGamma(mxyHP + 6) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 6 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 6 + 1) * Math.log(txHP + 2.6)) + Gamma.logGamma(mxyHP)
				- Gamma.logGamma(mxyHP + 4) + Gamma.logGamma(mxyHP + 4) - Gamma.logGamma(mxyHP)
				+ Gamma.logGamma(mxyHP + 4 + 1) + (mxyHP + 1) * Math.log(txHP)
				- (Gamma.logGamma(mxyHP + 1) + (mxyHP + 4 + 1) * Math.log(txHP + 2));

		double bsS2Actual = scoreFunction.compute(ctbn);
		assertEquals(bsS2Expected, bsS2Actual, 0.001);

		// Compare scores between structures.
		assertTrue(bsS1Actual > bsS2Actual);
	}

	/**
	 * Reset the feature and bridge subgraphs after each method
	 */
	@AfterEach
	public void resetStructureAfterTest() {
		resetStructure(nodesFs);
	}

	private void resetStructure(List<? extends Node> nodes) {
		for (Node node : nodes)
			node.removeAllEdges();
	}

}
