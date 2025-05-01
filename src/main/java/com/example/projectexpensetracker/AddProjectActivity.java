package com.example.projectexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddProjectActivity extends AppCompatActivity {

    private EditText projectEditText, projectIdEditText, managerEditText, budgetEditText,
            startDateEditText, endDateEditText, projectDescEditText, specialReqEditText,
            clientInfoEditText;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Spinner projectStatusSpinner;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_add_item);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        //Initialise the spinner - source: https://developer.android.com/develop/ui/views/components/spinner#java
        projectStatusSpinner = findViewById(R.id.projectStatusSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.projectStatusArray,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        projectStatusSpinner.setAdapter(adapter);
        //set default selection
        projectStatusSpinner.setSelection(0);

        //Initialise views
        projectEditText = findViewById(R.id.projectEditText);
        projectIdEditText = findViewById(R.id.projectIdEditText);
        managerEditText = findViewById(R.id.managerEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        projectDescEditText = findViewById(R.id.projectDescEditText);
        specialReqEditText = findViewById(R.id.specialReqEditText);
        clientInfoEditText = findViewById(R.id.clientInfoEditText);
        budgetEditText = findViewById(R.id.budgetEditText);

        // Initialize date picker
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set current date as default
        startDateEditText.setText(dateFormat.format(calendar.getTime()));
        endDateEditText.setText(dateFormat.format(calendar.getTime()));

        // Set up date picker dialog
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateEditText);
            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateEditText);
            }
        });

        //Initialise buttons
        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        //Set up save button
        saveButton.setOnClickListener(v -> showConfirmationDialog());

        // Set up cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Save Project")
                .setMessage("Are you sure you want to save this project?")
                .setPositiveButton("Save", (dialog, which) -> {
                    saveProject();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // A showDatePickerDialog method that handles both fields
    private void showDatePickerDialog(final EditText dateField) {
        new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateField.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void saveProject() {
        // Get the values from the EditText fields
        String projectName = projectEditText.getText().toString();
        String projectId = projectIdEditText.getText().toString();
        String budgetStr = budgetEditText.getText().toString();
        String startDateStr = startDateEditText.getText().toString();
        String endDateStr = endDateEditText.getText().toString();
        String projectDesc = projectDescEditText.getText().toString();
        String specialReq = specialReqEditText.getText().toString();
        String clientInfo = clientInfoEditText.getText().toString();
        String manager = managerEditText.getText().toString();

        // Validate the input
        if (projectName.isEmpty()) {
            projectEditText.setError("Please enter a project name");
            return;
        }

        if (projectId.isEmpty()) {
            projectIdEditText.setError("Please enter a project ID");
            return;
        }

        if (projectDesc.isEmpty()) {
            projectDescEditText.setError("Please enter a project description");
            return;
        }

        // Check for spinner selection first
        if (projectStatusSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Please select a project status", Toast.LENGTH_SHORT).show();
            return;
        }
        String projectStatus = projectStatusSpinner.getSelectedItem().toString();

        if (manager.isEmpty()) {
            managerEditText.setError("Please enter a project manager");
            return;
        }

        if (budgetStr.isEmpty()) {
            budgetEditText.setError("Please enter a budget");
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
            if (budget <= 0) {
                budgetEditText.setError("Budget must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            budgetEditText.setError("Please enter a valid number");
            return;
        }

        Date startDate, endDate;
        try {
            startDate = dateFormat.parse(startDateStr);
            endDate = dateFormat.parse(endDateStr);
            if (endDate.before(startDate)) {
                endDateEditText.setError("End date must be after start date");
                return;
            }
        } catch (ParseException e) {
            startDateEditText.setError("Please enter a valid date (DD/MM/YYYY)");
            return;
        }

        // Create the project object - using the string constructor to match Project.java
        Project project = new Project(
                projectName,
                projectId,
                manager,
                projectStatus,
                startDateStr,
                endDateStr,
                budgetStr,
                projectDesc,
                specialReq,
                clientInfo
        );

        // Insert project into the database
        long result = databaseHelper.insertProject(project);

        if (result != -1) {
            Toast.makeText(this, "Project saved successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("project", project);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save project", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

}