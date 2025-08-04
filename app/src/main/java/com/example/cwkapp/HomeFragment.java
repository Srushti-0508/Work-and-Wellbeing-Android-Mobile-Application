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

/** Displays an overview of the user's daily progress, including task and habit completion.
  *  Retrieves and displays the number of completed tasks from Firestore.
 *  Retrieves and displays the number of completed habits for the current day from Firestore.
*/
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

        SimpleDateFormat date = new SimpleDateFormat("d"); //retrieve date to display in Date TextView.
        String todayDate = date.format(calendar.getTime());
        Date.setText(todayDate);

        SimpleDateFormat day = new SimpleDateFormat("EEEE", Locale.getDefault()); //retrieve just day
        String todayDay = day.format(calendar.getTime()).toUpperCase();
        Day.setText(todayDay);

        SimpleDateFormat MonthYear = new SimpleDateFormat("MMMM YYYY"); //retrieve month name and year
        String CurrentMonthYear = MonthYear.format(calendar.getTime());
        MonthnYear.setText(CurrentMonthYear.toUpperCase());
        showTodayTask(); //display total number of task set for today.
        showHabits(); //display completion count of habits.
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
                    .collection("LoggedUser Task")

                    .whereEqualTo("date",TaskDate)
                    .whereEqualTo("isChecked",0)

                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                int TaskCount = value.size(); //retrieve documents size
                                TaskCountsView.setText("" + TaskCount); //setting the value as string
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
                    .collection("LoggedUser Habit")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("Firestore", "Listen failed.", error);
                                return;
                            }
                            if (value == null) {
                                Log.w("Firestore", "QuerySnapshot is null.");
                                return;
                            }
                            int totalHabit = 0;
                            int finishedHabit = 0;
                            for(DocumentSnapshot doc: value.getDocuments()){
                                totalHabit++; //increments by 1 on each loop
                                HabitModel habit = doc.toObject(HabitModel.class);
                                List<String> finishHabitDate =  (List<String>) habit.getCompletionDate(); //retrieve and store the completion dates in new list
                                if(finishHabitDate !=null && finishHabitDate.contains(HabitDate)){
                                    finishedHabit++; // increments by 1
                                }
                            }
                            if(totalHabit == 0){
                                HabitCountsView.setText("0/0"); //initially set the habit count as 0/0 if no habit is being completed.
                            }else{
                                HabitCountsView.setText(finishedHabit +" / "+ totalHabit);
                            }
                        }
                    });
        }
    }

}