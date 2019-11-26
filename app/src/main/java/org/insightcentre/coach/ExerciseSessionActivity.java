package org.insightcentre.coach;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class ExerciseSessionActivity extends AppCompatActivity
                                     implements DiscardChangesDialogFragment.DiscardChangesDialogListener,
                                                RPEDialogFragment.RPEDialogListener {

    private static final String[] SESSION_COLUMNS = {
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

    private static final int COL_DATE = 0;
    private static final int COL_SESSION = 1;
    private static final int COL_LEVEL = 2;
    private static final int COL_TARGET_LENGTH = 3;
    private static final int COL_ACTUAL_LENGTH = 4;
    private static final int COL_TARGET_RPE = 5;
    private static final int COL_ACTUAL_RPE = 6;
    private static final int COL_TYPE = 7;
    private static final int COL_SUCCESS = 8;
    private static final int COL_PRESCRIBED = 9;

    private long mDate;
    private int mSession;
    private int mLevel;
    private int mTargetLength;
    private int mActualLength;
    private int mTargetRPE;
    private int mActualRPE;
    private int mType;
    private int mSuccess;
    private int mPrescribed;

    private Spinner mTypeSpinner;
    private TextInputEditText mMinutesEditText;
    private TextInputEditText mSecondsEditText;
    private TextInputLayout mMinutesInputLayout;
    private TextInputLayout mSecondsInputLayout;
    private TextView mRPETextView;

    public static final String EXTRA_NEXT_EXTRA_SESSION = "next extra session";
    public static final String EXTRA_DATE = "date";

    @Override
    public void onBackPressed() {
        if (changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exercise_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.exercise_session);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        getSupportActionBar().setHomeActionContentDescription(R.string.go_back_to_the_previous_screen);

        mTypeSpinner = (Spinner) findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter =
            ArrayAdapter.createFromResource(this, R.array.exercise_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(adapter);

        mRPETextView = (TextView) findViewById(R.id.rpe);
        findViewById(R.id.rpe_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRPEDialog();
            }
        });

        mMinutesInputLayout = (TextInputLayout) findViewById(R.id.minutes_layout);
        mSecondsInputLayout = (TextInputLayout) findViewById(R.id.seconds_layout);

        mMinutesEditText = (TextInputEditText) findViewById(R.id.minutes);
        mMinutesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) >= 99) {
                    mMinutesInputLayout.setErrorEnabled(true);
                    mMinutesInputLayout.setError(getString(R.string.this_is_too_big));
                } else {
                    mMinutesInputLayout.setErrorEnabled(false);
                }
            }
        });

        mSecondsEditText = (TextInputEditText) findViewById(R.id.seconds);
        mSecondsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) >= 60) {
                    mSecondsInputLayout.setErrorEnabled(true);
                    mSecondsInputLayout.setError(getString(R.string.seconds_overflow_error_message));
                } else {
                    mSecondsInputLayout.setErrorEnabled(false);
                }
            }
        });

        if (savedInstanceState == null) {
            if (getIntent().getData() != null) {
                new FetchSessionDataTask().execute(getIntent().getData());
            } else {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                today.setTimeZone(Utility.timeZoneAtHome());
                Utility.setMidnight(today);
                Utility.adjustForDST(this, today);

                mDate = getIntent().getLongExtra(EXTRA_DATE, today.getTimeInMillis());
                mSession = getIntent().getIntExtra(EXTRA_NEXT_EXTRA_SESSION, 1);
                SharedPreferences levelSharedPrefs = getSharedPreferences(getString(R.string.level_key), Context.MODE_PRIVATE);
                mLevel = levelSharedPrefs.getInt(getString(R.string.current_level), 1);
                mTargetLength = 0;
                mActualLength = 0;
                mTargetRPE = 0;
                mActualRPE = 0;
                mType = TYPE_NOT_RECORDED;
                mSuccess = SUCCESS_NOT_RECORDED;
                mPrescribed = SESSION_NOT_PRESCRIBED;

                prepareUI();
            }
        } else {
            mDate = savedInstanceState.getLong(SESSION_COLUMNS[COL_DATE]);
            mSession = savedInstanceState.getInt(SESSION_COLUMNS[COL_SESSION]);
            mLevel = savedInstanceState.getInt(SESSION_COLUMNS[COL_LEVEL]);
            mTargetLength = savedInstanceState.getInt(SESSION_COLUMNS[COL_TARGET_LENGTH]);
            mActualLength = savedInstanceState.getInt(SESSION_COLUMNS[COL_ACTUAL_LENGTH]);
            mTargetRPE = savedInstanceState.getInt(SESSION_COLUMNS[COL_TARGET_RPE]);
            mActualRPE = savedInstanceState.getInt(SESSION_COLUMNS[COL_ACTUAL_RPE]);
            mType = savedInstanceState.getInt(SESSION_COLUMNS[COL_TYPE]);
            mSuccess = savedInstanceState.getInt(SESSION_COLUMNS[COL_SUCCESS]);
            mPrescribed = savedInstanceState.getInt(SESSION_COLUMNS[COL_PRESCRIBED]);

            if (mPrescribed == SESSION_NOT_PRESCRIBED) {
                findViewById(R.id.target_time).setVisibility(View.GONE);
                findViewById(R.id.target_rpe).setVisibility(View.GONE);
            }

            mMinutesInputLayout.setHintEnabled(true);
            mMinutesInputLayout.setHintAnimationEnabled(true);

            mSecondsInputLayout.setHintEnabled(true);
            mSecondsInputLayout.setHintAnimationEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SESSION_COLUMNS[COL_DATE], mDate);
        outState.putInt(SESSION_COLUMNS[COL_SESSION], mSession);
        outState.putInt(SESSION_COLUMNS[COL_LEVEL], mLevel);
        outState.putInt(SESSION_COLUMNS[COL_TARGET_LENGTH], mTargetLength);
        outState.putInt(SESSION_COLUMNS[COL_ACTUAL_LENGTH], mActualLength);
        outState.putInt(SESSION_COLUMNS[COL_TARGET_RPE], mTargetRPE);
        outState.putInt(SESSION_COLUMNS[COL_ACTUAL_RPE], mActualRPE);
        outState.putInt(SESSION_COLUMNS[COL_TYPE], mType);
        outState.putInt(SESSION_COLUMNS[COL_SUCCESS], mSuccess);
        outState.putInt(SESSION_COLUMNS[COL_PRESCRIBED], mPrescribed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exercise_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                if (changesWereMade()) {
                    showDiscardChangesDialog();
                    return true;
                }
                finish();
                return super.onOptionsItemSelected(item);
            case R.id.save:
                if (mMinutesEditText.getText().length() == 0) {
                    mMinutesInputLayout.setErrorEnabled(true);
                    mMinutesInputLayout.setError(getString(R.string.minutes_is_required));
                }
                if (mSecondsEditText.getText().length() == 0) {
                    mSecondsInputLayout.setErrorEnabled(true);
                    mSecondsInputLayout.setError(getString(R.string.seconds_is_required));
                }
                if (mMinutesInputLayout.getError() == null && mSecondsInputLayout.getError() == null) {
                    // Don't allow the user to record extra sessions of length 0
                    if (mPrescribed == SESSION_NOT_PRESCRIBED && mType == TYPE_NOT_RECORDED
                            && Integer.parseInt(mMinutesEditText.getText().toString()) == 0
                            && Integer.parseInt(mSecondsEditText.getText().toString()) == 0) {
                        return true;
                    }

                    // Save session data
                    Uri contentUri = getIntent().getData();
                    boolean newExtraSession;

                    if (mPrescribed == SESSION_NOT_PRESCRIBED && mType == TYPE_NOT_RECORDED) {
                        contentUri = buildExerciseCalendarDateWithPrescribedAndSession(mDate, mPrescribed, mSession);
                        newExtraSession = true;
                    } else {
                        newExtraSession = false;
                    }

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN_DATE, mDate);
                    contentValues.put(COLUMN_SESSION, mSession);
                    contentValues.put(COLUMN_LEVEL, mLevel);
                    contentValues.put(COLUMN_TARGET_LENGTH, mTargetLength);

                    mActualLength = Integer.parseInt(mSecondsEditText.getText().toString())
                                    + 60 * Integer.parseInt(mMinutesEditText.getText().toString());
                    contentValues.put(COLUMN_ACTUAL_LENGTH, mActualLength);
                    contentValues.put(COLUMN_TARGET_RPE, mTargetRPE);
                    contentValues.put(COLUMN_ACTUAL_RPE, Integer.valueOf(mRPETextView.getText().toString()));

                    // This code assumes that the integer codes for exercise types correspond to their positions in the spinner
                    contentValues.put(COLUMN_TYPE, mTypeSpinner.getSelectedItemPosition());

                    if (mPrescribed == SESSION_PRESCRIBED) {
                        if (mActualLength == 0) {
                            mSuccess = SESSION_FAILED;
                        } else if (mActualLength < mTargetLength) {
                            mSuccess = SESSION_STARTED;
                        } else { // mActualLength >= mTargetLength
                            mSuccess = SESSION_COMPLETED;
                        }
                    }
                    contentValues.put(COLUMN_SUCCESS, mSuccess);
                    contentValues.put(COLUMN_PRESCRIBED, mPrescribed);

                    Intent intent = new Intent(this, SaveDataService.class);
                    intent.putExtra(SaveDataService.EXTRA_CONTENT_VALUES, contentValues);
                    intent.putExtra(SaveDataService.EXTRA_NEW_EXTRA_SESSION, newExtraSession);
                    intent.setData(contentUri);
                    startService(intent);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChooseRPE(int RPE) {
        mRPETextView.setText(String.valueOf(RPE));
    }

    @Override
    public void onCancelChoosingRPE() {}

    private void showRPEDialog() {
        DialogFragment dialog = RPEDialogFragment.newInstance(Integer.parseInt(mRPETextView.getText().toString()));
        dialog.show(getSupportFragmentManager(), "rpe");
    }

    @Override
    public void onDiscardChanges() {
        finish();
    }

    @Override
    public void onCancelDiscardingChanges() {}

    private boolean changesWereMade() {
        if (mType != TYPE_NOT_RECORDED) {
            // This code assumes that the integer codes for exercise types correspond to their positions in the spinner
            if (mTypeSpinner.getSelectedItemPosition() != mType) {
                return true;
            }
        }

        if (mType != TYPE_NOT_RECORDED) {
            if (Integer.parseInt(mRPETextView.getText().toString()) != mActualRPE) {
                return true;
            }
        } else {
            if (Integer.parseInt(mRPETextView.getText().toString()) != Utility.DEFAULT_RPE) {
                return true;
            }
        }

        if (mType != TYPE_NOT_RECORDED) {
            if (mMinutesEditText.length() == 0 && mSecondsEditText.length() > 0
                    && mActualLength % 60 != Integer.parseInt(mSecondsEditText.getText().toString())) {
                return true;
            }
            if (mMinutesEditText.length() > 0 && mSecondsEditText.length() == 0
                    && mActualLength / 60 != Integer.parseInt(mMinutesEditText.getText().toString())) {
                return true;
            }
            if (mMinutesEditText.length() > 0 && mSecondsEditText.length() > 0 &&
                    mActualLength != 60 * Integer.parseInt(mMinutesEditText.getText().toString())
                                     + Integer.parseInt(mSecondsEditText.getText().toString())) {
                return true;
            }
        } else {
            if (mMinutesEditText.length() > 0 || mSecondsEditText.length() > 0) {
                return true;
            }
        }
        return false;
    }

    private void showDiscardChangesDialog() {
        DialogFragment dialog = new DiscardChangesDialogFragment();
        dialog.show(getSupportFragmentManager(), "discard");
    }

    private void prepareUI() {
        if (mPrescribed == SESSION_PRESCRIBED) {
            ((TextView) findViewById(R.id.session)).setText(Utility.getFriendlyDayString(ExerciseSessionActivity.this, mDate)
                + "     " + ExerciseSessionActivity.this.getString(R.string.session, mSession));

            SpannableStringBuilder targetTime = new SpannableStringBuilder();
            targetTime.append(getString(R.string.target_time));
            targetTime.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ExerciseSessionActivity.this, R.color.colorPrimaryDark)),
                               0, targetTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            targetTime.append(' ');
            targetTime.append(' ');
            targetTime.append(' ');
            targetTime.append(String.valueOf(mTargetLength / 60));
            targetTime.append(' ');
            targetTime.append(getString(R.string.mins));
            targetTime.append(' ');
            targetTime.append(String.valueOf(mTargetLength % 60));
            targetTime.append(' ');
            targetTime.append(getString(R.string.secs));

            ((TextView) findViewById(R.id.target_time)).setText(targetTime);

            SpannableStringBuilder targetRPE = new SpannableStringBuilder();
            targetRPE.append(getString(R.string.target_rpe));
            targetRPE.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ExerciseSessionActivity.this, R.color.colorPrimaryDark)),
                              0, targetRPE.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            targetRPE.append(' ');
            targetRPE.append(' ');
            targetRPE.append(' ');
            targetRPE.append(String.valueOf(mTargetRPE));

            ((TextView) findViewById(R.id.target_rpe)).setText(targetRPE);
        } else {
            ((TextView) findViewById(R.id.session)).setText(Utility.getFriendlyDayString(ExerciseSessionActivity.this, mDate)
                + "     " + ExerciseSessionActivity.this.getString(R.string.extra_session, mSession));

            findViewById(R.id.target_time).setVisibility(View.GONE);
            findViewById(R.id.target_rpe).setVisibility(View.GONE);
        }

        if (mType != TYPE_NOT_RECORDED) {
            switch (mType) {
                case STEP_UPS:
                    mTypeSpinner.setSelection(0);
                    break;
                case WALK:
                    mTypeSpinner.setSelection(1);
                    break;
            }

            mMinutesEditText.setText(String.valueOf(mActualLength / 60));
            mMinutesInputLayout.setHintEnabled(true);
            mMinutesInputLayout.setHintAnimationEnabled(true);

            mSecondsEditText.setText(String.valueOf(mActualLength % 60));
            mSecondsInputLayout.setHintEnabled(true);
            mSecondsInputLayout.setHintAnimationEnabled(true);

            mRPETextView.setText(String.valueOf(mActualRPE));
        } else {
            mMinutesInputLayout.setHintEnabled(true);
            mMinutesInputLayout.setHintAnimationEnabled(true);

            mSecondsInputLayout.setHintEnabled(true);
            mSecondsInputLayout.setHintAnimationEnabled(true);

            mRPETextView.setText(String.valueOf(Utility.DEFAULT_RPE));
        }
    }

    private class FetchSessionDataTask extends AsyncTask<Uri, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Uri... uris) {
            return getContentResolver().query(uris[0], SESSION_COLUMNS, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            cursor.moveToFirst();
            mDate = cursor.getLong(COL_DATE);
            mSession = cursor.getInt(COL_SESSION);
            mLevel = cursor.getInt(COL_LEVEL);
            mTargetLength = cursor.getInt(COL_TARGET_LENGTH);
            mActualLength = cursor.getInt(COL_ACTUAL_LENGTH);
            mTargetRPE = cursor.getInt(COL_TARGET_RPE);
            mActualRPE = cursor.getInt(COL_ACTUAL_RPE);
            mType = cursor.getInt(COL_TYPE);
            mSuccess = cursor.getInt(COL_SUCCESS);
            mPrescribed = cursor.getInt(COL_PRESCRIBED);
            cursor.close();

            prepareUI();
        }
    }
}
