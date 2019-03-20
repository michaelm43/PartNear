package com.example.shaym.partnear;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shaym.partnear.Logic.Activity;
import com.example.shaym.partnear.Logic.Upload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class CreateActivityFragment extends Fragment {

    private Toolbar mainToolbar;
    private EditText eventName;
    private EditText eventTime;
    private Button saveButton;
    private Button cancelButton;

    private ArrayList<Activity> activities = new ArrayList<>();
    private Set<String> activitiesIDs = new HashSet<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private DatabaseReference mDatabaseRef;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private Upload user;



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_activity, container, false);

        //mainToolbar = (Toolbar)view.findViewById(R.id.create_activity_toolbar);
        eventName = (EditText)view.findViewById(R.id.et_event_name);
        eventTime = (EditText)view.findViewById(R.id.time_text);
        saveButton = (Button) view.findViewById(R.id.save_event_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_event_button);

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calander = Calendar.getInstance();
                int year = calander.get(calander.YEAR);
                int month = calander.get(calander.MONTH);
                int day = calander.get(calander.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                        /*CreateActivityFragment.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);*/
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String date = day + "/" + month + "/" +year;
                eventTime.setText(date);
            }
        };

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(Upload.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eventName.toString().trim().equals("") || eventTime.toString().trim().equals("")){
                    buildNewActivity();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragmentContainer, new ActivityListFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });




        return view;
    }


    private void buildNewActivity(){

        final Activity activity = new Activity(eventName.toString(),user ,eventTime.toString());

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

                if(task.isSuccessful()){
                    //navActivityActivity(activity);
                }else{
                    View parentLayout = getView().findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
