/*  ChiselTextComponent.java
 *
 *  TextComponent is a lightweight text display control.
 *
 *  Modifications and additions are copyright (c) 1998 by Trapezium
 *  Development LLC.  All rights reserved.
 *
 *  Parts of the code in this component is derived from John Jensen's
 *  MPEDIT code.  The original MPEDIT copyright follows.
 */
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

package com.trapezium.chisel.gui;

import com.trapezium.chisel.AbstractAction;
import com.trapezium.chisel.ChiselResources;
import com.trapezium.chisel.ChiselKeys;
import com.trapezium.chisel.ChiselClipboard;
import com.trapezium.chisel.ChiselSet;
import com.trapezium.edit.Position;
import com.trapezium.edit.Searcher;
import com.trapezium.edit.LineInfo;
import com.trapezium.edit.Hilite;
import com.trapezium.vrml.NodeSelection;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

/*
 * Every private keyword was changed to protected and class was made final
 * It could be changed when anonymous class bug in javac will be corrected
 * TO do that just replace all 'protected' with 'private'
 */

public class ChiselTextComponent extends Component
	implements	MouseMotionListener, MouseListener,
				ComponentListener, KeyListener, FocusListener, AdjustmentListener
{
	final protected int EDGE = 10;

    protected Adjustable hscroll;
    protected Adjustable vscroll;

    protected boolean editEnabled = false;

	protected boolean		eactive,hactive;	// is there currently a range?
	protected boolean		mouseDown;
	protected boolean		autoIndent = true;	// auto indentation after ENTER?

	protected int			numVisibleLines;	       // number of visible lines

	protected int			fileStartLine,column,pix;	// the current column
	protected int			fileEndLine,ecolumn,epix;	// the current end column
	protected int			oldlines;			// repaint to old line count (if longer)
	protected int			fontHeight;			// save time
	protected int			fontWidth;			// save time
	protected int			fontDescent;		// save time
	protected int			tabSize;			// preference

	protected Font			font;				// preference
	protected FontMetrics	fontMetrics;		// save time
	protected Ruler			ruler;				// measure strings in pixels
	protected TextCursor	textCursor;			// friendly object
	protected ChiselDoc	doc;				// the document


	protected Hashtable actionDictionary;

	protected Vector /*TagLine*/ anchors;
	protected int currentAnchor = -1;

	final int LINE_MAX = 2000;

	public ChiselTextComponent(ChiselDoc doc, Adjustable hscroll, Adjustable vscroll) {
		super();
		this.hscroll = hscroll;
		this.vscroll = vscroll;

		this.doc = doc;
		doc.setViewer(this);

    	setBackground(Color.white);
		setForeground(Color.black);

		font = doc.getFont();
		tabSize = doc.getTabSize();

		ruler = new Ruler(doc);
		ruler.setTabSize(tabSize);

		updateFonts(null);

		createActionDictionary();
		anchors = new Vector(5);

		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addFocusListener(this);
		vscroll.addAdjustmentListener( this );

		addKeyListener(this);

		clear();
		resizeLines();
        requestFocus();
        setEditEnabled( true );
	}

    public boolean isEditEnabled() {
        return editEnabled;
    }

    public void setEditEnabled(boolean enabled) {

        if (!editEnabled && enabled) {
    		textCursor = new TextCursor();
	    	textCursor.start();

        } else if (editEnabled && !enabled) {
            release_cursor();
            textCursor.stop();
            textCursor = null;
        }

        editEnabled = enabled;
    }

	public ChiselDoc getDocument() {
		return doc;
	}

	/** Does nothing, template for ChiselAWTViewer */
	public void setVersion( int versionNo ) {
	}

	public int getVersion() {
	    return( 0 );
	}

    public Dimension getPreferredSize() {
        int numlines = doc.getLineCount() + 5;
        if (numlines < 10) {
            numlines = 10;
        }
    	return new Dimension(fontWidth * 128, fontHeight * numlines);
    }

    /** AdjustmentListener interface, listen for vscroll changes */
    public void adjustmentValueChanged( AdjustmentEvent e ) {
        int newFirstLine = vscroll.getValue() / fontHeight;
        setupMap( newFirstLine );
        setup_h();
        save_h();
    }

	protected boolean gotFocus = false;

	public void focusGained(FocusEvent e)
	{
//	    System.out.println( "Got focus" );
		gotFocus = true;
		release_cursor();
	}

	public void focusLost(FocusEvent e)
	{
//	    System.out.println( "Lost focus" );
		pause_cursor();
		gotFocus = false;
	}

	public void release_cursor()
	{
		if ((textCursor != null) && !eactive && gotFocus)
			textCursor.release_cursor(true);
	}

	public void pause_cursor()
	{
		if ((textCursor != null) && !eactive && gotFocus)
			textCursor.pause_cursor(true);
	}


	public int getHighest()	// highest visible line
	{
		return numVisibleLines - 1;
	}

	public int getLowest() // lowest visible line
	{
	    return 0;
	}
	
	public boolean isVisible( int lineNumber ) {
	    return(( lineNumber >= firstFileLineDisplayed ) && ( lineNumber < ( firstFileLineDisplayed + numberFileLinesDisplayed )));
	}
	
	public int getFirstVisibleLine() {
	    return( firstFileLineDisplayed );
	}
	
	public int getLastVisibleLine() {
	    return( firstFileLineDisplayed + numberFileLinesDisplayed );
	}

	public void clear()
	{
		//clear_area(null);
		hactive = eactive = false;
		updateCopyActions(false);
		oldlines = fileStartLine = column = 0;
		pix = EDGE;
	}


	public void setPos(Position pos)	{
		hactive = eactive = false;
		fileStartLine = fileEndLine = pos.line;
		column = ecolumn = pos.column;
		pix = epix = ruler.length(fileStartLine,column) + EDGE;
		shiftVert(fileStartLine);
		shiftHoriz(column);
		repaint();
	}

    /** Get display to jump to a particular line.
     *
     *  @param y file line to ensure is visible
     */
	public void showLine(int y)
	{
		if ((y >= 0) && (y < doc.getLineCount())) {
			boolean paint = hactive;
			hactive = eactive = false;


			// set the reference line depending on the direction
			int shiftLine = numVisibleLines / 2;
			
			// if the line is prior to the first visible line, shift is required
			if (y < firstFileLineDisplayed) {
				shiftLine = (y > (numVisibleLines / 2)) ? y - (numVisibleLines / 2) : 0;
			} else if ( y >= ( firstFileLineDisplayed + numberFileLinesDisplayed )) {
				shiftLine = y + (numVisibleLines / 2);
				shiftLine = (shiftLine < doc.getLineCount()) ? shiftLine : doc.getLineCount();
			} else {
			    repaint();
			    return;
			}

			fileStartLine = fileEndLine = y;
			column = ecolumn = 0;
			pix = epix = EDGE;

			if (shiftVert(shiftLine))
				paint = true;

			cursorAdjust();

			// if (shiftHoriz(pix)) -- this left it shifted to the right
			if (shiftHoriz(0))
				paint = true;

			if (paint)
				repaint();

		}
	}

    private void setNumVisibleLines() {
		updateFonts(null);
		numVisibleLines = (getVisibleSize().height + fontHeight - 1) / fontHeight;
	}

	public int getLine() {
		return fileStartLine;
	}

	public void legalizeCursor() {
		String whole;
		boolean illegal = false;
		int max_line;
		int max_column;

		max_line = doc.getLineCount();

		if (fileStartLine >= max_line)
		{
			fileStartLine = max_line - 1;
			illegal = true;
		}

		whole = doc.getLine(fileStartLine);
		max_column = whole.length();

		if (column > max_column)
		{
			column = max_column;
			illegal = true;
		}

		if (eactive && (fileEndLine >= max_line))
		{
			illegal = true;
		}

		if (eactive && !illegal)
		{
			whole = detabbed(doc.getLine(fileEndLine));
			max_column = whole.length();

			if (ecolumn > max_column)
			{
				illegal = true;
			}
		}

		if (fileStartLine < 0)
			fileStartLine = 0;

		if (column < 0)
			column = 0;

		if (illegal)
		{
			hactive = eactive = false;
			pix = epix = ruler.length(fileStartLine,column) + EDGE;
			shiftVert(fileStartLine);
			shiftHoriz(column);
			repaint();
		}
	}

	protected void updateFonts(Graphics g) {
		if (g != null) {
			g.setFont(font);
			fontMetrics = g.getFontMetrics(font);
        } else {
            fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        }

		fontHeight = fontMetrics.getHeight();
		fontDescent = fontMetrics.getDescent();
        fontWidth = fontMetrics.charWidth('X');
		ruler.setFontMetrics(fontMetrics);
	}


	public void addSearchPattern( String patt )
	{
		Searcher.getSearcher().addSearchPattern(patt);
	}

	public String[] getSearchPatterns()
	{
		return Searcher.getSearcher().getSearchPatterns();
	}

	public String getLatestSearchPattern()
	{
		return Searcher.getSearcher().getLatestSearchPattern();
	}

	public void addReplacePattern( String patt )
	{
		Searcher.getSearcher().addReplacePattern(patt);
	}

	public String[] getReplacePatterns()
	{
		return Searcher.getSearcher().getReplacePatterns();
	}

	public String getLatestReplacePattern()
	{
		return Searcher.getSearcher().getLatestReplacePattern();
	}


	public void findAgain(int direction)
	{
		String lastFind = getLatestSearchPattern();

		if ( lastFind == null )
			release_cursor();
		else
			find(lastFind, direction, lastRegex);
	}

	public boolean find( String pattern )
	{
		return find( pattern, 1, lastRegex );
	}

	boolean lastRegex = false;

	public boolean find( String pattern, int direction, boolean regex )
	{
		int i,max,ct,start,end,from;
		String whole;

		pause_cursor();

		lastRegex = regex;

		max = doc.getLineCount();
		i = fileStartLine;
		ct = 0;

		while (ct++ <= max)
		{
			whole = doc.getLine(i);

			if (( i == fileStartLine ) && ( ct == 1 ))
			{
				if (eactive)
					from = column + direction;
				else
					from = column;
			}
			else
				from = (direction > 0) ? 0 : whole.length();

			if ( direction > 0 )
				start = whole.indexOf(pattern,from);
			else
				start = whole.lastIndexOf(pattern, from );
			end = start + pattern.length();

			if (start >= 0)
			{
				fileStartLine = fileEndLine = i;
				column = start;
				pix = ruler.length(fileStartLine,column) + EDGE;
				ecolumn = end;
				epix = ruler.length(fileEndLine,ecolumn) + EDGE;
				eactive = true;
				setup_h();
				save_h();
				updateCopyActions(eactive);
				shiftVert(fileStartLine);
				shiftHoriz(column);
				shiftHoriz(ecolumn);
				repaint();
				return true;
			}

			if ( direction > 0 )
				i++;
			else
				i--;

			if (i >= max)
			{
				i = 0;
				System.out.println("Search wrapped.");
			}
			else if ( i < 0 )
			{
				i = max - 1;
				System.out.println("Search wrapped.");
			}
		}

		release_cursor();
		return false;
	}

	// Thanks to Ed McGlaughlin for cursor-select-* actions!
	// Ed's changes are tagged with //ecm ... the dead code
	// will probably be removed by v1.12

	public synchronized void mousePressed(MouseEvent e)	{
	    // filter out scroll bar events
	    if (e.getSource() != this) {
	        return;
	    }

	    // ecm
		// Graphics g;

		requestFocus();

		if (mouseDown)
			return;

		mouseDown = true;

		pause_cursor();

		clickPosition( e.getX(), e.getY() );

		if ((clickVisualLine < 0) || (visualToFileLine(clickVisualLine) >= doc.getLineCount()))
			return;

		// ecm -- split the function here
		startHighlight(e.getClickCount(), e.isShiftDown());

		// ecm -- had to pull this up from bottom half
		if (!e.isShiftDown())
    		addMouseMotionListener(this);
	}

	// ecm
	// This is the bottom half of mousePressed
	protected void startHighlight(int clickCount, boolean isShiftDown)
	{

		fileEndLine = visualToFileLine(clickVisualLine);
		ecolumn = ccolumn;
		epix = cpix;

		if (clickCount == 3) {
			fileEndLine = visualToFileLine(clickVisualLine);
			fileStartLine = visualToFileLine( clickVisualLine );
			column = 0;
			pix = EDGE;
			String whole = doc.getLine(fileStartLine);
			ecolumn = whole.length();
			epix = ruler.length(fileStartLine,10000000) + EDGE;
			eactive = true;
			setup_h();
			save_h();
			updateCopyActions(eactive);
			repaint();

		} else if (clickCount == 2) {
			char c;
			fileEndLine = visualToFileLine(clickVisualLine);
			fileStartLine = visualToFileLine( clickVisualLine );
			String whole = doc.getLine(fileStartLine);
			column = ecolumn = ccolumn;
			while (true)
			{
				if (column == 0)
					break;

				if (!Character.isLetterOrDigit(whole.charAt(column-1)))
					break;

				column--;
			}
			pix = ruler.length(fileStartLine,column) + EDGE;
			int max = whole.length();
			while (true)
			{
				if (ecolumn >= max)
					break;

				if (!Character.isLetterOrDigit(whole.charAt(ecolumn)))
					break;

				ecolumn++;
			}
			epix = ruler.length(fileStartLine,ecolumn) + EDGE;
			eactive = true;
			setup_h();
			save_h();
			updateCopyActions(eactive);
    		if ( doc.selectNode( NodeSelection.getSingleton() )) {
    		    NodeSelection nodeSelection = NodeSelection.getSingleton();
    			firstVisualLineSelection = fileToVisualLine( nodeSelection.startLine );
    			fileStartLine = visualToFileLine( firstVisualLineSelection );
    			column = startColumn = nodeSelection.startColumn;
    			lastVisualLineSelection = fileToVisualLine( nodeSelection.endLine );
    			fileEndLine = visualToFileLine( lastVisualLineSelection );
    			ecolumn = endColumn = nodeSelection.endColumn + 1;
    			pix = ruler.length(fileStartLine,column) + EDGE;
    			epix = ruler.length(fileEndLine, ecolumn) + EDGE;
    			System.out.println( "** fileStartLine " + fileStartLine + ", fileEndLine " + fileEndLine );
    			System.out.println( "** first VisualLine " + firstVisualLineSelection + ", lastVisual line " + lastVisualLineSelection );
    			setup_h();
    			save_h();
    		}
			repaint();

		} else if (isShiftDown)	{
			fileEndLine = visualToFileLine(clickVisualLine);
			ecolumn = ccolumn;
			epix = cpix;
			eactive = true;
			setup_h();
			save_h();
			updateCopyActions(eactive);
			repaint();

		} else {
			if (hactive) {
            	Graphics g = getGraphics();
                Dimension dim = getVisibleSize();
                g.clipRect(hscroll.getValue(), vscroll.getValue(), dim.width, dim.height);
				flip_h( g, oldFirstVisualLineSelection, opix, oldLastVisualLineSelection, oepix );
				g.dispose();
		    }

			fileEndLine = visualToFileLine(clickVisualLine);
			fileStartLine = visualToFileLine( clickVisualLine );
			String whole = doc.getLine(fileStartLine);
//			System.out.println( "fileStartLine " + clickVisualLine + " is '" + whole + "'" );
//			for ( int i = 0; i < ndListSize; i++ ) {
	//		    System.out.println( "fileStartLine " + (sy+i) + " above sum is " + ndList[i] );
		//	}
			ecolumn = column = ccolumn;
			epix = pix = cpix;
			hactive = false;
			eactive = true;
			updateCopyActions(eactive);
			// ecm -- move up to mousePressed
			// addMouseMotionListener(this);
		}
		// ecm -- add fileStartLine number display
		//textFrame.setLine(fileStartLine);
	}

	protected int opix, oepix, oldFirstVisualLineSelection, oldLastVisualLineSelection;
	protected int hpix, hepix, firstVisualLineSelection, lastVisualLineSelection, startColumn, endColumn;
	protected int lastx,lasty;

	public synchronized void mouseDragged(MouseEvent e) {
		if (e != null) {
			lastx = e.getX();
			lasty = e.getY();
		}

		clickPosition( lastx, lasty );

		// ecm -- split here
		moreHighlight();
	}

    class TextCursor extends Thread  {
    	private		boolean		flash;
    	private		boolean		undraw;		// true when the next draw erases
    	private		Rectangle	r;

    	public TextCursor() {
    		r = new Rectangle();
    	}

    	public void run() {
    		while (true) {
    			try {
    			    sleep(500);
    			} catch(InterruptedException e) {}
    			sync_draw();
    		}
    	}

    	private synchronized void sync_draw() {
    		if (flash) {
    			draw_or_undraw();
    	    }
    	}

    	public synchronized void pause_cursor(boolean draw) {
    		if (undraw) {
    			draw_or_undraw();
    	    }

    		flash = false;
    	}

    	public synchronized void release_cursor(boolean draw) {
    		if (!undraw) {
    			draw_or_undraw();
    	    }

    		flash = true;
    	}

    	private boolean draw_or_undraw() {
    		Graphics g = getGraphics();
    		if (g != null) {
    			if (!undraw) {
    				getCursorPos(g,r);
    		    }
    			g.setColor(Color.black);
    			g.setXORMode(Color.white);
    			g.drawLine(r.x, r.y, r.x, r.y+r.height-1);
    			undraw = !undraw;
    			g.dispose();
    		}
    		return undraw;
    	}
    }

	class TextScroller extends Thread {

		public TextScroller() {
		}

		public void run() {
			while (true)
			{
				try { sleep(10); }
				catch(InterruptedException e) {}
				mouseDragged(null);
			}
		}
	}

	protected TextScroller textScroller = null;


	protected void moreHighlight() {
		Graphics g = getGraphics();

		fileEndLine = visualToFileLine(clickVisualLine);
		ecolumn = ccolumn;
		epix = cpix;

        int leftx = hscroll.getValue();
        int leftcolumn = leftx / fontWidth;
        int topy = vscroll.getValue();
        int topline = topy / fontHeight;
        int numlines = numVisibleLines;
        Dimension dim = getVisibleSize();
        g.clipRect(leftx, topy, dim.width, dim.height);

		eactive = ((fileEndLine != fileStartLine) || (ecolumn != column));
		//System.out.println( "moreHighlight eactive " + eactive + ", fileStartLine " + fileStartLine + " fileEndLine " + fileEndLine + " visualToFileLine(fileEndLine) " + visualToFileLine(fileEndLine));
    	if (eactive || hactive)
		{
			setup_h();

			if ((fileToVisualLine(fileEndLine) < topline) || (fileToVisualLine(fileEndLine) >= topline + numlines) || (epix < leftx) || (epix >= dim.width)) {
				if (textScroller == null) {
					textScroller = new TextScroller();
					textScroller.start();
				}
				shiftVert(fileEndLine);
				shiftHoriz(ecolumn);
				setup_h();
				save_h();
				doc.extendHilite(topline + numlines - 1);
				int ndrawn = 0;
				ensureListCapacity( numlines );
				int j = 0;
				int u = 0;
				for (int i = 0; ndrawn < numlines; i++) {
				    mapList[j++] = ndrawn - i;
				    unmapList[u++] = ndrawn - i;
					int numberLinesDrawn = drawLine(g, topline + i, topline + ndrawn);
					for ( int k = 1; k < numberLinesDrawn; k++ ) {
					    if ( j < ndListSize ) {
    					    mapList[j++] = ndrawn - i;
    					}
					}
					ndrawn += numberLinesDrawn;
				}
			}
			else
			{
				if (textScroller != null)
				{
					textScroller.stop();
					textScroller = null;
				}
				if (hactive)
				{
					if ((firstVisualLineSelection < oldFirstVisualLineSelection) || ((firstVisualLineSelection == oldFirstVisualLineSelection) && (hpix < opix)))
						flip_h( g, firstVisualLineSelection, hpix, oldFirstVisualLineSelection, opix );
					if ((firstVisualLineSelection > oldFirstVisualLineSelection) || ((firstVisualLineSelection == oldFirstVisualLineSelection) && (hpix > opix)))
						flip_h( g, oldFirstVisualLineSelection, opix, firstVisualLineSelection, hpix );
					if ((lastVisualLineSelection < oldLastVisualLineSelection) || ((lastVisualLineSelection == oldLastVisualLineSelection) && (hepix < oepix)))
						flip_h( g, lastVisualLineSelection, hepix, oldLastVisualLineSelection, oepix );
					if ((lastVisualLineSelection > oldLastVisualLineSelection) || ((lastVisualLineSelection == oldLastVisualLineSelection) && (hepix > oepix)))
						flip_h( g, oldLastVisualLineSelection, oepix, lastVisualLineSelection, hepix );
				}
				else
					flip_h( g, firstVisualLineSelection, hpix, lastVisualLineSelection, hepix );

				hactive = eactive;
				save_h();
			}
		}
		// ecm -- add line number display
		//textFrame.setLine(fileStartLine);

		g.dispose();
	}

	protected int clickVisualLine, ccolumn, cpix;

	void clickPosition( int x, int y )
	{
		clickVisualLine = (y / fontHeight);

		if (clickVisualLine < 0) {
			clickVisualLine = ccolumn = cpix = 0;
			return;
		}

		if (visualToFileLine(clickVisualLine) >= doc.getLineCount()) {
		    int fline = doc.getLineCount() - 1;
			clickVisualLine = fileToVisualLine(fline);
    		String whole = doc.getLine(fline);
    		if ( whole != null ) {
    			ccolumn = whole.length();
    			cpix = ruler.length(fline,10000000) + EDGE;
    		}
			return;
		}

		x = x - EDGE;

		ccolumn = cpix = 0;

		if (x > 0) {
		    int fline = visualToFileLine( clickVisualLine );
			Position pos = ruler.position(fline,fline,x);
			ccolumn = pos.column;
			cpix = ruler.length(visualToFileLine(pos.line), ccolumn) + EDGE;
		}
	}

	protected void flip_h(Graphics g, int flipStartLine, int spix, int flipEndLine, int epix)
	{
		int i;
		int bx,ex,by;
		g.setColor(Color.black);
		g.setXORMode(Color.white);

        int sx = hscroll.getValue() / fontWidth;
        int sy = vscroll.getValue() / fontHeight;

		if (flipEndLine >= sy + numVisibleLines)
		{
			flipEndLine = sy + numVisibleLines - 1;
			epix = 5000;
		}

		for (i = flipStartLine; i <= flipEndLine; i++)
		{
			by = (i /*- sy*/) * fontHeight;
			bx = 0;
			ex = 5000;
			if (i == flipStartLine)
			{
				bx = spix; // - sx;
			}
			if (i == flipEndLine)
			{
				ex = epix; // - sx;
				if (flipStartLine == flipEndLine)
					ex -= bx;
			}
			g.fillRect(bx,by,ex,fontHeight);
		}
	}

    //  line is the document line number
    //  what is firstVisualLineSelection
	protected void setup_h()
	{
		if ((fileStartLine < fileEndLine) || ((fileStartLine == fileEndLine) && (column <= ecolumn)))
		{
			firstVisualLineSelection = fileToVisualLine(fileStartLine);
			startColumn = column;
			hpix = pix;
			lastVisualLineSelection = fileToVisualLine(fileEndLine);
			endColumn = ecolumn;
			hepix = epix;
		}
		else
		{
			firstVisualLineSelection = fileToVisualLine(fileEndLine);
			startColumn = ecolumn;
			hpix = epix;
			lastVisualLineSelection = fileToVisualLine(fileStartLine);
			endColumn = column;
			hepix = pix;
		}
	}

	protected void save_h()
	{
		opix = hpix;
		oepix = hepix;
		oldFirstVisualLineSelection = firstVisualLineSelection;
		oldLastVisualLineSelection = lastVisualLineSelection;
		hactive = eactive;
	}


	public synchronized void mouseReleased(MouseEvent e)
	{
		if (!mouseDown)
			return;

		mouseDown = false;

		if (textScroller != null)
		{
			textScroller.stop();
			textScroller = null;
		}
		removeMouseMotionListener(this);
		eactive = (fileEndLine != fileStartLine) || (ecolumn != column);
		release_cursor();
		updateCopyActions(eactive);
	}

	class ButtonPusher extends Thread {
		ActionListener listener;

		public ButtonPusher(ActionListener aListener ) {
			listener = aListener;
		}

		public void run() {
			listener.actionPerformed(null);
		}
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
    public void keyPressed(KeyEvent e)
	{
		int		keyModifiers = e.getModifiers();
		int		keyCode = e.getKeyCode();
		char	keyChar = e.getKeyChar();
		int		max;
		String	s;

		if (!gotFocus || e.getSource() != this) {
		    System.out.println(gotFocus ? "no focus" : "foreign");
			return;
	    }

		s = ChiselKeys.getKeyAction(keyModifiers,keyCode);

		if (s != null)
		{
			AbstractAction act = (AbstractAction)actionDictionary.get(s);

			if ( act != null )
			{
				e.consume();
				if ( !act.isEnabled() )
					return;
				pause_cursor();

//				System.out.println("Action name: " + act.getName() );
//				System.out.println("Action short: " + act.getShortDescription() );

				if (s.indexOf("dialog") >= 0)	// use a thread for dialogs
				{
					ButtonPusher bp = new ButtonPusher(act);
					bp.start();
				}
				else
				{
					act.actionPerformed(null);
				}
				release_cursor();
        		// ecm -- add line number display
        		//textFrame.setLine(fileStartLine);
				return;
			}
		}

		if (e.isMetaDown() || e.isControlDown() || e.isAltDown() )
			return;

		if ( doc.isReadOnly() )
			return;

		pause_cursor();

		// default character insert
		if (keyChar != KeyEvent.CHAR_UNDEFINED)
		{
			if (keyCode != KeyEvent.VK_ESCAPE && keyCode != KeyEvent.VK_SHIFT &&
				keyChar != '\n' &&
				keyChar != '\b') 
			{
				e.consume();
				if (hactive)
					copy(true,false);
				column++;
				ruler.invalidate( fileStartLine, fileStartLine );
				doc.insert_char(fileStartLine,column-1,keyChar);
				 // JJ 1998.01.03  - "pix" must be updated with "column"
				pix = ruler.length(fileStartLine,column) + EDGE;
				shiftHoriz(column);
        		// ecm -- add line number display
        		//textFrame.setLine(fileStartLine);
				return;
			}
		}
		release_cursor();
	}

    /** Execute an action */
    public void doAction( String actionName ) {
		AbstractAction act = (AbstractAction)actionDictionary.get( actionName );
		if ( act != null ) {
			if ( !act.isEnabled() )
				return;
			pause_cursor();
			if (actionName.indexOf("dialog") >= 0) {	// use a thread for dialogs
				ButtonPusher bp = new ButtonPusher(act);
				bp.start();
			} else {
				act.actionPerformed(null);
			}
			release_cursor();
			return;
		}
	}

    /** Structures for converting visual line numbers into file line numbers and vice versa */
	int ndListSize = 0;
	int[] mapList;
	int[] unmapList;
	void ensureListCapacity( int size ) {
	    size++;
	    if ( size > ndListSize ) {
	        mapList = new int[ size ];
	        unmapList = new int[ size ];
	        ndListSize = size;
	    }
	}

    /** Set up the mapping due to scrolling, without actually painting, this logic
     *  is duplicated in the paint method.
     */
     void setupMap( int newFirstLine ) {
        ensureListCapacity( numVisibleLines );
        int jj = 0;
        int ndrawn = 0;
		for (int i = 0; ndrawn <= numVisibleLines; i++) {
		    int numberDrawn = 1 + doc.getErrorCount( i + newFirstLine );
		    unmapList[i] = ndrawn - i;
   			for ( int j = 0; j < numberDrawn; j++ ) {
   			    if ( jj < ndListSize ) {
       			    mapList[jj++] = ndrawn - i;
       			}
   			}
   			ndrawn += numberDrawn;
		}
	 }

	/** Convert a visual line number into a file line number.
	 *  These are almost identical, except that visual line there are
	 *  sometimes more visual lines than there are file lines.  This occurs
	 *  when warning and error (i.e. non-file lines) are inserted between
	 *  file lines.
	 *
	 *  The variable "firstVisibleFileLine" is the file line of the very first line in the
	 *  window.
	 *
	 *  Given a visual line number that is in the displayable range, it
	 *  is converted to a file line number by subtracting the number of
	 *  "extra" lines inserted above that line.  This "extra" cound is
	 *  kept in the "mapList" array.
	 */
	int visualToFileLine( int lineNo ) {
	    int sy = vscroll.getValue() / fontHeight;
	    if (( lineNo - sy ) < 0 ) {
	        return( lineNo );
	    } else if (( lineNo - sy ) >= ndListSize ) {
	        return( lineNo );
	    }
	    return( lineNo - mapList[ lineNo - sy ] );
	}

	/** Convert a file line number into a visual line number */
	int fileToVisualLine( int lineNo ) {
	    int sy = vscroll.getValue() / fontHeight;
	    if (( lineNo - sy ) < 0 ) {
	        return( lineNo );
	    } else if (( lineNo - sy ) >= ndListSize ) {
	        return( lineNo );
	    }
	    return( lineNo + unmapList[ lineNo - sy ] );
	}

	public void paste(String s)
	{
		int oldline;
		int oldcolumn;
		Position pos;
		boolean paint;

		pause_cursor();

		if (hactive)
			copy(true,false);

		oldline = fileStartLine;
		oldcolumn = column;

		pos = doc.insert_section(fileStartLine,column,s,false);

		fileStartLine = pos.line;
		column = pos.column;
		ruler.invalidate(oldline,fileStartLine);
		pix = ruler.length(fileStartLine,column) + EDGE;

		doc.updateFrames(oldline,fileStartLine);

		paint = oldline != fileStartLine;

		paint = false;

		if (shiftVert(fileStartLine))
			paint = true;

		if (shiftHoriz(column))
			paint = true;

		if (paint)
			repaint();

	}

	public String copy(boolean cut, boolean visible)
	{
		String s;
		int oldline;
		int oldcolumn;
		boolean paint = false;

		if (!hactive)
			return null;

		if (firstVisualLineSelection == lastVisualLineSelection)
		{
			oldline = firstVisualLineSelection;
			oldcolumn = startColumn;
		}
		else
			paint = true;

		s = doc.delete_section(visualToFileLine(firstVisualLineSelection), startColumn, visualToFileLine(lastVisualLineSelection), endColumn, cut);
		if (visible) {
			doc.updateFrames(visualToFileLine(firstVisualLineSelection),visualToFileLine(lastVisualLineSelection));
		}

		if (cut) {
			fileStartLine = visualToFileLine(firstVisualLineSelection);
			column = startColumn;
			hactive = eactive = false;

			if (shiftVert(fileStartLine)) {
				paint = true;
			}

			pix = ruler.length(fileStartLine,column) + EDGE;
			if (shiftHoriz(column))
				paint = true;

			if (paint) {
				repaint();
			} else {
				if (visible) {
					linesChanged(fileStartLine,fileStartLine);
				}
			}
		}

		return s;
	}

	protected void cursorAdjust()
	{
		cursorAdjust(false);
	}

	protected void cursorAdjust(boolean force_paint)
	{
		int min, max;
		boolean paint = force_paint;

		if (eactive)
		{
			paint = true;
			hactive = eactive = false;
			updateCopyActions(eactive);
		}

		// straighten up lines first

		if (fileStartLine < 0)
			fileStartLine = 0;
		else
		{
			max = doc.getLineCount();

			if (fileStartLine >= max )
				fileStartLine = max - 1;
		}

		// straighten up columns

		if (column < 0)
			column = 0;
		else
		{
			max = (doc.getLine(fileStartLine)).length();
			if (column > max)
				column = max;
		}

		// off page?

		if (shiftVert(fileStartLine))
			paint = true;

		// JJ 1998.01.03  - "pix" must be updated with "column"

		pix = ruler.length(fileStartLine,column) + EDGE;
		if (shiftHoriz(column))
			paint = true;

		if (paint)
			repaint();
		else
			release_cursor();
	}

    /** Shift text vertically so a particular file line is visible */
	protected boolean shiftVert( int fileStartLine ) {
        int sy = vscroll.getValue()/fontHeight;

		if (fileStartLine < sy) {
			sy = fileStartLine;
			if (sy < 0) {
				sy = 0;
		    }
			vscroll.setValue(sy*fontHeight);
			return true;
		}

		if (fileStartLine >= sy + numberFileLinesDisplayed) {
			sy = fileStartLine - numberFileLinesDisplayed + 1;
			vscroll.setValue(sy * fontHeight);
			doc.extendHilite(sy + numVisibleLines - 1);
			return true;
		}

		return false;
	}

    /** Shift text horizontally so that a particular column is visible */
	protected boolean shiftHoriz( int column ) {
	    int scrollColumn = hscroll.getValue()/fontWidth;
        Dimension dim = getVisibleSize();
        int numcols = dim.width / fontWidth;

        // if the desired column is less than the scroll setting,
        // set the scroll setting to be a bit before the desired column
		if (column <= scrollColumn) {
			scrollColumn = column - numcols/5;

			if (scrollColumn < 0) {
				scrollColumn = 0;
		    }
			hscroll.setValue(scrollColumn* fontWidth);
			return true;
		}

		if (column >= scrollColumn + numcols) {
			scrollColumn = column - (4 * numcols / 5);
			hscroll.setValue(scrollColumn* fontWidth);
			return true;
		}

		return false;
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}

	public void componentResized(ComponentEvent e) {

	    //System.out.println("TextComp gets componentResized call");
	    resizeLines();
	    setNumVisibleLines();
	}


    // to get the visible size, get the minimum of the component size
    // and the grandparent's size, because the grandparent determines the
    // viewable area when the component belongs to a ScrollablePane
    private Dimension getVisibleSize() {
        Dimension size = getSize();
        Container parent = getParent();
        if (parent != null) {
            parent = parent.getParent();
            if (parent != null) {
                Dimension psize = parent.getSize();

                //System.out.println("getVisibleSize size " + size + "\n       granny size " + psize);

                size.width = Math.min(size.width, psize.width);
                size.height = Math.min(size.height, psize.height);
            }
        }
        return size;
    }


	protected void resizeLines()
	{
		if ((visualToFileLine(firstVisualLineSelection) >= 0) && (visualToFileLine(firstVisualLineSelection) < doc.getLineCount()))
			opix = hpix = ruler.length(visualToFileLine(firstVisualLineSelection),startColumn) + EDGE;

		if ((visualToFileLine(lastVisualLineSelection) >= 0) && (visualToFileLine(lastVisualLineSelection) < doc.getLineCount()))
			oepix = hepix = ruler.length(visualToFileLine(lastVisualLineSelection),endColumn) + EDGE;
	}


	protected String detabbed(String s)
	{
		if (s.indexOf('\t') < 0)
			return s;

		char c;
		String t = new String("");
		int j=0;
		int tabs;
		int max = s.length();

		for (int i=0; i<max; i++)
		{
			c = s.charAt(i);
			if (c == '\t')
			{
				tabs = tabSize - (j % tabSize);
				j += tabs;
				while (tabs-- > 0) t = t + ' ';
			}
			else
			{
				t = t + c;
				j++;
			}
		}

		return t;
	}

	char buffer[];

	protected int fillBuffer(LineInfo li)
	{
		char c;
		int i,j,tabs,max;
		char before[];

		before = li.getCharArray(); // li.data.toCharArray();

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

	protected int charsLength(int start, int length)
	{
		if (fontMetrics == null)
			return -1;

		return fontMetrics.charsWidth(buffer,start,length);
	}

	public void getCursorPos(Graphics g, Rectangle r)
	{
		//updateFonts(g);
		r.width = 2;
		r.height = fontHeight;
		r.y = fileToVisualLine(fileStartLine) * r.height;

		if (column > 0)
		{
			r.x = ruler.length(fileStartLine,column) + EDGE;
		}
		else
			r.x = EDGE;
	}

	public String getSelectionOrWordUnderCursor()
	{
		String str = copy(false,false);
		if ( str != null )
			return str;
		int leftBound = column;
		int rightBound = column;
		String currLine = doc.getLine(fileStartLine);
		int rightMax = currLine.length();

leftB:	while ( (--leftBound >= 0) )
		{
			switch( currLine.charAt(leftBound) )
			{
				case ' ':
				case '\t':
				case '.':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case ';':
					leftBound++;
					break leftB;
			}
		}
		if ( leftBound < 0 )
			leftBound = 0;

rightB:	while ( rightBound < rightMax )
		{
			switch( currLine.charAt(rightBound) )
			{
				case ' ':
				case '\t':
				case '.':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case ';':
					break rightB;
				default:
					rightBound++;

			}
		}

		if ( rightBound == 0 )
			return "";
		return currLine.substring(leftBound, rightBound);
	}

	public void linesChanged(int first, int last)
	{
	    int sy = vscroll.getValue() / fontHeight;
		if ((first > sy + numVisibleLines) || (last < sy)) {
			return;
	    }

		if (first == last) {
			Graphics g = getGraphics();
			updateFonts(g);
			drawLine(g, fileToVisualLine(first),fileToVisualLine(first));
			g.dispose();
			release_cursor();
		}
		else
			repaint();
	}


	public void updateUndoActions( boolean un, boolean re )
	{
		undoAction.setEnabled(un);
		redoAction.setEnabled(re);
	}

	public void updateCopyActions( boolean active )
	{
	    if ( !active ) {
	        NodeSelection.getSingleton().reset();
	    } else {
	        NodeSelection.getSingleton().set( fileStartLine, fileEndLine, startColumn, endColumn );
	    }
		cutAction.setEnabled(active);
		copyAction.setEnabled(active);
	}

	public void update(Graphics g)
	{
		paint(g);
	}

    int currentErrorLine = -1;
    int numberFileLinesDisplayed = 0;
    int firstFileLineDisplayed = 0;
	public void paint(Graphics g)
	{
		if ( !isShowing() )	 { // why java tries to repaint window during file load ?
    		System.out.println("painting invisible TextComponent???");
			return;
	    }
	    if ( !ChiselSet.chiselsEnabled() ) return;
        currentErrorLine = doc.getCurrentErrorLine();
		Rectangle clip;
		Rectangle curs = new Rectangle();

		pause_cursor();

		g.setPaintMode();
		updateFonts(g);

        int sx = hscroll.getValue() / fontWidth;
        int sy = vscroll.getValue() / fontHeight;

		int numlines = doc.getLineCount();
		if (oldlines != numlines) {
			oldlines = numlines;
			setNumVisibleLines();
		}


        // keep this outside the loop
		docWidth = getSize().width;

        ensureListCapacity( numVisibleLines );
        int jj = 0;
        int ndrawn = 0;
        numberFileLinesDisplayed = 0;
        firstFileLineDisplayed = sy;
		for (int i = 0; ndrawn <= numVisibleLines; i++) {
		    // the "drawLine" method will draw more than one line if there
		    // are errors or warnings associated with the line
		    int numberDrawn;
            try {
   			    numberDrawn = drawLine(g, sy + i, sy + ndrawn);
   			} catch (Exception e) {
   			    System.out.println("Exception in ChiselTextComponent.paint: " + e);
   			    break;
   			}
		    unmapList[i] = ndrawn - i;
   			for ( int j = 0; j < numberDrawn; j++ ) {
   			    if ( jj < ndListSize ) {
       			    mapList[jj++] = ndrawn - i;
       			}
   			}
   			ndrawn += numberDrawn;
   			numberFileLinesDisplayed = i;
		}

		release_cursor();
	}


	static Color textColor = Color.black;
	static Color textXColor = Color.white;
	static Color commentColor = new Color(0x009900);;
	static Color commentXColor = new Color(0x009900 ^ 0xffffff);
	static Color keywordColor = new Color(0x0000aa);
	static Color keywordXColor = new Color(0x0000aa ^ 0xffffff);
	static Color keyword2Color = new Color(0x8800aa);
	static Color keyword2XColor = new Color(0x8800aa ^ 0xffffff);
	static Color quoteColor = new Color(0xaa0000);
	static Color quoteXColor = new Color(0xaa0000 ^ 0xffffff);
    static Color warnColor = Color.blue.darker();
    static Color errorColor = Color.red.darker();

    /** Draw a line, along with warning and error lines.
     *
     *  @return the number of lines drawn
     */
	private int docWidth = 0;
	protected synchronized int drawLine(Graphics g, int i, int ylocator)
	{
		int x,y,m;
		int a,b,c;
		int max,key;
		String s;
		LineInfo hi;
		Rectangle curs = null;
		boolean xor = false;
//System.out.println( "draw line " + i );
		x = EDGE;
		y = (ylocator + 1) * fontHeight;
		m = fontHeight;

		g.setPaintMode();

        // oldFirstVisualLineSelection and oldLastVisualLineSelection are visual document lines
		if (hactive && (i > visualToFileLine(oldFirstVisualLineSelection)) && (i < visualToFileLine(oldLastVisualLineSelection))) {
    		g.setColor(Color.black);
			g.fillRect(0,y-fontHeight,docWidth,m);
			xor = true;

		} else {
    		g.setColor(getBackground());
			g.fillRect(0,y-fontHeight,docWidth,m);
		}

		if (i >= doc.getLineCount())
			return 1;

		hi = doc.getLineInfo(i);
		if ( hi == null ) {
		    return 1;
		}
		max = fillBuffer(hi);
		a = 0;
		key = 0;

		while (a < max) {
			if (key < hi.keyCt)
			{
				b = hi.keyStarts[key];
				c = hi.keyEnds[key];
				if ( b > max ) {
				    b = max;
				}
				if ( c > max ) {
				    c = max;
				}
			}
			else
			{
				b = max;
				c = max;
			}
			if (b > a)
			{
				if (xor) {
					g.setColor(textXColor);
				} else {
					g.setColor(textColor);
					if ( i == currentErrorLine ) {
					    g.setColor( Color.red );
					}
				}
				g.drawChars(buffer, a, b-a, x, y-fontDescent);
				x += charsLength(a, b-a);
			}
			if (c > b)
			{
				switch (hi.keyTypes[key]) {
				case Hilite.COMMENT:
					if (xor)
						g.setColor(commentXColor);
					else
						g.setColor(commentColor);
					break;
				case Hilite.KEYWORD:
					if (xor)
						g.setColor(keywordXColor);
					else
						g.setColor(keywordColor);
					break;
				case Hilite.KEYWORD2:
					if (xor)
						g.setColor(keyword2XColor);
					else
						g.setColor(keyword2Color);
					break;
				case Hilite.QUOTE:
					if (xor)
						g.setColor(quoteXColor);
					else
						g.setColor(quoteColor);
					break;
				case Hilite.PLAIN:
				    if (xor)
    					g.setColor(textXColor);
    				else
    					g.setColor(textColor);
	    			break;
				}
					if ( i == currentErrorLine ) {
					    g.setColor( Color.red );
					}
   				g.drawChars(buffer, b, c-b, x, y-fontDescent);
				x += charsLength(b, c-b);
			}
			a = c;
			key++;
		}

		if (hactive && ((i == visualToFileLine(oldFirstVisualLineSelection)) || (i == visualToFileLine(oldLastVisualLineSelection)))) {
			int bx = 0;
			int ex = 5000;
			if (i == visualToFileLine(oldFirstVisualLineSelection))
			{
				bx = opix;
			}
			if (i == visualToFileLine(oldLastVisualLineSelection))
			{
				ex = oepix;
				if (oldFirstVisualLineSelection == oldLastVisualLineSelection)
					ex -= bx;
			}
			g.setColor(Color.black);
			g.setXORMode(Color.white);
			g.fillRect(bx,y-fontHeight,ex,m);
        }

		int numberErrors = doc.getErrorCount( i );
		if ( numberErrors > 10 ) {
		    numberErrors = 10;
		}
   		x = EDGE;
		g.setPaintMode();
   		for ( int j = 0; j < numberErrors; j++ ) {
//   		    System.out.println( "draw error line at yloc " + (y-fontDescent));
       		y = (ylocator + 2 + j) * fontHeight;
       		String ss = doc.getErrorViewerString( i, j );
       		if ( ss == null ) {
       		    numberErrors--;
       		    continue;
       		}
   			if ( ss.indexOf( "Warning" ) > 0 ) {
   			    g.setColor( warnColor );
   			} else {
   			    g.setColor( errorColor );
   			}
   			g.drawString( ss, x, y - fontDescent );
    	}
		return( 1 + numberErrors );
	}

   // added by mash 3/16/98
    /** override getGraphics to clip the graphics to its container. *
    public Graphics getGraphics() {
        Graphics g = super.getGraphics();
        if (g != null) {
            Component parent = getParent();
            if (parent != null) {
                Rectangle b = getBounds();
                Rectangle pb = parent.getBounds();
                Rectangle ppb = parent.getParent().getBounds();
                //convert to this components coordinates
                ppb.x = -pb.x - b.x;
                ppb.y = -pb.y - b.y;

                pb.x = -b.x;
                pb.y = - b.y;

                g.clipRect(ppb.x, ppb.y, ppb.width, ppb.height);
                g.clipRect(pb.x, pb.y, pb.width, pb.height);
            }
        } else {
            System.out.println("graphics null!!!");
        }
        return g;
    }******/

	protected void addToDict( AbstractAction action )
	{
		actionDictionary.put(action.getIdString(), action );
	}

	public AbstractAction getAction( String id )
	{
		return (AbstractAction) actionDictionary.get(id);
	}

	protected void createActionDictionary()
	{
		actionDictionary = new Hashtable();

		addToDict( undoAction );
		addToDict( redoAction );

		addToDict( new
			AbstractAction("cursor-up") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					fileStartLine--;
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("cursor-down") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					fileStartLine++;
					cursorAdjust();
				}
		}
		);

		addToDict( new
			AbstractAction("cursor-forward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					column++;
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("cursor-backward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					column--;
					cursorAdjust();
				}
		}
		);

		addToDict( new
			AbstractAction("cursor-word-forward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					_wordForward();
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("cursor-word-backward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					_wordBackward();
					cursorAdjust();
				}
		}
		);

		addToDict( new
			AbstractAction("cursor-line-begin") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					column = 0;
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("cursor-line-end") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					column = doc.getLine(fileStartLine).length();
					cursorAdjust();
				}
		}
		);

		addToDict( new
			AbstractAction("cursor-page-begin") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					fileStartLine = vscroll.getValue()/fontHeight;
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("cursor-page-end") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					fileStartLine = Math.min( doc.getLineCount() -1, vscroll.getValue()/fontHeight + numVisibleLines - 1 );
					cursorAdjust();
				}
		}
		);

		addToDict( new
			AbstractAction("cursor-document-begin") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					fileStartLine = 0;
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("cursor-document-end") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					fileStartLine = doc.getLineCount() - 1;
					cursorAdjust();
				}
			}
		);

		addToDict( new
			AbstractAction("page-up") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					int offset = fileStartLine - vscroll.getValue() / fontHeight;
					fileStartLine -= numVisibleLines - 1;
					shiftVert(Math.max(fileStartLine-offset,0));
					cursorAdjust(true);
				}
		}
		);

		addToDict( new
			AbstractAction("page-down") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					int offset = fileStartLine - vscroll.getValue() / fontHeight;
					fileStartLine += numVisibleLines - 1;
					shiftVert(Math.min(fileStartLine-offset+numVisibleLines-1, doc.getLineCount() -1 ));
					cursorAdjust(true);
				}
			}
		);

		addToDict( new
			AbstractAction("find-next-forward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					findAgain(1);
				}
		}
		);

		addToDict( new
			AbstractAction("find-next-backward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					findAgain(-1);
				}
			}
		);

    /***
		addToDict( new
			AbstractAction("brace-match-forward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					findMatchingBrace(1);
				}
		}
		);

		addToDict( new
			AbstractAction("brace-match-backward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					findMatchingBrace(-1);
				}
			}
		);
    **********/
		addToDict( new
			AbstractAction("character-delete-forward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					_pressedDelete();
				}
		}
		);

		addToDict( new
			AbstractAction("character-delete-backward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					_pressedBackspace();
				}
			}
		);

		addToDict( new
			AbstractAction("line-break") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					_insertNewline();
				}
		}
		);

		addToDict( new
			AbstractAction("line-clone") {
				{
				}

				public void actionPerformed(ActionEvent e ) {
					doc.insert_line( fileStartLine, doc.getLine(fileStartLine));
					fileStartLine++;
					cursorAdjust(true);
				}
			}
		);

		addToDict( new
			AbstractAction("line-delete") {
				{
				}

				public void actionPerformed(ActionEvent e ) {
					_deleteLine();
				}
		}
		);

		addToDict( new
			AbstractAction("mode-autoindent-switch") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					autoIndent = !autoIndent;
					System.out.println("Autindentation turned " + (autoIndent ? "ON" : "OFF"));
				}
			}
		);

		addToDict( copyAction = new
			AbstractAction("selection-copy") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					String srcData = copy(false,false);
					if (srcData != null) {
			            StringSelection contents = new StringSelection(srcData);
			            ChiselClipboard.getClipboard().setContents(contents, ChiselClipboard.getOwner());
					}
				}
			}
		);

		addToDict( cutAction = new
			AbstractAction("selection-cut") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					String srcData = copy(true,true);
					if (srcData != null) {
			            StringSelection contents = new StringSelection(srcData);
			            ChiselClipboard.getClipboard().setContents(contents, ChiselClipboard.getOwner());
					}
				}
			}
		);

		addToDict( new
			AbstractAction("buffer-paste") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					String dstData;
			        Transferable content = ChiselClipboard.getClipboard().getContents(ChiselClipboard.getOwner());
			        if (content != null) {
			            try {
			                dstData = (String)content.getTransferData(DataFlavor.stringFlavor);

						} catch (Exception exc)	{
			                System.out.println("Could not read clipboard");
							return;
			            }
						paste(dstData);
			    	}
				}
			}
		);
        /****
		addToDict( new
			AbstractAction("keytable-save") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					ChiselKeys.saveKeytable();
				}
			}
		);

		addToDict( new
			AbstractAction("keytable-load") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					ChiselKeys.loadKeytable();
				}
			}
		); ****/

		addToDict( new
			AbstractAction("mode-readonly-switch") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					doc.setReadOnly(!doc.isReadOnly());
				}
			}
		);

		addToDict( new
			AbstractAction("selection-indent") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					// maybe allow something else instead of tab ?
					// this should have its own undo routine
					if ( !hactive )
						return;
					int max = visualToFileLine(lastVisualLineSelection);
					if (endColumn == 0)	// full line selections include column 0 from next line
						max--;			// so adjust maximum
					for (int i = visualToFileLine(firstVisualLineSelection); i <= max; i++ )
					{
						doc.insert_char(i, 0, '\t');
					}
				}
			}
		);

		addToDict( new
			AbstractAction("selection-unindent") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!hactive)
						return;
					int max = visualToFileLine(lastVisualLineSelection);
					if (endColumn == 0)	// full line selections include column 0 from next line
						max--;			// so adjust maximum
					for (int  i = visualToFileLine(firstVisualLineSelection); i <= max; i++ )
					{
						String line = doc.getLine(i);
						if (line.length() > 0)
						{
							if (line.charAt(0) == '\t')		// bottom out, but no further
								doc.delete_char(i, 0);
							else
							{
								int t = tabSize;
								int j = 0;
								// eat a tabs worth of spaces
								while ((line.charAt(j++) == ' ') && (t-- > 0))
									doc.delete_char(i, 0);
							}
						}
					}
				}
			}
		);


		// ==============================================================
		// ecm
		// --------------------------------------------------------------
		//	+	cursor-select-forward
		addToDict( new
			AbstractAction("cursor-select-forward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
					if (ecolumn < doc.getLine(fileEndLine).length()) {
						ccolumn = ecolumn+1;
						clickVisualLine = fileToVisualLine(fileEndLine);
					} else {
						ccolumn = 0;
						int lastLine = doc.getLineCount()-1;
						clickVisualLine = fileToVisualLine((fileEndLine < lastLine) ? fileEndLine+1 : lastLine);
					}
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-backward
		addToDict( new
			AbstractAction("cursor-select-backward") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
					if (ecolumn > 0) {
						ccolumn = ecolumn-1;
						clickVisualLine = fileToVisualLine(fileEndLine);
					} else {
						int temp = (fileEndLine>0) ? fileEndLine - 1 : 0;
						clickVisualLine = fileToVisualLine( temp );
						ccolumn = doc.getLine(temp).length();
					}
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-up
		addToDict( new
			AbstractAction("cursor-select-up") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
					clickVisualLine = fileToVisualLine((fileEndLine > 0) ? fileEndLine-1 : 0);
					ccolumn = ecolumn;
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-down
		addToDict( new
			AbstractAction("cursor-select-down") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
					int lastLine = doc.getLineCount()-1;
					clickVisualLine = fileToVisualLine((fileEndLine < lastLine) ? fileEndLine+1 : lastLine);
					ccolumn = ecolumn;
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-word-backward
		addToDict( new
			AbstractAction("cursor-select-word-backward") {
				{
                }

				public void actionPerformed( ActionEvent e ) {
        			String whole = null;
					if (!eactive) {
            			clickVisualLine = fileToVisualLine(fileStartLine);
        			    ccolumn = column;
            			whole = doc.getLine(fileStartLine);
            			int max = whole.length();
            			while (true)
            			{
            				if (column >= max)
            					break;

            				if (!Character.isLetterOrDigit(whole.charAt(column)))
            					break;

            				column++;
            			}
            			cpix = ruler.length(fileStartLine, column) + EDGE;
                        startHighlight(1, false);
                    }

                    int temp = fileEndLine;
					if (ecolumn > 0) {
						ccolumn = ecolumn-1;
						clickVisualLine = fileToVisualLine(fileEndLine);
					} else {
						temp = (fileEndLine > 0) ? fileEndLine-1 : 0;
						clickVisualLine = fileToVisualLine( temp );
						ccolumn = doc.getLine(temp).length();
					}
        			whole = doc.getLine(temp);
        			while (true)
        			{
                        if (ccolumn == 0)
        					break;

        				if (!Character.isLetterOrDigit(whole.charAt(ccolumn-1)))
        					break;

        				ccolumn--;
        			}
        			cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
        			moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-word-forward
		addToDict( new
			AbstractAction("cursor-select-word-forward") {
				{
                }

				public void actionPerformed( ActionEvent e ) {
        			String whole = null;
					if (!eactive) {
            			clickVisualLine = fileToVisualLine(fileStartLine);
        			    ccolumn = column;
            			whole = doc.getLine(fileStartLine);
            			while (true)
            			{
            				if (column == 0)
            					break;

            				if (!Character.isLetterOrDigit(whole.charAt(column-1)))
            					break;

            				column--;
            			}
            			cpix = ruler.length(fileStartLine, column) + EDGE;
                        startHighlight(1, false);
                    }

                    int temp = fileEndLine;
					if (ecolumn < doc.getLine(fileEndLine).length()) {
						ccolumn = ecolumn+1;
						clickVisualLine = fileToVisualLine(fileEndLine);
					} else {
						ccolumn = 0;
						int lastLine = doc.getLineCount()-1;
						temp = (fileEndLine < lastLine) ? fileEndLine+1 : lastLine;
						clickVisualLine = fileToVisualLine( temp );
					}
        			whole = doc.getLine(temp);
        			int max = whole.length();
        			while (true)
        			{
        				if (ccolumn >= max)
        					break;

        				if (!Character.isLetterOrDigit(whole.charAt(ccolumn)))
        					break;

        				ccolumn++;
        			}
        			cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
        			moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-document-begin
		addToDict( new
			AbstractAction("cursor-select-document-begin") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
					ccolumn = 0;
					clickVisualLine = 0;
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-document-end
		addToDict( new
			AbstractAction("cursor-select-document-end") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
                    clickVisualLine = fileToVisualLine(doc.getLineCount()-1);
                    ccolumn = doc.getLine(fileEndLine).length();
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-line-begin
		addToDict( new
			AbstractAction("cursor-select-line-begin") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
                    clickVisualLine = fileToVisualLine(fileEndLine);
                    ccolumn = 0;
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// --------------------------------------------------------------
		//	+	cursor-select-line-end
		// --------------------------------------------------------------
		addToDict( new
			AbstractAction("cursor-select-line-end") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
					if (!eactive) {
						clickVisualLine = fileToVisualLine(fileStartLine);
						ccolumn = column;
						cpix = ruler.length(fileStartLine, ccolumn) + EDGE;
						startHighlight(1, false);
					}
                    clickVisualLine = fileToVisualLine(fileEndLine);
                    ccolumn = doc.getLine(fileEndLine).length();
					cpix = ruler.length(visualToFileLine(clickVisualLine), ccolumn) + EDGE;
					moreHighlight();
				}
		}
		);
		// end ecm
		// ==============================================================
    	addToDict( new
    	    AbstractAction( "next-error" ) {
    	        {
    	        }
    	        public void actionPerformed(ActionEvent e ) {
    	            doc.nextError();
    	        }
    	    }
        );

    	addToDict( new
    	    AbstractAction( "prev-error" ) {
    	        {
    	        }

    	        public void actionPerformed(ActionEvent e ) {
    	            doc.prevError();
    	        }
    	    }
        );

	}

/* Action template

		addToDict( new
			AbstractAction("") {
				{
				}

				public void actionPerformed( ActionEvent e ) {
				}
			}
		);

*/

	AbstractAction undoAction = new
		AbstractAction("undo") {
			{
			}

			public void actionPerformed( ActionEvent e ) {
				doc.undo();
			}
	};

	AbstractAction redoAction = new
		AbstractAction("redo") {
			{
			}

			public void actionPerformed(ActionEvent e ) {
				doc.redo();
			}
	};


	AbstractAction copyAction;
	AbstractAction cutAction;

	void _deleteLine()
	{
		if ( doc.getLineCount() == 1 )
		{
			doc.clear_line(0);
			column = 0;
			pix = EDGE;
		}
		else if ( doc.getLineCount()-1 == fileStartLine )
		{
			doc.clear_line(fileStartLine);
			fileStartLine--;
		}
		else
		{
			doc.delete_line(fileStartLine);
		}
		cursorAdjust(true);
	}


	void _wordForward()
	{
		boolean skippedWord = false;
		char [] buf = doc.getLine(fileStartLine).toCharArray();

		if (buf.length == 0)	// failed on empty lines
			return;

		while ( column < buf.length )
		{
			if ( (buf[column] != ' ') && (buf[column] != '\t') )
			{
				if ( skippedWord )
				{
					break;
				}
			}
			else
			{
				skippedWord = true;
			}
			column++;
		}
	}

	void _wordBackward()
	{
		boolean skippedSpace = false;
		char [] buf = doc.getLine(fileStartLine).toCharArray();

		if (buf.length == 0)	// failed on empty lines
			return;

		column = Math.min(column, buf.length-1);
		if ( (buf[column] != ' ') && (buf[column] != '\t') )
			column--;
		while ( column >= 0 )
		{
			if ( (buf[column] == ' ') || (buf[column] == '\t') )
			{
				if ( skippedSpace )
				{
					column++;
					break;
				}
			}
			else
			{
				skippedSpace = true;
			}
			column--;
		}
	}

	void _pressedDelete()
	{
		String s;
		int max;

		if (hactive)
		{
			copy(true,true);
			return;
		}
		s = doc.getLine(fileStartLine);
		max = s.length();
		if (column < max)
		{
			doc.delete_char(fileStartLine,column);
			return;
		}
		else
			if (fileStartLine+1 < doc.getLineCount())
		{
			doc.join_line(fileStartLine,column);
			repaint();
			return;
		}
	}

	void _pressedBackspace()
	{
		String s;
		int max;

		if (hactive)
		{
			copy(true,true);
			return;
		}
		if (column > 0)
		{
			column--;
			doc.delete_char(fileStartLine,column);
			 // JJ 1998.01.03  - "pix" must be updated with "column"
			pix = ruler.length(fileStartLine,column) + EDGE;
			shiftHoriz(column); //pix);
			return;
		}
		else
			if (fileStartLine > 0)
		{
			fileStartLine--;
			s = doc.getLine(fileStartLine);
			column = s.length();
			shiftHoriz(ruler.length(fileStartLine,10000000) + EDGE);
			doc.join_line(fileStartLine,column);
			shiftVert(fileStartLine);
			repaint();
			return;
		}
	}

	void _insertNewline()
	{
		String s;
		int max;

		if (hactive)
			copy(true,false);
		doc.split_line(fileStartLine,column);
		shiftVert(++fileStartLine);
		column = 0;
		if ( autoIndent )
		{
			int i = 0;
			String prevline = doc.getLine(fileStartLine-1);
			char ch;
			while ( (i < prevline.length()) &&
				( ((ch = prevline.charAt(i)) == ' ') ||
				(ch == '\t') ) )
			{
				doc.insert_char(fileStartLine,column,ch);
				column++;
				i++;
			}
			 // JJ 1998.01.03  - "pix" must be updated with "column"
			pix = ruler.length(fileStartLine,column) + EDGE;
			shiftHoriz(pix);
		}
		else
		{
			pix = EDGE;
			shiftHoriz(0);
		}
		repaint();
		return;
	}

	private String[] readonlyActions =
	{
		"line-swap",
		"line-delete",
		"line-clone",
		"character-delete-forward",
		"character-delete-backward",
		"replace-dialog",
		"selection-cut",
		"buffer-paste",
		"line-break",
		"document-save"
	};

	/**
	 * Disables/enables all actions which modify text
	 * This method does not check if readOnly status have changed - you
	 * should check this before and do not call this if not needed
	 */
	public void setReadOnly(boolean readOnly) {
		int max = readonlyActions.length;
		int i;
		for ( i=0; i < max; i++ ) {
			getAction(readonlyActions[i]).setEnabled(!readOnly);
		}
	}

}


// the Ruler is used to measure pixel lengths of strings

class Ruler {

	private ChiselDoc	doc;
	private FontMetrics fontMetrics;
	private int			tabSize = 4;

	private final int LINE_MAX = 2000;

	private char before[];
	private char after[];

	private boolean hasTabs;

	private int beforeMax,afterMax;
	private int lineLast;

	public Ruler(ChiselDoc doc)
	{
		this.doc = doc;
		lineLast = -1;
	}

	// public methods are synchronized to guard lineLast and the buffers

	public synchronized void setFontMetrics(FontMetrics fm)
	{
		if (fm == fontMetrics)
			return;

		fontMetrics = fm;
		lineLast = -1;
	}

	public synchronized void setTabSize(int ts)
	{
		if (ts == tabSize)
			return;

		tabSize = ts;
		lineLast = -1;
	}

	public synchronized void invalidate(int first, int last)
	{
		if ((lineLast >= first) && (lineLast <= last))
		lineLast = -1;
	}

	private void fillBuffers(int line_no)
	{
		char c;
		int i,j,tabs,max;

		before = doc.getLineInfo(line_no).getCharArray(); //toCharArray();

		max = before.length;
		for (j=i=0; i<max; i++)
			if (before[i] == '\t')
				j++;

		if (j == 0)
		{
		    hasTabs = false;
			after = before;
			j = max;
		}
		else
		{
		    hasTabs = true;
			after = new char[max + (j * (tabSize-1))];

			for (j=i=0; i<max; i++)
			{
				c = before[i];
				if (c == '\t')
				{
					tabs = tabSize - (j % tabSize);
					while (tabs-- > 0) after[j++] = ' ';
				}
				else
				{
					after[j++] = c;
				}
			}
		}

		lineLast = line_no;
		beforeMax = before.length;
		afterMax = j;

	}

	public synchronized int length(int line, int column)
	{
		int i,j;
		int uColumn,tabs,temp;
		char c;
		boolean readline;
		readline = line != lineLast;
		if (fontMetrics == null)
		    return -1;

		if (column == 0)
		    return 0;

		if (readline)
		    fillBuffers(line);

		if (hasTabs)
		{
			uColumn = 1000000;

			for (i=j=0; i<beforeMax; i++)
			{
				if (i == column)
				{
					uColumn = j;
					break;
				}

				c = before[i];
				if (c == '\t')
				{
					tabs = tabSize - (j % tabSize);
					while (tabs-- > 0) j++;
				}
				else
				{
					j++;
				}
			}

			afterMax = j;
			lineLast = line;
		}
		else
		{
			uColumn = column;
		}
		if (uColumn > afterMax) {
			uColumn = afterMax;
		}
		return fontMetrics.charsWidth(after,0,uColumn);
	}

	public synchronized Position position(int line, int actualLine, int x)
	{
		int i,j;
		int temp,diff,odiff;
		int tabs;
		char c;
		boolean readline,looking;

		readline = line != lineLast;

		if (fontMetrics == null)
		    return new Position(0,0);

		if (readline)
		    fillBuffers(actualLine);

		temp = 0;
		diff = x;

		for (i=j=0; i<beforeMax; i++)
		{
		    c = before[i];
		    if (c == '\t')
		    {
			    tabs = tabSize - (j % tabSize);
			    while (tabs-- > 0) j++;
		    }
		    else
		    {
			    j++;
		    }

			odiff = diff;
			temp = fontMetrics.charsWidth(after,0,j);
			diff = Math.abs(x - temp);

			if (diff >= odiff)
				break;
		}

		return new Position(line, i);
	}
}

