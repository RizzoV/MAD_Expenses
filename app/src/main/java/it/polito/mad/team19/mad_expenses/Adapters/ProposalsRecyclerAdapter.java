package it.polito.mad.team19.mad_expenses.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polito.mad.team19.mad_expenses.Classes.Proposal;
import it.polito.mad.team19.mad_expenses.R;
import it.polito.mad.team19.mad_expenses.TopicActivity;

/**
 * Created by Jured on 31/03/17.
 */

public class ProposalsRecyclerAdapter extends RecyclerView.Adapter<ProposalsRecyclerAdapter.MyViewHolder> {

    ArrayList<Proposal> proposals;
    Activity context;
    private LayoutInflater mInflater;

    //LUDO: aggiunto metodo onItemClickListener
    private ExpensesRecyclerAdapter.OnItemClickListener mItemClickListener;
    private ExpensesRecyclerAdapter.OnItemLongClickListener mItemLongClickListener;

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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image;
        TextView name;
        TextView description;
        TextView amount;
        Button proposal_topic;
        int position;
        Proposal current;
        View itemView;



        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.proposal_imageView);
            name = (TextView) itemView.findViewById(R.id.proposal_name_tv);
            amount = (TextView) itemView.findViewById(R.id.proposal_cost_amount_tv);
            description = (TextView) itemView.findViewById(R.id.proposal_description_tv);
            proposal_topic = (Button) itemView.findViewById(R.id.proposal_topic_button);

            proposal_topic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, TopicActivity.class);
                    i.putExtra("topicType","proposals");
                    i.putExtra("topicId","xxxxxxxx");
                    i.putExtra("topicName",current.getName());
                    context.startActivity(i);
                }
            });

            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        //LUDO: aggiunto metodo onItemClickListener
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, position);
            }
        }

        public void setData (Proposal current, int position) {
            this.current = current;
            this.name.setText(current.getName());
            this.amount.setText(current.getCurrency().getSymbol().toString() + " " + String.format("%.2f", current.getExtimatedCost()));
            this.description.setText(current.getDescription());
            this.image.setImageResource(R.mipmap.ic_proposals);
            this.position = position;


        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public interface  OnItemLongClickListener {
        void onItemLongClick(View view , int position);
    }

    //Jured: onItemLongClickListener
    public void SetOnItemClickListener(final ExpensesRecyclerAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void SetOnItemLongClickListener (final ExpensesRecyclerAdapter.OnItemLongClickListener mItemLongClickListener){
        this.mItemLongClickListener = mItemLongClickListener;
    }

    @Override
    public void onBindViewHolder(ProposalsRecyclerAdapter.MyViewHolder holder, final int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        holder.itemView.setLongClickable(true);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemLongClickListener.onItemLongClick(v, position);
                return false;
            }
        });
    }
}
