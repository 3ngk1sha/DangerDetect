package com.unipi.vnikolis.unipismartalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unipi.vnikolis.unipismartalert.gpstracker.GPSCallback;
import com.unipi.vnikolis.unipismartalert.gpstracker.GPSManager;
import com.unipi.vnikolis.unipismartalert.internettracker.CheckInternetConnection;
import com.unipi.vnikolis.unipismartalert.model.Values;
import com.unipi.vnikolis.unipismartalert.mytts.MyTTS;
import com.unipi.vnikolis.unipismartalert.sms.SendSMS;
import com.unipi.vnikolis.unipismartalert.sqllite.Database;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The backend code for Main Activity
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, GPSCallback, ComponentCallbacks2 {

    Button abort, sos, goToStatistics, maps ;
    SensorManager sensorManager;
    Sensor accelerometer, lightSensor;
    LocationManager locationManager;
    MyTTS myTTS;
    Database myDb;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference possiblyDanger, bigDanger, lightDanger, speedDanger, dropDanger;

    private GPSManager gpsManager = null;

    private static final int ACCESS_FINE_LOCATION_CODE = 543;
    private static final int SEND_SMS_CODE = 123;
    private static final int VOICE_REC_RESULT = 6537;

    public static double latitudeForSpeed, longitudeForSpeed, latitude, longitude;

    private static final int MAX_ACCELERATION = 50; //----------------------------> ειναι 50
    private static final int MAX_SPEED = 80; //----------------------------> ειναι 80
    private static final int MAX_LIGHT = 1400; //----------------------------> ειναι 1400

    private MediaPlayer mp;

    int accelerationBlocker, lightBlocker, speedBlocker;

    CountDownTimer countDownTimerAcceleration = null, countDownTimerLight = null, countDownTimerSpeed = null;
    boolean isSOSPressed;
    static boolean isMapsButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //-------> screen always on

        myTTS = new MyTTS(this); //instance για την ομιλία
        myDb = new Database(this); //instance για την βάση δεδομένων SQLite

        //για την FireBase
        firebaseDatabase = FirebaseDatabase.getInstance();
        possiblyDanger = firebaseDatabase.getReference("PossiblyDanger");
        bigDanger = firebaseDatabase.getReference("BigDanger");
        lightDanger = possiblyDanger.child("LightDanger");
        speedDanger = possiblyDanger.child("SpeedDanger");
        dropDanger = possiblyDanger.child("DropDanger");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); //προσβαση στον Sensor Manager γενικά
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  //επιλογή sensor επιταχυνσης
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);  //επιλογή sensor φωτός
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //επιλογή Τοποθεσίας

        abort = findViewById(R.id.abort);
        sos = findViewById(R.id.sos);
        goToStatistics = findViewById(R.id.gotostatistics);
        maps = findViewById(R.id.maps);

        try {
            //έλεγχος του permission απο τον χρήστη για το SMS
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_CODE);
            }
            //έλεγχος του permission απο τον χρήστη για την τοποθεσία
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }

        try {
            //Άνοιγμα και έλεγχος του GPS
            gpsManager = new GPSManager(this);
            gpsManager.startListening(this);
            gpsManager.setGPSCallback(this);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }

        // εκίνηση του ACCELEROMETER
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // εκίνηση LightSensor
        sensorManager.registerListener(MainActivity.this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //επιλογή του ήχου
        mp = MediaPlayer.create(getBaseContext(), (R.raw.tic));
    }

    /**
     * Αποθήκευση του μηνύματος από την αναγνώριση
     * φωνής στην FireBase μαζί με τις συντεταγμένες
     * και την τρέχουσα ημερομηνία
     * @param requestCode κωδικός
     * @param resultCode αποτελέσματα
     * @param data δεδομένα
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            String message = null;
            if (requestCode == VOICE_REC_RESULT && resultCode == RESULT_OK) {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                for (String i : matches) {
                    message = i;
                }
                //έλεγχος εαν υπάρχει συνδεδεμένο wifi, εαν υπάρχει τότε να γίνει επιπλέον έλεγχος για επιλογή του provider γιατι μπορεί να μην πιάνει στίγμα το GPS εσωτερικά
                if(CheckInternetConnection.isConnectedWifi(this)) {
                    Location location = gpsManager.startListeningForSOS(this);
                    gpsManager.setGPSCallback(this);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                if (latitude != 0.0 && longitude != 0.0) {
                    if (CheckInternetConnection.isConnected(MainActivity.this) && CheckInternetConnection.isConnectedFast(MainActivity.this)) {
                        Values save = new Values(message, String.valueOf(latitude), String.valueOf(longitude));
                        bigDanger.push().setValue(save);
                        Toast.makeText(MainActivity.this, "Το μηνυμά σας αποθηκεύτηκε επιτυχώς", Toast.LENGTH_LONG).show();
                    } else {
                        notificationIfNoInternet();
                    }
                } else {
                    notificationIfNoGPS();
                }
            }
        }catch (Exception e){
            gpsManager.startListening(this);
            gpsManager.setGPSCallback(this);
            Log.e("SOS", "Something went wrong");
        }
    }

    /**
     * Μέθοδος αναγνώρισης φωνής όταν πατηθεί το κουμπί
     * @param view view
     */
    public void speak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el_GR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Βοηθήστε μας να σας εντοπίσουμε, πείτε κάτι");
        startActivityForResult(intent, VOICE_REC_RESULT);
    }



    /**
     * Εαν μπει στο παρσκηνιο η εφαρμογη να μην τρεχουν απο πίσω οι σενσορες και το GPS
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, lightSensor);
        if(gpsManager != null) {
            gpsManager.stopListening();
        }
    }

    /**
     * Εαν επανελθει από το παρασκηνιο η εφαρμογη να μπουν σε λειτουργια και πάλι
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
            return;
        }
        gpsManager.startListening(this);
        gpsManager.setGPSCallback(this);
    }

    /**
     * Κλείνοντας την εφαρμογή
     */
    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    /**
     * Λειτουργίες για τον σενσορα του φωτός
     * και για τον σένσορα της επιτάχυνσης
     * @param event γεγονός
     */
    @SuppressLint("SetTextI18n")
    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) //εαν ο τυπος του σενσορα ειναι acceleration
        {
            float x =  event.values[0];
            float y =  event.values[1];
            float z =  event.values[2];
            final double holeAcceleration = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));

            //ελεγχος ευαισθησίας επιτάχυνσης
            if (holeAcceleration >= MAX_ACCELERATION)
            {
                accelerationBlocker++;

                if (accelerationBlocker == 1)
                {
                    countDownTimerAcceleration = new CountDownTimer(30000, 1000){

                        @Override
                        public void onTick(long millisUntilFinished)
                        {
                            mp.start();
                        }

                        @Override
                        public void onFinish()
                        {
                            accelerationBlocker = 0;

                            if (latitude != 0.0 && longitude != 0.0) {
                                if (CheckInternetConnection.isConnected(MainActivity.this) && CheckInternetConnection.isConnectedFast(MainActivity.this)) {
                                    SOS();
                                    String message = "Προσοχή έπεσε το κινητό";
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                    Values save = new Values(message, String.valueOf(latitude), String.valueOf(longitude));
                                    dropDanger.push().setValue(save);
                                }else{
                                    notificationIfNoInternet();
                                }
                            }else{
                                notificationIfNoGPS();
                            }
                        }
                    };countDownTimerAcceleration.start();
                }
            }
        }

        if (sensor.getType() == Sensor.TYPE_LIGHT) //εαν ο τυπος του σενσορα ειναι light
        {
            float light = event.values[0];

            if(light > MAX_LIGHT )
            {
                if (latitude != 0.0 && longitude != 0.0)
                {
                    lightBlocker++;

                    if (lightBlocker == 1) {
                        if (CheckInternetConnection.isConnected(MainActivity.this) && CheckInternetConnection.isConnectedFast(MainActivity.this)) {
                            String message = "Προσοχή υψηλη ακτινοβολία";
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            Values save = new Values(message, String.valueOf(latitude), String.valueOf(longitude));
                            lightDanger.push().setValue(save);
                            countDownTimerLight = new CountDownTimer(10000, 1000) {//για να μην ρίχνει άσκοπα δεδομένα στη βάση, ρυθμίζουμε ν περασει κάποιο χρονικό διάστημα πριν ξαναρχισει

                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    lightBlocker = 0;
                                }
                            };
                            countDownTimerLight.start();
                        } else {
                            notificationIfNoInternet();
                            lightBlocker = 0;
                        }
                    }
                }else{
                    notificationIfNoGPS();
                    lightBlocker = 0;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Παρε την διέυθυνση από την τοποθεσία
     * @param context Η τρέχων δρασηριότητα
     * @param cod Γεωγραφικο μήκος-πλάτος
     * @return Συμβολοσειρά
     */
    public String findAddress(Context context, LatLng cod){
        // σου λεει σε ποια διευθυνση βρισκεσαι
        Geocoder gcd = new Geocoder(context, Locale.ENGLISH);//getDefault ->για τα ΕΛΛΗΝΙΚΑ
        String stringAddress = null;
        try {
            //βρες σπο τις συντεταγμενες την τοποθεσια και αποθηκευσε την στη λιστα
            List<Address> addresses = gcd.getFromLocation(cod.latitude,cod.longitude,1);
            if(addresses.size() > 0){
                stringAddress = addresses.get(0).getAddressLine(0);
            }
            else{
                Toast.makeText(MainActivity.this, "Δεν μπόρεσα να βρώ την διεύθυνση, προσπάθησε ξανά", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Ωχ κάτι πήγε στραβά, Προσπαθήστε ξανά", Toast.LENGTH_LONG).show();
        }
        return stringAddress;
    }


    /**
     * Λειτουργία κουμπιού ABORT για αυτό το Activity
     * @param view sos
     */
    public void abort(View view){
        try {
            if (accelerationBlocker >= 1) //εαν εχουμε εντοπισει επιτάχυνση...
            {
                countDownTimerAcceleration.cancel();
                accelerationBlocker = 0;

            } else if(isSOSPressed) { //εαν έχει σταλει μήνυμα SOS, μπες στη φόρμα να το ακυρώσεις

                Toast.makeText(MainActivity.this, "Άκυρος ο Συναγερμός. Όλα καλά", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, LoginForm.class);
                startActivity(intent);
                isSOSPressed = false;
            }
        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "Ωχ κάτι πήγε στραβά, Προσπαθήστε ξανά", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Λειτουργία κουμπιού SOS για αυτό το Activity
     * @param view sos
     */
    public void sos(View view){
        try {
            SOS();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }

    /**
     * Μέθοδος για αποθήκευση στην βάση και δημιουργία
     * μηνυμάτων κατηγορίας μεγάλου κινδύνου
     */
    public void SOS(){
        StringBuilder help = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        //έλεγχος εαν υπάρχει συνδεδεμένο wifi, εαν υπάρχει τότε να γίνει επιπλέον έλεγχος για επιλογή του provider γιατι μπορεί να μην πιάνει στίγμα το GPS εσωτερικά
        if(CheckInternetConnection.isConnectedWifi(getApplicationContext())) {
            Location location = gpsManager.startListeningForSOS(getApplicationContext());
            gpsManager.setGPSCallback(this);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        //εαν υπάρχει στίγμα
        if (latitude != 0.0 && longitude != 0.0) {
            //εαν υπάρχει σύνδεση στο NET
            if (CheckInternetConnection.isConnected(MainActivity.this) && CheckInternetConnection.isConnectedFast(MainActivity.this)) {
                isSOSPressed = true;
                    help.append("Tôi đang ở vị trí có kinh độ : ").append(longitude).append(" và vĩ độ\n : ").append(latitude).append(" và tôi cần giúp đỡ\n");
                Toast.makeText(MainActivity.this, help, Toast.LENGTH_LONG).show();

                Values save = new Values(help.toString(), String.valueOf(latitude), String.valueOf(longitude));
                bigDanger.push().setValue(save);

                SendSMS sms = new SendSMS();
                sms.SOS_SMS(myDb.getPhones(), longitude, latitude);

                LatLng cod = new LatLng(latitude, longitude);

                builder.append(findAddress(MainActivity.this, cod));
                for (int i = 0; i < 3; i++) {
                    myTTS.speak("Help me, I am in" + builder + " address, come and get me"); //ομιλία
                }
            }
            //εαν δεν υπαρχει στείλε μήνυμα
            else{
                notificationIfNoInternet();
            }
        }else{
            notificationIfNoGPS();
        }
    }

    /**
     * Μετάβαση στα στατιστικά
     * @param view view
     */
    public void goToStatistics(View view){
        Intent statistics = new Intent(MainActivity.this, ShowStatistics.class);
        startActivity(statistics);
    }

    /**
     * Μετάβαση στους χάρτες
     * @param view view
     */
    public void GoToMaps(View view){
        isMapsButtonPressed = true;
        ShowStatistics.isItemsButtonClicked = false;
        Intent maps = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(maps);
    }

    /**
     * Μήνυμα εαν δεν υπάρχει σύνδεση στο Internet
     */
    private void notificationIfNoInternet(){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Network Connection Problem!!!")
                        .setContentText("There is no Network Connection available");

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,257,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        assert manager != null;
        manager.notify(123,builder.build());
    }

    /**
     * Μήνυμα εαν δεν υπάρχει στίγμα GPS
     */
    private void notificationIfNoGPS(){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("GPS Signal Problem!!!")
                        .setContentText("GPS Signal Lost, Please Try again");

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,255,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        assert manager != null;
        manager.notify(123,builder.build());
    }

    /**
     * Με το που αλλάζει η τοποθεσία να παίρνει κάθε φορά τις συτεταγμένες και εαν
     * υπάρχει στίγμα ή Internet να τις αποθηκεύει στη βάση
     */
    @Override
    public void onGPSUpdate(Location location) {
        try {
            final double MPS_to_KPH = 3.6;
            if (location != null) { //εαν υπάρχει στίγμα
                latitudeForSpeed = location.getLatitude();
                longitudeForSpeed = location.getLongitude();
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                double speed = location.getSpeed() * MPS_to_KPH;
                if (speed > MAX_SPEED) {
                    speedBlocker++;
                    if (speedBlocker == 1) {
                        if (CheckInternetConnection.isConnected(MainActivity.this) && CheckInternetConnection.isConnectedFast(MainActivity.this)) {
                            String message = "Προσοχή υπάρχει κίνδυνος ατυχήματος λόγω υπερβολικής ταχύτητας!!!";
                            myTTS.speak("Please slow down "); //ομιλία
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            Values save = new Values(message, String.valueOf(latitudeForSpeed), String.valueOf(longitudeForSpeed));
                            speedDanger.push().setValue(save);
                            countDownTimerSpeed = new CountDownTimer(10000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }
                                @Override
                                public void onFinish() {
                                    speedBlocker = 0;
                                }
                            };
                            countDownTimerSpeed.start();
                        } else {
                            notificationIfNoInternet();
                            speedBlocker = 0;
                        }
                    }
                }
            } else {
                notificationIfNoGPS();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }
}
