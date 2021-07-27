package com.example.glyndwrtimetablesatnav;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

//  JSoup Imports
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;

public class TimetablesActivity extends AppCompatActivity
{
    // Database Variables
    OpenDatabase sqh;
    SQLiteDatabase db;
    // Dialog - sure to go to timetable? Give time to scrape etc
    Dialog dialog;
    Context context = this;
    // Interface
    Button searchButton;
    EditText searchEditText;
    ListView listview;
    ArrayList<String> list;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetables);
        // Initialise Database
        InitDatabase();
        // Action Bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }   // if (actionBar != null)
        //Setup Controls
        setUpControls();
    }   // protected void onCreate(Bundle savedInstanceState)

    public void InitDatabase()
    {
        // Init the SQLite Helper Class
        sqh = new OpenDatabase(this);
        // Retrieve a readable and write-able database
        db = sqh.getWritableDatabase();
    }   // public void InitDatabase()

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }   // public boolean onOptionsItemSelected(MenuItem item)

    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }   // public boolean onCreateOptionsMenu(Menu menu)

    protected void setUpControls()
    {
        // Initialise it in your activity so that the handler is bound to UI thread
        final Handler handlerUI = new Handler();
        // List View
        listview = findViewById(R.id.timetableListView);
        list = new ArrayList<String>();
        // Construct the list
        adapter = new ArrayAdapter(this, R.layout.simple_list_white, list);
        // Populate this ArrayList
        list.addAll(sqh.ListAllTimetables(db));
        // Link the ArrayAdapter to the list view
        listview.setAdapter(adapter);
        // List view on click
        // Dialog
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Open Dialog
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.timetable_search_dialog);
                // Dialog Interface
                String itemValue = (String)listview.getItemAtPosition( position );
                String[] DialogDetails = itemValue.split(", ");
                final String Name = DialogDetails[0];
                final String Type = DialogDetails[1];
                final String XML_URL = DialogDetails[2].replace(" ", "");
                final String TimetableID = DialogDetails[3].replace(" ", "");
                // Dialog Title
                TextView timetableDialogTextView = dialog.findViewById(R.id.searchDialogTitle);
                timetableDialogTextView.setText(Name);
                // Dialog Info
                TextView timetableDetails = dialog.findViewById(R.id.searchDialogDetails);
                timetableDetails.setText("Timetable ID = " + TimetableID + "\n" + "Type = " + Type + "\n" + "XML_URL = " + XML_URL);
                scrapeContents(XML_URL, TimetableID);
                Log.w("SCRAPE DONE: ", "Timetable Contents now populated");
                // Dialog Open Button
                // Scrape Contents as a method before going to activity
                Button openButton = dialog.findViewById(R.id.timetableButton);
                openButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent timetableContentsIntent = new Intent(getBaseContext(), TimetableContents.class);
                        timetableContentsIntent.putExtra("TimetableName", Name);
                        timetableContentsIntent.putExtra("TimetableID", TimetableID);
                        timetableContentsIntent.putExtra("XML_URL", XML_URL);
                        startActivity(timetableContentsIntent);
                    }
                }); // Open activity
                // Dialog Close Button
                Button backButton = dialog.findViewById(R.id.backButton);
                backButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }); // listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        // Search Button + Edit Text
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                list.clear();
                String searchTerm = searchEditText.getText().toString();
                list.addAll(sqh.SearchTimetableRecords(db, searchTerm));
                listview.setAdapter(adapter);
            }
        });
    }   // protected void setUpControls()

    protected void scrapeContents(final String XML_URL, final String T_ID)
    {
        //  Wipe timetable contents table before web scraping
        sqh.removeTimetableContents(db);
        Log.w("TIMETABLE_CONTENTS_WIPED", "Timetable Contents table wiped for fresh web scrape");   // Log Message to Logcat
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    //  JSoup document is original concatenated with XML_URL
                    final Document doc = Jsoup.connect("https://timetables.glyndwr.ac.uk/2021/Semester 1/" + XML_URL).get();
                    //  Using Event tags in document to differentiate different event records
                    Elements events = doc.select("event");
                    //  Looping through events to add each events details
                    for (Element event : events)
                    {
                        String check = event.attr("id");
                        if (check.equals("")) {
                            continue;
                        }
                        else
                        {
                            String Week_ID = event.select("prettyweeks").text().replace(",", "").replace("'", "");
                            String TimetableID = T_ID;
                            String EventID = event.attr("id");
                            String EventCat = event.select("category").text().replace(",", "").replace("'", "");
                            String Module = event.select("module").select("item").text().replace(",", "").replace("'", "");
                            String Event_Name = event.select("eventname").text().replace(",", "").replace("'", "");
                            if(!Event_Name.equals(""))
                            {
                             Module = Module + " Event - " + Event_Name;
                            }
                            String Room = event.select("room").select("item").text().replace(",", "").replace("'", "");
                            String Day = event.select("day").text().replace(",", "").replace("'", "");
                            String S_Time = event.select("starttime").text().replace(",", "").replace("'", "");
                            String E_Time = event.select("endtime").text().replace(",", "").replace("'", "");
                            String Runtime = event.attr("timesort");
                            sqh.addTimetableContents(db, Week_ID, TimetableID, EventID, EventCat, Module, Room, Day, S_Time, E_Time, Runtime);
                            Log.w("Record = ", Week_ID + EventCat + Module + Room + Day + S_Time + E_Time + Runtime);
                        }
                    }
                }
                catch (IOException e)
                {
                    Log.w("SCRAPING ERROR",e.getMessage());
                }
            }
        }).start();
    }   // protected void scrapeContents()

}   // public class TimetablesActivity extends AppCompatActivity
