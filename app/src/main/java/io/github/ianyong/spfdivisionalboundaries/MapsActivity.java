package io.github.ianyong.spfdivisionalboundaries;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static String DIV_CODE = "DIV";
    private final static String NPC_NAME = "NPC_NAME";
    private final static String DIV_NAME = "DIVISION";
    private final static LatLng SINGAPORE_CENTER = new LatLng(1.352083, 103.819836); // Center of Singapore
    private final static LatLngBounds SINGAPORE_BOUNDS = new LatLngBounds(
            new LatLng(1.1496, 103.594), new LatLng(1.4784001, 104.0945001)
    );

    private GoogleMap googleMap;
    private BottomSheetBehavior bottomSheetBehaviour;
    private AutocompleteFilter typeFilter;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private AutoCompleteTextView search;
    private View barrier;
    private TextView text;
    private KmlParser parser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        text = findViewById(R.id.kml_clicked);

        // Set up search bar.
        typeFilter = new AutocompleteFilter.Builder()
                .setCountry("SG")
                .build();
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, Places.getGeoDataClient(this), SINGAPORE_BOUNDS, typeFilter);
        search = findViewById(R.id.search);
        search.setAdapter(placeAutocompleteAdapter);

        // Disables map while searching, hides keyboard if user clicks anywhere else.
        barrier = findViewById(R.id.barrier);
        barrier.setClickable(false);
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    barrier.setClickable(true);
                } else {
                    barrier.setClickable(false);
                    hideKeyboard(v);
                }
            }
        });

        search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item instanceof AutocompletePrediction) {
                    text.setText("test");
                }
            }
        });

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    v.clearFocus();
                }
                return false;
            }
        });

        // Parse KML file for placemark properties.
        InputStream inputStream = getResources().openRawResource(R.raw.singapore_police_force_npc_boundary_kml);
        try {
            parser = new KmlParser(inputStream);
            parser.parseKml();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Obtain the SupportMapFragment.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Move compass location.
        View compassButton = mapFragment.getView().findViewWithTag("GoogleMapCompass");
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        rlp.topMargin = (int) (getResources().getDimension(R.dimen.sliding_search_view_header_height)/getResources().getDisplayMetrics().density) + rlp.leftMargin;

        // Set up the bottom sheet.
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

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SINGAPORE_CENTER, 10));
        this.googleMap.setLatLngBoundsForCameraTarget(SINGAPORE_BOUNDS);
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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
