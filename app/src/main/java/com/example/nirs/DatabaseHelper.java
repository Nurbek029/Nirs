package com.example.nirs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.nirs.entity.CartItem;
import com.example.nirs.entity.Dish;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Nirs.db";
    private static final int DATABASE_VERSION = 5; // Увеличили версию для обновления БД

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_IS_ADMIN = "is_admin";
    private static final String COLUMN_CREATED_AT = "created_at";

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
    private static final String COLUMN_DISH_IS_ACTIVE = "is_active";

    // Таблица корзины
    private static final String TABLE_CART = "cart";
    private static final String COLUMN_CART_ID = "cart_id";
    private static final String COLUMN_CART_USER_ID = "user_id";
    private static final String COLUMN_CART_DISH_ID = "dish_id";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_ADDED_DATE = "added_date";

    // Таблица заказов
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_USER_ID = "user_id";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_ORDER_TOTAL = "total_amount";
    private static final String COLUMN_ORDER_STATUS = "status";
    private static final String COLUMN_ORDER_PAYMENT_PROOF = "payment_proof";
    private static final String COLUMN_ORDER_NOTES = "notes";

    // Таблица деталей заказа
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String COLUMN_ORDER_ITEM_ID = "order_item_id";
    private static final String COLUMN_ORDER_ITEM_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_ITEM_DISH_ID = "dish_id";
    private static final String COLUMN_ORDER_ITEM_QUANTITY = "quantity";
    private static final String COLUMN_ORDER_ITEM_PRICE = "price_at_order";
    private static final String COLUMN_ORDER_ITEM_NAME = "dish_name_at_order";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DatabaseHelper", "DatabaseHelper initialized with version: " + DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating database tables...");
        createTables(db);
        insertSampleData(db);
        Log.d("DatabaseHelper", "Database created successfully");
    }

    private void createTables(SQLiteDatabase db) {
        // Создание таблицы пользователей с полем created_at
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_IS_ADMIN + " INTEGER DEFAULT 0,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
        Log.d("DatabaseHelper", "Table " + TABLE_USERS + " created");

        // Создание таблицы блюд с is_active
        String CREATE_DISHES_TABLE = "CREATE TABLE " + TABLE_DISHES + "("
                + COLUMN_DISH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DISH_NAME + " TEXT NOT NULL,"
                + COLUMN_DISH_DESCRIPTION + " TEXT,"
                + COLUMN_DISH_PRICE + " REAL NOT NULL,"
                + COLUMN_DISH_CATEGORY + " TEXT NOT NULL,"
                + COLUMN_DISH_IMAGE + " TEXT,"
                + COLUMN_DISH_INGREDIENTS + " TEXT,"
                + COLUMN_DISH_COOK_DATE + " TEXT,"
                + COLUMN_DISH_IS_ACTIVE + " INTEGER DEFAULT 1"
                + ")";
        db.execSQL(CREATE_DISHES_TABLE);
        Log.d("DatabaseHelper", "Table " + TABLE_DISHES + " created");

        // Создание таблицы корзины с added_date
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CART_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_CART_DISH_ID + " INTEGER NOT NULL,"
                + COLUMN_QUANTITY + " INTEGER DEFAULT 1,"
                + COLUMN_ADDED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_CART_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CART_DISH_ID + ") REFERENCES " + TABLE_DISHES + "(" + COLUMN_DISH_ID + "),"
                + "UNIQUE(" + COLUMN_CART_USER_ID + ", " + COLUMN_CART_DISH_ID + ")"
                + ")";
        db.execSQL(CREATE_CART_TABLE);
        Log.d("DatabaseHelper", "Table " + TABLE_CART + " created");

        // Создание таблицы заказов с payment_proof и notes
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_ORDER_TOTAL + " REAL NOT NULL,"
                + COLUMN_ORDER_STATUS + " TEXT DEFAULT 'В обработке',"
                + COLUMN_ORDER_PAYMENT_PROOF + " TEXT,"
                + COLUMN_ORDER_NOTES + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_ORDER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_ORDERS_TABLE);
        Log.d("DatabaseHelper", "Table " + TABLE_ORDERS + " created");

        // Создание таблицы деталей заказа с dish_name_at_order
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + COLUMN_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_ITEM_ORDER_ID + " INTEGER NOT NULL,"
                + COLUMN_ORDER_ITEM_DISH_ID + " INTEGER NOT NULL,"
                + COLUMN_ORDER_ITEM_QUANTITY + " INTEGER NOT NULL,"
                + COLUMN_ORDER_ITEM_PRICE + " REAL NOT NULL,"
                + COLUMN_ORDER_ITEM_NAME + " TEXT NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_ORDER_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_ORDER_ITEM_DISH_ID + ") REFERENCES " + TABLE_DISHES + "(" + COLUMN_DISH_ID + ")"
                + ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);
        Log.d("DatabaseHelper", "Table " + TABLE_ORDER_ITEMS + " created");
    }

    private void insertSampleData(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Inserting sample data...");

        try {
            // Добавляем админа (БЕЗ ПАРОЛЯ admin123, как в Constants)
            ContentValues adminValues = new ContentValues();
            adminValues.put(COLUMN_EMAIL, Constants.ADMIN_EMAIL);
            adminValues.put(COLUMN_PASSWORD, Constants.ADMIN_PASSWORD);
            adminValues.put(COLUMN_IS_ADMIN, 1);
            long adminId = db.insert(TABLE_USERS, null, adminValues);
            Log.d("DatabaseHelper", "Admin user created with ID: " + adminId);

            // Добавляем обычного пользователя
            ContentValues userValues = new ContentValues();
            userValues.put(COLUMN_EMAIL, "test@test.com");
            userValues.put(COLUMN_PASSWORD, "123456");
            userValues.put(COLUMN_IS_ADMIN, 0);
            long userId = db.insert(TABLE_USERS, null, userValues);
            Log.d("DatabaseHelper", "Regular user created with ID: " + userId);

            // Добавляем еще несколько тестовых пользователей
            String[][] testUsers = {
                    {"user1@test.com", "123456", "0"},
                    {"user2@test.com", "123456", "0"},
                    {"manager@test.com", "manager123", "1"} // Еще один админ
            };

            for (String[] user : testUsers) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_EMAIL, user[0]);
                values.put(COLUMN_PASSWORD, user[1]);
                values.put(COLUMN_IS_ADMIN, Integer.parseInt(user[2]));
                long id = db.insert(TABLE_USERS, null, values);
                Log.d("DatabaseHelper", "User " + user[0] + " created with ID: " + id);
            }

            // Добавляем тестовые блюда
            insertSampleDishes(db);

            // Добавляем тестовые заказы для статистики
            insertSampleOrders(db, userId);

            Log.d("DatabaseHelper", "Sample data inserted successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertSampleDishes(SQLiteDatabase db) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String[][] dishes = {
                {"Борщ", "Традиционный украинский борщ со сметаной", "75", "Горячее", "Свекла, капуста, картофель, морковь, мясо", "borsch.jpg", currentDate},
                {"Плов", "Узбекский плов с бараниной", "120", "Горячее", "Рис, баранина, морковь, лук, специи", "plov.jpg", currentDate},
                {"Котлета по-киевски", "Куриная котлета с маслом", "95", "Горячее", "Куриное филе, масло, зелень, панировка", "kotleta.jpg", currentDate},
                {"Цезарь", "Салат Цезарь с курицей", "80", "Салаты", "Курица, салат, сухарики, сыр, соус", "caesar.jpg", currentDate},
                {"Оливье", "Традиционный салат Оливье", "70", "Салаты", "Картофель, морковь, колбаса, яйца, горошек", "olivie.jpg", currentDate},
                {"Чай черный", "Черный чай с сахаром", "25", "Напитки", "Чайные листья, вода", "tea.jpg", currentDate},
                {"Кофе американо", "Классический американо", "35", "Напитки", "Кофейные зерна, вода", "coffee.jpg", currentDate},
                {"Пицца Маргарита", "Классическая итальянская пицца", "150", "Горячее", "Тесто, томатный соус, моцарелла, базилик", "pizza.jpg", currentDate},
                {"Суп грибной", "Грибной суп со сливками", "65", "Горячее", "Грибы, картофель, лук, сливки, зелень", "mushroom_soup.jpg", currentDate},
                {"Бургер", "Классический бургер с говядиной", "110", "Горячее", "Булочка, котлета, сыр, салат, помидор, соус", "burger.jpg", currentDate}
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
            dishValues.put(COLUMN_DISH_IS_ACTIVE, 1);

            long dishId = db.insert(TABLE_DISHES, null, dishValues);
            Log.d("DatabaseHelper", "Dish " + dish[0] + " created with ID: " + dishId);
        }
    }

    private void insertSampleOrders(SQLiteDatabase db, long userId) {
        // Добавляем несколько тестовых заказов для статистики
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String[][] orders = {
                {String.valueOf(userId), "450", "Завершен", currentDate},
                {String.valueOf(userId), "320", "Завершен", currentDate},
                {String.valueOf(userId), "180", "В обработке", currentDate},
                {String.valueOf(userId), "95", "Принятый", currentDate},
                {String.valueOf(userId), "150", "Готов", currentDate}
        };

        for (int i = 0; i < orders.length; i++) {
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_ORDER_USER_ID, Long.parseLong(orders[i][0]));
            orderValues.put(COLUMN_ORDER_TOTAL, Double.parseDouble(orders[i][1]));
            orderValues.put(COLUMN_ORDER_STATUS, orders[i][2]);
            orderValues.put(COLUMN_ORDER_DATE, orders[i][3]);

            long orderId = db.insert(TABLE_ORDERS, null, orderValues);
            Log.d("DatabaseHelper", "Order " + (i+1) + " created with ID: " + orderId + ", status: " + orders[i][2]);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);

        try {
            // Удаляем все таблицы
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISHES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

            // Создаем заново
            onCreate(db);

            Log.d("DatabaseHelper", "Database upgraded successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DatabaseHelper", "Downgrading database from version " + oldVersion + " to " + newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }

    // ============ УТИЛИТНЫЕ МЕТОДЫ ============

    /**
     * Убеждаемся, что администратор существует в базе данных
     */
    public boolean ensureAdminExists() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Проверяем, существует ли администратор
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_EMAIL + " = ? AND " + COLUMN_IS_ADMIN + " = 1",
                    new String[]{Constants.ADMIN_EMAIL},
                    null, null, null);

            boolean adminExists = cursor.getCount() > 0;
            cursor.close();

            if (!adminExists) {
                Log.d("DatabaseHelper", "Administrator not found, creating...");

                // Удаляем старые записи с таким email (если есть не-админы)
                db.delete(TABLE_USERS, COLUMN_EMAIL + " = ? AND " + COLUMN_IS_ADMIN + " = 0",
                        new String[]{Constants.ADMIN_EMAIL});

                ContentValues adminValues = new ContentValues();
                adminValues.put(COLUMN_EMAIL, Constants.ADMIN_EMAIL);
                adminValues.put(COLUMN_PASSWORD, Constants.ADMIN_PASSWORD);
                adminValues.put(COLUMN_IS_ADMIN, 1);

                long result = db.insert(TABLE_USERS, null, adminValues);

                if (result != -1) {
                    Log.d("DatabaseHelper", "Administrator created successfully with ID: " + result);

                    // Проверяем, что админ действительно создан
                    int adminId = getUserId(Constants.ADMIN_EMAIL);
                    boolean isAdmin = isAdmin(adminId);
                    Log.d("DatabaseHelper", "Admin verification - ID: " + adminId + ", IsAdmin: " + isAdmin);

                    return true;
                } else {
                    Log.e("DatabaseHelper", "Failed to create administrator");
                    return false;
                }
            } else {
                Log.d("DatabaseHelper", "Administrator already exists");

                // Проверяем пароль админа
                Cursor adminCursor = db.query(TABLE_USERS,
                        new String[]{COLUMN_PASSWORD},
                        COLUMN_EMAIL + " = ? AND " + COLUMN_IS_ADMIN + " = 1",
                        new String[]{Constants.ADMIN_EMAIL},
                        null, null, null);

                if (adminCursor.moveToFirst()) {
                    String currentPassword = adminCursor.getString(adminCursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                    adminCursor.close();

                    // Если пароль не совпадает с константой, обновляем его
                    if (!Constants.ADMIN_PASSWORD.equals(currentPassword)) {
                        Log.d("DatabaseHelper", "Updating admin password to match constants");
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_PASSWORD, Constants.ADMIN_PASSWORD);

                        int rows = db.update(TABLE_USERS, values,
                                COLUMN_EMAIL + " = ? AND " + COLUMN_IS_ADMIN + " = 1",
                                new String[]{Constants.ADMIN_EMAIL});

                        Log.d("DatabaseHelper", "Admin password updated, rows affected: " + rows);
                    }
                }

                return true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error ensuring admin exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Полная проверка базы данных
     */
    public boolean verifyDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Проверяем существование всех таблиц
            String[] tables = {TABLE_USERS, TABLE_DISHES, TABLE_CART, TABLE_ORDERS, TABLE_ORDER_ITEMS};

            for (String table : tables) {
                Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                        new String[]{table});
                boolean tableExists = cursor.getCount() > 0;
                cursor.close();

                if (!tableExists) {
                    Log.e("DatabaseHelper", "Table missing: " + table);
                    return false;
                }
            }

            Log.d("DatabaseHelper", "All tables exist");

            // Проверяем количество пользователей
            Cursor userCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
            userCursor.moveToFirst();
            int userCount = userCursor.getInt(0);
            userCursor.close();

            Log.d("DatabaseHelper", "Total users in database: " + userCount);

            // Проверяем наличие администратора
            Cursor adminCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS +
                            " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_IS_ADMIN + " = 1",
                    new String[]{Constants.ADMIN_EMAIL});
            adminCursor.moveToFirst();
            int adminCount = adminCursor.getInt(0);
            adminCursor.close();

            Log.d("DatabaseHelper", "Admin users: " + adminCount);

            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error verifying database: " + e.getMessage());
            return false;
        }
    }

    // ============ ПОЛЬЗОВАТЕЛИ ============
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_ADMIN, 0); // По умолчанию не админ

        try {
            long result = db.insert(TABLE_USERS, null, values);
            Log.d("DatabaseHelper", "User added: " + email + ", result: " + result);
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

        Log.d("DatabaseHelper", "User check: " + email + " - " + (exists ? "exists" : "not found"));
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

        Log.d("DatabaseHelper", "Email exists check: " + email + " - " + exists);
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

        Log.d("DatabaseHelper", "getUserId for " + email + ": " + userId);
        return userId;
    }

    public boolean isAdmin(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_IS_ADMIN};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        boolean isAdmin = false;
        if (cursor.moveToFirst()) {
            isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ADMIN)) == 1;
        }
        cursor.close();

        Log.d("DatabaseHelper", "isAdmin check for user " + userId + ": " + isAdmin);
        return isAdmin;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_EMAIL, COLUMN_IS_ADMIN, COLUMN_CREATED_AT},
                null, null, null, null, COLUMN_CREATED_AT + " DESC");
    }

    // ============ БЛЮДА ============
    public Cursor getAllDishes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DISH_IS_ACTIVE + " = 1";
        return db.query(TABLE_DISHES, null, selection, null, null, null, COLUMN_DISH_NAME + " ASC");
    }

    public Cursor getDishesByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DISH_CATEGORY + " = ? AND " + COLUMN_DISH_IS_ACTIVE + " = 1";
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

    public boolean addDish(String name, String description, double price,
                           String category, String imageBase64, String ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DISH_NAME, name);
        values.put(COLUMN_DISH_DESCRIPTION, description);
        values.put(COLUMN_DISH_PRICE, price);
        values.put(COLUMN_DISH_CATEGORY, category);
        values.put(COLUMN_DISH_IMAGE, imageBase64);
        values.put(COLUMN_DISH_INGREDIENTS, ingredients);
        values.put(COLUMN_DISH_IS_ACTIVE, 1);

        // Автоматическая дата приготовления
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        values.put(COLUMN_DISH_COOK_DATE, currentDate);

        long result = db.insert(TABLE_DISHES, null, values);

        Log.d("DatabaseHelper", "Dish added: " + name + ", result: " + result);
        return result != -1;
    }

    public boolean deleteDish(int dishId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Вместо удаления помечаем как неактивное
        ContentValues values = new ContentValues();
        values.put(COLUMN_DISH_IS_ACTIVE, 0);

        int rowsAffected = db.update(TABLE_DISHES, values,
                COLUMN_DISH_ID + " = ?",
                new String[]{String.valueOf(dishId)});

        Log.d("DatabaseHelper", "Dish marked as inactive: " + dishId + ", rows: " + rowsAffected);
        return rowsAffected > 0;
    }

    // ============ КОРЗИНА ============
    public boolean addToCart(int userId, int dishId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_CART,
                new String[]{COLUMN_CART_ID, COLUMN_QUANTITY},
                COLUMN_CART_USER_ID + " = ? AND " + COLUMN_CART_DISH_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(dishId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Обновляем существующий товар в корзине
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_ID));
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, existingQuantity + quantity);

            int rowsAffected = db.update(TABLE_CART, values,
                    COLUMN_CART_ID + " = ?",
                    new String[]{String.valueOf(cartId)});

            Log.d("DatabaseHelper", "Cart updated: user " + userId + ", dish " + dishId +
                    ", new quantity: " + (existingQuantity + quantity));
            return rowsAffected > 0;
        } else {
            if (cursor != null) {
                cursor.close();
            }

            // Добавляем новый товар в корзину
            ContentValues values = new ContentValues();
            values.put(COLUMN_CART_USER_ID, userId);
            values.put(COLUMN_CART_DISH_ID, dishId);
            values.put(COLUMN_QUANTITY, quantity);

            long result = db.insert(TABLE_CART, null, values);

            Log.d("DatabaseHelper", "Cart item added: user " + userId + ", dish " + dishId +
                    ", result: " + result);
            return result != -1;
        }
    }

    public Cursor getCartItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.cart_id, c.dish_id, c.quantity, " +
                "d.name, d.description, d.price, d.image " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_DISHES + " d ON c.dish_id = d.dish_id " +
                "WHERE c.user_id = ? AND d.is_active = 1 " +
                "ORDER BY c.added_date DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public boolean updateCartItemQuantity(int cartId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(cartId, -1); // -1 означает любой userId
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, newQuantity);

        int rowsAffected = db.update(TABLE_CART, values,
                COLUMN_CART_ID + " = ?",
                new String[]{String.valueOf(cartId)});

        Log.d("DatabaseHelper", "Cart quantity updated: " + cartId + " -> " + newQuantity);
        return rowsAffected > 0;
    }

    public boolean removeFromCart(int cartId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_CART_ID + " = ?";
        String[] whereArgs = {String.valueOf(cartId)};

        if (userId != -1) {
            whereClause += " AND " + COLUMN_CART_USER_ID + " = ?";
            whereArgs = new String[]{String.valueOf(cartId), String.valueOf(userId)};
        }

        int rowsDeleted = db.delete(TABLE_CART, whereClause, whereArgs);

        Log.d("DatabaseHelper", "Cart item removed: " + cartId + ", rows: " + rowsDeleted);
        return rowsDeleted > 0;
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CART, COLUMN_CART_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        Log.d("DatabaseHelper", "Cart cleared for user " + userId + ", items removed: " + rowsDeleted);
    }

    public double getCartTotal(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(c.quantity * d.price) " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_DISHES + " d ON c.dish_id = d.dish_id " +
                "WHERE c.user_id = ? AND d.is_active = 1";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();

        Log.d("DatabaseHelper", "Cart total for user " + userId + ": " + total);
        return total;
    }

    // ============ ЗАКАЗЫ ============
    public long createOrder(int userId, double totalAmount, List<CartItem> cartItems) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // Создаем заказ
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_ORDER_USER_ID, userId);
            orderValues.put(COLUMN_ORDER_TOTAL, totalAmount);
            orderValues.put(COLUMN_ORDER_STATUS, "В обработке");
            orderValues.put(COLUMN_ORDER_NOTES, "");

            long orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId == -1) {
                db.endTransaction();
                Log.e("DatabaseHelper", "Failed to create order");
                return -1;
            }

            Log.d("DatabaseHelper", "Order created with ID: " + orderId);

            // Добавляем товары в заказ
            for (CartItem item : cartItems) {
                Dish dish = getDishDetails(item.getDishId());
                if (dish != null) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_ORDER_ITEM_ORDER_ID, orderId);
                    itemValues.put(COLUMN_ORDER_ITEM_DISH_ID, item.getDishId());
                    itemValues.put(COLUMN_ORDER_ITEM_QUANTITY, item.getQuantity());
                    itemValues.put(COLUMN_ORDER_ITEM_PRICE, item.getPrice());
                    itemValues.put(COLUMN_ORDER_ITEM_NAME, dish.getName());

                    long itemId = db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                    Log.d("DatabaseHelper", "Order item added: " + itemId);
                }
            }

            // Очищаем корзину
            clearCart(userId);

            db.setTransactionSuccessful();

            Log.d("DatabaseHelper", "Order completed successfully, ID: " + orderId);
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
        String query = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                "u.email as user_email " +
                "FROM " + TABLE_ORDERS + " o " +
                "INNER JOIN " + TABLE_USERS + " u ON o.user_id = u.user_id " +
                "WHERE o.user_id = ? " +
                "ORDER BY o.order_date DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                "o.payment_proof, o.notes, u.email as user_email " +
                "FROM " + TABLE_ORDERS + " o " +
                "INNER JOIN " + TABLE_USERS + " u ON o.user_id = u.user_id " +
                "ORDER BY o.order_date DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getOrdersByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                "o.payment_proof, u.email as user_email " +
                "FROM " + TABLE_ORDERS + " o " +
                "INNER JOIN " + TABLE_USERS + " u ON o.user_id = u.user_id " +
                "WHERE o.status = ? " +
                "ORDER BY o.order_date DESC";
        return db.rawQuery(query, new String[]{status});
    }

    public Cursor getOrderDetails(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT oi.quantity, oi.price_at_order, oi.dish_name_at_order as name " +
                "FROM " + TABLE_ORDER_ITEMS + " oi " +
                "WHERE oi.order_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    public Cursor getOrderItems(long orderId) {
        return getOrderDetails(orderId);
    }

    public boolean updateOrderStatus(long orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_STATUS, status);

        int rowsAffected = db.update(TABLE_ORDERS, values,
                COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});

        Log.d("DatabaseHelper", "Order status updated: " + orderId + " -> " + status +
                ", rows: " + rowsAffected);
        return rowsAffected > 0;
    }

    public boolean updateOrderPaymentProof(long orderId, String paymentProof) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_PAYMENT_PROOF, paymentProof);

        int rowsAffected = db.update(TABLE_ORDERS, values,
                COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});

        Log.d("DatabaseHelper", "Order payment proof updated: " + orderId +
                ", rows: " + rowsAffected);
        return rowsAffected > 0;
    }

    public boolean updateOrderNotes(long orderId, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_NOTES, notes);

        int rowsAffected = db.update(TABLE_ORDERS, values,
                COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});

        Log.d("DatabaseHelper", "Order notes updated: " + orderId +
                ", rows: " + rowsAffected);
        return rowsAffected > 0;
    }

    public int getOrderCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        Log.d("DatabaseHelper", "Order count for user " + userId + ": " + count);
        return count;
    }

    // ============ СТАТИСТИКА ============
    public int getOrderCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE status = ?";
        Cursor cursor = db.rawQuery(query, new String[]{status});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        Log.d("DatabaseHelper", "Order count for status '" + status + "': " + count);
        return count;
    }

    public double getTotalSpent(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(total_amount) FROM " + TABLE_ORDERS +
                " WHERE user_id = ? AND status = 'Завершен'";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();

        Log.d("DatabaseHelper", "Total spent for user " + userId + ": " + total);
        return total;
    }

    public double getRevenueFromCompletedOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(total_amount) FROM " + TABLE_ORDERS +
                " WHERE status = 'Завершен'";
        Cursor cursor = db.rawQuery(query, null);

        double revenue = 0;
        if (cursor.moveToFirst()) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();

        Log.d("DatabaseHelper", "Revenue from completed orders: " + revenue);
        return revenue;
    }

    public double getTotalRevenue() {
        return getRevenueFromCompletedOrders();
    }

    public int getCompletedOrdersCount() {
        return getOrderCountByStatus("Завершен");
    }

    // ============ ОБНОВЛЕНИЕ БЛЮД ============
    public boolean updateDish(int dishId, String name, String description, double price,
                              String category, String imageBase64, String ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DISH_NAME, name);
        values.put(COLUMN_DISH_DESCRIPTION, description);
        values.put(COLUMN_DISH_PRICE, price);
        values.put(COLUMN_DISH_CATEGORY, category);
        values.put(COLUMN_DISH_IMAGE, imageBase64);
        values.put(COLUMN_DISH_INGREDIENTS, ingredients);
        values.put(COLUMN_DISH_IS_ACTIVE, 1);

        // Обновляем дату приготовления
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        values.put(COLUMN_DISH_COOK_DATE, currentDate);

        int rowsAffected = db.update(TABLE_DISHES, values,
                COLUMN_DISH_ID + " = ?",
                new String[]{String.valueOf(dishId)});

        Log.d("DatabaseHelper", "Updated dish ID " + dishId + ", rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    public Dish getDishById(int dishId) {
        return getDishDetails(dishId);
    }

    // ============ ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ============
    public Cursor searchDishes(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_DISH_NAME + " LIKE ? AND " + COLUMN_DISH_IS_ACTIVE + " = 1";
        String[] selectionArgs = {"%" + query + "%"};
        return db.query(TABLE_DISHES, null, selection, selectionArgs, null, null, COLUMN_DISH_NAME + " ASC");
    }

    public int getTotalUsersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    public int getTotalDishesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_DISHES + " WHERE " + COLUMN_DISH_IS_ACTIVE + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    public Cursor getOrdersByStatusWithRevenue(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                "o.payment_proof, u.email as user_email " +
                "FROM " + TABLE_ORDERS + " o " +
                "INNER JOIN " + TABLE_USERS + " u ON o.user_id = u.user_id " +
                "WHERE o.status = ? " +
                "ORDER BY o.order_date DESC";
        return db.rawQuery(query, new String[]{status});
    }

    /**
     * Получает все заказы за последние N дней
     */
    public Cursor getRecentOrders(int days) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                "u.email as user_email " +
                "FROM " + TABLE_ORDERS + " o " +
                "INNER JOIN " + TABLE_USERS + " u ON o.user_id = u.user_id " +
                "WHERE date(o.order_date) >= date('now', '-" + days + " days') " +
                "ORDER BY o.order_date DESC";
        return db.rawQuery(query, null);
    }

    /**
     * Получает самые популярные блюда
     */
    public Cursor getPopularDishes(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT d.dish_id, d.name, d.price, d.category, " +
                "SUM(oi.quantity) as total_sold " +
                "FROM " + TABLE_ORDER_ITEMS + " oi " +
                "INNER JOIN " + TABLE_DISHES + " d ON oi.dish_id = d.dish_id " +
                "INNER JOIN " + TABLE_ORDERS + " o ON oi.order_id = o.order_id " +
                "WHERE o.status = 'Завершен' " +
                "GROUP BY d.dish_id " +
                "ORDER BY total_sold DESC " +
                "LIMIT " + limit;
        return db.rawQuery(query, null);
    }

    @Override
    public synchronized void close() {
        super.close();
        Log.d("DatabaseHelper", "Database connection closed");
    }
}