package it.polito.mad.team19.mad_expenses.Classes;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by Jured on 24/03/17.
 */

public class Group
{

    String name;
    Bitmap icon;
    Float balance;
    Integer notifyCnt;
    String thumbPath;
    String groupId;
    //HashMap<String,GroupComponent> components;

    public Group(String name, Float balance, Integer notifyCnt, String groupId)
    {
        this(name, balance, notifyCnt, null, groupId);
    }


    public Group(String name, Float balance, Integer notifyCnt, String thumb, String groupId)
    {
        this.name = name;
        this.balance = balance;
        this.notifyCnt = notifyCnt;
        this.thumbPath = thumb;
        this.groupId = groupId;
    }

    //setter and getter methods

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

    public Float getBalance() {
        return balance;
    }

    public String getGroupId() {return groupId;}

    public String getImage() {return thumbPath;}


    public void setBalance(Float balance) {
        this.balance = balance;
    }


    public Integer getNotifyCnt() {
        return notifyCnt;
    }

    public void setNotifyCnt(Integer notifyCnt)
    {
        this.notifyCnt = notifyCnt;
    }
}
