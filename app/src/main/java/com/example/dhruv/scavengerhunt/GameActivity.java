package com.example.dhruv.scavengerhunt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    TextView userName;
    TextView waitingOrFound;

    RadioGroup createOrJoin;
    RadioButton createButton;
    RadioButton joinButton;

    EditText userEntry;
    EditText identificationEntry;

    RadioGroup officeOrPark;
    RadioButton officeButton;
    RadioButton parkButton;

    Button readyButton;

    DatabaseReference gameReference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Intent intentForHunt;

    Thread startGameThread;
    Boolean lookForStartVariable = true;

    Boolean doHunt = true;
    Boolean readyUp = false;

    String id = "";
    int random = 0;
    int counter = 0;
    Boolean identityMatch;

    Switch previousResults;

    ConstraintLayout itemFragment;

    ArrayList<String> officeList = new ArrayList<>();
    ArrayList<String> parkList = new ArrayList<>();
    ArrayList<String> gameList = new ArrayList<>();

    String data = "";
    JSONObject previousData;

    TextView winner;
    TextView loser;
    TextView mode;
    TextView arrayOne;
    TextView arrayTwo;
    TextView arrayThree;
    TextView arrayFour;
    TextView arrayFive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        userName = findViewById(R.id.ID_user);
        waitingOrFound = findViewById(R.id.ID_waitingOrFound);

        createOrJoin = findViewById(R.id.ID_createOrJoinGroup);
        createButton = findViewById(R.id.ID_createButton);
        joinButton = findViewById(R.id.ID_joinButton);

        userEntry = findViewById(R.id.ID_playerEntry);
        identificationEntry = findViewById(R.id.ID_identificationEntry);

        officeOrPark = findViewById(R.id.ID_modeGroup);
        officeButton = findViewById(R.id.ID_officeMode);
        parkButton = findViewById(R.id.ID_parkMode);

        readyButton = findViewById(R.id.ID_readyButton);

        previousResults = findViewById(R.id.ID_viewPrevious);

        itemFragment = findViewById(R.id.ID_itemFragment);

        winner = findViewById(R.id.ID_previousWinner);
        loser = findViewById(R.id.ID_previousLoser);
        mode = findViewById(R.id.ID_previousGamemode);
        arrayOne = findViewById(R.id.ID_previousItemOne);
        arrayTwo = findViewById(R.id.ID_previousItemTwo);
        arrayThree = findViewById(R.id.ID_previousItemThree);
        arrayFour = findViewById(R.id.ID_previousItemFour);
        arrayFive = findViewById(R.id.ID_previousItemFive);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("info.json")));
            data = reader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TAG", data);
        try {
            previousData = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            winner.setText(previousData.getString("winner").toString());
            winner.setTextColor(Color.GREEN);
            loser.setText(previousData.getString("loser"));
            loser.setTextColor(Color.RED);
            mode.setText("MODE: " + previousData.getString("mode"));
            arrayOne.setText(previousData.getJSONArray("items").getString(0));
            arrayTwo.setText(previousData.getJSONArray("items").getString(1));
            arrayThree.setText(previousData.getJSONArray("items").getString(2));
            arrayFour.setText(previousData.getJSONArray("items").getString(3));
            arrayFive.setText(previousData.getJSONArray("items").getString(4));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        officeList.add("Bottle");
        officeList.add("Ceiling");
        officeList.add("Computer");
        officeList.add("Keyboard");
        officeList.add("Mouse");
        officeList.add("Pencil");
        officeList.add("Phone");
        officeList.add("Drawer");
        officeList.add("Chair");
        officeList.add("Clock");
        officeList.add("Shoe");
        officeList.add("Cabinetry");
        officeList.add("Watch");
        officeList.add("Tablet");
        officeList.add("Window");
        officeList.add("Tape");
        officeList.add("Stapler");
        officeList.add("Pen");
        officeList.add("Lotion");
        officeList.add("Notebook");
        officeList.add("Binder");

        parkList.add("Swings");
        parkList.add("Football");
        parkList.add("Basketball");
        parkList.add("Baseball");
        parkList.add("Tree");
        parkList.add("Slide");
        parkList.add("Car");
        parkList.add("Dog");
        parkList.add("Monkey Bars");
        parkList.add("Water Fountain");
        parkList.add("Seesaw");
        parkList.add("Bench");
        parkList.add("Tire");
        parkList.add("Bicycle");
        parkList.add("Scooter");
        parkList.add("Flowers");
        parkList.add("Rock Wall");
        parkList.add("Tent");
        parkList.add("Tennis Racket");

        gameReference = database.getReference();

        startGameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (lookForStartVariable) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(previousResults.isChecked()){
                                itemFragment.setVisibility(View.VISIBLE);
                                readyButton.setVisibility(View.INVISIBLE);
                            }
                            else {
                                itemFragment.setVisibility(View.INVISIBLE);
                                readyButton.setVisibility(View.VISIBLE);
                            }

                            gameReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child("id").child("players").getChildrenCount() == 2 && doHunt == true && readyUp){
                                        waitingOrFound.setText("Starting Match");
                                        intentForHunt = new Intent(GameActivity.this, HuntActivity.class);
                                        intentForHunt.putExtra("ID", id);
                                        startActivity(intentForHunt);
                                        lookForStartVariable = false;
                                        doHunt = false;
                                    }
                                    else if(dataSnapshot.child("id").child("players").getChildrenCount() == 1 && readyUp){
                                        waitingOrFound.setText("Waiting For Opponent");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            if(joinButton.isChecked()){
                                officeOrPark.setVisibility(View.INVISIBLE);
                                gameReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.child("id").child("identification").exists()) {
                                            if (identificationEntry.getText().toString().equals(dataSnapshot.child("id").child("identification").getValue().toString())) {
                                                identityMatch = true;
                                                Log.d("g", "TRUE");
                                            } else
                                                identityMatch = false;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else
                                officeOrPark.setVisibility(View.VISIBLE);

                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        startGameThread.start();

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(createButton.isChecked()) {
                    gameReference.removeValue();
                    counter++;
                    Log.d("TAG", identificationEntry.getText().toString());
                    gameReference.child("id").child("identification").setValue(identificationEntry.getText().toString());
                    userName.setText(userEntry.getText().toString());

                    id = gameReference.push().getKey();
                    gameReference.child("id").child("players").child(id).child("username").setValue(userEntry.getText().toString());
                    gameReference.child("id").child("players").child(id).child("score").setValue(0);

                    if(officeButton.isChecked()){
                        gameReference.child("id").child("mode").setValue("Office");
                        for(int i = 0; i < 5; i++) {
                            random = (int) (Math.random() * 21);
                            if(!(gameList.contains(officeList.get(random))))
                                gameList.add(officeList.get(random));
                            else
                                i--;
                        }
                        gameReference.child("id").child("list").setValue(gameList);
                        gameList.clear();

                    }
                    else if(parkButton.isChecked()){
                        gameReference.child("id").child("mode").setValue("Park");
                        for(int i = 0; i < 5; i++) {
                            random = (int) (Math.random() * 21);
                            if(!(gameList.contains(parkList.get(random))))
                                gameList.add(parkList.get(random));
                            else
                                i--;
                        }
                        gameReference.child("id").child("list").setValue(gameList);
                        gameList.clear();

                    }

                }
                else if(joinButton.isChecked()){

                    if(identityMatch){

                        id = gameReference.push().getKey();
                        gameReference.child("id").child("players").child(id).child("username").setValue(userEntry.getText().toString());
                        gameReference.child("id").child("players").child(id).child("score").setValue(0);

                        userName.setText(userEntry.getText().toString());

                    }
                }

                readyUp = true;
            }
        });

    }

}
