package com.example.projectexpensetracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private List<Project> projectList;
    private OnProjectListener onProjectListener;
    private SimpleDateFormat dateFormat;

    public ProjectAdapter(List<Project> projectList, OnProjectListener onProjectListener) {
        this.projectList = projectList;
        this.onProjectListener = onProjectListener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(view, onProjectListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);

        // Set text fields
        holder.projectTextView.setText(project.getProjectName());
        holder.projectIdTextView.setText(project.getProjectID());
        holder.managerTextView.setText(project.getManager());
        holder.budgetTextView.setText(String.format(Locale.getDefault(), "£%.2f", project.getBudget()));
        holder.startDateTextView.setText(dateFormat.format(project.getStartDate()));
        holder.endDateTextView.setText(dateFormat.format(project.getEndDate()));

        // Set project status
        holder.projectStatusTextView.setText(project.getProjectStatus());

        // Set color based on status
        String status = project.getProjectStatus();
        if (status.equalsIgnoreCase("Completed")) {
            holder.projectStatusTextView.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (status.equalsIgnoreCase("Active")) {
            holder.projectStatusTextView.setTextColor(Color.parseColor("#2196F3")); // Blue
        } else if (status.equalsIgnoreCase("On Hold")) {
            holder.projectStatusTextView.setTextColor(Color.parseColor("#FFC107")); // Yellow/Amber
        }
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView projectTextView, projectIdTextView, managerTextView, budgetTextView;
        TextView startDateTextView, endDateTextView, projectStatusTextView;
        ImageButton deleteButton;
        OnProjectListener onProjectListener;

        public ProjectViewHolder(@NonNull View itemView, final OnProjectListener onProjectListener) {
            super(itemView);

            // Find all views
            projectTextView = itemView.findViewById(R.id.projectTextView);
            projectIdTextView = itemView.findViewById(R.id.projectIdTextView);
            managerTextView = itemView.findViewById(R.id.managerTextView);
            budgetTextView = itemView.findViewById(R.id.budgetTextView);
            startDateTextView = itemView.findViewById(R.id.startDateTextView);
            endDateTextView = itemView.findViewById(R.id.endDateTextView);
            projectStatusTextView = itemView.findViewById(R.id.projectStatusTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            this.onProjectListener = onProjectListener;

            // Set click listeners
            itemView.setOnClickListener(this);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onProjectListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onProjectListener.onDeleteClick(position);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            onProjectListener.onProjectClick(getAdapterPosition());
        }
    }

    public interface OnProjectListener {
        void onProjectClick(int position);
        void onDeleteClick(int position);
    }

    // Helper method to update the project list
    public void updateProjects(List<Project> projects) {
        this.projectList = projects;
        notifyDataSetChanged();
    }

    // Helper method to remove a project at a specific position
    public void removeProject(int position) {
        if (position >= 0 && position < projectList.size()) {
            projectList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, projectList.size());
        }
    }
}