package com.lethe_river.dokusyonow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HistoryActivity extends ActionBarActivity {

    BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle("History");

        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new BookAdapter(this);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        List<BookData> history = loadHistory(this);
        if(history != null) {
            adapter.reload(history);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<BookData> history = loadHistory(this);
        if(history != null && adapter != null) {
            adapter.reload(history);
        }
    }

    public static List<BookData> loadHistory(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("historyPrefs", Context.MODE_PRIVATE);
        String str = prefs.getString("data","");
        if(str.equals("")) {
            return null;
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT));
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (List<BookData>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveHistory(List<BookData> list, Activity activity) {
        SharedPreferences.Editor editor = activity.getSharedPreferences("historyPrefs", Context.MODE_PRIVATE).edit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(list);
            String str = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            editor.putString("data", str);
            editor.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addBook(BookData book, Activity activity) {
        List<BookData> list = loadHistory(activity);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(new BookData(book.date, book.title, book.author, book.comment, null));
        saveHistory(list, activity);
    }
}
