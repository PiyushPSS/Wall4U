package com.lithium.wall4u.PersonalProfile;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_forgot_password, null, false);
        setContentView(view);

        binding = ActivityForgotPasswordBinding.bind(view);
        auth = FirebaseAuth.getInstance();

        binding.checkEmail.setVisibility(View.GONE);
        binding.progressReset.setVisibility(View.GONE);

        //Go Back.
        binding.goBackForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Reset Password.
        binding.resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.resetButton.setEnabled(false);
                binding.progressReset.setVisibility(View.VISIBLE);

                if (binding.emailEdTxt.getText().toString().trim().equals("")) {

                    Toast.makeText(ForgotPasswordActivity.this, "Please enter valid E-Mail address",
                            Toast.LENGTH_SHORT).show();
                    binding.progressReset.setVisibility(View.GONE);
                    binding.resetButton.setEnabled(true);

                } else {

                    if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEdTxt.getText().toString().trim()).matches()) {

                        //If it doesn't match.
                        Toast.makeText(ForgotPasswordActivity.this, "Please enter valid E-Mail address",
                                Toast.LENGTH_SHORT).show();
                        binding.progressReset.setVisibility(View.GONE);
                        binding.resetButton.setEnabled(true);

                    } else {

                        //If it matches.
                        auth.sendPasswordResetEmail(binding.emailEdTxt.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            binding.checkEmail.setVisibility(View.VISIBLE);

                                        } else {

                                            Toast.makeText(ForgotPasswordActivity.this,
                                                    "Try Again. Something wrong happened",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                        binding.progressReset.setVisibility(View.GONE);
                                        binding.resetButton.setEnabled(true);

                                    }
                                });
                    }
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}