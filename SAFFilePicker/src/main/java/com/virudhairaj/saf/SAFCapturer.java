package com.virudhairaj.saf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.virudhairaj.saf.fileprovider.EasyFileProvider;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * This class is responsible for android capture intent and its data handling
 */

public class SAFCapturer {

    private Activity activity;
    private Fragment fragment;
    private Callback callback = null;
    private PermissionHelper permissionHelper;
    private PermissionListener permissionListener;
    private Uri outputUri = null;
    private File outputFile = null;
    private final AppExecutors executors;

    /**
     * default constructor
     * @param activity
     * @param fragment
     */
    private SAFCapturer(@NonNull Activity activity, @Nullable Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
        this.executors = new AppExecutors();

        try {
            Callback tmpCallback = (Callback) (fragment != null && fragment instanceof Callback ? fragment : activity);
            //check callback implemented in fragment or activity. if yes get that references
            if (tmpCallback != null) this.callback = tmpCallback;
        } catch (Exception e) {

        }
        try {
            PermissionListener tmpPermissionListener = (PermissionListener) (fragment != null ? fragment : activity);
            //check callback implemented in fragment or activity. if yes get that references
            if (tmpPermissionListener != null) this.permissionListener = tmpPermissionListener;
        } catch (Exception e) {

        }
        if (fragment != null) {
            this.permissionHelper = new PermissionHelper(
                    fragment,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    300
            );
        } else {
            this.permissionHelper = new PermissionHelper(
                    activity,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    300
            );
        }
    }

    /**
     * Capturer from fragment
     *
     * @param activity
     * @param fragment
     * @return SAFCapturer
     */
    public static SAFCapturer with(Activity activity, Fragment fragment) {
        return new SAFCapturer(activity, fragment);
    }

    /**
     * Capturer from activity
     *
     * @param activity
     * @return SAFCapturer
     */
    public static SAFCapturer with(Activity activity) {
        return new SAFCapturer(activity, null);
    }

    /**
     * Set listener for capturer callback
     *
     * @param callback
     * @return
     */
    public SAFCapturer setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * emmits permission dinied callback
     *
     * @param permissionListener
     * @return
     */
    public SAFCapturer setPermissionListener(PermissionListener permissionListener) {
        this.permissionListener = permissionListener;
        return this;
    }

    public void startCaptureIntent(@NonNull final Type type, @NonNull final File output) {
        if (type == null || output == null) return;

        this.outputFile = output;
        this.outputUri = EasyFileProvider.with(activity).getUriBy(outputFile);


        final PermissionHelper.PermissionCallback pCallback = new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(
                        type == Type.photoCapture ? MediaStore.ACTION_IMAGE_CAPTURE :
                                type == Type.videoCapture ? MediaStore.ACTION_VIDEO_CAPTURE :
                                        MediaStore.Audio.Media.RECORD_SOUND_ACTION
                );
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                try {
                    intent.putExtra("return-data", true);
                    if (fragment != null) {
                        fragment.startActivityForResult(intent, type.value);
                    } else {
                        activity.startActivityForResult(intent, type.value);
                    }
//                    if (callback != null) callback.onCaptureProgress(true);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {

            }

            @Override
            public void onPermissionDenied() {
                if (permissionListener != null)
                    permissionListener.onPermissionDenied(permissionHelper, this);
            }

            @Override
            public void onPermissionDeniedBySystem() {
                if (permissionListener != null)
                    permissionListener.onPermissionDeniedBySystem(permissionHelper, this);
            }
        };
        permissionHelper.request(pCallback);
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (callback != null && Type.isType(requestCode))
                callback.onMediaCaptureFailed(new Exception("Cancelled"));
            return;
        }

        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCaptureProgress(true);
                        }
                    });
                }
                try {
                    boolean isCaptureType = Type.isType(requestCode);
                    if (isCaptureType && outputUri != null) {
                        try {
                            final SAFFile file = new SAFFile(activity, outputUri);
                            if (callback != null) {
                                executors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onCaptureProgress(false);
                                        callback.onMediaCaptured(Type.parse(requestCode), file, outputFile);
                                    }
                                });
                            }
                        } catch (final Exception e) {
                            if (callback != null) {
                                executors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onCaptureProgress(false);
                                        callback.onMediaCaptureFailed(e);
                                    }
                                });
                            }
                        }

                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        executors.mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCaptureProgress(false);
                                callback.onMediaCaptureFailed(e);
                            }
                        });
                    }
                }
            }
        });
    }

    //for fragment
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static enum Type {
        photoCapture(14), videoCapture(15), audioCapture(16);
        public final int value;

        Type(int value) {
            this.value = value;
        }

        public static boolean isType(int value) {
            return value == photoCapture.value || value == videoCapture.value || value == audioCapture.value;
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

    public static interface Callback {

        void onMediaCaptured(final @NonNull Type type, @NonNull final SAFFile safFile, @NonNull final File outputFile);

        void onMediaCaptureFailed(final Exception e);

        void onCaptureProgress(final boolean isStarted);
    }

}