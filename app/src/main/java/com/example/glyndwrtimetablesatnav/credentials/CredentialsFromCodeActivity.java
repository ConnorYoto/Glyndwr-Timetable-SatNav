package com.example.glyndwrtimetablesatnav.credentials;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.example.glyndwrtimetablesatnav.MapTypes;
import com.example.glyndwrtimetablesatnav.R;

import java.util.Locale;

@MapTypes(description = R.string.credentials_description)
public class CredentialsFromCodeActivity extends AppCompatActivity implements IALocationListener {

    private IALocationManager mLocationManager;
    private TextView mLog;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //  Interface
        setContentView(R.layout.text_only);
        mLog = (TextView) findViewById(R.id.text);
        //  Variables
        Bundle extras = new Bundle(2);
        extras.putString(IALocationManager.EXTRA_API_KEY, getString(R.string.indooratlas_api_key));
        extras.putString(IALocationManager.EXTRA_API_SECRET, getString(R.string.indooratlas_api_secret));
        //  LocationManager
        mLocationManager = IALocationManager.create(this, extras);
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
        mLocationManager.requestLocationUpdates(IALocationRequest.create(), this);
        //  Resume Location update requests
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        super.onPause();
        mLocationManager.removeLocationUpdates(this);
        // Remove Location update requests whilst paused
    }   //  protected void onPause()

    @Override
    public void onLocationChanged(IALocation location)
    {
        log(String.format(Locale.US, "%f,%f, accuracy: %.2f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
        //  Log location message
    }   // public void onLocationChanged(IALocation location)

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        log("onStatusChanged: " + status);
        // Log status message
    }   //  public void onStatusChanged(String provider, int status, Bundle extras)

    private void log(String msg) {
        mLog.append("\n" + msg);
    }

}
