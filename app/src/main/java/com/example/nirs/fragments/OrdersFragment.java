package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.AddPaymentActivity;
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
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        }
        Log.d("OrdersFragment", "onCreate called");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("OrdersFragment", "onCreateView called");
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
        Log.d("OrdersFragment", "Loading orders...");
        orderList.clear();

        if (sharedPreferences == null && getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        }

        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);
        Log.d("OrdersFragment", "UserID from SharedPreferences: " + userId);

        if (userId != -1) {
            Cursor cursor = dbHelper.getUserOrders(userId);
            Log.d("OrdersFragment", "Cursor count: " + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                        String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                        double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                        Log.d("OrdersFragment", "Found order - ID: " + orderId + ", Date: " + orderDate + ", Total: " + totalAmount + ", Status: " + status);

                        Order order = new Order(
                                orderId,
                                orderDate,
                                totalAmount,
                                status
                        );

                        // Получаем чек оплаты
                        int paymentProofIndex = cursor.getColumnIndex("payment_proof");
                        if (paymentProofIndex != -1) {
                            String paymentProof = cursor.getString(paymentProofIndex);
                            order.setPaymentProof(paymentProof);
                            Log.d("OrdersFragment", "Payment proof for order " + orderId + ": " + (paymentProof != null ? "exists" : "null"));
                        }

                        // Получаем email пользователя
                        int emailIndex = cursor.getColumnIndex("user_email");
                        if (emailIndex != -1) {
                            String userEmail = cursor.getString(emailIndex);
                            order.setUserEmail(userEmail);
                        }

                        orderList.add(order);
                    } catch (Exception e) {
                        Log.e("OrdersFragment", "Error parsing order: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                cursor.close();
            } else {
                Log.d("OrdersFragment", "Cursor is null for user: " + userId);
            }

            if (orderList.isEmpty()) {
                Log.d("OrdersFragment", "No orders found for user");
                noOrdersText.setVisibility(View.VISIBLE);
                ordersRecyclerView.setVisibility(View.GONE);
                noOrdersText.setText("У вас пока нет заказов");
            } else {
                Log.d("OrdersFragment", "Loaded " + orderList.size() + " orders");
                noOrdersText.setVisibility(View.GONE);
                ordersRecyclerView.setVisibility(View.VISIBLE);
                ordersAdapter.notifyDataSetChanged();
            }
        } else {
            Log.e("OrdersFragment", "Invalid user ID: " + userId);
            noOrdersText.setText("Войдите в аккаунт для просмотра заказов");
            noOrdersText.setVisibility(View.VISIBLE);
            ordersRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrderClick(int position) {
        Log.d("OrdersFragment", "Order clicked at position: " + position);
        if (position >= 0 && position < orderList.size()) {
            Order order = orderList.get(position);
            Log.d("OrdersFragment", "Showing details for order: " + order.getId());
            showOrderDetails(order);
        } else {
            Toast.makeText(getContext(), "Ошибка: заказ не найден", Toast.LENGTH_SHORT).show();
        }
    }

    // Публичный метод для показа деталей заказа (используется в AdminOrdersFragment)
    public void showOrderDetails(Order order) {
        if (order == null) {
            Log.e("OrdersFragment", "Order is null in showOrderDetails");
            Toast.makeText(getContext(), "Ошибка: данные заказа отсутствуют", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getContext() == null || !isAdded()) {
            Log.e("OrdersFragment", "Fragment not attached to context");
            return;
        }

        try {
            Log.d("OrdersFragment", "Creating order details dialog for order: " + order.getId());

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_order_details, null);

            // Находим элементы
            TextView orderNumber = dialogView.findViewById(R.id.order_number);
            TextView orderDate = dialogView.findViewById(R.id.order_date);
            TextView orderStatus = dialogView.findViewById(R.id.order_status);
            TextView orderTotal = dialogView.findViewById(R.id.order_total);
            TextView userEmail = dialogView.findViewById(R.id.user_email);
            TextView paymentTitle = dialogView.findViewById(R.id.payment_title);
            ImageView ivPaymentProof = dialogView.findViewById(R.id.iv_payment_proof);
            Button btnAddPayment = dialogView.findViewById(R.id.btn_add_payment);
            RecyclerView itemsRecyclerView = dialogView.findViewById(R.id.order_items_recycler_view);
            Button closeButton = dialogView.findViewById(R.id.close_button);

            // Скрываем email пользователя (только для админа)
            if (userEmail != null) {
                userEmail.setVisibility(View.GONE);
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
                Log.d("OrdersFragment", "Order has payment proof, displaying...");
                try {
                    // Декодируем base64 строку в Bitmap
                    byte[] decodedString = android.util.Base64.decode(order.getPaymentProof(), android.util.Base64.DEFAULT);
                    if (decodedString != null && decodedString.length > 0) {
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedBitmap != null) {
                            ivPaymentProof.setImageBitmap(decodedBitmap);
                            ivPaymentProof.setVisibility(View.VISIBLE);
                            btnAddPayment.setVisibility(View.GONE);
                            paymentTitle.setVisibility(View.VISIBLE);
                            Log.d("OrdersFragment", "Payment proof image displayed successfully");
                        } else {
                            Log.d("OrdersFragment", "Failed to decode bitmap");
                            ivPaymentProof.setVisibility(View.GONE);
                            if ("Принятый".equals(order.getStatus())) {
                                btnAddPayment.setVisibility(View.VISIBLE);
                                paymentTitle.setVisibility(View.VISIBLE);
                            } else {
                                paymentTitle.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Log.d("OrdersFragment", "Decoded string is empty");
                        ivPaymentProof.setVisibility(View.GONE);
                        if ("Принятый".equals(order.getStatus())) {
                            btnAddPayment.setVisibility(View.VISIBLE);
                            paymentTitle.setVisibility(View.VISIBLE);
                        } else {
                            paymentTitle.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Log.e("OrdersFragment", "Error decoding payment proof: " + e.getMessage());
                    e.printStackTrace();
                    ivPaymentProof.setVisibility(View.GONE);
                    if ("Принятый".equals(order.getStatus())) {
                        btnAddPayment.setVisibility(View.VISIBLE);
                        paymentTitle.setVisibility(View.VISIBLE);
                    } else {
                        paymentTitle.setVisibility(View.GONE);
                    }
                }
            } else {
                Log.d("OrdersFragment", "Order has no payment proof");
                ivPaymentProof.setVisibility(View.GONE);
                // Показываем кнопку добавления чека только для принятых заказов
                if ("Принятый".equals(order.getStatus())) {
                    btnAddPayment.setVisibility(View.VISIBLE);
                    paymentTitle.setVisibility(View.VISIBLE);
                    Log.d("OrdersFragment", "Showing add payment button for accepted order");
                } else {
                    btnAddPayment.setVisibility(View.GONE);
                    paymentTitle.setVisibility(View.GONE);
                }
            }

            // Кнопка добавления чека
            btnAddPayment.setOnClickListener(v -> {
                Log.d("OrdersFragment", "Add payment button clicked for order: " + order.getId());
                showAddPaymentDialog(order);
                AlertDialog dialog = (AlertDialog) v.getTag();
                if (dialog != null) {
                    dialog.dismiss();
                }
            });

            // Загружаем и отображаем товары заказа
            List<OrderDetailsAdapter.OrderItem> orderItems = loadOrderItems(order.getId());
            Log.d("OrdersFragment", "Loaded " + orderItems.size() + " items for order");

            OrderDetailsAdapter adapter = new OrderDetailsAdapter(orderItems);
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            itemsRecyclerView.setAdapter(adapter);

            // Кнопка закрытия - ИСПРАВЛЕНО: используем правильный тип
            closeButton.setOnClickListener(v -> {
                AlertDialog dialog = (AlertDialog) v.getTag();
                if (dialog != null) {
                    dialog.dismiss();
                }
            });

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            closeButton.setTag(dialog);
            btnAddPayment.setTag(dialog);
            dialog.show();

            Log.d("OrdersFragment", "Order details dialog shown successfully");

        } catch (Exception e) {
            Log.e("OrdersFragment", "Error showing order details: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при открытии деталей заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddPaymentDialog(Order order) {
        try {
            Log.d("OrdersFragment", "Opening AddPaymentActivity for order: " + order.getId());
            Intent intent = new Intent(getActivity(), AddPaymentActivity.class);
            intent.putExtra("order_id", order.getId());
            intent.putExtra("order_number", String.valueOf(order.getId()));
            startActivityForResult(intent, 200);
        } catch (Exception e) {
            Log.e("OrdersFragment", "Error opening AddPaymentActivity: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при открытии формы оплаты", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            Log.d("OrdersFragment", "Payment added successfully, refreshing orders...");
            Toast.makeText(getContext(), "Чек оплаты добавлен", Toast.LENGTH_SHORT).show();
            loadOrders();
        }
    }

    private List<OrderDetailsAdapter.OrderItem> loadOrderItems(long orderId) {
        List<OrderDetailsAdapter.OrderItem> items = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = dbHelper.getOrderItems(orderId);
            Log.d("OrdersFragment", "Loading items for order: " + orderId);

            if (cursor != null) {
                Log.d("OrdersFragment", "Cursor count for items: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    try {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                        double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price_at_order"));

                        OrderDetailsAdapter.OrderItem item =
                                new OrderDetailsAdapter.OrderItem(name, quantity, price);
                        items.add(item);

                        Log.d("OrdersFragment", "Item: " + name + " x" + quantity + " = " + (quantity * price));
                    } catch (Exception e) {
                        Log.e("OrdersFragment", "Error parsing order item: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("OrdersFragment", "Error loading order items: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка загрузки деталей заказа", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Если не удалось загрузить реальные данные, показываем тестовые для отладки
        if (items.isEmpty()) {
            Log.d("OrdersFragment", "No items found, showing test data");
            items.add(new OrderDetailsAdapter.OrderItem("Борщ", 2, 75.0));
            items.add(new OrderDetailsAdapter.OrderItem("Плов", 1, 120.0));
            items.add(new OrderDetailsAdapter.OrderItem("Чай черный", 1, 25.0));
        }

        return items;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("OrdersFragment", "onResume called, refreshing orders...");
        loadOrders();
    }
}