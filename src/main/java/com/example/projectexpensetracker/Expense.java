//Source: https://medium.com/@kkunalandroid/serializable-in-android-with-example-38eb8b7334ea
package com.example.projectexpensetracker;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Expense implements Serializable {
    private String expenseType;
    private String expenseID;
    private double amount;
    private String claimant;
    private Date expenseDate;
    private String description;
    private String location;
    private String paymentMethod;
    private String paymentStatus;
    private String currency;
    private int id;
    private String projectID; //foreign key


    // Constructor that takes string values (for use with form data)
    public Expense(String expenseType, String expenseID, String claimant, String currency, Date expenseDate,
                   String description, String location, String paymentMethod, String paymentStatus, double amount) {

        this.expenseType = expenseType;
        this.expenseID = expenseID;
        this.claimant = claimant;
        this.currency = currency;
        this.description = description;
        this.location = location;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.expenseDate = expenseDate;
//        this.id = UUID.randomUUID().toString();
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public void setExpenseID(String expenseID) {
        this.expenseID = expenseID;
    }

    public void setAmount(double amount)  {
        this.amount = amount;
    }

    public void setClaimant(String claimant) {
        this.claimant = claimant;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    //Getters
    public int getId() {
        return id;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public String getClaimant() {
        return claimant;
    }

    public String getExpenseID() {
        return expenseID;
    }

    public double getAmount() {
        return amount;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProjectID() {
        return projectID;
    }
}