package com.example.cwkapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private FirebaseAuth authorization; //uses firbase authorization
    private TextView forgetPassword;
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

        Button backToSignin  = findViewById(R.id.MoveToSigninBtn); // redirects user to register activity
        backToSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginController.this, MainActivity.class);
                startActivity(intent);
            }
        });

        forgetPassword = findViewById(R.id.ForgetPasswordText);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ForgetPasswordDialog();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = authorization.getCurrentUser(); //checks if user is logged in on the start and skip the login screen if they are.
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

        if(username.isEmpty()){
            Username.setError("Please Enter Email");
        }
        if(password.isEmpty()){
            Password.setError("Enter Password");
        }else {
            Login(username,password);
        }

    }
    private void OpenDashboard(){
        Intent intent = new Intent(LoginController.this, Dashboard.class);
        startActivity(intent);
        finish();
    }

    private void ForgetPasswordDialog(){
        Dialog ForegtPassdialog = new Dialog(LoginController.this); //creates a simple custom modal dialog
        ForegtPassdialog.setContentView(R.layout.forgot_password_dialog);
        ForegtPassdialog.setCancelable(false); //disable to tap outside the box to cancel it.

        //initialise views inside dialog
        Button CancelBtn = ForegtPassdialog.findViewById(R.id.CancelBtn);
        Button resetBtn = ForegtPassdialog.findViewById(R.id.resetBtn);
        EditText email = ForegtPassdialog.findViewById(R.id.email);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ResetEmail = email.getText().toString();

                if(ResetEmail.isEmpty()){
                    Toast.makeText(LoginController.this, "Enter you registered email address", Toast.LENGTH_SHORT);
                    email.setError("Please Enter Email");

                }else{
                    //code adapted from: https://www.youtube.com/watch?v=y9tJh14ofaI&t=698s&ab_channel=AndroidKnowledge
                    authorization.sendPasswordResetEmail(ResetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginController.this, "Reset Password Email have been sent", Toast.LENGTH_SHORT).show();
                                ForegtPassdialog.dismiss();
                            }else{
                                Toast.makeText(LoginController.this, "Unable to Send", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ForegtPassdialog.dismiss();
            } //dismiss the dialog box
        });

        ForegtPassdialog.show(); //display the dialog
    }
}