package com.example.aulahub;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class HomeActivity extends AppCompatActivity {

    private Button mbtnSignOut;
    private TextView mTextViewEmail;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //instanciar las variables
        mTextViewEmail = (TextView) findViewById(R.id.TVShowEmail);
        mbtnSignOut = (Button) findViewById(R.id.btnSignOut);
        String uid = mAuth.getCurrentUser().getUid();

        // Metodo para obtener los datos del usuario
        mFirestore.collection("roles").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    Boolean isAdmin = documentSnapshot.getBoolean("admin");

                    Object rawAula = documentSnapshot.get("Aula");
                    List<String> Aula = new ArrayList<>();

                    if (rawAula instanceof List) {
                        Aula = (List<String>) rawAula;
                    } else if (rawAula instanceof String) {
                        Aula.add((String) rawAula);
                    }

                    aplicarRestricciones(isAdmin, Aula);
                } else {
                    Log.e("Firestore", "No existe el documento");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error al obtener datos: ", e);
        });


        // Mostrar correo directamente del usuario autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            mTextViewEmail.setText(email);
        }

        // Evento para cerrar sesion
        mbtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void aplicarRestricciones(Boolean isAdmin, List<String> AulaList) {
        CardView mCardAulaA = findViewById(R.id.card_aulaA);
        CardView mCardAulaB = findViewById(R.id.card_aulaB);
        CardView mCardAulaC = findViewById(R.id.card_aulaC);
        CardView mAuditorio = findViewById(R.id.card_auditorio);

        //primero ocultamos todo por defecto
        mCardAulaA.setVisibility(View.GONE);
        mCardAulaB.setVisibility(View.GONE);
        mCardAulaC.setVisibility(View.GONE);
        mAuditorio.setVisibility(View.GONE);

        if (!isAdmin) {
            //si es maestro podra ver todo
            mCardAulaA.setVisibility(View.VISIBLE);
            mCardAulaB.setVisibility(View.VISIBLE);
            mCardAulaC.setVisibility(View.VISIBLE);
            mAuditorio.setVisibility(View.VISIBLE);
        } else {
            if (AulaList != null && !AulaList.isEmpty()) {
                for (String aula : AulaList) {
                    switch (aula) {
                        case "A":
                            mCardAulaA.setVisibility(View.VISIBLE);
                            break;
                        case "B":
                            mCardAulaB.setVisibility(View.VISIBLE);
                            break;
                        case "C":
                            mCardAulaC.setVisibility(View.VISIBLE);
                            break;
                        case "Auditorio":
                            mAuditorio.setVisibility(View.VISIBLE);
                            break;
                        default:
                            Log.e("Firestore", "Aula no reconocida: " + aula);
                            break;
                    }
                }
            }
        }
    }
}

