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
	//static int startYear = 2012;
	static int startYear = 1990;
	static int startMonth = 1;
	static int startDay = 1;

	static int finalYear = 2014;
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

	// For parallel processing
	static int procs; 
	
	/*-------------------------------------------------------------------------
	// MAIN PROGRAM
	//-----------------------------------------------------------------------*/

	public static void main(String[] args) {

		// Control what messages we see.
		Tools.setDebug(debugMessages);
		Tools.setWarnings(warningMessages);

		long firstTime = System.currentTimeMillis();
		
		Dataset.DataType dataType = Dataset.DataType.SSMI;

		// Where does the data come from? Set the path. Platform dependent.
		
		// Linux
		// String dataPath = "/home/glgr9602/DATASETS/nsidc0001_polar_stereo_tbs/south/";	// SSMI
		// String dataPath = "/home/glgr9602/DATASETS/nsidc0001_polar_stereo_tbs/south/";	// AVHRR
		
		// Windows
		String dataPath = "/Users/glgr9602/Desktop/condense/data/" +
				dataType.toString() + "/daily/";
		
		// How many processors do we have available?
	    procs = Runtime.getRuntime().availableProcessors(); 
	    System.out.println("Available processors = " + procs);
	    
	    // Max out at 8 processes. Avoid meltdowns?
	    procs = 8;

	    // Create an array to hold parallel threads.
	    ParallelTask[] threads = new ParallelTask[procs]; 
	    		
		//************************************************
		// Generic one-time processing - passed parameters
		//************************************************
		
		///increment = Timespan.Increment.getType(args[0]);
		///suffix1 = args[1];  // Frequency, for SSMI data
		///suffix2 = args[2];  // Polarization, for SSMI data

		//***************************************
		// Batch Processing
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
			String outputPath = "/home/glgr9602/condense/climatology/" +
						dataType.toString() + "/";
			// Windows
			//String outputPath = "/Users/glgr9602/Desktop/condense/climatology/" +				
			//		dataType.toString() + "/" + name + "/";

			// This keeps track of how many parallel processes we create.
			int processID = 0;
			
			//***************************************
			// SSMI
			//***************************************
			/*
			String[] frequencies = {"19", "22", "37", "85"};
			String[] polarizations = {"h", "v"};
			
			// Loop through all frequencies
			for ( String freq : frequencies) {
				
				// Loop through all polarizations
				for ( String pol : polarizations) {
					
					// Create a new thread
				    threads[processID] = new ParallelTask(
				    		processID, dataType,
							startYear, startMonth, startDay,
							finalYear, finalMonth, finalDay,
							increment,
							dataPath, outputPath,
							freq, pol );
				    
				    // Start it.
				    threads[processID].start();
				    
				    // Increment the process ID.
				    processID++;
				}
			}

			*/

			//***************************************
			// AVHRR
			//***************************************

			String[] wavelengths = {"ch1", "ch2", "ch3", "ch4", "ch5"};

			// Loop through all wavelengths
			for ( String wavelength : wavelengths) {
					
				// Create a new thread
			    threads[processID] = new ParallelTask(
				    		processID, dataType,
							startYear, startMonth, startDay,
							finalYear, finalMonth, finalDay,
							increment,
							dataPath, outputPath,
							wavelength, "" );
					    
			   // Start it.
			   threads[processID].start();
					    
			   // Increment the process ID.
			   processID++;
			}

			//*****************************************
			// FINISHED WITH SENSOR-SPECIFIC PROCESSING
			//*****************************************
			
			// Combine the output
		    for (int i = 0; i < procs; i++) { 
		    	try { 
		    		// Re-unites all threads; waits until each thread is done.
		    		threads[i].join(); 
		    	}
		    	catch (InterruptedException e) {
		    	} 
		    } 
		}
		
		
		firstTime = (System.currentTimeMillis() - firstTime) / 1000;
		Tools.statusMessage("Total time to process = " + firstTime + " seconds");
	}


	/* ParallelTask
	 * 
	 * Class for a parallel thread.
	 */
	static class ParallelTask extends Thread { 

		// Variables to be retrieved by the master thread must be static 
		int threadNumber; 
		
		Dataset.DataType dataT;
		
		int startY;
		int startM;
		int startD;
		
		int finalY;
		int finalM;
		int finalD;
		
		Timespan.Increment inc;
		
		String dataP;
		String outputP;
		String suff1;
		String suff2;
		
		// constructor
		public ParallelTask(int id, Dataset.DataType datatype,
				int startYear, int startMonth, int startDay,
				int finalYear, int finalMonth, int finalDay,
				Timespan.Increment increment,
				String dataPath, String outputPath,
				String suffix1, String suffix2) {
			
			this.threadNumber = id;
			this.dataT = datatype;
			
			this.startY = startYear;
			this.startM = startMonth;
			this.startD = startDay;
			
			this.finalY = finalYear;
			this.finalM = finalMonth;
			this.finalD = finalDay;
			
			this.inc = increment;
			this.dataP = dataPath;
			this.outputP = outputPath;
			this.suff1 = suffix1;
			this.suff2 = suffix2;
		}

		/* run
		 * 
		 * This is where the work gets done.
		 */
		public void run() {

			Tools.statusMessage("Thread: " + threadNumber +
					"  Process " + dataT.toString() + " data, " +
					"suffix1 = " + suff1 + "  suffix2 = " + suff2 +
					"  increment = " + inc.toString());
			
			Tools.statusMessage(
					+ startY + "." + startM + "." + startD + " to "
					+ finalY + "." + finalM + "." + finalD);

			long startTime = System.currentTimeMillis();

			Climatology climate = new Climatology( dataT,
					startY, startM, startD,
					finalY, finalM, finalD, inc,
					dataP, outputP,
					suff1, suff2);

			climate.run();
			
			long endTime = System.currentTimeMillis();
			endTime = (endTime - startTime) / 1000;
			Tools.statusMessage("Time to process = " + endTime + " seconds");
			Tools.statusMessage("");
		}
	} 
}
