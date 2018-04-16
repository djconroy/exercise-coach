package org.insightcentre.coach.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the database, URIs for the data in the database, and methods
 * to extract information from the URIs.
 */
public class ExerciseProgramContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "org.insightcentre.coach";

    // Use CONTENT_AUTHORITY to create the base of all URI's which the app will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRESCRIBED_EXERCISES = "prescribed_exercises";
    public static final String PATH_EXERCISE_CALENDAR = "exercise_calendar";

    /* Inner class that defines the table contents of the prescribed exercises table */
    public static final class PrescribedExercisesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRESCRIBED_EXERCISES).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_PRESCRIBED_EXERCISES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_PRESCRIBED_EXERCISES;

        public static final String TABLE_NAME = "prescribed_exercises";

        // Week of the program is stored as an int
        public static final String COLUMN_WEEK = "week";
        // Day of the week is stored as an int
        public static final String COLUMN_DAY = "day";
        // Session number is stored as in int
        public static final String COLUMN_SESSION = "session";
        // Target session length in seconds is stored as an int
        public static final String COLUMN_TARGET_LENGTH = "target_length";
        // Target RPE for a session is stored as an int
        public static final String COLUMN_TARGET_RPE = "target_rpe";

        public static Uri buildPrescribedExerciseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPrescribedExercisesWeek(int week) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(week)).build();
        }

        public static Uri buildPrescribedExercisesWeekWithDay(int week, int day) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(week))
                    .appendPath(Integer.toString(day)).build();
        }

        public static int getWeekFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static int getDayFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }
    }

    /* Inner class that defines the table contents of the exercise calendar table */
    public static final class ExerciseCalendarEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXERCISE_CALENDAR).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_EXERCISE_CALENDAR;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_EXERCISE_CALENDAR;

        public static final String TABLE_NAME = "exercise_calendar";

        // Date in milliseconds since the Epoch is stored as an int
        public static final String COLUMN_DATE = "date";
        // Session number is stored as an int
        public static final String COLUMN_SESSION = "session";
        // Program level is stored as an int
        public static final String COLUMN_LEVEL = "level";
        // Target session length in seconds is stored as an int
        public static final String COLUMN_TARGET_LENGTH = "target_length";
        // Actual session length in seconds is stored as an int
        public static final String COLUMN_ACTUAL_LENGTH = "actual_length";
        // Target RPE for a session is stored as an int
        public static final String COLUMN_TARGET_RPE = "target_rpe";
        // Actual RPE for a session is stored as an int
        public static final String COLUMN_ACTUAL_RPE = "actual_rpe";

        // Exercise type (a walk or step ups) is stored as an int
        public static final String COLUMN_TYPE = "type";
        public static final int STEP_UPS = 0;
        public static final int WALK = 1;
        public static final int TYPE_NOT_RECORDED = 100;

        // Success level (completed, started, or failed) is stored as an int
        public static final String COLUMN_SUCCESS = "success";
        public static final int SESSION_FAILED = 0;
        public static final int SESSION_STARTED = 1;
        public static final int SESSION_COMPLETED = 2;
        public static final int SUCCESS_NOT_RECORDED = 100;

        // Whether a session is prescribed is stored as an int
        public static final String COLUMN_PRESCRIBED = "prescribed";
        public static final int SESSION_NOT_PRESCRIBED = 0;
        public static final int SESSION_PRESCRIBED = 1;

        public static Uri buildExerciseSessionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildExerciseCalendarDate(long date) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(date)).build();
        }

        public static Uri buildExerciseCalendarDateWithPrescribedAndSession(
                long date, int prescribed, int session) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(date))
                    .appendPath(Integer.toString(prescribed))
                    .appendPath(Integer.toString(session)).build();
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static int getPrescribedFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static int getSessionFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(3));
        }
    }
}
