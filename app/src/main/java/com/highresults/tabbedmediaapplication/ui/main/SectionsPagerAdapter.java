package com.highresults.tabbedmediaapplication.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.highresults.tabbedmediaapplication.MediaPlayerFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {


    private static final int PAGES_COUNT = 3;

    public SectionsPagerAdapter(FragmentActivity fa) {
        super(fa);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
            case 2:
                return PlaceholderFragment.newInstance(position + 1);
            case 1:
                return MediaPlayerFragment.newInstance();
            default:
                return PlaceholderFragment.newInstance(position);
        }
    }

    @Override
    public int getItemCount() {
        return PAGES_COUNT;
    }
}