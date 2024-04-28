package com.example.poseperfect.homeNav;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.poseperfect.R;
import com.example.poseperfect.baseUI.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;


public class ProfileFragment extends Fragment {
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private RoundedImageView profileImageView;
    private Switch cameraPermissionSwitch;
    private Switch storagePermissionSwitch;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        cameraPermissionSwitch = view.findViewById(R.id.cameraPermissionSwitch);
        storagePermissionSwitch = view.findViewById(R.id.storagePermissionSwitch);
        cameraPermissionSwitch.setChecked(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        storagePermissionSwitch.setChecked(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        cameraPermissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    showPermissionRevokeDialog(Manifest.permission.CAMERA, cameraPermissionSwitch);

                }
            }
        });
        // set on checked change listener for storage permission switch
        storagePermissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                  //  if switch is checked and permission is not granted, request the permission
                    // if switch is unchecked, show permission revoke dialog
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    showPermissionRevokeDialog(Manifest.permission.WRITE_EXTERNAL_STORAGE, storagePermissionSwitch);

                }
            }
        });

        Button updateButton = view.findViewById(R.id.updateBtn);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = usernameEditText.getText().toString();
                String newPassword = passwordEditText.getText().toString();

                if (!newUsername.isEmpty()) {
                    updateUserUsername(newUsername);
                }
                if (!newPassword.isEmpty()) {
                    updateUserPassword(newPassword);
                }
            }
        });
        // find profile image view by its id and set on long click listener
        profileImageView = view.findViewById(R.id.profile);
        profileImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openImagePicker();
                return true;
            }
        });

        if (user != null) {
            setUsername(view);
            setEmail(view);
            loadImage(view);

        }

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionSwitch.setChecked(true);
            } else {
                cameraPermissionSwitch.setChecked(false);
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermissionSwitch.setChecked(true);
            } else {
                storagePermissionSwitch.setChecked(false);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        cameraPermissionSwitch.setChecked(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        storagePermissionSwitch.setChecked(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void setUsername(View view) {
        String uid = user.getUid();
        databaseReference.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    User retrievedUser = task.getResult().getValue(User.class);
                    if (retrievedUser != null) {
                        String username = retrievedUser.getUsername();
                        TextView usernameTextView = view.findViewById(R.id.username);
                        usernameTextView.setText(username);
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to retrieve user.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void loadImage(View view) {
        String uid = user.getUid();
        databaseReference.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    User retrievedUser = task.getResult().getValue(User.class);
                    if (retrievedUser != null) {
                        String imageUrl = retrievedUser.getImageUrl();
                        if (imageUrl != null) {
                            Glide.with(getActivity()).load(imageUrl).circleCrop().into(profileImageView);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to retrieve image.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setEmail(View view) {
        String email = user.getEmail();
        if (email != null) {
            TextView emailTextView = view.findViewById(R.id.email);
            EditText emailEditText = view.findViewById(R.id.emailEditText);
            emailTextView.setText(email);
            emailEditText.setText(email);
        } else {
            Toast.makeText(getActivity(), "Failed to retrieve email.", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateUserUsername(String newUsername) {
        String uid = user.getUid();
        databaseReference.child("users").child(uid).child("username").setValue(newUsername)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Username updated successfully.", Toast.LENGTH_SHORT).show();
                            TextView usernameTextView = getView().findViewById(R.id.username);
                            usernameTextView.setText(newUsername);
                            usernameEditText.setText("");
                        } else {
                            Toast.makeText(getActivity(), "Failed to update username.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateUserPassword(String newPassword) {
        String passwordStrength = isPasswordStrong(newPassword);
        if (!passwordStrength.equals("strong")) {
            Toast.makeText(getActivity(), passwordStrength, Toast.LENGTH_SHORT).show();
            return;
        }

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                            passwordEditText.setText("");
                        } else {
                            Toast.makeText(getActivity(), "Failed to update password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private String isPasswordStrong(String password) {
        if (password.length() < 8) {
            return "Password needs to be at least 8 characters.";
        }
        if (!password.matches("(.*[A-Za-z].*)")) {
            return "Password needs to contain at least one letter.";
        }
        if (!password.matches("(.*[0-9].*)")) {
            return "Password needs to contain at least one number.";
        }
        return "strong";
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference profileImageRef = storageReference.child("profileImages/" + user.getUid() + ".jpg");
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateUserImage(imageUrl);
                        }))
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to upload image.", Toast.LENGTH_SHORT).show());
    }
    private void updateUserImage(String imageUrl) {
        String uid = user.getUid();
        databaseReference.child("users").child(uid).child("imageUrl").setValue(imageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Image updated successfully.", Toast.LENGTH_SHORT).show();
                            Glide.with(getActivity()).load(imageUrl).circleCrop().into(profileImageView);
                        } else {
                            Toast.makeText(getActivity(), "Failed to update image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void showPermissionRevokeDialog(String permission, CompoundButton permissionSwitch) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Revoke Permission")
                .setMessage("The permission needs to be revoked manually. Open settings?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionSwitch.setChecked(!permissionSwitch.isChecked());
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        permissionSwitch.setChecked(!permissionSwitch.isChecked());
                    }
                })
                .show();
    }

}