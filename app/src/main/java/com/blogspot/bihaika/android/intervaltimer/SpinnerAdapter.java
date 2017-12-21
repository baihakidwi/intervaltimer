package com.blogspot.bihaika.android.intervaltimer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Baihaki Dwi on 21/12/2017.
 */

class SpinnerAdapter extends ArrayAdapter<Task> {

    private final ArrayList<Task> mTaskList;
    private final LayoutInflater mLayoutInflater;

    public SpinnerAdapter(@NonNull Context context) {
        super(context, R.layout.item_spinner, R.id.txv_spinneritem_title);
        mTaskList = DataManager.getInstance(context).getTaskList();
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mTaskList.size() + 2;
    }

    @Nullable
    @Override
    public Task getItem(int position) {
        if (position == 0) {
            return new Task().setTaskName("");
        } else if (position == getCount() - 1) {
            return new Task().setTaskName(getContext().getString(R.string.create_dialog_title));
        } else {
            return mTaskList.get(position - 1);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = mLayoutInflater.inflate(R.layout.item_spinner, null);
        TextView title = convertView.findViewById(R.id.txv_spinneritem_title);
        title.setText(getItem(position).getTaskName());
        return convertView;
    }
}
