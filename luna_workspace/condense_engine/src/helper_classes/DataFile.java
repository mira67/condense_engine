package helper_classes;

import java.util.*;
import java.io.*;

/* DataFile
 * 
 * A class to encapsulate data file i/o.
 */

public class DataFile extends GeoObject {

	private String filename;
	private File file;
	private DataInputStream dataInputStream = null;
	private DataOutputStream dataOutputStream = null;

	private boolean fileIsReadable = false;
	private boolean fileIsWritable = false;

	// Constructors
	public DataFile() {
	};

	public DataFile(String filename) throws Exception {
		this.filename = filename;

		Tools.debugMessage("  DataFile::DataFile: file = " + filename);

		try {
			open();
		} catch (Exception e) {
			throw (e);
		}
	}

	/*
	 * open
	 */
	public void open() throws Exception {
		fileIsReadable = false;
		fileIsWritable = false;

		// Open the file.
		try {
			file = new File(filename);
			dataInputStream = new DataInputStream(new FileInputStream(file));

		} catch (FileNotFoundException noFile) {
			throw (noFile);
		}

		fileIsReadable = true;
	}

	/*
	 * create
	 * 
	 * Create a file for writing.
	 */
	public void create(String filename) throws Exception {
		fileIsReadable = false;
		fileIsWritable = false;

		// Open the file.
		try {
			file = new File(filename);
			dataOutputStream = new DataOutputStream(new FileOutputStream(file));
		} catch (Exception e) {
			throw (e);
		}

		this.filename = filename;
		fileIsWritable = true;
	}

	public String getName() {
		return filename;
	}

	public long length() {
		return file.length();
	}

	/*
	 * close
	 * 
	 * Close the input stream.
	 */
	public void close() {
		try {
			dataInputStream.close();
		} catch (Throwable t) {
		}

		fileIsReadable = false;
		fileIsWritable = false;
	}

	/*
	 * readStrings
	 * 
	 * Read string data from the file, each line is one string.
	 */
	public ArrayList<String> readStrings() throws Exception {

		ArrayList<String> strings = new ArrayList<String>();

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readStrings: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		try {
			close();
		} catch (Exception wutever) {
			throw wutever;
		}

		Tools.debugMessage("    DataFile::readStrings: reading file "
				+ filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));

		String line;

		while ((line = br.readLine()) != null) {
			strings.add(line);
		}
		br.close();

		open();

		Tools.debugMessage("DataFile::readStrings: lines read = "
				+ strings.size());

		return strings;
	}

	/*
	 * readByte
	 * 
	 * Read a byte from the data file.
	 */
	public byte readByte() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readByte: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		byte b;

		try {
			// Read in the data
			b = dataInputStream.readByte();
		} catch (Exception e) {
			Tools.warningMessage("DataFile::readByte: error on read - " + e);
			throw (e);
		}

		return b;
	}

	/*
	 * readBytes
	 * 
	 * Read the byte data from the file.
	 */
	public byte[] readBytes() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readBytes: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		Tools.debugMessage("    DataFile::readBytes: reading file " + filename);

		// Get the size of the file
		long length = file.length();
		Tools.debugMessage("    DataFile::readBytes: file length = " + length
				+ " bytes");

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		try {
			// Read in the data
			for (int i = 0; i < length; i++) {
				bytes[i] = dataInputStream.readByte();
			}
		} catch (Exception up) {
			Tools.warningMessage("DataFile::readBytes: error on read - " + up);
			throw (up);
		}

		return bytes;
	}

	/*
	 * readShort
	 * 
	 * Read a short integer from the file.
	 */
	public short readShort() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readShort: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		Tools.debugMessage("    DataFile::readShort: reading file " + filename);

		// Get the size of the file in bytes
		long length = file.length();
		Tools.debugMessage("    DataFile::readShort: file length = " + length
				+ " bytes, shorts = " + length / 2);

		// Create the byte array to hold the data. Short integer = 2 bytes.
		short data = 0;

		try {
			// Read in the data
			data = dataInputStream.readShort();
		} catch (Exception e) {
			Tools.warningMessage("DataFile::readShort: error on read - " + e);
			throw (e);
		}

		return data;
	}

	/*
	 * readShorts
	 * 
	 * Read short integer data from the file.
	 */
	public short[] readShorts() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readShorts: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		Tools.debugMessage("    DataFile::readShorts: reading file " + filename);

		// Get the size of the file in bytes
		long length = file.length();
		Tools.debugMessage("    DataFile::readShorts: file length = " + length
				+ " bytes, shorts = " + length / 2);

		// Create the byte array to hold the data. Short integer = 2 bytes.
		short[] shorts = new short[(int) length / 2];

		try {
			// Read in the data
			for (int i = 0; i < length / 2; i++) {
				shorts[i] = dataInputStream.readShort();
			}
		} catch (Exception up) {
			Tools.warningMessage("DataFile::readShorts: error on read - " + up);
			throw (up);
		}

		return shorts;
	}

	/*
	 * readLongs
	 * 
	 * Read long integer data from the file (8 bytes).
	 */
	public long[] readLongs() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readLongs: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		Tools.debugMessage("    DataFile::readLongs: reading file " + filename);

		// Get the size of the file in bytes
		long length = file.length();
		Tools.debugMessage("    DataFile::readLongs: file length = " + length
				+ " bytes, longs = " + length / 8);

		// Create the byte array to hold the data. Long integer = 8 bytes.
		long[] longs = new long[(int) length / 8];

		try {
			// Read in the data
			for (int i = 0; i < length / 8; i++) {
				longs[i] = dataInputStream.readLong();
			}
		} catch (Exception up) {
			Tools.warningMessage("DataFile::readLongs: error on read - " + up);
			throw (up);
		}

		return longs;
	}

	/*
	 * readInt
	 * 
	 * Read a single integer from the file.
	 */
	public int readInt() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readInt: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		int i;

		try {
			i = dataInputStream.readInt();
		} catch (Exception e) {
			Tools.warningMessage("DataFile::readInt: could not read integer");
			throw (e);
		}

		return i;
	}

	/*
	 * readDouble
	 * 
	 * Read a double floating point number from the file.
	 */
	public double readDouble() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readInt: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		double x;

		try {
			x = dataInputStream.readDouble();
		} catch (Exception e) {
			Tools.warningMessage("DataFile::readDouble: could not read double");
			throw (e);
		}

		return x;
	}

	/*
	 * readInts
	 * 
	 * Read integer data from the file.
	 */
	public int[] readInts() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readInts: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		Tools.debugMessage("    DataFile::readInts: reading file " + filename);

		// Get the size of the file in bytes
		long length = file.length();
		Tools.debugMessage("    DataFile::readInts: file length = " + length
				+ " bytes, Integers = " + length / 4);

		// Create the byte array to hold the data. Integer = 4 bytes.
		int[] ints = new int[(int) length / 4];
		Tools.debugMessage("    DataFile::readInts: integer array length = "
				+ ints.length);

		try {
			// Read in the data
			for (int i = 0; i < length / 4; i++) {
				ints[i] = dataInputStream.readInt();
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readInts: error on read - " + up);
			throw (up);
		}

		return ints;
	}

	/*
	 * readDoubles
	 * 
	 * Read double data from the file.
	 */
	public double[] readDoubles() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readDoubles: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		// Create the array to hold the data. Double = 8 bytes.
		double[] doubles = new double[(int) length / 8];

		try {
			// Read in the data
			for (int i = 0; i < length / 8; i++) {
				doubles[i] = dataInputStream.readDouble();
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readDoubles: error on read - "
					+ up);
			throw (up);
		}

		return doubles;
	}

	/*
	 * readDoubles2D
	 * 
	 * Reads a 2-dimensional double data from the file.
	 */
	public double[][] readDoubles2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readDoubles2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Create the array to hold the data. Double = 8 bytes.
		double[][] doubles = new double[rows][cols];

		try {
			// Read in the data
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					doubles[r][c] = dataInputStream.readDouble();
				}
			}
		} catch (Exception e) {
			Tools.warningMessage(" DataFile::readDoubles2D: error on read - "
					+ e);
			throw (e);
		}

		return doubles;
	}

	/*
	 * readFloats
	 * 
	 * Read float data from the file.
	 */
	public float[] readFloats() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readFloats: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		Tools.debugMessage("    DataFile::readFloats: reading file " + filename);

		// Get the size of the file in bytes
		long length = file.length();
		Tools.debugMessage("    DataFile::readFloats: file length = " + length
				+ " bytes, Floats = " + length / 4);

		// Create the array to hold the data. Float = 4 bytes.
		float[] floats = new float[(int) length / 4];
		Tools.debugMessage("    DataFile::readFloats: float array length = "
				+ floats.length);

		try {
			// Read in the data
			for (int i = 0; i < length / 4; i++) {
				floats[i] = dataInputStream.readFloat();
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readFloats: error on read - " + up);
			throw (up);
		}

		return floats;
	}

	/*
	 * writeDoubles
	 * 
	 * Write double data to the file.
	 */
	public void writeDoubles(double[] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeDoubles: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				dataOutputStream.writeDouble(data[i]);
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeDoubles: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeDouble2d
	 * 
	 * Write double array to the file.
	 */
	public void writeDouble2d(double[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeDoubles: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				writeDoubles(data[i]);
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeDoubles: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeFloats
	 * 
	 * Write float data to the file.
	 */
	public void writeFloats(float[] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeFloats: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				dataOutputStream.writeFloat(data[i]);
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeFloats: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeFloat2d
	 * 
	 * Write float data (a 2-D array) to the file.
	 */
	public void writeFloat2d(float[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeFloat2d: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					dataOutputStream.writeFloat(data[i][j]);
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeFloat2d: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeBytes
	 * 
	 * Write byte data to the file.
	 */
	public void writeBytes(Byte[] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeBytes: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				dataOutputStream.writeByte(data[i]);
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeBytes: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeByte2d
	 * 
	 * Write byte data (a 2-D array) to the file.
	 */
	public void writeByte2d(byte[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeByte2d: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					dataOutputStream.writeByte(data[i][j]);
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeByte2d: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeInt2d
	 * 
	 * Write integer data (a 2-D array) to the file.
	 */
	public void writeInt2d(int[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeInt2d: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					dataOutputStream.writeInt(data[i][j]);
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeInt2d: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * readInt2D
	 * 
	 * Read integer data (a 2-D array) from the file.
	 */
	public int[][] readInt2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readInt2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (length / 4 > rows * cols) {
			Tools.warningMessage(" DataFile::readInt2D: Supplied rows and cols is greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols);
			throw (new Error(
					"Error in FataFile::readInt2D: rows and cols mismatch with file"));
		}

		if (length / 4 != rows * cols) {
			Tools.warningMessage("DataFile::readInt2D: Warning! Supplied rows and cols mismatch with file size.");
			Tools.warningMessage("DataFile::readInt2D: file length = " + length
					+ " bytes, Integers = " + length / 4);
			Tools.warningMessage("Rows * Cols = " + rows * cols);
		}

		// Create the byte array to hold the data. Integer = 4 bytes.
		int[][] ints = new int[rows][cols];
		int r = 0;
		int c = 0;

		try {
			// Read in the data
			for (int i = 0; i < length / 4; i++) {
				ints[r][c] = dataInputStream.readInt();
				c++;
				if (c == cols) {
					r++;
					c = 0;
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readInt2D: error on read - " + up);
			throw (up);
		}

		return ints;
	}

	/*
	 * readShort2D
	 * 
	 * Read short integer data (a 2-D array) from the file.
	 */
	public short[][] readShort2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readShort2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (length / 2 > rows * cols) {
			Tools.warningMessage(" DataFile::readShort2D: Supplied rows and cols is greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols);
			throw (new Error(
					"Error in FataFile::readShort2D: rows and cols mismatch with file"));
		}

		if (length / 2 != rows * cols) {
			Tools.warningMessage("DataFile::readShort2D: Warning! Supplied rows and cols mismatch with file size.");
			Tools.warningMessage("DataFile::readShort2D: file length = "
					+ length + " bytes, Integers = " + length / 2);
			Tools.warningMessage("Rows * Cols = " + rows * cols);
		}

		// Create the byte array to hold the data. Short integer = 2 bytes.
		short[][] shorts = new short[rows][cols];
		int r = 0;
		int c = 0;

		try {
			// Read in the data
			for (int i = 0; i < length / 2; i++) {
				shorts[r][c] = dataInputStream.readShort();
				c++;
				if (c == cols) {
					r++;
					c = 0;
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readShort2D: error on read - " + up);
			throw (up);
		}

		return shorts;
	}

	/*
	 * readBytes2D
	 * 
	 * Read byte data (a 2-D array) from the file.
	 */
	public byte[][] readBytes2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readByte2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();
		Tools.debugMessage("    DataFile::readByte2D: file length = " + length
				+ " bytes");

		if (rows * cols > length) {
			Tools.warningMessage(" DataFile::readByte2D: Supplied rows and cols greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols);
			Tools.warningMessage(" File length = " + length + " bytes");
			throw (new Error(
					"Error in FataFile::readByte2d: rows and cols mismatch with file"));
		}

		// Create the byte array to hold the data. Integer = 4 bytes.
		byte[][] bytes = new byte[rows][cols];
		int r = 0;
		int c = 0;

		try {
			// Slightly faster to read as a 1-D array and then convert it.
			byte[] bytes1D = new byte[rows * cols];
			int num = dataInputStream.read(bytes1D);

			Tools.debugMessage("Datafile::readBytes2D: bytes read = " + num);

			for (int i = 0; i < length; i++) {
				bytes[r][c] = bytes1D[r * cols + c];
				c++;
				if (c == cols) {
					r++;
					c = 0;
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readByte2D: error on read - " + up);
			throw (up);
		}

		return bytes;
	}

	/*
	 * read2ByteInts2D
	 * 
	 * Read integer data (a 2-D array) from the file, where the integers are
	 * 2-byte little-Endian format.
	 */
	public int[][] read2ByteInts2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::read2ByteInts2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in 2-byte integers
		long length = file.length() / 2;

		if (rows * cols > length) {
			Tools.warningMessage(" DataFile::read2ByteInts2D: Supplied rows and cols greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols);
			Tools.warningMessage(" File length = " + length + " bytes");
			throw (new Error(
					"Error in FataFile::read2ByteInts2D: rows and cols mismatch with file"));
		}

		// Create the byte array to hold the data. Integer = 4 bytes.
		byte[][][] bytes = new byte[rows][cols][2];
		int r = 0;
		int c = 0;

		// And where the final integers will go...
		int[][] ints = new int[rows][cols];

		try {
			// Read in the data
			for (int i = 0; i < length; i++) {
				// Little-endian byte order
				bytes[r][c][0] = dataInputStream.readByte();
				bytes[r][c][1] = dataInputStream.readByte();
				ints[r][c] = Tools.byteArrayToInt(bytes[r][c]);
				c++;
				if (c == cols) {
					r++;
					c = 0;
				}
			}
		} catch (Exception e) {
			Tools.warningMessage(" DataFile::read2ByteInts2D: error on read - "
					+ e);
			throw (e);
		}

		return ints;
	}

	/*
	 * readFloat2d
	 * 
	 * Read float data (a 2-D array) from the file.
	 */
	public float[][] readFloat2d(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readFloat2d: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (length / 4 > rows * cols) {
			Tools.warningMessage("DataFile::readFloat2d: Supplied rows and cols is greater than file size.");
			Tools.warningMessage("DataFile::readFloat2d: file length = "
					+ length + " bytes, Floats = " + length / 4);
			Tools.warningMessage("Rows * Cols = " + rows * cols);
			throw (new Error(
					"Error in FataFile::readFloat2d: rows and cols mismatch with file"));
		}

		if (length / 4 != rows * cols) {
			Tools.warningMessage("DataFile::readFloat2d: Warning! Supplied rows and cols mismatch with file size.");
			Tools.warningMessage("DataFile::readFloat2d: file length = "
					+ length + " bytes, floats = " + length / 4);
			Tools.warningMessage("Rows * Cols = " + rows * cols);
		}

		// Create the byte array to hold the data. Integer = 4 bytes.
		float[][] floats = new float[rows][cols];
		int r = 0;
		int c = 0;

		try {
			// Read in the data
			for (int i = 0; i < length / 4; i++) {
				floats[r][c] = dataInputStream.readFloat();
				c++;
				if (c == cols) {
					r++;
					c = 0;
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readFloat2D: error on read - "
					+ up);
			throw (up);
		}

		return floats;
	}

	/*
	 * writeString
	 * 
	 * Write a string to the file.
	 */
	public void writeString(String line, boolean newLine) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeString: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			dataOutputStream.writeBytes(line);
			if (newLine)
				dataOutputStream.writeBytes(System.lineSeparator());
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeString: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeInt
	 * 
	 * Write integer data to the file.
	 */
	public void writeInt(int data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeInt: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			dataOutputStream.writeInt(data);
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeInt: error on write - " + up);
			throw (up);
		}
	}

	/*
	 * writeFloat
	 * 
	 * Write float data to the file.
	 */
	public void writeFloat(float data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeFloat: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			dataOutputStream.writeFloat(data);
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeFloat: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeDouble
	 * 
	 * Write double data to the file.
	 */
	public void writeDouble(double data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeDouble: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			dataOutputStream.writeDouble(data);
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeDouble: error on write - "
					+ up);
			throw (up);
		}
	}
}
