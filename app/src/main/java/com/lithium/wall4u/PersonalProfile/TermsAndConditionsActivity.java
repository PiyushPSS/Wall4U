package com.lithium.wall4u.PersonalProfile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.ActivityTermsAndCondtionsBinding;

public class TermsAndConditionsActivity extends AppCompatActivity {

    ActivityTermsAndCondtionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_terms_and_condtions,
                null, false);
        setContentView(view);
        binding = ActivityTermsAndCondtionsBinding.bind(view);

        binding.goBackTAndC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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