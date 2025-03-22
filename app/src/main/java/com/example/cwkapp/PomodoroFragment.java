package com.example.cwkapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class PomodoroFragment extends Fragment {
    private CountDownTimer countDownTimer;
    private long setTime = 10 * 1000;
    private ToggleButton TimerToggleBtn;
    private TextView sessionCountView, TaskSessionCountView;
    private Button resetTimerBtn, selectAudioBtn;
    private Chronometer timer;
    private boolean istimerStarted = false;
    private String selectedTask = null;
    private MediaPlayer mediaPlayer;
    private int SessionCompleted = 0, selectedAudioId = -1;
    private Long SessionCount;
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
        NotificationChannel();
        TimerToggleBtn = view.findViewById(R.id.timerToggleBtn);
        sessionCountView =view.findViewById(R.id.SessionCountView);
        TaskSessionCountView = view.findViewById(R.id.TaskSessionCountView);
        retrieveSessionData();

        updateTextView();
        timer = view.findViewById(R.id.chronometer);
        resetTimerBtn = view.findViewById(R.id.resetTimer);
        timer.setText("25:00");
        TimerToggleBtn.setText(null);
        TimerToggleBtn.setTextOn(null);
        TimerToggleBtn.setTextOff(null);
        TimerToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    TimerStart();
                    startAudio();
                    istimerStarted = true;

                }else{
                   Log.d("Timer","Timer is paused");
                    countDownTimer.cancel();
                    istimerStarted = false;

                    if(mediaPlayer !=null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }

                }
            }
        });



    /*.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean Boolean) {
                if(Boolean){
                    //startAudio();
                    TimerStart();
                }else{//pause the timer
                        countDownTimer.cancel();
                        istimerStarted = false;
                //pause music
                    *//*if(mediaPlayer !=null && mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }*//*
                }
            }
        });*/

        resetTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.cancel();
                setTime = 10 * 1000;
                /*int min = (int) (25 * 60 * 1000 / 1000) / 60;
                int sec = (int) (25 * 60 * 1000 / 1000) % 60;
                timer.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));*/
                timer.setText("25:00");
                TimerToggleBtn.setChecked(false);
                istimerStarted = false;
                if(mediaPlayer !=null && mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

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
                if(i == 0){
                    selectedTask = null;
                }else{
                    selectedTask = taskList.get(i).getId();
                    Log.d("Firestore","Selected task from the drop-down is: "+ selectedTask);
                }

            }
        });
        selectAudioBtn = view.findViewById(R.id.selectAudio);
        selectAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioBottomDialog();
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
                              taskModel.setTaskId(id);
                              taskList.add(taskModel);
                              taskName.add(taskModel.getTask());
                              //task name
                          }
                          taskSelectionList.notifyDataSetChanged();

                      }
                  }
              });

  }
}

private void TimerStart() {
countDownTimer = new CountDownTimer(setTime, 1000) {
    public void onTick(long timeUntilFinish) {
        setTime = timeUntilFinish;
        int min = (int) (timeUntilFinish / 1000) / 60;
        int sec = (int) (timeUntilFinish / 1000) % 60;
        timer.setText(String.format("%02d:%02d", min, sec));//display the time in two digits
    }

    public void onFinish() {
        timer.setText("00:00");
        TimerToggleBtn.setChecked(false);
        SessionCompleted++;
        Notifications();
       // increment the completed session only if session(timer) is finished.
        completedSession();
        retrieveTaskSessionData();
        updateTextView();

        if(mediaPlayer !=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        istimerStarted = false;

    }

}.start();
istimerStarted = true;
}


private void completedSession() {
    if(selectedTask!=null) {
        Log.d("Firestore","The id after task selection is: "+ selectedTask);
        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            //int sessionCounts = SessionCompleted;
            firestoredb.collection("Task").document(loggedUserId)
                    .collection("LoggedUser Task")
                    .document(selectedTask).update("sessionCounts", FieldValue.increment(1));

        }
        } else {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PomodoroSessions", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("Session Counts", SessionCompleted);
            editor.apply();
        }
    }


private void retrieveTaskSessionData() { //retrieve the session data stored in sharedpreferences.
    if (selectedTask != null) {
        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Task").document(loggedUserId).collection("LoggedUser Task").document(selectedTask).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    SessionCount = documentSnapshot.getLong("sessionCounts");
                    Log.d("FireStore", "no.of task sessions: " + SessionCount);
                    updateTaskTextView();
                    //TaskSessionCountView.setText("Task Session Count: " + SessionCount);
                }
            });
                    /*.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value != null) {
                                for (DocumentSnapshot doc : value.getDocuments()) {
                                    selectedTask = doc.getId();
                                    TaskModel taskModel = doc.toObject(TaskModel.class);
                                    Log.d("Firestore", "Total Session Counts: " + taskModel.getSessionCounts());
                                    sessionCountView.setText("Session Counts For Task: " + taskModel.getSessionCounts());
                                }

                            }

                        }
                    });*/
        }

    }
}
    private void retrieveSessionData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PomodoroSessions", Context.MODE_PRIVATE);
        SessionCompleted = sharedPreferences.getInt("Session Counts", 0);
        updateTextView();
    }

    private void updateTextView(){
        sessionCountView.setText("Session Counts: "+ SessionCompleted);
    }
    private void updateTaskTextView(){
        TaskSessionCountView.setText("Task Session Count: "+ SessionCount);
    }

    private void AudioBottomDialog(){
        BottomSheetDialog audioDialog = new BottomSheetDialog(requireContext());
        audioDialog.setContentView(R.layout.audio_bottomsheet);

        RadioGroup radiogrp = audioDialog.findViewById(R.id.audio_radiogrp);
        Button confirmAudioBtn = audioDialog.findViewById(R.id.OKBtn);

       /* radiogrp.setOnCheckedChangeListener((radioGroup, chosenId) ->{
            if(mediaPlayer != null){
                mediaPlayer.release(); //stop any previous audio.
                mediaPlayer=null;
            }
            int AudioPreviewId = getAudio(chosenId);
            if(AudioPreviewId != -1) {
                mediaPlayer = MediaPlayer.create(getContext(), AudioPreviewId);
                mediaPlayer.start();
            }
        });*/

        radiogrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int chosenId) {
                if(mediaPlayer != null){
                    mediaPlayer.release(); //stop any previous audio.
                    mediaPlayer=null;
                }

                if(chosenId ==R.id.rain_audio){
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.rain);
                    mediaPlayer.start();
                    selectedAudioId = R.raw.rain;
;                } else if(chosenId ==R.id.fire_audio){
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.forest_fire);
                    mediaPlayer.start();
                    selectedAudioId = R.raw.forest_fire;
                }else if(chosenId ==R.id.night_audio){
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.night_ambience);
                    mediaPlayer.start();
                    selectedAudioId = R.raw.night_ambience;
                }else if(chosenId ==R.id.nature_audio){
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.nature_soundscape);
                    mediaPlayer.start();
                    selectedAudioId = R.raw.nature_soundscape;
                }else if(chosenId ==R.id.waterfall_audio){
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.waterfall);
                    mediaPlayer.start();
                    selectedAudioId = R.raw.waterfall;
                }else if(chosenId ==R.id.no_audio){
                    mediaPlayer = null;
                    selectedAudioId = -1;
                }
            }
        });

       confirmAudioBtn.setOnClickListener(item->{
           if(mediaPlayer != null){
               mediaPlayer.release(); //stop any previous audio.
               mediaPlayer=null;
           }
          // selectedAudioId = getAudio(radiogrp.getCheckedRadioButtonId());
           audioDialog.dismiss();
       });

        audioDialog.show();
    }
/*private int getAudio(int chosenId){
    if(chosenId == R.id.rain_audio) return R.raw.rain;
    if(chosenId == R.id.fire_audio) return R.raw.forest_fire;
    if(chosenId == R.id.night_audio) return R.raw.night_ambience;
    if(chosenId == R.id.nature_audio) return R.raw.nature_soundscape;
    if(chosenId == R.id.waterfall_audio) return R.raw.waterfall;
    if(chosenId == R.id.no_audio) return -1;
    return -1;
}*/
    private void startAudio(){
        if(selectedAudioId != -1){
            if(mediaPlayer != null){
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(requireContext(),selectedAudioId);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void Notifications(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "POMODORO_CHANNEL")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Pomodoro Session Completed")
                .setContentText("It's time for a short break!!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getContext());
        if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS)!=
        PackageManager.PERMISSION_GRANTED){
        return;
        }
        manager.notify(1,builder.build());


    }

    private void NotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(Build.VERSION.SDK_INT >=33){
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }
            CharSequence name = "Pomodoro Channel";
            String description = "This is Channel for Pomodoro Timers";
            int important = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel("POMODORO_CHANNEL", name, important);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel.setDescription(description);
            }

            NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

        }
    }



}