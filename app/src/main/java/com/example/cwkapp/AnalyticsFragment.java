package com.example.cwkapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;


public class AnalyticsFragment extends Fragment {
private FirebaseFirestore firestoredb;
private FirebaseUser LoggedUser;
private BarChart TaskBarChart;
private PieChart SessionCountPieChart;

    public AnalyticsFragment() {
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
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        //Call the method to show the charts.
        DisplayChartNGraph();
        SessionCountPieChart = view.findViewById(R.id.SessionPieChart);
        TaskBarChart = view.findViewById(R.id.TaskBarChart);
    }

    private void DisplayChartNGraph(){


        firestoredb = FirebaseFirestore.getInstance();  //retreving the task names into the drop-down list.
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PomodoroSessions", Context.MODE_PRIVATE);
        int sessionCounts = sharedPreferences.getInt("Session Counts", 0);

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();

            firestoredb.collection("Task")
                    .document(loggedUserId).collection("LoggedUser Task")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshot) {
                            int index=0;
                            int TotalTaskSessionCounts = 0;
                            List<String> taskNameList =  new ArrayList<>();
                            List<BarEntry> barEntryList = new ArrayList<>();
                        for(QueryDocumentSnapshot documentSnapshot: snapshot){
                            String id = documentSnapshot.getId();
                            TaskModel taskModel = documentSnapshot.toObject(TaskModel.class);
                            taskModel.setTaskId(id);
                            if(taskModel!=null){
                                TotalTaskSessionCounts += taskModel.getSessionCounts();
                                barEntryList.add(new BarEntry(index,taskModel.getSessionCounts()));
                                taskNameList.add(taskModel.getTask());
                                index++;
                            }
                        }
                            DisplayBarChart(taskNameList,barEntryList);
                            DisplayPieChart(sessionCounts, TotalTaskSessionCounts);
                        }

                    });
        }


        //retrieve the session counts and task names from firestore.
        //session counts from shared preferences.
        //call the bar chart method.
        //call the pie chart method.
    }

    private void DisplayBarChart(List<String> taskNameList, List<BarEntry> barEntryList){
        BarDataSet DataSet = new BarDataSet(barEntryList,"Task");
        DataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        DataSet.setValueTextSize(10f);
        DataSet.setDrawValues(true);
        XAxis barXaxis = TaskBarChart.getXAxis();
        barXaxis.setValueFormatter(new IndexAxisValueFormatter(taskNameList));
        barXaxis.setLabelRotationAngle(90);
        barXaxis.setTextSize(10f);
        barXaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        TaskBarChart.setData(new BarData(DataSet));
        TaskBarChart.animateY(2000);
        TaskBarChart.getDescription().setEnabled(false);
        //TaskBarChart.getDescription().setTextColor(Color.BLUE);

        TaskBarChart.invalidate();
    }

    private void DisplayPieChart(int sessionCounts, int TaskSessionCounts){
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(sessionCounts,"Standalone Pomodoro Sessions"));
        pieEntries.add(new PieEntry(TaskSessionCounts,"Task Pomodoro Sessions"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries,"");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(15f);

        Legend pieLegend = SessionCountPieChart.getLegend();
        pieLegend.setForm(Legend.LegendForm.CIRCLE);
        pieLegend.setTextColor(Color.BLACK);
        SessionCountPieChart.setData(new PieData(pieDataSet));
        SessionCountPieChart.setUsePercentValues(true);
        SessionCountPieChart.setCenterText("Sessions Breakdown");
        SessionCountPieChart.getDescription().setEnabled(false);
        SessionCountPieChart.setHoleRadius(40f);
        SessionCountPieChart.setTransparentCircleRadius(50f);
        SessionCountPieChart.animateY(2000, Easing.EaseInOutCubic);

        SessionCountPieChart.invalidate();


    }
}