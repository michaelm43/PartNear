package com.example.shaym.partnear;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";

    private Toolbar mainToolbar;
    SearchView searchView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragmentContainer) != null) {
            if (savedInstanceState != null) {
                return;
            }
            ActivityListFragment firstFragment = new ActivityListFragment();

            firstFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, firstFragment).commit();
        }

        mAuth = FirebaseAuth.getInstance();

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        initSupportActionBar();
    }

    private void initSupportActionBar(){
        setTitle(R.string.app_name);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToLogin();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.activity_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout_btn:

                logOut();
                return true;

            case R.id.action_settings_btn:

                Intent settingsIntent = new Intent(MainActivity.this,SetupActivity.class);
                MainActivity.this.startActivity(settingsIntent);
                return true;


                default:
                    return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        MainActivity.this.startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if(currentFragment instanceof ActivityListAndMapFragment) {
            ((ActivityListAndMapFragment) currentFragment).addMapMarkers();
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if(currentFragment instanceof ActivityListFragment) {
            ((ActivityListFragment) currentFragment).filterActivities(newText);
        }
        if(currentFragment instanceof ActivityListAndMapFragment) {
            ((ActivityListAndMapFragment) currentFragment).filterActivitiesByName(newText);
        }
        return true;
    }
}
