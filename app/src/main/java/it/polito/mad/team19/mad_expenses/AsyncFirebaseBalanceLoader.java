package it.polito.mad.team19.mad_expenses;

import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

import it.polito.mad.team19.mad_expenses.Classes.BalanceCalculator;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

/**
 * Created by Jured on 17/05/17.
 */

public class AsyncFirebaseBalanceLoader extends AsyncTask<Void, Integer, Long> {

    String groupId;
    String idExpense;
    ArrayList<FirebaseGroupMember> groupMembersList;
    float expenseTotal;
    ArrayList<FirebaseGroupMember> contributors;
    ArrayList<FirebaseGroupMember> excluded;

    public AsyncFirebaseBalanceLoader(String groupId, String idExpense, ArrayList<FirebaseGroupMember> groupMembersList, float expenseTotal, ArrayList<FirebaseGroupMember> contributors, ArrayList<FirebaseGroupMember> excluded) {
        this.groupId = groupId;
        this.idExpense = idExpense;
        this.groupMembersList = groupMembersList;
        this.expenseTotal = expenseTotal;
        this.contributors = contributors;
        this.excluded = excluded;
    }

    @Override
    protected Long doInBackground(Void... vo) {

        BalanceCalculator.calculate(this.groupId,
        this.idExpense,
        this.groupMembersList,
        this.expenseTotal,
        this.contributors,
        this.excluded);

        return null;
    }

}
