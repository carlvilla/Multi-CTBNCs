package main.java.com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import main.java.com.cig.mctbnc.view.GraphDraw;

/**
 * @author Carlos Villa Blanco <carlos.villa@upm.es>
 *
 * Implements the Multi-dimensional Continuous Time Bayesian Network Classifier.
 * 
 */
public class MCTBNC {
	
	private List<Node> features;
	private List<Node> classVariables;
	
	private String initialBN;
	
	public MCTBNC(Dataset trainingDataset) {
		features = new ArrayList<Node>();
		classVariables = new ArrayList<Node>();
		
		for(String nameFeature:trainingDataset.getNameFeatures()) {
			features.add(new DiscreteNode(nameFeature, trainingDataset.getStatesVariable(nameFeature)));
		}
		
		for(String nameClassVariable:trainingDataset.getNameClassVariables()) {
			classVariables.add(new DiscreteNode(nameClassVariable, trainingDataset.getStatesVariable(nameClassVariable)));
		}
	}
	
	
	public void display() {
		Graph graph = new SingleGraph("Tutorial 1");
		
		for(Node classVariable: classVariables) {
			graph.addNode(classVariable.getName()).addAttribute("ui.label", classVariable.getName());
		}
		
		for(Node feature: features) {
			graph.addNode(feature.getName()).addAttribute("ui.label", feature.getName());
		}
		
		graph.display();
	}
	
			

}
