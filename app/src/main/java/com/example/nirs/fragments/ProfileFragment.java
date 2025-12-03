package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.example.nirs.Constants;
import com.example.nirs.DatabaseHelper;
import com.example.nirs.LoginActivity;
import com.example.nirs.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private Button logoutButton;
    private TextView userEmail, memberSince, totalOrders, totalSpent;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        dbHelper = new DatabaseHelper(getContext());

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(requireContext(), "Возврат в меню", Toast.LENGTH_SHORT).show();
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userEmail = view.findViewById(R.id.user_email);
        memberSince = view.findViewById(R.id.member_since);
        totalOrders = view.findViewById(R.id.total_orders);
        totalSpent = view.findViewById(R.id.total_spent);
        logoutButton = view.findViewById(R.id.logout_button);

        loadUserProfile();

        logoutButton.setOnClickListener(v -> {
            logout();
        });

        return view;
    }

    private void loadUserProfile() {
        String email = sharedPreferences.getString(Constants.KEY_EMAIL, "");
        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

        if (!email.isEmpty()) {
            userEmail.setText(email);

            // Дата регистрации (можно сохранять при регистрации, пока используем текущую дату)
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String currentDate = sdf.format(new Date());
            memberSince.setText("Пользователь с " + currentDate);

            // Загружаем статистику
            if (userId != -1) {
                loadUserStats(userId);
            }
        } else {
            userEmail.setText("Не авторизован");
            memberSince.setText("");
            totalOrders.setText("0");
            totalSpent.setText("0");
        }
    }

    private void loadUserStats(int userId) {
        try {
            // Получаем количество заказов
            Cursor ordersCursor = dbHelper.getUserOrders(userId);
            int ordersCount = 0;
            double totalAmount = 0;

            if (ordersCursor != null) {
                ordersCount = ordersCursor.getCount();

                // Рассчитываем общую сумму
                if (ordersCursor.moveToFirst()) {
                    do {
                        totalAmount += ordersCursor.getDouble(ordersCursor.getColumnIndexOrThrow("total_amount"));
                    } while (ordersCursor.moveToNext());
                }
                ordersCursor.close();
            }

            totalOrders.setText(String.valueOf(ordersCount));
            totalSpent.setText(String.format("%.0f", totalAmount));

        } catch (Exception e) {
            e.printStackTrace();
            totalOrders.setText("0");
            totalSpent.setText("0");
        }
    }

    private void logout() {
        Toast.makeText(getContext(), "Выход из аккаунта", Toast.LENGTH_SHORT).show();

        // Очищаем данные авторизации
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Переход на экран входа
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}