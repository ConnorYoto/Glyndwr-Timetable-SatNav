package com.example.glyndwrtimetablesatnav.simple;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.utils.ExampleUtils;
import com.example.glyndwrtimetablesatnav.MapTypes;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;

import java.util.Locale;

@MapTypes(description = R.string.simpleDescription)
public class SimpleActivity extends AppCompatActivity implements IALocationListener, IARegion.Listener
{
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
        setContentView(R.layout.activity_simple);
        mLog = findViewById(R.id.text);
        mScrollView = findViewById(R.id.scroller);
        mLocationManager = IALocationManager.create(this);
        // Register long click for sharing traceId
        ExampleUtils.shareTraceId(mLog, SimpleActivity.this, mLocationManager);
    }   //  public void onCreate(Bundle savedInstanceState)

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mLocationManager.destroy();
        //  Clean up after ourselves
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

    public void requestUpdates(View view)
    {
        setLocationRequestOptions();
    }   //  public void requestUpdates(View view)

    public void removeUpdates(View view)
    {
        log("removeLocationUpdates");
        mLocationManager.removeLocationUpdates(this);
    }   //  public void removeUpdates(View view)

    @Override
    public void onLocationChanged(IALocation location)
    {
        log(String.format(Locale.US, "%f,%f, accuracy: %.2f, certainty: %.2f", location.getLatitude(), location.getLongitude(), location.getAccuracy(),
                location.getFloorCertainty()));
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
    {   //  Makes IARegion getType human readable
        switch (type) {
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

    private void setLocationRequestOptions()
    {
        //  Location Request Dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        final View dialogLayout = inflater.inflate(R.layout.location_request_dialog, null);
        final EditText fastestInterval = dialogLayout.findViewById(R.id.edit_text_interval);
        final EditText shortestDisplacement = dialogLayout.findViewById(R.id.edit_text_displacement);
        if (mFastestInterval != -1L)
        {
            fastestInterval.setText(String.valueOf(mFastestInterval));
        }
        if (mShortestDisplacement != -1f)
        {
            shortestDisplacement.setText(String.valueOf(mShortestDisplacement));
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialogRequestOptionsTitle).setView(dialogLayout).setCancelable(true)
                .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mRequestStartTime = SystemClock.elapsedRealtime();
                        IALocationRequest request = IALocationRequest.create();
                        String fastestIntervalInput = fastestInterval.getText().toString();
                        String shortestDisplacementInput = shortestDisplacement.getText().toString();
                        if (!fastestIntervalInput.isEmpty())
                        {
                            mFastestInterval = Long.parseLong(fastestIntervalInput);
                            request.setFastestInterval(mFastestInterval);
                        }
                        else
                        {
                            mFastestInterval = -1L;
                        }
                        if (!shortestDisplacementInput.isEmpty())
                        {
                            mShortestDisplacement = Float.parseFloat(shortestDisplacementInput);
                            request.setSmallestDisplacement(mShortestDisplacement);
                        }
                        else
                        {
                            mShortestDisplacement = -1f;
                        }
                        mLocationManager.removeLocationUpdates(SimpleActivity.this);
                        mLocationManager.requestLocationUpdates(request, SimpleActivity.this);
                        log("requestLocationUpdates");
                    }
                })
                .setNegativeButton(R.string.buttonCancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }   //  private void setLocationRequestOptions()

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
}
