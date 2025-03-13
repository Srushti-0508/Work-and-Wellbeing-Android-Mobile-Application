package com.example.cwkapp;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.viewHolder> {
   private Context context;
    private TaskFragment taskFragment;
    private FirebaseUser LoggedUser;
    private ArrayList<TaskModel> taskList;
    private FirebaseFirestore db;

    public TaskAdapter(TaskFragment taskFragment/*Context context*/, ArrayList<TaskModel> taskList) {
        this.taskList = taskList;
        this.taskFragment = taskFragment;
       // this.context = context;
    }

    public void updateTaskAdapter(ArrayList<TaskModel> newTaskList){
        this.taskList.clear();
        this.taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_view, parent, false);
        db = FirebaseFirestore.getInstance();
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder vh, int position){
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        String loggedUserId = LoggedUser.getUid();
        TaskModel task = taskList.get(position);
        String id = task.getId();
        vh.taskText.setText(task.getTask());
        //Log.d("Adapter", "Binding Task: " + task.getTask());
        vh.categoryText.setText(task.getCategory());
        vh.priorityText.setText(task.getPriority());
        vh.dateText.setText(task.getDate());
        vh.taskText.setChecked(Checked(task.getIsChecked()));


        vh.taskText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                        db.collection("Task").document(loggedUserId)
                           .collection("LoggedUser Task").document(id).update("isChecked", 1);
                }else{
                    db.collection("Task").document(loggedUserId)
                            .collection("LoggedUser Task").document(id).update("isChecked",0);
                }
            }
        });

        vh.more_menu_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popMenu = new PopupMenu(taskFragment.getContext(), view);
                popMenu.getMenuInflater().inflate(R.menu.pop_up_menu, popMenu.getMenu());
                popMenu.show();
                popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.popup_edit) {
                            taskFragment.AddTask(task);
                           /* TaskModel ediTask = taskList.get(vh.getAdapterPosition());
                            if (ediTask == null) {
                                Log.e("Adapter", "Task object is NULL at position: " + vh.getAdapterPosition());
                            }*/
                            // taskFragment.AddTask(taskList.get(vh.getAdapterPosition()));
                   /* int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        taskFragment.AddTask(taskList.get(pos));*/
                            //taskFragment.AddTask(ediTask);

                        }else if(menuItem.getItemId() == R.id.popup_delete){
                            deleteTask(task, vh.getAdapterPosition());

                        }
                        return false;
                    }
                });
            }
        });

    }
    private boolean Checked(int isChecked){
        return isChecked != 0;
    }

    public void deleteTask(TaskModel taskModel, int position){
        String loggedUserId = LoggedUser.getUid();
        taskModel = taskList.get(position);
        String id = taskModel.getId();
        db.collection("Task").document(loggedUserId).
                collection("LoggedUser Task").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        taskList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(taskFragment.getContext(), "Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                });


    }
    /*public void updateAdapterList(ArrayList<TaskModel> newTaskList){
        taskList.clear();
        taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }*/

    @Override
    public int getItemCount(){
        return taskList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        private ImageView more_menu_img;
        private TextView categoryText, priorityText, dateText;
        private CheckBox taskText;
        public viewHolder(@NonNull View view){
            super(view);
            more_menu_img =view.findViewById(R.id.moreMenu);
            categoryText = view.findViewById(R.id.CTypeTextView);
            priorityText = view.findViewById(R.id.PLevelTextView);
            dateText = view.findViewById(R.id.DateTextView);
            taskText = view.findViewById(R.id.checkbox);
        }
    }
}
