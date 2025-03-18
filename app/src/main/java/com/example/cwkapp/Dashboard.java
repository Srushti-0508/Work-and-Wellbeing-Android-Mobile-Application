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

public class Dashboard extends AppCompatActivity {
    String title="Dashboard";
    Toolbar app_toolbar;
    private FirebaseAuth authorization;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        authorization = FirebaseAuth.getInstance();
        BottomNavigationView bottomNavigationView = findViewById(R.id.BottomNavView);
        app_toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(app_toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
bottomNavigationView.setItemActiveIndicatorColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.active_color)));
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home){
                title="Dashboard";
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem){

        if(menuItem.getItemId() == R.id.toolbar_notification){
            Toast.makeText(this, "Notifications is clicked",Toast.LENGTH_SHORT).show();
        }
        else if(menuItem.getItemId() == R.id.toolbar_settings){
            Toast.makeText(this, "Settings is clicked",Toast.LENGTH_SHORT).show();
        }
        else if(menuItem.getItemId() == R.id.toolbar_logout){
            authorization.signOut();
            Intent intent = new Intent(Dashboard.this, LoginController.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out Successfully",Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    private void changeFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView,fragment,null)
                .setReorderingAllowed(true).addToBackStack(null)
                .commit();
                app_toolbar.setTitle(title);
    }
}