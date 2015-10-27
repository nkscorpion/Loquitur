/*
    Loquitur, Code for English interface

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

Brain.sentence('@ redial#action');
Brain.sentence('@ call#action $people');
Brain.sentence('@ dial#action $number');

// Navigation

Brain.sentence('@ navigation#action PLACE');
Brain.sentence('@ drive#action me PLACE');
Brain.sentence('@ go#action PLACE');
Brain.sentence('@ bring#action me PLACE');
Brain.sentence('PLACE ... ^to|at|on|over|under|in|towards ~ the PLACEATOM');
Brain.sentence('PLACEATOM .place');
Brain.sentence('PLACEATOM $place');
Brain.sentence('PLACE PLACEATOM');
// Hour

Brain.sentence('HOUR ~ a ^\\d+|quarter|half#minute ^after|past#mid ^\\d+#hour ~ ^pm|am|p\\.m\\.|a\\.m\\.#ampm');
Brain.sentence('HOUR ~ a ^\\d+|quarter|half#minute ^to|til#mid ^\\d+#hour ~ ^pm|am|p\\.m\\.|a\\.m\\.#ampm');
Brain.sentence('HOUR ^\\d+#hour o clock#mid ~ ^pm|am|p\\.m\\.|a\\.m\\.#ampm');

// Minute

Brain.sentence('HOUR ^\\d+|an#hourplus ^hour|hours ~ and ^\\d+#minuteplus ^minute|minutes');
Brain.sentence('HOUR ^\\d+|an#hourplus ^hour|hours');
Brain.sentence('HOUR ^\\d+|a#minuteplus ^minute|minutes');

// Days

Brain.sentence('DAY ^(\\d+)\\w*#day ~ of ~ ^january|february|march|april|may|june|july|august|september|october|november|december#month ~ ^\\d+#year');
Brain.sentence('DAY ^tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday#relday');


// Wake up

Brain.sentence('@ wake#action ... ^at|in HOUR');
Brain.sentence('@ alert#action ... ^at|in HOUR');
Brain.sentence('@ alarm#action ... ^at|in HOUR');
Brain.sentence('@ delete#action ... ^alarm|alarms#what');
Brain.sentence('@ remove#action ... ^alarm|alarms#what');
Brain.sentence('@ modify#action ... ^alarm|alarms#what');


// Voice search on Internet

Brain.sentence('@ search#action');
Brain.sentence('@ internet#action');


// Calendar

Brain.sentence('@ remember#action HOURDAY');
Brain.sentence('@ calendar#action HOURDAY');
Brain.sentence('@ mark#action HOURDAY');
Brain.sentence('HOURDAY ... DAY ~ HOUR ... ^that|to $what');
Brain.sentence('HOURDAY ... DAY ~ HOUR ^that|to $what');


// Run

Brain.sentence('@ start#action $what');
Brain.sentence('@ run#action $what');

// Save
Brain.sentence('@ save#action ... ^place|position as $what');
Brain.sentence('@ set#action ... ^place|position as $what');

// Where are we
Brain.sentence('@ where#action ... ^we|i');



// ****** Utils ******

function vectToString( vec ) {
    var res=''
    var h='';
    for (var i = 0 ; i<vec.length ; ++i) {
      res+=h+vec[i];
      h=' ';
    }
    return res;
}


var faces = { happy:'im1.png', sorry:'im4.png', smile:'im3.png',think:'im2.png'};
function setImage( e ) {
  var img = document.getElementById ("image");
  img.src = faces[e];
}


function exit() {
  Intent.finish();
}

function runAndExit() {
  Intent.run();
  Intent.finish();
}


// ******



// ****** CODE ******

document.write('<center><img id="image" src="im3.png" width="80%" height="auto"/></center>');


Talk.listen('waiting_query()');


function waiting_query() {


  var result=Talk.getSentence();
  setImage('think');
  var p=Brain.process(result);
  parsed=JSON.parse(p);


  if (parsed.goal) {

    var action=parsed.args.ACTION;

    if (['call'].indexOf(action)>-1) {
        action_call();
        return;
    }
    else if (['redial'].indexOf(action)>-1) {
        action_redial();
        return;
    }
    else if (['dial'].indexOf(action)>-1) {
        action_dial();
        return;
    }
    else if (['navigation','drive','go','bring'].indexOf(action)>-1) {
        action_navigation();
        return;
    }
   else if (['wake','alarm','alert'].indexOf(action)>-1) {
          action_wakeup();
          return;
    }
   else if (['delete','remove'].indexOf(action)>-1) {
            action_delete();
            return;
    }
   else if (['modify'].indexOf(action)>-1) {
            action_modify();
            return;
    }
   else if (['start','run'].indexOf(action)>-1) {
            action_application();
            return;
    }
   else if (['search','internet'].indexOf(action)>-1) {
            action_internet();
            return;
    }
   else if (['save','set'].indexOf(action)>-1) {
              action_location('action_saveplace_callback');
              return;
    }
   else if (['where'].indexOf(action)>-1) {
          action_location('action_where_callback');
          return;
    }
   else if (['remember','calendar','mark'].indexOf(action)>-1) {
              action_calendar();
              return;
   }
  }

  setImage('sorry');
  Talk.say('Unknown command','exit()');


}



function action_dial() {
  try {
    number=vectToString(parsed.args.NUMBER).replace('più','+').replace(/ /g,'');
    var s="";
    for(var i=0;i<number.length;++i) {
      if ( ! ( ( (number[i]=='+') && (i==0) ) || ((number[i]>='0') && (number[i]<='9')) )) throw "wrong number";
      s+=' '+number[i];
    }
    if (number=='') throw "unspecified number";
    Intent.create('android.intent.action.CALL');
    Intent.data('tel:',number)
  } catch( err ) {
    setImage('sorry');
    Talk.say(err,'exit()');
    return;
  }
  setImage('happy');
  Talk.say('I\'m dialing '+s,'runAndExit()');
}



function action_call() {
  try {
    var person=vectToString(parsed.args.PEOPLE);
    var matching=PhoneDir.match(person,0.5);
    if (matching=="") throw "not in the phone directory";
    var number=JSON.parse(matching).phone;
    if (number=="") throw "no number for this voice";
    Intent.create('android.intent.action.CALL');
    Intent.data('tel:',number)
  } catch (err)  {
    setImage('sorry');
    Talk.say(err,'exit()');
    return;
  }
  setImage('happy');
  Talk.say('I\'m calling '+person,'runAndExit()');
}

function action_redial() {
  try {
    var number= PhoneDir.getLastCalledNumber();
    if (number=="") throw "no numbers to redial";
    Intent.create('android.intent.action.CALL');
    Intent.data('tel:',number)
  } catch (err)  {
    setImage('sorry');
    Talk.say(err,'exit()');
    return;
  }
  setImage('happy');
  Talk.say('I\'m dialing the last number','runAndExit()');
}


function action_navigation() {
    try {
      var place=parsed.args.PLACE.PLACEATOM.PLACE;
      if (place.length==0) throw "undefined destination";
      if (place.key) {
        var togo=place.value;
        var descr=vectToString(place.key);
      } else {
        var togo=vectToString(place);
        var descr=togo;
      }
    } catch (err ) {
        setImage('sorry');
        Talk.say(err,'exit()');
        return;
    }
    setImage('happy');
    Intent.create('android.intent.action.VIEW');
    Intent.data("google.navigation:q=",togo);
    Talk.say('navigation to'+descr,'runAndExit()');
}



function getTime( time ) {
    var hr=0;
    var min=0;
    if (!time.HOUR) {
      var hours=time.HOUR;

      // Calculated from now
      var date=new Date();
      var sec=date.getTime();

      var h1 = time.HOURPLUS;
      if (!(h1===undefined)) {
        if (h1 == 'an' ) {
          h1=1;
        } else {
          h1=parseInt(h1);
        }
        sec+=h1*3600000;
      }

      var h2 = time.MINUTEPLUS;
        if (!(h2===undefined)) {
          if (h2 == 'a' ) {
            h2=1;
          } else {
            h2=parseInt(h2);
          }
          sec+=h2*60000;
        }


      date.setTime(sec);

      var hr=date.getHours();
      var min=date.getMinutes();


    } else {
      // Direct form

      // Convert to 24 hour format
      var hr=parseInt(time.HOUR);
      var min=0;

      if ((hr<13)&&(hr>0)) {

        if (time.AMPM) {
          if ((time.AMPM[0]=='pm')||(time.AMPM[0]=='p.m.')) hr=(hr+12)%24;
        } else {

          var date=new Date();
          var a=date.getHours();
          if (hr>a%12) hr=(hr+12)%24;

        }

      }



      if (time.MINUTE) {

        c=time.MINUTE;
        if (c=='quarter') min=15;
        else if (c=='half') min=30;
        else if (['to','til'].indexOf(time.MID[0])>-1) {
          hr=(hr+23)%24;
          min=60-min;
        }
        else min=parseInt(c);

      }


    }

    return [ hr , min] ;

}



function action_wakeup() {

    dv=getTime(parsed.args.HOUR);
    hr=dv[0];
    min=dv[1];

    setImage('happy');
    Intent.create('android.intent.action.SET_ALARM');
    Intent.addInt('android.intent.extra.alarm.HOUR',hr);
    Intent.addInt('android.intent.extra.alarm.MINUTES',min);
    Intent.addBoolean('android.intent.extra.alarm.SKIP_UI',true);
    Intent.run();

    var t='at';

    if (min>30) {
      hr=(hr+1)%24;
      min=60-min;
      t+=min+" to ";
    } else {
      if (min!=0)
        t+=min+" after ";
    }


    if (hr==0) t+="12 pm";
    else if (hr==12) t+='12 am';
    else if (hr>12) t+=(hr%12)+" pm";
    else t+=hr+" am";


    Talk.say('Alarm set ' + t,'exit()');

}

function action_delete() {
    if (['alarm','alarms'].indexOf(parsed.args.WHAT[0])>-1) {
      setImage('happy');
      Intent.create('android.intent.action.SET_ALARM');
      Intent.addBoolean('android.intent.extra.alarm.SKIP_UI',false);
      Talk.say('please remove alarms manually','runAndExit()');
    }


}


function action_modify() {
    if (['alarm','alarms'].indexOf(parsed.args.WHAT[0])>-1) {
      setImage('happy');
      Intent.create('android.intent.action.SET_ALARM');
      Intent.addBoolean('android.intent.extra.alarm.SKIP_UI',false);
      Talk.say('please modify alarms manually','runAndExit()');
    }
}

function action_internet() {
    setImage('happy');
    Intent.create('android.speech.action.WEB_SEARCH');
    Intent.run();
    Intent.finish();
}


function action_application() {
    var what=vectToString(parsed.args.WHAT);
    var curr=Intent.matchApp(what,0.5);
    if (curr == "") {
      setImage('sorry');
      Talk.say('no applications found','exit()');
    } else {
      var current=JSON.parse(curr);
      setImage('happy');
      Intent.launchFromName(current.package);
      Talk.say('running '+current.name,'runAndExit()');
    }
}


function action_location(arg) {
    Location.currentLocation(arg);
}

function action_saveplace_callback(lat,lon,alt) {
    var what=vectToString(parsed.args.WHAT);
    if (what=='') {
      Talk.say("I did'n understand the name","exit()");
      return;
     }
    if (lat>500) {
      Talk.say("I cannot find the coordinates","exit()");
      return;
    }
    Storage.setKey('place',what,''+lat+','+lon);
    Talk.say("Position "+what+" saved","exit()");
    return;
}

function action_where_callback(lat,lon,alt) {

    if (lat>500) {
      Talk.say("I cannot find the coordinates","exit()");
      return;
    }

    place=JSON.parse(Location.geoCoder(lat,lon));
    if (place=='') {
      Talk.say("I cannot detect the place","exit()");
      return;
    }
    var s="";
    if (place[0]!='') s+="we are at "+place[0];
    if (place[1]!='') s+=", in "+place[1];
    if (place[2]!='') s+=", town "+place[2];
    if (place[3]!='') s+=" in "+place[3];
    if (place[4]!='') s+=" "+place[4];

    if (alt>=500) { //Significative altitude
      s+=" . altitude "+parseInt(alt*3.281)+" feet";
      return;
    }

    Talk.say(s,"exit()");
    return;
}




var monthsVector=['january','february','march','april','may','june','july','august','september','october','november','december'];


function getDate( day ) {

  var date=new Date();

  date.setMinutes(0);
  date.setHours(0);
  date.setSeconds(0);
  date.setMilliseconds(0);


  var relday=day.RELDAY;

  if (!relday) {

      // Explicit DATE
      var dd=parseInt(day.DAY[1]) ;

      if (!day.MONTH) {
        // If you do not describe the month we intend the current month or the next
        // if the day is less or equal to today and the current year

        if (dd<=date.getDate()) {
          // Next month
          date.setMonth(date.getMonth()+1);
        }
        date.setDate(dd)
        return date;

      } else {
        var dm=monthsVector.indexOf(day.MONTH[0]);
      }

      if (!(day.YEAR)) {
         // If year is undefined we have to verify that the date is in the future
         if ( date >= new Date(date.getFullYear(),dm,dd,0,0,0,0) ) {
            // Ok date is in the past, refers to the next year
            date.setFullYear(date.getFullYear()+1);
         }
        date.setDate(dd);
        date.setMonth(dm);
        return date;

      } else {
         var dy=parseInt(day.YEAR[0]);
         if (dy<100) dy+=2000;
      }

      date.setDate(dd);
      date.setMonth(dm);
      date.setFullYear(dy);


    } else {

      relday=relday[0];
      // OK the day is relative, we need some calculation
      var days=date.getDate();
      var dayofweek=date.getDay();
      if (relday=='tomorrow') days+=1;
      else {
        d=['sunday','monday','tuesday','wednesday','thursday','friday','saturday'].indexOf(relday);
        var diff=d-dayofweek;
        if (diff<1) diff+=7;
        days+=diff;
      }
      date.setDate(days);

   }


   return date;

  }




function setCalendar(date,fullday,title) {
  // Create a new context
  Contents.create();
  Contents.addInt('calendar_id',1);
  Contents.addString('title',title);
   if (fullday) {
   // Due to an Android Calendar non-bug we need some modifications here
    var d=new Date(date);
    d.setUTCMilliseconds=0;
    d.setUTCSeconds=0;
    d.setUTCHours=0;
    d.setUTCMinutes=0;
    d.setUTCDate(date.getDate());
    d.setUTCMonth(date.getMonth());
    d.setUTCFullYear(date.getFullYear());
    Contents.addLong('dtstart',d.getTime());
    Contents.addLong('dtend',d.getTime()+1000 * 60 * 60 * 24); // Event Granularity
    Contents.addString('eventTimezone','GMT');
    Contents.addInt('allDay',1);
 } else {
    Contents.addLong('dtstart',date.getTime());
    Contents.addLong('dtend',date.getTime()+1000 * 60 * 60); // Event Granularity
    Contents.addString('eventTimezone',Talk.getDefaultTimezone());
    Contents.addInt('allDay',0);
  }

  Contents.addInt('eventStatus',1); // Confirmed
  Contents.addInt('hasAlarm',1); // Of course it has alarm
  uid=Contents.set('content://com.android.calendar/events');

  // And now put the ALARM
  Contents.create();
  Contents.addLong('event_id',uid);
  Contents.addInt('minutes',-1);
  Contents.addInt('method',1); // Alert
  Contents.set('content://com.android.calendar/reminders');

  answer='notice set for the '+date.getDate()+" of ";
  answer+=monthsVector[date.getMonth()]+" ";
  answer+=date.getFullYear();

  if (!fullday) {
      var t=' at ';
      var hr=date.getHours();
      var min=date.getMinutes();
      if (min>30) {
        hr=(hr+1)%24;
        min=60-min;
        t+=min+" to ";
      } else {
        if (min!=0)
          t+=min+" after ";
      }


      if (hr==0) t+="12 pm";
      else if (hr==12) t+='12 am';
      else if (hr>12) t+=(hr%12)+" pm";
      else t+=hr+" am";
      answer+=t;


  }

  answer+="title : "+title;

  setImage('happy');
  Talk.say(answer,'exit()');

}



function action_calendar() {
  var fullday=false;
  title=vectToString(parsed.args.HOURDAY.WHAT);
  if ( ! parsed.args.HOURDAY.DAY ) {
    var h=getTime(parsed.args.HOURDAY.HOUR);
    var date=new Date();
    date.setMinutes(h[1]);
    date.setHours(h[0]);
    date.setSeconds(0);
    date.setMilliseconds(0);
  } else {
    var date=getDate(parsed.args.HOURDAY.DAY);
    if (!parsed.args.HOURDAY.HOUR) {
     fullday=true;
    } else {
      var h=getTime(parsed.args.HOURDAY.HOUR);
      date.setMinutes(h[1]);
      date.setHours(h[0]);
    }
  }

  setCalendar(date,fullday,title);

}