package com.sielski.marcin.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ContentValues mPopularMovie;

        final View mView;
        final ImageView mImageView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.avatar);
        }
    }

    // TODO?
    PopularMoviesAdapter(Context context, List<ContentValues> items, String sortCriterion) {
        mPopularMovies = items;
        mSortCriterion = sortCriterion;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popular_movie_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mPopularMovie = mPopularMovies.get(position);
        holder.mView.setOnClickListener((view) -> {
            Context context = view.getContext();
            if (PopularMoviesUtils.isNetworkAvailable(context)) {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putInt(mSortCriterion,
                                ((RecyclerView) view.getParent()).getLayoutManager().getPosition(view)).apply();
                Intent intent = new Intent(context, PopularMovieDetailsActivity.class);
                intent.putExtra(PopularMoviesContract.Details.TABLE_NAME, holder.mPopularMovie);
                context.startActivity(intent);
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
}
