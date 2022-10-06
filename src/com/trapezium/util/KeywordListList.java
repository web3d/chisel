package com.trapezium.util;

public class KeywordListList {
    KeywordList[] capList = new KeywordList[26];
    KeywordList[] smallList = new KeywordList[26];

    public void add( String s ) {
        char c = s.charAt( 0 );
         if (( c >= 'A' ) && ( c <= 'Z' )) {
            int capOffset = c - 'A';
            if ( capList[ capOffset ] == null ) {
                capList[ capOffset ] = new KeywordList();
            }
            capList[ capOffset ].addElement( s );
        } else if (( c >= 'a' ) && ( c <= 'z' )) {
            int smallOffset = c - 'a';
            if ( smallList[ smallOffset ] == null ) {
                smallList[ smallOffset ] = new KeywordList();
            }
            smallList[ smallOffset ].addElement( s );
        }
    }

    public KeywordList getCapList( int offset ) {
        if ( offset < 0 ) {
            return( null );
        } else if ( offset >= 26 ) {
            return( null );
        } else {
            return( capList[ offset ] );
        }
    }

    public KeywordList getSmallList( int offset ) {
        if ( offset < 0 ) {
            return( null );
        } else if ( offset >= 26 ) {
            return( null );
        } else {
            return( smallList[ offset ] );
        }
    }
}

 
