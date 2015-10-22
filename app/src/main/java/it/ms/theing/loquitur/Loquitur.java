/*
    Loquitur, Main Activity

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

package it.ms.theing.loquitur;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;
import java.util.Stack;

import it.ms.theing.loquitur.functions.Brain;
import it.ms.theing.loquitur.functions.IntentInterface;
import it.ms.theing.loquitur.functions.LoquiturModules;
import it.ms.theing.loquitur.functions.PhoneDir;
import it.ms.theing.loquitur.functions.Storage;
import it.ms.theing.loquitur.functions.Talk;

public class Loquitur extends AppCompatActivity {

    private WebView webView;
    private WebSettings webSettings;
    private Stack<LoquiturModules> modstack;
    public static final String TAG="LOQUITUR";
    private LoquiturWebViewClient loquitur;
    private Handler thisthread=new Handler();


    /**
     * Can be used inside a module to get a reference to another existing module
     * @param name
     * The name of the module
     * @return
     */
    public LoquiturModules getModule(String name) {
        for (LoquiturModules lm:modstack) {
            if (lm.getJavascriptName().equals(name)) {
                return lm;
            }
        }
        return null;
    }


    private void addModule(LoquiturModules lm) {
        webView.addJavascriptInterface(lm, lm.getJavascriptName());
        modstack.push(lm);
    }


    private class LoquiturWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }
    }


    /*
     * Create and add the modules here
     */

    private void initModules(){
        // System Modules
        addModule(new Storage(this));
        addModule(new Brain(this));
        addModule(new Talk(this));
        // Modules
        addModule(new IntentInterface(this));
        addModule(new PhoneDir(this));
    }


    static String stubPage="<!DOCTYPE html>\n" +
    "<html>\n" +
    "<title></title>\n" +
    "<head>\n" +
    "<meta charset=\"UTF-8\">\n" +
    "</head>\n" +
    "<body>\n" +
    "<script type='text/javascript' src='file:///android_asset/code_en.js'>\n" +
    "</script>\n" +
    "</body>\n" +
    "</html>\n";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loquitur);
        webView= (WebView) findViewById(R.id.webView);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(false);
        loquitur=new LoquiturWebViewClient();
        webView.setWebViewClient(loquitur);
        modstack=new Stack<>();
        initModules();
        String postfix=Locale.getDefault().getLanguage();
        if (postfix.length()!=0) {
            stubPage=stubPage.replace("code_en.js","code_"+postfix+".js");
        }
        webView.loadDataWithBaseURL("file:///android_asset/index.html",stubPage,"text/html","UTF-8",null);

    }


    @Override
    protected void onStop() {
        super.onStop();
        webSettings.setJavaScriptEnabled(false);
        while(modstack.size()>0) {
            modstack.pop().endModule();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void executeCallback(String callback) {
        final String temporary=callback;
        thisthread.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + temporary+";");
            }
        });
    }

}


