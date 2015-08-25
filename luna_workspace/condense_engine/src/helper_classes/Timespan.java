package helper_classes;

/* Timespan
 * 
 * A span of time, from one timestamp to another. 
 */

public class Timespan extends GeoObject {
	
	public static enum Increment {
			DAY ("day"),
			WEEK ("week"),
			MONTH ("month"),
			YEAR ("year"),
			SEASONAL ("seasonal"),
			MULTIYEAR ("multiyear"),
			MULTIYEARMONTH ("multiyear-month"),
			MULTIYEARSEASONAL ("multiyear-seasonal");
    	private final String name;       
    	private Increment(String s) {name = s;}
    	public String toString() {return name;}
	}

	protected Timestamp startTime;	// Beginning of the timespan
	protected Timestamp endTime;	// End of the timespan.
	
	// Default time increment
    protected Increment increment = Increment.DAY;
    
	/*---------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------*/

	public Timespan() {
		startTime = new Timestamp( 0.0 );
		endTime = new Timestamp( 0.0 );
	}


	/* Timespan
	 * 
	 * Constructor using two timestamps
	 */
	public Timespan(Timestamp begin, Timestamp end) {
		startTime = begin;
		endTime = end;
	}


	/* Timespan
	 * 
	 * Constructor using a starting timestamp and number of days. 
	 */
	public Timespan(Timestamp begin, int days) {
		startTime = begin;
		endTime = begin;
		endTime.incrementDays( days );
	}

	
	/* Timespan
	 * 
	 * Constructor using a starting and ending timestamps, and an increment.
	 * If the start and end are the same, the time span will be a single increment. 
	 */
	public Timespan(Timestamp start, Timestamp end, Increment i) {
		
		startTime = start;
		endTime = end;
		
		setIncrement(i);
	}
	
	/* setIncrement
	 * 
	 * Find the start and end dates based on the selected increment. Requires
	 * that the start and end timestamps be set already.
	 */
	public void setIncrement(Increment i) {
	
		increment = i;
		
		int startYear = startTime.year();
		int startMonth = startTime.month();
		int startDay = startTime.dayOfMonth();

		// Defaults
		int endYear = endTime.year();
		int endMonth = endTime.month();
		int endDay = endTime.dayOfMonth();
	
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
				
			case MONTH:
			case MULTIYEARMONTH:
                startDay = 1;

				if (i == Increment.MONTH) endYear = startYear;
				endMonth = startMonth;				
				endDay = Timestamp.daysInMonth(endMonth, endYear);

				break;

			case SEASONAL:
			case MULTIYEARSEASONAL:

				startDay = 1;
				if (i == Increment.SEASONAL) endYear = startYear;

				// December-January-February (90 or 91 days, depending on leap years)
				if (startMonth == 12 || startMonth == 1 || startMonth == 2) {

					// An exception if starting in Jan or Feb. User is stupid. Set the start year back to Dec.
					if (startMonth != 12) startYear = startYear - 1;
					
					startMonth = 12;
					endMonth = 2;

					// Special case for single-season increment time spans
					if (i == Increment.SEASONAL) endYear = startYear + 1;

					endDay = Timestamp.daysInMonth(2, endYear);
				}
				
				// March-April-May (92 days)
				if (startMonth == 3 || startMonth == 4 || startMonth == 5) {
					startMonth = 3;
					endMonth = 5;
					endDay = 31;
				}

				// June-July-August (92 days)
				if (startMonth == 6 || startMonth == 7 || startMonth == 8) {
					startMonth = 6;
					endMonth = 8;
					endDay = 31;
				}

				// September-October-November (91 days)
				if (startMonth == 9 || startMonth == 10 || startMonth == 11) {	// SON
					startMonth = 9;
					endMonth = 11;
					endDay = 30;
				}	
				
				break;
				
			case YEAR:
			case MULTIYEAR:
				
				// Use a full years, Jan 1 to Dec 31, starting in the start year.
				
				startYear = startTime.year();
				startMonth = 1;
				startDay = 1;

				if (i == Increment.YEAR) endYear = startYear;
				endMonth = 12;
				endDay = 31;
				
				break;
				
			default:
				break;
		}
		
		startTime = new Timestamp( startYear, startMonth, startDay );
		endTime = new Timestamp( endYear, endMonth, endDay );
	}

	
	// Various sets and gets.
	
	public void startTimestamp(Timestamp t) { startTime = t; }
	public Timestamp startTimestamp() { return startTime; }
	
	public void endTimestamp(Timestamp t) { endTime = t; }
	public Timestamp endTimestamp() { return endTime; }	

	/*
	 * fullDays
	 * 
	 * Return the integer number of whole days in the time span.
	 */
	public int fullDays() {

		int days = 0;
		
		switch (increment) {
			case DAY:
			case WEEK:
			case MONTH:
			case YEAR:
			case SEASONAL:
			case MULTIYEAR:
				days = ((int) endTime.days() - (int) startTime.days()) + 1;
				break;
				
			case MULTIYEARMONTH:
				for (int y = startTime.year(); y <= endTime.year(); y++) {
					days += Timestamp.daysInMonth(startTime.month(), y); 
				}
				break;
				
			case MULTIYEARSEASONAL:
				// Iterate over the seasonal time span, counting the days.
				for (int y = startTime.year(); y <= endTime.year(); y++) {
					if (startTime.month() == 3) days = days + 92;  // MAM
					if (startTime.month() == 6) days = days + 92;  // JJA
					if (startTime.month() == 9) days = days + 91;  // SON
					
					// DJF is a special case because it spans the year change. Also
					// could have a leap February.
					if (startTime.month() == 12) {
						if (y == endTime.year()) break;
						days = days + 62 + Timestamp.daysInMonth(2, y+1);
					}

				}
				break;

			default:
				break;
		}
		
		return days;
	}

	
	// Print methods.

	public void print() {
		startTime.print();
		System.out.print("");
		endTime.print();
	}

}
