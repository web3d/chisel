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

import java.awt.*;
import java.util.*;

// Meta Info for a Single Line

public class LineInfo {

 	public String	data;			// one line of text
	public boolean	inComment;		// true if line ends with continuing comment
	public boolean	inLiteral;		// true if line ends with continuing literal (possibly by mistake)
	public int		keyCt;			// # of keywords on line
	public short		keyStarts[];	// array of keyword starts
	public short		keyEnds[];		// array of keyword lengths
	public byte		keyTypes[];		// array of keyword types

	public LineInfo(String s)
	{
		data = s;
	}
	
	/** convert the String to a size limited char array, viewer need
	 *  not handle huge lines.
	 */
	public char[] getCharArray() {
	    if ( data.length() < 1000 ) {
	        return( data.toCharArray() );
	    } else {
	        String s = data.substring( 0, 999 );
	        return( s.toCharArray() );
	    }
	}



	/**
	 * this works for columns not char numbers, so not quite working now
	 * @return true if character at given place is not in comment nor literal
	 */

	public boolean isFreeStanding( int number )
	{
		return true;
	/*
		int i;
		for ( i =0; i < keyCt; i++)
		{
			if ( keyStarts[i] > number )
				continue;
			if ( (keyEnds[i] >= number) &&
				 ((keyTypes[i] == Hilite.QUOTE) || (keyTypes[i] == Hilite.COMMENT)) )
				return false;
		}
		return true;
	*/
	}
}

