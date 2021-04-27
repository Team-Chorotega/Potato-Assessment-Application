package com.chorotega_potato_assessment_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HistorySearch extends AppCompatActivity  {
        //This activity works with RecyclerAdapter and activity_history_search to look for all image files from previous runs and displays them in a list
    //this is not the workhorse of the activity
//image_view_layout controls individual cell appearance, activity_history_search controls the entire page

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private List<String> fileNameList;
    private List<File> pathList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_search);

        //tool bar is generated in tool_bar.xml and is the bar at the top of both screens
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History");
        /////////////////////////////////////////////////


        recyclerView=findViewById(R.id.history_list);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        String root =this.getFilesDir().toString();
        File imageDir = new File(root+"/saved_images");
        File files[]=imageDir.listFiles();
        if(files!=null) {
            String[] fileNames = new String[files.length];

            Log.v("HISTORY", "Size: " + files.length);
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = files[i].getName();
                Log.v("Names", fileNames[i]);
            }

            fileNameList = Arrays.asList(fileNames);
            pathList = Arrays.asList(files);
            adapter = new RecyclerAdapter(fileNameList, pathList, this);
            recyclerView.setAdapter(adapter);
        }
        else{
            getSupportActionBar().setTitle("NO IMAGES FOUND");

        }
    }


}




