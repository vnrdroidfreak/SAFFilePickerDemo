package com.virudhairaj.saf.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.virudhairaj.saf.SAFFile;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.Views> {

    List<SAFFile> data;



    public MediaAdapter(List<SAFFile> data) {
        this.data = data;
    }


    public void setData(List<SAFFile> data, boolean clearAll) {
        if (this.data == null) this.data = new ArrayList<>();
        if (clearAll) this.data.clear();
        this.data.addAll(data);
        if (clearAll) notifyDataSetChanged();
        else notifyItemRangeInserted(this.data.size(), data.size());
    }

    public void setData(List<SAFFile> data) {
        setData(data, false);
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public Views onCreateViewHolder(ViewGroup parent, int viewType) {
        return Views.newHolder(parent);
    }

    @Override
    public void onBindViewHolder(Views holder, int position) {
        holder.bind(data.get(position), position);
    }

    public static class Views extends RecyclerView.ViewHolder {
        final View root;
        final ImageView imgFile;
        final TextView txtFileName,txtSize,txtMime;

        public Views(View view) {
            super(view);
            root = view;
            imgFile = root.findViewById(R.id.imgFile);
            txtFileName = root.findViewById(R.id.txtFileName);
            txtSize = root.findViewById(R.id.txtSize);
            txtMime = root.findViewById(R.id.txtMime);
        }

        public static Views newHolder(@NonNull ViewGroup parent){
            return new Views(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item,parent,false)
            );
        }

        public void bind(final SAFFile item, int position) {
            txtFileName.setText(item.name);
            txtMime.setText(item.mime);
            txtSize.setText(String.valueOf(item.size));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtFileName.getContext().startActivity(item.getPreviewIntent());
                }
            });
        }
    }

}
