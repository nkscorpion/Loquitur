/*
    Loquitur, Contents Module

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

import android.content.ContentValues;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import it.ms.theing.loquitur.Loquitur;


/**
 * Contents provider handling.
 */
public class ContentsInterface implements LoquiturModules {

    private ContentValues values;
    private Loquitur context;


    public ContentsInterface(Loquitur loq) {
        context=loq;
    }


    /**
     * Create a new empty content
     */
    @JavascriptInterface
    public void create() {
        values = null;
        try {
            values = new ContentValues();
        } catch (Exception e){}
    }

    /**
     * Add a key with a string content into the current content
     * @param key
     * the key
     * @param value
     * the string value
     */
    @JavascriptInterface
    public void addString(String key,String value) {
        try {
            values.put(key, value);
        }catch (Exception e){}
    }

    /**
     * Add a key with a boolean content into the current content
     * @param key
     * the key
     * @param value
     * the boolean value
     */
    @JavascriptInterface
    public void addBoolean(String key,boolean value) {
        try {
        values.put(key, value);
        }catch (Exception e){}
    }

    /**
     * Add a key with a integer content into the current content
     * @param key
     * the key
     * @param value
     * the integer value
     */
    @JavascriptInterface
    public void addInt(String key,int value) {
        try {
        values.put(key, value);
        }catch (Exception e){}
    }

    /**
     * Add a key with a long content into the current content
     * @param key
     * the key
     * @param value
     * the long value
     */
    @JavascriptInterface
    public void addLong(String key,long value) {
        try {
            values.put(key, value);
        }catch (Exception e){}
    }

    /**
     * Add a key with a float content into the current content
     * @param key
     * the key
     * @param value
     * the float value
     */

    @JavascriptInterface
    public void addFloat(String key,float value) {
        try {
        values.put(key, value);
        }catch (Exception e){}
    }

    /**
     * Add a key with a double content into the current content
     * @param key
     * the key
     * @param value
     * the double value
     */

    @JavascriptInterface
    public void addDouble(String key,double value) {
        try {
            values.put(key, value);
        }catch (Exception e){}
    }

    /**
     * Get the current content and put it into the provider
     * @param uri
     * The provider URI
     * @return
     * The new item ID or -1 if the insertion failed.
     */

    @JavascriptInterface
    public long set(String uri) {
        try {
            Uri eventUri = context.getApplicationContext().getContentResolver().insert(Uri.parse(uri), values);
            long ID = Long.parseLong(eventUri.getLastPathSegment());
            return ID;
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * Delete a content on a where clauses
     * @param uri
     * The content uri
     * @param where
     * the where clause
     * @return
     * the number of contents involved in deletion.
     */

    @JavascriptInterface
    public int delete(String uri,String where) {
        try {
            int out = context.getApplicationContext().getContentResolver().delete(Uri.parse(uri), where, null);
            return out;
        } catch (Exception e) {return 0;}
    }


    @Override
    public String getJavascriptName() {
        return "Contents";
    }

    @Override
    public void endModule() {

    }
}
