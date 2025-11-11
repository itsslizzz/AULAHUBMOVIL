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


public class HomeActivity extends com.example.aulahub.utils.ToolbarManager {

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ImageButton mImageButton = findViewById(R.id.IbtnMenu);
        ImageView mFotoPerfil = findViewById(R.id.IVPerfil);

        // Recuperar los CardView
        CardView mCardAulaA = findViewById(R.id.card_aulaA);
        CardView mCardAulaB = findViewById(R.id.card_aulaB);
        CardView mCardAulaC = findViewById(R.id.card_aulaC);
        CardView mAuditorio = findViewById(R.id.card_auditorio);

        if (user == null){
            Intent intent =  new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        inicializarToolbar(mFotoPerfil, mImageButton);

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
                Intent intent = new Intent(HomeActivity.this, calendario.class);
                Intent exportarToToolbar = new Intent(HomeActivity.this, ToolbarManager.class);

                int id = v.getId();

                if (id == R.id.card_aulaA) {
                    intent.putExtra("roomName", "Laboratorio de Cómputo A");
                    intent.putExtra("modulo", "Módulo 1") ;
                    intent.putExtra("imagen", R.drawable.aula_a);
                } else if (id == R.id.card_aulaB) {
                    intent.putExtra("roomName", "Laboratorio de Cómputo B");
                    intent.putExtra("modulo", "Módulo 1");
                    intent.putExtra("imagen", R.drawable.aula_b);
                } else if (id == R.id.card_aulaC) {
                    intent.putExtra("roomName", "Laboratorio de Cómputo C");
                    intent.putExtra("modulo", "Módulo 2");
                    intent.putExtra("imagen", R.drawable.spoooky__3_);
                } else if (id == R.id.card_auditorio) {
                    intent.putExtra("roomName", "Auditorio FIC");
                    intent.putExtra("modulo", "Módulo 2");
                    intent.putExtra("imagen", R.drawable.auditorio);
                }
                intent.putExtra("isAdmin", isAdmin);
                exportarToToolbar.putExtra("isAdmin", isAdmin);
                startActivity(intent);
            }
        };


        // Asignar el listener a cada card
        mCardAulaA.setOnClickListener(irAula_Horario);
        mCardAulaB.setOnClickListener(irAula_Horario);
        mCardAulaC.setOnClickListener(irAula_Horario);
        mAuditorio.setOnClickListener(irAula_Horario);

        // -------------------------------------
        // BLOQUE DE FIRESTORE (ROLES)
        // -------------------------------------
        mFirestore.collection("roles").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Boolean adminDb = documentSnapshot.getBoolean("admin");

                            isAdmin = Boolean.TRUE.equals(adminDb);

                            String Aula = documentSnapshot.getString("Aula");
                            aplicarRestricciones(isAdmin, Aula);
                        } else {
                            Log.e("Firestore", "No existe el documento");
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener datos: ", e);
                });
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









