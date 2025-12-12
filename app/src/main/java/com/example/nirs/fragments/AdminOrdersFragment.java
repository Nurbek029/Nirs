package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.Constants;
import com.example.nirs.DatabaseHelper;
import com.example.nirs.R;
import com.example.nirs.adapters.AdminOrdersAdapter;
import com.example.nirs.adapters.OrderDetailsAdapter;
import com.example.nirs.entity.Order;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersFragment extends Fragment implements AdminOrdersAdapter.OnOrderActionListener {

    private RecyclerView ordersRecyclerView;
    private LinearLayout filterLayout;
    private TextView noOrdersText;
    private DatabaseHelper dbHelper;
    private List<Order> orderList;
    private AdminOrdersAdapter ordersAdapter;
    private SharedPreferences sharedPreferences;
    private String currentFilter = "Все";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        dbHelper = new DatabaseHelper(getContext());
        orderList = new ArrayList<>();

        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        filterLayout = view.findViewById(R.id.filter_layout);
        noOrdersText = view.findViewById(R.id.no_orders_text);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersAdapter = new AdminOrdersAdapter(orderList, this);
        ordersRecyclerView.setAdapter(ordersAdapter);

        setupFilters();
        loadAllOrders();

        return view;
    }

    private void setupFilters() {
        String[] filters = {"Все", "В обработке", "Принятые", "Готовые", "Завершенные"};

        // Очищаем фильтры
        filterLayout.removeAllViews();

        for (String filter : filters) {
            Button button = new Button(getContext());
            button.setText(filter);
            button.setAllCaps(false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                currentFilter = filter;
                updateFilterButtons();

                if (filter.equals("Все")) {
                    loadAllOrders();
                } else {
                    loadOrdersByStatus(getStatusForFilter(filter));
                }
            });

            filterLayout.addView(button);
        }

        updateFilterButtons();
    }

    private String getStatusForFilter(String filter) {
        switch (filter) {
            case "В обработке": return "В обработке";
            case "Принятые": return "Принятый";
            case "Готовые": return "Готов";
            case "Завершенные": return "Завершен";
            default: return "";
        }
    }

    private void updateFilterButtons() {
        for (int i = 0; i < filterLayout.getChildCount(); i++) {
            View child = filterLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                if (button.getText().toString().equals(currentFilter)) {
                    button.setBackgroundResource(R.drawable.button_category_active);
                    button.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    button.setBackgroundResource(R.drawable.button_category);
                    button.setTextColor(getResources().getColor(R.color.color_primary));
                }
            }
        }
    }

    private void loadAllOrders() {
        orderList.clear();
        Cursor cursor = dbHelper.getAllOrders();

        if (cursor != null) {
            Log.d("AdminOrdersFragment", "Total orders in DB: " + cursor.getCount());

            while (cursor.moveToNext()) {
                try {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );

                    // Получаем email пользователя
                    int emailIndex = cursor.getColumnIndex("user_email");
                    if (emailIndex != -1) {
                        order.setUserEmail(cursor.getString(emailIndex));
                    }

                    // Получаем чек оплаты
                    int paymentProofIndex = cursor.getColumnIndex("payment_proof");
                    if (paymentProofIndex != -1) {
                        String paymentProof = cursor.getString(paymentProofIndex);
                        order.setPaymentProof(paymentProof);
                    }

                    orderList.add(order);
                    Log.d("AdminOrdersFragment", "Added order: #" + order.getId() + " - " + order.getStatus());
                } catch (Exception e) {
                    Log.e("AdminOrdersFragment", "Error parsing order: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            cursor.close();
        } else {
            Log.d("AdminOrdersFragment", "Cursor is null");
        }

        updateOrdersList();
    }

    private void loadOrdersByStatus(String status) {
        orderList.clear();
        Cursor cursor = dbHelper.getOrdersByStatus(status);

        if (cursor != null) {
            Log.d("AdminOrdersFragment", "Orders with status '" + status + "': " + cursor.getCount());

            while (cursor.moveToNext()) {
                try {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );

                    // Получаем email пользователя
                    int emailIndex = cursor.getColumnIndex("user_email");
                    if (emailIndex != -1) {
                        order.setUserEmail(cursor.getString(emailIndex));
                    }

                    // Получаем чек оплаты
                    int paymentProofIndex = cursor.getColumnIndex("payment_proof");
                    if (paymentProofIndex != -1) {
                        String paymentProof = cursor.getString(paymentProofIndex);
                        order.setPaymentProof(paymentProof);
                    }

                    orderList.add(order);
                } catch (Exception e) {
                    Log.e("AdminOrdersFragment", "Error parsing order: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        updateOrdersList();
    }

    private void updateOrdersList() {
        if (orderList.isEmpty()) {
            noOrdersText.setVisibility(View.VISIBLE);
            ordersRecyclerView.setVisibility(View.GONE);
            noOrdersText.setText("Заказов нет");
            Log.d("AdminOrdersFragment", "No orders to display");
        } else {
            noOrdersText.setVisibility(View.GONE);
            ordersRecyclerView.setVisibility(View.VISIBLE);
            ordersAdapter.notifyDataSetChanged();
            Log.d("AdminOrdersFragment", "Displaying " + orderList.size() + " orders");
        }
    }

    @Override
    public void onStatusChange(long orderId, String newStatus) {
        Log.d("AdminOrdersFragment", "Changing status for order #" + orderId + " to: " + newStatus);

        boolean success = dbHelper.updateOrderStatus(orderId, newStatus);

        if (success) {
            Toast.makeText(getContext(), "Статус заказа обновлен", Toast.LENGTH_SHORT).show();

            // Обновляем список
            if (currentFilter.equals("Все")) {
                loadAllOrders();
            } else {
                loadOrdersByStatus(getStatusForFilter(currentFilter));
            }
        } else {
            Toast.makeText(getContext(), "Ошибка обновления статуса", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetails(Order order) {
        Log.d("AdminOrdersFragment", "Viewing details for order #" + order.getId());
        showOrderDetailsDialog(order);
    }

    // ИСПРАВЛЕННЫЙ МЕТОД: используем androidx.appcompat.app.AlertDialog
    private void showOrderDetailsDialog(Order order) {
        if (order == null || getContext() == null) {
            Toast.makeText(getContext(), "Ошибка: данные заказа отсутствуют", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_order_details, null);

            // Находим элементы
            TextView orderNumber = dialogView.findViewById(R.id.order_number);
            TextView orderDate = dialogView.findViewById(R.id.order_date);
            TextView orderStatus = dialogView.findViewById(R.id.order_status);
            TextView orderTotal = dialogView.findViewById(R.id.order_total);
            TextView userEmail = dialogView.findViewById(R.id.user_email); // НОВОЕ ПОЛЕ
            TextView paymentTitle = dialogView.findViewById(R.id.payment_title);
            ImageView ivPaymentProof = dialogView.findViewById(R.id.iv_payment_proof);
            Button btnAddPayment = dialogView.findViewById(R.id.btn_add_payment);
            RecyclerView itemsRecyclerView = dialogView.findViewById(R.id.order_items_recycler_view);
            Button closeButton = dialogView.findViewById(R.id.close_button);

            // ДОБАВЛЯЕМ ПОЛЬЗОВАТЕЛЯ для админа
            if (userEmail != null) {
                if (order.getUserEmail() != null && !order.getUserEmail().isEmpty()) {
                    userEmail.setText("Пользователь: " + order.getUserEmail());
                    userEmail.setVisibility(View.VISIBLE);
                } else {
                    userEmail.setVisibility(View.GONE);
                }
            }

            // Заполняем информацию о заказе
            orderNumber.setText("Заказ #" + order.getId());
            orderDate.setText("Дата: " + order.getFormattedDate());
            orderStatus.setText("Статус: " + order.getStatus());
            orderTotal.setText("Итого: " + order.getFormattedTotal());

            // Устанавливаем цвет статуса
            orderStatus.setTextColor(order.getStatusColor());

            // Отображаем чек оплаты если есть
            boolean hasPaymentProof = order.getPaymentProof() != null && !order.getPaymentProof().isEmpty();

            if (hasPaymentProof) {
                try {
                    byte[] decodedString = android.util.Base64.decode(order.getPaymentProof(), android.util.Base64.DEFAULT);
                    if (decodedString != null && decodedString.length > 0) {
                        android.graphics.Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedBitmap != null) {
                            ivPaymentProof.setImageBitmap(decodedBitmap);
                            ivPaymentProof.setVisibility(View.VISIBLE);
                            btnAddPayment.setVisibility(View.GONE);
                            paymentTitle.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ivPaymentProof.setVisibility(View.GONE);
                    paymentTitle.setVisibility(View.VISIBLE);
                }
            } else {
                ivPaymentProof.setVisibility(View.GONE);
                paymentTitle.setVisibility(View.VISIBLE);
                // Для админа скрываем кнопку добавления чека
                btnAddPayment.setVisibility(View.GONE);
            }

            // Загружаем и отображаем товары заказа
            List<OrderDetailsAdapter.OrderItem> orderItems = loadOrderItems(order.getId());

            OrderDetailsAdapter adapter = new OrderDetailsAdapter(orderItems);
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            itemsRecyclerView.setAdapter(adapter);

            // Кнопка закрытия - ИСПРАВЛЕНО: используем правильный тип AlertDialog
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

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при открытии деталей заказа", Toast.LENGTH_SHORT).show();
        }
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

        return items;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentFilter.equals("Все")) {
            loadAllOrders();
        } else {
            loadOrdersByStatus(getStatusForFilter(currentFilter));
        }
    }
}