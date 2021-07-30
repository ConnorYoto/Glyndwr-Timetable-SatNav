package com.example.glyndwrtimetablesatnav;

import android.annotation.SuppressLint;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

import org.junit.Test;

import static org.junit.Assert.*;

public class WebScrapeOneUnit
{
    @Test
    public void scrapeTimetables()
    {
        Log.w("TIMETABLE_RECORDS_TEST","Unit Testing Web Scraping Functionality");
        new Thread(new Runnable()
        {
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
                            assertNotNull(Name);
                            assertNotNull(Type);
                            assertNotNull(XML_URL);
                            Log.w("Record: ", Name + ", " + Type + ", " + XML_URL);
                        }
                    }
                    Log.w("SCRAPE TEST SUCCESSFUL", "Timetable Records have been scraped");
                }
                catch (IOException e)
                {
                    Log.w("SCRAPING ERROR:", Objects.requireNonNull(e.getMessage()));
                }
            }
        }).start();
    } //protected void scrapeTimetables()
}
