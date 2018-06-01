package com.unipi.vnikolis.unipismartalert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * SQLite Database create and make queries
 */

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "unipiDB";
    private static final String TABLE1_NAME = "userCredentials";
    private static final String TABLE2_NAME = "phoneNumbers";

    Database(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE1_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , UserName TEXT, Password TEXT)");
            db.execSQL("INSERT INTO " + TABLE1_NAME + "(UserName, Password) VALUES ('vaggelis', '123')");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE2_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , PhoneNumber TEXT)");
            db.execSQL("INSERT INTO " + TABLE2_NAME + "(PhoneNumber) VALUES ('6976004823')");
            db.execSQL("INSERT INTO " + TABLE2_NAME + "(PhoneNumber) VALUES ('6949861465')");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME);
            onCreate(db);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Έυρεση χρήστη γνωρίζοντας το UserName και το Password
     * @param UserName Όνομα Χρήστη
     * @param Password Κωδικός
     * @return Όνομα χρήστη, Κωδικό χρήστη
     */
    public Cursor getUser(String UserName, String Password)
    {
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            res = db.rawQuery("SELECT * FROM " + TABLE1_NAME + " WHERE UserName = '" + UserName + "' AND Password = '" + Password + "' ", null);

        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Έυρεση τηλεφωνικού καταλόγου παραληπτών
     * @return Λίστα τηλεφώνων
     */
    public ArrayList<String> getPhones() //επιστρέφει μια λιστα με τα τηλέφωνα
    {
        ArrayList<String> phones = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            @SuppressLint("Recycle") Cursor res = db.rawQuery("SELECT * FROM " + TABLE2_NAME, null);

            while (res.moveToNext()) {
                phones.add(res.getString(1));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return phones;
    }
}
