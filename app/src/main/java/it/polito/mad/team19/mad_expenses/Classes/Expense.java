package it.polito.mad.team19.mad_expenses.Classes;

/**
 * Created by Jured on 27/03/17.
 */

public class Expense {

    private String name;
    private Float cost;

    public Expense(String name, Float cost) {
        this.name = name;
        this.cost = cost;
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
}
