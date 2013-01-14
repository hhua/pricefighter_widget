package com.android.ebay.widgetbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.ebay.msif.core.ServiceCallback;
import org.ebay.msif.core.ServiceClientException;
import org.ebay.msif.core.ServiceResponse;

import com.android.ebay.widgetbar.R;
import com.android.ebay.widgetbar.WidgetDemo;
import com.ebay.services.merchandising.GetMostWatchedItemsRequest;
import com.ebay.services.merchandising.Item;
import com.ebay.services.merchandising.MerchandisingServiceItemResponse;
import com.ebay.services.merchandising.client.MerchandisingServiceClient;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class LoadItemsService extends Service {

	private static List<Object> itemTitles=new ArrayList<Object>();
	private static List<Bitmap> itemPics=new ArrayList<Bitmap>();
	private static List<Integer> itemLoading = new ArrayList<Integer>(); 
	private static final String TAG="LoadItemService";
	private final MerchandisingServiceClient client=new MerchandisingServiceClient(this);
	private static int itemCount=20;
	private Timer timer;
	private Timer timerLoading;
	private boolean isError=false;
	private int pos=-1;
	private int posLoading=-1;
	private boolean isLoading=true;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void getMostWatchedItemsFromEbay()
	{
		GetMostWatchedItemsRequest request=new GetMostWatchedItemsRequest();
		request.setMaxResults(itemCount);
		try
		{
			client.getMostWatchedItems(request, new ServiceCallback<MerchandisingServiceItemResponse>() {
				
				@Override
				public void onResponse(
						ServiceResponse<MerchandisingServiceItemResponse> arg0) {
					// TODO Auto-generated method stub
					
					
					
					if(arg0.hasErrors())
					{
						itemTitles.add(arg0.getErrors().get(0).message);
						isError=true;
					}
					else
					{
						List<Item> items = arg0.getResponseData().getItemRecommendations().getItem();
						for(Item item:items)
						{
							itemTitles.add(item);
							isError=false;
						}
					}
					loadItemImages();
					timerLoading.cancel();
					timerLoading.purge();
					Log.i(TAG, (timerLoading==null)+"");
					timerLoading=null;
				}
			});
		}
		catch(ServiceClientException ex)
		{
			itemTitles.add(ex.getMessage());
			isError=true;
		}
	}
	
	private void loadItemImages()
	{
		if(!itemTitles.isEmpty())
		{
			for(int i=0;i<itemCount;i++)
			{
				Item item=(Item)itemTitles.get(i);
				String imageUrl=item.getImageURL();
				URL fileUrl=null;
				Bitmap bitmap=null;
				try
				{
					fileUrl=new URL(imageUrl);
				}
				catch(MalformedURLException ex)
				{
					Log.e(TAG, ex.getMessage());
				}
				try
				{
					HttpURLConnection conn=(HttpURLConnection)fileUrl.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream is=conn.getInputStream();
					int length=(int)conn.getContentLength();
					if(length!=-1)
					{
						byte[] imgData=new byte[length];
						byte[] buffer=new byte[512];
						int readLen=0;
						int destPos=0;
						while((readLen=is.read(buffer))>0)
						{
							System.arraycopy(buffer, 0, imgData, destPos, readLen);
							destPos+=readLen;
						}
						bitmap=BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
					}
				}
				catch(IOException ex)
				{
					Log.e(TAG, ex.getMessage());
				}
				itemPics.add(bitmap);
			}
		}
	}
	
	public void onCreate()
	{
		if(!isLoading)
			return;
		if(itemLoading.size() == 0){
			itemLoading.add(R.drawable.btn_loading_1);
			itemLoading.add(R.drawable.btn_loading_2);
			itemLoading.add(R.drawable.btn_loading_3);
			itemLoading.add(R.drawable.btn_loading_4);
			itemLoading.add(R.drawable.btn_loading_5);
			itemLoading.add(R.drawable.btn_loading_6);
			itemLoading.add(R.drawable.btn_loading_7);
			itemLoading.add(R.drawable.btn_loading_8);
		}
		
		if(timerLoading == null){
			posLoading = 0;
			timerLoading = new Timer();
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			timerLoading.scheduleAtFixedRate(new LoadingTimeTask(this, appWidgetManager), 0, 500);
		}
		
		if(itemTitles!=null&&!itemTitles.isEmpty())
		{
			itemTitles.clear();
		}
		if(itemPics!=null&&!itemPics.isEmpty())
		{
			itemPics.clear();
		}
		getMostWatchedItemsFromEbay();
//		timerLoading.cancel();
//		timerLoading.purge();
	}
	
	public void onStart(Intent intent,int startId)
	{
		Log.d(TAG, "Service Start");
		String action=intent.getAction();
		if(action!=null&&action.equals(Globals.APP_WIDGET_VIEW_ITEM))
		{
			openUrl();
		}
		if(action!=null&&action.equals(Globals.APP_WIDGET_VIEW_SEARCH))
		{
			Log.d(TAG, "start search");
			openSearch();
//			openSearchUrl();
		}
		if(action!=null&&action.equals(Globals.APP_WIDGET_VIEW_REFRESH))
		{
			if(!isLoading)
			{
				isLoading = true;
				pos=0;
				posLoading=0;
				onCreate();
			}
		}
		if(timer==null)
		{
			timer=new Timer();
			AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(this);
			timer.scheduleAtFixedRate(new MyTimeTask(this, appWidgetManager), 0, 6500);
		}
	}
	
	public class LoadingTimeTask extends TimerTask{
		RemoteViews remoteViews;
		AppWidgetManager appWidgetManager;
		ComponentName thisWidget;
		Context context;
		
		public LoadingTimeTask(Context context, AppWidgetManager appWidgetManager){
			this.context=context;
			this.appWidgetManager=appWidgetManager;
			thisWidget=new ComponentName(context, WidgetDemo.class);
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			remoteViews=new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			
			Log.d(TAG, "loading position is now at " + posLoading);
			Log.d(TAG, "Res id is now" + itemLoading.get(posLoading));
			remoteViews.setTextViewText(R.id.tv01, "Just a few seconds! Please wait patiently!");
			//remoteViews.setTextViewText(R.id.tv_Price, "");
			remoteViews.setImageViewResource(R.id.tv_image, itemLoading.get(posLoading));
			remoteViews.setTextViewText(R.id.tv_pos, "");
			posLoading++;
			if(posLoading >= itemLoading.size())
				posLoading = 0;
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		}
		
	}
	
	public class MyTimeTask extends TimerTask
	{
		RemoteViews remoteViews;
		AppWidgetManager appWidgetManager;
		ComponentName thisWidget;
		Context context;
		
		public MyTimeTask(Context context, AppWidgetManager appWidgetManager)
		{
			this.context=context;
			this.appWidgetManager=appWidgetManager;
			thisWidget=new ComponentName(context, WidgetDemo.class);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			remoteViews=new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			if(!itemTitles.isEmpty()&&itemPics.size()==itemCount)
			{
				isLoading=false;
				pos+=1;
				Log.d(TAG, "Now the pos is:"+String.valueOf(pos));
				if(pos==itemTitles.size())
				{
					pos=0;
				}
				if(isError)
				{
					remoteViews.setTextViewText(R.id.tv01, (String)itemTitles.get(pos));
					remoteViews.setImageViewResource(R.id.tv_image, R.drawable.btn_loading_1);
				}
				else
				{
					Item item=(Item)itemTitles.get(pos);
					Log.i(TAG, item.getSubtitle()+","+item.getPrimaryCategoryName());
					remoteViews.setTextViewText(R.id.tv01, item.getTitle()+"\nPrice:$"+String.format("%.2f", item.getBuyItNowPrice().getValue()));
					//remoteViews.setTextViewText(R.id.tv_Price, "Price:$"+String.format("%.2f", item.getBuyItNowPrice().getValue()));
					remoteViews.setImageViewBitmap(R.id.tv_image, itemPics.get(pos));
				}
				String str=null;
				str=String.valueOf(pos+1)+"/"+String.valueOf(itemCount);
				remoteViews.setTextViewText(R.id.tv_pos, str);
			}
			else
			{
				isLoading=true;
				remoteViews.setTextViewText(R.id.tv01, "Just a few seconds! Please wait patiently!");
				//remoteViews.setTextViewText(R.id.tv_Price, "");
				remoteViews.setImageViewResource(R.id.tv_image, R.drawable.btn_loading_1);
				remoteViews.setTextViewText(R.id.tv_pos, "");
			}
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		}
	}
	
	private void openUrl()
	{
		if(!isLoading)
		{
			Item item=(Item)itemTitles.get(pos);
			String subCategory=item.getTitle();
			Bundle data = new Bundle();
			data.putString("Keywords", subCategory);
			Intent intent=new Intent();
			intent.setClassName("com.ebay.skunk", "com.ebay.skunk.ResultListActivity");
			intent.putExtra("newKW", data);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction("android.intent.action.VIEW");
			startActivity(intent);
		}
	}
	
	private void openSearch()
	{
		if(!isLoading)
		{
			Intent intent=new Intent();
			intent.setClassName("com.ebay.skunk", "com.ebay.skunk.SearchDemoActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
		}
	}
	
	private void openSearchUrl()
	{
		if(!isLoading)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://hp.mobileweb.ebay.com/home"));
			intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
		}
	}
	
	public void onDestroy()
	{
		Log.i(TAG, "service stopped");
		timer.cancel();
		timer.purge();
		super.onDestroy();
	}
}
