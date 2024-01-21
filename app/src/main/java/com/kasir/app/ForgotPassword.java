package com.kasir.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.kasir.app.databinding.ActivityForgotpasswordBinding;

public class ForgotPassword extends AppCompatActivity {
    private ActivityForgotpasswordBinding binding;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button buttonReset;

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.forgotpassword_progress_bar);

        EditText editTextEmail = binding.editTextEmail;
        buttonReset = findViewById(R.id.buttonReset);

        buttonReset.setOnClickListener(
                v -> {
                    String email = editTextEmail.getText().toString();
                    if (email.isEmpty()) {
                        Toast.makeText(
                                        ForgotPassword.this,
                                        "Email tidak boleh kosong",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else if (!isValidEmail(email)) {
                        Toast.makeText(
                                        ForgotPassword.this,
                                        "Format email tidak valid",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        resetPassword(email);
                    }
                });
    }

    private void resetPassword(String email) {
        setInProgress(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(
                                                ForgotPassword.this,
                                                "Email Reset Kata Sandi Sedang Dikirim ke Email Anda!",
                                                Toast.LENGTH_SHORT)
                                        .show();
                                reload();
                                setInProgress(false);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                                ForgotPassword.this,
                                                "Gagal Kirim Email!",
                                                Toast.LENGTH_SHORT)
                                        .show();
                                setInProgress(false);
                            }
                        });
    }

    private void reload() {
        Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            buttonReset.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonReset.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
