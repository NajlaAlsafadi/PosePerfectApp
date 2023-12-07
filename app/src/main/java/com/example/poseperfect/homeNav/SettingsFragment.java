package com.example.poseperfect.homeNav;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.poseperfect.R;

public class SettingsFragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button enableCameraButton = view.findViewById(R.id.enableCameraButton);
//        enableCameraButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ContextCompat.checkSelfPermission(getContext(),
//                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA},
//                            REQUEST_CAMERA_PERMISSION);
//                } else {
//                    Toast.makeText(getContext(), "Camera already enabled", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        Button enableStorageButton = view.findViewById(R.id.enableStorageButton);
//        enableStorageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ContextCompat.checkSelfPermission(getContext(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            REQUEST_STORAGE_PERMISSION);
//                } else {
//                    Toast.makeText(getContext(), "Storage already enabled", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        return view;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CAMERA_PERMISSION: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getContext(), "Camera enabled!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//            case REQUEST_STORAGE_PERMISSION: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getContext(), "Storage enabled!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
}