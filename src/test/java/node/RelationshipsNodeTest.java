package node;

import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the relationships between nodes are correctly defined.
 *
 * @author Carlos Villa Blanco
 */
public class RelationshipsNodeTest {
	static CPTNode nodeC1;
	static CPTNode nodeC2;
	static CPTNode nodeC3;
	static CIMNode nodeX1;
	static CIMNode nodeX2;
	static CIMNode nodeX3;

	/**
	 * Creates the nodes and establishes their relationships.
	 */
	@BeforeEach
	public void setUp() {
		// Define the nodes
		nodeC1 = new CPTNode("C1", List.of("A", "B", "C"));
		nodeC2 = new CPTNode("C2", List.of("A", "B", "C"));
		nodeC3 = new CPTNode("C3", List.of("A", "B", "C"));
		nodeX1 = new CIMNode("X1", List.of("A", "B", "C", "D"));
		nodeX2 = new CIMNode("X2", List.of("A", "B", "C"));
		nodeX3 = new CIMNode("X3", List.of("A", "B"));
		// Test with structure X3 <- C2 -> C3 <- C1 -> X1 -> X3 <-> X2
		// Structure class subgraph
		nodeC3.setParent(nodeC1);
		nodeC3.setParent(nodeC2);
		// Feature and bridge subgraphs
		nodeX1.setParent(nodeC1);
		nodeX3.setParent(nodeC2);
		nodeX2.setParent(nodeX3);
		nodeX3.setParent(nodeX1);
		nodeX3.setParent(nodeX2);
	}

	@Test
	@Order(1)
	void testChildrenNode() {
		// Node C1
		assertEquals(2, nodeC1.getChildren().size());
		nodeC1.setChild(nodeC2);
		assertEquals(3, nodeC1.getChildren().size());
		nodeC1.removeChild(nodeC2);
		assertEquals(2, nodeC1.getChildren().size());
	}

	@Test
	@Order(2)
	void testParentsNode() {
		// Node C1
		assertFalse(nodeC1.hasParents());
		assertEquals(0, nodeC1.getParents().size());
		nodeC1.setParent(nodeC2);
		assertTrue(nodeC1.hasParents());
		assertEquals(1, nodeC1.getParents().size());
	}

	@Test
	@Order(3)
	void testSetIdxState() {
		// Set and check states node X1
		nodeX1.setState(0);
		assertEquals(nodeX1.getState(), "A");
		nodeX1.setState(1);
		assertEquals(nodeX1.getState(), "B");
		nodeX1.setState(2);
		assertEquals(nodeX1.getState(), "C");
		nodeX1.setState(3);
		assertEquals(nodeX1.getState(), "D");
		// Set and check states node X3
		nodeX3.setState(0);
		assertEquals(nodeX3.getState(), "A");
		nodeX3.setState(1);
		assertEquals(nodeX3.getState(), "B");
		// If the state index does not exist, the state of the node is not changed
		nodeX3.setState(2);
		assertEquals(nodeX3.getState(), "B");
		// Set and check states node C1
		nodeC1.setState(0);
		assertEquals(nodeC1.getState(), "A");
		nodeC1.setState(1);
		assertEquals(nodeC1.getState(), "B");
		nodeC1.setState(2);
		assertEquals(nodeC1.getState(), "C");
	}

	@Test
	@Order(4)
	void testSetIdxStateParents() {
		// Set state parents X3 to C2 = "A", X1 = "A", X2 = "A"
		nodeX3.setStateParents(0);
		assertEquals(0, nodeX3.getIdxStateParents());
		assertEquals("A", nodeC2.getState());
		assertEquals("A", nodeX1.getState());
		assertEquals("A", nodeX2.getState());
		// Set state parents X3 to C2 = "B", X1 = "A", X2 = "A"
		nodeX3.setStateParents(1);
		assertEquals(nodeX3.getIdxStateParents(), 1);
		assertEquals(nodeC2.getState(), "B");
		assertEquals(nodeX1.getState(), "A");
		assertEquals(nodeX2.getState(), "A");
		// Set state parents X3 to C2 = "C", X1 = "A", X2 = "A"
		nodeX3.setStateParents(2);
		assertEquals(nodeX3.getIdxStateParents(), 2);
		assertEquals(nodeC2.getState(), "C");
		assertEquals(nodeX1.getState(), "A");
		assertEquals(nodeX2.getState(), "A");
		// Set state parents X3 to C2 = "A", X1 = "D", X2 = "A"
		nodeX3.setStateParents(9);
		assertEquals(nodeX3.getIdxStateParents(), 9);
		assertEquals(nodeC2.getState(), "A");
		assertEquals(nodeX1.getState(), "D");
		assertEquals(nodeX2.getState(), "A");
		// Set state parents X3 to C2 = "A", X1 = "A", X2 = "B"
		nodeX3.setStateParents(12);
		assertEquals(nodeX3.getIdxStateParents(), 12);
		assertEquals(nodeC2.getState(), "A");
		assertEquals(nodeX1.getState(), "A");
		assertEquals(nodeX2.getState(), "B");
		// Set state parents X3 to C2 = "C", X1 = "D", X2 = "C"
		nodeX3.setStateParents(35);
		assertEquals(nodeX3.getIdxStateParents(), 35);
		assertEquals(nodeC2.getState(), "C");
		assertEquals(nodeX1.getState(), "D");
		assertEquals(nodeX2.getState(), "C");
	}

	@Test
	@Order(5)
	void testGetIdxStateSubsetParents() {
		// Set state parents of C3 to C1 = "A", C2 = "A"
		nodeC3.setStateParents(0);
		assertEquals(0, nodeC3.getIdxStateParents(List.of("C1")));
		assertEquals(0, nodeC3.getIdxStateParents(List.of("C2")));
		// Set state parents of C3 to C1 = "B", C2 = "A"
		nodeC3.setStateParents(1);
		assertEquals(1, nodeC3.getIdxStateParents(List.of("C1")));
		assertEquals(0, nodeC3.getIdxStateParents(List.of("C2")));
		// Set state parents of C3 to C1 = "C", C2 = "B"
		nodeC3.setStateParents(5);
		assertEquals(2, nodeC3.getIdxStateParents(List.of("C1")));
		assertEquals(1, nodeC3.getIdxStateParents(List.of("C2")));
		// Set state parents of X3 to C2 = "A", X1 = "A", X2 = "A"
		nodeX3.setStateParents(0);
		assertEquals(0, nodeX3.getIdxStateParents(List.of("X1", "X2")));
		assertEquals(0, nodeX3.getIdxStateParents(List.of("X2", "X1")));
		assertEquals(0, nodeX3.getIdxStateParents(List.of("C2", "X2")));
		// Set state parents of X3 to C2 = "A", X1 = "D", X2 = "A"
		nodeX3.setStateParents(9);
		assertEquals(3, nodeX3.getIdxStateParents(List.of("X1", "X2")));
		assertEquals(3, nodeX3.getIdxStateParents(List.of("X2", "X1")));
		assertEquals(0, nodeX3.getIdxStateParents(List.of("C2", "X2")));
		// Set state parents of X3 to C2 = "A", X1 = "A", X2 = "B"
		nodeX3.setStateParents(12);
		assertEquals(4, nodeX3.getIdxStateParents(List.of("X1", "X2")));
		assertEquals(4, nodeX3.getIdxStateParents(List.of("X2", "X1")));
		assertEquals(3, nodeX3.getIdxStateParents(List.of("C2", "X2")));
		// Set state parents of X3 to C2 = "C", X1 = "D", X2 = "C"
		nodeX3.setStateParents(35);
		assertEquals(11, nodeX3.getIdxStateParents(List.of("X1", "X2")));
		assertEquals(11, nodeX3.getIdxStateParents(List.of("X2", "X1")));
		assertEquals(8, nodeX3.getIdxStateParents(List.of("C2", "X2")));
	}

	@Test
	@Order(6)
	void testNewModel() {
		// Create a new model from a BN and CTBN, and check that the relationships
		// between them do not change
		BN<CPTNode> bn = new BN<>(List.of(nodeC1, nodeC2, nodeC3));
		CTBN<CIMNode> ctbn = new CTBN<>(List.of(nodeX1, nodeX2, nodeX3), bn);
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(bn, ctbn);
		assertEquals(0, multiCTBNC.getNodeByName("C1").getParents().size());
		assertEquals(0, multiCTBNC.getNodeByName("C2").getParents().size());
		assertEquals(2, multiCTBNC.getNodeByName("C3").getParents().size());
		assertEquals(1, multiCTBNC.getNodeByName("X1").getParents().size());
		assertEquals(1, multiCTBNC.getNodeByName("X1").getChildren().size());
		assertEquals(1, multiCTBNC.getNodeByName("X2").getParents().size());
		assertEquals(1, multiCTBNC.getNodeByName("X2").getChildren().size());
		assertEquals(3, multiCTBNC.getNodeByName("X3").getParents().size());
		assertEquals(1, multiCTBNC.getNodeByName("X3").getChildren().size());
		// Changes in the initial BN and CTBN should not be reflected in the Multi-CTBNC
		// and vice versa
		bn.getNodeByName("C1").setChild(bn.getNodeByName("C2"));
		ctbn.getNodeByName("X1").setChild(ctbn.getNodeByName("X2"));
		assertEquals(1, bn.getNodeByName("C2").getParents().size());
		assertEquals(2, ctbn.getNodeByName("X2").getParents().size());
		assertEquals(0, multiCTBNC.getNodeByName("C2").getParents().size());
		assertEquals(1, multiCTBNC.getNodeByName("X2").getParents().size());
		multiCTBNC.getNodeByName("C2").setChild(multiCTBNC.getNodeByName("X2"));
		multiCTBNC.getNodeByName("C3").setChild(multiCTBNC.getNodeByName("X2"));
		assertEquals(2, ctbn.getNodeByName("X2").getParents().size());
		assertEquals(3, multiCTBNC.getNodeByName("X2").getParents().size());
	}

}