package com.example.cwkapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.viewHolder>{

private HabitFragment habitFragment;
private FirebaseUser LoggedUser;
private FirebaseFirestore db;
private ArrayList<HabitModel> habitList;
private String currentDate;

    public HabitAdapter(HabitFragment habitFragment, ArrayList<HabitModel> habitList) {
        this.habitFragment = habitFragment;
        this.habitList = habitList;
        currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
    }

    @NonNull
    @Override
    public HabitAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_view, parent, false);
        db = FirebaseFirestore.getInstance();
        return new viewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull viewHolder vh, int position) {
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        String loggedUserId = LoggedUser.getUid();
        HabitModel habit = habitList.get(position);
        String id = habit.getId();
        vh.habitText.setText(habit.getHabit());

        //method for checking the habit, and adding the date to the db.
        //Similarly to remove, Just like in task we are changing the values, here we need to store the date.
        vh.more_menu_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popMenu = new PopupMenu(habitFragment.getContext(), view);
                popMenu.getMenuInflater().inflate(R.menu.pop_up_menu, popMenu.getMenu());
                popMenu.show();
               popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                   @Override
                   public boolean onMenuItemClick(MenuItem menuItem) {
                       if(menuItem.getItemId() == R.id.popup_edit){
                           Log.d("DEBUG","Habit to Edit: "+ habit.getHabit() + " | "+habit.getRepeatTime()+" | "+habit.getReminderTime());
                           habitFragment.AddHabit(habit);
                       }else if(menuItem.getItemId() ==R.id.popup_delete){
                           deleteHabit(habit, vh.getAdapterPosition());
                       }
                       return true;
                   }
               });
            }
        });

    }
    private void deleteHabit(HabitModel habitModel, int positon){
        String loggedUserId = LoggedUser.getUid();
        habitModel = habitList.get(positon);
        String id = habitModel.getId();
        db.collection("Habit").document(loggedUserId).
                collection("LoggedUser Habit").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        habitList.remove(positon);
                        notifyItemRemoved(positon);
                        Toast.makeText(habitFragment.getContext(), "Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return habitList.size();
    }
    public static class viewHolder extends RecyclerView.ViewHolder{
        private ImageView more_menu_img;
        private CheckBox habitText;
        public viewHolder(@NonNull View view){
            super(view);
            more_menu_img = view.findViewById(R.id.HabitmoreMenu);
            habitText =view.findViewById(R.id.Habitcheckbox);
        }

    }

}

