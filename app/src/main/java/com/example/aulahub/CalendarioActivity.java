package com.example.aulahub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class CalendarioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Calendario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //declarar variables
        GridLayout mCalendarioMatutinoA = findViewById(R.id.calendarGridMatutinoA);
        GridLayout mCalendarioVespertinoA = findViewById(R.id.calendarGridVespertinoA);
        GridLayout mCalendarioMatutinoB = findViewById(R.id.calendarGridMatutinoB);
        GridLayout mCalendarioVespertinoB = findViewById(R.id.calendarGridVespertinoB);
        GridLayout mCalendarioMatutinoC = findViewById(R.id.calendarGridMatutinoC);
        GridLayout mCalendarioVespertionC = findViewById(R.id.calendarGridVespertinoC);
        GridLayout mCalendarioMatutinoAud = findViewById(R.id.calendarGridMatutinoAuditorio);
        GridLayout mCalendarioVespertinoAud = findViewById(R.id.calendarGridVespertinoAuditorio);

        //obtener datos de los Activity
        String aulaSeleccionada = getIntent().getStringExtra("CardSeleccionada");
        String Salon = getIntent().getStringExtra("aula");
        String Turno = getIntent().getStringExtra("turno");

        //ocultar los GRIDLAYOUT Por defecto
        mCalendarioMatutinoA.setVisibility(GridLayout.GONE);
        mCalendarioVespertinoA.setVisibility(GridLayout.GONE);

        mCalendarioMatutinoB.setVisibility(GridLayout.GONE);
        mCalendarioVespertinoB.setVisibility(GridLayout.GONE);

        mCalendarioMatutinoC.setVisibility(GridLayout.GONE);
        mCalendarioVespertionC.setVisibility(GridLayout.GONE);

        mCalendarioMatutinoAud.setVisibility(GridLayout.GONE);
        mCalendarioVespertinoAud.setVisibility(GridLayout.GONE);

        if ("Matutino".equals(Turno)) {
            switch (aulaSeleccionada) {
                case "Aula A":
                    mCalendarioMatutinoA.setVisibility(GridLayout.VISIBLE);
                    break;
                case "Aula B":
                    mCalendarioMatutinoB.setVisibility(GridLayout.VISIBLE);
                    break;
                case "Aula C":
                    mCalendarioMatutinoC.setVisibility(GridLayout.VISIBLE);
                    break;
                case "Auditorio":
                    mCalendarioMatutinoAud.setVisibility(GridLayout.VISIBLE);
                    break;
            }
        } else if ("Vespertino".equals(Turno)) {
            switch (aulaSeleccionada) {
                case "Aula A":
                    mCalendarioVespertinoA.setVisibility(GridLayout.VISIBLE);
                    break;
                case "Aula B":
                    mCalendarioVespertinoB.setVisibility(GridLayout.VISIBLE);
                    break;
                case "Aula C":
                    mCalendarioVespertionC.setVisibility(GridLayout.VISIBLE);
                    break;
                case "Auditorio":
                    mCalendarioVespertinoAud.setVisibility(GridLayout.VISIBLE);
                    break;
            }
        }

    }
}
