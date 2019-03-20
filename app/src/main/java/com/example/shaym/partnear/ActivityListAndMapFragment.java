package com.example.shaym.partnear;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shaym.partnear.Adapters.ActivityRecyclerAdapter;
import com.example.shaym.partnear.Logic.Activity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;

import static com.example.shaym.partnear.Util.Constants.MAPVIEW_BUNDLE_KEY;

public class ActivityListAndMapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener,
        ActivityRecyclerAdapter.ActivityRecyclerClickListener {
    private static final String TAG = "ActivityListAndMapFragment";

    //widgets
    private RecyclerView mActivityListRecyclerView;
    private MapView mMapView;


    //vars
    private ArrayList<Activity> mActivityList = new ArrayList<>();
    private ActivityRecyclerAdapter mActivityRecyclerAdapter;


    public static ActivityListAndMapFragment newInstance() {
        return new ActivityListAndMapFragment();
    }

    @Nullable
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityList = getArguments().getParcelableArrayList(getString(R.string.intent_activity_list));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_list_and_map, container, false);
        mActivityListRecyclerView = view.findViewById(R.id.activity_list_recycler_view);
        mMapView = view.findViewById(R.id.activity_list_map);

        initActivityRecyclerView();
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

    private void initActivityRecyclerView(){
        mActivityRecyclerAdapter = new ActivityRecyclerAdapter(mActivityList, this);
        mActivityListRecyclerView.setAdapter(mActivityRecyclerAdapter);
        mActivityListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

    @Override
    public void onActivitySelected(int position) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_map:{
                final FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new ActivityListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }
}