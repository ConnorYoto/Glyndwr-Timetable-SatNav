package com.example.glyndwrtimetablesatnav.mapsoverlay;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
//import android.support.design.widget.Snackbar;
import androidx.fragment.app.FragmentActivity;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

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
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.example.glyndwrtimetablesatnav.R;
//import com.indooratlas.android.sdk.examples.R;
import com.example.glyndwrtimetablesatnav.SdkExample;
//import com.indooratlas.android.sdk.examples.SdkExample;
import com.example.glyndwrtimetablesatnav.utils.ExampleUtils;
//import com.indooratlas.android.sdk.examples.utils.ExampleUtils;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

@SdkExample(description = R.string.example_googlemaps_overlay_description)
public class MapsOverlayActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback
{
    private static final String TAG = "IndoorAtlasExample";

    /* used to decide when bitmap should be downscaled */
    private static final int MAX_DIMENSION = 2048;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Circle mCircle;
    private Marker mMarker;
    private IARegion mOverlayFloorPlan = null;
    private GroundOverlay mGroundOverlay = null;
    private IALocationManager mIALocationManager;
    private Target mLoadTarget;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location
    private boolean mShowIndoorLocation = false;

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
                        .fillColor(0x201681FB)
                        .strokeColor(0x500A78DD)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                        .anchor(0.5f, 0.5f)
                        .rotation((float)bearing)
                        .flat(true));
            }   //  if (mMap != null)
        }   //  if (mCircle == null)
        else
        {
            // move existing markers position to received location
            mCircle.setCenter(center);
            mCircle.setRadius(accuracyRadius);
            mMarker.setPosition(center);
            mMarker.setRotation((float)bearing);
        }   //  else
    }   //  private void showBlueDot(LatLng center, double accuracyRadius, double bearing)

    //Listener that handles location change events.
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
            }   //  if (mMap == null)

            final LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

            if (mShowIndoorLocation)
            {
                showBlueDot(center, location.getAccuracy(), location.getBearing());
            }   //  if (mShowIndoorLocation)

            // our camera position needs updating if location has significantly changed
            if (mCameraPositionNeedsUpdating)
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 17.5f));
                mCameraPositionNeedsUpdating = false;
            }   //  if (mCameraPositionNeedsUpdating)
        }   //  public void onLocationChanged(IALocation location)
    };  //  private IALocationListener mListener = new IALocationListenerSupport()

    //Listener that changes overlay if needed
    private IARegion.Listener mRegionListener = new IARegion.Listener()
    {
        @Override
        public void onEnterRegion(IARegion region)
        {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
            {
                final String newId = region.getId();
                // Are we entering a new floor plan or coming back the floor plan we just left?
                if (mGroundOverlay == null || !region.equals(mOverlayFloorPlan))
                {
                    mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera
                    if (mGroundOverlay != null)
                    {
                        mGroundOverlay.remove();
                        mGroundOverlay = null;
                    }   //  if (mGroundOverlay != null)
                    mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                    fetchFloorPlanBitmap(region.getFloorPlan());
                }   //  if (mGroundOverlay == null || !region.equals(mOverlayFloorPlan))
                else
                {
                    mGroundOverlay.setTransparency(0.0f);
                }   //  else

                mShowIndoorLocation = true;
                showInfo("Showing IndoorAtlas SDK\'s location output");
            }   //  if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
            showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE ? "VENUE " : "FLOOR_PLAN ") + region.getId());
        }   //  public void onEnterRegion(IARegion region)

        @Override
        public void onExitRegion(IARegion region)
        {
            if (mGroundOverlay != null)
            {
                // Indicate we left this floor plan but leave it there for reference
                // If we enter another floor plan, this one will be removed and another one loaded
                mGroundOverlay.setTransparency(0.5f);
            }   //  if (mGroundOverlay != null)
            mShowIndoorLocation = false;
            showInfo("Exit " + (region.getType() == IARegion.TYPE_VENUE ? "VENUE " : "FLOOR_PLAN ") + region.getId());
        }   //  public void onExitRegion(IARegion region)
    };  //  private IARegion.Listener mRegionListener = new IARegion.Listener()

    @Override
    public void onLocationChanged(Location location)
    {
        if (!mShowIndoorLocation)
        {
            Log.d(TAG, "new LocationService location received with coordinates: " + location.getLatitude()
                    + "," + location.getLongitude());

            showBlueDot(new LatLng(location.getLatitude(), location.getLongitude()), location.getAccuracy(), location.getBearing());
        }   //  if (!mShowIndoorLocation)
    }   //  public void onLocationChanged(Location location)

    @Override
    public void onProviderDisabled(String provider)
    {

    }   //  public void onProviderDisabled(String provider)

    @Override
    public void onProviderEnabled(String provider)
    {

    }   //  public void onProviderEnabled(String provider)

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }   //  public void onStatusChanged(String provider, int status, Bundle extras)

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

        // enable indoor-outdoor mode, required since SDK 3.2
        mIALocationManager.lockIndoors(false);

        IALocationRequest locReq = IALocationRequest.create();

        // default mode
        locReq.setPriority(IALocationRequest.PRIORITY_HIGH_ACCURACY);

        // --- start receiving location updates & monitor region changes
        mIALocationManager.requestLocationUpdates(locReq, mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        super.onPause();
        // unregister location & region changes
        mIALocationManager.removeLocationUpdates(mListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
    }   //  protected void onPause()

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        // do not show Google's outdoor location
        mMap.setMyLocationEnabled(false);

        // Setup long click to share the traceId
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng latLng)
            {
                ExampleUtils.shareText(MapsOverlayActivity.this,
                        mIALocationManager.getExtraInfo().traceId, "traceId");
            }   //  public void onMapLongClick(LatLng latLng)
        }); //  mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
    }   //  public void onMapReady(GoogleMap googleMap)

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
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions().image(bitmapDescriptor).zIndex(0.0f)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters()).bearing(floorPlan.getBearing());

            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);
        }   //  if (mMap != null)
    }   //  private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap)

    //Download floor plan using Picasso library.
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan)
    {
        final String url = floorPlan.getUrl();
        mLoadTarget = new Target()
        {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
            {
                Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x"
                        + bitmap.getHeight());
                if (mOverlayFloorPlan != null && floorPlan.getId().equals(mOverlayFloorPlan.getId()))
                {
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
    }

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
    }   //   private void showInfo(String text)
}   //  public class MapsOverlayActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback
