package com.example.nirs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nirs.entity.CartItem;
import com.example.nirs.entity.Dish;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Nirs.db";
    private static final int DATABASE_VERSION = 2;

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
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы блюд
        String CREATE_DISHES_TABLE = "CREATE TABLE " + TABLE_DISHES + "("
                + COLUMN_DISH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DISH_NAME + " TEXT,"
                + COLUMN_DISH_DESCRIPTION + " TEXT,"
                + COLUMN_DISH_PRICE + " REAL,"
                + COLUMN_DISH_CATEGORY + " TEXT,"
                + COLUMN_DISH_IMAGE + " TEXT,"
                + COLUMN_DISH_INGREDIENTS + " TEXT,"
                + COLUMN_DISH_COOK_DATE + " TEXT" + ")";
        db.execSQL(CREATE_DISHES_TABLE);

        // Создание таблицы корзины
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CART_USER_ID + " INTEGER,"
                + COLUMN_CART_DISH_ID + " INTEGER,"
                + COLUMN_QUANTITY + " INTEGER" + ")";
        db.execSQL(CREATE_CART_TABLE);

        // Создание таблицы заказов
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_USER_ID + " INTEGER,"
                + COLUMN_ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_ORDER_TOTAL + " REAL,"
                + COLUMN_ORDER_STATUS + " TEXT DEFAULT 'Новый'" + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Создание таблицы деталей заказа
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + COLUMN_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_ITEM_ORDER_ID + " INTEGER,"
                + COLUMN_ORDER_ITEM_DISH_ID + " INTEGER,"
                + COLUMN_ORDER_ITEM_QUANTITY + " INTEGER,"
                + COLUMN_ORDER_ITEM_PRICE + " REAL" + ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);

        // Добавление тестовых данных
        insertSampleDishes(db);
        insertSampleUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        onCreate(db);
    }

    // Добавление пользователя
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        try {
            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Проверка пользователя
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Проверка существования email
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Получение ID пользователя по email
    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            cursor.close();
        }
        return userId;
    }

    // Добавление тестовых блюд
    private void insertSampleDishes(SQLiteDatabase db) {
        // Горячие блюда
        insertDish(db, "Борщ", "Традиционный украинский борщ со сметаной", 75.0, "Горячее",
                "Свекла, капуста, картофель, морковь, мясо", "borsch.jpg", "2024-11-25");
        insertDish(db, "Плов", "Узбекский плов с бараниной", 120.0, "Горячее",
                "Рис, баранина, морковь, лук, специи", "plov.jpg", "2024-11-25");
        insertDish(db, "Котлета по-киевски", "Куриная котлета с маслом", 95.0, "Горячее",
                "Куриное филе, масло, зелень, панировка", "kotleta.jpg", "2024-11-25");

        // Салаты
        insertDish(db, "Цезарь", "Салат Цезарь с курицей", 80.0, "Салаты",
                "Курица, салат, сухарики, сыр, соус", "caesar.jpg", "2024-11-25");
        insertDish(db, "Оливье", "Традиционный салат Оливье", 70.0, "Салаты",
                "Картофель, морковь, колбаса, яйца, горошек", "olivie.jpg", "2024-11-25");

        // Напитки
        insertDish(db, "Чай черный", "Черный чай с сахаром", 25.0, "Напитки",
                "Чайные листья, вода", "tea.jpg", "2024-11-25");
        insertDish(db, "Кофе американо", "Классический американо", 35.0, "Напитки",
                "Кофейные зерна, вода", "coffee.jpg", "2024-11-25");
    }

    // Добавление тестового пользователя
    private void insertSampleUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, "test@test.com");
        values.put(COLUMN_PASSWORD, "123456");
        db.insert(TABLE_USERS, null, values);
    }

    private void insertDish(SQLiteDatabase db, String name, String description,
                            double price, String category, String ingredients,
                            String image, String cookDate) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DISH_NAME, name);
        values.put(COLUMN_DISH_DESCRIPTION, description);
        values.put(COLUMN_DISH_PRICE, price);
        values.put(COLUMN_DISH_CATEGORY, category);
        values.put(COLUMN_DISH_INGREDIENTS, ingredients);
        values.put(COLUMN_DISH_IMAGE, image);
        values.put(COLUMN_DISH_COOK_DATE, cookDate);
        db.insert(TABLE_DISHES, null, values);
    }

    // Получение всех блюд
    public Cursor getAllDishes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DISHES, null, null, null, null, null, null);
    }

    // Получение блюд по категории
    public Cursor getDishesByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DISH_CATEGORY + " = ?";
        String[] selectionArgs = {category};
        return db.query(TABLE_DISHES, null, selection, selectionArgs, null, null, null);
    }

    // Получение деталей блюда
    public Dish getDishDetails(int dishId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_DISH_ID, COLUMN_DISH_NAME, COLUMN_DISH_DESCRIPTION,
                COLUMN_DISH_PRICE, COLUMN_DISH_CATEGORY, COLUMN_DISH_IMAGE,
                COLUMN_DISH_INGREDIENTS, COLUMN_DISH_COOK_DATE};
        String selection = COLUMN_DISH_ID + " = ?";
        String[] selectionArgs = {String.valueOf(dishId)};

        Cursor cursor = db.query(TABLE_DISHES, columns, selection, selectionArgs,
                null, null, null);

        Dish dish = null;
        if (cursor != null && cursor.moveToFirst()) {
            dish = new Dish(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISH_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_DESCRIPTION)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISH_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_IMAGE))
            );
            dish.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_INGREDIENTS)));
            dish.setCookDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_COOK_DATE)));
            cursor.close();
        }
        return dish;
    }

    // Добавление в корзину
    public boolean addToCart(int userId, int dishId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CART_USER_ID, userId);
        values.put(COLUMN_CART_DISH_ID, dishId);
        values.put(COLUMN_QUANTITY, quantity);

        long result = db.insert(TABLE_CART, null, values);
        return result != -1;
    }

    // Получение корзины пользователя
    public Cursor getCartItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, d." + COLUMN_DISH_NAME + ", d." + COLUMN_DISH_PRICE +
                ", d." + COLUMN_DISH_DESCRIPTION +
                " FROM " + TABLE_CART + " c " +
                " INNER JOIN " + TABLE_DISHES + " d ON c." + COLUMN_CART_DISH_ID +
                " = d." + COLUMN_DISH_ID +
                " WHERE c." + COLUMN_CART_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Создание заказа
    public long createOrder(int userId, double totalAmount, List<CartItem> cartItems) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Создаем заказ
        ContentValues orderValues = new ContentValues();
        orderValues.put(COLUMN_ORDER_USER_ID, userId);
        orderValues.put(COLUMN_ORDER_TOTAL, totalAmount);
        orderValues.put(COLUMN_ORDER_STATUS, "Завершен");

        long orderId = db.insert(TABLE_ORDERS, null, orderValues);

        if (orderId != -1) {
            // Добавляем товары в заказ
            for (CartItem item : cartItems) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(COLUMN_ORDER_ITEM_ORDER_ID, orderId);
                itemValues.put(COLUMN_ORDER_ITEM_DISH_ID, item.getDishId());
                itemValues.put(COLUMN_ORDER_ITEM_QUANTITY, item.getQuantity());
                itemValues.put(COLUMN_ORDER_ITEM_PRICE, item.getPrice());
                db.insert(TABLE_ORDER_ITEMS, null, itemValues);
            }
        }

        return orderId;
    }

    // Получение заказов пользователя
    public Cursor getUserOrders(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ORDERS +
                " WHERE " + COLUMN_ORDER_USER_ID + " = ?" +
                " ORDER BY " + COLUMN_ORDER_DATE + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Получение деталей заказа
    public Cursor getOrderItems(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT oi.*, d." + COLUMN_DISH_NAME +
                " FROM " + TABLE_ORDER_ITEMS + " oi" +
                " INNER JOIN " + TABLE_DISHES + " d ON oi." + COLUMN_ORDER_ITEM_DISH_ID +
                " = d." + COLUMN_DISH_ID +
                " WHERE oi." + COLUMN_ORDER_ITEM_ORDER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }
}