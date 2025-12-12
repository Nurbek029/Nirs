package com.example.nirs;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.nirs.R;
import com.example.nirs.entity.Dish;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditDishActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText etName, etDescription, etPrice, etIngredients;
    private Spinner spCategory;
    private Button btnUpdate, btnDelete, btnCancel, btnTakePhoto, btnPickPhoto;
    private ImageView ivDishImage;
    private DatabaseHelper dbHelper;
    private Bitmap dishImageBitmap;
    private int dishId;
    private String originalImageBase64;
    private Dish originalDish;
    private boolean imageChanged = false; // Флаг для отслеживания изменений изображения

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dish);

        // Получаем ID блюда из Intent
        dishId = getIntent().getIntExtra("dish_id", -1);
        Log.d("EditDishActivity", "Received dish_id: " + dishId);

        if (dishId == -1) {
            Toast.makeText(this, "Ошибка: блюдо не выбрано", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        // Инициализация элементов
        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etIngredients = findViewById(R.id.et_ingredients);
        spCategory = findViewById(R.id.sp_category);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnCancel = findViewById(R.id.btn_cancel);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnPickPhoto = findViewById(R.id.btn_pick_photo);
        ivDishImage = findViewById(R.id.iv_dish_image);

        // Настройка Spinner
        String[] categories = {"Горячее", "Салаты", "Напитки", "Десерты", "Закуски"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // Загрузка данных блюда
        loadDishData();

        // Кнопка сделать фото
        btnTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        // Кнопка выбрать из галереи
        btnPickPhoto.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        // Кнопка обновить
        btnUpdate.setOnClickListener(v -> updateDish());

        // Кнопка удалить
        btnDelete.setOnClickListener(v -> deleteDish());

        // Кнопка отмена
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadDishData() {
        Log.d("EditDishActivity", "Loading dish data for ID: " + dishId);
        originalDish = dbHelper.getDishById(dishId);

        if (originalDish != null) {
            etName.setText(originalDish.getName());
            etDescription.setText(originalDish.getDescription());
            etPrice.setText(String.valueOf(originalDish.getPrice()));
            etIngredients.setText(originalDish.getIngredients());

            // Устанавливаем категорию в Spinner
            String category = originalDish.getCategory();
            Log.d("EditDishActivity", "Dish category: " + category);

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spCategory.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(category);
                if (position >= 0) {
                    spCategory.setSelection(position);
                    Log.d("EditDishActivity", "Category set to position: " + position);
                } else {
                    Log.e("EditDishActivity", "Category not found in spinner: " + category);
                }
            }

            // Загружаем изображение
            String imageBase64 = originalDish.getImage();
            originalImageBase64 = imageBase64; // Сохраняем оригинальное изображение
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    dishImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivDishImage.setImageBitmap(dishImageBitmap);
                    Log.d("EditDishActivity", "Image loaded successfully");
                } catch (Exception e) {
                    Log.e("EditDishActivity", "Error loading image: " + e.getMessage());
                    e.printStackTrace();
                    ivDishImage.setImageResource(R.drawable.placeholder_food);
                }
            } else {
                Log.d("EditDishActivity", "No image for this dish");
                ivDishImage.setImageResource(R.drawable.placeholder_food);
                dishImageBitmap = null;
                originalImageBase64 = null;
            }

            Log.d("EditDishActivity", "Dish data loaded: " + originalDish.getName());
        } else {
            Toast.makeText(this, "Ошибка загрузки данных блюда", Toast.LENGTH_SHORT).show();
            Log.e("EditDishActivity", "Failed to load dish with ID: " + dishId);
            finish();
        }
    }

    // Методы проверки разрешений
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Разрешение на камеру необходимо", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Разрешение на доступ к галерее необходимо", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "На устройстве нет приложения камеры", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(pickPhotoIntent, "Выберите изображение"), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Фото с камеры
                Bundle extras = data.getExtras();
                if (extras != null) {
                    dishImageBitmap = (Bitmap) extras.get("data");
                    ivDishImage.setImageBitmap(dishImageBitmap);
                    imageChanged = true; // Отмечаем, что изображение изменилось
                    Toast.makeText(this, "Фото сделано успешно", Toast.LENGTH_SHORT).show();
                    Log.d("EditDishActivity", "Photo taken from camera");
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Фото из галереи
                Uri selectedImageUri = data.getData();
                try {
                    dishImageBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), selectedImageUri);
                    ivDishImage.setImageBitmap(dishImageBitmap);
                    imageChanged = true; // Отмечаем, что изображение изменилось
                    Toast.makeText(this, "Изображение выбрано", Toast.LENGTH_SHORT).show();
                    Log.d("EditDishActivity", "Photo selected from gallery");
                } catch (IOException e) {
                    Log.e("EditDishActivity", "Error loading image: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateDish() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        Log.d("EditDishActivity", "Updating dish - Name: " + name + ", Category: " + category);

        // Валидация
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название блюда", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Введите цену", Toast.LENGTH_SHORT).show();
            etPrice.requestFocus();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            if (price <= 0) {
                Toast.makeText(this, "Цена должна быть больше 0", Toast.LENGTH_SHORT).show();
                etPrice.requestFocus();
                return;
            }

            // Используем оригинальное изображение если новое не выбрано
            String imageBase64 = originalImageBase64;

            // Если изображение было изменено
            if (imageChanged && dishImageBitmap != null) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    dishImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    Log.d("EditDishActivity", "New image converted to base64, size: " + imageBase64.length() + " chars");
                } catch (Exception e) {
                    Log.e("EditDishActivity", "Error converting image: " + e.getMessage());
                    Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Log.d("EditDishActivity", "Using original image");
            }

            // Обновляем блюдо в базе данных
            boolean success = updateDishInDatabase(dishId, name, description, price, category, imageBase64, ingredients);

            if (success) {
                Log.d("EditDishActivity", "Dish updated successfully: " + name);
                Toast.makeText(this, "Блюдо \"" + name + "\" обновлено", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Log.e("EditDishActivity", "Failed to update dish in database");
                Toast.makeText(this, "Ошибка при обновлении блюда", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная цена. Введите число", Toast.LENGTH_SHORT).show();
            etPrice.requestFocus();
            etPrice.selectAll();
        } catch (Exception e) {
            Log.e("EditDishActivity", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Неожиданная ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updateDishInDatabase(int dishId, String name, String description, double price,
                                         String category, String imageBase64, String ingredients) {
        // Этот метод должен быть в DatabaseHelper
        return dbHelper.updateDish(dishId, name, description, price, category, imageBase64, ingredients);
    }

    private void deleteDish() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удаление блюда")
                .setMessage("Вы уверены, что хотите удалить блюдо \"" + originalDish.getName() + "\"?")
                .setPositiveButton("Да", (dialog, which) -> {
                    boolean success = dbHelper.deleteDish(dishId);
                    if (success) {
                        Toast.makeText(this, "Блюдо \"" + originalDish.getName() + "\" удалено", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении блюда", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}