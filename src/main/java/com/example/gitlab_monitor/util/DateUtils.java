package com.example.gitlab_monitor.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String isoToCommitDate(String ts) {
        try {
            OffsetDateTime dt = OffsetDateTime.parse(ts);
            return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
        } catch (Exception e) {
            return ts;
        }
    }
}