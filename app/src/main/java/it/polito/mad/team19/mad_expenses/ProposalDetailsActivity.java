package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

public class ProposalDetailsActivity extends AppCompatActivity {

    private String desc;
    private String name;
    private String cost;
    private TextView author_tv;
    private TextView cost_tv;
    private  TextView desc_tv;
    private  TextView name_tv;
    private CardView cw_topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_details);

        cost = getIntent().getStringExtra("ProposalCost");
        name = getIntent().getStringExtra("ProposalName");
        desc = getIntent().getStringExtra("ProposalDesc");

        cw_topic = (CardView) findViewById(R.id.proposal_topic_cw);
        author_tv = (TextView) findViewById(R.id.proposal_author_value);
        cost_tv = (TextView) findViewById(R.id.proposal_cost);
        desc_tv = (TextView) findViewById(R.id.proposal_description);
        name_tv = (TextView) findViewById(R.id.proposal_name);

        desc_tv.setText(desc);
        cost_tv.setText(cost);
        name_tv.setText(name);

        cw_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProposalDetailsActivity.this, TopicActivity.class);
                i.putExtra("topicType","proposals");
                i.putExtra("topicId","xxxxxxxx");
                i.putExtra("topicName",name);
                i.putExtra("ProposalInfoIntent","true");
                startActivity(i);
            }
        });




        getSupportActionBar().setTitle("Dettagli Proposta");


    }
}
