package com.example.nirs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.nirs.fragments.AdminOrdersFragment;
import com.example.nirs.fragments.CartFragment;
import com.example.nirs.fragments.MenuFragment;
import com.example.nirs.fragments.OrdersFragment;
import com.example.nirs.fragments.ProfileFragment;
import com.example.nirs.fragments.PromotionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddDish;
    private long backPressedTime;
    private SessionManager sessionManager;
    private boolean isAdmin = false;
    private int userId = -1;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация SessionManager
        sessionManager = new SessionManager(this);

        // Проверяем, вошел ли пользователь
        if (!sessionManager.isLoggedIn()) {
            Log.e("MainActivity", "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }

        // Получаем данные пользователя
        userId = sessionManager.getUserId();
        email = sessionManager.getEmail();
        isAdmin = sessionManager.isAdmin();

        Log.d("MainActivity", "=== MainActivity Started ===");
        Log.d("MainActivity", "UserID: " + userId);
        Log.d("MainActivity", "Email: '" + email + "'");
        Log.d("MainActivity", "IsAdmin: " + isAdmin);
        sessionManager.debugPrintAllData();

        // Убедимся, что администратор существует в БД
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.ensureAdminExists();

        // Загружаем соответствующий layout
        if (isAdmin) {
            setContentView(R.layout.activity_main_admin);
            Log.d("MainActivity", "Loaded admin layout");
        } else {
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "Loaded user layout");
        }

        Toast.makeText(this, "Добро пожаловать в столовую КГТУ!", Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Инициализация FAB для добавления блюда (только для админа)
        fabAddDish = findViewById(R.id.fab_add_dish);
        updateFABVisibility();

        if (fabAddDish != null) {
            fabAddDish.setOnClickListener(v -> {
                if (isAdmin) {
                    showAddDishDialog();
                }
            });
        }

        // Загрузка стартового фрагмента
        if (savedInstanceState == null) {
            if (isAdmin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new ProfileFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.menu_profile);
                Log.d("MainActivity", "Started with ProfileFragment for admin");
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new MenuFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.menu_menu);
                Log.d("MainActivity", "Started with MenuFragment for user");
            }
        }

        // Обработка двойного нажатия для выхода
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    logout();
                } else {
                    Toast.makeText(MainActivity.this, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем статус админа
        isAdmin = sessionManager.isAdmin();
        updateFABVisibility();
    }

    private void updateFABVisibility() {
        if (fabAddDish != null) {
            fabAddDish.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);

        // Показываем меню админа только если пользователь админ
        MenuItem adminOrdersItem = menu.findItem(R.id.action_admin_orders);
        if (adminOrdersItem != null) {
            adminOrdersItem.setVisible(isAdmin);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_admin_orders) {
            // Переход к админ-заказам
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new AdminOrdersFragment())
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                    // Если админ, показываем AdminOrdersFragment, иначе обычные заказы
                    selectedFragment = isAdmin ? new AdminOrdersFragment() : new OrdersFragment();
                } else if (itemId == R.id.menu_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, selectedFragment)
                            .commit();

                    // Скрываем FAB на всех фрагментах кроме меню
                    if (fabAddDish != null) {
                        fabAddDish.setVisibility(
                                (isAdmin && itemId == R.id.menu_menu) ? View.VISIBLE : View.GONE
                        );
                    }

                    return true;
                }
                return false;
            };

    private void showAddDishDialog() {
        // Открываем Activity для добавления блюда с камерой/галереей
        Intent intent = new Intent(this, AddDishActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Блюдо добавлено успешно", Toast.LENGTH_SHORT).show();

            // Обновляем MenuFragment если он открыт
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (currentFragment instanceof MenuFragment) {
                ((MenuFragment) currentFragment).loadAllDishes();
            }
        }
    }

    private void redirectToLogin() {
        Log.d("MainActivity", "Redirecting to LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        Log.d("MainActivity", "Logging out user");

        // Выход через SessionManager
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}