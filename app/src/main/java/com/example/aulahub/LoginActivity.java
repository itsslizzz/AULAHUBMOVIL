package com.example.aulahub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText mEditTextEmail;
    private EditText mEditTextpassword;
    private Button mButtonSign;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private String Email = "";
    private String Password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ajuste visual para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase y vistas
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mEditTextEmail = findViewById(R.id.ETemail);
        mEditTextpassword = findViewById(R.id.ETpassword);
        mButtonSign = findViewById(R.id.btnlogin);

        mButtonSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = mEditTextEmail.getText().toString();
                Password = mEditTextpassword.getText().toString();

                if (!Email.isEmpty() && !Password.isEmpty()) {
                    loginUser();
                } else {
                    Toast.makeText(LoginActivity.this, "Complete los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser() {
        mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();

                    // Buscar el documento en la colección 'roles'
                    mFirestore.collection("roles")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        // Si existe en roles, obtener los datos de la colección 'roles'
                                        Boolean adminDb = documentSnapshot.getBoolean("admin");
                                        String Aula = documentSnapshot.getString("Aula");

                                        boolean isAdmin = Boolean.TRUE.equals(adminDb);
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.putExtra("isAdmin", isAdmin);
                                        intent.putExtra("Aula", Aula);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Si no está en roles, buscar en la colección 'profesores'
                                        mFirestore.collection("profesores")
                                                .document(uid)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot profesorDoc) {
                                                        if (profesorDoc.exists()) {
                                                            // Si el documento existe en 'profesores', proceder
                                                            String nombreProfesor = profesorDoc.getString("Nombre");

                                                            // Proceder con el flujo si necesitas mostrar algo específico para el profesor
                                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            intent.putExtra("isAdmin", false); // Asignar el valor de admin como 'false'
                                                            intent.putExtra("Aula", ""); // Si no hay aula asignada, dejar vacío
                                                            intent.putExtra("profesorNombre", nombreProfesor); // Agregar el nombre del profesor
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Log.e("Firestore", "No existe el documento ni en roles ni en profesores");
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Firestore", "Error al obtener datos del profesor: ", e);
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error al obtener datos de roles: ", e);
                            });

                } else {
                    Toast.makeText(LoginActivity.this, "No se pudo iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Si ya hay un usuario logueado, pasar directamente a HomeActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }
}
