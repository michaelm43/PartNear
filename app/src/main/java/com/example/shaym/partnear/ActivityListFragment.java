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
import android.widget.ProgressBar;
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
import java.util.HashSet;
import java.util.Set;

import static com.example.shaym.partnear.Util.Constants.ERROR_DIALOG_REQUEST;
import static com.example.shaym.partnear.Util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.shaym.partnear.Util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class ActivityListFragment extends Fragment  implements View.OnClickListener,
    ActivityRecyclerAdapter.ActivityRecyclerClickListener {

    private static final String TAG = "ActivityListFragment";

    //widgets
    private ProgressBar mProgressBar;

    //vars
    private ArrayList<Activity> activities_list = new ArrayList<>();
    private ActivityRecyclerAdapter activityRecyclerAdapter;
    private RecyclerView activityRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private ListenerRegistration activityEventListener;

    private boolean mLocationPermissionGranted = false;

    private Context context;

    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_list, container, false);

        mAuth = FirebaseAuth.getInstance();

        activityRecyclerView = (RecyclerView) view.findViewById(R.id.activities_recycler_view);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        getActivity().findViewById(R.id.fab_create_activity).setOnClickListener(this);       //Create activity button


        getActivity().findViewById(R.id.fab_map).setOnClickListener(this);              //Open map button

        mDb = FirebaseFirestore.getInstance();

        //getActivities();

        initActivityRecyclerView();

        context = container.getContext();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);


        return view;
    }

    public void filterActivitiesByName(String newText){
        activityRecyclerAdapter.getFilter().filter(newText);
    }

    private void getActivities(){
        activities_list.clear();
        mDb.collection(getString(R.string.collection_activities)).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){//TODO fix dont do it every time or clean before?
                        Activity activity = doc.getDocument().toObject(Activity.class);
                        activities_list.add(activity);

                        activityRecyclerAdapter.notifyDataSetChanged();
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
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
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
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
            Toast.makeText(context, "You can't make map requests", Toast.LENGTH_SHORT).show();
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
    public void onActivitySelected(int position) {
        navActivityActivity(activities_list.get(position));
    }

    private void navActivityActivity(Activity activity){
        setInvisible();
        ActivityDetailFragment fragment = new ActivityDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("activity" ,activity);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        mProgressBar.setVisibility(View.GONE);
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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.fab_create_activity:{
                setInvisible();
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
                bundle.putParcelableArrayList(getString(R.string.intent_activity_list), activities_list);
                fragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
                transaction.replace(R.id.fragmentContainer, fragment, getString(R.string.fragment_activity_list));
                transaction.addToBackStack(getString(R.string.fragment_activity_list));
                transaction.commit();
                break;
            }
        }
    }
}
