package com.lithium.wall4u.PersonalProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lithium.wall4u.MainActivity;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_sign_up, null, false);
        setContentView(view);

        binding = ActivitySignUpBinding.bind(view);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.progressSignUp.setVisibility(View.GONE);

        //Create Account.
        binding.createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.progressSignUp.setVisibility(View.VISIBLE);
                binding.password.setEnabled(false);
                binding.reEnterPassword.setEnabled(false);
                binding.emailEdTxt.setEnabled(false);
                binding.fullName.setEnabled(false);
                binding.createAccount.setEnabled(false);

                if (Objects.requireNonNull(binding.fullName.getText()).toString().trim().equals("") ||
                        Objects.requireNonNull(binding.emailEdTxt.getText()).toString().trim().equals("") ||
                        Objects.requireNonNull(binding.password.getText()).toString().trim().equals("") ||
                        Objects.requireNonNull(binding.reEnterPassword.getText()).toString().trim().equals("")) {

                    //If any of them is empty.
                    Toast.makeText(SignUpActivity.this, "Please Enter Proper Details",
                            Toast.LENGTH_SHORT).show();
                    binding.progressSignUp.setVisibility(View.VISIBLE);
                    binding.password.setEnabled(true);
                    binding.reEnterPassword.setEnabled(true);
                    binding.emailEdTxt.setEnabled(true);
                    binding.fullName.setEnabled(true);
                    binding.createAccount.setEnabled(true);

                } else {

                    if (binding.password.getText().toString().trim().equals(
                            binding.reEnterPassword.getText().toString().trim()
                    )) {
                        saveDataInAuth(binding.emailEdTxt.getText().toString().trim(),
                                binding.password.getText().toString().trim());
                    } else {

                        Toast.makeText(SignUpActivity.this, "Passwords do not match",
                                Toast.LENGTH_SHORT).show();
                        binding.progressSignUp.setVisibility(View.GONE);
                        binding.password.setEnabled(true);
                        binding.reEnterPassword.setEnabled(true);
                        binding.emailEdTxt.setEnabled(true);
                        binding.fullName.setEnabled(true);
                        binding.createAccount.setEnabled(true);
                    }

                }

            }
        });


        //Terms and Conditions.
        binding.termsAndConditionsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, TermsAndConditionsActivity.class));
            }
        });

        //PrivacyPolicy.
        binding.privacyPolicyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, PrivacyPolicyActivity.class));
            }
        });

        //Login button.
        binding.createAccountLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        //On Back Pressed.
        binding.goBackSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void saveDataInAuth(String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        UserModel model = new UserModel(binding.fullName.getText().toString().trim(),
                                email);

                        binding.progressSignUp.setVisibility(View.GONE);
                        //On Success.
                        firebaseFirestore.collection("users")
                                .document(authResult.getUser().getUid())
                                .set(model)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        Toast.makeText(SignUpActivity.this,
                                                "Signed Up Successfully", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        intent.putExtra("token", "added");
                                        startActivity(intent);
                                        finish();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.progressSignUp.setVisibility(View.GONE);
                        binding.password.setEnabled(true);
                        binding.reEnterPassword.setEnabled(true);
                        binding.emailEdTxt.setEnabled(true);
                        binding.fullName.setEnabled(true);
                        binding.createAccount.setEnabled(true);

                    }
                });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}