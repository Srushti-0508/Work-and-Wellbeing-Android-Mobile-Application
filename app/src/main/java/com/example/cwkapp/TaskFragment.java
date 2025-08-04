package com.example.cwkapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class TaskFragment extends Fragment {
    private FirebaseFirestore firestoredb;
    private FirebaseAuth Auth;
    private FirebaseUser LoggedUser;

    private String[] category_list = {"General", "Work", "Study", "Home"};
    private String date, priorityText;
    private ArrayAdapter<String> adapterCategoryList, adapterCompletedTask;
    private List<String>completedTaskList;
    private ListView completedTaskLV;
    private AutoCompleteTextView dropdown_list;
    private FloatingActionButton taskFab;
    private Button saveBtn;

    private EditText task_text;
    private TextView date_picker, completedListTextView;
    private TextInputLayout dropdown;
    private MaterialButtonToggleGroup priorityBtnGrp;
    private TaskAdapter taskAdapter, CompletedTaskAdapter;
    private ArrayList<TaskModel> taskList;


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
        View root = inflater.inflate(R.layout.fragment_task, container, false);
        RecyclerView TaskRecyclerView = root.findViewById(R.id.taskrecyclerView); //initialise Recycler View
        TaskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext())); //display item vertically aligned
        displayTask(TaskRecyclerView);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        completedListTextView = view.findViewById(R.id.completedListHeading);
        completedListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(new CompletedTaskFragment());
            }
        });

        taskFab = view.findViewById(R.id.floatingActionButton3);
        if (taskFab != null) {
            taskFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AddTask(null);
                } //empty dialog box to add new task.
            });
        } else {
            Toast.makeText(getActivity(), "Error Opening Fab", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeFragment(Fragment fragment){
        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView,fragment,null)
                .setReorderingAllowed(true).addToBackStack(null)
                .commit();
    }
    /**
     * Opens a custom dialog to add a new task or edit an existing task.
     * The dialog allows users to input task details including name, priority, category, and due date.
     * If a task is passed in as a parameter, the dialog will pre-fill the fields for editing.
     *
     * param ediTask- The task to be edited. If null, a new task will be created.
     */

    public void AddTask(@Nullable TaskModel ediTask) {
        final Dialog AddTaskDialog = new Dialog(getContext());
        AddTaskDialog.setContentView(R.layout.add_task);
        /*enable to tap outside the dialog box to close it*/
        AddTaskDialog.setCancelable(true);

        //UI components
        task_text = AddTaskDialog.findViewById(R.id.textInputEditText);
        priorityBtnGrp = AddTaskDialog.findViewById(R.id.priorityBtn);
        saveBtn = AddTaskDialog.findViewById(R.id.saveBtn);

        /*Code Adapted from https://abhiandroid.com/ui/datepicker#gsc.tab=0*/
        date_picker = AddTaskDialog.findViewById(R.id.date);
        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar selectDate = Calendar.getInstance();
                int day = selectDate.get(Calendar.DAY_OF_MONTH);
                int month = selectDate.get(Calendar.MONTH);
                int year = selectDate.get(Calendar.YEAR);

                DatePickerDialog datePicker = new DatePickerDialog(getContext(), R.style.DatePickerBg, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int m, int d) {
                        date_picker.setText(d + "/" + (m + 1) + "/" + y);
                        date = d + "/" + (m + 1) + "/" + y; //save selected date
                    }
                }, year, month, day);
                datePicker.show();
            }
        });

        // Set up the task category dropdown using AutoCompleteTextView and ArrayAdapter
        dropdown = AddTaskDialog.findViewById(R.id.dropdown);
        dropdown_list = AddTaskDialog.findViewById(R.id.autocomplete_view); //Recycler view
        adapterCategoryList = new ArrayAdapter<String>(requireContext(), R.layout.dropdown_category_list, category_list); //each task.xml layout file.
        dropdown_list.setAdapter(adapterCategoryList);
        dropdown_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                String category_list = adapterView.getItemAtPosition(i).toString(); //get the category name based on the position to display it in autocomplete view
            }
        });
        // If editing an existing task, populate the dialog fields with current values
        if(ediTask != null){
            task_text.setText(ediTask.getTask());

            if(ediTask.getDate()!=null){
                date_picker.setText(ediTask.getDate()); //if date is not saved, it set the title of the TextView
            }else{
                date_picker.setText("Select Date");
            }

            dropdown_list.setText(ediTask.getCategory(), false); //to show category drop-down list also during edit.
            String getPriority = ediTask.getPriority();  //get the priority level and checks the button
            if(getPriority != null){
                //check the appropriate priority button
                switch (getPriority){
                    case("High"):
                        priorityBtnGrp.check(R.id.HighBtn);
                        break;
                    case"Medium":
                        priorityBtnGrp.check(R.id.MediumBtn);
                        break;
                    case"Low":
                        priorityBtnGrp.check(R.id.LowBtn);
                        break;
                }
            }else{
                priorityBtnGrp.clearChecked(); //keep it unchecked if no priority level is saved.
            }
            saveBtn.setText("Update Task");
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskText = task_text.getText().toString();
                String categoriesText = dropdown_list.getText().toString();

                int selectedPriority = priorityBtnGrp.getCheckedButtonId(); //to save the priority level as string in document,
                //getting the checked Button ID and setting the priority text accordingly

                if (selectedPriority == R.id.HighBtn) {
                    priorityText = "High";
                } else if (selectedPriority == R.id.MediumBtn) {
                    priorityText = "Medium";
                } else if (selectedPriority == R.id.LowBtn) {
                    priorityText = "Low";
                }

                int Check = 0; //default unchecked status
                int SessionCounts = 0; //default session count

                if (taskText.isEmpty()) {
                    Toast.makeText(getContext(), "Task name is required", Toast.LENGTH_SHORT).show();
                }else{
                    if (ediTask == null) {
                        //create and save new task
                        TaskModel taskModel = new TaskModel(taskText, priorityText, categoriesText, date , Check, SessionCounts);
                        saveTask(taskModel);
                        AddTaskDialog.dismiss();
                    } else {
                        //Log.d("EditTask", "Editing task with ID: " + ediTask.getId());
                        //update existing task
                        ediTask.setTask(taskText);
                        ediTask.setPriority(priorityText);
                        ediTask.setCategory(categoriesText);
                        ediTask.setDate(date);
                        ediTask.setIsChecked(Check);
                        ediTask.setSessionCounts(SessionCounts);
                        EditTask(ediTask.getId(), ediTask);
                        AddTaskDialog.dismiss();
                    }
                }
            }
        });
        AddTaskDialog.show(); //display the dialog
    }
    private void saveTask(TaskModel saveTask) {
        firestoredb = FirebaseFirestore.getInstance();
            LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
            if (LoggedUser != null) {
                String loggedUserId = LoggedUser.getUid();
                saveTask.setTaskId(null);
                saveTask.setIsChecked(0); //to initially save the task as unchecked.
                saveTask.setSessionCounts(0); //initially save task Session count as 0
                firestoredb.collection("Task")
                        .document(loggedUserId)
                        .collection("LoggedUser Task")
                        .add(saveTask).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Task Saved Successfully", Toast.LENGTH_SHORT).show();
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

    private void EditTask(String id, TaskModel updateTask){
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();
        TaskModel taskModel = new TaskModel();
        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();
            firestoredb.collection("Task").document(loggedUserId)
                    .collection("LoggedUser Task")
                    .document(id)//setting Document ID so it only updates the selected saved task document.
                    .set(updateTask).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Updated Task Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void displayTask(RecyclerView TaskRecyclerView) { //display all the unchecked/newly set tasks
        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();

            firestoredb.collection("Task")
                    .document(loggedUserId)
                    .collection("LoggedUser Task")
                    .whereEqualTo("isChecked",0)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (snapshot!= null) {

                                taskList = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    String id = doc.getId(); //getting document ID
                                    TaskModel taskModel = doc.toObject(TaskModel.class);
                                    taskModel.setTaskId(id);
                                    taskList.add(taskModel);
                                    taskAdapter = new TaskAdapter(TaskFragment.this, taskList);
                                    TaskRecyclerView.setAdapter(taskAdapter);
                                    taskAdapter.notifyDataSetChanged();
                                    Log.d("Firestore", "Task Name retrieved: " + taskModel.getTask());
                                }

                                Log.d("Firestore", "Total Tasks Retrieved: " + taskList.size());

                            }
                        }

                    });
        }
    }
}
