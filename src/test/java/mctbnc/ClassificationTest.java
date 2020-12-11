package mctbnc;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

class ClassificationTest {

	@BeforeAll
	static void setUp() {
		// Define class variables
		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));

		// Definition of the structure of the class subgraph
		CV1.setChild(CV2);

		Map<State, Double> CPTCV1 = new HashMap<State, Double>();
		
		State state1 = new State();
		state1.addEvent("CV1", "CV1_A");
		State state2 = new State();
		state2.addEvent("CV1", "CV1_B");
		CPTCV1.put(state1, 0.6);
		CPTCV1.put(state2, 0.4);
		
		CV1.setCPT(CPTCV1);

		
		Map CPTCV2 = new HashMap<State, Double>();
		CV2.setCPT(CPTCV2);

		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2));

		// Definition of the parameters of the Bayesian network (class subgraph)

		// Define features
		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B"));

		// Definition of the structure of the bridge and feature subgraphs
		CV1.setChild(X1);
		CV2.setChild(X2);
		X1.setChild(X2);

		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2), CS);
	}

	@Test
	void testClassification() {
		//fail("Not yet implemented");
	}

}
