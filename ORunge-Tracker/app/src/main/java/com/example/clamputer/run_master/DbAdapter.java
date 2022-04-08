package com.example.clamputer.run_master;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.DateFormat;

public class DbAdapter {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DbAdapter.class.getSimpleName();

    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    /*
    // Database fields
    private static final String DATABASE_TABLE = "contact";

    public static final String KEY_CONTACTID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_SEX = "sex";
    public static final String KEY_BIRTH_DATE = "birth_date";
*/
    public DbAdapter(Context context) {
        this.context = context;
    }

    public DbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }


    /*
    public boolean insertAthlete(String nickname,int eta) {


        ContentValues contentValues = new ContentValues();
        contentValues.put("nickname",nickname);
        contentValues.put("eta",eta);


        long result = database.insert("Athlete",null ,contentValues);

        if(result == -1)
            return false;
        else
            return true;

    }
*/
    public boolean insertProfile(String nome, int age, int peso, String dataCreazione, String creatoDa) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("nome",nome);
        contentValues.put("age",age);
        contentValues.put("peso", peso);
        contentValues.put("dataCreazione",dataCreazione);
        contentValues.put("creatoDa",creatoDa);


        try {
            database.insert("Profile",null ,contentValues);
            return true;
        }
        catch(SQLiteException sqle) {
            Log.i("errore-sql", "errore: " + sqle);
            // Gestione delle eccezioni
            return false;
        }

    }

    public boolean insertWork(String oraInizio,String oraFine,double distanza, double averageSpeed,
                              double maxSpeed, double kcal, String workedBy, String giornoDellaSettimana) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("oraInizio",oraInizio);
        contentValues.put("oraFine",oraFine);
        contentValues.put("distanza",distanza);
        contentValues.put("averageSpeed",averageSpeed);
        contentValues.put("maxSpeed",maxSpeed);
        contentValues.put("kcal",kcal);
        contentValues.put("workedBy",workedBy);
        contentValues.put("giornoDellaSettimana",giornoDellaSettimana);

        long result = database.insert("Work",null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean deleteProfile(String nomeProfilo) {

        try {
            return database.delete("Profile",  "nome = " +"\""+ nomeProfilo+"\"", null) > 0;
        }
        catch(SQLiteException sqle) {
            // Gestione delle eccezioni
            Log.i("errore-sql", "errore: " + sqle);
            return false;
        }
    }

    public Cursor getWork(String nomeProfilo) {

        return database.query("Work", new String[]{"oraInizio","oraFine","distanza","averageSpeed","maxSpeed","workedBy","giornoDellaSettimana"}, null, null, null, null, null);

    }

    public Cursor getWorkFilter(String nomeProfilo) {

        return database.query("Work", new String[]{"oraInizio","oraFine","distanza","averageSpeed","maxSpeed","kcal","workedBy","giornoDellaSettimana"}, "workedBy = \""+nomeProfilo+"\"", null, null, null, null);

    }

    public Cursor getWeightFilter(String nomeProfilo) {

        return database.query("Profile", new String[]{"peso"}, "nome = \""+nomeProfilo+"\"", null, null, null, null);

    }

    public Cursor filterFunzionante(String nomeProfilo) {

        return database.query("Profile", new String[]{"creatoDa"}, "nome = \""+nomeProfilo+"\"", null, null, null, null);

    }

    public boolean updateName(String oldName, String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("nome",newName);
        return database.update("Profile", contentValues, "nome" + "=" + "\""+oldName+"\"", null) > 0;
    }

    public boolean updatePeso(String nomeProfilo, int peso) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("peso",peso);
        return database.update("Profile", contentValues, "nome" + "=" + "\""+nomeProfilo+"\"", null) > 0;
    }



    public Cursor fetchAllProfiles() {
        //rawQuery("SELECT id, name FROM people WHERE name = ? AND id = ?", new String[] {"David", "2"});
        //return database.rawQuery("SELECT Profile.nome FROM Profile WHERE 1",null);

        return database.query("Profile", new String[]{"nome"}, null, null, null, null, null);

    }



    /*
    private ContentValues createContentValues(String name, String surname, String sex, String birth_date) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_SURNAME, surname);
        values.put(KEY_SEX, sex);
        values.put(KEY_BIRTH_DATE, birth_date);

        return values;
    }

    //create a contact
    public long createContact(String name, String surname, String sex, String birth_date) {
        ContentValues initialValues = createContentValues(name, surname, sex, birth_date);
        return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
    }

    //update a contact
    public boolean updateContact(long contactID, String name, String surname, String sex, String birth_date) {
        ContentValues updateValues = createContentValues(name, surname, sex, birth_date);
        return database.update(DATABASE_TABLE, updateValues, KEY_CONTACTID + "=" + contactID, null) > 0;
    }

    //delete a contact
    public boolean deleteContact(long contactID) {
        return database.delete(DATABASE_TABLE, KEY_CONTACTID + "=" + contactID, null) > 0;
    }

    //fetch all contacts
    public Cursor fetchAllContacts() {
        return database.query(DATABASE_TABLE, new String[]{KEY_CONTACTID, KEY_NAME, KEY_SURNAME, KEY_SEX, KEY_BIRTH_DATE}, null, null, null, null, null);
    }

    //fetch contacts filter by a string
    public Cursor fetchContactsByFilter(String filter) {
        Cursor mCursor = database.query(true, DATABASE_TABLE, new String[]{
                        KEY_CONTACTID, KEY_NAME, KEY_SURNAME, KEY_SEX, KEY_BIRTH_DATE},
                KEY_NAME + " like '%" + filter + "%'", null, null, null, null, null);

        return mCursor;
    }
*/
}