package com.lithium.wall4u.PersonalProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lithium.wall4u.MainActivity;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_login, null, false);
        setContentView(view);
        binding = ActivityLoginBinding.bind(view);

        auth = FirebaseAuth.getInstance();

        binding.progressLogin.setVisibility(View.GONE);

        //Login Button OnClick Listener.
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.password.setEnabled(false);
                binding.emailEdTxt.setEnabled(false);
                binding.progressLogin.setVisibility(View.VISIBLE);
                binding.loginButton.setEnabled(false);

                if (Objects.requireNonNull(binding.emailEdTxt.getText()).toString().trim().equals("") ||
                        Objects.requireNonNull(binding.password.getText()).toString().trim().equals("")) {

                    //Empty.
                    Toast.makeText(LoginActivity.this, "Please enter valid credentials", Toast.LENGTH_SHORT).show();

                    binding.password.setEnabled(true);
                    binding.emailEdTxt.setEnabled(true);
                    binding.progressLogin.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);

                } else {

                    //Not Empty.
                    auth.signInWithEmailAndPassword(binding.emailEdTxt.getText().toString().trim(),
                            binding.password.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();

                            //If success.
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("token", "added");
                            startActivity(intent);
                            finish();

                            binding.password.setEnabled(true);
                            binding.emailEdTxt.setEnabled(true);
                            binding.progressLogin.setVisibility(View.GONE);
                            binding.loginButton.setEnabled(true);

                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(LoginActivity.this, "Incorrect credentials",
                                    Toast.LENGTH_SHORT).show();

                            binding.password.setEnabled(true);
                            binding.emailEdTxt.setEnabled(true);
                            binding.progressLogin.setVisibility(View.GONE);
                            binding.loginButton.setEnabled(true);

                        }
                    });

                }

            }
        });


        //On Privacy Policy click.
        binding.privacyPolicyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, PrivacyPolicyActivity.class));
            }
        });

        //OnBack Pressed.
        binding.goBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //On Terms and Conditions.
        binding.termsAndConditionsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this, TermsAndConditionsActivity.class));

            }
        });

        //On Forgot password.
        binding.forgotPwdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });


        //Create Account.
        binding.createAccountLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}