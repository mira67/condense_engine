import java.io.File;
import java.io.FilenameFilter;

import condense.*;

/* a sandbox for code experimentation.
 * 
 */

public class Scratch2 {
	
	public static void main(String[] args) {

		String inputPath = "C:/Users/glgr9602/Desktop/condense/climatology/ssmi/doubles/";
		String outputPath = "C:/Users/glgr9602/Desktop/condense/climatology/ssmi/shorts/";
		
		String searchString = "bin";
		
		File[] files = findFiles(inputPath, searchString);
		
		for (int i = 0; i < files.length; i++) {
			try {
				String name = files[i].getName();

				DataFile inputFile = new DataFile(inputPath + name);

				Tools.message("input file = " + name);
				
				// Southern hemisphere,	everything except 85.5 and 91.7 GHz,
				// file size = 209824 Bytes
				int rows = 332;
				int cols = 316;
				
				// Southern hemisphere,	85.5 and 91.7 GHz, 839296 Bytes
				if (inputFile.length() > 900000) {
					rows = 664;
					cols = 632;
				}
				Tools.message("    rows, cols = " + rows + "," + cols);
				
				double[][] data = new double[rows][cols];
				data = inputFile.readDoubles2D(rows, cols);
				short[][] shortData = Tools.doubleArrayToShort(data);
				inputFile.close();
				
				String outputName = outputPath + name;
				DataFile outputFile = new DataFile();
				outputFile.create(outputName);
				outputFile.writeShorts2d(shortData);
				outputFile.close();
				
			}
			catch(Exception e) {
				Tools.errorMessage("", "", "Caught execption", e);
			}
		}
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
	
}
