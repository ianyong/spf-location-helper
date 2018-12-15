package io.github.ianyong.spfdivisionalboundaries;

import android.content.Context;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public class MapsHelper {

    private Context context;
    private GoogleMap googleMap;
    private int screenWidth, screenHeight;

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
                    moveCamera(myPlace.getViewport());
                    places.release();
                }
            }
        });
    }

    private void moveCamera(LatLngBounds bounds) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, screenWidth, screenHeight, 0));
        }
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

}
