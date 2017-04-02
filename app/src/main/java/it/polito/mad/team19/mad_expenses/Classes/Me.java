package it.polito.mad.team19.mad_expenses.Classes;

import java.util.Currency;

/**
 * Created by ikkoyeah on 31/03/17.
 */

public class Me {

    private String name;
    private Float amount;
    private Currency currency;

    public Me(String name, Float amount, Currency currency) {
        this.name = name;
        this.amount = amount;
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
