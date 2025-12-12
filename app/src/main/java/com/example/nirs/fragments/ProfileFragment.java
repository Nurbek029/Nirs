package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nirs.Constants;
import com.example.nirs.DatabaseHelper;
import com.example.nirs.LoginActivity;
import com.example.nirs.MainActivity;
import com.example.nirs.R;
import com.example.nirs.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private Button logoutButton;
    private TextView userEmail, memberSince, userType;
    private LinearLayout userStatsLayout;
    private TextView totalOrdersUser, totalSpentUser;
    private TextView processingOrdersAdmin, acceptedOrdersAdmin, readyOrdersAdmin;
    private TextView completedOrdersAdmin, revenueAdmin;
    private DatabaseHelper dbHelper;
    private boolean isAdmin = false;
    private int userId = -1;
    private String email = "";
    private SessionManager sessionManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ProfileFragment", "onCreate started");

        // Инициализация SessionManager
        if (getContext() != null) {
            sessionManager = new SessionManager(getContext());

            // Проверяем, вошел ли пользователь
            if (!sessionManager.isLoggedIn()) {
                Log.e("ProfileFragment", "User not logged in, redirecting to login");
                redirectToLogin();
                return;
            }

            // Получаем данные из SessionManager
            userId = sessionManager.getUserId();
            email = sessionManager.getEmail();
            isAdmin = sessionManager.isAdmin();

            Log.d("ProfileFragment", "=== ProfileFragment onCreate ===");
            Log.d("ProfileFragment", "UserID: " + userId);
            Log.d("ProfileFragment", "Email: '" + email + "'");
            Log.d("ProfileFragment", "IsAdmin: " + isAdmin);
            sessionManager.debugPrintAllData();
        }

        dbHelper = new DatabaseHelper(getContext());
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ProfileFragment", "onCreateView started, isAdmin: " + isAdmin);

        // Проверяем, инициализирован ли sessionManager
        if (sessionManager == null && getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }

        // ВАЖНО: Всегда используем fragment_profile.xml для обычных пользователей
        View view;
        if (isAdmin) {
            // Для админа используем специальный layout
            view = inflater.inflate(R.layout.fragment_profile_admin, container, false);
            Log.d("ProfileFragment", "Loading ADMIN layout (fragment_profile_admin)");

            // Инициализация только для админа
            userEmail = view.findViewById(R.id.user_email);
            memberSince = view.findViewById(R.id.member_since);
            userType = view.findViewById(R.id.user_type);
            logoutButton = view.findViewById(R.id.logout_button);

            // Админ статистика
            processingOrdersAdmin = view.findViewById(R.id.processing_orders);
            acceptedOrdersAdmin = view.findViewById(R.id.accepted_orders);
            readyOrdersAdmin = view.findViewById(R.id.ready_orders);
            completedOrdersAdmin = view.findViewById(R.id.completed_orders);
            revenueAdmin = view.findViewById(R.id.total_revenue);

        } else {
            // Для обычных пользователей используем обычный layout
            view = inflater.inflate(R.layout.fragment_profile, container, false);
            Log.d("ProfileFragment", "Loading USER layout (fragment_profile)");

            // Инициализация для обычных пользователей
            userEmail = view.findViewById(R.id.user_email);
            memberSince = view.findViewById(R.id.member_since);
            userType = view.findViewById(R.id.user_type);
            logoutButton = view.findViewById(R.id.logout_button);
            userStatsLayout = view.findViewById(R.id.user_stats_layout);
            totalOrdersUser = view.findViewById(R.id.total_orders);
            totalSpentUser = view.findViewById(R.id.total_spent);
        }

        // Загружаем данные профиля
        loadUserProfile();

        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserProfile() {
        Log.d("ProfileFragment", "loadUserProfile started");

        // Обновляем данные из SessionManager
        if (sessionManager != null) {
            userId = sessionManager.getUserId();
            email = sessionManager.getEmail();
            isAdmin = sessionManager.isAdmin();

            Log.d("ProfileFragment", "Loaded from SessionManager:");
            Log.d("ProfileFragment", "userId: " + userId);
            Log.d("ProfileFragment", "email: '" + email + "'");
            Log.d("ProfileFragment", "isAdmin: " + isAdmin);
        } else if (getContext() != null) {
            // Резервный вариант: загружаем из SharedPreferences
            sessionManager = new SessionManager(getContext());
            userId = sessionManager.getUserId();
            email = sessionManager.getEmail();
            isAdmin = sessionManager.isAdmin();
        }

        // Проверяем, есть ли данные пользователя
        boolean hasValidData = userId != -1 && !email.isEmpty();

        Log.d("ProfileFragment", "hasValidData check: userId != -1 = " + (userId != -1) +
                ", email not empty = " + (!email.isEmpty()) +
                ", result = " + hasValidData);

        if (hasValidData) {
            Log.d("ProfileFragment", "User is properly logged in, loading profile...");

            if (userEmail != null) {
                userEmail.setText(email);
            }

            // Устанавливаем тип пользователя
            if (isAdmin) {
                if (userType != null) {
                    userType.setText("Администратор");
                    userType.setTextColor(getResources().getColor(R.color.color_primary));
                }

                // Загружаем админ-статистику
                loadAdminStats();
            } else {
                if (userType != null) {
                    userType.setText("Покупатель");
                    userType.setTextColor(0xFF757575);
                }

                // Загружаем пользовательскую статистику
                if (userStatsLayout != null) {
                    userStatsLayout.setVisibility(View.VISIBLE);
                    loadUserStats(userId);
                }
            }

            // Дата регистрации
            if (memberSince != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String currentDate = sdf.format(new Date());
                if (isAdmin) {
                    memberSince.setText("Администратор с " + currentDate);
                } else {
                    memberSince.setText("Пользователь с " + currentDate);
                }
            }

            Log.d("ProfileFragment", "Profile loaded successfully for: " + email);
        } else {
            // Если данных нет, показываем сообщение
            StringBuilder debugText = new StringBuilder();
            debugText.append("Данные профиля отсутствуют\n");
            debugText.append("Email: '").append(email).append("'\n");
            debugText.append("UserID: ").append(userId).append("\n");
            debugText.append("IsAdmin: ").append(isAdmin);

            if (userEmail != null) {
                userEmail.setText(debugText.toString());
            }
            if (memberSince != null) {
                memberSince.setText("");
            }
            if (userType != null) {
                userType.setText("");
            }

            // Скрываем статистику для незалогиненных
            if (userStatsLayout != null) {
                userStatsLayout.setVisibility(View.GONE);
            }
            if (totalOrdersUser != null) totalOrdersUser.setText("0");
            if (totalSpentUser != null) totalSpentUser.setText("0 сом");

            Log.e("ProfileFragment", "User data incomplete!");
            Log.e("ProfileFragment", debugText.toString());

            // Перенаправляем на логин
            redirectToLogin();
        }
    }

    private void loadUserStats(int userId) {
        try {
            Log.d("ProfileFragment", "Loading user stats for ID: " + userId);

            if (dbHelper == null) {
                dbHelper = new DatabaseHelper(getContext());
            }

            int ordersCount = dbHelper.getOrderCount(userId);
            double totalAmount = dbHelper.getTotalSpent(userId);

            Log.d("ProfileFragment", "User stats - Orders: " + ordersCount + ", Total: " + totalAmount);

            if (totalOrdersUser != null) {
                totalOrdersUser.setText(String.valueOf(ordersCount));
            }
            if (totalSpentUser != null) {
                totalSpentUser.setText(String.format("%.0f сом", totalAmount));
            }

        } catch (Exception e) {
            Log.e("ProfileFragment", "Error loading user stats: " + e.getMessage());
            e.printStackTrace();
            if (totalOrdersUser != null) totalOrdersUser.setText("0");
            if (totalSpentUser != null) totalSpentUser.setText("0 сом");
        }
    }

    private void loadAdminStats() {
        try {
            Log.d("ProfileFragment", "Loading admin stats");

            if (dbHelper == null) {
                dbHelper = new DatabaseHelper(getContext());
            }

            // Получаем количество заказов по статусам
            int processingCount = dbHelper.getOrderCountByStatus("В обработке");
            int acceptedCount = dbHelper.getOrderCountByStatus("Принятый");
            int readyCount = dbHelper.getOrderCountByStatus("Готов");
            int completedCount = dbHelper.getOrderCountByStatus("Завершен");

            Log.d("ProfileFragment", "Admin stats - Processing: " + processingCount);
            Log.d("ProfileFragment", "Admin stats - Accepted: " + acceptedCount);
            Log.d("ProfileFragment", "Admin stats - Ready: " + readyCount);
            Log.d("ProfileFragment", "Admin stats - Completed: " + completedCount);

            // Вычисляем выручку от завершенных заказов
            double totalRevenue = dbHelper.getRevenueFromCompletedOrders();

            Log.d("ProfileFragment", "Admin stats - Revenue: " + totalRevenue);

            // Обновляем UI
            if (processingOrdersAdmin != null) processingOrdersAdmin.setText(String.valueOf(processingCount));
            if (acceptedOrdersAdmin != null) acceptedOrdersAdmin.setText(String.valueOf(acceptedCount));
            if (readyOrdersAdmin != null) readyOrdersAdmin.setText(String.valueOf(readyCount));
            if (completedOrdersAdmin != null) completedOrdersAdmin.setText(String.valueOf(completedCount));
            if (revenueAdmin != null) revenueAdmin.setText(String.format("%.0f сом", totalRevenue));

        } catch (Exception e) {
            Log.e("ProfileFragment", "Error loading admin stats: " + e.getMessage());
            e.printStackTrace();
            if (processingOrdersAdmin != null) processingOrdersAdmin.setText("0");
            if (acceptedOrdersAdmin != null) acceptedOrdersAdmin.setText("0");
            if (readyOrdersAdmin != null) readyOrdersAdmin.setText("0");
            if (completedOrdersAdmin != null) completedOrdersAdmin.setText("0");
            if (revenueAdmin != null) revenueAdmin.setText("0 сом");
        }
    }

    private void logout() {
        Toast.makeText(getContext(), "Выход из аккаунта", Toast.LENGTH_SHORT).show();

        // Выход через SessionManager
        if (sessionManager != null) {
            sessionManager.logout();
        }

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void redirectToLogin() {
        if (getActivity() != null) {
            Toast.makeText(getContext(), "Необходимо войти в аккаунт", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ProfileFragment", "onResume - Refreshing profile");
        loadUserProfile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}