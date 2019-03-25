package com.example.shaym.partnear.Adapters;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shaym.partnear.Logic.Activity;
import com.example.shaym.partnear.R;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class ActivityRecyclerAdapter extends RecyclerView.Adapter<ActivityRecyclerAdapter.ViewHolder> implements Filterable {

    private ArrayList<Activity> activities_list;
    private ArrayList<Activity> activities_list_full;
    private ActivityRecyclerClickListener ActivityRecyclerClickListener;
    private Location userLocation;

    int counter = 0;
    View view;

    public ActivityRecyclerAdapter(ArrayList<Activity> activities, ActivityRecyclerClickListener activityRecyclerClickListener) {
        this.activities_list = activities;
        ActivityRecyclerClickListener = activityRecyclerClickListener;
        activities_list_full = new ArrayList<>();
        activities_list_full.addAll(activities_list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_acivity_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view, ActivityRecyclerClickListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activities_list.get(position);
        (holder).eventName.setText(activity.getEventName());
        String type = view.getResources().getStringArray(R.array.eventTypes)[activity.getEventType()];
        (holder).eventType.setText(type);
        Location tmp = new Location("");
        tmp.setLatitude(activity.getLocation().getLatitude());
        tmp.setLongitude(activity.getLocation().getLongitude());
        if(userLocation != null)
            (holder).distance.setText(userLocation.distanceTo(tmp)/1000 + "km");
        (holder).eventImage.setImageResource(activity.getImage_id());

    }

    @Override
    public int getItemCount() {
        return activities_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener
    {
        View mView;
        TextView eventName;
        TextView eventType;
        TextView distance;
        ImageView eventImage;

        ActivityRecyclerClickListener clickListener;

        public ViewHolder(View itemView, ActivityRecyclerClickListener clickListener) {
            super(itemView);
            mView = itemView;
            eventName = itemView.findViewById(R.id.activity_title);
            eventType = itemView.findViewById(R.id.activity_type);
            distance = itemView.findViewById(R.id.activity_distance);
            eventImage = itemView.findViewById(R.id.activity_image);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onActivitySelected(getAdapterPosition());
        }
    }

    public interface ActivityRecyclerClickListener {
        public void onActivitySelected(int position);
    }

    @Override
    public Filter getFilter() {
        if(counter == 0){
            activities_list_full = new ArrayList<>(activities_list);
            counter++;
        }
        return activities_Filter;
    }

    private Filter activities_Filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Activity> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0)
                filteredList.addAll(activities_list_full);
            else {
                String str = constraint.toString();
                if(str.matches("[0-9]+") && str.length() <= 2){
                    for(Activity activity : activities_list_full){
                        if(activity.getEventType() == Integer.parseInt(str))
                            filteredList.add(activity);
                    }
                }
                else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Activity activity : activities_list_full) {
                        if (activity.getEventName().toLowerCase().contains(filterPattern))
                            filteredList.add(activity);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            activities_list.clear();
            activities_list.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

    public ArrayList<Activity> getActivities_list() {
        return activities_list;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

}