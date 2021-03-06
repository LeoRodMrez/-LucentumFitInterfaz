package lucentum.com;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.kml.KmlLayer;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class rutadetallada extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private Location location;
    private LocationManager locManager;
    private String nombreruta = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutadetallada);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000 * 5);        //cada 5 segundos actualiza

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            nombreruta = extras.getString("nombre");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);//el ultimo numero es 0 o 1
        } else {
            // permission has been granted, continue as usual
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (location == null) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
    }


    private Location getLastKnownLocation() {
        locManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = locManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /* esto va pero abre google maps
    public void showDirections(LatLng orige,LatLng fin) {

        final Intent intent = new
                Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                "saddr=" + orige.latitude + "," + orige.longitude + "&daddr=" + fin.latitude + "," +
                fin.longitude));
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        startActivity(intent);
    }*/

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    /*public void cargarkml(String ruta) {
        StringRequest request = new StringRequest(Request.Method.GET, ruta, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                InputStream stream = new ByteArrayInputStream(response.getBytes(Charset.forName("UTF-8")));

                KmlLayer layer = null;
                //R.raw.alicanteplaya
                try {
                    layer = new KmlLayer(mMap,stream,getApplicationContext());
                    layer.addLayerToMap();

                } catch (XmlPullParserException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("NO");
            }
        }) {
        };
        requestQueue.add(request);
    }*/


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        KmlLayer layer = null;
        LatLng inicio = null;
        LatLng meta = null;

        if(nombreruta.contains("alicanteplaya")) {
            try {
                layer = new KmlLayer(mMap, R.raw.alicanteplaya, getApplicationContext());
                layer.addLayerToMap();
            } catch (XmlPullParserException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inicio = new LatLng(38.343962,-0.481135);
            meta = new LatLng(38.348689,-0.472549); //esta es de alicante playa

        }else if(nombreruta.contains("monfortealicante")){
            try {
                layer = new KmlLayer(mMap, R.raw.monfortealicante, getApplicationContext());
                layer.addLayerToMap();
            } catch (XmlPullParserException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inicio = new LatLng(38.345943,-0.480482);//esta es de monforte alicante
            meta = new LatLng(38.380696,-0.730359);

        }else if(nombreruta.contains("sierraaitana")){
            try {
                layer = new KmlLayer(mMap, R.raw.sierraaitana, getApplicationContext());
                layer.addLayerToMap();
            } catch (XmlPullParserException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inicio = new LatLng(38.657537, -0.24194);//esta es de sierra de aitana
            meta = new LatLng(38.658036, -0.241692);
        }else if(nombreruta.contains("universidadalicante")) {
            try {
                layer = new KmlLayer(mMap, R.raw.universidadalicante, getApplicationContext());
                layer.addLayerToMap();
            } catch (XmlPullParserException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inicio = new LatLng(38.385987, -0.518201);
            meta = new LatLng(38.386145, -0.518249); //esta es de universidad
        }else if(nombreruta.contains("sanjuan")) {
            try {
                layer = new KmlLayer(mMap, R.raw.sanjuan, getApplicationContext());
                layer.addLayerToMap();
            } catch (XmlPullParserException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inicio = new LatLng(38.39901, -0.430198);
            meta = new LatLng(38.411505,-0.434505); //esta es de sanjuan
        }

        Location localizacion = this.getLastKnownLocation();

        mMap.addMarker(new MarkerOptions().position(inicio).title("INICIO"));
        mMap.addMarker(new MarkerOptions().position(meta).title("META"));

        if(localizacion != null){
            currentLatitude = localizacion.getLatitude();
            currentLongitude = localizacion.getLongitude();

            LatLng miposicion = new LatLng(currentLatitude,currentLongitude);
            mMap.addMarker(new MarkerOptions().position(miposicion).title("MI POSICION"));

            String url = getDirectionsUrl(miposicion, inicio);

            DownloadTask downloadTask = new DownloadTask();

            downloadTask.execute(url);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(miposicion.latitude, miposicion.longitude), 10));
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(inicio.latitude, inicio.longitude), 10));
        }
    }
}