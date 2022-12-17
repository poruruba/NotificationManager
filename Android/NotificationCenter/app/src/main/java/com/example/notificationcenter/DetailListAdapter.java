package com.example.notificationcenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DetailListAdapter extends ArrayAdapter<String[]>{
    public int[] idList;
    LayoutInflater mInflater;
    int layout;

    public DetailListAdapter(Context context, int layout, int[] idList)
    {
        super(context, 0);
        this.layout = layout;
        mInflater = LayoutInflater.from(context);
        this.idList = idList.clone();
    }

    public String getString( int position, int index )
    {
        String[] item = getItem(position);
        if( item == null )
            return null;
        return item[index];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = mInflater.inflate(layout, parent, false);
        }

        final String[] item = getItem(position);

        int i = 0;
        for( i = 0 ; i < idList.length && i < item.length ; i++ ) {
            final TextView text = (TextView)convertView.findViewById( idList[i] );
            text.setText( item[i] );
        }

        for( ; i < idList.length ; i++ ) {
            final TextView text = (TextView)convertView.findViewById( idList[i] );
            text.setText( "" );
        }

        return convertView;
    }
}