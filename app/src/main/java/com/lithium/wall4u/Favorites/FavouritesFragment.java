package com.lithium.wall4u.Favorites;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.lithium.wall4u.PersonalProfile.LoginActivity;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.FavouritesFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    Activity activity;
    FavouritesFragmentBinding binding;
    FavouritesCustomAdapter customAdapter;
    List<FavouritesModel> favourites = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favourites_fragment, container, false);
        binding = FavouritesFragmentBinding.bind(view);
        activity = (Activity) view.getContext();

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            binding.beforeLoginLayout.setVisibility(View.GONE);
            binding.afterLoginLayout.setVisibility(View.VISIBLE);
            getDataFromFirebase();

            GridLayoutManager manager = new GridLayoutManager(activity, 2);
            customAdapter = new FavouritesCustomAdapter(activity, favourites);

            binding.favouritesRecyclerView.setAdapter(customAdapter);
            binding.favouritesRecyclerView.setLayoutManager(manager);
            binding.progressFavourites.setVisibility(View.GONE);
        } else {
            binding.beforeLoginLayout.setVisibility(View.VISIBLE);
            binding.afterLoginLayout.setVisibility(View.GONE);
        }

        binding.favLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, LoginActivity.class));
            }
        });

        return view;
    }

    private void getDataFromFirebase() {

        if (auth.getCurrentUser() != null) {
            binding.progressFavourites.setVisibility(View.VISIBLE);

            firebaseFirestore.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .collection("favourites")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error == null) {

                                favourites.clear();

                                for (DocumentSnapshot snapshot : value.getDocuments()) {

                                    FavouritesModel fav = snapshot.toObject(FavouritesModel.class);
                                    favourites.add(fav);

                                }

                                customAdapter.notifyDataSetChanged();
                                binding.progressFavourites.setVisibility(View.GONE);
                            }
                        }
                    });
        }

    }
}
