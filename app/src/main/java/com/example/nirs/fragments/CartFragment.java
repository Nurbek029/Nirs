package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.Constants;
import com.example.nirs.DatabaseHelper;
import com.example.nirs.R;
import com.example.nirs.adapters.CartAdapter;
import com.example.nirs.entity.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private TextView itemsCountText, totalPriceText;
    private Button checkoutButton;
    private DatabaseHelper dbHelper;
    private List<CartItem> cartItems;
    private CartAdapter cartAdapter;
    private SharedPreferences sharedPreferences;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        dbHelper = new DatabaseHelper(getContext());
        cartItems = new ArrayList<>();

        // Инициализация UI
        cartRecyclerView = view.findViewById(R.id.cart_recycler_view);
        itemsCountText = view.findViewById(R.id.items_count);
        totalPriceText = view.findViewById(R.id.total_price);
        checkoutButton = view.findViewById(R.id.checkout_button);

        // Настройка RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemListener() {
            @Override
            public void onQuantityChanged() {
                updateCartSummary();
            }

            @Override
            public void onItemRemoved(int position) {
                removeItemFromCart(position);
            }
        });
        cartRecyclerView.setAdapter(cartAdapter);

        // Обработчик кнопки оформления заказа
        checkoutButton.setOnClickListener(v -> checkout());

        // Загрузка данных корзины
        loadCartItems();

        return view;
    }

    private void loadCartItems() {
        cartItems.clear();
        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

        if (userId != -1) {
            Cursor cursor = dbHelper.getCartItems(userId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        CartItem item = new CartItem(
                                cursor.getInt(cursor.getColumnIndexOrThrow("cart_id")),
                                cursor.getInt(cursor.getColumnIndexOrThrow("dish_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                                cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                        );
                        cartItems.add(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }

            cartAdapter.notifyDataSetChanged();
            updateCartSummary();
        } else {
            Toast.makeText(getContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartSummary() {
        int totalItems = 0;
        double totalPrice = 0;

        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            totalPrice += item.getPrice() * item.getQuantity();
        }

        itemsCountText.setText(String.format("%d товара", totalItems));
        totalPriceText.setText(String.format("%.0f сом", totalPrice));

        // Показываем или скрываем кнопку оформления заказа
        checkoutButton.setEnabled(totalItems > 0);
        checkoutButton.setAlpha(totalItems > 0 ? 1.0f : 0.5f);
    }

    private void removeItemFromCart(int position) {
        if (position >= 0 && position < cartItems.size()) {
            CartItem item = cartItems.get(position);
            int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

            if (userId != -1) {
                boolean success = dbHelper.removeFromCart(item.getCartId(), userId);
                if (success) {
                    cartItems.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    updateCartSummary();
                    Toast.makeText(getContext(), "Товар удален из корзины", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ошибка удаления товара", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Корзина пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            return;
        }

        // Рассчитываем общую сумму
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getPrice() * item.getQuantity();
        }

        // Создаем заказ
        long orderId = dbHelper.createOrder(userId, totalAmount, cartItems);

        if (orderId != -1) {
            Toast.makeText(getContext(),
                    String.format("Заказ #%d успешно оформлен! Сумма: %.0f сом", orderId, totalAmount),
                    Toast.LENGTH_LONG).show();

            // Очистка корзины после оформления
            cartItems.clear();
            cartAdapter.notifyDataSetChanged();
            updateCartSummary();
        } else {
            Toast.makeText(getContext(), "Ошибка оформления заказа", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartItems();
    }
}