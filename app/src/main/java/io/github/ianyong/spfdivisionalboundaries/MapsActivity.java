package io.github.ianyong.spfdivisionalboundaries;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.data.kml.KmlLayer;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingSearchView searchView;
    private List<LocationSuggestion> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        list = new ArrayList<>();
        searchView = findViewById(R.id.floating_search_view);
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                } else {
                    list.clear();
                    list.add(new LocationSuggestion("a"));
                    list.add(new LocationSuggestion("b"));
                    list.add(new LocationSuggestion("c"));
                    list.add(new LocationSuggestion("d"));
                    searchView.swapSuggestions(list);
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
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

        LatLng singapore = new LatLng(1.352083, 103.819836); // Center of Singapore
        LatLngBounds viewportBounds = new LatLngBounds(new LatLng(1.1496, 103.594), new LatLng(1.4784001, 104.0945001));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 10));
        mMap.setLatLngBoundsForCameraTarget(viewportBounds);
        mMap.setMinZoomPreference(10.0f);

        try {
            KmlLayer NPCs = new KmlLayer(mMap, R.raw.singapore_police_force_npc_boundary_kml, getApplicationContext());
            NPCs.addLayerToMap();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
