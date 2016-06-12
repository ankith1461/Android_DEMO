package com.continuesvoicerecognition;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class StartupActivity extends Activity implements View.OnClickListener,TextToSpeech.OnInitListener {

    private TextToSpeech ttstart;
    HashMap<String, String> params;
    private Intent nextIntent;
    private Bundle bundle;
    private Button startup_next;
    private ImageView logoView;
    private boolean checkAppFolderExists,checkFasterLevel,checkNormalLevel;
    public String AppFolder = "MyAppFolder";
    private String normalLevelFile = "MyAppFolder/KHALEL AL-HUSSARY  (NORMAL LEVEL)";
    private String fasterLevelFile = "MyAppFolder/KHALEL AL-HUSSARY (FASTER LEVEL)";
    private static File chapterName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        try
        {
            View v1 = findViewById(R.id.view1);

            findViews();
            setClickListeners();

            ttstart = new TextToSpeech(this, this);
            ttstart.setLanguage(Locale.ENGLISH);

            params = new HashMap<String, String>();
            bundle = new Bundle();

            //TO Create Main Application folder
            checkAppFolderExists = createAppDirIfNotExists(AppFolder);
            MediaScannerConnection.scanFile(this, new String[]{chapterName.toString()}, null, null);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    //here you can start your Activity B.
                    speakOut("Welcome","1");

                }

            }, 500);
        }
        catch (Exception e)
        {
            Log.e("OnCreate_StartUp",e.getMessage());
        }

    }

    public static boolean createAppDirIfNotExists(String path) {
        boolean ret = true;

        chapterName = new File(Environment.getExternalStorageDirectory(), path);
        if (!chapterName.exists()) {
            if (!chapterName.mkdirs()) {
                Log.e("Log :: ", "Problem creating folder");
                ret = false;
            }
        }
        return ret;
    }

    public static boolean checkLevelDirectory(String path) {
        boolean ret = true;

        chapterName = new File(Environment.getExternalStorageDirectory(), path);
        if (!chapterName.exists()) {
                ret = false;

        }
        return ret;
    }

    private void CallNext() {
        /*Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            public void run() {
                //here you can start your Activity B.
                speakOut("Click Next To Continue","2");
            }

        }, 500);*/
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            ttstart.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId.equals("utteranceId1")) {
                        if(!checkAppFolderExists) {
                            speakOut("Sorry no chapters found to play", "2");
                        }
                        else
                        {
                            checkFasterLevel = checkLevelDirectory(fasterLevelFile);
                            checkNormalLevel = checkLevelDirectory(normalLevelFile);
                            if(!checkFasterLevel)
                            {
                                speakOut("Sorry no faster level files to play","3");
                            }
                            if(!checkNormalLevel)
                            {
                                speakOut("Sorry no normal level files to play","4");
                            }
                            if(checkFasterLevel && checkNormalLevel)
                            {
                                Timer timer1 = new Timer();
                                timer1.schedule(new TimerTask() {
                                    public void run() {
                                        NextActivity();
                                    }

                                }, 1500);

                            }
                        }
                    }
                }

                @Override
                public void onError(String utteranceId) {
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });

            Locale loc =new Locale("en","US");
            int result = ttstart.setLanguage(loc);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    public void NextActivity()
    {
        nextIntent = new Intent(this, MainActivity.class);
        nextIntent.putExtras(bundle);
        startActivity(nextIntent);
    }

    private void speakOut(String s,String n) {

        String Uid = "utteranceId"+n;
        ttstart.setPitch((float) 1);
        ttstart.setSpeechRate((float) 0.9);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Uid);
        ttstart.speak(s, TextToSpeech.QUEUE_FLUSH, params);

    }

    private void findViews()
    {
        startup_next=(Button)findViewById(R.id.startup_next);
        logoView = (ImageView) findViewById(R.id.ImageView1);
    }

    private void setClickListeners()
    {
        startup_next.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.startup_next:
                if (ttstart != null) {
                    ttstart.stop();
                    ttstart.shutdown();
                }
                    NextActivity();
                break;

        }
    }

}
