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

public class MainActivity extends AppCompatActivity {

private FirebaseAuth authorization;

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        authorization = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button moveToLogin = findViewById(R.id.MoveToLoginBtn); //redirect users to login activity.
        moveToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginController.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = authorization.getCurrentUser(); //checks if user is logged in on the start and skip the login screen if they are.
        if(currentUser !=null){
            OpenDashboard(); //redirect to the home
        }
    }

    public void register(String email, String password){
    //create new account using email and password.
        authorization.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d("MainActivity", "createUserWithEmail:success");
                            FirebaseUser user = authorization.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Authentication Success.", Toast.LENGTH_SHORT).show();
                            OpenDashboard(); //direct to dashboard if authentication is successful.
                        }
                        else{
                            Log.w("MainActivity", "createUserWithEmail:failure",task.getException());
                            Toast.makeText(MainActivity.this,"Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
            });
    }

    public void registerBtnClicked(View view){
        EditText Username = findViewById(R.id.username);
        EditText Password  = findViewById(R.id.password);
        String username = Username.getText().toString();
        String password = Password.getText().toString();

        if(username.isEmpty()){
            Username.setError("Please Enter Email"); //set error message in ediText
        }
        if(password.isEmpty()){
            Password.setError("Enter Password");
        }else {
            register(username, password);
        }

    }
    private void OpenDashboard(){  //Intent to move to next activity.
        Intent intent = new Intent(MainActivity.this, Dashboard.class);
        startActivity(intent);
        finish();
    }


}
