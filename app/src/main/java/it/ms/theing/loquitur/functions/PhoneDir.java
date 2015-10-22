/*
    Loquitur, Phone Directory

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

import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import it.ms.theing.loquitur.Loquitur;
import it.ms.theing.loquitur.Utils;


/**
 * Read Phone dir
 */

public class PhoneDir implements  LoquiturModules {


    private Loquitur context;


    public PhoneDir(Loquitur activity) {
        context=activity;
    }

    @Override
    public String getJavascriptName() {
        return "PhoneDir";
    }

    @Override
    public void endModule() {

    }



    private String getEmail(String ID) {
        Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + ID, null, null);
        String s="";
        if (emails.moveToNext()) {
            s = emails.getString( emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        }
        emails.close();
        return s;
    }

    private String getPhone(String ID) {
        Cursor phones = context.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ ID, null, null);
        String s="";
        if (phones.moveToNext()) {
            s = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        phones.close();
        return s;
    }


    /**
     * Get the last called number
     * @return
     * The last called number
     */

    @JavascriptInterface
    public String getLastCalledNumber() {
        try {
            String lastCalledNumber = CallLog.Calls.getLastOutgoingCall(context);
            return lastCalledNumber;
        } catch (Exception e) {Utils.safe(e);}
        return "";
    }


    /**
     * This function returns the best matching object
     * @return
     * structure : { name:... , phone: ... , email: ... }
     */
    @JavascriptInterface
    public String match(String compare,float minscore) {
        Cursor cursor = null;
        try {
            JSONArray obj = new JSONArray();
            cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            String ID = null;
            String name = null;
            while (cursor.moveToNext()) {

                String nam = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                float score = Utils.match(nam, compare);
                if (score > minscore) {
                    minscore = score;
                    ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    name = nam;
                }
            }
            cursor.close();
            cursor=null;
            if (ID == null) return "";

            String phone = getPhone(ID);
            String email = getEmail(ID);

            JSONObject jo = new JSONObject();
            jo.put("name", name.toLowerCase());
            jo.put("phone", phone);
            jo.put("email", email);

            return jo.toString();
        } catch (Exception e) {Utils.safe(e);}

        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e){}
        }
        return "";
    }


    /**
     * This function returns the full phone directory
     * @return
     * a Json array of phone / email fields
     * structure : [ { name:... , phone: ... , email: ... }, ..... ]
     */
    @JavascriptInterface
    public String list() {
        Cursor cursor = null;
        try {
            JSONArray obj = new JSONArray();
            cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String phone = getPhone(ID);
                String email = getEmail(ID);

                JSONObject jo = new JSONObject();
                jo.put("name", name.toLowerCase());
                jo.put("phone", phone);
                jo.put("email", email);
                obj.put(jo);

            }
            cursor.close();
            cursor=null;
            return obj.toString();
        } catch (Exception e) {Utils.safe(e);}

        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e){}
        }
        return "";
    }

}
