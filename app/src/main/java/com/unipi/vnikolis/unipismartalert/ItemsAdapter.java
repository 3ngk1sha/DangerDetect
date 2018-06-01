package com.unipi.vnikolis.unipismartalert;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * A custom Adapter for our ListView
 * to store our variables
 */
public class ItemsAdapter extends ArrayAdapter<Values> {

    ItemsAdapter(Context context, ArrayList<Values> values) {
        super(context, 0, values);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Πάρε τα δεδομένα του αντικειμένου για αυτή τη θέση και αποθηκευσέτα στο μοντέλο Values
        Values mondel = getItem(position);

        // Τσεκαρε εαν μια υπάρχουσα View έχει ξαναχρησιμοποιηθει, αλλίως γέμισε το View
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.items, parent, false);
        }
        // Αρχικοποιησε τα αντικειμενα
        TextView date = convertView.findViewById(R.id.date);
        TextView latitude =  convertView.findViewById(R.id.latitude);
        TextView longitude =  convertView.findViewById(R.id.longitude);
        TextView stateEmergency = convertView.findViewById(R.id.stateEmergency);

        // γεμισε τα αντικείμενα με τις τιμες
        assert mondel != null;
        date.setText(String.valueOf(mondel.getDate()));
        latitude.setText(mondel.getLatitude());
        longitude.setText(mondel.getLongitude());
        stateEmergency.setText(mondel.getStateEmergency());

        // Επέστρεψε το View για να εμφανιστει στην οθόνη
        return convertView;
    }
}
