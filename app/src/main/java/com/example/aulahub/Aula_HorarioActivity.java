package com.example.aulahub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class Aula_HorarioActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aula_horario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aula_horario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //instanciar las variables
        Spinner mspTurno = findViewById(R.id.spTurno);
        Spinner mspAula = findViewById(R.id.spAula);
        Button btnContinuar = findViewById(R.id.btnContinuar);
        String uid = mAuth.getCurrentUser().getUid();
        List<String> listaAulas = new ArrayList<>();



        btnContinuar.setOnClickListener(v -> {
            String turnoSeleccionado = mspTurno.getSelectedItem().toString();
            String aulaSeleccionada = mspAula.getSelectedItem().toString();
            //recupero valor del HomeActivity
            String CardSeleccionada = getIntent().getStringExtra("CardSeleccionada");

            Intent intent = new Intent(Aula_HorarioActivity.this, CalendarioActivity.class);
            //experto valores a CalendarioActivity
            intent.putExtra("CardSeleccionada", CardSeleccionada);
            intent.putExtra("turno", turnoSeleccionado);
            intent.putExtra("aula", aulaSeleccionada);
            startActivity(intent);
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("FirestoreDebug", "No hay usuario autenticado");
            return;
        }

        mFirestore.collection("profesores")
                .document(uid)
                   .collection("salones")
                      .get()
                         .addOnSuccessListener(queryDocumentSnapshots -> {
                          listaAulas.clear();
                          for (DocumentSnapshot doc : queryDocumentSnapshots){
                               listaAulas.add(doc.getId());
                          }
                          if (listaAulas.isEmpty()) {
                              listaAulas.add("Sin aulas disponibles");
                          }
                             ArrayAdapter<String> adapterAula = new ArrayAdapter<>(
                               this,
                                     android.R.layout.simple_spinner_item,
                                       listaAulas
                             );
                            adapterAula.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                            mspAula.setAdapter(adapterAula);
                     });

    }
}

