package org.insightcentre.coach;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class DateActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_DATE = "date";

    private static final int DATE_LOADER = 0;

    private ExercisesAdapter mExercisesAdapter;
    private long mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(today);
        Utility.adjustForDST(this, today);
        mDate = getIntent().getLongExtra(EXTRA_DATE, today.getTimeInMillis());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utility.getDateString(mDate));

        TextView emptyView = (TextView) findViewById(R.id.recyclerview_exercises_empty);
        if (mDate < today.getTimeInMillis()) {
            emptyView.setText(getString(R.string.empty_exercises_list_past,
                    Utility.getFriendlyDayString(this, mDate)));
        } else {
            emptyView.setText(getString(R.string.empty_exercises_list,
                    Utility.getFriendlyDayString(this, mDate)));
        }

        mExercisesAdapter = new ExercisesAdapter(this,
                new ExercisesAdapter.OnClickHandler() {
                    @Override
                    public void onClick(long date, int prescribed, int session) {
                        onItemSelected(buildExerciseCalendarDateWithPrescribedAndSession(
                                date, prescribed, session));
                    }
                }, emptyView);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_exercises);
        recyclerView.setAdapter(mExercisesAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (mDate > today.getTimeInMillis()) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DateActivity.this, ExerciseSessionActivity.class);

                    int nextExtraSession = 1;
                    Cursor cursor = mExercisesAdapter.getCursor();

                    if (cursor.moveToLast()) {
                        if (cursor.getInt(HomeActivity.COL_EXERCISES_PRESCRIBED) ==
                                SESSION_NOT_PRESCRIBED) {
                            nextExtraSession =
                                    1 + cursor.getInt(HomeActivity.COL_EXERCISES_SESSION);
                        }
                    }
                    intent.putExtra(ExerciseSessionActivity.EXTRA_NEXT_EXTRA_SESSION,
                            nextExtraSession);
                    intent.putExtra(ExerciseSessionActivity.EXTRA_DATE, mDate);
                    startActivity(intent);
                }
            });
        }

        getSupportLoaderManager().initLoader(DATE_LOADER, null, this);
    }

    void onItemSelected(Uri contentUri) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(today);
        Utility.adjustForDST(this, today);
        if (mDate <= today.getTimeInMillis()) {
            Intent intent = new Intent(this, ExerciseSessionActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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
                buildExerciseCalendarDate(mDate),
                HomeActivity.EXERCISES_COLUMNS,
                null,
                null,
                COLUMN_PRESCRIBED + " DESC, " + COLUMN_SESSION + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mExercisesAdapter.swapCursor(data);
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) findViewById(R.id.toolbar).getLayoutParams();
        if (mExercisesAdapter.getItemCount() == 0) {
            params.setScrollFlags(0);
        } else {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mExercisesAdapter.swapCursor(null);
    }
}
