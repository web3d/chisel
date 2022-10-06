/*
 * @(#)MultipleTokenElement.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Scene graph component representing a range of tokens.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998, added "adjust" method
 *  @version         1.0, 25 Nov 1997
 *
 *  @since           1.0
 */
public class MultipleTokenElement extends SingleTokenElement {
    int lastTokenOffset = -1;

    public MultipleTokenElement( int firstTokenOffset ) {
        super( firstTokenOffset );
    }

    public void setLastTokenOffset( int lastTokenOffset ) {
        this.lastTokenOffset = lastTokenOffset;
    }
    
    public int getLastTokenOffset() {
        return( lastTokenOffset );
    }
    
    public boolean isTraversable() {
        return( true );
    }

    /** Adjust the token offset if greater than or equal to boundary */
    public void adjust( int boundary, int amount ) {
        super.adjust( boundary, amount );
        if ( lastTokenOffset >= boundary ) {
            lastTokenOffset += amount;
        } 
    }
    
    /** Get the text of the element as a String */
    public String getText() {
        Scene s = (Scene)getScene();
        if ( s != null ) {
            TokenEnumerator te = s.getTokenEnumerator();
            if ( te != null ) {
                StringBuffer sb = new StringBuffer();
                int scanner = getFirstTokenOffset();
                int end = getLastTokenOffset();
                te.setState( scanner );
                while ( scanner <= end ) {
                    sb.append( te.toString( scanner ));
                    sb.append( " " );
                    scanner = te.getNextToken();
                }
                return( new String( sb ));
            }
        }
        return( null );
    }
}
