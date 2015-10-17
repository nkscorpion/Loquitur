/*
    Loquitur, Module interface

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


/**
 * LoquiturModules, is the module interface.
 * All the loquitur modules, needs to implement this interface.
 * Note : all the functions used from javascript, must be
 * annotated as @JavascriptInterface.
 */
public interface LoquiturModules {

    /**
     * Abstract function returning the name of the
     * related javascript object.
     * @return
     */
    String getJavascriptName();

    /**
     * Close module
     */
    void endModule();

}
