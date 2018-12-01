package com.element54.utils.string;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FilteredStringCleaner implements StringCleaner {
    protected static final char[] ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_".toCharArray();
    protected final int max;
    protected Map<String, String> replaceMap = new HashMap<String, String>();

    public FilteredStringCleaner(final int max) {
        super();
        this.max = max;
        this.replaceMap.put("ä", "a");
        this.replaceMap.put("ö", "o");
        this.replaceMap.put("ü", "u");
        this.replaceMap.put("ß", "s");
        this.replaceMap.put(" ", "_");
        this.replaceMap.put("-", "_");
        this.replaceMap.put("\\(", "_");
        this.replaceMap.put("\\)", "_");
        this.replaceMap.put("\\/", "_");
        this.replaceMap.put("\\.", "_");

        this.replaceMap.put("é", "e");
        this.replaceMap.put("è", "e");
        this.replaceMap.put("ê", "e");
    }

    @Override
    public String purge(String name) {
        name = name.toLowerCase().trim();
        for (final Entry<String, String> e : this.replaceMap.entrySet()) {
            final String k = e.getKey();
            final String v = e.getValue();
            name = name.replaceAll(k, v);
        }

        final StringBuffer buf = new StringBuffer(name);
        for (int i = 0; i < buf.length(); i++) {
            final char current = buf.charAt(i);
            if (!isCharAllowed(current)) {
                buf.deleteCharAt(i);
                i--;
            }
        }

        String str = buf.toString().trim();
        str = str.replaceAll("_+", "_");
        str = specialTrim(str);
        if (this.max <= 0) {
            return str;
        }
        if (str.length() > this.max) {
            str = str.substring(0, this.max);
        }
        str = specialTrim(str);
        return str;
    }

    protected boolean isCharAllowed(final char c) {
        for (int i = 0; i < ALLOWED_CHARS.length; i++) {
            if (ALLOWED_CHARS[i] == c) {
                return true;
            }
        }
        return false;
    }

    protected String specialTrim(String str) {
        while (str.startsWith("_")) {
            str = str.substring(1);
        }
        while (str.endsWith("_")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

}
