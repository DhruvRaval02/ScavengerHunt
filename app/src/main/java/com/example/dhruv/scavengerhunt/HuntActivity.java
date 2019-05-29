package com.example.dhruv.scavengerhunt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HuntActivity extends AppCompatActivity {

    ArrayList<String> masterList = new ArrayList<>();
    ArrayList<String> itemList = new ArrayList<>();
    LinearLayout layout;
    ConstraintLayout scoreLayout;
    FloatingActionButton listToggle;
    FloatingActionButton scoreToggle;
    ImageView itemImage;

    ImageView listImage;
    ImageView scoreImage;

    TextView itemOne;
    TextView itemTwo;
    TextView itemThree;
    TextView itemFour;
    TextView itemFive;

    TextView usernameOne;
    TextView usernameTwo;
    TextView scoreOne;
    TextView scoreTwo;

    View layoutToastCorrect;
    View layoutToastIncorrect;
    TextView customToastCorrectText;

    DatabaseReference scoresReference;
    DatabaseReference listRef;

    Thread toggleThread;
    Boolean loadingVariable = true;
    Button pictureTake;

    Boolean match = false;
    String wordToBeGreened;
    int score = 0;
    String id;

    MediaPlayer correctSound;
    MediaPlayer incorrectSound;

    int counter = 0;
    int scoreCounter = 0;

    Boolean showButton = true;
    Boolean endGame = false;
    Boolean listClicked = false;
    Boolean scoreClicked = false;

    Intent endIntent;

    ArrayList<String> realMaster = new ArrayList<>();

    private Feature feature;
    private Bitmap bitmap;

    private static final int REC_REQUEST_CODE = 1;
    private static final int CAM_REQUEST_CODE = 2;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyD7XGVehjAu1ch_8BqOqeaZF245S1ITx3g";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);

        layout = findViewById(R.id.ID_itemFrag);
        scoreLayout = findViewById(R.id.ID_scoreLayout);
        pictureTake = findViewById(R.id.ID_pictureTake);
        itemImage = findViewById(R.id.ID_picture);

        listToggle = findViewById(R.id.ID_floatingListToggle);
        scoreToggle = findViewById(R.id.ID_floatingScoreToggle);

        listImage = findViewById(R.id.ID_listImage);
        scoreImage = findViewById(R.id.ID_scoreImage);

        itemOne = findViewById(R.id.ID_itemOne);
        itemTwo = findViewById(R.id.ID_itemTwo);
        itemThree = findViewById(R.id.ID_itemThree);
        itemFour = findViewById(R.id.ID_itemFour);
        itemFive = findViewById(R.id.ID_itemFive);

        usernameOne = findViewById(R.id.ID_usernameOne);
        usernameTwo = findViewById(R.id.ID_usernameTwo);

        scoreOne = findViewById(R.id.ID_scoreOne);
        scoreTwo = findViewById(R.id.ID_scoreTwo);

        listRef = FirebaseDatabase.getInstance().getReference();

        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                counter++;
                if(counter == 1) {
                    for (DataSnapshot listElement : dataSnapshot.child("id").child("list").getChildren())
                        masterList.add(listElement.getValue().toString());
                    itemOne.setText(masterList.get(0));
                    itemTwo.setText(masterList.get(1));
                    itemThree.setText(masterList.get(2));
                    itemFour.setText(masterList.get(3));
                    itemFive.setText(masterList.get(4));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        for(int i = 0; i<masterList.size(); i++){

        }

        scoresReference = FirebaseDatabase.getInstance().getReference();
        id = getIntent().getStringExtra("ID");

        LayoutInflater layoutInflater = getLayoutInflater();
        layoutToastCorrect = layoutInflater.inflate(R.layout.custom_toast_correct, (ViewGroup) findViewById(R.id.ID_customToastLayoutCorrect));
        layoutToastIncorrect = layoutInflater.inflate(R.layout.custom_toast_incorrect, (ViewGroup) findViewById(R.id.ID_customToastLayoutIncorrect));

        customToastCorrectText = (TextView) layoutToastCorrect.findViewById(R.id.ID_correct);

        correctSound = MediaPlayer.create(this, R.raw.gameshowding);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrectsound);

        toggleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (loadingVariable) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            listToggle.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    listClicked = !listClicked;

                                    if(listClicked){
                                        layout.setVisibility(View.VISIBLE);
                                        scoreToggle.hide();
                                        showButton = false;
                                        scoreImage.setVisibility(View.INVISIBLE);
                                    }
                                    else{
                                        layout.setVisibility(View.INVISIBLE);
                                        scoreToggle.show();
                                        scoreImage.setVisibility(View.VISIBLE);
                                        if(!scoreClicked)
                                            showButton = true;
                                    }
                                }
                            });

                            scoreToggle.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    scoreClicked = !scoreClicked;
                                    if(scoreClicked){
                                        scoreLayout.setVisibility(View.VISIBLE);
                                        listToggle.hide();
                                        showButton=false;
                                        listImage.setVisibility(View.INVISIBLE);
                                    }
                                    else{
                                        scoreLayout.setVisibility(View.INVISIBLE);
                                        listToggle.show();
                                        listImage.setVisibility(View.VISIBLE);
                                        if(!listClicked)
                                            showButton = true;
                                    }
                                }
                            });

                            if(showButton)
                                pictureTake.setVisibility(View.VISIBLE);
                            else
                                pictureTake.setVisibility(View.INVISIBLE);

                            if(itemOne.getText().equals(wordToBeGreened))
                                itemOne.setTextColor(Color.GREEN);
                            else if (itemTwo.getText().equals(wordToBeGreened))
                                itemTwo.setTextColor(Color.GREEN);
                            else if (itemThree.getText().equals(wordToBeGreened))
                                itemThree.setTextColor(Color.GREEN);
                            else if (itemFour.getText().equals(wordToBeGreened))
                                itemFour.setTextColor(Color.GREEN);
                            else if (itemFive.getText().equals(wordToBeGreened))
                                itemFive.setTextColor(Color.GREEN);

                            if(endGame == true) {
                                loadingVariable = false;
                                endIntent = new Intent(HuntActivity.this, EndActivity.class);
                                endIntent.putExtra("USER", scoresReference.child("id").child("players").child(id).child("username").toString());
                                startActivity(endIntent);
                            }

                            scoresReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot scoreSnap: dataSnapshot.child("id").child("players").getChildren()){
                                        scoreCounter++;
                                        if(scoreCounter == 1) {
                                            usernameOne.setText(scoreSnap.child("username").getValue().toString());
                                            scoreOne.setText(scoreSnap.child("score").getValue().toString());
                                        }
                                        else if (scoreCounter == 2){
                                            usernameTwo.setText(scoreSnap.child("username").getValue().toString());
                                            scoreTwo.setText(scoreSnap.child("score").getValue().toString());
                                            scoreCounter = 0;
                                        }

                                        if(scoreSnap.child("score").getValue().toString().equals("500"))
                                            endGame = true;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

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
        toggleThread.start();

        feature = new Feature();
        feature.setType("LABEL_DETECTION");
        feature.setMaxResults(5);

        pictureTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureTakeFromCamera();
            }
        });
    }

        @Override
        protected void onResume() {
            super.onResume();
            if (checkPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                pictureTake.setVisibility(View.VISIBLE);
            } else {
                pictureTake.setVisibility(View.INVISIBLE);
                makeRequest(Manifest.permission.CAMERA);
            }
        }

        private int checkPermission(String permission) {
            return ContextCompat.checkSelfPermission(this, permission);
        }

        private void makeRequest(String permission) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REC_REQUEST_CODE);
        }

        public void pictureTakeFromCamera() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAM_REQUEST_CODE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode,
        Intent data) {
            if (requestCode == CAM_REQUEST_CODE && resultCode == RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get("data");
                itemImage.setImageBitmap(bitmap);
                callCloudVision(bitmap, feature);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode == REC_REQUEST_CODE) {
                if (grantResults.length == 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                        && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish();
                } else {
                    pictureTake.setVisibility(View.VISIBLE);
                }
            }
        }

        @SuppressLint("StaticFieldLeak")
        private void callCloudVision(final Bitmap bitmap, final Feature feature) {
            final List<Feature> featureList = new ArrayList<>();
            featureList.add(feature);

            final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

            AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
            annotateImageReq.setFeatures(featureList);
            annotateImageReq.setImage(getImageEncodeImage(bitmap));
            annotateImageRequests.add(annotateImageReq);


            new AsyncTask<Object, Void, String>() {
                @Override
                protected String doInBackground(Object... params) {
                    try {

                        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                        VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                        builder.setVisionRequestInitializer(requestInitializer);

                        Vision vision = builder.build();

                        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                        batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                        Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                        annotateRequest.setDisableGZipContent(true);
                        BatchAnnotateImagesResponse response = annotateRequest.execute();
                        return convertResponseToString(response);
                    } catch (GoogleJsonResponseException e) {
                    } catch (IOException e) {
                    }
                    return "Vision Failed";
                }

                protected void onPostExecute(String result) {
                    for(int i = masterList.size()-1; i >= 0; i--){
                        if(itemList.contains(masterList.get(i).toUpperCase())){
                            Log.d("TAG", "MATCH" + masterList.get(i));

                            correctSound.start();

                            score += 100;

                            wordToBeGreened = masterList.get(i);

                            Log.d("TAG", getIntent().getStringExtra("ID"));

                            scoresReference.child("id").child("players").child(id).child("score").setValue(score);

                            customToastCorrectText.setText(masterList.get(i));
                            Toast toast = new Toast(getApplicationContext());
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 500);
                            toast.setView(layoutToastCorrect);
                            toast.show();

                            match = true;

                            masterList.remove(i);
                            Log.d("master", masterList.toString());

                            break;
                        }
                    }
                    if(!match) {
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 500);
                        toast.setView(layoutToastIncorrect);
                        toast.show();

                        incorrectSound.start();
                    }
                    else if(match)
                        match = false;

                    Log.d("TAG", result+"");
                    Log.d("TAG", itemList + "");
                    Log.d("item", masterList+"");
                }
            }.execute();
        }

        @NonNull
        private Image getImageEncodeImage(Bitmap bitmap) {
            Image base64EncodedImage = new Image();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64EncodedImage.encodeContent(imageBytes);
            return base64EncodedImage;
        }

        private String convertResponseToString(BatchAnnotateImagesResponse response) {

            AnnotateImageResponse imageResponses = response.getResponses().get(0);

            List<EntityAnnotation> entityAnnotations;

            String message = "";
            entityAnnotations = imageResponses.getLabelAnnotations();
            for(EntityAnnotation entity : entityAnnotations){
                itemList.add(entity.getDescription().toUpperCase());
            }
            return message;
        }


    }

