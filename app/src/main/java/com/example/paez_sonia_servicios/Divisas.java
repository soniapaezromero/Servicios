package com.example.paez_sonia_servicios;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.paez_sonia_servicios.databinding.ActivityDivisasBinding;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.paez_sonia_servicios.Conversion.convertirDolares;
import static com.example.paez_sonia_servicios.Conversion.convertirEuros;

/**
 * @author: Sonia Páez Romero
 * fecha:17/03/2021
 */

public class Divisas extends AppCompatActivity implements View.OnClickListener{
    private ActivityDivisasBinding binding;
    private static final int REQUEST_CONNECT = 1;
    public static final String ENLACE = "https://soniapaezromero.me/fichero/cambioDolaresEuros";
    public static final String ACTION_RESP = "RESPUESTA_DESCARGA";
    public static  File FICHERO ;//Obtenemos rutas donde se ha guardado el fichero
    public static String VALOR;//Obtenemos  valor del cambio en el Okhttp
    public  String cambioPasado;
    public String cambioString;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;
    public static final int notify =300000;//5 minutos en milisegundos
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_divisas);


        binding = ActivityDivisasBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.botonconv.setOnClickListener(this);
        intentFilter = new IntentFilter(ACTION_RESP);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastReceiver = new ReceptorOperacion();
        if (comprobarPermiso()) {// Damos los permiso y programamos que sea cada 5 minutos y lanzamos los servicios
            MyTimerTask myTask = new MyTimerTask();
            handler = new Handler();
            Timer myTimer = new Timer();
            myTimer.schedule(myTask, 0, notify);

        }

    }
    @Override
    public void onResume(){
        super.onResume();
        //---registrar el receptor ---
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        //--- anular el registro del recpetor ---
        unregisterReceiver(broadcastReceiver);
    }

    public class ReceptorOperacion extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String respuesta = intent.getStringExtra("resultado");
            binding.resultado.setText(respuesta);

        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.botonconv) {
            try {
                if( VALOR != null){// Le decimos que si el valor del okhttps es nulo nos lea el fichero
                    cambioString= VALOR;
                }else{
                    cambioString=leerFihero(FICHERO);
                    mostrarMensaje("Fichero Leido");
                }


                double cambio=Double.parseDouble(cambioString);
                if (binding.eurosDolares.isChecked()) {
                    binding.dolares.setText(convertirDolares(binding.euros.getText().toString(), cambio));
                } else {
                    binding.euros.setText(convertirEuros(binding.dolares.getText().toString(), cambio));
                }


            } catch (NumberFormatException e) {
                Toast.makeText(this, "Error en la conversión: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }


        }
    }
    private boolean comprobarPermiso() {//Damos permisos a la aplicacion
        //https://developer.android.com/training/permissions/requesting?hl=es-419
        String permiso = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // Manifest.permission.INTERNET
        boolean concedido = false;
        // comprobar los permisos
        if (ActivityCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            // pedir los permisos necesarios, porque no están concedidos
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permiso)) {
                concedido = false;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permiso}, REQUEST_CONNECT);
                // Cuando se cierre el cuadro de diálogo se ejecutará onRequestPermissionsResult
            }
        } else {
            concedido = true;
        }
        return concedido;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String permiso = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        //Manifest.permission.INTERNET;
        // chequeo los permisos de nuevo
        if (requestCode == REQUEST_CONNECT)
            if (ActivityCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED)
                // permiso concedido
                startService(new Intent(Divisas.this, ServiciodeDescarga.class));
            else
                // no hay permiso
                mostrarError("No se ha concedido permiso para conectarse a Internet");
    }


  public String leerFihero(File file){
      FileInputStream fis = null;
      InputStreamReader isr = null;
      BufferedReader br = null;

      String linea;
      StringBuilder cadena = new StringBuilder();

      try {
          fis = new FileInputStream(file);

      isr = new InputStreamReader(fis);
      br = new BufferedReader(isr);

      while((linea= br.readLine()) != null){
          cadena.append(linea).append('\n');
      }
      br.close();
      cambioPasado=cadena.toString();
      } catch (FileNotFoundException e) {
          mostrarError("Error en lectura"+ e.getMessage());
      } catch (IOException e) {
        mostrarError("Error en lectura"+ e.getMessage());
      }
      return cambioPasado;
  }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
    private void  mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
    class MyTimerTask extends TimerTask {
        public void run() {// le decimos que segun la eleccion del boton  use el Servicio o el IntentService
            if (!binding.switch1.isChecked()) {
                // uso con Service
                startService(new Intent(Divisas.this, ServiciodeDescarga.class));

            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // uso con IntentService
                        Intent i = new Intent(Divisas.this, ServicioDescargaIntent.class);
                        i.putExtra("web", ENLACE);
                        startService(i);
                        //  String cambiopasado = ServicioDescargaIntent.VALOR;
                    }
                });

            }
        }
    }
}







