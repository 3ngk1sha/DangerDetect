package com.unipi.vnikolis.unipismartalert.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.unipi.vnikolis.unipismartalert.R;
import com.unipi.vnikolis.unipismartalert.model.Values;
import java.util.ArrayList;

/**
 * A custom Adapter for our ListView
 * to store our variables
 */
public class ItemsAdapter extends ArrayAdapter<Values> {

    public ItemsAdapter(Context context, ArrayList<Values> values) {
        super(context, 0, values);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Πάρε τα δεδομένα του αντικειμένου για αυτή τη θέση και αποθηκευσέτα στο μοντέλο Values
        Values model = getItem(position);

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
        assert model != null;
        date.setText(String.valueOf(model.CorrectDate()));
        latitude.setText(model.getLatitude());
        longitude.setText(model.getLongitude());
        stateEmergency.setText(model.getStateEmergency());

        // Επέστρεψε το View για να εμφανιστει στην οθόνη
        return convertView;
    }
}
