package org.insightcentre.coach.notify;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;

import org.insightcentre.coach.HomeActivity;
import org.insightcentre.coach.R;
import org.insightcentre.coach.Utility;
import org.insightcentre.coach.alarms.AlarmScheduler;
import org.insightcentre.coach.alarms.EveningAlarmReceiver;

import java.util.Calendar;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class EveningNotificationService extends IntentService {
    public EveningNotificationService() {
        super("EveningNotificationService");
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

        if (today.getTimeInMillis() + DateUtils.DAY_IN_MILLIS <= endDate) {
            Calendar tomorrowLocal = Calendar.getInstance();
            Utility.setMidnight(tomorrowLocal);
            tomorrowLocal.add(Calendar.DAY_OF_MONTH, 1);
            // Set an alarm to go off tomorrow evening
            AlarmScheduler.setEveningAlarm(this, tomorrowLocal.getTimeInMillis());
        }

        Cursor prescriptionCursor = getContentResolver().query(buildExerciseCalendarDate(today.getTimeInMillis()),
            new String[]{"COUNT(*)"},
            COLUMN_PRESCRIBED + " = ? AND " + COLUMN_SUCCESS + " = ?",
            new String[]{Integer.toString(SESSION_PRESCRIBED), Integer.toString(SUCCESS_NOT_RECORDED)},
            null);
        boolean sendReminder = false;

        if (prescriptionCursor != null && prescriptionCursor.moveToFirst() && prescriptionCursor.getInt(0) > 0) {
            sendReminder = true;
        }
        if (prescriptionCursor != null) {
            prescriptionCursor.close();
        }

        if (sendReminder) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.reminder))
                    .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.big_view_reminder_message)));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                builder.setContentText(getString(R.string.normal_view_reminder_message));
            } else {
                builder.setContentText(getString(R.string.big_view_reminder_message));
            }

            Intent resultIntent = new Intent(this, HomeActivity.class);
            TaskStackBuilder taskStackBuilder =
                TaskStackBuilder.create(this).addParentStack(HomeActivity.class).addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(2, builder.build());
        }

        if (intent != null) {
            // Release the wake lock provided by the WakefulBroadcastReceiver
            EveningAlarmReceiver.completeWakefulIntent(intent);
        }
    }
}
