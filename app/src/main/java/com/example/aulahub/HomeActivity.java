package com.example.aulahub;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.aulahub.utils.ToolbarManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;


public class HomeActivity extends com.example.aulahub.utils.ToolbarManager {

    private boolean isAdmin = false;
    private String Aula;
    private String Horario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ImageButton mImageButton = findViewById(R.id.IbtnMenu);
        ImageView mFotoPerfil = findViewById(R.id.IVPerfil);


        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        Aula = getIntent().getStringExtra("Aula");
        Horario = getIntent().getStringExtra("Horario");

        if (user == null){
            Intent intent =  new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        inicializarToolbar(mFotoPerfil, mImageButton);

        // Recuperar los CardView
        CardView mCardAulaA = findViewById(R.id.card_aulaA);
        CardView mCardAulaB = findViewById(R.id.card_aulaB);
        CardView mCardAulaC = findViewById(R.id.card_aulaC);
        CardView mAuditorio = findViewById(R.id.card_auditorio);

        aplicarRestricciones(isAdmin, Aula);

        // obetner el FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Error obteniendo token", task.getException());
                        return;
                    }

                    // ESTE es el famoso FCM token
                    String token = task.getResult();
                    Log.d("FCM", "Token FCM: " + token);

                    // Si el usuario actual es PROFESOR:
                    mFirestore.collection("profesores")
                            .document(uid)                // tu “pura llave”
                            .update("fcmToken", token);   // aquí se guarda

                    // Si fuera ADMIN:
                    // mFirestore.collection("admins").document(uid).update("fcmToken", token);
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // -------------------------------------
        // BLOQUE DE EVENTO DE LAS CARDS
        // -------------------------------------
        View.OnClickListener irAula_Horario = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent exportarToCalendario = new Intent(HomeActivity.this, calendario.class);

                int id = v.getId();

                if (id == R.id.card_aulaA) {
                    exportarToCalendario.putExtra("roomName", "Laboratorio de Cómputo A");
                    exportarToCalendario.putExtra("modulo", "Módulo 1") ;
                    exportarToCalendario.putExtra("imagen", R.drawable.aula_a);
                } else if (id == R.id.card_aulaB) {
                    exportarToCalendario.putExtra("roomName", "Laboratorio de Cómputo B");
                    exportarToCalendario.putExtra("modulo", "Módulo 1");
                    exportarToCalendario.putExtra("imagen", R.drawable.aula_b);
                } else if (id == R.id.card_aulaC) {
                    exportarToCalendario.putExtra("roomName", "Laboratorio de Cómputo C");
                    exportarToCalendario.putExtra("modulo", "Módulo 2");
                    exportarToCalendario.putExtra("imagen", R.drawable.spoooky__3_);
                } else if (id == R.id.card_auditorio) {
                    exportarToCalendario.putExtra("roomName", "Auditorio FIC");
                    exportarToCalendario.putExtra("modulo", "Módulo 2");
                    exportarToCalendario.putExtra("imagen", R.drawable.auditorio);
                }
                exportarToCalendario.putExtra("isAdmin", isAdmin);
                startActivity(exportarToCalendario);
            }
        };


        // Asignar el listener a cada card
        mCardAulaA.setOnClickListener(irAula_Horario);
        mCardAulaB.setOnClickListener(irAula_Horario);
        mCardAulaC.setOnClickListener(irAula_Horario);
        mAuditorio.setOnClickListener(irAula_Horario);


    }

    private void aplicarRestricciones(boolean isAdmin, String Aula) {
        CardView mCardAulaA = findViewById(R.id.card_aulaA);
        CardView mCardAulaB = findViewById(R.id.card_aulaB);
        CardView mCardAulaC = findViewById(R.id.card_aulaC);
        CardView mAuditorio = findViewById(R.id.card_auditorio);

        // Primero ocultamos todo por defecto
        mCardAulaA.setVisibility(View.GONE);
        mCardAulaB.setVisibility(View.GONE);
        mCardAulaC.setVisibility(View.GONE);
        mAuditorio.setVisibility(View.GONE);

        if (!isAdmin) {
            // Si es maestro podrá ver todo
            mCardAulaA.setVisibility(View.VISIBLE);
            mCardAulaB.setVisibility(View.VISIBLE);
            mCardAulaC.setVisibility(View.VISIBLE);
            mAuditorio.setVisibility(View.VISIBLE);
        } else {
            switch (Aula) {
                case "AB":
                    mCardAulaA.setVisibility(View.VISIBLE);
                    mCardAulaB.setVisibility(View.VISIBLE);
                    break;
                case "C":
                    mCardAulaC.setVisibility(View.VISIBLE);
                    break;
                case "Auditorio":
                    mAuditorio.setVisibility(View.VISIBLE);
                    break;
                default:
                    Log.e("Firestore", "Aula no reconocida: " + Aula);
                    break;
            }
        }
    }
}