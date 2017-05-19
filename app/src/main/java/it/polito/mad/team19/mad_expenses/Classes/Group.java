package it.polito.mad.team19.mad_expenses.Classes;

import android.graphics.Bitmap;


/**
 * Created by Jured on 24/03/17.
 */

public class Group
{

    private String name;
    private Bitmap icon;
    private Float credit;
    private Float debt;
    private Integer notifyCnt;
    private  String thumbPath;
    private  String groupId;
    //HashMap<String,GroupComponent> components;

    public Group(String name, Float credit, Float debt, Integer notifyCnt, String groupId)
    {
        this(name, credit, debt, notifyCnt, null, groupId);
    }


    public Group(String name, Float credit, Float debt, Integer notifyCnt, String thumb, String groupId)
    {
        this.name = name;
        this.credit = credit;
        this.debt = debt;
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

    public Float getCredit() {
        return credit;
    }

    public Float getDebt() {
        return debt;
    }

    public String getGroupId() {return groupId;}

    public String getImage() {return thumbPath;}


    public void setBalance(Float balance) {
        this.credit = balance;
    }


    public Integer getNotifyCnt() {
        return notifyCnt;
    }

    public void setNotifyCnt(Integer notifyCnt)
    {
        this.notifyCnt = notifyCnt;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
}
