package com.sielski.marcin.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PopularMoviesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_popular_movies);

        if (PopularMoviesUtils.getTheMoviesDBApiKey(this).length() !=
                getResources().getInteger(R.integer.length_themoviedb_api_key)) {
            startActivity(new Intent(this, PopularMoviesSettingsActivity.class));
            Toast.makeText(this,
                    getString(R.string.toast_themoviedb_api_key), Toast.LENGTH_LONG).show();
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.viewpager);
        if (viewPager != null) {
            Adapter adapter = new Adapter(getSupportFragmentManager());
            Fragment fragment = new PopularMoviesFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PopularMoviesUtils.SORT_CRITERION, PopularMoviesUtils.POPULAR);
            fragment.setArguments(bundle);
            adapter.addFragment(fragment, getString(R.string.title_most_popular));
            fragment = new PopularMoviesFragment();
            bundle = new Bundle();
            bundle.putString(PopularMoviesUtils.SORT_CRITERION, PopularMoviesUtils.TOP_RATED);
            fragment.setArguments(bundle);
            adapter.addFragment(fragment, getString(R.string.title_top_rated));
            fragment = new PopularMoviesFragment();
            bundle = new Bundle();
            bundle.putString(PopularMoviesUtils.SORT_CRITERION, PopularMoviesUtils.FAVORITE);
            fragment.setArguments(bundle);
            adapter.addFragment(fragment, getString(R.string.title_favorite));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(getPreferences(Context.MODE_PRIVATE)
                    .getInt(PopularMoviesUtils.SORT_CRITERION, 0));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    getPreferences(Context.MODE_PRIVATE).edit().
                            putInt(PopularMoviesUtils.SORT_CRITERION, position).apply();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

        }

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popular_movies_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, PopularMoviesSettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
