package com.example.aulahub;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class solicitudes_pendientes extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout containerSolicitudes;
    private TextView tvVacio;

    private boolean isAdmin;
    private String aulaAdmin;
    private String horarioAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.solicitud_pendientes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        containerSolicitudes = findViewById(R.id.container_solicitudes);
        tvVacio = findViewById(R.id.tv_vacio);

        // datos que vienen desde ToolbarManager
        isAdmin      = getIntent().getBooleanExtra("isAdmin", false);
        aulaAdmin    = getIntent().getStringExtra("Aula");
        horarioAdmin = getIntent().getStringExtra("Horario");

        Log.d("Solicitudes", "isAdmin=" + isAdmin +
                ", aulaAdmin=" + aulaAdmin +
                ", horarioAdmin=" + horarioAdmin);

        cargarSolicitudes();
    }

    private void cargarSolicitudes() {

        if (isAdmin && "AB".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Matutino")
                    .whereIn("aula", Arrays.asList(
                            "Laboratorio de Cómputo A",
                            "Laboratorio de Cómputo B"
                    ))
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "AB".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Vespertino")
                    .whereIn("aula", Arrays.asList(
                            "Laboratorio de Cómputo A",
                            "Laboratorio de Cómputo B"
                    ))
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "C".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Matutino")
                    .whereEqualTo("aula", "Laboratorio de Cómputo C")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "C".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Vespertino")
                    .whereEqualTo("aula", "Laboratorio de Cómputo C")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "Auditorio".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Matutino")
                    .whereEqualTo("aula", "Auditorio FIC")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "Auditorio".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Vespertino")
                    .whereEqualTo("aula", "Auditorio FIC")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else {
            // Por si algo sale raro y no entra a ningún if, que al menos no truene.
            Log.w("Solicitudes", "No coincide ningún filtro de admin, no se cargan reservas.");
            tvVacio.setVisibility(View.VISIBLE);
        }
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


            String nombreProfesor = doc.getString("profesorName");
            ((TextView) cardView.findViewById(R.id.tv_maestro))
                    .setText(nombreProfesor != null ? nombreProfesor : "Nombre no disponible");

            ((TextView) cardView.findViewById(R.id.tv_materia)).setText(doc.getString("materia"));
            ((TextView) cardView.findViewById(R.id.tv_salon)).setText(doc.getString("aula"));
            ((TextView) cardView.findViewById(R.id.tv_turno)).setText(doc.getString("turno"));
            ((TextView) cardView.findViewById(R.id.tv_grupo)).setText(doc.getString("grupo"));
            ((TextView) cardView.findViewById(R.id.tv_horarios)).setText(doc.getString("horario"));

            TextView tvStatus = cardView.findViewById(R.id.tv_status);
            String status = doc.getString("status");
            tvStatus.setText(status != null ? status : "Pendiente");

            // --- Lógica para botones Aceptar y Rechazar ---
            cardView.findViewById(R.id.btn_aceptar_solicitud).setOnClickListener(v -> {
                actualizarEstadoReserva(doc, "Aceptada");
            });

            cardView.findViewById(R.id.btn_rechazar_solicitud).setOnClickListener(v -> {
                actualizarEstadoReserva(doc, "Rechazada");
            });

            containerSolicitudes.addView(cardView);
        }
    }

    // Método para actualizar Firestore y disparar el cambio en el calendario
    private void actualizarEstadoReserva(DocumentSnapshot doc, String nuevoEstado) {
        String idReserva = doc.getId();
        String horario = doc.getString("horario");
        String aula = doc.getString("aula");

        db.collection("reservas").document(idReserva)
                .update("status", nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reserva " + nuevoEstado, Toast.LENGTH_SHORT).show();

                    if ("Aceptada".equals(nuevoEstado)) {
                        // Rechaza automáticamente conflictos (misma aula y horario)
                        rechazarConflictos(horario, aula, idReserva);
                    }

                    // Recargar la lista para reflejar los cambios
                    cargarSolicitudes();
                })
                .addOnFailureListener(e -> Log.e("Solicitudes", "Error al actualizar", e));
    }

    // Método para evitar doble reserva en el mismo espacio/tiempo
    private void rechazarConflictos(String horario, String aula, String idExcluido) {
        db.collection("reservas")
                .whereEqualTo("horario", horario)
                .whereEqualTo("aula", aula)
                .whereEqualTo("status", "Pendiente")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                        if (!d.getId().equals(idExcluido)) {
                            d.getReference().update("status", "Rechazada");
                        }
                    }
                });
    }
}