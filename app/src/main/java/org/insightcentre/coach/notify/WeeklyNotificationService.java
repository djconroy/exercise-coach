package org.insightcentre.coach.notify;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;

import org.insightcentre.coach.R;
import org.insightcentre.coach.Utility;
import org.insightcentre.coach.alarms.AlarmScheduler;
import org.insightcentre.coach.alarms.WeeklyAlarmReceiver;

import java.util.Calendar;

public class WeeklyNotificationService extends IntentService {
    public WeeklyNotificationService() {
        super("WeeklyNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(today);
        Utility.adjustForDST(this, today);

        SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
        long endDate = datesSharedPrefs.getLong(getString(R.string.end_date), today.getTimeInMillis());

        if (today.getTimeInMillis() + DateUtils.WEEK_IN_MILLIS <= endDate) {
            Calendar nextWeekLocal = Calendar.getInstance();
            Utility.setMidnight(nextWeekLocal);
            nextWeekLocal.add(Calendar.DAY_OF_MONTH, 7);
            // Set an alarm to notify users of their progress after this week
            AlarmScheduler.setWeeklyAlarm(this, nextWeekLocal.getTimeInMillis());
        }

        SharedPreferences statsSharedPrefs = getSharedPreferences(getString(R.string.stats_key), Context.MODE_PRIVATE);
        float currentSuccessRate = statsSharedPrefs.getFloat(getString(R.string.current_success_rate), 1f);

        SharedPreferences levelSharedPrefs = getSharedPreferences(getString(R.string.level_key), Context.MODE_PRIVATE);
        int previousLevel = levelSharedPrefs.getInt(getString(R.string.previous_level), 0);
        int currentLevel = levelSharedPrefs.getInt(getString(R.string.current_level), 1);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.program_progress))
            .setDefaults(Notification.DEFAULT_ALL); // requires VIBRATE permission

        String bigViewText;

        if (currentSuccessRate >= Utility.TARGET_SUCCESS_RATE) {
            bigViewText = String.format(getString(R.string.big_view_message_on_progress),
                (int) (100 * currentSuccessRate), previousLevel, currentLevel);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigViewText));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                builder.setContentText(String.format(getString(R.string.message_on_progress), currentLevel));
            } else {
                builder.setContentText(bigViewText);
            }
        } else {
            bigViewText = String.format(getString(R.string.big_view_message_on_no_progress),
                (int) (100 * currentSuccessRate), previousLevel);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigViewText));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                builder.setContentText(String.format(getString(R.string.message_on_no_progress), currentLevel));
            } else {
                builder.setContentText(bigViewText);
            }
        }

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(3, builder.build());

        if (intent != null) {
            // Release the wake lock provided by the WakefulBroadcastReceiver
            WeeklyAlarmReceiver.completeWakefulIntent(intent);
        }
    }
}
