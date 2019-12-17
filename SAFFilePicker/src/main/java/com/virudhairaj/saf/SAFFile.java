package com.virudhairaj.saf;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;


public class SAFFile {

    public final Uri uri;
    public final String name;
    public final Long size;
    public final String mime;
    public final ParcelFileDescriptor descriptor;

    public SAFFile(final Context context, final Uri uri) throws Exception {
        descriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        this.uri = uri;
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        name = returnCursor.getString(nameIndex);
        size = returnCursor.getLong(sizeIndex);
        returnCursor.close();
        mime = context.getContentResolver().getType(uri);
    }

    public  String getFormattedSize() {
        if (size==null)return "";

        String s = size < 0 ? "-" : "";
        long b = size == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(size);
        return b < 1000L ? size + " B"
                : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                : String.format("%s%.1f EB", s, b / 1e6);
    }

    public Intent getPreviewIntent(){
        return new Intent(Intent.ACTION_VIEW)//
                .setDataAndType(uri,
                        mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    protected MediaMetadataRetriever openMeta() {
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(descriptor.getFileDescriptor());
        return retriever;
    }


    @NonNull
    @Override
    public String toString() {
        return "SAFFile{" +
                "uri=" + uri +
                ", name='" + name + '\'' +
                ", mime='" + mime + '\'' +
                '}';
    }
}
