package com.kasir.app;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kasir.app.adapter.ProductAdapter;
import com.kasir.app.model.Product;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SCANNER_REQUEST_CODE_20 = 20;
    private static final int SCANNER_REQUEST_CODE_10 = 10;

    private FirebaseUser currentUser;
    private RecyclerView recyclerViewChart;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private Button btnAddProduct, btnCancel;
    private EditText resultText, etNameProduct, etPriceProduct, etBarcodeProduct;
    private TextInputLayout textInput, textInputAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.currentUser = auth.getCurrentUser();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        textInput = findViewById(R.id.textInput);
        resultText = findViewById(R.id.result);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;

        swipeRefreshLayout.setColorSchemeColors(color);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadData();
                    }
                });

        recyclerViewChart = findViewById(R.id.recyclerViewChart);

        recyclerViewChart.setHasFixedSize(true);
        recyclerViewChart.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList, this);
        recyclerViewChart.setAdapter(productAdapter);

        resultText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int start, int before, int count) {}

                    @Override
                    public void onTextChanged(
                            CharSequence charSequence, int start, int before, int count) {
                        String searchText = charSequence.toString().trim();
                        filterResults(searchText);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

        textInput.setEndIconOnClickListener( v ->
                {
                        if (ContextCompat.checkSelfPermission(
                                        MainActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    MainActivity.this, Manifest.permission.CAMERA)) {
                                startScan(SCANNER_REQUEST_CODE_20);
                            } else {
                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[] {Manifest.permission.CAMERA},
                                        0);
                            }
                        } else {
                            startScan(SCANNER_REQUEST_CODE_20);
                        
                    }
                });
    }
    
    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);
        productList.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProductsRef =
                database.getReference(currentUser.getDisplayName()).child("products");

        userProductsRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(
                            @NonNull DataSnapshot dataSnapshot,
                            @Nullable String previousChildName) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (product != null && !productList.contains(product)) {
                            productList.add(0, product);
                            productAdapter.notifyItemInserted(0);
                            productAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onChildChanged(
                            @NonNull DataSnapshot dataSnapshot,
                            @Nullable String previousChildName) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onChildMoved(
                            @NonNull DataSnapshot dataSnapshot,
                            @Nullable String previousChildName) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast("Failed to fetch products: " + databaseError.getMessage());
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void DialogAddProduct() {
        View bottomSheetViewAddProduct =
                getLayoutInflater().inflate(R.layout.activity_add_product, null);
        BottomSheetDialog bottomSheetDialogAddProduct = new BottomSheetDialog(this);
        bottomSheetDialogAddProduct.setContentView(bottomSheetViewAddProduct);
        bottomSheetDialogAddProduct.setCanceledOnTouchOutside(false);

        textInputAdd = bottomSheetViewAddProduct.findViewById(R.id.textInputAdd);
        etNameProduct = bottomSheetViewAddProduct.findViewById(R.id.etNameProduct);
        etPriceProduct = bottomSheetViewAddProduct.findViewById(R.id.etPriceProduct);
        etBarcodeProduct = bottomSheetViewAddProduct.findViewById(R.id.etBarcodeProduct);
        btnAddProduct = bottomSheetViewAddProduct.findViewById(R.id.btnAddProduct);
        btnCancel = bottomSheetViewAddProduct.findViewById(R.id.btnCancel);

        etNameProduct.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etNameProduct, InputMethodManager.SHOW_IMPLICIT);

        textInputAdd.setEndIconOnClickListener(
                v -> {
                    if (ContextCompat.checkSelfPermission(
                                    MainActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                MainActivity.this, Manifest.permission.CAMERA)) {
                            startScan(SCANNER_REQUEST_CODE_10);
                        } else {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[] {Manifest.permission.CAMERA},
                                    0);
                        }
                    } else {
                        startScan(SCANNER_REQUEST_CODE_10);
                    }
                });

        etPriceProduct.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int start, int before, int count) {}

                    @Override
                    public void onTextChanged(
                            CharSequence charSequence, int start, int before, int count) {
                        etPriceProduct.removeTextChangedListener(this);
                        String cleanNumber = charSequence.toString().replaceAll("[^\\d]", "");
                        try {
                            double parsed = Double.parseDouble(cleanNumber);
                            String formatted = NumberFormat.getNumberInstance().format(parsed);
                            etPriceProduct.setText("Rp " + formatted);
                            etPriceProduct.setSelection(etPriceProduct.getText().length());
                        } catch (NumberFormatException e) {
                        }
                        etPriceProduct.addTextChangedListener(this);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

        btnAddProduct.setOnClickListener(
                v -> {
                    String productName = etNameProduct.getText().toString();
                    String productPrice = etPriceProduct.getText().toString();
                    String productBarcode = etBarcodeProduct.getText().toString();

                    if (productName.isEmpty()
                            || productPrice.isEmpty()
                            || productBarcode.isEmpty()) {
                        showToast("Isi semua data");
                        return;
                    }

                    if (isNameAlreadyExists(productName)) {
                        showToast(
                                "Produk dengan nama tersebut sudah terdaftar. Gunakan nama yang berbeda.");
                        return;
                    }

                    if (isBarcodeAlreadyExists(productBarcode)) {
                        showToast(
                                "Produk dengan barcode tersebut sudah terdaftar. Gunakan barcode yang berbeda.");
                        return;
                    }

                    saveProductToDatabase(productName, productPrice, productBarcode);

                    showToast("Produk ditambahkan");
                    bottomSheetDialogAddProduct.dismiss();
                });

        btnCancel.setOnClickListener(
                v -> {
                    bottomSheetDialogAddProduct.dismiss();
                });

        bottomSheetDialogAddProduct.show();
    }

    private boolean isNameAlreadyExists(String name) {
        for (Product product : productList) {
            if (product.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBarcodeAlreadyExists(String barcode) {
        for (Product product : productList) {
            if (product.getBarcode().equalsIgnoreCase(barcode)) {
                return true;
            }
        }
        return false;
    }

    private void saveProductToDatabase(String name, String price, String barcode) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProductsRef =
                database.getReference(currentUser.getDisplayName()).child("products");

        String productId = userProductsRef.push().getKey();

        Product product = new Product(productId, name, price, barcode);
        userProductsRef.child(productId).setValue(product);
    }

    private void DialogExit() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String displayName = user != null ? user.getDisplayName() : "User";

        builder.setTitle("" + displayName)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Apakah yakin ingin keluar?")
                .setPositiveButton(
                        "Ya",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                .setNegativeButton(
                        "Tidak",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setCancelable(false);

        builder.show();
    }

    private void startScan(int requestCode) {
        Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCANNER_REQUEST_CODE_20) {
            handleScanResult(resultCode, data, resultText);
        } else if (requestCode == SCANNER_REQUEST_CODE_10) {
            handleScanResult(resultCode, data, etBarcodeProduct);
        }
    }

    private void handleScanResult(int resultCode, Intent data, EditText targetEditText) {
        if (resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra("result");
            targetEditText.setText(code);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan(SCANNER_REQUEST_CODE_20);
            } else {
                showToast("Gagal membuka kamera!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addProduct) {
            DialogAddProduct();
        }

        if (id == R.id.logout) {
            DialogExit();
        }

        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void filterResults(String query) {
        if (query.isEmpty()) {
            productAdapter.filterList(productList);
        } else {
            List<Product> filteredList = new ArrayList<>();
            for (Product product : productList) {
                if ((product.getName() != null
                                && product.getName()
                                        .trim()
                                        .toLowerCase()
                                        .contains(query.toLowerCase()))
                        || (product.getBarcode() != null
                                && product.getBarcode()
                                        .trim()
                                        .toLowerCase()
                                        .contains(query.toLowerCase()))) {
                    filteredList.add(product);
                }
            }
            productAdapter.filterList(filteredList);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }
}
