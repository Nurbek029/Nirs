package com.example.nirs.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import com.example.nirs.EditDishActivity;
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
    private boolean isAdmin = false;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
            isAdmin = sharedPreferences.getBoolean(Constants.KEY_IS_ADMIN, false);
            Log.d("MenuFragment", "isAdmin: " + isAdmin);
        }
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

        // Настройка адаптера с передачей isAdmin
        menuAdapter = new MenuAdapter(dishList, new MenuAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(int position) {
                addToCart(dishList.get(position));
            }

            @Override
            public void onItemClick(int position) {
                Dish dish = dishList.get(position);
                if (isAdmin) {
                    showAdminDishDetailsDialog(dish, position); // ДЛЯ АДМИНА
                } else {
                    showDishDetailsDialog(dish); // ДЛЯ ПОЛЬЗОВАТЕЛЯ
                }
            }

            @Override
            public void onEditClick(int position) {
                // Теперь не используется - редактирование через диалог
            }

            @Override
            public void onDeleteClick(int position) {
                // Теперь не используется - удаление через диалог
            }
        }, isAdmin);

        menuRecyclerView.setAdapter(menuAdapter);

        // Загрузка категорий и блюд
        setupCategories();
        loadAllDishes();

        return view;
    }

    // Публичный метод для обновления списка блюд
    public void loadAllDishes() {
        dishList.clear();
        Cursor cursor = dbHelper.getAllDishes();

        if (cursor != null) {
            Log.d("MenuFragment", "Total dishes in DB: " + cursor.getCount());

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
                    Log.e("MenuFragment", "Error parsing dish: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        menuAdapter.notifyDataSetChanged();
        Log.d("MenuFragment", "Loaded " + dishList.size() + " dishes");
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
                    Log.e("MenuFragment", "Error parsing dish: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        menuAdapter.notifyDataSetChanged();
        Log.d("MenuFragment", "Loaded " + dishList.size() + " dishes in category: " + category);
    }

    private void addToCart(Dish dish) {
        int userId = sharedPreferences.getInt(Constants.KEY_USER_ID, -1);

        Log.d("MenuFragment", "Adding to cart - User ID: " + userId + ", Dish: " + dish.getName() + " (ID: " + dish.getId() + ")");

        if (userId != -1) {
            boolean success = dbHelper.addToCart(userId, dish.getId(), 1);
            if (success) {
                Toast.makeText(getContext(), dish.getName() + " добавлен в корзину", Toast.LENGTH_SHORT).show();
                Log.d("MenuFragment", "Successfully added to cart");
            } else {
                Toast.makeText(getContext(), "Ошибка добавления в корзину", Toast.LENGTH_SHORT).show();
                Log.e("MenuFragment", "Failed to add to cart");
            }
        } else {
            Toast.makeText(getContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            Log.e("MenuFragment", "User not logged in (userId = -1)");
        }
    }

    // ДИАЛОГ ДЛЯ ПОЛЬЗОВАТЕЛЯ (старый)
    private void showDishDetailsDialog(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dish_details, null);

        // Находим элементы - ДОБАВЬТЕ ImageView В ЛАЙАУТ!!!
        ImageView dishImage = dialogView.findViewById(R.id.dish_image); // ДОБАВЬТЕ ЭТОТ ЭЛЕМЕНТ В XML!
        TextView dishName = dialogView.findViewById(R.id.dish_name);
        TextView dishPrice = dialogView.findViewById(R.id.dish_price);
        TextView cookDate = dialogView.findViewById(R.id.cook_date);
        TextView ingredients = dialogView.findViewById(R.id.ingredients);
        TextView dishDescription = dialogView.findViewById(R.id.dish_description);
        Button addToCartBtn = dialogView.findViewById(R.id.add_to_cart_btn);

        // Загружаем изображение - ВАЖНО!!!
        try {
            String imageBase64 = dish.getImage();
            if (imageBase64 != null && !imageBase64.isEmpty() && !imageBase64.equals("null")) {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    dishImage.setImageBitmap(decodedBitmap);
                    dishImage.setVisibility(View.VISIBLE);
                } else {
                    dishImage.setImageResource(R.drawable.placeholder_food);
                    dishImage.setVisibility(View.VISIBLE);
                }
            } else {
                dishImage.setImageResource(R.drawable.placeholder_food);
                dishImage.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("MenuFragment", "Error loading image in user dialog: " + e.getMessage());
            dishImage.setImageResource(R.drawable.placeholder_food);
            dishImage.setVisibility(View.VISIBLE);
        }

        // Заполняем базовые данные
        dishName.setText(dish.getName());
        dishPrice.setText(dish.getFormattedPrice());

        // Остальной код без изменений...
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
                ingredients.setText("Ингредиенты: " + fullDish.getIngredients());
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

    // НОВЫЙ ДИАЛОГ ДЛЯ АДМИНА (с кнопками редактирования/удаления)
    private void showAdminDishDetailsDialog(Dish dish, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dish_details_admin, null);

        // Находим элементы
        ImageView dishImage = dialogView.findViewById(R.id.dish_image);
        TextView dishName = dialogView.findViewById(R.id.dish_name);
        TextView dishPrice = dialogView.findViewById(R.id.dish_price);
        TextView dishDescription = dialogView.findViewById(R.id.dish_description);
        TextView dishCategory = dialogView.findViewById(R.id.dish_category);
        TextView cookDate = dialogView.findViewById(R.id.cook_date);
        TextView ingredients = dialogView.findViewById(R.id.ingredients);
        Button btnEdit = dialogView.findViewById(R.id.btn_edit);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        // Загружаем изображение
        try {
            String imageBase64 = dish.getImage();
            if (imageBase64 != null && !imageBase64.isEmpty() && !imageBase64.equals("null")) {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    dishImage.setImageBitmap(decodedBitmap);
                }
            }
        } catch (Exception e) {
            Log.e("MenuFragment", "Error loading image: " + e.getMessage());
        }

        // Заполняем данные
        dishName.setText(dish.getName());
        dishPrice.setText(dish.getFormattedPrice());
        dishDescription.setText(dish.getDescription());

        // Получаем полные детали блюда
        Dish fullDish = dbHelper.getDishDetails(dish.getId());
        if (fullDish != null) {
            dishCategory.setText(fullDish.getCategory());

            if (fullDish.getCookDate() != null && !fullDish.getCookDate().isEmpty()) {
                cookDate.setText(fullDish.getCookDate());
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
            dishCategory.setText(dish.getCategory());
            cookDate.setVisibility(View.GONE);
            ingredients.setVisibility(View.GONE);
        }

        // Кнопка Редактировать
        btnEdit.setOnClickListener(v -> {
            // Закрываем диалог
            if (v.getTag() instanceof AlertDialog) {
                ((AlertDialog) v.getTag()).dismiss();
            }

            // Запускаем редактирование
            editDish(dish);
        });

        // Кнопка Удалить
        btnDelete.setOnClickListener(v -> {
            if (v.getTag() instanceof AlertDialog) {
                ((AlertDialog) v.getTag()).dismiss();
            }
            deleteDish(dish, position);
        });

        // Кнопка Закрыть
        closeButton.setOnClickListener(v -> {
            if (v.getTag() instanceof AlertDialog) {
                ((AlertDialog) v.getTag()).dismiss();
            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Сохраняем ссылку на диалог в тег каждой кнопки
        btnEdit.setTag(dialog);
        btnDelete.setTag(dialog);
        closeButton.setTag(dialog);

        dialog.show();
    }

    // Метод редактирования блюда - ИСПРАВЛЕНО
    private void editDish(Dish dish) {
        Log.d("MenuFragment", "Editing dish: " + dish.getName() + " (ID: " + dish.getId() + ")");

        try {
            // Используем requireActivity() для получения контекста
            Intent intent = new Intent(requireActivity(), EditDishActivity.class);
            intent.putExtra("dish_id", dish.getId());
            startActivity(intent);

            Log.d("MenuFragment", "EditDishActivity started successfully");
        } catch (Exception e) {
            Log.e("MenuFragment", "Error starting EditDishActivity: " + e.getMessage());
            e.printStackTrace();

            // Более подробное сообщение об ошибке
            String errorMsg = "Не удалось открыть редактор: ";
            if (e.getClass().getSimpleName().equals("ActivityNotFoundException")) {
                errorMsg += "Activity не найден. Проверьте AndroidManifest.xml";
            } else {
                errorMsg += e.getMessage();
            }

            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    // Метод удаления блюда
    private void deleteDish(Dish dish, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление блюда")
                .setMessage("Вы уверены, что хотите удалить блюдо \"" + dish.getName() + "\"?")
                .setPositiveButton("Да", (dialog, which) -> {
                    boolean success = dbHelper.deleteDish(dish.getId());
                    if (success) {
                        Toast.makeText(getContext(), "Блюдо \"" + dish.getName() + "\" удалено", Toast.LENGTH_SHORT).show();
                        dishList.remove(position);
                        menuAdapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(getContext(), "Ошибка при удалении блюда", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список при возвращении на фрагмент
        loadAllDishes();
    }
}