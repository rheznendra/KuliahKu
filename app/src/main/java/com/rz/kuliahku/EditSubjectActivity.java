package com.rz.kuliahku;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.techiness.progressdialoglibrary.ProgressDialog;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditSubjectActivity extends AppCompatActivity {

    private String MIN_TIME = null, MAX_TIME = null;
    private TextView title;
    private ImageView backBtn;
    private Button cancelBtn, submitBtn;
    private EditText subjectNameInput, startTimeInput, endTimeInput;
    private PowerSpinnerView classCodeInput, dayInput;
    private TimePickerDialog tpdS, tpdE;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LocalTime lt;
    private ProgressDialog pDialog;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_subject);

        pDialog = new ProgressDialog(this);
        id = getIntent().getStringExtra("id");

        title = findViewById(R.id.title);
        title.setText("Edit Subject");

        subjectNameInput = findViewById(R.id.subjectNameInput);
        classCodeInput = findViewById(R.id.classCodeInput);
        dayInput = findViewById(R.id.dayInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);

        backBtn = findViewById(R.id.backBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        submitBtn = findViewById(R.id.submitBtn);

        backBtn.setOnClickListener(v -> finish());
        cancelBtn.setOnClickListener(v -> finish());

        setData(id);

        startTimeInput.setOnClickListener(v -> {
            startTimeInput.setError(null);
            int h, m;

            lt = LocalTime.parse(startTimeInput.getText());
            h = lt.getHour();
            m = lt.getMinute();

            tpdS = TimePickerDialog.newInstance((view, hourOfDay, minute, second) -> {
                        startTimeInput.setText(formatTime(hourOfDay, minute));
                        MIN_TIME = setMinMaxTime(formatTime(hourOfDay, minute), 1);
                    },
                    h, m, true);
            if (MAX_TIME != null) {
                lt = LocalTime.parse(MAX_TIME);
                tpdS.setMaxTime(lt.getHour(), lt.getMinute(), 0);
            }
            tpdS.show(getSupportFragmentManager(), "START_TIME_PICKER");
        });
        endTimeInput.setOnClickListener(v -> {
            endTimeInput.setError(null);
            int h, m;

            lt = LocalTime.parse(endTimeInput.getText());
            h = lt.getHour();
            m = lt.getMinute();

            tpdE = TimePickerDialog.newInstance((view, hourOfDay, minute, second) -> {
                        endTimeInput.setText(formatTime(hourOfDay, minute));
                        MAX_TIME = setMinMaxTime(formatTime(hourOfDay, minute), -1);
                    },
                    h, m, true);
            if (MIN_TIME != null) {
                lt = LocalTime.parse(MIN_TIME);
                tpdE.setMinTime(lt.getHour(), lt.getMinute(), 0);
            }
            tpdE.show(getSupportFragmentManager(), "END_TIME_PICKER");
        });

        classCodeInput.setOnSpinnerItemSelectedListener((i, o, i1, t1) -> {
            classCodeInput.setError(null);
        });

        dayInput.setOnSpinnerItemSelectedListener((i, o, i1, t1) -> {
            dayInput.setError(null);
        });

        submitBtn.setOnClickListener(v -> {
            pDialog.show();
            String name = subjectNameInput.getText().toString().trim();
            int code = classCodeInput.getSelectedIndex();
            int day = dayInput.getSelectedIndex();
            String start = startTimeInput.getText().toString().trim();
            String end = endTimeInput.getText().toString().trim();

            boolean anyError = false;

            if(name.isEmpty()) {
                anyError = true;
                subjectNameInput.setError("Subject name is required");
            }

            if(code == -1) {
                anyError = true;
                classCodeInput.setError("Class code is required");
            }

            if(day == -1) {
                anyError = true;
                dayInput.setError("Day is required");
            }

            if(start.isEmpty()) {
                anyError = true;
                startTimeInput.setError("Start time is required");
            }

            if(end.isEmpty()) {
                anyError = true;
                endTimeInput.setError("End time is required");
            }

            if(!anyError) {
                updateData(name, code, day, start, end);
            } else {
                pDialog.dismiss();
            }
        });
    }

    private void updateData(String name, int code, int day, String start, String end) {

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("name", name);
        schedule.put("code", code);
        schedule.put("day", day);
        schedule.put("start", start);
        schedule.put("end", end);

        db.collection("schedule")
                .document(id)
                .update(schedule)
                .addOnCompleteListener(task -> pDialog.dismiss())
                .addOnSuccessListener(documentReference -> new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.SUCCESS)
                        .setTitle("Action Success")
                        .setMessage("Schedule successfully updated")
                        .show())
                .addOnFailureListener(e -> new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.ERROR)
                        .setTitle("Action Failed")
                        .setMessage("Failed to update schedule")
                        .show());
    }

    private void setData(String id) {
        pDialog.show();
        db.collection("schedule")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        String name = document.getString("name");
                        int code = document.getLong("code").intValue();
                        int day = document.getLong("day").intValue();
                        String start = document.getString("start");
                        String end = document.getString("end");

                        subjectNameInput.setText(name);
                        classCodeInput.selectItemByIndex(code);
                        dayInput.selectItemByIndex(day);
                        startTimeInput.setText(start);
                        endTimeInput.setText(end);

                        MIN_TIME = setMinMaxTime(start, 1);
                        MAX_TIME = setMinMaxTime(end, -1);

                    }
                    pDialog.dismiss();
                });
    }

    private String formatTime(int i, int n) {
        return fixLeadingZero(i) + ":" + fixLeadingZero(n);
    }

    private String fixLeadingZero(int i) {
        return i >= 10 ? String.valueOf(i) : "0" + i;
    }

    private String setMinMaxTime(String time, int min) {
        lt = LocalTime.parse(time);
        lt = lt.plusMinutes(min);
        return lt.toString();
    }
}
