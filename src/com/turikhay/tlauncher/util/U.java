package com.turikhay.tlauncher.util;

import java.text.NumberFormat;
import java.util.Random;

import com.turikhay.tlauncher.TLauncher;

public class U {
	
	public static void linelog(Object what){ System.out.print(what); }
	public static void log(Object what){ System.out.print("[TLauncher] "); System.out.println(what); }
	public static void log(Object what0, Object what1){ System.out.print("[TLauncher] "); System.out.print(what0); System.out.println(what1); }
	public static void log(Object what, Throwable e){ log(what); e.printStackTrace(); }
	public static void plog(Object what){ System.out.println(what); }
	
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
	
	public static String setFractional(double d, int fractional){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(fractional);
		
		return nf.format(d).replace(",", ".");
	}
}
