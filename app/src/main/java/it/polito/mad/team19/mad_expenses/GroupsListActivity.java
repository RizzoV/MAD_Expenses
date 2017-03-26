package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupsAdapter;

public class GroupsListActivity extends AppCompatActivity {

    ListView groupListView;
    ArrayList<Group> groups = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);

        groupListView = (ListView) findViewById(R.id.groups_lv);

        for (Float i = Float.valueOf(1); i < 4 ; i++) {
            Group g = new Group("group "+i, i*10+i, i, i.intValue());
            groups.add(g);
        }
        GroupsAdapter ga = new GroupsAdapter(GroupsListActivity.this, groups);
        groupListView.setAdapter(ga);
    }
}
