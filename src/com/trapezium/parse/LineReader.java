/*
 * @(#)LineReader.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.parse;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

/**
 *  Reads lines into memory using JDK1.1 or JDK1.0. 
 *
 *  <P>
 *  First tries JDK1.1, switches to JDK1.0 if NoClassDefFound exception
 *  occurs.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.11, 8 Feb 1998
 */
class LineReader {

    BufferedReader br;
    InputStream inStream;

    public LineReader( InputStream inStream ) {
        this.inStream = inStream;
        try {
    		InputStreamReader irs = new InputStreamReader( inStream );
    		br = new BufferedReader( irs );
    	} catch ( NoClassDefFoundError eee ) {
    	    this.inStream = inStream;
    	}
    }
    
    public String readLine() throws IOException {
        // JDK 1.1
        if ( br != null ) {
            return( br.readLine() );
        // JDK 1.0
        } else {
			// The StringBuffer has the contents of the line
			StringBuffer buf = new StringBuffer();

            //
            // line terminators:
            //  '\r' Unix
            //  '\n' MacOS
            //  '\r\n' Windows
            //
			int x;

			// read the stream until:  end of file, \r or \n
			boolean terminatedWithR = false;
			boolean terminatedWithN = false;
			while (( x = inStream.read() ) != -1 ) {
				if ( x == '\r' ) {
				    terminatedWithR = true;
				    if ( terminatedWithN ) {
				        continue;
				    }
					return( new String( buf ));
				} else if ( x == '\n' ) {
				    terminatedWithN = true;
				    if ( terminatedWithR ) {
				        continue;
				    }
				    return( new String( buf ));
				 } else {
				    terminatedWithR = terminatedWithN = false;
				    buf.append( (char)x );
				}
			}
			if ( buf.length() > 0 ) {
			    return( new String( buf ));
			} else {
			    inStream.close();
			    return( null );
			}
		}
	}
}

