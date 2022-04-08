package com.example.clamputer.run_master;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "runmaster2.db";
    private static final int DATABASE_VERSION = 1;

    // Lo statement SQL di creazione del database


    /*
    private static final String create_athlete_table = "CREATE TABLE IF NOT EXISTS "
            + "Athlete" + " ("
            + "nickname" + " text primary key not null, "
            + "eta" + " integer"
            + ");";

    private static final String create_profile_table = "CREATE TABLE IF NOT EXISTS "
            + "Profile" + " ("
            + "nome" + " text primary key not null, "
            + "dataCreazione" + " datetime, "
            + "dataConclusione" + " datetime, "
            + "creatoDa" + " text not null"
            + "FOREIGN KEY (creatoDa) REFERENCES Athlete(nickname));";

    */


    private static final String create_profile_table = "CREATE TABLE IF NOT EXISTS "
            + "Profile" + " ("
            + "nome" + " text primary key not null, "
            + "age" + " integer, "
            + "peso" + " integer, "
            + "dataCreazione" + " datetime, "
            + "creatoDa" + " text not null"
            + ");";

    private static final String create_work_table = "CREATE TABLE IF NOT EXISTS "
            + "Work" + " ("
            + "codWork" + " integer primary key autoincrement, "
            + "oraInizio" + " datetime, "
            + "oraFine" + " datetime, "
            + "distanza" + " integer, "
            + "averageSpeed" + " integer, "
            + "maxSpeed" + " integer, "
            + "kcal" + " integer, "
            + "workedBy" + " text not null, "
            + "giornoDellaSettimana" + " text not null, "
            + "FOREIGN KEY (workedBy) REFERENCES Profile(nome) "
            + "ON DELETE CASCADE ON UPDATE CASCADE);";

    //private static final String DATABASE_CREATE = "create table contact (_id integer primary key autoincrement, name text not null, surname text not null, sex text not null, birth_date text not null);";
    //private static final String create_athlete_table = "create table Athlete (codAthlete integer primary key autoincrement, nickname text not null, eta integer);";


    // Costruttore
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Questo metodo viene chiamato durante la creazione del database
    @Override
    public void onCreate(SQLiteDatabase database) {
        //database.execSQL(create_athlete_table);
        database.execSQL(create_profile_table);
        database.execSQL(create_work_table);

    }

    // Questo metodo viene chiamato durante l'upgrade del database, ad esempio quando viene incrementato il numero di versione
    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {

        //database.execSQL("DROP TABLE IF EXISTS Athlete");
        database.execSQL("DROP TABLE IF EXISTS Profile");
        database.execSQL("DROP TABLE IF EXISTS Work");


        onCreate(database);

    }

    /*
    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }*/


    @Override
    public void onConfigure(SQLiteDatabase database) {
        database.setForeignKeyConstraintsEnabled(true);
    }


}
