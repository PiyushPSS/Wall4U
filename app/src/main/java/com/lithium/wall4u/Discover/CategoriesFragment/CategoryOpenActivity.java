package com.lithium.wall4u.Discover.CategoriesFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lithium.wall4u.Favorites.FavouritesModel;
import com.lithium.wall4u.R;
import com.lithium.wall4u.WallpaperAttributes;
import com.lithium.wall4u.WallpaperBundleCustomAdapter;
import com.lithium.wall4u.databinding.CategoryOpenLayoutBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CategoryOpenActivity extends AppCompatActivity {

    CategoryOpenLayoutBinding binding;
    WallpaperBundleCustomAdapter adapter;
    GridLayoutManager manager;
    ArrayList<WallpaperAttributes> wallpaperAttributes = new ArrayList<>();

    String name;
    int pageCount = 0;

    List<FavouritesModel> favourites = new ArrayList<>();

    int scrolledViews, currentViews, totalViews;
    boolean isScrolling;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.category_open_layout, null, false);
        setContentView(view);
        binding = CategoryOpenLayoutBinding.bind(view);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        manager = new GridLayoutManager(CategoryOpenActivity.this, 2);
        adapter = new WallpaperBundleCustomAdapter(CategoryOpenActivity.this, wallpaperAttributes,
                favourites);

        binding.categoryOpenRecyclerView.setAdapter(adapter);
        binding.categoryOpenRecyclerView.setLayoutManager(manager);

        binding.categoryOpenName.setText(name + " Wallpapers");

        //Check if premium.
        SharedPreferences preferences = getSharedPreferences("adPremium", MODE_PRIVATE);
        String premiumCheck = preferences.getString("premium", "");

        if (!premiumCheck.equals("yes")) {
            //Set Ad.
            MobileAds.initialize(CategoryOpenActivity.this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            AdView adView = new AdView(CategoryOpenActivity.this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId("ca-app-pub-6078310243369312/1257428810");
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    binding.adTextCategoryOpen.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    binding.adTextCategoryOpen.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    binding.adTextCategoryOpen.setVisibility(View.GONE);
                }
            });
            adView.loadAd(adRequest);
            binding.adHolderCategoryOpen.addView(adView);
        } else {
            binding.adHolderCategoryOpen.setVisibility(View.GONE);
        }

        getDataFromUnsplash(++pageCount);

        binding.goBackCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        binding.categoryOpenRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentViews = manager.getChildCount();
                totalViews = manager.getItemCount();
                scrolledViews = manager.findFirstVisibleItemPosition();

                if (isScrolling && (currentViews + scrolledViews == totalViews)) {
                    getDataFromUnsplash(++pageCount);
                }
            }
        });
    }

    private void getDataFromUnsplash(int pageCount) {

        binding.progressCategoryOpen.setVisibility(View.VISIBLE);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.unsplash.com")
                .appendPath("search")
                .appendPath("photos")
                .appendQueryParameter("client_id", "s1G8tiat_wyrDhglCqZnWbTvJPEImuaRHfLzNepbB1o")
                .appendQueryParameter("query", name)
                .appendQueryParameter("page", String.valueOf(pageCount))
                .appendQueryParameter("per_page", "30")
                .appendQueryParameter("order_by", "latest");

        StringRequest request = new StringRequest(Request.Method.GET,
                builder.build().toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray photosArray = jsonObject.getJSONArray("results");

                    for (int i = 0; i < photosArray.length(); i++) {

                        JSONObject photosObject = photosArray.getJSONObject(i);

                        //ID.
                        String id = photosObject.getString("id");

                        //Open In Website.
                        JSONObject getUrl = photosObject.getJSONObject("links");
                        String openInURL = getUrl.getString("html");

                        //Photographer Details.
                        JSONObject user = photosObject.getJSONObject("user");
                        String photographerName = user.getString("name");
                        String photographerId = user.getString("id");

                        JSONObject links = user.getJSONObject("links");
                        String photographerUrl = links.getString("html");

                        //Image.
                        JSONObject imageObject = photosObject.getJSONObject("urls");
                        String originalImage = imageObject.getString("regular");
                        String mediumImage = imageObject.getString("small");


                        WallpaperAttributes wallHolder;
                        wallHolder = new WallpaperAttributes(id,
                                originalImage, mediumImage, openInURL,
                                photographerName, photographerUrl, photographerId);
                        wallpaperAttributes.add(wallHolder);

                    }

                    adapter.notifyDataSetChanged();
                    binding.progressCategoryOpen.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressCategoryOpen.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),
                                "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on unsplash.
                        Log.w("Connectivity", "Connected to internet");

                        getDataFromPixabay(pageCount);

                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    private void getDataFromPixabay(int pageCount) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("pixabay.com")
                .appendPath("api/")
                .appendQueryParameter("key", "22947934-e9e0787c9402bf75179ef32b9")
                .appendQueryParameter("q", name)
                .appendQueryParameter("per_page", "200")
                .appendQueryParameter("orientation", "vertical")
                .appendQueryParameter("page", String.valueOf(pageCount));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET
                , builder.build().toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray photosArray = response.getJSONArray("hits");

                    for (int i = 0; i < photosArray.length(); i++) {

                        JSONObject photosObject = photosArray.getJSONObject(i);

                        String type = photosObject.getString("type");
                        if (!type.contains("vector")) {

                            String id = String.valueOf(photosObject.getInt("id"));
                            String openInPexels = photosObject.getString("pageURL");
                            String photographerName = photosObject.getString("user");
                            String photographerId = photosObject.getString("user_id");

                            String originalImage = photosObject.getString("largeImageURL");
                            String mediumImage = photosObject.getString("webformatURL");


                            WallpaperAttributes wallHolder;
                            wallHolder = new WallpaperAttributes(id,
                                    originalImage, mediumImage, openInPexels,
                                    photographerName,
                                    "https://pixabay.com/users/" + photographerName, photographerId);
                            wallpaperAttributes.add(wallHolder);

                        }

                    }

                    adapter.notifyDataSetChanged();
                    binding.progressCategoryOpen.setVisibility(View.GONE);

                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressCategoryOpen.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on pexels.
                        Log.w("Connectivity", "Connected to internet");

                        Toast.makeText(getApplicationContext(), "No response from servers", Toast.LENGTH_SHORT).show();


                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        });

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }

    private boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}
