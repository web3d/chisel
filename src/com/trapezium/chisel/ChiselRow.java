/*
 * @(#)ChiselRow.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */

package com.trapezium.chisel;

import com.trapezium.chisel.gui.LabelConstants;
import com.trapezium.chisel.gui.TextLabel;
import com.trapezium.chisel.gui.ChiselController;
import com.trapezium.chisel.gui.LabelledImageButton;
import com.trapezium.chisel.gui.ChiselReporter;
import com.trapezium.chisel.gui.TableRowComponent;
import com.trapezium.factory.FactoryResponseListener;
import com.trapezium.factory.FactoryData;
import com.trapezium.factory.ParserFactory;
import com.trapezium.factory.FactoryChain;
import com.trapezium.parse.TokenEnumerator;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Font;
import java.io.File;

/**
 *  Handles GUI interface to a row in a chisel table.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */
public class ChiselRow extends TableRow implements LabelConstants, FactoryResponseListener, ActionListener, RowState {
    static Color greenGray = new Color(153, 204, 153);

    boolean enabled;    // user-set state
	ChiselDescriptor theChisel;
    ChiselOptionTable optionTable;
    ChiselTable owner;
    ChiselRowListener listener;
    
    boolean automaticallyChecked = false;

    /** global row number */
    int rowNumber;
    static int rowCounter = 1;

	public ChiselRow(int columns, Insets insets, ChiselRowListener listener, ChiselTable owner) {
	    super(columns, insets);
	    this.owner = owner;
	    this.listener = listener;
	    rowNumber = rowCounter;
	    rowCounter++;

	    if (listener == null) {
	        //setEnabled(false);
	        return;
	    }

        ChiselController controller = new ChiselController(this);
		setColumn(0, controller);

		for (int i = 1; i < columns; i++) {
		    setColumn(i, new ChiselReporter());
		}
		getChiselReporter(0).setForeground(Color.darkGray);
    }

   	public void actionPerformed( ActionEvent e ) {
   	    Object s = e.getSource();
   	    if ( s instanceof LabelledImageButton ) {
   	        LabelledImageButton lib = (LabelledImageButton)s;
   	        setEnabled( lib.getBooleanValue() );
   	        if ( !lib.getBooleanValue() ) {
   	            owner.rowDisabled();
   	        }
   	    }
    }

    public boolean isAutomaticallyChecked() {
        return( automaticallyChecked );
    }
    
	public void setChisel( ChiselDescriptor chisel, boolean autocheck ) {
	    automaticallyChecked = autocheck;
		theChisel = chisel;
		if (chisel != null) {
    		getChiselController().setLabel(new TextLabel(chisel.getShortDescription()));
    		if ( chisel.getInitialValue() instanceof String ) {
    		    String s = (String)chisel.getInitialValue();
    		    if ( s.compareTo( "true" ) == 0 ) {
            		setEnabled( true );
            	}
            }
    		getChiselController().setValue( String.valueOf(chisel.getInitialValue()) );
            if (optionTable != null) {
                removeFromExpansion(optionTable.getComponent());
            }
            optionTable = new ChiselOptionTable(chisel);
            addToExpansion(optionTable.getComponent());
            setExpandible(chisel.getNumberOptions() > 0);
        }
	}

	public ChiselDescriptor getChisel() {
		return theChisel;
	}

	public ChiselTable getOwner() {
		return owner;
	}

    public ChiselController getChiselController() {
        return (ChiselController) getColumn(0);
    }

    public ChiselReporter getChiselReporter(int n) {
        return (ChiselReporter) getColumn(n + 1);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (listener != null) {
            listener.rowStateChanged(this);
        }
    }

	// The following are the FactoryResponseListener interface
	public void done( FactoryData result ) {
	    owner.reset(); //setPercent( -1 );
	}

	/** replacement for the obsolete 'state' */
	public void rowReady() {
	    ChiselController controller = getChiselController();
	    controller.setColor( Color.lightGray );
	}

	public void rowRunning() {
        ChiselController controller = getChiselController();
        controller.setColor( Color.green );
    }

    public void rowDone() {
        ChiselController controller = getChiselController();
        controller.setColor( greenGray );
    }

	public void update( FactoryData result ) {
	}

	public void setNumberOfLines( int n ) {
	}

	public void setLinePercent( int percent ) {
	    int main = percent/10;
	    int remainder = percent%10;
	    owner.setPercent( main );
	}

    long fileLength = 0;
    public void setFileLength( long flen ) {
        fileLength = flen;
        TokenEnumerator.presetLength = flen;
    }

	public void setPolygonCount( int n ) {
	    if ( n == 0 ) {
	        owner.setText( "No polygons." );
	    } else if ( n == 1 ) {
	        owner.setText( "1 polygon." );
	    } else {
	        owner.setText( n + " polygons." );
	    }
	}

	public void setText( String s ) {
        ChiselReporter resultReporter = getChiselReporter(0);
        resultReporter.setLabelText( s );
	}

	public void setAction( String action ) {
	    owner.setTitle( action );
	}

	public void setFactoryData( FactoryData fd ) {
	}

    static final Insets optionCellInsets = new Insets(2, 2, 2, 2);
	class ChiselOptionTable extends Table implements ActionListener {

	    public ChiselOptionTable(ChiselDescriptor chisel) {
	        super(2, chisel.getNumberOptions(), optionCellInsets);
            getComponent().setBackground(inBetween(DEFAULT_TABLECOLOR, DEFAULT_TABLECOLOR.brighter()));
            int n = chisel.getNumberOptions();
            int maxwidth = 200;
            TableRow row;
            for (int i = 0; i < n; i++) {
                row = getRow(i);
    	        row.setColumnWidth(0, 32);
    	        Component ed = chisel.getOptionEditor(i, this);
    	        row.setColumn(1, ed);
    	        int edwidth = ed.getPreferredSize().width;
    	        if (edwidth > maxwidth) {
    	            maxwidth = edwidth;
    	        }
    	    }

            for (int i = 0; i < n; i++) {
                row = getRow(i);
    	        row.setColumnWidth(1, maxwidth);
                ((TableRowComponent) row.getComponent()).setColumnVaries(1, true);
    	    }
	    }
        /** by returning null from createHeader we make this a headerless table */
        public TableRow createHeader(int columns, Insets insets, Object data) {
            return null;
        }

        Color inBetween(Color c1, Color c2) {
            return new Color( (c1.getRed() + c2.getRed())/2,  (c1.getGreen() + c2.getGreen())/2, (c1.getBlue() + c2.getBlue())/2 );
        }
       	public void actionPerformed( ActionEvent e ) {
/*       	    Object s = e.getSource();
       	    if ( s instanceof LabelledImageButton ) {
       	        LabelledImageButton lib = (LabelledImageButton)s;
       	        ChiselController chiselController = getChiselController();
       	        setEnabled( lib.getValue() );
       	    }*/
        }
    }
}
