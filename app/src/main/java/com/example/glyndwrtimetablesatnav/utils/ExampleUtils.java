package com.example.glyndwrtimetablesatnav.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;

import com.indooratlas.android.sdk.IALocationManager;
import com.example.glyndwrtimetablesatnav.R;

public class ExampleUtils
{
    //  Shares the trace ID of the client. Trace ID can be used under certain conditions by IndoorAtlas to provide detailed support.
    public static void shareTraceId(View view, final Context context, final IALocationManager manager)
    {
        view.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                shareText(context, manager.getExtraInfo().traceId, "traceId");
                return true;
            }   //  public boolean onLongClick(View view)
        }); //  view.setOnLongClickListener(new View.OnLongClickListener()
    }   //   public static void shareTraceId(View view, final Context context, final IALocationManager manager)

    //Use the share tool to share text via Slack, email, WhatsApp etc.
    public static void shareText(Context context, String text, String title)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, title));
    }   //  public static void shareText(Context context, String text, String title)

    //Shows a {@link Snackbar} with defined text
    public static void showInfo(Activity activity, String text)
    {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.buttonClose, new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                snackbar.dismiss();
            }   //  public void onClick(View view)
        }); //  snackbar.setAction(R.string.button_close, new View.OnClickListener()
        snackbar.show();
    }   //  public static void showInfo(Activity activity, String text)
}   //  public class ExampleUtils
