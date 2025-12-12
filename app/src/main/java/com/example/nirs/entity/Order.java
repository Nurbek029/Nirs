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
    private String paymentProof;
    private String userEmail;

    public Order(int id, String date, double total, String status) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getPaymentProof() { return paymentProof; }
    public String getUserEmail() { return userEmail; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDate(String date) { this.date = date; }
    public void setTotal(double total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentProof(String paymentProof) { this.paymentProof = paymentProof; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    // Метод для форматирования даты
    public String getFormattedDate() {
        if (date == null || date.isEmpty()) {
            return "Дата не указана";
        }

        try {
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

    // Метод для получения статуса с цветом
    public int getStatusColor() {
        if (status == null) {
            return 0xFF757575;
        }

        switch (status.toLowerCase()) {
            case "в обработке":
                return 0xFF2196F3;
            case "принятый":
                return 0xFFFF9800;
            case "готов":
                return 0xFF4CAF50;
            case "завершен":
                return 0xFF4CAF50;
            case "отменен":
                return 0xFFF44336;
            default:
                return 0xFF757575;
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