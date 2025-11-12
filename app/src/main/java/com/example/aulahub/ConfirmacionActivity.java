package com.example.aulahub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

            if (horarios != null && !horarios.isEmpty()) {
                StringBuilder horariosTexto = new StringBuilder();

                for (String horario : horarios) {
                    horariosTexto.append(horario).append("\n");
                }

                tvHorarios.setText(horariosTexto.toString().trim());
            } else {
                tvHorarios.setText("No hay horarios seleccionados");
            }
        }

        //Encontrar el boton para ir a el apartado de mis reservas
        Button ir_a_reservas=findViewById(R.id.ir_a_reservas);
        ir_a_reservas.setOnClickListener(v ->{

            Intent misreservas= new Intent(ConfirmacionActivity.this, MisReservas.class);
            startActivity(misreservas);
            finish();
        });
    }
}
