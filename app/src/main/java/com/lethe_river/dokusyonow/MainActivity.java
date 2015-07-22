package com.lethe_river.dokusyonow;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private static final int REQ_CODE = 1;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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
        if(resultCode != Activity.RESULT_OK || requestCode != REQ_CODE) {
            return;
        }

        String isbn = data.getStringExtra("SCAN_RESULT");
        ((EditText) findViewById(R.id.isbnEditText)).setText(isbn);

//        ((EditText) findViewById(R.id.testEditText)).setText(AuthActivity.getAuthData(this).toString());
        fillBookData(isbn);
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
                    final Drawable img = Drawable.createFromStream(is, "");

                    // ネットワークは別のスレッドからなのに描画はUスレッドから面倒
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
//          ((EditText) MainActivity.this.findViewById(R.id.testEditText)).setText(document.toString());
            XMLDocumentWrapper amazon = new XMLDocumentWrapper(document);

            if (amazon.get("/Items/Item/ItemAttributes/Title") == null) {
                Toast.makeText(MainActivity.this, "not found on Amazon", Toast.LENGTH_SHORT).show();
                return;
            }
            amazon.setBase("/Items/Item/ItemAttributes");
            ((EditText) findViewById(R.id.titleEditText)).setText(amazon.get("./Title"));
            ((EditText) findViewById(R.id.authorEditText)).setText(amazon.get("./Author"));
        }
    }
}
