package com.example.paez_sonia_servicios;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button ejercicio1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ejercicio1=findViewById(R.id.ejercicio1);
        ejercicio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intento =new Intent(v.getContext(),Divisas.class);
                startActivityForResult(intento,0);
            }
        });

    }
}