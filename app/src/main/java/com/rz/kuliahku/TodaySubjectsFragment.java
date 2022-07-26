package com.rz.kuliahku;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rz.kuliahku.adapter.SubjectList;
import com.rz.kuliahku.model.Subject;
import com.techiness.progressdialoglibrary.ProgressDialog;

import java.util.ArrayList;
import java.util.Calendar;

public class TodaySubjectsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Subject> subjectList;
    private Subject subject;
    private SubjectList adapter;
    private RecyclerView rv_subjects;
    private ProgressDialog pDialog;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = this.getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        pDialog = new ProgressDialog(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_subjects, container, false);
        getData();
        subjectList = new ArrayList<>();

        rv_subjects = view.findViewById(R.id.rv_subjects);
        rv_subjects.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adapter = new SubjectList(this.getContext(), subjectList, false);
        rv_subjects.setAdapter(adapter);
        return view;
    }

    public void getData() {
        pDialog.show();
        Calendar calendar = Calendar.getInstance();
        int d = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String userId = preferences.getString("userId", null);
        db.collection("schedule")
                .whereEqualTo("day", d)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    subjectList.clear();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String start = document.getString("start");
                            String end = document.getString("end");
                            String code = getResources().getStringArray(R.array.class_code)[document.getLong("code").intValue()];
                            String day = getResources().getStringArray(R.array.days)[document.getLong("day").intValue()];

                            subject = new Subject(id, name, code, day, start, end);
                            subjectList.add(subject);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("get data", "Error getting documents.", task.getException());
                    }
                    pDialog.dismiss();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }
}