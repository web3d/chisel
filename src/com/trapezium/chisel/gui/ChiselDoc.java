package com.trapezium.chisel.gui;
import com.trapezium.edit.Document;
import com.trapezium.edit.Lines;
import com.trapezium.edit.LineInfo;
import com.trapezium.edit.Position;
import com.trapezium.edit.Hilite;
import com.trapezium.edit.HiliteVRML;
import com.trapezium.chisel.Workspace;
import com.trapezium.chisel.WorkspaceChild;
import com.trapezium.chisel.ChiselProperties;
import com.trapezium.factory.DocumentLoader;
import com.trapezium.vrml.SelectNode;
import com.trapezium.vrml.NodeSelection;
import com.trapezium.util.GlobalProgressIndicator;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ChiselDoc implements Document {

	private Workspace workspace;

	private ResourceBundle	strings;
	private Properties	props;			// the user preferences
	private Hilite  	hilite;
	private Lines		lines;			// the document data
	private DocumentLoader  docLoader;  // undo-redo reloading of document

	private Container		container;	// the workspace child containing this document
	private ChiselTextComponent 	viewer;
	private File			file;		// the document file
	private JournalItem		undo_list;	// where we are in undo
	private JournalItem		redo_list;	// where we are in redo

	private	boolean dirty;
	private	boolean neverNamed;
	private	boolean privateProps;
	private boolean highlighting;
	private boolean readOnly;

	private String	lineSeparator;

	private final int REPLACE_LINE	= 1;
	private final int SPLIT_LINE	= 2;
	private final int JOIN_LINE		= 3;
	private final int INSERT		= 4;
	private final int DELETE		= 5;
	private final int SWAP_LINES	= 6;
	private final int DELETE_LINE	= 7;
	private final int MARK_VERSION  = 8;

	MenuItem undoItem;
	MenuItem redoItem;
	public void setUndoItem( MenuItem undoItem ) {
	    this.undoItem = undoItem;
	}

	public void setRedoItem( MenuItem redoItem ) {
	    this.redoItem = redoItem;
	}
	
	// for Node selection on double click
	SelectNode nodeSelector = null;
	public void setNodeSelector( SelectNode nodeSelector ) {
	    this.nodeSelector = nodeSelector;
	}

    /** Select a Node that encompasses NodeSelection range.
     *
     *  @param nodeSelection contains start/end location of selection
     *     if true returned contains start/end selection of node
     *  @return true if a Node was selected and nodeSelection parameter
     *    updated, otherwise returns false
     */
	public boolean selectNode( NodeSelection nodeSelection ) {
	    if ( nodeSelector != null ) {
	        return( nodeSelector.selectNode( nodeSelection ));
	    } else {
	        return( false );
	    }
	}
	
	// for unique undo/redo serializations
    static int docId = 0;
	public ChiselDoc(Container container, ResourceBundle str, Properties pr)
	{
        docId++;
		this.container = container.getParent();
		workspace = null;
		viewer = null;

		strings = str;
		props = pr;

		hilite = new Hilite(lines,0,true);

		lineSeparator = System.getProperty("line.separator");

		undo_list = redo_list = null;
	}

	int currentErrorLine = -1;
	public int getCurrentErrorLine() {
	    return( currentErrorLine );
	}

	/** Jump to the next error line */
	public void nextError() {
	    if ( viewer != null ) {
	        int topLine = currentErrorLine;
	        if ( currentErrorLine == -1 ) {
    	        topLine = viewer.getLowest();
    	    } else if ( !viewer.isVisible( topLine )) {
	            topLine = viewer.getFirstVisibleLine();
	        }
	        if ( topLine != -1 ) {
	            int eline = lines.getNextError( topLine );
	            if (( eline == -1 ) && !viewer.isVisible( currentErrorLine )) {
	                eline = lines.getPrevError( topLine );
	            }
	            if ( eline != -1 ) {
	                int next = lines.getNextError( eline );
	                int prev = lines.getPrevError( eline );
	                String l = lines.getErrorStatusString( eline, 0 );
	                if ( next == -1 ) {
	                    l = l + " (last error line)";
	                } else if ( prev == -1 ) {
	                    l = l + " (first error line)";
	                }
	                showLine( eline, l );
	                currentErrorLine = eline;
	            }
	        }
	    }
	}

	/** Jump to the previous error line */
	public void prevError() {
	    if ( viewer != null ) {
	        int topLine = currentErrorLine;
	        if ( currentErrorLine == -1 ) {
	            topLine = viewer.getLowest();
	        } else if ( !viewer.isVisible( topLine )) {
	            topLine = viewer.getLastVisibleLine();
	        }
	        if ( topLine != -1 ) {
	            int eline = lines.getPrevError( topLine );
	            if (( eline == -1 ) && !viewer.isVisible( currentErrorLine )) {
	                eline = lines.getNextError( topLine );
	            }
	            if ( eline != -1 ) {
	                int prev = lines.getPrevError( eline );
	                int next = lines.getNextError( eline );
	                String l = lines.getErrorStatusString( eline, 0 );
	                if ( prev == -1 ) {
	                    l = l + " (first error line)";
	                } else if ( next == -1 ) {
	                    l = l + " (last error line)";
	                }
	                showLine( eline, l );
	                currentErrorLine = eline;
	            }
	        }
	    }
	}

	public void dump( String filename ) {
	    lines.dumpLines( filename );
	}

	/** Set the Lines data source for this document */
	public void setLines( Lines lines ) {
	    this.lines = lines;
	    if ( lines != null ) {
    		hilite = new HiliteVRML(lines, getTabSize(), false);
    	}
	}

	public void setDocumentLoader( DocumentLoader docLoader ) {
	    this.docLoader = docLoader;
	}

    /** Set the display viewer */
	public void setViewer(ChiselTextComponent viewer) {
		this.viewer = viewer;
	}

	/** Viewers get automatic version saved after each chisel sequence.
	 *
	 *  @param versionNo 1 based version temporary file for serialized object
	 *  @param serObject the object to serialize
	 */
	public void setViewerVersion( int versionNo, java.io.Serializable serObject ) {
	    viewer.setVersion( versionNo );
		JournalItem new_journal = new JournalItem();
		new_journal.action = MARK_VERSION;
//		new_journal.line = versionNo;
		new_journal.next = undo_list;
		undo_list = new_journal;
		// can fail due to disk space problems, in which case undo is impossible
		new_journal.line = serializeFileVersion( versionNo, serObject );
	}

	public void setViewerVersion( int versionNo ) {
	    viewer.setVersion( versionNo );
	}

	/** Undo & redo use this to switch between versions */
	int lastVersionLoaded = -1;
	static boolean reloadRunning = false;
	public void reloadFileVersion( int versionNo ) {
	    if ( reloadRunning ) {
	        System.out.println( "reload in progress.." );
	    }
	    if (( versionNo != lastVersionLoaded ) && ( versionNo != -1 )) {
	        reloadRunning = true;
	        VersionLoaderThread vlt = new VersionLoaderThread( getSerializeFileName( versionNo ), versionNo);
	        vlt.start();
	    }
	}

    class VersionLoaderThread extends Thread {
        String fileName;
        int versionNo;
        VersionLoaderThread( String fileName, int versionNo ) {
            this.fileName = fileName;
            this.versionNo = versionNo;
        }
        
        public void run() {
	        System.out.println( "Reloading file version " + versionNo );
	        if ( reloadFileVersion( fileName )) {
    	        lastVersionLoaded = versionNo;
    	        setViewerVersion( versionNo );
    	        System.out.println( "Reload succeeded" );
    	    } else System.out.println( "Reload failed" );
            GlobalProgressIndicator.reset();
            reloadRunning = false;
        }
    }

	boolean reloadFileVersion( String fileName ) {
	    try {
	        FileInputStream fis = new FileInputStream( fileName );
	        GZIPInputStream gis = new GZIPInputStream( fis );
	        ObjectInput s = new ObjectInputStream( gis );
	        Object scene = s.readObject();
	        docLoader.reload( scene );
	        return( true );
	    } catch ( Exception e ) {
	        e.printStackTrace();
	        return( false );
	    }
	}
	
	/** Remove serialized undo files */
	public void removeSerialFiles( int highestVersion ) {
	    for ( int i = 1; i <= highestVersion; i++ ) {
	        removeSerialFile( i );
	    }
	}
	
	/** Remove a particular serialized file */
	void removeSerialFile( int version ) {
	    File f = new File( getSerializeFileName( version ));
	    f.delete();
	}
	

	/** Serialize a version of the file */
	int serializeFileVersion( int versionNo, java.io.Serializable serObject ) {
	    String serFileName = getSerializeFileName( versionNo );
//	    System.out.println( "Serializing version " + versionNo + " to " + serFileName );
	    try {
	        FileOutputStream fos = new FileOutputStream( serFileName );
	        GZIPOutputStream gos = new GZIPOutputStream( fos );
	        ObjectOutput s = new ObjectOutputStream( gos );
	        s.writeObject( serObject );
	        s.flush();
	        gos.close();
	        fos.close();
//	        System.out.println( "Serializing version finished" );
                File f = new File(serFileName); // Added 15 May 2006 MLo
                f.deleteOnExit(); // Added 15 May 2006 MLo
	        return( versionNo );
	    } catch ( Exception e ) {
	        e.printStackTrace();
	        return( -1 );
	    }
	}

	String getSerializeFileName( int versionNo ) {
	    return( new String( "ser_" + docId + "." + versionNo ));
	}

	public int getErrorCount( int lineNo ) {
	    return( lines.getErrorCount( lineNo ));
	}

    /** Get the String error message associated with a line.
     *
     *  @param lineNo the line to check for the error
     *  @param errorStringNo an offset 0 or greater, for case where a single
     *     line is associated with several errors.
     */
	public String getErrorViewerString( int lineNo, int errorStringNo ) {
	    return( lines.getErrorViewerString( lineNo, errorStringNo ));
	}
	
	public String getErrorStatusString( int lineNo, int errorStringNo ) {
	    return( lines.getErrorStatusString( lineNo, errorStringNo ));
	}

	private Workspace getWorkspace() {
	    if ( container == null ) {
	        return( null );
	    }
		return ((WorkspaceChild) container).getWorkspace();
	}

	/**
	 * Closes this document.  If the "bail" flag is true changes will
	 * be discarded, otherwise the user will be queried.
     * @param	bail Exit immediately, discarding changes.
	 */

	public void close(boolean bail)	{
		if (viewer != null) {
			getWorkspace().close(viewer);
		}
	}

	/**
	 * Returns the full path to this document.
     * @return	Document name (full path).
	 */
	public String getPathname()
	{
		return file.getPath();
	}

	/**
	 * Returns the filename (no path) for this document.
     * @return	Document name (no path).
	 */
	public String getFilename()
	{
		return file.getName();
	}

	/**
	 * Fetch the dirty flag.
     * @return	True if the file has been changed since being written.
	 */
	public boolean isDirty()
	{
		return dirty;
	}

	/**
	 *  Get the line count for a document.
	 */
	public int getLineCount()
	{
	    if ( lines == null ) {
	        return( 0 );
	    }
		return lines.size();
	}

    /** Show a particular line on the string, print error message on status line
     *
     *  @param line the line to show in the viewer
     *  @param estr the associated error message, to show on status line
     */
	public boolean showLine(int line, String estr ) {
		if (line < 0) {
			return false;

		} else {
		    if ( estr != null ) {
		        System.out.println( "Line " + line + ": " + estr );
		    } else {
                System.out.println( "Line: " + line );
            }
			viewer.showLine(line);
			Workspace w = getWorkspace();
			if ( w != null ) {
    			w.moveToFront(viewer);
    		}
			return true;
		}
	}


    /** 
     * Get the text of a line
     * @param line  The line to get
     * @return the String text of the line, or null if the line number invalid
     */
	public String getLine(int line) {
		if (line < 0) {
			return null;
		} else {
			return lines.getLine(line);
		}
	}

	/**
	 * Set the text for a line.
     * @param	line	The line to set.
     * @param	text	The text to set.
     * @return	Success.
	 */

	public boolean setLine(int line, String s) {

		if (line < 0) {
			return false;
		}

		// set the text

		lines.setLine(s,line);

		// update

		updateFrames(line, line);
		dirty = true;

		return true;
	}

	/**
	 * Insert a line of text before the TagLine.
     * @param	line	The line to set.
     * @param	text	The text to set.
     * @return	Success.
	 */

	public boolean insertLine(int line, String s) {

		if (line < 0) {
			return false;
		}

		// set the text

		lines.insertElementAt(s,line);

		// update

		updateFrames(line, line+1);
		dirty = true;

		return true;
	}

	/**
	 * Delete a line.
     * @param	line	The line to delete.
     * @return	Success.
	 */

	public boolean deleteLine(int line) {

		if (line < 0)
			return false;

		// set the text

		lines.removeElementAt(line);

		// update

		updateFrames(line, line+1);
		dirty = true;

		return true;
	}

//
// utility functions, manage frames and canvases attached to this document
//

	public boolean isBusy()
	{
		return dirty || !neverNamed;
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly( boolean aReadOnly )
	{
		if ( aReadOnly != readOnly )
		{
			readOnly = aReadOnly;
			viewer.setReadOnly(readOnly);
			System.out.println("ReadOnly " + readOnly);
		}
	}


    /** Clear out the undo and redo lists */
	public void clear_undo()
	{
		undo_list = redo_list = null;
	}

//
// utility functions, manage syntax highlighting
//

	public void extendHilite(int highest)
	{
	    return;
	}


//
// utility functions, update canvases following changes to document
//

	public void updateFrames(int first, int last)
	{
		int tlast = hilite.update(first,last,getHighest());
		if ( tlast > 0 ) {
		    last = tlast;
		}
		viewer.legalizeCursor();
		viewer.linesChanged(first, last);
	}

	private void legalizeCursors()
	{
		viewer.legalizeCursor();
	}

//
// utility function, get highest visible line in any canvas
//

	private int getHighest()
	{
		return viewer.getHighest();

	}

//
// utility functions, access properties for this document
//

	public String getProperty(String p)
	{
		return props.getProperty(p);
	}

	public void setProperty(String p, String v)
	{
		props.put(p,v);
	}

	public void setProperties(Properties p)
	{
		props = p;
		privateProps = false;
	}

	public void splitProperties()
	{
		if (!privateProps)
		{
			props = new Properties(props);
			privateProps = true;
		}
	}

	public void updateProperties(boolean global)
	{
		if (global) {
			// probably need something else here

			ChiselProperties.saveProperties();

		} else {
			applyProperties();
		}
	}

	public void applyProperties()
	{
	}

	public String getFontStyle()
	{
		String style = props.getProperty("font.style");

		switch (Integer.valueOf(style).intValue())
		{

		case Font.PLAIN:
			return strings.getString("ChoicePlain");
		case Font.BOLD:
			return strings.getString("ChoiceBold");
		case Font.ITALIC:
			return strings.getString("ChoiceItalic");
		case Font.BOLD | Font.ITALIC:
			return strings.getString("ChoiceBoldItalic");
		default:
			break;
		}

		return strings.getString("ChoicePlain");
	}

	public void setFontStyle(String style)
	{
		int s = Font.PLAIN;

		if (style.equals(strings.getString("ChoiceBold")))
			s = Font.BOLD;
		else
		if (style.equals(strings.getString("ChoiceItalic")))
			s = Font.ITALIC;
		else
		if (style.equals(strings.getString("ChoiceBoldItalic")))
			s = Font.BOLD | Font.ITALIC;

		props.put("font.style",String.valueOf(s));
	}

	public Font getFont()
	{
		String name, style, size;
		int i;

		name  = props.getProperty("font.name");     // add defaults
		style = props.getProperty("font.style");
		size  = props.getProperty("font.size");

		return new Font(name,
						Integer.valueOf(style).intValue(),
						Integer.valueOf(size).intValue());
	}

	public int getTabSize()
	{
		int tabs;

		try
		{
			tabs = Integer.valueOf(props.getProperty("tab.size")).intValue();
		}

		catch (NumberFormatException e)
		{
			props.put("tab.size","4");
			tabs = 4;
		}

		return tabs;
	}

//
// utility functions, access data in "lines" structure
//

	public LineInfo getLineInfo(int i)
	{
	    if ( dirty ) {
	        // get line info directly from highlighter
	        if ( hilite != null ) {
	            return( hilite.createLineInfo( i ));
	        } else {
    	        return lines.getLineInfo(i);
    	    }
	    } else {
    		return lines.getLineInfo(i);
    	}
	}

//
// utility functions, update menus to reflect undo/redo and copy/paste status
//

	public void updateUndoItems(boolean un,boolean re)
	{
	    if ( undoItem != null ) {
	        if ( un ) {
	            undoItem.setEnabled( true );
	        } else {
	            undoItem.setEnabled( false );
	        }
	    }
	    if ( redoItem != null ) {
	        if ( re ) {
	            redoItem.setEnabled( true );
	        } else {
	            redoItem.setEnabled( false );
	        }
	    }
	}

	private void updateMenus()
	{
		boolean got_undo;
		boolean got_redo;

		got_undo = undo_list != null;
		got_redo = redo_list != null;

		updateUndoItems(got_undo, got_redo);
	}

//
// The following routines implement high-level actions for undo, and redo
//
void showList( JournalItem list, String title ) {
    System.out.println( title + "_list" );
    int item = 1;
    while ( list != null ) {
        System.out.println( item + ": " + list.action );
        list = list.next;
    }
}
	public void undo()
	{
		int first,last;
		JournalItem temp;
		Position pos;

		if (undo_list != null)
		{
			temp = undo_list;
			undo_list = temp.next;
			first = last = temp.line;

			switch (temp.action)
			{
			case REPLACE_LINE:
				redo_line(temp);
				break;
			case SPLIT_LINE:
				split_or_join(temp,true);
				last++;
				break;
			case JOIN_LINE:
				split_or_join(temp,false);
				break;
			case INSERT:
				copy_or_cut(temp,true);
				break;
			case DELETE:
				pos = insert(temp);
				last = pos.line;
				break;
			case DELETE_LINE:
				insert(temp);
				last++;
				break;
			case MARK_VERSION:
			    if ( temp.line > 1 ) {
       			    reloadFileVersion( temp.line - 1 );
       			}
			    break;
			default:
				;
			}

            if ( temp.action != MARK_VERSION ) {
    			updateFrames(first,last);

	    		pos = new Position(temp.line,temp.column);
	    	}

			legalizeCursors();

			temp.next = redo_list;
			redo_list = temp;
		}

		updateMenus();
	}

	public void redo()
	{
		int first,last;
		JournalItem temp;
		Position pos;
		int line, column;

		if (redo_list != null)
		{
			temp = redo_list;
			redo_list = temp.next;
			first = last = line = temp.line;
			column = temp.column;

			switch (temp.action)
			{
			case REPLACE_LINE:
				column = temp.text.length();
				redo_line(temp);
				break;
			case SPLIT_LINE:
				split_or_join(temp,false);
				column = 0;
				break;
			case JOIN_LINE:
				split_or_join(temp,true);
				last++;
				break;
			case INSERT:
				pos = insert(temp);
				last = pos.line;
				line = temp.eline;
				column = temp.ecolumn;
				break;
			case DELETE:
				copy_or_cut(temp,true);
				break;
			case DELETE_LINE:
				lines.removeElementAt(temp.line);
				break;
			case MARK_VERSION:
			    reloadFileVersion( temp.line );
			    break;
			default:
				;
			}

			updateFrames(first,last);

			pos = new Position(line,column);
			//textFrame.setPos(tp);

			legalizeCursors();

			temp.next = undo_list;
			undo_list = temp;
		}

		updateMenus();
	}

//
// The following routines implement low-level actions for do, undo, and redo
//

	public void insert_char(int line, int column, char c)
	{
		String  s;
		s = lines.getLine(line);
		remember_line(line,column,s);
		if ( column == s.length() ) {
		    s += c;
		} else {
    		s = s.substring(0, column) + c + s.substring(column, s.length());
    	}
		lines.setLine(s,line);
		updateFrames(line,line);
		dirty = true;
	}

	public void delete_char(int line, int column)
	{
		String s;
		s = lines.getLine(line);
		remember_line(line,column,s);
		s = s.substring(0, column) + s.substring(column+1, s.length());
		lines.setLine(s,line);
		updateFrames(line,line);
		dirty = true;
	}

	private void remember_line(int line, int column, String s)
	{
		boolean new_line = true;

		if (undo_list != null)
			new_line = (undo_list.action != REPLACE_LINE) || (undo_list.line != line);

		if (new_line)
		{
			JournalItem new_journal = new JournalItem();
			new_journal.action = REPLACE_LINE;
			new_journal.line = line;
			new_journal.column = column;
			new_journal.text = new String(s);
			new_journal.next = undo_list;
			undo_list = new_journal;
		}

		redo_list = null;
		updateMenus();
	}

	private void redo_line(JournalItem i)
	{
		String s;
		int line = i.line;
		s = lines.getLine(line);
		lines.setLine(i.text,line);
		i.text = s;
	}

	public void split_line(int line, int column)
	{
//	    System.out.println( "split line " + line + " at " + column );
		JournalItem new_journal = new JournalItem();
		new_journal.action = SPLIT_LINE;
		new_journal.line = line;
		new_journal.column = column;
		split_or_join(new_journal,false);
		new_journal.next = undo_list;
		undo_list = new_journal;
		redo_list = null;
		updateMenus();
		updateFrames(line,line+1);
		dirty = true;
	}

	public void join_line(int line, int column)
	{
		JournalItem new_journal = new JournalItem();
		new_journal.action = JOIN_LINE;
		new_journal.line = line;
		new_journal.column = column;
		split_or_join(new_journal,true);
		new_journal.next = undo_list;
		undo_list = new_journal;
		redo_list = null;
		updateMenus();
		updateFrames(line,line+1);
		dirty = true;
	}

	public void split_or_join(JournalItem i, boolean join)
	{
		String s;
		int line = i.line;
		int column = i.column;

		if (join)
		{
			s = lines.getLine(line);
			s = s.concat(lines.getLine(line+1));
			lines.setLine(s,line);
			lines.removeElementAt(line+1);
			hilite.lineRemoved(line+1);
			// chisel bug 271, repaint on join undo
			updateFrames( line, line + viewer.getHighest() );
		}
		else
		{
		    lines.split_line( line, column );
		}
	}

	public Position insert_section(int line, int column, String s, boolean update)
	{
		int charCt;
		int saveCt;
		int charMax;
		char c,c2;
		String s2 = null;
		Position pos;

		// start creating journal item

		JournalItem new_journal = new JournalItem();
		new_journal.action = INSERT;
		new_journal.text = new String(s);
		new_journal.line = line;		// starting line and column
		new_journal.column = column;

		// insert the text

		pos = insert(new_journal);

		// finish creating journal entry

		new_journal.eline = pos.line;		// updated line and column
		new_journal.ecolumn = pos.column;
		new_journal.next = undo_list;
		undo_list = new_journal;
		redo_list = null;
		updateMenus();
		if (update)
			updateFrames(line, pos.line);
		dirty = true;

		return pos;
	}

	public Position insert(JournalItem i)
	{
		int line = i.line;
		int column = i.column;
		String s = i.text;

		int charCt;
		int saveCt;
		int charMax;
		char c,c2;
		String s2 = null;

		// insert the text

		charMax = s.length();
		charCt = saveCt = 0;

		while (charCt < charMax)
		{
			c = s.charAt(charCt);
			charCt++;
			if ((c == '\r') || (c == '\n'))
			{
				s2 = lines.getLine(line);
				lines.setLine(s2.substring(0,column) + s.substring(saveCt,charCt-1),line);
				lines.insertElementAt(s2.substring(column,s2.length()),++line);
				column = 0;
				if (charCt < charMax)
				{
					c2 = s.charAt(charCt);
					if (((c == '\r') && (c2 == '\n')) || ((c2 == '\r') && (c == '\n')))
						charCt++;
				}
				saveCt = charCt;
			}
		}

		if (saveCt < charCt)
		{
			s2 = lines.getLine(line);
			s = s.substring(saveCt,charCt);
			if (column == 0)
				s2 = s + s2;
			else
				s2 = s2.substring(0, column) + s + s2.substring(column, s2.length());
			column += s.length();
			lines.setLine(s2,line);
		}
		return new Position(line,column);
	}

	public String delete_section(int line, int column, int eline, int ecolumn, boolean cut)
	{
		String text;

		JournalItem new_journal = new JournalItem();
		new_journal.action = DELETE;
		new_journal.line = line;
		new_journal.column = column;
		new_journal.eline = eline;
		new_journal.ecolumn = ecolumn;

		text = copy_or_cut(new_journal,cut);

		if (cut)
		{
			new_journal.text = new String(text);
			new_journal.next = undo_list;
			undo_list = new_journal;
			redo_list = null;
			updateMenus();
			dirty = true;
		}

		return text;
	}

	private String copy_or_cut(JournalItem i, boolean cut)
	{
		String s,s2;
		String text = null;

		int line = i.line;
		int column = i.column;
		int eline = i.eline;
		int ecolumn = i.ecolumn;

		if (line == eline)
		{
			s = lines.getLine(line);
			text = s.substring(column,ecolumn);
			if (cut)
			{
				s = s.substring(0, column) + s.substring(ecolumn, s.length());
				lines.setLine(s,line);
			}
		}
		else
		{
			s = lines.getLine(line);
			text = s.substring(column,s.length());
			s = s.substring(0, column);

			int diff = eline - line;
			int k = line + 1;

			for (int j = 1; j < diff; j++)
			{
				s2 = lines.getLine(k);
				text = text + lineSeparator + s2;
				if (cut) {
					lines.removeElementAt(k);
					hilite.lineRemoved(k);
				}
				else
					k++;
			}

			if (k != lines.size())
    		{
				s2 = lines.getLine(k);
				text = text + lineSeparator + s2.substring(0,ecolumn);
				s = s + s2.substring(ecolumn,s2.length());
				if (cut) {
					lines.removeElementAt(k);
					hilite.lineRemoved(k);
				}
			}
			if (cut)
				lines.setLine(s,line);
		}
		return text;
	}

	public String delete_line( int line )
	{
		JournalItem ji = new JournalItem();
		ji.action = DELETE_LINE;
		ji.line = line;
		ji.column = 0;
		ji.text = lines.getLine(line);
		ji.ecolumn = ji.text.length();
		ji.text += '\n';
		ji.next = undo_list;
		undo_list = ji;
		redo_list = null;

		lines.removeElementAt(line);
		updateMenus();
		updateFrames(line, line);

		return ji.text;
	}

	public String clear_line( int line )
	{
		return delete_section(line, 0, line, lines.getLine(line).length(), true);
	}

	public void insert_line( int after, String txt )
	{
		insert_section(after, 0, txt + '\n', true);
	}
}

//
// JournalItem is used in the redo/undo queues to track changes to a document
//

class JournalItem
{
	public JournalItem		next;
	public String			text;
	public int				action;
	public int				line;
	public int				column;
	public int				eline;
	public int				ecolumn;
}
