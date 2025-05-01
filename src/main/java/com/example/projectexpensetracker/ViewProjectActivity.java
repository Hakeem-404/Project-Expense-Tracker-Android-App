// source: https://abhiandroid.com/materialdesign/tablayout-example-android-studio.html#gsc.tab=0
// source: https://www.geeksforgeeks.org/how-to-implement-tablayout-with-icon-in-android/
// source: https://www.youtube.com/watch?v=LXl7D57fgOQ

package com.example.projectexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;

public class ViewProjectActivity extends AppCompatActivity implements ProjectDetailsFragment.DeleteProjectListener {
    private Project currentProject;
    private int projectPosition;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    // Constant for edit project request code
    private static final int REQUEST_CODE_EDIT_PROJECT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_info);

        // Retrieve the project and position from the intent
        currentProject = (Project) getIntent().getSerializableExtra("project");
        projectPosition = getIntent().getIntExtra("position", -1);

        // Initialize TabLayout and ViewPager2
        initializeViews();
        setupTabLayout();
    }

    private void initializeViews() {
        // Initialize TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupTabLayout() {
        // Set up ViewPager with adapter
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Project Details");
            } else {
                tab.setText("Expenses");
            }
        }).attach();
    }

    // Implementation of DeleteProjectListener interface
    @Override
    public void onDeleteProject(Project project) {
        // First, actually delete the project from the database
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        boolean deleted = databaseHelper.deleteProject(project.getId());
        databaseHelper.close();

            // Create intent to send back to the MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("project", project);
        resultIntent.putExtra("action", "delete");
        resultIntent.putExtra("position", projectPosition);
        setResult(RESULT_OK, resultIntent);
        finish(); // This will return to MainActivity
    }

    // ViewPager Adapter
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return ProjectDetailsFragment.newInstance(currentProject);
            } else {
                return ExpensesFragment.newInstance(currentProject.getProjectID());
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Two tabs: Project Details and Expenses
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Forward results to fragments
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        // If we have updated project data, also pass it back to MainActivity
        if (requestCode == REQUEST_CODE_EDIT_PROJECT && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("project")) {
                Project updatedProject = (Project) data.getSerializableExtra("project");
                if (updatedProject != null) {
                    // Update our current reference
                    currentProject = updatedProject;

                    // Forward to MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("project", updatedProject);
                    resultIntent.putExtra("action", "update");
                    resultIntent.putExtra("position", projectPosition);
                    setResult(RESULT_OK, resultIntent);
                }
            }
        }
    }
}