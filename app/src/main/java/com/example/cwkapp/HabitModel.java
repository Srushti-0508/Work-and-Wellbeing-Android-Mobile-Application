package com.example.cwkapp;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public class HabitModel {


    private String habit, reminderTime, repeatTime, todayDate;
    private List<String> completionDate;
    //private boolean isChecked;

    @Exclude                //avoid storing HabitId inside the Firestore doc.
    private String id;

    public HabitModel(){ }

    public HabitModel(String habit, String reminderTime, String repeatTime, List<String>completionDate, String todayDate){
        /*this.id= id;*/
        this.habit = habit;
        this.reminderTime = reminderTime;
        this.repeatTime = repeatTime;
        this.completionDate = completionDate;
        this.todayDate = todayDate;
        //this.isChecked = false;
    }



    public String getHabit() {
        return habit;
    }
    public void setHabit(String habit) {
        this.habit = habit;
    }

    public String getReminderTime() {
        return reminderTime;
    }
    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getRepeatTime() {
        return repeatTime;
    }
    public void setRepeatTime(String repeatTime) {
        this.repeatTime = repeatTime;
    }

    public String getTodayDate() {
        return todayDate;
    }
    public void setTodayDateDate(String todayDate) {
        this.todayDate = todayDate;
    }

    public List<String> getCompletionDate() {
        return completionDate;
    }
    public void setCompletionDate(List<String> completionDate) {
        this.completionDate = completionDate;
    }

    /*public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }*/

    @Exclude
    public String getId() {
        return id;
    }
    public void setHabitId(String habitId) {
        this.id = habitId;
    }




}
