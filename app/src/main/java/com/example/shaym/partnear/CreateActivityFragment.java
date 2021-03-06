package com.example.shaym.partnear;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.shaym.partnear.Logic.User;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.shaym.partnear.Util.Constants.collection_activities;

public class CreateActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "Create Activity";

    private Toolbar mainToolbar;
    private EditText eventName;
    private EditText eventTime;
    private EditText eventDate;
    private Button saveButton;
    private Button cancelButton;
    private Spinner spinner;

    ArrayAdapter<CharSequence> adapter;


    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;

    private String currentUserId;
    private int eventType;

    private GeoPoint location;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_activity, container, false);

        //mainToolbar = (Toolbar)view.findViewById(R.id.create_activity_toolbar);
        eventName = (EditText)view.findViewById(R.id.et_event_name);
        eventTime = (EditText)view.findViewById(R.id.time_text);
        eventDate = (EditText)view.findViewById(R.id.date_text);
        saveButton = (Button) view.findViewById(R.id.save_event_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_event_button);
        spinner = (Spinner) view.findViewById(R.id.types_spinner);

        adapter = ArrayAdapter.createFromResource(getContext(), R.array.eventTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.google_api_key));
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                location = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        eventDate.setOnClickListener(new View.OnClickListener() {
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
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String date = day + "/" + month + "/" +year;
                eventDate.setText(date);
            }
        };

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour, minutes;

                if(TextUtils.isEmpty(eventTime.getText().toString())) {
                    Calendar calander = Calendar.getInstance();
                    hour = calander.get(Calendar.HOUR_OF_DAY);
                    minutes = calander.get(Calendar.MINUTE);
                }
                else {
                    String[] timeParts = eventTime.getText().toString().split(":");
                    hour = Integer.parseInt(timeParts[0]);
                    minutes = Integer.parseInt(timeParts[1]);
                }

                TimePickerDialog dialog = new TimePickerDialog(getContext(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mTimeSetListener,
                        hour,minutes,
                        android.text.format.DateFormat.is24HourFormat(getActivity()));

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                String time;
                if(minutes<10)
                    time = hours + ":0" + minutes;
                else
                    time = hours + ":" + minutes;
                eventTime.setText(time);
            }
        };

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String activityName = eventName.getText().toString();
                String activityDate = eventDate.getText().toString();
                String activityTime = eventTime.getText().toString();

                if(!TextUtils.isEmpty(activityName) && !TextUtils.isEmpty(activityDate) && !TextUtils.isEmpty(activityTime) && eventType != 0 && location != null){

                    buildNewActivity();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragmentContainer, new ActivityListFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                else{
                    Toast.makeText(getContext(),R.string.activity_fail,Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new ActivityListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        return view;
    }


    private void buildNewActivity(){


        Map<String,Object> eventMap = new HashMap<>();
        eventMap.put("user_id", currentUserId);
        eventMap.put("event_date", eventDate.getText().toString());
        eventMap.put("event_time", eventTime.getText().toString());
        eventMap.put("eventName", eventName.getText().toString());
        eventMap.put("timestamp", FieldValue.serverTimestamp());
        eventMap.put("location", location);
        eventMap.put("eventType", eventType);

        mDb.collection(collection_activities).add(eventMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if(task.isSuccessful()){
                    Toast.makeText(getContext(),R.string.activity_success,Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        eventType = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO WHAT HAPPENED IF NOT SELECTED
    }
}