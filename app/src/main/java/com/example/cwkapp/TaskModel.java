package com.example.cwkapp;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Exclude;

public class TaskModel {
        //store Firestore doc Id to edit or delete the task.

    private String task, priority, category, date;
    private int isChecked, sessionCounts;

    public TaskModel() {
    }

    @Exclude              //avoid storing TaskId inside the Firestore doc.
    private String id;


    public TaskModel(String task, String priority, String category, String date, int isChecked, int sessionCounts) {
        /*this.id = id;*/
        this.task = task;
        this.priority = priority;
        this.category = category;
        this.date = date;
        this.isChecked = isChecked;
        this.sessionCounts = sessionCounts;
    }

    public String getTask() {return task; }
    public void setTask(String task) {
        this.task = task;
    }
    public String getCategory() {return category;}
    public void setCategory(String category) {
        this.category = category;
    }
    public String getDate() {return date;}
    public void setDate(String date) {
        this.date = date;
    }
    public String getPriority() { return priority; }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public int getIsChecked() { return isChecked; }
    public void setIsChecked(int isChecked) {
        this.isChecked = isChecked;
    }
    public int getSessionCounts(){return sessionCounts; }
    public void setSessionCounts(int sessionCounts) {
        this.sessionCounts = sessionCounts;
    }


    @Exclude
    public String getId() {return id;}
    public void setTaskId(String taskId) {
        this.id = taskId;
    }
}

