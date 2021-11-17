package com.highresults.tabbedmediaapplication;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.highresults.tabbedmediaapplication.ui.main.SectionsPagerAdapter;

public class MainActivity extends FragmentActivity {

    public static ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, MainActivity.viewPager,
                (tab, position) -> tab.setText("tab" + (position + 1))
        ).attach();
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }


}