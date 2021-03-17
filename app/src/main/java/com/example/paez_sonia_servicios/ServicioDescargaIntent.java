package com.example.paez_sonia_servicios;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ServicioDescargaIntent extends IntentService {

    public ServicioDescargaIntent() {
        super("ServiciodeDescarga");
    }

    @Override
    protected void onHandleIntent(Intent intent) {//
        if (intent != null) {
            String web = intent.getExtras().getString("web");
            URL url = null;
            try {
                url = new URL(web);
                descargaOkHTTP(url);

            } catch (MalformedURLException  e) {
                e.printStackTrace();
                enviarRespuesta("Error en la URL: " + e.getMessage());
            }
        }
    }

    private void descargaOkHTTP(URL web) {
        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(web)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("Error: ", e.getMessage());
                enviarRespuesta("Fallo: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException{
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        //throw new IOException("Unexpected code " + response);
                        Log.e("Error: ", "Unexpected code " + response);
                        enviarRespuesta("Error: Unexpected code " + response);
                    } else {
                        // Read data on the worker thread
                        final String responseData = response.body().string();
                        // guardar el fichero descargado en memoria externa
                        if (escribirExterna(responseData)) {
                            Divisas.VALOR= responseData;
                            Log.i("Descarga Intent: ", "fichero descargado");
                        } else {
                            Log.e("Error ", "no se ha podido descargar");
                        }
                    }
                }
            }
        });
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this,mensaje, Toast.LENGTH_SHORT).show();
    }

    private boolean escribirExterna(String cadena) {
        File miFichero, tarjeta;
        BufferedWriter bw = null;
        boolean correcto = false;
        try {
            tarjeta = Environment.getExternalStorageDirectory();
            miFichero = new File(tarjeta.getAbsolutePath(), "cambio.txt");
            Divisas.FICHERO= miFichero;
            bw = new BufferedWriter(new FileWriter(miFichero));
            bw.write(cadena);
            Log.i("Información: ", miFichero.getAbsolutePath());
            enviarRespuesta("Descarga: fichero descargado OK\n" + miFichero.getAbsolutePath());
        } catch (IOException e) {
            if (cadena != null)
                Log.e("Error: ", cadena);
            Log.e("Error de E/S", e.getMessage());
            enviarRespuesta("Error: " + e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                    correcto = true;
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
            }
        }
        return correcto;
    }



    private void enviarRespuesta (String mensaje) {
        Intent i = new Intent();
        i.setAction(Divisas.ACTION_RESP);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.putExtra("resultado", mensaje);
        sendBroadcast(i);
    }

}