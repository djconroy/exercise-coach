package org.insightcentre.coach;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static org.insightcentre.coach.data.ExerciseProgramContract.ExerciseCalendarEntry;

public class ExercisesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EXERCISE = 0;
    private static final int TYPE_SUBTITLE = -1;

    private Cursor mCursor;
    private List<Integer> mListItemToDataMap;
    private final Context mContext;
    private final OnClickHandler mClickHandler;
    private final View mEmptyView;

    public class SubtitleViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSubtitleView;

        public SubtitleViewHolder(View itemView) {
            super(itemView);
            mSubtitleView = (TextView) itemView.findViewById(R.id.subtitle);
        }
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final TextView mExerciseDescriptionView;
        public final TextView mSessionView;
        public final TextView mTimeView;
        public final TextView mRPEView;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            mExerciseDescriptionView = (TextView) itemView.findViewById(R.id.exercise_description);
            mSessionView = (TextView) itemView.findViewById(R.id.session);
            mTimeView = (TextView) itemView.findViewById(R.id.time);
            mRPEView = (TextView) itemView.findViewById(R.id.rpe);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(mListItemToDataMap.get(adapterPosition));
            mClickHandler.onClick(mCursor.getLong(HomeActivity.COL_EXERCISES_DATE),
                    mCursor.getInt(HomeActivity.COL_EXERCISES_PRESCRIBED),
                    mCursor.getInt(HomeActivity.COL_EXERCISES_SESSION));
        }
    }

    public interface OnClickHandler {
        void onClick(long date, int prescribed, int session);
    }

    public ExercisesAdapter(Context context, OnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }

    private void setup() {
        if (mCursor != null && mCursor.moveToFirst()) {
            long date = 0L;

            mListItemToDataMap = new ArrayList<>();
            do {
                if (mCursor.getLong(HomeActivity.COL_EXERCISES_DATE) > date) {
                    date = mCursor.getLong(HomeActivity.COL_EXERCISES_DATE);
                    mListItemToDataMap.add(TYPE_SUBTITLE);
                }
                mListItemToDataMap.add(mCursor.getPosition());
            } while (mCursor.moveToNext());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view;
            switch (viewType) {
                case TYPE_SUBTITLE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_subtitle, parent, false);
                    view.setFocusable(true);
                    return new SubtitleViewHolder(view);
                case TYPE_EXERCISE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_exercise, parent, false);
                    view.setFocusable(true);
                    return new ExerciseViewHolder(view);
                default:
                    throw new RuntimeException("Unknown view type");
            }
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_SUBTITLE:
                mCursor.moveToPosition(mListItemToDataMap.get(position + 1));
                SubtitleViewHolder subtitleViewHolder = (SubtitleViewHolder) holder;
                subtitleViewHolder.mSubtitleView.setText(
                        mContext.getString(R.string.date_and_level,
                                Utility.getFriendlyDayString(mContext,
                                        mCursor.getLong(HomeActivity.COL_EXERCISES_DATE)),
                                mCursor.getInt(HomeActivity.COL_EXERCISES_LEVEL)));
                break;
            case TYPE_EXERCISE:
                mCursor.moveToPosition(mListItemToDataMap.get(position));
                ExerciseViewHolder exerciseViewHolder = (ExerciseViewHolder) holder;

                String exerciseType;
                String exerciseDescription;
                switch (mCursor.getInt(HomeActivity.COL_EXERCISES_TYPE)) {
                    case ExerciseCalendarEntry.WALK:
                        exerciseType = mContext.getString(R.string.walk);
                        exerciseDescription = mContext.getString(R.string.a11y_type, exerciseType);
                        break;
                    case ExerciseCalendarEntry.STEP_UPS:
                        exerciseType = mContext.getString(R.string.step_ups);
                        exerciseDescription = mContext.getString(R.string.a11y_type, exerciseType);
                        break;
                    case ExerciseCalendarEntry.TYPE_NOT_RECORDED:
                    default:
                        exerciseType = "";
                        exerciseDescription = exerciseType;
                        break;
                }
                exerciseViewHolder.mExerciseDescriptionView.setText(exerciseType);
                exerciseViewHolder.mExerciseDescriptionView
                        .setContentDescription(exerciseDescription);

                boolean prescribed = false;
                switch (mCursor.getInt(HomeActivity.COL_EXERCISES_PRESCRIBED)) {
                    case ExerciseCalendarEntry.SESSION_PRESCRIBED:
                        prescribed = true;
                        break;
                    case ExerciseCalendarEntry.SESSION_NOT_PRESCRIBED:
                        prescribed = false;
                        break;
                }

                if (prescribed) {
                    exerciseViewHolder.mSessionView.setText(mContext.getString(R.string.session,
                            mCursor.getInt(HomeActivity.COL_EXERCISES_SESSION)));
                } else {
                    exerciseViewHolder.mSessionView.setText(mContext.getString(
                            R.string.extra_session,
                            mCursor.getInt(HomeActivity.COL_EXERCISES_SESSION)));
                }

                // Change the text size of the displayed exercise duration depending on the font
                // scale. This will ensure the exercise duration is displayed on only one line on
                // screens as small as 3.2" (320 x 480 mdpi) in QVGA resolution.
                float fontScale = mContext.getResources().getConfiguration().fontScale;
                if (fontScale > 1.15f) {
                    exerciseViewHolder.mTimeView.setTextSize(15);
                } else if (fontScale > 1.0f) {
                    exerciseViewHolder.mTimeView.setTextSize(16);
                }

                if (!prescribed || mCursor.getInt(HomeActivity.COL_EXERCISES_SUCCESS) ==
                        ExerciseCalendarEntry.SESSION_COMPLETED) {
                    exerciseViewHolder.mExerciseDescriptionView
                            .setCompoundDrawablesWithIntrinsicBounds(
                                    0, R.drawable.ic_bench_step_up_complete, 0, 0);
                } else {
                    exerciseViewHolder.mExerciseDescriptionView
                            .setCompoundDrawablesWithIntrinsicBounds(
                                    0, R.drawable.ic_bench_step_up, 0, 0);
                }

                int actualLength = mCursor.getInt(HomeActivity.COL_EXERCISES_ACTUAL_LENGTH);
                int actualMins = actualLength / 60;
                int actualSecs = actualLength % 60;

                int targetLength = mCursor.getInt(HomeActivity.COL_EXERCISES_TARGET_LENGTH);
                int targetMins = targetLength / 60;
                int targetSecs = targetLength % 60;

                SpannableStringBuilder timeText = new SpannableStringBuilder();
                timeText.append(String.valueOf(actualMins));
                timeText.setSpan(new RelativeSizeSpan(1.3f),
                        0, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                timeText.append(' ');
                int tempLength = timeText.length();
                timeText.append(mContext.getString(R.string.m));
                timeText.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(mContext, R.color.colorPrimary)),
                        tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                timeText.append(' ');
                tempLength = timeText.length();
                timeText.append(String.format("%02d", actualSecs));
                timeText.setSpan(new RelativeSizeSpan(1.3f),
                        tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                timeText.append(' ');
                tempLength = timeText.length();
                timeText.append(mContext.getString(R.string.s));
                timeText.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(mContext, R.color.colorPrimary)),
                        tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (prescribed) {
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append('/');
                    timeText.setSpan(new ForegroundColorSpan(
                                    ContextCompat.getColor(mContext, R.color.colorPrimary)),
                            tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append(String.valueOf(targetMins));
                    timeText.setSpan(new RelativeSizeSpan(1.3f),
                            tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append(mContext.getString(R.string.m));
                    timeText.setSpan(new ForegroundColorSpan(
                                    ContextCompat.getColor(mContext, R.color.colorPrimary)),
                            tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append(String.format("%02d", targetSecs));
                    timeText.setSpan(new RelativeSizeSpan(1.3f),
                            tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    timeText.append(' ');
                    tempLength = timeText.length();
                    timeText.append(mContext.getString(R.string.s));
                    timeText.setSpan(new ForegroundColorSpan(
                                    ContextCompat.getColor(mContext, R.color.colorPrimary)),
                            tempLength, timeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                exerciseViewHolder.mTimeView.setText(timeText);

                String timeDescription;
                if (prescribed) {
                    timeDescription = mContext.getString(R.string.a11y_time_and_target_time,
                            actualMins, actualSecs, targetMins, targetSecs);
                } else {
                    timeDescription =
                            mContext.getString(R.string.a11y_time, actualMins, actualSecs);
                }
                exerciseViewHolder.mTimeView.setContentDescription(timeDescription);

                int actualRPE = mCursor.getInt(HomeActivity.COL_EXERCISES_ACTUAL_RPE);
                int targetRPE = mCursor.getInt(HomeActivity.COL_EXERCISES_TARGET_RPE);

                String rpeDescription;
                SpannableStringBuilder rpeText = new SpannableStringBuilder();
                rpeText.append(mContext.getString(R.string.rpe_colon));
                rpeText.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(mContext, R.color.colorPrimary)),
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
                    rpeText.setSpan(new ForegroundColorSpan(
                                    ContextCompat.getColor(mContext, R.color.colorPrimary)),
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
            case TYPE_SUBTITLE:
                return TYPE_SUBTITLE;
            default:
                return TYPE_EXERCISE;
        }
    }

    @Override
    public int getItemCount() {
        if (null == mListItemToDataMap) return 0;
        return mListItemToDataMap.size();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (newCursor != null) {
            setup();
        } else {
            mListItemToDataMap = null;
        }
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
