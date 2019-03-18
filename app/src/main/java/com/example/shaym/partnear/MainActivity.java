package com.example.shaym.partnear;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shaym.partnear.Adapters.ActivityRecyclerAdapter;
import com.example.shaym.partnear.Logic.Activity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        ActivityRecyclerAdapter.ActivityRecyclerClickListener {

    //widgets
    private ProgressBar mProgressBar;

    //vars
    private ArrayList<Activity> activities = new ArrayList<>();
    private Set<String> activitiesIDs = new HashSet<>();
    private ActivityRecyclerAdapter activityRecyclerAdapter;
    private RecyclerView activityRecyclerView;
    private Toolbar mainToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private ListenerRegistration activityEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //EditText et = (EditText) findViewById(R.id.etSearch);

        mAuth = FirebaseAuth.getInstance();

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        activityRecyclerView = (RecyclerView) findViewById(R.id.activities_recycler_view);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.fab_create_activity).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        initSupportActionBar();
        initActivityRecyclerView();
    }

    private void initSupportActionBar(){
        setTitle(R.string.app_name);
    }

    private void initActivityRecyclerView(){
        activityRecyclerAdapter = new ActivityRecyclerAdapter(activities, this);
        activityRecyclerView.setAdapter(activityRecyclerAdapter);
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
    protected void onResume() {
        super.onResume();
        getActivities();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.fab_create_activity:{
                newActivityDialog();
            }
        }
    }

    @Override
    public void onActivitySelected(int position) {
        navActivityActivity(activities.get(position));
    }

    private void navActivityActivity(Activity activity){
        Intent intent = new Intent(MainActivity.this, ActivityActivity.class);
        intent.putExtra(getString(R.string.intent_activity), activity);
        //startActivity(intent);
    }

    private void newActivityDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a Activity name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().equals("")){
                    buildNewActivity(input.getText().toString());
                }
                else {
                    Toast.makeText(MainActivity.this, "Enter an activity name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void getActivities(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        CollectionReference activitiesCollection = mDb
                .collection(getString(R.string.collection_activities));

        activityEventListener = activitiesCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //Log.d(TAG, "onEvent: called.");

                if (e != null) {
                    //Log.e("onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Activity activity = doc.toObject(Activity.class);
                        if(!activitiesIDs.contains(activity.getActivityID())){
                            activitiesIDs.add(activity.getActivityID());
                            activities.add(activity);
                        }
                    }
                    //Log.d("onEvent: number of chatrooms: " + activities.size());
                    activityRecyclerAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    private void buildNewActivity(String type){

        final Activity activity = new Activity(type);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newActivityRef = mDb
                .collection(getString(R.string.collection_activities))
                .document();

        activity.setActivityID(newActivityRef.getId());

        newActivityRef.set(activity).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideDialog();

                if(task.isSuccessful()){
                    //navActivityActivity(activity);
                }else{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(activityEventListener != null){
            activityEventListener.remove();
        }
    }
}
