/*
    Loquitur, Storage module

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

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * This is a special open scheme database.
 * it uses a table with indexes and 3 fields.
 * The "genre" is the basic scheme, for example 'place', 'date' et cetera .
 * The key is the search key
 * The value is the value associated to the key
 */

public class Storage extends SQLiteOpenHelper implements LoquiturModules {


    private Activity context;

    public Storage(Activity context) {
        super(context, "storage", null, 1);
        this.context=context;
        dataBase = getWritableDatabase();
    }


    private SQLiteDatabase dataBase=null;


    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }


    @Override
    public void close() {
        if (dataBase!=null)
        {
            super.close();
            dataBase=null;
        }
    }


    /**
     * Delete a specific key
     * @param genre
     * The genre of the key
     * @param key
     * The key
     * @return
     * true if deleted false if not.
     */
    @JavascriptInterface
    public boolean deleteKey(String genre,String key) {
        int num=dataBase.delete("alias","genre=? and key=?",new String[] {genre.toUpperCase(),key});
        if (num!=0) return true;
        return false;
    }


    /**
     * Get a key value
     * @param genre
     * The genre of the key
     * @param key
     * The key
     * @return
     * The key value
     */
    @JavascriptInterface
    public String getKey(String genre,String key) {
        Cursor cursor= dataBase.rawQuery("select * from alias where genre=? and key=?",new String[] {genre.toUpperCase(),key});
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            return "";
        }
        String s=cursor.getString(2);
        cursor.close();
        return s;
    }


    /**
     * Set a key. If the value already exists is overwritten
     * @param genre
     * The genre of the key
     * @param key
     * The key
     * @param value
     * The key value
     */
    @JavascriptInterface
    public void setKey(String genre,String key,String value) {
        ContentValues val=new ContentValues();
        val.put("genre", genre.toUpperCase());
        val.put("key",key);
        val.put("value",value);
        dataBase.insert("alias", null, val);
    }


    /**
     * Get a list of keys and related values
     * @param genre
     * The genre of the key
     * @return
     * a string conaining a json array of json objects structure :
     * [ { key:<name>, value:<value>} , ... ]
     */

    @JavascriptInterface
    public String getList(String genre) {
        Cursor cursor= dataBase.rawQuery("select * from alias where genre=?",new String[] {genre.toUpperCase()});
        cursor.moveToFirst();
        JSONArray ja=new JSONArray();
        while(!cursor.isAfterLast()) {
            JSONObject jo=new JSONObject();
            try {
                jo.put("key",cursor.getString(1));
                jo.put("value",cursor.getString(2));
            } catch (JSONException e) {
            }
            ja.put(jo);
            cursor.moveToNext();
        }
        return ja.toString();
    }



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists alias ( genre text not null , key text not null , value text not null )");
        sqLiteDatabase.execSQL("create index if not exists alias_key on alias ( key )");
        sqLiteDatabase.execSQL("create index if not exists alias_genre on alias ( genre )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public Vector<Pair<String[], String>> loadCache(String name) {
        Cursor cursor= dataBase.rawQuery("select * from alias where genre=?",new String[] {name});
        cursor.moveToFirst();
        Vector<Pair<String[], String>> ja=new Vector<Pair<String[], String>>();
        while(!cursor.isAfterLast()) {
            String[] sv=cursor.getString(1).split("\'| ");
            String s=cursor.getString(2);
            Pair<String[], String> p=Pair.create(sv,s);
            ja.add(p);
            cursor.moveToNext();
        }
        return ja;
    }

    @Override
    public String getJavascriptName() {
        return "Storage";
    }

    @Override
    public void endModule() {
        close();
    }
}
