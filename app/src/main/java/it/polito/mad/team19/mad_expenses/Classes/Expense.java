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



    public Expense(String name, Float cost, String description, Image image) {
       new  Expense( name,  cost,  null,  description,  image);
    }

    public Expense(String name, Float cost, Currency currency, String description, Image image) {
        this.name = name;
        this.cost = cost;
        this.currency = currency;
        this.descritpion = description;
        this.image = image;
    }

    public String getName() {
        return name;
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
}
