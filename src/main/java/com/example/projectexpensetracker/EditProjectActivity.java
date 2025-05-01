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
public class EditProjectActivity extends AppCompatActivity {
    private EditText projectEditText, projectIdEditText, managerEditText, budgetEditText,
            startDateEditText, endDateEditText, projectDescEditText, specialReqEditText,
            clientInfoEditText;
    private Button saveButton, cancelButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Spinner projectStatusSpinner;
    private Project currentProject;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_edit_item);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        //Get the project to edit
        currentProject = (Project) getIntent().getSerializableExtra("project");
        if (currentProject == null) {
            Toast.makeText(this, "Error: Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //initialize views
        initializeViews();
        setupDatePicker();
        setupSpinner();
        populateProjectDetails();
        setupButtonListeners();
    }

    private void initializeViews() {
        projectEditText = findViewById(R.id.projectEditText);
        projectIdEditText = findViewById(R.id.projectIdEditText);
        managerEditText = findViewById(R.id.managerEditText);
        budgetEditText = findViewById(R.id.budgetEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        projectDescEditText = findViewById(R.id.projectDescEditText);
        specialReqEditText = findViewById(R.id.specialReqEditText);
        clientInfoEditText = findViewById(R.id.clientInfoEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        projectStatusSpinner = findViewById(R.id.projectStatusSpinner);

        //calendar
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void setupDatePicker() {
        //Set date picker
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
    }

    //setup spinner
    private void setupSpinner (){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.projectStatusArray,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        projectStatusSpinner.setAdapter(adapter);
    }

    private void populateProjectDetails() {
        projectEditText.setText(currentProject.getProjectName());
        projectIdEditText.setText(currentProject.getProjectID());
        managerEditText.setText(currentProject.getManager());
        budgetEditText.setText(String.format(Locale.getDefault(), "%.2f", currentProject.getBudget()));
        startDateEditText.setText(dateFormat.format(currentProject.getStartDate()));
        endDateEditText.setText(dateFormat.format(currentProject.getEndDate()));
        projectDescEditText.setText(currentProject.getProjectDesc());
        specialReqEditText.setText(currentProject.getSpecialReq());
        clientInfoEditText.setText(currentProject.getClientInfo());
//        projectStatusSpinner.setSelection(adapter.getPosition(currentProject.getProjectStatus()));

        //set spinner selection (fixed)
        String projectStatus = currentProject.getProjectStatus();
        ArrayAdapter adapter = (ArrayAdapter) projectStatusSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(projectStatus)) {
                projectStatusSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setupButtonListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProject();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void showDatePickerDialog(final EditText dateField) {
        // Parse the current date from the field
        Date currentDate = null;
        try {
            currentDate = dateFormat.parse(dateField.getText().toString());
        } catch (ParseException e) {
            currentDate = new Date();
        }

        // Set the calendar to the current date
        calendar.setTime(currentDate);
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

    private void updateProject() {
        if (!validateInput()) {
            return;
        }

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
        String projectStatus = projectStatusSpinner.getSelectedItem().toString();

        // Update/save the project object
        currentProject.setProjectName(projectName);
        currentProject.setProjectID(projectId);
        currentProject.setManager(manager);
        currentProject.setProjectStatus(projectStatus);
        currentProject.setProjectDesc(projectDesc);
        currentProject.setSpecialReq(specialReq);
        currentProject.setClientInfo(clientInfo);

        // Parse and set budget
        try {
            double budget = Double.parseDouble(budgetStr);
            currentProject.setBudget(budget);
        } catch (NumberFormatException e) {
            // This shouldn't happen as we validated the input
            budgetEditText.setError("Invalid budget value");
            return;
        }

        // Parse and set dates
        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            currentProject.setStartDate(startDate);
            currentProject.setEndDate(endDate);
        } catch (ParseException e) {
            // This shouldn't happen as we validated the input
            startDateEditText.setError("Invalid date format");
            return;
        }

        // Update project in the database
        boolean result = databaseHelper.updateProject(currentProject);

        // Return the updated project to calling activity
        if (result) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("project", currentProject);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update project", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    private boolean validateInput() {
        // Validate the input
        if (projectEditText.getText().toString().isEmpty()) {
            projectEditText.setError("Please enter a project name");
            return false;
        }
        if (projectIdEditText.getText().toString().isEmpty()) {
            projectIdEditText.setError("Please enter a project ID");
            return false;
        }
        if (projectDescEditText.getText().toString().isEmpty()) {
            projectDescEditText.setError("Please enter a project description");
            return false;
        }
        if (projectStatusSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Please select a project status", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (managerEditText.getText().toString().isEmpty()) {
            managerEditText.setError("Please enter a project manager");
            return false;
        }
        // Validate budget
        String budgetStr = budgetEditText.getText().toString().trim();
        if (budgetStr.isEmpty()) {
            budgetEditText.setError("Budget is required");
            return false;
        } else {
            try {
                double budget = Double.parseDouble(budgetStr);
                if (budget <= 0) {
                    budgetEditText.setError("Budget must be greater than 0");
                    return false;
                }
            } catch (NumberFormatException e) {
                budgetEditText.setError("Invalid budget format");
                return false;
            }
        }

        // Validate dates
        String startDateStr = startDateEditText.getText().toString().trim();
        String endDateStr = endDateEditText.getText().toString().trim();
        if (startDateStr.isEmpty()) {
            startDateEditText.setError("Start date is required");
            return false;
        }
        if (endDateStr.isEmpty()) {
            endDateEditText.setError("End date is required");
            return false;
        }
        return true;

    }

}
