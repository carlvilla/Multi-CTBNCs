package es.upm.fi.cig.multictbnc.data.representation;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousSequenceException;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a sequence of multivariate data, i.e., a set of data points with multiple variables where the order is
 * relevant. In this case, a sequence represents a time series since the data is ordered by a time variable.
 *
 * @author Carlos Villa Blanco
 */
public class Sequence {
    private static final Logger logger = LogManager.getLogger(Sequence.class);
    // Time formatter
    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern(
            "[dd-][dd/][d-][d/][MM-][MM/][M-][M/][yyyy][ ][HH][:mm][:ss][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]").parseDefaulting(
            ChronoField.DAY_OF_MONTH, LocalDate.now().getDayOfMonth()).parseDefaulting(ChronoField.MONTH_OF_YEAR,
            LocalDate.now().getMonthValue()).parseDefaulting(ChronoField.YEAR_OF_ERA,
            LocalDate.now().getYear()).parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(
            ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
    // Store the value of the time variable when the observation occurred
    private String nameTimeVariable;
    private double[] timeValues;
    // Store the value of all the variables (except the time variable)
    private String[][] variablesValues;
    private Map<String, String> classVariablesValues;
    // It is necessary to store these names since the dataset can be created
    // directly from sequences
    private List<String> nameFeatureVariables;
    // Store the position of the feature variables in the "variablesValues" array
    private Map<String, Integer> idxFeatureVariables;
    // Path to the file from which the sequence was extracted (if it exists)
    private String filePath;


    /**
     * Constructs a {@code Sequence}. It receives the names of all the variables and those that are time and class
     * variables, and the observations of the sequence.
     *
     * @param nameVariables      names of all the variables in the sequence. It is assumed that they are presented in
     *                           the same order as they appear in the data of the observations
     * @param nameTimeVariable   name of the time variable
     * @param nameClassVariables name of the class variables
     * @param dataObservations   list of arrays with the values of the observations
     * @throws ErroneousSequenceException if a valid sequence cannot be created with the provided data
     */
    public Sequence(List<String> nameVariables, String nameTimeVariable, List<String> nameClassVariables,
                    List<String[]> dataObservations) throws ErroneousSequenceException {
        // Set the name of the time variable
        this.nameTimeVariable = nameTimeVariable;
        // Set the names of the features by filtering the names of the class variables
        // and time variable
        this.nameFeatureVariables = Util.<String>filter(Util.<String>filter(nameVariables, nameClassVariables),
                nameTimeVariable);
        // Define values class variables for the sequence
        setValuesClassVariables(nameVariables, nameClassVariables, dataObservations);
        // Save the values of the features and time variables
        generateObservations(nameVariables, this.nameFeatureVariables, nameTimeVariable, dataObservations);
    }

    /**
     * Constructs a {@code Sequence}. It receives the names of the time, feature and class variables and their
     * observations.
     *
     * @param nameTimeVariable       name of the time variable
     * @param nameFeatureVariables   name of the feature variables
     * @param nameClassVariables     name of the class variables
     * @param timeValues             array of time values for the sequence
     * @param featureVariablesValues array of feature variable values for the sequence
     * @param classVariablesValues   array of class variable values for the sequence
     * @param idxFeatureVariables    mapping of feature variable names to their indexes
     */
    public Sequence(String nameTimeVariable, List<String> nameFeatureVariables, List<String> nameClassVariables,
                    double[] timeValues, String[][] featureVariablesValues, String[] classVariablesValues,
                    Map<String, Integer> idxFeatureVariables) {
        this.nameTimeVariable = nameTimeVariable;
        this.nameFeatureVariables = nameFeatureVariables;
        this.timeValues = timeValues;
        this.variablesValues = featureVariablesValues;
        this.classVariablesValues = new HashMap<>();
        for (int idxClassVariable = 0; idxClassVariable < nameClassVariables.size(); idxClassVariable++)
            this.classVariablesValues.put(nameClassVariables.get(idxClassVariable),
                    classVariablesValues[idxClassVariable]);
        this.idxFeatureVariables = idxFeatureVariables;
    }

    /**
     * Constructs a {@code Sequence}. It receives the names of all the variables and the observations of the sequence.
     *
     * @param nameVariables        names of all the variables in the sequence. It is assumed that they are presented in
     *                             the same order as they appear in the data of the observations
     * @param nameTimeVariable     name of the time variable
     * @param nameClassVariables   names of the class variables
     * @param nameFeatureVariables names of the feature variables
     * @param dataObservations     list of arrays with the values of the observations
     * @throws ErroneousSequenceException if a valid sequence cannot be created with the provided data
     */
    public Sequence(List<String> nameVariables, String nameTimeVariable, List<String> nameClassVariables,
                    List<String> nameFeatureVariables, List<String[]> dataObservations)
            throws ErroneousSequenceException {
        // Set the name of the time variable
        this.nameTimeVariable = nameTimeVariable;
        // Set the names of the features by filtering the names of the class variables
        // and time variable
        this.nameFeatureVariables = nameFeatureVariables;
        if (nameClassVariables != null)
            // Define values class variables for the sequence
            setValuesClassVariables(nameVariables, nameClassVariables, dataObservations);
        // Save the values of the features and time variables
        generateObservations(nameVariables, nameFeatureVariables, nameTimeVariable, dataObservations);
    }

    /**
     * Constructs a {@code Sequence}. This constructor is used by the models to sample sequences.
     *
     * @param stateClassVariables class configuration of the class variables
     * @param observations        list of {@code State} representing the observations of the sequence
     * @param nameTimeVariable    name of the time variable
     * @param timestamps          list of {@code Double} representing the timestamps when each observation of the
     *                            sequence was obtained
     * @throws ErroneousSequenceException if a valid sequence cannot be created with the provided data
     */
    public Sequence(State stateClassVariables, List<State> observations, String nameTimeVariable,
                    List<Double> timestamps) throws ErroneousSequenceException {
        if (observations.size() != timestamps.size())
            throw new ErroneousSequenceException("The number of observations and timestamps differ");
        // Set name of the time variable
        this.nameTimeVariable = nameTimeVariable;
        // Set names variables
        List<String> nameVariables = observations.get(0).getNameVariables();
        // Get names feature variables
        this.nameFeatureVariables = Util.<String>filter(
                Util.<String>filter(nameVariables, stateClassVariables.getNameVariables()), nameTimeVariable);
        // Set values class variables
        this.classVariablesValues = stateClassVariables.getEvents();
        // Initialise data structures
        this.timeValues = new double[timestamps.size()];
        this.variablesValues = new String[timestamps.size()][this.nameFeatureVariables.size()];
        for (int i = 0; i < timestamps.size(); i++) {
            checkIntegrityObservation(observations.get(i));
            this.timeValues[i] = timestamps.get(i);
            this.variablesValues[i] = observations.get(i).getValues();
        }
        this.idxFeatureVariables = IntStream.range(0, nameFeatureVariables.size()).boxed().collect(
                Collectors.toMap(i -> nameFeatureVariables.get(i), i -> i));
    }

    /**
     * A sequence has a unique value for each class variable, so the values of the class variables for the first
     * observation are stored.
     *
     * @param nameVariables      names of all variables
     * @param nameClassVariables names of the class variables
     * @param dataObservations   list of arrays with the values of the observations
     */
    private void setValuesClassVariables(List<String> nameVariables, List<String> nameClassVariables,
                                         List<String[]> dataObservations) {
        // LinkedHashMap maintains the order of the class variables as in the dataset
        this.classVariablesValues = new LinkedHashMap<String, String>();
        for (String nameClassVariable : nameClassVariables) {
            // It is obtained the index of each class variable in the observations
            for (int i = 0; i < nameVariables.size(); i++) {
                if (nameVariables.get(i).equals(nameClassVariable)) {
                    this.classVariablesValues.put(nameClassVariable, dataObservations.get(0)[i]);
                }
            }
        }
    }

    /**
     * Generate the observations of the sequence.
     *
     * @param nameVariables    names of all the variables in the sequence. It is assumed that they are presented in the
     *                         same order as they appear in the data of the observations
     * @param nameTimeVariable name of the time variable
     * @param dataObservations list of {@code String[]} with the observations
     * @throws ErroneousSequenceException thrown if the sequence cannot be generated with the provided data
     */
    private void generateObservations(List<String> nameVariables, List<String> nameFeatureVariables,
                                      String nameTimeVariable, List<String[]> dataObservations)
            throws ErroneousSequenceException {
        // Initialise data structures
        this.variablesValues = new String[dataObservations.size()][nameVariables.size()];
        this.timeValues = new double[dataObservations.size()];
        // Get indexes of class variables in the sequence (if there are)
        List<Integer> idxClassVariablesInDataset = new ArrayList<>();
        if (classVariablesValues != null) {
            for (String nameClassVariable : classVariablesValues.keySet())
                idxClassVariablesInDataset.add(nameVariables.indexOf(nameClassVariable));
        }
        // Get indexes of feature variables in the sequence
        List<Integer> idxFeatureVariablesInDataset = new ArrayList<>();
        for (String nameFeatureVariable : nameFeatureVariables)
            idxFeatureVariablesInDataset.add(nameVariables.indexOf(nameFeatureVariable));
        // Map the names of the features with their indexes
        this.idxFeatureVariables = IntStream.range(0, nameFeatureVariables.size()).boxed().collect(
                Collectors.toMap(i -> nameFeatureVariables.get(i), i -> i));
        // Get the index of the time variables in the sequence
        int idxTimeVariable = nameVariables.indexOf(nameTimeVariable);
        for (int idxObservation = 0; idxObservation < dataObservations.size(); idxObservation++) {
            checkIntegrityObservation(idxClassVariablesInDataset, dataObservations.get(idxObservation));

            try {
                for (int idxFeatureVariable = 0; idxFeatureVariable < nameFeatureVariables.size();
                     idxFeatureVariable++) {
                    this.variablesValues[idxObservation][idxFeatureVariable] = dataObservations.get(
                            idxObservation)[idxFeatureVariablesInDataset.get(idxFeatureVariable)];
                }
            } catch (Exception e) {

            }

            // Read time variable. Double and Date are accepted
            Double timeValue;
            try {
                timeValue = Double.parseDouble(dataObservations.get(idxObservation)[idxTimeVariable]);
            } catch (NumberFormatException nfe) {
                // Check if a date was provided
                String stringDate = dataObservations.get(idxObservation)[idxTimeVariable];
                try {
                    LocalDateTime dateTime = (LocalDateTime) dateTimeFormatter.parseBest(stringDate,
                            LocalDateTime::from, LocalDate::from);
                    timeValue = (double) dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                } catch (DateTimeParseException dtpe) {
                    throw new ErroneousSequenceException("The value " + stringDate +
                            " of the selected time variable was not correctly recognised as a timestamp. Numerical " +
                            "values or dates (e.g. dd-MM-yyyy HH:mm:ss.SSS, dd/MM/yyyy, HH:mm:ss) are expected");
                }
            }
            this.timeValues[idxObservation] = timeValue;
        }
    }

    private void checkIntegrityObservation(List<Integer> idxClassVariablesInObservation, String[] dataObservation)
            throws ErroneousSequenceException {
        if (this.classVariablesValues != null) {
            List<String> nameClassVariables = new ArrayList<>(this.classVariablesValues.keySet());
            for (int idxClassVariable = 0; idxClassVariable < this.classVariablesValues.size(); idxClassVariable++) {
                if (!dataObservation[idxClassVariablesInObservation.get(idxClassVariable)].equals(
                        this.classVariablesValues.get(nameClassVariables.get(idxClassVariable))))
                    throw new ErroneousSequenceException("Observations have different values for the class variables");
            }
        }
    }

    private void checkIntegrityObservation(State stateObservation) throws ErroneousSequenceException {
        if (this.classVariablesValues != null) {
            for (String nameClassVariable : this.classVariablesValues.keySet()) {
                if (!stateObservation.getValueVariable(nameClassVariable).equals(
                        this.classVariablesValues.get(nameClassVariable)))
                    throw new ErroneousSequenceException("Observations have different values for the class variables");
            }
        }

    }

    /**
     * Adds a new feature variable to the sequence by copying the values from another sequence with the same number of observations.
     *
     * @param nameFeatureVariable name of the new feature variable to be added
     * @param sequence            sequence containing the values of the new feature variable for each observation
     * @return {@code true} if the addition was successful, {@code false} otherwise
     */
    public boolean addFeatureVariable(String nameFeatureVariable, Sequence sequence) {
        if (getNumObservations() != sequence.getNumObservations())
            return false;
        // Add states feature variable to the observations
        int idxNewFeatureVariable = getNumClassAndFeatureVariables();
        String[][] newVariablesValues = new String[getNumObservations()][getNumClassAndFeatureVariables() + 1];
        for (int idxObservation = 0; idxObservation < getNumObservations(); idxObservation++) {
            String stateFeatureVariableInObservation = sequence.getValueFeatureVariable(idxObservation,
                    nameFeatureVariable);
            System.arraycopy(variablesValues[idxObservation], 0, newVariablesValues[idxObservation], 0,
                    variablesValues[idxObservation].length);
            newVariablesValues[idxObservation][idxNewFeatureVariable] = stateFeatureVariableInObservation;
        }
        this.variablesValues = newVariablesValues;
        // Register the name of the feature variable and its index in the array
        this.idxFeatureVariables.put(nameFeatureVariable, idxNewFeatureVariable);
        return true;
    }

    /**
     * Returns the number of observations that the sequence contains.
     *
     * @return number of observations
     */
    public int getNumObservations() {
        return this.variablesValues.length;
    }

    /**
     * Returns the total number of class and feature variables in the sequence.
     *
     * @return total number of class and feature variables in the sequence
     */
    public int getNumClassAndFeatureVariables() {
        return getNameClassVariables().size() + getNameFeatureVariables().size();
    }

    /**
     * Returns the value of a certain feature variable for a given observation.
     *
     * @param idxObservation observation index
     * @param nameVariable   name of the variable
     * @return value of the feature variable in the observation
     */
    public String getValueFeatureVariable(int idxObservation, String nameVariable) {
        try {
            int indexFeatureVariable = this.idxFeatureVariables.get(nameVariable);
            return variablesValues[idxObservation][indexFeatureVariable];
        } catch (NullPointerException npe) {
            throw new NullPointerException("Variable " + nameVariable + " was not found in Sequence " + this.filePath);
        }
    }

    /**
     * Returns the names of the class variables.
     *
     * @return names of the class variables
     */
    public List<String> getNameClassVariables() {
        Set<String> keys = this.classVariablesValues.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Returns the names of the feature variables.
     *
     * @return names of the feature variables.
     */
    public List<String> getNameFeatureVariables() {
        return this.nameFeatureVariables;
    }

    /**
     * Returns a Map object with the class variables' names and values.
     *
     * @return Map with names and values of the class variables
     */
    public Map<String, String> getClassVariables() {
        return this.classVariablesValues;
    }

    /**
     * Returns the path of the file from which the sequence was extracted.
     *
     * @return path of the file from which the sequence was extracted
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Sets the path of the file from which the sequence was extracted.
     *
     * @param filePath path of the file
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Retrieves the mapping of feature variable names to their indexes within the sequence.
     *
     * @return {@code Map} where keys are the names of the feature variables and values are their corresponding indexes in the observations.
     */
    public Map<String, Integer> getIdxFeatureVariables() {
        return idxFeatureVariables;
    }

    /**
     * Returns the name of all the variables, including the time variable.
     *
     * @return name of all the variables
     */
    public List<String> getNameAllVariables() {
        List<String> nameVariables = new ArrayList<>();
        nameVariables.add(getNameTimeVariable());
        nameVariables.addAll(getNameFeatureVariables());
        nameVariables.addAll(getNameClassVariables());
        return nameVariables;
    }

    /**
     * Returns the name of the time variable.
     *
     * @return name of the time variable
     */
    public String getNameTimeVariable() {
        return this.nameTimeVariable;
    }

    /**
     * Gets all the possible states of a specific variable. Returns null if the variable does not exist or was ignored.
     *
     * @param nameVariable name of the variable whose possible states we want to know
     * @return Array with the states of the variable
     */
    public String[] getStates(String nameVariable) {
        // If it is a class variable, there can only be one value per sequence.
        if (this.classVariablesValues != null && this.classVariablesValues.containsKey(nameVariable))
            return new String[]{this.classVariablesValues.get(nameVariable)};
        // If it is a feature, it is stored all its possible unique values.
        if (this.idxFeatureVariables != null && idxFeatureVariables.containsKey(nameVariable)) {
            Set<String> states = new HashSet<>();
            for (int idxObservation = 0; idxObservation < getNumObservations(); idxObservation++)
                states.add(getValueFeatureVariable(idxObservation, nameVariable));
            return states.toArray(new String[states.size()]);
        }
        return null;
    }

    /**
     * Returns the values of the time variable in the sequence.
     *
     * @return values of the time variable in the sequence
     */
    public double[] getTimeValues() {
        return timeValues;
    }

    /**
     * Returns the value of a certain variable for a given observation.
     *
     * @param idxObservation observation index
     * @param nameVariable   name of the variable
     * @return value of the variable in the observation
     */
    public String getValueVariable(int idxObservation, String nameVariable) {
        String valueClassVariable = getValueClassVariable(nameVariable);
        if (valueClassVariable != null)
            return valueClassVariable;
        try {
            int indexFeatureVariable = this.idxFeatureVariables.get(nameVariable);
            return variablesValues[idxObservation][indexFeatureVariable];
        } catch (NullPointerException npe) {
            throw new NullPointerException("Variable " + nameVariable + " was not found in Sequence " + this.filePath);
        }
    }

    /**
     * Returns the values of the specified class variable.
     *
     * @param nameClassVariable name of the class variable
     * @return values of the class variable
     */
    public String getValueClassVariable(String nameClassVariable) {
        if (this.classVariablesValues != null)
            return this.classVariablesValues.get(nameClassVariable);
        return null;
    }

    /**
     * Retrieves the array of values for all variables (excluding the time variable) for each observation in the sequence.
     *
     * @return array of {@code String} where each row represents an observation and each column represents a variable's value at that observation
     */
    public String[][] getVariablesValues() {
        return variablesValues;
    }

    /**
     * Removes a feature from the sequence.
     *
     * @param nameFeature name of the feature variable
     */
    public void removeFeatureVariable(String nameFeature) {
        try {
            this.nameFeatureVariables.remove(nameFeature);
            int idxFeature = this.idxFeatureVariables.get(nameFeature);
            // Update indexes of the feature variables after removing one of these variables
            for (Map.Entry<String, Integer> entry : this.idxFeatureVariables.entrySet()) {
                int index = entry.getValue();
                if (index > this.idxFeatureVariables.get(nameFeature))
                    entry.setValue(index - 1);
            }
            this.idxFeatureVariables.remove(nameFeature);
            // Remove values of the removed feature variable
            variablesValues = Util.removeColumnArray(variablesValues, idxFeature);
        } catch (NullPointerException e) {
            this.logger.error("An error occurred while removing the feature variable {}", nameFeature);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- TIME VARIABLE -----\n");
        sb.append(this.nameTimeVariable);
        sb.append("\n");
        for (int idxObservation = 0; idxObservation < getNumObservations(); idxObservation++) {
            sb.append(getTimeValue(idxObservation));
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("----- CLASS VARIABLES -----\n");
        sb.append(String.join(",", this.classVariablesValues.keySet()));
        sb.append("\n");
        sb.append(String.join(",", this.classVariablesValues.values()));
        sb.append("\n");
        sb.append("----- FEATURE VARIABLES -----\n");
        sb.append(Arrays.toString(this.nameFeatureVariables.toArray()));
        sb.append("\n");
        for (int idxObservation = 0; idxObservation < getNumObservations(); idxObservation++) {
            sb.append(Arrays.toString(getValuesFeatureVariables(idxObservation)));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the value of the time variable in a given observation.
     *
     * @param idxObservation observation index
     * @return value of the time variable
     */
    public double getTimeValue(int idxObservation) {
        return timeValues[idxObservation];
    }

    /**
     * Returns the values of all feature variables for a given observation.
     *
     * @param idxObservation observation index
     * @return values of the feature variables in the observation
     */
    public String[] getValuesFeatureVariables(int idxObservation) {
        return variablesValues[idxObservation];
    }

}