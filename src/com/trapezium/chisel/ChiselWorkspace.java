/*  ChiselWorkspace
 *
 */

package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.trapezium.factory.FactoryResponseListener;
import com.trapezium.factory.FactoryData;
import com.trapezium.chisel.gui.ScrollablePane;
import com.trapezium.chisel.gui.WorkspaceListener;
import com.trapezium.chisel.gui.ChiselAWTViewer;


/** A ChiselWorkspace holds an arbitrary number of ChiselViewers */
public class ChiselWorkspace extends ChiselPane implements FactoryResponseListener {

    ViewerVector viewers;
    int selectedViewer;

    int xOpen = 10;
    int yOpen = 10;
    int dxOpen = 16;
    int dyOpen = 28;
    int widthOpen = 320;
    int heightOpen = 400;

    // listen for window events
    WorkspaceListener workspaceListener;
    // listener to chain to
    WorkspaceListener nextListener = null;

    // the interface to the container
    Workspace workspace;

    ChiselTableStack chiselTableStack;
    public ChiselWorkspace(ChiselTableStack tables) {
        super();
        this.chiselTableStack = tables;

        // get the workspace interface.  This doesn't work with
        // JFC.
        workspace = new WorkspacePane();
        setComponent((Component) workspace);

        viewers = new ViewerVector();
        getComponent().setBackground(DEFAULT_WORKSPACECOLOR);
        selectedViewer = -1;
        workspaceListener = new ChiselWorkspaceListener();
    }

    public void setErrorMarks( ProcessedFile pf, BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
        ProcessedFileViewer pfv = getViewer( pf );
        if ( pfv != null ) {
            Component component = pfv.getComponent();
            if ( component instanceof ChiselAWTViewer ) {
                ((ChiselAWTViewer)component).setErrorMarks( errorMarks, warningMarks, nonconformanceMarks );
//            pfv.setErrorMarks( errorMarks, warningMarks, nonconformanceMarks );
            }
        }
    }

    public void paste() {
        if ( workspace != null ) {
            workspace.paste();
        }
    }

    public void cut() {
        if ( workspace != null ) {
            workspace.cut();
        }
    }

    public void copy() {
        if ( workspace != null ) {
            workspace.copy();
        }
    }

    public void undo() {
        if ( workspace != null ) {
            workspace.undo();
        }
    }

    public void redo() {
        if ( workspace != null ) {
            workspace.redo();
        }
    }

    public void nextError() {
        if ( workspace != null ) {
            workspace.nextError();
        }
    }

    public void prevError() {
        if ( workspace != null ) {
            workspace.prevError();
        }
    }

    public void setSelection(ProcessedFile file) {
        setSelection(viewers.findComponentFor(file));
    }

    public void setSelection(String url) {
        setSelection(viewers.findComponentFor(url));
    }

    public void setSelection(Component c) {
        int index = viewers.findIndexFor(c);
        if (index != selectedViewer) {
            Component old;
            if (selectedViewer >= 0 && selectedViewer < viewers.size()) {
                old = ((ProcessedFileViewer) viewers.elementAt(selectedViewer)).getComponent();
            } else {
                old = null;
            }
            if (old != null && old != c && old instanceof ChiselAWTViewer) {
                ((ChiselAWTViewer)old).setActivated(false);
            }
            workspace.setSelection(c);
            if (c != null && c != old && c instanceof ChiselAWTViewer) {
                ((ChiselAWTViewer)c).setActivated(true);
            }
            selectedViewer = index;
        }
    }

    public Component getSelection() {
        return workspace.getSelection(); //selectedViewer >= 0 ? ((ProcessedFileViewer) viewers.elementAt(selectedViewer)).getComponent() : null;
    }

    public void close(Component c) {
        int index = viewers.findIndexFor( c );
        viewers.removeElementAt(index);
        workspace.close(c);
        if (index == selectedViewer) {
            Component sel = workspace.getSelection();
            selectedViewer = viewers.findIndexFor(sel);
            if ( sel != null && sel instanceof ChiselAWTViewer ) {
                ((ChiselAWTViewer)sel).setActivated(true);
            }
            chiselTableStack.close();
//            chiselTableStack.updateHeaderLine();
        } 
    }

    public void moveToFront(Component c) {
        workspace.moveToFront(c);
    }

    public void moveToBack(Component c) {
        workspace.moveToBack(c);
    }


    class WorkspacePane extends ScrollablePane {

        public WorkspacePane() {
            super();
            setLayout(null);
            setScrollMode(DYNAMIC);
        }

        public void doLayout() {
            super.doLayout();
            boolean max = false;
            Dimension size = getSize();
            Enumeration elements = viewers.elements();
            while (elements.hasMoreElements()) {
                ProcessedFileViewer v = (ProcessedFileViewer) elements.nextElement();
                Component c = v.getComponent();
                if (c instanceof ChiselAWTViewer && ((ChiselAWTViewer) c).isMaximized()) {
                    ((ChiselAWTViewer) c).maximize(true);
                }
            }
        }

        // override to disable scroll when a child is maximized
        public void setScrollValues() {
            boolean enable = true;
            if (getComponentCount() > 0 && viewers != null) {
                Enumeration elements = viewers.elements();
                while (elements.hasMoreElements()) {
                    ProcessedFileViewer v = (ProcessedFileViewer) elements.nextElement();
                    Component c = v.getComponent();
                    if (c instanceof ChiselAWTViewer && ((ChiselAWTViewer) c).isMaximized()) {
                        enable = false;
                        break;
                    }
                }

            } else {
                enable = false;
            }
            enableScroll(enable, enable);
            super.setScrollValues();
        }
    }

    class ChiselWorkspaceListener implements WorkspaceListener {
        public void windowOpened(WindowEvent evt) {
            //System.out.println("CWL gets windowOpened");
            if (nextListener != null) {
                //System.out.println("OK!!!");
                nextListener.windowOpened(evt);
            }
        }
        public void windowClosing(WindowEvent evt) {
            //System.out.println("CWL gets windowClosing");
            if (nextListener != null) {
                //System.out.println("OK!!!");
                nextListener.windowClosing(evt);
            }
        }
        public void windowClosed(WindowEvent evt) {
            //System.out.println("CWL gets windowClosed");
            close(evt.getComponent());
            if (nextListener != null) {
                nextListener.windowClosed(evt);
            }
        }
        public void windowIconified(WindowEvent evt) {
            //System.out.println("CWL gets windowIconified");
            if (nextListener != null) {
                nextListener.windowIconified(evt);
            }
        }
        public void windowDeiconified(WindowEvent evt) {
            //System.out.println("CWL gets windowDeiconified");
            if (nextListener != null) {
                nextListener.windowDeiconified(evt);
            }
        }
        public void windowActivated(WindowEvent evt) {
            //System.out.println("CWL gets windowActivated");
            if (nextListener != null) {
                nextListener.windowActivated(evt);
            }
        }
        public void windowDeactivated(WindowEvent evt) {
            //System.out.println("CWL gets windowDeactivated");
            if (nextListener != null) {
                nextListener.windowDeactivated(evt);
            }
        }
        public void windowMaximized(WindowEvent evt) {
            //System.out.println("CWL gets windowMaximized");
            if (nextListener != null) {
                nextListener.windowMaximized(evt);
            }
        }
        public void windowDemaximized(WindowEvent evt) {
            //System.out.println("CWL gets windowDemaximized");
            if (nextListener != null) {
                nextListener.windowDemaximized(evt);
            }
        }
    }


    public synchronized void addWorkspaceListener(WorkspaceListener l) {
        //workspaceListener = AWTEventMulticaster.add(workspaceListener, l);
        nextListener = l;
    }

    /** determine if a file is currently being viewed */
    public boolean contains(ProcessedFile data) {
        Enumeration elements = viewers.elements();
        while (elements.hasMoreElements()) {
            ProcessedFileViewer v = (ProcessedFileViewer) elements.nextElement();
            if (v.getProcessedFile() == data) {
                return true;
            }
        }
        return false;
	}

    public void open(ProcessedFile data) {
        ProcessedFileViewer v = new ChiselViewer();
        v.setProcessedFile(data);
        add(v);
        //data.setDoneListener( this );
        setSelection(v.getComponent());
    }


    protected void setNextViewerBounds(Component viewer) {
        Dimension size = getComponent().getSize();
        if (xOpen > size.width - dxOpen) {
           xOpen = 10;
        }
        if (yOpen > size.height - dyOpen) {
           yOpen = 10;
        }
        viewer.setBounds(xOpen, yOpen, widthOpen, heightOpen);
        xOpen += dxOpen;
        yOpen += dyOpen;
    }


	public void add(ProcessedFileViewer viewer) {
	    //selectedViewer = viewers.size();
        viewers.addElement(viewer);
	    Container container = getContainer();
	    if (container != null) {
	        Component viewerComponent = viewer.getComponent();
            setNextViewerBounds(viewerComponent);
	        container.add(viewerComponent);
	        //viewerComponent.addMouseListener(clicker);

            if (workspaceListener != null && viewerComponent instanceof ChiselAWTViewer) {
                ((ChiselAWTViewer) viewerComponent).addWorkspaceListener(workspaceListener);
            }

            // without this for some reason the left and right edges don't show up
	        viewerComponent.repaint();
	    }
    }

	public void remove(ProcessedFileViewer viewer) {
        viewers.removeElement(viewer);
	    Container container = getContainer();
	    if (container != null) {
    	    container.remove(viewer.getComponent());
    	}
    }

    /** update the display if the object belongs to this viewer or is null */
    public void fileUpdated(ProcessedFile data) {
        Enumeration elements = viewers.elements();
        while (elements.hasMoreElements()) {
            ProcessedFileViewer v = (ProcessedFileViewer) elements.nextElement();
            v.fileUpdated(data);
        }
    }

    /** load an object into the viewer */
    public void done(FactoryData data) {
//        System.out.println("WORKSPACE DONE!");
        Enumeration elements = viewers.elements();
        while (elements.hasMoreElements()) {
            ProcessedFileViewer v = (ProcessedFileViewer) elements.nextElement();
            if (v.getProcessedFile().getId() == data.getId()) {
                v.fileDone(v.getProcessedFile());
//                System.out.println("WS calls v.fileDone!");
            }
        }
	}

	public ProcessedFileViewer getViewer( ProcessedFile pf ) {
	    Enumeration elements = viewers.elements();
	    while ( elements.hasMoreElements() ) {
	        ProcessedFileViewer v = (ProcessedFileViewer) elements.nextElement();
	        if ( v.getProcessedFile().getId() == pf.getId() ) {
	            return( v );
	        }
	    }
	    return( null );
	}

	public void update( FactoryData result ) {}
	public void setNumberOfLines( int n ) {}
	public void setPolygonCount( int n ) {}
	public void setAction( String action ) {}
	public void setText( String text ) {}
	public void setFactoryData( FactoryData fd ) {}
	public void setLinePercent( int n ) {}

	/** save the current document.  Prompts for filename if the document was not loaded
        from disk or previously saved. */
    public void save() {
    }

    /** remove the current document from the viewer and display an empty document */
    public void empty() {
    }

    /** get the ProcessedFile being viewed */
    public ProcessedFile getProcessedFile() {
        if (selectedViewer >= 0) {
            return ((ProcessedFileViewer) viewers.elementAt(selectedViewer)).getProcessedFile();
        } else {
            return null;
        }
    }

    /** set the ProcessedFile being viewed */
    public void setProcessedFile(ProcessedFile data) {
        if (selectedViewer >= 0) {
            ((ProcessedFileViewer) viewers.elementAt(selectedViewer)).setProcessedFile(data);
        }
    }

    /** get the ProcessedFile being viewed */
    public ProcessedFileViewer getActiveViewer() {
        if (selectedViewer >= 0) {
            return (ProcessedFileViewer) viewers.elementAt(selectedViewer);
        } else {
            return null;
        }
    }


    /** The default layout behavior for a workspace leaves all the windows where they are. */
    public void layoutContainer(Container target) {
    }


    /** dump the current document to the console */
    public void dump() {
        System.out.println("ChiselWorkspace containing " + viewers.size() + " viewers, #" + selectedViewer + " selected.");
    }
}

class ViewerVector extends Vector {
    public boolean containsViewer(ProcessedFileViewer viewer) {
        if (contains(viewer)) {
            return true;
        } else if (viewer != viewer.getComponent() && contains(viewer.getComponent())) {
            return true;
        }
        return false;
    }
    public void removeViewer(ProcessedFileViewer viewer) {
        Component comp = viewer.getComponent();
        if (viewer == comp) {
            viewer = findViewerFor(comp);
        }
        removeElement(viewer);
    }

    public int findIndexFor(Component comp) {
        int num = size();
        for (int index = 0; index < num; index++) {
            ProcessedFileViewer viewer = (ProcessedFileViewer) elementAt(index);
            if (viewer.getComponent() == comp) {
                return index;
            }
        }
        return -1;
    }

    public ProcessedFileViewer findViewerFor(Component comp) {
        Enumeration elements = elements();
        while (elements.hasMoreElements()) {
            ProcessedFileViewer viewer = (ProcessedFileViewer) elements.nextElement();
            if (viewer.getComponent() == comp) {
                return viewer;
            }
        }
        return null;
    }
    public Component findComponentFor(ProcessedFile data) {
        Enumeration elements = elements();
        while (elements.hasMoreElements()) {
            ProcessedFileViewer viewer = (ProcessedFileViewer) elements.nextElement();
            if (viewer != null && viewer.getProcessedFile() == data) {
                return viewer.getComponent();
            }
        }
        return null;
    }

    /** return the first component viewing a particular file, or null if
      * not found.
      */
    public Component findComponentFor(String url) {
        Enumeration elements = elements();
        while (elements.hasMoreElements()) {
            ProcessedFileViewer viewer = (ProcessedFileViewer) elements.nextElement();
            ProcessedFile file = viewer.getProcessedFile();
            if (file != null && file.getUrl().equals(url)) {
                return viewer.getComponent();
            }
        }
        return null;
    }
}

