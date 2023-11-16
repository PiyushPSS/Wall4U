package com.lithium.wall4u.PersonalProfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.lithium.wall4u.R;
import com.lithium.wall4u.SettingsActivity;
import com.lithium.wall4u.databinding.ProfileFragmentLayoutBinding;

public class ProfileFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    Activity activity;
    ProfileFragmentLayoutBinding binding;
    String name = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment_layout, container, false);
        activity = (Activity) view.getContext();
        binding = ProfileFragmentLayoutBinding.bind(view);

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            //There is a current user.
            binding.isNoUserLayout.setVisibility(View.GONE);
            binding.userPresentLayout.setVisibility(View.VISIBLE);

            getNameFromFirebase();

        } else {

            //No User.
            binding.isNoUserLayout.setVisibility(View.VISIBLE);
            binding.userPresentLayout.setVisibility(View.GONE);

        }

        //Sign-In Button.
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent goToSignIn = new Intent(activity, LoginActivity.class);
                startActivity(goToSignIn);

            }
        });


        //Privacy Policy.
        binding.privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, PrivacyPolicyActivity.class));
            }
        });

        //Terms & Conditions.
        binding.termsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, TermsAndConditionsActivity.class));
            }
        });

        //Log Out.
        binding.logOutActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setTitle("Log Out")
                        .setMessage("Do you want to log out?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (auth.getCurrentUser() != null) {

                                    auth.signOut();
                                    dialogInterface.dismiss();
                                    activity.recreate();

                                }
                            }
                        })
                        .create();

                dialog.show();
            }
        });

        //Settings Layout.
        binding.settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, SettingsActivity.class));
            }
        });


        //help and support.
        binding.helpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setTitle("Help & Feedback")
                        .setMessage("Hey, if you have any issues regarding the application or you want to give your feedback, you can simply click on 'OPEN PLAY STORE' button and write a review regarding your problem or your feedback. We usually reply within 24 hours.")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Open Play Store", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.lithium.wall4u"));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .create();

                dialog.show();
            }
        });


        //Rate us.
        binding.rateUsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.lithium.wall4u"));
                    startActivity(intent);
                } catch (Exception e) {

                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //Check for updates.
        binding.checkForUpdatesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMethod();
            }
        });

        return view;
    }

    private void updateMethod() {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Checking for Updates");
        progressDialog.setMessage("Please wait..");

        progressDialog.show();

        AlertDialog updateDialog = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .create();

        firebaseFirestore.collection("update")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {

                        if (error == null) {

                            for (DocumentSnapshot snapshot : value.getDocuments()) {

                                String newVersionAvailable = snapshot.getString("update");

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        progressDialog.dismiss();


                                        if (newVersionAvailable.equals("no")) {

                                            updateDialog.setMessage("You are up to date!");
                                            updateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            });

                                        } else {
                                            updateDialog.setTitle("Update Available");
                                            updateDialog.setMessage("Please update your app!");
                                            updateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Update", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    final String appPackageName = activity.getPackageName();

                                                    // getPackageName() from Context or Activity object
                                                    try {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                    } catch (android.content.ActivityNotFoundException anfe) {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                    }

                                                }
                                            });

                                            updateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Remind me later", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            });

                                        }

                                        updateDialog.show();
                                    }
                                }, 2000);

                            }

                        } else {

                            updateDialog.setMessage("Try again after some time!\nServer Issue!");
                            updateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            updateDialog.show();

                        }

                    }
                });

    }

    @SuppressLint("SetTextI18n")
    private void getNameFromFirebase() {

        if (auth.getCurrentUser() != null) {
            firebaseFirestore.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value,
                                            @Nullable FirebaseFirestoreException error) {

                            if (error == null) {
                                name = value.getString("fullName");

                                if (name != null) {
                                    if (name.contains(" ")) {
                                        name = name.substring(0, name.indexOf(" "));
                                    }
                                    binding.heyText.setText("Hey, " + name + "!");
                                }
                            }

                        }
                    });
        }

    }
}
