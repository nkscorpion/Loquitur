/*
    Loquitur, Code for Italian interface

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

// Call

Brain.sentence('@ richiama#action');
Brain.sentence('@ chiama#action $people');
Brain.sentence('@ componi#action $number');

// Navigation

Brain.sentence('@ navigazione#action PLACE');
Brain.sentence('@ guidami#action PLACE');
Brain.sentence('@ vai#action PLACE');
Brain.sentence('@ portami#action PLACE');
Brain.sentence('PLACE ... ^in|al|a|alla|alle|agli|verso|sulla|su|sul|nel|nella|nell|all ~ ^il|lo|la|i|gli|le|l PLACEATOM');
Brain.sentence('PLACEATOM .place');
Brain.sentence('PLACEATOM $place');

// Hour

Brain.sentence('HOUR ^alla|alle ~ ore ^(\\d+)\\:(\\d+)#hourmin');
Brain.sentence('HOUR ^tra|fra ^\\d+|un#hourplus ^ora|ore ~ e ^\\d+|un#minuteplus ^minuti|minuto');
Brain.sentence('HOUR ^tra|fra ^\\d+|un#hourplus ^ora|ore');
Brain.sentence('HOUR ^tra|fra ^\\d+|un#minuteplus ^minuti|minuto');

// Days

Brain.sentence('DAY ^\\d+#day ~ ^gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre#month ~ ^\\d+#year');
Brain.sentence('DAY ^domani|dopodomani|lunedì|martedi|mercoledì|giovedì|venerdì|sabato|domenica#relday');

// Wake up

Brain.sentence('@ sveglia#action HOUR');
Brain.sentence('@ svegliami#action HOUR');
Brain.sentence('@ avvisami#action HOUR');
Brain.sentence('@ cancella#action ... ^allarme|allarmi|sveglia|sveglie#what');
Brain.sentence('@ rimuovi#action ... ^allarme|allarmi|sveglia|sveglie#what');
Brain.sentence('@ modifica#action ... ^allarme|allarmi|sveglia|sveglie#what');

// Calendar

Brain.sentence('@ ricordami#action HOURDAY');
Brain.sentence('@ ricorda#action HOURDAY');
Brain.sentence('@ promemoria#action HOURDAY');
Brain.sentence('HOURDAY ... DAY ~ HOUR ... ^che|di $what');
Brain.sentence('HOURDAY ... DAY ~ HOUR ^che|di $what');


// Voice search on Internet

Brain.sentence('@ ricerca#action');
Brain.sentence('@ cerca#action');
Brain.sentence('@ internet#action');


// Run

Brain.sentence('@ avvia#action $what');
Brain.sentence('@ lancia#action $what');
Brain.sentence('@ apri#action $what');

// Save
Brain.sentence('@ salva#action ... ^posto|posizione come $what');

// Where are we
Brain.sentence('@ dove#action ^siamo|sono');


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

    if (['chiama'].indexOf(action)>-1) {
        action_call();
        return;
    }
    else if (['richiama'].indexOf(action)>-1) {
        action_redial();
        return;
    }
    else if (['componi'].indexOf(action)>-1) {
        action_dial();
        return;
    }
    else if (['navigazione','guidami','vai','portami'].indexOf(action)>-1) {
        action_navigation();
        return;
    }
    else if (['sveglia','svegliami','avvisami'].indexOf(action)>-1) {
        action_wakeup();
        return;
    }
   else if (['cancella','rimuovi'].indexOf(action)>-1) {
              action_delete();
              return;
      }
   else if (['modifica'].indexOf(action)>-1) {
            action_modify();
            return;
    }
   else if (['avvia','lancia','apri'].indexOf(action)>-1) {
           action_application();
           return;
   }
   else if (['cerca','ricerca','internet'].indexOf(action)>-1) {
           action_internet();
           return;
   }
   else if (['salva'].indexOf(action)>-1) {
           action_location('action_saveplace_callback');
           return;
   }
   else if (['dove'].indexOf(action)>-1) {
           action_location('action_where_callback');
           return;
   }
   else if (['ricordami','ricorda','promemoria'].indexOf(action)>-1) {
           action_calendar();
           return;
   }
  }

  setImage('sorry');
  Talk.say('Comando sconosciuto','exit()');

}


function action_dial() {
  try {
    number=vectToString(parsed.args.NUMBER).replace('più','+').replace(/ /g,'');
    var s="";
    for(var i=0;i<number.length;++i) {
      if ( ! ( ( (number[i]=='+') && (i==0) ) || ((number[i]>='0') && (number[i]<='9')) )) throw "numero errato";
      s+=' '+number[i];
    }
    if (number=='') throw "numero non specificato";
    Intent.create('android.intent.action.CALL');
    Intent.data('tel:',number)
  } catch( err ) {
    setImage('sorry');
    Talk.say(err,'exit()');
    return;
  }
  setImage('happy');
  Talk.say('chiamo '+s,'runAndExit()');
}



function action_call() {
  try {
    var person=vectToString(parsed.args.PEOPLE);
    var matching=PhoneDir.match(person,0.5);
    if (matching=="") throw "non in rubrica";
    var number=JSON.parse(matching).phone;
    if (number=="") throw "numero non associato";
    Intent.create('android.intent.action.CALL');
    Intent.data('tel:',number)
  } catch (err)  {
    setImage('sorry');
    Talk.say(err,'exit()');
    return;
  }
  setImage('happy');
  Talk.say('chiamo '+person,'runAndExit()');
}

function action_redial() {
  try {
    var number= PhoneDir.getLastCalledNumber();
    if (number=="") throw "nessun numero da richiamare";
    Intent.create('android.intent.action.CALL');
    Intent.data('tel:',number)
  } catch (err)  {
    setImage('sorry');
    Talk.say(err,'exit()');
    return;
  }
  setImage('happy');
  Talk.say('richiamo ultimo numero','runAndExit()');
}

function action_navigation() {
  try {
    var place=parsed.args.PLACE.PLACEATOM.PLACE;
    if (place.length==0) throw "destinazione non definita";
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
  Talk.say('navigazione verso'+descr,'runAndExit()');
}



function getTime( time ) {
    var hours=time.HOURMIN;
    if (hours===undefined) {
    // Calculate form
      var date=new Date();
      var sec=date.getTime();

      var h1 = time.HOURPLUS;
      if (!(h1===undefined)) {
        if (h1 == 'un' ) {
          h1=1;
        } else {
          h1=parseInt(h1);
        }
        sec+=h1*3600000;
      }

      var h2 = time.MINUTEPLUS;
        if (!(h2===undefined)) {
          if (h2 == 'un' ) {
            h2=1;
          } else {
            h2=parseInt(h2);
          }
          sec+=h2*60000;
        }


      date.setTime(sec);

      hr=date.getHours();
      min=date.getMinutes();


    } else {
      // Direct form
      hr=parseInt(hours[1]);
      min=parseInt(hours[2]);
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

    Talk.say('sveglia impostata per le ore '+hr+' e '+min,'exit()');

}



function action_delete() {
    if (['allarme','allarmi','sveglia','sveglie'].indexOf(parsed.args.WHAT[0])>-1) {
      setImage('happy');
      Intent.create('android.intent.action.SET_ALARM');
      Intent.addBoolean('android.intent.extra.alarm.SKIP_UI',false);
      Talk.say('rimuovere sveglie manualmente','runAndExit()');
    }

}


function action_modify() {
    if (['allarme','allarmi','sveglia','sveglie'].indexOf(parsed.args.WHAT[0])>-1) {
      setImage('happy');
      Intent.create('android.intent.action.SET_ALARM');
      Intent.addBoolean('android.intent.extra.alarm.SKIP_UI',false);
      Talk.say('modificare sveglie manualmente','runAndExit()');
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
      Talk.say('nessuna applicazione trovata','exit()');
    } else {
      var current=JSON.parse(curr);
      setImage('happy');
      Intent.launchFromName(current.package);
      Talk.say('avvio '+current.name,'runAndExit()');
    }
}


function action_location(arg) {
    Location.currentLocation(arg);
}

function action_saveplace_callback(lat,lon,alt) {
    var what=vectToString(parsed.args.WHAT);
    if (what=='') {
      Talk.say("Non ho capito il nome","exit()");
      return;
     }
    if (lat>500) {
      Talk.say("Non riesco a trovare le coordinate","exit()");
      return;
    }
    Storage.setKey('place',what,''+lat+','+lon);
    Talk.say("Posizione "+what+" salvata","exit()");
    return;
}

function action_where_callback(lat,lon,alt) {

    if (lat>500) {
      Talk.say("Non riesco a trovare le coordinate","exit()");
      return;
    }
    place=JSON.parse(Location.geoCoder(lat,lon));
    if (place=='') {
      Talk.say("Non riesco a cercare il posto","exit()");
      return;
    }
    var s="";
    if (place[0]!='') s+="siamo al "+place[0];
    if (place[1]!='') s+=", in "+place[1];
    if (place[2]!='') s+=", città "+place[2];
    if (place[3]!='') s+=" in "+place[3];
    if (place[4]!='') s+=" "+place[4];

    if (alt>=500) { //Significative altitude
      s+=" . Altitudine "+parseInt(alt)+" metri";
      return;
    }

    Talk.say(s,"exit()");
    return;
}




var monthsVector=['gennaio','febbraio','marzo','aprile','maggio','giugno','luglio','agosto','settembre','ottobre','novembre','dicembre'];


function getDate( day ) {

  var date=new Date();

  date.setMinutes(0);
  date.setHours(0);
  date.setSeconds(0);
  date.setMilliseconds(0);


  var relday=day.RELDAY;

  if (!relday) {


      // Explicit DATE
      var dd=parseInt(day.DAY[0]) ;

      if (day.MONTH === undefined) {
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


      if (day.YEAR === undefined) {
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
      if (relday=='domani') days+=1;
      else if (relday=='dopodomani') days+=2;
      else {
        d=['domenica','lunedì','martedì','mercoledì','giovedì','venerdì','sabato'].indexOf(relday);
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

  answer='Avviso impostato per il '+date.getDate()+" ";
  answer+=monthsVector[date.getMonth()]+" ";
  answer+=date.getFullYear();

  if (!fullday) {
      answer+=' alle '+date.getHours()+" E "+date.getMinutes();
  }

  answer+="titolo : "+title;

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
