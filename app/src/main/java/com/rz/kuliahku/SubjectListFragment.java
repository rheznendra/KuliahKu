package com.rz.kuliahku;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rz.kuliahku.adapter.ParentList;
import com.rz.kuliahku.model.ParentSubject;
import com.rz.kuliahku.model.Subject;
import com.techiness.progressdialoglibrary.ProgressDialog;

import java.util.ArrayList;

public class SubjectListFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Subject> subjectList;
    private ArrayList<ParentSubject> parentList;
    private Subject subject;
    private ParentSubject parentSubject;
    private ParentList adapter;

    private TextInputLayout layoutFDay, layoutFCD;
    private MaterialAutoCompleteTextView filter_day, filter_class;
    private FloatingActionButton addBtn;
    private RecyclerView rv_parent;
    private String[] arrayDays, arrayClass;
    private int posD = -1, posC = -1;

    private ProgressDialog pDialog;
    private SharedPreferences preferences;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayDays = getResources().getStringArray(R.array.days);
        arrayClass = getResources().getStringArray(R.array.class_code);
        preferences = this.getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", null);
        pDialog = new ProgressDialog(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_list, container, false);
        filter_day = view.findViewById(R.id.filter_day);
        filter_class = view.findViewById(R.id.filter_class);
        addBtn = view.findViewById(R.id.addBtn);
        addBtn.setColorFilter(Color.WHITE);
        parentList = new ArrayList<>();

        addBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddSubjectActivity.class));
        });
        rv_parent = view.findViewById(R.id.rv_parent);

        rv_parent.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adapter = new ParentList(this.getContext(), parentList);
        rv_parent.setAdapter(adapter);

        ArrayAdapter<String> adapter_day = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, arrayDays);
        filter_day.setAdapter(adapter_day);

        ArrayAdapter<String> adapter_class = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, arrayClass);
        filter_class.setAdapter(adapter_class);

        layoutFDay = view.findViewById(R.id.layoutFDay);
        layoutFCD = view.findViewById(R.id.layoutFCD);

        filter_day.setOnItemClickListener((parent, view1, position, id) -> {
            layoutFDay.setStartIconDrawable(R.drawable.ic_baseline_close_24);
            layoutFDay.setStartIconOnClickListener(v -> {
                getData(-1, posC);
                posD = -1;
                layoutFDay.setStartIconDrawable(null);
                filter_day.setText("");
            });
            posD = position;
            getData(position, posC);
        });

        filter_class.setOnItemClickListener((parent, view1, position, id) -> {
            layoutFCD.setStartIconDrawable(R.drawable.ic_baseline_close_24);
            layoutFCD.setStartIconOnClickListener(v -> {
                getData(posD, -1);
                posC = -1;
                layoutFCD.setStartIconDrawable(null);
                filter_class.setText("");
            });
            posC = position;
            getData(posD, position);
        });

        return view;
    }

    public void getData(int fd, int fc) {
        pDialog.show();
        subjectList = new ArrayList<>();
        parentList.clear();
        Query query = db.collection("schedule");
        if (fd >= 0) {
            query = query.whereEqualTo("day", fd);
        }
        if (fc >= 0) {
            query = query.whereEqualTo("code", fc);
        }
        query.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String start = document.getString("start");
                            String end = document.getString("end");
                            String code = arrayClass[document.getLong("code").intValue()];
                            String day = arrayDays[document.getLong("day").intValue()];

                            subject = new Subject(id, name, code, day, start, end);
                            subjectList.add(subject);
                        }
                        for (String arrayDay : arrayDays) {
                            ArrayList<Subject> childList = new ArrayList<>();
                            for (int j = 0; j < subjectList.size(); j++) {
                                if (subjectList.get(j).getDay().equals(arrayDay)) {
                                    childList.add(subjectList.get(j));
                                }
                            }
                            if (childList.size() > 0) {
                                parentSubject = new ParentSubject(arrayDay, childList);
                                parentList.add(parentSubject);
                            }
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
        getData(-1, -1);
    }
}