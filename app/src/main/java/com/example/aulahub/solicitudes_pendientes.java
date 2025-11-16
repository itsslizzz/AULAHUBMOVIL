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


        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        aulaAdmin = getIntent().getStringExtra("Aula");
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

        } else if (isAdmin && "AB".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)){

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

        }else if (isAdmin&& "C".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)){
            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Matutino")
                    .whereEqualTo("aula", "Laboratorio de Cómputo C")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);
        } else if (isAdmin&& "C".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {
            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Vespertino")
                    .whereEqualTo("aula", "Laboratorio de Cómputo C")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);
        } else if (isAdmin&& "Auditorio".equals(aulaAdmin) && "Matutino".equals(horarioAdmin)) {
            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Matutino")
                    .whereEqualTo("aula", "Auditorio FIC")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);
            
        } else if (isAdmin&& "Auditorio".equals(aulaAdmin) && "Vespertino".equals(horarioAdmin)) {
            db.collection("reservas")
                    .whereEqualTo("status", "Pendiente")
                    .whereEqualTo("turno", "Vespertino")
                    .whereEqualTo("aula", "Auditorio FIC")
                    .get()
                    .addOnSuccessListener(this::mostrarSolicitudes)
                    .addOnFailureListener(Throwable::printStackTrace);
            
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


            String profesorID = doc.getString("profesorID");


            db.collection("profesores").document(profesorID)
                    .get()
                    .addOnSuccessListener(profesorDoc -> {
                        String nombreProfesor = profesorDoc.getString("Nombre");
                        // Set the professor's name in the card's TextView
                        ((TextView) cardView.findViewById(R.id.tv_maestro)).setText(nombreProfesor != null ? nombreProfesor : "Nombre no disponible");
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error getting professor name", e));


            ((TextView) cardView.findViewById(R.id.tv_materia)).setText(doc.getString("materia"));
            ((TextView) cardView.findViewById(R.id.tv_salon)).setText(doc.getString("aula"));
            ((TextView) cardView.findViewById(R.id.tv_turno)).setText(doc.getString("turno"));
            ((TextView) cardView.findViewById(R.id.tv_grupo)).setText(doc.getString("grupo"));


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

            // cargar status
            TextView tvStatus = cardView.findViewById(R.id.tv_status);
            String status = doc.getString("status");
            tvStatus.setText(status != null ? status : "Pendiente");


            containerSolicitudes.addView(cardView);
        }
    }
}