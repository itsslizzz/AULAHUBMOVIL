package com.example.aulahub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import android.content.Intent;

public class calendario extends com.example.aulahub.utils.ToolbarManager {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private Spinner spMateria, spAula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);
        ImageButton mImageButton = findViewById(R.id.IbtnMenu);
        ImageView mFotoPerfil = findViewById(R.id.IVPerfil);

        inicializarToolbar(mFotoPerfil, mImageButton);

        // --- Referencias ---
        Spinner spTurno = findViewById(R.id.spTurno);
        spMateria = findViewById(R.id.spMateria);
        spAula = findViewById(R.id.spAula);

        TextView tvRoomName = findViewById(R.id.tvRoomName);
        TextView tvModulo = findViewById(R.id.tv_modulo);
        ImageView imgAula = findViewById(R.id.imgAula);

        Button btnContinuar = findViewById(R.id.btnContinuar);
        Button btn_Reservar = findViewById(R.id.btn_Reservar);
        TextView tvHorarios = findViewById(R.id.tv_horarios);
        TextView tvFechasSeleccionadas = findViewById(R.id.tvFechasSeleccionadas);
        HorizontalScrollView scrollMatutino = findViewById(R.id.scrollMatutino);
        HorizontalScrollView scrollVespertino = findViewById(R.id.scrollVespertino);

        // --- Estado inicial ---
        tvHorarios.setVisibility(View.GONE);
        scrollMatutino.setVisibility(View.GONE);
        scrollVespertino.setVisibility(View.GONE);
        btn_Reservar.setVisibility(View.GONE);

        // --- Mostrar nombre, módulo e imagen ---
        String roomName = getIntent().getStringExtra("roomName");
        if (roomName == null || roomName.isEmpty()) roomName = "Aula";
        tvRoomName.setText(roomName);

        String modulo = getIntent().getStringExtra("modulo");
        if (modulo == null || modulo.isEmpty()) modulo = "Módulo desconocido";
        tvModulo.setText(modulo);

        int imagen = getIntent().getIntExtra("imagen", 0);
        if (imagen != 0) imgAula.setImageResource(imagen);

        // --- FIREBASE ---
        String uid = mAuth.getCurrentUser().getUid();

        // 1️⃣ Obtener materias del profesor
        mFirestore.collection("profesores")
                .document(uid)
                .collection("salones")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> materiasSet = new HashSet<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String materia = doc.getString("nombre_materia");
                        if (materia != null && !materia.isEmpty()) {
                            materiasSet.add(materia);
                        } else {
                            materiasSet.add(doc.getId());
                        }
                    }

                    List<String> listaMaterias = new ArrayList<>(materiasSet);
                    if (listaMaterias.isEmpty()) listaMaterias.add("Sin materias disponibles");

                    ArrayAdapter<String> adapterMateria = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            listaMaterias
                    );
                    adapterMateria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spMateria.setAdapter(adapterMateria);
                })
                .addOnFailureListener(e -> Log.e("FirestoreDebug", "Error cargando materias", e));

        // 2️⃣ Mostrar grupos según la materia seleccionada
        spMateria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String materiaSeleccionada = parent.getItemAtPosition(position).toString();

                mFirestore.collection("profesores")
                        .document(uid)
                        .collection("salones")
                        .whereEqualTo("nombre_materia", materiaSeleccionada)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            List<String> grupos = new ArrayList<>();

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                String grupo = doc.getString("Salon");
                                if (grupo != null && !grupo.isEmpty()) {
                                    grupos.add(grupo);
                                } else {
                                    grupos.add(doc.getId());
                                }
                            }

                            if (grupos.isEmpty()) grupos.add("Sin grupos disponibles");

                            ArrayAdapter<String> adapterGrupo = new ArrayAdapter<>(
                                    calendario.this,
                                    android.R.layout.simple_spinner_item,
                                    grupos
                            );
                            adapterGrupo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spAula.setAdapter(adapterGrupo);
                        })
                        .addOnFailureListener(e -> Log.e("FirestoreDebug", "Error cargando grupos", e));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Botones de horarios ---
        List<String> horariosSeleccionados = new ArrayList<>();

        // Matutinos
        Button btn7a8 = findViewById(R.id.btn_7a8);
        Button btn8a9 = findViewById(R.id.btn_8a9);
        Button btn9a10 = findViewById(R.id.btn_9a10);
        Button btn10a11 = findViewById(R.id.btn_10a11);
        Button btn11a12 = findViewById(R.id.btn_11a12);

        // Vespertinos
        Button btn3a4 = findViewById(R.id.btn_3a4);
        Button btn4a5 = findViewById(R.id.btn_4a5);
        Button btn5a6 = findViewById(R.id.btn_5a6);
        Button btn6a7 = findViewById(R.id.btn_6a7);
        Button btn7a8pm = findViewById(R.id.btn_7a8pm);

        // Listener para seleccionar horarios
        View.OnClickListener horarioClickListener = v -> {
            Button boton = (Button) v;
            String texto = boton.getText().toString();

            if (horariosSeleccionados.contains(texto)) {
                horariosSeleccionados.remove(texto);
                boton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Azul_Fic));
            } else {
                horariosSeleccionados.add(texto);
                boton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Cafe_Fic));
            }

            Log.d("Horarios", "Seleccionados: " + horariosSeleccionados);
        };

        // Asignar listener
        btn7a8.setOnClickListener(horarioClickListener);
        btn8a9.setOnClickListener(horarioClickListener);
        btn9a10.setOnClickListener(horarioClickListener);
        btn10a11.setOnClickListener(horarioClickListener);
        btn11a12.setOnClickListener(horarioClickListener);

        btn3a4.setOnClickListener(horarioClickListener);
        btn4a5.setOnClickListener(horarioClickListener);
        btn5a6.setOnClickListener(horarioClickListener);
        btn6a7.setOnClickListener(horarioClickListener);
        btn7a8pm.setOnClickListener(horarioClickListener);

        // --- Botón Continuar: abrir calendario ---
        btnContinuar.setOnClickListener(v -> {
            tvFechasSeleccionadas.setText("");
            tvFechasSeleccionadas.setVisibility(View.GONE);

            MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Selecciona el rango de fechas");
            final MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

            picker.show(getSupportFragmentManager(), picker.toString());

            picker.addOnPositiveButtonClickListener(selection -> {
                Long startDate = selection.first;
                Long endDate = selection.second;

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                calendar.setTimeInMillis(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                String inicio = sdf.format(calendar.getTime());

                calendar.setTimeInMillis(endDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                String fin = sdf.format(calendar.getTime());

                tvFechasSeleccionadas.setText( inicio + " a " + fin);
                tvFechasSeleccionadas.setVisibility(View.VISIBLE);
                tvHorarios.setVisibility(View.VISIBLE);

                // Mostrar horarios según el turno
                String turnoSeleccionado = spTurno.getSelectedItem().toString();
                if (turnoSeleccionado.equalsIgnoreCase("Matutino")) {
                    scrollMatutino.setVisibility(View.VISIBLE);
                    scrollVespertino.setVisibility(View.GONE);
                } else if (turnoSeleccionado.equalsIgnoreCase("Vespertino")) {
                    scrollMatutino.setVisibility(View.GONE);
                    scrollVespertino.setVisibility(View.VISIBLE);
                } else {
                    scrollMatutino.setVisibility(View.GONE);
                    scrollVespertino.setVisibility(View.GONE);
                }

                btn_Reservar.setVisibility(View.VISIBLE);
            });
        });


        // --- NUEVO BLOQUE: Guardar reserva en Firestore ---
        btn_Reservar.setOnClickListener(v -> {
            Log.d("Reservas", "Botón Reservar presionado");

            String turnoSeleccionado = spTurno.getSelectedItem().toString();
            String materiaSeleccionada = spMateria.getSelectedItem().toString();
            String grupoSeleccionado = spAula.getSelectedItem().toString();
            String aulaSeleccionada = tvRoomName.getText().toString();
            String profesorID = mAuth.getCurrentUser().getUid();
            String fechasSeleccionadas = tvFechasSeleccionadas.getText().toString();


            Map<String, Object> reserva = new HashMap<>();
            reserva.put("profesorID", profesorID);
            reserva.put("materia", materiaSeleccionada);
            reserva.put("grupo", grupoSeleccionado);
            reserva.put("aula", aulaSeleccionada);
            reserva.put("turno", turnoSeleccionado);
            reserva.put("fechas", fechasSeleccionadas);
            reserva.put("horariosSeleccionados", horariosSeleccionados);
            reserva.put("timestamp", new Date());
            reserva.put("status", "Pendiente");

            mFirestore.collection("reservas")
                    .add(reserva)
                    .addOnSuccessListener(docRef -> {
                        Log.d("Reservas", "Reserva guardada con ID: " + docRef.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Reservas", "Error guardando reserva", e);
                    });

            Intent intent = new Intent(calendario.this, ConfirmacionActivity.class);
            intent.putExtra("materia", materiaSeleccionada);
            intent.putExtra("aula", aulaSeleccionada);
            intent.putExtra("grupo", grupoSeleccionado);
            intent.putExtra("turno", turnoSeleccionado);
            intent.putExtra("fechas", fechasSeleccionadas);
            intent.putStringArrayListExtra("horarios", new ArrayList<>(horariosSeleccionados));

            startActivity(intent);

        });




    }
}


