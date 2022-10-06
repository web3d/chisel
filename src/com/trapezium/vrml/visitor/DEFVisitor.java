/*
 * @(#)DEFVisitor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.node.DEFUSENode;
import java.util.Vector;

public class DEFVisitor extends Visitor {
    Vector defList;
    Vector nodeList;

    public DEFVisitor() {
        super( null );
        defList = null;
    }

    public boolean saveDEF( DEFUSENode dun ) {
        if ( dun.isDEF() ) {
            if ( defList == null ) {
                defList = new Vector();
                nodeList = new Vector();
            }
            defList.addElement( dun.getDEFName() );
            nodeList.addElement( dun );
            return( true );
        } else {
            return( false );
        }
    }

    public boolean visitObject( Object a ) {
        if ( a instanceof DEFUSENode ) {
            return( saveDEF( (DEFUSENode)a ));
        } else {
            return( true );
        }
    }

    public int getNumberDEFs() {
        if ( defList == null ) {
            return( 0 );
        } else {
            return( defList.size() );
        }
    }

    public String getDEF( int offset ) {
        return( (String)defList.elementAt( offset ));
    }
    
    public DEFUSENode getDEFnode( int offset ) {
        return( (DEFUSENode)nodeList.elementAt( offset));
    }
}
