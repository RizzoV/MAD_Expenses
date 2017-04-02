package it.polito.mad.team19.mad_expenses.Classes;

import android.media.Image;

import java.util.Currency;

/**
 * Created by Jured on 31/03/17.
 */

public class Proposal {

    private String name;
    private String description;
    private Float extimatedCost;
    private Image image;
    private Currency currency;

    public Proposal(String name, String description, Float extimatedCost, Image image, Currency currency) {
        this.name = name;
        this.description = description;
        this.extimatedCost = extimatedCost;
        this.image = image;
        this.currency = currency;
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

    public String getDescription() {
        return description;
    }

    public Float getExtimatedCost() {
        return extimatedCost;
    }

    public Image getImage() {
        return image;
    }
}

