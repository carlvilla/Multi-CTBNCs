package com.cig.mctbnc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		int numSequences = 5000;
		// Duration of the sequences
		int durationSequences = 10;
		// Destination path for the generated dataset
		String path = "src/main/resources/datasets/synthetic";
		
		// Define class variables
		CPTNode CV1 = new CPTNode("CV1", true, List.of("A", "B"));
		CPTNode CV2 = new CPTNode("CV2", true, List.of("A", "B"));
		CPTNode CV3 = new CPTNode("CV3", true, List.of("A", "B"));
		CPTNode CV4 = new CPTNode("CV4", true, List.of("A", "B"));
		CPTNode CV5 = new CPTNode("CV5", true, List.of("A", "B"));

		// Definition of the structure of the class subgraph
		CV1.setChild(CV2);
		CV1.setChild(CV3);
		CV1.setChild(CV4);
		CV3.setChild(CV5);
		CV4.setChild(CV5);
		BN<CPTNode> CS = new BN<CPTNode>(List.of(CV1, CV2, CV3, CV4, CV5));
		
		// Definition of the parameters of the Bayesian network (class subgraph)
		generateRandomConditionalDistributions(CS);

		// Define features
		CIMNode F1 = new CIMNode("F1", false, List.of("A", "B", "C"));
		CIMNode F2 = new CIMNode("F2", false, List.of("A", "B", "C"));
		CIMNode F3 = new CIMNode("F3", false, List.of("A", "B", "C"));
		CIMNode F4 = new CIMNode("F4", false, List.of("A", "B", "C"));

		// Definition of the structure of the bridge and feature subgraphs
		CV1.setChild(F1);		
		CV1.setChild(F2);
		CV3.setChild(F3);
		CV3.setChild(F2);
		CV3.setChild(F3);
		CV4.setChild(F4);
		CV5.setChild(F4);
		F1.setChild(F2);
		F2.setChild(F1);
		F3.setChild(F2);
		F4.setChild(F3);
		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(F1, F2, F3, F4), CS);
		
		// Definition of the parameters of the continuous time Bayesian network (feature
		// and bridge subgraph)
		generateRandomConditionalDistributions(FBS);

		// Define MCTBNC
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTBNC<CPTNode, CIMNode>(CS, FBS);

		// Sample sequences from the MCTBNC
		List<Sequence> sequences = new ArrayList<Sequence>();
		for (int i = 0; i < numSequences; i++)
			sequences.add(mctbnc.sample(durationSequences));

		// Save generated dataset
		Dataset dataset = new Dataset(sequences);
		MultipleCSVWriter.write(dataset, path);
	}

	/**
	 * Generate uniformly distributed random conditional probability tables for a
	 * Bayesian network.
	 * 
	 * @param bn Bayesian network
	 * 
	 */
	public static void generateRandomConditionalDistributions(BN<CPTNode> bn) {
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
					// IT IS ASSUMMED THAT THE VARIABLES ARE BINARY
					for (String valueNode : valuesNode) {
						State query = new State(stateParents.getEvents());
						query.addEvent(node.getName(), valueNode);
						CPT.put(query, prob);
						prob = 1 - prob;
					}
				}
			} else {
				// IT IS ASSUMMED THAT VARIABLES ARE BINARY
				double prob = Math.random();
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
	public static void generateRandomConditionalDistributions(CTBN<CIMNode> ctbn) {
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
