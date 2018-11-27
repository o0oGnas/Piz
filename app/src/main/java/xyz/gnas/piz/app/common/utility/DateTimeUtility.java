package xyz.gnas.piz.app.common.utility;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtility {
    /**
     * Convert LocalDateTime to Calendar.
     *
     * @param ldt the LocalDateTime
     * @return the Calendar
     */
    public static Calendar convertLocalDateTimeToCalendar(LocalDateTime ldt) {
        Calendar c = Calendar.getInstance();
        Date dt = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        c.setTime(dt);
        return c;
    }

    /**
     * Convert Calendar to LocalDateTime.
     *
     * @param c the Calendar
     * @return the LocalDateTime
     */
    public static LocalDateTime convertCalendarToLocalDateTime(Calendar c) {
        return LocalDateTime.ofInstant(c.getTime().toInstant(), ZoneId.systemDefault());
    }
}
