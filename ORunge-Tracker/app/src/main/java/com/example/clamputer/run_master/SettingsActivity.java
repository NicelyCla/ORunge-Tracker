package com.example.clamputer.run_master;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private Button openMain;
    private Button openActivityProfiles;
    private Button openSettings;
    private Button reset;
    private Button addChanges;

    private RadioGroup chooseMap;
    private RadioButton normalRadio;
    private RadioButton terrainRadio;
    private RadioButton hybridRadio;
    private RadioButton satelliteRadio;
    private RadioButton noneRadio;

    private RadioGroup choosePriority;
    private RadioButton balanced;
    private RadioButton high;
    private RadioButton low;
    private RadioButton no;

    private SeekBar seekbar;
    private TextView zoom;
    private AutoCompleteTextView sensibility;
    private AutoCompleteTextView setInterval;
    private AutoCompleteTextView setFastInterval;

    private ScrollView adaptRect;


    private int progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        addChanges = (Button) findViewById(R.id.addSettings);
        reset = (Button) findViewById(R.id.reset);
        seekbar = (SeekBar) findViewById(R.id.seekBar3);
        zoom = (TextView) findViewById(R.id.valueOfSeek);
        sensibility = (AutoCompleteTextView) findViewById(R.id.sensibility);
        setInterval = (AutoCompleteTextView) findViewById(R.id.setInterval);
        setFastInterval = (AutoCompleteTextView) findViewById(R.id.setFastInterval);
        openActivityProfiles = (Button) findViewById(R.id.buttonProfiles);
        openSettings = (Button) findViewById(R.id.buttonSettings);
        openMain = (Button) findViewById(R.id.buttonNewProfile);


        chooseMap = (RadioGroup) findViewById(R.id.chooseMap);
        normalRadio = (RadioButton)findViewById(R.id.normal);
        terrainRadio = (RadioButton)findViewById(R.id.terrain);
        hybridRadio = (RadioButton)findViewById(R.id.hybrid);
        satelliteRadio = (RadioButton)findViewById(R.id.satellite);
        noneRadio = (RadioButton)findViewById(R.id.none);

        choosePriority = (RadioGroup) findViewById(R.id.choosePriority);
        balanced = (RadioButton)findViewById(R.id.balanced);
        high = (RadioButton) findViewById(R.id.high);
        low = (RadioButton) findViewById(R.id.low);
        no = (RadioButton) findViewById(R.id.no);

        adaptRect = (ScrollView) findViewById(R.id.quadratoColor);

        //rendo il layout dinamicamente adattabile ai vari display, facendo una semplice proporzione con i valori del mio telefono
        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params=adaptRect.getLayoutParams();
            //Log.i("params", String.valueOf(params.width));
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            //Log.i("params", String.valueOf(params.height));
            params.width = (int) (894 * width) / 1080;
            adaptRect.setLayoutParams(params);

            if(width!=1080) {
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * width) / 1440;
                openMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openMain.setLayoutParams(params);
            }
            else{
                Log.i("params", String.valueOf(width));
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * width) / 1440;
                openMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openMain.setLayoutParams(params);
            }
        }
        else {
            ViewGroup.LayoutParams params;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;

            if (height != 1080) {
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * height) / 1440;
                openMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openMain.setLayoutParams(params);
            }
            else{
                Log.i("params", String.valueOf(height));
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * height) / 1440;
                openMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openMain.setLayoutParams(params);
            }
        }

            checkIfUsersSavedData();

        progress = Integer.parseInt(zoom.getText().toString());
        openMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        openActivityProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, saved_profiles.class);
                startActivity(intent);
            }
        });

        chooseMap.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                int radioButtonId = chooseMap.getCheckedRadioButtonId();
                normalRadio = (RadioButton)findViewById(radioButtonId);
            }
        });

        choosePriority.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                int radioButtonId = choosePriority.getCheckedRadioButtonId();
                balanced = (RadioButton)findViewById(radioButtonId);
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress=i;
                if (progress<1){
                    seekbar.setProgress(1);
                }
                zoom.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        addChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(!sensibility.getText().toString().equals("0") && !setInterval.getText().toString().equals("0") && !setFastInterval.getText().toString().equals("0")) {
                    SharedPreferences.Editor settings = getSharedPreferences("settings", MODE_PRIVATE).edit();

                    settings.putString("setMapStyle", normalRadio.getText().toString());
                    settings.putString("setPriority", balanced.getText().toString());
                    settings.putInt("setZoomMap", Integer.parseInt(zoom.getText().toString()));
                    settings.putInt("sensibility", Integer.parseInt(sensibility.getText().toString()));
                    settings.putInt("setInterval", Integer.parseInt(setInterval.getText().toString()));
                    settings.putInt("setFastInterval", Integer.parseInt(setFastInterval.getText().toString()));

                    settings.apply();

                    Toast.makeText(SettingsActivity.this, "All changes have been saved!", Toast.LENGTH_LONG).show();

                    //settings.edit().clear().commit(); eliminare
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Impossible to enter null values!", Toast.LENGTH_LONG).show();
                }

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sensibility.setText("5");
                setInterval.setText("2500");
                setFastInterval.setText("2000");
                seekbar.setProgress(15);
                normalRadio = (RadioButton)findViewById(R.id.normal);
                balanced = (RadioButton)findViewById(R.id.balanced) ;
                normalRadio.setChecked(true);
                balanced.setChecked(true);
                Toast.makeText(SettingsActivity.this, "All changes have been resetted!", Toast.LENGTH_LONG).show();

                SharedPreferences.Editor settings = getSharedPreferences("settings", MODE_PRIVATE).edit();

                settings.putString("setMapStyle", normalRadio.getText().toString());
                settings.putString("setPriority",balanced.getText().toString());
                settings.putInt("setZoomMap", Integer.parseInt(zoom.getText().toString()));
                settings.putInt("sensibility", Integer.parseInt(sensibility.getText().toString()));
                settings.putInt("setInterval", Integer.parseInt(setInterval.getText().toString()));
                settings.putInt("setFastInterval", Integer.parseInt(setFastInterval.getText().toString()));

                settings.apply();


            }
        });



    }

    void checkIfUsersSavedData(){
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        String mapStyleSP = prefs.getString("setMapStyle", null);
        String setPriority = prefs.getString("setPriority", null);
        int mapZoomSP = prefs.getInt("setZoomMap", 0);
        int sensibilitySP = prefs.getInt("sensibility", 0);
        int intervalSP = prefs.getInt("setInterval", 0);
        int fastIntervalSP = prefs.getInt("setFastInterval", 0);

        if (mapStyleSP != null && mapZoomSP != 0 && sensibilitySP !=0 && intervalSP !=0 && fastIntervalSP != 0 && setPriority != null) {
            sensibility.setText(String.valueOf(sensibilitySP));
            setInterval.setText(String.valueOf(intervalSP));
            setFastInterval.setText(String.valueOf(fastIntervalSP));
            seekbar.setProgress(mapZoomSP);
            zoom.setText(String.valueOf(mapZoomSP));


            if (mapStyleSP.equals("Normale")){
                normalRadio.setChecked(true);
            }
            else if(mapStyleSP.equals("Terrain")){
                terrainRadio.setChecked(true);
            }
            else if(mapStyleSP.equals("Hybrid")){
                hybridRadio.setChecked(true);
            }
            else if(mapStyleSP.equals("Satellite")){
                satelliteRadio.setChecked(true);
            }
            else if(mapStyleSP.equals("None")){
                noneRadio.setChecked(true);
            }

            /************/

            if (setPriority.equals("Balanced power accuracy")){
                balanced.setChecked(true);
            }
            else if(setPriority.equals("High accuracy")){
                high.setChecked(true);
            }
            else if(setPriority.equals("Low power")){
                low.setChecked(true);
            }
            else if(setPriority.equals("No power")){
                no.setChecked(true);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
