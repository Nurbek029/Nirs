package com.example.nirs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS_NAME = Constants.PREFS_NAME;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Сохраняем данные пользователя при входе
    public void loginUser(int userId, String email, boolean isAdmin) {
        editor.clear();
        editor.putInt(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putBoolean(Constants.KEY_IS_ADMIN, isAdmin);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);

        boolean saved = editor.commit();
        Log.d("SessionManager", "User logged in - ID: " + userId +
                ", Email: " + email + ", IsAdmin: " + isAdmin + ", Saved: " + saved);
    }

    // Проверяем, вошел ли пользователь
    public boolean isLoggedIn() {
        return sharedPreferences.getInt(Constants.KEY_USER_ID, -1) != -1;
    }

    // Получаем ID пользователя
    public int getUserId() {
        return sharedPreferences.getInt(Constants.KEY_USER_ID, -1);
    }

    // Получаем email пользователя
    public String getEmail() {
        return sharedPreferences.getString(Constants.KEY_EMAIL, "");
    }

    // Проверяем, является ли пользователь администратором
    public boolean isAdmin() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_ADMIN, false);
    }

    // Выход из системы
    public void logout() {
        editor.clear();
        boolean cleared = editor.commit();
        Log.d("SessionManager", "User logged out - Cleared: " + cleared);
    }

    // Для отладки: выводим все сохраненные данные
    public void debugPrintAllData() {
        Log.d("SessionManager", "=== DEBUG: All SharedPreferences Data ===");
        Log.d("SessionManager", "KEY_USER_ID: " + getUserId());
        Log.d("SessionManager", "KEY_EMAIL: '" + getEmail() + "'");
        Log.d("SessionManager", "KEY_IS_ADMIN: " + isAdmin());
        Log.d("SessionManager", "KEY_IS_LOGGED_IN: " +
                sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false));
    }
}