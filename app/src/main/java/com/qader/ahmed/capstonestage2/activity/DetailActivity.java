package com.qader.ahmed.capstonestage2.activity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.qader.ahmed.capstonestage2.Model.Movie;
import com.qader.ahmed.capstonestage2.Model.MovieDetail;
import com.qader.ahmed.capstonestage2.Model.Trailer;
import com.qader.ahmed.capstonestage2.MovieWidget;
import com.qader.ahmed.capstonestage2.Network.BaseUrls;
import com.qader.ahmed.capstonestage2.Network.CheckInternetConnection;
import com.qader.ahmed.capstonestage2.Network.JsonUtils;
import com.qader.ahmed.capstonestage2.Network.rest.ApiClient;
import com.qader.ahmed.capstonestage2.Network.rest.ApiInteface;
import com.qader.ahmed.capstonestage2.R;
import com.qader.ahmed.capstonestage2.adapter.MovieAdatpter;
import com.qader.ahmed.capstonestage2.adapter.TrailerAdapter;
import com.qader.ahmed.capstonestage2.database.MovieContruct;
import com.qader.ahmed.capstonestage2.database.MovieProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.qader.ahmed.capstonestage2.constants.Constants.MOVIE_ID;


public class DetailActivity extends AppCompatActivity {


    @BindView(R.id.detail_container_layout)
    LinearLayout linearLayout;
    @BindView(R.id.recyclerview_tailer)
    RecyclerView recyclerview_tailer;
    @BindView(R.id.img_backdrop_path)
    ImageView backDrobPathImageView;
    @BindView(R.id.text_view_tagline)
    TextView tagLineTextView;
    @BindView(R.id.tv_overview)
    TextView overviewTextView;
    @BindView(R.id.favorite_button)
    ToggleButton favorite;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_view_all_details)
    TextView text_view_all_details ;



    private static final String TAG = DetailActivity.class.getSimpleName();
    private final static String API_KEY = "41049e5d4f52d9922464f0055a20caaa";
    private BaseUrls baseUrls;
    private ApiInteface apiService;
    private MovieDetail movieDetail;
    private int movieId;
    private Cursor c;
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Trailer> trailers = new ArrayList<>();
    private TrailerAdapter trailerAdapter;


    @BindView(R.id.details_toolbar)
    Toolbar myToolbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        try {
            myToolbar.setTitle(getResources().getString(R.string.app_name));
            setSupportActionBar(myToolbar);
        }catch (Exception e){
        }
        ButterKnife.bind(this);
        baseUrls = new BaseUrls();
        linearLayout.setVisibility(View.GONE);
        apiService = ApiClient.getClient().create(ApiInteface.class);
        movieId = getIntent().getIntExtra(MOVIE_ID,0);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        getMovieDetail(movieId);

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favorite.isChecked()){

                    ContentValues values =  new ContentValues();
                    values.put(MovieContruct.Favorite.MOVIE_ID, movieDetail.getId());
                    values.put(MovieContruct.Favorite.POSTER_PATH, movieDetail.getPoster_path());
                    values.put(MovieContruct.Favorite.OVERVIEW, movieDetail.getOverview());
                    values.put(MovieContruct.Favorite.RELEASE_DATE, movieDetail.getRelease_date());
                    values.put(MovieContruct.Favorite.TITLE, movieDetail.getTitle());
                    values.put(MovieContruct.Favorite.VOTE_AVERAGE, movieDetail.getVote_average());
                    Uri uri = getContentResolver().insert(MovieProvider.CONTENT_URI,values);
                    Log.e(DetailActivity.class.getSimpleName(),uri+"");

                }else {

                    String selection = "movie_id = ?";
                    String selectionArgs [] = new String[]{movieDetail.getId() + ""};
                    int delete = getContentResolver().delete(MovieProvider.CONTENT_URI,selection,selectionArgs);
                    if (delete > -1) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.movie_deleted), Toast.LENGTH_SHORT).show();

                    }

                }
            }
        });
    }

    private void updateAllWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), MovieWidget.class));
        if (appWidgetIds.length > 0) {
            new MovieWidget().onUpdate(getApplicationContext(), appWidgetManager, appWidgetIds);
        }
    }

    String allDetails;
    private void initUi(){

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movieDetail.getId()));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, movieDetail.getTitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, baseUrls.getIMAGE_BASE_URL()+movieDetail.getPoster_path());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


        tagLineTextView.setText(movieDetail.getTagline());
        overviewTextView.setText(movieDetail.getOverview());

        if (!movieDetail.getBackdrop_path().isEmpty()){
            Picasso.get().load(baseUrls.getIMAGE_BASE_URL()+movieDetail.getBackdrop_path()).into(backDrobPathImageView);
            backDrobPathImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        allDetails = "Runtime   "+movieDetail.getRuntime()+"\n"
                +"Release date  "+movieDetail.getRelease_date()+"\n"
                +"Revenue  "+movieDetail.getRevenue()+"\n"
                +"Budget  "+movieDetail.getBudget()+"\n"
                +"Vote count  "+movieDetail.getVote_count()+"\n";

        text_view_all_details.setText(allDetails);


        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable(){
            @Override
            public void run() {
                Paper.book().write("title", movieDetail.getTitle());
                Paper.book().write("image", movieDetail.getPoster_path());
                updateAllWidgets();
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerview_tailer.setLayoutManager(linearLayoutManager);
        trailerAdapter = new TrailerAdapter(this,trailers,baseUrls.getIMAGE_BASE_URL()+movieDetail.getPoster_path());
        recyclerview_tailer.setAdapter(trailerAdapter);

    }
    public void getMovieDetail(final int id){
        Log.e("id",id+"");
        Log.v("aaaaaaaaaaaaaaaaaa","id = "+id);
        Call<MovieDetail> call = apiService.getMovieDetails(id,API_KEY);
        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {

                movieDetail = response.body();
                if (!movieDetail.equals(null)){
                    initUi();

                    checkIfMovieInFavorite();
                }

                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {

                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (CheckInternetConnection.isConnected(this)) {
            new FetchTrailerData().execute("http://api.themoviedb.org/3/movie/"+MOVIE_ID+"/videos?api_key="+API_KEY);
        }else
            Toast.makeText(this, "no internet for Trailers and Reviews !!", Toast.LENGTH_SHORT).show();
    }


    private class FetchTrailerData extends AsyncTask<String,Void,ArrayList<Trailer>> {

        public FetchTrailerData() {
        }
        @Override
        protected ArrayList<Trailer> doInBackground(String... strings) {

            if (strings.length < 1 || strings[0] == null)
            {return null;}

            JsonUtils movieUtils = new JsonUtils();
            ArrayList<Trailer> trailerModels = movieUtils.getTrailerList(strings[0]);
            return trailerModels;
        }
        @Override
        protected void onPostExecute(ArrayList<Trailer> trailers) {

            if (trailers != null && !trailers.isEmpty()) {
                trailerAdapter = new TrailerAdapter(DetailActivity.this,trailers,baseUrls.getIMAGE_BASE_URL()+movieDetail.getPoster_path());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DetailActivity.this);
                recyclerview_tailer.setLayoutManager(linearLayoutManager);
                recyclerview_tailer.setAdapter(trailerAdapter);
                trailerAdapter.notifyDataSetChanged();
            }


        }
    }



    public void checkIfMovieInFavorite(){

        //this method to check if the movie in favorite list
        String URL = MovieProvider.CONTENT_URI+"/"+movieDetail.getId();
        Uri uri = Uri.parse(URL);
        String selection = "movie_id = ?";
        String selectionArgs [] = new String[]{movieDetail.getId() + ""};
        c = getContentResolver().query(uri,null,selection,selectionArgs,null);
        if (c.moveToFirst()){
            favorite.setChecked(true);
        }else {
            favorite.setChecked(false);
        }
        }

}
