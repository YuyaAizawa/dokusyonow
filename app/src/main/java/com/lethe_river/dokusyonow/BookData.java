package com.lethe_river.dokusyonow;

import java.io.InputStream;

/**
 * Created by Yuya on 15/07/22.
 */
public class BookData {
    public final String date;
    public final String title;
    public final String author;
    public final String comment;
    public final InputStream imageStream;

    public BookData(String date, String title, String author, String comment, InputStream image) {
        this.date = date;
        this.title = title;
        this.author = author;
        this.comment = comment;
        this.imageStream = image;
    }
}
