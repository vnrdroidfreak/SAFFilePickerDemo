package com.virudhairaj.saf.demo;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

public class VideoRequestHandler extends RequestHandler {
    public static String SCHEME_VIDEO = "video://";

    @Override
    public boolean canHandleRequest(Request data) {
        String scheme = data.uri.getScheme();
        boolean contains=scheme.startsWith("video");
        return contains;
    }

    @Override
    public Result load(Request data, int arg1) throws IOException {
        final Uri uri= Uri.parse(data.uri.toString().replace(SCHEME_VIDEO,""));
        final ParcelFileDescriptor descriptor = App.getInstance().getContentResolver().openFileDescriptor(uri, "r");
        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(descriptor.getFileDescriptor());
        Bitmap bm = mMMR.getFrameAtTime();
        return new Result(bm, Picasso.LoadedFrom.DISK);
    }
}