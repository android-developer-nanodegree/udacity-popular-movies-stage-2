package com.sielski.marcin.popularmovies;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PopularMoviesFragment extends Fragment {
    private String mSortCriterion;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        mSortCriterion = PopularMoviesUtils.POPULAR;
        if (args != null) {
            mSortCriterion = args.getString(PopularMoviesUtils.SORT_CRITERION);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private final class TheMovieDBQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String theMovieDBResult = null;
            try {
                if (mSortCriterion.equals(PopularMoviesUtils.FAVORITE)) {
                    theMovieDBResult = PopularMoviesUtils.getResponseFromContentProvider(getContext());
                } else {
                    theMovieDBResult = PopularMoviesUtils.getResponseFromHttpUrl(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return theMovieDBResult;
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (s != null && s.length() > 0) {

                List<ContentValues> popularMovies = PopularMoviesUtils.getPopularMovies(s);
                mRecyclerView.setAdapter(new PopularMoviesAdapter(getActivity(), popularMovies,
                        mSortCriterion));
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_popular_movies, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mRecyclerView.getContext(),
                PopularMoviesUtils.spanCount(getContext()));
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mProgressBar = view.findViewById(R.id.progress_popular_movies);
        return view;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PopularMoviesUtils.isNetworkAvailable(getContext())) {
                new TheMovieDBQueryTask().execute(PopularMoviesUtils.buildApiUrl(getContext(),
                        mSortCriterion));
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        ((GridLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(
                PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getInt(mSortCriterion, 0), 0);
        Context context = getContext();
        if (context != null) context.registerReceiver(broadcastReceiver,
                new IntentFilter(PopularMoviesUtils.ACTION_NETWORK_CHANGE));
    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context != null) context.unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}
