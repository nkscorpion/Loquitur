/*
    Loquitur, Intent module

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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import it.ms.theing.loquitur.Loquitur;
import it.ms.theing.loquitur.Utils;


/**
 * The IntentInterface provides everything needed to handle the intent
 * subsystem.
 */
public class IntentInterface implements LoquiturModules {

    private Intent intent;
    private Loquitur context;


    public IntentInterface(Loquitur loquitur) {
        context=loquitur;
    }


    /**
     * Create a new intent by the application name
     * @param argument
     * Application name
     */
    @JavascriptInterface
    public void launchFromName(String argument) {
        try {
            PackageManager packman = context.getPackageManager();
            intent = packman.getLaunchIntentForPackage(argument);
        } catch (Exception e){Utils.safe(e);}

    }


    /**
     * Create a new content by the action type
     * @param type
     * action type
     */
    @JavascriptInterface
    public void create(String type) {
        try {
            intent = new Intent(type);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Set the data for the current intent
     * @param argument
     * is the first part of the data, usually a uri header, not encoded
     * @param rest
     * this part of the data is encoded in uri format
     */
    @JavascriptInterface
    public void data(String argument,String rest) {
        try {
            if (rest.length() != 0) {
                argument += Uri.encode(rest);
            }
            intent.setData(Uri.parse(argument));
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Set the required package
     * @param pack
     * The name of the package
     */
    @JavascriptInterface
    public void setPackage(String pack) {
        try{
            intent.setPackage(pack);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Add an extra string key
     * @param key
     * Extra key
     * @param value
     * String value
     */
    @JavascriptInterface
    public void addString(String key,String value) {
        try {
            intent.putExtra(key, value);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Add an extra boolean key
     * @param key
     * Extra key
     * @param value
     * Boolean value
     */
    @JavascriptInterface
    public void addBoolean(String key,boolean value) {
        try {
            intent.putExtra(key, value);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Add an extra integer key
     * @param key
     * Extra key
     * @param value
     * Integer value
     */
    @JavascriptInterface
    public void addInt(String key,int value) {
        try {
            intent.putExtra(key, value);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Add an extra double key
     * @param key
     * Extra key
     * @param value
     * double value
     */
    @JavascriptInterface
    public void addDouble(String key,double value) {
        try {
            intent.putExtra(key, value);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Add an extra float key
     * @param key
     * Extra key
     * @param value
     * float value
     */
    @JavascriptInterface
    public void addFloat(String key,float value) {
        try {
            intent.putExtra(key, value);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Add an extra long key
     * @param key
     * Extra key
     * @param value
     * long value
     */
    @JavascriptInterface
    public void addLong(String key,long value) {
        try {
            intent.putExtra(key, value);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Set the mime type fro the current intent
     * @param mime
     * The mime type string
     */
    @JavascriptInterface
    public void type(String mime) {
        try {
            intent.setType(mime);
        } catch (Exception e){Utils.safe(e);}
    }

    /**
     * Run the current intent
     */
    @JavascriptInterface
    public void run() {
        try {
            context.startActivity(intent);
        } catch (Exception e){Utils.safe(e);}
    }


    /**
     * List All Apps
     * @return
     * A string containing the json array of json object :
     * Structure :
     * [ { name:<name>  , package:<package> }, .... ]
     */
    @JavascriptInterface
    public String listApps() {
        try {
            JSONArray ja = new JSONArray();
            PackageManager packman;
            packman = context.getPackageManager();
            List<PackageInfo> pak = packman.getInstalledPackages(0);
            for (PackageInfo pi : pak) {
                String name = (String) packman.getApplicationLabel(pi.applicationInfo);
                String pk = (String) pi.packageName;
                JSONObject jo = new JSONObject();
                try {
                    jo.put("name", name);
                    jo.put("package", pk);
                    ja.put(jo);
                } catch (JSONException e) {
                }

            }
            return ja.toString();
        } catch (Exception e){Utils.safe(e);}
        return "[ ]";
    }

    /**
     * Find a similar app
     * @param compare
     * The name of the app to match with the list
     * @param minscore
     * The minimum accuracy
     * @return
     * The best matching
     */

    @JavascriptInterface
    public String matchApp(String compare,float minscore) {
        try {
            PackageManager packman;
            packman = context.getPackageManager();
            List<PackageInfo> pak = packman.getInstalledPackages(0);
            PackageInfo spi=null;
            for (PackageInfo pi : pak) {
                String name = (String) packman.getApplicationLabel(pi.applicationInfo);
                float score = Utils.match(name, compare);
                if (score > minscore) {
                    minscore = score;
                    spi = pi;
                }
            }
            if (spi==null) return "";
            String name = (String) packman.getApplicationLabel(spi.applicationInfo);
            String pk = (String) spi.packageName;
            JSONObject jo = new JSONObject();
            jo.put("name", name);
            jo.put("package", pk);
            return jo.toString();
        } catch (Exception e){Utils.safe(e);}
        return "";
    }



    /**
     * Close the program activiy
     */
    @JavascriptInterface
    public void finish() {
        // This is not protected because it closes the main application
        context.finish();
    }





    @Override
    public String getJavascriptName() {
        return "Intent";
    }

    @Override
    public void endModule() {

    }
}
