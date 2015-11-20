package condense;

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
		this.filename = filename;

		// Open the file.
		try {
			file = new File(filename);
			dataOutputStream = new DataOutputStream(new FileOutputStream(file));
		} catch (Exception e) {
			throw (e);
		}

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

		file = null;
		dataInputStream = null;
		dataOutputStream = null;
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

		BufferedReader br = new BufferedReader(new FileReader(filename));

		String line;

		while ((line = br.readLine()) != null) {
			strings.add(line);
		}
		br.close();

		open();

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

		// Get the size of the file
		long length = file.length();

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

		// Get the size of the file in bytes
		long length = file.length();

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

		// Get the size of the file in bytes
		long length = file.length();

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
	 * readLong
	 * 
	 * Read a single long integer from the file.
	 */
	public long readLong() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readLong: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		long i;

		try {
			i = dataInputStream.readLong();
		} catch (Exception e) {
			Tools.warningMessage("DataFile::readLong: could not read long integer");
			throw (e);
		}

		return i;
	}

	/*
	 * readFloat
	 * 
	 * Read a single float number from the file.
	 */
	public float readFloat() throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readFloat: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		float x;

		try {
			x = dataInputStream.readFloat();
		} catch (Exception e) {
			Tools.warningMessage("DataFile::readFloat: could not read long integer");
			throw (e);
		}

		return x;
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

		// Get the size of the file in bytes
		long length = file.length();

		// Create the byte array to hold the data. Integer = 4 bytes.
		int[] ints = new int[(int) length / 4];

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

		// Get the size of the file in bytes
		long length = file.length();

		// Create the array to hold the data. Float = 4 bytes.
		float[] floats = new float[(int) length / 4];

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
	 * writeDoubles2d
	 * 
	 * Write double array to the file.
	 */
	public void writeDoubles2d(double[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeDoubles2d: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				writeDoubles(data[i]);
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::writeDoubles2d: error on write - "
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
	 * writeFloats2d
	 * 
	 * Write float data (a 2-D array) to the file.
	 */
	public void writeFloats2d(float[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeFloats2d: " + filename
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
			Tools.warningMessage(" DataFile::writeFloats2d: error on write - "
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
	 * writeBytes2d
	 * 
	 * Write byte data (a 2-D array) to the file.
	 */
	public void writeBytes2d(byte[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeBytes2d: " + filename
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
			Tools.warningMessage(" DataFile::writeBytes2d: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * writeShorts2d
	 * 
	 * Write short integer data (a 2-D array) to the file.
	 */
	public void writeShorts2d(short[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeShorts2d: " + filename
					+ " is not open for writing.");
			throw (new Exception("Cannot write to file, not open for writing"));
		}

		try {
			// Write the data
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					dataOutputStream.writeShort(data[i][j]);
				}
			}
		} catch (Exception e) {
			Tools.warningMessage(" DataFile::writeShorts2d: error on write - "
					+ e);
			throw (e);
		}
	}

	/*
	 * writeInts2d
	 * 
	 * Write integer data (a 2-D array) to the file.
	 */
	public void writeInts2d(int[][] data) throws Exception {

		if (!fileIsWritable) {
			Tools.warningMessage("DataFile::writeInts2d: " + filename
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
			Tools.warningMessage(" DataFile::writeInts2d: error on write - "
					+ up);
			throw (up);
		}
	}

	/*
	 * readInts2D
	 * 
	 * Read integer data (a 2-D array) from the file.
	 */
	public int[][] readInts2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readInts2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (length / 4 > rows * cols) {
			Tools.warningMessage(" DataFile::readInts2D: Supplied rows and cols is greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols + "   Rows = " + rows + " Cols = " + cols);
			throw (new Error("DataFile::readInts2D: rows and cols mismatch with file"));
		}

		if (length / 4 != rows * cols) {
			Tools.warningMessage("DataFile::readInts2D:  Supplied rows and cols mismatch with file size.");
			Tools.warningMessage("DataFile::readInts2D: file length = " + length
					+ " bytes, Integers = " + length / 4);
			Tools.warningMessage("Rows * Cols = " + rows * cols + "  Filename = " + filename);
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
			Tools.warningMessage(" DataFile::readInts2D: error on read - " + up);
			throw (up);
		}

		return ints;
	}

	/*
	 * readShorts2D
	 * 
	 * Read short integer data (a 2-D array) from the file.
	 */
	public short[][] readShorts2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readShorts2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (length / 2 > rows * cols) {
			Tools.warningMessage(" DataFile::readShorts2D: Supplied rows and cols is greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols + "   Rows = " + rows + " Cols = " + cols);
			throw (new Error("DataFile::readShorts2D: rows and cols mismatch with file"));
		}

		if (length / 2 != rows * cols) {
			Tools.warningMessage("DataFile::readShorts2D: Supplied rows and cols mismatch with file size.");
			Tools.warningMessage("DataFile::readShorts2D: file length = "
					+ length + " bytes, Shorts = " + length / 2);
			Tools.warningMessage("Rows * Cols = " + rows * cols + "  Filename = " + filename);
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
			Tools.warningMessage(" DataFile::readShorts2D: error on read - " + up);
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
			Tools.warningMessage("DataFile::readBytes2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (rows * cols > length) {
			Tools.warningMessage(" DataFile::readBytes2D: Supplied rows and cols greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols + "   Rows = " + rows + " Cols = " + cols);
			Tools.warningMessage(" File length = " + length + " bytes");
			throw (new Error("DataFile::readBytes2d: rows and cols mismatch with file"));
		}

		// Create the byte array to hold the data. Integer = 4 bytes.
		byte[][] bytes = new byte[rows][cols];
		int r = 0;
		int c = 0;

		try {
			// Slightly faster to read as a 1-D array and then convert it.
			byte[] bytes1D = new byte[rows * cols];
			dataInputStream.read(bytes1D);

			for (int i = 0; i < length; i++) {
				bytes[r][c] = bytes1D[r * cols + c];
				c++;
				if (c == cols) {
					r++;
					c = 0;
				}
			}
		} catch (Exception up) {
			Tools.warningMessage(" DataFile::readBytes2D: error on read - " + up);
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
	public short[][] read2ByteInts2D(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::read2ByteInts2D: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in 2-byte integers
		long length = file.length() / 2;

		if (rows * cols > length) {
			Tools.warningMessage(" DataFile::read2ByteInts2D: Supplied rows and cols greater than file size.");
			Tools.warningMessage(" Rows * Cols = " + rows * cols + "   Rows = " + rows + " Cols = " + cols);
			Tools.warningMessage(" File length = " + length + " bytes");
			throw (new Exception("DataFile::read2ByteInts2D: rows and cols mismatch with file"));
		}

		// Create the byte array to hold the data. Integer = 4 bytes.
		byte[][][] bytes = new byte[rows][cols][2];
		int r = 0;
		int c = 0;

		// And where the final integers will go...
		short[][] shorts = new short[rows][cols];

		try {
			// Read in the data
			for (int i = 0; i < length; i++) {
				// Little-endian byte order
				bytes[r][c][0] = dataInputStream.readByte();
				bytes[r][c][1] = dataInputStream.readByte();
				shorts[r][c] = Tools.byteArrayToShort(bytes[r][c]);
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

		return shorts;
	}

	/*
	 * readFloats2d
	 * 
	 * Read float data (a 2-D array) from the file.
	 */
	public float[][] readFloats2d(int rows, int cols) throws Exception {

		if (!fileIsReadable) {
			Tools.warningMessage("DataFile::readFloats2d: " + filename
					+ " is not open for reading.");
			throw (new Exception("Cannot read file, not open for reading"));
		}

		// Get the size of the file in bytes
		long length = file.length();

		if (length / 4 > rows * cols) {
			Tools.warningMessage("DataFile::readFloats2d: Supplied rows and cols is greater than file size.");
			Tools.warningMessage("DataFile::readFloats2d: file length = "
					+ length + " bytes, Floats = " + length / 4);
			Tools.warningMessage(" Rows * Cols = " + rows * cols + "   Rows = " + rows + " Cols = " + cols);
			throw (new Error("DataFile::readFloats2d: rows and cols mismatch with file"));
		}

		if (length / 4 != rows * cols) {
			Tools.warningMessage("DataFile::readFloats2d: Supplied rows and cols mismatch with file size.");
			Tools.warningMessage("DataFile::readFloats2d: file length = "
					+ length + " bytes, floats = " + length / 4);
			Tools.warningMessage("Rows * Cols = " + rows * cols + "  Filename = " + filename);
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
			Tools.warningMessage(" DataFile::readFloats2D: error on read - "
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
