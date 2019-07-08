package com.virudhairaj.saf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;

public class SAFPicker {

    private Activity activity;
    private Fragment fragment;
    private Listener listener = null;
    private PermissionHelper permissionHelper;

    private boolean outputAsUri = true;


    private static enum Type {
        file(10), photo(11), video(12), audio(13);
        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Type parse(int value) {
            switch (value) {
                case 11:
                    return photo;
                case 12:
                    return video;
                case 13:
                    return audio;
                default:
                    return file;
            }
        }

        public static boolean isType(int value) {
            return value == file.getValue() || value == photo.getValue() || value == video.getValue() || value == audio.getValue();
        }
    }

    public static interface Listener {
        public void onFilePicked(final @NonNull Enum type, final ArrayList<String> files);

        public void onFilePickerFailed(final Exception e);
    }

    public static SAFPicker with(Activity activity, Fragment fragment) {
        return new SAFPicker(activity, fragment);
    }

    public static SAFPicker with(Activity activity) {
        return new SAFPicker(activity, null);
    }

    private SAFPicker(@NonNull Activity activity, @Nullable Fragment fragment) {
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

    public SAFPicker setListener(Listener pickerListener) {
        this.listener = pickerListener;
        return this;
    }

    public SAFPicker setOutputAsUri(boolean outputAsUri) {
        this.outputAsUri = outputAsUri;
        return this;
    }

    public void startPickerIntent(final Type type, final boolean hasMultiSelection) {
        if (type == null) return;
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                if (type == Type.audio) {
                    intent.setType("audio/*");
                } else if (type == Type.photo) {
                    intent.setType("image/*");
                } else if (type == Type.video) {
                    intent.setType("video/*");
                }

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
                            type.getValue());
                } else if (activity != null) {
                    activity.startActivityForResult(
                            Intent.createChooser(intent, "Select " + type.name()),
                            type.getValue());
                }
            }

            @Override
            public void onPermissionDeniedBySystem() {

            }

            @Override
            public void onPermissionDenied() {

            }

            @Override
            public void onIndividualPermissionGranted(String[] strings) {

            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (listener != null)
                listener.onFilePickerFailed(new Exception("Cancelled"));
            return;
        }
        try {
            boolean isPickerType = Type.isType(requestCode);
            if (isPickerType) {
                ArrayList<String> uris = new ArrayList<String>();
                if (data != null) {
                    if (data.getDataString() != null) {
                        Uri uri = Uri.parse(data.getDataString());
                        uris.add(outputAsUri ? uri.toString() : FileUtils.getFilePathFromUri(activity, uri));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                uris.add(outputAsUri ? uri.toString() : FileUtils.getFilePathFromUri(activity, uri));
                            }
                        }
                    }
                    if (data.hasExtra("uris")) {
                        ArrayList<Uri> paths = data.getParcelableArrayListExtra("uris");
                        for (Uri uri : paths) {
                            uris.add(outputAsUri ? uri.toString() : FileUtils.getFilePathFromUri(activity, uri));
                        }
                    } else if (data.hasExtra("data")) {
                        ArrayList<Uri> paths = data.getParcelableArrayListExtra("data");
                        for (Uri uri : paths) {
                            uris.add(outputAsUri ? uri.toString() : FileUtils.getFilePathFromUri(activity, uri));
                        }
                    }
                }
                if (listener != null) {
                    listener.onFilePicked(Type.parse(requestCode), uris);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onFilePickerFailed(e);

        }
    }

    //for fragment
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
