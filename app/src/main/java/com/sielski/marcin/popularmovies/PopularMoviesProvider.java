package com.sielski.marcin.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PopularMoviesProvider extends ContentProvider {
    private PopularMoviesDBHelper mPopularMoviesDBHelper;
    private static final int POPULAR_MOVIE = 100;
    private static final int POPULAR_MOVIE_WITH_ID = 200;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopularMoviesContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, PopularMoviesContract.Details.TABLE_NAME, POPULAR_MOVIE);
        uriMatcher.addURI(authority, PopularMoviesContract.Details.TABLE_NAME + "/#",
                POPULAR_MOVIE_WITH_ID);
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        mPopularMoviesDBHelper = new PopularMoviesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case POPULAR_MOVIE:
                return mPopularMoviesDBHelper.getReadableDatabase().query(
                        PopularMoviesContract.Details.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
            case POPULAR_MOVIE_WITH_ID:
                return mPopularMoviesDBHelper.getReadableDatabase().query(
                        PopularMoviesContract.Details.TABLE_NAME, projection,
                        PopularMoviesContract.Details._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null, null, sortOrder);
            default: throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case POPULAR_MOVIE: return PopularMoviesContract.Details.CONTENT_DIR_TYPE;
            case POPULAR_MOVIE_WITH_ID: return PopularMoviesContract.Details.CONTENT_ITEM_TYPE;
            default: throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = mPopularMoviesDBHelper.getWritableDatabase();
        Uri result;
        switch (sUriMatcher.match(uri)) {
            case POPULAR_MOVIE:
                long _id = sqLiteDatabase.insert(PopularMoviesContract.Details.TABLE_NAME,
                        null, values);
                if (_id > 0) {
                    result = PopularMoviesContract.Details.buildUri(_id);
                } else {
                    throw new SQLException(uri.toString());
                }
                break;
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mPopularMoviesDBHelper.getWritableDatabase();
        int result;
        switch (sUriMatcher.match(uri)) {
            case POPULAR_MOVIE:
                result = sqLiteDatabase.delete(PopularMoviesContract.Details.TABLE_NAME, selection,
                        selectionArgs);
                sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        PopularMoviesContract.Details.TABLE_NAME + "'");
                break;
            case POPULAR_MOVIE_WITH_ID:
                result = sqLiteDatabase.delete(PopularMoviesContract.Details.TABLE_NAME,
                        PopularMoviesContract.Details._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default: throw new UnsupportedOperationException(uri.toString());
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int result = 0;
        if (values == null) {
            return result;
        }
        final SQLiteDatabase sqLiteDatabase = mPopularMoviesDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case POPULAR_MOVIE:
                result = sqLiteDatabase.update(PopularMoviesContract.Details.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case POPULAR_MOVIE_WITH_ID:
                result = sqLiteDatabase.update(PopularMoviesContract.Details.TABLE_NAME, values,
                        PopularMoviesContract.Details._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            default: throw new UnsupportedOperationException(uri.toString());
        }
        if (result > 0) {
            Context context = getContext();
            if (context != null) context.getContentResolver().notifyChange(uri, null);
        }
        return result;
    }
}
