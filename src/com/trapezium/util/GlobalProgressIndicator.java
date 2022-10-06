/*
 * @(#)GlobalProgressIndicator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 */
package com.trapezium.util;

import java.awt.Color;

/**
 *  This class makes any ProgressIndicator globally available.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.2, 18 June 1998
 *
 *  @since           1.2
 */

public class GlobalProgressIndicator {
    /** The information necessary to track progress */
    static ProgressIndicator progressIndicator = null;
    static int currentItem = 0;
    static int totalItems = 0;
    static int lastPercent = 0;
    static int alternate_currentItem = 0;
    static int alternate_totalItems = 0;
    static int alternate_lastPercent = 0;
    static int unitSize = 0;
    static boolean alternateActive = false;
    
    /** a global abort flag, to abort progress */
    static public boolean abortCurrentProcess = false;

    /** The following three are used to estimate progress range in situations
     *  where the actual range is not known.  This is used for cases where
     *  vertices and faces are known, but edges aren't (V+F=E+2 for singly
     *  connected structures)
     */
    static public void setUnitSize( int size, int factor ) {
        unitSize = size;
        totalItems = size*factor;
    }
    
    static public void replaceUnit( int size ) {
        totalItems -= unitSize;
        totalItems += size;
    }

    static public void replaceHalfUnit( int size ) {
        totalItems -= unitSize/2;
        totalItems += size;
    }
    
    /** Used when remaining progress units are known, in a previously unknown
     *  progress range.
     */
    static public void endGame( int remaining ) {
        totalItems = currentItem + remaining;
    }
    
    /** Assign a global progress indicator.
     *
     *  @param pi the ProgressIndicator that becomes globally available
     *  @param title the String title that goes with the percent indication
     *  @param total the total number of items in the progress indicator.
     */
    static public void setProgressIndicator( ProgressIndicator pi, String title, int total ) {
        progressIndicator = pi;
        if ( progressIndicator != null ) {
            progressIndicator.setTitle( title );
        }
        totalItems = total;
        currentItem = 0;
        lastPercent = 0;
        if ( pi != null ) {
            pi.reset();
        }
    }
    
    static public void activateAlternateProgressIndicator( int total ) {
        alternateActive = true;
        alternate_totalItems = totalItems;
        alternate_currentItem = currentItem;
        alternate_lastPercent = lastPercent;
        if ( progressIndicator != null ) {
            progressIndicator.setColor( Color.blue );
        }
        totalItems = total;
        currentItem = 0;
        lastPercent = 0;
    }
    
    static public void deactivateAlternateProgressIndicator() {
        alternateActive = false;
        totalItems = alternate_totalItems;
        currentItem = alternate_currentItem;
        lastPercent = alternate_lastPercent;
    }

    /** Mark progress, called once for each item in the total */
    static public void markProgress() {
        if (( progressIndicator != null ) && ( totalItems > 0 )) {
            currentItem++;
            int percent = currentItem*100/totalItems;
            if ( percent != lastPercent ) {
                if ( alternateActive ) {
                    if ( totalItems > 300 )
                    progressIndicator.setAlternatePercent( percent );
                } else {
                    progressIndicator.setPercent( percent );
                }
                lastPercent = percent;
            }
        }
    }
    
    
    static public void incProgress( int itemCount ) {
        currentItem += itemCount;
        markProgress( currentItem );
    }
    
    static public void markProgress( int item ) {
        if (( progressIndicator != null ) && ( totalItems > 0 )) {
            currentItem = item;
            int percent = currentItem*100/totalItems;
            if ( percent != lastPercent ) {
                progressIndicator.setPercent( percent );
                lastPercent = percent;
            }
        }
    }
 
    /** Reset the progress indicator */
    static public void reset() {
        if ( progressIndicator != null ) {
            progressIndicator.reset();
        }
    }
}
