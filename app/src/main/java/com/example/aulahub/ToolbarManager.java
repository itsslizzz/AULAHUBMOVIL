package com.example.aulahub.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.aulahub.LoginActivity;
import com.example.aulahub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ToolbarManager extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected FirebaseFirestore mFirestore;
    protected FirebaseStorage mStorage;
    protected ImageView mFotoPerfil;
    protected ImageButton mImageButton;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    protected void inicializarToolbar(ImageView fotoPerfil, ImageButton imageButton) {
        this.mFotoPerfil = fotoPerfil;
        this.mImageButton = imageButton;

        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        cargarFotoPerfil(uid);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            mFotoPerfil.setImageURI(imageUri);
                            subirFoto(imageUri);
                        }
                    }
                });

        PopupMenu popupMenu = new PopupMenu(this, mImageButton);
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.ItemAjustes) {
                mostrarSubmenuAjustes(email);
            }else if (id == R.id.ItemAyuda) {
                return true;
            }else if (id == R.id.ItemMisReservas) {
                return true;
            } else if (id == R.id.ItemSubirFoto) {
                imageChooser();
            }else if (id == R.id.ItemBorrarFoto) {
                BorrarFoto(uid);
            }
            return true;
        });

        mImageButton.setOnClickListener(v -> popupMenu.show());
    }

    private void mostrarSubmenuAjustes(String Email) {
        PopupMenu subMenu = new PopupMenu(this, mImageButton);
        subMenu.getMenuInflater().inflate(R.menu.submenu_ajuste, subMenu.getMenu());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            subMenu.getMenu().findItem(R.id.ItemShowEmail).setTitle(currentUser.getEmail());
        }

        subMenu.setOnMenuItemClickListener(subitem -> {
            int subId = subitem.getItemId();
            if (subId == R.id.ItemChangePassword) {
                ChangePassword(Email);
            } else if (subId == R.id.ItemLogOut) {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            return true;
        });

        subMenu.show();
    }

    private void ChangePassword(String Email) {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(Email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Correo de restablecimiento enviado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void imageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Selecciona una imagen"));
    }

    private void subirFoto(Uri FotoUri) {
        String uid = mAuth.getCurrentUser().getUid();
        StorageReference ref = mStorage.getReference(uid + "/fotos_perfil.jpg");

        ref.delete().addOnCompleteListener(task -> {
            ref.putFile(FotoUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                guardarFoto(uid, downloadUri.toString());
                                Toast.makeText(this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                            })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void guardarFoto(String uid, String Url) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("FotoPerfil", Url);
        mFirestore.collection("users").document(uid)
                .set(datos, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "URL guardada correctamente"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error al guardar URL", e));
    }

    private void cargarFotoPerfil(String uid) {
        mFirestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String url = documentSnapshot.getString("FotoPerfil");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this)
                                    .load(url)
                                    .placeholder(R.drawable.ic_perfil)
                                    .into(mFotoPerfil);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error al cargar la foto", e));
    }

    private void BorrarFoto(String uid) {
        StorageReference ref = mStorage.getReference(uid + "/fotos_perfil.jpg");
        ref.delete()
                .addOnSuccessListener(aVoid -> {
                    mFirestore.collection("users").document(uid)
                            .update("FotoPerfil", FieldValue.delete())
                            .addOnSuccessListener(unused -> {
                                mFotoPerfil.setImageResource(R.drawable.ic_perfil);
                                Toast.makeText(this, "Foto eliminada correctamente", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al eliminar en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar en Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
