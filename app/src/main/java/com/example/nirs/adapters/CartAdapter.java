package com.example.nirs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.R;
import com.example.nirs.entity.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_dish, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.dishName.setText(item.getName());
        holder.dishPrice.setText(String.format("%.0f сом", item.getPrice()));
        holder.quantityText.setText(String.valueOf(item.getQuantity()));
        holder.totalText.setText(String.format("%.0f сом", item.getPrice() * item.getQuantity()));

        holder.increaseButton.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            holder.quantityText.setText(String.valueOf(item.getQuantity()));
            holder.totalText.setText(String.format("%.0f сом", item.getPrice() * item.getQuantity()));
            if (listener != null) {
                listener.onQuantityChanged();
            }
        });

        holder.decreaseButton.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                holder.quantityText.setText(String.valueOf(item.getQuantity()));
                holder.totalText.setText(String.format("%.0f сом", item.getPrice() * item.getQuantity()));
                if (listener != null) {
                    listener.onQuantityChanged();
                }
            } else {
                if (listener != null) {
                    listener.onItemRemoved(position);
                }
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView dishName, dishPrice, quantityText, totalText;
        Button decreaseButton, increaseButton;
        ImageButton removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            dishName = itemView.findViewById(R.id.dish_name);
            dishPrice = itemView.findViewById(R.id.dish_price);
            quantityText = itemView.findViewById(R.id.tv_quantity);
            totalText = itemView.findViewById(R.id.total_text);
            decreaseButton = itemView.findViewById(R.id.btn_decrease);
            increaseButton = itemView.findViewById(R.id.btn_increase);
            removeButton = itemView.findViewById(R.id.btn_remove);
        }
    }
}