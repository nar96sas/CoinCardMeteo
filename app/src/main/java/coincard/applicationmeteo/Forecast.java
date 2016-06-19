package coincard.applicationmeteo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

class Temp{
    private float day;

    public float getDay() {
        return day;
    }
}

public class Forecast {
    private long dt;
    private String day;
    private Temp temp;
    private ArrayList<Weather> weather;
    private int icon;

    public long getDt() {
        return dt;
    }

    public String getDay(){
        Date date = new Date(dt * 1000L); //convert timestamp to millisecond
        switch (date.getDay()){
            case 0 : day = "SUN"; break;
            case 1 : day = "MON"; break;
            case 2 : day = "TUE"; break;
            case 3 : day = "WED"; break;
            case 4 : day = "THU"; break;
            case 5 : day = "FRI"; break;
            case 6 : day = "SAT"; break;
        }
        return day;
    }

    public Temp getTemp() {
        return temp;
    }

    public ArrayList<Weather> getWeather() {
        return weather;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(String typeWeather) {
        switch(typeWeather){
            case "Clouds":
                this.icon = R.drawable.cloud;
                break;
            case "Clear":
                this.icon = R.drawable.clear;
                break;
            case "Rain":
                this.icon = R.drawable.rain;
                break;
            case "Snow":
                this.icon = R.drawable.snow;
                break;
            case "Drizzle":
                this.icon = R.drawable.drizzle;
                break;
            case "Mist":
                this.icon = R.drawable.mist;
                break;
            case "Thunderstorm":
                this.icon = R.drawable.thunderstorm;
                break;
        }
    }
}
