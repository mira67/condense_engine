package helper_classes;

/* Timespan
 * 
 * A span of time, from one timestamp to another. 
 */

public class Timespan extends GeoObject {
	
	public static enum Increment { WEEK, MONTH, YEAR, SEASONAL }

	protected Timestamp startTime;	// Beginning of the timespan
	protected Timestamp endTime;	// End of the timespan.

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
	 * Constructor using a starting timestamp and an increment. 
	 */
	public Timespan(Timestamp begin, Increment i) {
		
		startTime = begin;
		
		int startYear = begin.year();
		int startMonth = begin.month();
		int startDay = begin.dayOfMonth();
		int endYear = startYear;
		int endMonth = startMonth;
		int endDay = startDay;
		
		switch(i) {
			case WEEK:
				
				// Add 6 to encompass a whole week.
				// Warning! Doesn't start or end on any particular day of the week.
				endDay = startDay + 6;

				break;
				
			case MONTH:

				startDay = 1;				
				endDay = Timestamp.daysInMonth(endMonth, endYear);

				break;
				
			case SEASONAL:

				startDay = 1;
				endYear = startYear;

				if (startMonth == 12 || startMonth == 1 || startMonth == 2) {	// DJF
					startMonth = 12;
					endMonth = 2;
					endYear = startYear + 1;
					endDay = Timestamp.daysInMonth(2, endYear);
				}
				
				if (startMonth == 3 || startMonth == 4 || startMonth == 5) {	// MAM
					startMonth = 3;
					endMonth = 5;
					endDay = 31;
				}

				if (startMonth == 6 || startMonth == 7 || startMonth == 8) {	// JJA
					startMonth = 6;
					endMonth = 8;
					endDay = 31;
				}

				if (startMonth == 9 || startMonth == 10 || startMonth == 11) {	// SON
					startMonth = 9;
					endMonth = 11;
					endDay = 30;
				}
				
				break;
				
			case YEAR:
				
				// Use a full year, Jan 1 to Dec 31, starting in the start year.
				
				startYear = startTime.year();
				startMonth = 1;
				startDay = 1;
				endMonth = 12;
				endDay = 31;
				
				break;
		}
		
		startTime = new Timestamp( startYear, startMonth, startDay );
		endTime = new Timestamp( endYear, endMonth, endDay );
		
		Tools.debugMessage("TIMESPAN: " + startTime.dateString() + " to " +
							endTime.dateString());
	}

	
	// Various sets and gets.
	
	public void startTimestamp(Timestamp t) { startTime = t; }
	public Timestamp startTimestamp() { return startTime; }
	
	public void endTimestamp(Timestamp t) { endTime = t; }
	public Timestamp endTimestamp() { return endTime; }
	
	// Total number of elapsed days.
	public double days() {return (endTime.days() - startTime.days());}
	public int fullDays() {return ((int) endTime.days() - (int) startTime.days()) + 1;}

	
	// Print methods.

	public void print() {
		startTime.print();
		System.out.print("");
		endTime.print();
	}

}
