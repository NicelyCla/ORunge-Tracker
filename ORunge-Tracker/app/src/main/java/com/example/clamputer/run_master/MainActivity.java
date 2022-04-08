package com.example.clamputer.run_master;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //elementi grafici
    private Button openActivityProfiles;
    private Button openSettings;
    private Button openNewProfile;
    private Button add;
    private AutoCompleteTextView nome;
    private AutoCompleteTextView nickname;
    private AutoCompleteTextView eta;
    private AutoCompleteTextView peso;
    private ScrollView adaptRect;


    private DbAdapter dbHelper;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creo e apro il database
        dbHelper = new DbAdapter(this);
        dbHelper.open();
        //dbHelper.close();

        //mi è utile per vedere se il database è stato creato correttamente
        File database = getApplicationContext().getDatabasePath("runmaster2.db");
        if (!database.exists()) {
            Log.i("Database", " non lo trovo");
        } else {
            Log.i("Database", "l'ho trovato");
            Log.i("Database",database.toString());
        }

        adaptRect = (ScrollView) findViewById(R.id.quadratoColor);

        add = (Button) findViewById(R.id.buttonAdd);
        openSettings =(Button) findViewById(R.id.buttonSettings);
        openNewProfile = (Button) findViewById(R.id.buttonNewProfile);
        nome = (AutoCompleteTextView) findViewById(R.id.nomeProfilo);
        nickname = (AutoCompleteTextView) findViewById(R.id.nomeAtleta);
        eta = (AutoCompleteTextView) findViewById(R.id.eta);
        peso = (AutoCompleteTextView) findViewById(R.id.peso);
        openActivityProfiles = (Button) findViewById(R.id.buttonProfiles);


        //rendo il layout dinamicamente adattabile ai vari display, facendo una semplice proporzione con i valori del mio telefono
        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params=adaptRect.getLayoutParams();
            Log.i("params", String.valueOf(params.width));
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            //int height = displayMetrics.heightPixels;
            Log.i("params", String.valueOf(params.height));
            params.width = (int) (894 * width) / 1080;
            adaptRect.setLayoutParams(params);

            params = nome.getLayoutParams();
            params.width = (int) (693 * width) / 1080;
            Log.i("parax", String.valueOf(width));
            nome.setLayoutParams(params);
            nickname.setLayoutParams(params);
            eta.setLayoutParams(params);
            peso.setLayoutParams(params);


            if(width!=1080) {
                Log.i("params", String.valueOf(width));
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * width) / 1440;
                openNewProfile.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openNewProfile.setLayoutParams(params);

            }
            else{
                Log.i("params", String.valueOf(width));
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * width) / 1440;
                openNewProfile.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openNewProfile.setLayoutParams(params);
            }

            //openNewProfile.getWidth();
        }
        else{
            ViewGroup.LayoutParams params=adaptRect.getLayoutParams();
            Log.i("params", String.valueOf(params.width));
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;

            if (height!=1080) {
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * height) / 1440;
                openNewProfile.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openNewProfile.setLayoutParams(params);
            }
            else{
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * height) / 1440;
                openNewProfile.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openNewProfile.setLayoutParams(params);
            }
        }


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String nickAthlete = nickname.getText().toString();
                String ageAthleteString  = eta.getText().toString();
                String pesoAthlete = peso.getText().toString();
                String nomeProfilo = nome.getText().toString();
                Date dataCreazione = new Date();
                dateFormat.format(dataCreazione);
                String dataCreazioneS = dateFormat.format(dataCreazione);

                if( nomeProfilo.trim().length() > 0 && nickAthlete.trim().length()>0 &&
                        ageAthleteString.trim().trim().length()>0 && pesoAthlete.trim().length()>0 &&
                        !nomeProfilo.contains("'") && !nomeProfilo.contains("\"") &&
                        !nickAthlete.contains("'") && !nickAthlete.contains("\"") &&
                        !ageAthleteString.contains("'") && !ageAthleteString.contains("\"") &&
                        !pesoAthlete.contains("'") && !pesoAthlete.contains("\"")){

                    int ageAthlete = Integer.parseInt(ageAthleteString);
                    int pesoAthleteInt = Integer.parseInt(pesoAthlete);
                    if (dbHelper.insertProfile(nomeProfilo, ageAthlete, pesoAthleteInt, dataCreazioneS, nickAthlete)) {
                        //finish();
                        //startActivity(getIntent());
                        nome.setText("");
                        nickname.setText("");
                        eta.setText("");
                        peso.setText("");
                        Toast.makeText(getApplicationContext(), "The profile \"" + nomeProfilo + "\" has been created.", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "An error occurred.", Toast.LENGTH_LONG).show();
                    }
                }
                else if(nomeProfilo.contains("'") || nomeProfilo.contains("\"") ||
                        nickAthlete.contains("'") || nickAthlete.contains("\"") ||
                        ageAthleteString.contains("'") || ageAthleteString.contains("\"") ||
                        pesoAthlete.contains("'") || pesoAthlete.contains("\"")){
                    Toast.makeText(getApplicationContext(), "You have used characters that are not allowed.", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please, complete correctly all the fields.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        //apro l'activity dei profili salvati
        openActivityProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbHelper.close();
                Intent intent = new Intent(MainActivity.this, saved_profiles.class);
                startActivity(intent);
                finish();
            }
        });

        openSettings = (Button) findViewById(R.id.buttonSettings);
        openSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbHelper.close();
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

     }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
