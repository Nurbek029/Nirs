package com.example.nirs.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Order {
    private int id;
    private String date;
    private double total;
    private String status;

    public Order(int id, String date, double total, String status) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    // Setters (если понадобятся)
    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Метод для форматирования даты
    public String getFormattedDate() {
        if (date == null || date.isEmpty()) {
            return "Дата не указана";
        }

        try {
            // Пробуем разные форматы дат
            SimpleDateFormat[] inputFormats = {
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            };

            Date parsedDate = null;
            for (SimpleDateFormat format : inputFormats) {
                try {
                    parsedDate = format.parse(date);
                    if (parsedDate != null) break;
                } catch (ParseException e) {
                    // Пробуем следующий формат
                }
            }

            if (parsedDate != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                return outputFormat.format(parsedDate);
            } else {
                return date;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    // Метод для форматирования суммы в сомах
    public String getFormattedTotal() {
        return String.format(Locale.getDefault(), "%.0f сом", total);
    }

    // Метод для получения статуса с цветом (возвращает цвет в формате ARGB)
    public int getStatusColor() {
        if (status == null) {
            return 0xFF757575; // Серый по умолчанию
        }

        switch (status.toLowerCase()) {
            case "новый":
                return 0xFF2196F3; // Синий
            case "в процессе":
            case "в обработке":
                return 0xFFFF9800; // Оранжевый
            case "завершен":
            case "выполнен":
                return 0xFF4CAF50; // Зеленый
            case "отменен":
            case "отменён":
                return 0xFFF44336; // Красный
            case "доставляется":
                return 0xFF9C27B0; // Фиолетовый
            default:
                return 0xFF757575; // Серый
        }
    }

    // Метод для получения короткого статуса
    public String getShortStatus() {
        if (status == null) return "?";

        switch (status.toLowerCase()) {
            case "новый":
                return "Новый";
            case "в процессе":
            case "в обработке":
                return "В процессе";
            case "завершен":
            case "выполнен":
                return "Завершен";
            case "отменен":
            case "отменён":
                return "Отменен";
            case "доставляется":
                return "Доставляется";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", total=" + total +
                ", status='" + status + '\'' +
                '}';
    }
}