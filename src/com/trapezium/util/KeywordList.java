package com.trapezium.util;

import java.util.Vector;

public class KeywordList {
    Vector kList;
    int kSize;

    public KeywordList() {
    }

    public void addElement( String s ) {
        if ( kList == null ) {
            kList = new Vector();
        }
        kList.addElement( s );
        kSize++;
    }

    String kElement;
    void setKelement( int offset ) {
        kElement = (String)kList.elementAt( offset );
    }

    public boolean find( ByteString s, int offset, int size ) {
	    for ( int i = 0; i < kSize; i++ ) {
	        setKelement( i );
	        if ( kElement.length() != size ) {
	            continue;
	        }
	        boolean foundk1 = true;
            for ( int j = 1; j < size; j++ ) {
                if ( s.charAt( offset + j ) != kElement.charAt( j )) {
                    foundk1 = false;
                    break;
                }
            }
            if ( foundk1 ) {
                return( true );
            }
        }
        return( false );
	}
}
