package com.example.glyndwrtimetablesatnav;

import android.annotation.SuppressLint;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Objects;

public class WebScrapeTwoUnit
{
    @Test
   public void scrapeContents()
    {
        Log.w("TIMETABLE_CONTENTS_TEST", "Testing Timetable Contents Web Scrape");   // Log Message to Logcat
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    //  JSoup document is original concatenated with XML_URL
                    final Document doc = Jsoup.connect("https://timetables.glyndwr.ac.uk/2021/Semester%201/g737108.xml").get();
                    // Room set to MSc Computer Science
                    //  Using Event tags in document to differentiate different event records
                    Elements events = doc.select("event");
                    //  Looping through events to add each events details
                    for (Element event : events)
                    {
                        String check = event.attr("id");
                        if (!check.equals(""))
                        {
                            String Week_ID = event.select("prettyweeks").text().replace(",", "").replace("'", "");
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
                            assertNotNull(Week_ID);
                            assertNotNull(EventID);
                            assertNotNull(EventCat);
                            assertNotNull(Module);
                            assertNotNull(Room);
                            assertNotNull(Day);
                            assertNotNull(S_Time);
                            assertNotNull(E_Time);
                            assertNotNull(Runtime);
                        }
                    }
                }
                catch (IOException e)
                {
                    Log.w("SCRAPING ERROR", Objects.requireNonNull(e.getMessage()));
                }
            }
        }).start();
    }   // protected void scrapeContents()

}
