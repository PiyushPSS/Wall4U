package com.lithium.wall4u.Discover.DiscoverFragmentEssenials;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.lithium.wall4u.Discover.CategoriesFragment.CategoriesFragment;
import com.lithium.wall4u.Discover.SearchFragment;
import com.lithium.wall4u.Discover.TrendingFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    int numOfTabs;

    public SectionsPagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new TrendingFragment();
                break;
            case 1:
                fragment = new SearchFragment();
                break;
            case 2:
                fragment = new CategoriesFragment();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "TRENDING";
            case 1:
                return "SEARCH";
            case 2:
                return "CATEGORY";
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return numOfTabs;
    }
}