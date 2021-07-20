package com.example.glyndwrtimetablesatnav.imageview;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
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

@SdkExample(description = R.string.example_imageview_description)
public class ImageViewActivity extends FragmentActivity
{
    private static final String TAG = "IndoorAtlasExample";

    /* used to decide when bitmap should be downscaled */
    private static final int MAX_DIMENSION = 2048;

    // blue dot radius in meters
    private static final float dotRadius = 1.0f;

    private IALocationManager mIALocationManager;
    private IAFloorPlan mFloorPlan;
    private BlueDotView mImageView;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private Target mLoadTarget;

    private IALocationListener mLocationListener = new IALocationListenerSupport()
    {
        @Override
        public void onLocationChanged(IALocation location)
        {
            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude());
            if (mImageView != null && mImageView.isReady())
            {
                IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
                PointF point = mFloorPlan.coordinateToPoint(latLng);
                mImageView.setDotCenter(point);
                mImageView.setUncertaintyRadius(mFloorPlan.getMetersToPixels() * location.getAccuracy());
                mImageView.postInvalidate();
            }   //  if (mImageView != null && mImageView.isReady())
        }   //  public void onLocationChanged(IALocation location)
    };  //  private IALocationListener mLocationListener = new IALocationListenerSupport()

    private IAOrientationListener mOrientationListener = new IAOrientationListener()
    {
        @Override
        public void onHeadingChanged(long timestamp, double heading)
        {
            if (mFloorPlan != null)
            {
                mImageView.setHeading(heading - mFloorPlan.getBearing());
            }   //  if (mFloorPlan != null)
        }   //  public void onHeadingChanged(long timestamp, double heading)

        @Override
        public void onOrientationChange(long l, double[] doubles)
        {
            // No-op
        }   //  public void onOrientationChange(long l, double[] doubles)
    };  //  private IAOrientationListener mOrientationListener = new IAOrientationListener()

    private IARegion.Listener mRegionListener = new IARegion.Listener()
    {
        @Override
        public void onEnterRegion(IARegion region)
        {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
            {
                String id = region.getId();
                Log.d(TAG, "floorPlan changed to " + id);
                Toast.makeText(ImageViewActivity.this, id, Toast.LENGTH_SHORT).show();
                fetchFloorPlanBitmap(region.getFloorPlan());
            }   //  if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
        }   //  public void onEnterRegion(IARegion region)

        @Override
        public void onExitRegion(IARegion region)
        {
            // leaving a previously entered region
        }   //  public void onExitRegion(IARegion region)
    };  //  private IARegion.Listener mRegionListener = new IARegion.Listener()

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        mImageView = findViewById(R.id.imageView);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mIALocationManager = IALocationManager.create(this);

        // Setup long click listener for sharing traceId
        ExampleUtils.shareTraceId(findViewById(R.id.imageView), ImageViewActivity.this, mIALocationManager);
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
        // starts receiving location updates
        ///mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mLocationListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        IAOrientationRequest orientationRequest = new IAOrientationRequest(10f, 10f);
        mIALocationManager.registerOrientationListener(orientationRequest, mOrientationListener);

        IALocationRequest locReq = IALocationRequest.create();

        // default mode
        locReq.setPriority(IALocationRequest.PRIORITY_HIGH_ACCURACY);

        // --- start receiving location updates & monitor region changes
        mIALocationManager.requestLocationUpdates(locReq, mLocationListener);
        mIALocationManager.registerRegionListener(mRegionListener);
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        super.onPause();
        mIALocationManager.removeLocationUpdates(mLocationListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        mIALocationManager.unregisterOrientationListener(mOrientationListener);
    }   //  protected void onPause()

    //  Methods for fetching bitmap image
    private void showFloorPlanImage(Bitmap bitmap)
    {
        mImageView.setDotRadius(mFloorPlan.getMetersToPixels() * dotRadius);
        mImageView.setImage(ImageSource.bitmap(bitmap));
    }   //  private void showFloorPlanImage(Bitmap bitmap)

    //Download floor plan using Picasso library
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan)
    {
        mFloorPlan = floorPlan;
        final String url = floorPlan.getUrl();
        mLoadTarget = new Target()
        {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
            {
                Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                if (mFloorPlan != null && floorPlan.getId().equals(mFloorPlan.getId()))
                {
                    showFloorPlanImage(bitmap);
                }   //  if (mFloorPlan != null && floorPlan.getId().equals(mFloorPlan.getId()))
            }   //  public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable)
            {
                // N/A
            }   //  public void onPrepareLoad(Drawable placeHolderDrawable)

            @Override
            public void onBitmapFailed(Drawable placeHolderDrawable)
            {
                Toast.makeText(ImageViewActivity.this, "Failed to load bitmap", Toast.LENGTH_SHORT).show();
                mFloorPlan = null;
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
}   //  public class ImageViewActivity extends FragmentActivity

