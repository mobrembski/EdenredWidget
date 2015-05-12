package com.mobrembski.edenredwidget;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.HashMap;

public class MainActivity extends AppWidgetProvider {

    public static HashMap<Integer, Integer> WidgetWidths = new HashMap<>();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            //Log.d("EdenredWidget", "onUpdate");
            updateWidgets(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            return;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId <= 0) throw new AssertionError();
        int widgetWidth = 80;
        if (WidgetWidths.containsKey(appWidgetId))
            widgetWidth = WidgetWidths.get(appWidgetId);
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        Log.d("EdenredWidget", "onReceive" + intent.getAction());
        if (intent.getAction() == "MY_PACKAGE_NAME.WIDGET_BUTTON" ||
                intent.getAction() == "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS") {
            long cardNum = sp.getLong(appWidgetId + "_cardid", -1);
            if (cardNum >= 0) {
                new EdenredCommTask(context, appWidgetId, widgetWidth).execute(cardNum);
            }
        }
        /*if (intent.getAction() == AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED) {
            SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            int u = sp.getInt("EdenredCard", 000);
        }*/
        if (intent.getAction() == AppWidgetManager.ACTION_APPWIDGET_DELETED) {
            sp.edit().remove(appWidgetId+"_cardid").commit();
            sp.edit().remove(appWidgetId+"_value").commit();
            sp.edit().remove(appWidgetId+"_notify").commit();
        }
    }

    private void updateWidgets(Context context, AppWidgetManager widgetManager, int appWidgetId) {
        //Log.d("EdenredWidget", "UpdateWidgets");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x1);
        Intent intent = new Intent("MY_PACKAGE_NAME.WIDGET_BUTTON");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // From Twig's Tech tips, requestCode should be set to appwidgetID
        PendingIntent pending = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button1, pending);
        widgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        RemoteViews updateViews = generateLayout(context, appWidgetId, newOptions);
        if (WidgetWidths.containsKey(appWidgetId))
            WidgetWidths.remove(appWidgetId);
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        WidgetWidths.put(appWidgetId, minWidth);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    public static RemoteViews generateLayout(Context context, int appWidgetId, Bundle newOptions) {
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        return generateLayout(context, appWidgetId, minWidth);
    }

    public static RemoteViews generateLayout(Context context, int appWidgetId, int widgetWidth) {
        Intent intent = new Intent("MY_PACKAGE_NAME.WIDGET_BUTTON");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pending = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x1);
        if (widgetWidth <=40)
            Log.i("WidgetProvider","1x cell");
        if (widgetWidth > 40 && widgetWidth <= 110) {
            Log.i("WidgetProvider","2x cell");
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x1);
            updateViews.setOnClickPendingIntent(R.id.layout_1x1, pending);
        }

        if (widgetWidth > 110 && widgetWidth <= 180) {
            Log.i("WidgetProvider","3x cell");
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x2);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        if (widgetWidth > 180 && widgetWidth <= 250) {
            Log.i("WidgetProvider","3x cell");
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x3);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        if (widgetWidth > 250 && widgetWidth <= 320) {
            Log.i("WidgetProvider","3x cell");
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x4);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        if (widgetWidth > 320 && widgetWidth <= 390) {
            Log.i("WidgetProvider","3x cell");
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x4);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
            Log.i("WidgetProvider","6x cell");
        Log.i("WidgetProvider","Widget size changed");
        Log.i("WidgetProvider","minWidth: " + widgetWidth);
        Log.i("WidgetProvider","minHeight: " + widgetWidth);
        return updateViews;
    }
}