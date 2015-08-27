package helper_classes;

/* Timespan
 * 
 * A span of time, from one timestamp to another. 
 */

public class Timespan extends GeoObject {
	
	public static enum Increment {
			DAY ("day", 0, false),
			WEEK ("week", 0, false),
			MONTH ("month", 0, false),
			YEAR ("year", 0, false),
			SEASONAL ("seasonal", 0, false),
			MULTIYEARJAN ("multiyear-january", 1, true),
			MULTIYEARFEB ("multiyear-february", 2, true),
			MULTIYEARMAR ("multiyear-march", 3, true),
			MULTIYEARAPR ("multiyear-april", 4, true),
			MULTIYEARMAY ("multiyear-may", 5, true),
			MULTIYEARJUN ("multiyear-june", 6, true),
			MULTIYEARJUL ("multiyear-july", 7, true),
			MULTIYEARAUG ("multiyear-august", 8, true),
			MULTIYEARSEP ("multiyear-september", 9, true),
			MULTIYEAROCT ("multiyear-october", 10, true),
			MULTIYEARNOV ("multiyear-november", 11, true),
			MULTIYEARDEC ("multiyear-december", 12, true),
			MULTIYEARDJF ("multiyear-seasonal-DJF", 0, true),
			MULTIYEARMAM ("multiyear-seasonal-MAM", 0, true),
			MULTIYEARJJA ("multiyear-seasonal-JJA", 0, true),
			MULTIYEARSON ("multiyear-seasonal-SON", 0, true);
    	private final String name;
    	private final int month;
    	private final boolean multiyear;
    	private Increment(String s, int m, boolean flag) {name = s; month = m; multiyear = flag;}
    	public String toString() {return name;}
    	public int month() {return month;}
    	public boolean isMultiyear() {return multiyear;}
	}

	protected Timestamp startDate;	// Beginning of the timespan
	protected Timestamp endDate;	// End of the timespan.
	
	// Default time increment
    protected Increment increment = Increment.DAY;
    
	/*---------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------*/

	public Timespan() {
		startDate = new Timestamp( 0.0 );
		endDate = new Timestamp( 0.0 );
	}


	/* Timespan
	 * 
	 * Constructor using two timestamps
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
	 * If the start and end are the same, the time span will be a single increment. 
	 */
	public Timespan(Timestamp start, Timestamp end, Increment i) {
		
		startDate = start;
		endDate = end;
		
		setIncrement(i);
	}
	
	/* setIncrement
	 * 
	 * For a given timespan, reset the start and end dates to the selected increment.
	 * Requires that the start and end timestamps be set already. This is mostly
	 * based on the start date.
	 * 
	 * If a year increment is selected, the start and end dates will encompass
	 * the entire year of the start date.
	 * 
	 * If a month increment is selected, the start and end dates will encompass
	 * the entire start month, no fractional months.
	 * 
	 * If a multiyear-seasonal or multiyear-monthly increment is selected, the
	 * start/end months and days are replaced with the entire span.
	 */
	public void setIncrement(Increment i) {
	
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
				
			case MONTH:
                startDay = 1;
                endYear = startYear;
				endMonth = startMonth;				
				endDay = Timestamp.daysInMonth(endMonth, endYear);

				break;

			case YEAR:
				
				// Use a full years, Jan 1 to Dec 31, starting in the start year.	
				startYear = startDate.year();
				startMonth = 1;
				startDay = 1;

				if (i == Increment.YEAR) endYear = startYear;
				endMonth = 12;
				endDay = 31;
				
				break;

			case MULTIYEARJAN:
	            startDay = 1;
                startMonth = 1;
                endDay = 31;
				endMonth = 1;
				break;
			case MULTIYEARFEB:
                startDay = 1;
                startMonth = 2;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = Timestamp.daysInMonth(endMonth, endYear);
				endMonth = 2;
				break;
			case MULTIYEARMAR:
                startDay = 1;
                startMonth = 3;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 31;
				endMonth = 3;
				break;
			case MULTIYEARAPR:
                startDay = 1;
                startMonth = 4;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 30;
				endMonth = 4;
				break;
			case MULTIYEARMAY:
                startDay = 1;
                startMonth = 5;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 31;
				endMonth = 5;
				break;
			case MULTIYEARJUN:
                startDay = 1;
                startMonth = 6;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 30;
				endMonth = 6;
				break;
			case MULTIYEARJUL:
                startDay = 1;
                startMonth = 7;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 31;
				endMonth = 7;
				break;
			case MULTIYEARAUG:
                startDay = 1;
                startMonth = 8;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 31;
				endMonth = 8;
				break;
			case MULTIYEARSEP:
                startDay = 1;
                startMonth = 9;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 30;
				endMonth = 9;
				break;
			case MULTIYEAROCT:
                startDay = 1;
                startMonth = 10;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 31;
				endMonth = 10;
				break;
			case MULTIYEARNOV:
                startDay = 1;
                startMonth = 11;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 30;
				endMonth = 11;
				break;
			case MULTIYEARDEC:
                startDay = 1;
                startMonth = 12;
                if (endMonth < startMonth) endYear = endYear - 1;
                endDay = 31;
				endMonth = 12;
				break;
				
			case SEASONAL:
				startDay = 1;
				endYear = startYear;

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
				

			case MULTIYEARDJF:	// December-January-February
				// If the starting month is Jan or Feb, back up one year to start in Dec.
				if (startMonth < 3) startYear--;
				startMonth = 12;
				startDay = 1;
				endMonth = 2;
				endDay = Timestamp.daysInMonth(2, endYear);
				break;
				
			case MULTIYEARMAM:  // March-April-May
				startMonth = 3;
				startDay = 1;
                if (endMonth < startMonth) endYear = endYear - 1;
				endMonth = 5;
				endDay = 31;
				break;
				
			case MULTIYEARJJA:  // June-July-August
				startMonth = 6;
				startDay = 1;
                if (endMonth < startMonth) endYear = endYear - 1;
				endMonth = 8;
				endDay = 31;
				break;
				
			case MULTIYEARSON:  // September-October-November
				startMonth = 9;
				startDay = 1;
                if (endMonth < startMonth) endYear = endYear - 1;
				endMonth = 11;
				endDay = 30;
				break;
				
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

	/*
	 * fullDays
	 * 
	 * Return the integer number of whole days in the time span.
	 */
	public int fullDays() {

		int days = 0;
		
		switch (increment) {
			case DAY:
				days = 1;
				break;
				
			case WEEK:
				days = 7;
				break;
				
			case MONTH:
				days = 31;

				if (startDate.month() == 4 ||
					startDate.month() == 6 ||
					startDate.month() == 9 ||
					startDate.month() == 11) days = 30;

				if (startDate.month() == 2) days = Timestamp.daysInMonth(2, startDate.year());
				break;

			case YEAR:
				days = 365;
				if (Timestamp.daysInMonth(2, startDate.year()) == 29) days = 366;
				break;

			case SEASONAL:
				days = 92;
				break;

			// Months with 31 days
			case MULTIYEARJAN:
			case MULTIYEARMAR:
			case MULTIYEARMAY:
			case MULTIYEARJUL:
			case MULTIYEARAUG:
			case MULTIYEAROCT:
			case MULTIYEARDEC:
				days = ((endDate.year() - startDate.year()) + 1) * 31;
				break;
				
			// Months with 30 days
			case MULTIYEARAPR:
			case MULTIYEARJUN:
			case MULTIYEARSEP:
			case MULTIYEARNOV:
				days = ((endDate.year() - startDate.year()) + 1) * 30;
				break;

			// And then there's February...
			case MULTIYEARFEB:
				for (int y = startDate.year(); y <= endDate.year(); y++) {
					days += Timestamp.daysInMonth(2, y); 
				}
				break;
				
			case MULTIYEARDJF:
				days = (endDate.year() - startDate.year()) * 62;
				
				// Iterate over each February, counting the total days. The first February
				// is the next year after the start year, so the loop starts +1.
				for (int y = startDate.year() + 1; y <= endDate.year(); y++) {
					days = days + Timestamp.daysInMonth(2, y);
				}

				break;

			case MULTIYEARMAM:
				days = ((endDate.year() - startDate.year()) + 1) * 92;
				break;
			
			case MULTIYEARJJA:
				days = ((endDate.year() - startDate.year()) + 1) * 92;
				break;
			
			case MULTIYEARSON:
				days = ((endDate.year() - startDate.year()) + 1) * 91;
				break;

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
	public Timestamp nextDay( Timestamp d ) {

		Timestamp date = new Timestamp(d);
		
		date.incrementOneDay();
		
		// If we're incrementing multiple years over one particular month, and if we've
		// gone past that month, reset the date to the next year at the beginning
		// of the month, next year.
		if (increment.month() > 0 && increment.month() != date.month()) {
				date = new Timestamp(date.year()+1, increment.month(), 1);
		}
		
		// December 31st is a special case, don't increment the year twice.
		if (increment.month() == 12 && d.month() == 12 && d.dayOfMonth() == 31) {
				date = new Timestamp(d.year()+1, 12, 1);
		}
		
		// If we're incrementing multiple years over one season, and if we've
		// gone past that season, reset the date to the beginning of the next
		// year's season.
		if (increment == Timespan.Increment.MULTIYEARMAM && date.month() > 5) {
			date = new Timestamp(date.year()+1, 3, 1);
		}
		if (increment == Timespan.Increment.MULTIYEARJJA && date.month() > 8) {
			date = new Timestamp(date.year()+1, 6, 1);
		}
		if (increment == Timespan.Increment.MULTIYEARSON && date.month() > 11) {
			date = new Timestamp(date.year()+1, 9, 1);
		}
		if (increment == Timespan.Increment.MULTIYEARDJF && date.month() > 2 &&
				date.month() < 12) {
			date = new Timestamp(date.year(), 12, 1);
		}

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
