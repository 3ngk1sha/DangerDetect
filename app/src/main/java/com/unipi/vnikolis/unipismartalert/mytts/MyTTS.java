package com.unipi.vnikolis.unipismartalert.mytts;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Κάνει speak οτι του δηλωσεις(φτιαχνουμε τα settings εδω) Text To Translate
 */

//
public class MyTTS {

    private TextToSpeech tts;

    //Constructor
    public MyTTS(Context context)
    {
        //με την κλάσση TextToSpeech καλούμε την μέθοδο OnInitListener η οποία ειναι τυπου interface επιστρεφει δηλαδη interface
        TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //σε τι γλωσσα θελω να ξεκινησω
                if (status == TextToSpeech.SUCCESS)
                    tts.setLanguage(Locale.ENGLISH);  //root = για οτι γλώσσα είναι η συσκευη του κινητού
            }
        };
        tts = new TextToSpeech(context, initListener);
    }

    public void speak(String what2say){
        tts.speak(what2say, TextToSpeech.QUEUE_ADD,null);// δουλευει η μεθοδος
    }
}
