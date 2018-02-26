package com.sielski.marcin.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class PopularMoviesUtils {
    private final static String THEMOVIEDB_API_BASE_URL = "https://api.themoviedb.org/3/movie";
    private final static String THEMOVIEDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
    final static String POPULAR = "popular";
    final static String TOP_RATED = "top_rated";
    final static String REVIEWS = "reviews";
    final static String VIDEOS = "videos";

    final static String FAVORITE = "favorite";
    private final static String API_KEY = "api_key";
    private final static String POSTER_SIZE = "w342";

    private final static String RESULTS = "results";

    final static String SORT_CRITERION = "sort_criterion";

    final static String ACTION_NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private final static String SITE = "YouTube";
    private final static String YOUTUBE_API_BASE_URL = "http://www.youtube.com/watch";
    private final static String YOUTUBE_VIEW_QUERY_PARAMETER = "v";

    static String getTheMoviesDBApiKey(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_themoviedb_api_key), "");
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    static URL buildApiUrl(Context context, String entry, String endpoint) {
        Uri.Builder builder = Uri.parse(THEMOVIEDB_API_BASE_URL).buildUpon();
        if (entry != null && entry.length() > 0) {
            builder.appendPath(entry);
        }
        Uri uri = builder.appendPath(endpoint).appendQueryParameter(API_KEY, getTheMoviesDBApiKey(context)).build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    static URL buildApiUrl(Context context, String endpoint) {
        return buildApiUrl(context, null, endpoint);
    }

    static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            httpURLConnection.disconnect();
        }
    }

    static String getResponseFromContentProvider(Context context) {
        Cursor cursor = PopularMoviesContract.getFavoritePopularMovies(context);
        String result = null;
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String[] columnNames = cursor.getColumnNames();
                    JSONObject popularMovie = new JSONObject();
                    for(String columnName: columnNames) {
                        popularMovie.put(columnName, cursor.getString(cursor.getColumnIndex(columnName)));
                    }
                    jsonArray.put(popularMovie);
                } while (cursor.moveToNext());
            }
            result = jsonObject.put(RESULTS, jsonArray).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    static List<ContentValues> getPopularMovies(String json) {
        List<ContentValues> popularMovies = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(RESULTS)) {
                JSONArray jsonResults = jsonObject.getJSONArray(RESULTS);
                for (int index = 0; index < jsonResults.length(); index ++) {
                    JSONObject jsonPopularMovie = jsonResults.getJSONObject(index);
                    if (jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_ID) &&
                            jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_VOTE_AVERAGE) &&
                            jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_POSTER_PATH) &&
                            jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE) &&
                            jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_BACKDROP_PATH) &&
                            jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_OVERVIEW) &&
                            jsonPopularMovie.has(PopularMoviesContract.Details.COLUMN_RELEASE_DATE)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PopularMoviesContract.Details.COLUMN_ID,
                                String.valueOf(jsonPopularMovie.optInt(
                                        PopularMoviesContract.Details.COLUMN_ID)));
                        contentValues.put(PopularMoviesContract.Details.COLUMN_VOTE_AVERAGE,
                                jsonPopularMovie.optString(
                                        PopularMoviesContract.Details.COLUMN_VOTE_AVERAGE));

                        String posterPath = jsonPopularMovie.optString(
                                PopularMoviesContract.Details.COLUMN_POSTER_PATH);
                        if (!posterPath.startsWith(PopularMoviesUtils.THEMOVIEDB_IMAGE_BASE_URL)) {
                            posterPath = getPopularMoviePosterPath(posterPath);
                        }
                        contentValues.put(PopularMoviesContract.Details.COLUMN_POSTER_PATH,
                                posterPath);

                        contentValues.put(PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE,
                                jsonPopularMovie.optString(
                                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE));

                        String backdropPath = jsonPopularMovie.optString(
                                PopularMoviesContract.Details.COLUMN_BACKDROP_PATH);
                        if (!backdropPath.startsWith(PopularMoviesUtils.THEMOVIEDB_IMAGE_BASE_URL)) {
                            backdropPath = getPopularMoviePosterPath(backdropPath);
                        }
                        contentValues.put(PopularMoviesContract.Details.COLUMN_BACKDROP_PATH,
                                backdropPath);

                        contentValues.put(PopularMoviesContract.Details.COLUMN_OVERVIEW,
                                jsonPopularMovie.optString(
                                        PopularMoviesContract.Details.COLUMN_OVERVIEW));
                        contentValues.put(PopularMoviesContract.Details.COLUMN_RELEASE_DATE,
                                jsonPopularMovie.optString(
                                        PopularMoviesContract.Details.COLUMN_RELEASE_DATE));
                        popularMovies.add(contentValues);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return popularMovies;
    }

    static ContentValues[] getPopularMovieReviews(String json) {
        List<ContentValues> popularMovieReviews = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(RESULTS)) {
                JSONArray jsonResults = jsonObject.getJSONArray(RESULTS);
                for (int index = 0; index < jsonResults.length(); index ++) {
                    JSONObject jsonPopularMovie = jsonResults.getJSONObject(index);
                    if (jsonPopularMovie.has(PopularMoviesContract.Reviews.COLUMN_AUTHOR) &&
                            jsonPopularMovie.has(PopularMoviesContract.Reviews.COLUMN_CONTENT)) {
                        ContentValues popularMovieReview = new ContentValues();
                        popularMovieReview.put(PopularMoviesContract.Reviews.COLUMN_AUTHOR,
                                jsonPopularMovie.optString(PopularMoviesContract.Reviews.COLUMN_AUTHOR));
                        popularMovieReview.put(PopularMoviesContract.Reviews.COLUMN_CONTENT,
                                jsonPopularMovie.optString(PopularMoviesContract.Reviews.COLUMN_CONTENT));
                        popularMovieReviews.add(popularMovieReview);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return popularMovieReviews.toArray(new ContentValues[0]);
    }

    static ContentValues[] getPopularMovieVideos(String json) {
        List<ContentValues> popularMovieVideos = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(RESULTS)) {
                JSONArray jsonResults = jsonObject.getJSONArray(RESULTS);
                for (int index = 0; index < jsonResults.length(); index ++) {
                    JSONObject jsonPopularMovie = jsonResults.getJSONObject(index);
                    if (jsonPopularMovie.has(PopularMoviesContract.Videos.COLUMN_KEY) &&
                            jsonPopularMovie.has(PopularMoviesContract.Videos.COLUMN_NAME) &&
                            jsonPopularMovie.has(PopularMoviesContract.Videos.COLUMN_SITE) &&
                            jsonPopularMovie.has(PopularMoviesContract.Videos.COLUMN_SIZE) &&
                            jsonPopularMovie.has(PopularMoviesContract.Videos.COLUMN_TYPE)) {
                        ContentValues popularMovieVideo = new ContentValues();
                        popularMovieVideo.put(PopularMoviesContract.Videos.COLUMN_KEY,
                                jsonPopularMovie.optString(PopularMoviesContract.Videos.COLUMN_KEY));
                        popularMovieVideo.put(PopularMoviesContract.Videos.COLUMN_NAME,
                                jsonPopularMovie.optString(PopularMoviesContract.Videos.COLUMN_NAME));
                        popularMovieVideo.put(PopularMoviesContract.Videos.COLUMN_SITE,
                                jsonPopularMovie.optString(PopularMoviesContract.Videos.COLUMN_SITE));
                        popularMovieVideo.put(PopularMoviesContract.Videos.COLUMN_SIZE,
                                jsonPopularMovie.optString(PopularMoviesContract.Videos.COLUMN_SIZE));
                        popularMovieVideo.put(PopularMoviesContract.Videos.COLUMN_TYPE,
                                jsonPopularMovie.optString(PopularMoviesContract.Videos.COLUMN_TYPE));
                        popularMovieVideos.add(popularMovieVideo);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return popularMovieVideos.toArray(new ContentValues[0]);
    }


    static int spanCount(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (((float)(displayMetrics.widthPixels) / displayMetrics.density) / 180);
    }

    static boolean isSupported(String site) {
        return SITE.equals(site);
    }

    static Uri buildVideoUri(String key) {
        return Uri.parse(YOUTUBE_API_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_VIEW_QUERY_PARAMETER, key).build();
    }

    private static URL buildImageUrl(String path) {
        Uri uri= Uri.parse(THEMOVIEDB_IMAGE_BASE_URL).buildUpon().appendPath(POSTER_SIZE).
                appendPath(path.substring(1)).build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static String getPopularMoviePosterPath(String posterPath) {
        return buildImageUrl(posterPath).toString();
    }

}
