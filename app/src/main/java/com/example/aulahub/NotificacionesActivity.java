package com.example.aulahub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificacionesActivity extends AppCompatActivity {

    private LinearLayout container;
    private TextView tvVacio;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        container = findViewById(R.id.containerNotificaciones);
        tvVacio = findViewById(R.id.tvSinNotificaciones);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cargarNotificaciones();
        }
    }

    private void cargarNotificaciones() {
        db.collection("notificaciones")
                .whereEqualTo("destinatarioUid", uid)
                .orderBy("fecha", Query.Direction.DESCENDING) // Las más nuevas primero
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    container.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvVacio.setVisibility(View.VISIBLE);
                        return;
                    }
                    tvVacio.setVisibility(View.GONE);

                    LayoutInflater inflater = LayoutInflater.from(this);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        View card = inflater.inflate(R.layout.item_notificacion, container, false);

                        TextView tvTitulo = card.findViewById(R.id.tvTituloNoti);
                        TextView tvMensaje = card.findViewById(R.id.tvMensajeNoti);
                        TextView tvFecha = card.findViewById(R.id.tvFechaNoti);

                        tvTitulo.setText(doc.getString("titulo"));
                        tvMensaje.setText(doc.getString("mensaje"));

                        Date date = doc.getDate("fecha");
                        if (date != null) {
                            tvFecha.setText(sdf.format(date));
                        }

                        // --- MAGIA: Marcar como leída automáticamente al verla ---
                        if (Boolean.FALSE.equals(doc.getBoolean("leido"))) {
                            db.collection("notificaciones").document(doc.getId())
                                    .update("leido", true);
                        }

                        container.addView(card);
                    }
                });
    }
}