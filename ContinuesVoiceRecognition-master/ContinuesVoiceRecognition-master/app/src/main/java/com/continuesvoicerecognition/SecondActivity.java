package com.continuesvoicerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SecondActivity extends Activity implements View.OnClickListener,TextToSpeech.OnInitListener, View.OnLongClickListener{

    private String folderName,fromFile,toFile,repeatFileCount,rCount;
    private int numberOfFiles;

    private TextView view_text1,result_tv1,result_tv2,result_tv3;
    private EditText txtText1,txtText2,txtText3;
    private Button Second_back,Play;

    private TextToSpeech tts1;
    private Bundle bundle;
    private Intent nextIntent;

    private SpeechRecognizerManager mSpeechManager;
    HashMap<String, String> params;
    private MediaPlayer mp;
    private final static String TAG="SecondActivity";
    public ArrayList<String> filenames;
    private ArrayList<MediaPlayer> mPlayerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        findViews();
        setClickListeners();
        bundle=getIntent().getExtras();

        View v2 = findViewById(R.id.view2);
        v2.setOnLongClickListener(this);

        txtText1.requestFocus();
        tts1 = new TextToSpeech(this, this);
        params = new HashMap<>();

        folderName= bundle.getString("folderName");
        numberOfFiles=bundle.getInt("numberOfFiles");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //here you can start your Activity B.
                speakOut("Select the Start File" + "........................" + " ", "1");
            }

        }, 1000);

    }

    private void findViews()
    {
        result_tv1=(TextView)findViewById(R.id.result_tv1);
        result_tv2=(TextView)findViewById(R.id.result_tv2);
        result_tv3=(TextView)findViewById(R.id.result_tv3);
        view_text1=(TextView)findViewById(R.id.view_text1);
        txtText1=(EditText)findViewById(R.id.txtText1);
        txtText2=(EditText)findViewById(R.id.txtText2);
        txtText3=(EditText)findViewById(R.id.txtText3);
        Second_back=(Button)findViewById(R.id.Second_back);
        Play=(Button)findViewById(R.id.Play);
    }

    private void setClickListeners()
    {
        Second_back.setOnClickListener(this);
        Play.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Second_back:
                if(mSpeechManager!=null) {
                    mSpeechManager.destroy();
                    mSpeechManager=null;
                }
                if (tts1 != null) {
                    tts1.stop();
                    tts1.shutdown();
                }

                if (mp!=null) {
                    mp = new MediaPlayer();
                    mPlayerList.remove(mp);
                    mp.stop();
                    mp.release();
                    mp=null;
                }

                txtText1.setText("");
                txtText2.setText("");
                txtText3.setText("");

                nextIntent = new Intent(this, MainActivity.class);
                startActivity(nextIntent);

                break;
            case R.id.Play:
                if (txtText1.getText().toString().isEmpty() || txtText1.getText().toString().equals("")) {
                    speakOut("Please Enter Start File", "4");
                } else if (txtText3.getText().toString().isEmpty() || txtText3.getText().toString().equals("")) {
                    speakOut("Please Enter End File", "5");
                } else if (txtText2.getText().toString().isEmpty() || txtText2.getText().toString().equals("")) {
                    speakOut("Please Enter Number of times you want to play", "6");
                }else{
                    repeatFileCount = txtText2.getText().toString();
                    fromFile = txtText1.getText().toString();
                    toFile = txtText3.getText().toString();
                    if (isValidInteger(repeatFileCount)) {
                        SelectFilesToPlay(fromFile, toFile, repeatFileCount);
                    } else {
                        speakOut("Please enter valid number to repeat", "7");
                    }
                }
        }
    }

    private void SetSpeechListener()
    {
        mSpeechManager=new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {

                if(results!=null && results.size()>0)
                {
                    repeatFileCount="";
                    if(txtText1.getText().toString().equals("")||txtText1.getText().toString().isEmpty()) {
                        fromFile = results.get(0);
                        txtText1.setText(fromFile);
                        if(isValid("fromFile",fromFile)) {
                            txtText1.setText(fromFile);
                            txtText3.requestFocus();
                            speakOut("Select the end File ", "2");
                        }else {
                            speakOut("Please enter valid start file", "4");
                            txtText1.setText("");
                        }
                    }
                    else // For To
                    if(txtText3.getText().toString().equals("")||txtText3.getText().toString().isEmpty()) {
                        toFile = results.get(0);
                        txtText3.setText(toFile);
                        if(isValid("toFile",toFile)) {
                            txtText3.setText(toFile);
                            txtText2.requestFocus();
                            speakOut("How Many times you want to play", "3");
                        }else {
                            speakOut("Please enter valid End file", "4");
                            txtText3.setText("");
                        }
                    }
                    else { // For Repeat count
                        repeatFileCount = results.get(0);
                        txtText2.setText(repeatFileCount);
                        if(isValid("repeatFileCount",repeatFileCount)) {
                            try {
                                SelectFilesToPlay(fromFile, toFile, repeatFileCount);
                            }
                            catch (Exception e)
                            {
                                Log.e(TAG,e.getMessage());
                            }
                        }else {
                            speakOut("Please enter valid number to repeat", "4");
                        }
                    }

                }
                else
                    view_text1.setText(getString(R.string.no_results_found));
            }
        });
    }

    //To Validate fromFile toFile and repeatCount values
    public boolean isValid(String valueType,String value)
    {
        boolean flag = false;
        switch(valueType){
            case "fromFile" :
                if(isValidInteger(value)) {
                    if(Integer.parseInt(value)>0 && Integer.parseInt(value)<=numberOfFiles)
                    {
                        flag = true;
                    }
                }
                break;
            case "toFile" :
                if(isValidInteger(value)) {
                    if(Integer.parseInt(value)>0 && Integer.parseInt(value)<=numberOfFiles && Integer.parseInt(value)>Integer.parseInt(fromFile) )
                    {
                        flag = true;
                    }
                }

                break;
            case "repeatFileCount" :
                if(isValidInteger(value)) {
                    flag = true;
                }

                break;
            default :
                //Statements
        }
        return flag;
    }

    public void SelectFilesToPlay(String fromFile,String toFile,String count)
    {
        File getList = new File(folderName);
        File listfiles[] = getList.listFiles();
        rCount=count;
        filenames = new ArrayList<>();
        for (int j = Integer.parseInt(fromFile)-1; j <= Integer.parseInt(toFile)-1; j++) {
            filenames.add(listfiles[j].getName());
        }
         PlayMusic(filenames, count);
    }

    private void speakOut(String s,String n) {

        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }

        String uid="utteranceId"+n;
        tts1.setPitch((float) 1);
        tts1.setSpeechRate((float)0.8);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uid);
        tts1.speak(s, TextToSpeech.QUEUE_FLUSH, params);

    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            tts1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId.equals("utteranceId1") || utteranceId.equals("utteranceId2")
                            ||utteranceId.equals("utteranceId3")) {
                        StartSpeech();
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
            int result = tts1.setLanguage(loc);

            if(mSpeechManager!=null) {
                mSpeechManager.destroy();
                mSpeechManager=null;
            }
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } //else {
                //text_to_speech.setEnabled(true);
                //speakOut();
            //}

        } else {
            Log.e("TTS", "Initilization Failed");
        }

    }

    private void StartSpeech()
    {
        if (mSpeechManager == null) {
            SetSpeechListener();
        } else if (!mSpeechManager.ismIsListening()) {
            mSpeechManager.destroy();
            SetSpeechListener();
        }
        view_text1.setText(getString(R.string.you_may_speak));
    }

    //To Play Music
    public void PlayMusic(ArrayList<String> filenames, final String PlayCount) {

        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }
        if (mp != null) {
            mp.release();
        }
        mPlayerList.clear();
        // Create a new MediaPlayer to play this sound
        try {
            for(int z=0; z< Integer.parseInt(PlayCount); z++) {
                for (String filename : filenames) //Do not include last element
                {
                    String filePath = folderName + "/" + filename;
                    File path = new File(filePath);
                    mp = new MediaPlayer();
                    mp.setDataSource(path.toString());
                    mp.prepare();
                    mPlayerList.add(mp);
                }
            }
        }
        catch (Exception e)
        {
            txtText2.setText(e.getMessage());
        }

        view_text1.setText(String.valueOf(mPlayerList.size()));

            for (int i = 0; i < mPlayerList.size() - 1; i++) //Do not include last element
            {
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                try {
                    mPlayerList.get(i).setNextMediaPlayer(mPlayerList.get(i + 1));
                } catch (Exception e) {
                    txtText1.setText(e.getMessage());
                }
                // }
            }

            mPlayerList.get(0).start();
    }

    @Override
    public boolean onLongClick(View v) {

        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager = null;
        }

        StartSpeech();
        return true;
    }

    public static Boolean isValidInteger(String value) {
        try {
            Integer val = Integer.valueOf(value);
            return val != null;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
