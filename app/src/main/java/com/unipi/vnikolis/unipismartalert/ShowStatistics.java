package com.unipi.vnikolis.unipismartalert;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * The backend code for Statistics Activity
 */
public class ShowStatistics extends AppCompatActivity implements AdapterView.OnItemClickListener {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference possiblyDanger, bigDanger, lightDanger, speedDanger, dropDanger;
    Spinner mDangerSpinner, mSortDateSpinner;
    String dangerSelect, dateSelect, twoDigitMonth, twoDigitDay, dateToCompare;
    TextView dateView;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    ArrayList<Values> dangerList = new ArrayList<>();
    boolean dateIsSelected, sortDatesIsSelected, dangerIsSelected;
    static boolean isItemsButtonClicked;
    ArrayAdapter<String> myAdapter2, myAdapter;
    ItemsAdapter adapter;
    ListView mUserList;
    Values v;
    int firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_statistics);

        firebaseDatabase = FirebaseDatabase.getInstance();
        possiblyDanger = firebaseDatabase.getReference("PossiblyDanger");
        bigDanger = firebaseDatabase.getReference("BigDanger");
        lightDanger = possiblyDanger.child("LightDanger");
        speedDanger = possiblyDanger.child("SpeedDanger");
        dropDanger = possiblyDanger.child("DropDanger");
        mUserList = findViewById(R.id.listView);

        try {
            if (CheckInternetConnection.isConnected(ShowStatistics.this) && CheckInternetConnection.isConnectedFast(ShowStatistics.this)) { //ελεγχος εαν υπάρχει σύνδεση Internet
                calendarPicker();
                dangerPicker();
                datePicker();
                dangerSelect();
            }else{
                Toast.makeText(ShowStatistics.this, "Δεν υπάρχει σύνδεση στο Internet, προσπάθησε ξανά", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }

    /**
     * Κατασκεύη ημερολογίου και
     * Επιλογή ημερομηνίας
     */
    public void calendarPicker(){
        try {
            //create the calendar date picker
            dateView = findViewById(R.id.dateView);
            dateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dateIsSelected = true;
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog dialog = new DatePickerDialog(ShowStatistics.this, mDateSetListener, year, month, day);
                    dialog.show();
                }
            });

            mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    firstTime++;
                    if (firstTime > 1) { //για να μην κρασαρει την πρωτη φορα που ανοιγει η εφαρμογη επιλεγοντας πρωτα την ημερομηνια
                        mDangerSpinner.setAdapter(myAdapter);
                        adapter.clear();
                    }
                    if (firstTime == 1 && dangerIsSelected) { //για να μην κρασαρει την πρωτη φορα που ανοιγει η εφαρμογη επιλεγοντας πρωτα την ημερομηνια
                        mDangerSpinner.setAdapter(myAdapter);
                        adapter.clear();
                    }
                    month++;            // οι μήνες ξεκινάνε από το 0 οπότε προσθέτουμε 1
                    // Τωρα θα τα μετατρέψω σε 2 digit format γιατί έτσι είναι αποθηκευμένα στη βάση
                    // ώστε να κάνω σύγκριση

                    if (month < 10) {
                        twoDigitMonth = "0" + month;
                    } else {
                        twoDigitMonth = String.valueOf(month);
                    }
                    if (dayOfMonth < 10) {
                        twoDigitDay = "0" + dayOfMonth;
                    } else {
                        twoDigitDay = String.valueOf(dayOfMonth);
                    }
                    dateToCompare = year + "/" + twoDigitMonth + "/" + twoDigitDay;
                    dateView.setText(dateToCompare);

                }
            };
        }catch (Exception e){
            Toast.makeText(ShowStatistics.this, "Κάτι πήγε στραβά, προσπάθησε ξανά", Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Επιλογή κινδύνου από το dropDown Menu
     */
    public void dangerPicker(){
        try {
            //τραβάει τα δεδομένα από το dropdown menu ανα κατηγορια συμβαντος
            mDangerSpinner = findViewById(R.id.spinner);
            myAdapter = new ArrayAdapter<String>(ShowStatistics.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinnerItems)) {
                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if (position == 0) {
                        tv.setVisibility(View.GONE);
                    } else { //τοποθετηση χρώματος
                        tv.setVisibility(View.VISIBLE);
                        if (position % 2 == 1) {
                            tv.setBackgroundColor(Color.parseColor("#FFF9A600"));
                        } else {
                            tv.setBackgroundColor(Color.parseColor("#FFE49200"));
                        }
                    }
                    return view;
                }
            };
            myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mDangerSpinner.setPrompt("Choose Danger Category");
            mDangerSpinner.setAdapter(myAdapter);
        }catch (Exception e){
            Toast.makeText(ShowStatistics.this, "Κάτι πήγε στραβά, προσπάθησε ξανά", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Επιλογή ταξινόμισης από το dropDown Menu
     */
    public void datePicker(){
        mSortDateSpinner = findViewById(R.id.spinner2);
        myAdapter2 = new ArrayAdapter<String>(ShowStatistics.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinnerItems2)){
            @SuppressLint("SetTextI18n")
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent){
                sortDatesIsSelected  = true;
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0)
                {
                    tv.setVisibility(View.GONE);
                }
                else{ //τοποθετηση χρώματος
                    tv.setVisibility(View.VISIBLE);
                    if(position%2==1)
                    {
                        tv.setBackgroundColor(Color.parseColor("#FFF9A600"));
                    }
                    else{
                        tv.setBackgroundColor(Color.parseColor("#FFE49200"));
                    }
                }
                return view;
            }
        };
        myAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortDateSpinner.setPrompt("Choose to Sort by Date");
        mSortDateSpinner.setAdapter(myAdapter2);
    }

    /**
     * Ανάλογα με την επιλογή κινδύνου που θα γίνει
     * θα τραβήξει και τα αντίστοιχα αποτελέσματα
     */
    public void dangerSelect()
    {
        try {
            mDangerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    dangerSelect = mDangerSpinner.getSelectedItem().toString();
                    switch (dangerSelect) {
                        case "Drop Danger":
                            dangerIsSelected = true;
                            //επιλογή κινδύνου
                            dangerSelector(dropDanger);
                            //επιλογή ταξινόμισης
                            sortDateSelector(dropDanger, mSortDateSpinner);
                            break;
                        case "Speed Danger":
                            dangerIsSelected = true;
                            dangerSelector(speedDanger);
                            sortDateSelector(speedDanger, mSortDateSpinner);
                            break;
                        case "Light Danger":
                            dangerIsSelected = true;
                            dangerSelector(lightDanger);
                            sortDateSelector(lightDanger, mSortDateSpinner);
                            break;
                        case "Possibly Danger":
                            dangerIsSelected = true;
                            dangerSelector(possiblyDanger);
                            sortDateSelector(possiblyDanger, mSortDateSpinner);
                            break;
                        case "Big Danger":
                            dangerIsSelected = true;
                            dangerSelector(bigDanger);
                            sortDateSelector(bigDanger, mSortDateSpinner);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }

    /**
     * Συλλογή δεδομένων από την FireBase
     * ταξινόμηση εαν χρειάζεται και τοποθέτηση
     * των δεομένων στο ListView για την εμφάνιση των αποτελεσμάτων
     * @param keys Μεταβλητή Iterable τύπου DataSnapshot για την καταλληλότερη
     *             αναζήτηση αποτελεσμάτων
     */
    @SuppressLint("SetTextI18n")
    private void collectDangers(Iterable<DataSnapshot> keys) {
        try {
            dangerList.clear();
            adapter = new ItemsAdapter(this, dangerList);
            mUserList.setAdapter(adapter);
            mUserList.setOnItemClickListener(this);
            String compareDate;

            if (dangerSelect.equals("Possibly Danger")) {
                for (DataSnapshot i : keys) {
                    for (DataSnapshot j : i.getChildren()) {
                        v = j.getValue(Values.class);
                        //εαν υπάρχει διαθέσιμη ημερομηνία από το ημερολόγιο για σύγκριση... κάνε την σύγκριση
                        if (dateToCompare != null) {
                            assert v != null;
                            compareDate = v.date.substring(0, 10); //πάρε μονο ένα κομάτι από την συμβολοσειρά της ημερομηνίας που είναι αποθηκευμένη στη βάση
                            if (compareDate.equals(dateToCompare)) { //και συγκρινέ αυτήν με την ημερομηνία που έχει επιλεγεί από το ημερολόγιο
                                adapter.add(v); //γέμισε την λίστα
                            }
                        } else {
                            adapter.add(v); //εδω γεμίζει η list
                        }
                    }
                }
            } else if (dangerSelect.equals("Big Danger") || dangerSelect.equals("Light Danger") || dangerSelect.equals("Speed Danger") || dangerSelect.equals("Drop Danger")) {
                for (DataSnapshot i : keys) {
                    v = i.getValue(Values.class);
                    if (dateToCompare != null) {
                        assert v != null;
                        compareDate = v.date.substring(0, 10);
                        if (compareDate.equals(dateToCompare)) {
                            adapter.add(v);
                        }
                    } else {
                        adapter.add(v); //εδω γεμίζει η list
                    }
                }
            }

            //εαν εχει επιλεγει η ταξινομιση κάνε την κατα άυξουσα η φθίνουσα σειρά
            if (dateSelect != null) {
                if (dateSelect.equals("Ascending")) {
                    //ταξινόμιση βαση ημερομηνιας κατα αυξουσα
                    Collections.sort(dangerList, new Comparator<Values>() {
                        @Override
                        public int compare(Values o1, Values o2) {
                            return o1.date.compareTo(o2.date);
                        }
                    });
                } else if (dateSelect.equals("Descending")) {
                    //ταξινόμηση βαση ημερομηνιας κατα φθινουσα
                    Collections.sort(dangerList, Collections.reverseOrder(new Comparator<Values>() {
                        @Override
                        public int compare(Values o1, Values o2) {
                            return o1.date.compareTo(o2.date);
                        }
                    }));
                }
            }
            dateView.setText("Pick Date");
            dateToCompare = null;
            mSortDateSpinner.setAdapter(myAdapter2);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("SOS", "Something went wrong");
        }
    }

    /**
     * Σε κάθε αλλάγη της FireBase καλεί την μέδοδο collectDangers
     * @param kindOfDanger To Reference της FireBase
     */
    private void dangerSelector(DatabaseReference kindOfDanger){
        kindOfDanger.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) //εαν υπάρχει κάτι σε αυτον τον πίνακα
                {
                    collectDangers(dataSnapshot.getChildren());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowStatistics.this, "Αποτυχία Ανάγνωσης από τη Βάση", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Επιλογή ταξινόμησης κατα άυξουσα η φθίνουσα σειρά
     * @param kindOfDanger To Reference της FireBase
     * @param selectorToSort Ο Spinner που θέλουμε
     */
    //
    private void sortDateSelector(final DatabaseReference kindOfDanger, Spinner selectorToSort){
        selectorToSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateSelect = mSortDateSpinner.getSelectedItem().toString();
                switch (dateSelect){
                    case "Ascending":
                        //ταξινόμιση κατα άυξουσα
                        dangerSelector(kindOfDanger);
                        break;
                        //ταξινόμιση κατα φθίνουσα
                    case "Descending":
                        dangerSelector(kindOfDanger);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Μετάβαση στους χάρτες για έυρεση της συγκεκριμένης
     * τοποθεσίας από το ListView
     * @param parent ..
     * @param view ..
     * @param position ..
     * @param id ..
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        isItemsButtonClicked = true;
        MainActivity.isMapsButtonPressed = false;
        Values o =(Values) mUserList.getItemAtPosition(position);
        Intent maps = new Intent(ShowStatistics.this, MapsActivity.class);
        maps.putExtra("latitude", o.getLatitude());
        maps.putExtra("longitude", o.getLongitude());
        maps.putExtra("date", o.getDate());
        startActivity(maps);
    }
}
