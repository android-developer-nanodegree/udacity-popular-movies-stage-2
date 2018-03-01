package com.sielski.marcin.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sielski.marcin.popularmovies.data.PopularMoviesContract;

class PopularMoviesDBHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "popularmovies.db";
    private final static int DATABASE_VERSION = 2;

    PopularMoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE_DETAILS =
                "CREATE TABLE " + PopularMoviesContract.Details.TABLE_NAME + "(" +
                        PopularMoviesContract.Details._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PopularMoviesContract.Details.COLUMN_ID + " TEXT NOT NULL, " +
                        PopularMoviesContract.Details.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        PopularMoviesContract.Details.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                        PopularMoviesContract.Details.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                        PopularMoviesContract.Details.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        PopularMoviesContract.Details.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, " +
                        PopularMoviesContract.Details.COLUMN_RELEASE_DATE + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_DETAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.Details.TABLE_NAME);
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                PopularMoviesContract.Details.TABLE_NAME + "'");
        onCreate(sqLiteDatabase);
    }
}
