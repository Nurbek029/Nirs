package com.example.nirs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.nirs.entity.CartItem;
import com.example.nirs.entity.Dish;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Nirs.db";
    private static final int DATABASE_VERSION = 3; // Увеличиваем версию

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Таблица блюд
    private static final String TABLE_DISHES = "dishes";
    private static final String COLUMN_DISH_ID = "dish_id";
    private static final String COLUMN_DISH_NAME = "name";
    private static final String COLUMN_DISH_DESCRIPTION = "description";
    private static final String COLUMN_DISH_PRICE = "price";
    private static final String COLUMN_DISH_CATEGORY = "category";
    private static final String COLUMN_DISH_IMAGE = "image";
    private static final String COLUMN_DISH_INGREDIENTS = "ingredients";
    private static final String COLUMN_DISH_COOK_DATE = "cook_date";

    // Таблица корзины
    private static final String TABLE_CART = "cart";
    private static final String COLUMN_CART_ID = "cart_id";
    private static final String COLUMN_CART_USER_ID = "user_id";
    private static final String COLUMN_CART_DISH_ID = "dish_id";
    private static final String COLUMN_QUANTITY = "quantity";

    // Таблица заказов
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_USER_ID = "user_id";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_ORDER_TOTAL = "total_amount";
    private static final String COLUMN_ORDER_STATUS = "status";

    // Таблица деталей заказа
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String COLUMN_ORDER_ITEM_ID = "order_item_id";
    private static final String COLUMN_ORDER_ITEM_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_ITEM_DISH_ID = "dish_id";
    private static final String COLUMN_ORDER_ITEM_QUANTITY = "quantity";
    private static final String COLUMN_ORDER_ITEM_PRICE = "price_at_order";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertSampleData(db);
    }

    private void createTables(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы блюд
        String CREATE_DISHES_TABLE = "CREATE TABLE " + TABLE_DISHES + "("
                + COLUMN_DISH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DISH_NAME + " TEXT NOT NULL,"
                + COLUMN_DISH_DESCRIPTION + " TEXT,"
                + COLUMN_DISH_PRICE + " REAL NOT NULL,"
                + COLUMN_DISH_CATEGORY + " TEXT NOT NULL,"
                + COLUMN_DISH_IMAGE + " TEXT,"
                + COLUMN_DISH_INGREDIENTS + " TEXT,"
                + COLUMN_DISH_COOK_DATE + " TEXT" + ")";
        db.execSQL(CREATE_DISHES_TABLE);

        // Создание таблицы корзины
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CART_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_CART_DISH_ID + " INTEGER NOT NULL,"
                + COLUMN_QUANTITY + " INTEGER DEFAULT 1,"
                + "FOREIGN KEY(" + COLUMN_CART_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CART_DISH_ID + ") REFERENCES " + TABLE_DISHES + "(" + COLUMN_DISH_ID + ")"
                + ")";
        db.execSQL(CREATE_CART_TABLE);

        // Создание таблицы заказов
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_ORDER_TOTAL + " REAL NOT NULL,"
                + COLUMN_ORDER_STATUS + " TEXT DEFAULT 'Новый',"
                + "FOREIGN KEY(" + COLUMN_ORDER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Создание таблицы деталей заказа
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + COLUMN_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_ITEM_ORDER_ID + " INTEGER NOT NULL,"
                + COLUMN_ORDER_ITEM_DISH_ID + " INTEGER NOT NULL,"
                + COLUMN_ORDER_ITEM_QUANTITY + " INTEGER NOT NULL,"
                + COLUMN_ORDER_ITEM_PRICE + " REAL NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_ORDER_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_ORDER_ITEM_DISH_ID + ") REFERENCES " + TABLE_DISHES + "(" + COLUMN_DISH_ID + ")"
                + ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Добавляем тестового пользователя
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_EMAIL, "test@test.com");
        userValues.put(COLUMN_PASSWORD, "123456");
        db.insert(TABLE_USERS, null, userValues);

        // Добавляем тестовые блюда
        String[][] dishes = {
                {"Борщ", "Традиционный украинский борщ со сметаной", "75", "Горячее", "Свекла, капуста, картофель, морковь, мясо", "borsch.jpg", "2024-11-25"},
                {"Плов", "Узбекский плов с бараниной", "120", "Горячее", "Рис, баранина, морковь, лук, специи", "plov.jpg", "2024-11-25"},
                {"Котлета по-киевски", "Куриная котлета с маслом", "95", "Горячее", "Куриное филе, масло, зелень, панировка", "kotleta.jpg", "2024-11-25"},
                {"Цезарь", "Салат Цезарь с курицей", "80", "Салаты", "Курица, салат, сухарики, сыр, соус", "caesar.jpg", "2024-11-25"},
                {"Оливье", "Традиционный салат Оливье", "70", "Салаты", "Картофель, морковь, колбаса, яйца, горошек", "olivie.jpg", "2024-11-25"},
                {"Чай черный", "Черный чай с сахаром", "25", "Напитки", "Чайные листья, вода", "tea.jpg", "2024-11-25"},
                {"Кофе американо", "Классический американо", "35", "Напитки", "Кофейные зерна, вода", "coffee.jpg", "2024-11-25"}
        };

        for (String[] dish : dishes) {
            ContentValues dishValues = new ContentValues();
            dishValues.put(COLUMN_DISH_NAME, dish[0]);
            dishValues.put(COLUMN_DISH_DESCRIPTION, dish[1]);
            dishValues.put(COLUMN_DISH_PRICE, Double.parseDouble(dish[2]));
            dishValues.put(COLUMN_DISH_CATEGORY, dish[3]);
            dishValues.put(COLUMN_DISH_INGREDIENTS, dish[4]);
            dishValues.put(COLUMN_DISH_IMAGE, dish[5]);
            dishValues.put(COLUMN_DISH_COOK_DATE, dish[6]);
            db.insert(TABLE_DISHES, null, dishValues);
        }

        Log.d("DatabaseHelper", "Sample data inserted successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем старые таблицы
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Создаем заново
        onCreate(db);
    }

    // ============ ПОЛЬЗОВАТЕЛИ ============
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        try {
            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user: " + e.getMessage());
            return false;
        }
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();

        return userId;
    }

    // ============ БЛЮДА ============
    public Cursor getAllDishes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DISHES, null, null, null, null, null, COLUMN_DISH_NAME + " ASC");
    }

    public Cursor getDishesByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DISH_CATEGORY + " = ?";
        String[] selectionArgs = {category};
        return db.query(TABLE_DISHES, null, selection, selectionArgs, null, null, COLUMN_DISH_NAME + " ASC");
    }

    public Dish getDishDetails(int dishId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DISH_ID + " = ?";
        String[] selectionArgs = {String.valueOf(dishId)};

        Cursor cursor = db.query(TABLE_DISHES, null, selection, selectionArgs, null, null, null);

        Dish dish = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                dish = new Dish(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISH_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_DESCRIPTION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISH_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_IMAGE))
                );

                int ingredientsIndex = cursor.getColumnIndex(COLUMN_DISH_INGREDIENTS);
                if (ingredientsIndex != -1) {
                    dish.setIngredients(cursor.getString(ingredientsIndex));
                }

                int cookDateIndex = cursor.getColumnIndex(COLUMN_DISH_COOK_DATE);
                if (cookDateIndex != -1) {
                    dish.setCookDate(cursor.getString(cookDateIndex));
                }
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error getting dish details: " + e.getMessage());
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return dish;
    }

    // ============ КОРЗИНА ============
    public boolean addToCart(int userId, int dishId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Проверяем, есть ли уже такой товар в корзине
        Cursor cursor = db.query(TABLE_CART,
                new String[]{COLUMN_CART_ID, COLUMN_QUANTITY},
                COLUMN_CART_USER_ID + " = ? AND " + COLUMN_CART_DISH_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(dishId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Товар уже есть - обновляем количество
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_ID));
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, existingQuantity + quantity);

            int rowsAffected = db.update(TABLE_CART, values,
                    COLUMN_CART_ID + " = ?",
                    new String[]{String.valueOf(cartId)});
            return rowsAffected > 0;
        } else {
            if (cursor != null) {
                cursor.close();
            }
            // Товара нет - добавляем новый
            ContentValues values = new ContentValues();
            values.put(COLUMN_CART_USER_ID, userId);
            values.put(COLUMN_CART_DISH_ID, dishId);
            values.put(COLUMN_QUANTITY, quantity);

            long result = db.insert(TABLE_CART, null, values);
            return result != -1;
        }
    }

    public Cursor getCartItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.cart_id, c.dish_id, c.quantity, " +
                "d.name, d.description, d.price " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_DISHES + " d ON c.dish_id = d.dish_id " +
                "WHERE c.user_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public boolean removeFromCart(int cartId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CART,
                COLUMN_CART_ID + " = ? AND " + COLUMN_CART_USER_ID + " = ?",
                new String[]{String.valueOf(cartId), String.valueOf(userId)});
        return rowsDeleted > 0;
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_CART_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
    }

    // ============ ЗАКАЗЫ ============
    public long createOrder(int userId, double totalAmount, List<CartItem> cartItems) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // 1. Создаем заказ
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_ORDER_USER_ID, userId);
            orderValues.put(COLUMN_ORDER_TOTAL, totalAmount);
            orderValues.put(COLUMN_ORDER_STATUS, "Новый");

            long orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId == -1) {
                db.endTransaction();
                return -1;
            }

            // 2. Добавляем товары в заказ
            for (CartItem item : cartItems) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(COLUMN_ORDER_ITEM_ORDER_ID, orderId);
                itemValues.put(COLUMN_ORDER_ITEM_DISH_ID, item.getDishId());
                itemValues.put(COLUMN_ORDER_ITEM_QUANTITY, item.getQuantity());
                itemValues.put(COLUMN_ORDER_ITEM_PRICE, item.getPrice());

                long itemResult = db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                if (itemResult == -1) {
                    Log.e("DatabaseHelper", "Failed to insert order item");
                }
            }

            // 3. Очищаем корзину
            clearCart(userId);

            db.setTransactionSuccessful();
            return orderId;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating order: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getUserOrders(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT order_id, order_date, total_amount, status " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE user_id = ? " +
                "ORDER BY order_date DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public int getOrderCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS +
                " WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }
// Добавьте в класс DatabaseHelper:

    public Cursor getOrderDetails(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT oi.quantity, oi.price_at_order, d.name " +
                "FROM " + TABLE_ORDER_ITEMS + " oi " +
                "INNER JOIN " + TABLE_DISHES + " d ON oi.dish_id = d.dish_id " +
                "WHERE oi.order_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    // И обновите метод getOrderItems для совместимости:
    public Cursor getOrderItems(long orderId) {
        return getOrderDetails(orderId); // Просто вызываем новый метод
    }
    public double getTotalSpent(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(total_amount) FROM " + TABLE_ORDERS +
                " WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();

        return total;
    }
}