package it.polito.mad.team19.mad_expenses.Classes;

/**
 * Created by ikkoyeah on 31/03/17.
 */

public class Me {


    private String name;
    private Float amount;

    public Me(String name, Float amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float cost) {
        this.amount = cost;
    }
}
