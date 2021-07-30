package com.example.glyndwrtimetablesatnav.lockfloor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.MapTypes;
import com.example.glyndwrtimetablesatnav.utils.ExampleUtils;

import java.util.Locale;

@MapTypes(description = R.string.lockfloorDescription)
public class LockFloorActivity extends AppCompatActivity implements IALocationListener, IARegion.Listener
{   //  Simple example that demonstrates basic interaction with {@link IALocationManager}.
    static final String FASTEST_INTERVAL = "fastestInterval";
    static final String SHORTEST_DISPLACEMENT = "shortestDisplacement";

    private IALocationManager mLocationManager;
    private TextView mLog;
    private ScrollView mScrollView;
    private long mRequestStartTime;

    private long mFastestInterval = -1L;
    private float mShortestDisplacement = -1f;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mFastestInterval = savedInstanceState.getLong(FASTEST_INTERVAL);
            mShortestDisplacement = savedInstanceState.getFloat(SHORTEST_DISPLACEMENT);
        }
        setContentView(R.layout.activity_lockfloor);
        mLog = findViewById(R.id.text);
        mScrollView = findViewById(R.id.scroller);
        mLocationManager = IALocationManager.create(this);
        // Register long click for sharing traceId
        ExampleUtils.shareTraceId(mLog, LockFloorActivity.this, mLocationManager);
        mRequestStartTime = SystemClock.elapsedRealtime();
        IALocationRequest request = IALocationRequest.create();
        mLocationManager.requestLocationUpdates(request, LockFloorActivity.this);
        log("requestLocationUpdates");
    }   //  public void onCreate(Bundle savedInstanceState)

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mLocationManager.destroy();
    }   //  protected void onDestroy()

    @Override
    protected void onResume()
    {
        super.onResume();
        mLocationManager.registerRegionListener(this);
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        super.onPause();
        mLocationManager.unregisterRegionListener(this);
    }   //  protected void onPause()

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_simple, menu);
        return super.onCreateOptionsMenu(menu);
    }   //  public boolean onCreateOptionsMenu(Menu menu)

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int id = item.getItemId();
        switch (id)
        {
            case R.id.action_clear:
                mLog.setText(null);
                return true;
            case R.id.action_share:
                shareLog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }   //  public boolean onOptionsItemSelected(MenuItem item)

    public void lockFloor(View view)
    {
        askFloor();
    }   //  public void lockFloor(View view)

    @Override
    public void onLocationChanged(IALocation location)
    {
        log(String.format(Locale.US, "%f,%f, accuracy: %.2f, certainty: %.2f", location.getLatitude(), location.getLongitude(),
                location.getAccuracy(), location.getFloorCertainty()));
    }   //  public void onLocationChanged(IALocation location)

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        switch (status)
        {
            case IALocationManager.STATUS_AVAILABLE:
                log("onStatusChanged: Available");
                break;
            case IALocationManager.STATUS_LIMITED:
                log("onStatusChanged: Limited");
                break;
            case IALocationManager.STATUS_OUT_OF_SERVICE:
                log("onStatusChanged: Out of service");
                break;
            case IALocationManager.STATUS_TEMPORARILY_UNAVAILABLE:
                log("onStatusChanged: Temporarily unavailable");
        }
    }   //  public void onStatusChanged(String provider, int status, Bundle extras)

    @Override
    public void onEnterRegion(IARegion region)
    {
        log("onEnterRegion: " + regionType(region.getType()) + ", " + region.getId());
    }   //  public void onEnterRegion(IARegion region)

    @Override
    public void onExitRegion(IARegion region)
    {
        log("onExitRegion: " + regionType(region.getType()) + ", " + region.getId());
    }   //  public void onExitRegion(IARegion region)

    private String regionType(int type)
    {   //  Turn {@link IARegion#getType()} to human-readable name
        switch (type)
        {
            case IARegion.TYPE_UNKNOWN:
                return "unknown";
            case IARegion.TYPE_FLOOR_PLAN:
                return "floor plan";
            case IARegion.TYPE_VENUE:
                return "venue";
            default:
                return Integer.toString(type);
        }
    }   //  private String regionType(int type)

    private void log(String msg)
    {   //  Append message into log prefixing with duration since start of location requests.
        double duration = mRequestStartTime != 0 ? (SystemClock.elapsedRealtime() - mRequestStartTime) / 1e3 : 0d;
        mLog.append(String.format(Locale.US, "\n[%06.2f]: %s", duration, msg));
        mScrollView.smoothScrollBy(0, mLog.getBottom());
    }   //  private void log(String msg)

    private void askFloor()
    {
        final int minValue = -10;
        final int maxValue = 10;
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(maxValue - minValue);
        numberPicker.setValue(10);
        numberPicker.setFormatter(new NumberPicker.Formatter()
        {
            @Override
            public String format(int index)
            {
                return Integer.toString(index + minValue);
            }   //  public String format(int index)
        }); //  numberPicker.setFormatter(new NumberPicker.Formatter()

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lock / unlock floor");
        builder.setMessage("Floor number:");
        builder.setView(numberPicker);
        builder.setPositiveButton("Lock floor", new Dialog.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (mLocationManager != null)
                {
                    int number = numberPicker.getValue() + minValue;
                    mLocationManager.lockFloor(number);
                    log("Locking floor " + number);
                }   //  if (mLocationManager != null)
                dialog.cancel();
            }   //  public void onClick(DialogInterface dialog, int which)
        }); //  builder.setPositiveButton("Lock floor", new Dialog.OnClickListener()
        builder.setNeutralButton("Cancel", new Dialog.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }   //  public void onClick(DialogInterface dialog, int which)
        }); //  builder.setNeutralButton("Cancel", new Dialog.OnClickListener()
        builder.setNegativeButton("Unlock", new Dialog.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (mLocationManager != null)
                {
                    mLocationManager.unlockFloor();
                    log("Unlocking floor");
                }   //  if (mLocationManager != null)
                dialog.cancel();
            }   //   public void onClick(DialogInterface dialog, int which)
        }); //  builder.setNegativeButton("Unlock", new Dialog.OnClickListener()
        builder.show();
    }   //  private void askFloor()

    public void unlockIndoors(View view)
    {
        if (mLocationManager != null)
        {
            mLocationManager.lockIndoors(false);
            log("Unlocking indoors (enabling indoor-outdoor mode)");
        }   //  if (mLocationManager != null)
    }   //  public void unlockIndoors(View view)

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putLong(FASTEST_INTERVAL, mFastestInterval);
        savedInstanceState.putFloat(SHORTEST_DISPLACEMENT, mShortestDisplacement);
        super.onSaveInstanceState(savedInstanceState);
    }   //  public void onSaveInstanceState(Bundle savedInstanceState)

    private void shareLog()
    {   //  Share current log content using registered apps.
        Intent sendIntent = new Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, mLog.getText()).setType("text/plain");
        startActivity(sendIntent);
    }   //  private void shareLog()
}   //  public class LockFloorActivity extends AppCompatActivity implements IALocationListener, IARegion.Listener
