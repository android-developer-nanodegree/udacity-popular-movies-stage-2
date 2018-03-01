package com.sielski.marcin.popularmovies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.URL;

public class PopularMovieDetailsFragment extends Fragment {


    private Menu mMenu;
    private ContentValues mPopularMovie;
    private ContentValues[] mPopularMovieReviews;
    private ContentValues[] mPopularMovieVideos;
    private ProgressBar mProgressBar;
    private ImageView mPopularMovieBackdrop;
    private LinearLayout mPopularMovieDetails;

    public PopularMovieDetailsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("StaticFieldLeak")
    private class TheMovieDBQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String theMovieDBResult = null;
            try {
                theMovieDBResult = PopularMoviesUtils.getResponseFromHttpUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return theMovieDBResult;
        }
    }

    private void displayPopularMovieReviews() {
        Activity activity = getActivity();
        if (activity != null && mPopularMovieReviews != null) {
            for (ContentValues popularMovieReview : mPopularMovieReviews) {
                View view = activity.getLayoutInflater().inflate(R.layout.review_popular_movie,
                        mPopularMovieDetails, false);
                ((TextView) view.findViewWithTag(getString(R.string.tag_review_author)))
                        .setText(String.format("%s %s", getString(R.string.text_review_written_by),
                                popularMovieReview
                                        .getAsString(PopularMoviesContract.Reviews.COLUMN_AUTHOR)));
                ((TextView) view.findViewWithTag(getString(R.string.tag_review_content)))
                        .setText(popularMovieReview
                                .getAsString(PopularMoviesContract.Reviews.COLUMN_CONTENT));
                mPopularMovieDetails.addView(view);
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private final class TheMovieDBQueryTaskReviews
            extends PopularMovieDetailsFragment.TheMovieDBQueryTask {
        @Override
        protected void onPostExecute(String s) {
            if (s != null && s.length() > 0) {
                mPopularMovieReviews = PopularMoviesUtils.getPopularMovieReviews(s);
                displayPopularMovieReviews();
            }
        }
    }

    private void displayPopularMovieVideos() {
        for (ContentValues popularMovieVideo: mPopularMovieVideos) {
            if (PopularMoviesUtils.isSupported(popularMovieVideo.getAsString(
                    PopularMoviesContract.Videos.COLUMN_SITE))) {
                mMenu.add(popularMovieVideo.getAsString(
                        PopularMoviesContract.Videos.COLUMN_NAME))
                        .setOnMenuItemClickListener((item) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    PopularMoviesUtils.buildVideoUri(popularMovieVideo
                                            .getAsString(PopularMoviesContract.Videos.COLUMN_KEY)));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            return true;
                        });
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private final class TheMovieDBQueryTaskVideos
            extends PopularMovieDetailsFragment.TheMovieDBQueryTask {
        @Override
        protected void onPostExecute(String s) {
            if (s != null && s.length() > 0) {
                mPopularMovieVideos = PopularMoviesUtils.getPopularMovieVideos(s);
                displayPopularMovieVideos();
            }
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(
                R.layout.fragment_popular_movie_details, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPopularMovie = bundle.getParcelable(PopularMoviesContract.Details.TABLE_NAME);
        }

        mPopularMovieDetails = view.findViewById(R.id.details);

        if (savedInstanceState == null) {
            new TheMovieDBQueryTaskReviews().execute(
                    PopularMoviesUtils.buildApiUrl(getContext(),
                            mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ID),
                            PopularMoviesUtils.REVIEWS));
        } else {
            mPopularMovieReviews = (ContentValues[]) savedInstanceState.getParcelableArray(
                    PopularMoviesContract.Reviews.TABLE_NAME);
            mPopularMovieVideos = (ContentValues[]) savedInstanceState.getParcelableArray(
                    PopularMoviesContract.Videos.TABLE_NAME);
            displayPopularMovieReviews();
        }

        Activity activity = getActivity();
        if (activity instanceof PopularMovieDetailsActivity) {
            PopularMovieDetailsActivity popularMovieDetailsActivity =
                    (PopularMovieDetailsActivity) activity;
            popularMovieDetailsActivity.setSupportActionBar(view.findViewById(R.id.toolbar));
            ActionBar actionBar = popularMovieDetailsActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mPopularMovie.getAsString(
                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE));
            }
        } else {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            mMenu = toolbar.getMenu();
            if (mPopularMovieVideos == null) {
                new TheMovieDBQueryTaskVideos().execute(
                        PopularMoviesUtils.buildApiUrl(getContext(),
                                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ID),
                                PopularMoviesUtils.VIDEOS));
            } else {
                displayPopularMovieVideos();
            }
        }

        ((CollapsingToolbarLayout)view.findViewById(R.id.collapsing_toolbar)).setTitle(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE));

        FloatingActionButton floatingActionButton = view.findViewById(R.id.action_favorite);
        if (PopularMoviesContract.isPopularMovieFavorite(getContext(), mPopularMovie)) {
            floatingActionButton.setImageResource(R.drawable.ic_star_white_48dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_star_black_48dp);
        }
        view.findViewById(R.id.action_favorite).setOnClickListener((v) -> {
            if (PopularMoviesContract.isPopularMovieFavorite(getContext(), mPopularMovie)) {
                PopularMoviesContract.removePopularMovieFromFavorite(getContext(), mPopularMovie);
                ((FloatingActionButton) v).setImageResource(R.drawable.ic_star_black_48dp);
                Toast.makeText(getContext(), String.format("%s %s", mPopularMovie.getAsString(
                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE),
                        getString(R.string.toast_removed_from_favorites)), Toast.LENGTH_SHORT).show();
            } else {
                PopularMoviesContract.addPopularMovieToFavorite(getContext(), mPopularMovie);
                ((FloatingActionButton) v).setImageResource(R.drawable.ic_star_white_48dp);
                Toast.makeText(getContext(), String.format("%s %s", mPopularMovie.getAsString(
                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE),
                        getString(R.string.toast_added_to_favorites)), Toast.LENGTH_SHORT).show();
            }
            if (activity instanceof PopularMoviesActivity) {
                ((PopularMoviesActivity) activity).updateFavorite(
                        ((ViewPager)activity.findViewById(R.id.viewpager)).getCurrentItem());
            }
        });
        ((TextView)view.findViewById(R.id.details_overview)).setText(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_OVERVIEW));
        ((TextView)view.findViewById(R.id.details_vote_average)).setText(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_VOTE_AVERAGE));
        ((TextView)view.findViewById(R.id.details_release_date)).setText(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_RELEASE_DATE));
        mProgressBar = view.findViewById(R.id.progress_popular_movie_details);
        mPopularMovieBackdrop = view.findViewById(R.id.backdrop);
        return view;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (PopularMoviesUtils.isNetworkAvailable(context)) {
                Target target = new Target() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mPopularMovieBackdrop.setImageBitmap(bitmap);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                };
                mPopularMovieBackdrop.setTag(target);
                Picasso.with(context).load(mPopularMovie.getAsString(
                        PopularMoviesContract.Details.COLUMN_BACKDROP_PATH))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(target);
            } else {
                Toast.makeText(context, context.getString(R.string.toast_network_unavailable),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        if (activity != null) {
            activity.registerReceiver(broadcastReceiver,
                    new IntentFilter(PopularMoviesUtils.ACTION_NETWORK_CHANGE));
        }
    }

    @Override
    public void onStop() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(broadcastReceiver);
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(PopularMoviesContract.Reviews.TABLE_NAME,
                mPopularMovieReviews);
        outState.putParcelableArray(PopularMoviesContract.Videos.TABLE_NAME,
                mPopularMovieVideos);
    }

}
