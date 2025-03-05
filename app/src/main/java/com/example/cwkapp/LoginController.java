package com.example.cwkapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWebException;
import com.google.firebase.auth.FirebaseUser;

public class LoginController extends AppCompatActivity {
    private FirebaseAuth authorization;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_controller);
        authorization = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

Button backToSignin  = findViewById(R.id.MoveToSigninBtn);
        backToSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginController.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();

        FirebaseUser currentUser = authorization.getCurrentUser();
        if(currentUser !=null){
            OpenDashboard();
        }
    }

    public void Login(String email, String password){
        authorization.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = authorization.getCurrentUser();
                    Toast.makeText(LoginController.this, "You are Logged In.", Toast.LENGTH_SHORT).show();
                    //Intent to move to next activity.
                    OpenDashboard();
                }
                else{
                    Log.w("LoginController", "signInWithEmail:failure",task.getException());
                    Toast.makeText(LoginController.this,"Fail to Login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void LoginBtnClicked(View view){
        EditText Username = findViewById(R.id.LoginUsername);
        EditText Password  = findViewById(R.id.LoginPass);
        String username = Username.getText().toString();
        String password = Password.getText().toString();
        Login(username,password);
    }
    private void OpenDashboard(){
        Intent intent = new Intent(LoginController.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}