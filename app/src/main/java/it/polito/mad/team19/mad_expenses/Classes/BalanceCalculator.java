package it.polito.mad.team19.mad_expenses.Classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Valentino on 20/05/2017.
 */

public class BalanceCalculator {

    public static void calculate(String groupId, String idExpense, ArrayList<FirebaseGroupMember> groupMembersList, float expenseTotal, ArrayList<FirebaseGroupMember> contributors, ArrayList<FirebaseGroupMember> excluded) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        for (FirebaseGroupMember groupMember : groupMembersList) {
            Boolean stop = Boolean.FALSE;
            for (FirebaseGroupMember excludedMember : excluded) {
                if (groupMember.getUid().equals(excludedMember.getUid())) {
                    stop = Boolean.TRUE;
                    break;
                }
            }

            for (FirebaseGroupMember contributor : contributors) {
                if (groupMember.getUid().equals(contributor.getUid())) {
                    stop = Boolean.TRUE;
                    break;
                }
            }

            if (stop)
                continue;

            for (FirebaseGroupMember contributor : contributors) {
                DatabaseReference debtorRef = database.getReference("gruppi").child(groupId).child("expenses").child(idExpense)
                        .child("debtors").child(groupMember.getUid());
                debtorRef.child("riepilogo").child(contributor.getUid()).child("amount").setValue(String.format(Locale.getDefault(), "%.2f",
                        -(expenseTotal / contributors.size() / (groupMembersList.size() - excluded.size()))).replace(",", "."));
                debtorRef.child("nome").setValue(groupMember.getName());

                DatabaseReference creditorRef = database.getReference("gruppi").child(groupId).child("expenses").child(idExpense)
                        .child("contributors").child(contributor.getUid());

                if (groupMember.getImgUrl() != null) {
                    debtorRef.child("immagine").setValue(groupMember.getImgUrl());
                    creditorRef.child("riepilogo").child(groupMember.getUid()).child("immagine").setValue(groupMember.getImgUrl());
                }

                debtorRef.child("riepilogo").child(contributor.getUid()).child("nome").setValue(contributor.getName());

                creditorRef.child("riepilogo").child(groupMember.getUid()).child("amount").setValue(String.format(Locale.getDefault(), "%.2f",
                        +(expenseTotal / contributors.size() / (groupMembersList.size() - excluded.size()))).replace(",", "."));
                creditorRef.child("nome").setValue(contributor.getName());
                creditorRef.child("riepilogo").child(groupMember.getUid()).child("nome").setValue(groupMember.getName());

                if (contributor.getImgUrl() != null) {
                    creditorRef.child("immagine").setValue(contributor.getImgUrl());
                    debtorRef.child("riepilogo").child(contributor.getUid()).child("immagine").setValue(contributor.getImgUrl());
                }
            }
        }
    }
}
