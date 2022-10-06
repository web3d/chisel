/*  ChiselAWTViewer
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import com.trapezium.chisel.ProcessedFileViewer;
import com.trapezium.chisel.ProcessedFile;
import com.trapezium.chisel.ChiselResources;
import com.trapezium.chisel.ChiselProperties;
import com.trapezium.chisel.ChiselSet;
import com.trapezium.chisel.Chisel;
import com.trapezium.chisel.TokenPrinter;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.edit.TokenEditor;

import com.trapezium.factory.FactoryResponseListener;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.node.humanoid.HumanoidVisitor;
import com.trapezium.edit.EditLintVisitor;

import com.trapezium.edit.Document;


public class ChiselAWTViewer extends ChiselAWTPane implements ProcessedFileViewer, DirtyMarker {
    ProcessedFile data;
    Component editor;
    TitleBar titlebar;

    Clicker clicker;

    /** true if the contents of the viewer need to be reparsed */
    boolean dirty = true;   // provoke initial parse


    /** true if maximized */
    boolean maximized = false;

    /** true if minimized */
    boolean minimized = false;

    /** stores the non-maximized, non-minimized bounds of the component */
    Rectangle normalRect = null;


    public static final String untitledString = "Untitled";

    /** for use by mouse trackers.  Assumes only one component is responding
        to mouse events at any one time. */
    private static int anchorx, anchory;

    WorkspaceListener workspaceListener = null;

    public ChiselAWTViewer() {
        super(FRAMED_BORDER);
        setBackground(DEFAULT_FRAMECOLOR);
        setLayout(null);

        titlebar = new TitleBar(untitledString);
        add(titlebar);

        editor = createEditor();
        editor.setFont(new Font("Courier", Font.PLAIN, 12));
        add(editor);

        BorderSizer sizer = new BorderSizer(this);
        addMouseListener(sizer);
        addMouseMotionListener(sizer);

        clicker = new Clicker(this);
        addMouseListener(clicker);
        editor.addMouseListener(clicker);
        titlebar.addMouseListener(clicker);
    }

    public String getTitle() {
        return titlebar.getText();
    }

    public void setTitle(String title) {
        if (dirty) {
            title = title + '*';
        }
        titlebar.setText(title);
    }

    public void setViewerVersion( int versionNo ) {
        String title = getTitle();
        if ( dirty && ( title.indexOf( "*" ) == -1 )) {
            title = title + "*";
        }
        if (( title.indexOf( "v" ) == 0 ) && ( title.indexOf( ":" ) > 0 )) {
            title = title.substring( title.indexOf( ":" ) + 1 );
        }
        titlebar.setText( "v" + versionNo + ": " + title );
    }


    // DirtyMarker interface
    public void markDirty() {
        if ( !dirty ) {
            dirty = true;
            String titleText = titlebar.getText();
            if ( titleText.indexOf( "*" ) == -1 ) {
                titlebar.setText( titleText + "*" );
            }
        }
    }

    // DirtyMarker interface
    public void markClean() {
        if ( dirty ) {
            dirty = false;
            String titleText = titlebar.getText();
            int starIdx = titleText.indexOf( "*" );
            if ( starIdx != -1 ) {
                titlebar.setText( titleText.substring( 0, titleText.length() - 1 ));
            }
        }
    }

    /** highlight the title bar */
    public void setActivated(boolean activated) {
        titlebar.setActivated(activated);
    }

    ChiselAWTPane getWorkspace() {
        for (Container container = getParent(); container != null; container = container.getParent()) {
            if (container instanceof ChiselAWTPane) {
                return (ChiselAWTPane) container;
            }
        }
        return null;
    }


    public synchronized void addWorkspaceListener(WorkspaceListener l) {
        //workspaceListener = AWTEventMulticaster.add(workspaceListener, l);
        workspaceListener = l;
    }

    public synchronized void removeWorkspaceListener(WorkspaceListener l) {
        //workspaceListener = AWTEventMulticaster.remove(workspaceListener, l);
        if (workspaceListener == l) {
            workspaceListener = null;
        }
    }


    protected void processWorkspaceEvent(WorkspaceEvent e) {
        if (workspaceListener != null) {
            switch(e.getID()) {
              case WindowEvent.WINDOW_OPENED:
                workspaceListener.windowOpened(e);
                ((SceneTextPane)editor).activate();
                break;
              case WindowEvent.WINDOW_CLOSING:
                workspaceListener.windowClosing(e);
                break;
              case WindowEvent.WINDOW_CLOSED:
                workspaceListener.windowClosed(e);
                break;
              case WindowEvent.WINDOW_ICONIFIED:
                workspaceListener.windowIconified(e);
                break;
              case WindowEvent.WINDOW_DEICONIFIED:
                workspaceListener.windowDeiconified(e);
                break;
              case WindowEvent.WINDOW_ACTIVATED:
                workspaceListener.windowActivated(e);
                ((SceneTextPane)editor).activate();
                break;
              case WindowEvent.WINDOW_DEACTIVATED:
                workspaceListener.windowDeactivated(e);
                break;
              case WorkspaceEvent.WINDOW_MAXIMIZED:
                workspaceListener.windowMaximized(e);
                break;
              case WorkspaceEvent.WINDOW_DEMAXIMIZED:
                workspaceListener.windowDemaximized(e);
                break;
              default:
                break;
            }
        }
    }

    synchronized void postWorkspaceEvent(int id) {
        //if (workspaceListener != null) {
        //    WorkspaceEvent e = new WorkspaceEvent(getComponent(), id);
        //    Toolkit.getEventQueue().postEvent(e);
        //}

        processWorkspaceEvent(new WorkspaceEvent(this, id));
    }

    class Clicker extends MouseAdapter {
        ChiselAWTViewer viewer;

        public Clicker(ChiselAWTViewer viewer) {
            super();
            this.viewer = viewer;
        }
        public void mousePressed(MouseEvent evt) {
            viewer.select();
        }
    }

    public void show() {
	    super.show();
        postWorkspaceEvent(WindowEvent.WINDOW_OPENED);
    }

    public void select() {
        ChiselAWTPane pane = getWorkspace();
        if (pane != null) {
            pane.setSelection(this);
            postWorkspaceEvent(WindowEvent.WINDOW_ACTIVATED);
        }
    }

    //
    // ------------------- minimize and maximize --------------------
    //
    // With both maximize and minimize, we reposition and redraw
    // the window every time they are called, but we post events only
    // when the state changes.  By executing the reposition/redraw
    // logic every time, these methods can be used by containers to
    // readjust the window's position after resizing, etc.
    //

    public boolean isMaximized() {
        return maximized;
    }

    public void maximize() {
        maximize(!maximized);
    }

    public void maximize(boolean max) {
        ChiselAWTPane pane = getWorkspace();
        if (pane != null) {
            if (max) {
                if (!isMinimized() && !isMaximized()) {
                    normalRect = getBounds();
                }
                pane.moveToFront(this);
                Rectangle rect = pane.getParent().getBounds();
                Rectangle holderRect = getParent().getBounds();
                Insets insets = getEditorInsets();
                rect.x = -insets.left - holderRect.x;
                rect.y = -insets.top - holderRect.y;
                rect.width += insets.left + insets.right;
                rect.height += insets.top + insets.bottom;
                if ((holderRect.x + holderRect.width < rect.x + rect.width) || (holderRect.y + holderRect.height < rect.y + rect.height)) {
                    holderRect.width = rect.x + rect.width - holderRect.x;
                    holderRect.height = rect.y + rect.height - holderRect.y;
                    getParent().setBounds(holderRect);
                }
                setBounds(rect);

            } else {
                if (isMinimized()) {
                    minimize(true);
                } else {
                    if (normalRect == null) {
                        normalRect = new Rectangle(10, 10, 200, 200);
                    }
                    setBounds(normalRect);
                }
            }
        }
        if (max != maximized) {
            maximized = max;
            validate();
            postWorkspaceEvent(max ? WorkspaceEvent.WINDOW_MAXIMIZED : WorkspaceEvent.WINDOW_DEMAXIMIZED);
        }
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void minimize() {
        minimize(!minimized);
    }

    public void minimize(boolean min) {
        ChiselAWTPane pane = getWorkspace();
        if (pane != null) {
            if (min) {
                if (!isMinimized() && !isMaximized()) {
                    normalRect = getBounds();
                }
                // set location to minimized
            } else {
                if (isMaximized()) {
                    maximize(true);
                } else {
                    if (normalRect == null) {
                        normalRect = new Rectangle(10, 10, 200, 200);
                    }
                    setBounds(normalRect);
                }
            }
        }
        if (min != minimized) {
            minimized = min;
            postWorkspaceEvent(min ? WindowEvent.WINDOW_ICONIFIED : WindowEvent.WINDOW_DEICONIFIED);
        }
    }

    public void close() {
        //System.out.println("ChiselAWTViewer.close");
        postWorkspaceEvent(WindowEvent.WINDOW_CLOSED);
    }

    /** update the display if the object belongs to this viewer or is null */
    public void fileUpdated( ProcessedFile data ) {
        System.out.println( "I don't think this is called" );
    }

    public void setErrorMarks( BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
        ((SceneTextPane)editor).setErrorMarks( errorMarks, warningMarks, nonconformanceMarks );
    	    editor.invalidate();
    	    invalidate();
    	    validate();
    }
    
    public void xfileUpdated(ProcessedFile data) {

        if (data == null || data == this.data) {

            // create editor if necessary
            if (editor == null) {
                editor = createEditor();
                if (editor == null) {
                    System.err.println("Unable to create editor");
                    return;
                }
            }
            if (data != null) {
                data.setDocument( ((SceneTextPane)editor).getDocument() );
                titlebar.setText(data.getName());
                Scene s = data.getScene();
                if ( s != null ) {
                    TokenEnumerator te = data.getLineSource();
   				    s.setTokenEnumerator( te );
   				    ((SceneTextPane)editor).setScrollValues();
   				    ((SceneTextPane)editor).setScrollIncrements();

            		if ( data.hasLintInfo() ) {
            		    ProgressIndicator owner = ChiselSet.getProgressIndicator( ChiselSet.VALIDATORS );
            		    owner.reset();
            		    owner.setText( data.getErrorSummaryString() );
		            }
    			}
            }
            Chisel.disableTextWindowPaint = false;
    	    editor.invalidate();
    	    invalidate();
    	    validate();
        }
    }

    /** load a new file into the viewer */
    public void fileDone(ProcessedFile data) {
        this.data = data;
        xfileUpdated(data);
    }

    /** save the current document.  Prompts for filename if the document was not loaded
        from disk or previously saved. */
        /** Do we need this anymore??? */
    public void save() {
        if (data != null) {
//           data.save();
        } else {
            System.out.println("Cannot save; viewer is empty");
        }
    }

    /** remove the current document from the viewer and display an empty document */
    public void empty() {
        fileDone(null);
    }

    public void paste() {
        ((SceneTextPane)editor).paste();
    }

    public void cut() {
        ((SceneTextPane)editor).cut();
    }

    public void copy() {
        ((SceneTextPane)editor).copy();
    }

    public void undo() {
        ((SceneTextPane)editor).undo();
    }

    public void redo() {
        ((SceneTextPane)editor).redo();
    }

    public void nextError() {
        ((SceneTextPane)editor).nextError();
    }

    public void prevError() {
        ((SceneTextPane)editor).prevError();
    }


    /** get the ProcessedFile being viewed */
    public ProcessedFile getProcessedFile() {
        return data;
    }

    /** set the ProcessedFile being viewed */
    public void setProcessedFile(ProcessedFile data) {
        this.data = data;
        if (data != null) {
            titlebar.setText(data.getName());
            data.setDocument( ((SceneTextPane)editor).getDocument() );
        } else {
            titlebar.setText(untitledString);
        }
    }

    /** get the component embodying this viewer */
    public Component getComponent() {
        return this;
    }

    /** dump the current document to the console */
    public void dump() {
    }

    /**
     * Create an editor to represent the given document.
     */
    private Component createEditor() {
        SceneTextPane pane = new SceneTextPane( this );
        pane.hookScrollersTo(this);
        return pane;
  }

    static int sbwidth = Slider.WIDTH_VERTICAL;
    static int sbheight = Slider.HEIGHT_HORIZONTAL;
    public void doLayout() {
        if (editor != null) {
            Dimension size = getSize();
            Insets insets = getInsets();
            int tbheight = titlebar.getPreferredSize().height;
            titlebar.setBounds(insets.left, insets.top, size.width - insets.right - insets.left, tbheight);
            editor.setBounds(insets.left, insets.top + tbheight, size.width - insets.right - insets.left - sbwidth, size.height - insets.bottom - insets.top - tbheight - sbheight);
        }
    }

    public Insets getEditorInsets() {
        Insets insets = (Insets) getInsets().clone();
        insets.top += titlebar.getPreferredSize().height;
        return insets;
    }


	/** a control for displaying and editing scenes as text */
    class SceneTextComponent extends ChiselTextComponent implements MouseListener, MouseMotionListener {

        Scene scene = null;
        int topline = 0;
        int leftcol = 0;
        TokenEditor dataSource = null;

        DirtyMarker dirtyMarker;

        public SceneTextComponent( ChiselDoc doc, Adjustable horiz, Adjustable vert, DirtyMarker d ) {
			super(doc, horiz, vert);
	        dirtyMarker = d;
            setLayout(null);
            setBackground(DEFAULT_BGCOLOR);
            setForeground(DEFAULT_TEXTCOLOR);
			setCursor( new Cursor( Cursor.TEXT_CURSOR ));
		}

        public void setVersion( int version ) {
            setViewerVersion( version );
        }

        public void paste() {
            doAction( "buffer-paste" );
        }

        public void undo() {
            doAction( "undo" );
        }

        public void redo() {
            doAction( "redo" );
        }

        public void cut() {
            doAction( "selection-cut" );
		}

        public void copy() {
            doAction( "selection-copy" );
        }

        public void nextError() {
            doAction( "next-error" );
        }

        public void prevError() {
            doAction( "prev-error" );
        }

    }

    class SceneTextPane extends ScrollablePane {
        SceneTextComponent sceneTextComponent;
        Document doc;

        public SceneTextPane( DirtyMarker dirtyMarker ) {
            super();
            dirty = false;
            setLayout(null);

			ChiselDoc cd = new ChiselDoc(this, ChiselResources.getDefaultResourceBundle(), ChiselProperties.getProperties());
			doc = cd;
            sceneTextComponent = new SceneTextComponent(cd, (Adjustable) getHScrollComponent(), (Adjustable) getVScrollComponent(), dirtyMarker );

			add(sceneTextComponent);
            addMouseListener( sceneTextComponent );
            addComponentListener( sceneTextComponent );
 //           super.addMouseMotionListener( sceneTextComponent );
        }

        public Document getDocument() {
            return( doc );
        }

        public void addMouseListener(MouseListener ml) {
            super.addMouseListener(ml);
            if (ml != sceneTextComponent) { // TextComponents already listen to themselves
                sceneTextComponent.addMouseListener(ml);
            }
            getHScrollComponent().addMouseListener(ml);
            getVScrollComponent().addMouseListener(ml);
        }

        public void activate() {
            //sceneTextComponent.activate();
        }

        public void paste() {
            sceneTextComponent.paste();
        }

        public void cut() {
            sceneTextComponent.cut();
        }

        public void copy() {
            sceneTextComponent.copy();
        }

        public void undo() {
            sceneTextComponent.undo();
        }

        public void redo() {
            sceneTextComponent.redo();
        }

        public void nextError() {
            sceneTextComponent.nextError();
        }

        public void prevError() {
            sceneTextComponent.prevError();
        }


        public void doLayout() {
            Dimension size = getSize();
            Dimension pref = sceneTextComponent.getPreferredSize();

            pref.width = Math.max(pref.width, size.width);
            pref.height = Math.max(pref.height, size.height);
            sceneTextComponent.setBounds(0, 0, pref.width, pref.height);

            // this resizes the holder pane
            super.doLayout();

            setScrollIncrements();
        }

        public void setFont(Font font) {
            super.setFont(font);
            // set the text control's font explicitly so it can initialize
            // variables based on the font
            sceneTextComponent.setFont(font);

            // the scroll increments depend on the font
            setScrollIncrements();
        }

        public void setScrollIncrements() {
            // set increments according to font and visible size
            FontMetrics fm = getFontMetrics(getFont());
            int em = fm.charWidth('M');
            int ht = fm.getHeight();
            int numlines = (getSize().height + ht - 1)/ ht;
            setScrollIncrements(em, ht, 16 * em, (numlines - 1) * ht);
            // added "numlines/2" to allow scrolling past end, and to allow
            // for error lines in the display
            //sceneTextComponent.setScrollRatio( numlines, doc.getLineCount());
        }
    }
}

