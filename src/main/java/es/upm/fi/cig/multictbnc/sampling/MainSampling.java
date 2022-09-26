package es.upm.fi.cig.multictbnc.sampling;

import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Class to sample datasets from Multi-CTBNCs with provided or randomly generated structures.
 *
 * @author Carlos Villa Blanco
 */
public class MainSampling {
	private static final Logger logger = LogManager.getLogger(MainSampling.class);
	// Minimum and maximum values of the intensities
	private static int MININTENSITY = 1;
	private static int MAXINTENSITY = 3;
	// Extreme probabilities (only for binary class variables)
	private static boolean FORCEEXTREMEPROB = false;

	/**
	 * Application entry point. Arguments are expected to include the following elements and in the described order:
	 * (0)
	 * number of datasets to sample, (1) number of sequences, (2) duration of the sequences, (3) number of feature
	 * variables, (4) cardinality of feature variables, (5) number of class variables, (6) cardinality of feature
	 * variables, (7) probability of defining an arc in the class subgraph, (8) probability of defining an arc in the
	 * bridge subgraph, (9) probability of defining an arc in the feature subgraph (10) maximum number of feature
	 * variables that are parents of other feature variables, (11) {@code true} if different structures are used to
	 * sample every dataset, {@code false} if a unique structure is used, (12) path where the datasets will be stored,
	 * (13) adjacency matrix (optional, arguments 7 to 11 are ignored if used).
	 *
	 * @param args application command line arguments
	 */
	public static void main(String[] args) {
		double numDatasetsToSample = Integer.valueOf(args[0]);
		int numSequences = Integer.valueOf(args[1]);
		int durationSequences = Integer.valueOf(args[2]);
		int numFeatureVariables = Integer.valueOf(args[3]);
		int cardinalityFeatureVariables = Integer.valueOf(args[4]);
		int numClassVariables = Integer.valueOf(args[5]);
		int cardinalityClassVariables = Integer.valueOf(args[6]);
		double probabilityEdgeClassSubgraph = Double.valueOf(args[7]);
		double probabilityEdgeBridgeSubgraph = Double.valueOf(args[8]);
		double probabilityEdgeFeatureSubgraph = Double.valueOf(args[9]);
		int maxNumParentsFeatureVariables = Integer.valueOf(args[10]);
		boolean differentStructurePerDataset = Boolean.valueOf(args[11]);
		boolean[][] adjMatrix = retrieveAdjacencyMatrix(args);
		// Generate models and sample datasets from them
		for (int numDataset = 0; numDataset < numDatasetsToSample; numDataset++) {
			// Destination path for the generated dataset
			String destinationPath = defineDestinationPathDataset(args, numDatasetsToSample, numDataset);
			MultiCTBNC<CPTNode, CIMNode> multiCTBNC = DataSampler.generateModel(numFeatureVariables, numClassVariables,
					cardinalityFeatureVariables, cardinalityClassVariables, probabilityEdgeClassSubgraph,
					probabilityEdgeBridgeSubgraph, probabilityEdgeFeatureSubgraph, MININTENSITY, MAXINTENSITY,
					maxNumParentsFeatureVariables, differentStructurePerDataset, FORCEEXTREMEPROB, adjMatrix);
			System.out.println("Generating data from structure:");
			System.out.println(multiCTBNC);
			DataSampler.generateDataset(multiCTBNC, numSequences, durationSequences, destinationPath);
			saveInfoModel(multiCTBNC, cardinalityFeatureVariables, cardinalityClassVariables,
					probabilityEdgeClassSubgraph, probabilityEdgeBridgeSubgraph, probabilityEdgeFeatureSubgraph,
					MININTENSITY, MAXINTENSITY, numSequences, durationSequences, destinationPath);
		}
	}

	/**
	 * Define the destination path of a dataset.
	 *
	 * @param args                arguments
	 * @param numDatasetsToSample number of datasets to sample
	 * @param numDataset          number of the datasets whose destination path is being defined
	 * @return destination path
	 */
	private static String defineDestinationPathDataset(String[] args, double numDatasetsToSample, int numDataset) {
		if (numDatasetsToSample == 1) {
			// If one dataset is sampled, sequences are saved in the specified folder
			return args[12];
		}
		// If multiple datasets are sampled, sequences are saved in different folders in
		// the specified path
		String pathDataset = Paths.get(args[12], "dataset%d").toString();
		return String.format(pathDataset, numDataset);
	}

	/**
	 * Retrieve the adjacency matrix used to generate the models if provided
	 *
	 * @param args application command line arguments
	 * @return the provided adjacency matrix if it was found, {@code null} otherwise
	 */
	private static boolean[][] retrieveAdjacencyMatrix(String[] args) {
		boolean[][] adjMatrix = null;
		if (args.length > 13) {
			String flattenAdjMatrix = args[13];
			String[] rowsAdjMatrix = flattenAdjMatrix.split("//");
			adjMatrix = new boolean[rowsAdjMatrix.length][rowsAdjMatrix.length];
			for (int idxRow = 0; idxRow < rowsAdjMatrix.length; idxRow++) {
				String[] elementsRow = rowsAdjMatrix[idxRow].split(",");
				for (int idxElem = 0; idxElem < elementsRow.length; idxElem++) {
					adjMatrix[idxRow][idxElem] = Boolean.parseBoolean(elementsRow[idxElem]);
				}
			}
		}
		return adjMatrix;
	}

	/**
	 * Saves the information of the model used to sample the dataset.
	 *
	 * @param model           model
	 * @param destinationPath destination path
	 */
	private static void saveInfoModel(MultiCTBNC<CPTNode, CIMNode> model, int cardinalityFeatureVariables,
									  int cardinalityClassVariables, double probabilityEdgeClassSubgraph,
									  double probabilityEdgeBridgeSubgraph, double probabilityEdgeFeatureSubgraph,
									  int minIntensity, int maxIntensity, int numSequences, int durationSequences,
									  String destinationPath) {
		String fileName = destinationPath + "/info.txt";
		FileWriter file;
		try {
			file = new FileWriter(fileName);
			file.write("--- Information dataset ---\n");
			file.write("Cardinality feature variables: " + cardinalityFeatureVariables + "\n");
			file.write("Cardinality class variables: " + cardinalityClassVariables + "\n");
			file.write("Probability edge in class subgraph: " + probabilityEdgeClassSubgraph + "\n");
			file.write("Probability edge in bridge subgraph: " + probabilityEdgeBridgeSubgraph + "\n");
			file.write("Probability edge in feature subgraph: " + probabilityEdgeFeatureSubgraph + "\n");
			file.write("Num. sequences: " + numSequences + "\n");
			file.write("Duration sequences: " + durationSequences + "\n");
			file.write("Min. intensity: " + minIntensity + "\n");
			file.write("Max. intensity: " + maxIntensity + "\n");
			file.write(model.toString() + "\n");
			file.write("Adjacency matrix: " + Arrays.deepToString(model.getAdjacencyMatrix()) + "\n\n");
			file.write("++++++++CPTs++++++++\n");
			for (CPTNode cptnode : model.getNodesClassVariables()) {
				file.write(cptnode.getName() + "\n");
				file.write(Arrays.deepToString(cptnode.getCPT()) + "\n");
			}
			file.write("++++++++Intensities++++++++\n");
			for (CIMNode cimnode : model.getNodesFeatureVariables()) {
				file.write(cimnode.getName() + "\n");
				file.write(Arrays.deepToString(cimnode.getQx()) + "\n");
			}
			file.write("++++++++Probabilities++++++++\n");
			for (CIMNode cimnode : model.getNodesFeatureVariables()) {
				file.write(cimnode.getName() + "\n");
				file.write(Arrays.deepToString(cimnode.getOxy()) + "\n");
			}
			file.close();
		} catch (IOException ioe) {
			logger.error("There was an error while saving the information of the dataset");
		}
	}

}