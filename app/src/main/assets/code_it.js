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
Brain.sentence('PLACE ... ^in|al|a|alla|alle|agli|verso|sulla|su|sul|nel|nella|nell|all ~ ^il|lo|la|i|gli|le|l $place');



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
  var p=Brain.process(result);
  parsed=JSON.parse(p);

  setImage('think');


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
    var place=parsed.args.PLACE.PLACE;
    var togo=vectToString(place);
    if (togo=='') throw "destinazione non definita";
  } catch (err ) {
      setImage('sorry');
      Talk.say(err,'exit()');
      return;
  }
  setImage('happy');
  Intent.create('android.intent.action.VIEW');
  Intent.data("google.navigation:q=",togo);
  Talk.say('navigazione verso'+togo,'runAndExit()');
}

