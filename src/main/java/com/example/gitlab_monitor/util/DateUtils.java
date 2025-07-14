package com.example.gitlab_monitor.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtils {
    public static String isoToCommitDate(String ts) {
        try {
            OffsetDateTime dt = OffsetDateTime.parse(ts);
            return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
        } catch (Exception e) {
            return ts;
        }
    }

    public static List<Date> getDateRange(Date since, Date until) {
        List<Date> dates = new ArrayList<>();
        
        LocalDate start = since.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = until.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        while (!start.isAfter(end)) {
            Instant instant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
            dates.add(Date.from(instant));
            start = start.plusYears(1);
        }
        dates.add(until);
        return dates;
    }
}