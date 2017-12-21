package com.blogspot.bihaika.android.intervaltimer;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    public enum IntervalTimerState {STOP, RUN, PAUSED}

    private Spinner mSpnrTaskList;
    private SpinnerAdapter mSpinnerAdapter;
    private ListView mLsvTaskDetails;
    private TaskListAdapter mTaskListAdapter;
    private ImageButton mBtnDelete;
    private ImageButton mBtnStop;
    private ImageButton mBtnStart;
    private int mSpinnerIndex;
    private int mListViewIndex;
    private long mRemainingTime;
    private boolean mSound;
    private boolean mVibrate;
    private boolean mRepeat;
    private IntervalTimerState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariables(savedInstanceState);
        initView();
        initSpinner();
        initButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sound:
                mSound = !item.isChecked();
                item.setChecked(mSound);
                DataManager.getInstance(this).setSoundState(mSound);
                if (mTaskListAdapter != null) {
                    mTaskListAdapter.setSound(mSound);
                }
                break;
            case R.id.menu_vibrate:
                mVibrate = !item.isChecked();
                item.setChecked(mVibrate);
                DataManager.getInstance(this).setVibrateState(mVibrate);
                if (mTaskListAdapter != null) {
                    mTaskListAdapter.setVibrate(mVibrate);
                }
                break;
            case R.id.menu_repeat:
                mRepeat = !item.isChecked();
                item.setChecked(mRepeat);
                DataManager.getInstance(this).setRepeatState(mRepeat);
                if (mTaskListAdapter != null) {
                    mTaskListAdapter.setRepeat(mRepeat);
                }
                break;
        }
//        return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem menuSound = menu.findItem(R.id.menu_sound);
        MenuItem menuVibrate = menu.findItem(R.id.menu_vibrate);
        MenuItem menuRepeat = menu.findItem(R.id.menu_repeat);
        menuSound.setChecked(mSound);
        menuVibrate.setChecked(mVibrate);
        menuRepeat.setChecked(mRepeat);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        switch (mState) {
            case RUN:
            case PAUSED:
                mRemainingTime = mTaskListAdapter.getRemainingTime();
                mListViewIndex = mTaskListAdapter.getIndexRun();
                mTaskListAdapter.stop();
                break;
            case STOP:
                mRemainingTime = 0;
                mListViewIndex = -1;
        }

        outState.putInt(getString(R.string.param_spinnerindex), mSpinnerIndex);
        outState.putInt(getString(R.string.param_listviewindex), mListViewIndex);
        outState.putLong(getString(R.string.param_remainingtime), mRemainingTime);
        outState.putSerializable(getString(R.string.param_state), mState);
    }

    private void initVariables(Bundle savedInstanceState) {

        mSound = DataManager.getInstance(this).getSoundState();
        mVibrate = DataManager.getInstance(this).getVibrateState();
        mRepeat = DataManager.getInstance(this).getRepeatState();

        if (savedInstanceState == null) {
            mSpinnerIndex = 0;
            mListViewIndex = -1;
            mRemainingTime = 0;
            mState = IntervalTimerState.STOP;
        } else {
            mSpinnerIndex = savedInstanceState.getInt(getString(R.string.param_spinnerindex), 0);
            mListViewIndex = savedInstanceState.getInt(getString(R.string.param_listviewindex), -1);
            mRemainingTime = savedInstanceState.getLong(getString(R.string.param_remainingtime), 0);
            mState = (IntervalTimerState) savedInstanceState.getSerializable(getString(R.string.param_state));
        }

        mSpinnerAdapter = new SpinnerAdapter(this
        );

        mTaskListAdapter = new TaskListAdapter(MainActivity.this
        )
                .setActivity(this)
                .setSound(mSound)
                .setVibrate(mVibrate)
                .setRepeat(mRepeat);

    }

    private void initView() {

        setContentView(R.layout.activity_main);
        mSpnrTaskList = findViewById(R.id.spnr_main_tasklist);
        mLsvTaskDetails = findViewById(R.id.lsv_main_taskdetails);
        mBtnDelete = findViewById(R.id.btn_main_delete);
        mBtnStop = findViewById(R.id.btn_main_reset);
        mBtnStart = findViewById(R.id.btn_main_start);
        setBtnStartBackground();
    }

    private void initSpinner() {
        mSpnrTaskList.setAdapter(mSpinnerAdapter);
        mSpnrTaskList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSpinnerIndex = position;
                if (mSpinnerIndex == 0) {
                    mLsvTaskDetails.setAdapter(null);
                } else if (mSpinnerIndex == mSpinnerAdapter.getCount() - 1) {
                    createTask();
                    mSpnrTaskList.setSelection(0);
                } else {
                    Task task = DataManager.getInstance(MainActivity.this).getTask(position - 1);
                    if (mTaskListAdapter == null) {
                        mTaskListAdapter = new TaskListAdapter(MainActivity.this
                        )
                                .setActivity(MainActivity.this)
                                .setSound(mSound)
                                .setVibrate(mVibrate)
                                .setRepeat(mRepeat);
                    }
                    mTaskListAdapter.stop();
                    if (mState != IntervalTimerState.STOP) {
                        setTaskListAdapter();
                    }
                    mTaskListAdapter.setTask(task);
                    mTaskListAdapter.notifyDataSetChanged();
                    mLsvTaskDetails.setAdapter(mTaskListAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void initButtons() {

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaskListAdapter != null && mTaskListAdapter.getCount() > 1) {
                    start();
                }
            }
        });

    }

    private void setTaskListAdapter() {
        if (mTaskListAdapter != null) {
            mTaskListAdapter.setIndexRun(mListViewIndex)
                    .setRemainingTime(mRemainingTime);
        }

    }

    private void createTask() {
        final Dialog dialog = new Dialog(this, R.style.Dialog);
        dialog.setContentView(R.layout.dialog_createtask);
        dialog.setTitle(R.string.create_dialog_title);
        final EditText edtTaskName = dialog.findViewById(R.id.edt_dialogcreate);
        Button btnCreate = dialog.findViewById(R.id.btn_dialogcreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                String taskName = edtTaskName.getText().toString();
                if (!taskName.equals("")) {
                    DataManager.getInstance(MainActivity.this).createTask(taskName);
                    mSpnrTaskList.setSelection(
                            DataManager.getInstance(MainActivity.this).getTaskList().size());
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void delete() {

        switch (mState) {
            case STOP:
                int index = mSpnrTaskList.getSelectedItemPosition();
                if (index != 0 && index != mSpinnerAdapter.getCount() - 1) {
                    DataManager.getInstance(MainActivity.this).removeTask(index - 1);
                    mSpinnerAdapter.notifyDataSetChanged();
                    mSpnrTaskList.setSelection(0);
                }
                break;
        }

    }

    private void start() {

        if (mSpinnerIndex != 0 && mSpinnerIndex != mSpinnerAdapter.getCount() - 1) {
            switch (mState) {
                case RUN:
                    pause();
                    break;
                case STOP:
                case PAUSED:
                    mState = IntervalTimerState.RUN;
                    mTaskListAdapter.start();
                    break;
            }
            setBtnStartBackground();
        }

    }

    public void stop() {

        mState = IntervalTimerState.STOP;
        if (mTaskListAdapter != null) {
            mTaskListAdapter.stop();
        }
        mRemainingTime = 0;
        mListViewIndex = -1;
        mLsvTaskDetails.smoothScrollToPosition(0);
        setBtnStartBackground();

    }

    private void pause() {

        if (mTaskListAdapter != null) {
            mState = IntervalTimerState.PAUSED;
            mTaskListAdapter.pause();
            mListViewIndex = mTaskListAdapter.getIndexRun();
            mRemainingTime = mTaskListAdapter.getRemainingTime();
        }
        setBtnStartBackground();

    }

    private void setBtnStartBackground() {

        switch (mState) {
            case RUN:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBtnStart.setBackground(getDrawable(R.drawable.pause));
                } else {
                    mBtnStart.setBackground(getResources().getDrawable(R.drawable.pause));
                }
                break;
            case STOP:
            case PAUSED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBtnStart.setBackground(getDrawable(R.drawable.start));
                } else {
                    mBtnStart.setBackground(getResources().getDrawable(R.drawable.start));
                }
                break;
        }

        mBtnStart.invalidate();
    }

    public void setListViewFocus(int position) {
        mLsvTaskDetails.smoothScrollToPosition(position);
    }

    public IntervalTimerState getState() {
        return mState;
    }
}
