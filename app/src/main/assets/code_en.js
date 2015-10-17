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

// This is a demo sentence
Brain.sentence('@ who#verb are you')


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

function setImage( e ) {
  var img = document.getElementById ("image");
  img.src = e;
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

  setImage('im2.png');


  if (parsed.goal) {

    verb=parsed.args.VERB;

    if (['who'].indexOf(verb)>-1) {
        action_who();
        return;
    }


  }

  setImage('im4.png');
  Talk.say('Unknown command','exit()');


}



function action_who() {
    setImage('im1.png');
    Talk.say('I am Loquitur, your speech command interface','exit()');
}

