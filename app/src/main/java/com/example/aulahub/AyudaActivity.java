package com.example.aulahub;




import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class AyudaActivity extends com.example.aulahub.utils.ToolbarManager {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ayuda);
        ImageButton mImageButton = findViewById(R.id.IbtnMenu);
        ImageView mFotoPerfil = findViewById(R.id.IVPerfil);

        inicializarToolbar(mFotoPerfil, mImageButton);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AyudaLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método genérico que funciona para todas las tarjetas
    public void expand(View view) {
        // 1. Obtener el contenedor interno de la CardView (el LinearLayout)
        LinearLayout container = (LinearLayout) ((androidx.cardview.widget.CardView) view).getChildAt(0);

        // 2. Buscar el último hijo del contenedor (el TextView con los detalles)
        View detalles = container.getChildAt(container.getChildCount() - 1);

        // 3. Alternar visibilidad con animación
        int visible = (detalles.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
        TransitionManager.beginDelayedTransition(container, new AutoTransition());
        detalles.setVisibility(visible);
    }
}









