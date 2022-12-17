package com.example.notificationcenter;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = MainActivity.TAG;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent intent = getIntent();
        if( intent != null ) {
            try{
                String title = intent.getStringExtra("title");
                String body = intent.getStringExtra("body");
                String datetime_str = intent.getStringExtra("datetime");

                TextView text;
                text = (TextView) findViewById(R.id.txt_message_title);
                text.setText(title);
                text = (TextView) findViewById(R.id.txt_message_text);
                text.setText(body);
                text = (TextView) findViewById(R.id.txt_message_datetime);
                long datetime = Long.parseLong(datetime_str);
                text.setText(sdf.format(datetime));
            }catch(Exception ex){
                Log.d(TAG, ex.getMessage());
            }
        }

        Button btn;
        btn = (Button)findViewById(R.id.btn_message_close);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_message_close:{
                finish();
                break;
            }
        }
    }
}