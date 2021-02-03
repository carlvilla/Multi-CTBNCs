package com.cig.mctbnc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

		String path = "C:\\Users\\Carlos\\Desktop\\datasets\\synthetic\\Experiment10_extreme\\D5";

//				// ------------------- Experiment 1 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));

//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C3.setChild(C2);
//				C3.setChild(C4);
//				C3.setChild(C5);
//				C5.setChild(C4);

//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));

//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);

//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));

//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C1.setChild(X2);
//				C3.setChild(X2);
//				C3.setChild(X3);
//				C3.setChild(X4);
//				C5.setChild(X4);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 1 -------------------

//				// ------------------- Experiment 2 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C3.setChild(C2);
//				C3.setChild(C4);
//				C3.setChild(C5);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C1.setChild(X2);
//				C2.setChild(X2);
//				C3.setChild(X2);
//				C3.setChild(X3);
//				C3.setChild(X4);
//				C4.setChild(X4);
//				C5.setChild(X4);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 2 -------------------

//				// ------------------- Experiment 3 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C3.setChild(C2);
//				C3.setChild(C4);
//				C3.setChild(C5);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C2.setChild(X2);
//				C3.setChild(X3);
//				C4.setChild(X4);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 3 -------------------

//				// ------------------- Experiment 4 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C3.setChild(C2);
//				C3.setChild(C4);
//				C3.setChild(C5);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C3.setChild(X3);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 4 -------------------

//				// ------------------- Experiment 5 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C1.setChild(C4);
//				C5.setChild(C2);
//				C5.setChild(C3);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C1.setChild(X3);
//				C5.setChild(X3);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 5 -------------------

//				// ------------------- Experiment 6 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C1.setChild(C4);
//				C5.setChild(C2);
//				C5.setChild(C3);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C1.setChild(X3);
//				C2.setChild(X2);
//				C3.setChild(X3);
//				C4.setChild(X4);
//				C5.setChild(X3);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 6 -------------------

//				// ------------------- Experiment 7 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C3);
//				C2.setChild(C1);
//				C2.setChild(C3);
//				C4.setChild(C3);
//				C4.setChild(C5);
//				C5.setChild(C3);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C2.setChild(X2);
//				C2.setChild(X3);
//				C4.setChild(X3);
//				C4.setChild(X4);
//				C5.setChild(X5);
//				X1.setChild(X2);
//				X1.setChild(X3);
//				X2.setChild(X3);
//				X2.setChild(X4);
//				X4.setChild(X3);
//				X4.setChild(X5);
//				X5.setChild(X4);
//				// ------------------- Experiment 7 -------------------

//				// ------------------- Experiment 8 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C3.setChild(C2);
//				C3.setChild(C4);
//				C3.setChild(C5);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C1.setChild(X2);
//				C3.setChild(X2);
//				C3.setChild(X3);
//				C3.setChild(X4);
//				C5.setChild(X4);
//				C5.setChild(X5);
//				X1.setChild(X4);
//				X2.setChild(X1);
//				X3.setChild(X2);
//				X4.setChild(X3);
//				X5.setChild(X4);
//				// ------------------- Experiment 8 -------------------

//				// ------------------- Experiment 9 -------------------
//				// Define class variables
//				CPTNode C1 = new CPTNode("C1", true, List.of("C1_A", "C1_B"));
//				CPTNode C2 = new CPTNode("C2", true, List.of("C2_A", "C2_B"));
//				CPTNode C3 = new CPTNode("C3", true, List.of("C3_A", "C3_B"));
//				CPTNode C4 = new CPTNode("C4", true, List.of("C4_A", "C4_B"));
//				CPTNode C5 = new CPTNode("C5", true, List.of("C5_A", "C5_B"));
		//
//				// Definition of the structure of the class subgraph
//				C1.setChild(C2);
//				C1.setChild(C3);
//				C3.setChild(C2);
//				C3.setChild(C4);
//				C3.setChild(C5);
//				C5.setChild(C4);
		//
//				BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		//
//				// Definition of the parameters of the Bayesian network (class subgraph)
//				generateRandomCPD(CS, forceExtremeProb);
		//
//				// Define features
//				CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));
//				CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));
//				CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));
//				CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));
//				CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));
		//
//				// Definition of the structure of the bridge and feature subgraphs
//				C1.setChild(X1);
//				C1.setChild(X2);
//				C3.setChild(X2);
//				C3.setChild(X3);
//				C3.setChild(X4);
//				C5.setChild(X4);
//				C5.setChild(X5);
//				X1.setChild(X3);
//				X1.setChild(X4);
//				X2.setChild(X1);
//				X3.setChild(X2);
//				X3.setChild(X4);
//				X4.setChild(X5);
//				X5.setChild(X3);
//				// ------------------- Experiment 9 -------------------

		// ------------------- Experiment 10 -------------------
		// Define class variables
		CPTNode C1 = new CPTNode("C1", List.of("C1_A", "C1_B"), true);
		CPTNode C2 = new CPTNode("C2", List.of("C2_A", "C2_B"), true);
		CPTNode C3 = new CPTNode("C3", List.of("C3_A", "C3_B"), true);
		CPTNode C4 = new CPTNode("C4", List.of("C4_A", "C4_B"), true);

		// Definition of the structure of the class subgraph
		C2.setChild(C1);
		C2.setChild(C3);
		C4.setChild(C2);
		C4.setChild(C3);

		BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4));

		// Definition of the parameters of the Bayesian network (class subgraph)
		generateRandomCPD(CS, forceExtremeProb);

		// Define features
		CIMNode X1 = new CIMNode("X1", List.of("X1_A", "X1_B", "X1_C"), false);
		CIMNode X2 = new CIMNode("X2", List.of("X2_A", "X2_B", "X2_C"), false);
		CIMNode X3 = new CIMNode("X3", List.of("X3_A", "X3_B", "X3_C"), false);
		CIMNode X4 = new CIMNode("X4", List.of("X4_A", "X4_B", "X4_C"), false);
		CIMNode X5 = new CIMNode("X5", List.of("X5_A", "X5_B", "X5_C"), false);

		// Definition of the structure of the bridge and feature subgraphs
		C1.setChild(X1);
		C2.setChild(X1);
		C2.setChild(X2);
		C2.setChild(X3);
		C3.setChild(X3);
		C3.setChild(X4);
		C4.setChild(X5);
		X1.setChild(X2);
		X1.setChild(X3);
		X2.setChild(X3);
		X2.setChild(X4);
		X4.setChild(X3);
		X4.setChild(X5);
		X5.setChild(X4);
		// ------------------- Experiment 10 -------------------

		// Definition of the parameters of the continuous time Bayesian network (feature
		// and bridge subgraph)
		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);
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

		System.out.println("Dataset generated!");
	}

	/**
	 * Generate uniformly distributed random conditional probability tables for a
	 * Bayesian network. Variables are assumed binary.
	 * 
	 * @param bn               Bayesian network
	 * @param forceExtremeProb force the probabilities to be extreme (0 to 0.3 or
	 *                         0.7 to 1)
	 * 
	 */
	public static void generateRandomCPD(BN<CPTNode> bn, boolean forceExtremeProb) {
		// Iterate over all possible node to define their CPTs
		for (CPTNode node : bn.getNodes()) {
			// Number of states node and parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Initialice CPT
			double[][] CPT = new double[numStatesParents][numStates];
			// Iterate over states of the node and its parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Obtain probability of node state given the parents from uniform distribution
				double prob = forceExtremeProb ? ProbabilityUtil.extremeProbability() : Math.random();
				for (int idxState = 0; idxState < numStates; idxState++) {
					CPT[idxStateParents][idxState] = prob;
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
			// Number of states of the node and its parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Initialize parameters of the node
			double[][] Qx = new double[numStatesParents][numStates];
			double[][][] Oxx = new double[numStatesParents][numStates][numStates];
			// Iterate over states of the parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Define intensity matrix for the instantiation of the parents
				double[][] im = new double[numStates][numStates];
				// Iterate over elements of the intensity matrix
				for (int idxFromState = 0; idxFromState < numStates; idxFromState++) {
					for (int idxToState = 0; idxToState < numStates; idxToState++) {
						double intensity = minIntensity + (maxIntensity - minIntensity) * Math.random();
						if (idxToState != idxFromState)
							im[idxFromState][idxToState] = intensity;
					}
					// Intensity of leaving state "idxFromState" given the state of the parents
					im[idxFromState][idxFromState] = Util.sumRow(im, idxFromState);
					Qx[idxStateParents][idxFromState] = im[idxFromState][idxFromState];
					// Probability of transitioning from state "idxFromState" to any other given
					// the state of the parents
					for (int idxToState = 0; idxToState < numStates; idxToState++) {
						if (idxToState != idxFromState) {
							Oxx[idxStateParents][idxFromState][idxToState] = im[idxFromState][idxToState]
									/ im[idxFromState][idxFromState];
						}
					}
				}
			}
			// Set parameters on the node
			node.setParameters(Qx, Oxx);
		}
	}

}
