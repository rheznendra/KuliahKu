package com.rz.kuliahku;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techiness.progressdialoglibrary.ProgressDialog;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutMeFragment extends Fragment {

    private ImageView editBtn, settingBtn;
    private SharedPreferences preferences;
    private TextView name, nim, address, email, phone, birth;
    private CircleImageView img;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = this.getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
        pDialog = new ProgressDialog(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_me, container, false);

        name = view.findViewById(R.id.profileName);
        nim = view.findViewById(R.id.profileNim);
        address = view.findViewById(R.id.addresItem);
        email = view.findViewById(R.id.emailItem);
        phone = view.findViewById(R.id.pNumberItem);
        birth = view.findViewById(R.id.birthDayItem);
        img = view.findViewById(R.id.profileImage);

        editBtn = view.findViewById(R.id.editBtn);
        settingBtn = view.findViewById(R.id.settingBtn);

        settingBtn.setOnClickListener(v -> {
            startActivity(new Intent(this.getActivity(), SettingsActivity.class));
        });

        editBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AboutMeEditActivity.class));
        });

        return view;
    }

    private void getData() {
        pDialog.show();
        String userId = preferences.getString("userId", null);
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        name.setText(document.getString("name"));
                        nim.setText(document.getString("nim"));
                        if (document.getString("address") != null) {
                            address.setText(document.getString("address"));
                        }
                        if (document.getString("email") != null) {
                            email.setText(document.getString("email"));
                        }
                        if (document.getString("phone") != null) {
                            phone.setText(document.getString("phone"));
                        }
                        if (document.getString("birth") != null) {
                            birth.setText(document.getString("birth"));
                        }
                        if (document.getString("img") != null) {
                            Glide.with(this).load(document.getString("img")).into(img);
                        }
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