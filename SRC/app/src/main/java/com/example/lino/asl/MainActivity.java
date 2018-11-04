package com.example.lino.asl;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.ArrayList;
import java.util.Locale;

// Libraries for web scraper
import java.util.*;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv;
    private ImageButton mSpeakBtn;
    private Button Translate;
    private ArrayList<String> result;       //result from voice input

    /**Store videos links here ***/
    private List<String> url_results = new ArrayList<String>();

    private YouTubePlayer Player;
    private boolean PlayerIsInitialied = false;
    AnimationDrawable wordAnimation;

    //Youtube
    YouTubePlayerFragment youTubePlayerFragment;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    YouTubePlayer.PlaylistEventListener playlistEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*************  match with xml variables ***************/
        mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);
        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);
        youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_player_View);
        Translate = (Button) findViewById(R.id.button);
        /*************************************************************
         * Youtube player on initialize
         * Creates playlist of videos
         ********************************************************/
        onInitializedListener = new YouTubePlayer.OnInitializedListener(){
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    PlayerIsInitialied = true;
                    Player = player;
                    Player.setShowFullscreenButton(false);
                    Player.loadVideos(url_results,0,0);
                    url_results.clear();

                }
            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {

            }

        };
        /*************************************************************
         * Button method for speak botton
         * Add a OnClickListener object to botton .
         ********************************************************/
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });


        /*************************************************************
         * Button method Translate
         * Add a OnClickListener object to botton translate .
         ********************************************************/
        Translate.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {


                /*************Importante***********
                 * Release the player so that you can
                 * make custome playlist every time that translate
                 * Button is pressed
                 *********************************/
                if(PlayerIsInitialied == true){
                    Player.release();
                    PlayerIsInitialied =false;
                }

                //Retrive text from input text
                String input_text = mVoiceInputTv.getText().toString();


                if(input_text.length() == 0 ){
                    //Default is hello world !
                    PlayWord("hello world");
                    new WebScaper().execute("hello","world");

                    Toast.makeText(getApplicationContext(),"Please enter a word!",Toast.LENGTH_SHORT).show();
                }else{

                    System.out.println("!!!!!!!!!!!!!!!" +  input_text + "!!!!!!!!!!!!!!!");
                    Toast.makeText(getApplicationContext(),"Translating word!",Toast.LENGTH_SHORT).show();

                    String[] input_words = Parse.Prepare_text(input_text);
                    PlayWord(input_text);
                    new WebScaper().execute(input_words);


                }

            }
        });

    }



    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    mVoiceInputTv.setText(result.get(0));


                }
                break;
            }

        }
    }

    // Function that grabs data from ASL web dictionary
    class WebScaper extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... words) {
            // list that will store all URLs


            System.out.println("!!!!!!!!!!!!!!!In side WebScraper!!!!!!!!!!!!!!!");
            for(String word : words){
                try {
                    System.out.println("!!!!!!!!!!!!!!" + word + "!!!!!!!!!!!!!!!");
                    String URL = "https://www.signasl.org/sign/" + word;
                    // fetch the document over HTTP
                    Document doc = Jsoup.connect(URL).get();

                    System.out.println("!!!!!!!!!!!!!!!In side TRY!!!!!!!!!!!!!!!");

                    // Find all videos
                    Elements eles = doc.select("meta[itemprop=contentURL]");

                    /* Throw Exception if word not found */
                    if (eles.isEmpty()) {

                        throw new NoSuchElementException("Word was not found.");

                    }
                    List<String> url_links = new ArrayList<>();

                    /* Add all URLs to a list */
                    String temp_url = "";
                    for (Element ele : eles) {
                        temp_url = ele.attr("content");

                        /* Check if content is a link to video or a YouTube id
                         * Delete this if statement if we don't need to convert to a YouTube link*/
                        String substring = temp_url.substring(0, 4);
                        if (!"http".equals(substring)) {
                            url_links.add(temp_url);

                        }

                    }

                    /* Test list */
                    for (String link : url_links) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!link:  " + link);
                    }
                    url_results.add(url_links.get(0));

                } catch (Throwable t) {
                    System.out.println("!!!!!!!!!!!!!!!CATCH!!!!!!!!!!!!!!!");

                    t.printStackTrace();

                }

            }
            return "Success";
        }

        /**********************Execute playlist after returned urls ***********/
        protected void onPostExecute(String result) {
            // process results
            System.out.println("!!!!!!!!!!!!!!"+result+"!!!!!!!!!!!!!!!");
            youTubePlayerFragment.initialize(PlayerConfig.API_KEY,onInitializedListener);


            }

    }


    //Input text display
    public void PlayWord(String word){

        // variable to check if the image exits
        boolean exist = false;

        // create a new animation
        wordAnimation = new AnimationDrawable();

        // for loop to search each letter in the word and add it to the animation
        for(int i = 0; i < word.length(); i++){

            // gets the ith letter of the word
            String letter = String.valueOf(word.charAt(i));

            // check for numbers and replace the string
            if(letter.equals("0"))
                letter = letter.replace("0","zero");
            if(letter.equals("1"))
                letter = letter.replace("1","one");
            if(letter.equals("2"))
                letter = letter.replace("2","two");
            if(letter.equals("3"))
                letter = letter.replace("3","three");
            if(letter.equals("4"))
                letter = letter.replace("4","four");
            if(letter.equals("5"))
                letter = letter.replace("5","five");
            if(letter.equals("6"))
                letter = letter.replace("6","six");
            if(letter.equals("7"))
                letter = letter.replace("7","seven");
            if(letter.equals("8"))
                letter = letter.replace("8","eight");
            if(letter.equals("9"))
                letter = letter.replace("9","nine");

            // the the resource id of the image
            int drawId = getResources().getIdentifier(letter,"drawable", this.getPackageName());

            // if it doesn't exist the go on to the next one and if it does then add the image to the animation
            if (drawId == 0){
                exist = false;
                continue;
            }else{
                Drawable d = getResources().getDrawable(drawId);
                wordAnimation.addFrame(d, 1500);
                exist = true;
            }

        }

        // if the words exist then play the animation
        if(exist){
            ImageView letterAnim = (ImageView)findViewById(R.id.animationview);
            letterAnim.setBackgroundDrawable(wordAnimation);
            wordAnimation.setOneShot(true);
            wordAnimation.start();
        }else{
            Toast.makeText(getApplicationContext(),"Not found!",Toast.LENGTH_SHORT).show();
        }

    }
}
