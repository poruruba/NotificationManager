package com.example.notificationuploader;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;

public class MyNotificationListenerService extends NotificationListenerService {
    public static final String TAG = MainActivity.TAG;
    public static final int DEFAULT_TIMEOUT = 10000;

    NotificationDbHelper helper;
    SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "MyNotificationListenerService onCreate");

        helper = new NotificationDbHelper(this);
        db = helper.getReadableDatabase();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG,"onNotificationPosted");
        processNotification(sbn, true);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG,"onNotificationRemoved");
        processNotification(sbn, false);
    }

    private void processNotification( StatusBarNotification sbn, boolean posted ) {
        int id = sbn.getId();
        String packageName = sbn.getPackageName();
        String groupKey = sbn.getGroupKey();
        String key = sbn.getKey();
        String tag = sbn.getTag();
        long time = sbn.getPostTime();

        Log.d(TAG,"id:" + id + " packageName:" + packageName + " posted:" + posted + " time:" +time);
        Log.d(TAG,"groupKey:" + groupKey + " key:" + key + " tag:" + tag);

        if( !posted )
            return;

        try {
            Notification notification = sbn.getNotification();
            CharSequence tickerText = notification.tickerText;
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            CharSequence infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            Log.d(TAG, "Title:" + title + " Text:" + text + " subText:" + subText + " infoText:" + infoText + " tickerText:" + tickerText);

            String appName = helper.hasPackageName(db, packageName );
            if( appName != null ) {
                String message_body;
                if( text != null ){
                    if( title != null )
                        message_body = title + ":" + text;
                    else
                        message_body = text.toString();
                }else {
                    if (tickerText != null) {
                        message_body = tickerText.toString();
                    } else {
                        message_body = subText.toString();
                        if (title != null)
                            message_body = title + ":" + message_body;
                        if (infoText != null)
                            message_body = message_body + ":" + infoText;
                    }
                }
                long datetime = Calendar.getInstance().getTimeInMillis();
                JSONObject request = new JSONObject();
                request.put("client_id", MainActivity.CLIENT_ID);
                request.put("topic", MainActivity.TOPIC);
                request.put("title", appName);
                request.put("body", message_body);
                request.put("datetime", datetime);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject response = HttpPostJson.doPost_withApikey(MainActivity.BASE_URL + "/notification-push-message", request, MainActivity.API_KEY, DEFAULT_TIMEOUT);
                            Log.d(TAG, "HttpPostJson.doPost_withApikey OK");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
}
