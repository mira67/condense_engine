import condense.*;

/* a sandbox for code experimentation.
 * 
 */

public class Scratch2 {
	
	public static void main(String[] args) {

		String inputPath = "C:/Users/glgr9602/Desktop/condense/climatology/avhrr/";
		
		String searchString = "1400-mean-jan";
		
		String filename = Tools.findFile(inputPath, searchString);
		if (filename == null) {
			Tools.message("Could not find file with string: " + searchString);
			Tools.message("in path: " + inputPath);
			System.exit(1);
		}
		
		filename = "junk.bin";
		
		try {
			int rows = 1605;
			int cols = 1605;
			
			int r = 52;
			int c = 50;
			
			
			double[][] a = new double[rows][cols];
			a[r][c] = 2345.6789;
			
			DataFile file1 = new DataFile();
			file1.create("junk.bin");
			
			file1.writeDouble2d(a);
			file1.close();
			
			Tools.message("File written");
			
			
			DataFile file = new DataFile(filename);
			file.open();
			
			double[][] b = file.readDoubles2D(rows, cols);
			file.close();
			
			Tools.message("b[" + r + "][" + c + "] = " + b[r][c]);
			Tools.message("End");
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file");
		}
	}
}
