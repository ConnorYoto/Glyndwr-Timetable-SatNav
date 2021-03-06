package com.example.glyndwrtimetablesatnav.locationsettings;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.MapTypes;
import com.example.glyndwrtimetablesatnav.utils.ExampleUtils;


@MapTypes(description = R.string.locationSettingsDescription)
public class LocationSettingsActivity extends AppCompatActivity
{
    // This example demonstrates how to access some of the system settings that might affect overall
    // positioning performance with IndoorAtlas SDK.
    private static final int WIFI_BACKGROUND_SCANNING_ENABLED_REQUEST_CODE = 100;
    private static final int BT_ENABLED_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        final String text;
        switch (requestCode)
        {
            case WIFI_BACKGROUND_SCANNING_ENABLED_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {
                    text = getString(R.string.wifiBackgroundScanningEnabled);
                }
                else
                {
                    text = getString(R.string.wifiBackgroundScanningDenied);
                }
                ExampleUtils.showInfo(this, text);
                break;

            case BT_ENABLED_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {
                    text = getString(R.string.btEnabled);
                }
                else
                {
                    text = getString(R.string.btDenied);
                }
                ExampleUtils.showInfo(this, text);
                break;
        }
    }

    //   Check that WiFi is supported and background scanning is enabled
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCheckWiFiBackgroundScanning(View view)
    {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager == null)
        {
            ExampleUtils.showInfo(this, getString(R.string.wifiNotSupported));
        }   //  if (manager == null)
        else
        {
            if (manager.isScanAlwaysAvailable())
            {
                ExampleUtils.showInfo(this, getString(R.string.wifiBackgroundScanningEnabled));
            }   //  if (manager.isScanAlwaysAvailable())
            else
            {
                // Ask user to enable background scanning
                startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), WIFI_BACKGROUND_SCANNING_ENABLED_REQUEST_CODE);
            }   //  else
        }   //  else
    }   //  public void onCheckWiFiBackgroundScanning(View view)

    //Check if Bluetooth is supported and enabled
    public void onCheckBluetoothStatus(View view)
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null)
        {
            ExampleUtils.showInfo(this, getString(R.string.btNotSupported));
        }   //  if (adapter == null)
        else
        {
            if (adapter.getState() == BluetoothAdapter.STATE_ON)
            {
                ExampleUtils.showInfo(this, getString(R.string.btEnabled));
            }   //  if (adapter.getState() == BluetoothAdapter.STATE_ON)
            else
            {
                // Ask user to enable Bluetooth
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_ENABLED_REQUEST_CODE);
            }   //  else
        }   //  else
    }   //  public void onCheckBluetoothStatus(View view)

    //  Verify currently selected location mode
    public void onCheckLocationMode(View view)
    {
        // Check also https://developer.android.com/training/location/change-location-settings.html
        // using the LocationRequest adds dependency to Google Play Services SDK.
        // This approach below shows one way to get a reference about current location mode without
        // the dependency.
        try
        {
            final int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (mode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY || mode == Settings.Secure.LOCATION_MODE_BATTERY_SAVING)
            {
                ExampleUtils.showInfo(this, getString(R.string.locationProviderAvailable));
            }   //  if (mode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY || mode == Settings.Secure.LOCATION_MODE_BATTERY_SAVING)
            else
            {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }   //  else
        }   //  try
        catch (Settings.SettingNotFoundException exception)
        {
            ExampleUtils.showInfo(this, exception.getMessage());
        }   //  catch (Settings.SettingNotFoundException exception)
        catch (ActivityNotFoundException exception)
        {
            ExampleUtils.showInfo(this, exception.getMessage());
        }   //  catch (ActivityNotFoundException exception)
    }   //  public void onCheckLocationMode(View view)
}   //  public class LocationSettingsActivity extends AppCompatActivity
