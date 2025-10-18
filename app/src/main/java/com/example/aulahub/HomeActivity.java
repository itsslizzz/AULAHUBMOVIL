package com.example.aulahub;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
         ImageButton mImageButton = findViewById(R.id.IbtnMenu);
         String uid = mAuth.getCurrentUser().getUid();

         //inicializar el popup menu
        PopupMenu popupMenu = new PopupMenu(this,mImageButton);

        //mostrar lo que esta en menu_popup en el layou activity_home
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==R.id.ItemAjustes){
                   //inicializar el submenu de ajuste
                    PopupMenu subMenu = new PopupMenu(HomeActivity.this, mImageButton);

                    //mostrar lo que esta en submenu_ajuste en el layou activity_home
                    subMenu.getMenuInflater().inflate(R.menu.submenu_ajuste, subMenu.getMenu());

                    // Mostrar correo directamente del usuario autenticado
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
                            if (subId== R.id.ItemShowEmail){
                                return true;
                            }
                            else if(subId == R.id.ItemChangePassword){
                                return true;
                            }
                            else if(subId == R.id.ItemLogOut){
                                mAuth.signOut();
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                finish();
                                return true;
                            }
                            return false;
                        }

                    });
                    //mostrar el submenu de ajuste
                    subMenu.show();

                }else if(id==R.id.ItemAyuda){
                      return true;
                }
                else if(id==R.id.ItemMisReservas){
                    return true;
                }
                return false;
            }
        });

         //Evento para mostrar el menu hamburguesa
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               popupMenu.show();
                popupMenu.show();

            }
        });

        // Evento para obtener los datos del usuario
        mFirestore.collection("roles").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
