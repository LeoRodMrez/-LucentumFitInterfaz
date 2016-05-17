package lucentum.com;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
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

public class Contactos extends AppCompatActivity {

    TextView prueba;
    DatosContactos datosContactos;
    //ArrayList<DatosContactos> listaContactos;
    ListaContactos lista;
    ListView listview;
    RequestQueue requestQueue;
    String amigosURL = "http://46.101.84.36:3000/amigos/Prueba/";
    String usuarioURL = "http://46.101.84.36:3000/usuarios/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //listaContactos = new ArrayList<DatosContactos>();
        setContentView(R.layout.activity_contactos);

        SharedPreferences preferences = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        String usuario = preferences.getString("usu", "null");
        System.out.println("Usuario: "+usuario+"  "+usuario.length());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        cargarAmigos(usuario);

        listview = (ListView)findViewById(R.id.lv_Contactos);
        lista= new ListaContactos(this,R.layout.activity_lista_contactos);
        listview.setAdapter(lista);
    }


    public void cargarAmigos(String usuario) {
        StringRequest request = new StringRequest(Request.Method.GET, amigosURL+usuario, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("Me devuelve JSON");
                System.out.println("RESPONSE "+response);
                JSONArray contactos  = null; // json de los contactos
                MostrarToast(response);

                try {
                    contactos = new JSONArray(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JSONObject data = jObject.getJSONObject("data"); // get data object

                try {

                    for(int i=0;i<contactos.length();i++){

                        JSONObject e = contactos.getJSONObject(i);
                        String nombreContacto = e.getString("Amigo");
                        MostrarToast("Bienvenido " + nombreContacto);
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

    public void cargarDatosAmigos(String amigo) {
        StringRequest request = new StringRequest(Request.Method.GET, usuarioURL+amigo, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("Me devuelve JSON");
                System.out.println("RESPONSE "+response);
                JSONObject datoscontactos = null; // json de los datos de los contactos

                try {
                    datoscontactos = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JSONObject data = jObject.getJSONObject("data"); // get data object

                try {

                    String id, name, localidad,pais, ranking;

                    id = datoscontactos.getString("Usuario");
                    name = datoscontactos.getString("Nombre");
                    MostrarToast(id);
                    localidad = datoscontactos.getString("Ciudad");
                    pais = datoscontactos.getString("Pais");
                    DatosContactos datosContactos = new DatosContactos(id,name,localidad,pais);
                    lista.add(datosContactos);




                    /*String parametro = jObject.getString("Nombre");
                    if(!parametro.equals("null"))
                        inNombre.setText(parametro);

                    parametro = jObject.getString("Edad");
                    if(!parametro.equals("null"))
                        inEdad.setText(parametro);

                    // parametro = jObject.getString("Sexo");
                    // if(!parametro.equals("null"))
                    // inSexo.setText(parametro);

                    parametro = jObject.getString("Altura");
                    if(!parametro.equals("null"))
                        inAltura.setText(parametro);

                    parametro = jObject.getString("Peso");
                    if(!parametro.equals("null"))
                        inPeso.setText(parametro);

                    parametro = jObject.getString("Ciudad");
                    if(!parametro.equals("null"))
                        inCiudad.setText(parametro);

                    parametro = jObject.getString("Pais");
                    if(!parametro.equals("null"))
                        inPais.setText(parametro);

                    parametro = jObject.getString("Correo");
                    //if(!parametro.equals("null"))
                    inCorreo.setText(parametro);


                    //usu.setText(projectname, TextView.BufferType.EDITABLE);
                    System.out.println("RESPONSE "+parametro);
                    //usu.setText("hola");*/

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
    public void MostrarToast(String mensaje)
    {
        Toast toast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
        toast.show();
    }

}