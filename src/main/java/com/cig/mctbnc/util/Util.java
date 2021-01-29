package com.cig.mctbnc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.cig.mctbnc.data.representation.State;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
	 * Return the elements of a list "a" except those in "b".
	 * 
	 * @param a list
	 * @param b list of elements to filter from "a"
	 * @return list of "a" without elements "b"
	 */
	public static <T> List<T> filter(List<T> a, List<T> b) {
		List<T> listA = new ArrayList<T>(a);
		listA.removeAll(b);
		return listA;
	}

	/**
	 * Return the elements of a list "a" except "b".
	 * 
	 * @param a list
	 * @param b element to filter from "a"
	 * @return list of "a" without "b"
	 */
	public static <T> List<T> filter(List<T> a, T b) {
		List<T> listA = new ArrayList<T>(a);
		listA.remove(b);
		return listA;
	}

	/**
	 * Return the unique values of an array with Strings.
	 * 
	 * @param array
	 * @return array with unique values
	 */
	public static String[] getUnique(String[] array) {
		return Arrays.stream(array).distinct().toArray(String[]::new);
	}

	/**
	 * Returns the index of a String in an array.
	 * 
	 * @param array
	 * @param element
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
	 * @param array
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
	 * Returns entry with the largest value of a map.
	 * 
	 * @param map
	 * @return entry of the largest value
	 */
	public static <k, v extends Comparable<v>> Entry<k, v> getEntryLargestValue(Map<k, v> map) {
		Entry<k, v> maxEntry = Collections.max(map.entrySet(),
				(Entry<k, v> e1, Entry<k, v> e2) -> e1.getValue().compareTo(e2.getValue()));
		return maxEntry;
	}

	/**
	 * Transform a list into an array of Strings.
	 * 
	 * @param list
	 * @return array
	 */
	public static String[] listToArray(List<?> list) {
		return list.stream().toArray(String[]::new);
	}

	/**
	 * Objects in a list are shuffled.
	 * 
	 * @param <T>  type of the elements to shuffle
	 * @param list
	 * @param seed
	 */
	public static <T> void shuffle(List<T> list, Integer seed) {
		Random rd = new Random();
		if (seed != null)
			rd.setSeed(seed);
		Collections.shuffle(list, rd);
	}

	/**
	 * Kronecker delta function. It returns 1 if 'a' and 'b' are equal and 0
	 * otherwise.
	 * 
	 * @param a
	 * @param b
	 * @return 1 if 'a' and 'b' are equal and 0 otherwise
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
		else {
			// Recursion is used to calculate the product.
			List<State> statesHead = statesVariables.get(0);
			List<State> statesTail = cartesianProduct(statesVariables.subList(1, statesVariables.size()));
			// If "statesTail" is empty, we are obtaining the possible combinations of
			// states for a unique list (statesHead). In that case, the list itself is the
			// solution (despite the fact that the Cartesian product would be the empty
			// list).
			if (statesTail.isEmpty()) {
				return statesHead;
			}
			// If "statesTail" is not empty, we are obtaining the possible combinations of
			// states between two lists
			else {
				for (State stateHead : statesHead) {
					for (State stateTail : statesTail) {
						State tmpState = new State();
						tmpState.addEvents(stateHead.getEvents());
						tmpState.addEvents(stateTail.getEvents());
						result.add(tmpState);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Build a cache of a certain size with the specified key and value types.
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
	 * Sum all the values in a row of an array.
	 * 
	 * @param cim
	 * @param i
	 */
	public static double sumRow(double[][] array, int row) {
		double result = 0.0;
		for (int i = 0; i < array.length; i++)
			result += array[row][i];
		return result;
	}

	/**
	 * Fill a double 2d array with the provided double.
	 * 
	 * @param mxy
	 * @param mxyHP
	 */
	public static void fill2dArray(double[][] array, double value) {
		for (double[] row : array)
			Arrays.fill(row, value);
	}

	/**
	 * Fill a double 3d array with the provided double.
	 * 
	 * @param mxy
	 * @param mxyHP
	 */
	public static void fill3dArray(double[][][] array, double value) {
		for (double[][] matrix : array)
			fill2dArray(matrix, value);
	}
}
