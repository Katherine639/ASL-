package com.example.lino.asl;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
    private ArrayList<String> result;


    AnimationDrawable wordAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);
        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
        Translate = (Button) findViewById(R.id.button);

        // Add a OnClickListener object to button3.
        Translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String input_text = mVoiceInputTv.getText().toString();

                if(input_text == ""){
                    Toast.makeText(getApplicationContext(),"Please enter word!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Translating word!",Toast.LENGTH_SHORT).show();
                    PlayWord(input_text);
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
    public void WebScraper(String word) {
        try {
            // fetch the document over HTTP
            Document doc = Jsoup.connect("https://www.signasl.org/sign/" + word).get();

            // Find all videos
            Elements eles = doc.select("meta[itemprop=contentURL]");

            /* Throw Exception if word not found */
            if (eles.isEmpty()) {
                throw new NoSuchElementException("Word was not found.");
            }

            // list that will store all URLs
            List<String> url_links = new ArrayList<>();

            /* Add all URLs to a list */
            String temp_url = "";
            for (Element ele : eles) {
                temp_url = ele.attr("content");

                /* Check if content is a link to video or a YouTube id
                 * Delete this if statement if we don't need to convert to a YouTube link*/
                String substring = temp_url.substring(0, 4);
                if (!"http".equals(substring)) {
                    temp_url = "https://www.youtube.com/watch?v=" + temp_url;
                }

                url_links.add(temp_url);
            }

            /* Test list */
            for (String link : url_links) {
                System.out.println(link);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String Prepare_text(String Input ){
        String parse = "";
        return(parse);
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
