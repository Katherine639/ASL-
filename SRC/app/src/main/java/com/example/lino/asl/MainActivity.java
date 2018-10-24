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
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
//                Context  context = null;
//                result.set(0,mVoiceInputTv.getText().toString());
//                if(result.get(0) == ""){
//                    Toast toast = Toast.makeText(context, "Enter a word!", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }else{
//                    Toast toast = Toast.makeText(context, "Translating word!", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//
//                }

                // PlayWord(result.get(0));


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

            // Find first video
            Element div = doc.select("meta[itemprop=contentURL]").first();
            String content = div.attr("content");

            // Check if content is a link or YouTube code
            String substring = content.substring(0, 4);
            if (!"http".equals(substring)) {
                content = "https://www.youtube.com/watch?v=" + content;
            }

            // Print link for now
            System.out.println(content);

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
        wordAnimation = new AnimationDrawable();
        for(int i = 0; i < word.length(); i++){
            String letter = String.valueOf(word.charAt(i));
            int drawId = getResources().getIdentifier(letter,"drawable", this.getPackageName());
            Drawable d = getResources().getDrawable(drawId);
            wordAnimation.addFrame(d, 1500);
        }
        ImageView letterAnim = (ImageView)findViewById(R.id.animationview);
        letterAnim.setBackgroundDrawable(wordAnimation);
        wordAnimation.setOneShot(true);
        wordAnimation.start();
    }
}
