package com.chorotega_potato_assessment_app;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

//https://www.learningsomethingnew.com/how-to-use-a-recycler-view-to-show-images-from-storage
//https://www.youtube.com/watch?v=5SQbgMFOW6s&ab_channel=PRABEESHRK
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageViewHolder> {
    //This is the main workhorse function for the history_search
    //nothing should need to be modified in this
    //image_view_layout controls individual cell appearance, activity_history_search controls the entire page
    //this will need modified in some manner if you want to change the text that appears or add text

    //truthfully this function is magic to me
    private List<String> list;
    private  List<File> fileList;
    private Context context;



    public RecyclerAdapter(List<String> list, List<File> pathList, Context context){
        this.fileList=pathList;
        this.list=list;
        this.context=context;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view_layout,parent,false);
        ImageViewHolder imageViewHolder=new ImageViewHolder(view, context, list);//originally just view was used
    return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        //holder.fileNames.setText(list.get(position));
        holder.historyImage.setImageURI(Uri.fromFile(fileList.get(position)));
        holder.historyText.setText(list.get(position));
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView historyImage;
        TextView historyText;
        Context context;
        List<String> list;

        public ImageViewHolder(View itemView, Context context,List<String> list ) {
            super(itemView);
            historyImage=itemView.findViewById(R.id.history_image_view);
            historyText=itemView.findViewById(R.id.history_info_textView);
            itemView.setOnClickListener(this);
            this.context=context;
            this.list=list;
        }

        @Override
        public void onClick(View v) {
            Intent intent=new Intent(context, DisplayHistoryActivity.class);
            intent.putExtra("file_name", list.get(getAdapterPosition()));

            context.startActivity(intent);
        }
    }


}
