package com.continuesvoicerecognition;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    private boolean ready = false;

    private boolean allowed = false;

    public Speaker(Context context){
        tts = new TextToSpeech(context, this);
    }

    public boolean isAllowed(){
        return allowed;
    }

    public void allow(boolean allowed){
        this.allowed = allowed;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            // Change this to match your
            // locale
            tts.setLanguage(Locale.US);
           speak("i am ready");
            ready = true;
        }else{
            ready = false;
        }
    }

    public void speak(String text){

        // Speak only if the TTS is ready
        // and the user has allowed speech
      //&& allowed
        if(ready ) {

            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
       }
    }

//    public void pause(int duration){
//        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
//    }

    // Free up resources
    public void destroy(){
        tts.shutdown();
    }
}