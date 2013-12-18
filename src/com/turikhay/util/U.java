package com.turikhay.util;

import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Random;

import com.turikhay.tlauncher.TLauncher;

public class U {
	public final static int
		DEFAULT_READ_TIMEOUT = 15000,
		DEFAULT_CONNECTION_TIMEOUT = 15000;
	private static String PREFIX;
	
	public static void setPrefix(String prefix){ PREFIX = prefix; }
	public static String getPrefix(){ return PREFIX; }
	
	private static Object lock = new Object();
	public static void linelog(Object what){
		synchronized(lock){
			System.out.print(what);
		}
	}
	public static void log(Object... what){ hlog(PREFIX, what);	}
	public static void plog(Object... what){ hlog(null, what); }
	private static void hlog(String prefix, Object[] append){
		synchronized(lock){
			System.out.println(toLog(prefix, append));
		}		
	}
	public static String toLog(String prefix, Object... append){
		StringBuilder b = new StringBuilder(); boolean first = true;
		
		if(prefix != null){
			b.append(prefix);
			first = false;
		}
		
		if(append != null)
		for(Object e : append){	
			if(e != null)
			if(e.getClass().isArray()){
				if(!first) b.append(" ");
				
				if(e instanceof Object[]) b.append(toLog((Object[]) e));
				else b.append(arrayToLog(e));
				
				continue;
			}
			if(e instanceof Throwable){
				if(!first) b.append("\n");
				b.append(stackTrace((Throwable) e));
				b.append("\n");
				
				continue;
			}
			else {
				if(!first) b.append(" ");
				b.append(e);
			}
			
			if(first) first = false;
		}
		else b.append("null");
		
		return b.toString();
	}
	public static String toLog(Object... append){ return toLog(null, append); }
	public static String arrayToLog(Object e){
		if(!e.getClass().isArray())
			throw new IllegalArgumentException("Given object is not an array!");
		
		StringBuilder b = new StringBuilder(); boolean first = true;
		
		if(e instanceof Object[]) for(Object i : (Object[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof int[]) for(int i : (int[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof boolean[]) for(boolean i : (boolean[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof long[]) for(long i : (long[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof float[]) for(float i : (float[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof double[]) for(double i : (double[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof byte[]) for(byte i : (byte[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof short[]) for(short i : (short[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		else if(e instanceof char[]) for(char i : (char[]) e){ if(!first) b.append(" "); else first = false; b.append(i); }
		
		if(b.length() == 0)
			throw new UnknownError("Unknown array type given.");
		
		return b.toString();
	}
	
	public static short shortRandom(){
		return (short) new Random(System.currentTimeMillis()).nextInt(Short.MAX_VALUE);
	}
	
	public static double doubleRandom(){
		return new Random(System.currentTimeMillis()).nextDouble();
	}
	
	public static int random(int s, int e){
		return new Random(System.currentTimeMillis()).nextInt(e - s) + s;
	}
	
	public static boolean ok(int d){
		return new Random(System.currentTimeMillis()).nextInt(d) == 0;
	}
	
	public static double getAverage(double[] d){
		double a = 0; int k = 0;
		
		for(double curd : d){
			if(curd == 0) continue;
			a += curd; ++k;
		}
		
		if(k == 0) return 0;
		return a / k;
	}
	
	public static double getAverage(double[] d, int max){
		double a = 0; int k = 0;
		
		for(double curd : d){
			a += curd; ++k;
			if(k == max) break;
		}
		
		if(k == 0) return 0;
		return a / k;
	}
	
	public static int getAverage(int[] d){
		int a = 0, k = 0;
		
		for(int curd : d){
			if(curd == 0) continue;
			a += curd; ++k;
		}
		
		if(k == 0) return 0;
		return Math.round(a / k);
	}
	
	public static int getAverage(int[] d, int max){
		int a = 0, k = 0;
		
		for(int curd : d){
			a += curd; ++k;
			if(k == max) break;
		}
		
		if(k == 0) return 0;
		return Math.round(a / k);
	}
	
	public static int getSum(int[] d){
		int a = 0;
		
		for(int curd : d) a += curd;
		return a;
	}
	
	public static double getSum(double[] d){
		double a = 0;
		
		for(double curd : d) a += curd;
		return a;
	}
	
	public static int getMaxMultiply(int i, int max){
		if(i <= max) return 1;
		for(int x=max;x>1;x--)
			if(i % x == 0) return x;
		return (int) Math.ceil(i / max);
	}
	
	public static String r(String string, int max){
		if(string == null) return null;
		
		int len = string.length();
		if(len <= max) return string;
		
		String[] words = string.split(" ");		
		String ret = ""; int remaining = max + 1;
		
		for(int x=0;x<words.length;x++){
			String curword = words[x];
			int curlen = curword.length();
			
			if(curlen < remaining){
				ret += " " + curword;
				remaining -= curlen + 1;
				
				continue;
			}
			
			if(x == 0)
				ret += " " + curword.substring(0, remaining - 1);
			break;
		}
		
		if(ret.length() == 0) return "";
		return ret.substring(1) + "...";
	}
	
	public static String t(String string, int max){
		if(string == null) return null;
		
		int len = string.length();
		if(len <= max) return string;
		
		return string.substring(0, max) + "...";
	}
	
	public static String w(String string, int normal, char newline, boolean rude){
		char[] c = string.toCharArray();
		int len = c.length, remaining = normal;
		
		String ret = "";
		char cur;
		
		for(int x=0;x<len;x++){ --remaining;
			cur = c[x];
			
			if(c[x] == newline)
				remaining = normal;
			
			if(remaining < 1)
				if(cur == ' '){
					remaining = normal;
					ret += newline;
					continue;
				}
			ret += cur;
			
			if(remaining > 0) continue;
			if(!rude) continue;
			
			remaining = normal;
			ret += newline;			
		}
		
		return ret;
	}
	
	public static String w(String string, int max){ return w(string, max, '\n', false); }
	
	public static String setFractional(double d, int fractional){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(fractional);
		
		return nf.format(d).replace(",", ".");
	}
	
	public static String stackTrace(Throwable e){
		if(e == null) return null;
		
		String t = e.toString();
		if(t == null) t = "";
		
		StackTraceElement[] elems = e.getStackTrace();
		boolean found_out = false;
		
		for(int x=0;x<elems.length;x++){
			String elem = elems[x].toString();
			t += "\nat " + elem;
			
			if(!found_out) found_out = elem.startsWith("com.turikhay");
			
			if(x < 5 || !found_out) continue;
			
			int remain = (elems.length - x - 1);
			if(remain != 0) t += "\n" + "... and "+ remain + " more";
			
			break;
		}
		
		Throwable cause = e.getCause();
		if(cause != null)
			t += "\nCaused by: "+stackTrace(cause);
		
		return t;
		
	}
	
	public static long getUsingSpace(){
		return U.getTotalSpace() - U.getFreeSpace();
	}
	
	public static long getFreeSpace(){
		return (Runtime.getRuntime().freeMemory() / (1024*1024));
	}
	
	public static long getTotalSpace(){
		return (Runtime.getRuntime().totalMemory() / (1024*1024));
	}
	
	public static void gc(){		
		log("Starting garbage collector: "+ U.getUsingSpace() +" / "+ getTotalSpace() +" MB");
		System.gc();
		log("Garbage collector completed: "+ U.getUsingSpace() +" / "+ getTotalSpace() +" MB");
	}
	
	public static void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){ e.printStackTrace(); }
	}
	
	public static URL makeURL(String p){
		try{ return new URL(p); }catch(Exception e){
			//U.log("Cannot make URL from string: "+p+".", e);
		}
		
		return null;
	}
	
	public static URI makeURI(URL url){
		try{ return url.toURI(); }catch(Exception e){
			//U.log("Cannot make URI from URL: "+url+".", e);
		}
		
		return null;
	}
	
	public static URI makeURI(String p){
		return U.makeURI(U.makeURL(p));
	}
	
	public static boolean interval(int min, int max, int num, boolean including){
		return (including)? (num >= min && num <= max) : (num > max && num < max);
	}
	
	public static boolean interval(int min, int max, int num){ return interval(min, max, num, true); }
	
	public static long m(){
		return System.currentTimeMillis();
	}
	
	public static long n(){
		return System.nanoTime();
	}
	
	public static int getReadTimeout(){
		return getConnectionTimeout();
		/*TLauncher t = TLauncher.getInstance();
		if(t == null) return DEFAULT_READ_TIMEOUT;
		
		int timeout = t.getSettings().getInteger("timeout.read");
		if(timeout < 1) return DEFAULT_READ_TIMEOUT;
		
		return timeout;*/
	}
	
	public static int getConnectionTimeout(){
		TLauncher t = TLauncher.getInstance();
		if(t == null) return DEFAULT_CONNECTION_TIMEOUT;
		
		int timeout = t.getSettings().getInteger("timeout.connection");
		if(timeout < 1) return DEFAULT_CONNECTION_TIMEOUT;
		
		return timeout;
	}
}
