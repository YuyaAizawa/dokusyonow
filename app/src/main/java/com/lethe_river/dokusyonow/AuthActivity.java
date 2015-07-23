package com.lethe_river.dokusyonow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;


public class AuthActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
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

    public void clear(View view) {
        ((EditText) findViewById(R.id.awsAccessKeyEditText)).setText("");
        ((EditText) findViewById(R.id.awsSecretKeyEditText)).setText("");
        ((EditText) findViewById(R.id.associateTagEditText)).setText("");

        ((EditText) findViewById(R.id.accessTokenEditText))       .setText("");
        ((EditText) findViewById(R.id.accessTokenSecretEditText)) .setText("");
        ((EditText) findViewById(R.id.consumerKeyEditText))      .setText("");
        ((EditText) findViewById(R.id.consumerKeySecretEditText)).setText("");
    }

    public void save(View view) {
        SharedPreferences.Editor editor = getSharedPreferences("authPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("awsAccessKey", ((EditText) findViewById(R.id.awsAccessKeyEditText)).getText().toString());
        editor.putString("awsSecretKey", ((EditText) findViewById(R.id.awsSecretKeyEditText)).getText().toString());
        editor.putString("associateTag", ((EditText) findViewById(R.id.associateTagEditText)).getText().toString());

        editor.putString("accessToken"      , ((EditText) findViewById(R.id.accessTokenEditText))      .getText().toString());
        editor.putString("accessTokenSecret", ((EditText) findViewById(R.id.accessTokenSecretEditText)).getText().toString());
        editor.putString("consumerKey"      , ((EditText) findViewById(R.id.consumerKeyEditText))      .getText().toString());
        editor.putString("consumerKeySecret", ((EditText) findViewById(R.id.consumerKeySecretEditText)).getText().toString());
        editor.commit();
    }

    public void load(View view) {
        Map<String, String> authData = getAuthData(this);
        SharedPreferences prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE);
        ((EditText) findViewById(R.id.awsAccessKeyEditText)).setText(authData.get("awsAccessKey"));
        ((EditText) findViewById(R.id.awsSecretKeyEditText)).setText(authData.get("awsSecretKey"));
        ((EditText) findViewById(R.id.associateTagEditText)).setText(authData.get("associateTag"));

        ((EditText) findViewById(R.id.accessTokenEditText))      .setText(authData.get("accessToken"));
        ((EditText) findViewById(R.id.accessTokenSecretEditText)).setText(authData.get("accessTokenSecret"));
        ((EditText) findViewById(R.id.consumerKeyEditText))      .setText(authData.get("consumerKey"));
        ((EditText) findViewById(R.id.consumerKeySecretEditText)).setText(authData.get("consumerKeySecret"));
    }

    public static Map<String, String> getAuthData(Activity activity) {
        Map<String, String> auth = new HashMap<>();
        SharedPreferences prefs = activity.getSharedPreferences("authPrefs", Context.MODE_PRIVATE);
        auth.put("awsAccessKey"     , prefs.getString("awsAccessKey", ""));
        auth.put("awsSecretKey"     , prefs.getString("awsSecretKey", ""));
        auth.put("associateTag"     , prefs.getString("associateTag", ""));

        auth.put("accessToken"      , prefs.getString("accessToken", ""));
        auth.put("accessTokenSecret", prefs.getString("accessTokenSecret", ""));
        auth.put("consumerKey"      , prefs.getString("consumerKey", ""));
        auth.put("consumerKeySecret", prefs.getString("consumerKeySecret", ""));
        return auth;
    }
}
