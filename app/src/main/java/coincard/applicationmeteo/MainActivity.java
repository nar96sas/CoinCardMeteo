package coincard.applicationmeteo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import static coincard.applicationmeteo.R.id.drawer_layout;
import static coincard.applicationmeteo.R.id.time;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static TextView nbFavorites, txtCityName;
    TextView txtTemp, txtHumidity, txtPressure, txtDescription, txtGeo, txtDate, txtWind;
    static ImageView iconWeather,addFavorite;
    SearchView txtSearchValue;
    LinearLayout linearDays;
    String WEATHER_URL, FORECAST_URL, codeCity;
    static SharedPreferences favorites, lastSearch;
    City city;
    String list="";
    static int nbF = 0;
    ArrayList<Forecast> dayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //các sự kiện click của nút ở dưới
        // Map
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("lon", city.getLon());
                intent.putExtra("lat", city.getLat());
                intent.putExtra("name", city.getName());
                startActivity(intent);
            }
        });
        // Current location
        FloatingActionButton myLocation = (FloatingActionButton) findViewById(R.id.myLocation);
        assert myLocation != null;
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPSTracker gps = new GPSTracker(MainActivity.this);
                if(gps.canGetLocation()){
                    WEATHER_URL =
                            "http://api.openweathermap.org/data/2.5/weather?lon="+gps.getLongitude()+"&lat="+gps.getLatitude()+"&units=metric&appid=8b62177ed538309f1fe0756026559a29";
                    FORECAST_URL =
                            "http://api.openweathermap.org/data/2.5/forecast/daily?lon="+gps.getLongitude()+"&lat="+gps.getLatitude()+"&cnt=5&units=metric&appid=8b62177ed538309f1fe0756026559a29";
                    gps.stopUsingGPS();
                    new LongOperation().execute(WEATHER_URL, FORECAST_URL);
                }
                else gps.showSettingsAlert();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Resources res = getResources();
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.avatar);
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, src);
        dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight() / 2.0f));
        ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
        imageView.setImageDrawable(dr);

        nbFavorites = (TextView) navigationView.getMenu().findItem(R.id.nav_favorite).getActionView();
        //number favorites
        favorites = getSharedPreferences("my_favorites", Context.MODE_PRIVATE);
        if (favorites.contains("listIDCity")) {
            nbF = 1;
            list = favorites.getString("listIDCity", " ");
            for (int i = 0; i < list.length(); i++)
                if (list.charAt(i) == ',') nbF ++;
        } else nbF = 0;
        nbFavorites.setText(nbF+"");

        //Add to favorites
        addFavorite = (ImageView) findViewById(R.id.addFavorite);
        assert addFavorite != null;
        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = favorites.edit();

                if (favorites.contains("listIDCity")) {
                    list = favorites.getString("listIDCity", " ");
                    int pos = list.indexOf(city.getCode() + "");
                    if (pos == -1) {
                        Toast.makeText(getApplicationContext(), "Location added", Toast.LENGTH_LONG).show();
                        list += "," + city.getCode();
                        nbF++;
                        addFavorite.setImageResource(R.drawable.added);
                    } else {
                        Toast.makeText(getApplicationContext(), "Remove location", Toast.LENGTH_LONG).show();
                        int tmp = pos+city.getCode().length(); // TH ở cuối chuỗi hoặc giữa chuỗi
                        list = list.substring(0,tmp+1 > list.length()? pos-1 : pos) + list.substring(tmp+1 > list.length() ? tmp : tmp+1);
                        Log.e("jlasd",String.format("%s",list));
                        nbF--;
                        addFavorite.setImageResource(R.drawable.add_favorite);
                    }
                    edit.putString("listIDCity", list);
                    nbFavorites.setText(nbF + "");
                } else {
                    Toast.makeText(getApplicationContext(), "Location added", Toast.LENGTH_LONG).show();
                    edit.putString("listIDCity", city.getCode() + "");
                    addFavorite.setImageResource(R.drawable.added);
                    nbF++; nbFavorites.setText(nbF + "");
                }
                edit.commit();
            }
        });

        txtCityName = (TextView) findViewById(R.id.txtCityName);
        txtHumidity = (TextView) findViewById(R.id.txtHumidity);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtPressure = (TextView) findViewById(R.id.txtPressure);
        txtTemp = (TextView) findViewById(R.id.txtTemp);
        //txtWeather = (TextView) findViewById(R.id.txtMain);
        //txtGeo = (TextView) findViewById(R.id.txtGeo);
        txtWind = (TextView) findViewById(R.id.txtWind);
        //txtDate = (TextView) findViewById(R.id.txtDate);
        iconWeather = (ImageView) findViewById(R.id.iconWeather);
        linearDays = (LinearLayout) findViewById(R.id.linearDays);
        // Web service
        lastSearch = getSharedPreferences("last_search",Context.MODE_PRIVATE);
        //id=1566083=HCM
        WEATHER_URL =
                "http://api.openweathermap.org/data/2.5/weather?id="+ lastSearch.getString("cityId","1566083") +"&units=metric&appid=8b62177ed538309f1fe0756026559a29";
        FORECAST_URL =
                "http://api.openweathermap.org/data/2.5/forecast/daily?id="+ lastSearch.getString("cityId","1566083") +"&cnt=5&units=metric&appid=8b62177ed538309f1fe0756026559a29";
        // txtRequestUrl.setText(new Date() + "\n" + WEATHER_URL);
        // Use AsyncTask to execute potential slow task without freezing GUI
        new LongOperation().execute(WEATHER_URL, FORECAST_URL);
    }

    private class LongOperation extends AsyncTask<String, Void, Void> {
        private String jsonWeather, jsonForecast;
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            dialog.setMessage("Please wait..");
            dialog.show();
        }

        protected Void doInBackground(String... urls) {
            for(int i=0; i < 2; i++)
            try {
                // solution uses Java.Net class (Apache.HttpClient is now deprecated)
                // STEP1. Create a HttpURLConnection object releasing REQUEST to given site
                URL url = new URL(urls[i]); //argument supplied in the call to AsyncTask

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
                if (i==0) {
                    jsonWeather = strBuilder.toString();
                    Log.e("RESPONSE", jsonWeather);
                } else {
                    jsonForecast = strBuilder.toString();
                    Log.e("RESPONSE", jsonForecast);
                }

            } catch (Exception e) {
                Log.e("RESPONSE Error", e.getMessage());
            }
            return null; // needed to gracefully terminate Void method
        }

        protected void onPostExecute(Void unused) {
            try {
                dialog.dismiss();

                // update GUI with JSON Response
                //txtResponseJson.setText(jsonWeather);

                // Step4. Convert JSON list into a Java collection of Person objects
                // prepare to decode JSON response and create Java list
                //=================Cách 1
                if(jsonWeather != null) {
                    Gson gson = new Gson();
                    Meteo w = gson.fromJson(jsonWeather, Meteo.class);

                    city = new City(w.getId(), w.getName(), w.getSys().getCountry(), w.getCoord().getLon(), w.getCoord().getLat());

                    //Favorite button
                    list = favorites.getString("listIDCity", " ");
                    int imgFavorite = list.indexOf(city.getCode() + "") != -1 ? R.drawable.added : R.drawable.add_favorite;
                    addFavorite.setImageResource(imgFavorite);

                    //txtGeo.setText("[" + city.getLon() + ", " + city.getLat() + "]");
                    txtCityName.setText((city.getName() + "," + city.getCountry()).toUpperCase());
                    txtTemp.setText(Math.round(w.getMain().getTemp())+"°");
                    txtDescription.setText(w.getWeather().get(0).getDescription());
                    txtPressure.setText(w.getMain().getPressure() + " hpa");
                    txtHumidity.setText(w.getMain().getHumidity() + " %");
                    txtWind.setText(w.getWind().getSpeed() + " m/s");
                    w.setIcon(w.getWeather().get(0).getMain());
                    iconWeather.setImageResource(w.getIcon());

                    //save to SharePrefs
                    SharedPreferences.Editor editor = lastSearch.edit();
                    editor.putString("cityId",w.getId()+"");
                    editor.putString("cityName",w.getName());
                    editor.commit();

                    //Print forecast of another days
                    JsonElement jelement = new JsonParser().parse(jsonForecast);
                    JsonObject jobject = (JsonObject) jelement;
                    Type listType = new TypeToken<ArrayList<Forecast>>(){}.getType();
                    dayList = gson.fromJson(jobject.getAsJsonArray("list"), listType);
                    linearDays.removeAllViews();
                    for (int i=1; i < dayList.size(); i++){
                        Forecast dayI = dayList.get(i);
                        LinearLayout LLdayI = new LinearLayout(getApplicationContext());
                        ImageView iconDayI = new ImageView(getApplicationContext());
                        TextView txtTempI = new TextView(getApplicationContext());
                        TextView txtDayI = new TextView(getApplicationContext());

                        //Custom
                        LLdayI.setOrientation(LinearLayout.VERTICAL);
                        LLdayI.setBackgroundResource(R.drawable.background_day);
                        LLdayI.setPadding(0, 30, 0, 0);
                        LLdayI.setWeightSum(3);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 0.25f);
                        LLdayI.setLayoutParams(params);

                        dayI.setIcon(dayI.getWeather().get(0).getMain());
                        iconDayI.setImageResource(dayI.getIcon());
                        LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
                        iconDayI.setLayoutParams(paramss);

                        txtTempI.setText(Math.round(dayI.getTemp().getDay()) + "°C");
                        txtTempI.setTextColor(Color.rgb(112, 111, 108));
                        txtTempI.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        txtTempI.setLayoutParams(paramss);

                        txtDayI.setText(dayI.getDay());
                        txtDayI.setTextColor(Color.rgb(77, 95, 107));
                        txtDayI.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        txtDayI.setLayoutParams(paramss);

                        LLdayI.addView(txtDayI);
                        LLdayI.addView(iconDayI);
                        LLdayI.addView(txtTempI);
                        linearDays.addView(LLdayI);
                    }
                }
                //=================Cách 2
                /*
                Gson gson = new Gson();
                String city = "";
                String temp = "";
                String type = "";
                String cloudiness = "";
                String pressure = "";
                String humidity= "";

                try {
                    JsonElement jelement = new JsonParser().parse(jsonWeather);
                    JsonObject jobject = (JsonObject) jelement;
                    //City Name
                    JsonObject objSys = (JsonObject) jobject.get("sys");
                    city +=  jobject.get("name").toString().replace("\"", " ") + "," + objSys.get("country").toString().replace("\"", " ") + "\n";
                    //Temperature, Pressure, Humidity
                    JsonObject objMain = (JsonObject) jobject.get("main");
                    temp += objMain.get("temp").toString() + "°C \n";
                    pressure += objMain.get("pressure").toString() + "hpa \n";
                    humidity += objMain.get("humidity").toString() + "% \n";
                    //Type weather, cloudiness
                    JsonArray objWeather = (JsonArray) jobject.get("weather");
                    JsonObject objType = (JsonObject) objWeather.get(0);
                    type += objType.get("main").toString().replace("\"", " ") + "\n";
                    cloudiness += objType.get("description").toString().replace("\"", " ") + "\n";
                } catch (Exception e) {
                    Log.e("PARSING", e.getMessage());
                }
                txtCityName.setText(city);
                txtTemp.setText(temp);
                txtWeather.setText(type);
                txtDescription.setText(cloudiness);
                txtPressure.setText(pressure);
                txtHumidity.setText(humidity);*/
            } catch (JsonSyntaxException e) {
                Log.e("POST-Execute", e.getMessage());
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        txtSearchValue = (SearchView) menu.findItem(R.id.action_search).getActionView();
        txtSearchValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                invalidateOptionsMenu();
                txtSearchValue.setQuery("", false);
                codeCity = query.replaceAll(" ","");
                WEATHER_URL =
                        "http://api.openweathermap.org/data/2.5/weather?q="+ codeCity +"&units=metric&appid=8b62177ed538309f1fe0756026559a29";
                FORECAST_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?q="+ codeCity +"&cnt=5&units=metric&appid=8b62177ed538309f1fe0756026559a29";
                new LongOperation().execute(WEATHER_URL, FORECAST_URL);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        else if(id == R.id.action_settings){
            Toast.makeText(getApplicationContext(),"Je clique sur Settings", Toast.LENGTH_LONG).show();
            return true;
        }
        else if(id == R.id.action_about){
            Toast.makeText(getApplicationContext(),"Je clique sur About", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorite) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_home) {
            onBackPressed();
        } else if (id == R.id.nav_search) {
            Toast.makeText(getApplicationContext(),"Je clique sur Search", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_notification) {
            Toast.makeText(getApplicationContext(),"Je clique sur Notification", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(getApplicationContext(),"Je clique sur Settings", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_about) {
            Toast.makeText(getApplicationContext(),"Je clique sur About", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
