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
    private Float cost;
    private String imageUrl;
    private String author;

    public FirebaseProposal () {}

    public FirebaseProposal(String name, String description, String author, Float cost, String image) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.author = author;
        this.imageUrl = image;
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

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String image) {
        this.imageUrl = image;
    }

}
