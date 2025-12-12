package com.example.nirs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Убедимся, что администратор существует
        dbHelper.ensureAdminExists();

        Log.d("LoginActivity", "App started - Checking session");
        sessionManager.debugPrintAllData();

        // Проверяем, вошел ли пользователь
        if (sessionManager.isLoggedIn()) {
            Log.d("LoginActivity", "User already logged in, redirecting to MainActivity");
            redirectToMain();
            return;
        }

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

        Log.d("LoginActivity", "Login attempt - Email: " + email);

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем пользователя в базе данных
        boolean userExists = dbHelper.checkUser(email, password);
        Log.d("LoginActivity", "User check result: " + userExists);

        if (userExists) {
            int userId = dbHelper.getUserId(email);
            boolean isAdmin = dbHelper.isAdmin(userId);

            Log.d("LoginActivity", "Login successful! UserID: " + userId + ", IsAdmin: " + isAdmin);

            // Сохраняем данные через SessionManager
            sessionManager.loginUser(userId, email, isAdmin);

            // Для отладки
            sessionManager.debugPrintAllData();

            // Сохраняем логин/пароль если выбрано "Запомнить меня"
            if (rememberMeCheckbox.isChecked()) {
                saveCredentials(email, password);
            } else {
                clearCredentials();
            }

            Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d("LoginActivity", "Login failed - invalid credentials");
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("saved_email", email);
        editor.putString("saved_password", password);
        editor.putBoolean("remember_me", true);
        editor.apply();
    }

    private void loadSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean("remember_me", false);
        if (rememberMe) {
            String email = sharedPreferences.getString("saved_email", "");
            String password = sharedPreferences.getString("saved_password", "");
            emailEditText.setText(email);
            passwordEditText.setText(password);
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void clearCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("saved_email");
        editor.remove("saved_password");
        editor.remove("remember_me");
        editor.apply();
    }

    private void redirectToMain() {
        Log.d("LoginActivity", "=== REDIRECTING TO MAIN ===");
        sessionManager.debugPrintAllData();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}