package com.example.projectexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class EditExpenseActivity extends AppCompatActivity {
    private EditText expenseIdEditText, claimantEditText, amountEditText,
            expenseDateEditText, descriptionEditText, locationEditText;
    private Button saveButton, cancelButton, deleteButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Spinner paymentStatusSpinner, paymentMethodSpinner, expenseTypeSpinner, currencySpinner;
    private Expense currentExpense;
    private ExpenseDatabaseHelper expenseDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_edit_item);

        // Initialize the database helper
        expenseDatabaseHelper = new ExpenseDatabaseHelper(this);

        //Get the expense to edit
        currentExpense = (Expense) getIntent().getSerializableExtra("expense");
        if (currentExpense == null) {
            Toast.makeText(this, "Error: Expense not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //initialize views
        initializeViews();
        setupDatePicker();
        setupSpinner();
        populateExpenseDetails();
        setupButtonListeners();
    }

    // Initialize views
    private void initializeViews() {
        expenseIdEditText = findViewById(R.id.expenseIdEditText);
        claimantEditText = findViewById(R.id.claimantEditText);
        amountEditText = findViewById(R.id.amountEditText);
        expenseDateEditText = findViewById(R.id.expenseDateEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);

        // Initialize Buttons
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Initialize Spinners
        paymentStatusSpinner = findViewById(R.id.paymentStatusSpinner);
        paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner);
        expenseTypeSpinner = findViewById(R.id.expenseTypeSpinner);
        currencySpinner = findViewById(R.id.currencySpinner);

        // Initialize date-related objects
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    // Set up date picker
    private void setupDatePicker() {
        expenseDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(expenseDateEditText);
            }
        });
    }

    // Set up spinners
    private void setupSpinner() {
        ArrayAdapter<CharSequence> paymentStatusAdapter = ArrayAdapter.createFromResource(
                this, R.array.paymentStatusArray, android.R.layout.simple_spinner_item);
        paymentStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentStatusSpinner.setAdapter(paymentStatusAdapter);

        ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource(
                this, R.array.paymentMethodArray, android.R.layout.simple_spinner_item);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentMethodAdapter);

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                this, R.array.currencyArray, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        ArrayAdapter<CharSequence> expenseTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.expenseTypeArray, android.R.layout.simple_spinner_item);
        expenseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseTypeSpinner.setAdapter(expenseTypeAdapter);
    }

    // Populate expense details
    private void populateExpenseDetails() {
        expenseIdEditText.setText(currentExpense.getExpenseID());
        claimantEditText.setText(currentExpense.getClaimant());
        amountEditText.setText(String.format(Locale.getDefault(), "%.2f", currentExpense.getAmount()));
        expenseDateEditText.setText(dateFormat.format(currentExpense.getExpenseDate()));
        descriptionEditText.setText(currentExpense.getDescription());
        locationEditText.setText(currentExpense.getLocation());

        //set spinner selection
        String paymentStatus = currentExpense.getPaymentStatus();
        ArrayAdapter paymentStatusAdapter = (ArrayAdapter) paymentStatusSpinner.getAdapter();
        for (int i = 0; i < paymentStatusAdapter.getCount(); i++) {
            if (paymentStatusAdapter.getItem(i).toString().equals(paymentStatus)) {
                paymentStatusSpinner.setSelection(i);
                break;
            }
        }

        String paymentMethod = currentExpense.getPaymentMethod();
        ArrayAdapter paymentMethodAdapter = (ArrayAdapter) paymentMethodSpinner.getAdapter();
        for (int i = 0; i < paymentMethodAdapter.getCount(); i++) {
            if (paymentMethodAdapter.getItem(i).toString().equals(paymentMethod)) {
                paymentMethodSpinner.setSelection(i);
                break;
            }
        }

        String currency = currentExpense.getCurrency();
        ArrayAdapter currencyAdapter = (ArrayAdapter) currencySpinner.getAdapter();
        for (int i = 0; i < currencyAdapter.getCount(); i++) {
            if (currencyAdapter.getItem(i).toString().equals(currency)) {
                currencySpinner.setSelection(i);
                break;
            }
        }

        String expenseType = currentExpense.getExpenseType();
        ArrayAdapter expenseTypeAdapter = (ArrayAdapter) expenseTypeSpinner.getAdapter();
        for (int i = 0; i < expenseTypeAdapter.getCount(); i++) {
            if (expenseTypeAdapter.getItem(i).toString().equals(expenseType)) {
                expenseTypeSpinner.setSelection(i);
                break;
            }
        }
    }

    // Set up button listeners
    private void setupButtonListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExpense();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteExpense();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // Show date picker dialog
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

    private void updateExpense() {
        if (!validateInput()) {
            return;
        }

        // Get the values from the EditText fields
        String expenseId = expenseIdEditText.getText().toString();
        String claimant = claimantEditText.getText().toString();
        double amount = Double.parseDouble(amountEditText.getText().toString());
        String description = descriptionEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String paymentStatus = paymentStatusSpinner.getSelectedItem().toString();
        String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();
        String currency = currencySpinner.getSelectedItem().toString();
        String expenseType = expenseTypeSpinner.getSelectedItem().toString();

        // Parse date
        Date expenseDate;
        try {
            expenseDate = dateFormat.parse(expenseDateEditText.getText().toString());
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update/save the expense object
        currentExpense.setExpenseID(expenseId);
        currentExpense.setClaimant(claimant);
        currentExpense.setAmount(amount);
        currentExpense.setExpenseDate(expenseDate);
        currentExpense.setDescription(description);
        currentExpense.setLocation(location);
        currentExpense.setPaymentStatus(paymentStatus);
        currentExpense.setPaymentMethod(paymentMethod);
        currentExpense.setCurrency(currency);
        currentExpense.setExpenseType(expenseType);

        new AlertDialog.Builder(this)
                .setTitle("Save Expense")
                .setMessage("Are you sure you want to save this expense?")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Update in database
                    boolean result = expenseDatabaseHelper.updateExpense(currentExpense);

                    if (result) {
                        // Return the updated expense to ExpenseFragment
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("expense", currentExpense);
                        setResult(RESULT_OK, resultIntent);
                        Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense() {
        // Delete from database
        boolean result = expenseDatabaseHelper.deleteExpense(currentExpense.getId());

        if (result) {
            // Return delete info to fragment
            Intent resultIntent = new Intent();
            resultIntent.putExtra("expense", currentExpense);
            resultIntent.putExtra("delete", true);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete expense", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onDestroy() {
        if (expenseDatabaseHelper != null) {
            expenseDatabaseHelper.close();
        }
        super.onDestroy();
    }
}