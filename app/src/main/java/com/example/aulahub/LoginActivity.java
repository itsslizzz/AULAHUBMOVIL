package com.example.aulahub;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {


    private EditText mEditTextEmail;
    private EditText mEditTextpassword;
    private Button mButtonSign;
    private FirebaseAuth mAuth;

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
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
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