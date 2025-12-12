package com.example.nirs.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.entity.Dish;
import com.example.nirs.R;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.DishViewHolder> {

    private List<Dish> dishList;
    private OnItemClickListener listener;
    private boolean isAdmin = false;

    public interface OnItemClickListener {
        void onAddToCartClick(int position);
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public MenuAdapter(List<Dish> dishList, OnItemClickListener listener, boolean isAdmin) {
        this.dishList = dishList;
        this.listener = listener;
        this.isAdmin = isAdmin;
        Log.d("MenuAdapter", "Adapter created, isAdmin: " + isAdmin);
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

        // ЗАГРУЗКА КАРТИНКИ ИЗ BASE64
        try {
            String imageBase64 = dish.getImage();
            if (imageBase64 != null && !imageBase64.isEmpty() && !imageBase64.equals("null")) {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedBitmap != null) {
                    holder.dishImage.setImageBitmap(decodedBitmap);
                } else {
                    holder.dishImage.setImageResource(R.drawable.placeholder_food);
                }
            } else {
                holder.dishImage.setImageResource(R.drawable.placeholder_food);
            }
        } catch (Exception e) {
            Log.e("MenuAdapter", "Error loading image: " + e.getMessage());
            holder.dishImage.setImageResource(R.drawable.placeholder_food);
        }

        // ВАЖНО: Сначала устанавливаем видимость кнопки
        if (isAdmin) {
            holder.addToCartButton.setVisibility(View.GONE);
        } else {
            holder.addToCartButton.setVisibility(View.VISIBLE);
        }

        // Обработка клика на кнопку добавления в корзину
        holder.addToCartButton.setOnClickListener(v -> {
            Log.d("MenuAdapter", "Add to cart clicked at position: " + position);
            if (listener != null && !isAdmin) {
                listener.onAddToCartClick(position);
            }
        });

        // Обработка клика на весь элемент
        holder.itemView.setOnClickListener(v -> {
            Log.d("MenuAdapter", "Item clicked at position: " + position);
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishList != null ? dishList.size() : 0;
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView dishImage;
        TextView dishName, dishDescription, dishPrice;
        Button addToCartButton;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImage = itemView.findViewById(R.id.dish_image);
            dishName = itemView.findViewById(R.id.dish_name);
            dishDescription = itemView.findViewById(R.id.dish_description);
            dishPrice = itemView.findViewById(R.id.dish_price);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_btn);
        }
    }
}