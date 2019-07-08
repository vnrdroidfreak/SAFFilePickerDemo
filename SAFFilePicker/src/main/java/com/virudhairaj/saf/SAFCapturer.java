package com.virudhairaj.saf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class SAFCapturer {

    private Activity activity;
    private Fragment fragment;
    private Listener listener = null;
    private PermissionHelper permissionHelper;
    private Uri outputUri = null;
    private boolean outputAsUri = true;

    public enum Type {
        photoCapture(14), videoCapture(15), audioCapture(16);
        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static boolean isType(int value) {
            return value == photoCapture.getValue() || value == videoCapture.getValue() || value == audioCapture.getValue();
        }

        public static Type parse(int value) {
            switch (value) {
                case 14:
                    return photoCapture;
                case 15:
                    return videoCapture;
                default:
                    return audioCapture;
            }
        }
    }

    public static interface Listener {
        /**
         * @param type
         * @param path can be uri or file path
         */
        public void onMediaCaptured(final @NonNull Type type, final String path);

        public void onMediaCaptureFailed(final Exception e);
    }

    public static SAFCapturer with(Activity activity, Fragment fragment) {
        return new SAFCapturer(activity, fragment);
    }

    public static SAFCapturer with(Activity activity) {
        return new SAFCapturer(activity, null);
    }

    private SAFCapturer(@NonNull Activity activity, @Nullable Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
        try {
            this.listener = (Listener) fragment;
        } catch (Exception e) {

        }

        if (fragment != null) {
            this.permissionHelper = new PermissionHelper(
                    fragment,
                    new String[]{
//                        Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    300
            );
        } else {
            this.permissionHelper = new PermissionHelper(
                    activity,
                    new String[]{
//                        Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    300
            );
        }
    }


    public SAFCapturer setListener(Listener pickerListener) {
        this.listener = pickerListener;
        return this;
    }


    public SAFCapturer setOutputAsUri(boolean outputAsUri) {
        this.outputAsUri = outputAsUri;
        return this;
    }


    public void startCaptureIntent(@NonNull final Type type, @NonNull final Uri output) {
        if (type == null || output == null) return;
        this.outputUri = output;
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(
                        type == Type.photoCapture ? MediaStore.ACTION_IMAGE_CAPTURE :
                                type == Type.videoCapture ? MediaStore.ACTION_VIDEO_CAPTURE :
                                        MediaStore.Audio.Media.RECORD_SOUND_ACTION
                );
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtils.getFilePathFromUri(activity, outputUri));
                try {
                    intent.putExtra("return-data", true);
                    if (fragment != null) {
                        fragment.startActivityForResult(intent, type.value);
                    } else {
                        activity.startActivityForResult(intent, type.value);
                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {

            }

            @Override
            public void onPermissionDenied() {

            }

            @Override
            public void onPermissionDeniedBySystem() {

            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (listener != null) listener.onMediaCaptureFailed(new Exception("Cancelled"));
            return;
        }
        try {
            boolean isCaptureType = Type.isType(requestCode);
            if (isCaptureType && outputUri != null) {
                listener.onMediaCaptured(Type.parse(requestCode), outputAsUri ? outputUri.toString() : FileUtils.getFilePathFromUri(activity, outputUri));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onMediaCaptureFailed(e);
        }
    }

    //for fragment
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
