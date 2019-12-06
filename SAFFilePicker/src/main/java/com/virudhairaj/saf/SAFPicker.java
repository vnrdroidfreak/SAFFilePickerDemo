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

public class SAFPicker {

    private Activity activity;
    private Fragment fragment;
    private Callback callback = null;
    private PermissionHelper permissionHelper;
    private boolean hasMultiSelection = false;
    private PermissionCallback permissionCallback = null;

    private SAFPicker(@NonNull Activity activity, @Nullable Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;


        Callback tmpCallback = (Callback) (fragment != null ? fragment : activity);
        if (tmpCallback != null) this.callback = tmpCallback;

        PermissionCallback tmpPermissionCallback = (PermissionCallback) (fragment != null ? fragment : activity);
        if (tmpPermissionCallback != null) this.permissionCallback = tmpPermissionCallback;

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

    public static SAFPicker with(Activity activity, Fragment fragment) {
        return new SAFPicker(activity, fragment);
    }

    public static SAFPicker with(Activity activity) {
        return new SAFPicker(activity, null);
    }

    public SAFPicker setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public SAFPicker enableMultiSelection(boolean hasMultiSelection) {
        this.hasMultiSelection = hasMultiSelection;
        return this;
    }

    public void startPickerIntent(final Type type) {
        startPickerIntent(type, hasMultiSelection);
    }

    public void startPickerIntent(final Type type, final boolean hasMultiSelection) {
        if (type == null) return;
        this.hasMultiSelection = hasMultiSelection;
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
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
                if (permissionCallback != null) permissionCallback.onPermissionDenied();
            }

            @Override
            public void onPermissionDeniedBySystem() {
                if (permissionCallback != null) permissionCallback.onPermissionDeniedBySystem();
            }

        });
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (callback != null)
                callback.onFilePickerFailed(new Exception("Cancelled"));
            return;
        }
        try {
            boolean isPickerType = Type.isType(requestCode);
            if (isPickerType) {
                ArrayList<SAFFile> uris = new ArrayList<>();
                if (data != null) {
                    if (data.getDataString() != null) {
                        Uri uri = Uri.parse(data.getDataString());
                        uris.add(new SAFFile(activity, uri));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                uris.add(new SAFFile(activity, uri));
                            }
                        }
                    }

                    if (data.hasExtra("uris")) {
                        ArrayList<Uri> paths = data.getParcelableArrayListExtra("uris");
                        for (Uri uri : paths) {
                            uris.add(new SAFFile(activity, uri));
                        }
                    } else if (data.hasExtra("data")) {
                        ArrayList<Uri> paths = data.getParcelableArrayListExtra("data");
                        for (Uri uri : paths) {
                            uris.add(new SAFFile(activity, uri));
                        }
                    }
                }
                if (callback != null) {
                    callback.onFilePicked(Type.parse(requestCode), uris);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) callback.onFilePickerFailed(e);

        }
    }

    //for fragment
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public enum Type {
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

    public interface Callback {
        void onFilePicked(final @NonNull Type type, final List<SAFFile> safFiles);

        void onFilePickerFailed(final Exception e);
    }


}