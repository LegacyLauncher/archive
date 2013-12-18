package com.turikhay.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Time {
	private static final String defaultFormat = "[day.month hour:minute]";
	private static Map<Object, Long> timers = new HashMap<Object, Long>();
	
	// 21600000
	private int offset;
	
	public Time(int rawOffset){
		int
			timezoneOffset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
		
		offset = rawOffset - timezoneOffset;
	}
	public Time(TimeZone timezone){ this(timezone.getRawOffset()); }
	
	public long current(){
		return System.currentTimeMillis() + offset;
	}
	public long seccurrent(){
		return Math.round(current() / 1000);
	}
	
	public String presentDate(Calendar cal){		
		return presentDate(cal, defaultFormat);
	}
	
	public String presentDate(String format){
		return presentDate(current(), format);
	}
	
	public String presentDate(){
		return presentDate(current());
	}
	
	public int[] unix(long unixtime) {
		Calendar ca = GregorianCalendar.getInstance();
		ca.setTimeZone(TimeZone.getTimeZone("UTC"));
		ca.setTimeInMillis(unixtime);
		int[] toret = new int[5];
		
		toret[0] = ca.get(Calendar.WEEK_OF_YEAR)-1;
		toret[1] = ca.get(Calendar.DAY_OF_WEEK)-5;
		toret[2] = ca.get(Calendar.HOUR_OF_DAY);
		toret[3] = ca.get(Calendar.MINUTE);
		toret[4] = ca.get(Calendar.SECOND);
		return toret;
	}
	
	public static String presentDate(Calendar cal, String format){
		int month = 0;
		
		switch(cal.get(Calendar.MONTH)){
		case Calendar.JANUARY: month = 1; break;
		case Calendar.FEBRUARY: month = 2; break;
		case Calendar.MARCH: month = 3; break;
		case Calendar.APRIL: month = 4; break;
		case Calendar.MAY: month = 5; break;
		case Calendar.JUNE: month = 6; break;
		case Calendar.JULY: month = 7; break;
		case Calendar.AUGUST: month = 8; break;
		case Calendar.SEPTEMBER: month = 9; break;
		case Calendar.OCTOBER: month = 10; break;
		case Calendar.NOVEMBER: month = 11; break;
		case Calendar.DECEMBER: month = 12; break;
		}
		
		return format
			.replaceAll("day", zero(cal.get(Calendar.DAY_OF_MONTH)))
			.replaceAll("month", zero(month))
			.replaceAll("year", cal.get(Calendar.YEAR)+"")
			.replaceAll("hour", zero(cal.get(Calendar.HOUR_OF_DAY)))
			.replaceAll("minute", zero(cal.get(Calendar.MINUTE)))
			.replaceAll("second", zero(cal.get(Calendar.SECOND)));		
	}
	
	public static String presentDate(long unix, String format){
		Calendar p = GregorianCalendar.getInstance(); p.setTimeInMillis(unix);
		
		return presentDate(p, format);
	}
	
	public static String presentDate(long unix){
		Calendar p = GregorianCalendar.getInstance(); p.setTimeInMillis(unix);
		
		return presentDate(p, defaultFormat);
	}
	
	public static void start(Object holder){
		timers.remove(holder);
		timers.put(holder, System.currentTimeMillis());
	}
	
	public static long stop(Object holder){
		Long l = timers.get(holder);
		
		if(l == null)
			return 0;
		
		timers.remove(holder);
		return System.currentTimeMillis() - l;
	}
	
	public static void start(){ start(Thread.currentThread()); }
	public static long stop(){ return stop(Thread.currentThread()); }
	
	private static String zero(int integer){
		if(integer < 10) return "0"+integer;
		return integer+"";
	}
}
