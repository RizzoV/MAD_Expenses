package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.ExpenseDetailsActivity;
import it.polito.mad.team19.mad_expenses.GroupInfoActivity;
import it.polito.mad.team19.mad_expenses.ProposalDetailsActivity;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 08/05/17.
 */

public class NotificationsAdapter extends BaseAdapter {

    ArrayList<DataSnapshot> notitifcationsList;
    Activity context;
    String myNot;
    boolean newNot = false;

    public NotificationsAdapter(Context context, ArrayList<DataSnapshot> notificationsList, String myNot) {
        this.notitifcationsList = notificationsList;
        this.context = (Activity) context;
        this.myNot = myNot;
    }

    @Override
    public int getCount() {
        return notitifcationsList.size();
    }

    @Override
    public Object getItem(int position) {
        return notitifcationsList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.layout_notifications_list_row,parent,false);
        }

        LinearLayout ll = (LinearLayout)convertView.findViewById(R.id.not_container);
        TextView text=(TextView)convertView.findViewById(R.id.notification_text);
        TextView uname = (TextView) convertView.findViewById(R.id.uname); 
        TextView date=(TextView)convertView.findViewById(R.id.notification_date);
        TextView time = (TextView) convertView.findViewById(R.id.notification_time);
        
        final DataSnapshot notification=notitifcationsList.get(notitifcationsList.size()-1-position);

        uname.setText(notification.child("uname").getValue().toString());

        if(notification.getKey().equals(myNot))
            newNot=true;

        if(!newNot)
            ll.setBackgroundColor(Color.WHITE);

        String[] notDate = notification.child("data").getValue().toString().split("-");
        date.setText(notDate[0] + " " + getStringMonth(Integer.parseInt(notDate[1])));

        if(notDate.length<5)
            time.setVisibility(View.GONE);
        else {
            time.setVisibility(View.VISIBLE);
            time.setText(notDate[3]+":"+notDate[4]);
        }



        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationAddExpenseActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationAddExpenseText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ExpenseDetailsActivity.class);
                    intent.putExtra("ExpenseName",notification.child("ExpenseName").getValue().toString());
                    intent.putExtra("ExpenseDesc",notification.child("ExpenseDesc").getValue().toString());
                    intent.putExtra("ExpenseAuthorId",notification.child("ExpenseAuthorId").getValue().toString());
                    if(notification.child("ExpenseImgUrl").getValue()!=null)
                        intent.putExtra("ExpenseImgUrl",notification.child("ExpenseImgUrl").getValue().toString());
                    intent.putExtra("ExpenseCost",notification.child("ExpenseCost").getValue().toString());
                    intent.putExtra("ExpenseId",notification.child("id").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });
        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationModifiedExpenseActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationModifiedExpenseText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ExpenseDetailsActivity.class);
                    intent.putExtra("ExpenseName",notification.child("ExpenseName").getValue().toString());
                    intent.putExtra("ExpenseDesc",notification.child("ExpenseDesc").getValue().toString());
                    intent.putExtra("ExpenseAuthorId",notification.child("ExpenseAuthorId").getValue().toString());
                    if(notification.child("ExpenseImgUrl").getValue()!=null)
                        intent.putExtra("ExpenseImgUrl",notification.child("ExpenseImgUrl").getValue().toString());
                    intent.putExtra("ExpenseCost",notification.child("ExpenseCost").getValue().toString());
                    intent.putExtra("ExpenseId",notification.child("id").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });
        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationDenyPayedDebtActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationDenyPayedDebtText));

        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationAddExpenseFromProposalActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationAddExpenseFromProposalText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ExpenseDetailsActivity.class);
                    intent.putExtra("ExpenseName",notification.child("ExpenseName").getValue().toString());
                    intent.putExtra("ExpenseDesc",notification.child("ExpenseDesc").getValue().toString());
                    intent.putExtra("ExpenseAuthorId",notification.child("ExpenseAuthorId").getValue().toString());
                    if(notification.child("ExpenseImgUrl").getValue()!=null)
                        intent.putExtra("ExpenseImgUrl",notification.child("ExpenseImgUrl").getValue().toString());
                    intent.putExtra("ExpenseCost",notification.child("ExpenseCost").getValue().toString());
                    intent.putExtra("ExpenseId",notification.child("id").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });
        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationChangedExpenseBalancectivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationChangedExpenseBalanceText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ExpenseDetailsActivity.class);
                    intent.putExtra("ExpenseName",notification.child("ExpenseName").getValue().toString());
                    intent.putExtra("ExpenseDesc",notification.child("ExpenseDesc").getValue().toString());
                    intent.putExtra("ExpenseAuthorId",notification.child("ExpenseAuthorId").getValue().toString());
                    if(notification.child("ExpenseImgUrl").getValue()!=null)
                        intent.putExtra("ExpenseImgUrl",notification.child("ExpenseImgUrl").getValue().toString());
                    intent.putExtra("ExpenseCost",notification.child("ExpenseCost").getValue().toString());
                    intent.putExtra("ExpenseId",notification.child("id").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });
        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationAddProposalActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationAddProposalText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProposalDetailsActivity.class);
                    intent.putExtra("ProposalName",notification.child("ProposalName").getValue().toString());
                    intent.putExtra("ProposalCost",notification.child("ProposalCost").getValue().toString());
                    intent.putExtra("ProposalDesc",notification.child("ProposalDesc").getValue().toString());
                    intent.putExtra("ProposalId",notification.child("id").getValue().toString());
                    intent.putExtra("ProposalAuthorId",notification.child("uid").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });

        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationAddGroupActivity))) {
            text.setText(context.getResources().getString(R.string.notififcationAddGroupText));

        }
        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationAddMembersToGroupActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationAddMembersToGroupText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(context,GroupInfoActivity.class);
                    String image = null;
                    if(notification.child("GroupImage").getValue()!=null)
                        image = notification.child("GroupImage").getValue().toString();
                    intent.putExtra("groupImage",image);
                    intent.putExtra("groupName",notification.child("GroupName").getValue().toString());
                    intent.putExtra("groupId",notification.child("id").getValue().toString());
                    context.startActivity(intent);
                }
            });

        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationRemoveMembersToGroupActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationRemoveMembersToGroupText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(context,GroupInfoActivity.class);
                    String image = null;
                    if(notification.child("GroupImage").getValue()!=null)
                        image = notification.child("GroupImage").getValue().toString();
                    intent.putExtra("groupImage",image);
                    intent.putExtra("groupName",notification.child("GroupName").getValue().toString());
                    intent.putExtra("groupId",notification.child("id").getValue().toString());
                    context.startActivity(intent);
                }
            });

        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationAcceptedProposalActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationAcceptedProposalText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProposalDetailsActivity.class);
                    intent.putExtra("ProposalName",notification.child("ProposalName").getValue().toString());
                    intent.putExtra("ProposalCost",notification.child("ProposalCost").getValue().toString());
                    intent.putExtra("ProposalDesc",notification.child("ProposalDesc").getValue().toString());
                    intent.putExtra("ProposalId",notification.child("id").getValue().toString());
                    intent.putExtra("ProposalAuthorId",notification.child("uid").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });

        }

        if(notification.child("activity").getValue().toString().equals(context.getResources().getString(R.string.notififcationDenyProposalActivity)))
        {
            text.setText(context.getResources().getString(R.string.notififcationDenyProposalText));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProposalDetailsActivity.class);
                    intent.putExtra("ProposalName",notification.child("ProposalName").getValue().toString());
                    intent.putExtra("ProposalCost",notification.child("ProposalCost").getValue().toString());
                    intent.putExtra("ProposalDesc",notification.child("ProposalDesc").getValue().toString());
                    intent.putExtra("ProposalId",notification.child("id").getValue().toString());
                    intent.putExtra("ProposalAuthorId",notification.child("uid").getValue().toString());
                    intent.putExtra("groupId",notification.child("groupId").getValue().toString());
                    context.startActivity(intent);
                }
            });

        }

        return convertView;
    }

    private String getStringMonth(int s)
    {
        switch (s)
        {
            case 1:
                return context.getResources().getString(R.string.january);
            case 2:
                return context.getResources().getString(R.string.february);
            case 3:
                return context.getResources().getString(R.string.march);
            case 4:
                return context.getResources().getString(R.string.april);
            case 5:
                return context.getResources().getString(R.string.may);
            case 6:
                return context.getResources().getString(R.string.june);
            case 7:
                return context.getResources().getString(R.string.july);
            case 8:
                return context.getResources().getString(R.string.august);
            case 9:
                return context.getResources().getString(R.string.september);
            case 10:
                return context.getResources().getString(R.string.october);
            case 11:
                return context.getResources().getString(R.string.november);
            case 12:
                return context.getResources().getString(R.string.december);
            default:
                return "NNN";

        }

    }

}
