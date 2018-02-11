package com.vlousada.legacylenses;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import java.util.List;


public class ListAdapterLenses extends BaseAdapter {
    List<ParsedDataSet> data_list;
    Activity activity;

    public ListAdapterLenses(Activity activity, List<ParsedDataSet> data_list) {
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
            convertView = inflater.inflate(R.layout.list_lenses, null);

        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        TextView mountView = (TextView) convertView.findViewById(R.id.mount);
        TextView focalView = (TextView) convertView.findViewById(R.id.focal);
        TextView apertureView = (TextView) convertView.findViewById(R.id.apertures);

        final ParsedDataSet parsedDataSet = data_list.get(position);

        nameView.setText(parsedDataSet.getName());
        mountView.setText("["+ parsedDataSet.getMount() + "]");
        focalView.setText(parsedDataSet.getFocal());
        apertureView.setText(parsedDataSet.getApertures());

        return convertView;
    }
}

