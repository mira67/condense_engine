package helper_classes;

/*
 * Timestamp
 * 
 * A generic timestamp. 
 * 
 */

import java.util.*;
import java.text.*;

public class Timestamp {

	public static enum DateFormat {
		YYYYMMDD, MMDDYYYY
	}

	DateFormat dateFormat = DateFormat.YYYYMMDD;

	private Calendar cal; 	// Dopey Java Calendar object.
							// Mostly just used for it's time computation
							// methods.

	private double days; 	// Days since 1-1-1601. Technically this *is* the time
							// stamp.

	private int month; 		// 0 (January) - 11 (December)
	private int dayOfMonth; // Day of month, 1 - 31
	private int year;
	private int hour;
	private int minute;
	private int dayOfYear;

	protected int id = -1;	// Database table unique primary key. -1 means not in the database.
	
	protected static String dateSeparator = ".";

	/*---------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------*/

	protected Timestamp() {}

	/*
	 * Timestamp
	 * 
	 * Constructor using days since January 1, 1970 (Java default)
	 */
	public Timestamp(double daysInput) {

		days = daysInput;

		cal = Timestamp.daysSinceEpochToCal(daysInput);

		updateLocalVariables();
	}

	/*
	 * Timestamp
	 * 
	 * Constructor using days since January 1, 1970 as the epoch date,
	 * and a database index ID number
	 */
	public Timestamp(int indexID, double daysInput) {

		days = daysInput;

		cal = Timestamp.daysSinceEpochToCal(daysInput);

		updateLocalVariables();
		
		id = indexID;
	}

	/*
	 * Timestamp
	 * 
	 * Constructor using January 1 of the specified year. Use 1601 for NSIDC
	 * data.
	 */
	public Timestamp(double daysInput, int year) {

		days = daysInput;

		cal = Timestamp.convertDaysSinceEpoch(daysInput);

		updateLocalVariables();
	}

	/*
	 * Timestamp
	 * 
	 * Constructor using a Calendar object.
	 */
	public Timestamp(Calendar calendarObj) {

		// Set the internal calendar equal to the one passed in.
		cal = calendarObj;

		// Make sure we're using UTC time zone.
		TimeZone utcTimeZone = SimpleTimeZone.getTimeZone("UTC_TIME");
		cal.setTimeZone(utcTimeZone);

		updateLocalVariables();
	}

	/*
	 * Timestamp
	 * 
	 * Constructor using year, month, and day-of-month.
	 * 
	 * Note: internally month is indexed starting at zero: e.g., 0 = January. To
	 * the user, however, month is indexed from 1. Since this constructor is
	 * public, the month index is adjusted here.
	 */
	public Timestamp(int yearInput, int monthInput, int dayInput) {

		// Adjust the month indexing, 0 = January
		monthInput--;

		cal = Calendar.getInstance();
		cal.set(yearInput, monthInput, dayInput, 0, 0, 0); // And hour, minute,
															// second

		// Make sure we're using UTC time zone.
		TimeZone utcTimeZone = SimpleTimeZone.getTimeZone("UTC_TIME");
		cal.setTimeZone(utcTimeZone);

		updateLocalVariables();
	}

	/*
	 * Timestamp constructor
	 * 
	 * Constructor using year, month, and day-of-month as strings
	 */
	public Timestamp(String year, String month, String day) {

		this(Integer.valueOf(year), Integer.valueOf(month), Integer
				.valueOf(day));
	}

	/*
	 * clone a Timestamp
	 * 
	 */
	public Timestamp(Timestamp t) {
		this(t.days());
	}

	/*
	 * updateLocalVariables
	 * 
	 * Use the calendar value to update local variables.
	 */
	protected void updateLocalVariables() {
		month = cal.get(Calendar.MONTH); // Month is indexed 0-11 in the
											// calendar class.
		dayOfMonth = cal.get(Calendar.DATE);
		year = cal.get(Calendar.YEAR);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
		dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		days = Timestamp.getDays(cal);
		days = ((int) days * 100000) / 100000; // Get rid of calendar
												// millisecond residuals. yuck
	}

	// Various gets and sets.
	
	public static void dateSeparator(String s) { dateSeparator = s; }

	public double days() {
		return days;
	} // Days since epoch (DSE)

	public int msecs() {
		return cal.get(Calendar.MILLISECOND);
	} // Milliseconds since epoch

	public int month() {
		return month + 1;
	} // "Public" month is indexed from 1

	public int dayOfMonth() {
		return dayOfMonth;
	}

	public int year() {
		return year;
	}

	public int hour() {
		return hour;
	}

	public int minute() {
		return minute;
	}

	public int dayOfYear() {
		return dayOfYear;
	}
	
	public int id() {
		return id;
	}

	public Calendar getCalendar() {
		return (Calendar) cal.clone();
	} // You want this? Really? Okay...

	// Return values as strings

	public String monthString() {
		if (month + 1 > 9)
			return String.valueOf(month + 1);
		else
			return ("0" + String.valueOf(month + 1));
	}

	public String dayOfMonthString() {
		if (dayOfMonth > 9)
			return String.valueOf(dayOfMonth);
		else
			return ("0" + String.valueOf(dayOfMonth));
	}

	public String yearString() {
		return String.valueOf(year());
	}

	public String dayOfYearString() {
		if (dayOfYear > 99)
			return String.valueOf(dayOfYear);
		if (dayOfYear > 9)
			return ("0" + String.valueOf(dayOfYear));
		else
			return ("00" + String.valueOf(dayOfYear));
	}

	public String dateString() {
		switch (dateFormat) {
		case YYYYMMDD:
			return (yearString() + dateSeparator + monthString()
					+ dateSeparator + dayOfMonthString());
		case MMDDYYYY:
			return (monthString() + dateSeparator + dayOfMonthString()
					+ yearString() + dateSeparator);
		}

		return yearString() + monthString() + dayOfMonthString();
	}

	public void incrementOneDay() {
		cal.add(Calendar.DATE, 1);
		updateLocalVariables();
	}

	public void incrementDays(int d) {
		cal.add(Calendar.DATE, d);
		updateLocalVariables();
	}

	// Print methods.

	public void printLong() {
		System.out.println(
				" Timestamp: ID = " + id +
				" Days = " + days() +
				" Year = " + year() +
				" Month = " + month() + // When viewed, month is indexed from 1.
				" Date = " + dayOfMonth() +
				" Hour = " + hour() + 
				" Minute = " + minute() +
				" DOY = " + dayOfYear());
	}

	public void print() {
		System.out.print(id + " " + dateString() + " " + hour() + ":" + minute() + " "
				+ dayOfYear());
	}

	public void printCSV() {
		System.out.print(days() + "," + dayOfYear() + "," + year() + ","
				+ month() + "," + dayOfMonth() + "," + hour() + "," + minute());
	}

	// Static data conversion methods.

	/*
	 * convertDaysSinceEpoch
	 * 
	 * Given a number of days-since-epoch (January 1, 1601), return a calendar
	 * object containing the date and time.
	 */
	public static Calendar convertDaysSinceEpoch(Double daysInput) {

		Calendar calendar = Calendar.getInstance();
		TimeZone utcTimeZone = SimpleTimeZone.getTimeZone("UTC_TIME");

		// Calendar clear sets all calendar fields to undefined.
		calendar.clear();
		calendar.setTimeZone(utcTimeZone);

		// Initialize the calendar with the start of epoch, January 1st, 1601.
		// Arguments: year, month, day-of-month. Note: first month, January, is
		// zero!
		calendar.set(1601, 0, 1);

		// The calendar constructor only takes *integer* days, not a floating
		// point number.
		Integer intDays = daysInput.intValue();
		Double dayFraction = daysInput - ((double) intDays);
		Double hours = 24.0 * dayFraction;
		Double hourFraction = hours - (double) hours.intValue();
		Double minutes = 60.0 * hourFraction + 0.5;

		// Add the observation time to the default date.
		// /calendar.add(Calendar.DAY_OF_YEAR, intDays);
		calendar.add(Calendar.DATE, intDays);
		calendar.add(Calendar.HOUR_OF_DAY, hours.intValue());
		calendar.add(Calendar.MINUTE, minutes.intValue());

		return calendar;
	}

	/*
	 * daysSinceEpochToCal
	 * 
	 * Given a number of days-since-epoch (January 1, 1970), return a calendar
	 * object containing the date and time.
	 */
	public static Calendar daysSinceEpochToCal(Double daysInput) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(SimpleTimeZone.getTimeZone("UTC_TIME"));
		calendar.set(1970, 0, 1);

		// The calendar constructor only takes *integer* days, not a floating
		// point number.
		Integer intDays = daysInput.intValue();
		Double dayFraction = daysInput - ((double) intDays);
		Double hours = 24.0 * dayFraction;
		Double hourFraction = hours - (double) hours.intValue();
		// /Double minutes = 60.0 * hourFraction + 0.5;
		Double minutes = 60.0 * hourFraction;

		// Add the observation time to the default date.
		calendar.add(Calendar.DATE, intDays);
		calendar.add(Calendar.HOUR_OF_DAY, hours.intValue());
		calendar.add(Calendar.MINUTE, minutes.intValue());

		return calendar;
	}

	/*
	 * convertYearToDays
	 * 
	 * Convert a year to the number of days-since-epoch (January 1, 1601) This
	 * is useful for changing Julian days since epoch (DSE) to a simple
	 * day-of-year (DOY).
	 */
	public static double convertYearToDays(int year) {

		// Create a calendar object for comparison.
		TimeZone utcTimeZone = SimpleTimeZone.getTimeZone("UTC_TIME");
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTimeZone(utcTimeZone);
		cal.set(year, 0, 1, 0, 0, 0);

		// Compare the two dates.
		return convertCalendarToDays(cal);
	}

	/*
	 * convertCalendarToDays
	 * 
	 * Convert a calendar date to the number of days-since-epoch (January 1,
	 * 1601)
	 */
	public static double convertCalendarToDays(Calendar cal) {

		Calendar epoch = Calendar.getInstance();
		TimeZone utcTimeZone = SimpleTimeZone.getTimeZone("UTC_TIME");

		// Calendar clear sets all calendar fields to undefined.
		epoch.clear();
		epoch.setTimeZone(utcTimeZone);

		// Initialize the epoch calendar with the start of epoch, January 1st,
		// 1601.
		// Arguments: year, month, day-of-month. Note: first month, January, is
		// zero!
		epoch.set(1601, 0, 1, 0, 0, 0);

		// Compare the two dates.
		double diff = (double) (cal.getTimeInMillis() - epoch.getTimeInMillis());

		// Convert to days. ms/1000 = secs. secs/3600 = hours. hours/24=days.
		return ((diff / 1000.) / 3600.) / 24.;
	}

	/*
	 * convertDate
	 * 
	 * Convert a date in the format of mm-dd-yyyy into a calendar object.
	 */
	public static Calendar convertDate(String date) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("MM-dd-yyyy");
		Date processDate = sdf.parse(date);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(processDate);
		return calendar;
	}

	/*
	 * convertDateToDays
	 * 
	 * Convert a date (yyyy,mm,dd,hh,mm) to fractional days since epoch 1601.
	 */
	public static double convertDateToDays(int year, int month, int day,
			int hours, int minutes) {

		// Adjust the month indexing, 0 = January
		month--;

		// Create a calendar to hold this date
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.setTimeZone(SimpleTimeZone.getTimeZone("UTC_TIME"));

		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, day);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);

		// Initialize a calendar with the start of epoch, January 1st, 1601.
		// Arguments: year, month, day-of-month. Note: first month, January, is
		// zero!
		Calendar epoch = Calendar.getInstance();
		epoch.clear();
		epoch.setTimeZone(SimpleTimeZone.getTimeZone("UTC_TIME"));
		epoch.set(1601, 0, 1);

		// Find the difference in time between the two calendars
		double diff = (double) (calendar.getTimeInMillis() - epoch
				.getTimeInMillis());

		// Convert the difference into days
		diff = diff / 1000.0; // ...from milliseconds into seconds
		diff = diff / 3600.0; // ...from seconds into hours
		diff = diff / 24.0; // ...from hours into days

		return diff;
	}

	/*
	 * daysInMonth
	 * 
	 * How many days in a month? User must supply month and year.
	 */
	public static int daysInMonth(int m, int y) {
		
		// 30 days in April, June, September, November
		if (m == 4 || m == 6 || m == 9 || m == 11)
			return 30;

		// February, you bad boy.
		if (m == 2) {
			if (y % 4 != 0)
				return 28; // Not a leap year
			if (y % 100 == 0) { // Dunno
				if (y % 400 == 0)
					return 29; // Leap year
				return 28; // Not a leap year
			}
			return 29; // A leap year
		}

		return 31;
	}

	/*
	 * getDays
	 * 
	 * Days since epoch (DSE), including the fractional amount. The start of
	 * epoch is defined in Java as January 1, 1601.
	 */
	public static double getDays(Calendar c) {
		double dse = c.getTimeInMillis();
		dse = dse / 1000; // Now time in seconds
		dse = dse / 3600; // In hours
		dse = dse / 24; // In days
		return dse;
	}
}
