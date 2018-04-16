package org.insightcentre.coach;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import static org.insightcentre.coach.data.ExerciseProgramContract.*;

public class StorePrescribedExercisesService extends IntentService {
    public StorePrescribedExercisesService() {
        super("StorePrescribedExercisesService");
    }

    private ContentValues newContentValues(int week,
                                           int day,
                                           int session,
                                           int target_length,
                                           int target_rpe) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PrescribedExercisesEntry.COLUMN_WEEK, week);
        contentValues.put(PrescribedExercisesEntry.COLUMN_DAY, day);
        contentValues.put(PrescribedExercisesEntry.COLUMN_SESSION, session);
        contentValues.put(PrescribedExercisesEntry.COLUMN_TARGET_LENGTH, target_length);
        contentValues.put(PrescribedExercisesEntry.COLUMN_TARGET_RPE, target_rpe);
        return contentValues;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final int numSessions = 236;
            ContentValues[] contentValuesArray = new ContentValues[numSessions];
            int i = 0;
            // Week 1
            contentValuesArray[i++] = newContentValues(1, 2, 1, 120, 12);
            contentValuesArray[i++] = newContentValues(1, 2, 2, 120, 12);
            contentValuesArray[i++] = newContentValues(1, 4, 1, 120, 12);
            contentValuesArray[i++] = newContentValues(1, 4, 2, 120, 12);
            contentValuesArray[i++] = newContentValues(1, 6, 1, 120, 12);
            contentValuesArray[i++] = newContentValues(1, 6, 2, 120, 12);
            // Week 2
            contentValuesArray[i++] = newContentValues(2, 1, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 1, 2, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 3, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 3, 2, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 5, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 5, 2, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 7, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(2, 7, 2, 180, 12);
            // Week 3
            contentValuesArray[i++] = newContentValues(3, 2, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 2, 2, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 2, 3, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 4, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 4, 2, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 4, 3, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 6, 1, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 6, 2, 180, 12);
            contentValuesArray[i++] = newContentValues(3, 6, 3, 180, 12);
            // Week 4
            contentValuesArray[i++] = newContentValues(4, 1, 1, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 1, 2, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 1, 3, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 3, 1, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 3, 2, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 3, 3, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 5, 1, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 5, 2, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 5, 3, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 6, 1, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 6, 2, 240, 12);
            contentValuesArray[i++] = newContentValues(4, 6, 3, 240, 12);
            // Week 5
            contentValuesArray[i++] = newContentValues(5, 1, 1, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 1, 2, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 1, 3, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 2, 1, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 2, 2, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 2, 3, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 4, 1, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 4, 2, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 4, 3, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 6, 1, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 6, 2, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 6, 3, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 7, 1, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 7, 2, 240, 13);
            contentValuesArray[i++] = newContentValues(5, 7, 3, 240, 13);
            // Week 6
            contentValuesArray[i++] = newContentValues(6, 2, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 2, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 2, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 3, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 3, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 3, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 5, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 5, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 5, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 7, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 7, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(6, 7, 3, 300, 13);
            // Week 7
            contentValuesArray[i++] = newContentValues(7, 1, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 1, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 1, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 3, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 3, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 3, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 4, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 4, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 4, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 6, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 6, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 6, 3, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 7, 1, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 7, 2, 300, 13);
            contentValuesArray[i++] = newContentValues(7, 7, 3, 300, 13);
            // Week 8
            contentValuesArray[i++] = newContentValues(8, 2, 1, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 2, 2, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 2, 3, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 3, 1, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 3, 2, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 3, 3, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 5, 1, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 5, 2, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 5, 3, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 6, 1, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 6, 2, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 6, 3, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 7, 1, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 7, 2, 360, 13);
            contentValuesArray[i++] = newContentValues(8, 7, 3, 360, 13);
            // Week 9
            contentValuesArray[i++] = newContentValues(9, 2, 1, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 2, 2, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 2, 3, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 3, 1, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 3, 2, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 3, 3, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 4, 1, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 4, 2, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 4, 3, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 5, 1, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 5, 2, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 5, 3, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 6, 1, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 6, 2, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 6, 3, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 7, 1, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 7, 2, 360, 14);
            contentValuesArray[i++] = newContentValues(9, 7, 3, 360, 14);
            // Week 10
            contentValuesArray[i++] = newContentValues(10, 2, 1, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 2, 2, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 2, 3, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 3, 1, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 3, 2, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 3, 3, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 4, 1, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 4, 2, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 4, 3, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 5, 1, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 5, 2, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 5, 3, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 6, 1, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 6, 2, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 6, 3, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 7, 1, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 7, 2, 420, 14);
            contentValuesArray[i++] = newContentValues(10, 7, 3, 420, 14);
            // Week 11
            contentValuesArray[i++] = newContentValues(11, 2, 1, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 2, 2, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 2, 3, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 3, 1, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 3, 2, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 3, 3, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 4, 1, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 4, 2, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 4, 3, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 5, 1, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 5, 2, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 5, 3, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 6, 1, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 6, 2, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 6, 3, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 7, 1, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 7, 2, 480, 14);
            contentValuesArray[i++] = newContentValues(11, 7, 3, 480, 14);
            // Week 12
            contentValuesArray[i++] = newContentValues(12, 2, 1, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 2, 2, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 2, 3, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 3, 1, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 3, 2, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 3, 3, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 4, 1, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 4, 2, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 4, 3, 540, 14);
            contentValuesArray[i++] = newContentValues(12, 5, 1, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 5, 2, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 5, 3, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 6, 1, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 6, 2, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 6, 3, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 7, 1, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 7, 2, 540, 15);
            contentValuesArray[i++] = newContentValues(12, 7, 3, 540, 15);
            // Week 13
            contentValuesArray[i++] = newContentValues(13, 2, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 2, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 2, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 3, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 3, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 3, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 4, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 4, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 4, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 5, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 5, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 5, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 6, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 6, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 6, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 7, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 7, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(13, 7, 3, 600, 15);
            // Week 14
            contentValuesArray[i++] = newContentValues(14, 2, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 2, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 2, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 3, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 3, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 3, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 4, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 4, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 4, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 5, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 5, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 5, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 6, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 6, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 6, 3, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 7, 1, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 7, 2, 600, 15);
            contentValuesArray[i++] = newContentValues(14, 7, 3, 600, 15);
            // Week 15
            contentValuesArray[i++] = newContentValues(15, 2, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 2, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 2, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 3, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 3, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 3, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 4, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 4, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 4, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 5, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 5, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 5, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 6, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 6, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 6, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 7, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 7, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(15, 7, 3, 600, 16);
            // Week 16
            contentValuesArray[i++] = newContentValues(16, 2, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 2, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 2, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 3, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 3, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 3, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 4, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 4, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 4, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 5, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 5, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 5, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 6, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 6, 2, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 6, 3, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 7, 1, 600, 16);
            contentValuesArray[i++] = newContentValues(16, 7, 2, 600, 16);
            contentValuesArray[i] = newContentValues(16, 7, 3, 600, 16);

            getContentResolver().bulkInsert(
                    PrescribedExercisesEntry.CONTENT_URI, contentValuesArray);
        }
    }
}
