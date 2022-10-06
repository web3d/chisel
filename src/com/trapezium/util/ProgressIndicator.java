/*
 * @(#)ProgressIndicator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 */


package com.trapezium.util;
import java.awt.Color;

/** interface for ProgressBar-like objects */
public interface ProgressIndicator {
    void setText( String s );
    void setTitle( String s );
    String getTitle();
    void setPercent( int n );
    void setAlternatePercent( int n );  // for dual progress indicators
    void setColor( Color color );
    Color getColor();
    void reset();
}
