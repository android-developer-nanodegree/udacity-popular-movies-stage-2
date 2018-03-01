package com.sielski.marcin.popularmovies;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.sielski.marcin.popularmovies.data.PopularMoviesContract;
import com.sielski.marcin.popularmovies.util.PopularMoviesUtils;

import java.io.IOException;
import java.net.URL;

public class PopularMovieDetailsActivity extends AppCompatActivity {

    private Menu mMenu;
    private ContentValues mPopularMovie;
    private ContentValues[] mPopularMovieVideos;

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
        Bundle bundle = new Bundle();
        bundle.putParcelable(PopularMoviesContract.Details.TABLE_NAME, mPopularMovie);
        PopularMovieDetailsFragment popularMovieDetailsFragment = new PopularMovieDetailsFragment();
        popularMovieDetailsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.details_popular_movie_container,
                popularMovieDetailsFragment).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (mPopularMovieVideos == null) {
            new TheMovieDBQueryTaskVideos().execute(
                    PopularMoviesUtils.buildApiUrl(this,
                            mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_ID),
                            PopularMoviesUtils.VIDEOS));
        } else {
            displayPopularMovieVideos();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(PopularMoviesContract.Videos.TABLE_NAME,
                mPopularMovieVideos);
    }
}
