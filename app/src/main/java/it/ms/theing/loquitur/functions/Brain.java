/*
    Loquitur, Brain module

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

import android.util.Pair;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.ms.theing.loquitur.Loquitur;
import it.ms.theing.loquitur.Utils;

/**
 * Brain is the backtracking parser working as syntactical engine.
 * It creates a logical tree of json object representing
 * a logical analysis of the phrase.
 *
 * To use Brain you need to put a sequence of sentences description that are placed
 * into a dynamic structure.
 *
 * The syntax is described in the related documentation.
 */

public class Brain implements LoquiturModules {

	private Loquitur activity;
	private String separators;

	// we need the storage to get the aliases
	private final Storage storage;
	// each node into the database is connected to a seqence of phrases
	private final HashMap<String,Vector<Vector<Member>>> dataBase;
	private Vector<Vector<Member>> root;

	// token sequence storage
	private String[] tokens;
	private int index;

	public Brain(Loquitur activity) {
		this.activity=activity;
		this.storage= (Storage) activity.getModule("Storage");
		this.dataBase=new HashMap<String,Vector<Vector<Member>>>();
		separators=" |\\'";
	}


	@Override
	public String getJavascriptName() {
		return "Brain";
	}

	@Override
	public void endModule() {

	}

	// Results section, all the results are placed in a stack

	public abstract class Result {
		public String key;
		public abstract void putJSON(JSONObject jo);
	}

	
	public class SingleResult extends Result {
		public String value;

		@Override
		public void putJSON(JSONObject jo) {
			try {
				jo.put(key, value);
			} catch (JSONException e) {
			}
		}
	}
	
	
	
	public class MultipleResult extends Result {
		public Vector<String> value;

		@Override
		public void putJSON(JSONObject jo) {
			JSONArray ja=new JSONArray();
			for (String s:value) {
				ja.put(s);
			}
			try {
				jo.put(key,ja);
			} catch (JSONException e) {
			}
		}
	}
		
	public class SubResult extends Result {
		public Stack<Result> value;

		@Override
		public void putJSON(JSONObject jo) {
			JSONObject ja=new JSONObject();
			for(Result res:value) {
				res.putJSON(ja);
			}
			try {
				jo.put(key,ja);
			} catch (JSONException e) {
			}
		}
	}
	

	// A member is something that matches. If the match is positive it add the results
	
	
	private interface Member {
		boolean match(Vector<Member> content, int sequence, Stack<Result> results);
	}



	/*
	 * The alias class, load the data from the DB, fills an internal cache
	  * and tries to match the key
	 */

	public class AliasKey implements Member {

		private String name;
		private String alias;

		private Vector<Pair<String[],String>> cache;

		public AliasKey(String name) {
			this.name=name.toUpperCase();
			cache=null;
		}


		protected boolean matching() {
			alias="";
			for (Pair<String[],String> item : cache) {
				boolean b=true;
				for (int i=0;i<item.first.length;++i) {
					if (i + index >= tokens.length) {
						b=false;
						break;
					}
					if (!tokens[index+i].equals(item.first[i])) {
						b=false;
						break;
					}
				}
				if (b) {
					index += item.first.length;
					alias=item.second;
					return true;
				}

			}
			return false;
		}


		private void result(Stack<Result> results) {
			SingleResult sr=new SingleResult();
			sr.key=name;
			sr.value=alias;
			results.push(sr);
		}

		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {


			if (cache==null) {
				cache=storage.loadCache(name);
			}

			// Check if the sequence is solved
			int id=index;

			if (matching()) {
				// Try to match the next
				// Check if sequence is finished
				if (++sequence>=content.size()) {
					result(results);
					return true;
				};
				if (index<tokens.length)
				{
					if (content.get(sequence).match(content,sequence,results)) {
						result(results);
						return true;
					}
				}
			}

			// Sorry this definitely does not match
			index=id;
			return false;

		}



	}

	// A final key is a simple string to compare

	public class FinalKey implements Member {
		
		private String name;
		private String key;
		
		public FinalKey(String token) {
			key=null;
			String[] v=token.split("#");			
			name=v[0];
			if (v.length>1) key=v[1].toUpperCase();
		}
		

		protected boolean matching() {
			if (!tokens[index].equals(name)) return false;
			++index;
			return true;
		}


		private void result(String s,Stack<Result> results) {
			if (key==null) return;
			SingleResult sr=new SingleResult();
			sr.key=key;
			sr.value=s;
			results.push(sr);			
		}
		
		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {

			// Check if the sequence is solved			
			int id=index;			
									
			if (matching()) {
				// Try to match the next
				// Check if sequence is finished
				if (++sequence>=content.size()) {
					result(tokens[id],results);
					return true;
				};
				if (index<tokens.length) 
				{
					if (content.get(sequence).match(content,sequence,results)) {
						result(tokens[id],results);
						return true;		
					}
				}
			}			
			
			// Sorry this definitely does not match
			index=id;
			return false;
			
		}
		
	
	}


	// This is the regular expressions match class

	private class PregKey implements Member {
		Pattern name;
		Matcher m;
		String key;
		
		public PregKey(String token) {
			key=null;
			String[] v=token.split("#");			
			name=Pattern.compile(v[0]);
			if (v.length>1) key=v[1].toUpperCase();
		}
	

	
		protected boolean matching() {			
			m=name.matcher(tokens[index]);
			if (!m.matches()) return false;
			++index;
			return true;
			
		}

		private void result(Stack<Result> results) {
			if (key==null) return;
			MultipleResult sr=new MultipleResult();
			sr.key=key;
			sr.value=new Vector<String>();
			int j=m.groupCount();
			for(int i=0;i<=j;++i) sr.value.add(m.group(i));
			results.push(sr);			
		}


		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {
			// Check if the sequence is solved			
			int id=index;			
									
			if (matching()) {
				// Try to match the next
				// Check if sequence is finished
				if (++sequence>=content.size()) {
					result(results);
					return true;
				};
				if (index<tokens.length) 
				{
					if (content.get(sequence).match(content,sequence,results)) {
						result(results);
						return true;		
					}
				}
			}			
			
			// Sorry this definitely does not match
			index=id;
			return false;
		}
		
	}


	// Garbage discards 0 or more objects until the next match
	
	
	private class Garbage implements Member {

		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {
			// Check if the sequence is solved			
			int id=index;			
			
			
			// Check if sequence is finished
			if (++sequence>=content.size()) {				
				return true;
			};
			
			// If not finished check if it does NOT match

			while(index<tokens.length) {
				if (content.get(sequence).match(content,sequence,results)) {
					// Hey this match ... you can write here your data handler
					return true;		
				}
				// The garbage algorithm increments the index until the matching
				++index;
			}
			
			// Sorry this definitely does not match
			index=id;
			return false;
		}
		
	}
	
	
	// Almost made the next key matching optional.

	private class AlmostKey implements Member {

		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {
			// Check if the sequence is solved			
			int id=index;			
			
			// Check if the sequence is finished
			if (++sequence>=content.size()) {				
				return true;
			};
			
			if (content.get(sequence).match(content,sequence,results)) {
				// Hey this match ... you can write here your data handler
				return true;		
			}

			// Is not good ... restore and ignore 
			index=id;

			if (++sequence>=content.size()) {
				return true;
			};
			
			
			if (content.get(sequence).match(content,sequence,results)) {
				// Hey this match ... you can write here your data handler
				return true;		
			}
								
			// Sorry this definitely does not match
			index=id;
			return false;
		}

			
	}
	
	
	// This match the rest of the phrase whatever it is.
	// It is used to describe something of umpredictable, like a place
	
	public class RestKey implements Member {

		private String key;
		
		public RestKey(String name) {
			key=name.toUpperCase();
		}
		
		
		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {
		
			
			MultipleResult mr=new MultipleResult();
			mr.key=key;
			mr.value=new Vector<String>();
			while(index<tokens.length) {
				mr.value.add(tokens[index]);
				++index;
			}			
			results.push(mr);
			// Hey we don't have to match any more ...
			// Put here your data handler

			return true;
		}
		
			
	}
	
	
	// The rootkey, represents a branch of the logic three.
	// It sound similar to the rule concept in the Prolog language.
	
	public class RootKey implements Member {

		Vector<Vector<Member>> reference;
		String key;
		Stack<Result> res;
				
		public RootKey(String key) {
			this.key=key;
			// Already exists
			if (dataBase.containsKey(key)) {
				reference=dataBase.get(key);
				return;
			}
			// Else it is created with an empty vector
			reference=new Vector<Vector<Member>>();
			dataBase.put(key,reference );			
		}


		private void result(Stack<Result> results) {
			SubResult sr=new SubResult();
			sr.key=key;
			sr.value=res;
			results.push(sr);
		}
		
		
		@Override
		public boolean match(Vector<Member> content, int sequence,Stack<Result> results) {
			
			// Check if the sequence is solved			
			int id=index;
			
			for (Vector<Member> phrase : reference) {
				index=id;
				// Save the current stack
				res=new Stack<>();
				if ((phrase.get(0).match(phrase, 0,res))) {
					if (++sequence>=content.size()) {
						result(results);
						return true;					
					}
					if (index>=tokens.length) continue;
					if ( content.get(sequence).match(content, sequence,results) ) {
						result(results);						
						return true;
					}
				}
			}
			
			// Sorry this definitely does not match
			index=id;
			return false;
		}

		
		
	}


	/**
	 * This function clears the database, allowing you to replace the currentset of rules.
	 * with a new one. Can be used in progressive interactions.
	 */
		
	
	@JavascriptInterface
	public void reContext() {
		try {
			dataBase.clear();
		} catch (Exception e){Utils.safe(e);}
	}


	/**
	 * This put a new sentence int the database
	 * @param sentence
	 * The new sentence in meta language.
	 */

	@JavascriptInterface
	public void sentence(String sentence) {
		try {
			String[] tokens = sentence.split(" |\'");
			String name = null;
			Vector<Member> content = new Vector<Member>();
			if (tokens.length == 0) return;
			for (int i = 0; i < tokens.length; ++i) {
				if (i == 0) {
					name = tokens[0];
					continue;
				}
				if ((tokens[i].charAt(0) <= 'Z') && (tokens[i].charAt(0) >= 'A')) {
					RootKey rk = new RootKey(tokens[i]);
					content.add(rk);
				} else if (tokens[i].charAt(0) == '$') {
					RestKey rk = new RestKey(tokens[i].substring(1));
					content.add(rk);
				} else if (tokens[i].equals("~")) {
					AlmostKey ak = new AlmostKey();
					content.add(ak);
				} else if (tokens[i].equals("...")) {
					Garbage ga = new Garbage();
					content.add(ga);
				} else if (tokens[i].charAt(0) == '.') {
					AliasKey rk = new AliasKey(tokens[i].substring(1));
					content.add(rk);
				} else if (tokens[i].charAt(0) == '^') {
					PregKey pk = new PregKey(tokens[i].substring(1));
					content.add(pk);
				} else {
					FinalKey fk = new FinalKey(tokens[i]);
					content.add(fk);
				}
			}

			Vector<Vector<Member>> reference;

			if (dataBase.containsKey(name)) {
				reference = dataBase.get(name);
			} else {
				reference = new Vector<Vector<Member>>();
				dataBase.put(name, reference);
			}
			reference.add(content);
		} catch (Exception e){Utils.safe(e);}
	}

	/**
	 * Process a phrase
	 * @param phrase
	 * the phrase usually understood by the recognizer
	 * @return
	 * The resulting logical analysis
	 */

	@JavascriptInterface
	public String process(String phrase) {
		try {
			root = dataBase.get("@");
			Stack<Result> results = new Stack<Result>();
			tokens = phrase.toLowerCase().split(separators);


			boolean a = match(root, tokens, results);
			JSONObject shell = new JSONObject();
			try {
				shell.put("goal", a);

				if (a) {
					JSONObject json = new JSONObject();
					for (Result res : results) {
						res.putJSON(json);
					}

					shell.put("args", json);
				}
			} catch (JSONException e) {
			}
			return shell.toString();
		} catch (Exception e){Utils.safe(e);}
		return "[ ]";
	}





	private boolean match(Vector<Vector<Member>> r,String[] tokens,Stack<Result> results) {
		
		for (Vector<Member> phrase : root) {
			index=0;
			results.clear();
			if ((phrase.get(0).match(phrase, 0, results))) return true;
		}
		
		return false;
	}

	/**
	 * The separator is the regular expression key used to split the tokens.
	 * Usually it represents a single space, but we add the ' sign, so that
	 * for exaple "don't" will become ["don","t"] the default regular expression
	 * is " |\'". This can be used to change the separators, for foreign languages
	 * @param separators
	 * The new separator.
	 */

	@JavascriptInterface
	public void setSeparators(String separators) {
		// Cannot generate errors
		this.separators=separators;
	}
	
	
	
}
