package coincard.applicationmeteo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class FavoriteActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Meteo> meteoList;
    String SERVER_URL="";
    SharedPreferences listID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        listView = (ListView) findViewById(R.id.listView);
        Toolbar toolbarFavorite = (Toolbar) findViewById(R.id.toolbarFavorites);
        setSupportActionBar(toolbarFavorite);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Run web service
        listID = getSharedPreferences("my_favorites", Context.MODE_PRIVATE);
        if (listID.contains("listIDCity"))
            SERVER_URL =
                "http://api.openweathermap.org/data/2.5/group?id="+listID.getString("listIDCity","")+"&units=metric&appid=8b62177ed538309f1fe0756026559a29";
        new LongOperation().execute(SERVER_URL);
    }

    private class LongOperation extends AsyncTask<String, Void, Void> {
        private String jsonResponse;

        protected Void doInBackground(String... urls) {
            try {
                // solution uses Java.Net class (Apache.HttpClient is now deprecated)
                // STEP1. Create a HttpURLConnection object releasing REQUEST to given site
                URL url = new URL(urls[0]); //argument supplied in the call to AsyncTask
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "");
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                // STEP2. wait for incoming RESPONSE stream, place data in a buffer
                InputStream isResponse = urlConnection.getInputStream();
                BufferedReader responseBuffer = new BufferedReader(new
                        InputStreamReader(isResponse));

                // STEP3. Arriving JSON fragments are concatenate into a StringBuilder
                String myLine = "";
                StringBuilder strBuilder = new StringBuilder();
                while ((myLine = responseBuffer.readLine()) != null) {
                    strBuilder.append(myLine);
                }
                //show response (JSON encoded data)
                jsonResponse = strBuilder.toString();
                Log.e("RESPONSE", jsonResponse);
            } catch (Exception e) {
                Log.e("RESPONSE Error", e.getMessage());
            }
            return null; // needed to gracefully terminate Void method
        }

        protected void onPostExecute(Void unused) {
            if(jsonResponse != null) {
                Gson gson = new Gson();
                JsonElement jelement = new JsonParser().parse(jsonResponse);
                JsonObject jobject = (JsonObject) jelement;

                Type listType = new TypeToken<ArrayList<Meteo>>(){}.getType();
                meteoList = gson.fromJson(jobject.getAsJsonArray("list"), listType);
                listView.setAdapter(new MeteoAdapter(FavoriteActivity.this, meteoList));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}