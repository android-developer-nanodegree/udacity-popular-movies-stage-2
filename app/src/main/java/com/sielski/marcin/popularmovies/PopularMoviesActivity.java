package com.sielski.marcin.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.sielski.marcin.popularmovies.util.PopularMoviesUtils;

import java.util.ArrayList;
import java.util.List;

public class PopularMoviesActivity extends AppCompatActivity {

    private Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
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
            viewPager.setOffscreenPageLimit(2);
            mAdapter = new Adapter(getSupportFragmentManager());
            if (savedInstanceState == null) {
                Fragment fragment = new PopularMoviesFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PopularMoviesUtils.SORT_CRITERION, PopularMoviesUtils.POPULAR);
                fragment.setArguments(bundle);
                mAdapter.addFragment(fragment, getString(R.string.title_most_popular));
                fragment = new PopularMoviesFragment();
                bundle = new Bundle();
                bundle.putString(PopularMoviesUtils.SORT_CRITERION, PopularMoviesUtils.TOP_RATED);
                fragment.setArguments(bundle);
                mAdapter.addFragment(fragment, getString(R.string.title_top_rated));
                fragment = new PopularMoviesFragment();
                bundle = new Bundle();
                bundle.putString(PopularMoviesUtils.SORT_CRITERION, PopularMoviesUtils.FAVORITE);
                fragment.setArguments(bundle);
                mAdapter.addFragment(fragment, getString(R.string.title_favorite));
            } else {
                mAdapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState,
                        getString(R.string.title_most_popular)), getString(R.string.title_most_popular));
                mAdapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState,
                        getString(R.string.title_top_rated)), getString(R.string.title_top_rated));
                mAdapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState,
                        getString(R.string.title_favorite)), getString(R.string.title_favorite));
            }
            viewPager.setAdapter(mAdapter);
            int index = getPreferences(Context.MODE_PRIVATE)
                    .getInt(PopularMoviesUtils.SORT_CRITERION, 0);
            viewPager.setCurrentItem(index);
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
            if (index == 2) {
                mAdapter.updateFavorite(index);
            }

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
            if (fragment != null) {
                mFragments.add(fragment);
                mFragmentTitles.add(title);
            }
        }

        void updateFavorite(int index) {
            for (int i = 0; i < mFragments.size(); i ++) {
                if ((index == 2) || (i != index)) {
                    Fragment fragment = mFragments.get(i);
                    FragmentManager fragmentManager = fragment.getFragmentManager();
                    if (fragmentManager != null) {
                        fragmentManager.beginTransaction().
                                detach(fragment).attach(fragment).commitNow();
                    }
                }
            }
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
        @NonNull
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    public void updateFavorite(int index) {
        if (mAdapter != null) {
            mAdapter.updateFavorite(index);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Fragment fragment = mAdapter.getItem(i);
                getSupportFragmentManager().putFragment(outState,
                        mAdapter.getPageTitle(i).toString(), fragment);
            }
        }
    }
}
