package com.sielski.marcin.popularmovies;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.IOException;
import java.net.URL;

public class PopularMovieDetailsActivity extends AppCompatActivity {

    private Menu mMenu;
    private ContentValues mPopularMovie;
    private ContentValues[] mPopularMovieReviews;
    private ContentValues[] mPopularMovieVideos;
    private ProgressBar mProgressBar;

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
        LinearLayout linearLayout = findViewById(R.id.details);
        for (ContentValues popularMovieReview: mPopularMovieReviews) {
            View view = getLayoutInflater().inflate(R.layout.review_popular_movie,
                    linearLayout, false);
            ((TextView) view.findViewWithTag(getString(R.string.tag_review_author)))
                    .setText(String.format("%s %s", getString(R.string.text_review_written_by),
                            popularMovieReview
                                    .getAsString(PopularMoviesContract.Reviews.COLUMN_AUTHOR)));
            ((TextView) view.findViewWithTag(getString(R.string.tag_review_content)))
                    .setText(popularMovieReview
                            .getAsString(PopularMoviesContract.Reviews.COLUMN_CONTENT));
            linearLayout.addView(view);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private final class TheMovieDBQueryTaskReviews extends TheMovieDBQueryTask {
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
                            getApplication().startActivity(intent);
                            return true;
                        });
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private final class TheMovieDBQueryTaskVideos extends TheMovieDBQueryTask {
        @Override
        protected void onPostExecute(String s) {
            if (s != null && s.length() > 0) {
                mPopularMovieVideos = PopularMoviesUtils.getPopularMovieVideos(s);
                displayPopularMovieVideos();
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movie_details);
        mPopularMovie =
                getIntent().getParcelableExtra(PopularMoviesContract.Details.TABLE_NAME);

        if (savedInstanceState == null) {
            new TheMovieDBQueryTaskReviews().execute(
                    PopularMoviesUtils.buildApiUrl(getApplicationContext(),
                            mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ID),
                            PopularMoviesUtils.REVIEWS));
        } else {
            mPopularMovieReviews = (ContentValues[]) savedInstanceState.getParcelableArray(
                    PopularMoviesContract.Reviews.TABLE_NAME);
            mPopularMovieVideos = (ContentValues[]) savedInstanceState.getParcelableArray(
                    PopularMoviesContract.Videos.TABLE_NAME);
            displayPopularMovieReviews();
        }


        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mPopularMovie.getAsString(
                    PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE));
        }

        ((CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar)).setTitle(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE));


        FloatingActionButton floatingActionButton = findViewById(R.id.action_favorite);
        if (PopularMoviesContract.isPopularMovieFavorite(this, mPopularMovie)) {
            floatingActionButton.setImageResource(R.drawable.ic_star_white_48dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_star_black_48dp);
        }
        findViewById(R.id.action_favorite).setOnClickListener((view) -> {
            if (PopularMoviesContract.isPopularMovieFavorite(this, mPopularMovie)) {
                PopularMoviesContract.removePopularMovieFromFavorite(this, mPopularMovie);
                ((FloatingActionButton) view).setImageResource(R.drawable.ic_star_black_48dp);
                Toast.makeText(this, String.format("%s %s", mPopularMovie.getAsString(
                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE),
                        getString(R.string.toast_removed_from_favorites)), Toast.LENGTH_SHORT).show();
            } else {
                PopularMoviesContract.addPopularMovieToFavorite(this, mPopularMovie);
                ((FloatingActionButton) view).setImageResource(R.drawable.ic_star_white_48dp);
                Toast.makeText(this, String.format("%s %s", mPopularMovie.getAsString(
                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE),
                        getString(R.string.toast_added_to_favorites)), Toast.LENGTH_SHORT).show();
            }
        });
        ((TextView)findViewById(R.id.details_overview)).setText(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_OVERVIEW));
        ((TextView)findViewById(R.id.details_vote_average)).setText(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_VOTE_AVERAGE));
        ((TextView)findViewById(R.id.details_release_date)).setText(
                mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_RELEASE_DATE));
        mProgressBar = findViewById(R.id.progress_popular_movie_details);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (mPopularMovieVideos == null) {
            new TheMovieDBQueryTaskVideos().execute(
                    PopularMoviesUtils.buildApiUrl(getApplicationContext(),
                            mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ID),
                            PopularMoviesUtils.VIDEOS));
        } else {
            displayPopularMovieVideos();
        }
        return super.onCreateOptionsMenu(menu);
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (PopularMoviesUtils.isNetworkAvailable(context)) {
                ImageView backdrop = findViewById(R.id.backdrop);
                Target target = new Target() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        backdrop.setImageBitmap(bitmap);
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
                backdrop.setTag(target);
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
    protected void onStart() {
        super.onStart();
        this.registerReceiver(broadcastReceiver,
                new IntentFilter(PopularMoviesUtils.ACTION_NETWORK_CHANGE));
    }

    @Override
    protected void onStop() {
        this.unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(PopularMoviesContract.Reviews.TABLE_NAME,
                mPopularMovieReviews);
        outState.putParcelableArray(PopularMoviesContract.Videos.TABLE_NAME,
                mPopularMovieVideos);
    }
}
