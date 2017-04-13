package it.polito.mad.team19.mad_expenses.Classes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import java.io.Serializable;

/**
 * Created by ikkoyeah on 11/04/17.
 */

public class UserContact implements Parcelable{
    private String name;
    private String email;
    private Bitmap thumb;
    private boolean checked;
    private int position;
    private boolean gone;
    ContactsCompletionView contactView;

    public UserContact(String n, String e, Bitmap t) { name = n; email = e; thumb = t; checked=false; gone=false;}


    public String getName() { return name; }
    public String getEmail() { return email; }
    public Bitmap getThumb() { return thumb; }


    public void setPosition(int pos) {position=pos;}
    public int getPosition(){return position;}

    public boolean isChecked() { return checked; }
    public void check() { checked=true; }
    public void uncheck() { checked=false; }
    public void setGone(boolean res) { gone=res; }
    public boolean isGone() { return gone; }




    @Override
    public String toString() { return name; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { this.email});
    }

}
