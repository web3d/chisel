package com.trapezium.edit;

import java.awt.*;
import com.trapezium.factory.DocumentLoader;
import com.trapezium.vrml.SelectNode;
import com.trapezium.vrml.NodeSelection;

public interface Document {
    public void setLines( Lines lines );
    public void setDocumentLoader( DocumentLoader docLoader );
    public void setNodeSelector( SelectNode sn );
    public boolean selectNode( NodeSelection nodeSelection );
    
    public void removeSerialFiles( int highestVersion );


    //public void rescan();

	/**
	 * Closes this document.  If the "bail" flag is true changes will
	 * be discarded, otherwise the user will be queried.
     * @param	bail Exit immediately, discarding changes.
	 */
	public void close(boolean bail);

	/**
	 * Writes out this document.
	 */
	//public void save();

	/**
	 * Writes out this document.
     * @param	pathname New document name (full path).
	 */
	//public void saveAs(String pathname);

	/**
	 * Reloads the existing document.
     * @param	filename of document to be reloaded (full path).
	 */
	//public void reload(String filename);

	/**
	 * Returns the full path to this document.
     * @return	Document name (full path).
	 */
	public String getPathname();

	/**
	 * Returns the filename (no path) for this document.
     * @return	Document name (no path).
	 */
	public String getFilename();

	/**
	 * Fetch the dirty flag.
     * @return	True if the file has been changed since being written.
	 */
	public boolean isDirty();

	/**
	 *  Get the line count for a document.
     * @return	The line count.
	 */
	public int getLineCount();

	/**
	 * Bring forward any view window and scroll to the desired line.
     * @param	line	The line to display.
     * @return	Success.
	 */
	public boolean showLine(int line, String estr);

	/**
	 * Get the text for a line.
     * @param	line 	The line to get.
     * @return	The text for the line (null if TagLine not found).
	 */
	public String getLine(int line);

	/**
	 * Set the text for a line.
     * @param	line	The line to set.
     * @param	text	The text to set.
     * @return	Success.
	 */
	public boolean setLine(int line, String text);

	/**
	 * Insert a line of text before the TagLine.
     * @param	tag	The line to set.
     * @param	text	The text to set.
     * @return	Success.
	 */
	public boolean insertLine(int line, String text);

	/**
	 * Delete a line.
     * @param	line	The line to delete.
     * @return	Success.
	 */
	public boolean deleteLine(int line);

	/** Set the viewer version */
	public void setViewerVersion( int version, java.io.Serializable object );
	public void setViewerVersion( int version );

	/** Pass context controlled MenuItems to doc */
	public void setUndoItem( MenuItem undoItem );
	public void setRedoItem( MenuItem redoItem );

	/** invalidate undo-redo list */
	public void clear_undo();
}
