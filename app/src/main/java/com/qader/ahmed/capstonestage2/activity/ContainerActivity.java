package com.qader.ahmed.capstonestage2.activity;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.qader.ahmed.capstonestage2.R;


public class ContainerActivity extends AppCompatActivity {


    public static Fragment myFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getSupportFragmentManager().beginTransaction().add(R.id.container,myFragment).commit();
    }
}