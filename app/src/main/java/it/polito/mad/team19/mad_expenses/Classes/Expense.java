package it.polito.mad.team19.mad_expenses.Classes;

import android.media.Image;

import java.util.Currency;

/**
 * Created by Jured on 27/03/17.
 */

public class Expense {

    private String name;
    private String descritpion;
    private Float cost;
    private Currency currency;
    private Image image;
    private String imagelink;
    private String author;

    public Expense(String name, Float cost, String description, String imagelink, String author) {
       this(name,  cost,  null,  description,  imagelink, author);
    }

    public Expense(String name, Float cost, Currency currency, String description, String imagelink, String author) {
        this.name = name;
        this.cost = cost;
        this.currency = currency;
        this.descritpion = description;
        this.imagelink = imagelink;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public String getImagelink() {
        return imagelink;
    }


    public void setName(String name) {
        this.name = name;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
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
}
