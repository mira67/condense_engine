package climatology.nsidc.org;

import java.util.ArrayList;
import java.util.Iterator;

import condense.*;
import condense.Dataset.DataType;


/* Climate
 * 
 * Generate baseline climatology data files.
 * 
 * The user selects a time span and increment (monthly, seasonal, same month over multiple
 * years, etc), and the program reads the data, generating statistical files based on the
 * data.
 */

public class Climate extends GeoObject {

	// The type of data we're processing
	static Dataset.DataType dataType;
	
    // Parallel processes.
    static int procs;
    
    // When reading the source data, do we have to modify the path?
    static boolean addYearToInputDirectory = true;
    static boolean addDayToInputDirectory = true;   // Day-of-year, not day of month

	// Where to get the source data, and store the output
    static String dataPath = "";
    static String outputPath = "";
    static String hemisphere;
    
    // What to do about bad data in the files?
    static boolean filterBadData = true;
    
    // Start and end dates for the processing.
	// A data file for the start date *must* exist because that will be used
	// to generate the metadata.
	static int startYear = 0;
	static int startMonth = 0;
	static int startDay = 0;

	static int finalYear = 0;
	static int finalMonth = 0;
	static int finalDay = 0;
	
	// If we're testing, not doing a full run, set this to true --
	// eliminates some debugging junk.
	static boolean testing = false;
    
	static boolean warningMessages = true; // Receive warning messages?
	static boolean debugMessages = false; // Receive debug messages?

	// File suffixes, for data selection
	static String suffix2 = ""; // SSMI polarization, h or v, or AVHRR channel

	/*-------------------------------------------------------------------------
	// MAIN PROGRAM
	//-----------------------------------------------------------------------*/

	public static void main(String[] args) {

		// Control what messages we see.
		Tools.setDebug(debugMessages);
		Tools.setWarnings(warningMessages);

		long firstTime = System.currentTimeMillis();
		

		// Read the configuration file.
		if (args.length < 1) {
			Tools.message("No configuration file specifiec.");
			System.exit(1);
		}
		String configFilename = args[0];
		
		try {
			if (!readConfigFile(configFilename)) {
				Tools.errorMessage("Condense", "main",
						"Could not read the cofiguration file: "
								+ configFilename, new Exception());
			}
		} catch (Exception e) {
			Tools.message("Error when reading configuration file: " + configFilename);
			System.out.println(e);
			Tools.errorMessage("Climate", "main", "", new Exception());
		}
		
		// How many processors do we have available?
	    procs = Runtime.getRuntime().availableProcessors(); 
	    Tools.message("Available processors = " + procs);
	    
	    // Max out at 10 to avoid melt-downs?
	    if (procs > 10) procs = 10; 
	    
	    // Limit processors for testing
	    if (testing) procs = 2;

	    Tools.message("Using: " + procs + " processors.");
	    
	    // Create an array to hold parallel threads.
	    ParallelTask[] threads = new ParallelTask[procs]; 

		// Loop through all possible increment types. Create a new process
	    // for each increment.
		for (Timespan.Increment increment : Timespan.Increment.values()) {

			// Skip over unwanted increments, i.e., only process in the
			// desired increments.
			String name = increment.name().toLowerCase();
			
			if (name.compareTo("none") == 0  ||
				name.compareTo("day") == 0  ||
				name.compareTo("year") == 0  ||
				name.compareTo("week") == 0
			) continue;

			// For testing, only do January
			if (testing) {
				if (name.compareTo("jan") != 0) continue;
			}
			
			// This keeps track of how many parallel processes we create.
			int processID = 0;
			
		    switch(dataType) {
		    	case SEA_ICE:
		    		Tools.errorMessage("Climate", "Climate", "SEA_ICE type not enabled", 
		    				new Exception());
		    		break;
		    		
		    	case SSMI:
					String[] frequencies = {"19", "22", "37", "85"};
					String[] frequencies_short = {"19"};
					String[] polarizations = {"h", "v"};
					
					if (testing) {
						frequencies = frequencies_short;
					}
					
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
									freq, pol, hemisphere, testing );
						    
						    // Start it.
						    threads[processID].start();
						    
						    // Increment the process ID.
						    processID++;
						}
					}
					
					break;
					
		    	case AVHRR:
		    		
		    		String[] channels = {"albd", "chn1", "chn2", "chn3", "chn4", "chn5", "temp"};
		    		//String[] channels = {"temp", "albd", "chn1", "chn2"};
		    		String[] channels_short = {"chn1"};
		    		
		    		// If testing, only do one channel.
		    		if (testing) {
		    			channels = channels_short;
		    		}

					// Loop through all wavelengths
					for ( String channel : channels) {
						
						// Create a new thread
					    threads[processID] = new ParallelTask(
						    		processID, dataType,
									startYear, startMonth, startDay,
									finalYear, finalMonth, finalDay,
									increment,
									dataPath, outputPath,
									channel, suffix2, hemisphere,
									testing );
							    
					   // Start it.
					   threads[processID].start();
							    
					   // Increment the process ID.
					   processID++;
					}
					break;
		    	case EASE_GRID_SURFACE:
		    	default:
		    		Tools.errorMessage("Climate", "Climate", "Datatype not implemented: " + dataType,
		    				new Exception("exiting"));
		    }
		    
			// FINISHED WITH SENSOR-SPECIFIC PROCESSING
			
			// Combine the output
		    for (int i = 0; i < processID; i++) { 
		    	try { 
		    		// Re-unites all threads; waits until each thread is done.
		    		threads[i].join(); 
		    	}
		    	catch (InterruptedException e) {
		    		// Shouldn't happen under normal operation.
		    		Tools.message("Climate::Climate: Interrupted exception, id = " + i);
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
		String hemisphere;
		
		double min;
		double max;
		
		boolean testing;
		
		// constructor
		public ParallelTask(int id, Dataset.DataType datatype,
				int startYear, int startMonth, int startDay,
				int finalYear, int finalMonth, int finalDay,
				Timespan.Increment increment,
				String dataPath, String outputPath,
				String suffix1, String suffix2,
				String hemi, boolean test) {
			
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
			
			this.hemisphere = hemi;
			
			this.testing = test;
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

			Climatology climate;
			
			if (dataT == Dataset.DataType.AVHRR) {
				climate = new ClimatologyAVHRR( dataT,
						startY, startM, startD,
						finalY, finalM, finalD, inc,
						dataP, outputP,
						suff1, suff2, filterBadData,
						addYearToInputDirectory, addDayToInputDirectory,
						testing);			
			}
			else {
				climate = new ClimatologySSMI( dataT,
						startY, startM, startD,
						finalY, finalM, finalD, inc,
						dataP, outputP,
						suff1, suff2, filterBadData, hemisphere,
						addYearToInputDirectory, addDayToInputDirectory,
						testing);				
			}

			climate.run();
			
			long endTime = System.currentTimeMillis();
			endTime = (endTime - startTime) / 1000;
			Tools.statusMessage("Time to process = " + endTime + " seconds");
			Tools.statusMessage("");
		}
	} 
	
	/*
	 * readConfigFile
	 * 
	 * Read parameters from a configuration file.
	 */
	public static boolean readConfigFile(String filename) throws Exception {

		Tools.statusMessage("--------------------------------------------------------------");

		try {
			DataFile file = new DataFile(filename);

			Tools.statusMessage("Configuration file: " + filename);

			// All the lines in the file.
			ArrayList<String> lines = file.readStrings();

			Iterator<String> lineIter = lines.iterator();

			// Process the lines in the configuration file.
			while (lineIter.hasNext()) {

				// Read the line
				String input = lineIter.next();

				// Before cleaning up the string, save any literal text values.
				String textValue = Tools.parseString(input, 1, "=");
				textValue = textValue.trim(); // Remove any white space at the
												// beginning and end

				// Clean up the string: remove white spaces, and make all lower
				// case
				String line = Tools.removeCharacters(input, ' ');
				line = line.toLowerCase();

				Tools.debugMessage("  line: " + line);

				// Parse out the variable and parameter value
				String variable = Tools.parseString(line, 0, "=");
				String value = Tools.parseString(line, 1, "=");

				// Handle blank lines.
				if (variable.length() == 0)
					continue;

				// Comment lines
				if (variable.indexOf("*") == 0 ||
						variable.indexOf("#") == 0 ||
						variable.indexOf("!") == 0 ||
						variable.indexOf(";") == 0) continue;
				
				// Process the variable
				switch (variable) {
				case "startyear":
					startYear = Integer.valueOf(value);
					Tools.statusMessage("Start Year = " + startYear);
					break;
				case "startmonth":
					startMonth = Integer.valueOf(value);
					Tools.statusMessage("Start Month = " + startMonth);
					break;
				case "startday":
					startDay = Integer.valueOf(value);
					Tools.statusMessage("Start Day = " + startDay);
					break;
				case "finalyear":
					finalYear = Integer.valueOf(value);
					Tools.statusMessage("Final Year = " + finalYear);
					break;
				case "finalmonth":
					finalMonth = Integer.valueOf(value);
					Tools.statusMessage("Final Month = " + finalMonth);
					break;
				case "finalday":
					finalDay = Integer.valueOf(value);
					Tools.statusMessage("Final Day = " + finalDay);
					break;
				case "datatype":
					if (value.equals("sea_ice"))
						dataType = DataType.SEA_ICE;
					if (value.equals("ssmi"))
						dataType = DataType.SSMI;
					if (value.equals("avhrr"))
						dataType = DataType.AVHRR;
					Tools.statusMessage("Data Type = " + dataType);
					break;
				case "testing":
					testing = Boolean.valueOf(value);
					Tools.statusMessage("Testing = " + testing);
					break;
				case "debug":
					debugMessages = Boolean.valueOf(value);
					Tools.statusMessage("Debug = " + debugMessages);
					Tools.setDebug(debugMessages);
					break;
				case "warnings":
					warningMessages = Boolean.valueOf(value);
					Tools.statusMessage("Warnings = " + warningMessages);
					Tools.setWarnings(warningMessages);
					break;
				case "addyear":
					addYearToInputDirectory = Boolean.valueOf(value);
					Tools.statusMessage("Add the year to the input directory = "
							+ addYearToInputDirectory);
					break;
				case "adddoy":   // Add the day-of-year to the data path
					addDayToInputDirectory = Boolean.valueOf(value);
					Tools.statusMessage("Add the day-of-year to the input directory = "
							+ addDayToInputDirectory);
					break;
				case "datapath":
					dataPath = textValue;
					Tools.statusMessage("Data Path = " + dataPath);
					break;
				case "outputpath":
					outputPath = textValue;
					Tools.statusMessage("Output Path = " + outputPath);
					break;
				case "hemisphere":
					hemisphere = textValue;
					Tools.statusMessage("Hemisphere = " + hemisphere);
					break;
				case "time":			// AVHRR time: 0200 or 1400, also a file name suffix.
					suffix2 = value;
					Tools.statusMessage("Time = " + suffix2);
					break;
				case "filterbaddata":
					filterBadData = Boolean.valueOf(value);
					Tools.statusMessage("Filter bad data = " + filterBadData);
					break;
				default:
					Tools.warningMessage("Configuration file line not understood: "
							+ input);
					break;

				}

				file.close();
			}
		} catch (Exception e) {
			throw e;
		}

		Tools.statusMessage("--------------------------------------------------------------");

		return true;
	}
}
