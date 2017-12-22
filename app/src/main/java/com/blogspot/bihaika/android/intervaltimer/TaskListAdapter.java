package com.blogspot.bihaika.android.intervaltimer;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by Baihaki Dwi on 19/12/2017.
 */

public class TaskListAdapter extends ArrayAdapter<Task> {
    private static final int HOUR = 3600000;
    private static final int MINUTE = 60000;
    private static final int SECOND = 1000;

    private MainActivity mMainActivity;
    private Task mTask;
    private final LayoutInflater mInflater;

    private CountDownTimer mCountDownTimer;
    private TextView mRunningTitle, mRunningTimer;

    private final ToneGenerator mToneGenerator;
    private final Vibrator mVibrator;

    private int mIndexRun;
    private long mRemainingTime;
    private boolean mSound, mVibrate, mRepeat;

    public TaskListAdapter(@NonNull Context context) {
        super(context, R.layout.item_listview_detail);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIndexRun = -1;
        mRemainingTime = 0;
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public TaskListAdapter setTask(Task task) {
        mTask = task;
        return this;
    }

    public TaskListAdapter setActivity(MainActivity activity) {
        mMainActivity = activity;
        return this;
    }

    public TaskListAdapter setSound(boolean sound) {
        mSound = sound;
        return this;
    }

    public TaskListAdapter setVibrate(boolean vibrate) {
        mVibrate = vibrate;
        return this;
    }

    public TaskListAdapter setRepeat(boolean repeat) {
        mRepeat = repeat;
        return this;
    }

    public TaskListAdapter setIndexRun(int indexRun) {
        mIndexRun = indexRun;
        return this;
    }

    public TaskListAdapter setRemainingTime(long remainingTime) {
        mRemainingTime = remainingTime;
        return this;
    }

    public void stop() {
        mIndexRun = -1;
        mRemainingTime = 0;
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        notifyDataSetChanged();
    }

    public void start() {
        if (mIndexRun <= -1 || mIndexRun >= getCount() - 1) {
            mIndexRun = 0;
        }
        mMainActivity.setListViewFocus(mIndexRun);
        notifyDataSetChanged();
    }

    public void pause() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    public int getIndexRun() {
        return mIndexRun;
    }

    public long getRemainingTime() {
        return mRemainingTime;
    }

    @Override
    public int getCount() {
        if (mTask == null) {
            return 0;
        }
        return mTask.getTaskDetails().size() + 1;
    }

    @Nullable
    @Override
    public Task getItem(int position) {
        if (position == getCount() - 1) {
            return null;
        }
        return mTask.getTaskDetails().get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (mIndexRun == position && mIndexRun != getCount() - 1) { //running item
            mMainActivity.setListViewFocus(mIndexRun);
            convertView = mInflater.inflate(R.layout.item_listview_running, null);
            mRunningTitle = convertView.findViewById(R.id.txv_listviewrun_title);
            mRunningTimer = convertView.findViewById(R.id.txv_listviewrun_time);
            setCountDownTimer(getItem(position));
        } else if (position == getCount() - 1) { //add timer item
            convertView = mInflater.inflate(R.layout.item_listview_addtimer, null);
            ImageButton btnAdd = convertView.findViewById(R.id.btn_listviewitem_add);
            if (mIndexRun == -1 || mIndexRun >= getCount()) {
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTimerDialog(null);
                    }
                });
            }
        } else { //standard item
            convertView = mInflater.inflate(R.layout.item_listview_detail, null);
            TextView txvTitle = convertView.findViewById(R.id.txv_listviewitem_title);
            TextView txvTime = convertView.findViewById(R.id.txv_listviewitem_time);
            ImageButton btnEdit = convertView.findViewById(R.id.btn_listviewitem_edit);
            ImageButton btnDelete = convertView.findViewById(R.id.btn_listviewitem_delete);

            txvTitle.setText(getItem(position).getTaskName());
            txvTime.setText(formatTime(getItem(position).getTaskTime()));
            if (mIndexRun == -1 || mIndexRun >= getCount()) {
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTimerDialog(getItem(position));
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTask.removeTaskDetail(position);
                        DataManager.getInstance(getContext()).save();
                        notifyDataSetChanged();
                    }
                });
            }
        }
        return convertView;
    }

    private void showTimerDialog(final Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout;
        if (task != null) {
            builder.setTitle(R.string.edit_dialog_title);
            layout = inflater.inflate(R.layout.dialog_edittimer_nobutton, null);
        } else {
            builder.setTitle(R.string.add_dialog_title);
            layout = inflater.inflate(R.layout.dialog_addtimer_nobutton, null);
        }

        final EditText editText = layout.findViewById(R.id.edt_dialogadd);
        final NumberPicker pickerHour = layout.findViewById(R.id.picker_dialogadd_hour);
        final NumberPicker pickerMinute = layout.findViewById(R.id.picker_dialogadd_minute);
        final NumberPicker pickerSecond = layout.findViewById(R.id.picker_dialogadd_second);
        setNumberPickerRange(pickerHour, 23);
        setNumberPickerRange(pickerMinute, 59);
        setNumberPickerRange(pickerSecond, 59);
        if (task != null) {
            editText.setText(task.getTaskName());
            long hour, minute, second;
            hour = (task.getTaskTime() / HOUR) % 24;
            minute = (task.getTaskTime() / MINUTE) % 60;
            second = (task.getTaskTime() / SECOND) % 60;
            pickerHour.setValue((int) hour);
            pickerMinute.setValue((int) minute);
            pickerSecond.setValue((int) second);
        }

        builder.setView(layout);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String timerName = editText.getText().toString();
                if (timerName.equals("")) {
                    timerName = getContext().getString(R.string.timername_dialog_hint);
                }
                int hour = pickerHour.getValue();
                int minute = pickerMinute.getValue();
                int second = pickerSecond.getValue();
                long time = second * SECOND
                        + minute * MINUTE
                        + hour * HOUR;
                if (time == 0) {
                    time = 20;
                }
                if (task != null) {
                    task.setTaskName(timerName)
                            .setTaskTime(time);
                    DataManager.getInstance(getContext()).save();
                } else {
                    Task task = new Task()
                            .setTaskId(System.currentTimeMillis())
                            .setTaskName(timerName)
                            .setTaskTime(time);

                    mTask.addTaskDetail(task);
                    DataManager.getInstance(getContext()).save();
                }

                notifyDataSetChanged();
            }
        };
        if (task != null) {
            builder.setPositiveButton(R.string.edit_dialog_button, listener);
        } else {
            builder.setPositiveButton(R.string.add_dialog_button, listener);
        }
        builder.create().show();

    }

    private void setCountDownTimer(final Task task) {
        long time;
        if (mRemainingTime > 0) {
            time = mRemainingTime;
        } else {
            time = task.getTaskTime();
        }
        mRunningTitle.setText(task.getTaskName());
        mRunningTimer.setText(formatTime(time));

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(time, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                mRunningTimer.setText(formatTime(millisUntilFinished));
                mRemainingTime = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                mRemainingTime = 0;
                if (mVibrate) {
                    mVibrator.vibrate(150);
                }
                if (mSound) {
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 150);
                }
                mIndexRun++;
                if (mIndexRun == getCount() - 1) {
                    if (mRepeat) {
                        mIndexRun = 0;
                        mMainActivity.setListViewFocus(mIndexRun);
                    } else {
                        mMainActivity.stop();
                        mRemainingTime = 0;
                    }
                }
                notifyDataSetChanged();
            }
        };
        switch (mMainActivity.getState()) {
            case RUN:
                mCountDownTimer.start();

                break;
        }
    }

    private void setNumberPickerRange(NumberPicker picker, int max) {
        picker.setMinValue(0);
        picker.setMaxValue(max);
    }

    private String formatTime(long millis) {
        long hour, minute, second, tenth;
        hour = (millis / HOUR) % 24;
        minute = (millis / MINUTE) % 60;
        second = (millis / SECOND) % 60;
        tenth = (millis / 100) % 10;

        String shour = hour < 10 ? "0" + hour : "" + hour;
        String sminute = minute < 10 ? ":0" + minute : ":" + minute;
        String ssecond = second < 10 ? ":0" + second : ":" + second;

        return shour + sminute + ssecond + "." + tenth;
    }

}