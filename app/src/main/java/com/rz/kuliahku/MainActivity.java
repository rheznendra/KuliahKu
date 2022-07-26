package com.rz.kuliahku;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    private SmoothBottomBar bnv;
    private Fragment fragment = new TodaySubjectsFragment();
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();

        bnv = findViewById(R.id.bottom_navigation);
        bnv.setOnItemSelectedListener((OnItemSelectedListener) i -> {
            if (i == 0) {
                fragment = new TodaySubjectsFragment();
            } else if (i == 1) {
                fragment = new SubjectListFragment();
            } else if (i == 2) {
                fragment = new AboutMeFragment();
            } else {
                return false;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
            return true;
        });
    }
}