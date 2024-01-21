package com.kasir.app;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Patterns;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kasir.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    private boolean doubleBackToExitPressedOnce = false;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button buttonLogin;
    private static final String TAG = "LoginActivity";

    
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
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress_bar);

        EditText editTextEmail = binding.editTextEmail;
        EditText editTextPassword = binding.editTextPassword;
        TextView txtRegister = binding.txtRegister;
        TextView txtReset = binding.txtReset;
        buttonLogin = findViewById(R.id.buttonLogin);

        txtRegister.setPaintFlags(txtRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtReset.setPaintFlags(txtReset.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtRegister.setOnClickListener(
                v -> {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                });

        txtReset.setOnClickListener(
                v -> {
                    Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                    startActivity(intent);
                });

        

        buttonLogin.setOnClickListener(
                v -> {
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();

                    if (email.isEmpty()) {
                        Toast.makeText(
                                        LoginActivity.this,
                                        "Email tidak boleh kosong",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else if (password.isEmpty()) {
                        Toast.makeText(
                                        LoginActivity.this,
                                        "Password tidak boleh kosong",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else if (!isValidEmail(email)) {
                        Toast.makeText(
                                        LoginActivity.this,
                                        "Format email tidak valid",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        login(email, password);
                    }
                });

    }

   
    private void login(String email, String password) {
        setInProgress(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()
                                        && task.getResult() != null
                                        && task.getResult().getUser() != null) {
                                    setInProgress(false);
                                    Toast.makeText(
                                                    LoginActivity.this,
                                                    "Login Berhasil",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    reload();
                                } else {
                                    setInProgress(false);
                                    Toast.makeText(
                                                    LoginActivity.this,
                                                    "Login gagal. Periksa email dan password Anda.",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
    }

    private void reload() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        }
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
