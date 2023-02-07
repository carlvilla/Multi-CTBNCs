/**
 * @author Carlos Villa Blanco
 */
module es.upm.fi.cig.multictbnc {
	requires com.google.common;
	requires commons.lang3;
	requires commons.math3;
	requires gs.core;
	requires gs.ui.javafx;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.jfxtras.styles.jmetro;
	requires opencsv;
	requires org.apache.logging.log4j;
	requires org.apache.poi.ooxml;
	requires org.apache.poi.poi;
	requires org.controlsfx.controls;

	opens es.upm.fi.cig.multictbnc to javafx.fxml;
	opens es.upm.fi.cig.multictbnc.gui to javafx.fxml;

	exports es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC;
	exports es.upm.fi.cig.multictbnc.learning.structure;
	exports es.upm.fi.cig.multictbnc.learning.parameters.ctbn;
	exports es.upm.fi.cig.multictbnc.gui;
	exports es.upm.fi.cig.multictbnc.models;
	exports es.upm.fi.cig.multictbnc.learning.structure.hybrid;
	exports es.upm.fi.cig.multictbnc.data.writer;
	exports es.upm.fi.cig.multictbnc.writers.classification;
	exports es.upm.fi.cig.multictbnc.models.submodels;
	exports es.upm.fi.cig.multictbnc.services;
	exports es.upm.fi.cig.multictbnc.learning.parameters;
	exports es.upm.fi.cig.multictbnc.classification;
	exports es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn;
	exports es.upm.fi.cig.multictbnc;
	exports es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation;
	exports es.upm.fi.cig.multictbnc.sampling;
	exports es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores;
	exports es.upm.fi.cig.multictbnc.learning.structure.hybrid.hillclimbing;
	exports es.upm.fi.cig.multictbnc.tasks;
	exports es.upm.fi.cig.multictbnc.writers.performance;
	exports es.upm.fi.cig.multictbnc.learning.structure.constraints.BN;
	exports es.upm.fi.cig.multictbnc.learning.structure.hybrid.PC;
	exports es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing;
	exports es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC;
	exports es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn;
	exports es.upm.fi.cig.multictbnc.learning.structure.optimisation.tabusearch;
	exports es.upm.fi.cig.multictbnc.learning;
	exports es.upm.fi.cig.multictbnc.learning.parameters.bn;
	exports es.upm.fi.cig.multictbnc.nodes;
	exports es.upm.fi.cig.multictbnc.performance;
	exports es.upm.fi.cig.multictbnc.data.reader;
	exports es.upm.fi.cig.multictbnc.experiments;
	exports es.upm.fi.cig.multictbnc.exceptions;
	exports es.upm.fi.cig.multictbnc.data.representation;
	exports es.upm.fi.cig.multictbnc.learning.structure.constraints;
	exports es.upm.fi.cig.multictbnc.util;
}