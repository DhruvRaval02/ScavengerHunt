package com.example.dhruv.scavengerhunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    ProgressBar loadingBar;
    Intent intentForGame;
    Thread elmoThread;

    Boolean loadingVariable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingBar = findViewById(R.id.ID_loading);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("yeo");

        //myRef.setValue("Hello, World!");

        elmoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (loadingVariable) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingBar.incrementProgressBy(25);
                            if (loadingBar.getProgress() == 100) {
                                loadingVariable = false;
                                intentForGame = new Intent(MainActivity.this, GameActivity.class);
                                startActivity(intentForGame);
                            }

                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        elmoThread.start();


        }

    }

