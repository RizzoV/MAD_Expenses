package it.polito.mad.team19.mad_expenses.Classes;

import android.media.Image;

import java.util.Currency;

/**
 * Created by Jured on 27/03/17.
 */

public class Expense {

    private String name;
    private String descritpion;
    private Double cost;
    private Currency currency;
    private Image image;
    private String imagelink;
    private String author;
    private String firebaseId;
    private String category;
    private String date;

    public Expense(String name, Double cost, String description, String imagelink, String author, String firebaseId, String date, String category) {
       this(name,  cost,  null,  description,  imagelink, author, firebaseId, date, category);
    }

    public Expense(String name, Double cost, Currency currency, String description, String imagelink, String author, String firebaseId, String date, String category) {
        this.name = name;
        this.cost = cost;
        this.currency = currency;
        this.descritpion = description;
        this.imagelink = imagelink;
        this.author = author;
        this.firebaseId = firebaseId;
        this.category = category;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getImagelink() {
        return imagelink;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Currency getCurrency() { return currency; }

    public void setCurrency(Currency currency) { this.currency = currency; }

    public Image getImage() { return image; }

    public void setImage(Image image) { this.image = image; }

    public String getDescritpion() { return descritpion; }

    public void setDescritpion(String descritpion) { this.descritpion = descritpion; }

    public String getAuthor() {
        return author;
    }

    public void setAuthor (String author) {
        this.author = author;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

}
