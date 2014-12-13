package com.wwrkds.datamodels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sort {

	/**
	 * Standard bubblesort with linked related Object array {@code obj}.
	 * {@code obj} will be reordered according to the values in {@code num}. <br>
	 * <br>
	 * {@code ro} is a utils.RankingOrder object that defines ASCENDING or
	 * DESCENDING ranking types.
	 * 
	 * @param num
	 * @param obj
	 * @param ro
	 * @return
	 */
	public static int[] BubbleSort(double[] num, double[] obj, RankingOrder ro) {

		Double[] x = new Double[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}

		Double[] o = null;
		if (obj != null) {
			o = new Double[num.length];
			for (int i = 0; i < num.length; i++) {
				o[i] = obj[i];
			}
		}

		int[] ind = Sort.BubbleSort(x, o, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		if (obj != null) {
			for (int i = 0; i < num.length; i++) {
				obj[i] = o[i];
			}
		}
		return ind;
	}

	/**
	 * Standard bubblesort with linked related Object array {@code obj}.
	 * {@code obj} will be reordered according to the values in {@code num}. <br>
	 * <br>
	 * {@code ro} is a utils.RankingOrder object that defines ASCENDING or
	 * DESCENDING ranking types.
	 * 
	 * @param num
	 * @param obj
	 * @param ro
	 * @return
	 */
	public static int[] BubbleSort(double[] num, int[] obj, RankingOrder ro) {

		Double[] x = new Double[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}

		Integer[] o = null;
		if (obj != null) {
			o = new Integer[num.length];
			for (int i = 0; i < num.length; i++) {
				o[i] = obj[i];
			}
		}

		int[] ind = Sort.BubbleSort(x, o, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		if (obj != null) {
			for (int i = 0; i < num.length; i++) {
				obj[i] = o[i];
			}
		}
		return ind;
	}

	/**
	 * Bubble sort with additional indexing returns. Input array {@code num}
	 * will be resorted in place and the method will return an int [] denoting
	 * the reordering sequence.
	 * 
	 * @param num
	 * @return
	 */
	public static int[] BubbleSort(double[] num, RankingOrder ro) {
		Double[] x = new Double[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}
		int[] ind = Sort.BubbleSort(x, null, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		return ind;
	}

	/**
	 * Standard bubblesort with linked related Object array {@code obj}.
	 * {@code obj} will be reordered according to the values in {@code num}. <br>
	 * <br>
	 * {@code ro} is a utils.RankingOrder object that defines ASCENDING or
	 * DESCENDING ranking types.
	 * 
	 * @param num
	 * @param obj
	 * @param ro
	 * @return
	 */
	public static <T> int[] BubbleSort(double[] num, T[] obj, RankingOrder ro) {
		Double[] x = new Double[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}
		int[] ind = Sort.BubbleSort(x, obj, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		return ind;
	}

	/**
	 * Bubble sort with additional indexing returns. Input array {@code num}
	 * will be resorted in place and the method will return an int [] denoting
	 * the reordering sequence.
	 * 
	 * @param num
	 * @return
	 */
	public static int[] BubbleSort(float[] num, RankingOrder ro) {
		Float[] x = new Float[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}
		int[] ind = Sort.BubbleSort(x, null, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		return ind;
	}

	/**
	 * Standard bubblesort with linked related Object array {@code obj}.
	 * {@code obj} will be reordered according to the values in {@code num}. <br>
	 * <br>
	 * {@code ro} is a utils.RankingOrder object that defines ASCENDING or
	 * DESCENDING ranking types.
	 * 
	 * @param num
	 * @param obj
	 * @param ro
	 * @return
	 */
	public static int[] BubbleSort(int[] num, int[] obj, RankingOrder ro) {

		Integer[] x = new Integer[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}

		Integer[] o = null;
		if (obj != null) {
			o = new Integer[num.length];
			for (int i = 0; i < num.length; i++) {
				o[i] = obj[i];
			}
		}

		int[] ind = Sort.BubbleSort(x, o, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		if (obj != null) {
			for (int i = 0; i < num.length; i++) {
				obj[i] = o[i];
			}
		}
		return ind;
	}

	/**
	 * Bubble sort with additional indexing returns. Input array {@code num}
	 * will be resorted in place and the method will return an int [] denoting
	 * the reordering sequence.
	 * 
	 * @param num
	 * @return
	 */
	public static int[] BubbleSort(int[] num, RankingOrder ro) {

		Integer[] x = new Integer[num.length];
		for (int i = 0; i < num.length; i++) {
			x[i] = num[i];
		}
		int[] ind = Sort.BubbleSort(x, null, ro);
		for (int i = 0; i < num.length; i++) {
			num[i] = x[i];
		}
		return ind;
	}

	@SuppressWarnings({ "rawtypes" })
	public static <V extends Comparable> int[] BubbleSort(List<V> num) {
		int sz = num.size();
		Comparable[] k = num.toArray(new Comparable[sz]);
		int[] idx = Sort.BubbleSort(k, RankingOrder.ASCENDING);
		Sort.reorderInPlace(num, idx);
		return idx;
	}

	@SuppressWarnings({ "rawtypes" })
	public static <V extends Comparable, T> int[] BubbleSort(List<V> num,
			List<T> obj, RankingOrder ro) {
		int sz = num.size();
		Comparable[] k = num.toArray(new Comparable[sz]);
		Object[] v = obj.toArray(new Object[sz]);
		int[] idx = Sort.BubbleSort(k, v, ro);
		Sort.reorderInPlace(num, idx);
		Sort.reorderInPlace(obj, idx);
		return idx;
	}

	@SuppressWarnings({ "rawtypes" })
	public static <V extends Comparable> int[] BubbleSort(List<V> num,
			RankingOrder ro) {
		int sz = num.size();
		Comparable[] k = num.toArray(new Comparable[sz]);
		int[] idx = Sort.BubbleSort(k, ro);
		Sort.reorderInPlace(num, idx);
		return idx;
	}

	@SuppressWarnings("rawtypes")
	public static <V extends Comparable> int[] BubbleSort(V[] num) {
		return Sort.BubbleSort(num, null, RankingOrder.ASCENDING);
	}

	@SuppressWarnings("rawtypes")
	public static <V extends Comparable> int[] BubbleSort(V[] num,
			RankingOrder ro) {
		return Sort.BubbleSort(num, null, ro);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V extends Comparable, T> int[] BubbleSort(V[] num, T[] obj,
			RankingOrder ro) {
		int j;
		boolean flag = true; // set flag to true to begin first pass
		V temp = null; // holding variable
		int tind;
		T tobj = null;
		int[] ind = new int[num.length];
		for (int i = 0; i < num.length; i++) {
			ind[i] = i;
		}

		while (flag) {
			flag = false; // set flag to false awaiting a possible swap
			for (j = 0; j < num.length - 1; j++) {
				if (ro.getDirection() >= 0 ? num[j].compareTo(num[j + 1]) > 0
						: num[j].compareTo(num[j + 1]) < 0) // change to > for
															// ascending sort
				{
					temp = num[j]; // swap elements
					tind = ind[j];
					if (obj != null) {
						tobj = obj[j];
					}
					num[j] = num[j + 1];
					ind[j] = ind[j + 1];
					if (obj != null) {
						obj[j] = obj[j + 1];
					}
					num[j + 1] = temp;
					ind[j + 1] = tind;
					if (obj != null) {
						obj[j + 1] = tobj;
					}
					flag = true; // shows a swap occurred
				}
			}
		}
		return ind;
	}

	public static void main(String[] args) {
		int[] a = { 1, 2, 3, 4, 5, 6, 7, 8, 0 };
		System.out.println(Arrays.toString(a));

		int[] o = Sort.BubbleSort(a, RankingOrder.ASCENDING);
		System.out.println(Arrays.toString(a));
		System.out.println(Arrays.toString(o));
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static byte[] reorder(byte[] arr, int... idx) {
		byte[] ret = new byte[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static char[] reorder(char[] arr, int... idx) {
		char[] ret = new char[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static double[] reorder(double[] arr, int... idx) {
		double[] ret = new double[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static float[] reorder(float[] arr, int... idx) {
		float[] ret = new float[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static int[] reorder(int[] arr, int... idx) {
		int[] ret = new int[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T> List<T> reorder(List<T> arr, int... idx) {
		List<T> ret = new ArrayList<T>();
		for (int element : idx) {
			ret.add(arr.get(element));
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T, U extends Number> List<T> reorder(List<T> arr, List<U> idx) {
		List<T> ret = new ArrayList<T>();
		for (int i = 0; i < idx.size(); i++) {
			ret.add(arr.get(idx.get(i).intValue()));
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T, U extends Number> List<T> reorder(List<T> arr, U[] idx) {
		List<T> ret = new ArrayList<T>();
		for (U element : idx) {
			ret.add(arr.get(element.intValue()));
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static long[] reorder(long[] arr, int... idx) {
		long[] ret = new long[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing. DOES
	 * NOT ALTER THE INPUT LIST. All inputs must be of the same size.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static short[] reorder(short[] arr, int... idx) {
		short[] ret = new short[idx.length];
		for (int i = 0; i < idx.length; i++) {
			ret[i] = arr[idx[i]];
		}
		return ret;
	}

	/**
	 * Reorder the input list of object according to the given indexing.
	 * Reorders the input list in place.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T> void reorderInPlace(List<T> arr, int... idx) {
		ArrayList<T> ret = new ArrayList<T>();
		for (int element : idx) {
			ret.add(arr.get(element));
		}
		arr.clear();
		arr.addAll(ret);
	}

	/**
	 * Reorder the input list of object according to the given indexing.
	 * Reorders the input list in place.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T, U extends Number> void reorderInPlace(List<T> arr,
			List<U> idx) {
		ArrayList<T> ret = new ArrayList<T>();
		for (int i = 0; i < idx.size(); i++) {
			ret.add(arr.get(idx.get(i).intValue()));
		}
		arr.clear();
		arr.addAll(ret);
	}

	/**
	 * Reorder the input list of object according to the given indexing.
	 * Reorders the input list in place.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T, U extends Number> void reorderInPlace(List<T> arr, U[] idx) {
		ArrayList<T> ret = new ArrayList<T>();
		for (U element : idx) {
			ret.add(arr.get(element.intValue()));
		}
		arr.clear();
		arr.addAll(ret);
	}

	/**
	 * Reorder the input list of objects according to the given indexing.
	 * REORDERS the input array in place.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T> void reorderInPlace(T[] arr, int... idx) {
		ArrayList<T> ret = new ArrayList<T>();
		for (int element : idx) {
			ret.add(arr[element]);
		}
		for (int i = 0; i < arr.length & i < ret.size(); i++) {
			arr[i] = ret.get(i);
		}
	}

	/**
	 * Reorder the input list of objects according to the given indexing.
	 * REORDERS the input array in place.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T, U extends Number> void reorderInPlace(T[] arr, List<U> idx) {
		ArrayList<T> ret = new ArrayList<T>();
		for (int i = 0; i < idx.size(); i++) {
			ret.add(arr[idx.get(i).intValue()]);
		}
		for (int i = 0; i < arr.length & i < ret.size(); i++) {
			arr[i] = ret.get(i);
		}
	}

	/**
	 * Reorder the input list of objects according to the given indexing.
	 * REORDERS the input array in place.
	 * 
	 * @param arr
	 * @param idx
	 */
	public static <T, U extends Number> void reorderInPlace(T[] arr, U[] idx) {
		ArrayList<T> ret = new ArrayList<T>();
		for (U element : idx) {
			ret.add(arr[element.intValue()]);
		}
		for (int i = 0; i < arr.length & i < ret.size(); i++) {
			arr[i] = ret.get(i);
		}
	}

	/**
	 * Return the sorted order of the given input list. DOES NOT alter the
	 * ordering of the input object.
	 * 
	 * @param arr
	 * @param ro
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> int[] sortedOrder(List<T> arr, RankingOrder ro) {
		Comparable[] c = new Comparable[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			c[i] = (Comparable) arr.get(i);
		}
		return Sort.BubbleSort(c, ro);
	}

}
