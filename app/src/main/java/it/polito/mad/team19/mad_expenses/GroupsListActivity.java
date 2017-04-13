package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupsAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Group;

public class GroupsListActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseLogged";
    ListView groupListView;
    ArrayList<Group> groups = new ArrayList<>();
    protected FirebaseAuth.AuthStateListener mAuthStateListener;
    protected FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        userLogVerification();

        setContentView(R.layout.activity_groups_list);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.groups_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupsListActivity.this, CreateGroupActivity.class);
                startActivity(i);
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

    private void userLogVerification()
    {
        mAuth=FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isAnonymous())
                {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_inGroup:" + user.getUid());
                }

                else
                    {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_outGroup");
                    Intent intent = new Intent(GroupsListActivity.this,GoogleSignInActivity.class);
                    startActivity(intent);
                }
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_grouplist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.account:
                Intent intent = new Intent(GroupsListActivity.this,AccountActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
