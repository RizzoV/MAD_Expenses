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
    private static final int PROPOSAL_DETAILS = 1;

    private ArrayList<Proposal> proposals = new ArrayList<Proposal>();
    private Proposal clicked;
    private ProposalsRecyclerAdapter adapter;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PROPOSAL_DETAILS) {
            FirebaseDatabase.getInstance().getReference().child("gruppi").child(getActivity().getIntent().getStringExtra("groupId")).child("proposals").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChildren()) {
                        proposals.clear();
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_proposals, container, false);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.proposals_rv);
        adapter = new ProposalsRecyclerAdapter(getActivity(), proposals, getActivity().getIntent().getStringExtra("groupId"));
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        adapter.SetOnItemClickListener(new ExpensesRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                clicked = proposals.get(position);
                final Intent intent = new Intent(getActivity(), ProposalDetailsActivity.class);
                Log.d("Expenses", clicked.toString());
                intent.putExtra("ProposalName", clicked.getName());
                intent.putExtra("ProposalImgUrl", clicked.getImageUrl());
                intent.putExtra("ProposalDesc", clicked.getDescription());
                intent.putExtra("ProposalCost", String.format(Locale.getDefault(), "%.2f", clicked.getExtimatedCost()));
                intent.putExtra("ProposalAuthorId", clicked.getAuthor());
                intent.putExtra("groupId", getActivity().getIntent().getStringExtra("groupId"));
                intent.putExtra("ProposalId", clicked.getFirebaseId());
                startActivityForResult(intent, PROPOSAL_DETAILS);
            }
        });

        final TextView noProposals_tv = (TextView) rootView.findViewById(R.id.noproposals_tv);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference proposalsRef = database.getReference("gruppi").child(getActivity().getIntent().getStringExtra("groupId")).child("proposals");
        proposalsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    noProposals_tv.setVisibility(View.GONE);
                    //Ludo: ogni volta che si ricrea la lista, prima bisogna svuotarla per non avere elementi doppi
                    proposals.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        FirebaseProposal fp = child.getValue(FirebaseProposal.class);
                        proposals.add(0, new Proposal(fp.getName(), fp.getDescription(), fp.getAuthor(), fp.getCost(), fp.getImageUrl(), Currency.getInstance("EUR"), child.getKey()));

                        //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    noProposals_tv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalsListFragment", "Could not retrieve the list of proposals");
            }
        });


        // Make the fab appear/disappear based on crolling
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

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
