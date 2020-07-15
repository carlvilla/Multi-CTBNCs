package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.State;

/**
 * Extends the DiscreteNode class in order to store a CIM and the sufficient
 * statistics for a CTBN.
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 */
public class CIMNode extends DiscreteNode {


	public CIMNode(int index, String name, List<State> list) {
		super(index, name, list);
	}

}
