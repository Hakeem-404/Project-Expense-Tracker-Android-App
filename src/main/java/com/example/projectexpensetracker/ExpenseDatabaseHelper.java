package com.example.projectexpensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final String TABLE_NAME = "expense";

    public static final String ID_COLUMN = "id";
    public static final String EXPENSE_TYPE_COLUMN = "expense_type";
    public static final String EXPENSE_ID_COLUMN = "expense_id";
    public static final String CLAIMANT_COLUMN = "claimant";
    public static final String CURRENCY_COLUMN = "currency";

    public static final String EXPENSE_DATE_COLUMN = "expense_date";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String LOCATION_COLUMN = "location";
    public static final String PAYMENT_METHOD_COLUMN = "payment_method";
    public static final String PAYMENT_STATUS_COLUMN = "payment_status";
    public static final String AMOUNT_COLUMN = "amount";
    public static final String PROJECT_ID_COLUMN = "project_id"; //foreign key

    private SQLiteDatabase database;

    private static final String DATABASE_CREATE = String.format(
            "CREATE TABLE %s (" +
                    "   %s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT, " +
                    "   %s TEXT)",
            TABLE_NAME, ID_COLUMN, EXPENSE_TYPE_COLUMN, EXPENSE_ID_COLUMN, CLAIMANT_COLUMN, CURRENCY_COLUMN,
            EXPENSE_DATE_COLUMN, DESCRIPTION_COLUMN, LOCATION_COLUMN, PAYMENT_METHOD_COLUMN,
            PAYMENT_STATUS_COLUMN, AMOUNT_COLUMN, PROJECT_ID_COLUMN);

    public ExpenseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        database = getWritableDatabase();
        ensureTableExists();
    }

    public void ensureTableExists() {
        // Using the same table structure as defined in DATABASE_CREATE
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EXPENSE_TYPE_COLUMN + " TEXT, " +
                EXPENSE_ID_COLUMN + " TEXT, " +
                CLAIMANT_COLUMN + " TEXT, " +
                CURRENCY_COLUMN + " TEXT, " +
                EXPENSE_DATE_COLUMN + " TEXT, " +
                DESCRIPTION_COLUMN + " TEXT, " +
                LOCATION_COLUMN + " TEXT, " +
                PAYMENT_METHOD_COLUMN + " TEXT, " +
                PAYMENT_STATUS_COLUMN + " TEXT, " +
                AMOUNT_COLUMN + " TEXT, " +
                PROJECT_ID_COLUMN + " TEXT)");
    }

    //Creates the expense table with all necessary columns when the database is first created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    //Handles database version upgrades by dropping and recreating the table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        Log.v(this.getClass().getName(), DATABASE_NAME + " database upgrade to version " +
                newVersion + " - old data lost");
        onCreate(db);
    }

    //Inserts a new expense into the database using individual parameters
    public long insertExpense(String expenseType, String expenseID, String claimant, String currency,
                              Date expenseDate, String description, String location, String paymentMethod,
                              String paymentStatus, double amount, String projectID) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(EXPENSE_TYPE_COLUMN, expenseType);
        rowValues.put(PROJECT_ID_COLUMN, projectID);
        rowValues.put(CLAIMANT_COLUMN, claimant);
        rowValues.put(CURRENCY_COLUMN, currency);
        rowValues.put(PAYMENT_METHOD_COLUMN, paymentMethod);
        rowValues.put(PAYMENT_STATUS_COLUMN, paymentStatus);
        rowValues.put(EXPENSE_ID_COLUMN, expenseID);
        rowValues.put(AMOUNT_COLUMN, amount);
        rowValues.put(DESCRIPTION_COLUMN, description);
        rowValues.put(LOCATION_COLUMN, location);

        //convert string to date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        rowValues.put(EXPENSE_DATE_COLUMN, dateFormat.format(expenseDate));

        return database.insertOrThrow(TABLE_NAME, null, rowValues);
    }

    //Overloaded method that inserts an Expense object into the database
    public long insertExpense(Expense e) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(EXPENSE_TYPE_COLUMN, e.getExpenseType());
        rowValues.put(PROJECT_ID_COLUMN, e.getProjectID());
        rowValues.put(CLAIMANT_COLUMN, e.getClaimant());
        rowValues.put(PAYMENT_STATUS_COLUMN, e.getPaymentStatus());
        rowValues.put(EXPENSE_ID_COLUMN, e.getExpenseID());
        rowValues.put(AMOUNT_COLUMN, e.getAmount());
        rowValues.put(DESCRIPTION_COLUMN, e.getDescription());
        rowValues.put(LOCATION_COLUMN, e.getLocation());
        rowValues.put(PAYMENT_METHOD_COLUMN, e.getPaymentMethod());
        rowValues.put(CURRENCY_COLUMN, e.getCurrency());

        // Convert Date to String format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (e.getExpenseDate() != null) {
            rowValues.put(EXPENSE_DATE_COLUMN, dateFormat.format(e.getExpenseDate()));
        }

        return database.insertOrThrow(TABLE_NAME, null, rowValues);
    }

    public long insertExpense(Expense expense, String projectId) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(EXPENSE_TYPE_COLUMN, expense.getExpenseType());
        rowValues.put(PROJECT_ID_COLUMN, projectId);
        rowValues.put(CLAIMANT_COLUMN, expense.getClaimant());
        rowValues.put(PAYMENT_STATUS_COLUMN, expense.getPaymentStatus());
        rowValues.put(EXPENSE_ID_COLUMN, expense.getExpenseID());
        rowValues.put(AMOUNT_COLUMN, expense.getAmount());
        rowValues.put(DESCRIPTION_COLUMN, expense.getDescription());
        rowValues.put(LOCATION_COLUMN, expense.getLocation());
        rowValues.put(PAYMENT_METHOD_COLUMN, expense.getPaymentMethod());
        rowValues.put(CURRENCY_COLUMN, expense.getCurrency());

        // Convert Date to String format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (expense.getExpenseDate() != null) {
            rowValues.put(EXPENSE_DATE_COLUMN, dateFormat.format(expense.getExpenseDate()));
        }

        return database.insertOrThrow(TABLE_NAME, null, rowValues);
    }

    //Updates an expense in the database using an Expense object
    public boolean updateExpense(Expense e) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(EXPENSE_TYPE_COLUMN, e.getExpenseType());
        rowValues.put(PROJECT_ID_COLUMN, e.getProjectID());
        rowValues.put(CLAIMANT_COLUMN, e.getClaimant());
        rowValues.put(EXPENSE_ID_COLUMN, e.getExpenseID());
        rowValues.put(AMOUNT_COLUMN, e.getAmount());
        rowValues.put(DESCRIPTION_COLUMN, e.getDescription());
        rowValues.put(LOCATION_COLUMN, e.getLocation());
        rowValues.put(PAYMENT_METHOD_COLUMN, e.getPaymentMethod());
        rowValues.put(PAYMENT_STATUS_COLUMN, e.getPaymentStatus());
        rowValues.put(CURRENCY_COLUMN, e.getCurrency());

        // Convert Date to String format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (e.getExpenseDate() != null) {
            rowValues.put(EXPENSE_DATE_COLUMN, dateFormat.format(e.getExpenseDate()));
        }

        // Update where id matches
        return database.update(TABLE_NAME, rowValues, ID_COLUMN + "=?",
                new String[]{String.valueOf(e.getId())}) > 0;
    }

    //Deletes an expense from the database using the id
    public boolean deleteExpense(int id) {
        return database.delete(TABLE_NAME, ID_COLUMN + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    //Retrieves all expenses and returns them as a formatted string
    public String getExpenseString() {
        Cursor results = database.query(TABLE_NAME, new String[]{ID_COLUMN, EXPENSE_TYPE_COLUMN, PROJECT_ID_COLUMN,
                        EXPENSE_ID_COLUMN, CLAIMANT_COLUMN, PAYMENT_STATUS_COLUMN, EXPENSE_DATE_COLUMN, DESCRIPTION_COLUMN,
                        AMOUNT_COLUMN, PAYMENT_METHOD_COLUMN, LOCATION_COLUMN, CURRENCY_COLUMN},
                null, null, null, null, PROJECT_ID_COLUMN);

        StringBuilder resultText = new StringBuilder();
        results.moveToFirst();
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String expense_type = results.getString(1);
            String project_id = results.getString(2);
            String expense_id = results.getString(3);
            String claimant = results.getString(4);
            String payment_status = results.getString(5);
            String expense_date = results.getString(6);
            String description = results.getString(7);
            double amount = results.getDouble(8);
            String payment_method = results.getString(9);
            String location = results.getString(10);
            String currency = results.getString(11);


            resultText.append(id).append(" ").append(expense_type).append(" ").append(project_id).append(" ")
                    .append(expense_id).append(" ").append(claimant).append(" ").append(payment_status).append(" ")
                    .append(expense_date).append(" ").append(description).append(" ").append(amount).append(" ")
                    .append(payment_method).append(" ").append(location).append(" ").append(currency).append("\n");

            results.moveToNext();
        }

        results.close();
        return resultText.toString();
    }

    //Retrieves all expenses and returns them as an ArrayList of Expense objects
    public ArrayList<Expense> getExpense() {
        Cursor results = database.query(TABLE_NAME, new String[]{ID_COLUMN, EXPENSE_TYPE_COLUMN, PROJECT_ID_COLUMN,
                        EXPENSE_ID_COLUMN, CLAIMANT_COLUMN, PAYMENT_STATUS_COLUMN, EXPENSE_DATE_COLUMN, DESCRIPTION_COLUMN,
                        AMOUNT_COLUMN, PAYMENT_METHOD_COLUMN, LOCATION_COLUMN, CURRENCY_COLUMN},
                null, null, null, null, EXPENSE_TYPE_COLUMN);

        ArrayList<Expense> expenseList = new ArrayList<Expense>();

        results.moveToFirst();
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String expense_type = results.getString(1);
            String project_id = results.getString(2);
            String expense_id = results.getString(3);
            String claimant = results.getString(4);
            String payment_status = results.getString(5);
            String expense_date = results.getString(6);
            String description = results.getString(7);
            double amount = results.getDouble(8);
            String payment_method = results.getString(9);
            String location = results.getString(10);
            String currency = results.getString(11);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expenseDate;
            try {
                expenseDate = dateFormat.parse(expense_date);
            } catch (ParseException e) {
                expenseDate = new Date();
            }

            // Create Expense object
            Expense expense = new Expense(expense_type, expense_id, claimant, currency, expenseDate,
                    description, location, payment_method, payment_status, amount);
            expense.setId(id);
            expense.setProjectID(project_id);

            expenseList.add(expense);

            results.moveToNext();
        }

        results.close();
        return expenseList;
    }

    public List<Expense> getExpensesByProjectId(String projectId) {
        Cursor results = database.query(TABLE_NAME,
                new String[]{ID_COLUMN, EXPENSE_TYPE_COLUMN, PROJECT_ID_COLUMN, EXPENSE_ID_COLUMN,
                        CLAIMANT_COLUMN, PAYMENT_STATUS_COLUMN, EXPENSE_DATE_COLUMN, DESCRIPTION_COLUMN,
                        AMOUNT_COLUMN, PAYMENT_METHOD_COLUMN, LOCATION_COLUMN, CURRENCY_COLUMN},
                PROJECT_ID_COLUMN + "=?", new String[]{projectId},
                null, null, EXPENSE_TYPE_COLUMN);

        ArrayList<Expense> expenseList = new ArrayList<>();

        results.moveToFirst();
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String expense_type = results.getString(1);
            String project_id = results.getString(2);
            String expense_id = results.getString(3);
            String claimant = results.getString(4);
            String payment_status = results.getString(5);
            String expense_date = results.getString(6);
            String description = results.getString(7);
            double amount = results.getDouble(8);
            String payment_method = results.getString(9);
            String location = results.getString(10);
            String currency = results.getString(11);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expenseDate;
            try {
                expenseDate = dateFormat.parse(expense_date);
            } catch (ParseException e) {
                expenseDate = new Date();
            }

            // Create Expense object
            Expense expense = new Expense(expense_type, expense_id, claimant, currency, expenseDate,
                    description, location, payment_method, payment_status, amount);
            expense.setId(id);
            expense.setProjectID(project_id);

            expenseList.add(expense);

            results.moveToNext();
        }

        results.close();
        return expenseList;
    }
}