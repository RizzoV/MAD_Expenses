package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupsAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Group;

public class GroupsListActivity extends AppCompatActivity {

    ListView groupListView;
    ArrayList<Group> groups = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //Create Group
            }
        });

        groupListView = (ListView) findViewById(R.id.groups_lv);

        for (Float i = Float.valueOf(1); i < 15 ; i++) {
            Group g = new Group("Group "+i, i*i, i, i.intValue());
            groups.add(g);
        }
        GroupsAdapter ga = new GroupsAdapter(GroupsListActivity.this, groups);
        groupListView.setAdapter(ga);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GroupsListActivity.this, GroupActivity.class);
                intent.putExtra("group", ((Group)parent.getItemAtPosition(position)).getName());
                startActivity(intent);

            }
        });
    }
}
