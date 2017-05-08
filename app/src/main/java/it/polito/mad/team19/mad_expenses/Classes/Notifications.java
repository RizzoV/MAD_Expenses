package it.polito.mad.team19.mad_expenses.Classes;

/**
 * Created by ikkoyeah on 08/05/17.
 */

public class Notifications
{

    private String desc;
    private String date;

    public Notifications(String desc, String date)
    {
        this.desc = desc;
        this.date = date;
    }


    public String getDesc() {
        return desc;
    }
    public String getDate() {
        return date;
    }

}
