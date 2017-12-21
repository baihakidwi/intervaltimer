package com.blogspot.bihaika.android.intervaltimer;

import android.content.Context;
import android.content.SharedPreferences;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Baihaki Dwi on 19/12/2017.
 */

public class DataManager {
    private static DataManager sDataManager;
    private final Context mAppContext;
    private final ArrayList<Task> mTaskList;
    private final SharedPreferences mSharedPreferences;

    private DataManager(Context context) {
        mAppContext = context.getApplicationContext();
        mSharedPreferences = mAppContext.getSharedPreferences(
                mAppContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mTaskList = new ArrayList<>();
        load();
    }

    public static DataManager getInstance(Context context) {
        if (sDataManager == null) {
            sDataManager = new DataManager(context);
        }
        return sDataManager;
    }

    public ArrayList<Task> getTaskList() {
        return mTaskList;
    }

    public Task getTask(int index) {
        return mTaskList.get(index);
    }

    private void addTask(Task task) {
        mTaskList.add(task);
        save();
    }

    public void createTask(String name) {
        long id = System.currentTimeMillis();
        boolean exist;
        do {
            exist = false;
            for (Task t : mTaskList) {
                if (id == t.getTaskId()) {
                    exist = true;
                    id++;
                    break;
                }
            }
        } while (exist);
        Task task = new Task().setTaskId(id)
                .setTaskName(name);
        addTask(task);
    }

    public void removeTask(int index) {
        mTaskList.remove(index);
        save();
    }

    public void save() {
        StringBuilder data = new StringBuilder("<data>");
        for (Task t : mTaskList) {
            data.append(t.print());
        }
        data.append("</data>");

        File file = new File(mAppContext.getFilesDir(), mAppContext.getString(R.string.save_file));
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(file));
            writer.println(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void load() {
        File file = new File(mAppContext.getFilesDir(), mAppContext.getString(R.string.save_file));
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new FileReader(file));
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("data")) {
                mTaskList.clear();
                eventType = parser.next();
                long id = 0, time = 0;
                String name = "";
                Stack<Task> stack = new Stack<>();
                while ((eventType != XmlPullParser.END_TAG || !parser.getName().equals("data"))
                        && eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("task")) {
                            Task task = new Task();
                            stack.add(task);
                        } else if (parser.getName().equals("id")) {
                            parser.next();
                            id = Long.parseLong(parser.getText());
                        } else if (parser.getName().equals("name")) {
                            parser.next();
                            if (parser.getText() == null) {
                                name = "";
                            } else {
                                name = parser.getText();
                            }
                        } else if (parser.getName().equals("time")) {
                            parser.next();
                            time = Long.parseLong(parser.getText());
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (parser.getName().equals("task")) {
                            Task task = stack.pop();
                            if (stack.isEmpty()) {
                                mTaskList.add(task);
                            } else {
                                stack.get(stack.size() - 1).addTaskDetail(task);
                            }
                        } else if (parser.getName().equals("id")) {
                            stack.get(stack.size() - 1).setTaskId(id);
                        } else if (parser.getName().equals("name")) {
                            stack.get(stack.size() - 1).setTaskName(name);
                        } else if (parser.getName().equals("time")) {
                            stack.get(stack.size() - 1).setTaskTime(time);
                        }
                    }
                    eventType = parser.next();
                }
            }
        } catch (XmlPullParserException | NumberFormatException | IOException e) {
        }
    }

    public void setSoundState(boolean soundState) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mAppContext.getString(R.string.sound_preferences), soundState);
        editor.apply();
    }

    public boolean getSoundState() {
        return mSharedPreferences.getBoolean(
                mAppContext.getString(R.string.sound_preferences), true);
    }

    public void setVibrateState(boolean vibrateState) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mAppContext.getString(R.string.vibrate_preferences), vibrateState);
        editor.apply();
    }

    public boolean getVibrateState() {
        return mSharedPreferences.getBoolean(
                mAppContext.getString(R.string.vibrate_preferences), false);
    }

    public void setRepeatState(boolean repeatState) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mAppContext.getString(R.string.repeat_preferences), repeatState);
        editor.apply();
    }

    public boolean getRepeatState() {
        return mSharedPreferences.getBoolean(
                mAppContext.getString(R.string.repeat_preferences), false);
    }
}
