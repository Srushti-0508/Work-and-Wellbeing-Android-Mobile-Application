package com.example.cwkapp;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HabitFragment extends Fragment {
    private FirebaseFirestore firestoredb;
    private FirebaseAuth Auth;
    private FirebaseUser LoggedUser;
    private String time, currentDate, repeatText;

    private FloatingActionButton HabitFab;
    private EditText habit_text;
    //private TimePicker timePicker;
    private TextView timePickerTextView, successMsg;
    private MaterialButtonToggleGroup repeatBtnGrp;
    private Button saveBtn;

    private HabitAdapter habitAdapter;
    private ArrayList<HabitModel> habitList;



    public HabitFragment() {
        // Required empty public constructor
    }
    /**
     * Manages user habits in a similar way to the Task, with daily tracking functionality.
     *
     *  Allows users to create and edit daily habits using a dialog that supports pre-filled data for editing.
     * Automatically resets all habit checkboxes to "unchecked" each day.
     * When a user marks a habit as completed for the day, the completion is logged by date
     * The HomeFragment displays the total number of completed habits for the current day.
     * If all habits for the day are completed, it displays a motivational completion message.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_habit, container, false);
        RecyclerView HabitRecyclerView = root.findViewById(R.id.habitrecyclerView);
        HabitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DisplayHabit(HabitRecyclerView);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        successMsg = view.findViewById(R.id.habitTextView);
        HabitFab = view.findViewById(R.id.floatingActionButton);
        if (HabitFab != null) {
            HabitFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AddHabit(null); //to display empty dialog box to add new Habit.
                }
            });
        } else {
            Toast.makeText(getActivity(), "Error Opening Fab", Toast.LENGTH_SHORT).show();
        }
    }

    public void AddHabit(@Nullable HabitModel editHabit){
        final Dialog AddHabitDialog = new Dialog(getContext());
        AddHabitDialog.setContentView(R.layout.add_habit);
        AddHabitDialog.setCancelable(true);
        habit_text = AddHabitDialog.findViewById(R.id.textInputEditText);
        timePickerTextView = AddHabitDialog.findViewById(R.id.timePickerTextView);
        timePickerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar selectTime = Calendar.getInstance();
                int hour = selectTime.get(Calendar.HOUR);
                int min = selectTime.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int h, int m) {
                        time = String.format("%02d:%02d",h, m);
                        timePickerTextView.setText(time);
                    }
                }, hour, min,true);
                timePickerDialog.show();
            }
        });

        repeatBtnGrp = AddHabitDialog.findViewById(R.id.RepeatBtn);
        saveBtn = AddHabitDialog.findViewById(R.id.saveBtn);

        if(editHabit!=null){
            habit_text.setText(editHabit.getHabit());
            Log.d("Firestore","getting the repeat time: "+ editHabit.getRepeatTime());
            if(editHabit.getReminderTime()!=null){
                timePickerTextView.setText(editHabit.getReminderTime());
            }else{
                timePickerTextView.setText("Select Time");
            }

            String getRepeatText = editHabit.getRepeatTime();
            if(getRepeatText !=null){
                switch(getRepeatText){
                    case("Never"):
                        repeatBtnGrp.check(R.id.RepeatBtn1);
                        break;
                    case("Daily"):
                        repeatBtnGrp.check(R.id.RepeatBtn2);
                        break;
                    case("Weekly"):
                        repeatBtnGrp.check(R.id.RepeatBtn3);
                        break;
                }
            }else{
                repeatBtnGrp.clearChecked();
            }
            saveBtn.setText("Update Habit");
        }
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String habitText = habit_text.getText().toString();
                String reminderTime = time;
              //  String RepeatTimeText = dropDown_list.getText().toString();
                int selectedRepeatBtnId = repeatBtnGrp.getCheckedButtonId();

                if(selectedRepeatBtnId == R.id.RepeatBtn1){
                    repeatText = "Never";
                }else if(selectedRepeatBtnId == R.id.RepeatBtn2){
                    repeatText = "Daily";
                }
                else if(selectedRepeatBtnId == R.id.RepeatBtn3){
                    repeatText = "Weekly";
                }

                currentDate = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());
                List<String> completionDate = new ArrayList<>();

                if(habitText.isEmpty()){
                    Toast.makeText(getContext(), "Habit name is required", Toast.LENGTH_SHORT).show();
                }else{

                    if(editHabit == null){
                        HabitModel habitModel = new HabitModel(habitText, reminderTime, repeatText, completionDate, currentDate);
                        SaveHabit(habitModel);
                        AddHabitDialog.dismiss();
                    }else{
                        editHabit.setHabit(habitText);
                        editHabit.setReminderTime(reminderTime);
                        editHabit.setRepeatTime(repeatText);
                        Log.d("EditHabit", "Editing Habit with ID: " + editHabit.getId());
                        EditHabit(editHabit.getId(), editHabit);
                        AddHabitDialog.dismiss();
                    }

                }
            }
        });
        AddHabitDialog.show();
    }

    private void SaveHabit(HabitModel saveHabit) {
        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            saveHabit.setHabitId(null);
            saveHabit.setCompletionDate(new ArrayList<>());
            firestoredb.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit")
                    .add(saveHabit).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Habit Saved Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void EditHabit(String id, HabitModel updateHabit) {

        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit").document(id)
                    .set(updateHabit)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> t) {
                            if(t.isSuccessful()){
                             Toast.makeText(getContext(), "Updated Habit Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), t.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void DisplayHabit(RecyclerView HabitRecyclerView) {

        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        currentDate = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if(snapshot!=null){
                                int totalHabit = 0;
                                int isCheckedCount =0;
                                habitList = new ArrayList<>();
                                for(DocumentSnapshot document: snapshot.getDocuments()){
                                    totalHabit++;
                                    String id = document.getId();
                                    HabitModel habit = document.toObject(HabitModel.class);
                                    habit.setHabitId(id);
                                    List<String>checkCompletedDate = habit.getCompletionDate(); //storing completion date in new list
                                    if(checkCompletedDate !=null && checkCompletedDate.contains(currentDate)){
                                        isCheckedCount++;
                                    }

                                    habitList.add(habit);
                                    habitAdapter = new HabitAdapter(HabitFragment.this, habitList);
                                    HabitRecyclerView.setAdapter(habitAdapter);
                                    habitAdapter.notifyDataSetChanged();

                                }
                                if(isCheckedCount == totalHabit && totalHabit > 0){  //display success message on completion of all habits.
                                    //also set the visibility as gone if no habit is created yet.
                                    successMsg.setVisibility(View.VISIBLE);
                                }else{
                                    successMsg.setVisibility(View.GONE);
                                }

                            }
                        }
                    });
        }
    }

}