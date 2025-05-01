package com.example.projectexpensetracker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseDownloadService {
    private static final String TAG = "FirebaseDownloadService";

    public interface DownloadCallback {
        void onDownloadComplete(boolean success, String message, int projectCount);
        void onDownloadProgress(int progress, int total);
        void onDownloadStarted();
    }

    public static void downloadAllProjects(Context context, DownloadCallback callback) {
        // Notify that download has started
        callback.onDownloadStarted();

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "No network connection available");
            callback.onDownloadComplete(false, "No internet connection available", 0);
            return;
        }

        try {
            // Get Firebase database reference
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Force online mode and disable persistence for this operation
            try {
                database.setPersistenceEnabled(false);
            } catch (Exception e) {
                Log.w(TAG, "Could not disable persistence, may already be set: " + e.getMessage());
            }

            database.goOnline();

            if (database == null) {
                Log.e(TAG, "Firebase database instance is null");
                callback.onDownloadComplete(false, "Failed to connect to Firebase", 0);
                return;
            }

            // Log the database URL
            Log.d(TAG, "Firebase database URL: " + database.getReference().toString());

            DatabaseReference projectsRef = database.getReference("projects");
            Log.d(TAG, "Firebase reference created: " + projectsRef.toString());

            // Get reference to local database helpers
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            ExpenseDatabaseHelper expenseHelper = new ExpenseDatabaseHelper(context);

            // Listen for data from Firebase with timeout
            projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange triggered. Has data: " + dataSnapshot.exists());

                    if (!dataSnapshot.exists()) {
                        dbHelper.close();
                        expenseHelper.close();
                        callback.onDownloadComplete(false, "No projects found in cloud storage", 0);
                        return;
                    }

                    int totalProjects = (int) dataSnapshot.getChildrenCount();
                    Log.d(TAG, "Found " + totalProjects + " projects in Firebase");

                    // Safety check
                    if (totalProjects <= 0) {
                        dbHelper.close();
                        expenseHelper.close();
                        callback.onDownloadComplete(false, "No projects available to restore", 0);
                        return;
                    }

                    final int[] processedCount = {0};
                    final int[] successCount = {0};

                    for (DataSnapshot projectSnapshot : dataSnapshot.getChildren()) {
                        try {
                            Log.d(TAG, "Processing project: " + projectSnapshot.getKey());

                            // Extract project data with null checks
                            String projectName = getStringValue(projectSnapshot, "name");
                            String projectId = getStringValue(projectSnapshot, "projectId");
                            String manager = getStringValue(projectSnapshot, "manager");
                            String status = getStringValue(projectSnapshot, "status");
                            String startDateStr = getStringValue(projectSnapshot, "startDate");
                            String endDateStr = getStringValue(projectSnapshot, "endDate");
                            Double budget = getDoubleValue(projectSnapshot, "budget");
                            String description = getStringValue(projectSnapshot, "description");
                            String specialRequirements = getStringValue(projectSnapshot, "specialRequirements");
                            String clientInfo = getStringValue(projectSnapshot, "clientInfo");

                            // Check if required fields are present
                            if (projectName == null || projectId == null) {
                                Log.e(TAG, "Project missing required fields: name or ID");
                                processedCount[0]++;
                                continue;
                            }

                            // Parse dates from string format with fallback
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date startDate, endDate;
                            try {
                                startDate = (startDateStr != null) ? dateFormat.parse(startDateStr) : new Date();
                                endDate = (endDateStr != null) ? dateFormat.parse(endDateStr) : new Date();
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing dates: " + e.getMessage());
                                startDate = new Date();
                                endDate = new Date();
                            }

                            // Create Project object with default values where needed
                            Project project = new Project(
                                    projectName,
                                    projectId,
                                    manager != null ? manager : "Unknown",
                                    status != null ? status : "Active",
                                    startDate,
                                    endDate,
                                    budget != null ? budget : 0.0,
                                    description != null ? description : "",
                                    specialRequirements != null ? specialRequirements : "",
                                    clientInfo != null ? clientInfo : ""
                            );

                            Log.d(TAG, "Inserting project into local database: " + projectName);

                            // Insert project into local database
                            long projectDbId = dbHelper.insertProject(project);

                            if (projectDbId != -1) {
                                successCount[0]++;
                                Log.d(TAG, "Successfully inserted project: " + projectName);

                                // Process expenses if they exist
                                if (projectSnapshot.hasChild("expenses")) {
                                    Log.d(TAG, "Processing expenses for project: " + projectName);
                                    DataSnapshot expensesSnapshot = projectSnapshot.child("expenses");

                                    for (DataSnapshot expenseSnapshot : expensesSnapshot.getChildren()) {
                                        try {
                                            String expenseType = getStringValue(expenseSnapshot, "expenseType");
                                            String expenseId = getStringValue(expenseSnapshot, "expenseId");
                                            String claimant = getStringValue(expenseSnapshot, "claimant");
                                            String currency = getStringValue(expenseSnapshot, "currency");
                                            String expenseDateStr = getStringValue(expenseSnapshot, "expenseDate");
                                            String expenseDesc = getStringValue(expenseSnapshot, "description");
                                            String location = getStringValue(expenseSnapshot, "location");
                                            String paymentMethod = getStringValue(expenseSnapshot, "paymentMethod");
                                            String paymentStatus = getStringValue(expenseSnapshot, "paymentStatus");
                                            Double amount = getDoubleValue(expenseSnapshot, "amount");

                                            // Parse expense date
                                            Date expenseDate;
                                            try {
                                                expenseDate = (expenseDateStr != null) ?
                                                        dateFormat.parse(expenseDateStr) : new Date();
                                            } catch (ParseException e) {
                                                expenseDate = new Date();
                                            }

                                            // Create Expense object
                                            Expense expense = new Expense(
                                                    expenseType != null ? expenseType : "Miscellaneous",
                                                    expenseId != null ? expenseId : "EXP-" + System.currentTimeMillis(),
                                                    claimant != null ? claimant : "",
                                                    currency != null ? currency : "Pound Sterling (£)",
                                                    expenseDate,
                                                    expenseDesc != null ? expenseDesc : "",
                                                    location != null ? location : "",
                                                    paymentMethod != null ? paymentMethod : "Cash",
                                                    paymentStatus != null ? paymentStatus : "Pending",
                                                    amount != null ? amount : 0.0
                                            );

                                            // Insert expense into database
                                            expenseHelper.insertExpense(expense, projectId);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error processing expense: " + e.getMessage());
                                        }
                                    }
                                }
                            } else {
                                Log.e(TAG, "Failed to insert project: " + projectName);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing project: " + e.getMessage(), e);
                        } finally {
                            processedCount[0]++;

                            // Update progress
                            int currentProgress = processedCount[0];
                            Log.d(TAG, "Progress: " + currentProgress + "/" + totalProjects);
                            callback.onDownloadProgress(currentProgress, totalProjects);

                            // Check if all projects have been processed
                            if (currentProgress >= totalProjects) {
                                Log.d(TAG, "All projects processed. Success count: " + successCount[0]);
                                dbHelper.close();
                                expenseHelper.close();
                                database.goOffline(); // Disconnect from Firebase when done
                                callback.onDownloadComplete(
                                        successCount[0] > 0,
                                        successCount[0] + " projects restored successfully",
                                        successCount[0]
                                );
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase download canceled: " + error.getMessage() + ", code: " + error.getCode(), error.toException());
                    dbHelper.close();
                    expenseHelper.close();
                    database.goOffline(); // Disconnect from Firebase on error
                    callback.onDownloadComplete(false, "Failed to retrieve projects: " + error.getMessage() + " (Code: " + error.getCode() + ")", 0);
                }
            });

            // Add a timeout mechanism
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                Log.w(TAG, "Timeout reached waiting for Firebase response");
                // We don't call the callback here as it might have already been called
                // This is just a safeguard
            }, 30000); // 30 seconds timeout

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in download process", e);
            callback.onDownloadComplete(false, "Error connecting to Firebase: " + e.getMessage(), 0);
        }
    }

    // Helper methods to safely extract values
    private static String getStringValue(DataSnapshot snapshot, String key) {
        if (snapshot.hasChild(key)) {
            return snapshot.child(key).getValue(String.class);
        }
        return null;
    }

    private static Double getDoubleValue(DataSnapshot snapshot, String key) {
        if (snapshot.hasChild(key)) {
            return snapshot.child(key).getValue(Double.class);
        }
        return null;
    }
}