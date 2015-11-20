package condense;

import java.io.*;
import java.util.*;

/* Tools
 * 
 * Odds and ends methods.
 * 
 */

public class Tools extends GeoObject {

	protected static boolean debug = true; // Debug messages?
	protected static boolean status = true; // Status messages?
	protected static boolean warnings = true; // Warning messages?

	/*
	 * randomInt
	 * 
	 * Random number generator. Given an integer N, this returns another integer
	 * in the range of 0 to N-1.
	 */

	public static int randomInt(int n) {
		return (int) (Math.random() * (double) n);
	}

	/*
	 * inByteRange
	 * 
	 * Ensures that an integer is within the storage range of an unsigned byte,
	 * 0 - 255. Values out of range are truncated.
	 */
	public static int inByteRange(int i) {
		if (i > 255)
			i = 255;
		if (i < 0)
			i = 0;
		return i;
	}

	/*
	 * LSBtoMSB
	 * 
	 * Converts a double in LSB (least significant byte order, or "little
	 * endian") to a double in MSB (most significant byte order, "big endian").
	 * The input data is a series of 8 bytes representing an LSB double.
	 * 
	 * No parameter checking is done, so if the user inputs a byte array of more
	 * or less than 8 bytes, well, sayonara bojo.
	 * 
	 * I have moral objections to working with data at this low of a level.
	 * Don't let it happen again.
	 */
	public static Double LSBtoMSB(byte[] inBytes) {

		// "Accumulate" the re-arranged bytes in a long integer.
		long accumlator = 0;

		// Our index into the byte order.
		int byteIndex = 0;

		// Increment through each of the 8 bytes, re-arranging the order.
		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
			// A cast to long is required or else the shift would be done modulo
			// 32.
			accumlator |= ((long) (inBytes[byteIndex] & 0xff)) << shiftBy;
			byteIndex++;
		}

		// Return the long integer as a double. Ta-da!
		return Double.longBitsToDouble(accumlator);
	}

	/*
	 * intToByteArray
	 * 
	 * Converts an integer into a byte array.
	 */
	public static byte[] intToByteArray(int value) {
		return new byte[] { (byte) value, (byte) (value >>> 8),
				(byte) (value >>> 16), (byte) (value >>> 24) };
	}

	/*
	 * reverse
	 * 
	 * Bit-reverse an integer.
	 */
	public static int reverse(int i) {
		   return Integer.reverse(i);
	}

	/*
	 * swap
	 * 
	 * Byte-swap a double.
	 */
	public static double swap(double value) {
		   long longValue = Double.doubleToLongBits (value);
		    longValue = swap (longValue);
		    return Double.longBitsToDouble (longValue);
	}

	/*
	 * swap
	 * 
	 * Byte-swap a float.
	 */
	public static float swap(float value) {
		int intValue = Float.floatToRawIntBits (value);
	    intValue = swap (intValue);
	    return Float.intBitsToFloat (intValue);
	}
	
	/*
	 * Byte swap a single long value.
	 * 
	 */
	public static long swap(long value) {
	    long b1 = (value >>  0) & 0xff;
	    long b2 = (value >>  8) & 0xff;
	    long b3 = (value >> 16) & 0xff;
	    long b4 = (value >> 24) & 0xff;
	    long b5 = (value >> 32) & 0xff;
	    long b6 = (value >> 40) & 0xff;
	    long b7 = (value >> 48) & 0xff;
	    long b8 = (value >> 56) & 0xff;

	    return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
	           b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
	}
	  

	  
	/*
	 * intArrayToByteArray
	 * 
	 * Converts an integer array into a byte array. No error checking. May
	 * produce unexpected results for integers out of the 0-255 range.
	 */
	public static byte[] intArrayToByteArray(int[] values) {
		return new byte[] { (byte) values[0], (byte) values[1],
				(byte) values[2], (byte) values[3] };
	}

	/*
	 * byteArrayToInt
	 * 
	 * Converts a byte array into an integer
	 * Little-endian byte order
	 */
	public static int byteArrayToInt(byte[] bytes) {
		int answer = 0;

		if (bytes == null)
			return answer;

		for (int i = 0; i < bytes.length; i++) {
			answer = answer | ((bytes[i] & 0xff) << i * 8);
		}

		return answer;

	}

	/*
	 * byteArrayToShort
	 * 
	 * Converts a byte array into a short integer. No error checking.
	 * Little-endian byte order
	 */
	public static short byteArrayToShort(byte[] bytes) {
		short answer = 0;

		if (bytes == null)
			return answer;

		for (short i = 0; i < bytes.length; i++) {
			answer = (short) (answer | ((bytes[i] & 0xff) << i * 8));
		}

		return answer;

	}

	/*
	 * unsignedByteToInt
	 * 
	 * Converts an unsigned byte into an integer.
	 */
	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	/*
	 * unsignedByteToFloat
	 * 
	 * Converts an unsigned byte into an floating point number.
	 */
	public static float unsignedByteToFloat(byte b) {
		return (float) ((int) b & 0xFF);
	}

	/*
	 * intToUnsignedByte
	 * 
	 * Converts a positive integer to an unsigned byte. Only the least
	 * significant 8 bits of the integer are used (byte 0), and assumes the
	 * integer is between 0-255. Anything out of range is processed without
	 * warning and may produce unexpected results.
	 * 
	 * Note: if you print out the resulting byte, Java doesn't know that's it's
	 * unsigned... so a one in the highest bit position (MSB) will be
	 * interpreted as a sign. It is up to the user to "know" that the byte is
	 * unsigned and treat it thusly; i.e., i=255 results in 0b11111111, printed
	 * as byte=-1. Gotta love Java.
	 */
	public static byte intToUnsignedByte(int i) {
		return (byte) i;
	}

	/*
	 * parseIntByte
	 * 
	 * Returns the specified byte from the integer, as an integer. Assumes
	 * everything is unsigned.
	 */
	public static int parseIntByte(int value, int byteNumber) {
		return (Tools.unsignedByteToInt((byte) (value >>> 8 * byteNumber)));
	}

	/*
	 * insertByte
	 * 
	 * Insert a byte (from an integer) into the correct byte in another integer.
	 */
	public static int insertByte(int newByte, int existingInt, int position) {
		byte[] bytes = Tools.intToByteArray(existingInt);

		bytes[position] = (byte) newByte;

		return Tools.byteArrayToInt(bytes);
	}

	/*
	 * swap
	 * 
	 * Swaps the byte order of an integer: little-endian integer (LSB)
	 * to big-endian (MSB), and vice-versa.
	 */
	public static int swap(int i) {
		byte[] b = Tools.intToByteArray( i );
		
		byte[] b2 = new byte[4];
		b2[3] = b[0];
		b2[2] = b[1];
		b2[1] = b[2];
		b2[0] = b[3];
		
		return Tools.byteArrayToInt(b2);
	}

	/*
	 * reverse
	 * 
	 * Reverses the bit order of a double.
	 */
	public static double reverse(double x) {
		
		long y = Long.reverse(Double.doubleToLongBits(x));
		
		return Double.longBitsToDouble(y);
	}

	/*
	 * reverse
	 * 
	 * Reverses the bit order of a float.
	 */
	public static float reverse(float x) {
		
		int y = Integer.reverse(Float.floatToIntBits(x));
		
		return Float.intBitsToFloat(y);
	}

	/*
	 * reverse
	 * 
	 * Reverses the bit order of a long integer.
	 */
	public static long reverse(long x) {
		
		return Long.reverse(Double.doubleToLongBits(x));
	}

	/*
	 * uniqueFileName
	 * 
	 * Return a unique file name, just in case the supplied one already exists.
	 */
	public static String uniqueFileName(String filename) {

		Integer fileCount = 0;
		String newFilename = filename;
		File file;

		// If the file already exists, add "_n" to it.
		try {
			do {
				file = new File(newFilename);
				fileCount++;

				if (file.exists()) {
					// File exists. Append a number to the end of the first
					// token before the period.
					StringTokenizer st = new StringTokenizer(filename, ".");
					newFilename = st.nextToken() + "_" + fileCount.toString();

					while (st.hasMoreTokens())
						newFilename = newFilename + "." + st.nextToken();
				}
			} while (file.exists());
		} catch (Exception e) {
			Tools.errorMessage("Tools", "uniqueFileName", "when opening file: "
					+ filename, e);
		}

		if (fileCount > 1) {
			Tools.warningMessage("Renamed your file. Old name: "
					+ filename + "  New name: " + newFilename);
		}

		return newFilename;
	}

	/*
	 * scaleIntArray2D
	 * 
	 * Scale a 2D integer array of data.
	 */
	public static int[][] scaleIntArray2D(int[][] array, int min, int max) {

		int rows = array.length;
		int cols = array[0].length;

		float range = (float) max - (float) min;

		float minData = 999999;
		float maxData = -999999;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (array[r][c] == NODATA)
					continue;
				if (array[r][c] < minData)
					minData = array[r][c];
				if (array[r][c] > maxData)
					maxData = array[r][c];
			}
		}

		float dataRange = maxData - minData;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				array[r][c] = (int) (min + range
						* ((float) array[r][c] - minData) / dataRange);
			}
		}

		return array;
	}

	/*
	 * scaleIntArray2D
	 * 
	 * Scale a 2D integer array of data.
	 */
	public static int[][] scaleIntArray2DExcludeBad(int[][] array, int min, int max, int lowBad, int highBad) {

		int rows = array.length;
		int cols = array[0].length;

		float range = (float) max - (float) min;

		float minData = 999999;
		float maxData = -999999;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (array[r][c] == NODATA || array[r][c] < lowBad || array[r][c] > highBad)
					continue;
				if (array[r][c] < minData)
					minData = array[r][c];
				if (array[r][c] > maxData)
					maxData = array[r][c];
			}
		}

		float dataRange = maxData - minData;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				array[r][c] = (int) (min + range
						* ((float) array[r][c] - minData) / dataRange);
			}
		}

		return array;
	}

	/*
	 * scaleShortArray2D
	 * 
	 * Scale a 2D array of short integer data.
	 */
	public static short[][] scaleShortArray2D(short[][] array, int min, int max) {

		int rows = array.length;
		int cols = array[0].length;

		float range = (float) max - (float) min;

		short minData = 32767;
		short maxData = -32767;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (array[r][c] == NODATA)
					continue;
				if (array[r][c] < minData)
					minData = array[r][c];
				if (array[r][c] > maxData)
					maxData = array[r][c];
			}
		}

		float dataRange = maxData - minData;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (array[r][c] != NODATA) array[r][c] = (short) (min + range
						* ((float) array[r][c] - minData) / dataRange);
			}
		}

		return array;
	}

	/*
	 * discardBadData
	 * 
	 * In a 2D integer array, filter out any bad data (replace with NODATA
	 * value).
	 */
	public static int[][] discardBadData(int[][] array, int min, int max) {

		int rows = array.length;
		int cols = array[0].length;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (array[r][c] < min)
					array[r][c] = NODATA;
				if (array[r][c] > max)
					array[r][c] = NODATA;
			}
		}

		return array;
	}

	/*
	 * discardBadData
	 * 
	 * In a 2D short array, filter out any bad data (replace with NODATA
	 * value).
	 */
	public static short[][] discardBadData(short[][] array, int min, int max) {

		int rows = array.length;
		int cols = array[0].length;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (array[r][c] < min)
					array[r][c] = NODATA;
				if (array[r][c] > max)
					array[r][c] = NODATA;
			}
		}

		return array;
	}

	/*
	 * shortArrayToInteger
	 * 
	 * Convert an array of shorts to an array of integers. Also just because.
	 */
	public static int[][] shortArrayToInteger(short array[][]) {

		int rows = array.length;
		int cols = array[0].length;

		int[][] newArray = new int[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				newArray[r][c] = (int) array[r][c];
			}
		}

		return newArray;
	}

	/*
	 * doubleArrayToInteger
	 * 
	 * Convert an array of doubles to an array of integers. Just because.
	 */
	public static int[][] doubleArrayToInteger(double array[][]) {

		int rows = array.length;
		int cols = array[0].length;

		int[][] newArray = new int[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				newArray[r][c] = (int) Math.round(array[r][c]);
			}
		}

		return newArray;
	}

	/*
	 * doubleArrayToShort
	 * 
	 * Convert an array of doubles to an array of short integers.
	 * Warning: no error checking.
	 */
	public static short[][] doubleArrayToShort(double array[][]) {

		int rows = array.length;
		int cols = array[0].length;

		short[][] newArray = new short[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				newArray[r][c] = (short) Math.round(array[r][c]);
			}
		}

		return newArray;
	}

	/*
	 * doubleArrayToFloat
	 * 
	 * Convert an array of doubles to an array of floats. Also just because.
	 */
	public static float[][] doubleArrayToFloat(double array[][]) {

		int rows = array.length;
		int cols = array[0].length;

		float[][] newArray = new float[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				newArray[r][c] = (float) array[r][c];
			}
		}

		return newArray;
	}

	/*
	 * printMemory
	 */
	public static void printMemory() {

		// Total number of processors or cores available to the JVM
		System.out.println("Available processors (cores): "
				+ Runtime.getRuntime().availableProcessors());

		// Total amount of free memory available to the JVM
		System.out.println("Free memory (Mbytes): "
				+ Runtime.getRuntime().freeMemory() / 1000000);

		// This will return Long.MAX_VALUE if there is no preset limit
		long maxMemory = Runtime.getRuntime().maxMemory() / 1000000;
		// Maximum amount of memory the JVM will attempt to use
		System.out.println("Maximum memory (Mbytes): "
				+ (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

		// Total memory currently in use by the JVM
		System.out.println("Total memory (Mbytes): "
				+ Runtime.getRuntime().totalMemory() / 1000000);
	}

	/*
	 * printMemoryShort
	 */
	public static void printMemoryShort() {

		long maxMem = Runtime.getRuntime().maxMemory() / 1000000;
		long freeMem = Runtime.getRuntime().freeMemory() / 1000000;
		long totalMem = Runtime.getRuntime().totalMemory() / 1000000;
		long usedMem = totalMem - freeMem;

		// Total number of processors or cores available to the JVM
		System.out.println("Cores: "
				+ Runtime.getRuntime().availableProcessors()
				+ "  JVM max avail MB = " + maxMem + "  Currently alotted = "
				+ totalMem + "  In use = " + usedMem);
	}

	/*
	 * errorMessage
	 * 
	 * Standardized format for printing errors. Bail-out afterward.
	 */
	public static void errorMessage(String className, String method, String message, Exception e) {
		System.out.flush();
		System.err.println(">>>> Error: " + className + "::" + method + ": " + message);
		System.err.println("Exception: " + e);
		System.exit(1);
	}

	/*
	 * warningMessage
	 * 
	 * Standardized format for printing warnings.
	 */
	public static void warningMessage(String message) {
		System.out.flush();
		if (warnings)
			System.out.println(">> Warning: " + message);
	}

	/* message
	 * 
	 * Just print a message. No way to turn it off.
	 */
	public static void message(String message) {
		System.out.println(message);
		System.out.flush();

	}

	/* messageNoRTLF
	 * 
	 * Just print a message. No return-linefeed, and no way to turn it off.
	 */
	public static void messageNoRTLF(String message) {
		System.out.print(message);
		System.out.flush();

	}

	public static void setWarnings(boolean flag) {
		warnings = flag;
	}

	public static boolean warnings() {
		return warnings;
	}

	/*
	 * debugMessage
	 * 
	 * Standardized format for printing debug messages, selectable on/off.
	 */
	public static void debugMessage(String message) {
		if (debug)
			System.out.println(message);
		System.out.flush();
	}

	public static void setDebug(boolean flag) {
		debug = flag;
	}

	public static boolean debug() {
		return debug;
	}

	/*
	 * statusMessage
	 * 
	 * Standardized format for printing status messages, selectable on/off.
	 */
	public static void statusMessage(String message) {
		if (status)
			System.out.println(message);
		System.out.flush();
	}

	public static void setStatus(boolean flag) {
		status = flag;
	}

	public static boolean status() {
		return status;
	}

	public static void exit( int s ) {
		System.out.flush();
		System.exit(s);
	}

	/*
	 * parseString
	 * 
	 * Snarf out a portion of a string, using 'separator' as a separator
	 * character. Good for things like extracting data from a CSV file.
	 */
	public static String parseString(String line, int word, String separator) {

		String item = "";

		int start = 0;

		while (word > 0) {
			word--;
			start = line.indexOf(separator, start) + 1;
		}

		// Truncate the string up to the first separator
		item = line.substring(start, line.length());

		// Find the next occurrence of the separator.
		int end = item.indexOf(separator, 0);

		// Any other separator found? No? Set the end correctly.
		if (end > item.length() || end == -1)
			end = item.length();

		// Truncate any stuff beyond (and including) the next separator
		item = item.substring(0, end);
		return item;
	}

	/*
	 * removeCharacters
	 * 
	 * Remove any particular character from a string.
	 */
	public static String removeCharacters(String line, Character c) {

		String newString = "";

		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) != c) {
				newString = newString
						.concat(Character.toString(line.charAt(i)));
			}
		}

		return newString;
	}
	
	/*
	 * findFile
	 * 
	 * Search a directory for a file containing a specified string in its name.
	 * If more than one file is found, return the first one, with the full path
	 * and file name.
	 */
	public static String findFile( String path, String searchString ) {

		File dir = new File(path);

		File[] matches = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(searchString);
			}
		});

		// Did we find a file with that search string? If yes, use the first occurrence.
		if (matches != null) {
			if (matches.length != 0)
				return matches[0].getPath();
		}

		return null;
	}
	
	/*
	 * findFiles
	 * 
	 * Return all the files in one directory that match a search string.
	 */
	public static File[] findFiles( String path, String searchString ) {

		File dir = new File(path);

		File[] matches = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(searchString);
			}
		});

		return matches;
	}
	
	/*
	 * copyFromIntArray
	 * 
	 * Convert a 2-dimensional integer array to a double array.
	 */
	public static double[][] copyFromIntArray(int[][] source) {
	    double[][] dest = new double[source.length][source[0].length];
	    for (int r = 0; r < source.length; r++) {
	    	for (int c = 0; c < source[0].length; c++) {
	    		dest[r][c] = source[r][c];
	    	}
	    }
	    return dest;
	}
}
