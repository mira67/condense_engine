package helper_classes;

/* stats
 * 
 * Tools to compute statistics
 */

import java.util.*;

public class Stats {

  /* mean
   * 
   *  Compute the average from an array of float values.
   */
  static public float mean( float[] values ) {
     float sum = 0.0f;

     for (int a = 0; a < values.length; a++) sum += values[a];

     return (float)(sum / values.length);
  }
 

  /* mean
   * 
   *  Compute the average of an array of integer values. 
   */
  static public float mean( int[] values ) {
     float sum = 0.0f;

     for (int a = 0; a < values.length; a++) sum += (float) values[a];

     return (float)(sum / values.length);
  }
  

  /* meanNoBadData
   * 
   * Compute the average of an array of integer values, don't include data
   * that's bad.
   */
  static public float meanNoBadData( int[] values, int bad ) {

    float count = 0f;
    float sum = 0.0f;

    for (int a = 0; a < values.length; a++) {
      if (values[a] != bad) {
        sum += (float) values[a];
        count++;
      }
    }

    return (float)(sum / count);
  }
  

  /* meanNoBadData
   * 
   * Compute the average of an array of float values, don't include data
   * that's bad.
   */
  static public float meanNoBadData( float[] values, float bad ) {

    float count = 0f;
    float sum = 0.0f;

    for (int a = 0; a < values.length; a++) {
      if (values[a] != bad) {
        sum += values[a];
        count++;
      }
    }

    return (float)(sum / count);
  }


  /* standardDeviation
   *
   * Compute the standard deviation of an array of float values.
   *  
   * Bias adjusts the degrees of freedom (df). When computing the standard
   * deviation of an entire population (N), df typically equals N, (set
   * bias = 0). This is the Population Standard Deviation.
   *  
   * If computing the SD of a sample population (n), not the entire
   * population, df is often n-1 (set bias = 1 to compute an unbiased SD).
   * This is the Sample Standard Deviation.
   *  
   * Note: The actual df to compute an unbiased SD can be complex,
   * depending on the sample size, but that is not addressed here. 
   */
  static public float standardDeviation( float[] values, float mean,
		                                 float bias ) {

    float sumOfSquares = 0.0f;

    for (int a = 0; a < values.length; a++) {
      sumOfSquares += Math.pow(values[a] - mean, 2.0);
    }

    return (float)Math.sqrt(sumOfSquares / ((float)values.length - bias));
  }


  /* standardDeviation
   * 
   * Compute the standard deviation of an array of integer
   * values.
   *  
   * See note above about bias and degrees of freedom. 
   */
  static public float standardDeviation( int[] values, float mean,
		                                 float bias) {

    float sumOfSquares = 0.0f;

    for (int a = 0; a < values.length; a++) {
      sumOfSquares += Math.pow((float)values[a] - mean, 2.0);
    }

    return (float)Math.sqrt(sumOfSquares / ((float) values.length - bias));
  }


  /* standardDeviationNoBadData
   * 
   * Compute the standard deviation of an array of floats,
   * ignoring bad data.
   *  
   * See note above about bias and degrees of freedom. 
   */
  static public float standardDeviationNoBadData( float[] values, float mean,
		                                          float bad, float bias ) {

    float sumOfSquares = 0.0f;
    float count = 0f;

    for (int a = 0; a < values.length; a++) {
      if (values[a] != bad) {
        sumOfSquares += Math.pow(values[a] - mean, 2.0);
        count++;
      }
    }

    return (float)Math.sqrt(sumOfSquares / ((float) count - bias));
  }


  /* standardDeviationNoBadData
   * 
   * Compute the standard deviation of an array of integers,
   * ignoring bad data.
   *  
   * See note above about bias and degrees of freedom. 
   */
  static public float standardDeviationNoBadData( int[] values, float mean,
		                                          int bad, float bias ) {

    float sumOfSquares = 0.0f;
    float count = 0f;

    for (int a = 0; a < values.length; a++) {
      if (values[a] != bad) {
        sumOfSquares += Math.pow((float)values[a] - mean, 2.0);
        count++;
      }
    }

    return (float)Math.sqrt(sumOfSquares / ((float) count - bias));
  }
 

  /* median
   * 
   *  Compute the median of an array of float values. 
   */
  static public float median( float[] values ) {
	
	// Handle special cases
    if (values.length == 0) return 0f;
    if (values.length == 1) return values[0];
    
    int middle = values.length/2;

    Arrays.sort( values );

    // An odd number of values. Return the middle one.
    if (values.length % 2 == 1) return values[middle];
    
    // No exact middle. Interpolate a value.
    return (values[middle-1] + values[middle]) / 2.0f; 
  }

}
