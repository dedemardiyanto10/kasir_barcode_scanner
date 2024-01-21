package com.kasir.app;

import android.view.View;
import android.widget.ProgressBar;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Patterns;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kasir.app.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    
    private boolean doubleBackToExitPressedOnce = false;
    private FirebaseAuth mAuth;

    private static final String TAG = "RegisterActivity";
    private Button buttonRegister;
    private ProgressBar progressBar;

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Tekan kembali sekali lagi untuk keluar", Toast.LENGTH_SHORT)
                    .show();
            new Handler(Looper.getMainLooper())
                    .postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.register_progress_bar);

        EditText editTextName = binding.editTextName;
        EditText editTextEmail = binding.editTextEmail;
        EditText editTextPassword = binding.editTextPassword;
        EditText editTextRepassword = binding.editTextRepassword;
        TextView txtLogin = binding.txtLogin;
        buttonRegister = findViewById(R.id.buttonRegister);

        txtLogin.setPaintFlags(txtLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtLogin.setOnClickListener(
                v -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });

        buttonRegister.setOnClickListener(
                v -> {
                    String name = editTextName.getText().toString();
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();
                    String repassword = editTextRepassword.getText().toString();

                    if (!name.isEmpty()
                            && !email.isEmpty()
                            && !password.isEmpty()
                            && !repassword.isEmpty()) {

                        // Check for a valid email format using regex
                        if (isValidEmail(email)) {

                            if (password.equals(repassword)) {

                                if (name.length() >= 8 && password.length() >= 8) {
                                    register(name, email, password);
                                } else {
                                    Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Nama dan password harus memiliki minimal 8 karakter.",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            } else {
                                Toast.makeText(
                                                RegisterActivity.this,
                                                "Password tidak cocok!",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            Toast.makeText(
                                            RegisterActivity.this,
                                            "Format email tidak valid!",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Toast.makeText(
                                        RegisterActivity.this,
                                        "Semua kolom harus diisi!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void register(String name, String email, String password) {
        setInProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    FirebaseUser firebaseUser = task.getResult().getUser();
                                    if (firebaseUser != null) {
                                        UserProfileChangeRequest request =
                                                new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .build();

                                        firebaseUser
                                                .updateProfile(request)
                                                .addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(
                                                                    @NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    setInProgress(false);
                                                                    Toast.makeText(
                                                                                    RegisterActivity
                                                                                            .this,
                                                                                    "Pendaftaran Berhasil! Silahkan Login!",
                                                                                    Toast
                                                                                            .LENGTH_SHORT)
                                                                            .show();
                                                                    reload();
                                                                } else {
                                                                    setInProgress(false);
                                                                    Toast.makeText(
                                                                                    RegisterActivity
                                                                                            .this,
                                                                                    "Autentikasi gagal!",
                                                                                    Toast
                                                                                            .LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                            }
                                                        });
                                    } else {
                                        setInProgress(false);
                                        Toast.makeText(
                                                        RegisterActivity.this,
                                                        "Autentikasi gagal!",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                } else {
                                    setInProgress(false);
                                    Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Email sudah terdaftar!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        }
    }

    private void reload() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
