package com.continuesvoicerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener,TextToSpeech.OnInitListener,View.OnLongClickListener
{

    private TextView result_tv,view_text;
    private EditText txtText;
    private Button start_listen_btn,stop_listen_btn,text_to_speech;//mute

    private SpeechRecognizerManager mSpeechManager;

    private TextToSpeech tts;
    HashMap<String, String> params;

    private MediaPlayer mp;
    private SpeechRecognizer speech = null;
    private final int REQ_CODE_SPEECH_INPUT = 1234;
    private static File chapterName;
    private boolean chapterExists;

    private Intent nextIntent;
    private Bundle bundle;


    //private Speaker speaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);
        params = new HashMap<String, String>();
        bundle=new Bundle();

        View v1 = findViewById(R.id.view1);
        v1.setOnLongClickListener(this);

        findViews();
        setClickListeners();

        //TO Create Main Application folder
         try {
            chapterExists = createDirIfNotExists("MyAppFolder");
            MediaScannerConnection.scanFile(this, new String[]{chapterName.toString()}, null, null);
        }
        catch (Exception e)
        {
            view_text.setText(e.getMessage());
        }

        //TO Speak the chapter name
        final String text = result_tv.getText().toString();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //here you can start your Activity B.
                File path = new File(Environment.getExternalStoragePublicDirectory("MyAppFolder"), "Muhammad Ayyub 093 Ad-Dhuha" + "/" + "01af093001a");
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(path.toString());
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                speakOut(" ","1");
            }

        }, 2000);

    }

    public static boolean createDirIfNotExists(String path) {
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
    private void findViews()
    {
        result_tv=(TextView)findViewById(R.id.result_tv);
        start_listen_btn=(Button)findViewById(R.id.start_listen_btn);
        stop_listen_btn=(Button)findViewById(R.id.stop_listen_btn);
        text_to_speech=(Button)findViewById(R.id.text_to_speech);
        txtText=(EditText)findViewById(R.id.txtText);
        view_text=(TextView)findViewById(R.id.view_text);
        //mute=(Button)findViewById(R.id.mute);
    }

    private void setClickListeners()
    {
        start_listen_btn.setOnClickListener(this);
        stop_listen_btn.setOnClickListener(this);
        text_to_speech.setOnClickListener(this);
        //mute.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(PermissionHandler.checkPermission(this,PermissionHandler.RECORD_AUDIO)) {

            switch (v.getId()) {
                case R.id.start_listen_btn:
                    if(mSpeechManager==null)
                    {
                        SetSpeechListener();
                    }
                    else if(!mSpeechManager.ismIsListening())
                    {
                        mSpeechManager.destroy();
                        SetSpeechListener();
                    }
                    view_text.setText(getString(R.string.you_may_speak));

                    break;
                case R.id.stop_listen_btn:
                    if(mSpeechManager!=null) {
                        view_text.setText(getString(R.string.destroied));
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

                case  R.id.text_to_speech:
                    if(mSpeechManager!=null) {
                        mSpeechManager.destroy();
                        mSpeechManager=null;
                    }
                    txtText.setText(" ");
                      //speakOut(txtText.getText().toString(),"0");
            }
        }
        else
        {
            PermissionHandler.askForPermission(PermissionHandler.RECORD_AUDIO,this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case PermissionHandler.RECORD_AUDIO:
                if(grantResults.length>0) {
                    if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                        start_listen_btn.performClick();
                    }
                }
                break;

        }
    }

    private void SetSpeechListener()
    {
        mSpeechManager=new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {

                if(results!=null && results.size()>0) {
                   // if (txtText.getText().toString() == "" || txtText.getText().toString().isEmpty() || txtText.getText().toString() == null) {
                        txtText.setText(results.get(0).toString());//sb.toString()
                    //} else {
                     //   txtText.setText(txtText.getText().toString() + ".." + "&" + ".." + results.get(0).toString());
                     //   view_text.setText(Environment.getExternalStorageDirectory().getPath());
                   // }
                    //listing all
                    String search = txtText.getText().toString();
                    String filename = "MyAppFolder";
                    File getList = new File(Environment.getExternalStorageDirectory(), filename);
                    String s = "";
                    int fileCount = 0;
                    int searchCount=0;

                    File list[] = getList.listFiles();

                    //ArrayList<String> lwords=GenerateWords(list);

                    for (int i = 0; i < list.length; i++) {
                        if (list[i].getName().trim().toLowerCase().contains(search)) {
                            searchCount++;
                            File getFilenames = new File(Environment.getExternalStoragePublicDirectory("MyAppFolder"), list[i].getName());
                            File listfiles[] = getFilenames.listFiles();

                            for (int j = 0; j < listfiles.length; j++) {
                                fileCount++;
                            }
                            bundle.putString("folderName", list[i].getName());
                            bundle.putInt("numberOfFiles", fileCount);

                            speakOut("Surah" + search +"mempunyai" + " " + fileCount + " " + "ayat", "2");
                        }
                    }

                    if(searchCount==0)
                    {
                        if(mSpeechManager!=null) {
                            mSpeechManager.destroy();
                            mSpeechManager=null;
                        }
                        speakOut("Sorry No Chapter Found", "3");
                    }

                }
                else
                    view_text.setText(getString(R.string.no_results_found));
            }
        });
    }


//    public ArrayList<String> GenerateWords(File listwords[])
//    {
//       ArrayList<String> lw = new ArrayList<String>();
//        for (File each : listwords)
//            lw.add(each.getName().);
//
//
//        return lw;
//    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId.equals("utteranceId1")) {
                        txtText.setText("asdasdasdasd");
                    }
                    if (utteranceId.equals("utteranceId2")) {
                        NextMethod();
                    }
                    if (utteranceId.equals("utteranceId3")) {
                        txtText.setText("");
                    }
                }

                @Override
                public void onError(String utteranceId) {
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });

//            Locale[] locales = Locale.getAvailableLocales();
//            List<Locale> localeList = new ArrayList<Locale>();
//            for (Locale locale : locales) {
//                int res = tts.isLanguageAvailable(locale);
//                if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
//                    localeList.add(locale);
//                }
//            }

            Locale loc =new Locale("in","ID");
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


    private void speakOut(String s,String n) {

        String Uid = "utteranceId"+n;
        tts.setPitch((float) 1);
        tts.setSpeechRate((float) 0.4);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Uid);
       tts.speak(s, TextToSpeech.QUEUE_FLUSH, params);

    }

    //To startSpeech
    private void StartSpeech()
    {
                if (mSpeechManager == null) {
                    SetSpeechListener();
                } else if (!mSpeechManager.ismIsListening()) {
                    mSpeechManager.destroy();
                    SetSpeechListener();
                }
                view_text.setText(getString(R.string.you_may_speak));
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

    public void NextMethod()
    {
        nextIntent = new Intent(this, SecondActivity.class);
        nextIntent.putExtras(bundle);
        startActivity(nextIntent);
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
}
