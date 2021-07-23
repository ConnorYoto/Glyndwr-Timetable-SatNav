package com.example.glyndwrtimetablesatnav;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

// Entry into mapping examples for testing mapping capabilities
public class MappingActivity extends AppCompatActivity
{
    private static final String TAG = "IAExample";
    private ExamplesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        mAdapter = new ExamplesAdapter(this);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ComponentName example = mAdapter.mExamples.get(position).mComponentName;
                startActivity(new Intent(Intent.ACTION_VIEW).setComponent(example));
            }   //  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        }); //  listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        if (!isSdkConfigured())
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.configuration_incomplete_title)
                    .setMessage(R.string.configuration_incomplete_message)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    }).show();
        }   //  if (!isSdkConfigured())
    }   //   protected void onCreate(Bundle savedInstanceState)

    /**
     * Adapter for example activities.
     */
    class ExamplesAdapter extends BaseAdapter
    {
        final ArrayList<ExampleEntry> mExamples;
        ExamplesAdapter(Context context)
        {
            mExamples = listActivities(context);
        }

        @Override
        public int getCount()
        {
            return mExamples.size();
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ExampleEntry entry = mExamples.get(position);
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView labelText = (TextView) convertView.findViewById(android.R.id.text1);
            TextView descriptionText = (TextView) convertView.findViewById(android.R.id.text2);
            labelText.setText(entry.mLabel);
            descriptionText.setText(entry.mDescription);
            return convertView;
        }
    }

    /**
     * Returns a list of activities that are part of this application, skipping
     * those from included libraries and *this* activity.
     */
    public ArrayList<ExampleEntry> listActivities(Context context)
    {
        ArrayList<ExampleEntry> result = new ArrayList<>();
        try
        {
            final String packageName = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            for(ActivityInfo activityInfo : info.activities)
            {
                parseExample(activityInfo, result);
            }
            return result;

        } catch (Exception e) {
            throw new IllegalStateException("failed to get list of activities", e);
        }

    }

    private void parseExample(ActivityInfo info, ArrayList<ExampleEntry> list) {
        try {
            Class<?> cls = Class.forName(info.name);
            if (cls.isAnnotationPresent(MapTypes.class)) {
                MapTypes annotation = (MapTypes) cls.getAnnotation(MapTypes.class);
                list.add(new ExampleEntry(new ComponentName(info.packageName, info.name),
                        annotation.title() != -1
                                ? getString(annotation.title())
                                : info.loadLabel(getPackageManager()).toString(),
                        getString(annotation.description())));
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "failed to read example info for class: " + info.name, e);
        }
    }


    static class ExampleEntry implements Comparable<ExampleEntry>
    {
        final ComponentName mComponentName;
        final String mLabel;
        final String mDescription;

        ExampleEntry(ComponentName name, String label, String description)
        {
            mComponentName = name;
            mLabel = label;
            mDescription = description;
        }

        @Override
        public int compareTo(ExampleEntry another)
        {
            return mLabel.compareTo(another.mLabel);
        }
    }

    protected boolean isSdkConfigured()
    {
        return !"api-key-not-set".equals(getString(R.string.indooratlas_api_key))
                && !"api-secret-not-set".equals(getString(R.string.indooratlas_api_secret));
    }
}
