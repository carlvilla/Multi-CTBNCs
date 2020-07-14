package com.cig.mctbnc.util;

public class Util {

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

}
