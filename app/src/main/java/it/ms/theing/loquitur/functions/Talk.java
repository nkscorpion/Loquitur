/*
    Loquitur, Talk module

    Copyright (C) 2015 by TheIng
    http://github.com/theing/Loquitur

    This file is part of Loquitur.

    Loquitur is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package it.ms.theing.loquitur.functions;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import it.ms.theing.loquitur.Loquitur;


/**
 * This module implements the voice interaction engine
 */

public class Talk implements LoquiturModules {

    private final Loquitur context;
    private final HashMap<String, String> hashRender = new HashMap<String, String>();
    private String talkCallback="";
    private String speechResult="";
    private SpeechRecognizer speech=null;
    private TextToSpeech tts=null;
    private String recognizerCallback="";
    private Handler thisthread=new Handler();
    private boolean notified=false;
    private boolean beginning=false;

    private final TextToSpeech.OnInitListener initListener=new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                hashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "loquitur");
                tts.setLanguage(Locale.getDefault());
                tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                    @Override
                    public void onUtteranceCompleted(String s) {
                        context.executeCallback(talkCallback);
                    }
                });
            }
        }
    };

    /**
     * Constructor
     * @param context the activity
     */
    public Talk(Loquitur context) {
        this.context = context;
        tts = new TextToSpeech( context,initListener);
        speech = SpeechRecognizer.createSpeechRecognizer(context);
        speech.setRecognitionListener(loquiturListener);
    }

    /**
     * say , talks with the default language and, when finished, it call the callback function.
     * example : Talk.say('This is a sentence','myCallback()');
     * @param arg
     * @param callback
     */
    @JavascriptInterface
    public void say(String arg,String callback) {
        this.talkCallback=callback;
        tts.speak(arg, TextToSpeech.QUEUE_FLUSH, hashRender);
    }


    /**
     * Used to disconnect everything, should be called when the activity stops.
     */

    public void destroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        if (speech!=null) {
            speech.stopListening();
            speech.cancel();
            speech.destroy();
        }
    }

    /**
     * After a listen session, you can use this method to retrieve the
     * current speechResult.
     * @return
     */

    @JavascriptInterface
    public String getSentence() {
        return speechResult;
    }


    /**
     * This is used to listen the user command. After the command has been spoken the system
     * will call che callback function, that will be able to retrieve the content with :
     * getSentence.
     *
     * Example:
     * listen('myCallback()')
     * function myCallback() {
     *     var sentence = getSentence();
     * }
     *
     * Note : if there are problems with the network connections, the system will use the internal
     *        speech recognizer (if available).
     *
     * @param callb
     */

    @JavascriptInterface
    public void listen(final String callb) {

            thisthread.post(new Runnable() {
                @Override
                public void run() {
                    notified=false;
                    recognizerCallback = callb;
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
                    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                    speechResult = "";
                    try {
                        beginning=false;
                        speech.startListening(intent);
                    } catch (Exception e) {}

                }
            });
        }






    private final RecognitionListener loquiturListener=new RecognitionListener() {


        public void onReadyForSpeech(Bundle bundle) {
        }

        public void onBeginningOfSpeech() {
            beginning=true;
        }

        public void onRmsChanged(float rms)
        {
        }
        public void onBufferReceived(byte[] buffer)
        {
        }
        public void onEndOfSpeech()
        {
            speech.stopListening();
        }
        public void onError(int error)
        {
            if (!beginning) return;
            if (notified) return;
            switch(error) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    break;
                default:
                    return;
            }
            notified=true;
            context.executeCallback(recognizerCallback);

        }
        public void onResults(Bundle results)
        {
            String str = new String();
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data.size()>0) speechResult=data.get(0);
            context.executeCallback(recognizerCallback);
        }
        public void onPartialResults(Bundle results)
        {
            String str = new String();
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data.size()>0) speechResult=data.get(0);
        }
        public void onEvent(int eventType, Bundle params)
        {
        }

    };


    /**
     * Get the default language for the device
     * @return
     *
     */

    @JavascriptInterface
    public String getDefaultLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * Get the default country for the device
     * @return
     *
     */

    @JavascriptInterface
    public String getDefaultCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * Get the default timezone for the device
     * @return
     */

    @JavascriptInterface
    public String getDefaultTimezone() {
        return TimeZone.getDefault().getID();
    }


    @Override
    public String getJavascriptName() {
        return "Talk";
    }

    @Override
    public void endModule() {
        destroy();
    }
}
