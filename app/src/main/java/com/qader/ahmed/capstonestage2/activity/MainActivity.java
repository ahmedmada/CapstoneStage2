package com.qader.ahmed.capstonestage2.activity;

import android.database.Cursor;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.qader.ahmed.capstonestage2.Model.Movie;
import com.qader.ahmed.capstonestage2.Model.MoviesResponse;
import com.qader.ahmed.capstonestage2.Network.BaseUrls;
import com.qader.ahmed.capstonestage2.Network.CheckInternetConnection;
import com.qader.ahmed.capstonestage2.Network.rest.ApiClient;
import com.qader.ahmed.capstonestage2.Network.rest.ApiInteface;
import com.qader.ahmed.capstonestage2.R;
import com.qader.ahmed.capstonestage2.adapter.MovieAdatpter;
import com.qader.ahmed.capstonestage2.database.MovieContruct;
import com.qader.ahmed.capstonestage2.database.MovieProvider;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.qader.ahmed.capstonestage2.constants.Constants.ADMOB_APP_ID;
import static com.qader.ahmed.capstonestage2.constants.Constants.API_KEY;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private BaseUrls baseUrls;
    private ApiInteface apiService;
    private List<Movie> popularMovies = new ArrayList<>();
    private List<Movie> topRatedMovies = new ArrayList<>();
    private List<Movie> nowPlayingMovies = new ArrayList<>();
    private List<Movie> upcomingMovies = new ArrayList<>();
    private List<Movie> favouritMovies = new ArrayList<>();
    private MovieAdatpter movieAdatpter;


    @BindView(R.id.adView) AdView mAdView;
    @BindView(R.id.recyclerview_all_type)
    RecyclerView recyclerview_all_type;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        baseUrls = new BaseUrls();
        apiService = ApiClient.getClient().create(ApiInteface.class);
        getSupportLoaderManager().initLoader(0, null, this);

        recyclerview_all_type.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
        if (CheckInternetConnection.isConnected(getApplicationContext())){



            popularMovies = getListMovies("popular");

            initAdMob();
        }else {
            Toast.makeText(getApplicationContext(), "check your internet", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void initAdMob(){
        MobileAds.initialize(this, ADMOB_APP_ID);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdLeftApplication() {

            }

            @Override
            public void onAdClosed() {

            }
        });
    }


    public List<Movie> getListMovies(String type){
        popularMovies = new ArrayList<>();
        Call<MoviesResponse> call = apiService.getMovies(type,API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                popularMovies = response.body().getResults();

                progressBar.setVisibility(View.GONE);

                movieAdatpter = new MovieAdatpter(MainActivity.this,popularMovies);
                recyclerview_all_type.setAdapter(movieAdatpter);

                movieAdatpter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                popularMovies = null;
            }
        });

        return popularMovies;
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        return new CursorLoader(this,
                MovieProvider.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if (data != null && data.getCount() > 0) {
            List<Movie> movies = new ArrayList<>();
            data.moveToFirst();
            do {
                int movieID = data.getInt(data.getColumnIndex(MovieContruct.Favorite.MOVIE_ID));
                String movieTitle = data.getString(data.getColumnIndex(MovieContruct.Favorite.TITLE));
                String moviePosterPath = data.getString(data.getColumnIndex(MovieContruct.Favorite.POSTER_PATH));
                int movieRating = data.getInt(data.getColumnIndex(MovieContruct.Favorite.VOTE_AVERAGE));
                Movie movie = new Movie();
                movie.setId(movieID);
                movie.setPoster_path(moviePosterPath);
                movie.setTitle(movieTitle);
                movie.setVote_average(movieRating);
                favouritMovies.add(movie);
            } while (data.moveToNext());

        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public void openPopular(View view) {

        if (popularMovies.size() > 0){

            movieAdatpter = new MovieAdatpter(MainActivity.this,popularMovies);
            recyclerview_all_type.setAdapter(movieAdatpter);

            movieAdatpter.notifyDataSetChanged();
        }else
            popularMovies = getListMovies("popular");

    }

    public void openTopRated(View view) {
        if (topRatedMovies.size() > 0){

            movieAdatpter = new MovieAdatpter(MainActivity.this,topRatedMovies);
            recyclerview_all_type.setAdapter(movieAdatpter);

            movieAdatpter.notifyDataSetChanged();
        }else
            topRatedMovies = getListMovies("top_rated");

    }

    public void openNewPlaying(View view) {
        if (nowPlayingMovies.size() > 0){

            movieAdatpter = new MovieAdatpter(MainActivity.this,nowPlayingMovies);
            recyclerview_all_type.setAdapter(movieAdatpter);

            movieAdatpter.notifyDataSetChanged();
        }else
            nowPlayingMovies = getListMovies("upcoming");

    }

    public void openUpcoming(View view) {
        if (upcomingMovies.size() > 0){

            movieAdatpter = new MovieAdatpter(MainActivity.this,upcomingMovies);
            recyclerview_all_type.setAdapter(movieAdatpter);

            movieAdatpter.notifyDataSetChanged();
        }else
            upcomingMovies = getListMovies("now_playing");

    }

    public void openFavourit(View view) {
        if (favouritMovies.size() > 0){

            movieAdatpter = new MovieAdatpter(MainActivity.this,favouritMovies);
            recyclerview_all_type.setAdapter(movieAdatpter);
            movieAdatpter.notifyDataSetChanged();

        }else
            Toast.makeText(getApplicationContext(), "no favourit", Toast.LENGTH_SHORT).show();

    }
}
