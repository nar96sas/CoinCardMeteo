package coincard.applicationmeteo;

import java.util.ArrayList;

class Wind{
    private String speed;

    public String getSpeed() {
        return speed;
    }
}

class Main{
    private float temp;
    private String pressure;
    private String humidity;

    public float getTemp() {
        return temp;
    }

    public String getPressure() {
        return pressure;
    }

    public String getHumidity() {
        return humidity;
    }
}

class Coord{
    private float lon;
    private float lat;

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }
}

class Weather{
    private String main;
    private String description;

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }
}

class Sys{
    private String country;

    public String getCountry() {
        return country;
    }
}

public class Meteo {
    private String id;
    private String name;
    private Sys sys;
    private ArrayList<Weather> weather;
    private ArrayList<Meteo> list;
    private Main main;
    private Wind wind;
    private Coord coord;
    private int icon;

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Coord getCoord() {
        return coord;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public ArrayList<Meteo> getList() {
        return list;
    }

    public Sys getSys() {
        return sys;
    }

}
