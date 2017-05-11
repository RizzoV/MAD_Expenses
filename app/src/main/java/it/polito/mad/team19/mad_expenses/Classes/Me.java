package it.polito.mad.team19.mad_expenses.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Currency;

/**
 * Created by ikkoyeah on 31/03/17.
 */

public class Me implements Parcelable {

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

    public void addPartialAmount(Float amount) {
        this.amount += amount;
    }

    /* VALE
     * Classe resa parcellable in modo da passare un'array di Me da GroupActivity
     * all'activity sulle informazioni del gruppo per non riscaricare tutte le informazioni
     * su spese, debiti, crediti, etc.. da Firebase
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(amount);
        dest.writeString(currency.getCurrencyCode());
    }

    public static final Parcelable.Creator<Me> CREATOR = new Parcelable.Creator<Me>() {
        public Me createFromParcel(Parcel in) {
            return new Me(in.readString(), in.readFloat(), Currency.getInstance(in.readString()));
        }

        @Override
        public Me[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Me[size];
        }
    };
}
