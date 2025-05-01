//Source: https://github.com/Abhishek111883/Android-project/tree/d117982d9b8a1182330f717dca3d2d1a46329416

package com.example.projectexpensetracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ProjectAdapter.OnProjectListener {
    private static final int ADD_PROJECT_REQUEST = 1;
    private static final int VIEW_PROJECT_REQUEST = 2;

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private TextView emptyView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fabExpenses);
        emptyView = findViewById(R.id.emptyView);

        // Initialize project list
        projectList = new ArrayList<>();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter(projectList, this);
        recyclerView.setAdapter(projectAdapter);

        // Update empty state
        updateEmptyState();

        // Set up FloatingActionButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Add Project Activity
                Intent intent = new Intent(MainActivity.this, AddProjectActivity.class);
                startActivityForResult(intent, ADD_PROJECT_REQUEST);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Set up search functionality
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search as user types to show matching results in real-time
                if (newText.length() >= 2) {  // Only search when at least 2 characters are entered
                    performSearch(newText);
                } else if (newText.isEmpty()) {
                    loadAllProjects();
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_upload) {
            uploadProjectsToFirebase();
            return true;
        } else if (itemId == R.id.action_advanced_search) {
            showAdvancedSearchDialog();
            return true;
        } else if (itemId == R.id.action_restore) {
            showRestoreConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRestoreConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Restore Projects")
                .setMessage("This will download all projects from the cloud. Any existing projects with the same IDs will be updated. Continue?")
                .setPositiveButton("Restore", (dialog, which) -> {
                    restoreProjectsFromFirebase();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void restoreProjectsFromFirebase() {
        // First check for network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show();
            return;
        }

        // Create and show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Restoring Projects");
        progressDialog.setMessage("Connecting to cloud...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        // Add a timeout mechanism for the dialog itself
        final boolean[] completed = {false};

        // Start the download
        FirebaseDownloadService.downloadAllProjects(this, new FirebaseDownloadService.DownloadCallback() {
            @Override
            public void onDownloadStarted() {
                runOnUiThread(() -> {
                    progressDialog.show();
                });
            }

            @Override
            public void onDownloadComplete(boolean success, String message, int projectCount) {
                completed[0] = true;
                runOnUiThread(() -> {
                    try {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error dismissing dialog", e);
                    }

                    if (success) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        // Refresh the project list
                        loadAllProjects();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Restore Failed")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
            }

            @Override
            public void onDownloadProgress(int progress, int total) {
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMax(total);
                        progressDialog.setProgress(progress);
                        progressDialog.setMessage("Restoring project " + progress + " of " + total);
                    }
                });
            }
        });

        // Add a backup timeout mechanism
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!completed[0]) {
                try {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Restore Timed Out")
                                .setMessage("The connection to Firebase timed out. Please check your internet connection and try again.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error handling timeout", e);
                }
            }
        }, 60000); // 60 seconds total timeout
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Fetch all projects from the database
        loadAllProjects();
    }

    private void loadAllProjects() {
        List<Project> projects = databaseHelper.getProject();
        projectList.clear();
        projectList.addAll(projects);
        projectAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PROJECT_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("project")) {
                Project newProject = (Project) data.getSerializableExtra("project");
                projectList.add(newProject);
                projectAdapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(this, "Project added successfully", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == VIEW_PROJECT_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("action")) {
                String action = data.getStringExtra("action");
                Project project = (Project) data.getSerializableExtra("project");

                if ("delete".equals(action)) {
                    // Find project in the list and remove it
                    for (int i = 0; i < projectList.size(); i++) {
                        if (projectList.get(i).getId() == project.getId()) {
                            projectList.remove(i);
                            projectAdapter.notifyItemRemoved(i);
                            projectAdapter.notifyItemRangeChanged(i, projectList.size());
                            break;
                        }
                    }
                    updateEmptyState();
                    Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                } else if ("update".equals(action)) {
                    // Update existing project in list
                    for (int i = 0; i < projectList.size(); i++) {
                        if (projectList.get(i).getId() == project.getId()) {
                            projectList.set(i, project);
                            projectAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateEmptyState() {
        if (projectList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProjectClick(int position) {
        // Handle project click - open details or edit screen
        Project selectedProject = projectList.get(position);
        Intent intent = new Intent(MainActivity.this, ViewProjectActivity.class);
        intent.putExtra("project", selectedProject);
        intent.putExtra("position", position); // Add position to know which project was clicked
        startActivityForResult(intent, VIEW_PROJECT_REQUEST);  // Changed from startActivity
    }

    @Override
    public void onDeleteClick(int position) {
        // Remove the project at the given position
        Project project = projectList.get(position);
        boolean result = databaseHelper.deleteProject(project.getId());

        if (result) {
            projectList.remove(position);
            projectAdapter.notifyItemRemoved(position);
            projectAdapter.notifyItemRangeChanged(position, projectList.size());
            updateEmptyState();
            Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete project", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // If query is empty, show all projects
            loadAllProjects();
        } else {
            // Search by project name and description
            List<Project> nameResults = databaseHelper.getProjectByName(query);
            List<Project> descResults = databaseHelper.getProjectsByDescription(query);

            // Combine results, avoiding duplicates
            Set<Project> combinedResults = new HashSet<>();
            combinedResults.addAll(nameResults);
            combinedResults.addAll(descResults);

            // Update the UI with search results
            updateProjectList(new ArrayList<>(combinedResults));
        }
    }

    private void showAdvancedSearchDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_advanced_search);
        dialog.setCancelable(true);

        // Initialize UI elements
        Spinner statusSpinner = dialog.findViewById(R.id.statusSpinner);
        EditText managerEditText = dialog.findViewById(R.id.managerEditText);
        EditText startDateEditText = dialog.findViewById(R.id.startDateEditText);
        EditText endDateEditText = dialog.findViewById(R.id.endDateEditText);
        Button searchButton = dialog.findViewById(R.id.searchButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        // Set up status spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.projectStatusArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Add "Any" option at the beginning
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Any");
        for (int i = 0; i < adapter.getCount(); i++) {
            statusOptions.add(adapter.getItem(i).toString());
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Set up date pickers
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        startDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        startDateEditText.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        endDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        endDateEditText.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Add search and cancel buttons
        searchButton.setOnClickListener(v -> {
            performAdvancedSearch(
                    statusSpinner.getSelectedItemPosition() == 0 ? null : statusSpinner.getSelectedItem().toString(),
                    managerEditText.getText().toString(),
                    startDateEditText.getText().toString(),
                    endDateEditText.getText().toString()
            );
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void performAdvancedSearch(String status, String manager, String startDateStr, String endDateStr) {
        ArrayList<Project> results = new ArrayList<>();

        // Start with all projects
        ArrayList<Project> allProjects = databaseHelper.getProject();

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            results = new ArrayList<>();
            for (Project project : allProjects) {
                if (project.getProjectStatus().equalsIgnoreCase(status)) {
                    results.add(project);
                }
            }
            allProjects = results;
        }

        // Filter by manager if provided
        if (!manager.isEmpty()) {
            results = new ArrayList<>();
            for (Project project : allProjects) {
                if (project.getManager().toLowerCase().contains(manager.toLowerCase())) {
                    results.add(project);
                }
            }
            allProjects = results;
        }

        // Filter by date range if provided
        if (!startDateStr.isEmpty() && !endDateStr.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);

                // Get projects within this date range
                List<Project> dateProjects = databaseHelper.getProjectsByDate(startDate, endDate, "both");

                // Intersect with current filtered projects
                results = new ArrayList<>();
                for (Project project : allProjects) {
                    if (dateProjects.contains(project)) {
                        results.add(project);
                    }
                }
                allProjects = results;
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }
        }

        // Update the UI with search results
        updateProjectList(allProjects);
    }

    private void updateProjectList(List<Project> results) {
        projectList.clear();
        projectList.addAll(results);
        projectAdapter.notifyDataSetChanged();
        updateEmptyState();

        // Show a message about the search results
        if (results.isEmpty()) {
            Toast.makeText(this, "No matching projects found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, results.size() + " projects found", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProjectsToFirebase() {
        // First check for network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show();
            return;
        }

        // Make sure we have projects to upload
        if (projectList.isEmpty()) {
            Toast.makeText(this, "No projects to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Projects");
        progressDialog.setMessage("Preparing data...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Start the upload
        FirebaseUploadService.uploadAllProjects(this, new FirebaseUploadService.UploadCallback() {
            @Override
            public void onUploadComplete(boolean success, String message) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    if (success) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Upload Failed")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
            }

            @Override
            public void onUploadProgress(int progress, int total) {
                runOnUiThread(() -> {
                    progressDialog.setMax(total);
                    progressDialog.setProgress(progress);
                    progressDialog.setMessage("Uploading project " + progress + " of " + total);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}