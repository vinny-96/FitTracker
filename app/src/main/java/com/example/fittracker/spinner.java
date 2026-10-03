package com.example.fittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class spinner extends AppCompatActivity {

    private Spinner spEntrenamiento;
    private EditText etMinutos;
    private RadioGroup rgIntensidad;
    private CheckBox cbCalentamiento;
    private CheckBox cbHidratacion;
    private CheckBox cbEstiramiento;
    private RatingBar ratingEsfuerzo;
    private ProgressBar progresoDiario;
    private TextView tvProgreso;
    private RecyclerView rvSesiones;

    private final ArrayList<Sesion> sesiones = new ArrayList<>();
    private SesionAdapter adapter;

    private int minutosAcumulados = 0;
    private static final int META_DIARIA = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spinner);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.ime()
                    );

                    v.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            barras.bottom
                    );
                    return insets;
                }
        );

        spEntrenamiento = findViewById(R.id.spEntrenamiento);
        etMinutos = findViewById(R.id.etMinutos);
        rgIntensidad = findViewById(R.id.rgIntensidad);
        cbCalentamiento = findViewById(R.id.cbCalentamiento);
        cbHidratacion = findViewById(R.id.cbHidratacion);
        cbEstiramiento = findViewById(R.id.cbEstiramiento);
        ratingEsfuerzo = findViewById(R.id.ratingEsfuerzo);
        progresoDiario = findViewById(R.id.progresoDiario);
        tvProgreso = findViewById(R.id.tvProgreso);
        rvSesiones = findViewById(R.id.rvSesiones);

        String[] tipos = {
                "Fuerza", "Cardio", "Yoga", "Calistenia"
        };

        ArrayAdapter<String> tiposAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tipos
        );

        tiposAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spEntrenamiento.setAdapter(tiposAdapter);

        if (savedInstanceState != null) {
            ArrayList<String> tiposGuardados =
                    savedInstanceState.getStringArrayList("tipos");

            ArrayList<String> detallesGuardados =
                    savedInstanceState.getStringArrayList("detalles");

            if (tiposGuardados != null && detallesGuardados != null) {
                int cantidad = Math.min(
                        tiposGuardados.size(),
                        detallesGuardados.size()
                );

                for (int i = 0; i < cantidad; i++) {
                    sesiones.add(new Sesion(
                            tiposGuardados.get(i),
                            detallesGuardados.get(i)
                    ));
                }
            }

            minutosAcumulados =
                    savedInstanceState.getInt("minutosAcumulados", 0);
        }

        adapter = new SesionAdapter(sesiones);
        rvSesiones.setLayoutManager(new LinearLayoutManager(this));
        rvSesiones.setAdapter(adapter);

        progresoDiario.setMax(META_DIARIA);
        actualizarProgreso();

        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(v -> registrarSesion());
    }

    private void registrarSesion() {
        String textoMinutos = etMinutos.getText().toString().trim();

        if (textoMinutos.isEmpty()) {
            etMinutos.setError("Ingresa la duración");
            etMinutos.requestFocus();
            return;
        }

        int minutos;

        try {
            minutos = Integer.parseInt(textoMinutos);
        } catch (NumberFormatException e) {
            etMinutos.setError("Ingresa un número válido");
            return;
        }

        if (minutos <= 0 || minutos > 1440) {
            etMinutos.setError("Ingresa entre 1 y 1440 minutos");
            return;
        }

        int intensidadId = rgIntensidad.getCheckedRadioButtonId();

        if (intensidadId == -1) {
            Toast.makeText(
                    this,
                    "Selecciona una intensidad",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        float esfuerzo = ratingEsfuerzo.getRating();

        if (esfuerzo < 1) {
            Toast.makeText(
                    this,
                    "Califica el esfuerzo de 1 a 5 estrellas",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String tipo = spEntrenamiento.getSelectedItem().toString();
        RadioButton seleccion = findViewById(intensidadId);
        String intensidad = seleccion.getText().toString();

        String detalle =
                "Duración: " + minutos + " minutos"
                        + "\nIntensidad: " + intensidad
                        + " | Esfuerzo: " + (int) esfuerzo + "/5"
                        + "\nCalentamiento: "
                        + (cbCalentamiento.isChecked() ? "Sí" : "No")
                        + "\nHidratación: "
                        + (cbHidratacion.isChecked() ? "Sí" : "No")
                        + "\nEstiramiento: "
                        + (cbEstiramiento.isChecked() ? "Sí" : "No");

        sesiones.add(0, new Sesion(tipo, detalle));
        adapter.notifyItemInserted(0);
        rvSesiones.scrollToPosition(0);

        minutosAcumulados += minutos;
        actualizarProgreso();
        limpiarFormulario();

        Toast.makeText(
                this,
                "Sesión registrada",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void actualizarProgreso() {
        int porcentaje = (int) Math.min(
                100L,
                (long) minutosAcumulados * 100 / META_DIARIA
        );

        progresoDiario.setProgress(
                Math.min(minutosAcumulados, META_DIARIA)
        );

        tvProgreso.setText(
                "Meta diaria: " + minutosAcumulados
                        + " / " + META_DIARIA
                        + " minutos (" + porcentaje + "%)"
        );
    }

    private void limpiarFormulario() {
        etMinutos.setText("");
        rgIntensidad.clearCheck();
        cbCalentamiento.setChecked(false);
        cbHidratacion.setChecked(false);
        cbEstiramiento.setChecked(false);
        ratingEsfuerzo.setRating(1);
        spEntrenamiento.setSelection(0);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> tipos = new ArrayList<>();
        ArrayList<String> detalles = new ArrayList<>();

        for (Sesion sesion : sesiones) {
            tipos.add(sesion.tipo);
            detalles.add(sesion.detalle);
        }

        outState.putStringArrayList("tipos", tipos);
        outState.putStringArrayList("detalles", detalles);
        outState.putInt("minutosAcumulados", minutosAcumulados);
    }

    private static class Sesion {
        final String tipo;
        final String detalle;

        Sesion(String tipo, String detalle) {
            this.tipo = tipo;
            this.detalle = detalle;
        }
    }

    private static class SesionAdapter
            extends RecyclerView.Adapter<SesionAdapter.SesionViewHolder> {

        private final ArrayList<Sesion> sesiones;

        SesionAdapter(ArrayList<Sesion> sesiones) {
            this.sesiones = sesiones;
        }

        @NonNull
        @Override
        public SesionViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {
            View vista = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sesion, parent, false);

            return new SesionViewHolder(vista);
        }

        @Override
        public void onBindViewHolder(
                @NonNull SesionViewHolder holder,
                int position
        ) {
            Sesion sesion = sesiones.get(position);
            holder.tvTipo.setText(sesion.tipo);
            holder.tvDetalle.setText(sesion.detalle);
        }

        @Override
        public int getItemCount() {
            return sesiones.size();
        }

        static class SesionViewHolder extends RecyclerView.ViewHolder {
            final TextView tvTipo;
            final TextView tvDetalle;

            SesionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTipo = itemView.findViewById(R.id.tvTipoSesion);
                tvDetalle = itemView.findViewById(R.id.tvDetalleSesion);
            }
        }
    }
}