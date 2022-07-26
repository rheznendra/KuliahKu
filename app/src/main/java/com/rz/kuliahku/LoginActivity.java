package com.rz.kuliahku;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.techiness.progressdialoglibrary.ProgressDialog;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;

import org.mindrot.jbcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText nim, password;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences preferences;
    private TextView showRegister;
    private Button btn;
    private LinearLayout mainLayout;
    private TextInputLayout layoutPassword;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pDialog = new ProgressDialog(this);
        mainLayout = findViewById(R.id.mainLayout);
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);

        nim = findViewById(R.id.nimInput);
        password = findViewById(R.id.passwordInput);
        layoutPassword = findViewById(R.id.layoutPassword);

        btn = findViewById(R.id.btn);
        showRegister = findViewById(R.id.showRegister);

        showRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

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

        btn.setOnClickListener(v ->
        {
            pDialog.show();
            boolean anyError = false;
            if (nim.getText().toString().trim().isEmpty()) {
                anyError = true;
                nim.setError("NIM is required");
            }

            if (password.getText().toString().isEmpty()) {
                anyError = true;
                layoutPassword.setEndIconVisible(false);
                password.setError("Password is required");
            }
            if (!anyError) {
                checkLogin(nim.getText().toString(), password.getText().toString());
            } else {
                pDialog.dismiss();
            }
        });
    }

    private void checkLogin(String nim, String password) {
        db.collection("users")
                .whereEqualTo("nim", nim)
                .get()
                .addOnCompleteListener(task -> {
                    pDialog.dismiss();
                    int error = 1;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (BCrypt.checkpw(password, document.getString("password"))) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("userId", document.getId());
                                editor.apply();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                                error = 0;
                            }
                        }
                        if (error == 1) {
                            new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.ERROR)
                                    .setTitle("Login Failed")
                                    .setMessage("NIM or password is invalid")
                                    .show();
                        }
                    } else {
                        new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.ERROR)
                                .setTitle("Login Failed")
                                .setMessage("NIM or Password is invalid")
                                .show();
                        Log.d("get data", "error getting documents", task.getException());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nim.setText("");
        password.setText("");
        nim.setError(null);
        password.setError(null);
    }
}