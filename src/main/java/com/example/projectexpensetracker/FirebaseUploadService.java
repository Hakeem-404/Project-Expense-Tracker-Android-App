package com.example.projectexpensetracker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseUploadService {
    private static final String TAG = "FirebaseUploadService";

    public interface UploadCallback {
        void onUploadComplete(boolean success, String message);
        void onUploadProgress(int progress, int total);
    }

    public static void uploadAllProjects(Context context, UploadCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onUploadComplete(false, "No internet connection available");
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        ArrayList<Project> projects = dbHelper.getProject();
        dbHelper.close();

        if (projects.isEmpty()) {
            callback.onUploadComplete(false, "No projects to upload");
            return;
        }

        // Get Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference projectsRef = database.getReference("projects");

        // Track upload completion
        final int[] uploadedCount = {0};
        final int totalProjects = projects.size();

        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);

            // Convert project to Map for Firebase
            Map<String, Object> projectData = projectToMap(project, context);

            // Using the project ID as the key in Firebase
            String projectKey = project.getProjectID();

            // Upload to Firebase
            projectsRef.child(projectKey).setValue(projectData)
                    .addOnCompleteListener(task -> {
                        uploadedCount[0]++;

                        // Update progress
                        callback.onUploadProgress(uploadedCount[0], totalProjects);

                        // Check if all uploads are completed
                        if (uploadedCount[0] == totalProjects) {
                            callback.onUploadComplete(true, "Successfully uploaded " + totalProjects + " projects");
                        }

                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to upload project: " + task.getException());
                        }
                    });
        }
    }

    private static Map<String, Object> projectToMap(Project project, Context context) {
        Map<String, Object> projectMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        projectMap.put("id", project.getId());
        projectMap.put("name", project.getProjectName());
        projectMap.put("projectId", project.getProjectID());
        projectMap.put("manager", project.getManager());
        projectMap.put("status", project.getProjectStatus());
        projectMap.put("startDate", dateFormat.format(project.getStartDate()));
        projectMap.put("endDate", dateFormat.format(project.getEndDate()));
        projectMap.put("budget", project.getBudget());
        projectMap.put("description", project.getProjectDesc());
        projectMap.put("specialRequirements", project.getSpecialReq());
        projectMap.put("clientInfo", project.getClientInfo());

        // Get expenses for this project
        ExpenseDatabaseHelper expenseHelper = new ExpenseDatabaseHelper(context);
        List<Expense> expenses = expenseHelper.getExpensesByProjectId(project.getProjectID());
        expenseHelper.close();

        // Add expenses
        if (!expenses.isEmpty()) {
            List<Map<String, Object>> expensesList = new ArrayList<>();

            for (Expense expense : expenses) {
                Map<String, Object> expenseMap = new HashMap<>();
                expenseMap.put("id", expense.getId());
                expenseMap.put("expenseType", expense.getExpenseType());
                expenseMap.put("expenseId", expense.getExpenseID());
                expenseMap.put("claimant", expense.getClaimant());
                expenseMap.put("currency", expense.getCurrency());
                expenseMap.put("expenseDate", dateFormat.format(expense.getExpenseDate()));
                expenseMap.put("description", expense.getDescription());
                expenseMap.put("location", expense.getLocation());
                expenseMap.put("paymentMethod", expense.getPaymentMethod());
                expenseMap.put("paymentStatus", expense.getPaymentStatus());
                expenseMap.put("amount", expense.getAmount());

                expensesList.add(expenseMap);
            }

            projectMap.put("expenses", expensesList);
        }

        return projectMap;
    }
}