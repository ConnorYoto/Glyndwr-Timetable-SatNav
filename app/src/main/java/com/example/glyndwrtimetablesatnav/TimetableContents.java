package com.example.glyndwrtimetablesatnav;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glyndwrtimetablesatnav.wayfinding.WayfindingOverlayActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class TimetableContents extends AppCompatActivity
{
    // Database Variables
    OpenDatabase sqh;
    SQLiteDatabase db;
    //  Interface
    TextView T_IDText;
    TextView XMLText;
    ArrayList<String> duplicateWeekList;
    ArrayList<String> weekList;
    ArrayList<String> weekContents;
    ListView weekContentsList;
    Button weekButton;
    String weekChosen;
    Spinner weekSpinner;
    ArrayAdapter adapter;
    // Dialog - sure to go to timetable? Give time to scrape etc
    Dialog dialog2;
    Context context = this;
    Button navButton;
    Button closeButton;
    Bundle poiBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_contents);
        //  Accepting Variables
        String t_ID = getIntent().getStringExtra("TimetableID");
        String Name = getIntent().getStringExtra("TimetableName");
        String XML_URL = getIntent().getStringExtra("XML_URL");
        // Initialise Database
        InitDatabase();
        // Action Bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }   // if (actionBar != null)
        actionBar.setTitle(Name);
        //Setup Controls
        setUpControls(t_ID, XML_URL);
    }   // protected void onCreate(Bundle savedInstanceState)

    private void InitDatabase()
    {
        // Init the SQLite Helper Class
        sqh = new OpenDatabase(this);
        // Retrieve a Readable and Write-able Database
        db = sqh.getWritableDatabase();
    }   // private void InitDatabase()

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

    private void setUpControls(String t_ID, String XML_URL)
    {
        final String T_ID = t_ID;
        //  Prune duplicate weeks
        duplicateWeekList = new ArrayList<String>();
        duplicateWeekList.addAll(sqh.retrieveWeeks(db, T_ID));

        weekList = new ArrayList<String>();
        weekList.add("Select a week below");

        for(String element : duplicateWeekList)
        {
            if(!weekList.contains(element))
            {
                weekList.add(element);
            }
            else
            {
                continue;
            }
        }
        Log.d("Pruned Week List = ", weekList.toString());
        //  Timetable ID Text View
        T_IDText = findViewById(R.id.tIDTextView);
        T_IDText.setText("Timetable ID: " + t_ID);
        //  XML URL Text View
        XMLText = findViewById(R.id.xmlText);
        XMLText.setText("https://timetables.glyndwr.ac.uk/2021/Semester 1/" + XML_URL);
        // Week Spinner
        weekSpinner = findViewById(R.id.weekSpinner);
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, weekList);
        weekSpinner.setAdapter(adapter);
        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Object item = parent.getItemAtPosition(position);
                if(position != 0)
                {
                    weekChosen = item.toString();
                    Log.w("Week chosen = ", weekChosen);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        // List View
        weekContentsList = findViewById(R.id.contentsListView);
        weekContents = new ArrayList<String>();
        // Construct the List
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, weekContents);
        // Show Empty on load
        weekContents.add("Please Select a Week and Load");
        // Link the ArrayAdapter to the list view
        weekContentsList.setAdapter(adapter);
        // Button Load
        weekButton = findViewById(R.id.buttonUpdateWeek);
        weekButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                weekContents.clear();
                weekContents.addAll(sqh.weekContentsFormat(db, weekChosen, T_ID));
                weekContentsList.setAdapter(adapter);
            }
        });
        // Dialog...
        weekContentsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Open Dialog
                dialog2 = new Dialog(context);
                dialog2.setContentView(R.layout.timetable_contents_dialog);
                String[] Days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
                ArrayList<String> dayList = new ArrayList<String>(Arrays.asList(Days));
                String itemValue = (String)weekContentsList.getItemAtPosition(position);
                if(dayList.contains(itemValue))
                {
                    DisplayMessage("Not a Timetable");
                }
                else
                {
                    // Dialog Interface
                    String[] DialogDetails = itemValue.split(", ");
                    final String EventID = DialogDetails[4];

                    ArrayList<String> eventList = new ArrayList<>();
                    eventList = sqh.eventDetails(db, EventID, weekChosen);
                    String event = eventList.get(0);
                    Log.w("event = ", event);

                    String[] dialogContents = event.split(", ");

                    String Day = dialogContents[0].replace(", ", "");
                    String S_Time = dialogContents[1].replace(", ", "");
                    String E_Time = dialogContents[2].replace(", ", "");
                    String Module = dialogContents[3].replace(", ", "");
                    String EventCat = dialogContents[4].replace(", ", "");
                    String Room = dialogContents[5].replace(", ", "");
                    String DialogName = Module + " (" + EventID + ")";
                    String DialogInfo = "Day: " + Day + "\n Start Time: " + S_Time + "\n End Time: " + E_Time + "\n Event Category: " + EventCat + "\n Room: " + Room;
                    Log.w("Name + Info -", DialogName + DialogInfo);

                    // Dialog Title
                    TextView DialogTitle = dialog2.findViewById(R.id.contentsDialogTitle);
                    DialogTitle.setText(DialogName);
                    // Dialog Details
                    TextView dialogDetails = dialog2.findViewById(R.id.contentsDialogDetails);
                    dialogDetails.setText(DialogInfo);
                    // Dialog Navigate Button
                    navButton = dialog2.findViewById(R.id.navButton);
                    if(Room == "")
                    {
                         navButton.setText("Not Navigable");
                    }
                    else
                    {
                        final String POI = Room;
                        poiBundle.putString("POI", POI);
                        navButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                // Open Wayfinding Activity + Pass Room
                                Intent intent = new Intent(getBaseContext(), WayfindingOverlayActivity.class);
                                intent.putExtras(poiBundle);
                                Log.w("Room = ", POI);
                                startActivity(intent);
                            }
                        });
                        // Open activity
                    }
                }
                // Dialog Close Button
                Button backButton = dialog2.findViewById(R.id.closeContentsButton);
                backButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog2.dismiss();
                    }
                });
                dialog2.show();
            }
        }); // weekContentsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
    }   // private void setUpControls()

    public void DisplayMessage(CharSequence text)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}   // public class TimetableContents extends AppCompatActivity
