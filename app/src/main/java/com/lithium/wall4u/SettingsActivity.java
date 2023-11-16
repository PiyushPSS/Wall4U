package com.lithium.wall4u;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.lithium.wall4u.PersonalProfile.UserModel;
import com.lithium.wall4u.databinding.ActivitySettingsBinding;
import com.lithium.wall4u.databinding.DeleteAccountLayoutBinding;
import com.lithium.wall4u.databinding.PersonalSettingsBinding;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String fullName = "";
    String email = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_settings, null, false);
        setContentView(view);

        binding = ActivitySettingsBinding.bind(view);
        auth = FirebaseAuth.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(settings);

        //Version Number.
        binding.versionNumber.setText("version :" + getVersion());

        //Personal Settings.
        if (auth.getCurrentUser() != null) {
            binding.personalSettings.setVisibility(View.VISIBLE);
            binding.deleteAccount.setVisibility(View.VISIBLE);
            getDataFromFirebase();
        } else {
            binding.personalSettings.setVisibility(View.GONE);
            binding.deleteAccount.setVisibility(View.GONE);
        }

        //Personal Settings OnClick Listener.
        binding.personalSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personalSettingsMethod();
            }
        });

        //Back Button.
        binding.goBackSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Clear Data.
        binding.clearAllDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllDataMethod();
            }
        });

        //Clear Cache.
        binding.clearCacheLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCacheMethod();
            }
        });


        //Delete Account.
        binding.deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View deleteAccountView = getLayoutInflater().inflate(R.layout.delete_account_layout, null, false);
                DeleteAccountLayoutBinding binding = DeleteAccountLayoutBinding.bind(deleteAccountView);

                AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                        .setCancelable(true)
                        .setView(deleteAccountView)
                        .create();

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();

                binding.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                binding.deleteAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (auth.getCurrentUser() != null) {


                            if (!binding.currPass.getText().toString().trim().isEmpty()) {

                                AuthCredential credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(),
                                        binding.currPass.getText().toString().trim());
                                auth.getCurrentUser().reauthenticate(credential)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Toast.makeText(SettingsActivity.this, "Please wait", Toast.LENGTH_SHORT).show();

                                                String uid = auth.getCurrentUser().getUid();

                                                auth.getCurrentUser().delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    firebaseFirestore.collection("users")
                                                                            .document(uid)
                                                                            .delete()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (!task.isSuccessful()) {
                                                                                        Toast.makeText(SettingsActivity.this, "Something went wrong while deleting the data", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });

                                                                    Toast.makeText(SettingsActivity.this, "User Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                                    //If success.
                                                                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                                                    intent.putExtra("token", "added");
                                                                    dialog.dismiss();
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {

                                Toast.makeText(SettingsActivity.this, "Please enter a valid password", Toast.LENGTH_SHORT).show();

                            }

                        }

                    }
                });


            }
        });
    }

    private void getDataFromFirebase() {

        if (auth.getCurrentUser() != null) {
            firebaseFirestore.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (error == null) {
                                if (value.exists()) {
                                    UserModel model = value.toObject(UserModel.class);

                                    fullName = model.getFullName();
                                    email = model.getEmail();
                                }
                            }

                        }
                    });
        }

    }

    private void clearCacheMethod() {

        //Clear the cache.
        AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                .setCancelable(true)
                .setTitle("Clear the Cache?")
                .setMessage("Do you want to clear the app's cache?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearApplicationCache();
                    }
                })
                .create();

        dialog.show();


    }

    private void clearAllDataMethod() {

        //Clear the data.
        AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                .setCancelable(true)
                .setTitle("Clear the Data?")
                .setMessage("Do you want to clear the app's all data, files and cache?\nThis action cannot be undone.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean result = clearApplicationData();
                        if (result) {
                            Toast.makeText(SettingsActivity.this, "Data cleared", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();

        dialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void personalSettingsMethod() {

        View view = getLayoutInflater().inflate(R.layout.personal_settings, null, false);

        AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                .setCancelable(true)
                .setView(view)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        PersonalSettingsBinding binding = PersonalSettingsBinding.bind(view);
        binding.currPassLayout.setVisibility(View.GONE);
        binding.newPassLayout.setVisibility(View.GONE);
        binding.reNewPassLayout.setVisibility(View.GONE);
        binding.fullNameChange.setHint(fullName);
        binding.email.setHint(email);
        binding.email.setEnabled(false);

        //On Change Password Click.
        binding.changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.changePassword.setVisibility(View.GONE);
                binding.currPassLayout.setVisibility(View.VISIBLE);
                binding.newPassLayout.setVisibility(View.VISIBLE);
                binding.reNewPassLayout.setVisibility(View.VISIBLE);
            }
        });

        binding.saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullNameFromEd = binding.fullNameChange.getText().toString().trim();
                if (binding.currPassLayout.getVisibility() == View.VISIBLE) {

                    String currentPassword = binding.currPass.getText().toString().trim();
                    String newPassword = binding.newPass.getText().toString().trim();
                    String newPasswordAgain = binding.reEnterNewPass.getText().toString().trim();

                    if (currentPassword.equals("") || newPassword.equals("") || newPasswordAgain.equals("")) {
                        Toast.makeText(SettingsActivity.this, "Please enter appropriate password",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        if (newPassword.equals(newPasswordAgain)) {

                            AuthCredential authCredential = EmailAuthProvider.getCredential(email, currentPassword);
                            auth.getCurrentUser().reauthenticate(authCredential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                //Change the password.
                                                auth.getCurrentUser().updatePassword(newPassword)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (!task.isSuccessful()) {
                                                                    Toast.makeText(SettingsActivity.this, "Something went wrong while changing password. Try again after some time.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(SettingsActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                            } else {
                                                Toast.makeText(SettingsActivity.this, "Something went wrong. Please try again after sometime", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(SettingsActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }

                    }


                }

                if (fullNameFromEd.equals("")) {
                    //Empty/
                    Toast.makeText(SettingsActivity.this,
                            "Please enter an appropriate name", Toast.LENGTH_SHORT).show();
                } else {

                    if (!fullNameFromEd.equals(fullName)) {
                        if (auth.getCurrentUser() != null) {
                            firebaseFirestore.collection("users")
                                    .document(auth.getCurrentUser().getUid())
                                    .update("fullName", fullNameFromEd)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                dialog.dismiss();
                                                Toast.makeText(SettingsActivity.this,
                                                        "Name Changed", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(SettingsActivity.this,
                                                        "Something went wrong while changing data",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, "The name is same", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });


        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

            }
        });

        dialog.show();

    }

    private String getVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            if (packageInfo.versionName != null) {
                return packageInfo.versionName;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }


    //Clear Data.
    private boolean clearApplicationData() {
        return ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
                .clearApplicationUserData();
    }


    //Cache clear.
    public void clearApplicationCache() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}