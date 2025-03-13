package com.example.cwkapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class PomodoroFragment extends Fragment {
    private CountDownTimer countDownTimer;
    private ToggleButton TimerToggleBtn;
    private TextView sessionCountView;
    private Button resetTimerBtn;
    private Chronometer timer;
    private boolean istimerStarted = false;
    private long pauseSet = 0;
    private String selectedTask = null;
    private int SessionCompleted = 0;
    private FirebaseFirestore firestoredb;
    private FirebaseAuth Auth;
    private FirebaseUser LoggedUser;
    private TextInputLayout dropdown;
    private AutoCompleteTextView taskDropDown_list;
    private ArrayList<TaskModel> taskList = new ArrayList<>();
    private ArrayList<String> taskName = new ArrayList<>();  //list to store the task name
    private ArrayAdapter<String> taskSelectionList;

    public PomodoroFragment() {
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
        return inflater.inflate(R.layout.fragment_pomodoro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {

        TimerToggleBtn = view.findViewById(R.id.timerToggleBtn);
        sessionCountView =view.findViewById(R.id.SessionCountView);
        timer = view.findViewById(R.id.chronometer);
        resetTimerBtn = view.findViewById(R.id.resetTimer);
        retrieveSessionData();
        timer.setText("25:00");
        TimerToggleBtn.setText(null);
        TimerToggleBtn.setTextOn(null);
        TimerToggleBtn.setTextOff(null);
        TimerToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean Boolean) {
                if(Boolean){
                    TimerStart();
                    istimerStarted = true;
                }else{ //pause the timer
                countDownTimer.cancel();
                istimerStarted = false;
                }
            }
        });


        resetTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });
        dropdown = view.findViewById(R.id.dropdown);
        taskDropDown_list = view.findViewById(R.id.autocomplete_view);
        taskSelectionList = new ArrayAdapter<>(requireContext(), R.layout.dropdown_category_list, taskName);
        taskDropDown_list.setAdapter(taskSelectionList);
        selectTask();
        taskDropDown_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String taskName = adapterView.getItemAtPosition(i).toString();

            }
        });

    }


private void selectTask(){
  firestoredb = FirebaseFirestore.getInstance();  //retreving the task names into the drop-down list.
  LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
  if (LoggedUser != null) {
      String loggedUserId = LoggedUser.getUid();

      firestoredb.collection("Task").document(loggedUserId).collection("LoggedUser Task")
              .addSnapshotListener(new EventListener<QuerySnapshot>() {
                  @Override
                  public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                      if (value != null) {
                          for (DocumentSnapshot doc : value.getDocuments()) {
                              String id = doc.getId();
                              TaskModel taskModel = doc.toObject(TaskModel.class);
                              taskList.add(taskModel);
                              taskName.add(taskModel.getTask()); //task name
                              selectedTask = id; //get the Selected Task ID.
                          }
                          taskSelectionList.notifyDataSetChanged();

                      }
                  }
              });
  }
}

private void TimerStart() {
countDownTimer = new CountDownTimer(10 * 1000, 1000) {
    public void onTick(long timeUntilFinish) {
        int min = (int) (timeUntilFinish / 1000) / 60;
        int sec = (int) (timeUntilFinish / 1000) % 60;
        timer.setText(String.format("%02d:%02d", min, sec)); //display the time in two digits
    }

    public void onFinish() {
        timer.setText("00:00");
        TimerToggleBtn.setChecked(false);
        SessionCompleted++;// increment the completed session only if session(timer) is finished.
     //   resetTimer();
        completedSession();
        retrieveSessionData();
        istimerStarted = false;
    }

}.start();
istimerStarted = true;
}

private void resetTimer(){
    int min = (int) (25 * 60 * 1000/1000) / 60;
    int sec = (int) (25 * 60 * 1000/1000) % 60;
    timer.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
    istimerStarted = false;
}

private void completedSession() {
        //Save the session data when timer is used without task.
    if(selectedTask!=null){
        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        if(LoggedUser!=null){
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Task").document(loggedUserId)
                    .collection("LoggedUser Task").document(selectedTask).update("sessionCounts", SessionCompleted++);
        }

    }else {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PomodoroSessions", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("Session Counts", SessionCompleted + 1);
        editor.apply();
    }
}

private void retrieveSessionData() { //retrieve the session data stored in sharedpreferences.
    //if(SelectedTask == null)
    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PomodoroSessions", Context.MODE_PRIVATE);
    SessionCompleted = sharedPreferences.getInt("Session Counts", 0);
    sessionCountView.setText("Session Counts: " + SessionCompleted);
    }
}