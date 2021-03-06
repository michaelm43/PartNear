package com.example.shaym.partnear;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.shaym.partnear.Adapters.ActivityRecyclerAdapter;
import com.example.shaym.partnear.Logic.Activity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.example.shaym.partnear.Util.Constants.ACTIVITY_KEY;
import static com.example.shaym.partnear.Util.Constants.ERROR_DIALOG_REQUEST;
import static com.example.shaym.partnear.Util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.shaym.partnear.Util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.shaym.partnear.Util.Constants.collection_activities;
import static com.example.shaym.partnear.Util.Constants.intent_activity_list;

public class ActivityListFragment extends Fragment  implements View.OnClickListener,
    ActivityRecyclerAdapter.ActivityRecyclerClickListener, Spinner.OnItemSelectedListener {

    private static final String TAG = "ActivityListFragment";

    //widgets
    private Spinner mSpinner;
    private ImageButton mImageButton;

    //vars
    private ArrayList<Activity> activities_list = new ArrayList<>();
    private ActivityRecyclerAdapter activityRecyclerAdapter;
    private RecyclerView activityRecyclerView;
    private ArrayAdapter<CharSequence> adapter; // for spinner
    private Location userLocation;


    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private ListenerRegistration activityEventListener;

    private boolean mLocationPermissionGranted = false;

    private Context context;

    private FusedLocationProviderClient mFusedLocationClient;

    private int counter = 0;


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_list, container, false);

        mAuth = FirebaseAuth.getInstance();

        activityRecyclerView = (RecyclerView) view.findViewById(R.id.activities_recycler_view);

        mSpinner = (Spinner) view.findViewById(R.id.types_spinner);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.eventTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

        mImageButton = view.findViewById(R.id.btn_refresh);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterActivities("");
            }
        });

        getActivity().findViewById(R.id.fab_create_activity).setOnClickListener(this);       //Create activity button

        getActivity().findViewById(R.id.fab_map).setOnClickListener(this);              //Open map button

        mDb = FirebaseFirestore.getInstance();

        context = container.getContext();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        getLastKnownLocation();

        initActivityRecyclerView();

        return view;
    }

    public void filterActivities(String newText){
        if(!newText.equals("0")) {
            activityRecyclerAdapter.getFilter().filter(newText);
            if(!newText.matches("[0-9]+"))
                mSpinner.setSelection(0);
        }
    }

    private void getActivities(){
        activities_list.clear();
        mDb.collection(collection_activities).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Activity activity = doc.getDocument().toObject(Activity.class);
                            activities_list.add(activity);

                            activityRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
        Log.d(TAG, "Activities size: " + activities_list.size());
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    userLocation = task.getResult();
                    activityRecyclerAdapter.setUserLocation(userLocation);
                    GeoPoint geoPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d(TAG, "onComplete: longitude: " + geoPoint.getLongitude());
                }
            }
        });
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() { // Show a dialog to enable GPS
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.enable_gps)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){ // determine if the GPS is on or off
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getActivities();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){ // determine if there is Google Services on the device and is usable
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(context, R.string.cant_map, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { // What happened after the dialog permission
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getActivities(); // if accepted the GPS dialog
                    getLastKnownLocation();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    private void initActivityRecyclerView(){
        activityRecyclerAdapter = new ActivityRecyclerAdapter(activities_list, this);
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        activityRecyclerView.setAdapter(activityRecyclerAdapter);
        Log.d(TAG, "onComplete IN INIT ADAPTER");
    }

    @Override
    public void onResume() {
        super.onResume();
        setVisible();
        if(checkMapServices()){
            if(mLocationPermissionGranted) {
                getActivities();
                getLastKnownLocation();
            }
            else
                getLocationPermission();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        int hours,minutes,year,month,day;
        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hours = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);

        FirebaseFirestore deleteDb;
        deleteDb = FirebaseFirestore.getInstance();

        mDb.collection(collection_activities).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            Activity activity = doc.getDocument().toObject(Activity.class);

                            String[] dateParts = activity.event_date.split("/");
                            String[] timeParts = activity.event_time.split(":");

                            System.out.println("year:" + year + "-" + Integer.parseInt(dateParts[2]));
                            System.out.println("month:" + month + "-" + Integer.parseInt(dateParts[1]));
                            System.out.println("day:" + day + "-" + Integer.parseInt(dateParts[0]));
                            System.out.println("hours:" + hours + "-" + Integer.parseInt(timeParts[0]));
                            System.out.println("minute:" + minutes + "-" + Integer.parseInt(timeParts[1]));

                            if (year > Integer.parseInt(dateParts[2])) {        //check all dates field
                                deleteActivity(deleteDb, doc, '1');
                                System.out.println("1 if");
                            } else {
                                System.out.println("1 else");
                                if (month > Integer.parseInt(dateParts[1])
                                        && year == Integer.parseInt(dateParts[2])) {
                                    deleteActivity(deleteDb, doc, '2');
                                    System.out.println("2 if");
                                } else {
                                    System.out.println("2 else");
                                    if (day > Integer.parseInt(dateParts[0])
                                            && year == Integer.parseInt(dateParts[2])
                                            && month == Integer.parseInt(dateParts[1])) {
                                        deleteActivity(deleteDb, doc, '3');
                                        System.out.println("3 if");
                                    } else {
                                        System.out.println("3 else");
                                        if (hours > Integer.parseInt(timeParts[0])
                                                && year == Integer.parseInt(dateParts[2])
                                                && month == Integer.parseInt(dateParts[1])
                                                && day == Integer.parseInt(dateParts[0])) {
                                            deleteActivity(deleteDb, doc, '4');
                                            System.out.println("4 if");
                                        } else {
                                            System.out.println("4 else");
                                            if (minutes > Integer.parseInt(timeParts[1])
                                                    && year == Integer.parseInt(dateParts[2])
                                                    && month == Integer.parseInt(dateParts[1])
                                                    && day == Integer.parseInt(dateParts[0])
                                                    && hours == Integer.parseInt(timeParts[0])) {
                                                System.out.println("TASK IS TO OLD");
                                                System.out.println(doc.getDocument().getId());
                                                deleteActivity(deleteDb, doc, '5');
                                                System.out.println("5 if");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        });
    }

    public void deleteActivity(FirebaseFirestore deleteDb,DocumentChange doc,char ch){
        System.out.println("the char is "+ ch);
        deleteDb.collection(collection_activities)
                .document(doc.getDocument().getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("deleted task");
                        } else
                            System.out.println("deleted task");
                    }
                });
    }


    @Override
    public void onActivitySelected(int position) {
        navActivityActivity(activities_list.get(position));
    }

    private void navActivityActivity(Activity activity){
        setInvisible();
        ActivityDetailFragment fragment = new ActivityDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTIVITY_KEY ,activity);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(activityEventListener != null){
            activityEventListener.remove();
        }
    }

    public void setInvisible(){
        getActivity().findViewById(R.id.fab_create_activity).setVisibility(View.INVISIBLE);
        getActivity().findViewById(R.id.fab_map).setVisibility(View.INVISIBLE);
    }

    public void setVisible(){
        getActivity().findViewById(R.id.fab_create_activity).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.fab_map).setVisibility(View.VISIBLE);
        if(counter > 0){
            getActivity().findViewById(R.id.activity_search).setVisibility(View.VISIBLE);
        }
        counter++;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.fab_create_activity:{
                setInvisible();
                getActivity().findViewById(R.id.activity_search).setVisibility(View.INVISIBLE);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new CreateActivityFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            }
            case R.id.fab_map:{
                setInvisible();
                ActivityListAndMapFragment fragment = ActivityListAndMapFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(intent_activity_list, activities_list);
                fragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
                transaction.replace(R.id.fragmentContainer, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        filterActivities(position + "");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
