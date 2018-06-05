package com.unipi.vnikolis.unipismartalert.model;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Μοντέλο απόδοσης και αποθήκευσης τιμών στις μεταβλητές
 */

public class Values implements Comparable<Values> {

     private String stateEmergency, latitude, longitude, date;

    /**
     * Constructor used for writing values to FireBase
     * @param stateEmergency Κατάσταση ανάγκης
     * @param latitude Γεωγραφικό μήκος
     * @param longitude Γεωγραφικό πλάτος
     */
   public Values(String stateEmergency, String latitude, String longitude)
    {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        this.stateEmergency = stateEmergency;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = dateFormat.format(date);
    }

    /**
     * Empty Constructor we need it
     */
    public Values(){
    }


    public void setStateEmergency(String stateEmergency) {
        this.stateEmergency = stateEmergency;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStateEmergency() {
        return stateEmergency;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDate()
    {
        return date;
    }

    /**
     * Μέθοδος μτατροπής ημερομηνίας. Παίρνει την τρέχουσα ημερομηνία η οποία είναι ανάποδα και
     * @return Επιστρέφει την ημερομηνία στην κανονική της μορφή
     */
    public String CorrectDate(){
        @SuppressLint("SimpleDateFormat") DateFormat dateFromString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") DateFormat dateToString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date dates = null;
        try {
           dates = dateFromString.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToString.format(dates);
    }

    /**
     * Πάρε την τρέχουσα ημερομηνία
     * και μετατροπή στο κατάλληλο φορμάτ
     * @return Ημερομηνία σε Συμβολοσειρά
     */
    private String CurrentDate()
    {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return this.date = dateFormat.format(date);
    }

    /**
     * Συγκρίνει την τρεχουσα ημ/νια με αυτη που εχουμε στη βαση
     * @param o Κλάσση τύπου Values
     * @return Έναν ακέραιο που δηλώνει ότι έγινε η σύγκριση
     */
    //
    @Override
    public int compareTo(@NonNull Values o) {
        return CurrentDate().compareTo(this.date);
    }
}
