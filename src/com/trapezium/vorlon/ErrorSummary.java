/*
 * @(#)ErrorSummary.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vorlon;
import com.trapezium.util.ReturnInteger;
import java.util.Hashtable;
import java.util.Enumeration;

public class ErrorSummary implements java.io.Serializable {
    int warningLimit;
    int warningCount;
    Hashtable warningTable;
    
    /** Class constructor
     *
     *  @param warningLimit maximum number of warnings this object allows
     *     (restriction implemented by calling "countWarning" method)
     */
    public ErrorSummary( int warningLimit ) {
        this.warningLimit = warningLimit;
        warningCount = 0;
        warningTable = new Hashtable();
    }
    
    public int getWarningLimit() {
        return( warningLimit );
    }
    
    /** Count a warning of a particular type, increment total warning count.
     *
     *  @param warningStr type of warning to count
     *  @return true if the warning limit has not been reached, otherwise false
     */
    public boolean countWarning( String warningStr ) {
        warningCount++;
        ReturnInteger ri = (ReturnInteger)warningTable.get( warningStr );
        if ( ri == null ) {
            warningTable.put( warningStr, new ReturnInteger( 1 ));
        } else {
            ri.incValue();
        }
        return( warningCount < warningLimit );
    }
    
    /** Get the count associated with a particular String. */
    public int getCount( String s ) {
        Enumeration e = warningTable.keys();
        while ( e.hasMoreElements() ) {
            String test = (String)e.nextElement();
            if ( test.indexOf( s ) >= 0 ) {
                ReturnInteger ri = (ReturnInteger)warningTable.get( test );
                return( ri.getValue() );
            }
        }
        return( 0 );
    }
}
