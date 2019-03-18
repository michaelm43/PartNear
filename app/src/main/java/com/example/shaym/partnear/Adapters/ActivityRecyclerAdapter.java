package com.example.shaym.partnear.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shaym.partnear.Logic.Activity;
import com.example.shaym.partnear.R;

import java.util.ArrayList;

public class ActivityRecyclerAdapter extends RecyclerView.Adapter<ActivityRecyclerAdapter.ViewHolder>{
    private ArrayList<Activity> activities = new ArrayList<>();
    private ActivityRecyclerClickListener ActivityRecyclerClickListener;

    public ActivityRecyclerAdapter(ArrayList<Activity> activities, ActivityRecyclerClickListener activityRecyclerClickListener) {
        this.activities = activities;
        ActivityRecyclerClickListener = activityRecyclerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_acivity_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view, ActivityRecyclerClickListener);


        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((ViewHolder)holder).activityType.setText(activities.get(position).getType());
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener
    {
        TextView activityType;
        ActivityRecyclerClickListener clickListener;

        public ViewHolder(View itemView, ActivityRecyclerClickListener clickListener) {
            super(itemView);
            activityType = itemView.findViewById(R.id.activity_title);
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
}
