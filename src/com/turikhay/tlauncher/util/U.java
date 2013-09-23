package com.turikhay.tlauncher.util;

import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.turikhay.tlauncher.TLauncher;

public class U {
	private final static String PREFIX = "[TLauncher]";
	
	public static void linelog(Object what){ System.out.print(what); }
	public static void log(Object... what){ hlog(PREFIX, what);	}
	public static void plog(Object... what){ hlog(null, what); }
	private static void hlog(String prefix, Object[] append){
		StringBuilder b = new StringBuilder(); boolean first = true;
		
		if(prefix != null){
			b.append(prefix);
			first = false;
		}
		
		for(Object e : append){
			if(e != null)
			if(e instanceof Throwable){
				b.append("\n");
				b.append(stackTrace((Throwable) e));
				b.append("\n");
				
				continue;
			} else
			if(e instanceof Collection){
				Collection<?> col = (Collection<?>) e;
				
				for(Object obj : col){
					b.append("\n");
					b.append(obj);
				}
			} else
			if(e instanceof Map){
				Map<?, ?> col = (Map<?, ?>) e;
				
				for(Entry<?, ?> obj : col.entrySet()){
					b.append("\n");
					b.append(obj.getKey());
					b.append(" : ");
					b.append(obj.getValue());
				}
			}
			
			if(first) first = false;
			else b.append(" "); b.append(e);
		}
		
		System.out.println(b.toString());
	}
	
	private static TLauncher t;	
	public static void setWorkingTo(TLauncher to){ if(t == null) t = to; MinecraftUtil.setWorkingTo(to); }
	
	public static double doubleRandom(){
		return new Random(System.currentTimeMillis()).nextDouble();
	}
	
	public static int random(int s, int e){
		return new Random(System.currentTimeMillis()).nextInt(e - s) + s;
	}
	
	public static boolean ok(int d){
		return new Random(System.currentTimeMillis()).nextInt(d) == 0;
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
		String t = e.toString();
		if(t == null) t = "";
		
		StackTraceElement[] elems = e.getStackTrace();
		
		for(int x=0;x<elems.length;x++){
			t += "\nat " + elems[x].toString();
			
			if(x < 5) continue;
			
			int remain = (elems.length - x - 1);
			if(remain != 0) t += "\n" + "... and "+ remain + " more";
			
			break;
		}
		
		Throwable cause = e.getCause();
		
		if(cause != null){
			t += "\nCaused by: "+cause.toString();
			
			StackTraceElement[] causeelems = cause.getStackTrace();
			
			for(int x=0;x<causeelems.length;x++){
				t += "\nat " + causeelems[x].toString();
				
				if(x < 5) continue;
				
				int remain = (causeelems.length - x - 1);
				if(remain != 0) t += "\n" + "... and "+ remain + " more";
				
				break;
			}
		}
		
		return t;
		
	}
	
	public static long getFreeSpace(){
		return (Runtime.getRuntime().freeMemory() / (1024*1024));
	}
	
	public static long getTotalSpace(){
		return (Runtime.getRuntime().totalMemory() / (1024*1024));
	}
	
	public static void gc(){
		long total = getTotalSpace();
		
		log("Starting garbage collector: "+ U.getFreeSpace() +" / "+ total +" MB");
		System.gc();
		log("Garbage collector completed: "+ U.getFreeSpace() +" / "+ total +" MB");
	}
	
	public static void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){ e.printStackTrace(); }
	}
	
	public static URL makeURL(String p){
		try{ return new URL(p); }catch(Exception e){
			U.log("Cannot make URL from string: "+p+". Check out language file", e);
		}
		
		return null;
	}
	
	public static URI makeURI(URL url){
		try{ return url.toURI(); }catch(Exception e){
			U.log("Cannot make URI from URL: "+url+". Check out language file", e);
		}
		
		return null;
	}
	
	public static long m(){
		return System.currentTimeMillis();
	}
	
	public static long n(){
		return System.nanoTime();
	}
}
