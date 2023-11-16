package com.lithium.wall4u.Discover.CategoriesFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lithium.wall4u.R;

import java.util.ArrayList;

public class CategoriesCustomAdapter extends RecyclerView.Adapter<CategoriesCustomAdapter.MyViewHolder> {

    ArrayList<CategoriesModel> model;
    Activity activity;

    public CategoriesCustomAdapter(Activity activity, ArrayList<CategoriesModel> model) {
        this.activity = activity;
        this.model = model;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(activity).inflate(R.layout.category_item_layout, parent, false);

        return new MyViewHolder(view);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        CategoriesModel singularCategory = model.get(position);

        holder.categoryName.setText(singularCategory.getName());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));

        //Set Image in ImageView.
        Glide.with(activity)
                .load(singularCategory.getImage())
                .placeholder(activity.getResources().getDrawable(R.drawable.progress_img))
                .apply(requestOptions)
                .into(holder.categoryImage);

        holder.categoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToCategory = new Intent(activity, CategoryOpenActivity.class);
                goToCategory.putExtra("name", singularCategory.getName());
                activity.startActivity(goToCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImage;
        TextView categoryName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryImage = itemView.findViewById(R.id.categories_image);
            categoryName = itemView.findViewById(R.id.categories_title);
        }
    }
}