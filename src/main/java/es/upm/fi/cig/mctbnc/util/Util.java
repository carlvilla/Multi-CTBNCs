package es.upm.fi.cig.mctbnc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import es.upm.fi.cig.mctbnc.data.representation.State;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Utility class.
 * 
 * @author Carlos Villa Blanco
 *
 */
public final class Util {

	private Util() {
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
		List<T> listA = new ArrayList<T>(a);
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
		List<T> listA = new ArrayList<T>(a);
		listA.remove(b);
		return listA;
	}

	/**
	 * Returns the unique values of an {@code String} array.
	 * 
	 * @param array array of {@code String}
	 * @return array with unique values
	 */
	public static String[] getUnique(String[] array) {
		return Arrays.stream(array).distinct().toArray(String[]::new);
	}

	/**
	 * Returns the index of a {@code String} in an array.
	 * 
	 * @param array   array of Strings
	 * @param element element whose index in the array is searched
	 * @return index
	 */
	public static int getIndexElement(String[] array, String element) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(element))
				return i;
		}
		return -1;
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
	 * Creates a deep copy of a two-dimensional boolean array.
	 * 
	 * @param array array to copy
	 * @return copy of the array
	 */
	public static boolean[][] clone2DArray(boolean[][] array) {
		boolean[][] copy = new boolean[array.length][];
		for (int r = 0; r < array.length; r++)
			copy[r] = array[r].clone();
		return copy;
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
		Entry<k, v> maxEntry = Collections.max(map.entrySet(),
				(Entry<k, v> e1, Entry<k, v> e2) -> e1.getValue().compareTo(e2.getValue()));
		return maxEntry;
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
	 * Shuffles the elements of a list. The seed is computed using the current
	 * system time if the provided one is null.
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
	 * Kronecker delta function. It returns 1 if 'a' and 'b' are equal and 0
	 * otherwise.
	 * 
	 * @param a a {@code String}
	 * @param b a {@code String}
	 * @return 1 if 'a' and 'b' are equal, 0 otherwise
	 */
	public static int kroneckerDelta(String[] a, String[] b) {
		return Arrays.equals(a, b) ? 1 : 0;
	}

	/**
	 * Given a list of lists with the possible states of some variables, this method
	 * returns the Cartesian product between each of the possible state of each
	 * variable.
	 * 
	 * @param statesVariables contains for each variable a list with its possible
	 *                        states
	 * @return a list of as many states as possible combinations between the states
	 *         of the variables
	 */
	public static List<State> cartesianProduct(List<List<State>> statesVariables) {
		// The result is a list of states, where each state contains a possible value
		// for all the studied variables
		List<State> result = new ArrayList<>();
		// If it is given an empty list, the result is a empty list too
		if (statesVariables.isEmpty())
			return result;
		// Recursion is used to calculate the product.
		List<State> statesHead = statesVariables.get(0);
		List<State> statesTail = cartesianProduct(statesVariables.subList(1, statesVariables.size()));
		// If "statesTail" is empty, we are obtaining the possible combinations of
		// states for a unique list (statesHead). In that case, the list itself is the
		// solution (despite the fact that the Cartesian product would be the empty
		// list).
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
	 * Builds a cache of a certain size with the specified key and value types.
	 * 
	 * @param maxSize maximum size of the cache
	 * @param <Key>   key type
	 * @param <Value> value type
	 * @return cache
	 */
	public static <Key, Value> Cache<Key, Value> createCache(int maxSize) {
		Cache<Key, Value> cache = CacheBuilder.newBuilder().maximumSize(maxSize).build();
		return cache;
	}

	/**
	 * Sums all the values in the specified row of an array.
	 * 
	 * @param array {@code double} array
	 * @param row   row of the array
	 * @return sum of all the values in the specified row of the array
	 */
	public static double sumRow(double[][] array, int row) {
		double result = 0.0;
		for (int i = 0; i < array.length; i++)
			result += array[row][i];
		return result;
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
	 * Fills a three-dimensional {@code double} array with the provided
	 * {@code double}.
	 * 
	 * @param array a three-dimensional {@code double} array
	 * @param value {@code double} used to fill the array
	 */
	public static void fill3dArray(double[][][] array, double value) {
		for (double[][] matrix : array)
			fill2dArray(matrix, value);
	}

	/**
	 * Sets the state of a given node and its parents from a {@code State} object.
	 * 
	 * @param node        node whose state is changed
	 * @param statesNodes {@code State} object with state of the node and its
	 *                    parents
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
}
