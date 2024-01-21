package com.kasir.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.kasir.app.ProductActivity;
import com.kasir.app.R;
import com.kasir.app.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public void addProduct(Product product) {
        productList.add(product);
    }

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Animation animation =
                AnimationUtils.loadAnimation(
                        holder.itemView.getContext(), android.R.anim.slide_in_left);

        Product product = productList.get(position);
        holder.productNameTextView.setText(product.getName());
        holder.productPriceTextView.setText(product.getPrice());
        holder.productBarcodeTextView.setText(product.getBarcode());

        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filterList(List<Product> filteredList) {
        productList = filteredList;
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, productPriceTextView, productBarcodeTextView;
        MaterialCardView cardViewOptions;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            productBarcodeTextView = itemView.findViewById(R.id.productBarcodeTextView);
            cardViewOptions = itemView.findViewById(R.id.cardViewOptions);

            cardViewOptions.setOnClickListener(
                    view -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Product product = productList.get(position);
                            Intent intent = new Intent(view.getContext(), ProductActivity.class);
                            intent.putExtra("position", position);
                            intent.putExtra("id", product.getId());
                            intent.putExtra("name", product.getName());
                            intent.putExtra("price", product.getPrice());
                            intent.putExtra("barcode", product.getBarcode());
                            view.getContext().startActivity(intent);
                        }
                    });
        }
    }
}
