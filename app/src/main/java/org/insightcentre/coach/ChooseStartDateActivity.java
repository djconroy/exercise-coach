package org.insightcentre.coach;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.DefaultDayViewAdapter;

import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

public class ChooseStartDateActivity extends AppCompatActivity {
    private CalendarPickerView mCalendarPickerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_start_date);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Calendar today = Calendar.getInstance();
        today.setTimeZone(Utility.timeZoneAtHome());
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        today.setTimeZone(TimeZone.getDefault());

        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        nextYear.setTimeZone(Utility.timeZoneAtHome());
        nextYear.set(Calendar.DAY_OF_MONTH, nextYear.get(Calendar.DAY_OF_MONTH));
        nextYear.setTimeZone(TimeZone.getDefault());

        mCalendarPickerView = (CalendarPickerView) findViewById(R.id.calendar_view);
        mCalendarPickerView.setCustomDayView(new DefaultDayViewAdapter());
        mCalendarPickerView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        mCalendarPickerView.init(today.getTime(), nextYear.getTime())
                           .inMode(CalendarPickerView.SelectionMode.SINGLE)
                           .withSelectedDate(today.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_start_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.save) {
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(mCalendarPickerView.getSelectedDate());
            startDate.set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH));
            startDate.setTimeZone(Utility.timeZoneAtHome());
            Utility.setMidnight(startDate);

            boolean inDaylightTime = Utility.inDaylightTime(startDate);

            SharedPreferences datesSharedPrefs = getSharedPreferences(getString(R.string.dates_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = datesSharedPrefs.edit();
            editor.putLong(getString(R.string.start_date), startDate.getTimeInMillis());
            editor.putLong(getString(R.string.end_date),
                           startDate.getTimeInMillis() + Utility.PROGRAM_LENGTH_IN_MILLIS - DateUtils.DAY_IN_MILLIS);
            editor.putBoolean(getString(R.string.start_date_in_dst), inDaylightTime);
            editor.commit();
            setResult(RESULT_OK);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
