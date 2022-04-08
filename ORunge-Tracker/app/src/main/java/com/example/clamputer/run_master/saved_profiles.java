package com.example.clamputer.run_master;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class saved_profiles extends AppCompatActivity {

    private Button openActivityMain;
    private Button openActivityProfiles;

    private Button openSettings;
    private DbAdapter dbHelper;

    private LinearLayout ll;

    private Cursor nomiProfili;
    private String nome;

    String nomeAtleta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_profiles);

        //creo e apro il database
        dbHelper = new DbAdapter(this);
        dbHelper.open();

        nomiProfili = dbHelper.fetchAllProfiles();

        openActivityMain = (Button) findViewById(R.id.buttonNewProfile);
        openActivityMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbHelper.close();
                Intent intent = new Intent(saved_profiles.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        openSettings = (Button) findViewById(R.id.buttonSettings);
        openSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbHelper.close();
                Intent intent = new Intent(saved_profiles.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        openActivityProfiles = (Button) findViewById(R.id.buttonProfiles);


        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            if (width !=1080) {
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * width) / 1440;
                openActivityMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openActivityMain.setLayoutParams(params);
            }
            else{
                Log.i("params", String.valueOf(width));
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * width) / 1440;
                openActivityMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openActivityMain.setLayoutParams(params);
            }

            //openNewProfile.getWidth();
        }
        else {
            ViewGroup.LayoutParams params;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;

            if (height != 1080) {
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * height) / 1440;
                openActivityMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openActivityMain.setLayoutParams(params);
            }
            else{
                Log.i("params", String.valueOf(height));
                params = openActivityProfiles.getLayoutParams();
                params.width = (int) (396 * height) / 1440;
                openActivityMain.setTextSize(12.3f);
                openActivityProfiles.setTextSize(12.3f);
                openSettings.setTextSize(12.3f);
                openActivityProfiles.setLayoutParams(params);
                openSettings.setLayoutParams(params);
                openActivityMain.setLayoutParams(params);
            }
        }



        nomiProfili = dbHelper.fetchAllProfiles();
        generaProfilo(nomiProfili);
        //dbHelper.close();




    }

    //importante per differenziare i nomi dei diversi profili per ogni bottone dinamico
    View.OnClickListener getOnClickHandler(final Button button)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                //button.setText("text now set.. ");
                Intent intent = new Intent(saved_profiles.this, playSession.class);
                intent.putExtra("PROFILO",button.getText());
                startActivity(intent);
            }
        };


    }

    View.OnLongClickListener getOnLongClickHandler(final Button button){
        return new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:

                                    if(dbHelper.deleteProfile(button.getText().toString())) {
                                        finish();
                                        startActivity(getIntent());
                                        Toast.makeText(getApplicationContext(), "Profile eliminated", Toast.LENGTH_LONG).show();
                                    }

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //nothing
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(saved_profiles.this);
                    builder.setMessage("Do you really want to delete this profile?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                return false;
            }
        };
    }

    public void generaProfilo(Cursor nomiProfili){


        //nomiProfili.getColumnCount();

        //Toast.makeText(getApplicationContext(), "There are "+nomiProfili.getColumnNames()+" profiles created", Toast.LENGTH_LONG).show();

        while(nomiProfili.moveToNext()) {
            int index;

            index = nomiProfili.getColumnIndexOrThrow("nome");
            nome = nomiProfili.getString(index);

            LinearLayout.LayoutParams params;

            Button btn = new Button(this);
            btn.setId((int)index);
            btn.setTag(nome);
            btn.setBackground(getResources().getDrawable(R.drawable.profilesbutton,null));
            btn.setText(nome);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            //proporione per adattare i bottoni ad i diversi schermi dei telefoni
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,(int)(150*height)/1080);
                params.bottomMargin = 15;
                btn.setTextSize(22);
                btn.setPadding((int)(140*height)/1080,/*(int)(30*height)/1080*/0,(int)(-1)*(140*height)/1080,0/*(int)(-1)*(30*height)/1080*/);
            }
            else{

                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,(int)(100*width)/1080);
                params.bottomMargin = 15;
                btn.setTextSize(18);
                btn.setPadding((int)(90*width)/1080,/*(int)(7*width)/1080*/0,(int)(-1)*(90*width)/1080,/*(int)(-1)*(7*width)/1080*/0);


            }
            //Log.i("width-height", String.valueOf(height)+" "+String.valueOf(height));

            ll = (LinearLayout) findViewById(R.id.scrollIdLayout);

            //btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,100));
            btn.setLayoutParams(params);
            btn.setTextColor(Color.rgb(255,255,255));
            btn.setTransformationMethod(null);
            btn.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

            //bottoni.add(btn);
            ll.addView(btn);

            btn.setOnClickListener(getOnClickHandler(btn));
            btn.setOnLongClickListener(getOnLongClickHandler(btn));


            /*
            qui mi sarei imbattuto nel problema "nome" che sarebbe dovuta essere final o globale, ma in entrambi i casi
            non avrebbe dato un "identità" diversa per ogni onClick, quindi l'handler è la soluzione a questo problema
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(saved_profiles.this, playSession.class);
                    intent.putExtra("PROFILO",nome);
                    startActivity(intent);
                }
            });
            */

        }

    }

    @Override
    protected void onResume() {
        if(ll!=null) {
            ll.removeAllViews();
        }
        nomiProfili = dbHelper.fetchAllProfiles();
        generaProfilo(nomiProfili);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}
