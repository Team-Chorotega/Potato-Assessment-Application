package com.chorotega_potato_assessment_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DisplayHistoryActivity extends AppCompatActivity {
    //this activity controls displaying the full image and the table to the user
    //You change the look of the table by editing this.. please avoid breaking the loops
    //we may want to check for "None" in the coin type line and change the table headers to say pixels

    ImageView imageView;
    Toolbar toolbar;
    TableLayout t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_display_history);

        ////////////////////////////////////
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History");
        //////////////////////////////////////

        imageView=findViewById(R.id.display_history_view);


        String imageName=getIntent().getStringExtra("file_name");
      //  Log.v("HISTORYINFO", imageName);
        String neutralName=imageName.substring(0, imageName.length()-4);
       // Log.v("HISTORYINFO", neutralName);
        String dataName=neutralName+".txt";
       // Log.v("HISTORYINFO", dataName);
        getSupportActionBar().setTitle("History: "+ neutralName);
        String root =this.getFilesDir().toString();
        File imageDir = new File(root+"/saved_images");
        File imageFile=new File(imageDir,imageName);
        File dataDir = new File(root+"/saved_data");
        File dataFile = new File(dataDir, dataName);

        imageView.setImageURI(Uri.fromFile(imageFile));

        BuildTable(dataFile);
    }


    private void BuildTable(File dataFile){

        t1=findViewById(R.id.history_table);
        TextView textView=findViewById(R.id.avg_history);


        int headerID=100;
        int countId=headerID+1;//101
        int lengthId=headerID+2;//102
        int widthId=headerID+3;//103
        int ratioId=headerID+4;//104


        t1.removeAllViewsInLayout();
        //////////This is just for the header of the table
        TableRow tr_head= new TableRow(this);
        tr_head.setId(headerID); //for some reason it wouldn't accept just straight 10
        tr_head.setBackgroundColor(Color.BLACK);
        tr_head.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TextView potato_count = new TextView(this);
        potato_count.setId(countId);
        potato_count.setText("Tuber");
        potato_count.setTextColor(Color.WHITE);
        potato_count.setPadding(50,20,50,20);
        tr_head.addView(potato_count);

        TextView length_head = new TextView(this);
        length_head.setId(lengthId);
        length_head.setText("L(cm)");
        length_head.setTextColor(Color.WHITE);
        length_head.setPadding(50,20,50,20);
        tr_head.addView(length_head);

        TextView width_head = new TextView(this);
        width_head.setId(widthId);
        width_head.setText("W(cm)");
        width_head.setTextColor(Color.WHITE);
        width_head.setPadding(50,20,50,20);
        tr_head.addView(width_head);

        TextView avg_head = new TextView(this);
        avg_head.setId(ratioId);
        avg_head.setText("L/W");
        avg_head.setTextColor(Color.WHITE);
        avg_head.setPadding(50,20,50,20);
        tr_head.addView(avg_head);

        t1.addView(tr_head, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //////End of header
        /*
         * name: 2021-03-16-17-43-21-6.txt
         * # detected
         * # min Ratio
         * # max ratio
         * # avg Ratio
         * coinType
         * Potato 1 Length
         * Potato 1 Width
         * potato 1 Ratio
         * etc for every other potato until end
         * */
        int lineCounter=1;
        String avgText = " L/W avg: ";
        int tuberCounter=0;
        TextView[] textArray = new TextView[4];//should be how many columns
       // TableRow[] tr_tater = new TableRow[taterInt];//How many rows
        int columnTracker=4;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            String line = reader.readLine();
            String numTubers=line;
            TableRow[] tr_tater = new TableRow[(int)Double.parseDouble(numTubers.trim())];//How many rows


            while(line!=null){
                Log.v("LINE", String.valueOf(lineCounter));
                if(columnTracker==4) {//This being ran every loop was erasing the previous runs, this checker is the easiest way to control this
                    tr_tater[tuberCounter] = new TableRow(this);
                    tr_tater[tuberCounter].setId((tuberCounter + 2) * headerID);//header will be 100, potato 1 will be 200, then 300 then 400 etc
                    tr_tater[tuberCounter].setBackgroundColor(Color.WHITE);
                    tr_tater[tuberCounter].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                }
                //do something with the line
                if(lineCounter==4){
                    //this should be the line the afg ratio is stored
                    avgText+=line;
                    textView.setText(avgText);
                    Log.v("AVG", avgText);
                }
                //lines 6 7 8 hold info for one tater
                //this may not be the cleanest method, but this keeps a similar pattern to main_table
                //made for easier understanding and debugging
                else if((lineCounter%3==0)&&(lineCounter>=6)){//line 6, 9, 12, etc
                    //Put Tuber Column in
                    textArray[0]=new TextView(this);
                    textArray[0].setId(tr_tater[tuberCounter].getId()+0+1);
                    textArray[0].setTextColor(Color.BLACK);
                    textArray[0].setPadding(50, 10, 50, 10);
                    textArray[0].setText("  "+ String.valueOf(tuberCounter+1));
                    Log.v("Tuber",String.valueOf(tuberCounter+1) );

                    tr_tater[tuberCounter].addView(textArray[0]);
                    ///read in length data
                    textArray[1]=new TextView(this);
                    textArray[1].setId(tr_tater[tuberCounter].getId()+1+1);
                    textArray[1].setTextColor(Color.BLACK);
                    textArray[1].setPadding(50, 10, 50, 10);
                    textArray[1].setText(line);
                    tr_tater[tuberCounter].addView(textArray[1]);
                    Log.v("LENGTH",line );
                    columnTracker=2;
                }
                else if((lineCounter%3==1)&&(lineCounter>=6)){//line 7, 10, 13
                    //read width data into table
                    textArray[2]=new TextView(this);
                    textArray[2].setId(tr_tater[tuberCounter].getId()+2+1);
                    textArray[2].setTextColor(Color.BLACK);
                    textArray[2].setPadding(50, 10, 50, 10);
                    textArray[2].setText(line);
                    tr_tater[tuberCounter].addView(textArray[2]);
                    Log.v("WIDTH",line );
                    columnTracker=3;
                }
                else if((lineCounter%3==2)&&(lineCounter>=6)){//line 8, 11, 14 etc
                    //read ratio data into table
                    textArray[3]=new TextView(this);
                    textArray[3].setId(tr_tater[tuberCounter].getId()+3+1);
                    textArray[3].setTextColor(Color.BLACK);
                    textArray[3].setPadding(50, 10, 50, 10);
                    textArray[3].setText(line);
                    tr_tater[tuberCounter].addView(textArray[3]);
                    Log.v("RATIO",line );
                    t1.addView(tr_tater[tuberCounter], new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    tuberCounter++;//increment so we move down to next row
                    columnTracker=4;
                }
                //end of if statements

                //increment the line
                line=reader.readLine();
                lineCounter++;
            }



        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}