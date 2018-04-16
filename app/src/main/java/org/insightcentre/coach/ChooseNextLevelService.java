package org.insightcentre.coach;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.format.DateUtils;

import org.insightcentre.coach.alarms.AlarmScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class ChooseNextLevelService extends IntentService {
    public ChooseNextLevelService() {
        super("ChooseNextLevelService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(today);
        Utility.adjustForDST(this, today);
        long lastWeekInMillis = today.getTimeInMillis() - DateUtils.WEEK_IN_MILLIS;

        SharedPreferences datesSharedPrefs =
                getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
        long endDate =
                datesSharedPrefs.getLong(getString(R.string.end_date), today.getTimeInMillis());

        if (today.getTimeInMillis() + DateUtils.WEEK_IN_MILLIS <= endDate) {
            Calendar nextWeekLocal = Calendar.getInstance();
            Utility.setMidnight(nextWeekLocal);
            nextWeekLocal.add(Calendar.DAY_OF_MONTH, 7);
            // Set an alarm to schedule next week's exercises
            AlarmScheduler.setExerciseSchedulerAlarm(this, nextWeekLocal.getTimeInMillis());
        }

        // Check whether the database contains prescribed exercise data for today or any day in the
        // future. If so, then exercises have already been scheduled for the current week, so we
        // should return from this service so they are not scheduled again. I'm guessing this
        // situation could occur if the device is rebooted on the day those exercises were
        // scheduled and this service is started again.
        Cursor futurePrescriptionsCursor = getContentResolver().query(
                CONTENT_URI,
                new String[]{"COUNT(*)"},
                COLUMN_PRESCRIBED + " = ? AND " + COLUMN_DATE + " >= ?",
                new String[]{Integer.toString(SESSION_PRESCRIBED),
                        Long.toString(today.getTimeInMillis())},
                null);
        if (futurePrescriptionsCursor != null && futurePrescriptionsCursor.moveToFirst() &&
                futurePrescriptionsCursor.getInt(0) > 0) {
            futurePrescriptionsCursor.close();
            return;
        }
        if (futurePrescriptionsCursor != null) {
            futurePrescriptionsCursor.close();
        }

        Cursor prescriptionsCursor = getContentResolver().query(
                CONTENT_URI,
                new String[]{COLUMN_TARGET_LENGTH, COLUMN_SUCCESS},
                COLUMN_PRESCRIBED + " = ? AND " + COLUMN_DATE + " >= ? AND " +
                        COLUMN_DATE + " < ?",
                new String[]{Integer.toString(SESSION_PRESCRIBED),
                        Long.toString(lastWeekInMillis),
                        Long.toString(today.getTimeInMillis())},
                COLUMN_TARGET_LENGTH + " DESC");

        final int COL_PRE_TARGET_LEN = 0;
        final int COL_PRE_SUCCESS = 1;

        Cursor extraSessionsCursor = getContentResolver().query(
                CONTENT_URI,
                new String[]{COLUMN_ACTUAL_LENGTH},
                COLUMN_PRESCRIBED + " = ? AND " + COLUMN_DATE + " >= ? AND " +
                        COLUMN_DATE + " < ?",
                new String[]{Integer.toString(SESSION_NOT_PRESCRIBED),
                        Long.toString(lastWeekInMillis),
                        Long.toString(today.getTimeInMillis())},
                COLUMN_ACTUAL_LENGTH + " DESC");

        final int COL_EXTRA_LEN = 0;

        float successRate = 0f;

        if (prescriptionsCursor != null && extraSessionsCursor != null) {
            int numTargetsMet = 0;
            List<Integer> missedTargetLengths = new ArrayList<>();

            while (prescriptionsCursor.moveToNext()) {
                if (prescriptionsCursor.getInt(COL_PRE_SUCCESS) == SESSION_COMPLETED) {
                    numTargetsMet++;
                } else {
                    missedTargetLengths.add(prescriptionsCursor.getInt(COL_PRE_TARGET_LEN));
                }
            }

            if (numTargetsMet < prescriptionsCursor.getCount()) {
                int missedTargetIndex = 0;
                while (extraSessionsCursor.moveToNext() &&
                        missedTargetIndex < missedTargetLengths.size()) {
                    // Search for the next missed target not longer than the current extra session
                    while (missedTargetIndex < missedTargetLengths.size() &&
                            missedTargetLengths.get(missedTargetIndex) >
                                    extraSessionsCursor.getInt(COL_EXTRA_LEN)) {
                        missedTargetIndex++;
                    }
                    if (missedTargetIndex < missedTargetLengths.size()) {
                        // Found a missed target not longer than the current extra session
                        numTargetsMet++;
                        missedTargetIndex++;
                    }
                }
            }

            successRate = numTargetsMet / (float) prescriptionsCursor.getCount();
        }
        if (prescriptionsCursor != null) {
            prescriptionsCursor.close();
        }
        if (extraSessionsCursor != null) {
            extraSessionsCursor.close();
        }

        SharedPreferences statisticsSharedPrefs =
                getSharedPreferences(getString(R.string.statistics_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = statisticsSharedPrefs.edit();
        editor.putFloat(getString(R.string.current_success_rate), successRate);
        editor.commit();

        SharedPreferences levelSharedPrefs =
                getSharedPreferences(getString(R.string.level_key), Context.MODE_PRIVATE);
        int previousLevel = levelSharedPrefs.getInt(getString(R.string.previous_level), 0);
        int currentLevel = levelSharedPrefs.getInt(getString(R.string.current_level), 1);
        int nextLevel;

        if (successRate >= Utility.TARGET_SUCCESS_RATE) {
            nextLevel = currentLevel + 1;
        } else {
            if (currentLevel == previousLevel) {
                nextLevel = Math.max(currentLevel - 1, 1);
            } else {
                nextLevel = currentLevel;
            }
        }

        Intent scheduleExercisesIntent = new Intent(this, ScheduleExercisesService.class)
                .putExtra(ScheduleExercisesService.EXTRA_NEXT_LEVEL, nextLevel)
                .putExtra(ScheduleExercisesService.EXTRA_START_DATE, today.getTimeInMillis())
                .putExtra(ScheduleExercisesService.EXTRA_CHOOSE_LEVEL_INTENT, intent);
        startService(scheduleExercisesIntent);
    }
}
