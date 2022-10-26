package com.skydrm.rmc.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skydrm.rmc.R;

import java.util.List;

/**
 * Created by aning on 11/8/2017.
 */

public class ExpirySelectAdapter extends BaseAdapter{

    private Context context;
    private List<String>  data;

    public ExpirySelectAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.expiryName = (TextView)convertView.findViewById(R.id.item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.expiryName.setText(data.get(position));
        return convertView;
    }

    private class ViewHolder{
        TextView expiryName;
    }
}
