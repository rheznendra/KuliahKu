package com.rz.kuliahku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techiness.progressdialoglibrary.ProgressDialog;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutMeEditActivity extends AppCompatActivity {

    private RelativeLayout backBtn;
    private EditText birthDayInput, name, nim, phone, email, address;
    private Button cancelBtn, saveBtn;
    private CircleImageView img;

    private ProgressDialog pDialog;
    private SharedPreferences preferences;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private String userId;
    private ActivityResultLauncher<Intent> activityResultLauncherPhoto;
    private ActivityResultLauncher<String> activityResultLauncherGallery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me_edit);

        pDialog = new ProgressDialog(this);

        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", null);

        backBtn = findViewById(R.id.backBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        backBtn.setOnClickListener(v -> finish());
        cancelBtn.setOnClickListener(v -> finish());

        saveBtn = findViewById(R.id.saveBtn);

        name = findViewById(R.id.nameInput);
        nim = findViewById(R.id.nimInput);
        phone = findViewById(R.id.pNumberInput);
        address = findViewById(R.id.addressInput);
        email = findViewById(R.id.emailInput);
        img = findViewById(R.id.profileImage);

        birthDayInput = findViewById(R.id.birthDayInput);
        birthDayInput.setOnClickListener(v -> {
            MaterialDatePicker<Long> mdp = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build();
            mdp.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            mdp.addOnPositiveButtonClickListener(selection -> birthDayInput.setText(mdp.getHeaderText()));
        });

        img.setOnClickListener(v -> {
            CharSequence[] itemImage = {"Take a photo", "Choose from gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(itemImage, (DialogInterface dialog, int i) -> {
                if (i == 0) {
                    Intent intentPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activityResultLauncherPhoto.launch(intentPhoto);
                } else if (i == 1) {
                    activityResultLauncherGallery.launch("image/*");
                } else {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        activityResultLauncherPhoto =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    Thread thread = new Thread(() -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle bundle = result.getData().getExtras();
                            Bitmap bitmap = (Bitmap) bundle.get("data");
                            img.post(() -> img.setImageBitmap(bitmap));
                        }
                    });
                    thread.start();
                });

        activityResultLauncherGallery = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                img.setImageURI(result);
            }
        });

        saveBtn.setOnClickListener(v -> {
            boolean anyError = false;
            if (name.getText().toString().trim().isEmpty()) {
                anyError = true;
                name.setError("Full name is required");
            }

            if (nim.getText().toString().trim().isEmpty()) {
                anyError = true;
                nim.setError("NIM is required");
            } else {
                if (nim.getText().toString().length() != 11) {
                    anyError = true;
                    nim.setError("NIM is invalid");
                }
            }

            if (!anyError) {
                checkNim(nim.getText().toString().trim());
            }
        });
        getData();
    }

    private void checkNim(String n) {
        pDialog.show();
        Drawable imgDefault = getResources().getDrawable(R.drawable.icons8_user_144px);
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!n.equals(document.getString("nim"))) {
                            db.collection("users")
                                    .whereEqualTo("nim", n)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task.isSuccessful()) {
                                            if (task2.getResult().size() >= 1) {
                                                nim.setError("NIM already used.");
                                                pDialog.dismiss();
                                            } else {
                                                if (img.getDrawable().getConstantState().equals(imgDefault.getConstantState())) {
                                                    updateUser(name.getText().toString().trim(), nim.getText().toString().trim(),
                                                            birthDayInput.getText().toString(),
                                                            phone.getText().toString().trim(), address.getText().toString().trim(),
                                                            email.getText().toString().trim(), null);
                                                } else {
                                                    uploadImage();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            if (img.getDrawable().getConstantState().equals(imgDefault.getConstantState())) {
                                updateUser(name.getText().toString().trim(), nim.getText().toString().trim(),
                                        birthDayInput.getText().toString(),
                                        phone.getText().toString().trim(), address.getText().toString().trim(),
                                        email.getText().toString().trim(), null);
                            } else {
                                uploadImage();
                            }
                        }
                    }
                });
    }

    private void uploadImage() {
        img.setDrawingCacheEnabled(true);
        img.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        byte[] byteData = outputStream.toByteArray();

        StorageReference reference = storage.getReference("images").child("IMG" + new Date().getTime() + ".jpeg");
        UploadTask uploadTask = reference.putBytes(byteData);
        uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(task -> {
                            String imgURL = task.getResult().toString();
                            updateUser(name.getText().toString().trim(), nim.getText().toString().trim(),
                                    birthDayInput.getText().toString(),
                                    phone.getText().toString().trim(), address.getText().toString().trim(),
                                    email.getText().toString().trim(), imgURL);
                        });
                    }
                })
                .addOnFailureListener(e -> new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.ERROR)
                        .setTitle("Action Failed")
                        .setMessage("Failed to update data")
                        .show());
    }

    private void updateUser(String name, String nim, String birth, String phone, String address, String email, String imgUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("nim", nim);
        user.put("birth", birth.isEmpty() ? null : birth);
        user.put("phone", phone.isEmpty() ? null : phone);
        user.put("email", email.isEmpty() ? null : email);
        user.put("address", email.isEmpty() ? null : address);
        if(imgUrl != null) {
            user.put("img", imgUrl);
        }

        db.collection("users")
                .document(userId)
                .update(user)
                .addOnCompleteListener(task -> pDialog.dismiss())
                .addOnSuccessListener(unused -> new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.SUCCESS)
                        .setTitle("Action Success")
                        .setMessage("Data successfully updated")
                        .show())
                .addOnFailureListener(e -> new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.ERROR)
                        .setTitle("Action Failed")
                        .setMessage("Failed to update data")
                        .show());
    }

    private void getData() {
        pDialog.show();
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        name.setText(document.getString("name"));
                        nim.setText(document.getString("nim"));
                        if (document.getString("email") != null) {
                            email.setText(document.getString("email"));
                        }
                        if (document.getString("phone") != null) {
                            phone.setText(document.getString("phone"));
                        }
                        if (document.getString("birth") != null) {
                            birthDayInput.setText(document.getString("birth"));
                        }
                        if (document.getString("address") != null) {
                            address.setText(document.getString("address"));
                        }
                        if (document.getString("img") != null) {
                            Glide.with(this).load(document.getString("img")).into(img);
                        }
                    }
                    pDialog.dismiss();
                });
    }
}