package com.example.glyndwrtimetablesatnav;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

public class OpenDatabase extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "timetable_db5.db";

    // TOGGLE THIS NUMBER FOR UPDATING TABLES AND DATABASE
    private static final int DATABASE_VERSION = 1;

    OpenDatabase(Context context)
    {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    } // OpenDatabase(Context context)

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    } // public void onCreate(SQLiteDatabase db)

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    } // public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)

    // Method Remove All Timetable Records
    public void removeAllTimetables(SQLiteDatabase db)  // Timetable Records table
    {
        String removeTimetablesSQL = "DELETE FROM timetable_records;";

        db.execSQL( removeTimetablesSQL );

        // Reset AutoIncrement ID
        String resetSequenceSQL = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'timetable_records'";

        db.execSQL( resetSequenceSQL );
    }

    // Method Add Timetable Records
    public void addTimetableRecord(SQLiteDatabase db, String Name, String Type, String XML_URL) // Timetable Records table
    {
        String addTimetableSQL = "INSERT INTO timetable_records(Name, Type, XML_URL) VALUES ('" + Name + "', '" + Type + "', '" + XML_URL + "');";

        db.execSQL( addTimetableSQL );
    }

    // Method List All Timetable Records
    public ArrayList<String> ListAllTimetables(SQLiteDatabase db) // Timetable Records table
    {
        ArrayList<String> list = new ArrayList<String>();
        String str = "";

        Cursor c = db.rawQuery("SELECT * FROM timetable_records", null);
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    String Name = c.getString(1);
                    str = str + Name + ", ";
                    String Type = c.getString(2);
                    str = str + Type + ", ";
                    String XML_URL = c.getString(3);
                    str = str + XML_URL + ", ";
                    String TimetableID = c.getString(0);
                    str = str + TimetableID;

                    list.add(str);
                    str = "";

                } while (c.moveToNext());
            }
        }
        c.close();
        Log.w("Timetable Records Table Request","Table Successfully Loaded");
        return list;
    }   // public ArrayList<String> ListAllTimetables(SQLiteDatabase db)

    // Method Search Timetable Records
    public ArrayList<String> SearchTimetableRecords(SQLiteDatabase db, String searchTerm) // Timetable Records table
    {
        ArrayList<String> list = new ArrayList<String>();
        String str = "";

        Cursor c = db.rawQuery("SELECT * FROM timetable_records WHERE NAME LIKE '%" + searchTerm + "%' OR TYPE LIKE '%" + searchTerm + "%'", null);
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    String Name = c.getString(1);
                    str = str + Name + ", ";
                    String Type = c.getString(2);
                    str = str + Type + ", ";
                    String XML_URL = c.getString(3);
                    str = str + XML_URL + ", ";
                    String TimetableID = c.getString(0);
                    str = str + TimetableID;

                    list.add(str);
                    str = "";

                } while (c.moveToNext());
            }
        }
        c.close();
        Log.w("SEARCH TIMETABLE RECORDS","searchTerm = " + searchTerm);
        return list;
    }   // public ArrayList<String> SearchTimetableRecords(SQLiteDatabase db, String searchTerm)

    // Method Remove Timetable Contents
    public void removeTimetableContents(SQLiteDatabase db)
    {
        String removeTimetableContentsSQL = "DELETE FROM timetable_contents;";

        db.execSQL( removeTimetableContentsSQL );

        // Reset AutoIncrement ID
        String resetSequenceSQL = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'timetable_contents'";

        db.execSQL( resetSequenceSQL );
    } // public void WipeTimetableContents(SQLiteDatabase db)

    // Method Add Timetable Contents
    public void addTimetableContents(SQLiteDatabase db, String WeekID, String TimetableID, String EventID, String EventCat, String Module, String Room,
                                     String Day, String S_Time, String E_Time, String Runtime) // Timetable Details Table
    {
        String addTimetableContentsSQL = "INSERT INTO timetable_contents(WeekID, TimetableID, EventID, EventCat, Module, Room, Day, S_Time, E_Time, Runtime) VALUES ('" + WeekID + "', '" + TimetableID + "', '" + EventID + "', '" + EventCat + "', '" + Module + "', '"
                + Room + "', '" + Day + "', '" + S_Time + "', '" + E_Time + "', '" + Runtime + "');";
        db.execSQL( addTimetableContentsSQL );

        Log.w("Timetable Contents : ", "Added");
    } // public void addTimetableContents(SQLiteDatabase db, WeekID, TimetableID, Week_Start, EventID, EventCat, Module, Room, Day, S_Time, E_Time) // Timetable Details Table

    // Method Retrieve all weeks
    public ArrayList<String> retrieveWeeks(SQLiteDatabase db, String TimetableID)
    {
        ArrayList<String> list = new ArrayList<>();
        String str = "";

        Cursor c = db.rawQuery("SELECT WeekID FROM timetable_contents WHERE TimetableID LIKE '%" + TimetableID + "%' ORDER BY WeekID",null);
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    String WeekID = c.getString(0);
                    int WeekInt = Integer.parseInt(WeekID);
                    if( WeekInt <9 || WeekInt > 52)
                    {
                        Log.w("Week Pruned = ", WeekID);
                        continue;
                    }
                    else
                    {
                        str = str + WeekID;
                    }
                    list.add(str);
                    str = "";

                } while(c.moveToNext());
            }
        }
        c.close();
        Log.w("RETRIEVED WEEKS FOR ","Timetable ID = " + TimetableID);
        return list;
    } // public ArrayList<String> retrieveWeeks(SQLiteDatabase db, String TimetableID)

    // Method List All Contents in Order
    public ArrayList<String> weekContentsFormat(SQLiteDatabase db, String weekID, String TimetableID)
    {
        ArrayList<String> list = new ArrayList<String>();
        String str = "";
        String days[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for(int i = 0; i<=4; i++)
        {
            String Day = days[i];
            str = str + Day;
            list.add(str);
            str = "";
            Cursor c = db.rawQuery("SELECT S_Time, E_Time, Module, EventCat, EventID FROM timetable_contents WHERE Day LIKE '%" + i + "%' AND WeekID LIKE '%" + weekID + "%' AND TimetableID LIKE '%" + TimetableID + "%'", null);
            if(c != null)
            {
                if(c.moveToFirst())
                {
                    do
                    {
                        String S_Time = c.getString(0);
                        str = str + S_Time + ", ";
                        String E_Time = c.getString(1);
                        str = str + E_Time + ", ";
                        String Module = c.getString(2);
                        str = str + Module + ", ";
                        String EventCat = c.getString(3);
                        str = str + EventCat + ", ";
                        String EventID = c.getString(4);
                        str = str + EventID;

                        list.add(str);
                        str = "";
                    }while(c.moveToNext());
                }
            }
            c.close();
            Log.w("Retrieved Contents for Day/Week/Timetable", String.valueOf(i) + weekID + TimetableID);
        }
        Log.d("Week = ", list.toString());
        return list;
    } // public ArrayList<String> weekContentsFormat(SQLiteDatabase db, String weekID)

    // Method List Event Details
    public ArrayList<String> eventDetails(SQLiteDatabase db, String EventID, String WeekID)
    {
        ArrayList<String> list = new ArrayList<String>();
        String str = "";
        String days[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for(int i = 0; i<=4; i++) {
            String Day = days[i];
            str = "";
            Cursor c = db.rawQuery("SELECT Day, S_Time, E_Time, Module, EventCat, Room FROM timetable_contents WHERE EventID LIKE '%" + EventID + "%' AND WeekID LIKE '%" + WeekID + "%'", null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        str = str + Day + ", ";
                        String S_Time = c.getString(1);
                        str = str + S_Time + ", ";
                        String E_Time = c.getString(2);
                        str = str + E_Time + ", ";
                        String Module = c.getString(3);
                        str = str + Module + ", ";
                        String EventCat = c.getString(4);
                        str = str + EventCat + ", ";
                        String Room = c.getString(5);
                        str = str + Room;

                        list.add(str);
                        str = "";
                        Log.w("Str", str);
                    } while (c.moveToNext());
                }
            }
            c.close();
            Log.w("RETRIEVED EVENT DETAILS FOR ","EventID = " + EventID);
        }
        Log.d("list = ", list.toString());
        return list;
    } // public ArrayList<String> EventDetails(SQLiteDatabase db, String EventID)
} // public class OpenDatabase extends SQLiteOpenHelper
