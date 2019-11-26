package org.insightcentre.coach;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Utility {
    public static final long EIGHT_AND_A_HALF_HOURS_IN_MILLIS = 17 * DateUtils.HOUR_IN_MILLIS / 2;
    public static final long NINE_HOURS_IN_MILLIS = 9 * DateUtils.HOUR_IN_MILLIS;
    public static final long TWENTY_AND_A_HALF_HOURS_IN_MILLIS = 41 * DateUtils.HOUR_IN_MILLIS / 2;
    public static final long PROGRAM_LENGTH_IN_MILLIS = 16 * DateUtils.WEEK_IN_MILLIS;

    public static final float TARGET_SUCCESS_RATE = 0.75f;

    public static final int DEFAULT_RPE = 6;
    public static final int RPE_OFFSET = 6;

    public static void setMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static TimeZone timeZoneAtHome() {
//        return TimeZone.getTimeZone("US/Hawaii");
//        return TimeZone.getTimeZone("Japan");
        return TimeZone.getTimeZone("Eire");
    }

    public static boolean inDaylightTime(Calendar calendar) {
        return calendar.getTimeZone().inDaylightTime(calendar.getTime());
    }

    public static void adjustForDST(Context context, Calendar calendar) {
        SharedPreferences datesSharedPrefs =
            context.getSharedPreferences(context.getString(R.string.dates_key), Context.MODE_PRIVATE);
        boolean startDateInDaylightTime = datesSharedPrefs.getBoolean(context.getString(R.string.start_date_in_dst), false);
        boolean inDaylightTime = inDaylightTime(calendar);

        if (startDateInDaylightTime && !inDaylightTime) {
            calendar.add(Calendar.MILLISECOND, -calendar.getTimeZone().getDSTSavings());
        } else if (!startDateInDaylightTime && inDaylightTime) {
            calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings());
        }
    }

    public static String getDateString(long dateInMillis) {
        Calendar date = Calendar.getInstance(timeZoneAtHome());
        date.setTimeInMillis(dateInMillis);
        int hourOfDay = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        int second = date.get(Calendar.SECOND);
        int millisecond = date.get(Calendar.MILLISECOND);

        if (hourOfDay != 0 || minute != 0 || second != 0 || millisecond != 0) {
            if (inDaylightTime(date)) {
                date.add(Calendar.MILLISECOND, -timeZoneAtHome().getDSTSavings());
            } else {
                date.add(Calendar.MILLISECOND, timeZoneAtHome().getDSTSavings());
            }
        }

        try {
            SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
            simpleDateFormat.applyPattern("dd.MM.yy");
            simpleDateFormat.setTimeZone(timeZoneAtHome());
            return simpleDateFormat.format(date.getTimeInMillis());
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        dateFormat.setTimeZone(timeZoneAtHome());
        return dateFormat.format(date.getTimeInMillis());
    }

    public static String getFriendlyDayString(Context context, long dateInMillis) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(timeZoneAtHome());
        setMidnight(today);
        adjustForDST(context, today);
        long yesterday = today.getTimeInMillis() - DateUtils.DAY_IN_MILLIS;
        long tomorrow = today.getTimeInMillis() + DateUtils.DAY_IN_MILLIS;

        if (dateInMillis == today.getTimeInMillis()) {
            return context.getString(R.string.today);
        } else if (dateInMillis == yesterday) {
            return context.getString(R.string.yesterday);
        } else if (dateInMillis == tomorrow) {
            return context.getString(R.string.tomorrow);
        } else {
            Calendar date = Calendar.getInstance(timeZoneAtHome());
            date.setTimeInMillis(dateInMillis);
            int hourOfDay = date.get(Calendar.HOUR_OF_DAY);
            int minute = date.get(Calendar.MINUTE);
            int second = date.get(Calendar.SECOND);
            int millisecond = date.get(Calendar.MILLISECOND);

            if (hourOfDay != 0 || minute != 0 || second != 0 || millisecond != 0) {
                if (inDaylightTime(date)) {
                    date.add(Calendar.MILLISECOND, -timeZoneAtHome().getDSTSavings());
                } else {
                    date.add(Calendar.MILLISECOND, timeZoneAtHome().getDSTSavings());
                }
            }

            try {
                SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getDateInstance();

                if (tomorrow < dateInMillis && dateInMillis < today.getTimeInMillis() + DateUtils.WEEK_IN_MILLIS) {
                    simpleDateFormat.applyPattern("EEEE");
                } else {
                    simpleDateFormat.applyPattern("EEE, d MMM");
                }
                simpleDateFormat.setTimeZone(timeZoneAtHome());
                return simpleDateFormat.format(date.getTimeInMillis());
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            dateFormat.setTimeZone(timeZoneAtHome());
            return dateFormat.format(date.getTimeInMillis());
        }
    }

    // So Utility can't be instantiated
    private Utility() {}
}
