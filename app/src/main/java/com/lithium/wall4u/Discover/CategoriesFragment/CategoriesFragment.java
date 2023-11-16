package com.lithium.wall4u.Discover.CategoriesFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.CategoriesFragmentBinding;

import java.util.ArrayList;

public class CategoriesFragment extends Fragment {

    CategoriesCustomAdapter adapter;
    ArrayList<CategoriesModel> categoriesModels = new ArrayList<>();
    CategoriesFragmentBinding binding;
    Activity activity;
    FirebaseFirestore database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.categories_fragment, container, false);
        binding = CategoriesFragmentBinding.bind(view);
        activity = (Activity) view.getContext();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database = FirebaseFirestore.getInstance();
        database.setFirestoreSettings(settings);

        GridLayoutManager manager = new GridLayoutManager(activity, 2);
        adapter = new CategoriesCustomAdapter(activity, categoriesModels);
        binding.categoriesRecyclerView.setAdapter(adapter);
        binding.categoriesRecyclerView.setLayoutManager(manager);

        getCategoriesFromFirebase();

        return view;
    }

    private void getCategoriesFromFirebase() {
        database.collection("category_items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {

                            Toast.makeText(getContext(), "Couldn't connect to servers.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            categoriesModels.clear();
                            for (DocumentSnapshot snapshot : value.getDocuments()) {

                                CategoriesModel model = snapshot.toObject(CategoriesModel.class);

                                categoriesModels.add(model);
                            }

                            adapter.notifyDataSetChanged();

                            binding.progressCategories.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
