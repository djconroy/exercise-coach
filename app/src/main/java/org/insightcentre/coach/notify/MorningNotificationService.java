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
import org.insightcentre.coach.alarms.MorningAlarmReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class MorningNotificationService extends IntentService {
    public MorningNotificationService() {
        super("MorningNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        calendar.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(calendar);
        Utility.adjustForDST(this, calendar);
        long todayInMillis = calendar.getTimeInMillis();

        SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
        long endDate = datesSharedPrefs.getLong(getString(R.string.end_date), todayInMillis);

        if (todayInMillis + DateUtils.DAY_IN_MILLIS <= endDate) {
            Calendar tomorrowLocal = Calendar.getInstance();
            Utility.setMidnight(tomorrowLocal);
            tomorrowLocal.add(Calendar.DAY_OF_MONTH, 1);
            // Set an alarm to go off tomorrow morning
            AlarmScheduler.setMorningAlarm(this, tomorrowLocal.getTimeInMillis());
        }

        Cursor prescriptionCursor = getContentResolver().query(buildExerciseCalendarDate(todayInMillis),
            new String[]{COLUMN_TARGET_LENGTH, COLUMN_TARGET_RPE},
            COLUMN_PRESCRIBED + " = ?",
            new String[]{Integer.toString(SESSION_PRESCRIBED)},
            null);

        final int COL_PRE_TARGET_LEN = 0;
        final int COL_PRE_TARGET_RPE = 1;

        // Do nothing if prescriptionCursor is null
        if (prescriptionCursor == null) {
            return;
        }
        // Do nothing if there are no prescribed exercises for today
        if (prescriptionCursor.getCount() == 0) {
            prescriptionCursor.close();
            return;
        }

        long startDate = datesSharedPrefs.getLong(getString(R.string.start_date), todayInMillis);
        boolean isFirstPrescription = false;
        Cursor lastPrescriptionCursor = null;

        // Determine whether the first prescription is today.
        // If it's not, get the last prescription.
        if (startDate == calendar.getTimeInMillis()) {
            isFirstPrescription = true;
        } else {
            do {
                calendar.setTimeInMillis(calendar.getTimeInMillis() - DateUtils.DAY_IN_MILLIS);
                lastPrescriptionCursor = getContentResolver().query(buildExerciseCalendarDate(calendar.getTimeInMillis()),
                    new String[]{COLUMN_TARGET_LENGTH, COLUMN_SUCCESS},
                    COLUMN_PRESCRIBED + " = ?",
                    new String[]{Integer.toString(SESSION_PRESCRIBED)},
                    COLUMN_TARGET_LENGTH + " DESC");
                // If the do-while loop will iterate one more time, close the cursor opened in this iteration
                // to prevent memory leaks
                if (calendar.getTimeInMillis() > startDate && lastPrescriptionCursor != null
                        && lastPrescriptionCursor.getCount() == 0) {
                    lastPrescriptionCursor.close();
                }
            } while (calendar.getTimeInMillis() > startDate
                     && (lastPrescriptionCursor == null || lastPrescriptionCursor.getCount() == 0));

            if (calendar.getTimeInMillis() == startDate
                    && (lastPrescriptionCursor == null || lastPrescriptionCursor.getCount() == 0)) {
                isFirstPrescription = true;
            }
            // Treat the case when lastPrescriptionCursor is null the same as the case when it's the first prescription
            if (lastPrescriptionCursor == null) {
                isFirstPrescription = true;
            }
        }

        final int COL_LAST_TARGET_LEN = 0;
        final int COL_LAST_SUCCESS = 1;

        // Start building notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.exercise_prescription))
            .setDefaults(Notification.DEFAULT_ALL); // requires VIBRATE permission

        // Build list of prescribed exercises
        StringBuilder stringBuilder = new StringBuilder();
        for (int num = 1; prescriptionCursor.moveToNext(); num++) {
            stringBuilder.append(' ').append(' ').append(' ').append(num).append('.').append(' ')
                .append(' ').append(prescriptionCursor.getInt(COL_PRE_TARGET_LEN) / 60)
                .append(' ').append(getString(R.string.mins)).append(',').append(' ')
                .append(getString(R.string.rpe)).append(' ')
                .append(prescriptionCursor.getInt(COL_PRE_TARGET_RPE)).append('\n');
        }
        // Delete last newline character
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String prescription = stringBuilder.toString();

        prescriptionCursor.close();

        String bigViewMessage;

        if (isFirstPrescription) {
            bigViewMessage = getString(R.string.first_prescription_message);
        } else {
            int numCompleted = 0;
            int numStarted = 0;
            List<Integer> missedTargetLengths = new ArrayList<>();

            // Determine how many sessions of the last prescription were started or completed.
            // Also keep track of the lengths of missed sessions.
            while (lastPrescriptionCursor.moveToNext()) {
                switch (lastPrescriptionCursor.getInt(COL_LAST_SUCCESS)) {
                    case SESSION_COMPLETED:
                        numCompleted++;
                        break;
                    case SESSION_STARTED:
                        numStarted++;
                        missedTargetLengths.add(lastPrescriptionCursor.getInt(COL_LAST_TARGET_LEN));
                        break;
                    case SESSION_FAILED:
                        missedTargetLengths.add(lastPrescriptionCursor.getInt(COL_LAST_TARGET_LEN));
                        break;
                    case SUCCESS_NOT_RECORDED:
                        missedTargetLengths.add(lastPrescriptionCursor.getInt(COL_LAST_TARGET_LEN));
                        break;
                }
            }

            if (numCompleted == lastPrescriptionCursor.getCount()) {
                bigViewMessage = getString(R.string.message_on_complete_success);
            } else {
                Cursor extraSessionsCursor = getContentResolver().query(CONTENT_URI,
                    new String[]{COLUMN_ACTUAL_LENGTH},
                    COLUMN_PRESCRIBED + " = ? AND " + COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " < ?",
                    new String[]{Integer.toString(SESSION_NOT_PRESCRIBED),
                                 Long.toString(calendar.getTimeInMillis()),
                                 Long.toString(todayInMillis)},
                    COLUMN_ACTUAL_LENGTH + " DESC");

                final int COL_EXTRA_LEN = 0;

                if (extraSessionsCursor != null) {
                    int missedTargetIndex = 0;
                    while (extraSessionsCursor.moveToNext() && missedTargetIndex < missedTargetLengths.size()) {
                        // Search for the next missed target not longer than the current extra session
                        while (missedTargetIndex < missedTargetLengths.size()
                               && missedTargetLengths.get(missedTargetIndex) > extraSessionsCursor.getInt(COL_EXTRA_LEN)) {
                            missedTargetIndex++;
                        }
                        if (missedTargetIndex < missedTargetLengths.size()) {
                            // Found a missed target not longer than the current extra session
                            numCompleted++;
                            missedTargetIndex++;
                        }
                    }
                }

                if (numCompleted == lastPrescriptionCursor.getCount()) {
                    bigViewMessage = getString(R.string.message_on_complete_success);
                } else if (numCompleted > 0) {
                    bigViewMessage = getString(R.string.message_on_partial_success);
                } else if ((extraSessionsCursor != null && extraSessionsCursor.getCount() > 0) || numStarted > 0) {
                    bigViewMessage = getString(R.string.message_on_attempt);
                } else {
                    bigViewMessage = getString(R.string.message_on_failure);
                }

                if (extraSessionsCursor != null) {
                    extraSessionsCursor.close();
                }
            }
        }
        if (lastPrescriptionCursor != null) {
            lastPrescriptionCursor.close();
        }

        String bigViewText = bigViewMessage + "\n" + prescription;
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigViewText));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            builder.setContentText(getString(R.string.normal_view_morning_notification_text));
        } else {
            builder.setContentText(bigViewText);
        }

        Intent resultIntent = new Intent(this, HomeActivity.class);
        TaskStackBuilder taskStackBuilder =
            TaskStackBuilder.create(this).addParentStack(HomeActivity.class).addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());

        if (intent != null) {
            // Release the wake lock provided by the WakefulBroadcastReceiver
            MorningAlarmReceiver.completeWakefulIntent(intent);
        }
    }
}
