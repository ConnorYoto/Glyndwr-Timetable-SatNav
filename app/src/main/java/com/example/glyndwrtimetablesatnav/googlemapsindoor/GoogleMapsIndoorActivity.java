package com.example.glyndwrtimetablesatnav.googlemapsindoor;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.IndoorLevel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;

import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.MapTypes;

import java.util.HashMap;
import java.util.Map;

@MapTypes(description = R.string.googleMapsIndoorDescription)
public class GoogleMapsIndoorActivity extends FragmentActivity implements IALocationListener, GoogleMap.OnIndoorStateChangeListener, OnMapReadyCallback
{
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Circle mCircle;
    private Marker mMarker;
    private IALocationManager mIALocationManager;
    private GoogleMapsFloorLevelMatcher mFloorLevelMatcher;
    private IndoorLevel mLastAutoActivatedLevel;

    private static final int ACTIVE_LEVEL_BLUE_DOT_COLOR = 0x201681FB;
    private static final int OTHER_LEVEL_BLUE_DOT_COLOR = 0x30808080;

    /**
     * Matches Google Maps IndoorLevels to IndoorAtlas floor plans.
     * The IndoorAtlas floor plan name should match the IndoorLevel short name
     * in Google Maps for this to work reliably. Uses floor number matching as
     * a fallback
     */
    private static class GoogleMapsFloorLevelMatcher
    {
        /** Currently focused Google Maps Building */
        private IndoorBuilding mBuilding;

        /** Last detected IndoorAtlas location */
        private IALocation mLastLocation;

        /**
         * Mapping from floor name (e.g., "E", "M1", "1", "2") to
         * Google Maps IndoorLevel in the current building
         */
        private Map<String, IndoorLevel> mNameToLevel;

        /** Set current Google Maps building */
        void setBuilding(IndoorBuilding building)
        {
            mBuilding = building;
            mNameToLevel = new HashMap<>();
            for (IndoorLevel level : building.getLevels())
            {
                mNameToLevel.put(level.getShortName(), level);
            }   //  for (IndoorLevel level : building.getLevels())
        }   //  void setBuilding(IndoorBuilding building)

        /** Set current IndoorAtlas location */
        void setIALocation(IALocation location)
        {
            mLastLocation = location;
        }   //  void setIALocation(IALocation location)

        /** Is the user on the floor level that is active/visible in Google Maps */
        boolean isIALocationOnActiveLevel()
        {
            IndoorLevel iaLocationLevel = findIALocationLevel();
            return iaLocationLevel != null && iaLocationLevel.equals(activeGoogleMapsLevel());
        }   //  boolean isIALocationOnActiveLevel()

        /**
         * Get the IndoorLevel currently active/visible in Google Maps. This can be
         * changed by the user using the floor selector UI component in Google Maps
         */
        private IndoorLevel activeGoogleMapsLevel()
        {
            if (mBuilding == null) return null;
            return mBuilding.getLevels().get(mBuilding.getActiveLevelIndex());
        }   //  private IndoorLevel activeGoogleMapsLevel()

        /** Find out on which Google Maps IndoorLevel the current IALocation is */
        private IndoorLevel findIALocationLevel()
        {
            if (mLastLocation == null || !mLastLocation.hasFloorLevel() || mBuilding == null)
            {
                return null;
            }   // if (mLastLocation == null || !mLastLocation.hasFloorLevel() || mBuilding == null)
            // first try to match floor plan name to IndoorLevel short name
            IARegion region = mLastLocation.getRegion();
            if (region != null)
            {
                if (mNameToLevel.containsKey(region.getName()))
                {
                    return mNameToLevel.get(region.getName());
                }   //  if (mNameToLevel.containsKey(region.getName()))
            }   //if (region != null)
            // as a fall back, try to find an IndoorLevel whose short name matches the
            // floor level (number) in the IALocation
            return mNameToLevel.get("" + mLastLocation.getFloorLevel());
        }   //  private IndoorLevel findIALocationLevel()
    }   //  private static class GoogleMapsFloorLevelMatcher

    private void showBlueDot(LatLng center, double accuracyRadius, double bearing)
    {
        if (mCircle == null)
        {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null)
            {
                mCircle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(accuracyRadius)
                    .fillColor(ACTIVE_LEVEL_BLUE_DOT_COLOR)
                    .strokeColor(0x00000000)
                    .zIndex(1.0f)
                    .visible(true));
                mMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                    .anchor(0.5f, 0.5f)
                    .rotation((float)bearing)
                    .flat(true));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 17.0f));
            }   //  if (mMap != null)
        }   //  if (mCircle == null)
        else
        {
            mCircle.setCenter(center);
            mCircle.setRadius(accuracyRadius);
            mMarker.setPosition(center);
            mMarker.setRotation((float)bearing);
        }   //  else
    }   //  private void showBlueDot(LatLng center, double accuracyRadius, double bearing)

    private void updateLocationCircleColor()
    {
        // If the user is on the floor that is visible on Google Maps, show a blue circle.
        // Otherwise show a gray circle
        if (mCircle != null)
        {
            if (mFloorLevelMatcher.isIALocationOnActiveLevel())
            {
                mCircle.setFillColor(ACTIVE_LEVEL_BLUE_DOT_COLOR);
                mMarker.setVisible(true);
            }   //  if (mFloorLevelMatcher.isIALocationOnActiveLevel())
            else
            {
                mCircle.setFillColor(OTHER_LEVEL_BLUE_DOT_COLOR);
                mMarker.setVisible(false);
            }   //  else
        }   //  if (mCircle != null)
    }   //  private void updateLocationCircleColor()

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mIALocationManager = IALocationManager.create(this);
        mFloorLevelMatcher = new GoogleMapsFloorLevelMatcher();
        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }   //  protected void onCreate(Bundle savedInstanceState)

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mIALocationManager.destroy();
    }   //  protected void onDestroy()

    @Override
    protected void onResume()
    {
        super.onResume();
        // enable indoor-outdoor mode, required since SDK 3.2
        mIALocationManager.lockIndoors(false);
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), this);
        if (mMap != null)
        {
            mMap.setOnIndoorStateChangeListener(this);
        }   //  if (mMap != null)
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mIALocationManager != null)
        {
            mIALocationManager.removeLocationUpdates(this);
        }   //  if (mIALocationManager != null)
        if (mMap != null)
        {
            mMap.setOnIndoorStateChangeListener(null);
        }   //  if (mMap != null)
    }   //  protected void onPause()

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setOnIndoorStateChangeListener(this);
    }   //  public void onMapReady(GoogleMap googleMap)

    @Override
    public void onLocationChanged(IALocation location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        showBlueDot(latLng, location.getAccuracy(), location.getBearing());
        mFloorLevelMatcher.setIALocation(location);
        updateLocationCircleColor();

        // When the IndoorAtlas SDK detects a floor change, view the corresponding level in
        // Google Maps, but do not jump back to that level on each update so that the user can
        // view other levels in using the Google Maps floor selector UI too
        IndoorLevel iaLocationLevel = mFloorLevelMatcher.findIALocationLevel();
        if (iaLocationLevel != null && !iaLocationLevel.equals(mLastAutoActivatedLevel))
        {
            mLastAutoActivatedLevel = iaLocationLevel;
            mLastAutoActivatedLevel.activate();
        }   //  if (iaLocationLevel != null && !iaLocationLevel.equals(mLastAutoActivatedLevel))
    }   //  public void onLocationChanged(IALocation location)

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }   //  public void onStatusChanged(String provider, int status, Bundle extras)

    @Override
    public void onIndoorBuildingFocused()
    {

    }   //  public void onIndoorBuildingFocused()

    @Override
    public void onIndoorLevelActivated(IndoorBuilding indoorBuilding)
    {
        mFloorLevelMatcher.setBuilding(indoorBuilding);
        updateLocationCircleColor();
    }   //  public void onIndoorLevelActivated(IndoorBuilding indoorBuilding)
}   //  public class GoogleMapsIndoorActivity extends FragmentActivity implements IALocationListener, GoogleMap.OnIndoorStateChangeListener, OnMapReadyCallback

