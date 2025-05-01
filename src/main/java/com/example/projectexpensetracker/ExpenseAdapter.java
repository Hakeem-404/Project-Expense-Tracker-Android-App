package com.example.projectexpensetracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenseList;
    private OnExpenseListener onExpenseListener;
    private SimpleDateFormat dateFormat;

    public ExpenseAdapter(List<Expense> expenseList, OnExpenseListener onExpenseListener) {
        this.expenseList = expenseList;
        this.onExpenseListener = onExpenseListener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view, onExpenseListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Set text fields
        holder.expenseIdTextView.setText(expense.getExpenseID());
        holder.claimantTextView.setText(expense.getClaimant());
        holder.amountTextView.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
        holder.expenseDateTextView.setText(dateFormat.format(expense.getExpenseDate()));
        holder.currencyTextView.setText(expense.getCurrency());

        // Set spinners
        holder.paymentStatusTextView.setText(expense.getPaymentStatus());
        holder.paymentMethodTextView.setText(expense.getPaymentMethod());
        holder.expenseTypeTextView.setText(expense.getExpenseType());

        // Set color based on status
        String status = expense.getPaymentStatus();
        if (status.equalsIgnoreCase("Paid")) {
            holder.paymentStatusTextView.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (status.equalsIgnoreCase("Reimbursed")) {
            holder.paymentStatusTextView.setTextColor(Color.parseColor("#2196F3")); // Blue
        } else if (status.equalsIgnoreCase("Pending")) {
            holder.paymentStatusTextView.setTextColor(Color.parseColor("#FFC107")); // Yellow/Amber
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView expenseTypeTextView, expenseIdTextView, claimantTextView, amountTextView;
        TextView expenseDateTextView, currencyTextView, paymentMethodTextView, paymentStatusTextView;
        ImageButton deleteButton;
        OnExpenseListener onExpenseListener;

        public ExpenseViewHolder(@NonNull View itemView, final OnExpenseListener onExpenseListener) {
            super(itemView);

            // Find all views
            expenseTypeTextView = itemView.findViewById(R.id.expenseTypeTextView);
            expenseIdTextView = itemView.findViewById(R.id.expenseIdTextView);
            claimantTextView = itemView.findViewById(R.id.claimantTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            expenseDateTextView = itemView.findViewById(R.id.expenseDateTextView);
            paymentMethodTextView = itemView.findViewById(R.id.paymentMethodTextView);
            currencyTextView = itemView.findViewById(R.id.currencyTextView);
            paymentStatusTextView = itemView.findViewById(R.id.paymentStatusTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            this.onExpenseListener = onExpenseListener;

            // Set click listeners
            itemView.setOnClickListener(this);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onExpenseListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onExpenseListener.onDeleteClick(position);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            onExpenseListener.onExpenseClick(getAdapterPosition());
        }
    }

    public interface OnExpenseListener {
        void onExpenseClick(int position);
        void onDeleteClick(int position);
    }

    // Helper method to update the expense list
    public void updateExpenses(List<Expense> expenses) {
        this.expenseList = expenses;
        notifyDataSetChanged();
    }

    // Helper method to remove a expense at a specific position
    public void removeExpense(int position) {
        if (position >= 0 && position < expenseList.size()) {
            expenseList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, expenseList.size());
        }
    }
}