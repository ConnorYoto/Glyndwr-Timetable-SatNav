package com.example.glyndwrtimetablesatnav.orientation;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.MapTypes;

@MapTypes(description = R.string.orientation_description)
public class OrientationActivity extends AppCompatActivity implements IALocationListener, IAOrientationListener
{
    GLSurfaceView mGlView;
    OrientationRenderer mRenderer;
    TextView mTextBearing;
    TextView mTextHeading;
    TextView mTextOrientation;
    IALocationManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation);
        //  Set background view to panorama
        mGlView = (GLSurfaceView) findViewById(R.id.gl_view);
        mGlView.setEGLContextClientVersion(2);
        mRenderer = new OrientationRenderer(this, R.raw.panorama);
        mGlView.setRenderer(mRenderer);
        mGlView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // Bearing Interface
        mTextBearing = (TextView) findViewById(R.id.text_bearing);
        mTextHeading = (TextView) findViewById(R.id.text_heading);
        mTextOrientation = (TextView) findViewById(R.id.text_orientation);
        // Initiate Location Manager
        mManager = IALocationManager.create(this);
    }   //  protected void onCreate(Bundle savedInstanceState)

    @Override
    protected void onResume()
    {
        super.onResume();
        mManager.requestLocationUpdates(IALocationRequest.create(), this);
        // trigger heading and orientation updates when they have changed by 5 degrees
        mManager.registerOrientationListener(new IAOrientationRequest(5.0, 5.0), this);
    }   //  protected void onResume()

    @Override
    protected void onPause()
    {
        mManager.unregisterOrientationListener(this);
        mManager.removeLocationUpdates(this);
        super.onPause();
    }   //  protected void onPause()

    @Override
    protected void onDestroy()
    {
        mManager.destroy();
        super.onDestroy();
        //  Clean up after ourselves
    }   //  protected void onDestroy()

    @Override
    public void onLocationChanged(IALocation iaLocation)
    {
        mTextBearing.setText(getString(R.string.text_bearing, iaLocation.getBearing()));
    }   //  public void onLocationChanged(IALocation iaLocation)

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onHeadingChanged(long timestamp, double heading)
    {
        mTextHeading.setText(getString(R.string.text_heading, heading));
    }   //  public void onHeadingChanged(long timestamp, double heading)

    @Override
    public void onOrientationChange(long timestamp, double[] orientation)
    {
        final double qw = orientation[0];
        final double qx = orientation[1];
        final double qy = orientation[2];
        final double qz = orientation[3];
        // Compute Euler angles
        final double pitch = Math.atan2(2.0 * (qw * qx + qy * qz), 1.0 - 2.0 * (qx * qx + qy * qy));
        final double roll =  Math.asin(2.0 * (qw * qy - qz * qx));
        final double yaw = -Math.atan2(2.0 * (qw * qz + qx * qy), 1.0 - 2.0 * (qy * qy + qz * qz));

        mTextOrientation.setText(getString(R.string.text_orientation, Math.toDegrees(yaw), Math.toDegrees(pitch), Math.toDegrees(roll)));
        mRenderer.setOrientation(orientation);
        mGlView.requestRender();
    }   //  public void onOrientationChange(long timestamp, double[] orientation)
}   //  public class OrientationActivity extends AppCompatActivity implements IALocationListener, IAOrientationListener
