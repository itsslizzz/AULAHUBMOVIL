package com.example.aulahub;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class solicitudes_pendientes extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout containerSolicitudes;
    private TextView tvVacio;
    private Spinner spinnerStatus;

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
        spinnerStatus = findViewById(R.id.spinnerStatus);

        // Configurar el Spinner
        String[] opciones = {"Pendiente", "Aceptada", "Rechazada"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Datos que vienen desde ToolbarManager
        isAdmin      = getIntent().getBooleanExtra("isAdmin", false);
        aulaAdmin    = getIntent().getStringExtra("Aula");
        horarioAdmin = getIntent().getStringExtra("Horario");

        // Escuchador del Spinner para recargar cuando cambie el filtro
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String statusSeleccionado = opciones[position];
                cargarSolicitudes(statusSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarSolicitudes(String statusFiltro) {

        if (isAdmin && "AB".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", statusFiltro)
                    .whereEqualTo("turno", "Matutino")
                    .whereIn("aula", Arrays.asList("Laboratorio de Cómputo A", "Laboratorio de Cómputo B"))
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "AB".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", statusFiltro)
                    .whereEqualTo("turno", "Vespertino")
                    .whereIn("aula", Arrays.asList("Laboratorio de Cómputo A", "Laboratorio de Cómputo B"))
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "C".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", statusFiltro)
                    .whereEqualTo("turno", "Matutino")
                    .whereEqualTo("aula", "Laboratorio de Cómputo C")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "C".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", statusFiltro)
                    .whereEqualTo("turno", "Vespertino")
                    .whereEqualTo("aula", "Laboratorio de Cómputo C")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "Auditorio".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", statusFiltro)
                    .whereEqualTo("turno", "Matutino")
                    .whereEqualTo("aula", "Auditorio FIC")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else if (isAdmin && "Auditorio".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {

            db.collection("reservas")
                    .whereEqualTo("status", statusFiltro)
                    .whereEqualTo("turno", "Vespertino")
                    .whereEqualTo("aula", "Auditorio FIC")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);

        } else {
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

            CardView cardReserva = cardView.findViewById(R.id.cardReserva);

            String status = doc.getString("status");

            if (cardReserva != null) {
                if ("Aceptada".equals(status)) {
                    cardReserva.setCardBackgroundColor(getResources().getColor(R.color.Aceptar));
                } else if ("Rechazada".equals(status)) {
                    cardReserva.setCardBackgroundColor(getResources().getColor(R.color.Rechazar));
                } else {
                    cardReserva.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                }
            }

            ((TextView) cardView.findViewById(R.id.tv_maestro)).setText(doc.getString("profesorName"));
            ((TextView) cardView.findViewById(R.id.tv_materia)).setText(doc.getString("materia"));
            ((TextView) cardView.findViewById(R.id.tv_salon)).setText(doc.getString("aula"));
            ((TextView) cardView.findViewById(R.id.tv_turno)).setText(doc.getString("turno"));
            ((TextView) cardView.findViewById(R.id.tv_grupo)).setText(doc.getString("grupo"));
            ((TextView) cardView.findViewById(R.id.tv_horarios)).setText(doc.getString("horario"));

            TextView tvStatusText = cardView.findViewById(R.id.tv_status);
            tvStatusText.setText(status);

            View btnAceptar = cardView.findViewById(R.id.btn_aceptar_solicitud);
            View btnRechazar = cardView.findViewById(R.id.btn_rechazar_solicitud);

            if (!"Pendiente".equals(status)) {
                btnAceptar.setVisibility(View.GONE);
                btnRechazar.setVisibility(View.GONE);

                int colorBlanco = getResources().getColor(android.R.color.white);
                ((TextView) cardView.findViewById(R.id.tv_maestro)).setTextColor(colorBlanco);
                ((TextView) cardView.findViewById(R.id.tv_materia)).setTextColor(colorBlanco);
                ((TextView) cardView.findViewById(R.id.tv_salon)).setTextColor(colorBlanco);
                ((TextView) cardView.findViewById(R.id.tv_turno)).setTextColor(colorBlanco);
                ((TextView) cardView.findViewById(R.id.tv_grupo)).setTextColor(colorBlanco);
                ((TextView) cardView.findViewById(R.id.tv_horarios)).setTextColor(colorBlanco);
                tvStatusText.setTextColor(colorBlanco);
            } else {
                btnAceptar.setOnClickListener(v -> actualizarEstadoReserva(doc, "Aceptada"));
                btnRechazar.setOnClickListener(v -> actualizarEstadoReserva(doc, "Rechazada"));
            }

            containerSolicitudes.addView(cardView);
        }
    }

    private void actualizarEstadoReserva(DocumentSnapshot doc, String nuevoEstado) {
        db.collection("reservas").document(doc.getId())
                .update("status", nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reserva " + nuevoEstado, Toast.LENGTH_SHORT).show();

                    // --- NUEVO: ENVIAR NOTIFICACIÓN AL PROFESOR ---
                    String profesorUid = doc.getString("ProfesorUID");
                    String materia = doc.getString("materia");
                    String fecha = doc.getString("fecha");
                    String horario = doc.getString("horario"); // Ojo: a veces guardas la hora completa aquí

                    enviarNotificacion(profesorUid, materia, nuevoEstado, fecha, horario);
                    // ----------------------------------------------

                    if ("Aceptada".equals(nuevoEstado)) {
                        // Pasamos también el ID del profesor para avisar a los rechazados (Opcional avanzado)
                        rechazarConflictos(doc.getString("horario"), doc.getString("aula"), doc.getId());
                    }
                    // Refrescar con el status actual del spinner
                    cargarSolicitudes(spinnerStatus.getSelectedItem().toString());
                });
    }

    private void rechazarConflictos(String horario, String aula, String idExcluido) {
        db.collection("reservas")
                .whereEqualTo("horario", horario)
                .whereEqualTo("aula", aula)
                .whereEqualTo("status", "Pendiente")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                        if (!d.getId().equals(idExcluido)) {
                            // 1. Actualizar estado en la BD
                            d.getReference().update("status", "Rechazada");

                            // 2. RECUPERAR DATOS PARA NOTIFICAR AL PROFESOR RECHAZADO
                            // (Esto es lo que faltaba)
                            String uidProf = d.getString("ProfesorUID");
                            String mat = d.getString("materia");
                            String fecha = d.getString("fecha");
                            String hor = d.getString("horario");

                            // 3. ENVIAR LA NOTIFICACIÓN
                            enviarNotificacion(uidProf, mat, "Rechazada", fecha, hor);
                        }
                    }
                });
    }

    // Función para enviar notificación al buzón del profesor
    private void enviarNotificacion(String profesorUid, String materia, String nuevoEstado, String fechaReserva, String horaReserva) {
        if (profesorUid == null || profesorUid.isEmpty()) return;

        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("destinatarioUid", profesorUid);
        notificacion.put("titulo", "Reserva " + nuevoEstado);
        notificacion.put("mensaje", "Tu solicitud para " + materia + " (" + fechaReserva + " " + horaReserva + ") ha sido " + nuevoEstado.toLowerCase() + ".");
        notificacion.put("leido", false);
        notificacion.put("fecha", FieldValue.serverTimestamp()); // Hora exacta del servidor
        notificacion.put("tipo", "estado_reserva");

        db.collection("notificaciones")
                .add(notificacion)
                .addOnSuccessListener(documentReference -> Log.d("Notificacion", "Notificación enviada al profesor"))
                .addOnFailureListener(e -> Log.e("Notificacion", "Error al enviar notificación", e));
    }
}