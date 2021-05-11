package com.imagesharer.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imagesharer.R;
import com.imagesharer.models.Upload;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    // fiedls
    private Context mContext;
    private List<Upload> mUploads;
    // constructor
    public ImageAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    //nested class to hold views
    public class ImageViewHolder extends RecyclerView.ViewHolder {
        // views
        public TextView textViewName;
        public ImageView imageView;
        // constructor
        public ImageViewHolder(View itemView) {
            super(itemView);
            //init views
            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
        }
    }
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate view
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_image, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        // get an element of upload object from the list
        Upload uploadCurrent = mUploads.get(position);
        // set text view to show upload nam/description
        holder.textViewName.setText(uploadCurrent.getName());
        //draw image using url of the upload object
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }
}