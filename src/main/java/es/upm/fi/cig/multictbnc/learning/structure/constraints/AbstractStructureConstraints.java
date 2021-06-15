package es.upm.fi.cig.multictbnc.learning.structure.constraints;

import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Contains common attributes and methods for classes that determine the
 * structure constraints of PFG.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractStructureConstraints implements StructureConstraints {

	@Override
	public boolean uniqueStructure() {
		// For most of the PGMs, there is no a unique structure. Those that have only
		// one, such as naive Bayes, have to override this method.
		return false;
	}

	@Override
	public void initializeStructure(PGM<? extends Node> pgm) {
		return;
	}

}
