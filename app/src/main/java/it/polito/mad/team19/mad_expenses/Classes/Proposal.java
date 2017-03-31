package it.polito.mad.team19.mad_expenses.Classes;

import android.media.Image;

/**
 * Created by Jured on 31/03/17.
 */

public class Proposal {

    private String name;
    private String description;
    private Float extimatedCost;
    private Image image;

    public Proposal(String name, String description, Float extimatedCost, Image image) {
        this.name = name;
        this.description = description;
        this.extimatedCost = extimatedCost;
        this.image = image;
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

