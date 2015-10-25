/*
    Loquitur, Location Module

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

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.ms.theing.loquitur.Loquitur;

/**
 * This module retrieves the user current location.
 */


public class LocationInterface implements LoquiturModules {

    private LocationManager locationManager;
    private Loquitur context;
    private Handler timeout=new Handler();
    private String gpsCallback="";
    private boolean done=false;
    private final long TIMEOUT=60000;
    private final long TIMEVAL=60000;



    private void sendLocation(Location location) {
        String conv = String.format(Locale.ENGLISH,"(%3.8f,%3.8f,%3.8f)",
                location.getLatitude(),location.getLongitude(),location.getAltitude());
        context.executeCallback(gpsCallback + conv);
    }

    private final LocationListener ll= new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            timeout.removeCallbacks(tout);
            sendLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            timeout.removeCallbacks(tout);
            tout.run();
        }
    };


    private Runnable tout= new Runnable() {
        @Override
        public void run() {
            locationManager.removeUpdates(ll);
            context.executeCallback(gpsCallback + "(1000.00,1000.00,1000.00)");
        }
    };


    public LocationInterface(Loquitur activity) {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        context=activity;
    }



    private Location getLocation() {
        done=true;
        long timeInMillis= Calendar.getInstance().getTimeInMillis();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String fine = locationManager.getBestProvider(criteria, true);
        if (fine==null) return null;
        Location location=locationManager.getLastKnownLocation(fine);
        if (location!=null) {
            if (timeInMillis-location.getTime()<TIMEVAL) {
                return location;
            }
        }
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String coarse = locationManager.getBestProvider(criteria, true);
        if (!(fine.equals(coarse))) {
            location=locationManager.getLastKnownLocation(fine);
            if (location!=null) {
                if (timeInMillis-location.getTime()<TIMEVAL) {
                    return location;
                }
            }
        }
        timeout.postDelayed(tout, TIMEOUT); // At most 1 minute
        //locationManager.requestSingleUpdate(fine, ll, Looper.myLooper());
        locationManager.requestSingleUpdate(fine, ll, null);
        done=false;
        return null;
    }




    /**
     * Get the current location
     * @param gpsCallback
     * after the location has been taken calling back the location coordinates
     * example : Location.currentLocation('callThis');
     * after grabbed the location it calls "callThis(50.22213,0.523713,300)"
     * if something goes wrong, it calls : "callThis(999,999,999)"
     *
     */

    @JavascriptInterface
    public void currentLocation(String callback) {
        Location loc=getLocation();
        gpsCallback=callback;
        if (loc==null) {
            if (done) {
                tout.run();
                return;
            }
        } else {
            sendLocation(loc);
        }
    }

    /**
     * Get a string with the location.
     * @param lat
     * latitude
     * @param lon
     * longitude
     * @return
     * location or empty string
     */
    @JavascriptInterface
    public String geoCoder(float lat,float lon) {
        try{
            Geocoder geo = new Geocoder(context);
            if (!geo.isPresent()) return "";
            List<Address> addresses = geo.getFromLocation(lat, lon, 1);
            if (addresses==null) return "";
            if (addresses.size() > 0) {
                JSONArray ja=new JSONArray();
                if (addresses.get(0).getFeatureName()!=null) ja.put(addresses.get(0).getFeatureName());
                else ja.put("");
                if (addresses.get(0).getAddressLine(0)!=null) ja.put(addresses.get(0).getAddressLine(0));
                else ja.put("");
                if (addresses.get(0).getLocality()!=null) ja.put(addresses.get(0).getLocality());
                else ja.put("");
                if (addresses.get(0).getAdminArea()!=null) ja.put(addresses.get(0).getAdminArea());
                else ja.put("");
                if (addresses.get(0).getCountryName()!=null) ja.put(addresses.get(0).getCountryName());
                else ja.put("");
                return ja.toString();
            }
        }
        catch (Exception e) {
        }
        return "";
    }


    @Override
    public String getJavascriptName() {
        return "Location";
    }

    @Override
    public void endModule() {
        locationManager.removeUpdates(ll);
        timeout.removeCallbacks(tout);
    }
}
