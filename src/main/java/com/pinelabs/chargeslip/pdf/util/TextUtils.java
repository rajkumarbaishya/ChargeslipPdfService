package com.pinelabs.chargeslip.pdf.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for text processing and normalization
 */
@UtilityClass
public class TextUtils {

    /**
     * Normalize text by replacing non-printable ASCII characters with spaces
     */
    public static String normalize(String s) {
        if (s == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append((c >= 32 && c <= 126) ? c : ' ');
        }
        return sb.toString();
    }

    /**
     * Return empty string if input is null
     */
    public static String safe(String s) {
        return s == null ? "" : s;
    }
}