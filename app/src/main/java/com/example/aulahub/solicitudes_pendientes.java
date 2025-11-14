package com.example.aulahub;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class solicitudes_pendientes extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout containerSolicitudes;
    private TextView tvVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.solicitud_pendientes);

        // Ajuste visual
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase y vistas
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        containerSolicitudes = findViewById(R.id.container_solicitudes);
        tvVacio = findViewById(R.id.tv_vacio);

        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        db.collection("reservas")
                .whereEqualTo("status", "Pendiente") // solo pendientes
                .get()
                .addOnSuccessListener(this::mostrarSolicitudes)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void mostrarSolicitudes(QuerySnapshot querySnapshot) {
        LayoutInflater inflater = LayoutInflater.from(this);
        containerSolicitudes.removeAllViews();

        if (querySnapshot.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            return;
        } else {
            tvVacio.setVisibility(View.GONE);
        }

        for (DocumentSnapshot doc : querySnapshot) {
            View cardView = inflater.inflate(R.layout.item_card_solicitud, containerSolicitudes, false);

            // obtener id
            String profesorID = doc.getString("profesorID");

            // obtener el nombre a base del ID del profe para poder mostrarlo en el apartado Maestro
            db.collection("profesores").document(profesorID)
                    .get()
                    .addOnSuccessListener(profesorDoc -> {
                        String nombreProfesor = profesorDoc.getString("Nombre");

                        ((TextView) cardView.findViewById(R.id.tv_maestro)).setText(nombreProfesor != null ? nombreProfesor : "Nombre no disponible");
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener el nombre del maestro", e));

            // Set other information in the card view
            ((TextView) cardView.findViewById(R.id.tv_materia)).setText(doc.getString("materia"));
            ((TextView) cardView.findViewById(R.id.tv_salon)).setText(doc.getString("aula"));
            ((TextView) cardView.findViewById(R.id.tv_turno)).setText(doc.getString("turno"));
            ((TextView) cardView.findViewById(R.id.tv_grupo)).setText(doc.getString("grupo"));

            // Mostrar horas y fechas de mejor manera
            Object horarios = doc.get("horariosSeleccionados");
            TextView tvHorarios = cardView.findViewById(R.id.tv_horarios);

            if (horarios instanceof List<?>) {
                List<?> listaHorarios = (List<?>) horarios;
                StringBuilder horariosTexto = new StringBuilder();
                for (Object h : listaHorarios) {
                    if (h != null) {
                        horariosTexto.append(h.toString()).append("\n");
                    }
                }
                tvHorarios.setText(horariosTexto.toString().trim());
            } else {
                tvHorarios.setText("No hay horarios seleccionados");
            }

            TextView tvStatus = cardView.findViewById(R.id.tv_status);
            String status = doc.getString("status");
            tvStatus.setText(status != null ? status : "Pendiente");

            // mostrar la card en el layout
            containerSolicitudes.addView(cardView);
        }
    }

}