package it.polito.mad.team19.mad_expenses.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Currency;

/**
 * Created by ikkoyeah on 31/03/17.
 */

public class Me implements Parcelable {

    private String name;
    private Double amount;
    private Currency currency;
    private String ImgUrl;
    private String id;

    public Me(String id, String name, Double amount, Currency currency, String ImgUrl) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.ImgUrl = ImgUrl;
    }

    public String getId() {
        return id;
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

    public String getImgUrl() {
        return ImgUrl;
    }


    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double cost) {
        this.amount = cost;
    }

    public void addPartialAmount(Double amount) {
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(amount);
        dest.writeString(currency.getCurrencyCode());
        dest.writeString(ImgUrl);
    }

    public static final Parcelable.Creator<Me> CREATOR = new Parcelable.Creator<Me>() {
        public Me createFromParcel(Parcel in) {
            return new Me(in.readString(), in.readString(), in.readDouble(), Currency.getInstance(in.readString()),in.readString());
        }

        @Override
        public Me[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Me[size];
        }
    };
}
