package it.polito.mad.team19.mad_expenses.Classes;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Bolz on 13/04/2017.
 */

@IgnoreExtraProperties
public class FirebaseProposal
{
    private String name;
    private String description;
    private Double cost;
    private String imageUrl;
    private String author;
    private String currencyCode;

    public FirebaseProposal () {}

    public FirebaseProposal(String name, String description, String author, Double cost, String currencyCode) {
        this(name, description, author, cost, currencyCode, null);
    }

    public FirebaseProposal(String name, String description, String author, Double cost, String currencyCode, String image) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.author = author;
        this.imageUrl = image;
        this.currencyCode = currencyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String image) {
        this.imageUrl = image;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
