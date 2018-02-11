package com.vlousada.legacylenses;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;


public class ListAdapterSpecial extends BaseAdapter {
    List<ParsedDataSet> data_list;
    Activity activity;

    public ListAdapterSpecial(Activity activity, List<ParsedDataSet> data_list) {
        this.activity = activity;
        this.data_list = data_list;
    }

    @Override
    public int getCount() {
        return data_list.size();
    }

    @Override
    public Object getItem(int position) {
        return data_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_special, null);

        TextView specialView = (TextView) convertView.findViewById(R.id.special);
        TextView mathView = (TextView) convertView.findViewById(R.id.math);
        TextView descripView = (TextView) convertView.findViewById(R.id.description);


        final ParsedDataSet parsedDataSet = data_list.get(position);

        specialView.setText(parsedDataSet.getName());
        mathView.setText(parsedDataSet.getMath());
        descripView.setText(parsedDataSet.getDescription());


        return convertView;
    }
}

