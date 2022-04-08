package com.example.clamputer.run_master;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;


    //mi servono per disegnare il tracciato
    private ArrayList<LatLng> points = new ArrayList<LatLng>();
    private Polyline line; //added

    //formattazioni data
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    //elementi grafici
    private Button start_stop;
    private Button pause_resume;
    private Button abort;
    private TextView distanceText;
    private TextView velocitaKmH;
    private TextView velocitaKmH_max;
    private TextView velocitaKmH_media;
    private TextView calorie;

    private TextView timeStarted;
    private TextView falsoCronometro;
    private Chronometer cronometro;
    long timeWhenStopped; //per far riprendere il cronometro

    int mapZoomSP;

    //profilo in uso
    private String nomeProfilo;

    //peso
    private String peso;


    //database
    private DbAdapter dbHelper;


    private Date oraInizio;
    private Date oraFine;
    private Calendar calendar;
    private String currentDay;
    private double averageSpeed;
    private double speed;
    private double maxSpeed;
    private double distance;
    private double kcal;


    private int day;

    private LocationUpdatesService mService = null;
    private boolean mBound = false;

    //serve per mantenere lo zoom dell'utente
    boolean utenteZoommed = false;
    boolean primoGiro = true;
    float currentZoom;



    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            //if(mService!=null) requestLocationUpdatesON();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent serviceIntent = new Intent(this, LocationUpdatesService.class);

        /*
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            serviceIntent.setAction("101");
            //startForegroundService(serviceIntent);
        }*/
        serviceIntent.setAction("101");

        bindService(serviceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);



        nomeProfilo = getIntent().getStringExtra("PROFILO");//ricevo il nome profilo da playSession
        peso = getIntent().getStringExtra("PESO");//ricevo il peso profilo da playSession


        SharedPreferences.Editor settings = getSharedPreferences("pesoToService", MODE_PRIVATE).edit();
        settings.putString("peso", peso);
        settings.apply();

        distance = 0.0;
        maxSpeed = 0.0;
        averageSpeed = 0.0;
        speed = 0.0;
        kcal = 0.0;

        distanceText = (TextView) findViewById(R.id.distanzaPercorsa);
        velocitaKmH = (TextView) findViewById(R.id.speed);
        velocitaKmH_max = (TextView) findViewById(R.id.maxim);
        velocitaKmH_media = (TextView) findViewById(R.id.average);
        timeStarted = (TextView) findViewById(R.id.started);
        calorie = (TextView) findViewById(R.id.kcal);
        cronometro = (Chronometer) findViewById(R.id.chronometerNot);
        start_stop = (Button) findViewById(R.id.stop);
        pause_resume = (Button) findViewById(R.id.pause_notification);
        abort = (Button) findViewById(R.id.abort);
        falsoCronometro = (TextView) findViewById(R.id.falseChronometer);



        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY:
                currentDay = "monday";
                break;
            case Calendar.TUESDAY:
                currentDay = "tuesday";
                break;
            case Calendar.WEDNESDAY:
                currentDay = "wednesday";
                break;
            case Calendar.THURSDAY:
                currentDay = "thursday";
                break;
            case Calendar.FRIDAY:
                currentDay = "friday";
                break;
            case Calendar.SATURDAY:
                currentDay = "saturday";
                break;
            case Calendar.SUNDAY:
                currentDay = "sunday";
                break;
        }

        //formatto il cronometro
        cronometro.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";

                //cArg.setText(hh+":"+mm+":"+ss);

                String toHtml = "<big>" + hh + "</big>" + "<small>h</small>:" + "<big>" + mm + "</big>" + "<small>m</small>:" + "<big>" + ss + "</big>" + "<small>s</small>";

                cArg.setText(Html.fromHtml(toHtml, Html.FROM_HTML_MODE_COMPACT));
                //Log.i("formato", cArg.getText().toString());

            }
        });

        cronometro.setVisibility(View.INVISIBLE);
        String toHtml = "<big>00</big><small>h</small>:<big>00</big><small>m</small>:<big>00</big><small>s</small>";
        falsoCronometro.setText(Html.fromHtml(toHtml, Html.FROM_HTML_MODE_COMPACT));


        pause_resume.setEnabled(false);
        start_stop.setText("start");

        start_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );


                if (start_stop.getText().toString() == "start") {

                    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        Toast.makeText(MapsActivity.this, "Enable the GPS before clicking start!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    oraInizio = new Date();
                    timeStarted.setText(timeFormat.format(oraInizio));


                    //cronometro si avvia
                    cronometro.setBase(SystemClock.elapsedRealtime());
                    cronometro.start();

                    //chiedo aggiornamenti
                    requestLocationUpdatesON();

                    falsoCronometro.setVisibility(View.INVISIBLE);
                    cronometro.setVisibility(View.VISIBLE);
                    pause_resume.setEnabled(true);

                    start_stop.setText("stop");
                }
                else {

                    oraFine = new Date();//terminato alle ore

                    requestLocationUpdatesOFF();
                    pauseCronometro();
                    pause_resume.setText("resume");

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:

                                    Intent intent = new Intent(MapsActivity.this, playSession.class);
                                    intent.putExtra("PROFILO", nomeProfilo);
                                    start_stop.setText("start");
                                    requestLocationUpdatesOFF();

                                    //creo e apro il database
                                    dbHelper = new DbAdapter(MapsActivity.this);
                                    dbHelper.open();

                                    if(dbHelper.insertWork(dateFormat.format(oraInizio),dateFormat.format(oraFine),distance,averageSpeed,maxSpeed,kcal,nomeProfilo,currentDay)){
                                        Toast.makeText(MapsActivity.this, "Session and stats saved!", Toast.LENGTH_LONG).show();
                                        points.clear();
                                        startActivity(intent);
                                        finish();

                                    }
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //nothing
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setMessage("Have you finished your session?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            }
        });
        pause_resume.setText("pause");
        pause_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(pause_resume.getText().toString()=="pause"){
                    requestLocationUpdatesOFF();
                    pauseCronometro();
                    pause_resume.setText("resume");
                }
                else{
                    requestLocationUpdatesON();
                    resumeCronometro();
                    pause_resume.setText("pause");
                }
            }
        });

        abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(start_stop.getText().toString() == "stop") {
                    requestLocationUpdatesOFF();
                    pauseCronometro();
                    pause_resume.setText("resume");
                }

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(MapsActivity.this, playSession.class);
                                intent.putExtra("PROFILO", nomeProfilo);
                                startActivity(intent);
                                finish();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //nothing
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Do you really want to abort the session?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });


        //sono costretto ad usare le api di android per verificare che l'utente abbia acceso o spento il gps
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(this, "Enable the GPS!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Press start when you're ready!", Toast.LENGTH_LONG).show();

        }


        //timeFormat.format(oraInizio);


        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);


    }

    public void requestLocationUpdatesOFF(){
        mService.removeLocationUpdates();
        //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
    public void requestLocationUpdatesON(){

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                //Location Permission already granted
                //mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mService.requestLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            //mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mService.requestLocationUpdates();
            mGoogleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        String mapStyleSP = prefs.getString("setMapStyle", null);

        mGoogleMap = googleMap;

        mGoogleMap.setPadding(0,450,0,0);// sposta i due bottoncini leggermente in basso

        if (mapStyleSP != null) {

            if (mapStyleSP.equals("Normale")) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else if (mapStyleSP.equals("Terrain")) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (mapStyleSP.equals("Hybrid")) {
                //a causa della mappa troppo scura, il colore originale non sarebbe risultato visibile
                distanceText.setTextColor(Color.WHITE);
                velocitaKmH.setTextColor(Color.WHITE);
                velocitaKmH_max.setTextColor(Color.WHITE);
                velocitaKmH_media.setTextColor(Color.WHITE);
                calorie.setTextColor(Color.WHITE);
                timeStarted.setTextColor(Color.WHITE);
                cronometro.setTextColor(Color.WHITE);
                falsoCronometro.setTextColor(Color.WHITE);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else if (mapStyleSP.equals("Satellite")) {
                //a causa della mappa troppo scura, il colore originale non sarebbe risultato visibile
                distanceText.setTextColor(Color.WHITE);
                velocitaKmH.setTextColor(Color.WHITE);
                velocitaKmH_max.setTextColor(Color.WHITE);
                velocitaKmH_media.setTextColor(Color.WHITE);
                calorie.setTextColor(Color.WHITE);
                timeStarted.setTextColor(Color.WHITE);
                cronometro.setTextColor(Color.WHITE);
                falsoCronometro.setTextColor(Color.WHITE);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (mapStyleSP.equals("None")) {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
        else{

            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        mapZoomSP = prefs.getInt("setZoomMap", 0);

        //rendo leggermente opaca la mappa
        View v = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        v.setAlpha(0.9f);
    }



    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mService.requestLocationUpdates();
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onPause() {


        super.onPause();
        //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        overridePendingTransition(0, 0);

        if(myReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        myReceiver = null;
    }

    private void redrawLine(){

        mGoogleMap.clear();  //elimina tutti i Markers e Polylines

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        line = mGoogleMap.addPolyline(options); //aggiunge una Polyline
    }
    private void pauseCronometro(){
        timeWhenStopped = cronometro.getBase() - SystemClock.elapsedRealtime();
        cronometro.stop();
    }

    private void resumeCronometro(){
        cronometro.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        cronometro.start();
    }

    private MyBroadcastReceiver myReceiver;

    @Override
    public void onResume() {

        myReceiver = new MyBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter("DataFromService");
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);

        super.onResume();

    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Here you have the received broadcast
            // And if you added extras to the intent get them here too
            // this needs some null checks
            Bundle b = intent.getExtras();
            distance = b.getDouble("distanceKm");
            speed = b.getDouble("speed");
            averageSpeed = b.getDouble("averageSpeed");
            maxSpeed = b.getDouble("maxSpeed");
            LatLng latLng = b.getParcelable("latLng");
            points = b.getParcelableArrayList("points");
            if(points.size()>0){
                redrawLine();
            }

            //Log.i("kcal", String.valueOf(distance) + " " + String.valueOf(peso));
            kcal=0.9*distance*Integer.parseInt(peso);

            distanceText.setText(String.format("%.3f",distance)+"Km");
            velocitaKmH.setText(String.valueOf(String.format("%.2f",speed)+"Km/h"));
            velocitaKmH_media.setText(String.valueOf(String.format("%.2f",averageSpeed)+"Km/h"));
            velocitaKmH_max.setText(String.valueOf(String.format("%.2f",maxSpeed)+"Km/h"));
            calorie.setText(String.valueOf(String.format("%.2f",kcal)+"kcal"));




            //la prima posizione è quella delle shared preferences, le successive sono scelte dall'utente
            if (mapZoomSP != 0) {
                //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoomSP));
                //Log.i("zoom", String.valueOf(mGoogleMap.getCameraPosition().zoom));
                if(primoGiro) {
                    primoGiro=false;
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(mapZoomSP));
                }
                else{
                    currentZoom = mGoogleMap.getCameraPosition().zoom;
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom));
                }
            } else {
                //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                if (primoGiro) {
                    primoGiro=false;
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
                else{
                    currentZoom = mGoogleMap.getCameraPosition().zoom;
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom));
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(3129999);
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
