package com.virudhairaj.saf.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.virudhairaj.saf.SAFFile;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements SAFDialog.Callback {
    public static final String TAG = "MainActivity";

    List<SAFFile> data=new ArrayList<>();

    MediaAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter=new MediaAdapter(data);
        final Button btnAdd=findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SAFDialog.newInstance(MainActivity.this).setCallback(MainActivity.this).show(getSupportFragmentManager(),TAG);
            }
        });

        final RecyclerView recycler=findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);
        recycler.setNestedScrollingEnabled(false);
        recycler.setAdapter(adapter);


    }

    @Override
    public void onDataReceived(List<SAFFile> files) {
        adapter.setData(files,true);
    }
}
