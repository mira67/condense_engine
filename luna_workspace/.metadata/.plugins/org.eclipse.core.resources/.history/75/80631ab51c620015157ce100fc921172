import helper_classes.*;

public class Scratch {

	static String filename = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\SHLATLSB.bin";

	final static int ROWS = 1441;
	final static int COLS = 1441;
	
	public static void main(String[] args) {

		DataFile file;

		double array[][] = new double[ROWS][COLS];
		
		// Read the file
		try {
			file = new DataFile( filename );	
			
			for (int r = 0; r < ROWS; r++) {
				for (int c = 0; c < COLS; c++) {
					
					int i = file.readInt();
					
					// Reverse the byte order (for Windows).
					i = Tools.reverseByteOrder(i);
					
					// Decode the data
					
					array[r][c] = ((double) i) / 100000.0;
				}
			}

			file.close();			
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file: " + filename );
		}

		for (int i = 0; i < ROWS; i = i + 10) {
			Tools.message("Array["+i+"]["+i+"] = " + array[i][i]);
		}
	}

}
