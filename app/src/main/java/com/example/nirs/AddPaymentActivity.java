package com.example.nirs;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.nirs.R;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddPaymentActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_CAMERA_CODE = 102;
    private static final int REQUEST_GALLERY_CODE = 103;

    private TextView tvOrderInfo;
    private Button btnTakePhoto, btnPickPhoto, btnSave, btnCancel;
    private ImageView ivPaymentImage;
    private DatabaseHelper dbHelper;
    private Bitmap paymentImageBitmap;
    private int orderId; // ИЗМЕНЕНО С long НА int
    private String orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        // ИСПРАВЛЕНИЕ: Получаем order_id как int, а не long
        orderId = getIntent().getIntExtra("order_id", -1);
        orderNumber = getIntent().getStringExtra("order_number");

        Log.d("AddPayment", "Received orderId: " + orderId);
        Log.d("AddPayment", "Received orderNumber: " + orderNumber);

        if (orderId == -1) {
            Toast.makeText(this, "Ошибка: неверный заказ (id: " + orderId + ")", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        // Инициализация элементов
        tvOrderInfo = findViewById(R.id.tv_order_info);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnPickPhoto = findViewById(R.id.btn_pick_photo);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        ivPaymentImage = findViewById(R.id.iv_payment_image);

        // Устанавливаем информацию о заказе
        tvOrderInfo.setText("Заказ #" + orderNumber + "\nДобавление чека оплаты");

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

        // Кнопка сохранить
        btnSave.setOnClickListener(v -> savePayment());

        // Кнопка отмена
        btnCancel.setOnClickListener(v -> finish());
    }

    // МЕТОД ПРОВЕРКИ РАЗРЕШЕНИЯ КАМЕРЫ
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // МЕТОД ЗАПРОСА РАЗРЕШЕНИЯ КАМЕРЫ
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    // МЕТОД ПРОВЕРКИ РАЗРЕШЕНИЯ ХРАНИЛИЩА
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Для Android 13+ используем READ_MEDIA_IMAGES
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Для старых версий используем READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    // МЕТОД ЗАПРОСА РАЗРЕШЕНИЯ ХРАНИЛИЩА
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
                    paymentImageBitmap = (Bitmap) extras.get("data");
                    ivPaymentImage.setImageBitmap(paymentImageBitmap);
                    Toast.makeText(this, "Фото сделано успешно", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Фото из галереи
                Uri selectedImageUri = data.getData();
                try {
                    paymentImageBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), selectedImageUri);
                    ivPaymentImage.setImageBitmap(paymentImageBitmap);
                    Toast.makeText(this, "Изображение выбрано", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void savePayment() {
        if (paymentImageBitmap == null) {
            Toast.makeText(this, "Сначала добавьте фото чека", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Конвертируем Bitmap в base64 строку
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            paymentImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imageBase64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

            Log.d("AddPayment", "Saving payment for orderId: " + orderId);
            Log.d("AddPayment", "Image size: " + imageBase64.length() + " chars");

            // Сохраняем чек в базу данных
            boolean success = dbHelper.updateOrderPaymentProof(orderId, imageBase64);

            if (success) {
                Toast.makeText(this, "Чек оплаты добавлен", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Ошибка сохранения чека", Toast.LENGTH_SHORT).show();
                Log.e("AddPayment", "Failed to save payment proof for order: " + orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка обработки изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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