package com.example.glyndwrtimetablesatnav.wayfinding;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.glyndwrtimetablesatnav.MainActivity;
import com.google.android.material.snackbar.Snackbar;
//import android.support.design.widget.Snackbar;
import androidx.fragment.app.FragmentActivity;
//import android.support.v4.app.FragmentActivity;
//import com.indooratlas.android.sdk.examples.ListExamplesActivity;
import com.example.glyndwrtimetablesatnav.R;
//import com.indooratlas.android.sdk.examples.R;
import com.example.glyndwrtimetablesatnav.SdkExample;
//import com.indooratlas.android.sdk.examples.SdkExample;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
import com.indooratlas.android.sdk.IAPOI;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.IARoute;
import com.indooratlas.android.sdk.IAWayfindingListener;
import com.indooratlas.android.sdk.IAWayfindingRequest;

import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAVenue;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

@SdkExample(description = R.string.example_wayfinding_description)
public class WayfindingOverlayActivity extends FragmentActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback
{
    private static final String TAG = "IndoorAtlasExample";

    /* used to decide when bitmap should be downscaled */
    private static final int MAX_DIMENSION = 2048;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Circle mCircle;
    private IARegion mOverlayFloorPlan = null;
    private GroundOverlay mGroundOverlay = null;
    private IALocationManager mIALocationManager;
    private Target mLoadTarget;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location
    private Marker mDestinationMarker;
    private Marker mHeadingMarker;
    private IAVenue mVenue;
    private List<Marker> mPoIMarkers = new ArrayList<>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private IARoute mCurrentRoute;

    private IAWayfindingRequest mWayfindingDestination;
    private IAWayfindingListener mWayfindingListener = new IAWayfindingListener()
    {
        @Override
        public void onWayfindingUpdate(IARoute route)
        {
            mCurrentRoute = route;
            if (hasArrivedToDestination(route))
            {
                // stop wayfinding
                showInfo("You're there!");
                mCurrentRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
            }
            updateRouteVisualization();
        }   //  public void onWayfindingUpdate(IARoute route)
    };  //  private IAWayfindingListener mWayfindingListener = new IAWayfindingListener()

    private IAOrientationListener mOrientationListener = new IAOrientationListener()
    {
        @Override
        public void onHeadingChanged(long timestamp, double heading)
        {
            updateHeading(heading);
        }   //  public void onHeadingChanged(long timestamp, double heading)

        @Override
        public void onOrientationChange(long timestamp, double[] quaternion)
        {
            // we do not need full device orientation in this example, just the heading
        }   //  public void onOrientationChange(long timestamp, double[] quaternion)
    };  //  private IAOrientationListener mOrientationListener = new IAOrientationListener()

    private int mFloor;

    private void showLocationCircle(LatLng center, double accuracyRadius)
    {
        if (mCircle == null)
        {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null)
            {
                mCircle = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(accuracyRadius)
                        .fillColor(0x201681FB)
                        .strokeColor(0x500A78DD)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mHeadingMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }   // if (mMap != null)
        }   //  if (mCircle == null)
        else
        {
            // move existing markers position to received location
            mCircle.setCenter(center);
            mHeadingMarker.setPosition(center);
            mCircle.setRadius(accuracyRadius);
        }   //   else
    }   //  private void showLocationCircle(LatLng center, double accuracyRadius)

    private void updateHeading(double heading)
    {
        if (mHeadingMarker != null)
        {
            mHeadingMarker.setRotation((float)heading);
        }
    }   // private void updateHeading(double heading)

    //Listener that handles location change events
    private IALocationListener mListener = new IALocationListenerSupport()
    {
        // Location changed, move marker and camera position.
        @Override
        public void onLocationChanged(IALocation location)
        {
            Log.d(TAG, "new location received with coordinates: " + location.getLatitude() + "," + location.getLongitude());
            if (mMap == null)
            {
                // location received before map is initialized, ignoring update here
                return;
            }   // if (mMap == null)
            final LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
            final int newFloor = location.getFloorLevel();
            if (mFloor != newFloor)
            {
                updateRouteVisualization();
            }   //  if (mFloor != newFloor)
            mFloor = newFloor;
            showLocationCircle(center, location.getAccuracy());
            // our camera position needs updating if location has significantly changed
            if (mCameraPositionNeedsUpdating)
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 17.5f));
                mCameraPositionNeedsUpdating = false;
            }      //   if (mCameraPositionNeedsUpdating)
        }   //  public void onLocationChanged(IALocation location)
    };  //  private IALocationListener mListener = new IALocationListenerSupport()

    // Listener that changes overlay if needed
    private IARegion.Listener mRegionListener = new IARegion.Listener()
    {
        @Override
        public void onEnterRegion(final IARegion region)
        {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
            {
                Log.d(TAG, "enter floor plan " + region.getId());
                mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera
                if (mGroundOverlay != null)
                {
                    mGroundOverlay.remove();
                    mGroundOverlay = null;
                }   //  if (mGroundOverlay != null)
                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                fetchFloorPlanBitmap(region.getFloorPlan());
                setupPoIs(mVenue.getPOIs(), region.getFloorPlan().getFloorLevel());
            } //    if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
            else if (region.getType() == IARegion.TYPE_VENUE)
            {
                mVenue = region.getVenue();
            }   //  else if (region.getType() == IARegion.TYPE_VENUE)
        }   //  public void onEnterRegion(final IARegion region)
        @Override
        public void onExitRegion(IARegion region)
        {

        }   //  public void onExitRegion(IARegion region)
    };  //  private IARegion.Listener mRegionListener = new IARegion.Listener()

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);
        // instantiate IALocationManager
        mIALocationManager = IALocationManager.create(this);
        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }   //  protected void onCreate(Bundle savedInstanceState)

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // remember to clean up after ourselves
        mIALocationManager.destroy();
    }   //  protected void onDestroy()

    @Override
    protected void onResume()
    {
        super.onResume();
        // start receiving location updates & monitor region changes
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        mIALocationManager.registerOrientationListener(
                // update if heading changes by 1 degrees or more
                new IAOrientationRequest(1, 0),
                mOrientationListener);
        if (mWayfindingDestination != null)
        {
            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
        }   //  if (mWayfindingDestination != null)
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        super.onPause();
        // unregister location & region changes
        mIALocationManager.removeLocationUpdates(mListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        mIALocationManager.unregisterOrientationListener(mOrientationListener);

        if (mWayfindingDestination != null)
        {
            mIALocationManager.removeWayfindingUpdates();
        }   //  if (mWayfindingDestination != null)
    }   //  protected void onPause()

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        if (!MainActivity.checkLocationPermissions(this))
        {
            finish(); // Handle permission asking in MapActivity
            return;
        }   //  if (!MapActivity.checkLocationPermissions(this))
        // do not show Google's outdoor location
        mMap.setMyLocationEnabled(false);
        mMap.setOnMapClickListener(this);
        // disable various Google maps UI elements that do not work indoors
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                // ignore clicks to artificial wayfinding target markers
                if (marker == mDestinationMarker) return false;

                setWayfindingTarget(marker.getPosition(), false);
                // do not consume the event so that the popup with marker name is displayed
                return false;
            }   //  public boolean onMarkerClick(Marker marker)
        }); //  mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
    }   //  public void onMapReady(GoogleMap googleMap)

    private void setupPoIs(List<IAPOI> pois, int currentFloorLevel)
    {
        Log.d(TAG, pois.size() + " PoI(s)");
        // remove any existing markers
        for (Marker m : mPoIMarkers)
        {
            m.remove();
        }   //  for (Marker m : mPoIMarkers)
        mPoIMarkers.clear();
        for (IAPOI poi : pois)
        {
            if (poi.getFloor() == currentFloorLevel)
            {
                mPoIMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(poi.getName())
                        .position(new LatLng(poi.getLocation().latitude, poi.getLocation().longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
            }   //  if (poi.getFloor() == currentFloorLevel)
        }   //  for (IAPOI poi : pois)
    }   //  private void setupPoIs(List<IAPOI> pois, int currentFloorLevel)

    //Sets bitmap of floor plan as ground overlay on Google Maps
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap)
    {
        if (mGroundOverlay != null)
        {
            mGroundOverlay.remove();
        }   //  if (mGroundOverlay != null)

        if (mMap != null)
        {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .zIndex(0.0f)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());
            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);
        }   //  if (mMap != null)
    }   //  private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap)

    //Download floor plan using Picasso library.
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan)
    {
        if (floorPlan == null)
        {
            Log.e(TAG, "null floor plan in fetchFloorPlanBitmap");
            return;
        }   //  if (floorPlan == null)

        final String url = floorPlan.getUrl();
        Log.d(TAG, "loading floor plan bitmap from "+url);

        mLoadTarget = new Target()
        {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
            {
                Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                if (mOverlayFloorPlan != null && floorPlan.getId().equals(mOverlayFloorPlan.getId()))
                {
                    Log.d(TAG, "showing overlay");
                    setupGroundOverlay(floorPlan, bitmap);
                }   //  if (mOverlayFloorPlan != null && floorPlan.getId().equals(mOverlayFloorPlan.getId()))
            }   //  public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable)
            {
                // N/A
            }   //  public void onPrepareLoad(Drawable placeHolderDrawable)

            @Override
            public void onBitmapFailed(Drawable placeHolderDrawable)
            {
                showInfo("Failed to load bitmap");
                mOverlayFloorPlan = null;
            }   //  public void onBitmapFailed(Drawable placeHolderDrawable)
        };  //  mLoadTarget = new Target()
        RequestCreator request = Picasso.with(this).load(url);

        final int bitmapWidth = floorPlan.getBitmapWidth();
        final int bitmapHeight = floorPlan.getBitmapHeight();

        if (bitmapHeight > MAX_DIMENSION)
        {
            request.resize(0, MAX_DIMENSION);
        }   //  if (bitmapHeight > MAX_DIMENSION)
        else if (bitmapWidth > MAX_DIMENSION)
        {
            request.resize(MAX_DIMENSION, 0);
        }   //  else if (bitmapWidth > MAX_DIMENSION)
        request.into(mLoadTarget);
    }   //  private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan)

    private void showInfo(String text)
    {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.button_close, new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                snackbar.dismiss();
            }   //  public void onClick(View view)
        }); //  snackbar.setAction(R.string.button_close, new View.OnClickListener()
        snackbar.show();
    }   //  private void showInfo(String text)

    @Override
    public void onMapClick(LatLng point)
    {
        if (mPoIMarkers.isEmpty())
        {
            // if PoIs exist, only allow wayfinding to PoI markers
            setWayfindingTarget(point, true);
        }   //  if (mPoIMarkers.isEmpty())
    }   //  public void onMapClick(LatLng point)

    private void setWayfindingTarget(LatLng point, boolean addMarker)
    {
        if (mMap == null)
        {
            Log.w(TAG, "map not loaded yet");
            return;
        }   //  if (mMap == null)
        mWayfindingDestination = new IAWayfindingRequest.Builder()
                .withFloor(mFloor)
                .withLatitude(point.latitude)
                .withLongitude(point.longitude)
                .build();

        mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
        if (mDestinationMarker != null)
        {
            mDestinationMarker.remove();
            mDestinationMarker = null;
        }   //  if (mDestinationMarker != null)
        if (addMarker)
        {
            mDestinationMarker = mMap.addMarker(new MarkerOptions().position(point)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }   //  if (addMarker)
        Log.d(TAG, "Set destination: (" + mWayfindingDestination.getLatitude() + ", " + mWayfindingDestination.getLongitude() + "), floor=" +
                mWayfindingDestination.getFloor());
    }   //  private void setWayfindingTarget(LatLng point, boolean addMarker)

    private boolean hasArrivedToDestination(IARoute route)
    {
        // empty routes are only returned when there is a problem, for example, missing or disconnected routing graph
        if (route.getLegs().size() == 0)
        {
            return false;
        }   //  if (route.getLegs().size() == 0)

        final double FINISH_THRESHOLD_METERS = 8.0;
        double routeLength = 0;
        for (IARoute.Leg leg : route.getLegs()) routeLength += leg.getLength();
        return routeLength < FINISH_THRESHOLD_METERS;
    }   //  private boolean hasArrivedToDestination(IARoute route)

    //  Clear the visualizations for the wayfinding paths
    private void clearRouteVisualization()
    {
        for (Polyline pl : mPolylines)
        {
            pl.remove();
        }   //  for (Polyline pl : mPolylines)
        mPolylines.clear();
    }   //  private void clearRouteVisualization()

    //Visualize the IndoorAtlas Wayfinding route on top of the Google Maps.
    private void updateRouteVisualization()
    {
        clearRouteVisualization();

        if (mCurrentRoute == null)
        {
            return;
        }   //  if (mCurrentRoute == null)

        for (IARoute.Leg leg : mCurrentRoute.getLegs())
        {
            if (leg.getEdgeIndex() == null)
            {
                // Legs without an edge index are, in practice, the last and first legs of the
                // route. They connect the destination or current location to the routing graph.
                // All other legs travel along the edges of the routing graph.

                // Omitting these "artificial edges" in visualization can improve the aesthetics
                // of the route. Alternatively, they could be visualized with dashed lines.
                continue;
            }   //  if (leg.getEdgeIndex() == null)

            PolylineOptions opt = new PolylineOptions();
            opt.add(new LatLng(leg.getBegin().getLatitude(), leg.getBegin().getLongitude()));
            opt.add(new LatLng(leg.getEnd().getLatitude(), leg.getEnd().getLongitude()));

            // Here wayfinding path in different floor than current location is visualized in
            // a semi-transparent color
            if (leg.getBegin().getFloor() == mFloor && leg.getEnd().getFloor() == mFloor)
            {
                opt.color(0xFF0000FF);
            }   //  if (leg.getBegin().getFloor() == mFloor && leg.getEnd().getFloor() == mFloor)
            else
            {
                opt.color(0x300000FF);
            }   //  else
            mPolylines.add(mMap.addPolyline(opt));
        }   //  for (IARoute.Leg leg : mCurrentRoute.getLegs())
    }   //  private void updateRouteVisualization()
}
