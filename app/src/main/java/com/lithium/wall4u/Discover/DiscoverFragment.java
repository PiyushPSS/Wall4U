package com.lithium.wall4u.Discover;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lithium.wall4u.Discover.DiscoverFragmentEssenials.SectionsPagerAdapter;
import com.lithium.wall4u.R;
import com.lithium.wall4u.databinding.DiscoverFragmentBinding;

public class DiscoverFragment extends Fragment {

    Activity activity;
    DiscoverFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.discover_fragment, container, false);
        activity = (Activity) view.getContext();

        binding = DiscoverFragmentBinding.bind(view);
        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(), 3);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        return view;

    }
}