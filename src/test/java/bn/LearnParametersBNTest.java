package bn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNBayesianEstimation;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests the estimation of sufficient statistics and parameters of a BN. Maximum
 * likelihood estimation test is executed first so the sufficient statistics are
 * computed before their test starts.
 * 
 * @author Carlos Villa Blanco
 *
 */
@TestMethodOrder(OrderAnnotation.class)
class LearnParametersBNTest {
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
		dataSequence1.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence1.add(new String[] { "0.1", "a", "b", "b" });
		dataSequence1.add(new String[] { "0.3", "a", "b", "b" });

		List<String[]> dataSequence2 = new ArrayList<String[]>();
		dataSequence2.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence2.add(new String[] { "0.0", "b", "b", "b" });
		dataSequence2.add(new String[] { "0.4", "b", "b", "b" });
		dataSequence2.add(new String[] { "0.5", "b", "b", "b" });

		List<String[]> dataSequence3 = new ArrayList<String[]>();
		dataSequence3.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence3.add(new String[] { "0.2", "a", "a", "a" });
		dataSequence3.add(new String[] { "0.7", "a", "a", "a" });

		List<String[]> dataSequence4 = new ArrayList<String[]>();
		dataSequence4.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence4.add(new String[] { "0.0", "c", "b", "b" });
		dataSequence4.add(new String[] { "0.2", "c", "b", "b" });
		dataSequence4.add(new String[] { "0.5", "c", "b", "b" });

		List<String[]> dataSequence5 = new ArrayList<String[]>();
		dataSequence5.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence5.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence5.add(new String[] { "0.2", "a", "b", "b" });
		dataSequence5.add(new String[] { "0.7", "a", "b", "b" });

		List<String[]> dataSequence6 = new ArrayList<String[]>();
		dataSequence6.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence6.add(new String[] { "0.0", "a", "b", "b" });
		dataSequence6.add(new String[] { "0.1", "a", "b", "b" });

		List<String[]> dataSequence7 = new ArrayList<String[]>();
		dataSequence7.add(new String[] { "Time", "C1", "C2", "C3" });
		dataSequence7.add(new String[] { "0.5", "c", "b", "b" });
		dataSequence7.add(new String[] { "0.6", "c", "b", "b" });

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6);
		dataset.addSequence(dataSequence7);

		// Define nodes
		// As it is tested the learned parameters, that a node is a class variable or
		// not is irrelevant.
		nodes = new ArrayList<CPTNode>();
		for (String nameClassVariable : dataset.getNameVariables()) {
			nodes.add(new CPTNode(nameClassVariable, dataset.getPossibleStatesVariable(nameClassVariable)));
		}

		// C2 and C3 are parents of C1 and C3 is parent of C2
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
	}

	@Test
	@Order(1)
	public void testMLEParameters() {
		// Learn sufficient statistics and parameters given the nodes and the dataset
		BNParameterLearningAlgorithm bnParameterLearning = new BNMaximumLikelihoodEstimation();
		bnParameterLearning.learn(nodes, dataset);

		// Retrieve nodes
		CPTNode nodeC1 = nodes.get(0);
		CPTNode nodeC2 = nodes.get(1);
		CPTNode nodeC3 = nodes.get(2);

		// Check parameters C1
		Integer idxFromStateNode = nodeC1.setState("a");
		nodeC2.setState("b");
		nodeC3.setState("b");
		Integer idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0.5, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC1.setState("b");
		nodeC2.setState("a");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC1.setState("c");
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0.333, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Check parameters C2
		idxFromStateNode = nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(1, nodeC2.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC2.setState("a");
		nodeC3.setState("a");
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(1, nodeC2.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Check parameters C3
		idxFromStateNode = nodeC3.setState("a");
		idxStateParents = nodeC3.getIdxStateParents();
		assertEquals(0.142, nodeC3.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC3.setState("b");
		idxStateParents = nodeC3.getIdxStateParents();
		assertEquals(0.857, nodeC3.getCP(idxStateParents, idxFromStateNode), 0.001);

		// States not seen before
		// Never seen state of the node
		idxFromStateNode = nodeC1.setState("d");
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Never seen state of the parents
		idxFromStateNode = nodeC1.setState("a");
		nodeC2.setState("f");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Never seen state of the node and its parents
		idxFromStateNode = nodeC1.setState("d");
		nodeC2.setState("f");
		nodeC3.setState("d");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);
	}

	@Test
	public void testMLESufficientStatistics() {
		// Retrieve nodes
		CPTNode nodeC1 = nodes.get(0);
		CPTNode nodeC2 = nodes.get(1);
		CPTNode nodeC3 = nodes.get(2);
		// Sufficient statistics node C1
		double[][] nxC1 = nodeC1.getSufficientStatistics().getNx();
		// Sufficient statistics node C2
		double[][] nxC2 = nodeC2.getSufficientStatistics().getNx();
		// Sufficient statistics node C3
		double[][] nxC3 = nodeC3.getSufficientStatistics().getNx();

		// Check sufficient statistics C1
		nodeC1.setState("a");
		nodeC2.setState("b");
		nodeC3.setState("b");
		Integer idxStateNode = nodeC1.getIdxState();
		Integer idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(3, nxC1[idxStateParents][idxStateNode]);

		nodeC1.setState("a");
		nodeC2.setState("a");
		nodeC3.setState("b");
		idxStateNode = nodeC1.getIdxState();
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nxC1[idxStateParents][idxStateNode]);

		nodeC1.setState("c");
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateNode = nodeC1.getIdxState();
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(2, nxC1[idxStateParents][idxStateNode]);

		// Check sufficient statistics C2
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateNode = nodeC2.getIdxState();
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(6, nxC2[idxStateParents][idxStateNode]);

		nodeC2.setState("a");
		nodeC3.setState("a");
		idxStateNode = nodeC2.getIdxState();
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(1, nxC2[idxStateParents][idxStateNode]);

		// Check sufficient statistics C3
		nodeC3.setState("a");
		idxStateParents = nodeC3.getIdxStateParents();
		idxStateNode = nodeC3.getIdxState();
		assertEquals(1, nxC3[idxStateParents][idxStateNode]);

		nodeC3.setState("b");
		idxStateParents = nodeC3.getIdxStateParents();
		idxStateNode = nodeC3.getIdxState();
		assertEquals(6, nxC3[idxStateParents][idxStateNode]);

		// Never seen state
		nodeC2.setState("b");
		idxStateNode = nodeC3.setState("c");
		assertEquals(-1, idxStateNode);
	}

	/**
	 * Check the parameters of a BN obtained by Bayesian estimation. The imaginary
	 * counts are all established to 0, so the result should be the same as with
	 * maximum likelihood estimation.
	 */
	@Test
	public void testBE_ImaginaryCountsOff() {
		// Establish imaginary counts
		double NxyPrior = 0.0;
		BNParameterLearningAlgorithm bnParameterLearning = new BNBayesianEstimation(NxyPrior);
		bnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testMLESufficientStatistics();
		// Parameters
		testMLEParameters();
	}

	/**
	 * Check the parameters of a BN obtained by Bayesian estimation. All the
	 * imaginary counts are more than 0.
	 */
	@Test
	public void testBE_ImaginaryCountsOn() {
		// Establish imaginary counts
		double NxPrior = 3.0;
		BNParameterLearningAlgorithm bnParameterLearning = new BNBayesianEstimation(NxPrior);
		bnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testBESufficientStatistic();
		// Parameters
		testBEParameters();
	}

	/**
	 * Check the estimation of the parameters of a given BN and dataset when using
	 * Bayesian estimation.
	 */
	public void testBEParameters() {
		// Retrieve nodes
		CPTNode nodeC1 = nodes.get(0);
		CPTNode nodeC2 = nodes.get(1);
		CPTNode nodeC3 = nodes.get(2);

		// Check parameters C1
		Integer idxFromStateNode = nodeC1.setState("a");
		nodeC2.setState("b");
		nodeC3.setState("b");
		Integer idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0.4, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC1.setState("b");
		nodeC2.setState("a");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0.333, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC1.setState("c");
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0.333, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Check parameters C2
		idxFromStateNode = nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(0.75, nodeC2.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC2.setState("a");
		nodeC3.setState("a");
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(0.571, nodeC2.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Check parameters C3
		idxFromStateNode = nodeC3.setState("a");
		idxStateParents = nodeC3.getIdxStateParents();
		assertEquals(0.307, nodeC3.getCP(idxStateParents, idxFromStateNode), 0.001);

		idxFromStateNode = nodeC3.setState("b");
		idxStateParents = nodeC3.getIdxStateParents();
		assertEquals(0.692, nodeC3.getCP(idxStateParents, idxFromStateNode), 0.001);

		// States not seen before
		// Never seen state of the node
		idxFromStateNode = nodeC1.setState("d");
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Never seen state of the parents
		idxFromStateNode = nodeC1.setState("a");
		nodeC2.setState("f");
		nodeC3.setState("b");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);

		// Never seen state of the node and its parents
		idxFromStateNode = nodeC1.setState("d");
		nodeC2.setState("f");
		nodeC3.setState("d");
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(0, nodeC1.getCP(idxStateParents, idxFromStateNode), 0.001);
	}

	/**
	 * Check the computation of the sufficient statistics of a given BN and dataset
	 * when using Bayesian estimation. They should include the defined
	 * hyperparameters.
	 */
	public void testBESufficientStatistic() {
		// Retrieve nodes
		CPTNode nodeC1 = nodes.get(0);
		CPTNode nodeC2 = nodes.get(1);
		CPTNode nodeC3 = nodes.get(2);
		// Sufficient statistics node C1
		double[][] nxC1 = nodeC1.getSufficientStatistics().getNx();
		// Sufficient statistics node C2
		double[][] nxC2 = nodeC2.getSufficientStatistics().getNx();
		// Sufficient statistics node C3
		double[][] nxC3 = nodeC3.getSufficientStatistics().getNx();

		// Check sufficient statistics C1
		nodeC1.setState("a");
		nodeC2.setState("b");
		nodeC3.setState("b");
		Integer idxStateNode = nodeC1.getIdxState();
		Integer idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(6, nxC1[idxStateParents][idxStateNode]);

		nodeC1.setState("a");
		nodeC2.setState("a");
		nodeC3.setState("b");
		idxStateNode = nodeC1.getIdxState();
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(3, nxC1[idxStateParents][idxStateNode]);

		nodeC1.setState("c");
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateNode = nodeC1.getIdxState();
		idxStateParents = nodeC1.getIdxStateParents();
		assertEquals(5, nxC1[idxStateParents][idxStateNode]);

		// Check sufficient statistics C2
		nodeC2.setState("b");
		nodeC3.setState("b");
		idxStateNode = nodeC2.getIdxState();
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(9, nxC2[idxStateParents][idxStateNode]);

		nodeC2.setState("a");
		nodeC3.setState("a");
		idxStateNode = nodeC2.getIdxState();
		idxStateParents = nodeC2.getIdxStateParents();
		assertEquals(4, nxC2[idxStateParents][idxStateNode]);

		// Check sufficient statistics C3
		nodeC3.setState("a");
		idxStateParents = nodeC3.getIdxStateParents();
		idxStateNode = nodeC3.getIdxState();
		assertEquals(4, nxC3[idxStateParents][idxStateNode]);

		nodeC3.setState("b");
		idxStateParents = nodeC3.getIdxStateParents();
		idxStateNode = nodeC3.getIdxState();
		assertEquals(9, nxC3[idxStateParents][idxStateNode]);

		// Never seen state
		nodeC2.setState("b");
		idxStateNode = nodeC3.setState("c");
		assertEquals(-1, idxStateNode);
	}

}
