package helper_classes;

/* Timespan
 * 
 * A span of time, from one timestamp to another. 
 */

public class Timespan extends GeoObject {
	
	public static enum Increment {
		NONE ("day", 0, 0),
		DAY ("day", 0, 1),
		WEEK ("week", 0, 7),
		JAN ("jan", 1, 31),
		FEB ("feb", 2, 29),
		MAR ("mar", 3, 31),
		APR ("apr", 4, 30),
		MAY ("may", 5, 31),
		JUN ("jun", 6, 30),
		JUL ("jul", 7, 31),
		AUG ("aug", 8, 31),
		SEP ("sep", 9, 30),
		OCT ("oct", 10, 31),
		NOV ("nov", 11, 30),
		DEC ("dec", 12, 31),
		YEAR ("year", 1, 366),
		DJF ("djf", 12, 91),// Dec-Jan-Feb
		MAM ("mam", 3, 92),	// Mar-Apr-May
		JJA ("jja", 6, 92),	// Jun-Jul-Aug
		SON ("son", 9, 92);	// Sep-Oct-Nov
    	
		private final String name;
    	private final int startMonth;
    	private final int days;
    	private Increment(String s, int month, int maxDays) {
    		name = s;
    		startMonth = month;
    		days = maxDays;}
    	public int startMonth() {return startMonth;}
    	public int maxDays() {return days;}
    	public String getName() {return name;}

    	/* getType
    	 * 
    	 * Given the name of one of the increments, return an increment
    	 * of that type.
    	 */
    	public static Increment getType( String name ) {
    		
    		name = name.toUpperCase();
    		name = name.substring(0,3);
    	
    		if (name.compareTo(Increment.JAN.name()) == 0) return Increment.JAN;
    		if (name.compareTo(Increment.FEB.name()) == 0) return Increment.FEB;
    		if (name.compareTo(Increment.MAR.name()) == 0) return Increment.MAR;
    		if (name.compareTo(Increment.APR.name()) == 0) return Increment.APR;
    		if (name.compareTo(Increment.MAY.name()) == 0) return Increment.MAY;
    		if (name.compareTo(Increment.JUN.name()) == 0) return Increment.JUN;
    		if (name.compareTo(Increment.JUL.name()) == 0) return Increment.JUL;
    		if (name.compareTo(Increment.AUG.name()) == 0) return Increment.AUG;
    		if (name.compareTo(Increment.SEP.name()) == 0) return Increment.SEP;
    		if (name.compareTo(Increment.OCT.name()) == 0) return Increment.OCT;
    		if (name.compareTo(Increment.NOV.name()) == 0) return Increment.NOV;
    		if (name.compareTo(Increment.DEC.name()) == 0) return Increment.DEC;
    		if (name.compareTo(Increment.DJF.name()) == 0) return Increment.DJF;
    		if (name.compareTo(Increment.MAM.name()) == 0) return Increment.MAM;
    		if (name.compareTo(Increment.JJA.name()) == 0) return Increment.JJA;
    		if (name.compareTo(Increment.SON.name()) == 0) return Increment.SON;
    		if (name.compareTo(Increment.DAY.name()) == 0) return Increment.DAY;
    		if (name.compareTo(Increment.WEEK.name()) == 0) return Increment.WEEK;
    		if (name.compareTo(Increment.YEAR.name()) == 0) return Increment.YEAR;

    		return Increment.NONE;
    	}
	}

	protected Timestamp startDate;	// Beginning of the timespan
	protected Timestamp endDate;	// End of the timespan.
	
	// Default time increment
    protected Increment increment = Increment.NONE;
    
	/*---------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------*/

	public Timespan() {
		startDate = new Timestamp( 0.0 );
		endDate = new Timestamp( 0.0 );
	}


	/* Timespan
	 * 
	 * Constructor using two timestamps. Defaults to no increment.
	 */
	public Timespan(Timestamp begin, Timestamp end) {
		startDate = begin;
		endDate = end;
	}


	/* Timespan
	 * 
	 * Constructor using a starting timestamp and number of days. 
	 */
	public Timespan(Timestamp begin, int days) {
		startDate = begin;
		endDate = begin;
		endDate.incrementDays( days );
	}

	
	/* Timespan
	 * 
	 * Constructor using a starting and ending timestamps, and an increment.
	 */
	public Timespan(Timestamp start, Timestamp end, Increment i) {
		
		startDate = start;
		endDate = end;
		
		setSpan(i);
	}
	
	/* setSpan
	 * 
	 * Resets the start and end dates to the selected increment.
	 * Requires that the start and end timestamps be set already.
	 * 
	 * The new span is based on the start date. Ending dates that
	 * span multiple years will be truncated unless the increment
	 * is "NONE".
	 * 
	 * If a year increment is selected, the start and end dates will encompass
	 * the entire year of the start date.
	 * 
	 * If a month increment is selected, the start and end dates will encompass
	 * the entire start month, no fractional months.
	 */
	public void setSpan(Increment i) {
	
		increment = i;
		
		int startYear = startDate.year();
		int startMonth = startDate.month();
		int startDay = startDate.dayOfMonth();

		// Defaults
		int endYear = endDate.year();
		int endMonth = endDate.month();
		int endDay = endDate.dayOfMonth();
	
		switch(increment) {
			case DAY:
				endYear = startYear;
				endMonth = startMonth;
				endDay = startDay;
				break;
				
			case WEEK:
				endYear = startYear;
				endMonth = startMonth;
				// Add 6 to encompass a whole week.
				// Warning! Doesn't start or end on any particular day of the week.
				endDay = startDay + 6;

				break;
				
			case JAN:
			case FEB:
			case MAR:
			case APR:
			case MAY:
			case JUN:
			case JUL:
			case AUG:
			case SEP:
			case OCT:
			case NOV:
			case DEC:
				startMonth = increment.startMonth;
                startDay = 1;
                endYear = startYear;
				endMonth = startMonth;				
				endDay = Timestamp.daysInMonth(startMonth, startYear);;
				break;

			case YEAR:
				
				// Use a full years, Jan 1 to Dec 31, starting in the start year.	
				startMonth = 1;
				startDay = 1;
				endYear = startYear;
				endMonth = 12;
				endDay = 31;
				break;				

			case DJF:	// December-January-February
				endYear = startYear + 1;
				startMonth = increment.startMonth();
				startDay = 1;
				endMonth = 2;
				endDay = Timestamp.daysInMonth(2, endYear);
				break;
				
			case MAM:  // March-April-May
				startMonth = increment.startMonth();
				startDay = 1;
				endYear = startYear;
				endMonth = startMonth + 2;
				endDay = 31;
				break;
				
			case JJA:  // June-July-August
				startMonth = increment.startMonth();
				startDay = 1;
				endYear = startYear;
				endMonth = startMonth + 2;
				endDay = 31;
				break;
				
			case SON:  // September-October-November
				startMonth = increment.startMonth();
				startDay = 1;
				endYear = startYear;
				endMonth = startMonth + 2;
				endDay = 30;
				break;
				
			case NONE:
			default:
				break;
		}
		
		startDate = new Timestamp( startYear, startMonth, startDay );
		endDate = new Timestamp( endYear, endMonth, endDay );
	}

	
	// Various sets and gets.
	
	public void startTimestamp(Timestamp t) { startDate = t; }
	public Timestamp startTimestamp() { return startDate; }
	
	public void endTimestamp(Timestamp t) { endDate = t; }
	public Timestamp endTimestamp() { return endDate; }	

	public Timespan.Increment increment() { return increment; }
	
	/*
	 * days
	 * 
	 * Return the integer number of whole days in the time span.
	 */
	public int days() {

		int days = 0;
		
		switch (increment) {
			case DAY:
				days = 1;
				break;
				
			case WEEK:
				days = 7;
				break;
				
			case JAN:
			case FEB:
			case MAR:
			case APR:
			case MAY:
			case JUN:
			case JUL:
			case AUG:
			case SEP:
			case OCT:
			case NOV:
			case DEC:
				days = increment.days;

				if (startDate.month() == 2) days = Timestamp.daysInMonth(2, startDate.year());
				break;

			case YEAR:
				days = 365;
				if (Timestamp.daysInMonth(2, startDate.year()) == 29) days = 366;
				break;

				
			case DJF:
				days = 62 + Timestamp.daysInMonth(2, startDate.year()+1); // Feb of next year
				break;

			case MAM:
			case JJA:
			case SON:
				days = increment.days;
				break;

			case NONE:
				days = (int) (Math.floor(endDate.days()) - Math.floor(startDate.days())) + 1;
				
			default:
				break;
		}
		
		return days;
	}

	/*
	 * nextDay
	 * 
	 * Given any day, return a timestamp of the next day in this timespan. Returns
	 * null if there are no more days in this timespan. This is useful for skipping
	 * over days that aren't in the time increment.
	 */
	public Timestamp nextDay( Timestamp t ) {

		// Sanity check
		if (t == null) return null;
		
		Timestamp date = new Timestamp(t);
		
		date.incrementDay();
		
		if (date.days() > endDate.days()) return null;
		
		return date;
	}
	
	// Print methods.

	public void print() {
		startDate.print();
		System.out.print("");
		endDate.print();
		Tools.message("Increment: " + increment.name());
	}
}
