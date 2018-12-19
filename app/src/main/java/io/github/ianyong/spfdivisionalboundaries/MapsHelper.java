package io.github.ianyong.spfdivisionalboundaries;

import android.content.Context;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public class MapsHelper {

    private Context context;
    private GoogleMap googleMap;
    private int screenWidth, screenHeight;
    private Marker marker;

    public MapsHelper(Context context) {
        this.context = context;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public void processLocation(AutocompletePrediction autocompletePrediction) {
        String placeId = autocompletePrediction.getPlaceId();
        Places.getGeoDataClient(context).getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    moveCamera(myPlace);
                    places.release();
                }
            }
        });
    }

    private void moveCamera(Place myPlace) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(myPlace.getViewport(), screenWidth, screenHeight, 0));
            addMarker(myPlace.getLatLng(), myPlace.getName().toString());
        }
    }

    public void addMarker(LatLng pos, String name) {
        if (marker != null) {
            marker.remove();
        }
        marker = googleMap.addMarker(new MarkerOptions().position(pos)
                .title(name));
        marker.showInfoWindow();
    }

    public void initialiseGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarker(latLng, "");
            }
        });
    }

}
