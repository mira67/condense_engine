package helper_classes;

/* stats
 * 
 * Tools to compute statistics
 */

import java.util.*;

public class Stats {

	/*
	 * mean
	 * 
	 * Compute the average from an array of float values.
	 */
	static public float mean(float[] values) {
		float sum = 0.0f;

		for (int a = 0; a < values.length; a++)
			sum += values[a];

		return (float) (sum / values.length);
	}

	/*
	 * mean
	 * 
	 * Compute the average of an array of integer values.
	 */
	static public float mean(int[] values) {
		float sum = 0.0f;

		for (int a = 0; a < values.length; a++)
			sum += (float) values[a];

		return (float) (sum / values.length);
	}

	/*
	 * meanNoBadData
	 * 
	 * Compute the average of an array of integer values, don't include data
	 * that's bad.
	 */
	static public float meanNoBadData(int[] values, int bad) {

		float count = 0f;
		float sum = 0.0f;

		for (int a = 0; a < values.length; a++) {
			if (values[a] != bad) {
				sum += (float) values[a];
				count++;
			}
		}

		return (float) (sum / count);
	}

	/*
	 * meanNoBadData
	 * 
	 * Compute the average of an array of float values, don't include data
	 * that's bad.
	 */
	static public float meanNoBadData(float[] values, float bad) {

		float count = 0f;
		float sum = 0.0f;

		for (int a = 0; a < values.length; a++) {
			if (values[a] != bad) {
				sum += values[a];
				count++;
			}
		}

		return (float) (sum / count);
	}

	/*
	 * meanNoBadData2d
	 * 
	 * Compute the average at each location of a 2-D array of double values,
	 * returning a 2-D array of doubles. Don't include bad data. Assumes the
	 * 'values' have been summed up, and that population is the total number
	 * of samples.
	 */
	static public double[][] meanNoBadData2d( double[][] values, int[][] population,
			int badData) {

		double out[][] = new double[values.length][values[0].length];

		for (int r = 0; r < values.length; r++) {
			for (int c = 0; c < values[0].length; c++) {
				if (population[r][c] > 0 && values[r][c] != badData) {
					out[r][c] = values[r][c] / (double) population[r][c];
				} else {
					out[r][c] = (double) badData;
				}
			}
		}

		return out;
	}

	/*
	 * standardDeviation
	 * 
	 * Compute the standard deviation of an array of float values.
	 * 
	 * Bias adjusts the degrees of freedom (df). When computing the standard
	 * deviation of an entire population (N), df typically equals N, (set bias =
	 * 0). This is the Population Standard Deviation.
	 * 
	 * If computing the SD of a sample population (n), not the entire
	 * population, df is often n-1 (set bias = 1 to compute an unbiased SD).
	 * This is the Sample Standard Deviation.
	 * 
	 * Note: The actual df to compute an unbiased SD can be complex, depending
	 * on the sample size, but that is not addressed here.
	 */
	static public float standardDeviation(float[] values, float mean, float bias) {

		float sumOfSquares = 0.0f;

		for (int a = 0; a < values.length; a++) {
			sumOfSquares += Math.pow(values[a] - mean, 2.0);
		}

		return (float) Math.sqrt(sumOfSquares / ((float) values.length - bias));
	}

	/*
	 * standardDeviation
	 * 
	 * Compute the standard deviation of an array of integer values.
	 * 
	 * See note above about bias and degrees of freedom.
	 */
	static public float standardDeviation(int[] values, float mean, float bias) {

		float sumOfSquares = 0.0f;

		for (int a = 0; a < values.length; a++) {
			sumOfSquares += Math.pow((float) values[a] - mean, 2.0);
		}

		return (float) Math.sqrt(sumOfSquares / ((float) values.length - bias));
	}

	/*
	 * standardDeviationNoBadData
	 * 
	 * Compute the standard deviation of an array of floats, ignoring bad data.
	 * 
	 * See note above about bias and degrees of freedom.
	 */
	static public float standardDeviationNoBadData(float[] values, float mean,
			float bad, float bias) {

		float sumOfSquares = 0.0f;
		float count = 0f;

		for (int a = 0; a < values.length; a++) {
			if (values[a] != bad) {
				sumOfSquares += Math.pow(values[a] - mean, 2.0);
				count++;
			}
		}

		return (float) Math.sqrt(sumOfSquares / ((float) count - bias));
	}

	/*
	 * standardDeviationNoBadData
	 * 
	 * Compute the standard deviation of an array of integers, ignoring bad
	 * data.
	 * 
	 * See note above about bias and degrees of freedom.
	 */
	static public double standardDeviationNoBadData(int[] values, double mean,
			int bad, double bias) {

		float sumOfSquares = 0.0f;
		float count = 0f;

		for (int a = 0; a < values.length; a++) {
			if (values[a] != bad) {
				sumOfSquares += Math.pow((float) values[a] - mean, 2.0);
				count++;
			}
		}

		return (float) Math.sqrt(sumOfSquares / ((float) count - bias));
	}

	/*
	 * standardDeviationNoBadData2d
	 * 
	 * Compute the SD at each location of a 2-D array of double values,
	 * where the doubles are the sums of the differences squared.
	 */
	static public double[][] standardDeviationNoBadData2d(double[][] values,
			int population[][], double bad, double bias) {
		
		double out[][] = new double[values.length][values[0].length];

		for (int r = 0; r < values.length; r++) {
			for (int c = 0; c < values[0].length; c++) {

				if (values[r][c] != bad && (population[r][c] - bias) > 0.0) {
					out[r][c] = Math.sqrt( values[r][c] / (population[r][c] - bias));					
				}
				else {
					out[r][c] = bad;
				}
				
			}
		}

		return out;
	}

	/*
	 * median
	 * 
	 * Compute the median of an array of float values.
	 */
	static public float median(float[] values) {

		// Handle special cases
		if (values.length == 0)
			return 0f;
		if (values.length == 1)
			return values[0];

		int middle = values.length / 2;

		Arrays.sort(values);

		// An odd number of values. Return the middle one.
		if (values.length % 2 == 1)
			return values[middle];

		// No exact middle. Interpolate a value.
		return (values[middle - 1] + values[middle]) / 2.0f;
	}

}
