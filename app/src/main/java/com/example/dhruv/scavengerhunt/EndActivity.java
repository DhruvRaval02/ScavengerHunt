package com.example.dhruv.scavengerhunt;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EndActivity extends AppCompatActivity {

    String winnerUsername = "";
    String winnerScore = "";

    String loserUsername = "";
    String loserScore = "";

    ConstraintLayout constraintLayout;

    String modePlayed = "";
    String itemOne = "";
    String itemTwo = "";
    String itemThree = "";
    String itemFour = "";
    String itemFive = "";

    int counter = 0;
    JSONObject object = new JSONObject();

    TextView winnerResult;
    TextView loserResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        constraintLayout = findViewById(R.id.ID_constraintLayou);
        winnerResult = findViewById(R.id.ID_winnerResult);
        loserResult = findViewById(R.id.ID_loserResult);

        String yourUsername = getIntent().getStringExtra("USER");
        Log.d("TAG", yourUsername);

        DatabaseReference winOrLoseReference = FirebaseDatabase.getInstance().getReference();

        winOrLoseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot winnerSnapshot : dataSnapshot.child("id").child("players").getChildren()){
                    Log.d("TAG", winnerSnapshot.child("score").getValue().toString());
                    if(winnerSnapshot.child("score").getValue().toString().equals("500")){
                        winnerUsername = winnerSnapshot.child("username").getValue().toString();
                        winnerScore = (winnerSnapshot.child("score").getValue().toString());
                    }
                    else{
                        loserUsername = winnerSnapshot.child("username").getValue().toString();
                        loserScore = (winnerSnapshot.child("score").getValue().toString());
                    }
                }
                modePlayed = dataSnapshot.child("id").child("mode").getValue().toString();
                for(DataSnapshot listSnapshot : dataSnapshot.child("id").child("list").getChildren()){
                    counter++;
                    if(counter == 1)
                        itemOne = listSnapshot.getValue().toString();
                    else if(counter == 2)
                        itemTwo = listSnapshot.getValue().toString();
                    else if(counter == 3)
                        itemThree = listSnapshot.getValue().toString();
                    else if(counter == 4)
                        itemFour = listSnapshot.getValue().toString();
                    else if (counter == 5)
                        itemFive = listSnapshot.getValue().toString();
                }
                JSONArray objectArray = new JSONArray();

                try {
                    object.put("winner", winnerUsername);
                    object.put("loser", loserUsername);
                    object.put("mode", modePlayed);

                    objectArray.put(itemOne);
                    objectArray.put(itemTwo);
                    objectArray.put(itemThree);
                    objectArray.put(itemFour);
                    objectArray.put(itemFive);

                    object.put("items", objectArray);

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("info.json", Context.MODE_PRIVATE));
                    outputStreamWriter.write(object.toString());
                    outputStreamWriter.close();

                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                winnerResult.setText(winnerUsername + " wins!");
                loserResult.setText(loserUsername  + " loses with " + loserScore + " points");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });









    }
}
