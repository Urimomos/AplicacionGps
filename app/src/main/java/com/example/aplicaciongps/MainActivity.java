package com.example.aplicaciongps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Variables para la UI
    private TextView tvLatitud, tvLongitud, tvAltitud, tvDireccion;
    private Button btnActualizar;

    // Cliente de ubicación de Google
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vincular las variables con el diseño XML
        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvAltitud = findViewById(R.id.tvAltitud);
        tvDireccion = findViewById(R.id.tvDireccion);
        btnActualizar = findViewById(R.id.btnActualizar);

        // 2. Inicializar el servicio de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 3. Configurar el botón
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerUbicacion();
            }
        });
    }

    private void obtenerUbicacion() {
        // Verificar si tenemos permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permisos, los pedimos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        // Obtener la última ubicación conocida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Tenemos ubicación, actualizamos los textos
                            tvLatitud.setText(String.valueOf(location.getLatitude()));
                            tvLongitud.setText(String.valueOf(location.getLongitude()));
                            tvAltitud.setText(location.getAltitude() + " m");

                            // Llamamos a la función para obtener la dirección real
                            obtenerDireccion(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación. Activa el GPS.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void obtenerDireccion(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);

            if (direcciones != null && !direcciones.isEmpty()) {
                // Obtenemos la primera dirección encontrada
                String direccionCompleta = direcciones.get(0).getAddressLine(0);
                tvDireccion.setText(direccionCompleta);
            } else {
                tvDireccion.setText("Dirección no encontrada");
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvDireccion.setText("Error de red al buscar dirección");
        }
    }

    // Metodo opcional: Para saber si el usuario aceptó los permisos y actualizar automáticamente
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}