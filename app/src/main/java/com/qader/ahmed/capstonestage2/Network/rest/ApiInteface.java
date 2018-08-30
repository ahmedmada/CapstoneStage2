package com.qader.ahmed.capstonestage2.Network.rest;


import com.qader.ahmed.capstonestage2.Model.MovieDetail;
import com.qader.ahmed.capstonestage2.Model.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInteface {

    @GET("movie/{type}")
    Call<MoviesResponse> getMovies(@Path("type") String type,@Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<MovieDetail> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

}
