package com.cig.mctbnc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cig.mctbnc.data.representation.State;

public class Util {

	/**
	 * Return the elements of a list "a" except those in "b".
	 * 
	 * @param a
	 *            list
	 * @param b
	 *            elements we do not want in "a"
	 * @return list of "a" without elements "b"
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> filterArray(List<T> a, T... b) {
		List<T> listA = new ArrayList<T>(a);
		listA.removeAll(Arrays.asList(b));
		return listA;
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
		for (int i = 0; i < array.length; i++) {
			largestIdx = array[i] > array[largestIdx] ? i : largestIdx;
		}

		return largestIdx;

	}

	/**
	 * Given a list of lists with the possible states of some variables, this method
	 * returns the Cartesian product between each of the possible state of each
	 * variable.
	 * 
	 * @param statesVariables
	 *            contains for each variable a list with its possible states
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
}
