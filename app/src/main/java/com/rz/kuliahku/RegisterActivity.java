package com.rz.kuliahku;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.techiness.progressdialoglibrary.ProgressDialog;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText name, nim, password;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button btn;
    private TextView showLogin;
    private ProgressDialog pDialog;
    private TextInputLayout layoutPassword;
    private String nameInput = null, nimInput = null, pwInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pDialog = new ProgressDialog(this);

        name = findViewById(R.id.nameInput);
        nim = findViewById(R.id.nimInput);
        password = findViewById(R.id.passwordInput);
        layoutPassword = findViewById(R.id.layoutPassword);

        btn = findViewById(R.id.btn);
        showLogin = findViewById(R.id.showLogin);

        showLogin.setOnClickListener(v -> finish());

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                layoutPassword.setEndIconVisible(true);
                password.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btn.setOnClickListener(v -> {

            nameInput = name.getText().toString().trim();
            nimInput = nim.getText().toString().trim();
            pwInput = password.getText().toString().trim();

            pDialog.show();
            boolean anyError = false;

            if (nameInput.isEmpty()) {
                anyError = true;
                name.setError("Name is required");
            }

            if (nimInput.isEmpty()) {
                anyError = true;
                nim.setError("NIM is required");
            } else {
                if (nimInput.length() != 11) {
                    anyError = true;
                    nim.setError("NIM is invalid");
                }
            }

            if (pwInput.isEmpty()) {
                anyError = true;
                layoutPassword.setEndIconVisible(false);
                password.setError("Password is required");
            }
            if (!anyError) {
                db.collection("users")
                        .whereEqualTo("nim", nim.getText().toString().trim())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = task.getResult();
                                if (snapshot.size() >= 1) {
                                    nim.setError("NIM already registered");
                                    pDialog.dismiss();
                                } else {
                                    registerUser(nameInput, nimInput, pwInput);
                                }
                            }
                        });
            } else {
                pDialog.dismiss();
            }
        });
    }

    private void registerUser(String name, String nim, String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("nim", nim);
        user.put("password", hashed);

        db.collection("users")
                .add(user)
                .addOnCompleteListener(task -> {
                    pDialog.dismiss();
                })
                .addOnSuccessListener(documentReference -> {
                    new AestheticDialog.Builder(this, DialogStyle.FLAT, DialogType.SUCCESS)
                            .setTitle("Success")
                            .setMessage("Successfully registered")
                            .setCancelable(false)
                            .setOnClickListener(sDialog -> finish())
                            .show();
                })
                .addOnFailureListener(e -> new AestheticDialog.Builder(this, DialogStyle.FLAT, DialogType.ERROR)
                        .setTitle("Error")
                        .setMessage("Failed to registered")
                        .show());
    }
}