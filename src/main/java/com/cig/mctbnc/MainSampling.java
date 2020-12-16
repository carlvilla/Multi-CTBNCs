package com.cig.mctbnc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.data.writer.MultipleCSVWriter;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.util.ProbabilityUtil;
import com.cig.mctbnc.util.Util;

/**
 * Class use to sample sequences from a MCTBNC.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MainSampling {

	/**
	 * Application entry point.
	 * 
	 * @param args application command line arguments
	 */
	public static void main(String[] args) {
		// Number of sequences to generate
		int numSequences = 10000;
		// Duration of the sequences
		int durationSequences = 10;
		// Extreme probabilities
		boolean forceExtremeProb = true;
		// Minimum and maximum values of the intensities
		int minIntensity = 0;
		int maxIntensity = 10;
		// Destination path for the generated dataset
		String path = "src/main/resources/datasets/Experiment12/extreme/D1";

// ------------------- Experiment 1 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV4.setChild(CV2);
//		CV4.setChild(CV3);
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV1.setChild(X1);
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV3.setChild(X3);
//		CV3.setChild(X4);
//		CV4.setChild(X5);
//		X1.setChild(X2);
//		X1.setChild(X3);
//		X2.setChild(X3);
//		X2.setChild(X4);
//		X4.setChild(X3);
//		X4.setChild(X5);
//		X5.setChild(X4);
//		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// ------------------- Experiment 1 -------------------

		// ------------------- Experiment 2 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV4.setChild(CV2);
//		CV4.setChild(CV3);
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV1.setChild(X1);
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV2.setChild(X3);
//		CV3.setChild(X3);
//		CV3.setChild(X4);
//		CV4.setChild(X5);
//		X1.setChild(X2);
//		X1.setChild(X3);
//		X2.setChild(X3);
//		X2.setChild(X4);
//		X4.setChild(X3);
//		X4.setChild(X5);
//		X5.setChild(X4);
//		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// ------------------- Experiment 2 -------------------

		// ------------------- Experiment 3 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV4.setChild(CV2);
//		CV4.setChild(CV3);
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV3.setChild(X3);
//		CV3.setChild(X4);
//		X1.setChild(X2);
//		X1.setChild(X3);
//		X2.setChild(X3);
//		X2.setChild(X4);
//		X4.setChild(X3);
//		X4.setChild(X5);
//		X5.setChild(X4);
//		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// ------------------- Experiment 3 -------------------

//		// ------------------- Experiment 4 -------------------
//		
//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV2.setChild(CV4);
//		CV3.setChild(CV4);
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV2.setChild(X3);
//		CV3.setChild(X3);
//		CV3.setChild(X4);
//		X1.setChild(X2);
//		X1.setChild(X3);
//		X2.setChild(X3);
//		X2.setChild(X4);
//		X4.setChild(X3);
//		X4.setChild(X5);
//		X5.setChild(X4);
//		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);
//		
//		// ------------------- Experiment 4 -------------------	

		// ------------------- Experiment 5 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//		CPTNode CV5 = new CPTNode("CV5", true, List.of("CV5_A", "CV5_B"));
//		CPTNode CV6 = new CPTNode("CV6", true, List.of("CV6_A", "CV6_B"));
//
//		// Definition of the structure of the class subgraph
//		CV1.setChild(CV2);
//		CV1.setChild(CV3);
//		CV1.setChild(CV5);
//		CV2.setChild(CV5);
//		CV3.setChild(CV4);
//		CV3.setChild(CV6);
//		CV4.setChild(CV6);
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4, CV5, CV6));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));// , "X1_D", "X1_E"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));// , "X2_D", "X2_E"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));// , "X3_D", "X3_E"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));// , "X4_D", "X4_E"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));// , "X5_D", "X5_E"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV1.setChild(X1);
//		CV2.setChild(X2);
//		CV3.setChild(X3);
//		CV4.setChild(X4);
//		CV5.setChild(X5);
//		CV6.setChild(X5);
//		X1.setChild(X2);
//		X1.setChild(X5);
//		X2.setChild(X1);
//		X3.setChild(X4);
//		X3.setChild(X5);
//		X4.setChild(X3);
//		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// ------------------- Experiment 5 -------------------

		// ------------------- Experiment 6 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//		CPTNode CV5 = new CPTNode("CV5", true, List.of("CV5_A", "CV5_B"));
//		CPTNode CV6 = new CPTNode("CV6", true, List.of("CV6_A", "CV6_B"));
//		CPTNode CV7 = new CPTNode("CV7", true, List.of("CV7_A", "CV7_B"));
//
//		// Definition of the structure of the class subgraph
//		CV1.setChild(CV2);
//		CV1.setChild(CV4);
//		CV3.setChild(CV2);
//		CV3.setChild(CV4);
//		CV5.setChild(CV4);
//		CV5.setChild(CV6);
//		CV7.setChild(CV6);
//		CV7.setChild(CV4);
//		
//
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4, CV5, CV6, CV7));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV4.setChild(X2);
//		CV4.setChild(X3);
//		CV5.setChild(X4);
//		CV5.setChild(X5);
//		X1.setChild(X2);
//		X2.setChild(X3);
//		X3.setChild(X4);
//		X4.setChild(X1);
//		X5.setChild(X4);
//		
//		
//		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// ------------------- Experiment 6 -------------------

		// ------------------- Experiment 7 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV1.setChild(CV3);
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV4.setChild(CV3);
//
//
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV4.setChild(X3);
//		CV4.setChild(X4);
//		X1.setChild(X2);
//		X2.setChild(X3);
//		X3.setChild(X4);
//		X4.setChild(X5);
//		X5.setChild(X4);

		// ------------------- Experiment 7 -------------------

		// ------------------- Experiment 8 -------------------
//		
//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV1.setChild(CV2);
//		CV1.setChild(CV3);
//		CV1.setChild(CV4);
//		CV2.setChild(CV3);
//		CV2.setChild(CV4);
//		CV3.setChild(CV4);
//
//
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV1.setChild(X1);
//		CV1.setChild(X2);
//		CV2.setChild(X3);
//		CV2.setChild(X4);
//		X1.setChild(X2);
//		X2.setChild(X3);
//		X3.setChild(X4);	
		// ------------------- Experiment 8 -------------------

//		// ------------------- Experiment 9 -------------------	
//		
//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//
//		// Definition of the structure of the class subgraph
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV2.setChild(CV4);
//		CV3.setChild(CV1);
//		CV3.setChild(CV4);
//		
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV3.setChild(X3);
//		CV3.setChild(X4);
//		X1.setChild(X2);
//		X2.setChild(X3);
//		X3.setChild(X4);	
//		// ------------------- Experiment 9 -------------------	

		// ------------------- Experiment 10 -------------------

//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//		CPTNode CV5 = new CPTNode("CV5", true, List.of("CV5_A", "CV5_B"));
//
//		// Definition of the structure of the class subgraph
//		CV2.setChild(CV1);
//		CV2.setChild(CV3);
//		CV2.setChild(CV5);
//		CV3.setChild(CV4);
//		CV4.setChild(CV1);
//		CV4.setChild(CV5);
//	
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4, CV5));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV2.setChild(X1);
//		CV2.setChild(X2);
//		CV3.setChild(X3);
//		CV4.setChild(X4);
//		CV4.setChild(X5);
//		X1.setChild(X2);
//		X2.setChild(X3);
//		X3.setChild(X4);
//		X4.setChild(X5);
//		X5.setChild(X4);
		// ------------------- Experiment 10 -------------------

//		// ------------------- Experiment 11 -------------------	
//		
//		// Define class variables
//		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
//		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
//		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
//		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
//		CPTNode CV5 = new CPTNode("CV5", true, List.of("CV5_A", "CV5_B"));
//
//		// Definition of the structure of the class subgraph
//		CV1.setChild(CV2);
//		CV1.setChild(CV3);
//		CV3.setChild(CV2);
//		CV3.setChild(CV4);
//		CV3.setChild(CV5);
//		CV5.setChild(CV4);
//	
//		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4, CV5));
//
//		// Definition of the parameters of the Bayesian network (class subgraph)
//		generateRandomCPD(CS, forceExtremeProb);
//
//		// Define features
//		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
//
//		// Definition of the structure of the bridge and feature subgraphs
//		CV1.setChild(X1);
//		CV1.setChild(X2);
//		CV3.setChild(X3);
//		CV5.setChild(X4);
//		CV5.setChild(X5);
//		X1.setChild(X2);
//		X1.setChild(X3);
//		X2.setChild(X3);
//		X2.setChild(X4);
//		X4.setChild(X3);
//		X4.setChild(X5);
//		X5.setChild(X4);
//		
//		// ------------------- Experiment 11 -------------------

		// ------------------- Experiment 12 -------------------

		// Define class variables
		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));
		CPTNode CV5 = new CPTNode("CV5", true, List.of("CV5_A", "CV5_B"));

		// Definition of the structure of the class subgraph
		CV1.setChild(CV2);
		CV1.setChild(CV3);
		CV3.setChild(CV2);
		CV3.setChild(CV4);
		CV3.setChild(CV5);
		CV5.setChild(CV4);

		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4, CV5));

		// Definition of the parameters of the Bayesian network (class subgraph)
		generateRandomCPD(CS, forceExtremeProb);

		// Define features
		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));

		// Definition of the structure of the bridge and feature subgraphs
		CV1.setChild(X1);
		CV1.setChild(X2);
		CV3.setChild(X2);
		CV3.setChild(X3);
		CV3.setChild(X4);
		CV5.setChild(X4);
		CV5.setChild(X5);
		X1.setChild(X2);
		X1.setChild(X3);
		X2.setChild(X3);
		X2.setChild(X4);
		X4.setChild(X3);
		X4.setChild(X5);
		X5.setChild(X4);

		// ------------------- Experiment 12 -------------------

		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// Definition of the parameters of the continuous time Bayesian network (feature
		// and bridge subgraph)
		generateRandomCPD(FBS, minIntensity, maxIntensity);

		// Define MCTBNC
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTBNC<CPTNode, CIMNode>(CS, FBS);

		// Sample sequences from the MCTBNC
		List<Sequence> sequences = new ArrayList<Sequence>();
		for (int i = 0; i < numSequences; i++)
			sequences.add(mctbnc.sample(durationSequences));

		// Save generated dataset
		Dataset dataset = new Dataset(sequences);

		State[] states = dataset.getStatesClassVariables();
		Set<State> statesUniq = new HashSet<State>();
		for (State state : states) {
			statesUniq.add(state);
		}

		MultipleCSVWriter.write(dataset, path);
	}

	/**
	 * Generate uniformly distributed random conditional probability tables for a
	 * Bayesian network. IT IS ASSUMMED BINARY VARIABLES.
	 * 
	 * @param bn               Bayesian network
	 * @param forceExtremeProb force the probabilities to be extreme (0 to 0.3 or
	 *                         0.7 to 1)
	 * 
	 */
	public static void generateRandomCPD(BN<CPTNode> bn, boolean forceExtremeProb) {
		// Iterate over all possible node to define their CPTs
		for (CPTNode node : bn.getNodes()) {
			Map<State, Double> CPT = new HashMap<State, Double>();
			// All possible combinations between the states of the parents
			List<State> statesParents = node.getStatesParents();
			List<State> statesNode = node.getStates();
			// String[] valuesNode = node.getStates().stream().map(state ->
			// state.getValues()[0]).toArray(String[]::new);
			// Iterate over all possible states
			for (State stateParents : statesParents) {
				// Obtain probability of each node state given the parents from uniform
				// distribution
				double prob = forceExtremeProb ? ProbabilityUtil.extremeProbability() : Math.random();
				for (State stateNode : statesNode) {
					State query = new State(stateParents.getEvents());
					query.addEvents(stateNode.getEvents());
					CPT.put(query, prob);
					prob = 1 - prob;
				}
			}
			node.setCPT(CPT);
		}
	}

	/**
	 * Generate uniformly distributed random conditional intensity tables for a
	 * continuous time Bayesian network.
	 * 
	 * @param ctbn         continuous time Bayesian network
	 * @param minIntensity minimum value of the intensities
	 * @param maxIntensity maximum value of the intensities
	 * 
	 */
	public static void generateRandomCPD(CTBN<CIMNode> ctbn, double minIntensity, double maxIntensity) {
		// The initial distribution of a CTBN is a Bayesian network
		for (CIMNode node : ctbn.getNodes()) {
			List<State> statesNode = node.getStates();
			int numStates = node.getStates().size();
			Map<State, Double> Qx = new HashMap<State, Double>();
			Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
			// All possible combinations between the states of the parents
			List<State> statesParents = node.getStatesParents(); // getStatesParents(node);
			for (State stateParents : statesParents) {
				// Define CIM
				double[][] cim = new double[numStates][numStates];
				for (int i = 0; i < numStates; i++) {
					for (int j = 0; j < numStates; j++) {
						double intensity = minIntensity + (maxIntensity - minIntensity) * Math.random();
						if (i != j)
							cim[i][j] = intensity;
					}
					cim[i][i] = Util.sumRow(cim, i);
					// Current variable stays on state 'i' an amount of time that follows an
					// exponential distribution with parameter 'cim[i][i]' when the parents have
					// state 'stateParents'
					State query = new State(stateParents.getEvents());
					query.addEvents(statesNode.get(i).getEvents());
					Qx.put(query, cim[i][i]);
					// Define probabilities of the variable transitioning from state 'i' to any
					// other 'j' given the current state of the parents 'stateParents'
					Map<State, Double> prob = new HashMap<State, Double>();
					for (int j = 0; j < numStates; j++)
						if (i != j)
							prob.put(statesNode.get(j), cim[i][j] / cim[i][i]);
					Oxx.put(query, prob);
				}
			}
			// Set parameters on the node
			node.setParameters(Qx, Oxx);
		}
	}

}
