package com.example.aulahub;

import android.content.Intent;
import android.content.SharedPreferences;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mEditTextEmail = (EditText) findViewById(R.id.ETemail);
        mEditTextpassword = (EditText) findViewById(R.id.ETpassword);
        mButtonSign = (Button) findViewById(R.id.btnlogin);

        mButtonSign.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Email = mEditTextEmail.getText().toString();
                Password = mEditTextpassword.getText().toString();

                if(!Email.isEmpty() && !Password.isEmpty()){

                    loginUser();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Complete los campos", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void loginUser(){
        mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    String uid = mAuth.getCurrentUser().getUid();

                    mFirestore.collection("roles").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Boolean adminDb = documentSnapshot.getBoolean("admin");

                                    boolean isAdmin = Boolean.TRUE.equals(adminDb);
                                    String Aula = documentSnapshot.getString("Aula");
                                    String Horario= documentSnapshot.getString("Horario");
                                    SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                                    prefs.edit().clear().apply();  // Limpia antes de guardar nuevos datos
                                    prefs.edit()
                                            .putBoolean("isAdmin", isAdmin)
                                            .putString("Aula", Aula)
                                            .putString("Horario", Horario)
                                            .apply();  // Usa apply() para realizar la operación de manera asíncrona
                                    Log.d("LoginActivity", "isAdmin saved to SharedPreferences: " + isAdmin);

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.putExtra("isAdmin", isAdmin);
                                    intent.putExtra("Aula", Aula);
                                    intent.putExtra("Horario", Horario);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Log.e("Firestore", "No existe el documento");
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("Firestore", "Error al obtener datos: ", e);
                            });
                }
                else{
                    Toast.makeText(LoginActivity.this, "No se pudo iniciar sesion", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }
}