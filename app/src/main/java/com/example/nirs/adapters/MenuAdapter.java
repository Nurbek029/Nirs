package com.example.nirs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.entity.Dish;
import com.example.nirs.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.DishViewHolder> {

    private List<Dish> dishList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddToCartClick(int position);
        void onItemClick(int position);
    }

    public MenuAdapter(List<Dish> dishList, OnItemClickListener listener) {
        this.dishList = dishList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishList.get(position);
        holder.dishName.setText(dish.getName());
        holder.dishDescription.setText(dish.getDescription());
        holder.dishPrice.setText(dish.getFormattedPrice());

        holder.addToCartButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(position);
            }
        });

        // Обработка клика на весь элемент
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        TextView dishName, dishDescription, dishPrice;
        Button addToCartButton;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishName = itemView.findViewById(R.id.dish_name);
            dishDescription = itemView.findViewById(R.id.dish_description);
            dishPrice = itemView.findViewById(R.id.dish_price);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_btn);
        }
    }
}