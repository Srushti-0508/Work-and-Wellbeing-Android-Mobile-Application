package com.example.cwkapp;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public class HabitModel {
    @Exclude                //avoid storing TaskId inside the Firestore doc.
    private String id;
    private String habit, reminderTime, repeatTime;
    private List<String> completionDate;

public HabitModel(){

}
    private HabitModel(String id, String habit, String reminderTime, String repeatTime, List<String>completionDate){
        this.id= id;
        this.habit = habit;
        this.reminderTime = reminderTime;
        this.repeatTime = repeatTime;
        this.completionDate = completionDate;
    }

    public String getId() {
        return id;
    }
    public void setHabitId(String habitId) {
        this.id = habitId;
    }

    public String getHabit() {
        return habit;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public String getRepeatTime() {
        return repeatTime;
    }

    public List<String> getCompletionDate() {
        return completionDate;
    }
}
