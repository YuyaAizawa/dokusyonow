package com.lethe_river.dokusyonow;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private static final int REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }
}
