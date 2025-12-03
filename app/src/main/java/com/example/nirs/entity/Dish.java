package com.example.nirs.entity;

public class Dish {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String image;
    private String ingredients;
    private String cookDate;

    public Dish(int id, String name, String description, double price,
                String category, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.image = image;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getCookDate() { return cookDate; }
    public void setCookDate(String cookDate) { this.cookDate = cookDate; }

    // Метод для форматирования цены в сомах
    public String getFormattedPrice() {
        return String.format("%.0f сом", price);
    }
}