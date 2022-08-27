package com.oracle.email.util;

public class EncodeUtil {
	private static final String range = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static long decode(String s) {
	    final int B = range.length();
	    long num = 0;
	    for (char ch : s.toCharArray()) {
	        num *= B;
	        num += range.indexOf(ch);
	    }
	    return num;
	}//decode
	
	public static String encode(long num) {
	    final int B = range.length();
	    StringBuilder sb = new StringBuilder();
	    while (num != 0) {
	        sb.append(range.charAt((int) (num % B)));
	        num /= B;
	    }
	    return sb.reverse().toString();
	}//encode
	
	public static void main(String[] args){
		long val = Long.MAX_VALUE;
		System.out.println(val + " => " + EncodeUtil.encode(val));
		
	}
	
}
