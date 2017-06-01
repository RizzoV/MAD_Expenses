package it.polito.mad.team19.mad_expenses.Classes;

import java.util.Currency;

/**
 * Created by Jured on 31/03/17.
 */

public class Proposal {

    private String name;
    private String description;
    private Float extimatedCost;
    private String imageUrl;
    private Currency currency;
    private String firebaseId;
    private String author;

    public Proposal(String name, String description, String author, Float extimatedCost, String imageUrl, Currency currency, String firebaseId) {
        this.name = name;
        this.description = description;
        this.author = author;
        this.extimatedCost = extimatedCost;
        this.imageUrl = imageUrl;
        this.currency = currency;
        this.firebaseId = firebaseId;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public Float getExtimatedCost() {
        return extimatedCost;
    }

    public String getImageUrl() {
        return imageUrl;
    }


}

