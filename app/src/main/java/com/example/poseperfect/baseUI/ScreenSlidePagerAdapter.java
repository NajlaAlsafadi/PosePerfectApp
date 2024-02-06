package com.example.poseperfect.baseUI;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {

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