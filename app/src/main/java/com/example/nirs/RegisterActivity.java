package com.example.nirs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, loginButton;
    private CheckBox termsCheckbox;
    private DatabaseHelper dbHelper;

    // Регулярное выражение для проверки email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        // Инициализация элементов
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);
        termsCheckbox = findViewById(R.id.terms_checkbox);

        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Обработка кнопки назад
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Проверки
        if (!validateEmail(email)) {
            return;
        }

        if (!validatePassword(password)) {
            return;
        }

        if (!validateConfirmPassword(password, confirmPassword)) {
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Необходимо согласие с условиями использования", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка существования email
        if (dbHelper.isEmailExists(email)) {
            Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
            return;
        }

        // Регистрация пользователя
        if (dbHelper.addUser(email, password)) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Ошибка регистрации. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Подтвердите пароль", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}