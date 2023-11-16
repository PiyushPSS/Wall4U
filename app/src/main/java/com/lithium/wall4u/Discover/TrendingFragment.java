package com.lithium.wall4u.Discover;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lithium.wall4u.Favorites.FavouritesModel;
import com.lithium.wall4u.R;
import com.lithium.wall4u.WallpaperAttributes;
import com.lithium.wall4u.WallpaperBundleCustomAdapter;
import com.lithium.wall4u.databinding.TrendingFragmentBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrendingFragment extends Fragment {

    Activity activity;
    TrendingFragmentBinding binding;
    ArrayList<WallpaperAttributes> wallpaperAttributes = new ArrayList<>();
    WallpaperBundleCustomAdapter customAdapter;
    ProductsDatabaseHelper databaseHelper;
    FirebaseFirestore firebaseFirestore;
    List<FavouritesModel> favourites = new ArrayList<>();

    String websiteCheck = "";

    private static final int TIME_TO_UPDATE = 1;

    int currentViews, scrolledUpViews, totalViews;
    int pageCount = 0;
    ArrayList<Integer> alreadyShowedPages = new ArrayList<>();
    boolean isScrolling = false;

    final int min = 1;
    final int max = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trending_fragment, container, false);
        binding = TrendingFragmentBinding.bind(view);
        activity = (Activity) view.getContext();
        databaseHelper = new ProductsDatabaseHelper(activity);
        firebaseFirestore = FirebaseFirestore.getInstance();

        GridLayoutManager manager = new GridLayoutManager(activity, 2);
        customAdapter = new WallpaperBundleCustomAdapter(activity, wallpaperAttributes, favourites);
        binding.trendingRecyclerView.setAdapter(customAdapter);
        binding.trendingRecyclerView.setLayoutManager(manager);
        binding.progressTrending.setVisibility(View.GONE);

        pageCount = new Random().nextInt((max - min) + 1) + min;

        //Check if the data is present in cache Database, if yes then check the date.
        Cursor readData = databaseHelper.readAllData(ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME);
        if (readData.getCount() == 0) {
            alreadyShowedPages.add(pageCount);
            fetchWallpaper(pageCount, true);
        } else {
            readData.moveToFirst();
            int dateOfUpdateCache = Integer.parseInt(readData.getString(8));
            int currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            if (currentDate < dateOfUpdateCache) {
                Log.w("Trending Fragment", "From cache");
                getDataFromCache();
            } else {
                Log.w("Trending Fragment", "Fetch New Wallpapers");
                fetchWallpaper(pageCount, true);
            }
        }

        binding.trendingRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                scrolledUpViews = manager.findFirstVisibleItemPosition();

                if (!websiteCheck.equals("pixabay")) {
                    if (isScrolling && (currentViews + scrolledUpViews == totalViews - 2)) {
                        pageCount = new Random().nextInt((max - min) + 1) + min;
                        for (int i = 0; i < alreadyShowedPages.size(); i++) {
                            if (alreadyShowedPages.contains(pageCount)) {
                                pageCount = new Random().nextInt((max - min) + 1) + min;
                            } else {
                                break;
                            }
                        }
                        alreadyShowedPages.add(pageCount);
                        if (alreadyShowedPages.size() != 5) {
                            fetchWallpaper(pageCount, false);
                        }
                    }
                }
            }
        });

        return view;
    }

    //This Gets the wallpapers from Pexels.
    private void fetchWallpaper(int pageCount, boolean toDelete) {

        binding.progressTrending.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.GET,
                "https://api.pexels.com/v1/curated/?page=" + pageCount + "&per_page=80&orientation=portrait",
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {

                        websiteCheck = "pexels";

                        if (toDelete) {
                            //Delete All the data first.
                            databaseHelper.deleteAll(ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME);
                        }

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray photosArray = jsonObject.getJSONArray("photos");

                            for (int i = 0; i < photosArray.length(); i++) {

                                JSONObject photosObject = photosArray.getJSONObject(i);

                                String id = String.valueOf(photosObject.getInt("id"));
                                String openInPexels = photosObject.getString("url");
                                String photographerName = photosObject.getString("photographer");
                                String photographerUrl = photosObject.getString("photographer_url");
                                String photographerId = photosObject.getString("photographer_id");

                                JSONObject imageObject = photosObject.getJSONObject("src");
                                String originalImage = imageObject.getString("large2x");
                                String mediumImage = imageObject.getString("medium");

                                boolean addProduct;

                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.DAY_OF_MONTH, +TIME_TO_UPDATE);
                                int DateToUpdate = calendar.get(Calendar.DAY_OF_MONTH);

                                addProduct = databaseHelper.addProduct(
                                        ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME,
                                        id, mediumImage, originalImage, openInPexels, photographerName,
                                        photographerId, photographerUrl,
                                        String.valueOf(DateToUpdate));
                                if (!addProduct) {
                                    Toast.makeText(activity, "Something Went Wrong",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }

                            getDataFromCache();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressTrending.setVisibility(View.GONE);
                        Toast.makeText(activity, "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on unsplash.
                        Log.w("Connectivity", "Connected to internet");
                        Log.w("Trending Fragment", "Going towards Unsplash");

                        getDataFromUnsplash(pageCount, toDelete);

                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("Authorization", "563492ad6f91700001000001039d137d82504e3d8cdc14c07d6ff53e");

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(request);

    }

    //Data from Unsplash.
    private void getDataFromUnsplash(int pageCount, boolean toDelete) {

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                "https://api.unsplash.com/photos/?client_id=s1G8tiat_wyrDhglCqZnWbTvJPEImuaRHfLzNepbB1o&page="
                        + pageCount + "&per_page=30&order_by=popular",
                null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                websiteCheck = "unsplash";

                if (toDelete) {
                    //Delete All the data first.
                    databaseHelper.deleteAll(ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME);
                }

                try {

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject photosObject = response.getJSONObject(i);

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

                        boolean addProduct;

                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, +TIME_TO_UPDATE);
                        int DateToUpdate = calendar.get(Calendar.DAY_OF_MONTH);

                        addProduct = databaseHelper.addProduct(
                                ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME,
                                id, mediumImage, originalImage, openInURL, photographerName,
                                photographerId, photographerUrl,
                                String.valueOf(DateToUpdate));
                        if (!addProduct) {
                            Toast.makeText(activity, "Something Went Wrong",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    getDataFromCache();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressTrending.setVisibility(View.GONE);
                        Toast.makeText(activity, "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on unsplash.
                        Log.w("Connectivity", "Connected to internet");
                        Log.w("Trending Fragment", "Going towards Pixabay");

                        fetchFromPixabay(toDelete);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(request);
    }

    private void fetchFromPixabay(boolean toDelete) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("pixabay.com")
                .appendPath("api/")
                .appendQueryParameter("key", "22947934-e9e0787c9402bf75179ef32b9")
                .appendQueryParameter("q", "trending")
                .appendQueryParameter("per_page", "200")
                .appendQueryParameter("orientation", "vertical")
                .appendQueryParameter("page", "1");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET
                , builder.build().toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                websiteCheck = "pixabay";

                if (toDelete) {
                    //Delete All the data first.
                    databaseHelper.deleteAll(ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME);
                }

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

                            boolean addProduct;

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DAY_OF_MONTH, +TIME_TO_UPDATE);
                            int DateToUpdate = calendar.get(Calendar.DAY_OF_MONTH);

                            addProduct = databaseHelper.addProduct(
                                    ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME,
                                    id, mediumImage, originalImage, openInPexels, photographerName,
                                    photographerId, "https://pixabay.com/users/" + photographerName,
                                    String.valueOf(DateToUpdate));
                            if (!addProduct) {
                                Toast.makeText(activity, "Something Went Wrong",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                    }

                    getDataFromCache();

                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressTrending.setVisibility(View.GONE);
                        Toast.makeText(activity, "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on unsplash.
                        Log.w("Connectivity", "Connected to internet");

                        Toast.makeText(activity, "Please try after some time", Toast.LENGTH_SHORT).show();
                        binding.progressTrending.setVisibility(View.GONE);

                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        });

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataFromCache() {

        Cursor readAllData = databaseHelper.readAllData(ProductsDatabaseHelper.PRODUCT_CACHE_TABLE_NAME);
        if (readAllData.getCount() != 0) {

            while (readAllData.moveToNext()) {
                WallpaperAttributes attributes = new WallpaperAttributes(
                        readAllData.getString(1),
                        readAllData.getString(3), readAllData.getString(2),
                        readAllData.getString(4), readAllData.getString(5),
                        readAllData.getString(7), readAllData.getString(6));

                wallpaperAttributes.add(attributes);
            }

            binding.progressTrending.setVisibility(View.GONE);
            customAdapter.notifyDataSetChanged();

        }
    }

    private boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}
