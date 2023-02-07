package es.upm.fi.cig.multictbnc.ctbn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the estimation of sufficient statistics and parameters of a CTBN.
 *
 * @author Carlos Villa Blanco
 */
@TestMethodOrder(OrderAnnotation.class)
class LearnParametersCTBNTest {
	static Dataset dataset;
	static List<CIMNode> nodes;

	/**
	 * Defines a dataset and the structure of a CTBN. Instead of learning the structure of the CTBN from the data,
	 * it is
	 * directly established over CPT nodes. This is done to know which structure we are working on during the tests.
	 * The
	 * CTBN will be formed by the three variables X1, X2 and X3, where X2 and X3 are the parents of X1, and X3 is the
	 * parent of X2.
	 */
	@BeforeAll
	public static void setUp() {
		// Define dataset
		List<String> nameClassVariables = new ArrayList<>();
		String nameTimeVariable = "Time";

		List<String[]> dataSequence1 = new ArrayList<>();
		dataSequence1.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence1.add(new String[]{"0.0", "a", "b", "b"});
		dataSequence1.add(new String[]{"0.1", "a", "b", "b"});
		dataSequence1.add(new String[]{"0.3", "a", "b", "a"});

		List<String[]> dataSequence2 = new ArrayList<>();
		dataSequence2.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence2.add(new String[]{"0.0", "a", "a", "b"});
		dataSequence2.add(new String[]{"0.4", "a", "b", "b"});
		dataSequence2.add(new String[]{"0.5", "b", "b", "b"});

		List<String[]> dataSequence3 = new ArrayList<>();
		dataSequence3.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence3.add(new String[]{"0.2", "a", "a", "a"});
		dataSequence3.add(new String[]{"0.7", "c", "a", "c"});

		List<String[]> dataSequence4 = new ArrayList<>();
		dataSequence4.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence4.add(new String[]{"0.0", "b", "b", "b"});
		dataSequence4.add(new String[]{"0.2", "a", "b", "b"});
		dataSequence4.add(new String[]{"0.5", "c", "b", "b"});

		List<String[]> dataSequence5 = new ArrayList<>();
		dataSequence5.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence5.add(new String[]{"0.0", "a", "b", "b"});
		dataSequence5.add(new String[]{"0.2", "b", "b", "b"});
		dataSequence5.add(new String[]{"0.7", "b", "b", "a"});

		List<String[]> dataSequence6 = new ArrayList<>();
		dataSequence6.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence6.add(new String[]{"0.0", "a", "b", "b"});
		dataSequence6.add(new String[]{"0.1", "a", "a", "a"});

		// Following sequence is added, but it will be irrelevant for the parameters
		List<String[]> dataSequence7 = new ArrayList<>();
		dataSequence7.add(new String[]{"Time", "X1", "X2", "X3"});
		dataSequence7.add(new String[]{"0.5", "c", "b", "b"});

		dataset = new Dataset(nameTimeVariable, nameClassVariables);
		dataset.addSequence(dataSequence1);
		dataset.addSequence(dataSequence2);
		dataset.addSequence(dataSequence3);
		dataset.addSequence(dataSequence4);
		dataset.addSequence(dataSequence5);
		dataset.addSequence(dataSequence6);
		dataset.addSequence(dataSequence7);

		// Define nodes
		// As learnt parameters are tested, whether a node is a class variable or not
		// is irrelevant.
		nodes = new ArrayList<>();
		for (String nameFeature : dataset.getNameVariables())
			nodes.add(new CIMNode(nameFeature, dataset.getPossibleStatesVariable(nameFeature)));

		// X2 and X3 are the parents of X1 and X3 is the parent of X2
		nodes.get(0).setParent(nodes.get(1));
		nodes.get(0).setParent(nodes.get(2));
		nodes.get(1).setParent(nodes.get(2));
	}

	/**
	 * Checks the parameters of a CTBN obtained by maximum likelihood estimation.
	 */
	@Test
	public void testMLE() {
		// Learn sufficient statistics and parameters given the nodes and the dataset
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNMaximumLikelihoodEstimation();
		ctbnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testMLESufficientStatisticMxy();
		testMLESufficientStatisticMx();
		testMLESufficientStatisticTx();
		// Parameters
		testMLEParameterQx();
		testMLEParameterOxx();
	}

	/**
	 * Checks the computation of the sufficient statistic Mxy for a certain CTBN and dataset when using maximum
	 * likelihood estimation.
	 */
	public void testMLESufficientStatisticMxy() {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);
		// Sufficient statistics Mxy of node X1
		double[][][] mxyX1 = nodeX1.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node X2
		double[][][] mxyX2 = nodeX2.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node X3
		double[][][] mxyX3 = nodeX3.getSufficientStatistics().getMxy();

		// Check sufficient statistics X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		int idxStateParents = nodeX1.getIdxStateParents();
		Integer idxToState = nodeX1.setState("b");
		assertEquals(2, mxyX1[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX1.setState("c");
		assertEquals(1, mxyX1[idxStateParents][idxFromState][idxToState]);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0, mxyX1[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX1.setState("c");
		assertEquals(1, mxyX1[idxStateParents][idxFromState][idxToState]);

		// Sufficient statistics X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("b");
		assertEquals(0, mxyX2[idxStateParents][idxFromState][idxToState]);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("a");
		assertEquals(1, mxyX2[idxStateParents][idxFromState][idxToState]);

		// Sufficient statistics X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("b");
		assertEquals(0, mxyX3[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX3.setState("c");
		assertEquals(1, mxyX3[idxStateParents][idxFromState][idxToState]);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("a");
		assertEquals(3, mxyX3[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX3.setState("c");
		assertEquals(0, mxyX3[idxStateParents][idxFromState][idxToState]);
	}

	/**
	 * Checks the computation of the sufficient statistic Mx for a certain CTBN and dataset when using maximum
	 * likelihood estimation.
	 */
	public void testMLESufficientStatisticMx() {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);
		// Sufficient statistics Mxy of node X1
		double[][] mxX1 = nodeX1.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node X2
		double[][] mxX2 = nodeX2.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node X3
		double[][] mxX3 = nodeX3.getSufficientStatistics().getMx();

		// Check sufficient statistics X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		int idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(3, mxX1[idxStateParents][idxFromState]);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(1, mxX1[idxStateParents][idxFromState]);

		// Check sufficient statistics X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, mxX2[idxStateParents][idxFromState]);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(1, mxX2[idxStateParents][idxFromState]);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, mxX2[idxStateParents][idxFromState]);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(1, mxX2[idxStateParents][idxFromState]);

		// Check sufficient statistics X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(1, mxX3[idxStateParents][idxFromState]);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(3, mxX3[idxStateParents][idxFromState]);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0, mxX3[idxStateParents][idxFromState]);
	}

	/**
	 * Checks the sufficient statistic Tx for a certain CTBN and dataset, which is related to the time that a variable
	 * stays in a certain state when using maximum likelihood estimation.
	 */
	public void testMLESufficientStatisticTx() {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);
		// Sufficient statistics Tx of node X1
		double[][] txX1 = nodeX1.getSufficientStatistics().getTx();
		// Sufficient statistics Tx of node X2
		double[][] txX2 = nodeX2.getSufficientStatistics().getTx();
		// Sufficient statistics Tx of node X3
		double[][] txX3 = nodeX3.getSufficientStatistics().getTx();

		// Check sufficient statistics X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		int idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0.5, txX1[idxStateParents][idxFromState], 0.01);

		// Last observation of a sequence. It never changes, so the duration is unknown.
		idxFromState = nodeX1.setState("c");
		nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, txX1[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(1.0, txX1[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, txX1[idxStateParents][idxFromState], 0.01);

		// Check sufficient statistics X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0.5, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0.4, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(1.7, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, txX2[idxStateParents][idxFromState], 0.01);

		// Check sufficient statistics X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0.5, txX3[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(2.1, txX3[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0, txX3[idxStateParents][idxFromState], 0.01);
	}

	/**
	 * Checks the parameter qx, i.e., the instantaneous probability of a variable leaving a certain state for another
	 * given the state of its parents.
	 */
	public void testMLEParameterQx() {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);

		// Check parameters X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		int idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(2, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(3, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX1.setState("b");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX1.setState("c");
		nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		// Check parameters X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(2.5, nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0.58, nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0, nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		// Check parameters X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(2, nodeX3.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(1.42, nodeX3.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0, nodeX3.getQx(idxStateParents, idxFromState), 0.01);

		// States not seen before
		// Never seen state of the node
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		// Never seen state of the parents
		idxFromState = nodeX1.setState("c");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		// Never seen state of the node and its parents
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);
	}

	/**
	 * Checks the parameter Oxx, i.e., the probability that a variable transitions from one state to another while its
	 * parents are in a certain state.
	 */
	public void testMLEParameterOxx() {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);

		// Check parameters X1
		int idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		int idxStateParents = nodeX1.getIdxStateParents();
		int idxToState = nodeX1.setState("b");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(1, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0.66, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(0.33, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Check parameters X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("b");
		assertEquals(0, nodeX2.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("a");
		assertEquals(1, nodeX2.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("a");
		assertEquals(0, nodeX2.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Check parameters X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("b");
		assertEquals(0, nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX3.setState("c");
		assertEquals(1, nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("a");
		assertEquals(1, nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX3.setState("c");
		assertEquals(0, nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("a");
		assertEquals(0, nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX3.setState("b");
		assertEquals(0, nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// States not seen before
		// Never seen state of the node
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("d");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("e");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX1.setState("f");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("d");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("e");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Never seen state of the parents
		idxFromState = nodeX1.setState("a");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Never seen state of the node and its parents
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("d");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("e");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX1.setState("f");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("d");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("e");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
	}

	/**
	 * Checks the parameters of a CTBN obtained by Bayesian estimation. The imaginary counts are all established to 0,
	 * so the result should be the same as with maximum likelihood estimation.
	 */
	@Test
	public void testBE_ImaginaryCountsOff() {
		// Establish imaginary counts
		double mHP = 0.0;
		double tHP = 0.0;
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNBayesianEstimation(mHP, tHP);
		ctbnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testMLESufficientStatisticMxy();
		testMLESufficientStatisticMx();
		testMLESufficientStatisticTx();
		// Parameters
		testMLEParameterQx();
		testMLEParameterOxx();
	}

	/**
	 * Checks the parameters of a CTBN obtained by Bayesian estimation. All the imaginary counts are more than 0.
	 */
	@Test
	public void testBE_ImaginaryCountsOn() {
		// Establish imaginary counts
		double mHP = 1.0;
		double tHP = 2.0;
		CTBNParameterLearningAlgorithm ctbnParameterLearning = new CTBNBayesianEstimation(mHP, tHP);
		ctbnParameterLearning.learn(nodes, dataset);
		// Sufficient statistics
		testBESufficientStatisticMxy(mHP);
		testBESufficientStatisticMx(mHP);
		testBESufficientStatisticTx(tHP);
		// Parameters
		testBEParameterQx(mHP, tHP);
		testBEParameterOxx(mHP);
	}

	/**
	 * Checks the computation of the sufficient statistic Mxy for a certain CTBN and dataset when using Bayesian
	 * estimation.
	 *
	 * @param mHP number of times a variable transitions from a certain state (hyperparameter)
	 */
	public void testBESufficientStatisticMxy(double mHP) {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);
		// Sufficient statistics Mxy of node X1
		double[][][] mxyX1 = nodeX1.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node X2
		double[][][] mxyX2 = nodeX2.getSufficientStatistics().getMxy();
		// Sufficient statistics Mxy of node X3
		double[][][] mxyX3 = nodeX3.getSufficientStatistics().getMxy();

		// Hyperparameters of the Dirichlet distributions
		double mxyHPX1 = mHP / (nodeX1.getNumStatesParents() * Math.pow(nodeX1.getNumStates(), 2));
		double mxyHPX2 = mHP / (nodeX2.getNumStatesParents() * Math.pow(nodeX2.getNumStates(), 2));
		double mxyHPX3 = mHP / (nodeX3.getNumStatesParents() * Math.pow(nodeX3.getNumStates(), 2));

		// Check sufficient statistics X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		int idxStateParents = nodeX1.getIdxStateParents();
		Integer idxToState = nodeX1.setState("b");
		assertEquals(2 + mxyHPX1, mxyX1[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX1.setState("c");
		assertEquals(1 + mxyHPX1, mxyX1[idxStateParents][idxFromState][idxToState]);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0 + mxyHPX1, mxyX1[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX1.setState("c");
		assertEquals(1 + mxyHPX1, mxyX1[idxStateParents][idxFromState][idxToState]);

		// Sufficient statistics X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("b");
		assertEquals(0 + mxyHPX2, mxyX2[idxStateParents][idxFromState][idxToState]);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("a");
		assertEquals(1 + mxyHPX2, mxyX2[idxStateParents][idxFromState][idxToState]);

		// Sufficient statistics X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("b");
		assertEquals(0 + mxyHPX3, mxyX3[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX3.setState("c");
		assertEquals(1 + mxyHPX3, mxyX3[idxStateParents][idxFromState][idxToState]);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("a");
		assertEquals(3 + mxyHPX3, mxyX3[idxStateParents][idxFromState][idxToState]);
		idxToState = nodeX3.setState("c");
		assertEquals(0 + mxyHPX3, mxyX3[idxStateParents][idxFromState][idxToState]);
	}

	/**
	 * Checks the computation of the sufficient statistic Mx for a certain CTBN and dataset when using Bayesian
	 * estimation.
	 *
	 * @param mHP number of times a variable transitions from a certain state (hyperparameter)
	 */
	public void testBESufficientStatisticMx(double mHP) {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);
		// Sufficient statistics Mxy of node X1
		double[][] mxX1 = nodeX1.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node X2
		double[][] mxX2 = nodeX2.getSufficientStatistics().getMx();
		// Sufficient statistics Mxy of node X3
		double[][] mxX3 = nodeX3.getSufficientStatistics().getMx();

		// Sum hyperparameters of the Dirichlet distribution given a certain state of
		// the node and parents
		double mxHPX1 = (mHP / (nodeX1.getNumStatesParents() * Math.pow(nodeX1.getNumStates(), 2))) *
				(nodeX1.getNumStates() - 1);
		double mxHPX2 = (mHP / (nodeX2.getNumStatesParents() * Math.pow(nodeX2.getNumStates(), 2))) *
				(nodeX2.getNumStates() - 1);
		double mxHPX3 = (mHP / (nodeX3.getNumStatesParents() * Math.pow(nodeX3.getNumStates(), 2))) *
				(nodeX3.getNumStates() - 1);

		// Check sufficient statistics X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		int idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(3 + mxHPX1, mxX1[idxStateParents][idxFromState]);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(1 + mxHPX1, mxX1[idxStateParents][idxFromState]);

		// Check sufficient statistics X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0 + mxHPX2, mxX2[idxStateParents][idxFromState]);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(1 + mxHPX2, mxX2[idxStateParents][idxFromState]);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0 + mxHPX2, mxX2[idxStateParents][idxFromState]);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(1 + mxHPX2, mxX2[idxStateParents][idxFromState]);

		// Check sufficient statistics X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(1 + mxHPX3, mxX3[idxStateParents][idxFromState]);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(3 + mxHPX3, mxX3[idxStateParents][idxFromState]);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0 + mxHPX3, mxX3[idxStateParents][idxFromState]);
	}

	/**
	 * Checks the sufficient statistic Tx for a certain CTBN and dataset, which is related to the time that a variable
	 * stays in a certain state when using Bayesian estimation.
	 *
	 * @param tHP time that a variable stays in a certain state (hyperparameter)
	 */
	public void testBESufficientStatisticTx(double tHP) {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);
		// Sufficient statistics Mxy of node X1
		double[][] txX1 = nodeX1.getSufficientStatistics().getTx();
		// Sufficient statistics Mxy of node X2
		double[][] txX2 = nodeX2.getSufficientStatistics().getTx();
		// Sufficient statistics Mxy of node X3
		double[][] txX3 = nodeX3.getSufficientStatistics().getTx();

		// Hyperparameters gamma distribution
		double txHPX1 = tHP / (nodeX1.getNumStatesParents() * nodeX1.getNumStates());
		double txHPX2 = tHP / (nodeX2.getNumStatesParents() * nodeX2.getNumStates());
		double txHPX3 = tHP / (nodeX3.getNumStatesParents() * nodeX3.getNumStates());

		// Check sufficient statistics X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		int idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0.5 + txHPX1, txX1[idxStateParents][idxFromState], 0.01);

		// Last observation of a sequence. It never changes, so the duration is unknown.
		idxFromState = nodeX1.setState("c");
		nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0 + txHPX1, txX1[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(1 + txHPX1, txX1[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("c"); // State that never occurs
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0 + txHPX1, txX1[idxStateParents][idxFromState], 0.01);

		// Check sufficient statistics X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0.5 + txHPX2, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0.4 + txHPX2, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0 + txHPX2, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0 + txHPX2, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(1.7 + txHPX2, txX2[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals(0 + txHPX2, txX2[idxStateParents][idxFromState], 0.01);

		// Check sufficient statistics X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0.5 + txHPX3, txX3[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(2.1 + txHPX3, txX3[idxStateParents][idxFromState], 0.01);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals(0 + txHPX3, txX3[idxStateParents][idxFromState], 0.01);
	}

	/**
	 * Checks the parameter qx, i.e., the instantaneous probability of a variable leaving a certain state for another
	 * given the state of its parents.
	 *
	 * @param mHP number of times a variable transitions from a certain state (hyperparameter)
	 * @param tHP time that a variable stays in a certain state (hyperparameter)
	 */
	public void testBEParameterQx(double mHP, double tHP) {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);

		// Sum hyperparameters of the Dirichlet distribution given a certain state of
		// the node
		// and parents
		double mxHPX1 = (mHP / (nodeX1.getNumStatesParents() * Math.pow(nodeX1.getNumStates(), 2))) *
				(nodeX1.getNumStates() - 1);
		double mxHPX2 = (mHP / (nodeX2.getNumStatesParents() * Math.pow(nodeX2.getNumStates(), 2))) *
				(nodeX2.getNumStates() - 1);
		double mxHPX3 = (mHP / (nodeX3.getNumStatesParents() * Math.pow(nodeX3.getNumStates(), 2))) *
				(nodeX3.getNumStates() - 1);
		// Hyperparameters gamma distribution
		double txHPX1 = tHP / (nodeX1.getNumStatesParents() * nodeX1.getNumStates());
		double txHPX2 = tHP / (nodeX2.getNumStatesParents() * nodeX2.getNumStates());
		double txHPX3 = tHP / (nodeX3.getNumStatesParents() * nodeX3.getNumStates());

		// Check parameters X1
		Integer idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		int idxStateParents = nodeX1.getIdxStateParents();
		assertEquals((1 + mxHPX1) / (0.5 + txHPX1), nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals((3 + mxHPX1) / (1 + txHPX1), nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX1.setState("b");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals((0 + mxHPX1) / (0 + txHPX1), nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX1.setState("c");
		nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals((0 + mxHPX1) / (0 + txHPX1), nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		// Check parameters X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals((0 + mxHPX2) / (0.5 + txHPX2), nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals((1 + mxHPX2) / (0.4 + txHPX2), nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals((0 + mxHPX2) / (0 + txHPX2), nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals((0 + mxHPX2) / (0 + txHPX2), nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals((1 + mxHPX2) / (1.7 + txHPX2), nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		assertEquals((0 + mxHPX2) / (0 + txHPX2), nodeX2.getQx(idxStateParents, idxFromState), 0.01);

		// Check parameters X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals((1 + mxHPX3) / (0.5 + txHPX3), nodeX3.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals((3 + mxHPX3) / (2.1 + txHPX3), nodeX3.getQx(idxStateParents, idxFromState), 0.01);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		assertEquals((0 + mxHPX3) / (0 + txHPX3), nodeX3.getQx(idxStateParents, idxFromState), 0.01);

		// States not seen before
		// Never seen state of the node
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("a");
		nodeX3.setState("c");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		// Never seen state of the parents
		idxFromState = nodeX1.setState("c");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);

		// Never seen state of the node and its parents
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		assertEquals(0, nodeX1.getQx(idxStateParents, idxFromState), 0.01);
	}

	/**
	 * Checks the parameter Oxx, i.e., the probability that a variable transitions from one state to another while its
	 * parents are in a certain state.
	 *
	 * @param mHP number of times a variable transitions from a certain state (hyperparameter)
	 */
	public void testBEParameterOxx(double mHP) {
		// Retrieve nodes
		CIMNode nodeX1 = nodes.get(0);
		CIMNode nodeX2 = nodes.get(1);
		CIMNode nodeX3 = nodes.get(2);

		// Hyperparameters of the Dirichlet distributions
		double mxyHPX1 = mHP / (nodeX1.getNumStatesParents() * Math.pow(nodeX1.getNumStates(), 2));
		double mxyHPX2 = mHP / (nodeX2.getNumStatesParents() * Math.pow(nodeX2.getNumStates(), 2));
		double mxyHPX3 = mHP / (nodeX3.getNumStatesParents() * Math.pow(nodeX3.getNumStates(), 2));

		// Sum the hyperparameters of the Dirichlet distribution given a certain state
		// of the node and parents
		double mxHPX1 = (mHP / (nodeX1.getNumStatesParents() * Math.pow(nodeX1.getNumStates(), 2))) *
				(nodeX1.getNumStates() - 1);
		double mxHPX2 = (mHP / (nodeX2.getNumStatesParents() * Math.pow(nodeX2.getNumStates(), 2))) *
				(nodeX2.getNumStates() - 1);
		double mxHPX3 = (mHP / (nodeX3.getNumStatesParents() * Math.pow(nodeX3.getNumStates(), 2))) *
				(nodeX3.getNumStates() - 1);

		// Check parameters X1
		int idxFromState = nodeX1.setState("a");
		nodeX2.setState("a");
		nodeX3.setState("a");
		int idxStateParents = nodeX1.getIdxStateParents();
		int idxToState = nodeX1.setState("b");
		assertEquals((0 + mxyHPX1) / (1 + mxHPX1), nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals((1 + mxyHPX1) / (1 + mxHPX1), nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX1.setState("a");
		nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals((2 + mxyHPX1) / (3 + mxHPX1), nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals((1 + mxyHPX1) / (3 + mxHPX1), nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Check parameters X2
		idxFromState = nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("b");
		assertEquals((0 + mxyHPX2) / (0 + mxHPX2), nodeX2.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("b");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("a");
		assertEquals((1 + mxyHPX2) / (1 + mxHPX2), nodeX2.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX2.setState("b");
		nodeX3.setState("c");
		idxStateParents = nodeX2.getIdxStateParents();
		idxToState = nodeX2.setState("a");
		assertEquals((0 + mxyHPX2) / (0 + mxHPX2), nodeX2.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Check parameters X3
		idxFromState = nodeX3.setState("a");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("b");
		assertEquals((0 + mxyHPX3) / (1 + mxHPX3), nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX3.setState("c");
		assertEquals((1 + mxyHPX3) / (1 + mxHPX3), nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX3.setState("b");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("a");
		assertEquals((3 + mxyHPX3) / (3 + mxHPX3), nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX3.setState("c");
		assertEquals((0 + mxyHPX3) / (3 + mxHPX3), nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		idxFromState = nodeX3.setState("c");
		idxStateParents = nodeX3.getIdxStateParents();
		idxToState = nodeX3.setState("a");
		assertEquals((0 + mxyHPX3) / (0 + mxHPX3), nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX3.setState("b");
		assertEquals((0 + mxyHPX3) / (0 + mxHPX3), nodeX3.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// States not seen before
		// Never seen state of the node
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("a");
		nodeX3.setState("a");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("f");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Never seen state of the parents
		idxFromState = nodeX1.setState("a");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("b");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

		// Never seen state of the node and its parents
		idxFromState = nodeX1.setState("d");
		nodeX2.setState("d");
		nodeX3.setState("e");
		idxStateParents = nodeX1.getIdxStateParents();
		idxToState = nodeX1.setState("f");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);
		idxToState = nodeX1.setState("c");
		assertEquals(0, nodeX1.getOxy(idxStateParents, idxFromState, idxToState), 0.01);

	}

}