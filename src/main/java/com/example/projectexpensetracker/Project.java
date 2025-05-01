//Source: https://medium.com/@kkunalandroid/serializable-in-android-with-example-38eb8b7334ea
package com.example.projectexpensetracker;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Project implements Serializable {
    private String projectName;
    private String projectID;
    private double budget;
    private String manager;
    private Date startDate;
    private Date endDate;
    private String projectDesc;
    private String specialReq;
    private String clientInfo;
    private String projectStatus;

    private int id;


    // Constructor that takes string values (for use with form data)
    public Project(String projectName, String projectID, String manager, String projectStatus,
                   String startDateStr, String endDateStr, String budgetStr,
                   String projectDesc, String specialReq, String clientInfo) {

        this.projectName = projectName;
        this.projectID = projectID;
        this.manager = manager;
        this.projectStatus = projectStatus;
        this.projectDesc = projectDesc;
        this.specialReq = specialReq;
        this.clientInfo = clientInfo;

        // Parse budget
        try {
            this.budget = Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            this.budget = 0.0;
        }

        // Parse dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            this.startDate = dateFormat.parse(startDateStr);
            this.endDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            this.startDate = new Date();
            this.endDate = new Date();
        }
    }

    // Constructor that takes Date objects and double for budget (for AddProjectActivity)
    public Project(String projectName, String projectID, String manager, String projectStatus,
                   Date startDate, Date endDate, double budget,
                   String projectDesc, String specialReq, String clientInfo) {

        this.projectName = projectName;
        this.projectID = projectID;
        this.manager = manager;
        this.projectStatus = projectStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.projectDesc = projectDesc;
        this.specialReq = specialReq;
        this.clientInfo = clientInfo;
//        this.id = -1;
    }

    // Constructor that takes Date objects and double for database
    public Project(int id, String projectName, String projectID, String manager, String projectStatus,
                   Date startDate, Date endDate, double budget,
                   String projectDesc, String specialReq, String clientInfo) {

        this.id = id;
        this.projectName = projectName;
        this.projectID = projectID;
        this.manager = manager;
        this.projectStatus = projectStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.projectDesc = projectDesc;
        this.specialReq = specialReq;
        this.clientInfo = clientInfo;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public void setSpecialReq(String specialReq) {
        this.specialReq = specialReq;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    //Getters
    public int getId() {
        return id;
    }

    public String getManager() {
        return manager;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectID() {
        return projectID;
    }

    public double getBudget() {
        return budget;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public String getSpecialReq() {
        return specialReq;
    }

    public String getProjectStatus() {
        return projectStatus;
    }
}