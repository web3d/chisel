/*
 * @(#)ChiselTable.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;
import java.awt.event.*;
import java.awt.*;
import com.trapezium.chisel.gui.ProgressBar;
import com.trapezium.chisel.gui.TableRowComponent;
import com.trapezium.chisel.gui.ChiselController;
import com.trapezium.util.ProgressIndicator;

/**
 *  Information display window for Chisel.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */
public class ChiselTable extends Table implements ProgressIndicator {

    static final Insets cellInsets = new Insets(2, 2, 3, 2);

    boolean rowDisabledByUser = false;
    boolean greenable = false;
    
    int baseState;
    int updatedState;

    public ChiselTable(int numcolumns, int numrows, ChiselRowListener listener) {
        super(numcolumns, numrows, cellInsets, listener);
        baseState = 0;
        updatedState = 0;
    }
    
    public void updateState( int row, boolean stateVal ) {
        int x = (1 << row);
        if ( stateVal ) {
            updatedState |= x;
        } else {
            updatedState &= ~x;
        }
    }
    
    public boolean stateChanged() {
//        System.out.println( "updated state " + updatedState + ", baseState " + baseState );
        return( updatedState != baseState );
    }
    
    public void setBaseState() {
        baseState = updatedState;
 //       System.out.println( "base state set to " + baseState );
    }
    
    public void clearBaseState() {
        baseState = 0;
    }
    
    public void setGreenable( boolean val ) {
        greenable = val;
    }
    
    public boolean isGreenable() {
        //return( true ); // 
        return( greenable );
    }

    public void rowDisabled() {
        rowDisabledByUser = true;
    }

    public boolean anyRowDisabled() {
        return( rowDisabledByUser );
    }

    public void resetRowDisabled() {
        rowDisabledByUser = false;
    }



    /** ProgressIndicator interface, set the percent */
    public void setPercent( int percent ) {
        ProgressBar pb = getProgressBar();
        if (pb != null) {
            pb.setPercent( percent );
        }
    }
    
    public void setAlternatePercent( int percent ) {
        ProgressBar pb = getProgressBar();
        if ( pb != null ) {
            pb.setAlternatePercent( percent );
        }
    }

    /** ProgressIndicator interface, reset the ProgressBar */
    public void reset() {
        ProgressBar pb = getProgressBar();
        if (pb != null) {
            pb.setPercent(-1);
        }
    }

    /** ProgressIndicator interface, set text that goes with percent */
    public void setTitle( String title ) {
        ProgressBar pb = getProgressBar();
        if (pb != null) {
            pb.setTitle( title );
        }
    }

    /** ProgressIndicator interface, Set the text of the ProgressBar */
    public void setText( String text ) {
        ProgressBar pb = getProgressBar();
        if (pb != null) {
            pb.setText( text );
            pb.pushText( text );
        }
    }
    
    public String getTitle() {
        ProgressBar pb = getProgressBar();
        if ( pb != null ) {
            return( pb.getTitle() );
        } else {
            return( null );
        }
    }
    
    /** ProgressIndicator interface, set the color of the ProgressBar */
    public void setColor( Color color ) {
        ProgressBar pb = getProgressBar();
        if (pb != null) {
            pb.setColor( color );
        }
    }
    
    public Color getColor() {
        ProgressBar pb = getProgressBar();
        if ( pb != null ) {
            return( pb.getColor() );
        } else {
            return( null );
        }
    }

    private ProgressBar getProgressBar() {
        Component c = getHeader().getColumn(1);
        if (c instanceof ProgressBar) {
            return (ProgressBar) c;
        } else {
            return null;
        }
    }

    public TableRow createHeader(int columns, Insets insets, Object data) {
        TableRow header = new TableRow(2, insets, true);
        TableRowComponent rowcomp = (TableRowComponent) header.getComponent();
        // this sets a fixed width for the left hand column
        rowcomp.setColumnWidth(0, 96);
        return header;
    }

    public TableRow createRow(int rownum, int columns, Insets insets, Object data) {
        ChiselRowListener listener = (ChiselRowListener) data;
        TableRow row = new ChiselRow(columns, insets, listener, this);
        TableRowComponent rowcomp = (TableRowComponent) row.getComponent();
        // this sets the starting width for the left hand column
        rowcomp.setColumnWidth(0, 172);
        // this lets column 0 widen when more space is available
        // (column 0 is fixed width, and the other columns are variable width by default)
        rowcomp.setColumnVaries(0, true);
        return row;
    }

    public void empty() {
        int num = getNumberRows();
	 	for ( int i = 0; i < num; i++ ) {
    		ChiselRow row = (ChiselRow) getRow( i );
    		row.setText("");
        }
    }
    
    public void resetAllRows() {
        int num = getNumberRows();
        for ( int i = 0; i < num; i++ ) {
            ChiselRow row = (ChiselRow) getRow( i );
            ChiselController cc = row.getChiselController();
            cc.setValue( "false" );
            row.setEnabled( false );
        }
        baseState = 0;
    }
}
