package com.example.glyndwrtimetablesatnav.foregroundservice;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.glyndwrtimetablesatnav.R;
import com.example.glyndwrtimetablesatnav.MapTypes;

@MapTypes(description = R.string.foregroundserviceDescription)
public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground);
        Button startButton = findViewById(R.id.button1);
        Button stopButton = findViewById(R.id.button2);
        // Start foreground service will create persistent notification with the "Start" and "Stop"
        // buttons for positioning
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }   //  protected void onCreate(Bundle savedInstanceState)

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button1:
                // After starting the foreground service, you can close the example app and continue
                // using the foreground service notification
                Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                startIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
                startService(startIntent);
                break;
            case R.id.button2:
                // To close the foreground service, "Stop foreground service" button must be pressed
                Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
                stopIntent.setAction(ForegroundService.STOPFOREGROUND_ACTION);
                startService(stopIntent);
                break;
            default:
                break;
        }   //  switch (v.getId())
    }   //  public void onClick(View v)
}   //  public class MainActivity extends AppCompatActivity implements View.OnClickListener
