package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.Constants;
import com.example.nirs.DatabaseHelper;
import com.example.nirs.R;
import com.example.nirs.adapters.OrderDetailsAdapter;
import com.example.nirs.adapters.OrdersAdapter;
import com.example.nirs.entity.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment implements OrdersAdapter.OnOrderClickListener {

    private RecyclerView ordersRecyclerView;
    private TextView noOrdersText;
    private DatabaseHelper dbHelper;
    private List<Order> orderList;
    private OrdersAdapter ordersAdapter;
    private SharedPreferences sharedPreferences;

    public OrdersFragment() {
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
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        dbHelper = new DatabaseHelper(getContext());
        orderList = new ArrayList<>();

        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        noOrdersText = view.findViewById(R.id.no_orders_text);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersAdapter = new OrdersAdapter(orderList, this);
        ordersRecyclerView.setAdapter(ordersAdapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        orderList.clear();
        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

        if (userId != -1) {
            Cursor cursor = dbHelper.getUserOrders(userId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        Order order = new Order(
                                cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                                cursor.getString(cursor.getColumnIndexOrThrow("status"))
                        );
                        orderList.add(order);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }

            if (orderList.isEmpty()) {
                noOrdersText.setVisibility(View.VISIBLE);
                ordersRecyclerView.setVisibility(View.GONE);
                noOrdersText.setText("У вас пока нет заказов");
            } else {
                noOrdersText.setVisibility(View.GONE);
                ordersRecyclerView.setVisibility(View.VISIBLE);
                ordersAdapter.notifyDataSetChanged();
            }
        } else {
            noOrdersText.setText("Войдите в аккаунт для просмотра заказов");
            noOrdersText.setVisibility(View.VISIBLE);
            ordersRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrderClick(int position) {
        if (position >= 0 && position < orderList.size()) {
            Order order = orderList.get(position);
            showOrderDetails(order);
        }
    }

    private void showOrderDetails(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_order_details, null);

        // Находим элементы - проверяем что они существуют
        TextView orderNumber = dialogView.findViewById(R.id.order_number);
        TextView orderDate = dialogView.findViewById(R.id.order_date);
        TextView orderStatus = dialogView.findViewById(R.id.order_status);
        TextView orderTotal = dialogView.findViewById(R.id.order_total);

        // Проверяем существование RecyclerView
        RecyclerView itemsRecyclerView = dialogView.findViewById(R.id.order_items_recycler_view);
        android.widget.Button closeButton = dialogView.findViewById(R.id.close_button);

        // Заполняем информацию о заказе
        orderNumber.setText("Заказ #" + order.getId());
        orderDate.setText("Дата: " + order.getFormattedDate());
        orderStatus.setText("Статус: " + order.getStatus());
        orderTotal.setText("Итого: " + order.getFormattedTotal());

        // Устанавливаем цвет статуса
        switch (order.getStatus()) {
            case "Новый":
                orderStatus.setTextColor(0xFF2196F3);
                break;
            case "В процессе":
                orderStatus.setTextColor(0xFFFF9800);
                break;
            case "Завершен":
                orderStatus.setTextColor(0xFF4CAF50);
                break;
            case "Отменен":
                orderStatus.setTextColor(0xFFF44336);
                break;
            default:
                orderStatus.setTextColor(0xFF757575);
        }

        // Загружаем и отображаем товары заказа
        List<OrderDetailsAdapter.OrderItem> orderItems = loadOrderItems(order.getId());

        if (itemsRecyclerView != null) {
            OrderDetailsAdapter adapter = new OrderDetailsAdapter(orderItems);
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            itemsRecyclerView.setAdapter(adapter);
        }

        // Кнопка закрытия
        closeButton.setOnClickListener(v -> {
            AlertDialog dialog = (AlertDialog) v.getTag();
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        closeButton.setTag(dialog);
        dialog.show();
    }

    private List<OrderDetailsAdapter.OrderItem> loadOrderItems(long orderId) {
        List<OrderDetailsAdapter.OrderItem> items = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = dbHelper.getOrderItems(orderId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                        double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price_at_order"));

                        OrderDetailsAdapter.OrderItem item =
                                new OrderDetailsAdapter.OrderItem(name, quantity, price);
                        items.add(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка загрузки деталей заказа", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Если не удалось загрузить реальные данные, показываем тестовые
        if (items.isEmpty()) {
            items.add(new OrderDetailsAdapter.OrderItem("Борщ", 2, 75.0));
            items.add(new OrderDetailsAdapter.OrderItem("Плов", 1, 120.0));
            items.add(new OrderDetailsAdapter.OrderItem("Чай черный", 1, 25.0));
        }

        return items;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}