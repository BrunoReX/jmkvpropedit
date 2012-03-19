/* 
 * Copyright (c) 2012 Bruno Barbieri
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package com.googlecode.jmkvpropedit;

import java.text.NumberFormat;
import java.text.DecimalFormat;

public class Utils {
		
	/* Start of OS detection functions */
	
	public static boolean isWindows() {
		String OS = System.getProperty("os.name");
		
		if (OS.toLowerCase().startsWith("windows"))
			return true;
		else
			return false;
	}
	
	public static boolean isMac() {
		String OS = System.getProperty("os.name");
		
		if (OS.toLowerCase().startsWith("mac"))
			return true;
		else
			return false;
	}
	
	public static boolean isLinux() {
		String OS = System.getProperty("os.name");
		
		if (OS.toLowerCase().startsWith("linux"))
			return true;
		else
			return false;
	}
	
	/* End of OS detection functions */
	
	
	/* Start of escaping functions */
	
	public static String escapeName (String name) {
		if (!name.isEmpty()) {
			name = name.replace("\\","\\\\");
			name = name.replace(" ", "\\s");
			name = name.replace("\"","\\2");
			name = name.replace(":","\\c");
			name = name.replace("#","\\h");
		}
		
		return name;
	}
	
	public static String escapeNameCmdLine (String name) {
		name = name.replace("\"", "\\\"");
		
		return name;
	}
	
	public static String escapeBackslashes (String text) {
		text = text.replace("\\", "\\\\");
		
		return text;
	}
	
	public static String escapePath (String path) {
		if (Utils.isWindows()) {
			path = path.replace("\\", "\\\\");
		} else {
			path = path.replace("\\", "\\\\");
			path = path.replace(" ", "\\ ");
			path = path.replace("[", "\\[");
			path = path.replace("]", "\\]");
			path = path.replace("{", "\\{");
			path = path.replace("}", "\\}");
			path = path.replace("<", "\\<");
			path = path.replace(">", "\\>");
			path = path.replace("\'", "\\\'");
			path = path.replace("\"", "\\\"");
			path = path.replace("&", "\\&");
			path = path.replace("*", "\\*");
			path = path.replace("?", "\\?");
			path = path.replace("|", "\\|");
			path = path.replace(":", "\\:");
			path = path.replace(";", "\\;");
		}
		
		return path;
	}
	
	/* End of escaping functions */
	
	
	public static NumberFormat padNumber(int pad) {
		NumberFormat formatter = new DecimalFormat("0");
		if (pad > 0) {
			String n = "";
			for (int i = 0; i < pad; i++) {
				n += 0;
			}
			formatter = new DecimalFormat(n);
		}
		return formatter;
	}
	
}
