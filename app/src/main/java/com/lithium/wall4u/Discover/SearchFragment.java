package com.lithium.wall4u.Discover;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lithium.wall4u.Favorites.FavouritesModel;
import com.lithium.wall4u.R;
import com.lithium.wall4u.WallpaperAttributes;
import com.lithium.wall4u.WallpaperBundleCustomAdapter;
import com.lithium.wall4u.databinding.SearchFragmentBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    Activity activity;
    SearchFragmentBinding binding;
    ArrayList<WallpaperAttributes> wallpaperAttributes = new ArrayList<>();
    WallpaperBundleCustomAdapter customAdapter;
    List<FavouritesModel> favourites = new ArrayList<>();

    String queryToSearch;

    int currentViews, scrolledUpViews, totalViews;
    int pageCount = 0;
    boolean isScrolling = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        binding = SearchFragmentBinding.bind(view);
        activity = (Activity) view.getContext();

        GridLayoutManager manager = new GridLayoutManager(activity, 2);
        customAdapter = new WallpaperBundleCustomAdapter(activity, wallpaperAttributes, favourites);
        binding.searchRecyclerView.setAdapter(customAdapter);
        binding.searchRecyclerView.setLayoutManager(manager);
        binding.progressSearch.setVisibility(View.GONE);

        //On End drawable listener of search bar.
        binding.searchBarTxtInput.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.searchBar.getText().toString().trim().isEmpty()) {
                    //Text Field is not empty.
                    hideKeyboard(activity);

                    queryToSearch = binding.searchBar.getText().toString().trim();
                    fetchDataFromPixabay(++pageCount, queryToSearch, false);

                } else {
                    Toast.makeText(activity, "Please search for a valid query", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.searchRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                if (isScrolling && (currentViews + scrolledUpViews == 70)) {
                    fetchDataFromPixabay(++pageCount, queryToSearch, true);
                }
            }
        });

        return view;
    }

    private void fetchDataFromPixabay(int pageCount, String query, boolean loadMore) {

        binding.progressSearch.setVisibility(View.VISIBLE);

        if (!loadMore) {
            binding.searchRecyclerView.setVisibility(View.GONE);
            wallpaperAttributes.clear();
        }
        binding.progressSearch.setVisibility(View.VISIBLE);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("pixabay.com")
                .appendPath("api/")
                .appendQueryParameter("key", "22947934-e9e0787c9402bf75179ef32b9")
                .appendQueryParameter("q", query)
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
                                    "https://pixabay.com/users/" + photographerName,
                                    photographerId);
                            wallpaperAttributes.add(wallHolder);

                        }
                    }

                    binding.searchRecyclerView.setVisibility(View.VISIBLE);
                    customAdapter.notifyDataSetChanged();
                    binding.progressSearch.setVisibility(View.GONE);

                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressSearch.setVisibility(View.GONE);
                        Toast.makeText(activity, "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on unsplash.
                        Log.w("Connectivity", "Connected to internet");

                        Log.w("Connectivity", "Move to unsplash");
                        fetchDataFromUnsplash(pageCount, query, loadMore);

                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        });

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);

    }

    private void fetchDataFromUnsplash(int pageCount, String query, boolean loadMore) {

        binding.progressSearch.setVisibility(View.VISIBLE);

        if (!loadMore) {
            binding.searchRecyclerView.setVisibility(View.GONE);
            wallpaperAttributes.clear();
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.unsplash.com")
                .appendPath("search")
                .appendPath("photos")
                .appendQueryParameter("client_id", "s1G8tiat_wyrDhglCqZnWbTvJPEImuaRHfLzNepbB1o")
                .appendQueryParameter("query", query)
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

                    binding.searchRecyclerView.setVisibility(View.VISIBLE);
                    customAdapter.notifyDataSetChanged();
                    binding.progressSearch.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    if (!isConnected()) {
                        binding.progressSearch.setVisibility(View.GONE);
                        Toast.makeText(activity, "Couldn't connect to servers.", Toast.LENGTH_SHORT).show();
                        Log.w("Connectivity", "No Connectivity");
                    } else {

                        //If there is connectivity
                        //It could be possible that hourly limit is over.
                        //In that case we will rely on unsplash.
                        Log.w("Connectivity", "Connected to internet");

                        Toast.makeText(activity, "Please try after some time", Toast.LENGTH_SHORT).show();
                        binding.progressSearch.setVisibility(View.GONE);

                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(request);

    }

    private boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
