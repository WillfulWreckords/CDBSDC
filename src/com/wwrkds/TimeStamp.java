package com.wwrkds;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a custom class for manipulating time.<br>
 * <br>
 * All internal calculations and outputs of the TimeStamp class are performed in
 * UTC (GMT) time via Julian Date Number refernced to . The
 * TimeStamp.getJDNOffset() function can be used to get the offset for a
 * particular time zone.
 * 
 * @author <a href="mailto:lareaujo@saic.com">lareaujo(SAIC)</a>
 */
public class TimeStamp implements Comparator<TimeStamp>, Comparable<TimeStamp>,
		Serializable {
	public static final double epochoffset = 2440587.5; // Jan 1 1970

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Map<String, TimeZone> timezones = Collections
			.synchronizedMap(new HashMap<String, TimeZone>());

	public static TimeStamp add(TimeStamp t0, TimeStamp t1) {
		return new TimeStamp(t0.getJulianDateNumber()
				+ t1.getJulianDateNumber());
	}

	/**
	 * Returns time as long millis since 1970 (same as "UNIX time")
	 * 
	 * @see http://en.wikipedia.org/wiki/Julian_day
	 * @return
	 */
	public static long convertToMillis(TimeStamp t) {
		// Returns UTC Time
		return (long) ((t.getJulianDateNumber() - TimeStamp.epochoffset) * 1000 * 86400);
	}

	/**
	 * Converts a given date to a Julian Date Number converting all inputs
	 * except Seconds to integers first
	 * 
	 * @param YY
	 *            - Year (double)
	 * @param MM
	 *            - Month (double)
	 * @param DD
	 *            - Day (double)
	 * @param hh
	 *            - Hour (double)
	 * @param mm
	 *            - Minute (double)
	 * @param ss
	 *            - Second (double)
	 * @return the Julian Date Number
	 * @see http://en.wikipedia.org/wiki/Julian_day
	 */
	public static double date2Julian(double YY, double MM, double DD,
			double hh, double mm, double ss) {
		return TimeStamp.date2Julian((int) YY, (int) MM, (int) DD, (int) hh,
				(int) mm, ss);
	}

	/**
	 * Converts a given date to a Julian Date Number
	 * 
	 * @param YY
	 *            - Year (int)
	 * @param MM
	 *            - Month (int)
	 * @param DD
	 *            - Day (int)
	 * @param hh
	 *            - Hour (int)
	 * @param mm
	 *            - Minute (int)
	 * @param ss
	 *            - Second (double)
	 * @return the Julian Date Number
	 * @see http://en.wikipedia.org/wiki/Julian_day
	 */
	public static double date2Julian(int YY, int MM, int DD, int hh, int mm,
			double ss) {
		int a = (14 - MM) / 12;
		YY = YY + 4800 - a;
		MM = MM + 12 * a - 3;

		int JDN = DD + (153 * MM + 2) / 5 + 365 * YY + YY / 4 - YY / 100 + YY
				/ 400 - 32045;
		double jd = JDN + (hh - 12) / 24.0 + mm / 1440.0 + ss / 86400.0;
		return jd;
	}

	/**
	 * Converts a date time string. "YYYY-MM-DD hh:mm:ss.sss Z". Function uses a
	 * regular expression of the form:
	 * "(\\d\\d\\d\\d)(.(\\d\\d))?(.(\\d\\d))?(.(\\d\\d))?(.(\\d\\d))?(.(\\d\\d(\\.\\d+)*))?(\\s*(\\w*))?"
	 * 
	 * @param s
	 * @return
	 */
	public static double dateTimeString2Julian(String s) {
		s = s.trim();

		if (s.matches("(?i)nan") || s.matches("(?i)-?infinity")) {
			return Double.parseDouble(s);
		}

		if (s.matches("(?i)today")) {
			TimeStamp now = new TimeStamp();
			TimeStamp today = new TimeStamp(now.getYear(), now.getMonth(),
					now.getDay());
			return today.getJulianDateNumber();
		}

		if (s.matches("(?i)today[+-](\\d+).(\\d+).(\\d+)")) {
			TimeStamp now = new TimeStamp();
			TimeStamp today = new TimeStamp(now.getYear(), now.getMonth(),
					now.getDay());
			double base = today.getJulianDateNumber();

			s = s.toLowerCase().replace("today", "");

			boolean plus = true;
			if (s.startsWith("-")) {
				plus = false;
			}
			s = s.substring(1);

			Matcher m = Pattern.compile("(\\d+).(\\d+).(\\d+)").matcher(s);

			m.find();
			double hh = Double.parseDouble(m.group(1));
			double mm = Double.parseDouble(m.group(2));
			double ss = Double.parseDouble(m.group(3));
			if (plus) {
				base += hh / 24.0 + mm / 24.0 / 60.0 + ss / 24.0 / 60.0 / 6.0;
			} else {
				base -= hh / 24.0 + mm / 24.0 / 60.0 + ss / 24.0 / 60.0 / 6.0;
			}

			return base;
		}

		if (s.matches("(?i)now")) {
			return TimeStamp.now();
		}

		if (s.matches("(?i)now[+-](\\d+).(\\d+).(\\d+)")) {
			double base = TimeStamp.now();

			s = s.toLowerCase().replace("now", "");

			boolean plus = true;
			if (s.startsWith("-")) {
				plus = false;
			}
			s = s.substring(1);

			Matcher m = Pattern.compile("(\\d+).(\\d+).(\\d+)").matcher(s);

			m.find();
			double hh = Double.parseDouble(m.group(1));
			double mm = Double.parseDouble(m.group(2));
			double ss = Double.parseDouble(m.group(3));
			if (plus) {
				base += hh / 24.0 + mm / 24.0 / 60.0 + ss / 24.0 / 60.0 / 6.0;
			} else {
				base -= hh / 24.0 + mm / 24.0 / 60.0 + ss / 24.0 / 60.0 / 6.0;
			}

			return base;
		}

		// Regular expression for a JAVA double
		String db = "((-?(\\d)+(\\.)?(\\d)*)([eE]-?\\d+)?)|(-?Infinity)|(NaN)";

		if (s.matches(db)) {
			// if its given as a string representation of a double value, parse
			// the JDN
			return Double.parseDouble(s);
		}

		double YY = 2013, MM = 01, DD = 01, hh = 00, mm = 00, ss = 00;
		String timezone = "Z";

		s = s.trim();
		Pattern p = Pattern
				.compile("(\\d{4})(.(\\d{1,2}))?(.(\\d{1,2}))?(.(\\d{1,2}))?(.(\\d{1,2}))?(.(\\d{1,2}(\\.\\d+)*))?(\\s*([\\w+-:]*))?");

		// Pattern.compile("(\\d\\d\\d\\d)[:-](\\d\\d)[:-](\\d\\d).(\\d\\d)[:-](\\d\\d)[:-](\\d\\d(\\.\\d+)*)\\s*(\\w*)");
		Matcher m = p.matcher(s);
		boolean tf = m.find();
		if (tf) {
			String g = m.group(1);
			if (g != null && !g.trim().isEmpty()) {
				YY = Double.parseDouble(g.trim());
			}

			g = m.group(3);
			if (g != null && !g.trim().isEmpty()) {
				MM = Double.parseDouble(g.trim());
			}

			g = m.group(5);
			if (g != null && !g.trim().isEmpty()) {
				DD = Double.parseDouble(g.trim());
			}

			g = m.group(7);
			if (g != null && !g.trim().isEmpty()) {
				hh = Double.parseDouble(g.trim());
			}

			g = m.group(9);
			if (g != null && !g.trim().isEmpty()) {
				mm = Double.parseDouble(g.trim());
			}

			g = m.group(11);
			if (g != null && !g.trim().isEmpty()) {
				ss = Double.parseDouble(g.trim());
			}

			g = m.group(14);
			if (g != null && !g.trim().isEmpty()) {
				timezone = g.trim();
			}
		} else {
			// fallback ...
			YY = Double.parseDouble(s.substring(0, 4));
			MM = Double.parseDouble(s.substring(5, 7));
			DD = Double.parseDouble(s.substring(8, 10));
			hh = Double.parseDouble(s.substring(11, 13));
			mm = Double.parseDouble(s.substring(14, 16));
			ss = Double.parseDouble(s.substring(17, 19));
			timezone = s.substring(19);
		}
		if (timezone != null && !timezone.trim().isEmpty()) {
			timezone = timezone.trim();
		} else {
			timezone = "GMT";
		}
		double jdn = TimeStamp.date2Julian(YY, MM, DD, hh, mm, ss);
		double off = 0;
		off = TimeStamp.getJDNOffset(timezone);
		return jdn - off;
	}

	/**
	 * Get the string representation of the given Julian Date Number
	 * 
	 * @param jd
	 * @return
	 */
	public static String getDateString(double jd) {
		double[] DT = TimeStamp.julian2Date(jd);
		String str = String.format("%4.0f-%02.0f-%02.0f", DT[0], DT[1], DT[2]);
		return str;
	}

	/**
	 * Get the string representation of the given Julian Date Number
	 * 
	 * @param jd
	 * @return
	 */
	public static String getDateTimeString(double jd) {
		double[] DT = TimeStamp.julian2Date(jd);
		double SS = Math.round(DT[5]);
		double yy = DT[0];
		double mm = DT[1];
		double dd = DT[2];
		double HH = DT[3];
		double MM = DT[4];
		if (SS >= 60) {
			SS -= 60;
			MM++;
		}
		if (MM >= 60) {
			MM -= 60;
			HH++;
		}
		if (HH >= 24) {
			HH -= 24;
			dd++;
		}
		return String.format("%04.0f-%02.0f-%02.0fT%02.0f:%02.0f:%02.0fZ", yy,
				mm, dd, HH, MM, SS);
	}

	/**
	 * Get the string representation of the given Time Object
	 * 
	 * @param t
	 * @return
	 */
	public static String getDateTimeString(TimeStamp t) {
		return TimeStamp.getDateTimeString(t.getJulianDateNumber());
	}

	/**
	 * Get the string representation of the given Julian Date Number (safe for
	 * using in filename)
	 * 
	 * @param jd
	 * @return
	 */
	public static String getDateTimeStringF(double jd) {
		double[] DT = TimeStamp.julian2Date(jd);
		double SS = Math.round(DT[5]);
		double yy = DT[0];
		double mm = DT[1];
		double dd = DT[2];
		double HH = DT[3];
		double MM = DT[4];
		if (SS >= 60) {
			SS -= 60;
			MM++;
		}
		if (MM >= 60) {
			MM -= 60;
			HH++;
		}
		if (HH >= 24) {
			HH -= 24;
			dd++;
		}
		return String.format("%04.0f%02.0f%02.0f_%02.0f%02.0f%02.0fZ", yy, mm,
				dd, HH, MM, SS);
	}

	/**
	 * What is the offset from GMT of the given time zone returned in JDN days.
	 * 
	 * @param timezone
	 *            as defined by the java.util.TimeZone class
	 * @return
	 */
	public static double getJDNOffset(String timezone) {
		TimeZone tz;
		if (TimeStamp.timezones.containsKey(timezone)) {
			tz = TimeStamp.timezones.get(timezone);
		} else {
			tz = TimeZone.getTimeZone(timezone);
			TimeStamp.timezones.put(timezone, tz);
		}
		return tz.getRawOffset() / 1000.0 / 86400.0;
	}

	/**
	 * Get the string representation of the given Julian Date Number
	 * 
	 * @param jd
	 * @return
	 */
	public static String getTimeString(double jd) {
		double[] DT = TimeStamp.julian2Date(jd);
		return String.format("%02.0f:%02.0f:%02.3f", DT[3], DT[4], DT[5]);
	}

	/**
	 * Compute the GMST (Greenwich Mean Sidereal Time) and GAST (Greenwich
	 * Apparent Sidereal Time) using TimeStamp.now() as input. Output is in
	 * radians by default, but can be given as 'radians', 'degrees', or
	 * 'hour-angle'
	 * 
	 * @see http
	 *      ://www.usno.navy.mil/USNO/astronomical-applications/astronomical-
	 *      information-center/approx-sider-time
	 * @param jd
	 * @param output_units
	 * @return
	 */
	public static double GMST() {
		return TimeStamp.GMST(TimeStamp.now(), "radians");
	}

	/**
	 * Compute the GMST (Greenwich Mean Sidereal Time) and GAST (Greenwich
	 * Apparent Sidereal Time) from a given julian date number (UTC/UT). Output
	 * is in radians by default, but can be given as 'radians', 'degrees', or
	 * 'hour-angle'
	 * 
	 * @see http
	 *      ://www.usno.navy.mil/USNO/astronomical-applications/astronomical-
	 *      information-center/approx-sider-time
	 * @param jd
	 * @param output_units
	 * @return
	 */
	public static double GMST(double jd) {
		return TimeStamp.GMST(jd, "radians");
	}

	/**
	 * Compute the GMST (Greenwich Mean Sidereal Time) and GAST (Greenwich
	 * Apparent Sidereal Time) from a given julian date number (UTC/UT). Output
	 * is in radians by default, but can be given as 'radians', 'degrees', or
	 * 'hour-angle'
	 * 
	 * @see http
	 *      ://www.usno.navy.mil/USNO/astronomical-applications/astronomical-
	 *      information-center/approx-sider-time
	 * @param jd
	 * @param output_units
	 * @return
	 */
	public static double GMST(double jd, String output_units) {

		double jdp = Math.floor(jd - .5) - .5; // Julian date of previous
		// midnight
		double H = (jd - jdp) * 24; // %Hours elapsed since previous midnight
		double D = jd - 2451545; // %difference from 1/1/2000 12:00:00 UT/UTC
		double D0 = jdp - 2451545; // %difference from 1/1/2000 12:00:00 UT/UTC
		double T = D / 36525; // %Number of centuries since 1/1/2000

		// %Alternative formula with loss of .1 second per century
		// gmst = 18.697374558 + 24.06570982441908 * (jd-2451545);
		// gmst = mod(gmst,24); %Reduce to range of 0-24
		// gmst = gmst*15; %There are 15 degrees per sidereal hour

		// %Standard formula
		double gmst = 6.697374558 + 0.06570982441908 * D0 + 1.00273790935 * H
				+ 0.000026 * T * T;
		gmst = TimeStamp.mod(gmst, 24); // %Reduce to range of 0-24
		gmst = gmst * 15; // %There are 15 degrees per sidereal hour

		// %Calculation of GAST (Greenwich Apparent Sidereal Time)
		double Omega = 125.04 - .052954 * D; // %Longitude of Ascending Node of
		// Moon
		double L = 280.47 + 0.98565 * D; // %Mean Longitude of the Sun
		double Epsilon = 23.4393 - .0000004 * D; // %Obliquity
		double DeltaPsi = -.000319 * Math.sin(Omega) - .000024
				* Math.sin(2 * L);
		double eqeq = DeltaPsi * Math.cos(Epsilon); // %Equation of the
		// Equinoxes
		double gast = gmst + eqeq;

		if (output_units.contentEquals("radians")) {
			gmst = gmst * Math.PI / 180;
			gast = gast * Math.PI / 180;
		} else if (output_units.contentEquals("degrees")) {
			// %Do nothing
		} else if (output_units.contentEquals("hour-angle")) {
			gmst = gmst / 15;
			gast = gast / 15;
		}
		return gmst;
	}

	/**
	 * Compute the GMST (Greenwich Mean Sidereal Time) and GAST (Greenwich
	 * Apparent Sidereal Time) using TimeStamp.now() as input. Output is in
	 * radians by default, but can be given as 'radians', 'degrees', or
	 * 'hour-angle'
	 * 
	 * @see http
	 *      ://www.usno.navy.mil/USNO/astronomical-applications/astronomical-
	 *      information-center/approx-sider-time
	 * @param jd
	 * @param output_units
	 * @return
	 */
	public static double GMST(String str) {
		return TimeStamp.GMST(TimeStamp.now(), str);
	}

	/**
	 * Convert the given Julian Date number to an [6 x 1] Array representing YY,
	 * MM, DD, hh, mm, ss fields
	 * 
	 * @param jd
	 * @return
	 */
	public static double[] julian2Date(double jd) {
		double[] ret = { 0, 0, 0, 0, 0, 0 };
		double J, j, g, dg, c, dc, b, db, a, da, y, m, d, YY, MM, DD, T, hh, mm, ss;
		J = Math.floor(jd + .5);
		j = J + 32044;
		g = Math.floor(j / 146097);
		dg = TimeStamp.mod(j, 146097);
		c = Math.floor((Math.floor(dg / 36524) + 1) * 3 / 4);
		dc = dg - c * 36524;
		b = Math.floor(dc / 1461);
		db = TimeStamp.mod(dc, 1461);
		a = Math.floor((Math.floor(db / 365) + 1) * 3 / 4);
		da = db - a * 365;

		y = g * 400 + c * 100 + b * 4 + a;
		m = Math.floor((da * 5 + 308) / 153) - 2;
		d = da - Math.floor((m + 4) * 153 / 5) + 122;

		YY = y - 4800 + Math.floor((m + 2) / 12);
		MM = TimeStamp.mod(m + 2, 12) + 1;
		DD = d + 1;

		T = jd - Math.floor(jd + .5);
		hh = Math.floor(T * 24);
		T = T - hh / 24;
		hh = hh + 12;
		mm = Math.floor(T * 1440);
		T = T - mm / 1440;
		ss = T * 86400;

		ret[0] = YY;
		ret[1] = MM;
		ret[2] = DD;
		ret[3] = hh;
		ret[4] = mm;
		ret[5] = ss;
		return ret;
	}

	/**
	 * Return the day of the week (as an integer 0=Monday) given a Julian Date
	 * Number
	 * 
	 * @param jd
	 * @return
	 */
	public static int julian2Weekday(double jd) {
		double[] d = TimeStamp.julian2Date(jd);
		double JDN = jd - (d[3] - 12) / 24.0 - d[4] / 1440.0 - d[5] / 86400.0;
		return (int) (JDN % 7.0); // day of the week
	}

	public static void main(String[] args) {
		TimeStamp ts;
		ts = new TimeStamp(TimeStamp.now("YYYY-MM-DD hh:mm:S"));
		System.out.print(ts.toString(TimeZone.getTimeZone("EST")));
		System.out.println(" OR " + ts.toString());

		ts = new TimeStamp("2011-01-12 22:15:30.526 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22:15:30.526");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22:15:30 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22:15 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011-01 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011 GMT");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22:15:30");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22:15");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12 22");
		System.out.println(ts);
		ts = new TimeStamp("2011-01-12");
		System.out.println(ts);
		ts = new TimeStamp("2011-01");
		System.out.println(ts);
		ts = new TimeStamp("2011");
		System.out.println(ts);
	}

	public static TimeStamp midpoint(TimeStamp t0, TimeStamp t1) {
		// GB
		TimeStamp t = new TimeStamp(
				(t0.getJulianDateNumber() + t1.getJulianDateNumber()) / 2);
		return t;
	}

	private static double mod(double a, double b) {
		return a % b;
	}

	/**
	 * @return the difference, measured in milliseconds, between the current
	 *         time and midnight, January 1, 1970 UTC.
	 */
	public static double MS1970() {
		return System.currentTimeMillis();
	}

	/**
	 * Convert from number of MS since 1970 to a Julian Date Number
	 * 
	 * @param ms
	 * @return
	 */
	public static double ms1970ToJDN(long ms) {
		return TimeStamp.epochoffset + ms / 1000.0 / 86400.0;
	}

	/**
	 * Returns current UTC time (in days)
	 * 
	 * @return
	 */
	public static double now() {
		// Returns UTC Time
		return TimeStamp.ms1970ToJDN(System.currentTimeMillis());
	}

	/**
	 * Return the current UTC time plus an offset (in days)...
	 * 
	 * @param delta
	 * @return
	 */
	public static double now(double delta) {
		// Returns UTC Time
		return TimeStamp.epochoffset + System.currentTimeMillis() / 1000.0
				/ 86400.0 + delta;
	}

	/**
	 * Return the jdn for now, but using the given date format (i.e.
	 * "YYYY-MM-DD" for the jdn representing the current year, month and day)
	 * 
	 * @param format
	 * @return
	 */
	public static double now(String format) {
		return new TimeStamp(new TimeStamp().toString(format))
				.getJulianDateNumber();
	}

	public static TimeStamp Subtract(TimeStamp t0, TimeStamp t1) {
		return new TimeStamp(t0.getJulianDateNumber()
				- t1.getJulianDateNumber());
	}

	/**
	 * Convert # of days given by a Julian Date Number to hours
	 * 
	 * @param jd
	 * @return
	 */
	public static double toHours(double jd) {
		return jd * 24;
	}

	/**
	 * Convert # of days given by a Julian Date Number to minutes
	 * 
	 * @param jd
	 * @return
	 */
	public static double toMinutes(double jd) {
		return jd * 1440;
	}

	public static double toMS1970(double jdn) {
		return (jdn - TimeStamp.epochoffset) * 1000.0 * 86400.0;
	}

	/**
	 * Convert # of days given by a Julian Date Number to seconds
	 * 
	 * @param jd
	 * @return
	 */
	public static double toSeconds(double jd) {
		return jd * 86400;
	}

	/**
	 * Convert # of days given by a Julian Date Number to years
	 * 
	 * @param jd
	 * @return
	 */
	public static double toYears(double jd) {
		return jd / 365.25;
	}

	private transient double DD = 01;
	private transient double hh = 00;
	private double julianDateNumber = TimeStamp.epochoffset;
	private transient double mm = 00;
	private transient double MM = 01;
	private transient double offsetFromUTC = 0; // in days
	private transient double ss = 00;
	private transient double YY = 1970;

	/**
	 * Initialize a Time Object with current UTC time
	 */
	public TimeStamp() {
		// intitialize with now UTC
		this.julianDateNumber = TimeStamp.now();
		double[] d = TimeStamp.julian2Date(this.julianDateNumber);
		this.YY = d[0];
		this.MM = d[1];
		this.DD = d[2];
		this.hh = d[3];
		this.mm = d[4];
		this.ss = d[5];
	}

	/**
	 * Initialize a Time object with a Julian Date Number
	 * 
	 * @param jd
	 */
	public TimeStamp(double jd) {
		this.julianDateNumber = jd;
		double[] d = TimeStamp.julian2Date(this.julianDateNumber);
		this.YY = d[0];
		this.MM = d[1];
		this.DD = d[2];
		this.hh = d[3];
		this.mm = d[4];
		this.ss = d[5];
	}

	/**
	 * Initialize the TimeStamp Object with an array of doubles
	 * 
	 * @param DT
	 *            = {YY,MM,DD,hh,mm,ss}
	 */
	public TimeStamp(double... DT) {
		if (DT.length > 0) {
			this.YY = DT[0];
		}
		if (DT.length > 1) {
			this.MM = DT[1];
		}
		if (DT.length > 2) {
			this.DD = DT[2];
		}
		if (DT.length > 3) {
			this.hh = DT[3];
		}
		if (DT.length > 4) {
			this.mm = DT[4];
		}
		if (DT.length > 5) {
			this.ss = DT[5];
		}
		this.julianDateNumber = TimeStamp.date2Julian(this.YY, this.MM,
				this.DD, this.hh, this.mm, this.ss);
	}

	/**
	 * constructor from a string of the form returned by
	 * getDateTimeString(double jd)
	 * 
	 * @param s
	 *            : a String in the format "2012-01-30T15:50:28Z"
	 */
	public TimeStamp(String s) {
		// FIXME: conversions to/from JDN need better precision. Somewhere in
		// the routines we're getting a small error injected into the LSB
		this.julianDateNumber = TimeStamp.dateTimeString2Julian(s);
		double[] d = TimeStamp.julian2Date(this.julianDateNumber);
		this.YY = d[0];
		this.MM = d[1];
		this.DD = d[2];
		this.hh = d[3];
		this.mm = d[4];
		this.ss = d[5];
	}

	/**
	 * Return a new TimeStamp Object that is equal to this TimeStamp plus a
	 * given number of days
	 * 
	 * @param days
	 * @return
	 */
	public TimeStamp addDays(double days) {
		return new TimeStamp(this.julianDateNumber + days);
	}

	/**
	 * Return a new TimeStamp Object that is equal to this TimeStamp plus a
	 * given number of hours
	 * 
	 * @param hours
	 * @return
	 */
	public TimeStamp addHours(double hours) {
		return new TimeStamp(this.julianDateNumber + hours / 24.0);
	}

	/**
	 * Return a new TimeStamp Object that is equal to this TimeStamp plus a
	 * given number of minutes
	 * 
	 * @param minutes
	 * @return new Time object
	 */
	public TimeStamp addMinutes(double minutes) {
		return new TimeStamp(this.julianDateNumber + minutes / (60.0 * 24.0));
	}

	public TimeStamp addSeconds(double seconds) {
		return new TimeStamp(this.julianDateNumber + seconds / (3600.0 * 24.0));
	}

	/**
	 * Returns a new TimeStamp that is equal to this plus the given number of
	 * years
	 * 
	 * @param years
	 * @return
	 */
	public TimeStamp addYears(double years) {
		return new TimeStamp(this.julianDateNumber + years * 354.25);
	}

	/**
	 * The Barycentric Julian Date (BJD) is the Julian Date (JD) corrected for
	 * differences in the Earth's position with respect to the barycentre of the
	 * Solar System. Due to the finite speed of light, the time an astronomical
	 * event is observed depends on the changing position of the observer in the
	 * Solar System. Before multiple observations can be combined, they must be
	 * reduced to a common, fixed, reference location. This correction also
	 * depends on the direction to the object or event being timed.
	 * 
	 * @param r
	 *            vector \vec{r} from the heliocentre to the observer
	 * @param n
	 *            unit vector \hat{n} from the observer toward the object or
	 *            event
	 * @param c
	 *            speed of light
	 * @param d
	 *            distance from observer to observed object
	 * @see http://en.wikipedia.org/wiki/Barycentric_Julian_Date
	 * @return
	 */
	public double barycentricJDN(double[] r, double[] n, double d, double c) {

		double dot = 0;
		double norm = 0;
		double rdr = 0;
		double rdn = 0;
		for (int i = 0; i < r.length; i++) {
			dot += r[i] * n[i];
			rdr += r[i] * r[i];
			norm += n[i] * n[i];
		}
		rdr /= norm;
		rdn /= norm;
		norm = Math.sqrt(norm);

		return this.julianDateNumber + rdn / c + (rdr - rdn * rdn)
				/ (2 * c * d);

	}

	private void check_hhmmss() {
		while (this.ss >= 60) {
			this.ss -= 60;
			this.mm++;
		}
		while (this.mm >= 60) {
			this.mm -= 60;
			this.hh++;
		}
		while (this.hh >= 24) {
			this.hh -= 24;
			this.DD++;
		}
	}

	/**
	 * The Chronological Julian Date was recently proposed by Peter
	 * Meyer[12][13] and has been used by some students of the calendar and in
	 * some scientific software packages.[14] CJD is usually defined relative to
	 * local civil time, rather than UT, requiring a time zone (tz) offset to
	 * convert from JD. In addition, days start at midnight rather than noon.
	 * Users of CJD sometimes refer to Julian Date as Astronomical Julian Date
	 * to distinguish it.
	 * 
	 * @param timeZone
	 *            as defined by java.util.TimeZone
	 * @return
	 */
	public double chronJDN(String timeZone) {

		return this.julianDateNumber + .5 + TimeStamp.getJDNOffset(timeZone);

	}

	@Override
	public int compare(TimeStamp t1, TimeStamp t2) {

		if (t1.getJulianDateNumber() < t2.getJulianDateNumber()) {
			return -1;
		} else if (t1.getJulianDateNumber() == t2.getJulianDateNumber()) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int compareTo(TimeStamp t2) {
		if (this.getJulianDateNumber() < t2.getJulianDateNumber()) {
			return -1;
		} else if (this.getJulianDateNumber() == t2.getJulianDateNumber()) {
			return 0;
		} else {
			return 1;
		}
	}

	public double differenceMinutes(TimeStamp t) {
		return (this.julianDateNumber - t.julianDateNumber) * 24 * 60;
	}

	@Override
	public boolean equals(Object t1) {
		return this.julianDateNumber == ((TimeStamp) t1).getJulianDateNumber();
	}

	/**
	 * Get a Date-Time String for this TimeStamp
	 * 
	 * @return
	 */
	public String getDateString() {
		String str = TimeStamp.getDateString(this.julianDateNumber);
		return str;
	}

	/**
	 * Get a Date-Time String for this TimeStamp
	 * 
	 * @return
	 */
	public String getDateTimeString() {
		return TimeStamp.getDateTimeString(this.julianDateNumber);
	}

	/**
	 * @return {YYYY,MM,DD,hh,mm,ss}
	 */
	public double[] getDateVector() {
		return TimeStamp.julian2Date(this.julianDateNumber);
	}

	/**
	 * @return DD
	 */
	public int getDay() {
		return (int) this.DD;
	}

	/**
	 * @return the day of the week as an integer monday == 0
	 */
	public int getDayOfWeek() {
		return TimeStamp.julian2Weekday(this.julianDateNumber);
	}

	/**
	 * @return the fractional seconds
	 */
	public double getFracSecond() {
		return this.ss - this.getSecond();
	}

	/**
	 * @return hh
	 */
	public int getHour() {
		return (int) this.hh;
	}

	/**
	 * @return the julian date number for this TimeStamp
	 */
	public double getJulianDateNumber() {
		return this.julianDateNumber;
	}

	/**
	 * @return mm
	 */
	public int getMinute() {
		return (int) this.mm;
	}

	/**
	 * @return MM
	 */
	public int getMonth() {
		return (int) this.MM;
	}

	/**
	 * @return the number of days since the 1st of the year
	 */
	public double getOrdinalDayNumber() {
		return this.julianDateNumber
				- TimeStamp.date2Julian(this.YY, 1, 1, 0, 0, 0) + 1;
	}

	/**
	 * @return ss
	 */
	public int getSecond() {
		return (int) this.ss;
	}

	/**
	 * formatted string with rounded seconds
	 * 
	 * @return
	 */
	public String getString(boolean msec) {
		double[] DT = this.getDateVector();
		double s = DT[5];
		double m = DT[4];
		double h = DT[3];
		double d = DT[2];
		if (s >= 60) {
			s -= 60;
			m++;
			if (m >= 60) {
				m -= 60;
				h++;
				if (h >= 24) {
					h -= 24;
					d++;
				}
			}
		}
		// return String.format("%.0f/%.0f/%.0f %02.0f:%02.0f:%02.0f", DT[1], d,
		// DT[0], h, m, s);
		String tmp = null;
		if (msec == false) {
			s = Math.floor(s);
			tmp = String.format("%04.0f-%02.0f-%02.0fT%02.0f:%02.0f:%02.0fZ",
					DT[0], DT[1], d, h, m, s);
		} else {
			tmp = String.format("%04.0f-%02.0f-%02.0fT%02.0f:%02.0f:%02.3fZ",
					DT[0], DT[1], d, h, m, s);
		}
		return tmp;
	}

	/**
	 * Get a Date-Time String for this TimeStamp
	 * 
	 * @return
	 */
	public String getTimeString() {
		return TimeStamp.getTimeString(this.julianDateNumber);
	}

	/**
	 * @return the offset from UTC
	 */
	public double getUTCOffset() {
		return this.offsetFromUTC;
	}

	/**
	 * @return the string representation of the UTC offset
	 */
	public String getUTCOffsetString() {
		String zzzzzz = "";
		TimeStamp t = new TimeStamp(Math.abs(this.offsetFromUTC));
		if (this.offsetFromUTC < 0) {
			zzzzzz = "-" + t.getHour() + ":" + t.getMinute();
		} else if (this.offsetFromUTC == 0) {
			zzzzzz = "Z"; // no offset from UTC time...
		} else {
			zzzzzz = "+" + t.getHour() + ":" + t.getMinute();
		}
		return zzzzzz;
	}

	/**
	 * @return YYYY
	 */
	public int getYear() {
		return (int) this.YY;
	}

	/**
	 * In terms of the vector \vec{r} from the heliocentre to the observer, the
	 * unit vector \hat{n} from the observer toward the object or event, and the
	 * speed of light c:
	 * 
	 * @param r
	 *            vector \vec{r} from the heliocentre to the observer
	 * @param n
	 *            unit vector \hat{n} from the observer toward the object or
	 *            event
	 * @param c
	 *            speed of light
	 * @return
	 */
	public double helioCentricJDN(double[] r, double[] n, double c) {

		double dot = 0;
		double norm = 0;
		for (int i = 0; i < r.length; i++) {
			dot += r[i] * n[i];
			norm += n[i] * n[i];
		}
		norm = Math.sqrt(norm);

		return this.julianDateNumber + dot / c / norm;
	}

	/**
	 * Mars solar date
	 * 
	 * @return
	 */
	public double marsSolDate() {
		return (this.julianDateNumber - 2405522) / 1.02749;
	}

	/**
	 * Set the UTC offset
	 * 
	 * @param hour
	 * @param minute
	 */
	public void setUTCOffset(int hour, int minute) {
		this.offsetFromUTC = TimeStamp.date2Julian(0, 0, 0, 0, hour, minute);
	}

	/**
	 * The ANSI Date defines January 1, 1601 as day 1, and is used as the origin
	 * of COBOL integer dates. This epoch is the beginning of the previous
	 * 400-year cycle of leap years in the Gregorian calendar, which ended with
	 * the year 2000.
	 * 
	 * @return
	 */
	public double toANSIDate() {
		return Math.floor(this.julianDateNumber - 2305812.5);
	}

	/**
	 * Returns the time elapsed since the reference epoch (Jan 1 1970) in days
	 * as represented by this TimeStamp (Same as Julian Date Number)
	 * 
	 * @return
	 */
	public double toDays() {
		return this.julianDateNumber;
	}

	/**
	 * The Dublin Julian Date (DJD) is the number of days that has elapsed since
	 * the epoch of the solar and lunar ephemerides used from 1900 through 1983,
	 * Newcomb's Tables of the Sun and Ernest W. Brown's Tables of the Motion of
	 * the Moon (1919). This epoch was noon UT on January 0, 1900, which is the
	 * same as noon UT on December 31, 1899. The DJD was defined by the
	 * International Astronomical Union at their 1955 meeting in Dublin,
	 * Ireland.
	 * 
	 * @return
	 */
	public double toDublinJDN() {
		return this.julianDateNumber - 2415020;
	}

	/**
	 * Convert this TimeStamp to GMST
	 * 
	 * @return
	 */
	public double toGMST() {
		return TimeStamp.GMST(this.julianDateNumber, "radians");
	}

	/**
	 * Returns the time elapsed since the reference epoch (Jan 1 1970) in hours
	 * as represented by this TimeStamp
	 * 
	 * @return
	 */
	public double toHours() {
		return TimeStamp.toHours(this.julianDateNumber);
	}

	/**
	 * Returns an equivalent Java Date Object
	 * 
	 * @return
	 */
	public Date toJavaDate() {
		Date dt = new Date((long) this.toMS1970());
		return dt;
	}

	/**
	 * The Lilian day number is a count of days of the Gregorian calendar and
	 * not defined relative to the Julian Date. It is an integer applied to a
	 * whole day; day 1 was October 15, 1582, which was the day the Gregorian
	 * calendar went into effect. The original paper defining it makes no
	 * mention of the time zone, and no mention of time-of-day.[15] It was named
	 * for Aloysius Lilius, the principal author of the Gregorian calendar.
	 * 
	 * @return
	 */
	public double toLilianDate() {
		return Math.floor(this.julianDateNumber - 2299159.5);
	}

	/**
	 * Returns the time elapsed since the reference epoch (Jan 1 1970) in
	 * minutes as represented by this TimeStamp
	 * 
	 * @return
	 */
	public double toMinutes() {
		return TimeStamp.toMinutes(this.julianDateNumber);
	}

	/**
	 * The Modified Julian Date (MJD) was introduced by the Smithsonian
	 * Astrophysical Observatory in 1957 to record the orbit of Sputnik via an
	 * IBM 704 (36-bit machine) and using only 18 bits until August 7, 2576. MJD
	 * is the epoch of OpenVMS, using 63-bit date/time postponing the next Y2K
	 * campaign to July 31, 31086 02:48:05.47.[7] MJD is defined relative to
	 * midnight, rather than noon.
	 * 
	 * @return
	 */
	public double toModifiedJDN() {
		return this.julianDateNumber - 2400000.5;
	}

	public double toMS1970() {
		//
		return TimeStamp.toMS1970(this.getJulianDateNumber());
	}

	/**
	 * Rata Die is a system (or more precisely a family of three systems) used
	 * in the book Calendrical Calculations. It uses the local timezone, and day
	 * 1 is January 1, 1, that is, the first day of the Christian or Common Era
	 * in the proleptic Gregorian calendar.
	 * 
	 * @return
	 */
	public double toRataDie() {
		return Math.floor(this.julianDateNumber - 1721424.5);
	}

	/**
	 * Days elapsed since 1858-11-16T12Z (JD 2400000.0)
	 * 
	 * @return
	 */
	public double toReducedJDN() {
		return this.julianDateNumber - 2400000.0;
	}

	/**
	 * Returns the time elapsed since the reference epoch (Jan 1 1970) in
	 * seconds as represented by this TimeStamp
	 * 
	 * @return
	 */
	public double toSeconds() {
		return TimeStamp.toSeconds(this.julianDateNumber);
	}

	@Override
	public String toString() {
		// ISO 8601 Standard
		if (Double.isInfinite(this.julianDateNumber)
				|| Double.isNaN(this.julianDateNumber)) {
			return this.julianDateNumber + "";
		}
		return this.toString("YYYY-MM-DDThh:mm:ssZ");
	}

	/**
	 * Output the TimeStamp String with the given format specifier. "YYYY"=year.
	 * "MM"=month. "DD"=day. "hh"=hour. "mm"=minute. "ss"=seconds (truncated).
	 * "fs"=fractions of a second (decimal). "S"=seconds with fractional part
	 * 
	 * @param format
	 * @return
	 */
	public String toString(String formatString) {
		String format = formatString + "";
		format = format.replace("YYYY", String.format("%04d", this.getYear()));
		format = format.replace("MM", String.format("%02d", this.getMonth()));
		format = format.replace("DD", String.format("%02d", this.getDay()));
		format = format.replace("hh", String.format("%02d", this.getHour()));
		format = format.replace("mm", String.format("%02d", this.getMinute()));
		format = format.replace("ss", String.format("%02d", this.getSecond()));
		format = format
				.replace("fs", String.format("%f", this.getFracSecond()));
		format = format.replace("S", String.format("%02f", this.ss));
		return format;
	}

	/**
	 * Output the TimeStamp String with the given format specifier and TimeZone.
	 * "YYYY"=year. "MM"=month. "DD"=day. "hh"=hour. "mm"=minute. "ss"=seconds
	 * (truncated). "fs"=fractions of a second (decimal). "S"=seconds with
	 * fractional part
	 * 
	 * @param formatString
	 * @param tz
	 * @return
	 */
	public String toString(String formatString, TimeZone tz) {
		double off = tz.getRawOffset() / 1000.0 / 86400.0;
		TimeStamp ts = new TimeStamp(this.getJulianDateNumber() + off);
		return ts.toString(formatString) + " " + tz.getDisplayName();
	}

	/**
	 * Output the TimeStamp string in the Given Time Zone
	 * 
	 * @param tz
	 * @return
	 */
	public String toString(TimeZone tz) {
		return this.toString("YYYY-MM-DDThh:mm:ss", tz);
	}

	/**
	 * The Truncated Julian Day (TJD) was introduced by NASA/Goddard in 1979 as
	 * part of a parallel grouped binary time code (PB-5)
	 * "designed specifically, although not exclusively, for spacecraft applications."
	 * TJD was a 4-digit day count from MJD 44000, which was May 24, 1968,
	 * represented as a 14-bit binary number. Since this code was limited to
	 * four digits, TJD recycled to zero on MJD 45000, or October 10, 1995,
	 * "which gives a long ambiguity period of 27.4 years". (NASA codes
	 * PB-1���PB-4 used a 3-digit day-of-year count.) Only whole days are
	 * represented. Time of day is expressed by a count of seconds of a day,
	 * plus optional milliseconds, microseconds and nanoseconds in separate
	 * fields. Later PB-5J was introduced which increased the TJD field to 16
	 * bits, allowing values up to 65535, which will occur in the year 2147.
	 * There are five digits recorded after TJD 9999.
	 * 
	 * @return
	 */
	public double toTruncatedJDN() {
		return this.julianDateNumber - 2440000.0;
	}

	/**
	 * Returns the time elapsed since the reference epoch (Jan 1 1970) in years
	 * as represented by this TimeStamp
	 * 
	 * @return
	 */
	public double toYears() {
		return TimeStamp.toYears(this.julianDateNumber);
	}

}
