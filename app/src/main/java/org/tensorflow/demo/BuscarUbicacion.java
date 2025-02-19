package org.tensorflow.demo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BuscarUbicacion extends AppCompatActivity {
    ProgressDialog pg;
    Geocoder geocoder;
    boolean sino=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_ubicacion);
        pg=new ProgressDialog(this);
        pg.setMessage("Buscándote...");
        pg.show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            geocoder = new Geocoder(this, Locale.getDefault());

            locationStart();
        }
    }
    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    //           mensaje2.setText("Mi direccion es: \n"
                    //                 + DirCalle.getAddressLine(0));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        BuscarUbicacion mainActivity;

        public BuscarUbicacion getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(BuscarUbicacion mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            try {
                pg.setMessage("Te encontramos.");
                loc.getLatitude();
                loc.getLongitude();
                if (sino) {
                    pg.hide();
                    String lon = String.valueOf(loc.getLongitude());
                    String lat = String.valueOf(loc.getLatitude());
                    Log.i("AQUI",lon+""+lat);
                    /*
                    Intent i = new Intent(getApplicationContext(), MapasMeterUbicacion.class);
                    i.putExtra("long", lon);
                    i.putExtra("lat", lat);
                    i.putExtra("Nombre",getIntent().getStringExtra("Nombre"));

                    startActivity(i);
                    */
                    List<Address> direccion = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(),1);
                    String adress=direccion.get(0).getAddressLine(0);
                    String mess="Me encuentro en peligro, por favor localicenme en esta direccion: \n"+adress;
                    Toast.makeText(getApplication(),adress,Toast.LENGTH_LONG).show();
                    String nume="";
                    Datos_Usuario conex=new Datos_Usuario(mainActivity, "DBUsuario",null,2);
                    SQLiteDatabase db=conex.getReadableDatabase();

                    final Cursor cursor=db.rawQuery("SELECT Numero FROM Usuario",null);
                    try {
                        if (cursor.moveToFirst()) {
                            nume = cursor.getString(0);
                        }
                    }catch (Exception e){
                        Toast.makeText(mainActivity, "error", Toast.LENGTH_SHORT).show();
                    }
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(nume,null,mess,null,null);
                    smsManager.sendTextMessage(Uri.parse("tel:"+nume).toString(),null,mess,
                            null,null);

                   // smsManager.sendTextMessage(Uri.parse("tel:"+getIntent().getStringExtra("Numero")).toString(),
                    //      null,
                    //    mess, //mensaje
                    //  null,
                    //null);
                    startActivity(new Intent(getApplicationContext(),Inicio_Visual.class));
                    sino = false;
                    finish();
                }

                String Text = "Mi ubicacion actual es: " + "\n Lat = "
                        + loc.getLatitude() + "\n Long = " + loc.getLongitude();
                //        mensaje1.setText(Text);
                this.mainActivity.setLocation(loc);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            //          mensaje1.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
//            mensaje1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
}

