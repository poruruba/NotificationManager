package com.example.notificationcenter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, Handler.Callback {
    public static final String TAG = "LogTag";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    DetailListAdapter adapter;
    NotificationManager notificationManager;
    public static UIHandler handler;
    public static SharedPreferences pref;
    static final int DEFAULT_TIMEOUT = 10000;

    public static final String TOPIC = "fcm_notification";
    public static final String API_KEY = "12345678";
    public static final String BASE_URL = "http://【Node.jsサーバのホスト名】:20080";

    static public class NotificationItem{
        public long datetime;
        public String messageTitle;
        public String messageText;
        public String id;

        public NotificationItem(long datetime, String title, String text, String id){
            this.datetime = datetime;
            this.messageTitle = title;
            this.messageText = text;
            this.id = id;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new UIHandler(this);
        pref = getSharedPreferences("NotificationCenter", MODE_PRIVATE);

        adapter = new DetailListAdapter( this, R.layout.list_item, new int[] { R.id.txt_title, R.id.txt_datetime, R.id.txt_message } );
        ListView listView;
        listView = (ListView)findViewById( R.id.list_items );
        listView.setAdapter(adapter);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(TOPIC,"通知チャネル", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        final String[] texts = {
                "今日", "昨日", "今週", "今月", "先月", "今年", "昨年"
        };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, texts);
        Spinner spin;
        spin = (Spinner)findViewById(R.id.spin_target_range);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setOnItemSelectedListener(this);
        spin.setAdapter(arrayAdapter);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        Log.d(TAG, "token=" + token);
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg + " : " + TOPIC);
                    }
                });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            String datetime_str = intent.getStringExtra("datetime");

            if( title == null && body == null && datetime_str == null )
                return;
            
            Intent activity_intent = new Intent(this, NotificationActivity.class);
            activity_intent.putExtra("title", title);
            activity_intent.putExtra("body", body);
            activity_intent.putExtra("datetime", datetime_str);
            startActivity(activity_intent);
        }catch(Exception ex){
            Log.d(TAG, ex.getMessage());
        }
    }

    private void listUpdate(int position){
        Calendar cal_end = Calendar.getInstance();
        cal_end.set(Calendar.HOUR, 0);
        cal_end.set(Calendar.MINUTE, 0);
        cal_end.set(Calendar.SECOND, 0);
        cal_end.set(Calendar.MILLISECOND, 0);
        Calendar cal_start = (Calendar)cal_end.clone();
        switch(position){
            case 0: { // 今日
                cal_end.add(Calendar.DATE, 1);
                break;
            }
            case 1: { // 昨日
                cal_start.add(Calendar.DATE, -1);
                break;
            }
            case 2: { // 今週
                cal_end.add(Calendar.DATE, 1);
                cal_start.add(Calendar.DATE, -6);
                break;
            }
            case 3: { // 今月
                cal_end.add(Calendar.DATE, 1);
                cal_start.set(Calendar.DATE, 1);
                break;
            }
            case 4: { // 先月
                cal_end.set(Calendar.DATE, 1);
                cal_start.add(Calendar.MONTH, -1);
                cal_start.set(Calendar.DATE, 1);
                break;
            }
            case 5: { // 今年
                cal_end.add(Calendar.DATE, 1);
                cal_start.set(Calendar.MONTH, 0);
                cal_start.set(Calendar.DATE, 1);
                break;
            }
            case 6: { // 昨年
                cal_end.set(Calendar.MONTH, 0);
                cal_end.set(Calendar.DATE, 1);
                cal_start.add(Calendar.YEAR, -1);
                cal_start.set(Calendar.MONTH, 0);
                cal_start.set(Calendar.DATE, 1);
                break;
            }
        }
        long end = cal_end.getTimeInMillis();
        long start = cal_start.getTimeInMillis();

        new ProgressAsyncTaskManager.Callback(this, "通信中です。", null) {
            @Override
            public Object doInBackground(Object obj) throws Exception {
                JSONObject json = new JSONObject();
                json.put("topic", TOPIC);
                json.put("start", start);
                json.put("end", end);
                JSONObject response = HttpPostJson.doPost_withApikey(BASE_URL + "/notification-get-list", json, API_KEY, DEFAULT_TIMEOUT);
                return response;
            }

            @Override
            public void doPostExecute(Object obj) {
                if (obj instanceof Exception) {
                    Toast.makeText(getApplicationContext(), obj.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject response = (JSONObject)obj;
                    adapter.clear();
                    JSONArray list = response.getJSONArray("rows");
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject item = list.getJSONObject(i);
                        String[] params = new String[3];
                        params[0] = item.getString("client_id") + ":" + item.getString("messageTitle");
                        params[1] = sdf.format(item.getLong("datetime"));
                        params[2] = item.getString("messageText");
                        adapter.add(params);
                    }

                    ListView listView;
                    listView = (ListView) findViewById(R.id.list_items);
                    listView.deferNotifyDataSetChanged();

                    TextView textView;
                    textView = (TextView)findViewById(R.id.txt_list_message);
                    if( list.length() > 0 )
                        textView.setVisibility(View.GONE);
                    else
                        textView.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        };
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        listUpdate(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean handleMessage(@NonNull Message message) {
        if( message.what == UIHandler.MSG_ID_OBJ_BASE + 0 ){
            try {
                NotificationItem item = (NotificationItem) message.obj;

                Intent notifyIntent = new Intent(this, NotificationActivity.class);
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                notifyIntent.putExtra("title", item.messageTitle);
                notifyIntent.putExtra("body", item.messageText);
                notifyIntent.putExtra("datetime", String.valueOf(item.datetime));
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

                Notification.Builder builder = new Notification.Builder(this);
                builder.setContentTitle(item.messageTitle);
                builder.setContentText(item.messageText);
                builder.setSmallIcon(android.R.drawable.ic_popup_reminder);
                builder.setChannelId(TOPIC);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                Notification notification = builder.build();
                notificationManager.notify(1, notification);

                Spinner spin;
                spin = (Spinner) findViewById(R.id.spin_target_range);
                int position = spin.getSelectedItemPosition();
                listUpdate(position);

                return true;
            }catch(Exception ex){
                Log.d(TAG, ex.getMessage());
            }
        }

        return false;
    }
}
