package it.polito.mad.team19.mad_expenses.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by ikkoyeah on 25/04/17.
 */
@IgnoreExtraProperties

public class FirebaseGroupMember implements Parcelable {
    private String uid;
    private String name;
    private String imgUrl;
    private Float isAdmin;

    //LUDO: per ceccare
    private boolean checked = false;

    public FirebaseGroupMember(String name, String imgUrl, String uid, Float isAdmin)
    {
        this.name = name;
        this.uid = uid;
        this.imgUrl = imgUrl;
        this.isAdmin = isAdmin;
    }

    public String getName(){return name;}
    public String getUid(){return uid;}
    public String getImgUrl(){return imgUrl;}

    public Float getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Float isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imgUrl);
        dest.writeString(uid);
    }

    public static final Parcelable.Creator<FirebaseGroupMember> CREATOR = new Parcelable.Creator<FirebaseGroupMember>() {
        public FirebaseGroupMember createFromParcel(Parcel in) {
            return new FirebaseGroupMember(in.readString(), in.readString(), in.readString(), 0f);
        }

        @Override
        public FirebaseGroupMember[] newArray(int size) {
            // TODO Auto-generated method stub
            return new FirebaseGroupMember[size];
        }
    };

    //LUDO: per ceccare
    public void check(boolean state)
    {
        checked = state;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public float isAdmin() { return isAdmin; }
}
