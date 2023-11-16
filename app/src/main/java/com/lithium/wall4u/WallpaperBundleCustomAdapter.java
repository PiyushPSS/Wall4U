package com.lithium.wall4u;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lithium.wall4u.Favorites.FavouritesModel;
import com.lithium.wall4u.PersonalProfile.LoginActivity;
import com.lithium.wall4u.WallpaperShow.WallpaperShowActivity;

import java.util.List;

public class WallpaperBundleCustomAdapter extends RecyclerView.Adapter<WallpaperBundleCustomAdapter.MyViewHolder> {

    Context context;
    List<WallpaperAttributes> wallpaperAttributes;
    List<FavouritesModel> favouritesList;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;

    public WallpaperBundleCustomAdapter(Context context,
                                        List<WallpaperAttributes> wallpaperAttributes,
                                        List<FavouritesModel> favourites) {
        this.context = context;
        this.wallpaperAttributes = wallpaperAttributes;
        this.favouritesList = favourites;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallaper_bundle_item_layout,
                null, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new MyViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        WallpaperAttributes singularPhoto = wallpaperAttributes.get(position);

        SharedPreferences preferences = context.getSharedPreferences("fav_pref", Context.MODE_PRIVATE);
        if (auth.getCurrentUser() != null) {
            String check = preferences.getString(singularPhoto.getId(), "");
            if (check.equals("y")) {
                //Is in favourites
                holder.icon_before_fav.setVisibility(View.GONE);
                holder.icon_after_fav.setVisibility(View.VISIBLE);

            } else {
                //Not in Favourites.
                holder.icon_before_fav.setVisibility(View.VISIBLE);
                holder.icon_after_fav.setVisibility(View.GONE);
            }
        } else {
            holder.icon_before_fav.setVisibility(View.VISIBLE);
            holder.icon_after_fav.setVisibility(View.GONE);
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));

        //Set Image in ImageView.
        Glide.with(context)
                .load(singularPhoto.getMediumPhoto())
                .placeholder(context.getResources().getDrawable(R.drawable.progress_img))
                .apply(requestOptions)
                .into(holder.wallpaperHolder);

        //To make fav icon.
        holder.icon_before_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser() == null) {

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setCancelable(true)
                            .setMessage("You need to sign-in to save images to favourites")
                            .setTitle("Sign In")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    context.startActivity(new Intent(context, LoginActivity.class));
                                }
                            })
                            .create();

                    dialog.show();

                } else {

                    //Add Wallpaper to favourites.
                    FavouritesModel addFavourites = new FavouritesModel(singularPhoto.getId(), singularPhoto.getMediumPhoto(),
                            singularPhoto.getOriginalPhoto(), singularPhoto.getOpenInPexels(),
                            singularPhoto.getPhotographerName(), singularPhoto.photographerId,
                            singularPhoto.photographerUrl);

                    addDataToFirebase(singularPhoto.getId(), addFavourites);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(singularPhoto.getId(), "y");
                    editor.apply();

                    holder.icon_before_fav.setVisibility(View.GONE);
                    holder.icon_after_fav.setVisibility(View.VISIBLE);

                }

            }
        });

        //After Fav onClickListener.
        holder.icon_after_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Remove from favourites.
                if (favouritesList.size() > holder.getAdapterPosition()) {
                    deleteDataFromFirebase(favouritesList.get(holder.getAdapterPosition()));

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(singularPhoto.getId()).apply();
                }

                holder.icon_after_fav.setVisibility(View.GONE);
                holder.icon_before_fav.setVisibility(View.VISIBLE);

            }
        });

        //Set OnClick Listener On Image.
        holder.wallpaperHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent goToWall = new Intent(context, WallpaperShowActivity.class);
                goToWall.putExtra("photoId", String.valueOf(singularPhoto.getId()));
                goToWall.putExtra("originalPhoto", singularPhoto.getOriginalPhoto());
                goToWall.putExtra("photographerName", singularPhoto.getPhotographerName());
                goToWall.putExtra("photographerUrl", singularPhoto.getPhotographerUrl());
                goToWall.putExtra("url", singularPhoto.getOpenInPexels());
                context.startActivity(goToWall);
            }
        });
    }

    private void deleteDataFromFirebase(FavouritesModel favourites) {

        if (auth.getCurrentUser() != null) {
            firebaseFirestore.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .collection("favourites")
                    .document(favourites.getPhotoId())
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(context, "Something went wrong while removing favourites. It can be because of bad internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void addDataToFirebase(String id, FavouritesModel favourites) {

        if (auth.getCurrentUser() != null) {

            firebaseFirestore.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .collection("favourites")
                    .document(id)
                    .set(favourites)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(context,
                                        "Something went wrong while saving favourites. " +
                                                "It can be because of bad internet connection.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return wallpaperAttributes.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView wallpaperHolder;
        ImageView icon_before_fav, icon_after_fav;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            wallpaperHolder = itemView.findViewById(R.id.show_wall_in_discover);
            icon_before_fav = itemView.findViewById(R.id.icon_before_fav);
            icon_after_fav = itemView.findViewById(R.id.icon_after_fav);
        }
    }
}
