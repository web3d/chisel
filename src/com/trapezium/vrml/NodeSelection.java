package com.trapezium.vrml;

/** Used along with Scene.selectNode to select a Node based on a smaller
 *  selection within the text.  This is a global singleton object, so
 *  only one Node can be selected this way at any given time.
 */
public class NodeSelection {
    /** Locaton of selection int he text */
    public int startLine = -1;
    public int endLine = -1;
    public int startColumn = -1;
    public int endColumn = -1;

    /** Node fields */
    public int firstNodeToken = -1;
    public int lastNodeToken = -1;

    /** Singleton pattern for this object */
    static NodeSelection singleton = null;
    static public NodeSelection getSingleton() {
        if ( singleton == null ) {
            singleton = new NodeSelection();
        }
        return( singleton );
    }
    
    public NodeSelection() {
        reset();
    }
    
    public void reset() {
        startLine = -1;
        endLine = -1;
        startColumn = -1;
        endColumn = -1;
        firstNodeToken = -1;
        lastNodeToken = -1;
    }

    public void set( int sLine, int eLine, int sColumn, int eColumn ) {
        startLine = sLine;
        endLine = eLine;
        startColumn = sColumn;
        endColumn = eColumn;
    }
    
    public boolean hasSelection() {
        return( startLine != -1 );
    }
}
