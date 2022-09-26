package es.upm.fi.cig.multictbnc.data.representation;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a time series dataset, which stores sequences and provides methods to access and modify their
 * information.
 *
 * @author Carlos Villa Blanco
 */
public class Dataset {
	private static final Logger logger = LogManager.getLogger(Dataset.class);
	private List<Sequence> sequences;
	private String nameTimeVariable;
	private List<String> nameFeatureVariables;
	private List<String> nameClassVariables;
	// A list of class variables that should be ignored
	private List<String> ignoredClassVariables;
	// Store the possible states of a variable to avoid recomputations
	private Map<String, List<String>> statesVariables;

	/**
	 * Creates an empty dataset with the names of the time variable and class variables.
	 *
	 * @param nameTimeVariable   name of the time variable
	 * @param nameClassVariables names of the class variables
	 */
	public Dataset(String nameTimeVariable, List<String> nameClassVariables) {
		this.sequences = new ArrayList<>();
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		this.statesVariables = new HashMap<>();
	}

	/**
	 * Creates a dataset with a list of sequences.
	 *
	 * @param sequences list of {@code Sequence}
	 */
	public Dataset(List<Sequence> sequences) {
		this.sequences = sequences;
		this.nameFeatureVariables = sequences.get(0).getNameFeatureVariables();
		this.nameClassVariables = sequences.get(0).getNameClassVariables();
		this.nameTimeVariable = sequences.get(0).getNameTimeVariable();
		this.statesVariables = new HashMap<>();
	}

	/**
	 * Receives a list of Strings (a sequence) from which a {@code Sequence} is created and adds it to the dataset. The
	 * first array of Strings has to contain the name of the variables.
	 *
	 * @param data list of Strings (a sequence) where the first array contains the name of the variables
	 * @return <code>true</code> if the sequence was successfully added to the
	 * dataset; <code>false</code> otherwise.
	 */
	public boolean addSequence(List<String[]> data) {
		return addSequence(data, "No file path");
	}

	/**
	 * Receives a list of Strings (a sequence) and the path of the file from which it was extracted. Then, it creates a
	 * {@code Sequence} and adds it to the dataset. The first array of Strings representing the sequence has to contain
	 * the name of the variables.
	 *
	 * @param data     list of Strings (a sequence) where the first array contains the name of the variables
	 * @param filePath path of the file from which the sequence was extracted
	 * @return <code>true</code> if the sequence was successfully added to the
	 * dataset; <code>false</code> otherwise.
	 */
	public boolean addSequence(List<String[]> data, String filePath) {
		try {
			Sequence sequence = createSequence(data);
			sequence.setFilePath(filePath);
			this.sequences.add(sequence);
			return true;
		} catch (ErroneousSequenceException ese) {
			logger.warn("Sequence {} not added. {}", filePath, ese.getMessage());
			return false;
		}
	}

	/**
	 * Receives a {@code Sequence} to add it to the dataset.
	 *
	 * @param sequence a {@code Sequence}
	 * @return <code>true</code> if the sequence was successfully added to the
	 * dataset; <code>false</code> otherwise.
	 */
	public boolean addSequence(Sequence sequence) {
		try {
			sequence.setFilePath("");
			this.sequences.add(sequence);
		} catch (NullPointerException npe) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the name of all the variables, including the time variable.
	 *
	 * @return name of all the variables
	 */
	public List<String> getNameAllVariables() {
		List<String> nameVariables = new ArrayList<>();
		nameVariables.add(getNameTimeVariable());
		nameVariables.addAll(getNameVariables());
		return nameVariables;
	}

	/**
	 * Returns the name of the class variables. Those class variables that should be ignored are filtered.
	 *
	 * @return list with the names of the class variables
	 */
	public List<String> getNameClassVariables() {
		if (!(this.ignoredClassVariables == null || this.ignoredClassVariables.isEmpty()))
			return this.nameClassVariables.stream().filter(var -> !this.ignoredClassVariables.contains(var)).collect(
					Collectors.toList());
		return this.nameClassVariables;
	}

	/**
	 * Returns the names of the feature variables.
	 *
	 * @return list with the names of the feature variables
	 */
	public List<String> getNameFeatureVariables() {
		return this.nameFeatureVariables;
	}

	/**
	 * Returns the name of the time variable.
	 *
	 * @return name of time variable
	 */
	public String getNameTimeVariable() {
		return this.nameTimeVariable;
	}

	/**
	 * Returns the name of all the variables except the time variable. The list returned contains first the features
	 * and
	 * then the class variables.
	 *
	 * @return name of all the variables except the time variable
	 */
	public List<String> getNameVariables() {
		List<String> nameVariables = new ArrayList<>();
		nameVariables.addAll(getNameFeatureVariables());
		// If there are class variables, add them to the list
		Optional.ofNullable(getNameClassVariables()).ifPresent(nameVariables::addAll);
		return nameVariables;
	}

	/**
	 * Returns the number of class variables.
	 *
	 * @return number of class variables.
	 */
	public int getNumClassVariables() {
		return this.nameClassVariables.size();
	}

	/**
	 * Returns the number of data points. In this case, this is the number of sequences.
	 *
	 * @return number of sequences
	 */
	public int getNumDataPoints() {
		return getSequences().size();
	}

	/**
	 * Returns the number of observations in the dataset, i.e., the number of observations that occur in all the
	 * sequences.
	 *
	 * @return number of observations
	 */
	public int getNumObservation() {
		return this.sequences.stream().mapToInt(sequence -> sequence.getNumObservations()).sum();
	}

	/**
	 * Returns the possible states of the specified variable. The states of the variable are extracted once and stored
	 * in a map to avoid recomputations. In order to not always return a reference to the same State list, the State
	 * objects from the map are copied.
	 *
	 * @param nameVariable variable name
	 * @return states of the variable
	 */
	public List<String> getPossibleStatesVariable(String nameVariable) {
		// Extract states from the Map (if previously obtained)
		List<String> states = this.statesVariables.get(nameVariable);
		if (states == null) {
			// Use a HashSet to save each state just once
			Set<String> statesSet = new HashSet<>();
			for (Sequence sequence : getSequences()) {
				String[] statesSequence = sequence.getStates(nameVariable);
				if (statesSequence == null)
					continue;
				for (int i = 0; i < statesSequence.length; i++) {
					statesSet.add(statesSequence[i]);
				}
			}
			states = new ArrayList<>(statesSet);
			this.statesVariables.put(nameVariable, states);
		}
		return states;
	}

	/**
	 * Returns the sequences of the dataset.
	 *
	 * @return list with the sequences of the dataset
	 */
	public List<Sequence> getSequences() {
		return new ArrayList<>(this.sequences);
	}

	/**
	 * Gets the states of the class variables for each of the sequences.
	 *
	 * @return array of {@code State} objects
	 */
	public State[] getStatesClassVariables() {
		State[] stateClassVariables = new State[getSequences().size()];
		for (int i = 0; i < getSequences().size(); i++)
			stateClassVariables[i] = new State(getSequences().get(i).getClassVariables());
		return stateClassVariables;
	}

	/**
	 * Gets the possible states of all variables.
	 *
	 * @return array of {@code State} objects
	 */
	public Map<String, List<String>> getStatesVariables() {
		return this.statesVariables;
	}

	/**
	 * Retrieves the states of the class variables and stores them in a {@code Map}.
	 */
	public void initialiazeStatesClassVariables() {
		for (String nameClassVariable : this.nameClassVariables) {
			// Use a HashSet to save each state just once
			Set<String> statesSet = new HashSet<>();
			for (Sequence sequence : getSequences()) {
				String[] statesSequence = sequence.getStates(nameClassVariable);
				for (int i = 0; i < statesSequence.length; i++) {
					statesSet.add(statesSequence[i]);
				}
			}
			this.statesVariables.put(nameClassVariable, new ArrayList<>(statesSet));
		}
	}

	/**
	 * Removes from the dataset those feature variables with zero variance. This method should be used when the entire
	 * dataset was read, as new sequences could be included.
	 *
	 * @param removeZeroVariance {@code true} to remove variables with no variance, {@code false} otherwise
	 */
	public void checkVarianceFeatures(boolean removeZeroVariance) {
		// Use the temporal list to avoid a concurrent modification exception
		List<String> tempList = new ArrayList<>(this.nameFeatureVariables);
		for (String nameFeature : tempList) {
			if (getPossibleStatesVariable(nameFeature).size() == 1) {
				if (removeZeroVariance) {
					logger.warn("Feature variable {} is removed since its variance is zero", nameFeature);
					removeFeature(nameFeature);
				} else {
					logger.warn("Feature variable {} has zero variance. Consider ignoring this variable", nameFeature);
				}
			}
		}
	}

	/**
	 * Sets the class variables to ignored.
	 *
	 * @param ignoredClassVariables names of the class variables to ignore
	 */
	public void setIgnoredClassVariables(List<String> ignoredClassVariables) {
		this.ignoredClassVariables = ignoredClassVariables;
	}

	/**
	 * Sets states of all variables. This method is used when training and test datasets are defined, and the training
	 * dataset needs to know all possible states.
	 *
	 * @param statesVariables a {code Map} linking the names of the variables with their possible states
	 */
	public void setStatesVariables(Map<String, List<String>> statesVariables) {
		this.statesVariables = statesVariables;
	}

	/**
	 * Checks if it is possible to create a sequence with the provided data. An exception is thrown if this is not the
	 * case.
	 *
	 * @param data list of arrays that contains the data of the sequence
	 * @throws ErroneousSequenceException if a valid sequence cannot be created with the provided data
	 */
	private void checkIntegrityData(List<String[]> data) throws ErroneousSequenceException {
		// Check content data. There should be at least two arrays, one for the names of
		// the variables and another for an observation
		if (data.size() < 3) {
			String message = "Sequences must contain, at least, two observations";
			throw new ErroneousSequenceException(message);
		}
		// Check names of variables
		List<String> nameVariablesSequence = Arrays.asList(data.get(0));
		if (!nameVariablesSequence.contains(this.nameTimeVariable)) {
			String message = String.format("Time variable '%s' not specified", this.nameTimeVariable);
			throw new ErroneousSequenceException(message);
		} else if (this.nameClassVariables != null && !nameVariablesSequence.containsAll(this.nameClassVariables)) {
			// There are no class variables if the dataset is for classification
			String message = "One or more specified class variables are not present in the sequence";
			throw new ErroneousSequenceException(message);
		} else if (getNumDataPoints() > 0 && (!nameVariablesSequence.containsAll(getNameAllVariables()) ||
				!getNameAllVariables().containsAll(nameVariablesSequence))) {
			// If there is a sequence in the dataset, they must have the same variables
			String message = "Sequences cannot have different variables";
			throw new ErroneousSequenceException(message);
		}
	}

	/**
	 * Receives a list of Strings (a sequence) from which a {@code Sequence} is created.
	 *
	 * @param data data from which the {@code Sequence} is generated
	 * @return a {@code Sequence}
	 * @throws ErroneousSequenceException thrown if the sequence cannot be created with the provided data
	 */
	private Sequence createSequence(List<String[]> data) throws ErroneousSequenceException {
		// Check if it is possible to add the sequence
		checkIntegrityData(data);
		// Obtain names of variables
		List<String> nameVariablesSequence = Arrays.asList(data.get(0));
		// If there are no sequences in the dataset, it is stored the name of the
		// feature variables. They are given by the names of the variables that were not
		// defined as the time variable or class variable.
		if (getSequences().size() == 0)
			this.nameFeatureVariables = extractFeatureNames(nameVariablesSequence);
		// Drop names of variables
		data.remove(0);
		// Create sequence. Check if information about class variables is provided
		return new Sequence(nameVariablesSequence, this.nameTimeVariable, this.nameClassVariables,
				this.nameFeatureVariables, data);
	}

	/**
	 * Extract the name of the feature variables knowing which are the time and class variables.
	 *
	 * @param nameVariablesSequence names of all variables
	 * @return names of the feature variables
	 */
	private List<String> extractFeatureNames(List<String> nameVariablesSequence) {
		return nameVariablesSequence.stream().filter(name -> !name.equals(this.nameTimeVariable) &&
				(this.nameClassVariables == null || !this.nameClassVariables.contains(name))).collect(
				Collectors.toList());
	}

	/**
	 * Removes a feature from the dataset.
	 *
	 * @param nameFeature name of the feature variable to remove
	 */
	private void removeFeature(String nameFeature) {
		// Remove the feature from all the sequences of the dataset
		for (Sequence sequence : this.sequences)
			sequence.removeFeatureVariable(nameFeature);
		// Remove the name of the feature from the dataset
		this.nameFeatureVariables.remove(nameFeature);
		this.statesVariables.remove(nameFeature);
	}

}