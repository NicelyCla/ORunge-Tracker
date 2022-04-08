package com.example.clamputer.run_master;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class playSession extends AppCompatActivity {

    //elementi grafici
    private Button back;
    private Button goMaps;
    private Button changeNameProfile;
    private Button changeWeight;
    private Button viewHistory;
    private AutoCompleteTextView currentProfile;
    private AutoCompleteTextView currentWeight;
    private String nomeProfilo;
    private TextView lunediOreMinuti;
    private TextView martediOreMinuti;
    private TextView mercolediOreMinuti;
    private TextView giovediOreMinuti;
    private TextView venerdiOreMinuti;
    private TextView sabatoOreMinuti;
    private TextView domenicaOreMinuti;
    private TextView maxMaximumSpeedTextView;
    private GraphView graph,graph2;


    StringBuffer stringBuffer = new StringBuffer();

    private Date dataInizio,dataFine,dataPrimoAllenamento;

    private long lunediOreMinutiTotali[] = new long[4];
    private long martediOreMinutiTotali[] = new long[4];
    private long mercolediOreMinutiTotali[]= new long[4];
    private long giovediOreMinutiTotali[]= new long[4];
    private long venerdiOreMinutiTotali[]= new long[4];
    private long sabatoOreMinutiTotali[]= new long[4];
    private long domenicaOreMinutiTotali[]= new long[4];
    private long maxTimeWorked[] = new long[7];

    private static ArrayList<Double> y_axis=new ArrayList<Double>();
    private static ArrayList<Date> x_axis=new ArrayList<Date>();
    private static ArrayList<Double> y2_axis=new ArrayList<Double>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd\nHH:mm:ss");

    //variabili per database
    private DbAdapter dbHelper;
    private Cursor works;
    private Cursor weight;

    private boolean pressedTextView;
    private boolean pressedTextView2;
    private double maxMaximumSpeed;//tra tutte le velocità massima, la maggiore
    private int indiceGiorno;
    private int peso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_session);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(3129999);

        boolean primoAllenamento = false;
        maxMaximumSpeed=0.0;

        dataPrimoAllenamento = new Date();//inizializzo
        dataInizio = new Date();

        //inizializzo gli array

        for(int x = 0;x<4;x++){
            lunediOreMinutiTotali[x]=0;
            martediOreMinutiTotali[x]=0;
            mercolediOreMinutiTotali[x]=0;
            giovediOreMinutiTotali[x]=0;
            venerdiOreMinutiTotali[x]=0;
            sabatoOreMinutiTotali[x]=0;
            domenicaOreMinutiTotali[x]=0;
        }

        for(int x=0;x<7;x++){
            maxTimeWorked[x]=0;
        }




        pressedTextView = false;
        pressedTextView2=false;
        //creo e apro il database
        dbHelper = new DbAdapter(this);
        dbHelper.open();

        nomeProfilo = getIntent().getStringExtra("PROFILO");//ricevo il nome profilo da saved_profiles


        currentProfile = (AutoCompleteTextView) findViewById(R.id.profiloCorrente);
        currentWeight = (AutoCompleteTextView) findViewById(R.id.pesoCorrente);

        back = (Button) findViewById(R.id.back);
        goMaps = (Button) findViewById(R.id.buttonSTART);
        changeNameProfile =(Button) findViewById(R.id.modifica);
        changeWeight = (Button) findViewById(R.id.modificaPeso);
        viewHistory = (Button) findViewById(R.id.viewHistory);

        //proporziono il bottone buttonSTART
        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            if(width==1440) {
                params = goMaps.getLayoutParams();
                params.width = (int) (800 * width) / 1440;
                goMaps.setLayoutParams(params);
            }
            else if(width==720){
                params = goMaps.getLayoutParams();
                params.width = (int) (900 * width) / 1440;
                goMaps.setLayoutParams(params);
            }

        }
        else{
            ViewGroup.LayoutParams params;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            if(height==1440) {
                params = goMaps.getLayoutParams();
                params.width = (int) (800 * height) / 1440;
                goMaps.setLayoutParams(params);
            }
            else if(height==720){
                params = goMaps.getLayoutParams();
                params.width = (int) (900 * height) / 1440;
                goMaps.setLayoutParams(params);
            }
        }

        //stats
        lunediOreMinuti = (TextView) findViewById(R.id.lunediOreMinuti);
        martediOreMinuti = (TextView) findViewById(R.id.martediOreMinuti);
        mercolediOreMinuti = (TextView) findViewById(R.id.mercolediOreMinuti);
        giovediOreMinuti = (TextView) findViewById(R.id.giovediOreMinuti);
        venerdiOreMinuti = (TextView) findViewById(R.id.venerdiOreMinuti);
        sabatoOreMinuti = (TextView) findViewById(R.id.sabatoOreMinuti);
        domenicaOreMinuti = (TextView) findViewById(R.id.domenicaOreMinuti);

        maxMaximumSpeedTextView = (TextView) findViewById(R.id.maxMaximumSpeed);

        viewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder message = new AlertDialog.Builder(playSession.this,R.style.AlertDialogStyle);

                message.setMessage(stringBuffer);
                message.setCancelable(true);
                message.setTitle("History of "+nomeProfilo);
                message.show();

            }
        });



        weight = dbHelper.getWeightFilter(nomeProfilo);
        while (weight.moveToNext()) {
            int index;
            index = weight.getColumnIndexOrThrow("peso");
            if (weight!=null)
                peso = Integer.parseInt(weight.getString(index));
        }

        works = dbHelper.getWorkFilter(nomeProfilo);
        int numberWork = 1;
        while (works.moveToNext()) {
            int index;

            stringBuffer.append("Session: "+numberWork+"\n");
            numberWork++;
            index = works.getColumnIndexOrThrow("oraInizio");
            String oraInizio = works.getString(index);
            stringBuffer.append("Started: "+oraInizio+"\n");

            index = works.getColumnIndexOrThrow("oraFine");
            String oraFine = works.getString(index);
            stringBuffer.append("Finished: " + oraFine+"\n");

            index = works.getColumnIndexOrThrow("distanza");
            String distanza = works.getString(index);
            stringBuffer.append("Distance traveled: "+distanza+"Km\n");

            index = works.getColumnIndexOrThrow("averageSpeed");
            String averageSpeed = works.getString(index);
            stringBuffer.append("Average speed: "+averageSpeed+"Km/h\n");

            index = works.getColumnIndexOrThrow("maxSpeed");
            String maxSpeed = works.getString(index);
            stringBuffer.append("Maxim speed: "+maxSpeed+"Km/h\n");

            index = works.getColumnIndexOrThrow("kcal");
            String kcal = works.getString(index);
            stringBuffer.append("Calories: "+maxSpeed+"kcal\n");

            /*
            index = works.getColumnIndexOrThrow("workedBy");
            String workedBy = works.getString(index);
            stringBuffer.append("Profile: "+workedBy+"\n");
            */

            index = works.getColumnIndexOrThrow("giornoDellaSettimana");
            String giornoDellaSettimana = works.getString(index);

            stringBuffer.append("\n");

            //trovo la velocità maggiore fra tutte le massime
            double maxSpeedDouble = Double.parseDouble(maxSpeed);
            if(maxSpeedDouble>maxMaximumSpeed){
                maxMaximumSpeed=maxSpeedDouble;
            }

            try {

                if(!primoAllenamento) {
                    //trovo la data del primo allenamento
                    dataPrimoAllenamento=dateFormat.parse(oraInizio);
                    primoAllenamento=true;
                }
                dataInizio = dateFormat.parse(oraInizio);//alla fine del ciclo, questa sarà l'ultima data inizio, e verrà messa come ultimo punto ndel grafico
                dataFine = dateFormat.parse(oraFine);


            } catch (ParseException e) {
                e.printStackTrace();
            }
            setterTextView(dataInizio, dataFine, giornoDellaSettimana);
            x_axis.add(dataInizio);
            y_axis.add(getMinutesWork(dataInizio,dataFine));
            //faccio un approssimazione per il grafico
            int averageSpeedApprox = (int) Double.parseDouble(averageSpeed);
            y2_axis.add((double)averageSpeedApprox);

        }

        maxMaximumSpeedTextView.setText("Your maximum speed reached is "+String.format("%.2f",maxMaximumSpeed)+" Km/h");

        String lunediNumberBold = "<b>"+lunediOreMinutiTotali[0]+"</b>" + "d " + "<b>"+lunediOreMinutiTotali[1]+"</b>" + "h " +"<b>"+ lunediOreMinutiTotali[2]+"</b>" + "m " + "<b>"+lunediOreMinutiTotali[3]+"</b>" + "s";
        String martediNumberBold = "<b>"+ martediOreMinutiTotali[0]+"</b>" + "d " +"<b>"+martediOreMinutiTotali[1]+"</b>" + "h "+"<b>" + martediOreMinutiTotali[2]+"</b>" + "m " + "<b>"+martediOreMinutiTotali[3]+"</b>" + "s";
        String mercolediNumberBold = "<b>"+mercolediOreMinutiTotali[0]+"</b>" + "d " + "<b>"+mercolediOreMinutiTotali[1]+"</b>" + "h " +"<b>" +mercolediOreMinutiTotali[2]+"</b>" + "m " + "<b>" + mercolediOreMinutiTotali[3]+"</b>" + "s";
        String giovediNumberBold = "<b>"+giovediOreMinutiTotali[0]+"</b>" + "d " +"<b>"+giovediOreMinutiTotali[1]+"</b>" + "h " + "<b>"+giovediOreMinutiTotali[2]+"</b>" + "m " +"<b>"+giovediOreMinutiTotali[3]+"</b>" + "s";
        String venerdiNumberBold = "<b>"+venerdiOreMinutiTotali[0] +"</b>"+ "d " +"<b>"+venerdiOreMinutiTotali[1]+"</b>" + "h " +"<b>"+venerdiOreMinutiTotali[2]+"</b>" + "m " + "<b>"+venerdiOreMinutiTotali[3]+"</b>" + "s";
        String sabatoNumberBold = "<b>"+ sabatoOreMinutiTotali[0]+"</b>" + "d " +"<b>" +sabatoOreMinutiTotali[1]+"</b>" + "h " +"<b>"+sabatoOreMinutiTotali[2]+"</b>" + "m " +"<b>" +sabatoOreMinutiTotali[3] +"</b>"+ "s";
        String domenicaNumberBold = "<b>"+domenicaOreMinutiTotali[0]+"</b>" + "d " +"<b>"+domenicaOreMinutiTotali[1]+"</b>" + "h " +"<b>"+domenicaOreMinutiTotali[2]+"</b>" + "m " +"<b>"+ domenicaOreMinutiTotali[3]+"</b>" + "s";

        lunediOreMinuti.setText(Html.fromHtml(lunediNumberBold, Html.FROM_HTML_MODE_COMPACT));
        martediOreMinuti.setText(Html.fromHtml(martediNumberBold, Html.FROM_HTML_MODE_COMPACT));
        mercolediOreMinuti.setText(Html.fromHtml(mercolediNumberBold, Html.FROM_HTML_MODE_COMPACT));
        giovediOreMinuti.setText(Html.fromHtml(giovediNumberBold, Html.FROM_HTML_MODE_COMPACT));
        venerdiOreMinuti.setText(Html.fromHtml(venerdiNumberBold, Html.FROM_HTML_MODE_COMPACT));
        sabatoOreMinuti.setText(Html.fromHtml(sabatoNumberBold, Html.FROM_HTML_MODE_COMPACT));
        domenicaOreMinuti.setText(Html.fromHtml(domenicaNumberBold, Html.FROM_HTML_MODE_COMPACT));

        //coloro il giorno in cui ho lavorato di più
        long temp=0;
        indiceGiorno=0;
        for(int x=0;x<7;x++){
            if(maxTimeWorked[x]>temp){
                temp = maxTimeWorked[x];
                indiceGiorno=x;

            }

        }

        if(temp != 0) {
            switch (indiceGiorno) {
                case 0:
                    lunediOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    lunediOreMinuti.setTextSize(15.5f);
                    break;

                case 1:
                    martediOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    martediOreMinuti.setTextSize(15.5f);
                    break;

                case 2:
                    mercolediOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    mercolediOreMinuti.setTextSize(15.5f);
                    break;

                case 3:
                    giovediOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    giovediOreMinuti.setTextSize(15.5f);
                    break;

                case 4:
                    venerdiOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    venerdiOreMinuti.setTextSize(15.5f);
                    break;

                case 5:
                    sabatoOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    sabatoOreMinuti.setTextSize(15.5f);
                    break;

                case 6:
                    domenicaOreMinuti.setTextColor(Color.argb(255, 255, 255, 0));
                    domenicaOreMinuti.setTextSize(15.5f);
                    break;


            }
        }


        currentProfile.setHint(nomeProfilo);
        currentProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!pressedTextView) {
                    currentProfile.setText(currentProfile.getHint());
                    currentProfile.setHint("");
                    changeNameProfile.setEnabled(true);
                    pressedTextView = true;
                }
            }
        });

        currentWeight.setHint(String.valueOf(peso));
        currentWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!pressedTextView2) {
                    currentWeight.setText(currentWeight.getHint());
                    currentWeight.setHint("");
                    changeWeight.setEnabled(true);
                    pressedTextView2 = true;
                }
            }
        });


        changeNameProfile.setEnabled(false);
        changeWeight.setEnabled(false);

        changeNameProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentProfile.getText().toString().equals("")){
                    currentProfile.setHint(nomeProfilo);
                    pressedTextView = false;
                    Toast.makeText(getApplicationContext(), "Do not leave the field empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(currentProfile.getText().toString().contains("'") || currentProfile.getText().toString().contains("\"")){
                    Toast.makeText(getApplicationContext(), "You have used characters that are not allowed.", Toast.LENGTH_LONG).show();
                    return;
                }
                //controlla che il campo non sia uguale all'hint precedente
                else if(!nomeProfilo.equals(currentProfile.getText().toString())){
                    dbHelper.updateName(nomeProfilo,currentProfile.getText().toString());
                }
                finish();
                startActivity(getIntent().putExtra("PROFILO",currentProfile.getText().toString()));
            }
        });

        changeWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentWeight.getText().toString().equals("")){
                    currentWeight.setHint(String.valueOf(peso));
                    pressedTextView2 = false;
                    Toast.makeText(getApplicationContext(), "Do not leave the field empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                //controlla che il campo non sia uguale all'hint precedente
                else if(peso!=Integer.parseInt(currentWeight.getText().toString())){
                    dbHelper.updatePeso(nomeProfilo,Integer.parseInt(currentWeight.getText().toString()));
                }
                finish();
                startActivity(getIntent().putExtra("PROFILO",nomeProfilo));
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(playSession.this, saved_profiles.class);
                startActivity(intent);
                finish();
            }
        });

        goMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(playSession.this, MapsActivity.class);
                intent.putExtra("PROFILO",nomeProfilo);
                intent.putExtra("PESO",String.valueOf(peso));
                startActivity(intent);
                finish();
            }
        });

        //Grafico 1
        graph = (GraphView) findViewById(R.id.graph);
        graph.setBackgroundColor(Color.argb(255, 255, 255, 255));

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(playSession.this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph.getGridLabelRenderer().setNumVerticalLabels(6);

        graph.getViewport().setMinX(dataPrimoAllenamento.getTime());//cambia con la prima data letta nel database
        graph.getViewport().setMaxX(dataInizio.getTime());//cambia con l'ultima data letta nel database
        graph.getViewport().setMinY(0.0);

        double maxTemp = 1;
        if(y_axis.size()>0) {
            maxTemp = Collections.max(y_axis);
        }

        graph.getViewport().setMaxY(maxTemp);//cambia con il massimo allenamento

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setPadding(50);
        graph.getGridLabelRenderer().setTextSize(30f);
        graph.getGridLabelRenderer().reloadStyles();

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX){
                    return sdf.format(new Date((long)value));
                }
                else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });


        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data());
        graph.addSeries(series);

        //grafico 2
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph2.setBackgroundColor(Color.argb(255, 255, 255, 255));

        graph2.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(playSession.this));
        graph2.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph2.getGridLabelRenderer().setNumVerticalLabels(6);

        graph2.getViewport().setMinX(dataPrimoAllenamento.getTime());//cambia con la prima data letta nel database
        graph2.getViewport().setMaxX(dataInizio.getTime());//cambia con l'ultima data letta nel database
        graph2.getViewport().setMinY(0.0);

        double maxAverageSpeed = 1;
        if(y2_axis.size()>0) {
            maxAverageSpeed = Collections.max(y2_axis);
        }

        graph2.getViewport().setMaxY((int)maxAverageSpeed);//cambia con il massimo allenamento

        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getGridLabelRenderer().setHumanRounding(false);
        graph2.getGridLabelRenderer().setPadding(50);
        graph2.getGridLabelRenderer().setTextSize(30f);
        graph2.getGridLabelRenderer().reloadStyles();


        graph2.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX){
                    return sdf.format(new Date((long)value));
                }
                else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(data2());
        graph2.addSeries(series2);


    }

    public void setterTextView(Date startDate, Date endDate, String giornoDellaSettimana) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        long different2 = different;

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        switch (giornoDellaSettimana){
            case "monday":
                lunediOreMinutiTotali[0]+=elapsedDays;
                lunediOreMinutiTotali[1]+=elapsedHours;
                lunediOreMinutiTotali[2]+=elapsedMinutes;
                lunediOreMinutiTotali[3]+=elapsedSeconds;
                if(lunediOreMinutiTotali[3]>=60){
                    lunediOreMinutiTotali[3]-=60;
                    lunediOreMinutiTotali[2]+=1;
                }
                if(lunediOreMinutiTotali[2]>=60){
                    lunediOreMinutiTotali[2]-=60;
                    lunediOreMinutiTotali[1]+=1;
                }
                if(lunediOreMinutiTotali[1]>=24){
                    lunediOreMinutiTotali[1]-=24;
                    lunediOreMinutiTotali[0]+=1;
                }
                maxTimeWorked[0]+=different2;
                break;
            case "tuesday":
                martediOreMinutiTotali[0]+=elapsedDays;
                martediOreMinutiTotali[1]+=elapsedHours;
                martediOreMinutiTotali[2]+=elapsedMinutes;
                martediOreMinutiTotali[3]+=elapsedSeconds;
                if(martediOreMinutiTotali[3]>=60){
                    martediOreMinutiTotali[3]-=60;
                    martediOreMinutiTotali[2]+=1;
                }
                if(martediOreMinutiTotali[2]>=60){
                    martediOreMinutiTotali[2]-=60;
                    martediOreMinutiTotali[1]+=1;
                }
                if(martediOreMinutiTotali[1]>=24){
                    martediOreMinutiTotali[1]-=24;
                    martediOreMinutiTotali[0]+=1;
                }

                maxTimeWorked[1]+=different2;

                break;
            case "wednesday":
                mercolediOreMinutiTotali[0]+=elapsedDays;
                mercolediOreMinutiTotali[1]+=elapsedHours;
                mercolediOreMinutiTotali[2]+=elapsedMinutes;
                mercolediOreMinutiTotali[3]+=elapsedSeconds;
                if(mercolediOreMinutiTotali[3]>=60){
                    mercolediOreMinutiTotali[3]-=60;
                    mercolediOreMinutiTotali[2]+=1;
                }
                if(mercolediOreMinutiTotali[2]>=60){
                    mercolediOreMinutiTotali[2]-=60;
                    mercolediOreMinutiTotali[1]+=1;
                }
                if(mercolediOreMinutiTotali[1]>=24){
                    mercolediOreMinutiTotali[1]-=24;
                    mercolediOreMinutiTotali[0]+=1;
                }
                Log.i("mercoledi", String.valueOf(different));
                maxTimeWorked[2]+=different2;
                break;
            case "thursday":
                giovediOreMinutiTotali[0]+=elapsedDays;
                giovediOreMinutiTotali[1]+=elapsedHours;
                giovediOreMinutiTotali[2]+=elapsedMinutes;
                giovediOreMinutiTotali[3]+=elapsedSeconds;
                if(giovediOreMinutiTotali[3]>=60){
                    giovediOreMinutiTotali[3]-=60;
                    giovediOreMinutiTotali[2]+=1;
                }
                if(giovediOreMinutiTotali[2]>=60){
                    giovediOreMinutiTotali[2]-=60;
                    giovediOreMinutiTotali[1]+=1;
                }
                if(giovediOreMinutiTotali[1]>=24){
                    giovediOreMinutiTotali[1]-=24;
                    giovediOreMinutiTotali[0]+=1;
                }
                maxTimeWorked[3]+=different2;
                break;
            case "friday":
                venerdiOreMinutiTotali[0]+=elapsedDays;
                venerdiOreMinutiTotali[1]+=elapsedHours;
                venerdiOreMinutiTotali[2]+=elapsedMinutes;
                venerdiOreMinutiTotali[3]+=elapsedSeconds;
                if(venerdiOreMinutiTotali[3]>=60){
                    venerdiOreMinutiTotali[3]-=60;
                    venerdiOreMinutiTotali[2]+=1;
                }
                if(venerdiOreMinutiTotali[2]>=60){
                    venerdiOreMinutiTotali[2]-=60;
                    venerdiOreMinutiTotali[1]+=1;
                }
                if(venerdiOreMinutiTotali[1]>=24){
                    venerdiOreMinutiTotali[1]-=24;
                    venerdiOreMinutiTotali[0]+=1;
                }
                maxTimeWorked[4]+=different2;
                break;
            case "saturday":
                sabatoOreMinutiTotali[0]+=elapsedDays;
                sabatoOreMinutiTotali[1]+=elapsedHours;
                sabatoOreMinutiTotali[2]+=elapsedMinutes;
                sabatoOreMinutiTotali[3]+=elapsedSeconds;
                if(sabatoOreMinutiTotali[3]>=60){
                    sabatoOreMinutiTotali[3]-=60;
                    sabatoOreMinutiTotali[2]+=1;
                }
                if(sabatoOreMinutiTotali[2]>=60){
                    sabatoOreMinutiTotali[2]-=60;
                    sabatoOreMinutiTotali[1]+=1;
                }
                if(sabatoOreMinutiTotali[1]>=24){
                    sabatoOreMinutiTotali[1]-=24;
                    sabatoOreMinutiTotali[0]+=1;
                }
                maxTimeWorked[5]+=different2;
                break;
            case "sunday":
                domenicaOreMinutiTotali[0]+=elapsedDays;
                domenicaOreMinutiTotali[1]+=elapsedHours;
                domenicaOreMinutiTotali[2]+=elapsedMinutes;
                domenicaOreMinutiTotali[3]+=elapsedSeconds;
                if(domenicaOreMinutiTotali[3]>=60){
                    domenicaOreMinutiTotali[3]-=60;
                    domenicaOreMinutiTotali[2]+=1;
                }
                if(domenicaOreMinutiTotali[2]>=60){
                    domenicaOreMinutiTotali[2]-=60;
                    domenicaOreMinutiTotali[1]+=1;
                }
                if(domenicaOreMinutiTotali[1]>=24){
                    domenicaOreMinutiTotali[1]-=24;
                    domenicaOreMinutiTotali[0]+=1;
                }
                maxTimeWorked[6]+=different2;

                break;
        }

        /*
        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
                */
    }
    public double getMinutesWork(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();


        double differentMinutes= different*0.0000166667;
        return differentMinutes;

    }

    //crea il grafico1 dall'array
    public DataPoint[] data(){
        int n=x_axis.size();
        DataPoint[] values = new DataPoint[n];
        for(int x = 0;x<n;x++){
            DataPoint v = new DataPoint(x_axis.get(x),y_axis.get(x));
            values[x]=v;
        }

        return values;
    }
    //crea il grafico 2 dall'array
    public DataPoint[] data2(){
        int n=x_axis.size();
        DataPoint[] values = new DataPoint[n];
        graph2.removeAllSeries();

        for(int x = 0;x<n;x++){
            DataPoint v = new DataPoint(x_axis.get(x),y2_axis.get(x));

            values[x]=v;
        }
        //importante pulire gli array dopo aver generato i grafici, altrimenti potrei avere problemi nelle successive generazione del form
        x_axis.clear();
        y_axis.clear();
        y2_axis.clear();
        return values;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
