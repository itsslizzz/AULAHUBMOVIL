package com.example.aulahub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MisReservas extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout containerReservas;
    private TextView tvVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_reservas);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        containerReservas = findViewById(R.id.container_reservas);
        tvVacio = findViewById(R.id.tv_vacio);

        cargarReservas();
    }

    private void cargarReservas() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("reservas")
                .whereEqualTo("profesorID", uid)
                .get()
                .addOnSuccessListener(this::mostrarReservas)
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void mostrarReservas(QuerySnapshot querySnapshot) {
        LayoutInflater inflater = LayoutInflater.from(this);
        containerReservas.removeAllViews();

        // Si no hay reservas → mostrar mensaje vacío
        if (querySnapshot == null || querySnapshot.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            return;
        } else {
            tvVacio.setVisibility(View.GONE);
        }

        // Mostrar las reservas si existen
        for (DocumentSnapshot doc : querySnapshot) {
            View cardView = inflater.inflate(R.layout.item_card_reserva, containerReservas, false);

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

            TextView tvStatus = cardView.findViewById(R.id.tv_status);
            String status = doc.getString("status");
            tvStatus.setText(status != null ? status : "Pendiente");

            containerReservas.addView(cardView);
        }
    }
}
