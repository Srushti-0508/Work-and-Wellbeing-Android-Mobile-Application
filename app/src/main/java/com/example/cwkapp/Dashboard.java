package com.example.cwkapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity {
    private String title;
    Toolbar app_toolbar;
    private FirebaseAuth authorization;
    private FirebaseUser loggedUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        authorization = FirebaseAuth.getInstance();
        BottomNavigationView bottomNavigationView = findViewById(R.id.BottomNavView);
        app_toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(app_toolbar);

        loggedUser = authorization.getCurrentUser();
        String Logged_username = loggedUser.getEmail();
       getSupportActionBar().setTitle("Welcome: "+Logged_username); //when the application runs by default it will show the logged user email in toolbar title.

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNavigationView.setItemActiveIndicatorColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.active_color))); //change the colour of active fragment indicator

        bottomNavigationView.setOnItemSelectedListener(item -> { //switch between different fragments
            if(item.getItemId() == R.id.nav_home){
                title= Logged_username; //set the user email as title on Home Fragment
                changeFragment(new HomeFragment());
            }else if(item.getItemId() == R.id.nav_task){
                title = "Task";
                changeFragment(new TaskFragment());
            } else if(item.getItemId() == R.id.nav_habit){
                title="Habit";
                changeFragment(new HabitFragment());
            }else if(item.getItemId() == R.id.nav_pomodoro){
                title="Pomodoro";
                changeFragment(new PomodoroFragment());
            }
            else if(item.getItemId() == R.id.nav_analytics){
                title="Analytics";
                changeFragment(new AnalyticsFragment());
            }
            return true;
        });
    }
    private void changeFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView,fragment,null)
                .setReorderingAllowed(true).addToBackStack(null)
                .commit();
        app_toolbar.setTitle(title);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem){
        if(menuItem.getItemId() == R.id.toolbar_logout){
            authorization.signOut(); //Logout user
            Intent intent = new Intent(Dashboard.this, LoginController.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out Successfully",Toast.LENGTH_SHORT).show();
        }
        return true;
    }


}