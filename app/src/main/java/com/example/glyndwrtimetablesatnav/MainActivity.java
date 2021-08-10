package com.example.glyndwrtimetablesatnav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
// JSoup Imports
import com.example.glyndwrtimetablesatnav.wayfinding.WayfindingOverlayActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
// Java IO Imports ---- Do I need this?
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;


public class MainActivity extends AppCompatActivity
{
    // Database Paths + Name + Log Tag
    private static final String DATABASE_PATH = "/data/data/com.example.glyndwrtimetablesatnav/databases/";
    private static final String DATABASE_PATH2 = "/data/data/com.example.glyndwrtimetablesatnav/databases"; // no / at end of path !!!
    private static final String DATABASE_NAME = "timetable_db5.db";
    private static final String LOG_TAG = "TIMETABLE_DB5";

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final String TAG = "IAExample";

    // Database Variables
    Context ctx;
    OpenDatabase sqh;
    SQLiteDatabase db;

    // Interface
    Button mapButton;
    Button timetableButton;
    Button surveyButton;
    Button mappingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up Database
        setUpDatabase();
        // Initialise Database
        InitDatabase();
        // JSoup Web-Scraping
        scrapeTimetables();
        // Toast DisplayMessage
        DisplayMessage("Timetables Loaded");
        // Set up Controls
        setUpControls();
    } // protected void onCreate(Bundle savedInstanceState)

    protected void scrapeTimetables()
    {
        // Wipe timetableRecords table before Web Scraping
        sqh.removeAllTimetables(db);
        Log.w("TIMETABLE_RECORDS_WIPED","Timetable Records table wiped for fresh web scrape"); // Log Message to Logcat - Timetable Wiped
        new Thread(new Runnable()
        {
            @SuppressLint("LogNotTimber")
            @Override
            public void run()
            {
                try
                {
                    final Document doc = Jsoup.connect("https://timetables.glyndwr.ac.uk/2021/Semester 1/finder.xml").get();

                    Elements resources = doc.select("resource");

                    for (Element resource : resources)
                    {
                        String check = resource.attr("id");
                        if (!check.equals(""))
                        {
                            String Name = resource.select("name").text().replace(",", "").replace("'", "");
                            String Type = resource.attr("type").replace(",", "");
                            String XML_URL = resource.select("link.pdf").attr("href").replace(".pdf", ".xml");
                            sqh.addTimetableRecord(db, Name, Type, XML_URL);
                            //Log.w("Record:","Name = " + Name + ", Type = " + Type + ", URL = " + XML_URL);
                        }
                    }
                    Log.w("SCRAPE SUCCESSFUL", "Timetable Records have been scraped"); // Log Message to Logcat - Web Scrape Successful
                }
                catch (IOException e)
                {
                    Log.w("SCRAPING ERROR:", Objects.requireNonNull(e.getMessage())); // Log Message to Logcat - Scraping error + exception message
                }
            }
        }).start();
    } //protected void scrapeTimetables()

    public void DisplayMessage(CharSequence text)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void setUpDatabase()
    {
        ctx = this.getBaseContext();
        try
        {
            CopyDataBaseFromAsset();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    } //public void setUpDatabase()

    @SuppressLint("LogNotTimber")
    public void CopyDataBaseFromAsset() throws IOException
    {
        // Get the SQLite database in the assets folder
        InputStream in = ctx.getAssets().open(DATABASE_NAME);
        // LOG TAG for LOGCAT - Starting to Copy
        Log.w( LOG_TAG , "Starting to copy Database from Assets...");
        String outputFileName = DATABASE_PATH + DATABASE_NAME;
        File databaseFolder = new File( DATABASE_PATH2 );
        // Databases folder exists? No - Create it and copy !!!
        if ( !databaseFolder.exists() )
        {
            databaseFolder.mkdir();
            OutputStream out = new FileOutputStream(outputFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ( (length = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, length);
            } // while ( (length = in.read(buffer)) > 0)
            out.flush();
            out.close();
            in.close();
            //LOG TAG for LOGCAT - Completed Copy
            Log.w(LOG_TAG, "Completed.");
        } // if ( !databaseFolder.exists() )
    } // public void CopyDatabaseFromAsset() throws Exception

    public void InitDatabase()
    {
        // Init the SQLite Helper Class
        sqh = new OpenDatabase(this);
        // Retrieve a Readable and Write-able Database
        db = sqh.getWritableDatabase();
    } // public void InitDatabase()

    protected void setUpControls() // Menu
    {
        // Map Button
        mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Opens Map Activity
                Intent mapIntent = new Intent(getBaseContext(), WayfindingOverlayActivity.class);
                startActivity(mapIntent);
            }
        });
        // Timetables Button
        timetableButton = findViewById(R.id.timetableButton);
        timetableButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Opens Timetables Activity
                Intent timetableIntent = new Intent(getBaseContext(), TimetablesActivity.class);
                startActivity(timetableIntent);
            }
        });
        // Survey Button
        surveyButton = findViewById(R.id.surveyButton);
        surveyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Opens External Survey
                Intent browserIntent = new Intent(Intent.ACTION_VIEW).setData( Uri.parse("https://forms.gle/WsUMvPQpNJU3f9ELA")); // Survey
                startActivity(browserIntent);
            }
        });
        //  Mapping Button
        mappingButton = findViewById(R.id.mappingButton);
        mappingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent mappingIntent = new Intent(getBaseContext(), MappingActivity.class);
                startActivity(mappingIntent);
            }
        });
    } // protected void setUpControls() // Menu

    // Location Permissions for app
    public static boolean checkLocationPermissions(Activity activity)
    {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }   //  public static boolean checkLocationPermissions(Activity activity)

    //  Checks that we have access to required information, if not ask for users permission.
    private void ensurePermissions()
    {
        if (!checkLocationPermissions(this))
        {
            // We don't have access to FINE_LOCATION (Required by Google Maps example)
            // IndoorAtlas SDK has minimum requirement of COARSE_LOCATION to enable WiFi scanning
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this).setTitle(R.string.locationPermissionRequest)
                        .setMessage(R.string.locationPermissionRequestRationale)
                        .setPositiveButton(R.string.permissionAcceptButton, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.d(TAG, "request permissions");
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }   //  public void onClick(DialogInterface dialog, int which)
                        })  //  .setPositiveButton(R.string.permission_button_accept, new DialogInterface.OnClickListener()
                        .setNegativeButton(R.string.permissionDenyButton, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Toast.makeText(MainActivity.this,
                                        R.string.locationPermissionDeniedMsg,
                                        Toast.LENGTH_LONG).show();
                            }   //  public void onClick(DialogInterface dialog, int which)
                        })  //  .setNegativeButton(R.string.permission_button_deny, new DialogInterface.OnClickListener()
                        .show();
            }
            else
            {
                // ask user for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }   //  else
        }   //  if (!checkLocationPermissions(this))
    }   //  private void ensurePermissions()

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION)
        {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(this, R.string.locationPermissionDeniedMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (isSdkConfigured())
        {
            ensurePermissions();
        }   //  if (isSdkConfigured())
    }   //  protected void onResume()

    protected boolean isSdkConfigured()
    {
        return !"api-key-not-set".equals(getString(R.string.indooratlas_api_key))
                && !"api-secret-not-set".equals(getString(R.string.indooratlas_api_secret));
    }
} // public class MainActivity extends AppCompatActivity
