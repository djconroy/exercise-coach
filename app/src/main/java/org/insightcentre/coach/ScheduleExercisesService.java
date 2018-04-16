package org.insightcentre.coach;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.format.DateUtils;

import org.insightcentre.coach.alarms.AlarmScheduler;
import org.insightcentre.coach.alarms.ExerciseSchedulerAlarmReceiver;

import java.util.Calendar;
import java.util.TimeZone;

import static org.insightcentre.coach.data.ExerciseProgramContract.*;
import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class ScheduleExercisesService extends IntentService {
    public static final String EXTRA_START_DATE = "start date";
    public static final String EXTRA_NEXT_LEVEL = "next level";
    public static final String EXTRA_CHOOSE_LEVEL_INTENT = "choose level intent";

    public ScheduleExercisesService() {
        super("ScheduleExercisesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(today);
        Utility.adjustForDST(this, today);
        long startDate = intent.getLongExtra(EXTRA_START_DATE, today.getTimeInMillis());

        SharedPreferences levelSharedPrefs = getSharedPreferences(
                getString(R.string.level_key), Context.MODE_PRIVATE);
        int previousLevel = levelSharedPrefs.getInt(getString(R.string.current_level), 0);
        int currentLevel = intent.getIntExtra(EXTRA_NEXT_LEVEL, previousLevel + 1);

        SharedPreferences.Editor editor = levelSharedPrefs.edit();
        editor.putInt(getString(R.string.previous_level), previousLevel);
        editor.putInt(getString(R.string.current_level), currentLevel);
        editor.commit();

        Cursor cursor = getContentResolver().query(
                PrescribedExercisesEntry.buildPrescribedExercisesWeek(currentLevel),
                new String[]{PrescribedExercisesEntry.COLUMN_DAY,
                        PrescribedExercisesEntry.COLUMN_SESSION,
                        PrescribedExercisesEntry.COLUMN_TARGET_LENGTH,
                        PrescribedExercisesEntry.COLUMN_TARGET_RPE},
                null, null,
                PrescribedExercisesEntry.COLUMN_DAY + " ASC, " +
                        PrescribedExercisesEntry.COLUMN_SESSION + " ASC");
        final int COL_DAY = 0;
        final int COL_SESSION = 1;
        final int COL_TARGET_LEN = 2;
        final int COL_TARGET_RPE = 3;

        if (cursor == null) {
            return;
        }

        int numSessions = cursor.getCount();
        ContentValues[] contentValuesArray = new ContentValues[numSessions];
        for (int i = 0; cursor.moveToNext(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DATE, startDate +
                    (cursor.getInt(COL_DAY) - 1) * DateUtils.DAY_IN_MILLIS);
            contentValues.put(COLUMN_SESSION, cursor.getInt(COL_SESSION));
            contentValues.put(COLUMN_LEVEL, currentLevel);
            contentValues.put(COLUMN_TARGET_LENGTH, cursor.getInt(COL_TARGET_LEN));
            contentValues.put(COLUMN_ACTUAL_LENGTH, 0);
            contentValues.put(COLUMN_TARGET_RPE, cursor.getInt(COL_TARGET_RPE));
            contentValues.put(COLUMN_ACTUAL_RPE, 0);
            contentValues.put(COLUMN_TYPE, TYPE_NOT_RECORDED);
            contentValues.put(COLUMN_SUCCESS, SUCCESS_NOT_RECORDED);
            contentValues.put(COLUMN_PRESCRIBED, SESSION_PRESCRIBED);
            contentValuesArray[i] = contentValues;
        }
        cursor.close();

        getContentResolver().bulkInsert(CONTENT_URI, contentValuesArray);

        if (previousLevel == 0) { // The first week's exercises were just scheduled
            Calendar calendar = Calendar.getInstance(Utility.timeZoneAtHome());
            calendar.setTimeInMillis(startDate);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
            calendar.setTimeZone(TimeZone.getDefault());
            Utility.setMidnight(calendar);

            AlarmScheduler.setMorningAlarm(this, calendar.getTimeInMillis());
            AlarmScheduler.setEveningAlarm(this, calendar.getTimeInMillis());

            calendar.add(Calendar.DAY_OF_MONTH, 7);

            // Set an alarm to schedule exercises for the second week
            AlarmScheduler.setExerciseSchedulerAlarm(this, calendar.getTimeInMillis());
            // Set an alarm to notify users of their progress after the first week
            AlarmScheduler.setWeeklyAlarm(this, calendar.getTimeInMillis());
        } else { // A subsequent week's exercises were just scheduled
            Intent chooseNextLevelIntent = intent.getParcelableExtra(EXTRA_CHOOSE_LEVEL_INTENT);
            if (chooseNextLevelIntent != null) {
                // Release the wake lock provided by the WakefulBroadcastReceiver
                ExerciseSchedulerAlarmReceiver.completeWakefulIntent(chooseNextLevelIntent);
            }
        }
    }
}
