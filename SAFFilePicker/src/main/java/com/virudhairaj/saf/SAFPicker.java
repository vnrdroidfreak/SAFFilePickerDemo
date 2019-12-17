package com.virudhairaj.saf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * this class is responsible for android picker intent and its data handling
 */
public class SAFPicker {

    private Activity activity;
    private Fragment fragment;
    private Callback callback = null;
    private PermissionHelper permissionHelper;
    private boolean hasMultiSelection = false;
    private PermissionListener permissionListener = null;
    private final AppExecutors executors;

    /**
     * default constructor.
     *
     * @param activity
     * @param fragment
     */
    private SAFPicker(@NonNull Activity activity, @Nullable Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
        this.executors = new AppExecutors();


        try {
            Callback tmpCallback = (Callback) (fragment != null ? fragment : activity);
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
     * Initialization
     *
     * @param activity
     * @param fragment
     * @return
     */
    public static SAFPicker with(Activity activity, Fragment fragment) {
        return new SAFPicker(activity, fragment);
    }

    /**
     * Initialization
     *
     * @param activity
     * @return
     */
    public static SAFPicker with(Activity activity) {
        return new SAFPicker(activity, null);
    }

    /**
     * This function set callback manually. For effective use need to call before startPickerIntent()
     *
     * @param callback
     * @return
     */
    public SAFPicker setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * emmits permission denied callback
     *
     * @param permissionListener
     * @return
     */
    public SAFPicker setPermissionListener(PermissionListener permissionListener) {
        this.permissionListener = permissionListener;
        return this;
    }

    /**
     * This option configures picker intent has multi selection
     *
     * @param hasMultiSelection
     * @return
     */
    public SAFPicker enableMultiSelection(boolean hasMultiSelection) {
        this.hasMultiSelection = hasMultiSelection;
        return this;
    }

    /**
     * Invoke this when you need to start picker
     *
     * @param type
     */
    public void startPickerIntent(final Type type) {
        startPickerIntent(type, hasMultiSelection);
    }

    /**
     * Invoke this when you need to start picker
     *
     * @param type
     * @param hasMultiSelection
     */
    public void startPickerIntent(final Type type, final boolean hasMultiSelection) {
        if (type == null) return;
        this.hasMultiSelection = hasMultiSelection;
        PermissionHelper.PermissionCallback pCallback = new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(type.mime);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, hasMultiSelection);
                } else if (hasMultiSelection) {
                    Log.e("SAFPicker", "multi-selection option not available");
                }

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                if (fragment != null) {
                    fragment.startActivityForResult(
                            Intent.createChooser(intent, "Select " + type.name()),
                            type.code);
                } else if (activity != null) {
                    activity.startActivityForResult(
                            Intent.createChooser(intent, "Select " + type.name()),
                            type.code);
                }
            }

            @Override
            public void onIndividualPermissionGranted(String[] strings) {

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


    /**
     * Need to invoked in  onActivityResult()  of fragment or activity you have implemented
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @SuppressLint("ObsoleteSdkInt")
    public void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (callback != null) {
                callback.onFilePickerFailed(new Exception("Cancelled"));
            }
            return;
        }
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    boolean isPickerType = Type.isType(requestCode);
                    if (isPickerType) {
                        final ArrayList<SAFFile> uris = new ArrayList<>();
                        if (data != null) {
                            if (data.getDataString() != null) {
                                Uri uri = Uri.parse(data.getDataString());
                                updateProgress(1);
                                uris.add(new SAFFile(activity, uri));
                                updateProgress(100);
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                if (data.getClipData() != null) {
                                    ClipData clipData = data.getClipData();
                                    final int total = clipData.getItemCount();
                                    for (int i = 0; i < total; i++) {
                                        Uri uri = clipData.getItemAt(i).getUri();
                                        uris.add(new SAFFile(activity, uri));
                                        int progress = (int) (((i + 1) / (float) total) * 100);
                                        updateProgress(progress);
                                    }
                                }
                            }

                            if (data.hasExtra("uris")) {
                                ArrayList<Uri> paths = data.getParcelableArrayListExtra("uris");
                                final int total = paths.size();
                                for (int i = 0; i < total; i++) {
                                    Uri uri = paths.get(i);
                                    uris.add(new SAFFile(activity, uri));
                                    int progress = (int) (((i + 1) / (float) total) * 100);
                                    updateProgress(progress);
                                }
                            } else if (data.hasExtra("data")) {
                                ArrayList<Uri> paths = data.getParcelableArrayListExtra("data");
                                final int total = paths.size();
                                for (int i = 0; i < total; i++) {
                                    Uri uri = paths.get(i);
                                    uris.add(new SAFFile(activity, uri));
                                    int progress = (int) (((i + 1) / (float) total) * 100);
                                    updateProgress(progress);
                                }
                            }
                        }
                        if (callback != null) {
                            executors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFilePicked(Type.parse(requestCode), uris);
                                }
                            });
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        executors.mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                updateProgress(100);
                                callback.onFilePickerFailed(e);
                            }
                        });
                    }

                }

            }
        });
    }

    private void updateProgress(final int progress) {
        if (callback != null) {
            executors.mainThread().execute(new Runnable() {
                @Override
                public void run() {
                    callback.onPickerProgress(progress);
                }
            });
        }
    }

    /**
     * Need to invoked in  onRequestPermissionsResult()  of fragment or activity you have implemented
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static enum Type {
        file(10, "*/*"),
        photo(11, "image/*"),
        video(12, "video/*"),
        audio(13, "audio/*"),
        text(14, "text/*"),
        application(15, "application/*");

        public final int code;
        public final String mime;

        Type(int code, String mime) {
            this.code = code;
            this.mime = mime;
        }

        public static Type parse(int value) {
            switch (value) {
                case 11:
                    return photo;
                case 12:
                    return video;
                case 13:
                    return audio;
                case 14:
                    return text;
                case 15:
                    return application;
                default:
                    return file;
            }
        }

        public static boolean isType(int value) {
            return value == file.code || value == photo.code || value == video.code || value == audio.code || value == text.code || value == application.code;
        }
    }

    public static interface Callback {
        void onFilePicked(final @NonNull Type type, final List<SAFFile> safFiles);

        void onFilePickerFailed(final Exception e);

        void onPickerProgress(final int progress);
    }


}