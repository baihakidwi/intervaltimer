package com.blogspot.bihaika.android.intervaltimer;

import java.util.ArrayList;

/**
 * Created by Baihaki Dwi on 19/12/2017.
 */

public class Task {
    private long mTaskId;
    private String mTaskName;
    private long mTaskTime;
    private final ArrayList<Task> mTaskDetails;

    public Task() {
        mTaskId = 0;
        mTaskName = "";
        mTaskTime = 0;
        mTaskDetails = new ArrayList<>();
    }

    public long getTaskId() {
        return mTaskId;
    }

    public Task setTaskId(long taskId) {
        mTaskId = taskId;
        return this;
    }

    public String getTaskName() {
        return mTaskName;
    }

    public Task setTaskName(String taskName) {
        mTaskName = taskName;
        return this;
    }

    public long getTaskTime() {
        return mTaskTime;
    }

    public Task setTaskTime(long taskTime) {
        mTaskTime = taskTime;
        return this;
    }

    public ArrayList<Task> getTaskDetails() {
        return mTaskDetails;
    }

    public void addTaskDetail(Task task) {
        mTaskDetails.add(task);
    }

    public void removeTaskDetail(int position) {
        mTaskDetails.remove(position);
    }

    public String toString() {
        return mTaskName;
    }

    public String print() {
        StringBuilder print;
        print = new StringBuilder("<task>" +
                "<id>" + mTaskId +
                "</id>" +
                "<name>" + mTaskName +
                "</name>" +
                "<time>" + mTaskTime +
                "</time>" +
                "<detail>");
        for (Task t : mTaskDetails) {
            print.append(t.print());
        }
        print.append("</detail>" + "</task>");
        return print.toString();
    }
}
