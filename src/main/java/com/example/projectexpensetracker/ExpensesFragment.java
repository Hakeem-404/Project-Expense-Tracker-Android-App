// source: https://abhiandroid.com/materialdesign/tablayout-example-android-studio.html#gsc.tab=0
// source: https://www.geeksforgeeks.org/how-to-implement-tablayout-with-icon-in-android/
// source: https://www.youtube.com/watch?v=LXl7D57fgOQ

package com.example.projectexpensetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ExpensesFragment extends Fragment implements ExpenseAdapter.OnExpenseListener {

    private static final String ARG_PROJECT_ID = "projectId";
    private static final int ADD_EXPENSE_REQUEST = 1;
    private static final int EDIT_EXPENSE_REQUEST = 2;

    private String projectId;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private FloatingActionButton fabExpenses;
    private List<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;
    private ExpenseDatabaseHelper expenseDatabaseHelper;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    public static ExpensesFragment newInstance(String projectId) {
        ExpensesFragment fragment = new ExpensesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ExpenseDatabaseHelper
        expenseDatabaseHelper = new ExpenseDatabaseHelper(getContext());
        // Get project ID from arguments
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }

        // Initialize expense list
        expenseList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.expense_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Set up FAB to add new expense
        setupFabListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch expenses for the current project
        loadExpenses();
    }

    private void loadExpenses() {
        // Fetch expenses from database
        List<Expense> expenses = expenseDatabaseHelper.getExpensesByProjectId(projectId);
        expenseList.clear();
        expenseList.addAll(expenses);
        expenseAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void initializeViews(View view) {
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView = view.findViewById(R.id.recyclerView);
        fabExpenses = view.findViewById(R.id.fabExpenses);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenseList, this);
        recyclerView.setAdapter(expenseAdapter);

        // Update empty state
        updateEmptyState();
    }

    private void setupFabListener() {
        fabExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
            intent.putExtra("projectId", projectId);
            startActivityForResult(intent, ADD_EXPENSE_REQUEST);
        });
    }

    private void updateEmptyState() {
        if (expenseList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            // Reload expenses from database to ensure UI is in sync
            loadExpenses();

            // Show appropriate success message
            if (requestCode == ADD_EXPENSE_REQUEST) {
                Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            } else if (requestCode == EDIT_EXPENSE_REQUEST && data != null) {
                boolean isDelete = data.getBooleanExtra("delete", false);
                if (isDelete) {
                    Toast.makeText(getContext(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onExpenseClick(int position) {
        // Open expense details/edit activity
        Expense selectedExpense = expenseList.get(position);
        Intent intent = new Intent(getActivity(), EditExpenseActivity.class);
        intent.putExtra("expense", selectedExpense);
        startActivityForResult(intent, EDIT_EXPENSE_REQUEST);
    }

    @Override
    public void onDeleteClick(int position) {
        // Confirm and delete expense
        Expense expenseToDelete = expenseList.get(position);

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove from database
                    boolean result = expenseDatabaseHelper.deleteExpense(expenseToDelete.getId());
                    if (result) {
                        // Remove from list and update UI
                        expenseList.remove(position);
                        expenseAdapter.notifyItemRemoved(position);
                        updateEmptyState();
                        Toast.makeText(getContext(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Method to get total expenses for the project
    public double getTotalExpenses() {
        double total = 0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }
        return total;
    }

    @Override
    public void onDestroy() {
        if (expenseDatabaseHelper != null) {
            expenseDatabaseHelper.close();
        }
        super.onDestroy();
    }
}