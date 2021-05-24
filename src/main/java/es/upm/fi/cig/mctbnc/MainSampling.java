package es.upm.fi.cig.mctbnc;

import java.util.ArrayList;
import java.util.List;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.data.representation.Sequence;
import es.upm.fi.cig.mctbnc.data.writer.MultipleCSVWriter;
import es.upm.fi.cig.mctbnc.models.BN;
import es.upm.fi.cig.mctbnc.models.CTBN;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.nodes.CIMNode;
import es.upm.fi.cig.mctbnc.nodes.CPTNode;
import es.upm.fi.cig.mctbnc.util.ProbabilityUtil;
import es.upm.fi.cig.mctbnc.util.Util;

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
		// Extreme probabilities (only for binary class variables)
		boolean forceExtremeProb = false;
		// Minimum and maximum values of the intensities
		int minIntensity = 0;
		int maxIntensity = 10;

		// Destination path for the generated dataset
		String path = "datasets/synthetic/Experiment1/D1";
		// Select the experiment from which the dataset will be generated
		String selectedExperiment = "1";

		// MCTBNC with the structure from the selected experiment
		MCTBNC<CPTNode, CIMNode> mctbnc = generateModelSelectedExperiment(selectedExperiment, forceExtremeProb,
				minIntensity, maxIntensity);

		if (mctbnc == null)
			// The selected experiment was not found
			return;

		// Sample sequences from the MCTBNC
		List<Sequence> sequences = new ArrayList<Sequence>();
		for (int i = 0; i < numSequences; i++)
			sequences.add(mctbnc.sample(durationSequences));

		// Create a dataset with the generated sequences
		Dataset dataset = new Dataset(sequences);
		// Save dataset in provided path
		MultipleCSVWriter.write(dataset, path);

		System.out.println("Dataset generated!");
	}

	/**
	 * Generate a MCTBNC for the selected experiment.
	 * 
	 * @param selectedExperiment experiment from which the model is defined
	 * @param forceExtremeProb   true to force the probabilities of the CPTs to be
	 *                           extreme (0 to 0.3 or 0.7 to 1)
	 * @param minIntensity       minimum value of the intensities of the CIMs
	 * @param maxIntensity       maximum value of the intensities of the CIMs
	 * @return a {@code MCTBNC}
	 */
	private static MCTBNC<CPTNode, CIMNode> generateModelSelectedExperiment(String selectedExperiment,
			boolean forceExtremeProb, int minIntensity, int maxIntensity) {
		// Define class variables
		CPTNode C1 = new CPTNode("C1", List.of("C1_A", "C1_B"), true);
		CPTNode C2 = new CPTNode("C2", List.of("C2_A", "C2_B"), true);
		CPTNode C3 = new CPTNode("C3", List.of("C3_A", "C3_B"), true);
		CPTNode C4 = new CPTNode("C4", List.of("C4_A", "C4_B"), true);
		CPTNode C5 = new CPTNode("C5", List.of("C5_A", "C5_B"), true);
		// Define feature variables
		CIMNode X1 = new CIMNode("X1", List.of("X1_A", "X1_B", "X1_C"), false);
		CIMNode X2 = new CIMNode("X2", List.of("X2_A", "X2_B", "X2_C"), false);
		CIMNode X3 = new CIMNode("X3", List.of("X3_A", "X3_B", "X3_C"), false);
		CIMNode X4 = new CIMNode("X4", List.of("X4_A", "X4_B", "X4_C"), false);
		CIMNode X5 = new CIMNode("X5", List.of("X5_A", "X5_B", "X5_C"), false);

		switch (selectedExperiment) {
		// ------------------- Experiment 1 (Figure 1a)-------------------
		// Define class variables
		case ("1"):
			// Definition of the structure of the class subgraph
			C1.setChild(C2);
			C1.setChild(C3);
			C3.setChild(C2);
			C3.setChild(C4);
			C3.setChild(C5);
			C5.setChild(C4);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C1.setChild(X2);
			C2.setChild(X2);
			C3.setChild(X2);
			C3.setChild(X3);
			C3.setChild(X4);
			C4.setChild(X4);
			C5.setChild(X4);
			C5.setChild(X5);
			X1.setChild(X2);
			X1.setChild(X3);
			X2.setChild(X3);
			X2.setChild(X4);
			X4.setChild(X3);
			X4.setChild(X5);
			X5.setChild(X4);
			break;
		// ------------------- Experiment 1 (Figure 1a)-------------------
		// ------------------- Experiment 2 (Figure 3a)-------------------
		case ("2"):
			// Definition of the structure of the class subgraph
			C1.setChild(C2);
			C1.setChild(C3);
			C3.setChild(C2);
			C3.setChild(C4);
			C3.setChild(C5);
			C5.setChild(C4);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C1.setChild(X2);
			C3.setChild(X2);
			C3.setChild(X3);
			C5.setChild(X4);
			C5.setChild(X5);
			X1.setChild(X2);
			X1.setChild(X3);
			X2.setChild(X3);
			X2.setChild(X4);
			X4.setChild(X3);
			X4.setChild(X5);
			X5.setChild(X4);
			break;
		// ------------------- Experiment 2 (Figure 3a)-------------------
		// ------------------- Experiment 3 (Figure 3b)-------------------
		case ("3"):
			// Definition of the structure of the class subgraph
			C1.setChild(C3);
			C2.setChild(C1);
			C2.setChild(C3);
			C4.setChild(C3);
			C4.setChild(C5);
			C5.setChild(C3);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C2.setChild(X2);
			C2.setChild(X3);
			C4.setChild(X3);
			C4.setChild(X4);
			C5.setChild(X5);
			X1.setChild(X2);
			X1.setChild(X3);
			X2.setChild(X3);
			X2.setChild(X4);
			X4.setChild(X3);
			X4.setChild(X5);
			X5.setChild(X4);
			break;
		// ------------------- Experiment 3 (Figure 3b)-------------------
		// ------------------- Experiment 4 (Figure 3c)-------------------
		case ("4"):
			// Definition of the structure of the class subgraph
			C1.setChild(C2);
			C2.setChild(C3);
			C3.setChild(C5);
			C4.setChild(C3);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C1.setChild(X2);
			C1.setChild(X3);
			C2.setChild(X2);
			C2.setChild(X3);
			C3.setChild(X4);
			C4.setChild(X5);
			C5.setChild(X4);
			X1.setChild(X4);
			X2.setChild(X1);
			X3.setChild(X2);
			X4.setChild(X3);
			X5.setChild(X4);
			break;
		// ------------------- Experiment 4 (Figure 3c)-------------------
		// ------------------- Experiment 5 (Figure 3d)-------------------
		case ("5"):
			// Definition of the structure of the class subgraph
			C1.setChild(C2);
			C2.setChild(C3);
			C2.setChild(C4);
			C3.setChild(C4);
			C4.setChild(C5);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C2.setChild(X1);
			C2.setChild(X2);
			C3.setChild(X3);
			C4.setChild(X4);
			C5.setChild(X4);
			C5.setChild(X4);
			C5.setChild(X5);
			X1.setChild(X3);
			X2.setChild(X1);
			X3.setChild(X2);
			X3.setChild(X4);
			X4.setChild(X5);
			X5.setChild(X3);
			break;
		// ------------------- Experiment 5 (Figure 3d)-------------------
		// ------------------- Experiment 6 (Figure 3e)-------------------
		case ("6"):
			// Definition of the structure of the class subgraph
			C2.setChild(C1);
			C3.setChild(C4);
			C4.setChild(C5);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C2.setChild(X3);
			C3.setChild(X2);
			C3.setChild(X4);
			C4.setChild(X3);
			C4.setChild(X5);
			X1.setChild(X2);
			X2.setChild(X4);
			X3.setChild(X4);
			X3.setChild(X5);
			X5.setChild(X4);
			break;
		// ------------------- Experiment 6 (Figure 3e)-------------------
		// ------------------- Experiment 7 (Figure 3f)-------------------
		case ("7"):
			// Definition of the structure of the class subgraph
			C2.setChild(C1);
			C3.setChild(C2);
			C4.setChild(C3);
			C4.setChild(C5);
			// Definition of the structure of the bridge and feature subgraphs
			C2.setChild(X1);
			C2.setChild(X2);
			C3.setChild(X2);
			C3.setChild(X3);
			C3.setChild(X4);
			C3.setChild(X5);
			C4.setChild(X4);
			X2.setChild(X1);
			X2.setChild(X4);
			X3.setChild(X2);
			X4.setChild(X3);
			X5.setChild(X3);
			break;
		// ------------------- Experiment 7 (Figure 3f)-------------------
		// ------------------- Experiment 8 (Figure 3g)-------------------
		case ("8"):
			// Definition of the structure of the class subgraph
			C1.setChild(C2);
			C1.setChild(C3);
			C3.setChild(C2);
			C4.setChild(C2);
			C4.setChild(C3);
			C4.setChild(C5);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C2.setChild(X1);
			C3.setChild(X2);
			C3.setChild(X3);
			C4.setChild(X4);
			C5.setChild(X5);
			X1.setChild(X2);
			X1.setChild(X3);
			X3.setChild(X4);
			X3.setChild(X5);
			break;
		// ------------------- Experiment 8 (Figure 3g)-------------------
		// ------------------- Experiment 9 (Figure 3h)-------------------
		case ("9"):
			// Empty class subgraph
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X1);
			C1.setChild(X3);
			C2.setChild(X2);
			C3.setChild(X3);
			C4.setChild(X3);
			C4.setChild(X5);
			C5.setChild(X4);
			C5.setChild(X5);
			X2.setChild(X4);
			X3.setChild(X1);
			X3.setChild(X2);
			X5.setChild(X3);
			break;
		// ------------------- Experiment 9 (Figure 3h)-------------------
		// ------------------- Experiment 10 (Figure 3i)-------------------
		case ("10"):
			// Definition of the structure of the class subgraph
			C2.setChild(C3);
			C2.setChild(C4);
			C3.setChild(C1);
			C4.setChild(C5);
			C5.setChild(C3);
			// Definition of the structure of the bridge and feature subgraphs
			C1.setChild(X2);
			C2.setChild(X1);
			C2.setChild(X3);
			C3.setChild(X4);
			C4.setChild(X3);
			C4.setChild(X5);
			C5.setChild(X4);
			X1.setChild(X4);
			X2.setChild(X1);
			X3.setChild(X2);
			X4.setChild(X5);
			X5.setChild(X3);
			break;
		// ------------------- Experiment 10 (Figure 3i)-------------------
		default:
			System.err.print(
					"The selected experiment was not found. Available datasets range from number 1 to 10 (for example, specify \"1\" to use structure from Figure 1a)");
			return null;
		}

		// Definition of the parameters of the Bayesian network (class subgraph)
		BN<CPTNode> CS = new BN<CPTNode>(List.of(C1, C2, C3, C4, C5));
		generateRandomCPTs(CS, forceExtremeProb);
		// Definition of the parameters of the continuous time Bayesian network (feature
		// and bridge subgraph)
		CTBN<CIMNode> FBS = new CTBN<CIMNode>(List.of(X1, X2, X3, X4, X5), CS);
		generateRandomCIMs(FBS, minIntensity, maxIntensity);

		// Define MCTBNC
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTBNC<CPTNode, CIMNode>(CS, FBS);
		return mctbnc;
	}

	/**
	 * Generate uniformly distributed random conditional probability tables for a
	 * Bayesian network.
	 * 
	 * @param bn               a Bayesian network
	 * @param forceExtremeProb true to force the probabilities to be extreme (0 to
	 *                         0.3 or 0.7 to 1) if the size of the sample space of
	 *                         the class variables is 2, false otherwise
	 * 
	 */
	private static void generateRandomCPTs(BN<CPTNode> bn, boolean forceExtremeProb) {
		// Iterate over all possible node to define their CPTs
		for (CPTNode node : bn.getNodes()) {
			// Number of states node and parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Initialice CPT
			double[][] CPT = new double[numStatesParents][numStates];
			// Iterate over states of the node and its parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				if (numStates == 2) {
					// Obtain probability of the node state given the parents from an uniform
					// distribution
					double prob = forceExtremeProb ? ProbabilityUtil.extremeProbability() : Math.random();
					for (int idxState = 0; idxState < numStates; idxState++) {
						CPT[idxStateParents][idxState] = prob;
						prob = 1 - prob;
					}
				} else {
					// Generate a random vector uniformly distributed over a numStates-dimensional
					// simplex (Rubinstein, 1982)
					double x[] = new double[numStates];
					double sum = 0.0;
					for (int idxState = 0; idxState < numStates; idxState++) {
						x[idxState] = -Math.log(1.0 - Math.random());
						sum += x[idxState];
					}
					for (int idxState = 0; idxState < numStates; idxState++)
						CPT[idxStateParents][idxState] = x[idxState] / sum;
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
	private static void generateRandomCIMs(CTBN<CIMNode> ctbn, double minIntensity, double maxIntensity) {
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