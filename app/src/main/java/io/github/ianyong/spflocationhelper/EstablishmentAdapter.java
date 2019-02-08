package io.github.ianyong.spflocationhelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class EstablishmentAdapter extends ArrayAdapter<EstablishmentDistance> {

    private Context context;
    private int resourceLayout;

    public EstablishmentAdapter(Context context, int resourceLayout, ArrayList<EstablishmentDistance> establishmentDistanceList) {
        super(context, resourceLayout, establishmentDistanceList);
        this.context = context;
        this.resourceLayout = resourceLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            v = LayoutInflater.from(context).inflate(resourceLayout, null);
        }

        EstablishmentDistance establishmentDistance = getItem(position);
        if(establishmentDistance != null) {
            TextView name = v.findViewById(R.id.adapter_establishment_name);
            TextView distance = v.findViewById(R.id.adapter_establishment_distance);

            if(name != null) {
                name.setText(formatName(establishmentDistance.getName()));
            }
            if(distance != null) {
                distance.setText(formatDistance(establishmentDistance.getDistance()));
            }
        }

        return v;
    }

    private String formatName(String name) {
        if(name.contains("Centre")) {
            name = name.replace("Neighbourhood Police Centre", "NPC");
        } else if(name.contains("Post")) {
            name = name.replace("Neighbourhood Police Post", "NPP");
        }
        return name;
    }

    // Format distance to one decimal place and convert to km if >= 1km.
    private String formatDistance(double distance) {
        String unit = "m";
        DecimalFormat formatter = new DecimalFormat("###.#");
        if(distance >= 1000) {
            distance /= 1000;
            unit = "km";
        }
        String distanceString = formatter.format(distance);
        return distanceString.concat(unit);
    }

}
