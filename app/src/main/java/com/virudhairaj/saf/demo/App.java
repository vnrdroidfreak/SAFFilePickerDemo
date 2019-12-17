package com.virudhairaj.saf.demo;

import android.app.Application;

import com.squareup.picasso.Picasso;

public class App extends Application {
    private static App instance=null;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public static App getInstance() {
        return instance;
    }

    private static Picasso picasso=null;
    public static Picasso getPicasso(){
        if (picasso==null) {
            picasso = new Picasso.Builder(getInstance())
                    .addRequestHandler(new VideoRequestHandler())
//                    .loggingEnabled(true)
//                    .setIndicatorsEnabled(true);
                    .build();
        }
        return picasso;
    }
}
