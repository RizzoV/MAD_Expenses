package it.polito.mad.team19.mad_expenses.Classes;

/**
 * Created by ikkoyeah on 12/05/17.
 */

public class  Topic {

    private String name;
    private String msg;
    private boolean me;
    private String date;

    public Topic(String name, String msg, String date, boolean me) {
        this.name = name;
        this.msg = msg;
        this.me = me;
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public String getName() {
        return name;
    }

    public boolean isMe() {
        return me;
    }

    public String getDate() {
        return date;
    }
}
