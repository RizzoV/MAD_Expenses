package it.polito.mad.team19.mad_expenses.Classes;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by ikkoyeah on 08/05/17.
 */
@IgnoreExtraProperties

public class Notifications
{

    private String activity;
    private String data;
    private String id;
    private String uid;
    private String uname;
    private String notKey;


    public Notifications(){}

    public Notifications(String activity, String data, String id, String uid, String uname)
    {
        this(activity,data,id,uid,uname,null);
    }

    public Notifications(String activity, String data, String id, String uid, String uname,String notKey)
    {
        this.activity = activity;
        this.data = data;
        this.id = id;
        this.uid = uid;
        this.uname = uname;
        this.notKey = notKey;

    }


    public String getData() {
        return data;
    }

    public String getActivity() {
        return activity;
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }
    public String getUname() {
        return uname;
    }

    @Override
    public String toString() {
        return getActivity()+" "+getData()+" "+getId()+" "+getUid()+" "+getUname();
    }


    public String getNotKey() {
        return notKey;
    }

    public boolean compareActivity(String obj) {

        boolean equals = true;

        String[] compare = obj.trim().split("");
        String[] temp = activity.trim().split("");

        Log.d("Notifi",compare.length+""+temp.length);

        if(compare.length!=temp.length)
            return false;
        else
        {
            for (int i = 0; i < temp.length - 1; i++) {
                if (compare[i] != temp[i]) {
                    equals = false;
                }
            }

            return equals;
        }
    }
}
