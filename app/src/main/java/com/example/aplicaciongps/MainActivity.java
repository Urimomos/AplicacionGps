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
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Variables UI
    private TextView tvLatitud, tvLongitud, tvAltitud, tvDireccion;

    // Variables GPS
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vincular vistas
        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvAltitud = findViewById(R.id.tvAltitud);
        tvDireccion = findViewById(R.id.tvDireccion);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Configurar cómo queremos recibir las actualizaciones
        // Usamos Builder para versiones nuevas de Play Services, o la forma clásica:
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5000ms = 5 segundos
                .setMinUpdateIntervalMillis(3000) // No actualizar más rápido que cada 3 seg
                .build();

        // 3. Definir qué hacer cuando llega una nueva ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // ¡Aquí ocurre la magia en tiempo real!
                    actualizarUI(location);
                }
            }
        };

        // 4. Iniciar automáticamente si ya tenemos permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            iniciarActualizaciones();
        } else {
            // Pedir permisos al iniciar la app
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    private void iniciarActualizaciones() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Toast.makeText(this, "Rastreando ubicación en tiempo real...", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerActualizaciones() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void actualizarUI(Location location) {
        // Actualizar textos
        tvLatitud.setText(String.format(Locale.getDefault(), "%.5f", location.getLatitude()));
        tvLongitud.setText(String.format(Locale.getDefault(), "%.5f", location.getLongitude()));
        tvAltitud.setText(String.format(Locale.getDefault(), "%.1f m", location.getAltitude()));

        // Geocoding (Dirección)
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> direcciones = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (direcciones != null && !direcciones.isEmpty()) {
                tvDireccion.setText(direcciones.get(0).getAddressLine(0));
            } else {
                tvDireccion.setText("Buscando dirección...");
            }
        } catch (IOException e) {
            tvDireccion.setText("Sin conexión para dirección");
        }
    }

    // --- Ciclo de Vida de la App ---
    // Esto es IMPORTANTE: Solo actualizamos cuando la app está en pantalla para ahorrar batería

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            iniciarActualizaciones();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerActualizaciones();
    }

    // Manejo de respuesta de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarActualizaciones();
            } else {
                Toast.makeText(this, "Se requiere permiso para mostrar los datos", Toast.LENGTH_LONG).show();
            }
        }
    }
}