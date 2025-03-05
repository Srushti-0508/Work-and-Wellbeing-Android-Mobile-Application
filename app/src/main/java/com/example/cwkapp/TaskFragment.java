package com.example.cwkapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class TaskFragment extends Fragment {
private FirebaseFirestore firestoredb;

private String[] category_list = {"General","Work","Study","Home"};
    private String date, priorityText;
    private ArrayAdapter<String> adapterCategoryList;
    private AutoCompleteTextView dropdown_list;
    private FloatingActionButton taskFab;
    private Button saveBtn;
    private MaterialButton high, medium, low;
    private EditText task_text;
    private TextView date_picker;
    private TextInputLayout dropdown;
    private MaterialButtonToggleGroup priorityBtnGrp;


    public TaskFragment() {
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
        return inflater.inflate(R.layout.fragment_task, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState){

        taskFab = view.findViewById(R.id.floatingActionButton3);
        if(taskFab!=null) {
            taskFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { AddTask();}
            });
        }else{
            Toast.makeText(getActivity(),"Error Opening Fab",Toast.LENGTH_SHORT).show();
        }


    }

    private void AddTask(){
        final Dialog AddTaskDialog = new Dialog(getContext());
        AddTaskDialog.setContentView(R.layout.add_task);
        /*enable to tap outside the dialog box to close it*/
        AddTaskDialog.setCancelable(true);

        /*final TextView dialog_heading = AddTaskDialog.findViewById(R.id.dialogHeading);*/
        task_text = AddTaskDialog.findViewById(R.id.textInputEditText);
        priorityBtnGrp = AddTaskDialog.findViewById(R.id.priorityBtn);
        high = AddTaskDialog.findViewById(R.id.HighBtn);
        medium = AddTaskDialog.findViewById(R.id.MediumBtn);
        low =AddTaskDialog.findViewById(R.id.LowBtn);
        /*Date Picker Code Adapted from https://abhiandroid.com/ui/datepicker#gsc.tab=0*/
        date_picker = AddTaskDialog.findViewById(R.id.date);
        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar selectDate = Calendar.getInstance();
                int day = selectDate.get(Calendar.DAY_OF_MONTH);
                int month = selectDate.get(Calendar.MONTH);
                int year = selectDate.get(Calendar.YEAR);

                DatePickerDialog datePicker = new DatePickerDialog(getContext(),R.style.DatePickerBg,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int m, int d) {
                        date_picker.setText(d + "/" + (m + 1) + "/" + y);
                        date = d + "/" + (m + 1) + "/" + y;
                    }
                }, year, month, day);
                datePicker.show();
            }
        });

        dropdown = AddTaskDialog.findViewById(R.id.dropdown);
        /*Code for drop-down adapted from https://www.youtube.com/watch?v=KsprqXfugGQ&ab_channel=CubixSol*/
        dropdown_list = AddTaskDialog.findViewById(R.id.autocomplete_view);
        adapterCategoryList = new ArrayAdapter<String>(requireContext(),R.layout.dropdown_category_list, category_list);

        dropdown_list.setAdapter(adapterCategoryList);
        dropdown_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                String category_list = adapterView.getItemAtPosition(i).toString();
            }
        });
        saveBtn = AddTaskDialog.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTask(task_text, date, dropdown_list, priorityBtnGrp);
            }
        });
        AddTaskDialog.show();
    }

    private void saveTask(EditText task_text, String date, AutoCompleteTextView dropdown_list, MaterialButtonToggleGroup priorityBtnGrp){
        firestoredb = FirebaseFirestore.getInstance();
        String taskText = task_text.getText().toString();
        String categoriesText = dropdown_list.getText().toString();

        int selectedPriority = priorityBtnGrp.getCheckedButtonId();
        if(selectedPriority == R.id.HighBtn){
            priorityText = "High";
        }else if(selectedPriority == R.id.MediumBtn){
            priorityText="Medium";
        }else if(selectedPriority == R.id.LowBtn){
            priorityText="Low";
        }

        /*String priorityText = "Low";
        if(high.isSelected()){
            priorityText = "High";
        }else if(medium.isSelected()){
            priorityText = "Medium";
        }else if(low.isSelected()){
            priorityText = "Low";
        }*/

        if(taskText.isEmpty()){
            Toast.makeText(getContext(),"Please enter Task", Toast.LENGTH_SHORT).show();
        }else{
            Map<String, Object> tasks = new HashMap<>();
            tasks.put("task", taskText);
            tasks.put("date", date);
            tasks.put("category", categoriesText);
            tasks.put("priority", priorityText);
            tasks.put("status",0);

            firestoredb.collection("Task").add(tasks).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(),"Task saved Successfully",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }




    }
}