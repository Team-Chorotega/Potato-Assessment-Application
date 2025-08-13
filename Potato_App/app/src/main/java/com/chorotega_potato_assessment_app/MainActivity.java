package com.chorotega_potato_assessment_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.preference.PreferenceManager;

import static android.Manifest.permission.CAMERA;


//implements SharedPreferences.OnSharedPreferenceChangeListener was suggested but gave errors
public class MainActivity extends AppCompatActivity  {
    //Access Gallery
    Toolbar toolbar;

    private static final int GALLERY_REQUEST_CODE = 123;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private final Map<Integer, PermissionCallback> permissionCallbacks = new HashMap<>(); // Store callbacks for each request

    android.widget.ImageButton btn, btnPick, rstBtn, photoButton;
    android.widget.ImageView iv, iv2;
    android.widget.TextView text1, text2, text3, text4;

    android.graphics.drawable.BitmapDrawable drawable;
    android.graphics.Bitmap bitmap;
    String imageString="";
    Uri imageUri;

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        //tool bar is generated in tool_bar.xml and is the bar at the top of both screens
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Toast is the processing popup
        Context context = getApplicationContext();
        int duration= Toast.LENGTH_LONG;
        CharSequence text="Processing";
        Toast toast=Toast.makeText(context, text, duration);


        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);

        btnPick = findViewById(R.id.btnPickImage);
        rstBtn = findViewById(R.id.btnrst);
        btn = findViewById(R.id.button);
        iv = findViewById(R.id.imageView);
        iv2 = findViewById(R.id.imageView2);
        photoButton = findViewById(R.id.btnCamera);
        text4 = findViewById(R.id.avg);
        ContentValues values = new ContentValues();

//Check for camera permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]
                            {
                                    Manifest.permission.CAMERA
                            }, 100);
        }

        //OnClickListener Camera button
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 100);
            }
        });




        //Pick image from gallery
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);

            }
        });

        //Reset Button
        rstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        final Python py = Python.getInstance();

        //Trying this outside of OnClickListener
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);//pulls the info from settings


        // Go button listener
        btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)//added for the datastorage call
            @Override
            public void onClick(View v) {
                drawable = (BitmapDrawable)iv.getDrawable();

                // Return if there is no image to analyze
                if (imageUri == null && drawable == null) {
                    Toast.makeText(context, "Select an image", Toast.LENGTH_LONG).show();
                    return;
                }

                toast.show();
                Bitmap bitmap = Bitmap.createBitmap(drawable.getBitmap());

                String coinType=sharedPreferences.getString("Coin_Type","Quarter");//The settings are pulled on process run
                String processType=sharedPreferences.getString("Processing_Method", "Machine_Learning");
                String customSize=sharedPreferences.getString("Custom_Coin", "0");
                String customPass="Custom_"+customSize;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] imageBytes = baos.toByteArray();

                Log.v("COINSTRING", "Coin Type: "+coinType);
                Log.v("PROCESS", "Process Method: "+processType);
                Log.v("CUSTOM", "Coin Size: "+customSize);
                Log.v("CUSTOM", "Custom Pass: "+customPass);
                PyObject pyo;

                if(processType.equals("Machine_Learning")){
                    Log.v("PROCESS", "Running Machine Learning");
                    pyo = py.getModule("potato_project_machine_learning_chqv5");
                }
                else{
                    Log.v("PROCESS", "Running Classic Method");
                    pyo = py.getModule("potato_project_classic_chqv4");
                }

                List<PyObject> obj;
                if(coinType.equals("Custom")){
                    obj = pyo.callAttr("main", imageBytes, customPass).asList();
                }
                else {
                    obj = pyo.callAttr("main", imageBytes, coinType).asList();
                }

                // If there is no data, show an error and return
                Float dataValues[][];
                try {
                    dataValues = obj.get(0).toJava(Float[][].class);
                } catch(Exception e) {
                    Toast.makeText(context, "No Tubers Detected", Toast.LENGTH_LONG).show();
                    return;
                }

                String taterNum = obj.get(1).toJava(String.class);
                String minRatio = obj.get(2).toJava(String.class);
                String maxRatio = obj.get(3).toJava(String.class);
                String avgRatio = obj.get(4).toJava(String.class);
                //String outputImage = obj.get(5).toJava(String.class);
                byte imageData[] = obj.get(5).toJava(byte[].class);
                Log.v("Test", "Tater num output: "+ taterNum);
                Log.v("Array", "Array[0][0] "+ dataValues[0][0]);

                Bitmap bmp = BitmapFactory.decodeByteArray(imageData,0,imageData.length);

                iv.setImageBitmap(bmp);

                String dateString;
                boolean dataSuccess;
                if(isStoragePermissionGranted()) {
                    dateString = StoreImage(bmp); //passing the bitmap to Store Function to save to local files
                    if(dateString!=null){
                        dataSuccess=StoreData(dateString,taterNum,minRatio,maxRatio,avgRatio,coinType, dataValues);

                    }
                    else{dataSuccess=false;}
                }//original function was void, now StoreImage returns the name used for the file (basically just the time stamp)
                else {
                    Toast.makeText(context, "ERROR: Files not saved, please update permissions to allow data saving", Toast.LENGTH_LONG).show();
                }//May add a Toast telling them data not stored and to modify their permissions

//                String numText = "Tubers Detected: "+taterNum;
//                text1.setText(numText);
//                String minText = "Ratio Min: "+minRatio;
//                text2.setText(minText);
//                String maxText = "Ratio Max: "+maxRatio;
//                text3.setText(maxText);
                String avgText = " L/W avg: "+avgRatio;
                text4.setText(avgText);
                text4.setVisibility(View.VISIBLE);
                //--------------------------------------------------------------------------------------------------------------------------
                //This chunk of code works with the main_table in the bottom of activity_main.xml
                //This can likely get moved to a seperate function and it will still work.
                //it is just here as a first draft
                //https://stackoverflow.com/questions/24078275/how-to-add-a-row-dynamically-in-a-tablelayout-in-android
                TableLayout t1=(TableLayout)findViewById(R.id.main_table);
                t1.removeAllViewsInLayout();//removes previous entries on multiple runs

                //column headings
                TableRow tr_head= new TableRow(MainActivity.this);
                int headerID=10;
                tr_head.setId(headerID); //for some reason it wouldn't accept just straight 10
                tr_head.setBackgroundColor(Color.BLACK);
                tr_head.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                //add 4 sections for information
                /*
                 * # potatoes | Length | Width | Ratio
                 * Potato 1   | ####   | ####  | ####
                 * etc
                 * */


                int countId=headerID+1;//11
                int lengthId=headerID+2;//12
                int widthId=headerID+3;//13
                int ratioId=headerID+4;//14
                TextView potato_count = new TextView(MainActivity.this);
                potato_count.setId(countId);
                potato_count.setText("Tuber");
                potato_count.setTextColor(Color.WHITE);
                //the thing has .setPaddings(5,5,5,5); but leaving this out for now
                //trying with padding
                // potato_count.setPaddingRelative(); may also be an option
                potato_count.setPadding(50,20,50,20);
                tr_head.addView(potato_count);

                TextView length_head = new TextView(MainActivity.this);
                length_head.setId(lengthId);
                // length_head.setText("L(cm)");
                if(coinType.equals("None")){
                    length_head.setText("L(pixels)");
                }
                else{
                    length_head.setText("L(cm)");
                }
                length_head.setTextColor(Color.WHITE);
                length_head.setPadding(50,20,50,20);
                tr_head.addView(length_head);

                TextView width_head = new TextView(MainActivity.this);
                width_head.setId(widthId);
                //width_head.setText("W(cm)");
                if(coinType.equals("None")){
                    width_head.setText("W(pixels)");
                }
                else{
                    width_head.setText("W(cm)");
                }
                width_head.setTextColor(Color.WHITE);
                width_head.setPadding(50,20,50,20);
                tr_head.addView(width_head);

                TextView avg_head = new TextView(MainActivity.this);
                avg_head.setId(ratioId);
                avg_head.setText("L/W");
                avg_head.setTextColor(Color.WHITE);
                avg_head.setPadding(50,20,50,20);
                tr_head.addView(avg_head);

                //  t1.addView(tr_head, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_CONTENT ));
                t1.addView(tr_head, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                //This takes all that data we just put into the header and prints into the table

                //Now to make a for loop to fill out the potato data
                float taterFloat = Float.parseFloat(taterNum); //Added by Alex
                int taterInt= (int)taterFloat;//cast the taterNum string to an int for math
                TextView[] textArray = new TextView[4];//should be how many columns
                TableRow[] tr_tater = new TableRow[taterInt];//How many rows

                for(int i=0;i<taterInt;i++){//one row for every potato
                    tr_tater[i]=new TableRow(MainActivity.this);
                    tr_tater[i].setId((i+2)*headerID);//header will be 10, potato 1 will be 20, then 30 then 40 etc
                    tr_tater[i].setBackgroundColor(Color.WHITE);
                    tr_tater[i].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    //now add text to the table slots
                    for(int j=0;j<4;j++){// Potato # Length Width Ratio
                        textArray[j] = new TextView(MainActivity.this);
                        textArray[j].setId(tr_tater[i].getId()+j+1);//first one will be 21, then 22, 23, 24, then 31, 32, 33, 34 etc
                        textArray[j].setTextColor(Color.BLACK);
                        textArray[j].setPadding(50, 10, 50, 10);
                        if(j==0){
                            textArray[j].setText("  "+ (i+1));
                        }
                        else {
                            NumberFormat formatter = new DecimalFormat("#0.00"); // Added by Alex
                            String data = formatter.format(dataValues[i][j-1]); //arrays use 0 as a start, not 1 as a start, so we -1 to access the proper spot
                            textArray[j].setText(data);
                        }
                        tr_tater[i].addView(textArray[j]);
                    }
                    //this might be better outside of the for loop but will try inside first
                    t1.addView(tr_tater[i], new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                }
                //------------------------------------------------------------------------------------------------------------------------------
                //End of table generation
            }

        });
    }


    private void requestPermission(String permission, int requestCode, PermissionCallback callback) {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]
                        {
                                permission
                        }, requestCode);
        permissionCallbacks.put(requestCode, callback); // Store callback for later use
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionCallback callback = permissionCallbacks.remove(requestCode); // Remove used callback
        if (callback != null) {
            // Permission request for storage access
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with storage operations
                callback.onPermissionGranted();
            } else {
                // Permission denied, handle user rejection
                callback.onPermissionDenied();
            }
        } else if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
             if (requestCode == MY_CAMERA_PERMISSION_CODE) {
                Toast.makeText(MainActivity.this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //This generates the little bubble when the settings button is pushed
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);

        return true;
    }
    //This is what gets ran when the options button gets pushed
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;


            case R.id.action_history:
                startActivity(new Intent(this, HistorySearch.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }



    //Some python script stuff
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();

            iv.setImageURI(imageData);
        }
        //added for camera functionality
        else if(requestCode == 100 && resultCode == RESULT_OK && imageUri != null)
        {
            switch (requestCode) {

                case 100:
                    if (requestCode == 100)
                        if (resultCode == Activity.RESULT_OK) {
                            try {
                                Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
                                        getContentResolver(), imageUri);
                                iv.setImageBitmap(thumbnail);
                                //imageurl = getRealPathFromURI(imageUri);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
            }

        }
    }
    //python
    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    //This stores the processed image based on a timestamp and returns that stamp for storing data
    private String StoreImage(Bitmap procImage){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S");
        Date date= new Date();

        String dateString = dateFormat.format(date); //the string should hold a time stamp for naming
        //should look like 2021-03-16-17:43:21.6 for march 16 at 5:43pm and 21.6s
        //Update, removing the : symbol and moving to only -
        //2021-03-16-17-43-21-6
        Log.v("DATE", "StoreImage has made time: "+dateString);

        //https://stackoverflow.com/questions/41952535/saving-image-from-image-view-into-internal-external-device-storage
        //  String root = Environment.getExternalStorageDirectory().toString();// this is a root file, not local
        //File myDir = new File(root+"/saved_images/potato_App");//the link has  File myDir = new File(root+"/saved_images"), im seeing if I can do something here
        Context context=this;
        String root =context.getFilesDir().toString();
        //attempting to use local storage!
        File myDir = new File(context.getFilesDir(),"saved_images");

        Log.v("ROOT", "The root is:"+root);
        Log.v("PATH", "Full directory path:"+myDir);
        if(myDir.mkdirs()){//this will make the directory saved_images if it doesnt exist.
            Log.v("DIR", "mkDirs() was successful");

        }
        else{
            Log.v("DIR", "mkDirs() FAILED" );
        }
        //use dateString for the names
        String fileName= dateString+".jpg";
        Log.v("NAME", "StoreImage named file: "+fileName);
        File file = new File(myDir, fileName);
        Log.v("FILE", "File made");
        if(file.exists()) file.delete();//if a file by the same name already exists, delete it. May need reworked for (duplicate)
        try{
            FileOutputStream out = new FileOutputStream(file);
            procImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.v("FILE", "We tried the file store");
        if(file.exists()){
            Log.v("EXISTS", "HEY IT SAVED");
            return dateString;
        }
        else{return null;}

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)//This line was added by the SDK because of the Environment.DirectoryDocuments line
    private boolean StoreData(String dateString, String numTater, String minRatio, String maxRatio, String avgRatio,String coinType, Float dataValues[][]){
        //This function takes the time stamp for the image, and the data related to the image to be stored in a basic file
        //will likely make a bunch of .txt files for simplicity

        //Follow the same pattern we did for the image store
        NumberFormat formatter = new DecimalFormat("#0.00"); // Added by Alex

        Context context=this;
        String root =context.getFilesDir().toString();
        File myDir = new File(context.getFilesDir(),"saved_data");
        //directory is now "root"/saved_data, wherever app file storage is

        Log.v("DATA", "File to be saved in: "+myDir);
        myDir.mkdirs();
        String fileName=dateString+".txt";
        Log.v("DATA", "File Name: "+fileName);

        File file = new File(myDir,fileName);

        // Find the user store
        File userDocs = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File userFile = new File(userDocs, fileName);

        // Ensure the user writable location exists
        userFile.mkdirs();

        if(file.exists()){file.delete();}//we will likely implement some sort of duplication in the future, for now lets just erase old stuff
        if(userFile.exists()){userFile.delete();}

        Log.v("USERFILE", "Attempting to locate userfile: "+ userFile.getPath());//testing for new file location

        String newLine = System.getProperty("line.separator");//Because apparently java doesnt have \n???
        try{
            FileWriter out = new FileWriter(file);
            out.write(numTater+newLine+minRatio+newLine+maxRatio+newLine+avgRatio+newLine+coinType+newLine);
            //FileOutputStream fos =new FileOutputStream(userFile); gonna try filewriter first

            FileWriter userOut=new FileWriter(userFile);
            userOut.write(numTater+newLine+minRatio+newLine+maxRatio+newLine+avgRatio+newLine+coinType+newLine);

            //Wrote out the stuff in the file as needed
            //This double for loop should iterate through the 2D array for dataValues and write to the txt
            //4/15/21 adding

            for(int rows=0; rows<dataValues.length;rows++){//dataValues.length should tell us how many rows the 2D array has
                /*we know our array is only 3 columns wide, length, width, ratio
                 * [] [] []
                 * [] [] []
                 * [] [] []
                 * [] [] []
                 *
                 * */
                for(int columns=0; columns < 3; columns++){
                    out.write(formatter.format(dataValues[rows][columns])+newLine); // Added by Alex
                    userOut.write(formatter.format(dataValues[rows][columns])+newLine);
                    Log.v("POTATO", "Potato: "+rows+" -"+ dataValues[rows][0]+" "+dataValues[rows][1]+" "+dataValues[rows][2]);
                }
                //this should do 00, 01, 02, then 10, 11, 12, 20, 21, 22, and so on

            }


            out.flush();//following previous pattern, may not be 100% needed
            out.close();
            userOut.flush();
            userOut.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(file.exists()){
            Log.v("DATA", "HEY IT SAVED");
            if(userFile.exists()){
                Log.v("USER", "HEY IT SAVED TOO");
            }
            Toast.makeText(context, "Data Saved Successfully", Toast.LENGTH_LONG).show();
            return true;
        }
        else{return false;}
        //successful files should have the following
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
    }




    //https://stackoverflow.com/questions/41952535/saving-image-from-image-view-into-internal-external-device-storage
    public  boolean isStoragePermissionGranted() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            /*
              Since we are saving in app's internal storage, no permission is required
             */
            return true;

            /*
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("PERM","Permission is granted");
                return true;
            } else {

                Log.v("PERM","Permission is revoked");
                //  ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                //  getActivity() throws an error, commenting this out for now
                return false;
            }
            */
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("PERM","Permission is granted for sdk<23");
            return true;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}