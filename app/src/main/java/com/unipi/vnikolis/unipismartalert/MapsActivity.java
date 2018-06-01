package com.unipi.vnikolis.unipismartalert;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * The backend code for Maps Activity
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dropDanger, possiblyDanger;
    MainActivity mainActivity = new MainActivity();
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        possiblyDanger = firebaseDatabase.getReference("PossiblyDanger");
        dropDanger = possiblyDanger.child("DropDanger");
    }

    /**
     * Κλείνοντας την εφαρμογή εαν υπάρχει νήμα να σταματήσει την λειτουργία του
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(t != null) {
            t.interrupt();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(MainActivity.isMapsButtonPressed) { //εαν πατηθεί το κουμπί "Maps"
            if (CheckInternetConnection.isConnected(MapsActivity.this) && CheckInternetConnection.isConnectedFast(MapsActivity.this)) { //ελεγχος εαν υπάρχει σύνδεση Internet
                t =  new Thread(new Runnable() {
                    @Override
                    public void run() {
                        putTheMarkers(dropDanger);
                    }
                });
                t.start();
            }else{
                Toast.makeText(MapsActivity.this, "Δεν υπάρχει σύνδεση στο Internet, προσπάθησε ξανά", Toast.LENGTH_LONG).show();
            }
        }else if(ShowStatistics.isItemsButtonClicked){
            if (CheckInternetConnection.isConnected(MapsActivity.this) && CheckInternetConnection.isConnectedFast(MapsActivity.this)) { //ελεγχος εαν υπάρχει σύνδεση Internet
                putTheMarkersFromList();
            }else{
                Toast.makeText(MapsActivity.this, "Δεν υπάρχει σύνδεση στο Internet, προσπάθησε ξανά", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Τοποθετεί όλα τα σημάδια της
     * κατηγορίας BigDanger επάνω στον χάρτη
     */
    public void putTheMarkers(DatabaseReference reference){
        reference.addValueEventListener(new ValueEventListener() {
            Values v;
            LatLng coordinates;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) //εαν υπάρχει κάτι σε αυτον τον πίνακα
                    {
                        for (DataSnapshot i : dataSnapshot.getChildren()) {
                            v = i.getValue(Values.class);
                            assert v != null;
                            coordinates = new LatLng(Double.parseDouble(v.getLatitude()), Double.parseDouble(v.getLongitude()));
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            markerOptions.title(mainActivity.findAddress(MapsActivity.this, coordinates));
                            markerOptions.position(coordinates);
                            markerOptions.snippet(v.getDate());
                            mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 2));
                            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(coordinates, 10)));
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("SOS", "Something went wrong");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Παίρνει κάθε γραμμή της ViewList και εμφανίζει
     * το σημάδι επάνω στον χάρτη
     */
    public void putTheMarkersFromList(){
        try {
            String latitude = Objects.requireNonNull(getIntent().getExtras()).getString("latitude");
            String longitude = getIntent().getExtras().getString("longitude");
            String date = getIntent().getExtras().getString("date");
            double latitudeToDouble = Double.parseDouble(latitude);
            double longitudeToDouble = Double.parseDouble(longitude);
            LatLng coordinates = new LatLng(latitudeToDouble, longitudeToDouble);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            markerOptions.title(mainActivity.findAddress(MapsActivity.this, coordinates ));
            markerOptions.position(coordinates);
            markerOptions.snippet(date);
            mMap.addMarker(markerOptions);
            //εστίαση στο συγκεκριμένο σημείο του χάρτη
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 2));
            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(coordinates, 15)));
        }catch (Exception e){
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }
}
