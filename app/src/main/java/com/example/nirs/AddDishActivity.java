package com.example.nirs;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddDishActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText etName, etDescription, etPrice, etIngredients;
    private Spinner spCategory;
    private Button btnAdd, btnCancel, btnTakePhoto, btnPickPhoto;
    private ImageView ivDishImage;
    private DatabaseHelper dbHelper;
    private Bitmap dishImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);

        dbHelper = new DatabaseHelper(this);

        // Инициализация элементов
        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etIngredients = findViewById(R.id.et_ingredients);
        spCategory = findViewById(R.id.sp_category);
        btnAdd = findViewById(R.id.btn_add);
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

        // Кнопка добавить - ИСПРАВЛЕНО
        btnAdd.setOnClickListener(v -> {
            Log.d("AddDishActivity", "Add button clicked");
            addDish();
        });

        // Кнопка отмена
        btnCancel.setOnClickListener(v -> finish());
    }

    // МЕТОД ПРОВЕРКИ РАЗРЕШЕНИЯ КАМЕРЫ
    private boolean checkCameraPermission() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        Log.d("AddDishActivity", "Camera permission granted: " + granted);
        return granted;
    }

    // МЕТОД ЗАПРОСА РАЗРЕШЕНИЯ КАМЕРЫ
    private void requestCameraPermission() {
        Log.d("AddDishActivity", "Requesting camera permission");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    // МЕТОД ПРОВЕРКИ РАЗРЕШЕНИЯ ХРАНИЛИЩА
    private boolean checkStoragePermission() {
        boolean granted;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Для Android 13+ используем READ_MEDIA_IMAGES
            granted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Для старых версий используем READ_EXTERNAL_STORAGE
            granted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        Log.d("AddDishActivity", "Storage permission granted: " + granted);
        return granted;
    }

    // МЕТОД ЗАПРОСА РАЗРЕШЕНИЯ ХРАНИЛИЩА
    private void requestStoragePermission() {
        Log.d("AddDishActivity", "Requesting storage permission");
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
        Log.d("AddDishActivity", "Permission result: requestCode=" + requestCode + ", grantResults=" + (grantResults.length > 0 ? grantResults[0] : "empty"));

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Разрешение на камеру необходимо для добавления фото", Toast.LENGTH_SHORT).show();
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
        Log.d("AddDishActivity", "Opening camera");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "На устройстве нет приложения камеры", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Log.d("AddDishActivity", "Opening gallery");
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(pickPhotoIntent, "Выберите изображение"), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AddDishActivity", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Фото с камеры
                if (data != null && data.getExtras() != null) {
                    dishImageBitmap = (Bitmap) data.getExtras().get("data");
                    if (dishImageBitmap != null) {
                        ivDishImage.setImageBitmap(dishImageBitmap);
                        Toast.makeText(this, "Фото сделано успешно", Toast.LENGTH_SHORT).show();
                        Log.d("AddDishActivity", "Photo taken from camera, size: " + dishImageBitmap.getWidth() + "x" + dishImageBitmap.getHeight());
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Фото из галереи
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        dishImageBitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), selectedImageUri);
                        ivDishImage.setImageBitmap(dishImageBitmap);
                        Toast.makeText(this, "Изображение выбрано", Toast.LENGTH_SHORT).show();
                        Log.d("AddDishActivity", "Photo selected from gallery, size: " + dishImageBitmap.getWidth() + "x" + dishImageBitmap.getHeight());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        Log.e("AddDishActivity", "Error loading image: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void addDish() {
        Log.d("AddDishActivity", "Starting addDish method");

        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        Log.d("AddDishActivity", "Adding dish: " + name);
        Log.d("AddDishActivity", "Category: " + category);
        Log.d("AddDishActivity", "Price string: " + priceStr);
        Log.d("AddDishActivity", "Image bitmap null: " + (dishImageBitmap == null));

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

        // УБРАЛ ТРЕБОВАНИЕ ОБЯЗАТЕЛЬНОГО ИЗОБРАЖЕНИЯ - сделаем его опциональным
        if (dishImageBitmap == null) {
            Toast.makeText(this, "Добавьте изображение блюда (рекомендуется)", Toast.LENGTH_SHORT).show();
            // Можно продолжить без изображения, если нужно
            // return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            if (price <= 0) {
                Toast.makeText(this, "Цена должна быть больше 0", Toast.LENGTH_SHORT).show();
                etPrice.requestFocus();
                return;
            }

            // Конвертируем Bitmap в строку (base64) для сохранения в базе данных
            String imageBase64 = "";
            if (dishImageBitmap != null) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    dishImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    Log.d("AddDishActivity", "Image converted to base64, size: " + imageBase64.length() + " chars");
                } catch (Exception e) {
                    Log.e("AddDishActivity", "Error converting image to base64: " + e.getMessage());
                    Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Добавляем блюдо в базу данных
            Log.d("AddDishActivity", "Calling dbHelper.addDish()");
            boolean success = dbHelper.addDish(name, description, price, category, imageBase64, ingredients);

            if (success) {
                Log.d("AddDishActivity", "Dish added successfully: " + name);
                Toast.makeText(this, "Блюдо \"" + name + "\" добавлено успешно", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Log.e("AddDishActivity", "Failed to add dish to database");
                Toast.makeText(this, "Ошибка при добавлении блюда в базу данных", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная цена. Введите число", Toast.LENGTH_SHORT).show();
            etPrice.requestFocus();
            etPrice.selectAll();
        } catch (Exception e) {
            Log.e("AddDishActivity", "Unexpected error: " + e.getMessage(), e);
            Toast.makeText(this, "Неожиданная ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}