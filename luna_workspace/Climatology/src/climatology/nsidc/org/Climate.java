package climatology.nsidc.org;

import helper_classes.*;

/* Climate
 * 
 * Generate baseline climatology data files.
 * 
 * The user selects a time span and increment (monthly, seasonal, same month over multiple
 * years, etc), and the program reads the data, generating statistical files based on the
 * data.
 */

public class Climate extends GeoObject {

	// Start and end dates for the processing.
	// A data file for the start date *must* exist because that will be used
	// to generate the metadata.
	static int startYear = 2012;
	//static int startYear = 2010;
	static int startMonth = 1;
	static int startDay = 1;

	static int finalYear = 2012;
	//static int finalYear = 1990;
	static int finalMonth = 12;
	static int finalDay = 31;

	static int lastStartYear = 0;
	static int lastStartMonth = 0;
	static int lastStartDay = 0;
	
	static boolean warningMessages = true; // Receive warning messages?
	static boolean debugMessages = false; // Receive debug messages?

	// SSMI data selection
	static String suffix1 = ""; // Frequency of SSMI data
	static String suffix2 = ""; // SSMI polarization, h or v

	// Files and paths for i/o
	
	//...WINDOWS
	/*
	static final 
	static final String dataPath = "/Users/glgr9602/Desktop/condense/data/" +
			dataType.toString() + "/daily/";
	*/
	
	// ...LINUX
	
	
	/*-------------------------------------------------------------------------
	// MAIN PROGRAM
	//-----------------------------------------------------------------------*/

	public static void main(String[] args) {

		// Control what messages we see.
		Tools.setDebug(debugMessages);
		Tools.setWarnings(warningMessages);

		long firstTime = System.currentTimeMillis();
		long endTime = 0;
		
		Dataset.DataType dataType = Dataset.DataType.SSMI;

		// Linux
		// String dataPath = "/home/glgr9602/DATASETS/nsidc0001_polar_stereo_tbs/south/";
		// Windows
		String dataPath = "/Users/glgr9602/Desktop/condense/data/" +
				dataType.toString() + "/daily/";
		
		//***************************************
		// Generic Processing
		//***************************************
		
		///increment = Timespan.Increment.getType(args[0]);
		///suffix1 = args[1];  // Frequency, for SSMI data
		///suffix2 = args[2];  // Polarization, for SSMI data

		//***************************************
		// SSMI Processing
		//***************************************
		
		// Loop through all possible increment types.
		for (Timespan.Increment increment : Timespan.Increment.values()) {
			
			// Skip over unwanted increments
			String name = increment.name().toLowerCase();
			
			if (name.compareTo("none") == 0  ||
				name.compareTo("day") == 0  ||
				name.compareTo("year") == 0  ||
				name.compareTo("week") == 0
			) continue;

			// Linux
			// String outputPath = "/home/glgr9602/condense/climatology/" +
			//			dataType.toString() + "/" + name + "/";
			// Windows
			String outputPath = "/Users/glgr9602/Desktop/condense/climatology/" +				
					dataType.toString() + "/" + name + "/";
			
			String[] frequencies = {"19", "22", "37", "85"};
			String[] polarizations = {"h", "v"};

			// Loop through all frequencies
			for ( String freq : frequencies) {
				
				suffix1 = freq;
				
				// Loop through all polarizations
				for ( String pol : polarizations) {
				
					suffix2 = pol;
					
					Tools.statusMessage("Process " + dataType.toString() + " data, " +
							"frequency = " + freq + "  polarization = " + pol +
							"  increment = " + increment.toString());
					Tools.statusMessage(
							+ startYear + "." + startMonth + "." + startDay + " to "
							+ finalYear + "." + finalMonth + "." + finalDay);

					long startTime = System.currentTimeMillis();

					Climatology climate = new Climatology( dataType,
							startYear, startMonth, startDay,
							finalYear, finalMonth, finalDay, increment,
							dataPath, outputPath,
							freq, pol);

					climate.run();
					
					endTime = System.currentTimeMillis();
					endTime = (endTime - startTime) / 1000;
					Tools.statusMessage("Time to process = " + endTime + " seconds");
					Tools.statusMessage("");
				}
			}
		}
		endTime = (endTime - firstTime) / 1000;
		Tools.statusMessage("Total time to process = " + endTime + " seconds");
	}
}
