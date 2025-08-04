package com.example.cwkapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class CompletedTaskFragment extends Fragment {
private RecyclerView CompletedTaskRC;
private FirebaseFirestore firestoredb;
    private FirebaseAuth Auth;
    private FirebaseUser LoggedUser;
    private CompletedTaskAdapter CTAdapter;
    private ArrayList<TaskModel> CTaskList;

    public CompletedTaskFragment() {
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
        View root = inflater.inflate(R.layout.fragment_completed_task, container, false);
        CompletedTaskRC = root.findViewById(R.id.CTrecyclerView);
        CompletedTaskRC.setLayoutManager(new LinearLayoutManager(getContext()));
        displayCompletedTaskList(CompletedTaskRC);

        return root;
    }

    private void displayCompletedTaskList(RecyclerView CTRecyclerView){
        firestoredb = FirebaseFirestore.getInstance();
        LoggedUser = FirebaseAuth.getInstance().getCurrentUser();

        if (LoggedUser != null) {
            String loggedUserId = LoggedUser.getUid();

            firestoredb.collection("Task")
                    .document(loggedUserId)
                    .collection("LoggedUser Task").whereEqualTo("isChecked",1)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (snapshot!= null) {
                                CTaskList = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    String id = doc.getId();
                                    TaskModel taskModel = doc.toObject(TaskModel.class);
                                    taskModel.setTaskId(id);
                                    CTaskList.add(taskModel);
                                    CTAdapter = new CompletedTaskAdapter(CompletedTaskFragment.this, CTaskList);
                                    CompletedTaskRC.setAdapter(CTAdapter);
                                    CTAdapter.notifyDataSetChanged();

                                }
                            }
                        }

                    });
        }
    }
}