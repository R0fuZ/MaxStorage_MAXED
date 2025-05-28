package com.example.maxstorage.Almacen;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.SearchAutoComplete;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maxstorage.R;

import java.util.ArrayList;
import java.util.List;

public class AlmacenPaquetesActivity extends AppCompatActivity {

    private SearchView      searchView;
    private RecyclerView    rvPaquetes;
    private TextView        areaDetalles;
    private Button          btnBorrar;
    private AlmacenDAO      dao;
    private List<Almacen>   originalList;
    private List<Almacen>   filteredList;
    private AlmacenAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_almacen_paquetes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // 1) Referencias
        searchView   = findViewById(R.id.searchViewId);
        rvPaquetes   = findViewById(R.id.rvPaquetes);
        areaDetalles = findViewById(R.id.areaDetalles);
        btnBorrar    = findViewById(R.id.btnBorrarPaquete);

        // 1.a) Personaliza el SearchView para usar texto negro
        @SuppressLint("RestrictedApi")
        SearchAutoComplete searchText =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchText.setTextColor(Color.BLACK);
        searchText.setHintTextColor(Color.BLACK);

        // 2) Carga datos desde la BD
        dao           = new AlmacenDAO(this);
        originalList  = dao.getAll();
        filteredList  = new ArrayList<>(originalList);

        // 3) Configura RecyclerView y Adapter
        adapter = new AlmacenAdapter(filteredList, this::showDetails);
        rvPaquetes.setLayoutManager(new LinearLayoutManager(this));
        rvPaquetes.setAdapter(adapter);

        // 4) Busca por ID al escribir en SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query)  { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterById(newText.trim());
                return true;
            }
        });

        // 5) Botón para borrar paquete
        btnBorrar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Borrar paquete");
            builder.setMessage("Ingresa ID de paquete a borrar:");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("Borrar", (dialog, which) -> {
                String texto = input.getText().toString().trim();
                if (texto.isEmpty()) {
                    Toast.makeText(this, "Debes ingresar un ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                int id = Integer.parseInt(texto);
                int filas = dao.delete(id);
                if (filas > 0) {
                    Toast.makeText(this, "Paquete eliminado", Toast.LENGTH_SHORT).show();
                    // Actualiza listas
                    originalList.clear();
                    originalList.addAll(dao.getAll());
                    filteredList.clear();
                    filteredList.addAll(originalList);
                    adapter.notifyDataSetChanged();
                    areaDetalles.setText("");
                } else {
                    Toast.makeText(this, "No existe paquete con ID " + id, Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
    }

    /** Filtra la lista por package_id que contenga el texto */
    private void filterById(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            for (Almacen a : originalList) {
                if (String.valueOf(a.getPackageId()).contains(text)) {
                    filteredList.add(a);
                }
            }
        }
        adapter.notifyDataSetChanged();
        areaDetalles.setText("");
    }

    /** Muestra en el TextView los detalles del paquete seleccionado */
    private void showDetails(Almacen a) {
        String details = ""
                + "ID: "             + a.getPackageId()               + "\n"
                + "Peso: "           + a.getWeight()                  + " kg\n"
                + "Registrado por: " + a.getRegisteredBy()           + "\n"
                + "Entrada: "        + a.getArrivalDate()            + "\n"
                + "Salida: "         + a.getEstimatedDepartureDate() + "\n"
                + "QR: "             + a.getQrCode()                 + "\n"
                + "Estado: "         + a.getStatus();
        areaDetalles.setText(details);
    }
}



