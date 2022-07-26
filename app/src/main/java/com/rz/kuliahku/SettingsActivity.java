package com.rz.kuliahku;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    ListView lv_setting;
    RelativeLayout backBtn;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        lv_setting = findViewById(R.id.lv_setting);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.lv_setting, getResources().getStringArray(R.array.settings));
        lv_setting.setAdapter(adapter);

        lv_setting.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                startActivity(new Intent(this, AuthorActivity.class));
            } else if (position == 1) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("userId");
                editor.apply();
                startActivity(new Intent(this, SplashActivity.class));
                finish();
            }
        });
    }
}