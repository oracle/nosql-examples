package com.oracle.email.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isEmpty(String str) {
		boolean flag = false;
		if (str == null || str.trim().length() == 0
				|| "null".equalsIgnoreCase(str.trim())) {
			flag = true;
		}
		return flag;
	}

	public static String formatDate(long date) {
		String dFormat = null;
		Date myDate = new Date(date);

		SimpleDateFormat mdyFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		// Format the date to Strings
		dFormat = mdyFormat.format(myDate);
		return dFormat;
	}// formatDate

	public static String formatString(String source, int max) {
		int len = 0;
		if (StringUtil.isNotEmpty(source)) {
			len = source.length();
			if (len > max)
				source = source.substring(0, max) + "...";
		}// EOF if(StringUtil.isNotEmpty(source)){
		return String.format("%1$-" + max + "s", source);
	}// formatString
	
	

}
