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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.Constants;
import com.example.nirs.DatabaseHelper;
import com.example.nirs.R;
import com.example.nirs.adapters.OrdersAdapter;
import com.example.nirs.entity.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

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

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(requireContext(), "Возврат в меню", Toast.LENGTH_SHORT).show();
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });
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
        ordersAdapter = new OrdersAdapter(orderList);
        ordersRecyclerView.setAdapter(ordersAdapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        orderList.clear();
        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

        if (userId != -1) {
            Cursor cursor = dbHelper.getUserOrders(userId);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );
                    orderList.add(order);
                } while (cursor.moveToNext());
                cursor.close();
            }

            if (orderList.isEmpty()) {
                noOrdersText.setVisibility(View.VISIBLE);
                ordersRecyclerView.setVisibility(View.GONE);
            } else {
                noOrdersText.setVisibility(View.GONE);
                ordersRecyclerView.setVisibility(View.VISIBLE);
                ordersAdapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            noOrdersText.setText("Войдите в аккаунт для просмотра заказов");
            noOrdersText.setVisibility(View.VISIBLE);
            ordersRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}