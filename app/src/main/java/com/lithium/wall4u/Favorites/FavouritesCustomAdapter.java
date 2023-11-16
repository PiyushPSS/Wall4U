package com.lithium.wall4u.Favorites;

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
import com.lithium.wall4u.R;
import com.lithium.wall4u.WallpaperShow.WallpaperShowActivity;

import java.util.List;

public class FavouritesCustomAdapter extends
        RecyclerView.Adapter<FavouritesCustomAdapter.MyViewHolder> {

    Context context;
    List<FavouritesModel> favouritesList;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    public FavouritesCustomAdapter(Context context, List<FavouritesModel> favourites) {
        this.context = context;
        this.favouritesList = favourites;
    }

    @NonNull
    @Override
    public FavouritesCustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallaper_bundle_item_layout,
                null, false);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new FavouritesCustomAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouritesCustomAdapter.MyViewHolder holder, int position) {

        FavouritesModel singularPhoto = favouritesList.get(position);

        holder.icon_before_fav.setVisibility(View.GONE);

        //Set Image in ImageView.
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));

        //Set Image in ImageView.
        Glide.with(context)
                .load(singularPhoto.getMediumPic())
                .placeholder(context.getResources().getDrawable(R.drawable.progress_img))
                .apply(requestOptions)
                .into(holder.wallpaperHolder);

        //Set OnClick Listener On Image.
        holder.wallpaperHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToWall = new Intent(context, WallpaperShowActivity.class);
                goToWall.putExtra("photoId", String.valueOf(singularPhoto.getPhotoId()));
                goToWall.putExtra("originalPhoto", singularPhoto.getOriginalPic());
                goToWall.putExtra("photographerName", singularPhoto.getPhotographerName());
                goToWall.putExtra("photographerUrl", singularPhoto.getPhotographerUrl());
                goToWall.putExtra("url", singularPhoto.getOpenInWebsite());
                context.startActivity(goToWall);

            }
        });

        //Delete Data From Favourites.
        holder.icon_after_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Add Dialog Box.
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage("Do you want to remove this photo from favourites?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

//                                viewModel.delete(singularPhoto);
                                deleteDataFromFirebase(singularPhoto);
                                SharedPreferences preferences = context.getSharedPreferences("fav_pref", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove(singularPhoto.getPhotoId()).apply();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();

                dialog.show();

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
                                Toast.makeText(context, "Something went wrong while removing favourites." +
                                        " It can be because of bad internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
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
