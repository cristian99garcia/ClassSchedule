package com.cristiangarcia.classschedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

public class TimetableWidget extends AppWidgetProvider {

    private static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    private static final String ACTION_TIME_SET = "android.intent.action.TIME_SET";
    private static final String ACTION_SCHEDULED_UPDATE = "android.appwidget.action.SCHEDULED_UPDATE";
    private static final String ACTION_APPWIDGET_DELETED = "android.appwidget.action.APPWIDGET_DELETED";
    private static final String ACTION_APPWIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";
    private static final String ACTION_APPWIDGET_OPTIONS = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
    private static final String ACTION_CLICK_EDIT = "com.cristiangarcia.classschedule.WIDGET_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        super.onUpdate(context, manager, appWidgetIds);

        for (int appWidgetId: appWidgetIds) {
            Intent remoteViewsFactoryIntent = new Intent(context, TimetableWidgetService.class);
            remoteViewsFactoryIntent.putExtra("update", true);

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.timetable_widget);
            rv.setEmptyView(R.id.empty_view, R.id.widget_listview);
            rv.setRemoteAdapter(R.id.widget_listview, remoteViewsFactoryIntent);
            rv.setImageViewResource(R.id.widget_edit_button, R.drawable.ic_edit_24dp);

            Intent intent = new Intent(context, TimetableWidget.class);
            intent.setAction(ACTION_CLICK_EDIT);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_edit_button, pIntent);

            manager.updateAppWidget(appWidgetId, rv);
            manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview);
        }

        scheduleNextUpdate(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager manager;
        String action = intent.getAction();

        Log.d("onReceive", action);

        if (action == null)
            return;

        switch (action) {
            case ACTION_CLICK_EDIT:
                Intent _intent = new Intent(context, MainActivity.class);
                _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(_intent);

                break;

            case ACTION_APPWIDGET_UPDATE:
            case ACTION_TIME_SET:
            case ACTION_SCHEDULED_UPDATE:
            case ACTION_APPWIDGET_ENABLED:
            case ACTION_APPWIDGET_OPTIONS:

            manager = AppWidgetManager.getInstance(context);
            final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, TimetableWidget.class));

            for (int id: appWidgetIds)
                if (id != 0)
                    manager.notifyAppWidgetViewDataChanged(id, R.id.widget_listview);

            break;

            case ACTION_APPWIDGET_DELETED:
                Log.d("TimetableWidget", "Widget deleted");
                break;

            default:
                Log.d("TimetableWidget", "onReceive, no handled action: " + action);
                break;
        }
    }

    public static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimetableWidget.class);
        intent.setAction(ACTION_SCHEDULED_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 20);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1);

        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), pendingIntent);
        } catch (java.lang.NullPointerException e) {
            Pojo.addLog(context, e.getMessage());
        }
    }
}
