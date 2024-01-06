package es.upm.fi.cig.multictbnc.util;

import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.DoubleBinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class.
 *
 * @author Carlos Villa Blanco
 */
public final class Util {

    private Util() {
    }

    /**
     * Converts an array of strings into a queue of strings.
     *
     * @param args the array of strings to be converted
     * @return a queue of strings containing the elements from the input array
     */
    public static Queue<String> arrayToQueue(String[] args) {
        Queue<String> queue = new LinkedList<String>();
        //Adding array to stack
        for (String str : args)
            queue.add(str);
        return queue;
    }

    /**
     * Given a list of lists with the possible states of some variables, this method returns the Cartesian product
     * between each of the possible states of each variable.
     *
     * @param statesVariables contains for each variable a list with its possible states
     * @return a list of as many states as possible combinations between the states of the variables
     */
    public static List<State> cartesianProduct(List<List<State>> statesVariables) {
        // The result is a list of states, where each state contains a possible value
        // for all the studied variables
        List<State> result = new ArrayList<>();
        // If it is given an empty list, the result is an empty list too
        if (statesVariables.isEmpty())
            return result;
        // Recursion is used to calculate the product.
        List<State> statesHead = statesVariables.get(0);
        List<State> statesTail = cartesianProduct(statesVariables.subList(1, statesVariables.size()));
        // If "statesTail" is empty, we are obtaining the possible combinations of
        // states for a unique list (statesHead). In that case, the list itself is the
        // solution (even though the Cartesian product would be the empty list).
        if (statesTail.isEmpty())
            return statesHead;
        for (State stateHead : statesHead) {
            for (State stateTail : statesTail) {
                State tmpState = new State();
                tmpState.addEvents(stateHead.getEvents());
                tmpState.addEvents(stateTail.getEvents());
                result.add(tmpState);
            }
        }
        return result;
    }

    /**
     * Creates a deep copy of a two-dimensional double array.
     *
     * @param array array to copy
     * @return copy of the array
     */
    public static double[][] clone2DArray(double[][] array) {
        double copy[][] = new double[array.length][array[0].length];
        for (int i = 0; i < array.length; i++)
            System.arraycopy(array[i], 0, copy[i], 0, array[0].length);
        return copy;
    }

    /**
     * Creates a deep copy of a three-dimensional double array.
     *
     * @param array array to copy
     * @return copy of the array
     */
    public static double[][][] clone3DArray(double[][][] array) {
        double copy[][][] = new double[array.length][array[0].length][array[0][0].length];
        for (int i = 0; i < array.length; i++)
            for (int j = 0; j < array[0].length; j++)
                System.arraycopy(array[i][j], 0, copy[i][j], 0, array[0][0].length);
        return copy;
    }

    /**
     * Returns all possible combinations of size 'k' of a given list of elements.
     *
     * @param <type>   type of the elements
     * @param elements list of elements
     * @param k        size of the combinations
     * @return all possible combinations of size 'k'
     */
    public static <type> List<List<type>> combination(List<type> elements, int k) {
        List<List<type>> result = new ArrayList<>();
        subsetsOf(elements, k, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Returns the first {@code long} in a {@code String}. Returns 0 if it was not possible to extract the number.
     *
     * @param string a {@code String} containing a {@code long}
     * @return numbers in the {@code String}
     */
    public static long extractFirstLong(String string) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(string);
        if (m.find())
            return Long.valueOf(m.group(0));
        return 0;
    }

    /**
     * Returns an {@code int} from a {@code String}. Returns 0 if it was not possible to extract the number.
     *
     * @param string a {@code String} that includes numbers
     * @return {@code int} in the {@code String}
     */
    public static int extractInt(String string) {
        String num = string.replaceAll("\\D", "");
        int resulting_num;
        try {
            resulting_num = num.isEmpty() ? 0 : Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return 0;
        }
        return resulting_num;
    }

    /**
     * Returns the path to the datasets given the folder of a experiment.
     *
     * @param pathExperiment path to the experiment folder
     * @return dataset paths
     */
    public static String[] extractPathExperimentDatasets(String pathExperiment) {
        String[] pathDatasets = Util.retrieveSubfolders(pathExperiment);
        if (pathDatasets == null || pathDatasets.length == 0) {
            // Only one dataset is assumed in the current experiment whose files are located in pathExperiment
            return new String[]{pathExperiment};
        }
        // Sort the paths of the datasets
        Arrays.sort(pathDatasets, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Long.compare(Util.extractLong(s1), Util.extractLong(s2));
            }
        });
        return pathDatasets;
    }

    /**
     * Returns the names of the subfolders in a certain folder.
     *
     * @param pathFolder path to the folder
     * @return names of the subfolders
     */
    public static String[] retrieveSubfolders(String pathFolder) {
        // Retrieve the names of the subfolders
        File file = new File(pathFolder);
        String[] folders = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        if (folders != null) {
            // Add path to subfolder names
            for (int i = 0; i < folders.length; i++)
                folders[i] = Paths.get(pathFolder, folders[i]).toString();
        }
        return folders;
    }

    /**
     * Returns a {@code long} from a {@code String}. Returns 0 if it was not possible to extract the number.
     *
     * @param string a {@code String} that includes numbers
     * @return {@code long} in the {@code String}
     */
    public static long extractLong(String string) {
        String num = string.replaceAll("\\D", "");
        long resulting_num;
        try {
            resulting_num = num.isEmpty() ? 0 : Long.parseLong(num);
        } catch (NumberFormatException nfe) {
            return 0;
        }
        return resulting_num;
    }

    /**
     * Fills a three-dimensional {@code double} array with the provided {@code double}.
     *
     * @param array a three-dimensional {@code double} array
     * @param value {@code double} used to fill the array
     */
    public static void fill3dArray(double[][][] array, double value) {
        for (double[][] matrix : array)
            fill2dArray(matrix, value);
    }

    /**
     * Fills a two-dimensional {@code double} array with the provided {@code double}.
     *
     * @param array a two-dimensional {@code double} array
     * @param value {@code double} used to fill the array
     */
    public static void fill2dArray(double[][] array, double value) {
        for (double[] row : array)
            Arrays.fill(row, value);
    }

    /**
     * Returns the elements of a list "a" except those in "b".
     *
     * @param <T> type of the elements
     * @param a   list
     * @param b   list of elements to filter from "a"
     * @return list of "a" without elements in "b"
     */
    public static <T> List<T> filter(List<T> a, List<T> b) {
        List<T> listA = new ArrayList<>(a);
        listA.removeAll(b);
        return listA;
    }

    /**
     * Returns the elements of a list "a" except "b".
     *
     * @param <T> type of the elements
     * @param a   list
     * @param b   element to filter from the list
     * @return list "a" without element "b"
     */
    public static <T> List<T> filter(List<T> a, T b) {
        List<T> listA = new ArrayList<>(a);
        listA.remove(b);
        return listA;
    }

    /**
     * Returns entry with the largest value of a {@code Map}.
     *
     * @param <k> key type of the {@code Map}
     * @param <v> value type of the {@code Map}
     * @param map a {@code Map}
     * @return entry of the largest value
     */
    public static <k, v extends Comparable<v>> Entry<k, v> getEntryLargestValue(Map<k, v> map) {
        return Collections.max(map.entrySet(),
                (Entry<k, v> e1, Entry<k, v> e2) -> e1.getValue().compareTo(e2.getValue()));
    }

    /**
     * Returns the index of the largest value in an array.
     *
     * @param array array of {@code double}
     * @return index of the largest value
     */
    public static int getIndexLargestValue(double[] array) {
        // Array not valid
        if (array == null || array.length == 0)
            return -1;
        // Index largest value
        int largestIdx = 0;
        for (int i = 1; i < array.length; i++) {
            largestIdx = array[i] > array[largestIdx] ? i : largestIdx;
        }
        return largestIdx;
    }

    /**
     * Returns the maximum value between those passed as parameters
     *
     * @param values list of {@code double}
     * @return maximum value
     */
    public static double getMaxValue(double... values) {
        double max = values[0];
        for (double value : values)
            if (max < value)
                max = value;
        return max;
    }

    /**
     * Get {@code k} random elements from an {\@code Integer} list.
     *
     * @param list list of {@code Integer}
     * @param k    number of {@code int} to retrieve
     * @return list of selected random elements
     */
    public static List<Integer> getRandomElements(List<Integer> list, int k) {
        List<Integer> tmpList = new ArrayList<Integer>(list);
        Collections.shuffle(tmpList);
        return k > tmpList.size() ? tmpList.subList(0, tmpList.size()) : tmpList.subList(0, k);
    }

    /**
     * Retrieves the possible subsets of a certain size from a given set of elements without including a certain
     * element.
     *
     * @param <type>          type of the elements
     * @param list            list of elements
     * @param size            size of the subsets
     * @param elementToIgnore element to ignore
     * @return list of subsets
     */
    public static <type> List<List<type>> getSubsets(List<type> list, int size, type elementToIgnore) {
        List<type> listWithoutElemToIgnore = new ArrayList<>(list);
        if (elementToIgnore != null)
            listWithoutElemToIgnore.remove(elementToIgnore);
        return Util.combination(listWithoutElemToIgnore, size);
    }

    /**
     * Checks if an array is empty.
     *
     * @param <T>   type of the elements in the array
     * @param array array
     * @return {@code true} if the array is empty, {@code false} otherwise
     */
    public static <T> boolean isArrayEmpty(T[] array) {
        if (array == null)
            return true;
        else if (array.length == 0)
            return true;
        else {
            for (T elem : array) {
                if (elem != null)
                    return false;
            }
        }
        return true;
    }

    /**
     * Kronecker delta function. It returns one if 'a' and 'b' are equal and zero otherwise.
     *
     * @param a a {@code String}
     * @param b a {@code String}
     * @return 1 if 'a' and 'b' are equal, 0 otherwise
     */
    public static int kroneckerDelta(String[] a, String[] b) {
        return Arrays.equals(a, b) ? 1 : 0;
    }

    /**
     * Transforms a list into an {@code String} array.
     *
     * @param list list
     * @return {@code String} array
     */
    public static String[] listToArray(List<?> list) {
        return list.stream().toArray(String[]::new);
    }

    /**
     * Checks if two given lists of {@code Node} contain the same elements independently of their orders.
     *
     * @param listA      a list of {@code Node}
     * @param listB      a list of {@code Node}
     * @param <typeNode> subtype of the {@code Node} objects
     * @return {@code true} if the lists contain the same elements, {@code false} otherwise.
     */
    public static <typeNode extends Node> boolean listsOfNodeContainSameElements(List<typeNode> listA,
                                                                                 List<typeNode> listB) {
        if (listA == null && listB == null)
            return true;
        if (listA == null || listB == null || listA.size() != listB.size())
            return false;
        // Extract names nodes
        List<String> nameNodesListA = listA.stream().map(node -> node.getName()).collect(Collectors.toList());
        List<String> nameNodesListB = listB.stream().map(node -> node.getName()).collect(Collectors.toList());
        // Sort the lists so they can be considering the same independently of the order of their elements
        Collections.sort(nameNodesListA);
        Collections.sort(nameNodesListB);
        return listA.equals(listB);
    }

    /**
     * Remove a column from an array.
     *
     * @param array             array which column should be removed
     * @param idxColumnToRemove index of the column to remove
     * @return array without the selected column
     */
    public static String[][] removeColumnArray(String[][] array, int idxColumnToRemove) {
        int row = array.length;
        int column = array[0].length;
        String[][] arrayWithoutColumn = new String[row][column - 1];
        try {
            for (int idxRow = 0; idxRow < row; idxRow++) {
                for (int idxColumn = 0, currColumn = 0; idxColumn < column; idxColumn++) {
                    if (idxColumn != idxColumnToRemove) {
                        arrayWithoutColumn[idxRow][currColumn++] = array[idxRow][idxColumn];
                    }
                }
            }
        } catch (Exception e) {
            System.out.println();
        }
        return arrayWithoutColumn;
    }

    /**
     * Sets the state of a given node and its parents from a {@code State} object.
     *
     * @param node        node whose state is changed
     * @param statesNodes {@code State} with the state of the node and its parents
     */
    public static void setStateNodeAndParents(DiscreteStateNode node, State statesNodes) {
        // Set state node
        String state = statesNodes.getValueVariable(node.getName());
        node.setState(state);
        // Set state parents node
        for (Node nodeParent : node.getParents()) {
            String stateParent = statesNodes.getValueVariable(nodeParent.getName());
            ((DiscreteStateNode) nodeParent).setState(stateParent);
        }
    }

    /**
     * Shuffles the elements of a list. The seed is computed using the current system time if the provided one is null.
     *
     * @param <T>  type of the elements to shuffle
     * @param list list to shuffle
     * @param seed seed used to shuffle the sequences
     */
    public static <T> void shuffle(List<T> list, Long seed) {
        Random rd = new Random();
        if (seed != null)
            rd.setSeed(seed);
        Collections.shuffle(list, rd);
    }

    /**
     * Extract a list of Strings from a String representation of a list with the format "element1,element2,element3"
     * (commas are the delimiters).
     *
     * @param listContent string containing the list elements separated by semicolons
     * @return list of strings, each representing an element from the input string
     */
    public static List<String> stringToList(String listContent) {
        return Arrays.asList(listContent.split(";"));
    }

    /**
     * Extract a map from a String with the format "String:Double,String:Double", where the Strings are the keys and
     * the doubles the values.
     *
     * @param mapContent string containing key-value pairs separated by colons and commas
     * @return map where each key and value is extracted from the input string
     */
    public static Map<String, Double> stringToMap(String mapContent) {
        Map<String, Double> resultingMap = new HashMap<String, Double>();
        String[] keyValues = Arrays.stream(mapContent.split(",")).toArray(String[]::new);
        for (String keyValue : keyValues) {
            String key = keyValue.split(":")[0];
            Double value = Double.valueOf(keyValue.split(":")[1]);
            resultingMap.put(key, value);
        }
        return resultingMap;
    }

    /**
     * Given two non-negative numbers, this method returns a non-negative integer that is uniquely associated with that
     * pair.
     *
     * @param a non-negative {@code int}
     * @param b non-negative {@code int}
     * @return non-negative {@code int} uniquely associated with the provided pair of non-negative {@code int}
     * @see <a href="http://szudzik.com/ElegantPairing.pdf">http://szudzik.com/ElegantPairing.pdf</a>
     */
    public static int szudzikFunction(int a, int b) {
        return a >= b ? a * a + a + b : a + b * b;
    }

    private static <type> void subsetsOf(List<type> values, int k, int index, List<type> tempSet,
                                         List<List<type>> finalSet) {
        if (tempSet.size() == k) {
            finalSet.add(new ArrayList<>(tempSet));
            return;
        }
        if (index == values.size())
            return;
        type value = values.get(index);
        tempSet.add(value);
        subsetsOf(values, k, index + 1, tempSet, finalSet);
        tempSet.remove(value);
        subsetsOf(values, k, index + 1, tempSet, finalSet);
    }


}