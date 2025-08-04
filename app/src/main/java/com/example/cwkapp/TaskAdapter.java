package com.example.cwkapp;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private Button confirm, cancel;


    public TaskAdapter(TaskFragment taskFragment, ArrayList<TaskModel> taskList) {
        this.taskList = taskList;
        this.taskFragment = taskFragment;
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
        String id = task.getId(); //getting document ID
        vh.taskText.setText(task.getTask());
        vh.categoryText.setText(task.getCategory());
        vh.priorityText.setText(task.getPriority());
        vh.dateText.setText(task.getDate());
        vh.taskText.setChecked(Checked(task.getIsChecked()));

        vh.taskText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int p = vh.getAdapterPosition();
                if(b){
                        db.collection("Task").document(loggedUserId)
                           .collection("LoggedUser Task").document(id).update("isChecked", 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        taskList.remove(p);
                                        notifyItemRemoved(p);
                                        Toast.makeText(taskFragment.getContext(), "Task Completed: Check Completed Task List", Toast.LENGTH_SHORT).show();
                                    }
                                });
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

                        } else if(menuItem.getItemId() == R.id.popup_delete){
                            Dialog ConfirmationDialog = new Dialog(taskFragment.getContext());
                            ConfirmationDialog.setContentView(R.layout.confirmation_dialog);
                            ConfirmationDialog.setCancelable(false);

                            confirm = ConfirmationDialog.findViewById(R.id.OKBtn);
                            cancel = ConfirmationDialog.findViewById(R.id.CancelBtn);

                            confirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deleteTask(task, vh.getAdapterPosition());
                                    ConfirmationDialog.dismiss();
                                }
                            });
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ConfirmationDialog.dismiss();
                                }
                            });
                            ConfirmationDialog.show();

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
                collection("LoggedUser Task").document(id)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        taskList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(taskFragment.getContext(), "Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
