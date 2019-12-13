package com.virudhairaj.saf.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.virudhairaj.saf.PermissionCallback;
import com.virudhairaj.saf.SAFCapturer;
import com.virudhairaj.saf.SAFFile;
import com.virudhairaj.saf.SAFPicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SAFDialog extends DialogFragment {

    private Activity activity;
    private SAFCapturer capturer;
    private SAFPicker picker;
    private Callback callback;

    private Views views;

    public static interface Callback{
        public void onDataReceived(List<SAFFile>data);
    }

    public static SAFDialog newInstance(@NonNull final Activity activity) {
        SAFDialog fragment = new SAFDialog();
        fragment.activity=activity;
        return fragment;
    }

    public SAFDialog setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        capturer = SAFCapturer.with(activity,this);
        picker = SAFPicker.with(activity,this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        views=new Views(inflater);
        capturer.setCallback(views);
        picker.setCallback(views);
        views.init();
        return views.root;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        picker.onActivityResult(requestCode, resultCode, data);
        capturer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        picker.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capturer.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    class Views implements SAFPicker.Callback, SAFCapturer.Callback,PermissionCallback  {
        final View root;

        final ProgressBar progressBar;

        final Button btnCapture,btnPicker;

        final View layoutCapture,layoutPicker;

        final RadioGroup rdoGrpType,rdoGrpCaptureMedia,rdoGrpPickerMedia;

        final CheckBox chkMultiSelect;

        private Views(@NonNull LayoutInflater inflater){
            root=inflater.inflate(R.layout.fragment_saf,null);
            progressBar=root.findViewById(R.id.progressBar);
            btnCapture=root.findViewById(R.id.btnCapture);
            btnPicker=root.findViewById(R.id.btnPicker);
            chkMultiSelect=root.findViewById(R.id.chkMultiSelect);

            layoutCapture=root.findViewById(R.id.layoutCapture);
            layoutPicker=root.findViewById(R.id.layoutPicker);

            rdoGrpType=root.findViewById(R.id.rdoGrpType);
            rdoGrpCaptureMedia=root.findViewById(R.id.rdoGrpCaptureMedia);
            rdoGrpPickerMedia=root.findViewById(R.id.rdoGrpPickerMedia);

        }

        public void init(){
            progressBar.setVisibility(View.GONE);
            rdoGrpType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    boolean isPicker=checkedId == R.id.rdoPicker;
                    layoutPicker.setVisibility(isPicker?View.VISIBLE:View.GONE);
                    layoutCapture.setVisibility(isPicker?View.GONE:View.VISIBLE);
                }
            });

            rdoGrpType.check(R.id.rdoPicker);

            btnPicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SAFPicker.Type type=rdoGrpPickerMedia.getCheckedRadioButtonId() ==R.id.rdoPickerText?SAFPicker.Type.text:
                            rdoGrpPickerMedia.getCheckedRadioButtonId() ==R.id.rdoPickerPhoto?SAFPicker.Type.photo:
                            rdoGrpPickerMedia.getCheckedRadioButtonId() ==R.id.rdoPickerAudio?SAFPicker.Type.audio:
                            rdoGrpPickerMedia.getCheckedRadioButtonId() ==R.id.rdoPickerVideo?SAFPicker.Type.video:
                            rdoGrpPickerMedia.getCheckedRadioButtonId() ==R.id.rdoPickerApplication?SAFPicker.Type.application:
                                    SAFPicker.Type.file;

                    boolean multiSelection=chkMultiSelect.isChecked();
                    picker.startPickerIntent(type,multiSelection);
                }
            });

            btnCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SAFCapturer.Type type =rdoGrpCaptureMedia.getCheckedRadioButtonId() ==R.id.rdoCaptureMediaPhoto?SAFCapturer.Type.photoCapture:
                            rdoGrpCaptureMedia.getCheckedRadioButtonId() ==R.id.rdoCaptureMediaVideo?SAFCapturer.Type.videoCapture:
                                    SAFCapturer.Type.audioCapture;
                    File outFile=new File(activity.getFilesDir(),"file"+
                            (type == SAFCapturer.Type.photoCapture ? ".jpg": type== SAFCapturer.Type.videoCapture?".mp4":".mp3"));
                    capturer.startCaptureIntent(type,outFile);
                }
            });
        }

        @Override
        public void onPermissionDenied() {

        }

        @Override
        public void onPermissionDeniedBySystem() {

        }


        @Override
        public void onMediaCaptured(@NonNull SAFCapturer.Type type, @NonNull SAFFile safFile, @NonNull File outputFile) {
            dismiss();
            if (callback!=null){
                List<SAFFile> data=new ArrayList<>();
                data.add(safFile);
                callback.onDataReceived(data);
            }
        }

        @Override
        public void onMediaCaptureFailed(Exception e) {
            Toast.makeText(activity,"CaptureFailed: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCaptureStatusChanged(boolean isStarted) {
            progressBar.setVisibility(isStarted?View.VISIBLE:View.GONE);
        }

        @Override
        public void onFilePicked(@NonNull SAFPicker.Type type, List<SAFFile> safFiles) {
            dismiss();
            if (callback!=null)callback.onDataReceived(safFiles);
        }

        @Override
        public void onFilePickerFailed(Exception e) {
            Toast.makeText(activity,"PickerFailed: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPickerStatusChanged(boolean isStarted) {
            progressBar.setVisibility(isStarted?View.VISIBLE:View.GONE);
        }

    }
}
