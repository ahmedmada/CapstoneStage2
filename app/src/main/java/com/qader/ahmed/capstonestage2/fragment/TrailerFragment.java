package com.qader.ahmed.capstonestage2.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.qader.ahmed.capstonestage2.Model.Trailer;
import com.qader.ahmed.capstonestage2.Network.BaseUrls;
import com.qader.ahmed.capstonestage2.Network.CheckInternetConnection;
import com.qader.ahmed.capstonestage2.Network.JsonUtils;
import com.qader.ahmed.capstonestage2.Network.rest.ApiInteface;
import com.qader.ahmed.capstonestage2.R;
import com.qader.ahmed.capstonestage2.adapter.TrailerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TrailerFragment extends Fragment {


    private static final String TAG = TrailerFragment.class.getSimpleName();
    private final static String API_KEY = "ef11fa4ee85a2ce3a4ec3bb228455eb4";
    public static int MOVIE_ID;
    public static String MOVIE_POSTER;
    private BaseUrls baseUrls;
    private ApiInteface apiService;
    private List<Trailer> trailers = new ArrayList<>();
    private TrailerAdapter trailerAdapter;

    @BindView(R.id.trailer_recycler)
    RecyclerView trailerRecyclerView;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
     View view = inflater.inflate(R.layout.fragment_trailer, container, false);
     ButterKnife.bind(this,view);
//        baseUrls = new BaseUrls();
//        apiService = ApiClient.getClient().create(ApiInteface.class);
//        getTrailersMovies(MOVIE_ID);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (CheckInternetConnection.isConnected(getActivity())) {
            new FetchTrailerData().execute("http://api.themoviedb.org/3/movie/"+MOVIE_ID+"/videos?api_key="+API_KEY);
        }else
            Toast.makeText(getActivity(), R.string.no_internet+" for Trailers and Reviews !!", Toast.LENGTH_SHORT).show();
    }

    private void initUi(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        trailerRecyclerView.setLayoutManager(linearLayoutManager);
        trailerAdapter = new TrailerAdapter(getContext(),trailers,MOVIE_POSTER);
        trailerRecyclerView.setAdapter(trailerAdapter);
    }


    private class FetchTrailerData extends AsyncTask<String,Void,ArrayList<Trailer>> {
        private final String LOG_TAG = FetchTrailerData.class.getSimpleName();

        public FetchTrailerData() {
        }
        @Override
        protected ArrayList<Trailer> doInBackground(String... strings) {
//        Log.e(TAG,"Here Is doInBackground Of MovieAsyncTask");
            if (strings.length < 1 || strings[0] == null)
            {return null;}

            JsonUtils movieUtils = new JsonUtils();
            ArrayList<Trailer> trailerModels = movieUtils.getTrailerList(strings[0]);
            return trailerModels;
        }
        @Override
        protected void onPostExecute(ArrayList<Trailer> trailers) {
//        Log.e(TAG,"Here Is onPostExecute Of MovieAsyncTask");
            if (trailers != null && !trailers.isEmpty()) {
                trailerAdapter = new TrailerAdapter(getActivity(),trailers,MOVIE_POSTER);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                trailerRecyclerView.setLayoutManager(linearLayoutManager);
                trailerRecyclerView.setAdapter(trailerAdapter);
                trailerAdapter.notifyDataSetChanged();
            }


        }
    }

}