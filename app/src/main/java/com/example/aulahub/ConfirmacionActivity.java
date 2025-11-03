package com.example.aulahub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ConfirmacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmacion);

        // Ajuste de márgenes por sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias de los TextView
        TextView tvMateria = findViewById(R.id.tvMateria);
        TextView tvSalon = findViewById(R.id.tvSalon);
        TextView tvTurno = findViewById(R.id.tvTurno);
        TextView tvGrupo = findViewById(R.id.tvGrupo);
        TextView tvFechas = findViewById(R.id.tvFechas);
        TextView tvHorarios = findViewById(R.id.tvhorarios);

        // Recupera los datos enviados desde reservas.java
        Intent intent = getIntent();
        if (intent != null) {
            String materia = intent.getStringExtra("materia");
            String aula = intent.getStringExtra("aula");
            String grupo = intent.getStringExtra("grupo");
            String turno = intent.getStringExtra("turno");
            String fechas = intent.getStringExtra("fechas");
            ArrayList<String> horarios = intent.getStringArrayListExtra("horarios");

            // Muestra los datos si existen
            if (materia != null) tvMateria.setText(materia);
            if (aula != null) tvSalon.setText(aula);
            if (grupo != null) tvGrupo.setText(grupo);
            if (turno != null) tvTurno.setText(turno);
            if (fechas != null) tvFechas.setText(fechas);

            if (horarios != null && !horarios.isEmpty()) {
                tvHorarios.setText(String.join(", ", horarios));
            } else {
                tvHorarios.setText("No seleccionados");
            }
        }
    }
}
