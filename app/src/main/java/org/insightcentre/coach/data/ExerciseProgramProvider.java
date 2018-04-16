package org.insightcentre.coach.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import static org.insightcentre.coach.data.ExerciseProgramContract.*;

public class ExerciseProgramProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ExerciseProgramDatabaseHelper mOpenHelper;

    static final int PRESCRIBED_EXERCISES = 100;
    static final int PRESCRIBED_EXERCISES_WITH_WEEK = 101;
    static final int PRESCRIBED_EXERCISES_WITH_WEEK_AND_DAY = 102;
    static final int EXERCISE_CALENDAR = 200;
    static final int EXERCISE_CALENDAR_WITH_DATE = 201;
    static final int EXERCISE_CALENDAR_WITH_DATE_AND_PRESCRIBED_AND_SESSION = 202;

    // week = ?
    private static final String sWeekSelection = PrescribedExercisesEntry.COLUMN_WEEK + " = ? ";

    // week = ? AND day = ?
    private static final String sWeekAndDaySelection = PrescribedExercisesEntry.COLUMN_WEEK +
            " = ? AND " + PrescribedExercisesEntry.COLUMN_DAY + " = ? ";

    // date = ?
    private static final String sDateSelection = ExerciseCalendarEntry.COLUMN_DATE + " = ? ";

    // date = ? AND prescribed = ? AND session = ?
    private static final String sDateAndPrescribedAndSessionSelection =
            ExerciseCalendarEntry.COLUMN_DATE + " = ? AND " +
                    ExerciseCalendarEntry.COLUMN_PRESCRIBED + " = ? AND " +
                    ExerciseCalendarEntry.COLUMN_SESSION + " = ? ";

    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found. The code passed into the constructor represents the code to return for the root
        // URI. It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, PATH_PRESCRIBED_EXERCISES, PRESCRIBED_EXERCISES);
        matcher.addURI(CONTENT_AUTHORITY, PATH_PRESCRIBED_EXERCISES + "/#",
                PRESCRIBED_EXERCISES_WITH_WEEK);
        matcher.addURI(CONTENT_AUTHORITY, PATH_PRESCRIBED_EXERCISES + "/#/#",
                PRESCRIBED_EXERCISES_WITH_WEEK_AND_DAY);
        matcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISE_CALENDAR, EXERCISE_CALENDAR);
        matcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISE_CALENDAR + "/#",
                EXERCISE_CALENDAR_WITH_DATE);
        matcher.addURI(CONTENT_AUTHORITY, PATH_EXERCISE_CALENDAR + "/#/#/#",
                EXERCISE_CALENDAR_WITH_DATE_AND_PRESCRIBED_AND_SESSION);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ExerciseProgramDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        // Given a URI, this switch statement will determine what kind of request it is, and
        // query the database accordingly.
        Cursor returnCursor;
        String[] newSelectionArgs;
        int week;
        int day;
        switch (sUriMatcher.match(uri)) {
            case PRESCRIBED_EXERCISES:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        PrescribedExercisesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PRESCRIBED_EXERCISES_WITH_WEEK:
                week = PrescribedExercisesEntry.getWeekFromUri(uri);
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        PrescribedExercisesEntry.TABLE_NAME,
                        projection,
                        sWeekSelection,
                        new String[]{Integer.toString(week)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case PRESCRIBED_EXERCISES_WITH_WEEK_AND_DAY:
                week = PrescribedExercisesEntry.getWeekFromUri(uri);
                day = PrescribedExercisesEntry.getDayFromUri(uri);
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        PrescribedExercisesEntry.TABLE_NAME,
                        projection,
                        sWeekAndDaySelection,
                        new String[]{Integer.toString(week), Integer.toString(day)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISE_CALENDAR:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        ExerciseCalendarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISE_CALENDAR_WITH_DATE:
                newSelectionArgs =
                        new String[1 + (selectionArgs == null ? 0 : selectionArgs.length)];
                if (selectionArgs != null) {
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
                }
                newSelectionArgs[0] = Long.toString(ExerciseCalendarEntry.getDateFromUri(uri));
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        ExerciseCalendarEntry.TABLE_NAME,
                        projection,
                        sDateSelection +
                                (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""),
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISE_CALENDAR_WITH_DATE_AND_PRESCRIBED_AND_SESSION:
                newSelectionArgs =
                        new String[3 + (selectionArgs == null ? 0 : selectionArgs.length)];
                if (selectionArgs != null) {
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 3, selectionArgs.length);
                }
                newSelectionArgs[0] = Long.toString(ExerciseCalendarEntry.getDateFromUri(uri));
                newSelectionArgs[1] =
                        Integer.toString(ExerciseCalendarEntry.getPrescribedFromUri(uri));
                newSelectionArgs[2] =
                        Integer.toString(ExerciseCalendarEntry.getSessionFromUri(uri));
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        ExerciseCalendarEntry.TABLE_NAME,
                        projection,
                        sDateAndPrescribedAndSessionSelection +
                                (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""),
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Set the notification URI for the cursor to the one that was passed into the method.
        // This causes the cursor to register a content observer, to watch for changes that happen
        // to that URI and any of its descendants. This allows the content provider to easily tell
        // the UI when the cursor changes, on operations like a database insert or update.
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRESCRIBED_EXERCISES:
                return PrescribedExercisesEntry.CONTENT_TYPE;
            case PRESCRIBED_EXERCISES_WITH_WEEK:
                return PrescribedExercisesEntry.CONTENT_TYPE;
            case PRESCRIBED_EXERCISES_WITH_WEEK_AND_DAY:
                return PrescribedExercisesEntry.CONTENT_TYPE;
            case EXERCISE_CALENDAR:
                return ExerciseCalendarEntry.CONTENT_TYPE;
            case EXERCISE_CALENDAR_WITH_DATE:
                return ExerciseCalendarEntry.CONTENT_TYPE;
            case EXERCISE_CALENDAR_WITH_DATE_AND_PRESCRIBED_AND_SESSION:
                return ExerciseCalendarEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PRESCRIBED_EXERCISES: {
                long _id = sqLiteDatabase.insert(
                        PrescribedExercisesEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = PrescribedExercisesEntry.buildPrescribedExerciseUri(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            }
            case EXERCISE_CALENDAR: {
                long _id = sqLiteDatabase.insert(
                        ExerciseCalendarEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = ExerciseCalendarEntry.buildExerciseSessionUri(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            }
            case EXERCISE_CALENDAR_WITH_DATE_AND_PRESCRIBED_AND_SESSION: {
                long _id = sqLiteDatabase.insert(
                        ExerciseCalendarEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = ExerciseCalendarEntry.buildExerciseSessionUri(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // When we insert into the database, we want it to notify every content observer that might
        // have data modified by our insert. Luckily, cursors register themselves as notify for
        // descendants.
        // Note that we must also use the passed in URI, and not the return URI, as that will not
        // correctly notify our cursors of the change.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // This makes delete all rows return the number of rows deleted
        if (null == selection) {
            selection = "1";
        }
        switch (match) {
            case PRESCRIBED_EXERCISES:
                rowsDeleted = sqLiteDatabase.delete(
                        PrescribedExercisesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EXERCISE_CALENDAR:
                rowsDeleted = sqLiteDatabase.delete(
                        ExerciseCalendarEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PRESCRIBED_EXERCISES:
                rowsUpdated = sqLiteDatabase.update(PrescribedExercisesEntry.TABLE_NAME,
                        contentValues, selection, selectionArgs);
                break;
            case EXERCISE_CALENDAR:
                rowsUpdated = sqLiteDatabase.update(ExerciseCalendarEntry.TABLE_NAME,
                        contentValues, selection, selectionArgs);
                break;
            case EXERCISE_CALENDAR_WITH_DATE_AND_PRESCRIBED_AND_SESSION:
                String[] newSelectionArgs =
                        new String[3 + (selectionArgs == null ? 0 : selectionArgs.length)];
                if (selectionArgs != null) {
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 3, selectionArgs.length);
                }
                newSelectionArgs[0] = Long.toString(ExerciseCalendarEntry.getDateFromUri(uri));
                newSelectionArgs[1] =
                        Integer.toString(ExerciseCalendarEntry.getPrescribedFromUri(uri));
                newSelectionArgs[2] =
                        Integer.toString(ExerciseCalendarEntry.getSessionFromUri(uri));
                rowsUpdated = sqLiteDatabase.update(
                        ExerciseCalendarEntry.TABLE_NAME,
                        contentValues,
                        sDateAndPrescribedAndSessionSelection +
                                (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""),
                        newSelectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] contentValuesArray) {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case PRESCRIBED_EXERCISES:
                sqLiteDatabase.beginTransaction();
                try {
                    for (ContentValues value : contentValuesArray) {
                        long _id = sqLiteDatabase.insert(
                                PrescribedExercisesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case EXERCISE_CALENDAR:
                sqLiteDatabase.beginTransaction();
                try {
                    for (ContentValues value : contentValuesArray) {
                        long _id = sqLiteDatabase.insert(
                                ExerciseCalendarEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, contentValuesArray);
        }
    }
}
