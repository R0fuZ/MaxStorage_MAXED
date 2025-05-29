package com.example.maxstorage.Almacen;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.maxstorage.Login.UserDAO;
import com.example.maxstorage.Login.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;

public class AlmacenPaquetesActivity extends AppCompatActivity {

    private SearchView      searchView;
    private RecyclerView    rvPaquetes;
    private TextView        areaDetalles;
    private ImageView       ivQrDetail;
    private Button          btnBorrar;
    private AlmacenDAO      dao;
    private UserDAO         userDao;
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
        ivQrDetail   = findViewById(R.id.ivQrDetail);
        btnBorrar    = findViewById(R.id.btnBorrarPaquete);

        // 1.a) Personaliza el SearchView
        @SuppressLint("RestrictedApi")
        SearchAutoComplete searchText =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchText.setTextColor(Color.BLACK);
        searchText.setHintTextColor(Color.BLACK);

        // 2) DAOs
        dao      = new AlmacenDAO(this);
        userDao  = new UserDAO(this);

        // 3) Carga y configura lista
        originalList = dao.getAll();
        filteredList = new ArrayList<>(originalList);
        adapter = new AlmacenAdapter(filteredList, this::showDetails);
        rvPaquetes.setLayoutManager(new LinearLayoutManager(this));
        rvPaquetes.setAdapter(adapter);

        // 4) Filtro en SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query)  { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterById(newText.trim());
                return true;
            }
        });

        // 5) Borrar paquete
        btnBorrar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Borrar paquete")
                    .setMessage("Ingresa ID de paquete a borrar:");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("Borrar", (dlg, which) -> {
                String txt = input.getText().toString().trim();
                if (txt.isEmpty()) {
                    Toast.makeText(this, "Debes ingresar un ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                int id = Integer.parseInt(txt);
                int rows = dao.delete(id);
                if (rows > 0) {
                    Toast.makeText(this, "Paquete eliminado", Toast.LENGTH_SHORT).show();
                    originalList.clear();
                    originalList.addAll(dao.getAll());
                    filteredList.clear();
                    filteredList.addAll(originalList);
                    adapter.notifyDataSetChanged();
                    areaDetalles.setText("");
                    ivQrDetail.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "No existe paquete con ID " + id,
                            Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancelar", null).show();
        });
    }

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
        ivQrDetail.setVisibility(View.GONE);
    }

    private void showDetails(Almacen a) {
        // Obtener nombre del usuario que registró
        User u = userDao.findById(a.getRegisteredBy());
        String ownerName = (u != null) ? u.getOwnerName() : "Desconocido";

        String details = ""
                + "ID: "             + a.getPackageId()               + "\n"
                + "Peso: "           + a.getWeight()                  + " kg\n"
                + "Registrado por: " + ownerName                      + "\n"
                + "Entrada: "        + a.getArrivalDate()            + "\n"
                + "Salida: "         + a.getEstimatedDepartureDate() + "\n"
                + "Estado: "         + a.getStatus();
        areaDetalles.setText(details);

        // Generar y mostrar el QR
        Bitmap qrBmp = generateQrBitmap(a.getQrCode(), 200, 200);
        if (qrBmp != null) {
            ivQrDetail.setImageBitmap(qrBmp);
            ivQrDetail.setVisibility(View.VISIBLE);
        } else {
            ivQrDetail.setVisibility(View.GONE);
        }
    }

    private Bitmap generateQrBitmap(String text, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            com.google.zxing.common.BitMatrix bm =
                    writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bm.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
