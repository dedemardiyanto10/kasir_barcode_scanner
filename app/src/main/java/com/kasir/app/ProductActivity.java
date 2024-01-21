package com.kasir.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kasir.app.adapter.ProductAdapter;
import com.kasir.app.model.Product;
import java.text.NumberFormat;
import java.util.List;

public class ProductActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    private Button btnEditProduct, btnDelete;
    private EditText etNameProduct, etPriceProduct, etBarcodeProduct;
    private TextInputLayout textInputEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.currentUser = auth.getCurrentUser();

        Intent intent = getIntent();

        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String price = intent.getStringExtra("price");
        String barcode = intent.getStringExtra("barcode");

        textInputEdit = findViewById(R.id.textInputEdit);
        etNameProduct = findViewById(R.id.etNameProduct);
        etPriceProduct = findViewById(R.id.etPriceProduct);
        etBarcodeProduct = findViewById(R.id.etBarcodeProduct);
        btnEditProduct = findViewById(R.id.btnEditProduct);
        btnDelete = findViewById(R.id.btnDelete);

        etNameProduct.setText(name);
        etPriceProduct.setText(price);
        etBarcodeProduct.setText(barcode);

        etNameProduct.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etNameProduct, InputMethodManager.SHOW_IMPLICIT);

        textInputEdit.setEndIconOnClickListener(
                v -> {
                    if (ContextCompat.checkSelfPermission(
                                    ProductActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                ProductActivity.this, Manifest.permission.CAMERA)) {
                            startScan();
                        } else {
                            ActivityCompat.requestPermissions(
                                    ProductActivity.this,
                                    new String[] {Manifest.permission.CAMERA},
                                    0);
                        }
                    } else {
                        startScan();
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

        btnEditProduct.setOnClickListener(
                v -> {
                    String updatedName = etNameProduct.getText().toString();
                    String updatedPrice = etPriceProduct.getText().toString();
                    String updatedBarcode = etBarcodeProduct.getText().toString();

                    if (updatedName.isEmpty()
                            || updatedPrice.isEmpty()
                            || updatedBarcode.isEmpty()) {
                        showToast("Isi semua data");
                        return;
                    }

                    String productId = intent.getStringExtra("id");
                    Product updatedProduct =
                            new Product(productId, updatedName, updatedPrice, updatedBarcode);

                    updateProductInDatabase(updatedProduct);

                    showToast("Produk berhasil diupdate");
                });

        btnDelete.setOnClickListener(
                v -> {
                    String productId = intent.getStringExtra("id");
                    Product productToDelete =
                            new Product(
                                    productId,
                                    etNameProduct.getText().toString(),
                                    etPriceProduct.getText().toString(),
                                    etBarcodeProduct.getText().toString());

                    deleteProductFromDatabase(productToDelete);

                    showToast("Produk berhasil dihapus");
                });
    }

    private void updateProductInDatabase(Product product) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProductsRef =
                database.getReference(currentUser.getDisplayName()).child("products");

        String productId = product.getId();

        if (productId != null) {
            DatabaseReference productRef = userProductsRef.child(productId);
            productRef.setValue(product);
            finish();
        }
    }

    private void deleteProductFromDatabase(Product product) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProductsRef =
                database.getReference(currentUser.getDisplayName()).child("products");

        String productId = product.getId();

        if (productId != null) {
            DatabaseReference productRef = userProductsRef.child(productId);
            productRef
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> {});
            finish();
        }
    }

    private void startScan() {
        Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
        startActivityForResult(intent, 20);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20) {
            if (resultCode == RESULT_OK && data != null) {
                String code = data.getStringExtra("result");
                etBarcodeProduct.setText(code);
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
