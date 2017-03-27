package it.polito.mad.team19.mad_expenses.Classes;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by Jured on 24/03/17.
 */

public class Group {

    String name;
    Bitmap icon;
    Float total;
    Float credits;
    Float debits;
    Integer notifyCnt;
    HashMap<String,GroupComponent> components;


    public Group(String name, Float credits, Float debits, Integer notifyCnt) {
        this.name = name;
        this.credits = credits;
        this.debits = debits;
        this.notifyCnt = notifyCnt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public Float getCredits() {
        return credits;
    }

    public void setCredits(Float credits) {
        this.credits = credits;
    }

    public Float getDebits() {
        return debits;
    }

    public void setDebits(Float debits) {
        this.debits = debits;
    }

    public Integer getNotifyCnt() {
        return notifyCnt;
    }

    public void setNotifyCnt(Integer notifyCnt) {
        this.notifyCnt = notifyCnt;
    }
}
