package com.withutechnologies.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.withutechnologies.compressor.classes.ImageCompressor;
import com.withutechnologies.compressor.configuration.ImageConfiguration;
import com.withutechnologies.compressor.listeners.ImageCompressListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private AppCompatActivity activity = MainActivity.this;
    private String cameraImagePath = null;
    private static final int REQUEST_TAKE_PHOTO = 113;
    private AppCompatImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imgView);
        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    /**
     * Device Camera Intent Methods
     */
    private void dispatchTakePictureIntent() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                                // Create the File where the photo should go
                                File newFile = null;
                                try {
                                    newFile = createImageFile();
                                    cameraImagePath = newFile.getAbsolutePath();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        cameraImagePath = newFile.getAbsolutePath();
                                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        Uri photoURI = FileProvider.getUriForFile(activity, "com.withutechnologies.demo.fileprovider", newFile);
                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    } else {
                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
                                    }
                                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Toast.makeText(activity, R.string.no_camera_exists, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                throw new IOException();
            }
        }
        return new File(storageDir, imageFileName);
    }

    /**Device Camera Intent Methods*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK && cameraImagePath != null && !TextUtils.isEmpty(cameraImagePath)) {
                    File mFile = new File(cameraImagePath);
                    if (mFile != null && mFile.exists()) {
                        new ImageCompressor.Builder(activity)
                                .setConfiguration(ImageConfiguration.MEDIA_QUALITY_LOW)
                                .setImage(mFile.getAbsolutePath())
                                //.setImageOutputSize(2000.0f, 1000.0f) // if declared then wil override configuration
                                .onImageCompressed(new ImageCompressListener() {
                                    @Override
                                    public void onImageCompressed(byte[] bytes, File file) {
                                        if(file != null && file.exists()) {
                                            imageView.setImageURI(Uri.parse(file.getAbsolutePath()));
                                        }
                                    }
                                })
                                .build();
                    }
                    cameraImagePath = null;
                }
                break;
        }
    }
}
