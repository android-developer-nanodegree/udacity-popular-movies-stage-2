package com.sielski.marcin.popularmovies;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import java.util.List;

public class PopularMoviesAdapter extends RecyclerView.Adapter<PopularMoviesAdapter.ViewHolder> {

    private final List<ContentValues> mPopularMovies;
    private final String mSortCriterion;
    private final boolean mTwoPane;
    private ContentValues mPopularMovie;
    private final Fragment mParentFragment;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ContentValues mPopularMovie;

        final View mView;
        final ImageView mImageView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.poster_popular_movie);
        }
    }

    PopularMoviesAdapter(Fragment parentFragment, List<ContentValues> items, String sortCriterion,
                         boolean twoPane) {
        mParentFragment = parentFragment;
        mPopularMovies = items;
        mSortCriterion = sortCriterion;
        mTwoPane = twoPane;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poster_popular_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mPopularMovie = mPopularMovies.get(position);
        holder.mView.setOnClickListener((view) -> {
            mPopularMovie = holder.mPopularMovie;
            Context context = view.getContext();
            if (PopularMoviesUtils.isNetworkAvailable(context)) {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putInt(mSortCriterion, ((RecyclerView) view.getParent())
                                .getLayoutManager().getPosition(view)).apply();
                if (mTwoPane) {
                    PopularMovieDetailsFragment popularMovieDetailsFragment =
                            new PopularMovieDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(PopularMoviesContract.Details.TABLE_NAME,
                            holder.mPopularMovie);
                    popularMovieDetailsFragment.setArguments(bundle);
                    FragmentManager fragmentManager = mParentFragment.getFragmentManager();
                    if (fragmentManager != null) {
                        fragmentManager.beginTransaction()
                                .replace(PopularMoviesUtils.id.get(mSortCriterion),
                                        popularMovieDetailsFragment).commit();
                    }
                } else {
                    Intent intent = new Intent(context, PopularMovieDetailsActivity.class);
                    intent.putExtra(PopularMoviesContract.Details.TABLE_NAME, holder.mPopularMovie);
                    context.startActivity(intent);
                }
            } else {
                Toast.makeText(context, context.getString(R.string.toast_network_unavailable),
                        Toast.LENGTH_SHORT).show();
            }
        });

        Picasso.with(holder.mImageView.getContext())
                .load(holder.mPopularMovie.getAsString(PopularMoviesContract.Details.COLUMN_POSTER_PATH))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.mImageView);

    }

    @Override
    public int getItemCount() {
        return mPopularMovies.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mTwoPane && mPopularMovie != null) {
            PopularMovieDetailsFragment popularMovieDetailsFragment = new PopularMovieDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(PopularMoviesContract.Details.TABLE_NAME, mPopularMovie);
            popularMovieDetailsFragment.setArguments(bundle);
            FragmentManager fragmentManager = mParentFragment.getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                        .replace(PopularMoviesUtils.id.get(mSortCriterion),
                                popularMovieDetailsFragment).commit();
            }
        }
    }
}
