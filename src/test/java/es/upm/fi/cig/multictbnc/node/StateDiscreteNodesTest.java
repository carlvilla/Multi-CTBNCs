package es.upm.fi.cig.multictbnc.node;

import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the state of discrete-state nodes is correctly established.
 *
 * @author Carlos Villa Blanco
 */
@TestMethodOrder(OrderAnnotation.class)
public class StateDiscreteNodesTest {
	static CPTNode nodeC1;
	static CPTNode nodeC2;
	static CPTNode nodeC3;
	static CIMNode nodeX1;
	static CIMNode nodeX2;
	static CIMNode nodeX3;

	/**
	 * Creates the nodes and establishes their relationships.
	 */
	@BeforeAll
	public static void setUp() {
		// Define the nodes
		nodeC1 = new CPTNode("C1", List.of("a", "b"));
		nodeC2 = new CPTNode("C2", List.of("a", "b"));
		nodeC3 = new CPTNode("C3", List.of("a", "b"));
		nodeX1 = new CIMNode("X1", List.of("a", "b", "c"));
		nodeX2 = new CIMNode("X2", List.of("a", "b"));
		nodeX3 = new CIMNode("X3", List.of("a", "b"));
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
	void testInitialStateNode() {
		assertEquals("a", nodeC1.getState());
		assertEquals("a", nodeC2.getState());
		assertEquals("a", nodeC3.getState());
		assertEquals("a", nodeX1.getState());
		assertEquals("a", nodeX2.getState());
		assertEquals("a", nodeX3.getState());
	}

	@Test
	void testSetStateNode() {
		// Node C1
		// State: "a"
		nodeC1.setState("a");
		assertEquals(0, nodeC1.getIdxState());
		assertEquals("a", nodeC1.getState());
		// State: "b"
		nodeC1.setState("b");
		assertEquals(1, nodeC1.getIdxState());
		assertEquals("b", nodeC1.getState());
		// State that does not exist
		assertEquals(-1, nodeC1.setState("c"));

		// Node C2
		// State: "a"
		nodeC2.setState("a");
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// State: "b"
		nodeC2.setState("b");
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());

		// Node C3
		// State: "a"
		nodeC3.setState("a");
		assertEquals(0, nodeC3.getIdxState());
		assertEquals("a", nodeC3.getState());
		// State: "b"
		nodeC3.setState("b");
		assertEquals(1, nodeC3.getIdxState());
		assertEquals("b", nodeC3.getState());

		// Node X1
		// State: "a"
		nodeX1.setState("a");
		assertEquals(0, nodeX1.getIdxState());
		assertEquals("a", nodeX1.getState());
		// State: "b"
		nodeX1.setState("b");
		assertEquals(1, nodeX1.getIdxState());
		assertEquals("b", nodeX1.getState());
		// State: "c"
		nodeX1.setState("c");
		assertEquals(2, nodeX1.getIdxState());
		assertEquals("c", nodeX1.getState());

		// Node X2
		// State: "a"
		nodeX2.setState("a");
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		// State: "b"
		nodeX2.setState("b");
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());

		// Node X3
		// State: "a"
		nodeX3.setState("a");
		assertEquals(0, nodeX3.getIdxState());
		assertEquals("a", nodeX3.getState());
		// State: "b"
		nodeX3.setState("b");
		assertEquals(1, nodeX3.getIdxState());
		assertEquals("b", nodeX3.getState());
	}

	@Test
	void testSetStateNodeByIdx() {
		// Node C1
		// State: "a"
		nodeC1.setState(0);
		assertEquals(0, nodeC1.getIdxState());
		assertEquals("a", nodeC1.getState());
		// State: "b"
		nodeC1.setState(1);
		assertEquals(1, nodeC1.getIdxState());
		assertEquals("b", nodeC1.getState());
		// State that does not exist. The state is not changed
		nodeC1.setState(2);
		assertEquals(1, nodeC1.getIdxState());
		assertEquals("b", nodeC1.getState());

		// Node C2
		// State: "a"
		nodeC2.setState(0);
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// State: "b"
		nodeC2.setState(1);
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());

		// Node C3
		// State: "a"
		nodeC3.setState(0);
		assertEquals(0, nodeC3.getIdxState());
		assertEquals("a", nodeC3.getState());
		// State: "b"
		nodeC3.setState(1);
		assertEquals(1, nodeC3.getIdxState());
		assertEquals("b", nodeC3.getState());

		// Node X1
		// State: "a"
		nodeX1.setState(0);
		assertEquals(0, nodeX1.getIdxState());
		assertEquals("a", nodeX1.getState());
		// State: "b"
		nodeX1.setState(1);
		assertEquals(1, nodeX1.getIdxState());
		assertEquals("b", nodeX1.getState());
		// State: "c"
		nodeX1.setState(2);
		assertEquals(2, nodeX1.getIdxState());
		assertEquals("c", nodeX1.getState());

		// Node X2
		// State: "a"
		nodeX2.setState(0);
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		// State: "b"
		nodeX2.setState(1);
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());

		// Node X3
		// State: "a"
		nodeX3.setState(0);
		assertEquals(0, nodeX3.getIdxState());
		assertEquals("a", nodeX3.getState());
		// State: "b"
		nodeX3.setState(1);
		assertEquals(1, nodeX3.getIdxState());
		assertEquals("b", nodeX3.getState());
	}

	@Test
	void testStateParentsNode() {
		// Node C3
		// Parents -> C1: "a", C2: "a"
		nodeC3.setStateParents(0);
		assertEquals(0, nodeC1.getIdxState());
		assertEquals("a", nodeC1.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> C1: "b", C2: "a"
		nodeC3.setStateParents(1);
		assertEquals(1, nodeC1.getIdxState());
		assertEquals("b", nodeC1.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> C1: "a", C2: "b"
		nodeC3.setStateParents(2);
		assertEquals(0, nodeC1.getIdxState());
		assertEquals("a", nodeC1.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
		// Parents -> C1: "b", C2: "b"
		nodeC3.setStateParents(3);
		assertEquals(1, nodeC1.getIdxState());
		assertEquals("b", nodeC1.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());

		// Node X1
		// Parents -> C1: "a"
		nodeX1.setStateParents(0);
		assertEquals(0, nodeC1.getIdxState());
		assertEquals("a", nodeC1.getState());
		// Parents -> C1: "b"
		nodeX1.setStateParents(1);
		assertEquals(1, nodeC1.getIdxState());
		assertEquals("b", nodeC1.getState());

		// Node X2
		// Parents -> X3: "a"
		nodeX2.setStateParents(0);
		assertEquals(0, nodeX3.getIdxState());
		assertEquals("a", nodeX3.getState());
		// Parents -> X3: "b"
		nodeX2.setStateParents(1);
		assertEquals(1, nodeX3.getIdxState());
		assertEquals("b", nodeX3.getState());

		// Node X3
		// Parents -> X1: "a", X2: "a", C2: "a"
		nodeX3.setStateParents(0);
		assertEquals(0, nodeX3.getIdxStateParents());
		assertEquals(0, nodeX1.getIdxState());
		assertEquals("a", nodeX1.getState());
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> X1: "a", X2: "a", C2: "b"
		nodeX3.setStateParents(1);
		assertEquals(1, nodeX3.getIdxStateParents());
		assertEquals(0, nodeX1.getIdxState());
		assertEquals("a", nodeX1.getState());
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
		// Parents -> X1: "b", X2: "a", C2: "a"
		nodeX3.setStateParents(2);
		assertEquals(2, nodeX3.getIdxStateParents());
		assertEquals(1, nodeX1.getIdxState());
		assertEquals("b", nodeX1.getState());
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> X1: "b", X2: "a", C2: "b"
		nodeX3.setStateParents(3);
		assertEquals(3, nodeX3.getIdxStateParents());
		assertEquals(1, nodeX1.getIdxState());
		assertEquals("b", nodeX1.getState());
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
		// Parents -> X1: "c", X2: "a", C2: "a"
		nodeX3.setStateParents(4);
		assertEquals(4, nodeX3.getIdxStateParents());
		assertEquals(2, nodeX1.getIdxState());
		assertEquals("c", nodeX1.getState());
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> X1: "c", X2: "a", C2: "b"
		nodeX3.setStateParents(5);
		assertEquals(5, nodeX3.getIdxStateParents());
		assertEquals(2, nodeX1.getIdxState());
		assertEquals("c", nodeX1.getState());
		assertEquals(0, nodeX2.getIdxState());
		assertEquals("a", nodeX2.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
		// Parents -> X1: "a", X2: "b", C2: "a"
		nodeX3.setStateParents(6);
		assertEquals(6, nodeX3.getIdxStateParents());
		assertEquals(0, nodeX1.getIdxState());
		assertEquals("a", nodeX1.getState());
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> X1: "a", X2: "b", C2: "b"
		nodeX3.setStateParents(7);
		assertEquals(7, nodeX3.getIdxStateParents());
		assertEquals(0, nodeX1.getIdxState());
		assertEquals("a", nodeX1.getState());
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
		// Parents -> X1: "b", X2: "b", C2: "a"
		nodeX3.setStateParents(8);
		assertEquals(8, nodeX3.getIdxStateParents());
		assertEquals(1, nodeX1.getIdxState());
		assertEquals("b", nodeX1.getState());
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> X1: "b", X2: "b", C2: "b"
		nodeX3.setStateParents(9);
		assertEquals(9, nodeX3.getIdxStateParents());
		assertEquals(1, nodeX1.getIdxState());
		assertEquals("b", nodeX1.getState());
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
		// Parents -> X1: "c", X2: "b", C2: "a"
		nodeX3.setStateParents(10);
		assertEquals(10, nodeX3.getIdxStateParents());
		assertEquals(2, nodeX1.getIdxState());
		assertEquals("c", nodeX1.getState());
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());
		assertEquals(0, nodeC2.getIdxState());
		assertEquals("a", nodeC2.getState());
		// Parents -> X1: "c", X2: "b", C2: "b"
		nodeX3.setStateParents(11);
		assertEquals(11, nodeX3.getIdxStateParents());
		assertEquals(2, nodeX1.getIdxState());
		assertEquals("c", nodeX1.getState());
		assertEquals(1, nodeX2.getIdxState());
		assertEquals("b", nodeX2.getState());
		assertEquals(1, nodeC2.getIdxState());
		assertEquals("b", nodeC2.getState());
	}

}