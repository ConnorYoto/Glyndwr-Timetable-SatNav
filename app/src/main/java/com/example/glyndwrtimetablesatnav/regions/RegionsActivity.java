package com.example.glyndwrtimetablesatnav.regions;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.MapTypes;

//Demonstrates automatic region transitions and floor level certainty
@MapTypes(description = R.string.regionsDescription)
public class RegionsActivity extends FragmentActivity implements IALocationListener, IARegion.Listener
{
    IALocationManager mManager;
    IARegion mCurrentVenue = null;
    IARegion mCurrentFloorPlan = null;
    Integer mCurrentFloorLevel = null;
    Float mCurrentCertainty = null;

    TextView mUiVenue;
    TextView mUiVenueId;
    TextView mUiFloorPlan;
    TextView mUiFloorPlanId;
    TextView mUiFloorLevel;
    TextView mUiFloorCertainty;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regions);

        mManager = IALocationManager.create(this);
        mManager.registerRegionListener(this);
        mManager.requestLocationUpdates(IALocationRequest.create(), this);

        mUiVenue = findViewById(R.id.text_view_venue);
        mUiVenueId = findViewById(R.id.text_view_venue_id);
        mUiFloorPlan = findViewById(R.id.text_view_floor_plan);
        mUiFloorPlanId = findViewById(R.id.text_view_floor_plan_id);
        mUiFloorLevel = findViewById(R.id.text_view_floor_level);
        mUiFloorCertainty = findViewById(R.id.text_view_floor_certainty);

        updateUi();
    }   //  public void onCreate(Bundle savedInstanceState)

    @Override
    protected void onDestroy()
    {
        mManager.destroy();
        super.onDestroy();
    }   //  protected void onDestroy()

    @Override
    public void onLocationChanged(IALocation iaLocation)
    {
        mCurrentFloorLevel = iaLocation.hasFloorLevel() ? iaLocation.getFloorLevel() : null;
        mCurrentCertainty = iaLocation.hasFloorCertainty() ? iaLocation.getFloorCertainty() : null;
        updateUi();
    }   //  public void onLocationChanged(IALocation iaLocation)

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }   //  public void onStatusChanged(String s, int i, Bundle bundle)

    @Override
    public void onEnterRegion(IARegion iaRegion)
    {
        if (iaRegion.getType() == IARegion.TYPE_VENUE)
        {
            mCurrentVenue = iaRegion;
        }   //  if (iaRegion.getType() == IARegion.TYPE_VENUE)
        else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN)
        {
            mCurrentFloorPlan = iaRegion;
        }   //  else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN)
        updateUi();
    }   //  public void onEnterRegion(IARegion iaRegion)

    @Override
    public void onExitRegion(IARegion iaRegion)
    {
        if (iaRegion.getType() == IARegion.TYPE_VENUE)
        {
            mCurrentVenue = iaRegion;
        }   //  if (iaRegion.getType() == IARegion.TYPE_VENUE)
        else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN)
        {
            mCurrentFloorPlan = iaRegion;
        }   //  else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN)
        updateUi();
    }   //  public void onExitRegion(IARegion iaRegion)

    void updateUi()
    {
        String venue = getString(R.string.venueOutside);
        String venueId = "";
        String floorPlan = "";
        String floorPlanId = "";
        String level = "";
        String certainty = "";
        if (mCurrentVenue != null)
        {
            venue = getString(R.string.venueInside);
            venueId = mCurrentVenue.getId();
            if (mCurrentFloorPlan != null)
            {
                floorPlan = mCurrentFloorPlan.getName();
                floorPlanId = mCurrentFloorPlan.getId();
            }   //  if (mCurrentFloorPlan != null)
            else
            {
                floorPlan = getString(R.string.floorPlanOutside);
            }   //   else
        }   //  if (mCurrentVenue != null)
        if (mCurrentFloorLevel != null)
        {
            level = mCurrentFloorLevel.toString();
        }   //  if (mCurrentFloorLevel != null)
        if (mCurrentCertainty != null)
        {
            certainty = getString(R.string.floorCertaintyPercentage, mCurrentCertainty * 100.0f);
        }   //  if (mCurrentCertainty != null)
        setText(mUiVenue, venue, true);
        setText(mUiVenueId, venueId, true);
        setText(mUiFloorPlan, floorPlan, true);
        setText(mUiFloorPlanId, floorPlanId, true);
        setText(mUiFloorLevel, level, true);
        setText(mUiFloorCertainty, certainty, false); // do not animate as changes can be frequent
    }   //  void updateUi()

    //  Set the text of a TextView and make a animation to notify when the value has changed
    void setText(@NonNull TextView view, @NonNull String text, boolean animateWhenChanged)
    {
        if (!view.getText().toString().equals(text))
        {
            view.setText(text);
            if (animateWhenChanged)
            {
                view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.notify_change));
            }   //  if (animateWhenChanged)
        }   //  if (!view.getText().toString().equals(text))
    }   //  void setText(@NonNull TextView view, @NonNull String text, boolean animateWhenChanged)
}   //  public class RegionsActivity extends FragmentActivity implements IALocationListener, IARegion.Listener
