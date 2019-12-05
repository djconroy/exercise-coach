package org.insightcentre.coach;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class HomeActivity extends AppCompatActivity
                          implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private static final int REQUEST_CHOOSE_START_DATE = 0;
    private static final int EXERCISES_LOADER = 0;

    static final String[] EXERCISES_COLUMNS = {
            _ID,
            COLUMN_DATE,
            COLUMN_SESSION,
            COLUMN_LEVEL,
            COLUMN_TARGET_LENGTH,
            COLUMN_ACTUAL_LENGTH,
            COLUMN_TARGET_RPE,
            COLUMN_ACTUAL_RPE,
            COLUMN_TYPE,
            COLUMN_SUCCESS,
            COLUMN_PRESCRIBED
    };

    // These indices are tied to EXERCISES_COLUMNS, so if EXERCISES_COLUMNS changes, these must change too
    static final int COL_EXERCISES_ID = 0;
    static final int COL_EXERCISES_DATE = 1;
    static final int COL_EXERCISES_SESSION = 2;
    static final int COL_EXERCISES_LEVEL = 3;
    static final int COL_EXERCISES_TARGET_LENGTH = 4;
    static final int COL_EXERCISES_ACTUAL_LENGTH = 5;
    static final int COL_EXERCISES_TARGET_RPE = 6;
    static final int COL_EXERCISES_ACTUAL_RPE = 7;
    static final int COL_EXERCISES_TYPE = 8;
    static final int COL_EXERCISES_SUCCESS = 9;
    static final int COL_EXERCISES_PRESCRIBED = 10;

    private ExercisesAdapter mExercisesAdapter;
    private long mExercisesStartDate;
    private long mExercisesEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View emptyView = findViewById(R.id.recyclerview_exercises_empty);
        mExercisesAdapter = new ExercisesAdapter(this,
            new ExercisesAdapter.OnClickHandler() {
                @Override
                public void onClick(long date, int prescribed, int session) {
                    onItemSelected(buildExerciseCalendarDateWithPrescribedAndSession(date, prescribed, session));
                }
            }, emptyView);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_exercises);
        recyclerView.setAdapter(mExercisesAdapter);

        // Set the layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Add dividers between the items of the recycler view
        DividerItemDecoration dividerItemDecoration =
            new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ExerciseSessionActivity.class);

                int nextExtraSession = 1;
                Cursor cursor = mExercisesAdapter.getCursor();
                cursor.moveToLast();

                Calendar today = Calendar.getInstance();
                today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                today.setTimeZone(Utility.timeZoneAtHome());
                Utility.setMidnight(today);
                Utility.adjustForDST(HomeActivity.this, today);

                if (cursor.getLong(COL_EXERCISES_DATE) >= today.getTimeInMillis()) {
                    if (cursor.getInt(COL_EXERCISES_PRESCRIBED) == SESSION_NOT_PRESCRIBED) {
                        nextExtraSession = cursor.getInt(COL_EXERCISES_SESSION) + 1;
                    } else {
                        if (cursor.getLong(COL_EXERCISES_DATE) > today.getTimeInMillis()) {
                            while (cursor.moveToPrevious() && cursor.getLong(COL_EXERCISES_DATE) >= today.getTimeInMillis()) {
                                if (cursor.getInt(COL_EXERCISES_PRESCRIBED) == SESSION_NOT_PRESCRIBED) {
                                    nextExtraSession = cursor.getInt(COL_EXERCISES_SESSION) + 1;
                                    break;
                                }
                            }
                        }
                    }
                }
                intent.putExtra(ExerciseSessionActivity.EXTRA_NEXT_EXTRA_SESSION, nextExtraSession);
                intent.putExtra(ExerciseSessionActivity.EXTRA_DATE, today.getTimeInMillis());
                startActivity(intent);
            }
        });

        SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
        long startDate = datesSharedPrefs.getLong(getString(R.string.start_date), 0L);
        long endDate = datesSharedPrefs.getLong(getString(R.string.end_date), 0L);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(today);
        Utility.adjustForDST(this, today);

        if (startDate != 0L && today.getTimeInMillis() >= startDate && today.getTimeInMillis() <= endDate) {
            fab.setVisibility(View.VISIBLE);
        }

        if (!datesSharedPrefs.contains(getString(R.string.start_date))) {
            Intent chooseStartDateIntent = new Intent(this, ChooseStartDateActivity.class);
            startActivityForResult(chooseStartDateIntent, REQUEST_CHOOSE_START_DATE);

            startService(new Intent(this, StorePrescribedExercisesService.class));
        } else {
            if (savedInstanceState == null) {
                new DisplayCorrectExercisesTask().execute();
            } else {
                getSupportLoaderManager().initLoader(EXERCISES_LOADER, null, this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_START_DATE) {
            if (resultCode == RESULT_OK) {
                SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
                Calendar today = Calendar.getInstance();
                today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                today.setTimeZone(Utility.timeZoneAtHome());
                Utility.setMidnight(today);
                long startDate = datesSharedPrefs.getLong(getString(R.string.start_date), today.getTimeInMillis());

                if (today.getTimeInMillis() >= startDate) {
                    findViewById(R.id.fab).setVisibility(View.VISIBLE);
                }

                Snackbar.make(findViewById(R.id.toolbar),
                              getString(R.string.start_date_confirmation, Utility.getFriendlyDayString(this, startDate)),
                              Snackbar.LENGTH_LONG).show();

                Intent scheduleExercisesIntent =
                    new Intent(this, ScheduleExercisesService.class).putExtra(ScheduleExercisesService.EXTRA_START_DATE, startDate);
                startService(scheduleExercisesIntent);

                findViewById(R.id.activity_home).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new DisplayCorrectExercisesTask().execute();
                    }
                }, 300);
            }
        }
    }

    void onItemSelected(Uri contentUri) {
        Intent intent = new Intent(this, ExerciseSessionActivity.class).setData(contentUri);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_calendar) {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                                CONTENT_URI,
                                EXERCISES_COLUMNS,
                                COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ? ",
                                new String[]{Long.toString(mExercisesStartDate), Long.toString(mExercisesEndDate)},
                                COLUMN_DATE + " ASC, " + COLUMN_PRESCRIBED + " DESC, " + COLUMN_SESSION + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mExercisesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mExercisesAdapter.swapCursor(null);
    }

    private class DisplayCorrectExercisesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
            calendar.setTimeZone(Utility.timeZoneAtHome());
            Utility.setMidnight(calendar);
            Utility.adjustForDST(HomeActivity.this, calendar);
            long today = calendar.getTimeInMillis();
            long startDate = datesSharedPrefs.getLong(getString(R.string.start_date), today);
            long endDate = datesSharedPrefs.getLong(getString(R.string.end_date), today);
            boolean foundCorrectDate = false;

            if (calendar.getTimeInMillis() < startDate) {
                calendar.setTimeInMillis(startDate);
            } else if (calendar.getTimeInMillis() > endDate) {
                calendar.setTimeInMillis(endDate);
            }
            long week = (calendar.getTimeInMillis() - startDate) / DateUtils.WEEK_IN_MILLIS;
            long startOfWeek = startDate + week * DateUtils.WEEK_IN_MILLIS;
            long startOfNextWeek = startOfWeek + DateUtils.WEEK_IN_MILLIS;

            while (!foundCorrectDate && calendar.getTimeInMillis() >= startOfWeek) {
                Cursor prescriptionCursor = getContentResolver().query(buildExerciseCalendarDate(calendar.getTimeInMillis()),
                    new String[]{"COUNT(*)"},
                    COLUMN_PRESCRIBED + " = ? ",
                    new String[]{Integer.toString(SESSION_PRESCRIBED)},
                    null);

                if (prescriptionCursor != null && prescriptionCursor.moveToFirst() && prescriptionCursor.getInt(0) > 0) {
                    foundCorrectDate = true;
                } else {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - DateUtils.DAY_IN_MILLIS);
                }
                if (prescriptionCursor != null) {
                    prescriptionCursor.close();
                }
            }
            if (foundCorrectDate) {
                mExercisesStartDate = calendar.getTimeInMillis();
                mExercisesEndDate = Math.max(today, startDate);
            } else {
                calendar.setTimeInMillis(Math.max(today, startDate) + DateUtils.DAY_IN_MILLIS);

                while (!foundCorrectDate && calendar.getTimeInMillis() < startOfNextWeek) {
                    Cursor prescriptionCursor = getContentResolver().query(buildExerciseCalendarDate(calendar.getTimeInMillis()),
                        new String[]{"COUNT(*)"},
                        COLUMN_PRESCRIBED + " = ? ",
                        new String[]{Integer.toString(SESSION_PRESCRIBED)},
                        null);

                    if (prescriptionCursor != null && prescriptionCursor.moveToFirst() && prescriptionCursor.getInt(0) > 0) {
                        foundCorrectDate = true;
                    } else {
                        calendar.setTimeInMillis(calendar.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
                    }
                    if (prescriptionCursor != null) {
                        prescriptionCursor.close();
                    }
                }
                if (foundCorrectDate) {
                    mExercisesStartDate = Math.max(today, startDate);
                    mExercisesEndDate = calendar.getTimeInMillis();
                } else {
//                    Log.v(LOG_TAG, "Couldn't find any prescribed exercises for this week");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getSupportLoaderManager().initLoader(EXERCISES_LOADER, null, HomeActivity.this);
        }
    }
}
