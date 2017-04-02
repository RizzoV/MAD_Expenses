package it.polito.mad.team19.mad_expenses.Adapters;


import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Proposal;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 31/03/17.
 */

public class ProposalsRecyclerAdapter extends RecyclerView.Adapter<ProposalsRecyclerAdapter.MyViewHolder> {

    ArrayList<Proposal> proposals;
    Activity context;
    private LayoutInflater mInflater;

    public ProposalsRecyclerAdapter(Context context, ArrayList<Proposal> proposals) {
        this.proposals = proposals;
        this.context = (Activity) context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.proposals_list_row, parent, false);
        MyViewHolder holder = new ProposalsRecyclerAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Proposal currentObj = proposals.get(position);
        holder.setData(currentObj,position);
    }


    @Override
    public int getItemCount() {
        return proposals.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        TextView description;
        TextView amount;
        int position;
        Proposal current;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.proposal_image);
            name = (TextView) itemView.findViewById(R.id.proposal_name_tv);
            amount = (TextView) itemView.findViewById(R.id.proposal_extimated_cost_amount_tv);
            //description = (TextView) itemView.findViewById(R.id.proposal_description_tv);
        }

        public void setData (Proposal current, int position) {
            this.current = current;
            this.name.setText(current.getName());
            this.amount.setText(String.format("%.2f", current.getExtimatedCost()) + current.getCurrency().getSymbol());
            //this.description.setText(current.getDescription());
            this.image.setImageResource(R.drawable.circle);
            this.position = position;



        }
    }
}
