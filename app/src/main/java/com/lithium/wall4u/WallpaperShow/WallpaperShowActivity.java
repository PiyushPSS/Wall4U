package com.lithium.wall4u.WallpaperShow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.WallpaperOpenActivityBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WallpaperShowActivity extends AppCompatActivity {

    WallpaperOpenActivityBinding binding;
    String originalPhoto, photographerName, photographerUrl, url, photoId;
    WallpaperManager wallpaperManager;
    OutputStream outputStream;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;

    private static final String TAG = "WallpaperShowActivity";

    private InterstitialAd mInterstitialAd;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.wallpaper_open_activity, null, false);
        setContentView(view);
        binding = WallpaperOpenActivityBinding.bind(view);

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent getData = getIntent();
        photoId = getData.getStringExtra("photoId");
        originalPhoto = getData.getStringExtra("originalPhoto");

        photographerName = getData.getStringExtra("photographerName");
        if (photographerName.trim().equals("")) {
            photographerName = "Unknown";
        }
        photographerUrl = getData.getStringExtra("photographerUrl");
        if (photographerUrl.trim().equals("")) {
            photographerUrl = "Unknown";
        }
        url = getData.getStringExtra("url");
        if (url.trim().equals("")) {
            url = "Unknown";
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadAd();
            }
        });

        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        //Back Button.
        binding.backWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInterstitial();
                finish();
            }
        });

        //Image View.
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));

        //Set Image in ImageView.
        Glide.with(WallpaperShowActivity.this)
                .load(originalPhoto)
                .placeholder(getResources().getDrawable(R.drawable.progress_img))
                .apply(requestOptions)
                .into(binding.imageWall);

        //Fav or not.
        SharedPreferences isProductPresentInFav = getSharedPreferences("fav", Context.MODE_PRIVATE);
        String check = isProductPresentInFav.getString(String.valueOf(photoId), "null");

        if (check.equals("y")) {
            //Present in favourites.
            binding.iconBeforeFavOpenWallpaper.setVisibility(View.GONE);
            binding.iconAfterFavOpenWallpaper.setVisibility(View.VISIBLE);
        } else {
            //Not Present in favourites.
            binding.iconBeforeFavOpenWallpaper.setVisibility(View.VISIBLE);
            binding.iconAfterFavOpenWallpaper.setVisibility(View.GONE);
        }

        //Info Button.
        binding.productInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageInfoBottomFragment infoBottomFragment = new ImageInfoBottomFragment();
                Bundle imageInfoBundle = new Bundle();
                imageInfoBundle.putString("photographer_name", photographerName);
                imageInfoBundle.putString("photographer_url", photographerUrl);
                imageInfoBundle.putString("image_url", url);
                infoBottomFragment.setArguments(imageInfoBundle);
                infoBottomFragment.show(getSupportFragmentManager(), "Image Info");
            }
        });

        binding.setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    View wallpaper_category = LayoutInflater.from(WallpaperShowActivity.this)
                            .inflate(R.layout.set_wallpaper_type_layout, null, false);

                    TextView setHomeScreen = wallpaper_category.findViewById(R.id.setHomeScreen);
                    TextView setLockScreen = wallpaper_category.findViewById(R.id.setLockScreen);

                    AlertDialog dialog = new AlertDialog.Builder(WallpaperShowActivity.this)
                            .setView(wallpaper_category)
                            .setCancelable(true)
                            .create();

                    dialog.show();

                    //HOME SCREEN.
                    setHomeScreen.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            ProgressDialog progressDialog = new ProgressDialog(WallpaperShowActivity.this);
                            progressDialog.setTitle("Setting Wallpaper");
                            progressDialog.setMessage("Please Wait..");
                            progressDialog.show();

                            Glide.with(WallpaperShowActivity.this).asBitmap().load(originalPhoto)
                                    .listener(new RequestListener<Bitmap>() {
                                        @SuppressLint("CheckResult")
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                    Target<Bitmap> target, boolean isFirstResource) {

                                            Toast.makeText(getApplicationContext(), "Something went wrong. " +
                                                    "\nTry again after some time", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            dialog.dismiss();
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model,
                                                                       Target<Bitmap> target, DataSource dataSource,
                                                                       boolean isFirstResource) {

                                            dialog.dismiss();

                                            setHomeScreenWallpaper(resource, progressDialog);

                                            return false;
                                        }
                                    }).submit();
                        }
                    });


                    //LOCK SCREEN.
                    setLockScreen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            ProgressDialog progressDialog = new ProgressDialog(WallpaperShowActivity.this);
                            progressDialog.setTitle("Setting Wallpaper");
                            progressDialog.setMessage("Please Wait..");
                            progressDialog.show();

                            Glide.with(WallpaperShowActivity.this).asBitmap().load(originalPhoto)
                                    .listener(new RequestListener<Bitmap>() {
                                        @SuppressLint("CheckResult")
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                    Target<Bitmap> target, boolean isFirstResource) {

                                            Toast.makeText(getApplicationContext(), "Something went wrong. " +
                                                    "\nTry again after some time", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model,
                                                                       Target<Bitmap> target, DataSource dataSource,
                                                                       boolean isFirstResource) {

                                            dialog.dismiss();

                                            setLockScreenWallpaper(resource, progressDialog);

                                            return false;
                                        }
                                    }).submit();

                        }
                    });

                } else {

                    //If the API < 24. Don't show the alert dialog.
                    //API<24 doesn't support Lock Screen.
                    ProgressDialog progressDialog = new ProgressDialog(WallpaperShowActivity.this);
                    progressDialog.setTitle("Setting Wallpaper");
                    progressDialog.setMessage("Please Wait..");
                    progressDialog.show();

                    Glide.with(WallpaperShowActivity.this).asBitmap()
                            .load(originalPhoto)
                            .listener(new RequestListener<Bitmap>() {
                                @SuppressLint("CheckResult")
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Bitmap> target, boolean isFirstResource) {

                                    Toast.makeText(getApplicationContext(), "Something went wrong. " +
                                            "\nTry again after some time", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model,
                                                               Target<Bitmap> target, DataSource dataSource,
                                                               boolean isFirstResource) {

                                    setHomeScreenWallpaper(resource, progressDialog);

                                    return false;
                                }
                            }).submit();

                }

            }
        });


        //Download Button.
        binding.productDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Permissions();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAd();
    }

    //Set Lock Screen Wallpaper.
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setLockScreenWallpaper(Bitmap resource, ProgressDialog progressDialog) {

        try {

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int phoneHeight = metrics.heightPixels;
            int phoneWidth = metrics.widthPixels;

            Bitmap bitmap = ThumbnailUtils.extractThumbnail(resource, phoneWidth,
                    phoneHeight);

            wallpaperManager.setBitmap(bitmap,
                    null, true, WallpaperManager.FLAG_LOCK);

            progressDialog.dismiss();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Something went wrong. " +
                    "\nTry again after some time", Toast.LENGTH_SHORT).show();
        }
    }

    //Set Home Screen.
    private void setHomeScreenWallpaper(Bitmap resource, ProgressDialog dialog) {

        try {

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int phoneHeight = metrics.heightPixels;
            int phoneWidth = metrics.widthPixels;

            Bitmap bitmap = ThumbnailUtils.extractThumbnail(resource, phoneWidth,
                    phoneHeight);
            wallpaperManager.setBitmap(bitmap);

            dialog.dismiss();

        } catch (IOException e) {
            e.printStackTrace();
            Log.w("mes", e.getMessage());
            Toast.makeText(getApplicationContext(), "Something went wrong. " +
                    "\nTry again after some time", Toast.LENGTH_SHORT).show();
        }

    }

    //Get Permissions.
    private void Permissions() {

        if (ContextCompat.checkSelfPermission(WallpaperShowActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            downloadImage();

        } else {

            requestPermissionFromUser();

        }

    }

    private void requestPermissionFromUser() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Important!!")
                    .setMessage("We need this permission to download this image.")
                    .setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ActivityCompat.requestPermissions(WallpaperShowActivity.this
                                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();

                        }
                    }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                downloadImage();

            } else {

                Toast.makeText(WallpaperShowActivity.this, "Permission to download has been denied", Toast.LENGTH_SHORT).show();

            }

        }

    }

    @SuppressLint("SetTextI18n")
    private void downloadImage() {
        //Do the downloading....
        File dir = new File(Environment.getExternalStorageDirectory(), "Wall4U");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File checkForSame = new File(Environment.getExternalStorageDirectory(), "Wall4U/" + photoId + ".jpg");

        if (!checkForSame.exists()) {

            new downloadImageFromServer(photoId, originalPhoto, dir).execute();

        } else {

            loadAd();
            Toast.makeText(WallpaperShowActivity.this, "This file already exists in your device", Toast.LENGTH_SHORT).show();
            SharedPreferences preferences = getSharedPreferences("adPremium", MODE_PRIVATE);
            String premiumCheck = preferences.getString("premium", "");
            if (!premiumCheck.equals("yes")) {
                showInterstitial();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showInterstitial();
        finish();
    }


    //Download.
    @SuppressLint("StaticFieldLeak")
    private class downloadImageFromServer extends AsyncTask<Void, Void, Void> {

        String photoId, originalPhoto;
        File dir;

        ProgressDialog dialog = new ProgressDialog(WallpaperShowActivity.this);

        public downloadImageFromServer(String photoId, String originalPhoto, File dir) {

            this.photoId = photoId;
            this.originalPhoto = originalPhoto;
            this.dir = dir;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.setTitle("Downloading Image");
            dialog.setCancelable(false);
            dialog.setMessage("Please Wait...");

            dialog.show();

            loadAd();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Glide.with(getApplicationContext()).asBitmap().load(originalPhoto).
                    listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Bitmap> target, boolean isFirstResource) {

                            Toast.makeText(getApplicationContext(), "Something went wrong." +
                                    "\nTry again after some time", Toast.LENGTH_SHORT).show();

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model,
                                                       Target<Bitmap> target, DataSource dataSource,
                                                       boolean isFirstResource) {

                            File file = new File(dir, photoId + ".jpg");
                            try {
                                outputStream = new FileOutputStream(file);

                                resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                try {
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.w("Wallpaper Download", "Error in flushing output stream");
                                }

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Something went wrong." +
                                        "\nTry again after some time", Toast.LENGTH_SHORT).show();

                            }
                            return false;
                        }
                    }).submit();

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void isSaved) {
            super.onPostExecute(isSaved);

            Handler beforeDownload = new Handler();

            beforeDownload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.setMessage("Almost Downloaded");
                }
            }, 2000);


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Toast.makeText(WallpaperShowActivity.this, "Downloaded", Toast.LENGTH_SHORT).show();

                    showInterstitial();

                }
            }, 5000);

        }
    }

    //Show the interstitial.
    private void showInterstitial() {
        SharedPreferences preferences = getSharedPreferences("adPremium", MODE_PRIVATE);
        String premiumCheck = preferences.getString("premium", "");
        if (!premiumCheck.equals("yes")) {
            // Show the ad if it's ready.
            if (mInterstitialAd != null) {
                mInterstitialAd.show(WallpaperShowActivity.this);
            } else {
                Log.w(TAG, "ad did not show");
                loadAd();
            }
        }
    }

    //Load the ad.
    private void loadAd() {
        SharedPreferences preferences = getSharedPreferences("adPremium", MODE_PRIVATE);
        String premiumCheck = preferences.getString("premium", "");
        if (!premiumCheck.equals("yes")) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(
                    this,
                    "ca-app-pub-6078310243369312/1784730786",
                    adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;
                            Log.w(TAG, "onAdLoaded");

                            interstitialAd.setFullScreenContentCallback(
                                    new FullScreenContentCallback() {
                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Called when fullscreen content is dismissed.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            mInterstitialAd = null;
                                            Log.w("TAG", "The ad was dismissed.");
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                                            // Called when fullscreen content failed to show.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            mInterstitialAd = null;
                                            Log.w("TAG", "The ad failed to show.");
                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            // Called when fullscreen content is shown.
                                            Log.w("TAG", "The ad was shown.");
                                        }
                                    });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.w(TAG, loadAdError.getMessage());
                            mInterstitialAd = null;

                            @SuppressLint("DefaultLocale") String error =
                                    String.format(
                                            "domain: %s, code: %d, message: %s",
                                            loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                            Log.w(TAG, "Error is " + error);
                        }
                    });
        }
    }
}
