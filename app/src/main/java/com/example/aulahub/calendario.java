package com.example.aulahub;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class calendario extends com.example.aulahub.utils.ToolbarManager {

    private Spinner spMateria, spAula;

    // Semana que se está mostrando (inicio en lunes)
    private Calendar semanaActualInicio;

    // Horarios por turno
    private final String[] HORAS_MATUTINO = {
            "07:00 - 08:00",
            "08:00 - 09:00",
            "09:00 - 10:00",
            "10:00 - 11:00",
            "11:00 - 12:00",
            "12:00 - 13:00"
    };

    private final String[] HORAS_VESPERTINO = {
            "14:00 - 15:00",
            "15:00 - 16:00",
            "16:00 - 17:00",
            "17:00 - 18:00",
            "18:00 - 19:00"
    };

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
        TextView tvHorarios = findViewById(R.id.tv_horariosCalendario);
        TextView tvFechasSeleccionadas = findViewById(R.id.tvFechasSeleccionadas);
        HorizontalScrollView scrollMatutino = findViewById(R.id.scrollMatutino);
        HorizontalScrollView scrollVespertino = findViewById(R.id.scrollVespertino);

        // NUEVO: calendario semanal
        LinearLayout layoutSemana = findViewById(R.id.layoutSemana);
        TableLayout tlSemana = findViewById(R.id.tlSemana);
        TextView tvRangoSemana = findViewById(R.id.tvRangoSemana);
        Button btnSemanaAnterior = findViewById(R.id.btnSemanaAnterior);
        Button btnSemanaSiguiente = findViewById(R.id.btnSemanaSiguiente);

        // --- Estado inicial ---
        tvHorarios.setVisibility(View.GONE);
        scrollMatutino.setVisibility(View.GONE);
        scrollVespertino.setVisibility(View.GONE);
        layoutSemana.setVisibility(View.GONE);
        btn_Reservar.setVisibility(View.GONE);

        //Obetner valores de la variables exportardas de otras actividades

        // recuperara el valor de la variable admin de HomeActivity
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        ocultarObjetos(isAdmin);

        // --- Mostrar nombre, módulo e imagen ---
        String roomName = getIntent().getStringExtra("roomName");
        if (roomName == null || roomName.isEmpty()) roomName = "Aula";
        tvRoomName.setText(roomName);

        String modulo = getIntent().getStringExtra("modulo");
        if (modulo == null || modulo.isEmpty()) modulo = "Módulo desconocido";
        tvModulo.setText(modulo);

        int imagen = getIntent().getIntExtra("imagen", 0);
        if (imagen != 0) imgAula.setImageResource(imagen);


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

        // Lista donde se irán guardando "fecha + hora"
        List<String> horariosSeleccionados = new ArrayList<>();

        // Configurar inicio de semana (lunes de la semana actual)
        semanaActualInicio = Calendar.getInstance();
        semanaActualInicio.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Navegación de semanas
        btnSemanaAnterior.setOnClickListener(v -> {
            horariosSeleccionados.clear();
            tvFechasSeleccionadas.setText("");
            tvFechasSeleccionadas.setVisibility(View.GONE);
            semanaActualInicio.add(Calendar.WEEK_OF_YEAR, -1);
            String turnoActual = spTurno.getSelectedItem().toString();
            String[] horasTurno = obtenerHorasPorTurno(turnoActual);
            actualizarSemana(tlSemana, tvRangoSemana, tvFechasSeleccionadas, horariosSeleccionados, horasTurno);
        });

        btnSemanaSiguiente.setOnClickListener(v -> {
            horariosSeleccionados.clear();
            tvFechasSeleccionadas.setText("");
            tvFechasSeleccionadas.setVisibility(View.GONE);
            semanaActualInicio.add(Calendar.WEEK_OF_YEAR, 1);
            String turnoActual = spTurno.getSelectedItem().toString();
            String[] horasTurno = obtenerHorasPorTurno(turnoActual);
            actualizarSemana(tlSemana, tvRangoSemana, tvFechasSeleccionadas, horariosSeleccionados, horasTurno);
        });

        // --- Botón Continuar: mostrar calendario semanal ---
        btnContinuar.setOnClickListener(v -> {
            // limpiar selección anterior
            horariosSeleccionados.clear();
            tvFechasSeleccionadas.setText("");
            tvFechasSeleccionadas.setVisibility(View.GONE);

            String turnoSeleccionado = spTurno.getSelectedItem().toString();
            String[] horasTurno = obtenerHorasPorTurno(turnoSeleccionado);

            actualizarSemana(tlSemana, tvRangoSemana, tvFechasSeleccionadas, horariosSeleccionados, horasTurno);


            layoutSemana.setVisibility(View.VISIBLE);

            // los scrolls viejos se quedan ocultos
            scrollMatutino.setVisibility(View.GONE);
            scrollVespertino.setVisibility(View.GONE);

            btn_Reservar.setVisibility(View.VISIBLE);
        });

        if (isAdmin){
            btnContinuar.performClick();
        }


        // --- Guardar reserva en Firestore ---
        btn_Reservar.setOnClickListener(v -> {
            Log.d("Reservas", "Botón Reservar presionado");

            String turnoSeleccionado   = spTurno.getSelectedItem().toString();
            String materiaSeleccionada = spMateria.getSelectedItem().toString();
            String grupoSeleccionado   = spAula.getSelectedItem().toString();
            String aulaSeleccionada    = tvRoomName.getText().toString();
            String fechasSeleccionadas = tvFechasSeleccionadas.getText().toString();

            // Validaciones primero
            if (fechasSeleccionadas.isEmpty()){
                Toast.makeText(this, "Selecciona un horario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (horariosSeleccionados.isEmpty()){
                Toast.makeText(this, "Selecciona un rango de fechas", Toast.LENGTH_SHORT).show();
                return;
            }

            mFirestore.collection("profesores").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String teacherName = null;
                        if (documentSnapshot.exists()){
                            teacherName = documentSnapshot.getString("Nombre");
                        }

                        // Copiamos para no tocar la lista original
                        List<String> copiaHorarios = new ArrayList<>(horariosSeleccionados);

                        for (String claveHorario : copiaHorarios) {

                            // claveHorario = "yyyy-MM-dd 07:00 - 08:00"
                            String fechaSolo = "";
                            int espacio = claveHorario.indexOf(" ");
                            if (espacio > 0) {
                                fechaSolo = claveHorario.substring(0, espacio); // "yyyy-MM-dd"
                            }

                            Map<String, Object> reserva = new HashMap<>();
                            reserva.put("ProfesorUID", uid);
                            reserva.put("profesorName", teacherName);
                            reserva.put("materia", materiaSeleccionada);
                            reserva.put("grupo", grupoSeleccionado);
                            reserva.put("aula", aulaSeleccionada);
                            reserva.put("turno", turnoSeleccionado);

                            // Guardamos uno por horario
                            reserva.put("fecha", fechaSolo);
                            reserva.put("horario", claveHorario); // "yyyy-MM-dd 07:00 - 08:00"

                            reserva.put("status", "Pendiente");
                            reserva.put("timestamp", new Date());

                            mFirestore.collection("reservas")
                                    .add(reserva)
                                    .addOnSuccessListener(docRef -> {
                                        Log.d("Reservas", "Reserva guardada con ID: " + docRef.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Reservas", "Error guardando reserva", e);
                                        Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show();
                                    });
                        }

                        // Aquí ya puedes lanzar la pantalla de confirmación si quieres,
                        // pasando la lista de horariosSeleccionados como ya lo haces.
                        Intent intent = new Intent(calendario.this, ConfirmacionActivity.class);
                        intent.putExtra("materia", materiaSeleccionada);
                        intent.putExtra("aula", aulaSeleccionada);
                        intent.putExtra("grupo", grupoSeleccionado);
                        intent.putExtra("turno", turnoSeleccionado);
                        intent.putExtra("fechas", fechasSeleccionadas);
                        intent.putStringArrayListExtra(
                                "horarios",
                                new ArrayList<>(horariosSeleccionados)
                        );
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Profesor", "Error al obtener el profesor", e);
                        Toast.makeText(this, "Error al obtener datos del profesor", Toast.LENGTH_SHORT).show();
                    });

        });

    }

    // Devuelve el arreglo de horas según el turno
    private String[] obtenerHorasPorTurno(String turno) {
        if (turno == null) {
            return HORAS_MATUTINO;
        }
        if (turno.equalsIgnoreCase("Matutino")) {
            return HORAS_MATUTINO;
        } else  {
            return HORAS_VESPERTINO;
        }
    }

    // Rellena la tabla con la semana actual y engancha los clics en cada celda
    private void actualizarSemana(
            TableLayout tlSemana,
            TextView tvRangoSemana,
            TextView tvFechasSeleccionadas,
            List<String> horariosSeleccionados,
            String[] horasTurno
    ) {
        tlSemana.removeAllViews();

        SimpleDateFormat formatoCabecera =
                new SimpleDateFormat("EEE dd/MM", new Locale("es", "ES"));
        SimpleDateFormat formatoFecha =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Fila de cabecera: "Hora" + días de la semana
        TableRow filaHeader = new TableRow(this);

        TextView tvHoraHeader = new TextView(this);
        tvHoraHeader.setText("Hora");
        filaHeader.addView(tvHoraHeader);

        Calendar dia = (Calendar) semanaActualInicio.clone();
        for (int d = 0; d < 7; d++) {
            TextView tvDia = new TextView(this);
            tvDia.setText(formatoCabecera.format(dia.getTime()));
            tvDia.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            filaHeader.addView(tvDia);
            dia.add(Calendar.DAY_OF_MONTH, 1);
        }
        tlSemana.addView(filaHeader);

        if (horasTurno == null) {
            horasTurno = new String[0];
        }

        // Filas por cada hora del turno
        for (String hora : horasTurno) {
            TableRow fila = new TableRow(this);

            // Primera columna: texto de la hora
            TextView tvHora = new TextView(this);
            tvHora.setText(hora);
            fila.addView(tvHora);

            Calendar fechaDia = (Calendar) semanaActualInicio.clone();
            for (int d = 0; d < 7; d++) {
                final String fechaStr = formatoFecha.format(fechaDia.getTime());
                final String clave = fechaStr + " " + hora;

                Button celda = new Button(this);
                celda.setText(""); // si quieres, aquí puedes mostrar algo

                // Color según si está seleccionado o no
                if (horariosSeleccionados.contains(clave)) {
                    celda.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.Cafe_Fic)
                    );
                } else {
                    celda.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.Azul_Fic)
                    );
                }

                celda.setOnClickListener(v -> {
                    // Si es admin: mostrar popup con reservas de ese horario
                    if (isAdmin) {
                        mostrarReservas(clave);
                        return;
                    }

                    if (horariosSeleccionados.contains(clave)) {
                        // ya estaba seleccionado -> lo quitamos
                        horariosSeleccionados.remove(clave);
                        celda.setBackgroundTintList(
                                ContextCompat.getColorStateList(this, R.color.Azul_Fic)
                        );
                    } else {
                        // no había nada (o solo este mismo) -> lo seleccionamos
                        horariosSeleccionados.add(clave);
                        celda.setBackgroundTintList(
                                ContextCompat.getColorStateList(this, R.color.Cafe_Fic)
                        );
                    }

                    actualizarTextoFechas(tvFechasSeleccionadas, horariosSeleccionados);
                    Log.d("Horarios", "Seleccionados: " + horariosSeleccionados);
                });

                fila.addView(celda);
                fechaDia.add(Calendar.DAY_OF_MONTH, 1);
            }

            tlSemana.addView(fila);
        }

        // Texto del rango de la semana (ej: 10 Nov 2025 - 16 Nov 2025)
        Calendar finSemana = (Calendar) semanaActualInicio.clone();
        finSemana.add(Calendar.DAY_OF_MONTH, 6);
        SimpleDateFormat formatoTitulo =
                new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        String rango = formatoTitulo.format(semanaActualInicio.getTime())
                + " - "
                + formatoTitulo.format(finSemana.getTime());
        tvRangoSemana.setText(rango);

        actualizarTextoFechas(tvFechasSeleccionadas, horariosSeleccionados);
    }

    // Actualiza el TextView donde muestras las fechas seleccionadas
    private void actualizarTextoFechas(
            TextView tvFechasSeleccionadas,
            List<String> horariosSeleccionados
    ) {
        if (horariosSeleccionados == null || horariosSeleccionados.isEmpty()) {
            tvFechasSeleccionadas.setText("");
            tvFechasSeleccionadas.setVisibility(View.GONE);
            return;
        }

        // Extraer solo las fechas (yyyy-MM-dd) de "yyyy-MM-dd HH:MM - HH:MM"
        Set<String> fechas = new HashSet<>();
        for (String s : horariosSeleccionados) {
            int espacio = s.indexOf(" ");
            if (espacio > 0) {
                fechas.add(s.substring(0, espacio));
            }
        }

        // Ordenar fechas
        List<String> lista = new ArrayList<>(fechas);
        Collections.sort(lista);

        StringBuilder sb = new StringBuilder();
        for (String f : lista) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(f);
        }

        tvFechasSeleccionadas.setText(sb.toString());
        tvFechasSeleccionadas.setVisibility(View.VISIBLE);
    }

    private void ocultarObjetos(boolean isAdmin){
        TextView tv_horarios = findViewById(R.id.tv_horariosCalendario);
        TextView tvturno = findViewById(R.id.tvturno);
        Spinner spTurno = findViewById(R.id.spTurno);
        TextView tvmateria = findViewById(R.id.tvmateria);
        Spinner spMateria = findViewById(R.id.spMateria);
        TextView tvgrupo = findViewById(R.id.tvgrupo);
        Spinner spAula = findViewById(R.id.spAula);
        Button btnContinuar = findViewById(R.id.btnContinuar);

        if(!isAdmin) {
            tv_horarios.setVisibility(View.VISIBLE);
            tvturno.setVisibility(View.VISIBLE);
            tvgrupo.setVisibility(View.VISIBLE);
            tvmateria.setVisibility(View.VISIBLE);
            spTurno.setVisibility(View.VISIBLE);
            spAula.setVisibility(View.VISIBLE);
            spMateria.setVisibility(View.VISIBLE);
            btnContinuar.setVisibility(View.VISIBLE);
        } else {
            tv_horarios.setVisibility(View.GONE);
            tvturno.setVisibility(View.GONE);
            tvgrupo.setVisibility(View.GONE);
            tvmateria.setVisibility(View.GONE);
            spTurno.setVisibility(View.GONE);
            spAula.setVisibility(View.GONE);
            spMateria.setVisibility(View.GONE);
            btnContinuar.setVisibility(View.GONE);
        }
    }
    private void mostrarReservas(String claveHorario) {

        mFirestore.collection("reservas")
                .whereEqualTo("horario", claveHorario)
                .whereEqualTo("status", "Pendiente")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        new AlertDialog.Builder(calendario.this)
                                .setTitle("Reservas para " + claveHorario)
                                .setMessage("No hay reservas para este horario.")
                                .setPositiveButton("Cerrar", null)
                                .show();
                        return;
                    }

                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    List<String> descripciones = new ArrayList<>();

                    for (DocumentSnapshot doc : docs) {
                        String materia = doc.getString("materia");
                        String grupo   = doc.getString("grupo");
                        String status  = doc.getString("status");
                        String horario = doc.getString("horario");

                        StringBuilder linea = new StringBuilder();
                        linea.append(materia != null ? materia : "Sin materia");
                        if (grupo != null && !grupo.isEmpty()) {
                            linea.append(" - ").append(grupo);
                        }
                        if (status != null && !status.isEmpty()) {
                            linea.append(" [").append(status).append("]");
                        }
                        if (horario != null && !horario.isEmpty()) {
                            linea.append("\n").append(horario);
                        }

                        descripciones.add(linea.toString());
                    }

                    new AlertDialog.Builder(calendario.this)
                            .setTitle("Reservas para " + claveHorario)
                            .setItems(descripciones.toArray(new String[0]), (dialog, which) -> {
                                DocumentSnapshot docSeleccionado = docs.get(which);
                                mostrarDialogoAccionReserva(docSeleccionado);
                            })
                            .setNegativeButton("Cerrar", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Reservas", "Error consultando reservas", e);
                    Toast.makeText(calendario.this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogoAccionReserva(DocumentSnapshot doc) {
        String materia  = doc.getString("materia");
        String grupo    = doc.getString("grupo");
        String turno    = doc.getString("turno");
        String aula     = doc.getString("aula");
        String fecha    = doc.getString("fecha");
        String status   = doc.getString("status");
        String profesor = doc.getString("profesorName");
        String horario  = doc.getString("horario");

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Materia: ").append(materia != null ? materia : "N/A").append("\n");
        mensaje.append("Grupo: ").append(grupo != null ? grupo : "N/A").append("\n");
        mensaje.append("Aula: ").append(aula != null ? aula : "N/A").append("\n");
        mensaje.append("Turno: ").append(turno != null ? turno : "N/A").append("\n");
        mensaje.append("Fecha: ").append(fecha != null ? fecha : "N/A").append("\n");
        mensaje.append("Horario: ").append(horario != null ? horario : "N/A").append("\n");
        mensaje.append("Status: ").append(status != null ? status : "Pendiente").append("\n");
        mensaje.append("Profesor: ").append(profesor != null ? profesor : "N/A");

        new AlertDialog.Builder(calendario.this)
                .setTitle("Detalle de reserva")
                .setMessage(mensaje.toString())
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    actualizarStatusReserva(doc.getId(), "Aceptada");
                })
                .setNegativeButton("Rechazar", (dialog, which) -> {
                    actualizarStatusReserva(doc.getId(), "Rechazada");
                })
                .setNeutralButton("Cerrar", null)
                .show();
    }

    private void actualizarStatusReserva(String reservaId, String nuevoStatus) {
        mFirestore.collection("reservas")
                .document(reservaId)
                .update("status", nuevoStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            calendario.this,
                            "Reserva " + nuevoStatus,
                            Toast.LENGTH_SHORT
                    ).show();
                    Log.d("Reservas", "Status actualizado a " + nuevoStatus);
                })
                .addOnFailureListener(e -> {
                    Log.e("Reservas", "Error actualizando status", e);
                    Toast.makeText(
                            calendario.this,
                            "Error al actualizar la reserva",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

}
