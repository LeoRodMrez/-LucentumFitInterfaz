package lucentum.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Contactos extends AppCompatActivity {

    ListaContactos lista;
    ListView listview;
    RequestQueue requestQueue;
    String amigosURL = "http://46.101.84.36/amigos/LeerAmigos/"; //url de los amigos en la API
    String usuarioURL = "http://46.101.84.36/usuarios/"; //url de los usuarios en la API
    String anadirURL = "http://46.101.84.36/amigos/Relacion"; //url para crear amigos en la API
    String eliminarURL = "http://46.101.84.36/amigos/Romper"; //url para borrar amigos en la API
    EditText nuevo;
    String usuario,viejo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);
        nuevo = (EditText) findViewById(R.id.et_nuevo_contacto);

        //recoge el id del usuario que usa la app
        SharedPreferences preferences = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        usuario = preferences.getString("usu", "null");
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        cargarAmigos(usuario);

        listview = (ListView)findViewById(R.id.lv_Contactos);
        lista= new ListaContactos(this,R.layout.activity_lista_contactos);
        listview.setAdapter(lista);
    }


    //carga los amigos del usuario
    public void cargarAmigos(String usuario) {
        StringRequest request = new StringRequest(Request.Method.GET, amigosURL+usuario, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONArray contactos  = null; // json de los contactos


                try {
                    contactos = new JSONArray(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    //recorre todos los contactos y recoge sus amigos
                    for(int i=0;i<contactos.length();i++){

                        JSONObject e = contactos.getJSONObject(i);
                        String nombreContacto = e.getString("Amigo");
                        if(!nombreContacto.equals("null")) {
                            cargarDatosAmigos(nombreContacto);
                        }

                    }




                } catch (JSONException e) {
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
    }

    //cargar datos de los amigos
    public void cargarDatosAmigos(String amigo) {
        StringRequest request = new StringRequest(Request.Method.GET, usuarioURL+amigo, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject datoscontactos = null; // json de los datos de los contactos

                try {
                    datoscontactos = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JSONObject data = jObject.getJSONObject("data"); // get data object

                try {
                    //recoge la informacion de los amigos y la almacena
                    String id, name, localidad,pais, ranking;

                    id = datoscontactos.getString("Usuario");
                    name = datoscontactos.getString("Nombre");
                    localidad = datoscontactos.getString("Ciudad");
                    pais = datoscontactos.getString("Pais");

                    DatosContactos datosContactos = new DatosContactos(id,name,localidad,pais);
                    lista.add(datosContactos);

                } catch (JSONException e) {
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
    }

    //añadir contactos
    public void nuevoContacto(View v)
    {

        StringRequest request = new StringRequest(Request.Method.POST, anadirURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                //reinicia la activity


                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                MostrarToast("No existe el contacto.");

            }
        }) {

            //añade al amigo en la API
            @Override
            protected Map<String,String> getParams() throws
                    AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("Content-Type", "application/json; charset=utf-8");
                parametros.put("Usuario", usuario);
                parametros.put("Amigo", nuevo.getText().toString());
                return parametros;
            }
        };
        requestQueue.add(request);
    }

    //elimina el contacto seleccionado
    public void eliminarContacto(View v) {

        //recoge la posicion del contacto en la listview
        int pos = listview.getPositionForView(v);
        DatosContactos contacto = (DatosContactos)lista.getItem(pos);
        viejo = contacto.getId();

        StringRequest request = new StringRequest(Request.Method.POST, eliminarURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                requestQueue = Volley.newRequestQueue(getApplicationContext());

                //reinicia la activity
                Intent intent = getIntent();
                finish();
                startActivity(intent);

            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                MostrarToast("No existe el contacto.");

            }
        }) {

            //Actualiza los amigos en la Api
            @Override
            protected Map<String,String> getParams() throws
                    AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("Content-Type", "application/json; charset=utf-8");
                parametros.put("Usuario", usuario);
                parametros.put("Amigo", viejo);
                return parametros;
            }
        };
        requestQueue.add(request);
    }


    //muestra un mensaje
    public void MostrarToast(String mensaje)
    {
        Toast toast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
        toast.show();
    }

}