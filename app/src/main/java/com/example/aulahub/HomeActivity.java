package com.example.aulahub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

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

public class HomeActivity extends AppCompatActivity {

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

        // Instanciar las variables
        CardView mCardAulaA = findViewById(R.id.card_aulaA);
        CardView mCardAulaB = findViewById(R.id.card_aulaB);
        CardView mCardAulaC = findViewById(R.id.card_aulaC);
        CardView mAuditorio = findViewById(R.id.card_auditorio);
        ImageButton mImageButton = findViewById(R.id.IbtnMenu);
        String uid = mAuth.getCurrentUser().getUid();

        // -------------------------------------
        // BLOQUE DE EVENTO DE LAS CARDS
        // -------------------------------------
        View.OnClickListener irAula_Horario = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, reservas.class);

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

                startActivity(intent);
            }
        };

        // Asignar el listener a cada card
        mCardAulaA.setOnClickListener(irAula_Horario);
        mCardAulaB.setOnClickListener(irAula_Horario);
        mCardAulaC.setOnClickListener(irAula_Horario);
        mAuditorio.setOnClickListener(irAula_Horario);

        // -------------------------------------
        // BLOQUE DEL MENÚ POPUP
        // -------------------------------------
        PopupMenu popupMenu = new PopupMenu(this, mImageButton);
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.ItemAjustes) {
                    PopupMenu subMenu = new PopupMenu(HomeActivity.this, mImageButton);
                    subMenu.getMenuInflater().inflate(R.menu.submenu_ajuste, subMenu.getMenu());

                    // Mostrar correo del usuario autenticado
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String email = currentUser.getEmail();
                        MenuItem emailItem = subMenu.getMenu().findItem(R.id.ItemShowEmail);
                        emailItem.setTitle(email);
                    }

                    subMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem subitem) {
                            int subId = subitem.getItemId();
                            if (subId == R.id.ItemShowEmail) {
                                return true;
                            } else if (subId == R.id.ItemChangePassword) {
                                return true;
                            } else if (subId == R.id.ItemLogOut) {
                                mAuth.signOut();
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                finish();
                                return true;
                            }
                            return false;
                        }
                    });

                    subMenu.show();

                } else if (id == R.id.ItemAyuda) {
                    return true;
                } else if (id == R.id.ItemMisReservas) {
                    return true;
                }
                return false;
            }
        });

        // Evento para mostrar el menú hamburguesa
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        // -------------------------------------
        // BLOQUE DE FIRESTORE (ROLES)
        // -------------------------------------
        mFirestore.collection("roles").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");
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

    private void aplicarRestricciones(Boolean isAdmin, String Aula) {
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
