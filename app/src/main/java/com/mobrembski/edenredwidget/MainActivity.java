package com.mobrembski.edenredwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class MainActivity extends AppWidgetProvider {

    public static final String ButtonActionIntent = "MY_PACKAGE_NAME.WIDGET_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            updateWidgets(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
            return;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId <= 0) throw new AssertionError();
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
        int widgetWidth = sp.getInt(appWidgetId + "_width", 80);
        if (intent.getAction().equals(ButtonActionIntent) ||
                intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED) ||
                intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            long cardNum = sp.getLong(appWidgetId + "_cardid", -1);
            if (cardNum >= 0) {
                new EdenredCommTask(context, appWidgetId, widgetWidth).execute(cardNum);
            }
        }
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
            sp.edit().remove(appWidgetId + "_cardid").apply();
            sp.edit().remove(appWidgetId + "_value").apply();
            sp.edit().remove(appWidgetId + "_notify").apply();
            sp.edit().remove(appWidgetId + "_width").apply();
        }
    }

    private void updateWidgets(Context context, AppWidgetManager widgetManager, int appWidgetId) {
        int widgetWidth;
        boolean isPortrait = context.getResources().getBoolean(R.bool.isPort);

        AppWidgetProviderInfo info = AppWidgetManager.getInstance(
                context.getApplicationContext()).getAppWidgetInfo(appWidgetId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Bundle mAppWidgetOptions = widgetManager.getAppWidgetOptions(appWidgetId);
            if (isPortrait)
                widgetWidth = mAppWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            else
                widgetWidth = mAppWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        } else
            widgetWidth = info.minWidth;
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
        sp.edit().putInt(appWidgetId + "_width", widgetWidth).commit();

        RemoteViews updateViews = generateLayout(context, appWidgetId, widgetWidth);
        Intent intent = new Intent(ButtonActionIntent);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        context.sendBroadcast(intent);
        widgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager,
                                          int appWidgetId,
                                          Bundle newOptions) {

        RemoteViews updateViews = generateLayout(context, appWidgetId, newOptions);
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        SharedPreferences sp = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        sp.edit().putInt(appWidgetId + "_width", minWidth).commit();
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    public static RemoteViews generateLayout(Context context, int appWidgetId, Bundle newOptions) {
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        return generateLayout(context, appWidgetId, minWidth);
    }

    public static RemoteViews generateLayout(Context context, int appWidgetId, int widgetWidth) {
        Intent intent = new Intent(ButtonActionIntent);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x1);
        if (widgetWidth <= 110) {
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x1);
            updateViews.setOnClickPendingIntent(R.id.layout_1x1, pending);
        }
        if (widgetWidth > 110 && widgetWidth <= 180) {
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x2);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        if (widgetWidth > 180 && widgetWidth <= 250) {
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x3);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        if (widgetWidth > 250 && widgetWidth <= 320) {
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x4);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        if (widgetWidth > 320) {
            updateViews = new RemoteViews(context.getPackageName(), R.layout.activity_main_1x4);
            updateViews.setOnClickPendingIntent(R.id.button1, pending);
        }
        return updateViews;
    }
}