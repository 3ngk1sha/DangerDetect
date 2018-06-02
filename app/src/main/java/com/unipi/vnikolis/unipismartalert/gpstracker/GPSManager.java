package com.unipi.vnikolis.unipismartalert.gpstracker;

import java.util.List;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Check device's GPS settings and selecting the best provider
 *
 * gpsManager = new GPSManager(MainActivity.this);
   gpsManager.startListening(MainActivity.this);
   gpsManager.setGPSCallback(this); ------> χρησιμοποιούμε αυτές τις εντολές στην onCreate()
 * για να χρησιμοποιήσουμε την κλάσση

 * gpsManager.startListening(MainActivity.this);
   gpsManager.setGPSCallback(this); ------> και αυτές για τα υπόλοιπα
 *
 */
public class GPSManager implements android.location.GpsStatus.Listener
{
    private static final int gpsMinTime = 500;
    private static final int gpsMinDistance = 0;
    private boolean isGPSEnabled, isNetworkEnabled = false;
    private static LocationManager locationManager = null;
    private static LocationListener locationListener = null;
    private static GPSCallback gpsCallback = null;
    private Context mContext;
    //Constructor
    public GPSManager(Context context) {
        this.mContext=context;
        GPSManager.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                if (GPSManager.gpsCallback != null) {
                    GPSManager.gpsCallback.onGPSUpdate(location);
                }
            }

            @Override
            public void onProviderDisabled(final String provider) {
            }

            @Override
            public void onProviderEnabled(final String provider) {
            }

            @Override
            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }
        };
    }

    /**
     * Μεταβαση στο μενου για χειροκίνητο ανοιγμα του GPS από τον χρήστη
     */
    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to enable it?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * την χρησιμοποιούμε για να πάρουμε την απάντηση του Listener
     * @param gpsCallback Παίρνουμε τα GPS Updates από τον GPSListener
     */
    public void setGPSCallback(final GPSCallback gpsCallback) {
        GPSManager.gpsCallback = gpsCallback;
    }

    /**
     * Αυτόματη επιλογή του κατάλληλου Provider
     * κατάλληλη επιλογή όταν θέλουμε να πάρουμε την τρέχουσα ταχύτητα
     * Την καλούμε όταν ξεκινάει η εφαρμογή μας ή όταν επανέρχεται από το παρασκήνιο
     */
    public void startListening(final Context context) {
        if (GPSManager.locationManager == null) {
            GPSManager.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            showSettingsAlert();
        }

        final Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String bestProvider = GPSManager.locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        if (bestProvider != null && bestProvider.length() > 0) {
            GPSManager.locationManager.requestLocationUpdates(bestProvider, GPSManager.gpsMinTime,
                    GPSManager.gpsMinDistance, GPSManager.locationListener);
        }
        else {
            final List<String> providers = GPSManager.locationManager.getProviders(true);
            for (final String provider : providers)
            {
                GPSManager.locationManager.requestLocationUpdates(provider, GPSManager.gpsMinTime,
                        GPSManager.gpsMinDistance, GPSManager.locationListener);
            }
        }
    }

    /**
     * Παίρνει την τρέχουσα τοποθεσία είτε μέσω Network είτε μέσω GPS
     * για κάθε περίπτωση εκτος του υπολογισμού της ταχύτητας
     * η οποία προσδιορίζεται διαφορετικά
     */
    public Location startListeningForSOS(final Context context){
        Location location = null;
        if (GPSManager.locationManager == null) {
            GPSManager.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            showSettingsAlert();
        }else {
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPSManager.gpsMinTime,
                        GPSManager.gpsMinDistance, GPSManager.locationListener);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,GPSManager.gpsMinTime,
                            GPSManager.gpsMinDistance, GPSManager.locationListener);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        }if (location == null) {
            Toast.makeText(context, "I can't find your location, please try again", Toast.LENGTH_LONG).show();
        }
        return location;
    }

    /**
     * Όταν η εφαρμογή φύγει από το MainActivity π.χ. πάει στο παρασκήνιο
     */
    public void stopListening() {
        try
        {
            if (GPSManager.locationManager != null && GPSManager.locationListener != null) {
                GPSManager.locationManager.removeUpdates(GPSManager.locationListener);
            }
            GPSManager.locationManager = null;
        }
        catch (final Exception ex) {
            ex.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }

    public void onGpsStatusChanged(int event) {
        int Satellites = 0;
        int SatellitesInFix = 0;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        int timeToFix = locationManager.getGpsStatus(null).getTimeToFirstFix();
        Log.i("GPs", "Time to first fix = "+String.valueOf(timeToFix));
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if(sat.usedInFix()) {
                SatellitesInFix++;
            }
            Satellites++;
        }
        Log.i("GPS", String.valueOf(Satellites) + " Used In Last Fix ("+SatellitesInFix+")");
    }
}