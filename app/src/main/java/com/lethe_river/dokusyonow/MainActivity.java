package com.lethe_river.dokusyonow;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class MainActivity extends Activity {

    private static final int REQ_IMAGE = 0;
    private static final int REQ_CODE = 1;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    byte[] image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((EditText) findViewById(R.id.dateEditText)).setText(
                DATE_FORMAT.format(new Date(System.currentTimeMillis()))); // LocalDateがない！
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openAuthActivity(View view) {
        startActivity(new Intent(this, AuthActivity.class));
    }

    public void getBarcode(View view) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        try {
            startActivityForResult(intent, REQ_CODE);
        } catch (ActivityNotFoundException e){
            Toast.makeText(this, "Barcode Scanner not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQ_IMAGE:
                Bitmap bitmap = (Bitmap) data.getExtras().getParcelable("data");
                ImageView imageView = ((ImageView) findViewById(R.id.imageView));
                imageView.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                image = baos.toByteArray();
                return;


            case REQ_CODE:
                String isbn = data.getStringExtra("SCAN_RESULT");
                ((EditText) findViewById(R.id.isbnEditText)).setText(isbn);
                fillBookData(isbn);
                return;

            default:
        }
    }

    public void fillBookData(View view) {
        String isbn = ((EditText) findViewById(R.id.isbnEditText)).getText().toString();
        fillBookData(isbn);
    }

    public void fillBookData(String isbn) {
        Map<String, String> authData = AuthActivity.getAuthData(this);
        AmazonAPI.setKeys(
                authData.get("awsAccessKey"),
                authData.get("awsSecretKey"),
                authData.get("associateTag"));

        new DocumentGetter().execute(isbn);
    }

    public void tweet(View view) {
        String date = ((EditText) findViewById(R.id.dateEditText)).getText().toString();
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String author = ((EditText) findViewById(R.id.authorEditText)).getText().toString();
        String comment = ((EditText) findViewById(R.id.commentEditText)).getText().toString();

        InputStream is = image != null ? new ByteArrayInputStream(image) : null;

        BookData bookData = new BookData(date, title, author, comment, is);

        new Tweeter().execute(bookData);
    }

   public void getImageFromCamera(View view) {
       Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       intent.addCategory(Intent.CATEGORY_DEFAULT);

       startActivityForResult(intent, REQ_IMAGE);
   }

    void makeText(final String message) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    class DocumentGetter extends AsyncTask<String, Void, Document> {
        private String isbn;

        @Override
        protected Document doInBackground(String... params) {
            Document document = AmazonAPI.getDocument(Long.parseLong(params[0]));
            XMLDocumentWrapper amazon = new XMLDocumentWrapper(document);

            String urlString = amazon.get("/Items/Item/LargeImage/URL");
            if (urlString != null) {
                try {
                    InputStream is = new URL(urlString).openStream();

                    BufferedInputStream bis = new BufferedInputStream(is);

                    image = new byte[0xfffff];

                    for(int i = 0,b;(b = bis.read())!=-1;i++) {
                        image[i] = (byte)b;
                    }
                    if(bis.read()!=-1) {
                        image = null;
                        makeText(getString(R.string.image_too_big));
                    }

                    final Drawable img = Drawable.createFromStream(new ByteArrayInputStream(image), "");

                    // ネットワークは別のスレッドからなのに描画はUIスレッドから面倒
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView imageView = ((ImageView) findViewById(R.id.imageView));
                            imageView.setImageDrawable(img);
                            imageView.invalidate();
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            XMLDocumentWrapper amazon = new XMLDocumentWrapper(document);

            if (amazon.get("/Items/Item/ItemAttributes/Title") == null) {
                makeText(getString(R.string.not_found_on_amazon));
                return;
            }
            amazon.setBase("/Items/Item/ItemAttributes");
            ((EditText) findViewById(R.id.titleEditText)).setText(amazon.get("./Title"));
            ((EditText) findViewById(R.id.authorEditText)).setText(amazon.get("./Author"));
        }
    }

    class Tweeter extends AsyncTask<BookData, Void, Void> {
        @Override
        protected Void doInBackground(BookData... params) {
            BookData bookData = params[0];

            String message = bookData.comment + "[" + bookData.title + ", " + bookData.author + "]";

            StatusUpdate status = new StatusUpdate(message);
            if(bookData.imageStream != null) {
                status.media("book.jpg", bookData.imageStream);
            }
            ConfigurationBuilder cb = new ConfigurationBuilder();
            Map<String, String> map = AuthActivity.getAuthData(MainActivity.this);
            cb.setDebugEnabled(true)
                    .setOAuthAccessToken(map.get("accessToken"))
                    .setOAuthAccessTokenSecret(map.get("accessTokenSecret"))
                    .setOAuthConsumerKey(map.get("consumerKey"))
                    .setOAuthConsumerSecret(map.get("consumerKeySecret"));
            Twitter twitter = new TwitterFactory(cb.build()).getInstance();
            try {
                twitter.updateStatus(status);
            } catch (TwitterException e) {
                makeText(getString(R.string.tweet_error));
                e.printStackTrace();
            }
            makeText(bookData.title+getString(R.string.tweet_about));
            return null;
        }
    }
}
