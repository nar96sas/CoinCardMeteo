package coincard.applicationmeteo;

public class City {
    private String code;
    private String name;
    private String country;
    private float lon;
    private float lat;

    public City(){

    }

    public City(String c, String n, String ct, float lo, float la){
        code = c;
        name = n;
        country = ct;
        lon = lo;
        lat = la;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }

}
