package com.example.projectexpensetracker;

//https://www.geeksforgeeks.org/how-to-create-and-add-data-to-sqlite-database-in-android/

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
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final String TABLE_NAME = "project";

    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "project_name";
    public static final String PROJECT_ID_COLUMN = "project_id";
    public static final String MANAGER_COLUMN = "manager";
    public static final String PROJECT_STATUS_COLUMN = "project_status";
    public static final String START_DATE_COLUMN = "start_date";
    public static final String END_DATE_COLUMN = "end_date";
    public static final String BUDGET_COLUMN = "budget";
    public static final String PROJECT_DESC_COLUMN = "project_desc";
    public static final String SPECIAL_REQ_COLUMN = "special_req";
    public static final String CLIENT_INFO_COLUMN = "client_info";

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
                    "   %s TEXT)",
            TABLE_NAME, ID_COLUMN, NAME_COLUMN, PROJECT_ID_COLUMN, MANAGER_COLUMN, PROJECT_STATUS_COLUMN,
            START_DATE_COLUMN, END_DATE_COLUMN, BUDGET_COLUMN, PROJECT_DESC_COLUMN, SPECIAL_REQ_COLUMN, CLIENT_INFO_COLUMN);

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        database = getWritableDatabase();
    }

    //Creates the project table with all necessary columns when the database is first created
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

    //Inserts a new project into the database using individual parameters
    public long insertProject(String projectName, String projectID, String manager, String projectStatus,
                              Date startDate, Date endDate, double budget,
                              String projectDesc, String specialReq, String clientInfo) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(NAME_COLUMN, projectName);
        rowValues.put(PROJECT_ID_COLUMN, projectID);
        rowValues.put(MANAGER_COLUMN, manager);
        rowValues.put(PROJECT_STATUS_COLUMN, projectStatus);

        //convert string to date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        rowValues.put(START_DATE_COLUMN, dateFormat.format(startDate));
        rowValues.put(END_DATE_COLUMN, dateFormat.format(endDate));

        rowValues.put(BUDGET_COLUMN, budget);
        rowValues.put(PROJECT_DESC_COLUMN, projectDesc);
        rowValues.put(SPECIAL_REQ_COLUMN, specialReq);
        rowValues.put(CLIENT_INFO_COLUMN, clientInfo);

        return database.insertOrThrow(TABLE_NAME, null, rowValues);
    }

    //Overloaded method that inserts a Project object into the database
    public long insertProject(Project p) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(NAME_COLUMN, p.getProjectName());
        rowValues.put(PROJECT_ID_COLUMN, p.getProjectID());
        rowValues.put(MANAGER_COLUMN, p.getManager());
        rowValues.put(PROJECT_STATUS_COLUMN, p.getProjectStatus());

        // Convert Date to String format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (p.getStartDate() != null) {
            rowValues.put(START_DATE_COLUMN, dateFormat.format(p.getStartDate()));
        }
        if (p.getEndDate() != null) {
            rowValues.put(END_DATE_COLUMN, dateFormat.format(p.getEndDate()));
        }

        rowValues.put(BUDGET_COLUMN, p.getBudget());
        rowValues.put(PROJECT_DESC_COLUMN, p.getProjectDesc());
        rowValues.put(SPECIAL_REQ_COLUMN, p.getSpecialReq());
        rowValues.put(CLIENT_INFO_COLUMN, p.getClientInfo());

        return database.insertOrThrow(TABLE_NAME, null, rowValues);
    }

    //Updates a project in the database using a Project object
    public boolean updateProject(Project p) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(NAME_COLUMN, p.getProjectName());
        rowValues.put(PROJECT_ID_COLUMN, p.getProjectID());
        rowValues.put(MANAGER_COLUMN, p.getManager());
        rowValues.put(PROJECT_STATUS_COLUMN, p.getProjectStatus());

        // Convert Date to String format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (p.getStartDate() != null) {
            rowValues.put(START_DATE_COLUMN, dateFormat.format(p.getStartDate()));
        }
        if (p.getEndDate() != null) {
            rowValues.put(END_DATE_COLUMN, dateFormat.format(p.getEndDate()));
        }

        rowValues.put(BUDGET_COLUMN, p.getBudget());
        rowValues.put(PROJECT_DESC_COLUMN, p.getProjectDesc());
        rowValues.put(SPECIAL_REQ_COLUMN, p.getSpecialReq());
        rowValues.put(CLIENT_INFO_COLUMN, p.getClientInfo());

        // Update where id matches
        return database.update(TABLE_NAME, rowValues, ID_COLUMN + "=?",
                new String[] { String.valueOf(p.getId()) }) > 0;
    }

    //Deletes a project from the database using the id
    public boolean deleteProject(int id) {
        return database.delete(TABLE_NAME, ID_COLUMN + "=?",
                new String[] { String.valueOf(id) }) > 0;
    }

    //Retrieves all projects and returns them as a formatted string
    public String getProjectString() {
        Cursor results = database.query("project", new String[] {"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                null, null, null, null, "project_name");

        StringBuilder resultText = new StringBuilder();
        results.moveToFirst();
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String project_name = results.getString(1);
            String project_id = results.getString(2);
            String manager = results.getString(3);
            String project_status = results.getString(4);
            String start_date = results.getString(5);
            String end_date = results.getString(6);
            double budget = results.getDouble(7);
            String project_desc = results.getString(8);
            String special_req = results.getString(9);
            String client_info = results.getString(10);

            resultText.append(id).append(" ").append(project_name).append(" ").append(project_id).append(" ")
                    .append(manager).append(" ").append(project_status).append(" ").append(start_date).append(" ")
                    .append(end_date).append(" ").append(budget).append(" ").append(project_desc).append(" ")
                    .append(special_req).append(" ").append(client_info).append("\n");

            results.moveToNext();
        }

        results.close();
        return resultText.toString();
    }

    //Retrieves all projects and returns them as an ArrayList of Project objects
    public ArrayList<Project> getProject() {
        Cursor results = database.query("project", new String[] {"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                null, null, null, null, "project_name");

        ArrayList<Project> projectList = new ArrayList<Project>();

        results.moveToFirst();
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String project_name = results.getString(1);
            String project_id = results.getString(2);
            String manager = results.getString(3);
            String project_status = results.getString(4);
            String start_date = results.getString(5);
            String end_date = results.getString(6);
            double budget = results.getDouble(7);
            String project_desc = results.getString(8);
            String special_req = results.getString(9);
            String client_info = results.getString(10);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date startDate, endDate;
            try {
                startDate = dateFormat.parse(start_date);
                endDate = dateFormat.parse(end_date);
            } catch (ParseException e) {
                startDate = new Date();
                endDate = new Date();
            }

            // Create a Project object
            Project project = new Project(project_name, project_id, manager, project_status,
                    startDate, endDate, budget, project_desc, special_req, client_info);

            // Set the ID - this is crucial for updating the project later
            project.setId(id);

            projectList.add(project);

            results.moveToNext();
        }

        results.close();
        return projectList;
    }

    //Retrieves a project by name and returns it as a Project object
    public ArrayList<Project> getProjectByName(String projectName) {
        ArrayList<Project> projectList = new ArrayList<>();

        Cursor result = database.query(TABLE_NAME,
                new String[] {"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                "LOWER(" + NAME_COLUMN + ") LIKE LOWER(?)", new String[] { "%" + projectName + "%" },
                null, null, null);

        if (result != null) {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                int id = result.getInt(0);
                String project_name = result.getString(1);
                String project_id = result.getString(2);
                String manager = result.getString(3);
                String project_status = result.getString(4);
                String start_date = result.getString(5);
                String end_date = result.getString(6);
                double budget = result.getDouble(7);
                String project_desc = result.getString(8);
                String special_req = result.getString(9);
                String client_info = result.getString(10);

                // Parse date strings to Date objects
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate, endDate;
                try {
                    startDate = dateFormat.parse(start_date);
                    endDate = dateFormat.parse(end_date);
                } catch (ParseException e) {
                    startDate = new Date();
                    endDate = new Date();
                }

                // Create the Project object with all required parameters
                Project project = new Project(project_name, project_id, manager, project_status,
                        startDate, endDate, budget, project_desc, special_req, client_info);

                // Set the ID separately if your Project class has an ID setter
                project.setId(id);

                projectList.add(project);
                result.moveToNext();
            }
            result.close();
        }

        return projectList;
    }

    //Retrieves projects by description and returns it as a Project object
    public ArrayList<Project> getProjectsByDescription(String projectDesc) {
        ArrayList<Project> projectList = new ArrayList<>();

        Cursor result = database.query(TABLE_NAME,
                new String[]{"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                "LOWER(" + PROJECT_DESC_COLUMN + ") LIKE LOWER(?)", new String[]{"%" + projectDesc + "%"},
                null, null, null);

        if (result != null) {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                int id = result.getInt(0);
                String project_name = result.getString(1);
                String project_id = result.getString(2);
                String manager = result.getString(3);
                String project_status = result.getString(4);
                String start_date = result.getString(5);
                String end_date = result.getString(6);
                double budget = result.getDouble(7);
                String project_desc = result.getString(8);
                String special_req = result.getString(9);
                String client_info = result.getString(10);

                // Parse date strings to Date objects
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate, endDate;
                try {
                    startDate = dateFormat.parse(start_date);
                    endDate = dateFormat.parse(end_date);
                } catch (ParseException e) {
                    startDate = new Date();
                    endDate = new Date();
                }

                // Create the Project object with all required parameters
                Project project = new Project(project_name, project_id, manager, project_status,
                        startDate, endDate, budget, project_desc, special_req, client_info);

                // Set the ID separately if your Project class has an ID setter
                project.setId(id);

                projectList.add(project);
                result.moveToNext();
            }
            result.close();
        }
        return projectList;
    }

    //from claude AI
    /**
     * Retrieves projects that match a specific date or fall within a date range
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive), can be same as startDate for single date search
     * @param searchType The type of search: "start" for start dates, "end" for end dates, "both" for either
     * @return ArrayList of Project objects that match the criteria
     */
    public ArrayList<Project> getProjectsByDate(Date startDate, Date endDate, String searchType) {
        ArrayList<Project> projectList = new ArrayList<>();

        // Convert Date objects to string format used in the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Build the WHERE clause based on search type
        String whereClause;
        String[] whereArgs;

        if (searchType.equalsIgnoreCase("start")) {
            // Search only in start dates
            if (startDate.equals(endDate)) {
                // Exact date match
                whereClause = START_DATE_COLUMN + "=?";
                whereArgs = new String[] { startDateStr };
            } else {
                // Date range
                whereClause = START_DATE_COLUMN + " BETWEEN ? AND ?";
                whereArgs = new String[] { startDateStr, endDateStr };
            }
        } else if (searchType.equalsIgnoreCase("end")) {
            // Search only in end dates
            if (startDate.equals(endDate)) {
                // Exact date match
                whereClause = END_DATE_COLUMN + "=?";
                whereArgs = new String[] { startDateStr };
            } else {
                // Date range
                whereClause = END_DATE_COLUMN + " BETWEEN ? AND ?";
                whereArgs = new String[] { startDateStr, endDateStr };
            }
        } else {
            // Search in both start and end dates
            if (startDate.equals(endDate)) {
                // Exact date match
                whereClause = START_DATE_COLUMN + "=? OR " + END_DATE_COLUMN + "=?";
                whereArgs = new String[] { startDateStr, startDateStr };
            } else {
                // Date range
                whereClause = START_DATE_COLUMN + " BETWEEN ? AND ? OR " +
                        END_DATE_COLUMN + " BETWEEN ? AND ?";
                whereArgs = new String[] { startDateStr, endDateStr, startDateStr, endDateStr };
            }
        }

        Cursor result = database.query(
                TABLE_NAME,
                new String[] {"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                whereClause,
                whereArgs,
                null, null, START_DATE_COLUMN);

        if (result != null) {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                int id = result.getInt(0);
                String project_name = result.getString(1);
                String project_id = result.getString(2);
                String manager = result.getString(3);
                String project_status = result.getString(4);
                String start_date = result.getString(5);
                String end_date = result.getString(6);
                double budget = result.getDouble(7);
                String project_desc = result.getString(8);
                String special_req = result.getString(9);
                String client_info = result.getString(10);

                // Parse date strings to Date objects
                Date projectStartDate, projectEndDate;
                try {
                    projectStartDate = dateFormat.parse(start_date);
                    projectEndDate = dateFormat.parse(end_date);
                } catch (ParseException e) {
                    projectStartDate = new Date();
                    projectEndDate = new Date();
                }

                // Create the Project object with all required parameters
                Project project = new Project(project_name, project_id, manager, project_status,
                        projectStartDate, projectEndDate, budget, project_desc, special_req, client_info);

                // Set the ID separately if your Project class has an ID setter
                project.setId(id);

                projectList.add(project);
                result.moveToNext();
            }
            result.close();
        }

        return projectList;
    }
    public ArrayList<Project> getProjectsByStatus(String projectStatus) {
        ArrayList<Project> projectList = new ArrayList<>();

        Cursor result = database.query(TABLE_NAME,
                new String[]{"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                "LOWER(" + PROJECT_DESC_COLUMN + ") LIKE LOWER(?)", new String[]{"%" + projectStatus + "%"},
                null, null, null);

        if (result != null) {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                int id = result.getInt(0);
                String project_name = result.getString(1);
                String project_id = result.getString(2);
                String manager = result.getString(3);
                String project_status = result.getString(4);
                String start_date = result.getString(5);
                String end_date = result.getString(6);
                double budget = result.getDouble(7);
                String project_desc = result.getString(8);
                String special_req = result.getString(9);
                String client_info = result.getString(10);

                // Parse date strings to Date objects
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate, endDate;
                try {
                    startDate = dateFormat.parse(start_date);
                    endDate = dateFormat.parse(end_date);
                } catch (ParseException e) {
                    startDate = new Date();
                    endDate = new Date();
                }

                // Create the Project object with all required parameters
                Project project = new Project(project_name, project_id, manager, project_status,
                        startDate, endDate, budget, project_desc, special_req, client_info);

                // Set the ID separately if your Project class has an ID setter
                project.setId(id);

                projectList.add(project);
                result.moveToNext();
            }
            result.close();
        }
        return projectList;
    }

    public ArrayList<Project> getProjectsByManager(String manager) {
        ArrayList<Project> projectList = new ArrayList<>();

        Cursor result = database.query(TABLE_NAME,
                new String[]{"id", "project_name", "project_id", "manager", "project_status",
                        "start_date", "end_date", "budget", "project_desc", "special_req", "client_info"},
                "LOWER(" + PROJECT_DESC_COLUMN + ") LIKE LOWER(?)", new String[]{"%" + manager + "%"},
                null, null, null);

        if (result != null) {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                int id = result.getInt(0);
                String project_name = result.getString(1);
                String project_id = result.getString(2);
                String manager_name = result.getString(3);
                String project_status = result.getString(4);
                String start_date = result.getString(5);
                String end_date = result.getString(6);
                double budget = result.getDouble(7);
                String project_desc = result.getString(8);
                String special_req = result.getString(9);
                String client_info = result.getString(10);

                // Parse date strings to Date objects
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate, endDate;
                try {
                    startDate = dateFormat.parse(start_date);
                    endDate = dateFormat.parse(end_date);
                } catch (ParseException e) {
                    startDate = new Date();
                    endDate = new Date();
                }

                // Create the Project object with all required parameters
                Project project = new Project(project_name, project_id, manager_name, project_status,
                        startDate, endDate, budget, project_desc, special_req, client_info);

                // Set the ID separately if your Project class has an ID setter
                project.setId(id);

                projectList.add(project);
                result.moveToNext();
            }
            result.close();
        }
        return projectList;
    }


}