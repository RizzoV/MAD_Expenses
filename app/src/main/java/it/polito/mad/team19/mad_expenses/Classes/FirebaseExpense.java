package it.polito.mad.team19.mad_expenses.Classes;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Jured on 05/04/17.
 */

@IgnoreExtraProperties
public class FirebaseExpense
{

    private String author;
    private String key;
    private String name;
    private String description;
    private Double cost;
    private String currencyCode;
    private String image;
    private String isModified;

    public FirebaseExpense () {}

    public FirebaseExpense(String author, String name, String description, Double cost, String currencyCode)
    {
        this(author, name, description, cost, currencyCode, null, null);
    }

    public FirebaseExpense(String author, String name, String description, Double cost, String currencyCode, String image) {
        this(author, name, description, cost, currencyCode, image, null);
    }

    public FirebaseExpense(String author, String name, String description, Double cost, String currencyCode, String image, String isModified)
    {
        this.author = author;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.image = image;
        //this.key = null;
        this.isModified = isModified;
        this.currencyCode = currencyCode;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
