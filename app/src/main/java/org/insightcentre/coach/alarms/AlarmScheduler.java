package org.insightcentre.coach.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.insightcentre.coach.Utility;

public class AlarmScheduler {
    private static final String LOG_TAG = AlarmScheduler.class.getSimpleName();

    public static void setMorningAlarm(Context context, long alarmDate) {
        long morningAlarmTime = alarmDate + Utility.NINE_HOURS_IN_MILLIS;

        Intent morningAlarmIntent = new Intent(context, MorningAlarmReceiver.class);
        PendingIntent morningAlarmPendingIntent =
            PendingIntent.getBroadcast(context, 0, morningAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set morning alarm to notify users of their scheduled exercises
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Wakes up the device in Doze Mode
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, morningAlarmTime, morningAlarmPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Wakes up the device in Idle Mode
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, morningAlarmTime, morningAlarmPendingIntent);
        } else {
            // Old APIs
            alarmManager.set(AlarmManager.RTC_WAKEUP, morningAlarmTime, morningAlarmPendingIntent);
        }
//        Log.v(LOG_TAG, "Set morning alarm to notify users of their scheduled exercises");
    }

    public static void setEveningAlarm(Context context, long alarmDate) {
        long eveningAlarmTime = alarmDate + Utility.TWENTY_AND_A_HALF_HOURS_IN_MILLIS;

        Intent eveningAlarmIntent = new Intent(context, EveningAlarmReceiver.class);
        PendingIntent eveningAlarmPendingIntent =
            PendingIntent.getBroadcast(context, 0, eveningAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set evening alarm to remind users to record their exercise sessions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Wakes up the device in Doze Mode
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eveningAlarmTime, eveningAlarmPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Wakes up the device in Idle Mode
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, eveningAlarmTime, eveningAlarmPendingIntent);
        } else {
            // Old APIs
            alarmManager.set(AlarmManager.RTC_WAKEUP, eveningAlarmTime, eveningAlarmPendingIntent);
        }
//        Log.v(LOG_TAG, "Set evening alarm to remind users to record their exercise sessions");
    }

    public static void setExerciseSchedulerAlarm(Context context, long alarmDate) {
        Intent exerciseSchedulerAlarmIntent = new Intent(context, ExerciseSchedulerAlarmReceiver.class);
        PendingIntent exerciseSchedulerAlarmPendingIntent =
            PendingIntent.getBroadcast(context, 0, exerciseSchedulerAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set alarm to schedule exercises for the week beginning on alarmDate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Wakes up the device in Doze Mode
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDate, exerciseSchedulerAlarmPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Wakes up the device in Idle Mode
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmDate, exerciseSchedulerAlarmPendingIntent);
        } else {
            // Old APIs
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDate, exerciseSchedulerAlarmPendingIntent);
        }
//        Log.v(LOG_TAG, "Set alarm to schedule exercises for the week beginning on " + Utility.getDateString(alarmDate));
    }

    public static void setWeeklyAlarm(Context context, long alarmDate) {
        long weeklyAlarmTime = alarmDate + Utility.EIGHT_AND_A_HALF_HOURS_IN_MILLIS;

        Intent weeklyAlarmIntent = new Intent(context, WeeklyAlarmReceiver.class);
        PendingIntent weeklyAlarmPendingIntent =
            PendingIntent.getBroadcast(context, 0, weeklyAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set weekly alarm to notify users of their progress in the program
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Wakes up the device in Doze Mode
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyAlarmTime, weeklyAlarmPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Wakes up the device in Idle Mode
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, weeklyAlarmTime, weeklyAlarmPendingIntent);
        } else {
            // Old APIs
            alarmManager.set(AlarmManager.RTC_WAKEUP, weeklyAlarmTime, weeklyAlarmPendingIntent);
        }
//        Log.v(LOG_TAG, "Set weekly alarm to notify users of their progress in the program");
    }
}
