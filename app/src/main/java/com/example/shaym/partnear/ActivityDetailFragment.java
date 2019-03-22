package com.example.shaym.partnear;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shaym.partnear.Logic.Activity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityDetailFragment extends Fragment {

    private TextView eventName;
    private TextView eventDate;
    private TextView eventTime;
    private TextView managerName;
    private TextView managerEmail;
    private TextView managerPhone;

    private FirebaseFirestore mDb;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_activity_detail_fragmant, container, false);
        Activity activity = new Activity();

        Bundle bundle = this.getArguments();
        if(bundle!=null){
            activity = bundle.getParcelable("activity");
        }

        eventName = (TextView) view.findViewById(R.id.event_title);
        eventTime = (TextView)view.findViewById(R.id.event_starting_time);
        eventDate = (TextView)view.findViewById(R.id.event_starting_date);
        managerName = (TextView) view.findViewById(R.id.manager_name);
        managerEmail = (TextView) view.findViewById(R.id.manager_email);
        managerPhone = (TextView) view.findViewById(R.id.manager_phone);

        eventName.setText(activity.getEventName());
        eventDate.setText(activity.getEvent_date());
        eventTime.setText(activity.getEvent_time());


        mDb = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        //Get Event manager information
        databaseReference.child(activity.user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                managerName.setText(String.valueOf(dataSnapshot.child("name").getValue()));
                managerEmail.setText(String.valueOf(dataSnapshot.child("email").getValue()));
                managerPhone.setText(String.valueOf(dataSnapshot.child("phone").getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}
