package com.sielski.marcin.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PopularMoviesContract {
    static final String CONTENT_AUTHORITY = "com.sielski.marcin.popularmovies";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private PopularMoviesContract() {}

    public static final class Details implements BaseColumns {
        public static final String TABLE_NAME = "details";

        static final String _ID = "_id";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE = "release_date";

        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + TABLE_NAME;
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + TABLE_NAME;

        static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class Reviews implements BaseColumns {
        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
    }

    public static final class Videos implements BaseColumns {
        public static final String TABLE_NAME = "videos";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";
    }

    public static boolean isPopularMovieFavorite(Context context, ContentValues popularMovie) {
        Cursor cursor = context.getContentResolver().query(
                PopularMoviesContract.Details.CONTENT_URI,
                new String[] {PopularMoviesContract.Details._ID}, Details.COLUMN_ID + "=" +
                        popularMovie.getAsString(Details.COLUMN_ID), null,
                null);
        boolean result = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return result;
    }

    public static void addPopularMovieToFavorite(Context context, ContentValues popularMovie) {
        context.getContentResolver().insert(Details.CONTENT_URI, popularMovie);
    }

    public static void removePopularMovieFromFavorite(Context context, ContentValues popularMovie) {
        context.getContentResolver().delete(Details.CONTENT_URI, Details.COLUMN_ID +
                "=" + popularMovie.getAsString(Details.COLUMN_ID), null);
    }

    public static Cursor getFavoritePopularMovies(Context context) {
        return context.getContentResolver().query(Details.CONTENT_URI, new String[]{
                Details.COLUMN_ID, Details.COLUMN_POSTER_PATH, Details.COLUMN_ORIGINAL_TITLE,
                Details.COLUMN_BACKDROP_PATH, Details.COLUMN_OVERVIEW, Details.COLUMN_VOTE_AVERAGE,
                Details.COLUMN_RELEASE_DATE
        }, null, null, null);
    }

}
