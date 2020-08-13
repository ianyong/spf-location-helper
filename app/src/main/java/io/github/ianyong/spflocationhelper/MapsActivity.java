package io.github.ianyong.spflocationhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPoint;
import com.google.maps.android.data.kml.KmlPolygon;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
    private final static String EST_FB_ID = "FACEBOOK_ID";
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
    private TextView bottomSheetHeader, bottomSheetHeaderSubtext, bottomSheetAddress, bottomSheetOperatingStatus,
            bottomSheetOperatingHours, bottomSheetTelephone, bottomSheetFax, bottomSheetWebsite;
    private ImageView bottomSheetImage;
    private LinearLayout bottomSheetButtonCall, bottomSheetButtonDirections, bottomSheetButtonWebsite;
    private CoordinatorLayout baseLayout;
    private Intent callIntent, directionsIntent, websiteIntent;
    private KmlParser npcBoundaries, spfEstablishments;
    private Marker marker;
    private AddressResultReceiver resultReceiver;
    private Location selectedLocation;
    private KmlLayer npcLayer, establishmentsLayer;
    private Drawable menu, delete;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Switch establishmentsSwitch;
    private FloatingActionButton floatingActionButton;
    private AlertDialog noMarkerDialog, searchTypeDialog;
    private boolean bottomSheetHidden = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not reload app state if already loaded.
        if(savedInstanceState != null) {
            return;
        }
        setContentView(R.layout.activity_maps);

        // Force keyboard to not resize application.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        resultReceiver = new AddressResultReceiver(new Handler());
        menu = getApplicationContext().getResources().getDrawable(R.drawable.baseline_menu_black_24);
        delete = getApplicationContext().getResources().getDrawable(R.drawable.places_ic_clear);

        // Initialise bottom sheet dynamic elements.
        bottomSheetImage = findViewById(R.id.bottom_sheet_image);
        bottomSheetHeader = findViewById(R.id.bottom_sheet_header);
        bottomSheetHeaderSubtext = findViewById(R.id.bottom_sheet_header_subtext);
        bottomSheetAddress = findViewById(R.id.bottom_sheet_info_address);
        bottomSheetOperatingStatus = findViewById(R.id.bottom_sheet_info_operating_status);
        bottomSheetOperatingHours = findViewById(R.id.bottom_sheet_info_operating_hours);
        bottomSheetTelephone = findViewById(R.id.bottom_sheet_info_telephone);
        bottomSheetFax = findViewById(R.id.bottom_sheet_info_fax);
        bottomSheetWebsite = findViewById(R.id.bottom_sheet_info_website);

        // Set up bottom sheet call button.
        callIntent = new Intent(Intent.ACTION_DIAL);
        bottomSheetButtonCall = findViewById(R.id.bottom_sheet_button_call);
        bottomSheetButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callIntent.getData() != null) {
                    startActivity(callIntent);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_number_found), Toast.LENGTH_SHORT).show();
                }
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

        // Set up bottom sheet website button.
        websiteIntent = new Intent(Intent.ACTION_VIEW);
        bottomSheetButtonWebsite = findViewById(R.id.bottom_sheet_button_website);
        bottomSheetButtonWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(websiteIntent.getData() != null) {
                    startActivity(websiteIntent);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_link_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up navigation drawer.
        baseLayout = findViewById(R.id.base_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        // TODO: Implement the establishments feature
        // establishmentsSwitch = (Switch) navigationView.getMenu().findItem(R.id.nav_establishments).getActionView();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Open about page.
                if(menuItem.getItemId() == R.id.nav_about) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.base_view, new AboutFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    drawerLayout.closeDrawers();
                } // else if(menuItem.getItemId() == R.id.nav_establishments) {
                //     establishmentsSwitch.toggle();
                // }
                return true;
            }
        });
        // TODO: Set up establishments feature
        // Set up establishments switch.
        // establishmentsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //     @Override
        //     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //         if(isChecked) {
        //             //
        //         } else {
        //             //
        //         }
        //     }
        // });

        // Set up floating action button.
        floatingActionButton = findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(marker == null) {
                    noMarkerDialog.show();
                } else {
                    searchTypeDialog.show();
                }
            }
        });

        // Set up no marker dialog.
        noMarkerDialog = new AlertDialog.Builder(MapsActivity.this, R.style.AlertDialog).create();
        noMarkerDialog.setTitle(getString(R.string.no_marker_dialog_title));
        noMarkerDialog.setMessage(getString(R.string.no_marker_dialog_message));
        noMarkerDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.no_marker_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Set up nearest station search type dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this, R.style.AlertDialog);
        builder.setTitle(getString(R.string.search_type_dialog_title));
        builder.setItems(R.array.search_type_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                findNearestStation(which);
            }
        });
        searchTypeDialog = builder.create();

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
        bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
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
                removeMarker();
            }
        });

        try {
            // Add NPC boundaries to map.
            npcLayer = new KmlLayer(this.googleMap, R.raw.singapore_police_force_npc_boundary_kml, getApplicationContext());
            npcLayer.addLayerToMap();
            // Set a listener for geometry clicked events.
            npcLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                @Override
                public void onFeatureClick(Feature feature) {
                    removeMarker();
                    if(feature != null) {
                        updateBottomSheetFromBoundariesKmlId(feature.getProperty("name"));
                    }
                }
            });
            // Create a KmlLayer for use in finding closest police station.
            establishmentsLayer = new KmlLayer(this.googleMap, R.raw.singapore_police_force_establishments_kml, getApplicationContext());
            establishmentsLayer.addLayerToMap(); // Necessary to add and remove the KmlLayer so that the containers get loaded.
            establishmentsLayer.removeLayerFromMap();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if(bottomSheetBehaviour.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED) {
            bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
        } else if(bottomSheetBehaviour.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT) {
            bottomSheetBehaviour.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        } else {
            moveTaskToBack(true);
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
        if(googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(myPlace.getViewport(),
                    getApplicationContext().getResources().getDisplayMetrics().widthPixels,
                    getApplicationContext().getResources().getDisplayMetrics().heightPixels,
                    0));
            addMarker(myPlace.getLatLng(), myPlace.getName().toString());
        }
    }

    private void addMarker(LatLng pos, String name) {
        removeMarker();
        marker = googleMap.addMarker(new MarkerOptions().position(pos)
                .title(name));
        marker.showInfoWindow();
    }

    private void removeMarker() {
        if(marker != null) {
            marker.remove();
            marker = null;
        }
    }

    private void findNearestStation(int searchMode) {
        // Calculate the distance of each NPC and/ or NPP from the marker.
        final ArrayList<EstablishmentDistance> list = new ArrayList<>();
        LatLng markerLatLng = marker.getPosition();
        for(KmlContainer container : establishmentsLayer.getContainers()) {
            for(KmlContainer nestedContainer : container.getContainers()) {
                for (KmlPlacemark placemark : nestedContainer.getPlacemarks()) {
                    KmlPlacemarkProperties placemarkProperties = spfEstablishments.getKmlPlacemark(placemark.getProperty("name"));
                    // Filter out establishments that are not NPCs or NPPs.
                    if(placemarkProperties.getProperty(EST_NAME).contains("Neighbourhood")) {
                        // Filter out NPPs if only searching for NPCs.
                        if(searchMode == 0 && placemarkProperties.getProperty(EST_NAME).contains("Post")) {
                            continue;
                        }
                        LatLng placemarkLatLng = ((KmlPoint) placemark.getGeometry()).getGeometryObject();
                        list.add(new EstablishmentDistance(placemark.getProperty("name"),
                                spfEstablishments.getKmlPlacemark(placemark.getProperty("name")).getProperty(EST_NAME),
                                SphericalUtil.computeDistanceBetween(markerLatLng, placemarkLatLng)));
                    }
                }
            }
        }
        Collections.sort(list);
        // Create dialog showing the list of nearest stations.
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this, R.style.AlertDialog);
        builder.setAdapter(new EstablishmentAdapter(getApplicationContext(), R.layout.establishment_adapter_view,
                list), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateBottomSheetFromEstablishmentsKmlId(list.get(which).getEstablishmentKmlId());
            }
        });
        builder.setTitle(getString(R.string.search_results_dialog_title));
        builder.show();
    }

    private void showKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Get Establishments KML ID before calling updateBottomSheetFromEstablishmentsKmlId().
    private void updateBottomSheetFromBoundariesKmlId(String boundariesKmlId) {
        String establishmentsKmlId = null;
        // Find the relevant entry in the Establishments KML file.
        for(Map.Entry<String, KmlPlacemarkProperties> entry : spfEstablishments.getKmlPlacemarks().entrySet()) {
            if(entry.getValue().hasProperty(EST_NAME) &&
                    entry.getValue().getProperty(EST_NAME).equals(npcBoundaries.getKmlPlacemark(boundariesKmlId).getProperty(NPC_NAME)
                            + " " + getString(R.string.neighbourhood_police_centre))) {
                establishmentsKmlId = entry.getKey();
                break;
            }
        }
        if(establishmentsKmlId != null) {
            updateBottomSheetFromEstablishmentsKmlId(establishmentsKmlId);
        }
    }

    // Updates bottom sheet dynamic information.
    private void updateBottomSheetFromEstablishmentsKmlId(String establishmentsKmlId) {
        KmlPlacemarkProperties placemark = spfEstablishments.getKmlPlacemark(establishmentsKmlId);
        // Update image.
        String imageUrl = null;
        if(placemark.hasProperty(EST_FB_ID)) {
            imageUrl = getFacebookProfileImageUrl(placemark.getProperty(EST_FB_ID));
        }
        updateBottomSheetImage(imageUrl);
        // Update name.
        String name = placemark.getProperty(EST_NAME);
        if(name.contains(getString(R.string.neighbourhood_police_centre))) {
            name = name.replace(" " + getString(R.string.neighbourhood_police_centre), "");
            bottomSheetHeaderSubtext.setText(getString(R.string.neighbourhood_police_centre));
            mergedAppBarLayoutBehaviour.setToolbarTitle(name + " " + getString(R.string.neighbourhood_police_centre_abbreviation));
        } else if(name.contains(getString(R.string.neighbourhood_police_post))) {
            name = name.replace(" " + getString(R.string.neighbourhood_police_post), "");
            bottomSheetHeaderSubtext.setText(getString(R.string.neighbourhood_police_post));
            mergedAppBarLayoutBehaviour.setToolbarTitle(name + " " + getString(R.string.neighbourhood_police_post_abbreviation));
        }
        bottomSheetHeader.setText(name);
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
        if(bottomSheetHidden) {
            showBottomSheet();
        }
        // Update website link.
        if(placemark.hasProperty(EST_FB_ID)) {
            bottomSheetWebsite.setText(getString(R.string.facebook_url_prefix) + placemark.getProperty(EST_FB_ID));
            websiteIntent.setData(Uri.parse("https://www.facebook.com/" + placemark.getProperty(EST_FB_ID)));
        } else {
            bottomSheetWebsite.setText("-");
            websiteIntent.setData(null);
        }
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

    private String getFacebookProfileImageUrl(String id) {
        return "https://graph.facebook.com/" + id + "/picture?type=large&width=720&height=720";
    }

    private void updateBottomSheetImage(String url) {
        Picasso.get().load(url)
                .placeholder(R.drawable.spf_crest)
                .into(bottomSheetImage);
    }

    // Finds the Placemark which contains the point specified.
    private void findKmlPlacemark(LatLng point) {
        for(KmlContainer container : npcLayer.getContainers()) {
            for(KmlContainer nestedContainer : container.getContainers()) {
                for (KmlPlacemark placemark : nestedContainer.getPlacemarks()) {
                    if (PolyUtil.containsLocation(point, ((KmlPolygon) placemark.getGeometry()).getOuterBoundaryCoordinates(), true)) {
                        updateBottomSheetFromBoundariesKmlId(placemark.getProperty("name"));
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
