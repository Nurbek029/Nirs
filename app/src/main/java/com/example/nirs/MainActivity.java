package com.example.nirs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nirs.fragments.CartFragment;
import com.example.nirs.fragments.MenuFragment;
import com.example.nirs.fragments.OrdersFragment;
import com.example.nirs.fragments.ProfileFragment;
import com.example.nirs.fragments.PromotionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private long backPressedTime;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // Проверяем, авторизован ли пользователь
        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        Toast.makeText(this, "Добро пожаловать в столовую КГТУ!", Toast.LENGTH_SHORT).show();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Загрузка стартового фрагмента
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new MenuFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.menu_menu);
        }

        // Обработка двойного нажатия для выхода
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    // Выход из аккаунта
                    logout();
                } else {
                    Toast.makeText(MainActivity.this, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.menu_menu) {
                    selectedFragment = new MenuFragment();
                } else if (itemId == R.id.menu_promotions) {
                    selectedFragment = new PromotionsFragment();
                } else if (itemId == R.id.menu_cart) {
                    selectedFragment = new CartFragment();
                } else if (itemId == R.id.menu_orders) {
                    selectedFragment = new OrdersFragment();
                } else if (itemId == R.id.menu_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            };

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false) &&
                sharedPreferences.contains(Constants.KEY_USER_ID);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        // Очищаем данные авторизации
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.KEY_EMAIL);
        editor.remove(Constants.KEY_PASSWORD);
        editor.remove(Constants.KEY_REMEMBER_ME);
        editor.remove(Constants.KEY_USER_ID);
        editor.remove(Constants.KEY_IS_LOGGED_IN);
        editor.apply();

        // Переход на экран входа
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}