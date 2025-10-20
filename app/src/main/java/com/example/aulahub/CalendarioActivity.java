package com.example.aulahub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CalendarioActivity extends AppCompatActivity {

    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

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

        String uid = mAuth.getCurrentUser().getUid();

        // Referencias de los GridLayouts
        GridLayout mCalendarioMatutinoA = findViewById(R.id.calendarGridMatutinoA);
        GridLayout mCalendarioVespertinoA = findViewById(R.id.calendarGridVespertinoA);
        GridLayout mCalendarioMatutinoB = findViewById(R.id.calendarGridMatutinoB);
        GridLayout mCalendarioVespertinoB = findViewById(R.id.calendarGridVespertinoB);
        GridLayout mCalendarioMatutinoC = findViewById(R.id.calendarGridMatutinoC);
        GridLayout mCalendarioVespertinoC = findViewById(R.id.calendarGridVespertinoC);
        GridLayout mCalendarioMatutinoAud = findViewById(R.id.calendarGridMatutinoAuditorio);
        GridLayout mCalendarioVespertinoAud = findViewById(R.id.calendarGridVespertinoAuditorio);

        // Datos del intent
        String aulaSeleccionada = getIntent().getStringExtra("CardSeleccionada");
        String salon = getIntent().getStringExtra("aula");
        String turno = getIntent().getStringExtra("turno");

        // Ocultar todos por defecto
        mCalendarioMatutinoA.setVisibility(View.GONE);
        mCalendarioVespertinoA.setVisibility(View.GONE);
        mCalendarioMatutinoB.setVisibility(View.GONE);
        mCalendarioVespertinoB.setVisibility(View.GONE);
        mCalendarioMatutinoC.setVisibility(View.GONE);
        mCalendarioVespertinoC.setVisibility(View.GONE);
        mCalendarioMatutinoAud.setVisibility(View.GONE);
        mCalendarioVespertinoAud.setVisibility(View.GONE);

        // Mostrar el correcto y asignar listeners
        if ("Matutino".equals(turno)) {
            switch (aulaSeleccionada) {
                case "Aula A":
                    mCalendarioMatutinoA.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioMatutinoA, uid, salon);
                    break;
                case "Aula B":
                    mCalendarioMatutinoB.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioMatutinoB, uid, salon);
                    break;
                case "Aula C":
                    mCalendarioMatutinoC.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioMatutinoC, uid, salon);
                    break;
                case "Auditorio":
                    mCalendarioMatutinoAud.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioMatutinoAud, uid, salon);
                    break;
            }
        } else if ("Vespertino".equals(turno)) {
            switch (aulaSeleccionada) {
                case "Aula A":
                    mCalendarioVespertinoA.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioVespertinoA, uid, salon);
                    break;
                case "Aula B":
                    mCalendarioVespertinoB.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioVespertinoB, uid, salon);
                    break;
                case "Aula C":
                    mCalendarioVespertinoC.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioVespertinoC, uid, salon);
                    break;
                case "Auditorio":
                    mCalendarioVespertinoAud.setVisibility(View.VISIBLE);
                    setButtonListeners(mCalendarioVespertinoAud, uid, salon);
                    break;
            }
        }
    }

    //asigna listeners para cada boton de los gridlayout
    private void setButtonListeners(GridLayout grid, String uid, String salon) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(v -> mostrarFormulario(uid, salon));
            }
        }
    }

    //metodo para mostrar el formulario de reserva
    private void mostrarFormulario(String uid, String salon) {
        View formView = getLayoutInflater().inflate(R.layout.activity_form_reserva, null);

        TextView mTextViewMateria = formView.findViewById(R.id.TVMateria);
        TextView mTextViewSalon = formView.findViewById(R.id.TVSalon);
        TextView mTextViewNombreMaestro = formView.findViewById(R.id.TVNombreMaestro);
        EditText mEditTextMotivo = formView.findViewById(R.id.ETMotivo);

        // Obtener nombre del maestro
        mFirestore.collection("profesores").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        mTextViewNombreMaestro.setText(documentSnapshot.getString("Nombre"));
                    } else {
                        Log.e("Firestore", "No existe el documento del profesor: " + uid);
                    }
                });

        // Obtener datos del salón y del nombre de la materia
        mFirestore.collection("profesores").document(uid)
                .collection("salones").document(salon)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {
                        mTextViewSalon.setText(documentSnapshot.getString("Salon"));
                        mTextViewMateria.setText(documentSnapshot.getString("nombre_materia"));
                    } else {
                        Log.e("Firestore", "No existe el documento del salón");
                    }
                });

        // Mostrar el formulario en un AlertDialog
        new AlertDialog.Builder(this)
                .setView(formView)
                .setTitle("Formulario de reserva")
                .setPositiveButton("Reservar", (dialog, which) -> {
                    String motivo = mEditTextMotivo.getText().toString();
                    Toast.makeText(this, "Motivo: " + motivo, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
