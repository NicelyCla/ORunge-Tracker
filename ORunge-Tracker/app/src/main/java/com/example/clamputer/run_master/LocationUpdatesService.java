package com.example.clamputer.run_master;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static java.lang.System.exit;


public class LocationUpdatesService extends Service{

    //private static final String TAG = LocationUpdatesService.class.getSimpleName();

    public static final String CHANNEL_1_ID = "channel1";
    private final IBinder mBinder = new LocalBinder();

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLastLocation;

    private NotificationManagerCompat notificationManager;


    //private Handler mServiceHandler;

    private double averageSpeed;
    private double speed;
    private double maxSpeed;
    private double distance;
    private double averageDistance;

    private List<Location> old_and_new_location = new ArrayList<Location>();
    private List<Double> tutteLeVelocita = new ArrayList<Double>();
    private ArrayList<LatLng> points = new ArrayList<LatLng>();

    private boolean primaVoltaCronometro = true;

    private Chronometer cronometroInvisibile;
    private Long timeWhenStopped; // per riprendere il cronometro

    private RemoteViews contentView;
    private String peso;
    private int pesoInt;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date tempo1,tempo2;
    int t;
    PowerManager mPM;
    PowerManager.WakeLock mWL;


    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {


        //wake-lock per impedire la chiusura dell'app in sleepmode
        mPM = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWL = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,
                "MyWakelockTag");
        mWL.acquire(10000);
        Log.i("MyWakeLockTag", "onCreate");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel1.setDescription("This is Channel 1");
            channel1.setSound(null,null);
            channel1.enableLights(false);
            channel1.setLightColor(Color.BLUE);
            channel1.enableVibration(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);

        }


        //mi serve per le notifiche
        contentView = new RemoteViews(getPackageName(), R.layout.custom_push);
        t=0;
        averageDistance=0;

        SharedPreferences pesoDalSP = getSharedPreferences("pesoToService", MODE_PRIVATE);
        peso = pesoDalSP.getString("peso",null);
        pesoInt = Integer.parseInt(peso);

        //mi devo allacciare ad un cronometro invisibile per poter mettere in pausa e ripristinare tutte le volte
        cronometroInvisibile = new Chronometer(this);
        cronometroInvisibile.setId((int)99887766);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();

                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    mLastLocation = location;


                    //Place current location marker
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    //mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                    /*
                    int mapZoomSP = prefs.getInt("setZoomMap", 0);

                    if (sensibilitySP != 0) {
                        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoomSP));
                    } else {
                        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    }
                    */

                    //calcolo la distanza percorsa ed il tempo
                    if (old_and_new_location.size() > 1) {
                        old_and_new_location.remove(0);
                    }
                    old_and_new_location.add(locationList.get(locationList.size() - 1));

                    //calcolo la velocità con getSpeed, il metodo restituisce la velocità in m/s, dobbiamo convertirla in kmph (km/h)
                    if(location.hasSpeed()) {
                        speed = location.getSpeed() * 3.6;
                        t=0;
                        Log.i("avera", "ho una speed");
                    }
                    else{
                        Log.i("avera", String.valueOf(t));

                        //implemento un metodo manuale per determinare la velocità, sulla base della media delle ultime 4 distanze
                        if(t==1){
                            tempo1 = new Date();
                            averageDistance += old_and_new_location.get(0).distanceTo(old_and_new_location.get(old_and_new_location.size() - 1));
                        }
                        else if(t==2){
                            averageDistance += old_and_new_location.get(0).distanceTo(old_and_new_location.get(old_and_new_location.size() - 1));
                        }
                        else if(t==3){
                            t = -1;
                            tempo2 = new Date();
                            averageDistance += old_and_new_location.get(0).distanceTo(old_and_new_location.get(old_and_new_location.size() - 1));

                            String tempo1String = dateFormat.format(tempo1);
                            String tempo2String = dateFormat.format(tempo2);

                            try {
                                Date oldTime = dateFormat.parse(tempo1String);
                                Date newTime = dateFormat.parse(tempo2String);
                                double difference = (newTime.getTime() - oldTime.getTime())/1000;
                                if(difference!=0) {
                                    Log.i("avera", String.valueOf(averageDistance/4.0)+ " "+String.valueOf(difference));
                                    speed = ((averageDistance / 3.0) / difference) * 3.6;
                                    averageDistance=0;

                                }
                                else{
                                    speed = 0;
                                }


                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.i("avera", "errore");

                            }

                        }
                        t++;

                    }

                    //tutte le istruzioni dentro il suddetto controllo si eseguono solo se si corre ad almeno 5km/h (camminata veloce)

                    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);


                    int sensibilitySP = prefs.getInt("sensibility", 0);

                    if (sensibilitySP == 0) {
                        sensibilitySP = 5;
                    }
                     /*
                    tramite questo controllo riesco ad aggirare il margine di errore del gps, che altrimenti avrebbe percecpito
                    degli spostamenti, seppur minimi, istantaneamente, aumentando, stando fermi i metri percorsi, e disegnando
                    tracciati imprecisi.
                     */


                    if (speed >= (double) sensibilitySP) {

                        points.add(latLng);

                        distance += old_and_new_location.get(0).distanceTo(old_and_new_location.get(old_and_new_location.size() - 1));
                        tutteLeVelocita.add(speed);

                        //redrawLine();//funzione che mi permette di disegnare il tracciato dinamicamente

                        double media = 0.0;
                        for (int x = 0; x < tutteLeVelocita.size(); x++) {
                            if (tutteLeVelocita.get(x)<50.0) {
                                media += tutteLeVelocita.get(x);
                            }
                        }

                        averageSpeed = media / tutteLeVelocita.size();

                        //velocitaKmH_media.setText(String.valueOf(String.format("%.2f",averageSpeed)+"Km/h"));
                        if (speed > maxSpeed && speed<50.0) {
                            maxSpeed = speed;
                            //velocitaKmH_max.setText(String.valueOf(String.format("%.2f",maxSpeed)+"Km/h"));
                        }



                    }

                    double distanceKm = distance * 0.001;

                    //passa i parametri all'activity della mappa che eventualmente provvederà a stamparli
                    Intent intent = new Intent("DataFromService");
                    Bundle bundle = new Bundle();
                    bundle.putDouble("distanceKm", distanceKm);
                    bundle.putDouble("speed", speed);
                    bundle.putDouble("averageSpeed",averageSpeed);
                    bundle.putDouble("maxSpeed",maxSpeed);
                    bundle.putParcelableArrayList("points",points);
                    bundle.putParcelable("latLng",latLng);

                    intent.putExtras(bundle);
                    LocalBroadcastManager.getInstance(LocationUpdatesService.this).sendBroadcast(intent);
                    //distanceText.setText(String.format("%.3f",distanceKm)+"Km");
                    //velocitaKmH.setText(String.valueOf(String.format("%.2f",speed)+"Km/h"));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendNotificationOreo();
                    }
                    else {
                        sendNotification();
                    }
                    /*
                    double accuratezza0 = old_and_new_location.get(0).getAccuracy();
                    double accuratezza1 = old_and_new_location.get(old_and_new_location.size() - 1).getAccuracy();
                    Log.i("accuratezza", String.valueOf(accuratezza0) + " " + String.valueOf(accuratezza1));
                    */

                }
            }
        };

        createLocationRequest();

        //mi serve per far aprire il MapsActivity al click della notifica
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(android.R.color.transparent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(intent)
                .setOngoing(true)
                .setContent(contentView)



                .build();
        startForeground(3129999, notification);


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //return START_STICKY;

        return START_NOT_STICKY;
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public IBinder onBind(Intent intentt) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.

        Log.i("Servizio", "in onBind()");
        //stopForeground(true);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i("Servizio", "in onRebind()");
        //stopForeground(true);
        super.onRebind(intent);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("Servizio", "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.

        //stopSelf();

        return true; // Ensures onRebind() is called when a client re-binds.
    }

    public void requestLocationUpdates() {

        //sendNotification();

        if(!primaVoltaCronometro){
            resumeCronometro();
        }
        else{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                //startForegroundService(new Intent(getApplicationContext(), LocationUpdatesService.class));
                ContextCompat.startForegroundService(this,new Intent(getApplicationContext(), LocationUpdatesService.class));
            }
            else{
                startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotificationOreo();
        }
        else {
            sendNotification();

        }
        Log.i("Servizio", "Requesting location updates");
        //Utils.setRequestingLocationUpdates(this, true);


        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            //Utils.setRequestingLocationUpdates(this, false);
            Log.e("Servizio", "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    private void pauseCronometro(){
        long oldElapsedRealYime = SystemClock.elapsedRealtime();
        timeWhenStopped = cronometroInvisibile.getBase() - oldElapsedRealYime;
        cronometroInvisibile.stop();
        contentView.setChronometer(R.id.chronometerNot, timeWhenStopped+oldElapsedRealYime, "\uD83C\uDFC3%s\uD83D\uDCA8", false);

    }

    private void resumeCronometro(){
        long oldElapsedRealYime = SystemClock.elapsedRealtime();
        cronometroInvisibile.setBase(oldElapsedRealYime + timeWhenStopped);
        cronometroInvisibile.start();
        contentView.setChronometer(R.id.chronometerNot, oldElapsedRealYime+timeWhenStopped, "\uD83C\uDFC3%s\uD83D\uDCA8", true);

    }

    public void removeLocationUpdates() {

        pauseCronometro();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotificationOreo();
            //stopForeground(true);
        }
        else {
            sendNotification();
        }
        Log.i("Servizio", "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException unlikely) {
            Log.e("Servizio", "Lost location permission. Could not remove updates. " + unlikely);
        }
    }


    private void createLocationRequest() {

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        int intervalSP = prefs.getInt("setInterval", 0);
        int fastIntervalSP = prefs.getInt("setFastInterval", 0);
        String setPriority = prefs.getString("setPriority", null);


        if (intervalSP == 0 || fastIntervalSP == 0) {

            intervalSP = 2500;
            fastIntervalSP = 2000;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(intervalSP);
        mLocationRequest.setFastestInterval(fastIntervalSP);


        if (setPriority != null) {
            if (setPriority.equals("Balanced power accuracy")) {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            } else if (setPriority.equals("High accuracy")) {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else if (setPriority.equals("Low power")) {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            } else if (setPriority.equals("No power")) {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
            }
        }
        else{
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }


    public void sendNotificationOreo() {

        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        contentView.setTextViewText(R.id.title, "Custom notification");
        contentView.setTextViewText(R.id.text, "This is a custom layout");

        double distanceKm = distance*0.001;
        String distanza = String.format("%.3f",distanceKm);

        String velocita = String.format("%.2f",speed);

        String massima = String.format("%.2f", maxSpeed);

        String media = String.format("%.2f", averageSpeed);

        double kcal = pesoInt * 0.9 * distanceKm;
        String calorie = String.format("%.2f",kcal);

        //verifica che arrivano aggiornamenti, con un colore diverso per ogni aggiornamento ricevuto
        Random r = new Random(),g = new Random(), b = new Random();

        contentView.setTextColor(R.id.pallinaAggiornamenti, Color.rgb(r.nextInt(255),g.nextInt(255),b.nextInt(255)));
        contentView.setTextViewText(R.id.distanza,distanza+"Km");
        contentView.setTextViewText(R.id.velocita,velocita+"Km/h");
        contentView.setTextViewText(R.id.massima,massima+"Km/h");
        contentView.setTextViewText(R.id.media,media+"Km/h");
        contentView.setTextViewText(R.id.calorie,calorie+"kcal");

        //mi serve per far aprire il MapsActivity al click della notifica
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(android.R.color.transparent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(intent)
                .setOngoing(true)
                .setContent(contentView)


                .build();

        if(primaVoltaCronometro) {
            //startForeground(1,notification);

            long BasePerIcronometri = SystemClock.elapsedRealtime();

            cronometroInvisibile.setBase(BasePerIcronometri);
            contentView.setChronometer(R.id.chronometerNot, BasePerIcronometri, "\uD83C\uDFC3%s\uD83D\uDCA8", true);
            cronometroInvisibile.start();
            primaVoltaCronometro=false;
        }
        notificationManager.notify(3129999, notification);
    }
    public void sendNotification(){

        /*
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this,
                0, i, PendingIntent.FLAG_ONE_SHOT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Background")
                .setContentText("The app will continue to work even in background")
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pi);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    */

        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        contentView.setTextViewText(R.id.title, "Custom notification");
        contentView.setTextViewText(R.id.text, "This is a custom layout");

        double distanceKm = distance*0.001;
        String distanza = String.format("%.3f",distanceKm);

        String velocita = String.format("%.2f",speed);

        String massima = String.format("%.2f", maxSpeed);

        String media = String.format("%.2f", averageSpeed);

        double kcal = pesoInt * 0.9 * distanceKm;
        String calorie = String.format("%.2f",kcal);

        //verifica che arrivano aggiornamenti, con un colore diverso per ogni aggiornamento ricevuto
        Random r = new Random(),g = new Random(), b = new Random();

        contentView.setTextColor(R.id.pallinaAggiornamenti, Color.rgb(r.nextInt(255),g.nextInt(255),b.nextInt(255)));
        contentView.setTextViewText(R.id.distanza,distanza+"Km");
        contentView.setTextViewText(R.id.velocita,velocita+"Km/h");
        contentView.setTextViewText(R.id.massima,massima+"Km/h");
        contentView.setTextViewText(R.id.media,media+"Km/h");
        contentView.setTextViewText(R.id.calorie,calorie+"kcal");

        //mi serve per far aprire il MapsActivity al click della notifica
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id))
                //.setContentTitle("Background")
                //.setContentText("The app will continue to work even in background")
                .setAutoCancel(true)
                .setContent(contentView)
                .setOngoing(true)
                .setChannelId("notificheSympleMaster")
                .setContentIntent(intent);
        builder.setSmallIcon(android.R.color.transparent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if(primaVoltaCronometro) {

            long BasePerIcronometri = SystemClock.elapsedRealtime();

            cronometroInvisibile.setBase(BasePerIcronometri);
            contentView.setChronometer(R.id.chronometerNot, BasePerIcronometri, "\uD83C\uDFC3%s\uD83D\uDCA8", true);
            cronometroInvisibile.start();
            primaVoltaCronometro=false;
        }

        manager.notify(3129999, builder.build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}