// source: https://abhiandroid.com/materialdesign/tablayout-example-android-studio.html#gsc.tab=0
// source: https://www.geeksforgeeks.org/how-to-implement-tablayout-with-icon-in-android/
// source: https://www.youtube.com/watch?v=LXl7D57fgOQ

package com.example.projectexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProjectDetailsFragment extends Fragment {

    private static final String ARG_PROJECT = "project";
    private static final int REQUEST_CODE_EDIT_PROJECT = 1001;
    private DeleteProjectListener deleteProjectListener;

    private Project project;
    private TextView projectNameTextView, projectIdTextView, managerTextView,
            projectStatusTextView, startDateTextView, endDateTextView,
            budgetTextView, projectDescTextView, specialReqTextView,
            clientInfoTextView;
    private Button editButton, deleteButton, backButton;

    public interface DeleteProjectListener {
        void onDeleteProject(Project project);
    }

    public ProjectDetailsFragment() {
        // Required empty public constructor
    }

    public static ProjectDetailsFragment newInstance(Project project) {
        ProjectDetailsFragment fragment = new ProjectDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROJECT, project);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            project = (Project) getArguments().getSerializable(ARG_PROJECT);
        }
        // Ensure the parent activity implements our interface
        try {
            deleteProjectListener = (DeleteProjectListener) requireActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString()
                    + " must implement DeleteProjectListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.project_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        if (project != null) {
            populateProjectDetails(project);
        }

        setupButtonListeners();
    }

    private void initializeViews(View view) {
        // Initialize TextViews
        projectNameTextView = view.findViewById(R.id.projectTextView);
        projectIdTextView = view.findViewById(R.id.projectIdTextView);
        managerTextView = view.findViewById(R.id.managerTextView);
        projectStatusTextView = view.findViewById(R.id.projectStatusTextView);
        startDateTextView = view.findViewById(R.id.startDateTextView);
        endDateTextView = view.findViewById(R.id.endDateTextView);
        budgetTextView = view.findViewById(R.id.budgetTextView);
        projectDescTextView = view.findViewById(R.id.projectDescTextView);
        specialReqTextView = view.findViewById(R.id.specialReqTextView);
        clientInfoTextView = view.findViewById(R.id.clientInfoTextView);

        // Initialize Buttons
        editButton = view.findViewById(R.id.editButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        backButton = view.findViewById(R.id.backButton);
    }

    private void populateProjectDetails(Project project) {
        // Populate TextViews with project details
        projectNameTextView.setText(project.getProjectName());
        projectIdTextView.setText(project.getProjectID());
        managerTextView.setText(project.getManager());
        projectStatusTextView.setText(project.getProjectStatus());
        budgetTextView.setText(String.format(Locale.getDefault(), "£%.2f", project.getBudget()));
        projectDescTextView.setText(project.getProjectDesc());

        // Handle optional fields
        String specialReq = project.getSpecialReq();
        if (specialReq != null && !specialReq.isEmpty()) {
            specialReqTextView.setText(specialReq);
            specialReqTextView.setVisibility(View.VISIBLE);
        } else {
            specialReqTextView.setVisibility(View.GONE);
        }

        // Use the correct getter method for client info
        String clientInfo = project.getClientInfo();
        if (clientInfo != null && !clientInfo.isEmpty()) {
            clientInfoTextView.setText(clientInfo);
            clientInfoTextView.setVisibility(View.VISIBLE);
        } else {
            clientInfoTextView.setVisibility(View.GONE);
        }

        // Format dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        startDateTextView.setText(dateFormat.format(project.getStartDate()));
        endDateTextView.setText(dateFormat.format(project.getEndDate()));
    }

    private void setupButtonListeners() {
        // Set up Edit Button
        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(requireActivity(), EditProjectActivity.class);
            editIntent.putExtra("project", project);
            startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROJECT);
        });

        // Set up Delete Button
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(project));

        // Set up back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void showDeleteConfirmationDialog(final Project project) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete this project?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Use the callback to notify parent activity
                    deleteProjectListener.onDeleteProject(project);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PROJECT && resultCode == requireActivity().RESULT_OK) {
            // Update the project details if edited
            Project updatedProject = (Project) data.getSerializableExtra("project");
            if (updatedProject != null) {
                // Update current project reference
                project = updatedProject;

                // Update the views with new project details
                populateProjectDetails(updatedProject);
            }
        }
    }
}