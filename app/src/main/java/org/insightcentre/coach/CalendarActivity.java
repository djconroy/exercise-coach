package org.insightcentre.coach;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.DefaultDayViewAdapter;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class CalendarActivity extends AppCompatActivity
                              implements CalendarPickerView.OnDateSelectedListener {
    private CalendarPickerView mCalendarPickerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.exercise_calendar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        calendar.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(calendar);
        Utility.adjustForDST(this, calendar);

        SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
        long startDate = datesSharedPrefs.getLong(getString(R.string.start_date), calendar.getTimeInMillis());
        long endDate = datesSharedPrefs.getLong(getString(R.string.end_date), calendar.getTimeInMillis());

        if (calendar.getTimeInMillis() < startDate) {
            calendar.setTimeInMillis(startDate);
        } else if (calendar.getTimeInMillis() > endDate) {
            calendar.setTimeInMillis(endDate);
        }

        int week = (int) ((calendar.getTimeInMillis() - startDate) / DateUtils.WEEK_IN_MILLIS);

        Calendar start = Calendar.getInstance(Utility.timeZoneAtHome());
        start.setTimeInMillis(startDate);
        start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH));
        start.setTimeZone(TimeZone.getDefault());

        Calendar end = Calendar.getInstance(Utility.timeZoneAtHome());
        end.setTimeInMillis(startDate);
        end.add(Calendar.DAY_OF_MONTH, (week + 1) * 7);
        end.set(Calendar.DAY_OF_MONTH, end.get(Calendar.DAY_OF_MONTH));
        end.setTimeZone(TimeZone.getDefault());

        mCalendarPickerView = (CalendarPickerView) findViewById(R.id.calendar_view);
        mCalendarPickerView.setCustomDayView(new DefaultDayViewAdapter());
        mCalendarPickerView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        mCalendarPickerView.setOnDateSelectedListener(this);
        mCalendarPickerView.init(start.getTime(), end.getTime())
                           .inMode(CalendarPickerView.SelectionMode.SINGLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
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
    public void onDateSelected(Date date) {
        Intent intent = new Intent(this, DateActivity.class);
        Calendar chosenDate = Calendar.getInstance();
        chosenDate.setTime(date);
        chosenDate.set(Calendar.DAY_OF_MONTH, chosenDate.get(Calendar.DAY_OF_MONTH));
        chosenDate.setTimeZone(Utility.timeZoneAtHome());
        Utility.setMidnight(chosenDate);
        Utility.adjustForDST(this, chosenDate);
        intent.putExtra(DateActivity.EXTRA_DATE, chosenDate.getTimeInMillis());
        startActivity(intent);
    }

    @Override
    public void onDateUnselected(Date date) {}
}
