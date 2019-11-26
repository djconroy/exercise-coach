package org.insightcentre.coach;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class HistoryActivity extends AppCompatActivity
                             implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int HISTORY_LOADER = 0;

    private RecyclerView mRecyclerView;
    private HistoryAdapter mHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.exercise_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_exercises);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        getSupportLoaderManager().initLoader(HISTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                                CONTENT_URI,
                                HomeActivity.EXERCISES_COLUMNS,
                                null,
                                null,
                                COLUMN_DATE + " ASC, " + COLUMN_PRESCRIBED + " DESC, " + COLUMN_SESSION + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mHistoryAdapter = new HistoryAdapter(this, data);
        mRecyclerView.setAdapter(mHistoryAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHistoryAdapter.swapCursor(null);
    }
}
