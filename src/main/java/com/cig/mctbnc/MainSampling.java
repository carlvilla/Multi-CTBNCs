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
import com.cig.mctbnc.nodes.DiscreteNode;
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
		int numSequences = 1000;
		// Duration of the sequences
		int durationSequences = 20;
		// Destination path for the generated dataset
		String path = "src/main/resources/datasets/Decidir/extreme/D1/";

		// Define class variables
		CPTNode CV1 = new CPTNode("CV1", true, List.of("CV1_A", "CV1_B"));
		CPTNode CV2 = new CPTNode("CV2", true, List.of("CV2_A", "CV2_B"));
		CPTNode CV3 = new CPTNode("CV3", true, List.of("CV3_A", "CV3_B"));
		CPTNode CV4 = new CPTNode("CV4", true, List.of("CV4_A", "CV4_B"));

		// Definition of the structure of the class subgraph
		CV2.setChild(CV1);
		CV2.setChild(CV3);
		CV4.setChild(CV2);
		CV4.setChild(CV3);

		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4));

		// Definition of the parameters of the Bayesian network (class subgraph)
		generateRandomCPD(CS);

		// Define features
		CIMNode X1 = new CIMNode("X1", false, List.of("X1_A", "X1_B", "X1_C"));// , "X1_D", "X1_E"));
		CIMNode X2 = new CIMNode("X2", false, List.of("X2_A", "X2_B", "X2_C"));// , "X2_D", "X2_E"));
		CIMNode X3 = new CIMNode("X3", false, List.of("X3_A", "X3_B", "X3_C"));// , "X3_D", "X3_E"));
		CIMNode X4 = new CIMNode("X4", false, List.of("X4_A", "X4_B", "X4_C"));// , "X4_D", "X4_E"));
		CIMNode X5 = new CIMNode("X5", false, List.of("X5_A", "X5_B", "X5_C"));// , "X5_D", "X5_E"));

		// Definition of the structure of the bridge and feature subgraphs
		CV1.setChild(X1);
		CV2.setChild(X1);
		CV2.setChild(X2);
		CV3.setChild(X3);
		CV3.setChild(X4);
		CV4.setChild(X5);
		X1.setChild(X2);
		X1.setChild(X3);
		X2.setChild(X3);
		X2.setChild(X4);
		X4.setChild(X3);
		X4.setChild(X5);
		X5.setChild(X4);
		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);

		// Definition of the parameters of the continuous time Bayesian network (feature
		// and bridge subgraph)
		generateRandomCPD(FBS);

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
	 * Bayesian network.
	 * 
	 * @param bn Bayesian network
	 * 
	 */
	public static void generateRandomCPD(BN<CPTNode> bn) {
		// Iterate over all possible node to define their CPTs
		for (CPTNode node : bn.getNodes()) {
			Map<State, Double> CPT = new HashMap<State, Double>();
			if (node.hasParents()) {
				// All possible combinations between the states of the parents
				List<State> statesParents = getStatesParents(node);
				String[] valuesNode = node.getStates().stream().map(state -> state.getValues()[0])
						.toArray(String[]::new);
				// Iterate over all possible states
				for (State stateParents : statesParents) {
					// Obtain probability of each node state given the parents from uniform
					// distribution
					double prob = Math.random();
					// Extreme probabilities
					// double prob = ProbabilityUtil.extremeProbability();

					// IT IS ASSUMMED BINARY VARIABLES
					for (String valueNode : valuesNode) {
						State query = new State(stateParents.getEvents());
						query.addEvent(node.getName(), valueNode);
						CPT.put(query, prob);
						prob = 1 - prob;
					}
				}
			} else {
				// IT IS ASSUMMED BINARY VARIABLES
				double prob = Math.random();
				// Extreme probabilities
				// double prob = ProbabilityUtil.extremeProbability();

				State state1 = node.getStates().get(0);
				State state2 = node.getStates().get(1);
				CPT.put(state1, prob);
				CPT.put(state2, 1 - prob);
			}
			node.setCPT(CPT);
		}
	}

	/**
	 * Generate uniformly distributed random conditional intensity tables for a
	 * continuous time Bayesian network.
	 * 
	 * @param ctbn continuous time Bayesian network
	 * 
	 */
	public static void generateRandomCPD(CTBN<CIMNode> ctbn) {
		// Min and max values of the intensities
		int min = 1;
		int max = 10;
		// The initial distribution of a CTBN is a Bayesian network
		for (CIMNode node : ctbn.getNodes()) {
			int numStates = node.getStates().size();
			List<State> statesNode = node.getStates();
			Map<State, Double> Qx = new HashMap<State, Double>();
			Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
			if (node.hasParents()) {
				// All possible combinations between the states of the parents
				List<State> statesParents = getStatesParents(node);
				for (State stateParents : statesParents) {
					// Define CIM
					double[][] cim = new double[numStates][numStates];
					for (int i = 0; i < numStates; i++) {
						for (int j = 0; j < numStates; j++) {
							double intensity = min + (max - min) * Math.random();
							if (i != j)
								cim[i][j] = intensity;
						}
						cim[i][i] = Util.sumRow(cim, i);
						// Current variable stays on state 'i' an amount of time that follows an
						// exponential distribution with parameter 'cim[i][i]' when the parents have
						// state 'stateParents'
						State query = new State(stateParents.getEvents());
						query.addEvent(node.getName(), statesNode.get(i).getValues()[0]);
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
			} else {
				double[][] cim = new double[numStates][numStates];
				for (int i = 0; i < numStates; i++) {
					for (int j = 0; j < numStates; j++) {
						double intensity = min + (max - min) * Math.random();
						if (i != j)
							cim[i][j] = intensity;
					}
					cim[i][i] = Util.sumRow(cim, i);
					// Current variable stays on state 'i' an amount of time that follows an
					// exponential distribution with parameter 'cim[i][i]'
					Qx.put(statesNode.get(i), cim[i][i]);
					// Define probabilities of the variable transitioning from state 'i' to any
					// other 'j'
					Map<State, Double> prob = new HashMap<State, Double>();
					for (int j = 0; j < numStates; j++)
						if (i != j)
							prob.put(statesNode.get(j), cim[i][j] / cim[i][i]);
					Oxx.put(statesNode.get(i), prob);
				}
			}
			// Set parameters on the node
			node.setParameters(Qx, Oxx);
		}
	}

	/**
	 * Obtain all state combinations of the parents of a discrete node.
	 * 
	 * @param node
	 * @return state combinations of the parents of the node
	 */
	private static List<State> getStatesParents(DiscreteNode node) {
		// All possible states of each parent of the node
		List<List<State>> statesEachParent = new ArrayList<List<State>>();
		for (int i = 0; i < node.getParents().size(); i++) {
			DiscreteNode parentNode = (DiscreteNode) node.getParents().get(i);
			statesEachParent.add(parentNode.getStates());
		}
		// All possible combinations between the states of the parents
		List<State> statesParents = Util.cartesianProduct(statesEachParent);
		return statesParents;
	}

}
