package com.chorotega_potato_assessment_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);

       // Toolbar toolbar;
        //tool bar is generated in tool_bar.xml and is the bar at the top of both screens
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");//we can set the title using the toolbar allowing for more specific calls

        if(findViewById(R.id.fragment_container)!=null)//this fragment can be found in Activity_settings.xml
        {

            if(savedInstanceState!=null)
            {
                return;
            }

            getFragmentManager().beginTransaction().add(R.id.fragment_container,new SettingsFragment()).commit();
          //Calls the fragment thing for settings
        }

    }
}
