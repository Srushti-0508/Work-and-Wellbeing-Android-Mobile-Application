package com.example.cwkapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment {

    private TextView Date, Day, MonthnYear, TaskCountsView, HabitCountsView;
    private FirebaseAuth Auth;
    private FirebaseUser LoggedUser;
    private FirebaseFirestore firestore;


    public HomeFragment() {
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        Date = view.findViewById(R.id.date);
        Day = view.findViewById(R.id.day);
        MonthnYear = view.findViewById(R.id.monthNyear);
        TaskCountsView = view.findViewById(R.id.TaskCount);
        HabitCountsView =view.findViewById(R.id.HabitCount);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat date = new SimpleDateFormat("d");
        String todayDate = date.format(calendar.getTime());
        Date.setText(todayDate);

        SimpleDateFormat day = new SimpleDateFormat("EEEE", Locale.getDefault());
        String todayDay = day.format(calendar.getTime()).toUpperCase();
        Day.setText(todayDay);

        SimpleDateFormat MonthYear = new SimpleDateFormat("MMMM YYYY");
        String CurrentMonthYear = MonthYear.format(calendar.getTime());
        MonthnYear.setText(CurrentMonthYear.toUpperCase());
        showTodayTask();
        showHabits();
    }

    private void showTodayTask() {
        firestore = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        SimpleDateFormat TodayDate = new SimpleDateFormat("d/M/yyyy",Locale.getDefault());
        String TaskDate = TodayDate.format(new Date());

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestore.collection("Task")
                    .document(loggedUserId)
                    .collection("LoggedUser Task").whereEqualTo("date",TaskDate)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                int TaskCount = value.size();
                                TaskCountsView.setText(String.valueOf(TaskCount));
                                TaskCountsView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                        TaskFragment taskFragment = new TaskFragment();
                                        fragmentTransaction.replace(R.id.fragmentContainerView, taskFragment);
                                        fragmentTransaction.addToBackStack(null);
                                        fragmentTransaction.commit();

                                    }
                                });

                        }
                    });

        }
    }

    private void showHabits(){
        firestore = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        SimpleDateFormat TodayHabitDate = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        String HabitDate = TodayHabitDate.format(new Date());

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestore.collection("Habit")
                    .document(loggedUserId)
                    .collection("LoggedUser Habit").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            int totalHabit = 0;
                            int finishedHabit = 0;
                            for(DocumentSnapshot doc: value.getDocuments()){
                                totalHabit++;
                                HabitModel habit = doc.toObject(HabitModel.class);
                                List<String> finishHabitDate =  (List<String>) habit.getCompletionDate();
                                if(finishHabitDate !=null && finishHabitDate.contains(HabitDate)){
                                    finishedHabit++;
                                }
                            }
                            if(totalHabit == 0){
                                HabitCountsView.setText("0/0");
                            }else{
                                HabitCountsView.setText(finishedHabit +" / "+ totalHabit);
                            }
                        }
                    });
        }
    }

}