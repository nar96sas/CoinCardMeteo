package coincard.applicationmeteo;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MeteoAdapter extends BaseAdapter {
    String list = "";
    private ArrayList<Meteo> favoriteList;
    private LayoutInflater inflater;
    private Context context;

    public MeteoAdapter(Context c, ArrayList<Meteo> fl){
        this.favoriteList = fl;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public int getCount(){
        return favoriteList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder{
        private ImageView img;
        private TextView nameCity;
        private TextView temperature;
        private ImageView btnRemove;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder h = new Holder();
        final Meteo favorite = this.favoriteList.get(position);
        //Get view of item
        final View rowView = this.inflater.inflate(R.layout.item_view, null);
        h.img = (ImageView) rowView.findViewById(R.id.iconWeather);
        h.nameCity = (TextView) rowView.findViewById(R.id.nameCity);
        h.temperature = (TextView) rowView.findViewById(R.id.temperature);
        h.btnRemove = (ImageView) rowView.findViewById(R.id.btnRemove);
        h.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = MainActivity.favorites.edit();

                list = MainActivity.favorites.getString("listIDCity", " ");
                int pos = list.indexOf(favorite.getId() + "");
                Toast.makeText(context, "Remove location", Toast.LENGTH_LONG).show();
                int tmp = pos+favorite.getId().length(); // TH ở cuối chuỗi hoặc giữa chuỗi
                list = list.substring(0,tmp+1 > list.length()? pos-1 : pos) + list.substring(tmp+1 > list.length() ? tmp : tmp+1);
                edit.putString("listIDCity", list);
                edit.commit();
                MainActivity.nbF--;
                MainActivity.nbFavorites.setText(MainActivity.nbF + "");

                if (MainActivity.txtCityName.getText().equals(favorite.getName()+","+favorite.getSys().getCountry()))
                    MainActivity.addFavorite.setImageResource(R.drawable.add_favorite);
                rowView.setVisibility(View.GONE);
            }
        });

        //Fill contents
        favorite.setIcon(favorite.getWeather().get(0).getMain());
        h.img.setImageResource(favorite.getIcon());
        h.nameCity.setText(favorite.getName());
        h.temperature.setText(favorite.getMain().getTemp()+"°C");

        return rowView;
    }
}
