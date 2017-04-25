package it.polito.mad.team19.mad_expenses.Classes;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by ikkoyeah on 25/04/17.
 */
@IgnoreExtraProperties

public class FirebaseGroupMembers {
    private String uid;
    private String name;
    private String imgUrl;

    public FirebaseGroupMembers () {}

    public FirebaseGroupMembers(String name, String imgUrl, String uid)
    {
        this.name = name;
        this.uid = uid;
        this.imgUrl = imgUrl;
    }

    public String getName(){return name;}
    public String getUid(){return uid;}
    public String getImgUrl(){return imgUrl;}

}
