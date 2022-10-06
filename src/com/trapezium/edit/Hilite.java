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

public class Hilite
{
	Lines	lines;			// data for all lines
	int		tabSize;		// needed to expand lines
	int		highestEver;	// highest line scanned
	boolean inComment;		// used in syntax scan
	boolean inLiteral;		// used in syntax scan
	boolean initialComment;	// used in syntax scan - initial comment state
	boolean initialLiteral;	// used in syntax scan - initial literal state
	boolean inactive;		// disable everything.
	boolean raise;			// raise case before comparing keywords

	char	keys[][];		// array of keyword strings
	char	keys2[][];		// array of keyword strings

	int		keyCt;							// temp buffers
	int		keyStarts[] = new int[100];
	int		keyEnds[] = new int[100];
	byte	keyTypes[] = new byte[100];

	public static final byte PLAIN = 0;
	public static final byte KEYWORD = 1;
	public static final byte COMMENT = 2;
	public static final byte QUOTE = 3;
	public static final byte KEYWORD2 = 4;

	public Hilite(Lines l, int t, boolean a)
	{
		lines = l;
		inactive = a;
		highestEver = -1;
		raise = false;
		initialComment = false;

		if (t > 0)
			tabSize = t;
		else
			tabSize = 4;	// saftey
	}

	public void scan(int highest)
	{
		if (inactive)
			return;

		highestEver = -1;
		inComment = initialComment;
		inLiteral = initialLiteral;

		for (int i=0; i<=highest; i++)
		{
			scanLine(i);
		}

		highestEver = highest;
	}

	public void extendScan(int highest)
	{
		if ((highest <= highestEver) || inactive)
			return;

		if (highestEver >= 0)
		{
			LineInfo hi;
			hi = lines.getLineInfo(highestEver);
			inComment = hi.inComment;
			inLiteral = hi.inLiteral;
		}
		else
		{
			inComment = initialComment;
			inLiteral = initialLiteral;
		}

		for (int i=highestEver+1; i<=highest; i++)
		{
			scanLine(i);
		}

		highestEver = highest;
	}

	public int update(int first, int last, int highest)
	{
		int i;
		boolean oldComment = false;
		boolean oldLiteral = false;
		LineInfo hi;

		if (inactive)
			return last;

		if (first > 0)
		{
			hi = lines.getLineInfo(first-1);
			inComment = hi.inComment;
			inLiteral = hi.inLiteral;
		}
		else
		{
			inComment = initialComment;
			inLiteral = initialLiteral;
		}

		i = lines.size() - 1;

		if (last > i)
			last = i;

		for (i=first; i<=last; i++)
		{
			scanLine(i);
		}

		if (highest < highestEver)
			highestEver = highest;

		while (i <= highestEver)
		{
			hi = lines.getLineInfo(i);
			oldComment = hi.inComment;
			oldLiteral = hi.inLiteral;
			scanLine(i++);
			if ((oldComment == inComment) && (oldLiteral == inLiteral))
			    break;
		}

		return i-2;		// "i" is two past the last changed line
	}

	protected void scanLine(int i)
	{
	}

	public LineInfo createLineInfo( int i )
	{
	    return( null );
	}

	public void lineRemoved(int i)
	{
		if (i <= highestEver)
			highestEver--;
	}

	protected int matchOneKey(int key, int start, int end, char keys[][])
	{
		int i,j,max;
		char c,d;

		char theKey[] = keys[key];

		i = 0;
		j = start;
		max = theKey.length;

		while ((i < max) && (j < end))
		{
			c = theKey[i++];
			d = buffer[j++];

			if (raise && (d >= 'a') && (d <= 'z'))
				d = (char)(d + 'A' - 'a');

			if ( c > d )
				return -1;

			if ( c < d )
				return 1;
		}

		if (i < max)
			return -1;

		if (j < end)
			return 1;

		return 0;
	}

	protected boolean matchKeys(int start, int end)
	{
	    return( matchKeys( start, end, keys ));
	}

	protected boolean matchKeys2(int start, int end)
	{
	    return( matchKeys( start, end, keys2 ));
	}

	boolean matchKeys(int start, int end, char keys[][])
	{
		int i,j,max,diff,extra;

		i = j = 0;
		diff = max = keys.length;

		// binary search

		while (0 != (diff >>= 1))
		{
			if (j < 0)
				i -= diff;
			else
				i += diff;

			j = matchOneKey(i,start,end,keys);

			if (j == 0)
				return true;
		}

		extra = 4;

		while (extra-- > 0)
		{
			if (j < 0)
				i--;
			else
				i++;

			if ((i < 0) || (i >= max))
				return false;

			j = matchOneKey(i,start,end,keys);

			if (j == 0)
				return true;
		}

		return false;
	}

	char buffer[];

	protected int fillBuffer(int line_no)
	{
		char c;
		int i,j,tabs,max;
		char before[];

		before = lines.getLine(line_no).toCharArray();

		max = before.length;
		for (j=i=0; i<max; i++)
			if (before[i] == '\t')
				j++;

		if (j == 0)
		{
			buffer = before;
			j = max;
		}
		else
		{
			buffer = new char[max + (j * (tabSize-1))];

			for (j=i=0; i<max; i++)
			{
				c = before[i];
				if (c == '\t')
				{
					tabs = tabSize - (j % tabSize);
					while (tabs-- > 0) buffer[j++] = ' ';
				}
				else
				{
					buffer[j++] = c;
				}
			}
		}

		return j;
	}

}
