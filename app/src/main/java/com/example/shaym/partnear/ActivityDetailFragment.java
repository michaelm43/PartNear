package com.example.shaym.partnear;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shaym.partnear.Logic.Activity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.GeoApiContext;

import static com.example.shaym.partnear.Util.Constants.MAPVIEW_BUNDLE_KEY;

public class ActivityDetailFragment extends Fragment implements OnMapReadyCallback {

    private TextView eventName;
    private TextView eventDate;
    private TextView eventTime;
    private TextView managerName;
    private TextView managerEmail;
    private TextView managerPhone;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    private FirebaseFirestore mDb;
    private DatabaseReference databaseReference;

    Activity activity = new Activity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_activity_detail_fragmant, container, false);

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
        mMapView = view.findViewById(R.id.activity_map);

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

        initGoogleMap(savedInstanceState);

        return view;
    }

    private void initGoogleMap(Bundle savedInstanceState){
        // * IMPORTANT *
        // MapView requires that the Bundle you pass contain ONLY MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        MapsInitializer.initialize(getContext());
        map.setMapType(map.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        LatLng latLng = new LatLng(activity.getLocation().getLatitude(), activity.getLocation().getLongitude());
        map.addMarker(new MarkerOptions().position(latLng)
                .title(activity.getEventName()));
        CameraPosition cameraPosition = CameraPosition.builder().target(latLng).zoom(16).bearing(0).build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
