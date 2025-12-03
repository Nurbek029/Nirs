package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.example.nirs.adapters.MenuAdapter;
import com.example.nirs.entity.Dish;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    private RecyclerView menuRecyclerView;
    private LinearLayout categoriesContainer;
    private DatabaseHelper dbHelper;
    private List<Dish> dishList;
    private MenuAdapter menuAdapter;
    private SharedPreferences sharedPreferences;

    public MenuFragment() {
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
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        dbHelper = new DatabaseHelper(getContext());
        dishList = new ArrayList<>();

        // Инициализация UI
        categoriesContainer = view.findViewById(R.id.categories_container);
        menuRecyclerView = view.findViewById(R.id.menu_recycler_view);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Настройка адаптера
        menuAdapter = new MenuAdapter(dishList, new MenuAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(int position) {
                addToCart(dishList.get(position));
            }

            @Override
            public void onItemClick(int position) {
                showDishDetailsDialog(dishList.get(position));
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);

        // Загрузка категорий и блюд
        setupCategories();
        loadAllDishes();

        return view;
    }

    private void setupCategories() {
        String[] categories = {"Все", "Горячее", "Салаты", "Напитки"};

        categoriesContainer.removeAllViews();

        for (String category : categories) {
            Button button = new Button(getContext());
            button.setText(category);
            button.setAllCaps(false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                // Сбрасываем стиль всех кнопок
                for (int i = 0; i < categoriesContainer.getChildCount(); i++) {
                    View child = categoriesContainer.getChildAt(i);
                    if (child instanceof Button) {
                        child.setBackgroundResource(R.drawable.button_category);
                        ((Button) child).setTextColor(getResources().getColor(R.color.color_primary));
                    }
                }

                // Устанавливаем активный стиль для выбранной кнопки
                button.setBackgroundResource(R.drawable.button_category_active);
                button.setTextColor(getResources().getColor(android.R.color.white));

                if (category.equals("Все")) {
                    loadAllDishes();
                } else {
                    loadDishesByCategory(category);
                }
            });

            categoriesContainer.addView(button);
        }

        // Устанавливаем первую кнопку как активную
        if (categoriesContainer.getChildCount() > 0) {
            Button firstButton = (Button) categoriesContainer.getChildAt(0);
            firstButton.setBackgroundResource(R.drawable.button_category_active);
            firstButton.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void loadAllDishes() {
        dishList.clear();
        Cursor cursor = dbHelper.getAllDishes();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    Dish dish = new Dish(
                            cursor.getInt(cursor.getColumnIndexOrThrow("dish_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image"))
                    );
                    dishList.add(dish);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        menuAdapter.notifyDataSetChanged();
    }

    private void loadDishesByCategory(String category) {
        dishList.clear();
        Cursor cursor = dbHelper.getDishesByCategory(category);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    Dish dish = new Dish(
                            cursor.getInt(cursor.getColumnIndexOrThrow("dish_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image"))
                    );
                    dishList.add(dish);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        menuAdapter.notifyDataSetChanged();
    }

    private void addToCart(Dish dish) {
        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

        if (userId != -1) {
            boolean success = dbHelper.addToCart(userId, dish.getId(), 1);
            if (success) {
                Toast.makeText(getContext(), dish.getName() + " добавлен в корзину", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Ошибка добавления в корзину", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDishDetailsDialog(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dish_details, null);

        // Находим элементы
        android.widget.TextView dishName = dialogView.findViewById(R.id.dish_name);
        android.widget.TextView dishPrice = dialogView.findViewById(R.id.dish_price);
        android.widget.TextView cookDate = dialogView.findViewById(R.id.cook_date);
        android.widget.TextView ingredients = dialogView.findViewById(R.id.ingredients);
        android.widget.TextView dishDescription = dialogView.findViewById(R.id.dish_description);
        android.widget.Button addToCartBtn = dialogView.findViewById(R.id.add_to_cart_btn);

        // Заполняем базовые данные
        dishName.setText(dish.getName());
        dishPrice.setText(dish.getFormattedPrice());

        // Пробуем получить детали
        Dish fullDish = dbHelper.getDishDetails(dish.getId());
        if (fullDish != null) {
            if (fullDish.getDescription() != null && !fullDish.getDescription().isEmpty()) {
                dishDescription.setText(fullDish.getDescription());
                dishDescription.setVisibility(View.VISIBLE);
            } else {
                dishDescription.setVisibility(View.GONE);
            }

            if (fullDish.getCookDate() != null && !fullDish.getCookDate().isEmpty()) {
                cookDate.setText("Дата приготовления: " + fullDish.getCookDate());
                cookDate.setVisibility(View.VISIBLE);
            } else {
                cookDate.setVisibility(View.GONE);
            }

            if (fullDish.getIngredients() != null && !fullDish.getIngredients().isEmpty()) {
                ingredients.setText(fullDish.getIngredients());
                ingredients.setVisibility(View.VISIBLE);
            } else {
                ingredients.setVisibility(View.GONE);
            }
        } else {
            // Если не получилось загрузить детали, показываем только базовое описание
            dishDescription.setText(dish.getDescription());
            cookDate.setVisibility(View.GONE);
            ingredients.setVisibility(View.GONE);
        }

        // Кнопка добавления в корзину
        addToCartBtn.setOnClickListener(v -> {
            addToCart(dish);
            AlertDialog dialog = (AlertDialog) v.getTag();
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        addToCartBtn.setTag(dialog);
        dialog.show();
    }
}