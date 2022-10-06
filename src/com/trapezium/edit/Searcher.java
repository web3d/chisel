/*
 * Copyright (c) 1997, 1998 John Jensen. All rights reserved.
 *
 * This software is FREE FOR COMMERCIAL AND NON-COMMERCIAL USE,
 * provided the following condition is met.
 *
 * Permission to use, copy, modify, and distribute this software and
 * its documentation for any purpose and without fee is hereby granted,
 * provided that any copy or derivative of this software or documentation
 * retaining the name "John Jensen" also retains this condition and the
 * following disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * CopyrightVersion 1.0
 */

package com.trapezium.edit;

import java.util.*;
import java.io.*;
import java.awt.*;

public class Searcher {

	private static Searcher globalSearcher;
	static {
		globalSearcher = new Searcher();
		globalSearcher.addSearchPattern("");
		globalSearcher.addReplacePattern("");
	}
	
	
	public static Searcher getSearcher() {
		return globalSearcher;
	}

	
	public Searcher() {
	}

	// remember search and replace

	private Vector searchPatterns = new Vector();

	public void addSearchPattern(String patt)
	{
		int i = searchPatterns.indexOf(patt);

		// do not do anything if it patt is last element
		if ( i != searchPatterns.size() -1 )
		{
			// if already exists delete
			if ( i >= 0 ) 
				searchPatterns.removeElementAt(i);
			
			// add at end of a list
			searchPatterns.addElement(patt);
		}
	}

	public String[] getSearchPatterns()
	{
		String[] newArray = new String[searchPatterns.size()];
		searchPatterns.copyInto(newArray);
		return newArray;
	}

	public String getLatestSearchPattern()
	{
		return (String)searchPatterns.lastElement();
	}

	private Vector /*String*/ replacePatterns = new Vector();

	public void addReplacePattern(String patt)
	{
		int i = replacePatterns.indexOf(patt);

		// do not do anything if it patt is last element
		if ( i != replacePatterns.size() -1 )
		{
			// if already exists delete
			if ( i >= 0 ) 
				replacePatterns.removeElementAt(i);
			
			// add at end of a list
			replacePatterns.addElement(patt);
		}
	}

	public String[] getReplacePatterns()
	{
		String[] newArray = new String[replacePatterns.size()];
		replacePatterns.copyInto(newArray);
		return newArray;
	}

	public String getLatestReplacePattern()
	{
		return (String)replacePatterns.lastElement();
	}
}

