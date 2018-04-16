package org.insightcentre.coach.alarms;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;

import org.insightcentre.coach.R;
import org.insightcentre.coach.Utility;

import java.util.Calendar;
import java.util.TimeZone;

public class DeviceBootAndTimeZoneChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            today.setTimeZone(Utility.timeZoneAtHome());
            Utility.setMidnight(today);
            Utility.adjustForDST(context, today);

            SharedPreferences datesSharedPrefs = context.getSharedPreferences(
                    context.getString(R.string.dates_key), Context.MODE_PRIVATE);
            long startDate = datesSharedPrefs.getLong(
                    context.getString(R.string.start_date), today.getTimeInMillis());
            long endDate = datesSharedPrefs.getLong(
                    context.getString(R.string.end_date), today.getTimeInMillis());

            if (today.getTimeInMillis() > endDate) {
                ComponentName receiver =
                        new ComponentName(context, DeviceBootAndTimeZoneChangeReceiver.class);
                PackageManager packageManager = context.getPackageManager();

                packageManager.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                return;
            }

            Calendar startLocal = Calendar.getInstance(Utility.timeZoneAtHome());
            startLocal.setTimeInMillis(startDate);
            startLocal.set(Calendar.DAY_OF_MONTH, startLocal.get(Calendar.DAY_OF_MONTH));
            startLocal.setTimeZone(TimeZone.getDefault());
            Utility.setMidnight(startLocal);

            Calendar endLocal = Calendar.getInstance(Utility.timeZoneAtHome());
            endLocal.setTimeInMillis(endDate);
            endLocal.set(Calendar.DAY_OF_MONTH, endLocal.get(Calendar.DAY_OF_MONTH));
            endLocal.setTimeZone(TimeZone.getDefault());
            Utility.setMidnight(endLocal);

            Calendar todayLocal = Calendar.getInstance();
            Utility.setMidnight(todayLocal);

            long alarmDate;
            long schedulerAlarmDate;

            if (today.getTimeInMillis() <= startDate) {
                alarmDate = startLocal.getTimeInMillis();
                Calendar secondWeekLocal = Calendar.getInstance();
                secondWeekLocal.setTimeInMillis(startLocal.getTimeInMillis());
                secondWeekLocal.add(Calendar.DAY_OF_MONTH, 7);
                schedulerAlarmDate = secondWeekLocal.getTimeInMillis();
            } else {
                alarmDate = todayLocal.getTimeInMillis();
                int dayOffset =
                        (int) (((today.getTimeInMillis() - startDate) % DateUtils.WEEK_IN_MILLIS)
                                / DateUtils.DAY_IN_MILLIS);
                if (dayOffset == 0) {
                    schedulerAlarmDate = todayLocal.getTimeInMillis();
                } else {
                    Calendar nextWeekLocal = Calendar.getInstance();
                    nextWeekLocal.setTimeInMillis(todayLocal.getTimeInMillis());
                    nextWeekLocal.add(Calendar.DAY_OF_MONTH, 7 - dayOffset);
                    schedulerAlarmDate = nextWeekLocal.getTimeInMillis();
                }
            }

            AlarmScheduler.setMorningAlarm(context, alarmDate);
            AlarmScheduler.setEveningAlarm(context, alarmDate);

            if (schedulerAlarmDate <= endLocal.getTimeInMillis()) {
                AlarmScheduler.setExerciseSchedulerAlarm(context, schedulerAlarmDate);
                AlarmScheduler.setWeeklyAlarm(context, schedulerAlarmDate);
            }
        }
    }
}
