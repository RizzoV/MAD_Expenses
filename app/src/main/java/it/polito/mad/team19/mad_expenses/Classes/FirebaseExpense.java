package it.polito.mad.team19.mad_expenses.Classes;

import android.util.Log;

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
    private String date;
    private String isModified;
    private String category;
    private String modTime;

    public FirebaseExpense () {}

    /*public FirebaseExpense(String author, String name, String description, Double cost, String currencyCode, String image, String date, String category)
    {
        this(author, name, description, cost, currencyCode, null, null, category);
    }

    public FirebaseExpense(String author, String name, String description, Double cost, String currencyCode, String image, String category) {
        this(author, name, description, cost, currencyCode, image, null, category);
    }*/

    public FirebaseExpense(String author, String name, String description, Double cost, String currencyCode, String image, String date, String category)
    {
        this.author = author;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.image = image;
        //this.key = null;
        this.currencyCode = currencyCode;
        this.category = category;
        this.date = date;
    }

    public String getModTime() {
        return modTime;
    }

    public void setModTime(String modTime) {
        this.modTime = modTime;
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

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

    public void setImage(String image) {
        this.image = image;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCategory() { return category; }
}
