package com.example.poseperfect.baseUI;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    // constructor to associate adapter with the lifecycle of FragmentActivity
    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    // method to create fragments for different pages
    @Override
    public Fragment createFragment(int position) {
        // switch statement to return different fragments based on position
        switch (position) {
            case 0:
                return new IntroFragment();
            case 1:
                return new TechniquesFragment();
            case 2:
                return new HealthFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; }
}