package com.unipi.vnikolis.unipismartalert;

import android.location.Location;

/**
 * Callback interface to receive GPS updates from GPSManager.
 */
public interface GPSCallback
{
    void onGPSUpdate(Location location);
}