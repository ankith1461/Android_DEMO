package com.continuesvoicerecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener,TextToSpeech.OnInitListener,View.OnLongClickListener
{

    private TextView result_tv,view_text;
    private EditText txtText,EditText2;
    private Button start_listen_btn,stop_listen_btn,text_to_speech,main_next;//mute
    public  Context mContext;

    private SpeechRecognizerManager mSpeechManager;

    private TextToSpeech tts;
    HashMap<String, String> params;

    private MediaPlayer mp;
    private SpeechRecognizer speech = null;
    private final int REQ_CODE_SPEECH_INPUT = 1234;

    private Intent nextIntent;
    private Bundle bundle;
    //private Speaker speaker;
    private final static String TAG="MainActivity";

    private String search ;
    private String normalFileName,fastFileName ;
    private File normalGetList,fastGetList;
    private int normalFileCount = 0;
    private int fasterFileCount = 0;
    private int searchCount = 0;

    private String normalLevelFile = "MyAppFolder/KHALEL AL-HUSSARY  (NORMAL LEVEL)";
    private String fasterLevelFile = "MyAppFolder/KHALEL AL-HUSSARY (FASTER LEVEL)";

   //private String[] CommonList = new String[50];
    private Map<String, String> normalMap,fastMap;


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId.equals("utteranceId1")) {
                        //txtText.setText("");
                    }
                    if (utteranceId.equals("utteranceId2")) {
                        speakOut("Select Level to Play", "0");
                        //NextMethod();
                    }
                    if (utteranceId.equals("utteranceId3")) {
                        //txtText.setText("");
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                NextMethod();
                            }
                        }, 500);
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
            //Log.i("-------------", Arrays.toString(loc.getAvailableLocales()));
            int result = tts.setLanguage(loc);

            view_text.setText(String.valueOf(result));
            //txtText.setText(localeList.toString());

            if(mSpeechManager!=null) {
                mSpeechManager.destroy();
                mSpeechManager=null;
            }
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
                text_to_speech.setEnabled(true);
                //speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.ENGLISH);

        params = new HashMap<String, String>();
        bundle = new Bundle();

        View v1 = findViewById(R.id.view1);
        v1.setOnLongClickListener(this);

        findViews();
        setClickListeners();
        GetChapterNames();


        //TO Speak the chapter name
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //here you can start your Activity B.
                speakOut("What is the chapter Name", "1");
            }

        }, 200);
    }

    public void GetChapterNames()
    {
        try
        {
            File normalChaptersList = new File(Environment.getExternalStorageDirectory(), normalLevelFile);
            File fastChaptersList = new File(Environment.getExternalStorageDirectory(), fasterLevelFile);
            File normalList[] = normalChaptersList.listFiles();
            File fastList[] = fastChaptersList.listFiles();

            normalMap = new HashMap<>();
            fastMap = new HashMap<>();

            if(normalList.length==fastList.length)
            {
                for(int i=0;i<normalList.length-1;i++)
                {
                   // CommonList[i]=normalList[i].toString().replace(Environment.getExternalStorageDirectory() +"/"+ normalLevelFile,"").replaceAll("[^a-zA-Z]","");
                    normalMap.put(normalList[i].toString().replace(Environment.getExternalStorageDirectory() +"/"+ normalLevelFile,"").replaceAll("[^a-zA-Z]","").toLowerCase(),normalList[i].toString());
                }
                for(int k=0;k<fastList.length-1;k++)
                {
                    // CommonList[i]=normalList[i].toString().replace(Environment.getExternalStorageDirectory() +"/"+ normalLevelFile,"").replaceAll("[^a-zA-Z]","");
                    fastMap.put(fastList[k].toString().replace(Environment.getExternalStorageDirectory() +"/"+ fasterLevelFile,"").replaceAll("[^a-zA-Z]","").toLowerCase(),fastList[k].toString());
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }

    private void findViews()
    {
        try
        {
            result_tv = (TextView) findViewById(R.id.result_tv);
            start_listen_btn = (Button) findViewById(R.id.start_listen_btn);
            stop_listen_btn = (Button) findViewById(R.id.stop_listen_btn);
            text_to_speech = (Button) findViewById(R.id.text_to_speech);
            main_next = (Button) findViewById(R.id.main_next);
            txtText = (EditText) findViewById(R.id.txtText);
            view_text = (TextView) findViewById(R.id.view_text);
            EditText2 = (EditText) findViewById(R.id.EditText2);
            //mute=(Button)findViewById(R.id.mute);
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }

    private void setClickListeners()
    {
        try
        {
            start_listen_btn.setOnClickListener(this);
            stop_listen_btn.setOnClickListener(this);
            text_to_speech.setOnClickListener(this);
            main_next.setOnClickListener(this);
            //mute.setOnClickListener(this);
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.start_listen_btn:
                    if (mSpeechManager == null) {
                        SetSpeechListener();
                    } else if (!mSpeechManager.ismIsListening()) {
                        mSpeechManager.destroy();
                        SetSpeechListener();
                    }
                    view_text.setText(getString(R.string.you_may_speak));

                    break;
                case R.id.stop_listen_btn:
                    if (mSpeechManager != null) {
                        view_text.setText(getString(R.string.destroyed));
                        mSpeechManager.destroy();
                        mSpeechManager = null;
                    }
                    break;
//                case R.id.mute:
//                    if(mSpeechManager!=null) {
//                        if(mSpeechManager.isInMuteMode()) {
//                            mute.setText(getString(R.string.mute));
//                            mSpeechManager.mute(false);
//                        }
//                        else
//                        {
//                            mute.setText(getString(R.string.un_mute));
//                            mSpeechManager.mute(true);
//                        }
//                    }
//                    break;

                case R.id.text_to_speech:
                    if (mSpeechManager != null) {
                        mSpeechManager.destroy();
                        mSpeechManager = null;
                    }
                    //txtText.setText(" ");
                    speakOut(txtText.getText().toString(), "0");
                    break;
                case R.id.main_next:
                    if (txtText.getText().toString().isEmpty() || txtText.getText().toString().equals("")) {
                        speakOut("Please Enter Chapter Name", "7");
                    } else {
                        int n = GetFilesCount();
                        if (n == 0) {
                            speakOut("Sorry No Chapter Found", "4");
                            txtText.setText("");
                        }//else {
//                            Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                public void run() {
                        // NextMethod();
//                                }
//
//                            }, 2000);

                        //}
                    }

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void SetSpeechListener()
    {
        try {
            mSpeechManager = new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
                @Override
                public void onResults(ArrayList<String> results) {

                    if (results != null && results.size() > 0) {


                        if (txtText.getText().toString().equals("") || txtText.getText().toString().isEmpty()) {
                            txtText.setText(results.get(0).toLowerCase());//sb.toString()
                            int count = GetFilesCount();
                            if (count == 0) {
                                speakOut("Sorry No Chapter Found", "0");
                                //txtText.setText("");
                            } else {
                                EditText2.requestFocus();
                                //speakOut("Select Level to Play", "0");
                            }
                        }
                        else if (!txtText.getText().toString().equals("") || !txtText.getText().toString().isEmpty()) {
                            String level = results.get(0).toLowerCase();
                            EditText2.setText(level);
                            if (level.equals("normal") || level.equals("faster")) {
                                if (EditText2.getText().toString().toLowerCase().equals("normal")) {
                                    bundle.putString("folderName", normalFileName);
                                    bundle.putInt("numberOfFiles", normalFileCount);
                                }
                                if (EditText2.getText().toString().toLowerCase().equals("faster")) {
                                    bundle.putString("folderName", fastFileName);
                                    bundle.putInt("numberOfFiles", fasterFileCount);
                                }
                                speakOut("You have selected " + EditText2.getText().toString() + " to play", "3");
                            }
                            else {
                                speakOut("Select Valid Level to Play", "0");
                                EditText2.setText("");
                                EditText2.requestFocus();
                            }
                        }
                        else
                        {
                            speakOut("What is the chapter Name", "1");
                        }

                    } else
                        view_text.setText(getString(R.string.no_results_found));
                }
            });
        }catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }

    private int GetFilesCount()
    {
        try
        {
            search = txtText.getText().toString().replaceAll("[^a-z,A-Z]","");

            normalFileName = normalMap.get(search);
            fastFileName = fastMap.get(search);

            normalGetList = new File(normalFileName);
            fastGetList = new File(fastFileName);
            File a[] = normalGetList.listFiles();
            File b[] = fastGetList.listFiles();

            if(a.length == b.length)
            {
                searchCount = a.length;
                normalFileCount = a.length;
                fasterFileCount = b.length;
                speakOut("The chapter " + search + "contains" + " " + a.length + " " + "files", "2");
            }
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
        return searchCount;
    }

    private void speakOut(String s,String n)
    {

        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }

        String Uid = "utteranceId" + n;
        tts.setPitch((float) 1);
        tts.setSpeechRate((float) 0.8);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Uid);
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, params);
    }

    //To startSpeech
    private void StartSpeech()
    {
        try {
            if (mSpeechManager == null) {
                SetSpeechListener();
            } else if (!mSpeechManager.ismIsListening()) {
                mSpeechManager.destroy();
                SetSpeechListener();
            }
            view_text.setText(getString(R.string.you_may_speak));
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        if (mSpeechManager != null) {
            mSpeechManager.destroy();
            mSpeechManager = null;
        }
        StartSpeech();
        return true;
    }

    public void NextMethod()
    {
        try {
            nextIntent = new Intent(this, SecondActivity.class);
            nextIntent.putExtras(bundle);
            startActivity(nextIntent);
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if(mSpeechManager!=null) {
            mSpeechManager.destroy();
            mSpeechManager=null;
        }

        if (mp != null) {
            mp.release();
        }
        super.onDestroy();
        //speaker.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_HOME);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
            finish();
            //android.os.Process.sendSignal(pid, android.os.Process.SIGNAL_KILL);
            tts.stop();
            //System.exit(0);
            //Application.Quit();
        }
        return super.onKeyDown(keyCode, event);
    }

}
