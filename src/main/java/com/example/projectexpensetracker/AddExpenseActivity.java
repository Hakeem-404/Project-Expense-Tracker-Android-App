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

//Source: https://developer.android.com/develop/ui/views/components/spinner
//Source https://www.geeksforgeeks.org/datepickerdialog-in-android/?ref=header_ind
// Source: https://www.geeksforgeeks.org/datepicker-in-android/?ref=header_ind

public class AddExpenseActivity extends AppCompatActivity {

    private EditText expenseDateEditText, descriptionEditText, expenseIdEditText,
            locationEditText, amountEditText, claimantEditText;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Spinner paymentStatusSpinner, paymentMethodSpinner, expenseTypeSpinner, currencySpinner;
    private ExpenseDatabaseHelper expenseDatabaseHelper;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_add_item);

        // Initialize ExpenseDatabaseHelper
        expenseDatabaseHelper = new ExpenseDatabaseHelper(this);

        // Get project ID from intent
        projectId = getIntent().getStringExtra("projectId");

        // Initialize the spinners
        paymentStatusSpinner = findViewById(R.id.paymentStatusSpinner);
        paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner);
        expenseTypeSpinner = findViewById(R.id.expenseTypeSpinner);
        currencySpinner = findViewById(R.id.currencySpinner);

        // Create ArrayAdapters for spinners
        ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.paymentMethodArray,
                android.R.layout.simple_spinner_item
        );

        ArrayAdapter<CharSequence> paymentStatusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.paymentStatusArray,
                android.R.layout.simple_spinner_item
        );

        ArrayAdapter<CharSequence> expenseTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.expenseTypeArray,
                android.R.layout.simple_spinner_item
        );

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.currencyArray,
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapters to the spinners
        paymentStatusSpinner.setAdapter(paymentStatusAdapter);
        paymentMethodSpinner.setAdapter(paymentMethodAdapter);
        expenseTypeSpinner.setAdapter(expenseTypeAdapter);
        currencySpinner.setAdapter(currencyAdapter);

        // Set default selections
        paymentStatusSpinner.setSelection(0);
        paymentMethodSpinner.setSelection(0);
        expenseTypeSpinner.setSelection(0);
        currencySpinner.setSelection(0);

        // Initialize views
        expenseDateEditText = findViewById(R.id.expenseDateEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        expenseIdEditText = findViewById(R.id.expenseIdEditText);
        locationEditText = findViewById(R.id.locationEditText);
        amountEditText = findViewById(R.id.amountEditText);
        claimantEditText = findViewById(R.id.claimantEditText);

        // Initialize date picker
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set current date as default
        expenseDateEditText.setText(dateFormat.format(calendar.getTime()));

        // Set up date picker dialog
        expenseDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(expenseDateEditText);
            }
        });

        // Initialize buttons
        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        // Set up save button
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
                .setTitle("Save Expense")
                .setMessage("Are you sure you want to save this expense?")
                .setPositiveButton("Save", (dialog, which) -> {
                    saveExpense();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

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

    private boolean validateInput() {
        boolean isValid = true;
        if (expenseIdEditText.getText().toString().trim().isEmpty()) {
            expenseIdEditText.setError("Please enter an expense ID");
            isValid = false;
        }
        if (claimantEditText.getText().toString().trim().isEmpty()) {
            claimantEditText.setError("Please enter a claimant");
            isValid = false;
        }
        if (amountEditText.getText().toString().trim().isEmpty()) {
            amountEditText.setError("Please enter an amount");
            isValid = false;
        } else {
            try {
                double amountValue = Double.parseDouble(amountEditText.getText().toString().trim());
                if (amountValue <= 0) {
                    amountEditText.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                amountEditText.setError("Please enter a valid number");
                isValid = false;
            }
        }
        if (expenseDateEditText.getText().toString().trim().isEmpty()) {
            expenseDateEditText.setError("Please select a date");
            isValid = false;
        }
        return isValid;
    }

    private void saveExpense() {
        if (!validateInput()) {
            return;
        }

        // Get the values from the EditText fields
        String expenseId = expenseIdEditText.getText().toString();
        String claimant = claimantEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String location = locationEditText.getText().toString();

        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(amountEditText.getText().toString());
        } catch (NumberFormatException e) {
            amountEditText.setError("Invalid amount");
            return;
        }

        // Parse date
        Date expenseDate;
        try {
            expenseDate = dateFormat.parse(expenseDateEditText.getText().toString());
        } catch (ParseException e) {
            expenseDateEditText.setError("Invalid date");
            return;
        }

        // Get spinner selections
        String expenseType = expenseTypeSpinner.getSelectedItem().toString();
        String paymentStatus = paymentStatusSpinner.getSelectedItem().toString();
        String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();
        String currency = currencySpinner.getSelectedItem().toString();

        // Create the expense object
        Expense expense = new Expense(
                expenseType,
                expenseId,
                claimant,
                currency,
                expenseDate,
                description,
                location,
                paymentMethod,
                paymentStatus,
                amount
        );

        // Insert expense into the database
        long result = expenseDatabaseHelper.insertExpense(expense, projectId);

        if (result != -1) {
            Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("expense", expense);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        expenseDatabaseHelper.close();
        super.onDestroy();
    }
}