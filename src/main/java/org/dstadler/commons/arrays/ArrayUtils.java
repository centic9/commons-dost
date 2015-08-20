package org.dstadler.commons.arrays;

/**
 * General utilities for arrays.
 *
 * @author dominik.stadler
 *
 */
public class ArrayUtils {
	/**
	 * Call toString() on each element of the array and concatenate the resulting
	 * strings together, separated by the given delimiter.
	 *
	 * @param array Array of elements to print.
	 * @param delimiter The delimiter to use when concatenating the strings,
	 * 				", " leads to the same behavior as Arrays.toString()
	 * @return The resulting string encapsulated in brackets ('[...]')
	 */
	public static String toString(Object[] array, String delimiter) {
		return toString(array, delimiter, "[", "]");
	}

	/**
	 * Call toString() on each element of the array and concatenate the resulting
	 * strings together, separated by the given delimiter and by adding and appending
	 * the given prefix and suffix.
	 *
	 * toString(null, *) => "null"
	 * toString(new Object[0], *, "(", ")") => "()"
	 * toString(new Object[] {"a"}, *, "(", ")") => "(a)"
	 * toString(new Object[] {"a", "b"}, ",", "(", ")") => "(a,b)"
	 *
	 * @param array Array of elements to print.
	 * @param delimiter The delimiter to use when concatenating the strings,
	 * 				", " leads to the same behavior as Arrays.toString()
	 * @param prefix the starting string which is concatenated to the beginning
	 * @param suffix the ending string which is concatenated at the end
	 * @return The resulting string encapsulated in the given prefix and suffix
	 */
	public static String toString(Object[] array, String delimiter, String prefix, String suffix) {
		if (array == null) {
			return "null";
		}

		if (array.length == 0) {
			return prefix+suffix;
		} else if (array.length == 1) {
			return prefix+array[0]+suffix;
		}

		StringBuilder b = new StringBuilder(prefix);
		for (Object element : array) {
			b.append(String.valueOf(element)).append(delimiter);
		}

		// cut off last delimiter
		b.setLength(b.length()-delimiter.length());

		return b.append(suffix).toString();
	}
}
