package com.example.cwkapp;

import android.app.Dialog;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HabitFragment extends Fragment {
    private FirebaseFirestore firestoredb;
    private FirebaseAuth Auth;
    private FirebaseUser LoggedUser;
    private String time, currentDate;
    private String[] repeat_list = {"Never", "Daily", "Weekly"};
    private ArrayAdapter<String> adapterRepeatList;
    private TextInputLayout dropDown;
    private AutoCompleteTextView dropDown_list;

    private FloatingActionButton HabitFab;
    private EditText habit_text;
    private TimePicker timePicker;
    private Button saveBtn;

    private HabitAdapter habitAdapter;
    private ArrayList<HabitModel> habitList;

    private TextView timeTV;

    public HabitFragment() {
        // Required empty public constructor
    }

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
        timeTV = AddHabitDialog.findViewById(R.id.timerTextView);
        habit_text = AddHabitDialog.findViewById(R.id.textInputEditText);
        timePicker = AddHabitDialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int min) {
                timeTV.setText(hour + ":" + min);
                time = hour+":"+min;
            }
        });

        saveBtn = AddHabitDialog.findViewById(R.id.saveBtn);

        dropDown = AddHabitDialog.findViewById(R.id.dropdown);
        dropDown_list =AddHabitDialog.findViewById(R.id.autocomplete_view);
        adapterRepeatList = new ArrayAdapter<String>(getContext(), R.layout.dropdown_category_list, repeat_list);

        dropDown_list.setAdapter(adapterRepeatList);
        dropDown_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String repeat_list = adapterView.getItemAtPosition(i).toString();
            }
        });

        if(editHabit!=null){
            habit_text.setText(editHabit.getHabit());
            Log.d("Firestore","getting the repeat time: "+editHabit.getRepeatTime());
            dropDown_list.setText(editHabit.getRepeatTime(),false);
            saveBtn.setText("Update Habit");
            if(editHabit.getReminderTime()!=null){
                String[] parts = editHabit.getReminderTime().split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                timePicker.post(()->{
                    timePicker.setHour(hour);
                    timePicker.setMinute(minute);
                });

            }


        }
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String habitText = habit_text.getText().toString();
                String RepeatTimeText = dropDown_list.getText().toString();

                if(habitText.isEmpty()){
                    Toast.makeText(getContext(), "Habit name is required", Toast.LENGTH_SHORT).show();
                }else{
                    if(editHabit == null){
                        SaveHabit(habitText, RepeatTimeText, time);
                    }else{
                        Log.d("EditHabit", "Editing Habit with ID: " + editHabit.getId());
                        EditHabit(editHabit.getId(), habitText, RepeatTimeText, time);
                    }

                }
            }
        });
        AddHabitDialog.show();
    }


    private void SaveHabit(String habitText, String RepeatTimeText, String reminderTime) {
        firestoredb = FirebaseFirestore.getInstance();
        currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Map<String, Object> Habit = new HashMap<>();
        Habit.put("habit", habitText);
        Habit.put("time", reminderTime);
        Habit.put("repeat", RepeatTimeText);
        Habit.put("completionDate", FieldValue.arrayUnion(currentDate));


        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();

            firestoredb.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit")
                    .add(Habit).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
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

    private void EditHabit(String id, String habitText, String RepeatTimeText, String reminderTime) {
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit").document(id)
                    .update("habit", habitText, "repeat",RepeatTimeText, "time", reminderTime)
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

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if(snapshot!=null){
                                habitList = new ArrayList<>();
                                for(DocumentSnapshot document: snapshot.getDocuments()){
                                    String id = document.getId();
                                    HabitModel habitModel = document.toObject(HabitModel.class);
                                    habitModel.setHabitId(id);
                                    habitList.add(habitModel);
                                    habitAdapter = new HabitAdapter(HabitFragment.this,habitList);
                                    HabitRecyclerView.setAdapter(habitAdapter);
                                    habitAdapter.notifyDataSetChanged();
                                    Log.d("Firestore", "Habit Name retrieved: " + habitModel.getHabit());
                                }
                                Log.d("Firestore","Total Habit Retrieved: "+habitList.size());
                            }
                        }
                    });
        }
    }

}