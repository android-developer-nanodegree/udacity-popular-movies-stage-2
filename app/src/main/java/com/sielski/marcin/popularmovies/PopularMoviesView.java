package com.sielski.marcin.popularmovies;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class PopularMoviesView extends RecyclerView {

    private static class PopularMoviesSavedState extends View.BaseSavedState {
        int mScrollPosition;
        PopularMoviesSavedState(Parcel in) {
            super(in);
            mScrollPosition = in.readInt();
        }
        PopularMoviesSavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mScrollPosition);
        }
        public static final Parcelable.Creator<PopularMoviesSavedState> CREATOR
                = new Parcelable.Creator<PopularMoviesSavedState>() {
            @Override
            public PopularMoviesSavedState createFromParcel(Parcel in) {
                return new PopularMoviesSavedState(in);
            }

            @Override
            public PopularMoviesSavedState[] newArray(int size) {
                return new PopularMoviesSavedState[size];
            }
        };
    }

    private int mScrollPosition;

    public PopularMoviesView(Context context) {
        super(context);
    }

    public PopularMoviesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopularMoviesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        LayoutManager layoutManager = getLayoutManager();
        if(layoutManager != null && layoutManager instanceof GridLayoutManager){
            mScrollPosition =
                    ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        }
        PopularMoviesSavedState newState = new PopularMoviesSavedState(superState);
        newState.mScrollPosition = mScrollPosition;
        return newState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if(state != null && state instanceof PopularMoviesSavedState){
            mScrollPosition = ((PopularMoviesSavedState) state).mScrollPosition;
            GridLayoutManager layoutManager = (GridLayoutManager) getLayoutManager();
            if(layoutManager != null){
                if(mScrollPosition != RecyclerView.NO_POSITION){
                    layoutManager.scrollToPositionWithOffset(mScrollPosition, 0);
                }
            }
        }
    }
}
