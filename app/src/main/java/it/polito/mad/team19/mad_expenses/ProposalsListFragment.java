package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpensesRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Adapters.ProposalsRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseProposal;
import it.polito.mad.team19.mad_expenses.Classes.Proposal;

/**
 * Created by Valentino on 22/05/2017.
 */

public class ProposalsListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public ProposalsListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ProposalsListFragment newInstance(int sectionNumber) {
        ProposalsListFragment fragment = new ProposalsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        final ArrayList<Proposal> proposals = new ArrayList<Proposal>();

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.proposals_rv);
        final ProposalsRecyclerAdapter adapter = new ProposalsRecyclerAdapter(getActivity(), proposals, getActivity().getIntent().getStringExtra("groupId"));
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        adapter.SetOnItemClickListener(new ExpensesRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Proposal clicked = proposals.get(position);
                final Intent intent = new Intent(getActivity(), ProposalDetailsActivity.class);
                Log.d("Expenses", clicked.toString());
                intent.putExtra("ProposalName", clicked.getName());
                //intent.putExtra("ProposalImgUrl", clicked.getImage());
                intent.putExtra("ProposalDesc", clicked.getDescription());
                intent.putExtra("ProposalCost", String.format(Locale.getDefault(), "%.2f", clicked.getExtimatedCost()));
                //intent.putExtra("ProposalAuthorId", clicked.getAuthor());
                intent.putExtra("groupId", getActivity().getIntent().getStringExtra("groupId"));
                //intent.putExtra("ProposalId", clicked.getFirebaseId());
                startActivity(intent);
            }
        });


        final TextView noproposalstv = (TextView) rootView.findViewById(R.id.noproposals_tv);


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(getActivity().getIntent().getStringExtra("groupId")).child("proposals");


        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    noproposalstv.setVisibility(View.GONE);
                    //Ludo: ogni volta che si ricrea la lista, prima bisogna svuotarla per non avere elementi doppi
                    proposals.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        FirebaseProposal fp = child.getValue(FirebaseProposal.class);
                        proposals.add(new Proposal(fp.getName(), fp.getDescription(), fp.getCost(), null, Currency.getInstance("EUR"),child.getKey()));
                        //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                        adapter.notifyDataSetChanged();


                        //pBar.setVisibility(View.GONE);
                    }
                } else {
                    //pBar.setVisibility(View.GONE);
                    noproposalstv.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //RecyclerView expensesList = (RecyclerView) rootView.findViewById(R.id.expenses_lv);
        //expensesList.setAdapter(adapter);

        //final LinearLayout meCardsViewLayout = (LinearLayout) rootView.findViewById(R.id.cards);

        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

            /*fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Intent i = new Intent(getActivity(), AddExpenseActivity.class);
                    //startActivity(i);
                }
            });*/

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });

        return rootView;
    }
}
