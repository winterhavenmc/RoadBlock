package com.winterhaven_mc.roadblock;

//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

/**
 * Fast String Utilities.
 *
 * These string utilities provide both convenience methods and performance
 * improvements over most standard library versions. The main aim of the
 * optimizations is to avoid object creation unless absolutely required.
 *
 * @author Greg Wilkins (gregw)
 */
public class StringUtil {
	
	/*
	 * Private class constructor to prevent instantiation
	 */
	private StringUtil() {
		throw new AssertionError();
	}

	/**
	 * replace substrings within string.
	 */
	public static String replace(final String s, final String sub, final String with) {
		int c = 0;
		int i = s.indexOf(sub, c);
		if (i == -1)
			return s;

		StringBuffer buf = new StringBuffer(s.length() + with.length());

		synchronized (buf) {
			do {
				buf.append(s.substring(c, i));
				buf.append(with);
				c = i + sub.length();
			} while ((i = s.indexOf(sub, c)) != -1);

			if (c < s.length())
				buf.append(s.substring(c, s.length()));

			return buf.toString();
		}
	}

}
