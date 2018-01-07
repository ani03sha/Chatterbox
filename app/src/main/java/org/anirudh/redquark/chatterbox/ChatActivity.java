package org.anirudh.redquark.chatterbox;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.anirudh.redquark.chatterbox.adapter.MessageListAdapter;
import org.anirudh.redquark.chatterbox.application.ChatterboxApplication;
import org.anirudh.redquark.chatterbox.model.Message;
import org.anirudh.redquark.chatterbox.receiver.ConnectivityReceiver;

public class ChatActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    private DatabaseReference databaseReference;

    // Declaring views
    private EditText messageET;

    /* Progress Bar */
    private ProgressBar progressBar;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Creating and setting the toolbar on the activity
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        // Setting up Navigational Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        drawerLayout.addDrawerListener(drawerToggle);

        // Creating an instance of Navigation Drawer
        NavigationView navigationView = findViewById(R.id.nvView);
        setupDrawerContent(navigationView);


        // setting up Firebase context
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        databaseReference = firebaseDatabase.child("messages");

        // Getting views from layout
        messageET = findViewById(R.id.message_text);
        ListView messageLV = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);

        MessageListAdapter<Message> adapter = new MessageListAdapter<Message>(databaseReference, Message.class, R.layout.message_row, this) {
            @Override
            protected void populateView(View v, Message model) {
                ((TextView) v.findViewById(R.id.username_text_view)).setText(model.getName());
                ((TextView) v.findViewById(R.id.message_text_view)).setText(model.getMessage());
            }
        };
        messageLV.setAdapter(adapter);
    }

    public void onSendButtonClick(View view) {
        if(checkConnection()) {
            String message = messageET.getEditableText().toString(); //message to be sent
            databaseReference.push().setValue(new Message(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), message));
            messageET.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    /**
     * @return ActionBarDrawerToggle
     */
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                Intent i1 = new Intent(ChatActivity.this, SettingsActivity.class);
                startActivity(i1);
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
        return isConnected;
    }

    private void showSnack(boolean isConnected) {
        String message;
        if (!isConnected) {
            message = "Sorry! Not connected to internet. Please check your internet connection";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        ChatterboxApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
