package org.insightcentre.coach.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static org.insightcentre.coach.data.ExerciseProgramContract.*;

/**
 * Manages a local database for exercise program data.
 */
public class ExerciseProgramDatabaseHelper extends SQLiteOpenHelper {
//    private static final String LOG_TAG = "ExerciseProgramDatabase";
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "exercise_program.db";

    public ExerciseProgramDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

//        // Create an in-memory database while still testing and developing app
//        super(context, null, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PRESCRIBED_EXERCISES_TABLE = "CREATE TABLE " +
                PrescribedExercisesEntry.TABLE_NAME + " (" +
                PrescribedExercisesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PrescribedExercisesEntry.COLUMN_WEEK + " INTEGER NOT NULL, " +
                PrescribedExercisesEntry.COLUMN_DAY + " INTEGER NOT NULL, " +
                PrescribedExercisesEntry.COLUMN_SESSION + " INTEGER NOT NULL, " +
                PrescribedExercisesEntry.COLUMN_TARGET_LENGTH + " INTEGER NOT NULL, " +
                PrescribedExercisesEntry.COLUMN_TARGET_RPE + " INTEGER NOT NULL);";

//        Log.v(LOG_TAG, SQL_CREATE_PRESCRIBED_EXERCISES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PRESCRIBED_EXERCISES_TABLE);

        final String SQL_CREATE_EXERCISE_CALENDAR_TABLE = "CREATE TABLE " +
                ExerciseCalendarEntry.TABLE_NAME + " (" +
                ExerciseCalendarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ExerciseCalendarEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_SESSION + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_LEVEL + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_TARGET_LENGTH + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_ACTUAL_LENGTH + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_TARGET_RPE + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_ACTUAL_RPE + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_SUCCESS + " INTEGER NOT NULL, " +
                ExerciseCalendarEntry.COLUMN_PRESCRIBED + " INTEGER NOT NULL);";

//        Log.v(LOG_TAG, SQL_CREATE_EXERCISE_CALENDAR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EXERCISE_CALENDAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }
}
