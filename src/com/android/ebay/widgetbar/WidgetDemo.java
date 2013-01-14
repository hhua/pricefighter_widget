package com.android.ebay.widgetbar;

import java.util.Timer;
import java.util.TimerTask;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetDemo extends AppWidgetProvider {
	
	private final ComponentName thisWidget=new ComponentName("com.android.ebay.widgetbar", "com.android.ebay.widgetbar.WidgetDemo");

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.d("MyWidget", "启动服务");
		defaultWidget(context, appWidgetIds);
		context.startService(new Intent(context, LoadItemsService.class));
	}
	
	private void defaultWidget(Context context, int[] appWidgetIds)
	{
		final RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		LinkControl(context, remoteViews);
		pushUpdate(context, appWidgetIds, remoteViews);
	}
	
	private void LinkControl(Context context, RemoteViews remoteViews)
	{
		Intent intent;
		Intent intent2;
		Intent intent3;
		PendingIntent pendingIntent;
		PendingIntent pendingIntent2;
		PendingIntent pendingIntent3;
		final ComponentName serviceName=new ComponentName(context, LoadItemsService.class);
		intent=new Intent(Globals.APP_WIDGET_VIEW_ITEM);
		intent.setComponent(serviceName);
		pendingIntent=PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		intent2=new Intent(Globals.APP_WIDGET_VIEW_REFRESH);
		intent2.setComponent(serviceName);
		pendingIntent2=PendingIntent.getService(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
		intent3=new Intent(Globals.APP_WIDGET_VIEW_SEARCH);
		intent3.setComponent(serviceName);
		pendingIntent3=PendingIntent.getService(context, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.layout_view, pendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.btn_Refresh, pendingIntent2);
		//remoteViews.setOnClickPendingIntent(R.id.tv_search, pendingIntent3);
		remoteViews.setOnClickPendingIntent(R.id.btn_Search, pendingIntent3);
	}
	
	private void pushUpdate(Context context,int[] appWidgetIds ,RemoteViews remoteViews)
	{
		final AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
		if(appWidgetIds!=null)
		{
			appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		}
		else
		{
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		}
	}
	
	private boolean hasInstances(Context context)
	{
		AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
		int[] appWidgetIds=appWidgetManager.getAppWidgetIds(thisWidget);
		Log.i("MyTag", String.valueOf(appWidgetIds.length));
		return appWidgetIds.length>0;
	}
	
	public void onDeleted (Context context, int[] appWidgetIds)
	{
		if(!hasInstances(context))
		{
			context.stopService(new Intent(context, LoadItemsService.class));
		}
		super.onDeleted(context, appWidgetIds);
	}
	
}
