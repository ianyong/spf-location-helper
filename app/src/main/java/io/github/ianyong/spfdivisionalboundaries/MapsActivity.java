package io.github.ianyong.spfdivisionalboundaries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPolygon;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Boundaries KML tags
    private final static String NPC_DIV_CODE = "DIV";
    private final static String NPC_NAME = "NPC_NAME";
    private final static String NPC_DIV_NAME = "DIVISION";
    private final static String NPC_INC_CRC = "INC_CRC";
    private final static String NPC_FMEL_UPD_D = "FMEL_UPD_D";

    // Establishments KML tags
    private final static String EST_NAME = "DEPARTMENT";
    private final static String EST_ST_NAME = "STREET_NAME";
    private final static String EST_BLDG_NAME = "BUILDING_NAME";
    private final static String EST_DIV = "TYPE";
    private final static String EST_TEL = "TELEPHONE";
    private final static String EST_TEL_ALT = "ALT_TELEPHONE_LINE";
    private final static String EST_UNIT_NO = "UNIT";
    private final static String EST_OPR_HRS = "OPERATING_HOURS";
    private final static String EST_FAX = "FAX";
    private final static String EST_BLK_NO = "HSE_BLK_NO";
    private final static String EST_POSTAL_CODE = "POSTAL_CODE";
    private final static String EST_INC_CRC = "INC_CRC";
    private final static String EST_FMEL_UPD_D = "FMEL_UPD_D";

    // Police station type
    private final static String TYPE_NPC = "Neighbourhood Police Centre";
    private final static String TYPE_NPP = "Neighbourhood Police Post";
    private final static String TYPE_OTHER = "Other";

    private final static LatLng SINGAPORE_CENTER = new LatLng(1.352083, 103.819836); // Center of Singapore
    private final static LatLngBounds SINGAPORE_BOUNDS = new LatLngBounds(
            new LatLng(1.1496, 103.594), new LatLng(1.4784001, 104.0945001)
    );
    private final int DRAWABLE_LEFT = 0, DRAWABLE_TOP = 1, DRAWABLE_RIGHT = 2, DRAWABLE_BOTTOM = 3;

    private GoogleMap googleMap;
    private BottomSheetBehaviorGoogleMapsLike bottomSheetBehaviour;
    private MergedAppBarLayout mergedAppBarLayout;
    private MergedAppBarLayoutBehavior mergedAppBarLayoutBehaviour;
    private AutocompleteFilter typeFilter;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private AutoCompleteTextView search;
    private View barrier, bottomSheet;
    private TextView bottomSheetHeader, bottomSheetAddress, bottomSheetOperatingStatus,
            bottomSheetOperatingHours, bottomSheetTelephone, bottomSheetFax;
    private LinearLayout bottomSheetButtonCall, bottomSheetButtonDirections;
    private Intent callIntent, directionsIntent;
    private KmlParser npcBoundaries, spfEstablishments;
    private Marker marker;
    private AddressResultReceiver resultReceiver;
    private Location selectedLocation;
    private KmlLayer npcLayer;
    private Drawable menu, delete;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton floatingActionButton;
    private boolean bottomSheetHidden = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        resultReceiver = new AddressResultReceiver(new Handler());
        menu = getApplicationContext().getResources().getDrawable(R.drawable.baseline_menu_black_24);
        delete = getApplicationContext().getResources().getDrawable(R.drawable.places_ic_clear);

        // Initialise bottom sheet dynamic elements.
        floatingActionButton = findViewById(R.id.floating_action_button);
        bottomSheetHeader = findViewById(R.id.bottom_sheet_header);
        bottomSheetAddress = findViewById(R.id.bottom_sheet_info_address);
        bottomSheetOperatingStatus = findViewById(R.id.bottom_sheet_info_operating_status);
        bottomSheetOperatingHours = findViewById(R.id.bottom_sheet_info_operating_hours);
        bottomSheetTelephone = findViewById(R.id.bottom_sheet_info_telephone);
        bottomSheetFax = findViewById(R.id.bottom_sheet_info_fax);

        // Set up bottom sheet call button.
        callIntent = new Intent(Intent.ACTION_DIAL);
        bottomSheetButtonCall = findViewById(R.id.bottom_sheet_button_call);
        bottomSheetButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(callIntent);
            }
        });

        // Set up bottom sheet directions button.
        directionsIntent = new Intent(Intent.ACTION_VIEW);
        directionsIntent.setPackage("com.google.android.apps.maps");
        bottomSheetButtonDirections = findViewById(R.id.bottom_sheet_button_directions);
        bottomSheetButtonDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(directionsIntent);
            }
        });

        // Set up navigation drawer.
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.nav_about) {
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });

        // Set up search bar.
        typeFilter = new AutocompleteFilter.Builder()
                .setCountry("SG")
                .build();
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, Places.getGeoDataClient(this), SINGAPORE_BOUNDS, typeFilter);
        search = findViewById(R.id.search);
        search.setAdapter(placeAutocompleteAdapter);
        search.setCompoundDrawablesWithIntrinsicBounds(menu, null, null, null);

        // Disables map while searching, hides keyboard if user clicks anywhere else.
        barrier = findViewById(R.id.barrier);
        barrier.setClickable(false);
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    barrier.setClickable(true);
                    showKeyboard(v);
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
                    processLocation((AutocompletePrediction) item);
                    search.clearFocus();
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

        // Clear text when delete button is clicked.
        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(search.getCompoundDrawables()[DRAWABLE_RIGHT] != null && event.getRawX() + search.getPaddingLeft() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        search.setText("");
                    } else if(event.getRawX() - search.getPaddingLeft() <= (search.getLeft() + search.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        search.clearFocus();
                        search.setEnabled(false); // Temporarily disable search bar to prevent it from gaining focus.
                        drawerLayout.openDrawer(GravityCompat.START);
                        search.setEnabled(true);
                        return true;
                    }
                }
                return false;
            }
        });

        // Shows delete button when search bar is not empty.
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {
                    search.setCompoundDrawablesWithIntrinsicBounds(menu, null, delete, null);
                } else {
                    search.setCompoundDrawablesWithIntrinsicBounds(menu, null, null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Parse KML file for placemark properties.
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.singapore_police_force_npc_boundary_kml);
            npcBoundaries = new KmlParser(inputStream);
            npcBoundaries.parseKml();
            inputStream = getResources().openRawResource(R.raw.singapore_police_force_establishments_kml);
            spfEstablishments = new KmlParser(inputStream);
            spfEstablishments.parseKml();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Obtain the SupportMapFragment.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Move compass location.
        View compassButton = mapFragment.getView().findViewWithTag("GoogleMapCompass");
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        rlp.topMargin = (int) (getResources().getDimension(R.dimen.search_view_header_height)/getResources().getDisplayMetrics().density) + rlp.leftMargin;

        // Set up the bottom sheet.
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehaviour = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        bottomSheetBehaviour.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Force redraw bottom sheet if previously hidden.
                if(newState == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN) {
                    bottomSheetHidden = true;
                } else if (newState == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED && bottomSheetHidden) {
                    bottomSheetHidden = false;
                    bottomSheet.requestLayout();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        mergedAppBarLayout = findViewById(R.id.merged_app_bar_layout);
        mergedAppBarLayoutBehaviour = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehaviour.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehaviour.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED) {
                    bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                } else if(bottomSheetBehaviour.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT) {
                    bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                }
            }
        });
        // A non-empty ArrayList must be passed for ImagePagerAdapter.instantiateItem() to be called.
        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this, new ArrayList<>(Arrays.asList("test")));
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(imagePagerAdapter);

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

        // Set a listener for long clicks.
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addMarker(latLng, "");
                selectedLocation = new Location("MapLongClick");
                selectedLocation.setLatitude(latLng.latitude);
                selectedLocation.setLongitude(latLng.longitude);
                findKmlPlacemark(latLng);
                // Start reverse geocode.
                startIntentService();
            }
        });

        // Set a listener for clicks (only affects areas not overlaid by the KML layer).
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideBottomSheet();
            }
        });

        try {
            npcLayer = new KmlLayer(this.googleMap, R.raw.singapore_police_force_npc_boundary_kml, getApplicationContext());
            npcLayer.addLayerToMap();
            // Set a listener for geometry clicked events.
            npcLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                @Override
                public void onFeatureClick(Feature feature) {
                    if(feature != null) {
                        updateBottomSheet(feature.getProperty("name"));
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void startIntentService() {
        Intent intent = new Intent(getApplicationContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, selectedLocation);
        startService(intent);
    }

    private void processLocation(AutocompletePrediction autocompletePrediction) {
        String placeId = autocompletePrediction.getPlaceId();
        Places.getGeoDataClient(getApplicationContext()).getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    moveCamera(myPlace);
                    findKmlPlacemark(myPlace.getLatLng());
                    places.release();
                }
            }
        });
    }

    private void moveCamera(Place myPlace) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(myPlace.getViewport(),
                    getApplicationContext().getResources().getDisplayMetrics().widthPixels,
                    getApplicationContext().getResources().getDisplayMetrics().heightPixels,
                    0));
            addMarker(myPlace.getLatLng(), myPlace.getName().toString());
        }
    }

    private void addMarker(LatLng pos, String name) {
        if (marker != null) {
            marker.remove();
        }
        marker = googleMap.addMarker(new MarkerOptions().position(pos)
                .title(name));
        marker.showInfoWindow();
    }

    private void showKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Updates bottom sheet dynamic information.
    private void updateBottomSheet(String kmlId) {
        KmlPlacemarkProperties placemark = null;
        // Find the relevant entry in the Establishments KML file.
        for(Map.Entry<String, KmlPlacemarkProperties> entry : spfEstablishments.getKmlPlacemarks().entrySet()) {
            if(entry.getValue().hasProperty(EST_NAME) &&
                    entry.getValue().getProperty(EST_NAME).equals(npcBoundaries.getKmlPlacemark(kmlId).getProperty(NPC_NAME) + " "
                    + getString(R.string.neighbourhood_police_centre))) {
                placemark = entry.getValue();
                break;
            }
        }
        // Update name.
        String name = placemark.getProperty(EST_NAME).replace(" " + getString(R.string.neighbourhood_police_centre), "");
        bottomSheetHeader.setText(name);
        mergedAppBarLayoutBehaviour.setToolbarTitle(name + " " + getString(R.string.neighbourhood_police_centre_abbreviation));
        // Update address.
        String address = constructAddress(placemark);
        bottomSheetAddress.setText(address);
        directionsIntent.setData(Uri.parse("google.navigation:q=" + address.replace(" ", "+")));
        // Update operating hours.
        // TODO: Implement operating status based off current time.
        bottomSheetOperatingHours.setText(placemark.getProperty(EST_OPR_HRS));
        // Update telephone number.
        if(placemark.hasProperty(EST_TEL)) {
            bottomSheetTelephone.setText(placemark.getProperty(EST_TEL));
            callIntent.setData(Uri.parse("tel:" + placemark.getProperty(EST_TEL)));
        } else {
            bottomSheetTelephone.setText("-");
            callIntent.setData(null);
        }
        // Update fax number.
        if(placemark.hasProperty(EST_FAX)) {
            bottomSheetFax.setText(placemark.getProperty(EST_FAX));
        } else {
            bottomSheetFax.setText("-");
        }
        showBottomSheet();
    }

    private void showBottomSheet() {
        bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        floatingActionButton.show();
    }

    private void hideBottomSheet() {
        bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
    }

    private String constructAddress(KmlPlacemarkProperties placemark) {
        String address = placemark.getProperty(EST_BLK_NO) + " "
                + placemark.getProperty(EST_ST_NAME);
        if(placemark.hasProperty(EST_UNIT_NO)) {
            address += " " + placemark.getProperty(EST_UNIT_NO);
        }
        address += ", Singapore " + placemark.getProperty(EST_POSTAL_CODE);
        return address;
    }

    // Finds the Placemark which contains the point specified.
    private void findKmlPlacemark(LatLng point) {
        for(KmlContainer container : npcLayer.getContainers()) {
            for(KmlContainer nestedContainer : container.getContainers()) {
                for (KmlPlacemark placemark : nestedContainer.getPlacemarks()) {
                    if (PolyUtil.containsLocation(point, ((KmlPolygon) placemark.getGeometry()).getOuterBoundaryCoordinates(), true)) {
                        updateBottomSheet(placemark.getProperty("name"));
                        return;
                    }
                }
            }
        }
        // Hide bottom sheet if location chosen does not fall under any placemarks.
        hideBottomSheet();
    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            if (resultCode == Constants.SUCCESS_RESULT) {
                // Update marker with reverse geocode result.
                marker.setTitle(addressOutput);
                marker.showInfoWindow();
            }
        }

    }

}
