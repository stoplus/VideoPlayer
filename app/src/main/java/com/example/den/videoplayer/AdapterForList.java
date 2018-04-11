package com.example.den.videoplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AdapterForList extends BaseAdapter {
    private List<String> list;
    private LayoutInflater inflater; // для загрузки разметки элемента
    private Context context;

    public AdapterForList(Context context, List<String> list) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    private class ViewHolder {
        final TextView name;


        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.adm_adapter_reference_name);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder; //объект класса ViewHolder

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_reference, parent, false);
            viewHolder = new AdapterForList.ViewHolder(convertView); // создать viewholder
            convertView.setTag(viewHolder);  // сохранить ссылки на элементы разметки
        } else {
            // читаем сохраненный элемент
            viewHolder = (AdapterForList.ViewHolder) convertView.getTag();
        } // if

        // связать отображаемые элементы и значения полей
        viewHolder.name.setText(list.get(pos));

        // вернуть ссылку на сформированный элемент интерфейса
        return convertView;
    } // getView
}
