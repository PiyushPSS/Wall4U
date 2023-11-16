package com.lithium.wall4u;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.lithium.wall4u.Discover.DiscoverFragment;
import com.lithium.wall4u.Favorites.FavouritesFragment;
import com.lithium.wall4u.PersonalProfile.LoginActivity;
import com.lithium.wall4u.PersonalProfile.PrivacyPolicyActivity;
import com.lithium.wall4u.PersonalProfile.ProfileFragment;
import com.lithium.wall4u.PersonalProfile.TermsAndConditionsActivity;
import com.lithium.wall4u.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    Fragment discoverFragment = new DiscoverFragment();
    Fragment favouritesFragment = new FavouritesFragment();
    Fragment profileFragment = new ProfileFragment();
    Fragment activeFragment = discoverFragment;
    ActivityMainBinding binding;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;

    BillingClient billingClient;
    ProgressDialog dialog;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_main, null, false);
        setContentView(view);
        binding = ActivityMainBinding.bind(view);

        dialog = new ProgressDialog(this);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(settings);
        auth = FirebaseAuth.getInstance();

        //Set up the billing client.
        setUpBillingClient();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemRippleColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        bottomNavigationView.setOnNavigationItemSelectedListener(itemSelectedListener);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                discoverFragment, "discover").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                favouritesFragment, "favourites").hide(favouritesFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                profileFragment, "profile").hide(profileFragment).commit();


        SharedPreferences preferences = getSharedPreferences("adPremium", MODE_PRIVATE);
        String premiumCheck = preferences.getString("premium", "");
        if (premiumCheck.equals("yes")) {
            binding.noAdsMainActivity.setVisibility(View.GONE);
        } else {
            binding.noAdsMainActivity.setVisibility(View.VISIBLE);
        }

        //No Ads.
        binding.noAdsMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                init();

            }
        });

        //More Options.
        binding.moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.inflate(R.menu.main_activity_more_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {

                            case R.id.settingsMenu:
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                return true;
                            case R.id.privacyPolicyMenu:
                                startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                                return true;
                            case R.id.termsAndCMenu:
                                startActivity(new Intent(MainActivity.this, TermsAndConditionsActivity.class));
                                return true;
                            case R.id.HelpMenu:
                                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
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

                                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                                try {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                }
                                            }
                                        })
                                        .create();

                                dialog.show();
                                return true;

                            case R.id.supportUsMenu:
                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                return true;

                            case R.id.checkForUpdatesMenu:
                                updateMethod();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void updateMethod() {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Checking for Updates");
        progressDialog.setMessage("Please wait..");

        progressDialog.show();

        AlertDialog updateDialog = new AlertDialog.Builder(MainActivity.this)
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

                                                    final String appPackageName = getPackageName();

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

    private final BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.discoverNav:
                            getSupportFragmentManager().beginTransaction().hide(activeFragment)
                                    .show(discoverFragment).commit();
                            activeFragment = discoverFragment;
                            break;
                        case R.id.favouriteNav:
                            getSupportFragmentManager().beginTransaction().hide(activeFragment)
                                    .show(favouritesFragment).commit();
                            activeFragment = favouritesFragment;
                            break;
                        case R.id.profileNav:
                            getSupportFragmentManager().beginTransaction().hide(activeFragment)
                                    .show(profileFragment).commit();
                            activeFragment = profileFragment;
                            if (auth.getCurrentUser() == null) {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                            break;
                    }
                    return true;
                }
            };


    private void setUpBillingClient() {
        billingClient = BillingClientSetup.getInstance(this, this);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.w("BILLING", "SUCCESS : " + 0);

                    List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                            .getPurchasesList();
                    handleItemAlreadyPurchased(purchases);

                } else {
                    Toast.makeText(MainActivity.this, "Error code : " + billingResult.getResponseCode()
                            , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w("Billing Service", "Disconnected from billing service");

            }
        });
    }

    private void handleItemAlreadyPurchased(List<Purchase> purchases) {

        for (Purchase purchase : purchases) {
            if (purchase.getSku().equals("no_ads")) {
                //If the item is purchased.
                SharedPreferences preferences = getSharedPreferences("adPremium", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("premium", "yes").apply();
            }
        }
    }

    private void init() {

        if (!isBillingClientReady()) {
            dialog.setMessage("Please wait while Billing opens...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            handler.postDelayed(inAppRunnable, 100);
        } else {

            Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();

            SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                    .setSkusList(Arrays.asList("no_ads"))
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                 @Nullable List<SkuDetails> list) {

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        launchBillingFlow(list.get(0));

                    } else {
                        Toast.makeText(MainActivity.this, "Something went wrong :" +
                                billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private final Runnable inAppRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBillingClientReady()) {
                handler.postDelayed(this, 1000);
            } else {
                dialog.dismiss();

                SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                        .setSkusList(Arrays.asList("no_ads"))
                        .setType(BillingClient.SkuType.INAPP)
                        .build();

                billingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     @Nullable List<SkuDetails> list) {

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                            launchBillingFlow(list.get(0));

                        } else {
                            Toast.makeText(MainActivity.this, "Something went wrong :" +
                                    billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    };

    private void launchBillingFlow(SkuDetails skuDetails) {

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();

        int response = billingClient.launchBillingFlow(MainActivity.this, billingFlowParams).getResponseCode();

        switch (response) {
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                Toast.makeText(this, "Billing unavailable", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                Toast.makeText(this, "Developer error", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                Toast.makeText(this, "Feature not supported", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                Toast.makeText(this, "You already own this item", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                Toast.makeText(this, "Service Disconnected", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                Toast.makeText(this, "Service Timeout", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private boolean isBillingClientReady() {

        return billingClient.isReady();

    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK &&
                list != null) {
            Toast.makeText(MainActivity.this, "Thank You! :)", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Restart the app to enable NO ADS PREMIUM", Toast.LENGTH_SHORT).show();
            handleItemAlreadyPurchased(list);
        }

        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                Toast.makeText(MainActivity.this, "Billing Unavailable", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                Toast.makeText(MainActivity.this, ":(\nPurchase Cancelled", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.OK:
                break;
            default:
                Toast.makeText(MainActivity.this, "Error : " + billingResult.getResponseCode() + "\nContact the developer to resolve", Toast.LENGTH_SHORT).show();
        }

    }
}