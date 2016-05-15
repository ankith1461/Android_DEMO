package com.continuesvoicerecognition;

import android.app.Activity;
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

public class SecondActivity extends Activity implements TextToSpeech.OnInitListener, View.OnLongClickListener{

    private String folderName,fromFile,toFile,repeatFileCount,rCount;
    private int numberOfFiles;

    private TextView view_text1,result_tv1,result_tv2,result_tv3;
    private EditText txtText1,txtText2,txtText3;

    private TextToSpeech tts1;
    private Bundle bundle;

    private SpeechRecognizerManager mSpeechManager;
    HashMap<String, String> params;
    private MediaPlayer mp;

    public ArrayList<String> filenames;
    private ArrayList<MediaPlayer> mPlayerList = new ArrayList<MediaPlayer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        findViews();
        bundle=getIntent().getExtras();

        View v2 = findViewById(R.id.view2);
        v2.setOnLongClickListener(this);

        txtText1.requestFocus();
        tts1 = new TextToSpeech(this, this);
        params = new HashMap<String, String>();

        folderName= bundle.getString("folderName");
        numberOfFiles=bundle.getInt("numberOfFiles");

        //TO Speak the chapter name
        final String text = result_tv1.getText().toString();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //here you can start your Activity B.
                speakOut(text + "........................" + " ", "1");
            }

        }, 2000);

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
    }

    private void SetSpeechListener()
    {
        mSpeechManager=new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {

                if(results!=null && results.size()>0)
                {
                    repeatFileCount="";
                    if(txtText1.getText().toString()==""||txtText1.getText().toString().isEmpty()||txtText1.getText().toString()==null) {
                        fromFile = results.get(0).toString();
                        txtText1.setText(fromFile);
                        txtText3.requestFocus();
                        speakOut(result_tv3.getText().toString(), "2");
                    }
                    else if(txtText3.getText().toString()==""||txtText3.getText().toString().isEmpty()||txtText3.getText().toString()==null) {
                        toFile = results.get(0).toString();
                        txtText3.setText(toFile);
                        txtText2.requestFocus();
                        speakOut(result_tv2.getText().toString(), "3");
                    }
                    else {
                        repeatFileCount = results.get(0).toString();
                        txtText2.setText(repeatFileCount);
                        SelectFilesToPlay(fromFile,toFile,repeatFileCount);
                    }

                }
                else
                    view_text1.setText(getString(R.string.no_results_found));
            }
        });
    }

    public void SelectFilesToPlay(String fromFile,String toFile,String count)
    {
        Integer maxfile,minfile;
        File getList = new File(Environment.getExternalStoragePublicDirectory("MyAppFolder"), folderName);
        File listfiles[] = getList.listFiles();

        rCount=count;
        filenames = new ArrayList<String>();
        for (int j = Integer.parseInt(fromFile)-1; j <= Integer.parseInt(toFile)-1; j++) {
            //filenames.add(Integer.parseInt(listfiles[j].getName().substring(4, 10)));
            filenames.add(listfiles[j].getName());
        }
        //maxfile= Collections.max(filenames);
        //minfile=Collections.min(filenames);

        //view_text1.setText(String.valueOf(minfile)+"...."+String.valueOf(maxfile));
         PlayMusic(filenames, count);
    }

    private void speakOut(String s,String n) {

        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }

        String uid="utteranceId"+n;
        tts1.setPitch((float) 1);
        tts1.setSpeechRate((float) 0.5);
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

            Locale loc =new Locale("in","ID");
            int result = tts1.setLanguage(loc);

            if(mSpeechManager!=null) {
                mSpeechManager.destroy();
                mSpeechManager=null;
            }
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
                //text_to_speech.setEnabled(true);
                //speakOut();
            }

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
            for(int j=0; j< Integer.parseInt(PlayCount); j++) {
                for (String filename : filenames) //Do not include last element
                {
                    String filePath = folderName + "/" + filename;
                    File path = new File(Environment.getExternalStoragePublicDirectory("MyAppFolder"), filePath);
                    mp = new MediaPlayer();
                    mp.setDataSource(path.toString());
                    mp.prepare();
//                final int[] count = {0};
//                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                            if (count[0] < Integer.parseInt(PlayCount) - 1) {
//                                mp.start();
//                                count[0]++;
//                            }
//                        }
//                });
                    // mp.start();

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



}
