package com.element54.utils.string;

public class StringUtils {
	public static boolean isEmpty(final String s) {
		if (s == null) {
			return true;
		}
		return s.length() == 0;
	}
}
