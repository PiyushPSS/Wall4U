package com.lithium.wall4u.WallpaperShow;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.ImageInfoBtmLayoutBinding;

public class ImageInfoBottomFragment extends BottomSheetDialogFragment {

    ImageInfoBtmLayoutBinding binding;

    String photographer_name, photographer_url, image_url;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.image_info_btm_layout, container, false);
        binding = ImageInfoBtmLayoutBinding.bind(view);

        Bundle bundle = getArguments();
        if (bundle != null) {
            photographer_name = bundle.getString("photographer_name");
            photographer_url = bundle.getString("photographer_url");
            image_url = bundle.getString("image_url");
        }

        if (photographer_name.equals("Unknown")) {
            binding.showByLayout.setVisibility(View.GONE);
        } else {
            binding.shotBy.setText(photographer_name);
        }

        if (photographer_url.equals("Unknown")) {
            binding.userUrlLayout.setVisibility(View.GONE);
        } else {
            binding.photographerUrl.setText(photographer_url);
        }

        if (image_url.equals("Unknown")) {
            binding.imageUrlLayout.setVisibility(View.GONE);
        } else {
            binding.imageUrl.setText(image_url);
        }

        binding.photographerUrl.setPaintFlags(binding.photographerUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.imageUrl.setPaintFlags(binding.imageUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //Set OnClick listener on photographer url.
        binding.photographerUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoUrlOpen = new Intent(Intent.ACTION_VIEW);
                photoUrlOpen.setData(Uri.parse(photographer_url));
                startActivity(photoUrlOpen);
            }
        });

        //Set onclick listener on image url.
        binding.imageUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageOpen = new Intent(Intent.ACTION_VIEW);
                imageOpen.setData(Uri.parse(image_url));
                startActivity(imageOpen);
            }
        });

        return view;
    }
}
