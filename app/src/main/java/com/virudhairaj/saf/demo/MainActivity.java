package com.virudhairaj.saf.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.virudhairaj.saf.PermissionCallback;
import com.virudhairaj.saf.SAFCapturer;
import com.virudhairaj.saf.SAFFile;
import com.virudhairaj.saf.SAFPicker;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SAFPicker.Callback, SAFCapturer.Callback, PermissionCallback {
    public static final String TAG = "MainActivity";
    SAFCapturer capturer;
    SAFPicker picker;

    Button btnFilePicker, btnPhotoPicker, btnVideoPicker, btnAudioPicker,btnApplicationPicker,btnTextPicker;
    Button btnPhotoCapture, btnVideoCapture, btnAudioCapture;
    CheckBox chkMultiSelect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        capturer = SAFCapturer.with(this);
        picker = SAFPicker.with(this);

        btnPhotoCapture = findViewById(R.id.btnPhotoCapture);
        btnVideoCapture = findViewById(R.id.btnVideoCapture);
        btnAudioCapture = findViewById(R.id.btnAudioCapture);

        chkMultiSelect = findViewById(R.id.chkMultiSelect);

        btnFilePicker = findViewById(R.id.btnFilePicker);
        btnPhotoPicker = findViewById(R.id.btnPhotoPicker);
        btnVideoPicker = findViewById(R.id.btnVideoPicker);
        btnAudioPicker = findViewById(R.id.btnAudioPicker);
        btnApplicationPicker = findViewById(R.id.btnApplicationPicker);
        btnTextPicker = findViewById(R.id.btnTextPicker);


        btnPhotoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturer.startCaptureIntent(
                        SAFCapturer.Type.photoCapture,
                        new File(getFilesDir(), "test.jpg")
                );
            }
        });

        btnVideoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturer.startCaptureIntent(
                        SAFCapturer.Type.videoCapture,
                        new File(getFilesDir(), "test.mp4")
                );

            }
        });

        btnAudioCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturer.startCaptureIntent(
                        SAFCapturer.Type.audioCapture,
                        new File(getFilesDir(), "test.mp3")
                );

            }
        });

        chkMultiSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                picker.enableMultiSelection(isChecked);
            }
        });


        btnFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.startPickerIntent(SAFPicker.Type.file);
            }
        });

        btnPhotoPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.startPickerIntent(SAFPicker.Type.photo);
            }
        });

        btnVideoPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.startPickerIntent(SAFPicker.Type.video);
            }
        });

        btnAudioPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.startPickerIntent(SAFPicker.Type.audio);
            }
        });



        btnApplicationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.startPickerIntent(SAFPicker.Type.application);
            }
        });

        btnTextPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.startPickerIntent(SAFPicker.Type.text);
            }
        });
    }

    @Override
    public void onMediaCaptured(@NonNull SAFCapturer.Type type, SAFFile safFile, @NonNull File outputFile) {
        Log.e(TAG, "onMediaCaptured Type:" + type.name() + " file:  " + safFile.toString() + " outputFile: " + outputFile);
        startActivity(safFile.getPreviewIntent());
    }

    @Override
    public void onMediaCaptureFailed(Exception e) {
        Log.e(TAG, "onMediaCaptureFailed: ", e);
    }

    @Override
    public void onFilePicked(@NonNull SAFPicker.Type type, List<SAFFile> safFiles) {
        Log.e(TAG, "onMediaCaptured Type:" + type.name());
        for (SAFFile file : safFiles) {
            Log.e(TAG, "file:  " + file.toString());
            startActivity(file.getPreviewIntent());
        }
    }

    @Override
    public void onFilePickerFailed(Exception e) {
        Log.e(TAG, "onFilePickerFailed: ", e);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (picker != null)
            picker.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (capturer != null)
            capturer.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (picker != null) picker.onActivityResult(requestCode, resultCode, data);
        if (capturer != null) capturer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPermissionDenied() {
        Log.e(TAG, "onPermissionDenied");
    }

    @Override
    public void onPermissionDeniedBySystem() {
        Log.e(TAG, "onPermissionDeniedBySystem");
    }
}
