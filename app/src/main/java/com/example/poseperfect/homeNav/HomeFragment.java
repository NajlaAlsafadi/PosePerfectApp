package com.example.poseperfect.homeNav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.poseperfect.ExerciseFragment;
import com.example.poseperfect.R;
import com.example.poseperfect.baseUI.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Calendar;

public class HomeFragment extends Fragment {
    private ViewPager instructionPager;
    private Button startExercises;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private RoundedImageView  homeProfileImageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        homeProfileImageView = view.findViewById(R.id.imageProfile);
        loadImage();
        View yogaContent1 = view.findViewById(R.id.yoga_content_1);
        View yogaContent2 = view.findViewById(R.id.yoga_content_2);
        View yogaContent3 = view.findViewById(R.id.yoga_content_3);
        View yogaContent4 = view.findViewById(R.id.yoga_content_4);
        yogaContent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYoutubeLink("https://youtu.be/j7rKKpwdXNE?si=hfWjyCt0K5mPan-d");
            }
        });
        yogaContent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYoutubeLink("https://youtu.be/_8kV4FHSdNA?si=JXi5x_PP3Sc_HkR1");
            }
        });
        yogaContent3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYoutubeLink("https://youtu.be/SvPKFsCiMsw?si=cq2pj18FGIHVgCvu");
            }
        });
        yogaContent4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYoutubeLink("https://youtu.be/JqyHToMWl3E?si=D8r7YXBUQJr7-DgJ");
            }
        });

        TextView dayTimeMsg = view.findViewById(R.id.daytimemsg);
        instructionPager = view.findViewById(R.id.instructionPager);
        instructionPager.setAdapter(new InstructionPagerAdapter());
        float density = getResources().getDisplayMetrics().density;
        int margin = (int) (16 * density);
        instructionPager.setPageMargin(margin);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (currentHour >= 0 && currentHour < 12) {
            dayTimeMsg.setText(R.string.good_morning);
        } else if (currentHour >= 12 && currentHour < 17) {
            dayTimeMsg.setText(R.string.good_afternoon);
        } else if (currentHour >= 17 && currentHour < 24) {
            dayTimeMsg.setText(R.string.good_evening);
        }
        startExercises = view.findViewById(R.id.startExercises);
        startExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ExerciseFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                ((HomeActivity) getActivity()).selectExerciseTab();
            }
        });
        setUsername();
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
    private void loadImage() {
        if (user != null) {
            String uid = user.getUid();
            databaseReference.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        User retrievedUser = task.getResult().getValue(User.class);
                        if (retrievedUser != null) {
                            String imageUrl = retrievedUser.getImageUrl();
                            if (imageUrl != null) {
                                Glide.with(getActivity()).load(imageUrl).circleCrop().into(homeProfileImageView);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to retrieve image.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void openYoutubeLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    private void setUsername() {
        if (user != null) {
            String uid = user.getUid();
            databaseReference.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        User retrievedUser = task.getResult().getValue(User.class);
                        if (retrievedUser != null) {
                            String username = retrievedUser.getUsername();
                            TextView usernameTextView = getView().findViewById(R.id.username);
                            usernameTextView.setText(username);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to retrieve user.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}