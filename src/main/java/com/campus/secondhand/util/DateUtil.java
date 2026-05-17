package com.campus.secondhand.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtil() {
    }

    public static String nowText() {
        return format(new Date());
    }

    public static String format(Date date) {
        return new SimpleDateFormat(DEFAULT_PATTERN).format(date);
    }
}
