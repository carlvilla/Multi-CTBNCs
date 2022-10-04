package es.upm.fi.cig.multictbnc.util;

import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class.
 *
 * @author Carlos Villa Blanco
 */
public final class Util {

    private Util() {
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
     * Sets the state of a given node and its parents from a {@code State} object.
     *
     * @param node        node whose state is changed
     * @param statesNodes {@code State} with the state of the node and its parents
     */
    public static void setStateNodeAndParents(DiscreteNode node, State statesNodes) {
        // Set state node
        String state = statesNodes.getValueVariable(node.getName());
        node.setState(state);
        // Set state parents node
        for (Node nodeParent : node.getParents()) {
            String stateParent = statesNodes.getValueVariable(nodeParent.getName());
            ((DiscreteNode) nodeParent).setState(stateParent);
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