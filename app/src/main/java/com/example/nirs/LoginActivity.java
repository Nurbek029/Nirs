package com.example.nirs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private CheckBox rememberMeCheckbox;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // Инициализация элементов
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);

        // Загрузка сохраненных данных
        loadSavedCredentials();

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Обработка кнопки назад
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Проверки
        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка пользователя в базе данных
        if (dbHelper.checkUser(email, password)) {
            // Сохранение данных, если отмечено "Запомнить меня"
            if (rememberMeCheckbox.isChecked()) {
                saveCredentials(email, password);
            } else {
                clearCredentials();
            }

            // Сохраняем ID пользователя
            int userId = dbHelper.getUserId(email);
            saveUserId(userId);

            Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show();

            // Переход в главное меню
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_PASSWORD, password);
        editor.putBoolean(Constants.KEY_REMEMBER_ME, true);
        editor.apply();
    }

    private void saveUserId(int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.KEY_USER_ID, userId);
        editor.apply();
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean(Constants.KEY_REMEMBER_ME, false);
        if (rememberMe) {
            String email = sharedPreferences.getString(Constants.KEY_EMAIL, "");
            String password = sharedPreferences.getString(Constants.KEY_PASSWORD, "");
            emailEditText.setText(email);
            passwordEditText.setText(password);
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void clearCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.KEY_EMAIL);
        editor.remove(Constants.KEY_PASSWORD);
        editor.remove(Constants.KEY_REMEMBER_ME);
        editor.remove(Constants.KEY_USER_ID);
        editor.apply();
    }
}