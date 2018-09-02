package com.alice.knowyourmoney;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TalkInput extends Activity{

    private SpeechRecognizer recognizer;
    private StringBuilder sentence = null;
    private TextView myInputText = null;

    private RecognizerListener recognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
        }

        @Override
        public void onBeginOfSpeech() {
            Toast.makeText(TalkInput.this, "start talking", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEndOfSpeech() {

            Toast.makeText(TalkInput.this, "stop talking", Toast.LENGTH_SHORT).show();
            myInputText.setText(sentence.toString());
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            try {
                if(!b) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray words = jsonObject.getJSONArray("ws"); //words
                    sentence = new StringBuilder("");
                    for (int i = 0; i < words.length(); i++) {
                        JSONObject word = words.getJSONObject(i);
                        JSONArray subArray = word.getJSONArray("cw"); //current word
                        JSONObject subWord = subArray.getJSONObject(0);
                        String character = subWord.getString("w");
                        Log.d("alice_debug", "character " + i + " is " + character);
                        Log.d("alice_debug", "b is " + b);
                        sentence.append(character);
                    }
                }
                //Log.e("alice_debug", sentence.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(SpeechError speechError) {
            speechError.getPlainDescription(true);
            Toast.makeText(TalkInput.this, "Return error", Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };



    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("alice_debug", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(TalkInput.this,"initiallize failed. Failed code：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_input);
            Log.d("alice_debug","I am here into the class");
            SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5b828660");

        recognizer = SpeechRecognizer.createRecognizer(this,mInitListener);

        recognizer.setParameter(SpeechConstant.DOMAIN, "iat");
        recognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        recognizer.setParameter(SpeechConstant.ACCENT, "mandarin ");
        Button myTalkInput = findViewById(R.id.receive_input);
        myInputText = findViewById(R.id.converted_input);
        myTalkInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizer.startListening(recognizerListener);
                myInputText.setText("start to listen what you are saying");
            }
        });
    }
}
