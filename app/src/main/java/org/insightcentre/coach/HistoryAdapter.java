package org.insightcentre.coach;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry.*;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EXERCISE = 0;
    private static final int TYPE_WEEK_SUBTITLE = -1;
    private static final int TYPE_DAY_SUBTITLE = -2;

    private Cursor mCursor;
    private List<Integer> mListItemToDataMap;
    private final Context mContext;
    private final long mStartDate;

    public class WeekSubtitleViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSubtitleView;

        public WeekSubtitleViewHolder(View itemView) {
            super(itemView);
            mSubtitleView = (TextView) itemView.findViewById(R.id.week_subtitle);
        }
    }

    public class DaySubtitleViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSubtitleView;

        public DaySubtitleViewHolder(View itemView) {
            super(itemView);
            mSubtitleView = (TextView) itemView.findViewById(R.id.day_subtitle);
        }
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        public final View mDividerView;
        public final ImageView mExerciseIconView;
        public final TextView mSessionAndTypeView;
        public final TextView mTimeView;
        public final TextView mRPEView;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            mDividerView = itemView.findViewById(R.id.divider);
            mExerciseIconView = (ImageView) itemView.findViewById(R.id.exercise_icon);
            mSessionAndTypeView = (TextView) itemView.findViewById(R.id.session_and_type);
            mTimeView = (TextView) itemView.findViewById(R.id.time);
            mRPEView = (TextView) itemView.findViewById(R.id.rpe);
        }
    }

    public HistoryAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        mContext = context;
        SharedPreferences datesSharedPrefs =
            mContext.getSharedPreferences(mContext.getString(R.string.dates_key), Context.MODE_PRIVATE);
        mStartDate = datesSharedPrefs.getLong(mContext.getString(R.string.start_date), 0);
        setup();
    }

    private void setup() {
        int week = -1;
        long date = 0L;

        mListItemToDataMap = new ArrayList<>();

        mCursor.moveToFirst();
        do {
            int temp = (int) ((mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) - mStartDate) / DateUtils.WEEK_IN_MILLIS);

            if (temp > week) {
                week = temp;
                mListItemToDataMap.add(TYPE_WEEK_SUBTITLE);
            }
            if (mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) > date) {
                date = mCursor.getLong(HomeActivity.COL_EXERCISES_DATE);
                mListItemToDataMap.add(TYPE_DAY_SUBTITLE);
            }
            mListItemToDataMap.add(mCursor.getPosition());
        } while (mCursor.moveToNext());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view;
            switch (viewType) {
                case TYPE_WEEK_SUBTITLE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_week_subtitle, parent, false);
                    view.setFocusable(true);
                    return new WeekSubtitleViewHolder(view);
                case TYPE_DAY_SUBTITLE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_day_subtitle, parent, false);
                    view.setFocusable(true);
                    return new DaySubtitleViewHolder(view);
                case TYPE_EXERCISE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_session, parent, false);
                    view.setFocusable(true);
                    return new ExerciseViewHolder(view);
                default:
                    throw new RuntimeException("Unknown view type");
            }
        } else {
            throw new RuntimeException("Not bound to RecyclerView selection");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_WEEK_SUBTITLE:
                mCursor.moveToPosition(mListItemToDataMap.get(position + 2));
                WeekSubtitleViewHolder weekSubtitleViewHolder = (WeekSubtitleViewHolder) holder;
                weekSubtitleViewHolder.mSubtitleView.setText(mContext.getString(R.string.week_and_level,
				    1 + ((mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) - mStartDate) / DateUtils.WEEK_IN_MILLIS),
					mCursor.getInt(HomeActivity.COL_EXERCISES_LEVEL)));
                break;
            case TYPE_DAY_SUBTITLE:
                mCursor.moveToPosition(mListItemToDataMap.get(position + 1));
                DaySubtitleViewHolder daySubtitleViewHolder = (DaySubtitleViewHolder) holder;
                int week = (int) ((mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) - mStartDate) / DateUtils.WEEK_IN_MILLIS);
                daySubtitleViewHolder.mSubtitleView.setText(mContext.getString(R.string.day_and_date,
                    1 + ((mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) - mStartDate - (week * DateUtils.WEEK_IN_MILLIS))
					     / DateUtils.DAY_IN_MILLIS),
                    Utility.getFriendlyDayString(mContext, mCursor.getLong(HomeActivity.COL_EXERCISES_DATE))));
                break;
            case TYPE_EXERCISE:
                mCursor.moveToPosition(mListItemToDataMap.get(position));
                ExerciseViewHolder exerciseViewHolder = (ExerciseViewHolder) holder;

                if (mListItemToDataMap.get(position) > 0) {
                    long date = mCursor.getLong(HomeActivity.COL_EXERCISES_DATE);
                    mCursor.moveToPrevious();
                    if (mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) < date) {
                        exerciseViewHolder.mDividerView.setVisibility(View.GONE);
                    } else {
                        exerciseViewHolder.mDividerView.setVisibility(View.VISIBLE);
                    }
                    mCursor.moveToNext();
                } else {
                    exerciseViewHolder.mDividerView.setVisibility(View.GONE);
                }

                boolean prescribed = false;
                switch (mCursor.getInt(HomeActivity.COL_EXERCISES_PRESCRIBED)) {
                    case SESSION_PRESCRIBED:
                        prescribed = true;
                        break;
                    case SESSION_NOT_PRESCRIBED:
                        prescribed = false;
                        break;
                }
                SpannableStringBuilder sessionAndTypeText = new SpannableStringBuilder();
                if (prescribed) {
                    sessionAndTypeText.append(mContext.getString(R.string.session,
                        mCursor.getInt(HomeActivity.COL_EXERCISES_SESSION)));
                } else {
                    sessionAndTypeText.append(mContext.getString(R.string.extra_session,
                        mCursor.getInt(HomeActivity.COL_EXERCISES_SESSION)));
                }
                sessionAndTypeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                    0, sessionAndTypeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                switch (mCursor.getInt(HomeActivity.COL_EXERCISES_TYPE)) {
                    case STEP_UPS:
                        sessionAndTypeText.append(' ');
                        sessionAndTypeText.append(' ');
                        sessionAndTypeText.append(' ');
                        sessionAndTypeText.append(mContext.getString(R.string.step_ups));
                        break;
                    case WALK:
                        sessionAndTypeText.append(' ');
                        sessionAndTypeText.append(' ');
                        sessionAndTypeText.append(' ');
                        sessionAndTypeText.append(mContext.getString(R.string.walk));
                        break;
                    case TYPE_NOT_RECORDED:
                    default:
                        break;
                }
                exerciseViewHolder.mSessionAndTypeView.setText(sessionAndTypeText);

                if (mCursor.getInt(HomeActivity.COL_EXERCISES_SUCCESS) == SESSION_COMPLETED || !prescribed) {
                    exerciseViewHolder.mExerciseIconView.setImageResource(R.drawable.ic_bench_step_up_complete);
                } else {
                    exerciseViewHolder.mExerciseIconView.setImageResource(R.drawable.ic_bench_step_up);
                }

                int actualLength = mCursor.getInt(HomeActivity.COL_EXERCISES_ACTUAL_LENGTH);
                int actualMins = actualLength / 60;
                int actualSecs = actualLength % 60;

                int targetLength = mCursor.getInt(HomeActivity.COL_EXERCISES_TARGET_LENGTH);
                int targetMins = targetLength / 60;
                int targetSecs = targetLength % 60;

                SpannableStringBuilder timeText = new SpannableStringBuilder();
                timeText.append(String.valueOf(actualMins));
                timeText.append(' ');
                int tempLength = timeText.length();
                timeText.append(mContext.getString(R.string.m));
                timeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                    tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                timeText.append(' ');
                timeText.append(String.format("%02d", actualSecs));
                timeText.append(' ');
                tempLength = timeText.length();
                timeText.append(mContext.getString(R.string.s));
                timeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                    tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (prescribed) {
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append('/');
                    timeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                        tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    timeText.append(' ');
                    timeText.append(String.valueOf(targetMins));
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append(mContext.getString(R.string.m));
                    timeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                        tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    timeText.append(' ');
                    timeText.append(String.format("%02d", targetSecs));
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append(mContext.getString(R.string.s));
                    timeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                        tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                exerciseViewHolder.mTimeView.setText(timeText);

                String timeDescription;
                if (prescribed) {
                    timeDescription = mContext.getString(R.string.a11y_time_and_target_time,
					    actualMins, actualSecs, targetMins, targetSecs);
                } else {
                    timeDescription = mContext.getString(R.string.a11y_time, actualMins, actualSecs);
                }
                exerciseViewHolder.mTimeView.setContentDescription(timeDescription);

                int actualRPE = mCursor.getInt(HomeActivity.COL_EXERCISES_ACTUAL_RPE);
                int targetRPE = mCursor.getInt(HomeActivity.COL_EXERCISES_TARGET_RPE);

                String rpeDescription;
                SpannableStringBuilder rpeText = new SpannableStringBuilder();
                rpeText.append(mContext.getString(R.string.rpe_colon));
                rpeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                    0, rpeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                rpeText.append(' ');
                rpeText.append(String.valueOf(actualRPE));

                if (prescribed) {
                    rpeDescription = mContext.getString(R.string.a11y_rpe, actualRPE, targetRPE);
                    rpeText.append(' ');
                    rpeText.append(' ');
                    tempLength = rpeText.length();
                    rpeText.append(mContext.getString(R.string.target));
                    rpeText.append(':');
                    rpeText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorPrimary)),
                        tempLength, rpeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    rpeText.append(' ');
                    rpeText.append(String.valueOf(targetRPE));
                } else {
                    rpeDescription = rpeText.toString();
                }
                exerciseViewHolder.mRPEView.setText(rpeText);
                exerciseViewHolder.mRPEView.setContentDescription(rpeDescription);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (mListItemToDataMap.get(position)) {
            case TYPE_WEEK_SUBTITLE:
                return TYPE_WEEK_SUBTITLE;
            case TYPE_DAY_SUBTITLE:
                return TYPE_DAY_SUBTITLE;
            default:
                return TYPE_EXERCISE;
        }
    }

    @Override
    public int getItemCount() {
		return mListItemToDataMap == null ? 0 : mListItemToDataMap.size();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (newCursor != null) {
            setup();
        }
        notifyDataSetChanged();
    }
}
