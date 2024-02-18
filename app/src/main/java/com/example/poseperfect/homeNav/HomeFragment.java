package com.example.poseperfect.homeNav;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.poseperfect.R;

public class HomeFragment extends Fragment {
    private ViewPager instructionPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        instructionPager = view.findViewById(R.id.instructionPager);
        instructionPager.setAdapter(new InstructionPagerAdapter());
        float density = getResources().getDisplayMetrics().density;
        int margin = (int) (16 * density);
        instructionPager.setPageMargin(margin);

        return view;
    }

    private class InstructionPagerAdapter extends PagerAdapter {
        private int[] layouts = {
                R.layout.instructions_1,
                R.layout.instructions_2,
                R.layout.instructions_3,
                R.layout.instructions_4,
                R.layout.instructions_5
        };

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(getContext()).inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}