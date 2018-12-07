package io.github.ianyong.spfdivisionalboundaries;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlLayer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static String DIV_CODE = "DIV";
    private final static String NPC_NAME = "NPC_NAME";
    private final static String DIV_NAME = "DIVISION";

    private GoogleMap googleMap;
    private FloatingSearchView searchView;
    private BottomSheetBehavior bottomSheetBehaviour;
    private List<LocationSuggestion> suggestionList;
    private TextView text;
    private KmlParser parser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        text = findViewById(R.id.kml_clicked);
        suggestionList = new ArrayList<>();
        searchView = findViewById(R.id.floating_search_view);
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                } else {
                    suggestionList.clear();
                    suggestionList.add(new LocationSuggestion("a"));
                    suggestionList.add(new LocationSuggestion("b"));
                    suggestionList.add(new LocationSuggestion("c"));
                    suggestionList.add(new LocationSuggestion("d"));
                    searchView.swapSuggestions(suggestionList);
                }
            }
        });

        InputStream inputStream = getResources().openRawResource(R.raw.singapore_police_force_npc_boundary_kml);
        try {
            parser = new KmlParser(inputStream);
            parser.parseKml();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Obtain the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Move compass location
        View compassButton = mapFragment.getView().findViewWithTag("GoogleMapCompass");
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        rlp.topMargin = (int) (getResources().getDimension(R.dimen.sliding_search_view_header_height)/getResources().getDisplayMetrics().density) + rlp.leftMargin;

        // Set up the bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet);

        // Get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);
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
        this.googleMap = googleMap;

        LatLng singapore = new LatLng(1.352083, 103.819836); // Center of Singapore
        LatLngBounds viewportBounds = new LatLngBounds(new LatLng(1.1496, 103.594), new LatLng(1.4784001, 104.0945001));

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 10));
        this.googleMap.setLatLngBoundsForCameraTarget(viewportBounds);
        this.googleMap.setMinZoomPreference(10.0f);

        try {
            KmlLayer npcLayer = new KmlLayer(this.googleMap, R.raw.singapore_police_force_npc_boundary_kml, getApplicationContext());
            npcLayer.addLayerToMap();
            // Set a listener for geometry clicked events.
            npcLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                @Override
                public void onFeatureClick(Feature feature) {
                    if(feature != null) {
                        text.setText(parser.getKmlPlacemark(feature.getProperty("name")).getProperty(NPC_NAME));
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
