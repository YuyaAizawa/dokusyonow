package com.lethe_river.dokusyonow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Yuya on 15/07/23.
 */
public class BookAdapter extends BaseAdapter {

    final Context context;
    final LayoutInflater inflater;
    List<BookData> bookList;

    public BookAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.bookList = new ArrayList<>();
    }

    public void reload(List<BookData> list) {
        bookList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public BookData getItem(int position) {
        return bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.book_list_row,parent,false);

        BookData book = bookList.get(position);

        ((TextView) convertView.findViewById(R.id.dateText)).setText(book.date);
        ((TextView) convertView.findViewById(R.id.titleAndAuthorText)).setText(book.title+", "+book.author);
        ((TextView) convertView.findViewById(R.id.listComment)).setText(book.comment);

        return convertView;
    }
}
