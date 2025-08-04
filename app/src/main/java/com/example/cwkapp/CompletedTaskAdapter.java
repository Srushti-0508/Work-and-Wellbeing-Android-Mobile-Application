package com.example.cwkapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CompletedTaskAdapter extends RecyclerView.Adapter<CompletedTaskAdapter.viewHolder>{
    private CompletedTaskFragment completedTaskFragment;
    private FirebaseUser LoggedUser;
    private ArrayList<TaskModel> CompletedTaskList;
    private FirebaseFirestore db;

   /** Displays completed tasks in a separate fragment for better task management and decluttering.
    * Automatically shows tasks marked as "checked" (completed) from Firestore.
    * Provides with the option to permanently delete completed tasks.
    * */

    public CompletedTaskAdapter(CompletedTaskFragment completedTaskFragment, ArrayList<TaskModel> CompletedTaskList){
        this.CompletedTaskList = CompletedTaskList;
        this.completedTaskFragment = completedTaskFragment;
    }


    @NonNull
    @Override
    public CompletedTaskAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_completed_task, parent, false);
        db = FirebaseFirestore.getInstance();
        return new CompletedTaskAdapter.viewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull CompletedTaskAdapter.viewHolder vh, int position){
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        String loggedUserId = LoggedUser.getUid();

        TaskModel task = CompletedTaskList.get(position);
        String id = task.getId();

        vh.completedTaskName.setText(task.getTask());
        vh.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCompletedTask(task, vh.getAdapterPosition());
            }
        });
    }

    public void deleteCompletedTask(TaskModel taskModel, int position){

        String loggedUserId = LoggedUser.getUid();
        taskModel = CompletedTaskList.get(position);
        String id = taskModel.getId();
        db.collection("Task").document(loggedUserId).
                collection("LoggedUser Task").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        CompletedTaskList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(completedTaskFragment.getContext(), "Completed Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public int getItemCount(){
        return CompletedTaskList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        TextView completedTaskName;
        ImageView done, delete;
        public viewHolder(@NonNull View view){
            super(view);
            completedTaskName = view.findViewById(R.id.taskNameTV);
            done = view.findViewById(R.id.doneIcon);
            delete = view.findViewById(R.id.deleteIcon);
        }
    }
}
