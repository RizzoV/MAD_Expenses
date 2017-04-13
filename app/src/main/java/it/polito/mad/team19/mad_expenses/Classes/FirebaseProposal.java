package it.polito.mad.team19.mad_expenses.Classes;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Bolz on 13/04/2017.
 */

@IgnoreExtraProperties
public class FirebaseProposal
{
    private String key;
    private String name;
    private String description;
    private Float cost;
    private String image;

    public FirebaseProposal () {}

    public FirebaseProposal(String name, String description, Float cost, String image)
    {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.image = image;
        this.key = null;
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

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
